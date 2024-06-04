package search.mcts;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;

import game.Game;
import game.types.state.GameType;
import main.DaemonThreadFactory;
import main.collections.FVector;
import main.collections.FastArrayList;
import main.math.statistics.IncrementalStats;
import metadata.ai.features.Features;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilitySimple;
import other.AI;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import other.state.State;
import other.trial.Trial;
import policies.Policy;
import policies.softmax.SoftmaxFromMetadataPlayout;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicyLinear;
import policies.softmax.SoftmaxPolicyLogitTree;
import search.mcts.backpropagation.AlphaGoBackprop;
import search.mcts.backpropagation.BackpropagationStrategy;
import search.mcts.backpropagation.HeuristicBackprop;
import search.mcts.backpropagation.MonteCarloBackprop;
import search.mcts.backpropagation.QualitativeBonus;
import search.mcts.finalmoveselection.FinalMoveSelectionStrategy;
import search.mcts.finalmoveselection.MaxAvgScore;
import search.mcts.finalmoveselection.ProportionalExpVisitCount;
import search.mcts.finalmoveselection.RobustChild;
import search.mcts.nodes.BaseNode;
import search.mcts.nodes.OpenLoopNode;
import search.mcts.nodes.ScoreBoundsNode;
import search.mcts.nodes.StandardNode;
import search.mcts.playout.HeuristicSampingPlayout;
import search.mcts.playout.PlayoutStrategy;
import search.mcts.playout.RandomPlayout;
import search.mcts.selection.AG0Selection;
import search.mcts.selection.NoisyAG0Selection;
import search.mcts.selection.ProgressiveBias;
import search.mcts.selection.ProgressiveHistory;
import search.mcts.selection.SelectionStrategy;
import search.mcts.selection.UCB1;
import search.mcts.selection.UCB1GRAVE;
import search.mcts.selection.UCB1Tuned;
import training.expert_iteration.ExItExperience;
import training.expert_iteration.ExpertPolicy;
import utils.AIUtils;

/**
 * A modular implementation of Monte-Carlo Tree Search (MCTS) for playing games
 * in Ludii.
 * 
 * @author Dennis Soemers
 */
public class MCTS extends ExpertPolicy
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Different strategies for initializing Q(s, a) values (or V(s) values of
	 * nodes)
	 * 
	 * @author Dennis Soemers
	 */
	public static enum QInit
	{
		/** 
		 * Give unvisited nodes a very large value 
		 * (actually 10,000 rather than infinity) 
		 */
		INF,
		
		/** 
		 * Estimate the value of unvisited nodes as a loss (-1). This is a 
		 * highly pessimistic value for unvisited nodes, and causes us to rely 
		 * much more on prior distribution. Word on the street is that DeepMind
		 * does this in Alpha(Go) Zero.
		 */
		LOSS,
		
		/** 
		 * Estimate the value of unvisited nodes as a draw (0.0). This causes
		 * us to prioritise empirical wins over unvisited nodes.
		 */
		DRAW,
		
		/**
		 * Estimate the value of unvisited nodes as a win (1). Very similar to
		 * INF, just a bit less extreme.
		 */
		WIN,
		
		/**
		 * Estimate the value of unvisited nodes as the value estimate of the
		 * parent (with corrections for mover).
		 */
		PARENT,
	}
	
	//-------------------------------------------------------------------------
	
	// Flags for things we want to do when expanding a node
	
	/** Compute a heuristic-based value estimate for expanded nodes */
	public final static int HEURISTIC_INIT				= 0x1;
	
	//-------------------------------------------------------------------------
	
	// Basic members of MCTS
	
	/** Root node of the last search process */
	protected volatile BaseNode rootNode = null;
	
	/** Implementation of Selection phase */
	protected SelectionStrategy selectionStrategy;
	
	/** Implementation of Play-out phase */
	protected PlayoutStrategy playoutStrategy;
	
	/** Implementation of Backpropagation of results through the tree */
	protected BackpropagationStrategy backpropagationStrategy;
	
	/** Algorithm to select move to play in the "real" game after searching */
	protected FinalMoveSelectionStrategy finalMoveSelectionStrategy;
	
	/** Strategy for init of Q-values for unvisited nodes. */
	protected QInit qInit = QInit.PARENT;
	
	/** Flags indicating what data needs to be backpropagated */
	protected int backpropFlags = 0;
	
	/** Flags indicating things we want to do when expanding a node */
	protected int expansionFlags = 0;
	
	/** We'll automatically return our move after at most this number of seconds if we only have one move */
	protected double autoPlaySeconds = 0.0;	// TODO allow customisation
	
	/** Our thread pool for tree parallelisation */
	private ExecutorService threadPool = null;
	
	/** Number of threads this MCTS should use for parallel iterations */
	private int numThreads = 1;
	
	/** Lets us track whether all threads in our thread pool have completely finished */
	private AtomicInteger numThreadsBusy = new AtomicInteger(0);
	
	//-------------------------------------------------------------------------
	
	/** State flags of the game we're currently playing */
	protected long currentGameFlags = 0;
	
	/** 
	 * We'll memorise the number of iterations we have executed in our last 
	 * search here 
	 */
	protected int lastNumMctsIterations = -1;
	
	/**
	 * We'll memorise the number of actions we have executed in play-outs
	 * during our last search here
	 */
	protected int lastNumPlayoutActions = -1;
	
	/**
	 * Value estimate of the last move we returned
	 */
	protected double lastReturnedMoveValueEst = 0.0;
	
	/** String to print to Analysis tab of the Ludii app */
	protected String analysisReport = null;
	
	/** 
	 * If true, we preserve our root node after running search. Will increase memory usage,
	 * but allows us to use it to access data afterwards (for instance for training algorithms)
	 */
	protected boolean preserveRootNode = false;
	
	//-------------------------------------------------------------------------
	
	// Following members are related to and/or required because of Tree Reuse
	
	/** 
	 * Whether or not to reuse trees generated in previous 
	 * searches in the same game 
	 */
	protected boolean treeReuse = true;
	
	/** 
	 * Need to memorise this such that we know which parts of the tree to 
	 * traverse to before starting Tree Reuse 
	 */
	protected int lastActionHistorySize = 0;
	
	/** Decay factor for global action statistics when reusing trees */
	protected final double globalActionDecayFactor = 0.6;
	
	//-------------------------------------------------------------------------
	
	/** A learned policy to use in Selection phase */
	protected Policy learnedSelectionPolicy = null;
	
	/** Do we want to load heuristics from metadata on init? */
	protected boolean wantsMetadataHeuristics = false;
	
	/** Do we want to track pessimistic and optimistic score bounds in nodes, for solving? */
	protected boolean useScoreBounds = false;
	
	/** 
	 * If we have heuristic value estimates in nodes, we assign this weight to playout outcomes, 
	 * and 1 minus this weight to the value estimate of node before playout.
	 * 
	 * TODO can move this into the AlphaGoBackprop class I think
	 * 
	 * 1.0 --> normal MCTS
	 * 0.5 --> AlphaGo
	 * 0.0 --> AlphaGo Zero
	 */
	protected double playoutValueWeight = 1.0;
	
	//-------------------------------------------------------------------------
	
	/** Table of global (MCTS-wide) action stats (e.g., for Progressive History) */
    protected final Map<MoveKey, ActionStatistics> globalActionStats;
    
    /** Table of global (MCTS-wide) N-gram action stats (e.g., for NST) */
    protected final Map<NGramMoveKey, ActionStatistics> globalNGramActionStats;
    
    /** Max length of N-grams of actions we consider */
    protected final int maxNGramLength;
    
    /** For every player, a global MCTS-wide tracker of statistics on heuristics */
    protected IncrementalStats[] heuristicStats = null;
    
    //-------------------------------------------------------------------------
    
    /**
     * Global flag telling us whether we want MCTS objects to null (clear) undo
     * data in Trial objects stored in their nodes. True by default, since
     * usually we want to do this to reduce memory usage.
     * 
     * Sometimes in self-play training this causes issues though, and there
     * we typically don't worry about the memory usage anyway since we tend
     * to have rather short and shallow searches, so we can set this to false.
     */
    public static boolean NULL_UNDO_DATA = true;
    
    //-------------------------------------------------------------------------
	
	/** 
	 * Creates standard UCT algorithm, with exploration constant = sqrt(2.0)
	 * @return UCT agent
	 */
	public static MCTS createUCT()
	{
		return createUCT(Math.sqrt(2.0));
	}
	
	/**
	 * Creates standard UCT algorithm with parameter for 
	 * UCB1's exploration constant
	 * 
	 * @param explorationConstant
	 * @return UCT agent
	 */
	public static MCTS createUCT(final double explorationConstant)
	{
		final MCTS uct = 
				new MCTS
				(
					new UCB1(explorationConstant), 
					new RandomPlayout(200),
					new MonteCarloBackprop(),
					new RobustChild()
				);
		
		uct.friendlyName = "UCT";
		
		return uct;
	}
	
	/**
	 * Creates a Biased MCTS agent which attempts to use features and
	 * weights embedded in a game's metadata file.
	 * @param epsilon Epsilon for epsilon-greedy feature-based playouts. 1 for uniform, 0 for always softmax
	 * @return Biased MCTS agent
	 */
	public static MCTS createBiasedMCTS(final double epsilon)
	{
		final MCTS mcts = 
				new MCTS
				(
					new NoisyAG0Selection(), 
					epsilon < 1.0 ? new SoftmaxFromMetadataPlayout(epsilon) : new RandomPlayout(200),
					new MonteCarloBackprop(),
					new RobustChild()
				);
		
		mcts.setQInit(QInit.WIN);
		mcts.setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(epsilon));
		mcts.friendlyName = epsilon < 1.0 ? "Biased MCTS" : "Biased MCTS (Uniform Playouts)";
		
		return mcts;
	}
	
	/**
	 * Creates a Biased MCTS agent using given collection of features
	 * 
	 * @param features
	 * @param epsilon Epsilon for epsilon-greedy feature-based playouts. 1 for uniform, 0 for always softmax
	 * @return Biased MCTS agent
	 */
	public static MCTS createBiasedMCTS(final Features features, final double epsilon)
	{
		final MCTS mcts = 
				new MCTS
				(
					new NoisyAG0Selection(), 
					epsilon < 1.0 ? SoftmaxPolicyLinear.constructPlayoutPolicy(features, epsilon) : new RandomPlayout(200),
					new MonteCarloBackprop(),
					new RobustChild()
				);
		
		mcts.setQInit(QInit.WIN);
		mcts.setLearnedSelectionPolicy(SoftmaxPolicyLinear.constructSelectionPolicy(features, epsilon));
		mcts.friendlyName = epsilon < 1.0 ? "Biased MCTS" : "Biased MCTS (Uniform Playouts)";
		
		return mcts;
	}
	
	/**
	 * Creates a Hybrid MCTS agent which attempts to use heuristics in a game's metadata file.
	 * @return Hybrid MCTS agent
	 */
	public static MCTS createHybridMCTS()
	{
		final MCTS mcts = 
				new MCTS
				(
					new UCB1(Math.sqrt(2.0)), 
					new HeuristicSampingPlayout(),
					new AlphaGoBackprop(),
					new RobustChild()
				);

		mcts.setWantsMetadataHeuristics(true);
		mcts.setPlayoutValueWeight(0.5);
		mcts.friendlyName = "MCTS (Hybrid Selection)";
		return mcts;
	}
	
	/**
	 * Creates a Bandit Tree Search using heuristic to guide the search but no playout.
	 * @return Bandit Tree Search agent
	 */
	public static MCTS createBanditTreeSearch()
	{
		final MCTS mcts = 
				new MCTS
				(
					new UCB1(Math.sqrt(2.0)), 
					new RandomPlayout(0),
					new AlphaGoBackprop(),
					new RobustChild()
				);

		mcts.setWantsMetadataHeuristics(true);
		mcts.setPlayoutValueWeight(0.0);
		mcts.friendlyName = "Bandit Tree Search (Avg)";
		return mcts;
	}
	
	/**
	 * Creates a Policy-Value Tree Search agent, using features for policy and heuristics
	 * for value function.
	 * 
	 * @param features
	 * @param heuristics
	 * @return Policy-Value Tree Search agent
	 */
	public static MCTS createPVTS(final Features features, final Heuristics heuristics)
	{
		final MCTS mcts = 
				new MCTS
				(
					new NoisyAG0Selection(), 
					new RandomPlayout(0),
					new AlphaGoBackprop(),
					new RobustChild()
				);
		
		mcts.setLearnedSelectionPolicy(SoftmaxPolicyLinear.constructSelectionPolicy(features, 0.0));
		mcts.setPlayoutValueWeight(0.0);
		mcts.setWantsMetadataHeuristics(false);
		mcts.setHeuristics(heuristics);
		mcts.friendlyName = "PVTS";
		
		return mcts;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor with arguments for all strategies
	 * @param selectionStrategy
	 * @param playoutStrategy
	 * @param finalMoveSelectionStrategy
	 */
	public MCTS
	(
		final SelectionStrategy selectionStrategy, 
		final PlayoutStrategy playoutStrategy, 
		final BackpropagationStrategy backpropagationStrategy,
		final FinalMoveSelectionStrategy finalMoveSelectionStrategy
	)
	{
		this.selectionStrategy = selectionStrategy;
		this.playoutStrategy = playoutStrategy;
		this.backpropagationStrategy = backpropagationStrategy;
		
		backpropFlags = selectionStrategy.backpropFlags() | playoutStrategy.backpropFlags();
		expansionFlags = selectionStrategy.expansionFlags();
		
		this.backpropagationStrategy.setBackpropFlags(backpropFlags);
		backpropFlags = backpropFlags | this.backpropagationStrategy.backpropagationFlags();
		
		this.finalMoveSelectionStrategy = finalMoveSelectionStrategy;
		
		if ((backpropFlags & BackpropagationStrategy.GLOBAL_ACTION_STATS) != 0)
			globalActionStats = new ConcurrentHashMap<MoveKey, ActionStatistics>();
		else
			globalActionStats = null;
		
		if ((backpropFlags & BackpropagationStrategy.GLOBAL_NGRAM_ACTION_STATS) != 0)
		{
			globalNGramActionStats = new ConcurrentHashMap<NGramMoveKey, ActionStatistics>();
			maxNGramLength = 3; 	// Hardcoded to 3 for now, should make it a param...
		}
		else
		{
			globalNGramActionStats = null;
			maxNGramLength = 0;
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Move selectAction
	(
		final Game game, 
		final Context context, 
		final double maxSeconds,
		final int maxIterations,
		final int maxDepth
	)
	{
		final long startTime = System.currentTimeMillis();
		long stopTime = (maxSeconds > 0.0) ? startTime + (long) (maxSeconds * 1000) : Long.MAX_VALUE;
		final int maxIts = (maxIterations >= 0) ? maxIterations : Integer.MAX_VALUE;
		
		while (numThreadsBusy.get() != 0 && System.currentTimeMillis() < Math.min(stopTime, startTime + 1000L))
		{
			// Give threads in thread pool some more time to clean up after themselves from previous iteration
		}
		
		// We'll assume all threads are really done now and just reset to 0
		numThreadsBusy.set(0);
				
		final AtomicInteger numIterations = new AtomicInteger();
		
		// Find or create root node
		if (treeReuse && rootNode != null)
		{
			// Want to reuse part of existing search tree
			
			// Need to traverse parts of old tree corresponding to 
			// actions played in the real game
			final List<Move> actionHistory = context.trial().generateCompleteMovesList();
			int offsetActionToTraverse = actionHistory.size() - lastActionHistorySize;
			
			if (offsetActionToTraverse < 0)
			{
				// Something strange happened, probably forgot to call
				// initAI() for a newly-started game. Won't be a good
				// idea to reuse tree anyway
				rootNode = null;
			}
			
			while (offsetActionToTraverse > 0)
			{
				final Move move = actionHistory.get(actionHistory.size() - offsetActionToTraverse);
				rootNode = rootNode.findChildForMove(move);
				
				if (rootNode == null)
				{
					// Didn't have a node in tree corresponding to action 
					// played, so can't reuse tree
					break;
				}
								
				--offsetActionToTraverse;
			}
		}
		
		if (rootNode == null || !treeReuse)	
		{
			// Need to create a fresh root
			rootNode = createNode(this, null, null, null, context);
			//System.out.println("NO TREE REUSE");
		}
		else
		{
			//System.out.println("successful tree reuse");
			
			// We're reusing a part of previous search tree
			// Clean up unused parts of search tree from memory
			rootNode.setParent(null);
					
			// TODO in nondeterministic games + OpenLoop MCTS, we'll want to 
			// decay statistics gathered in the entire subtree here
		}
		
		if (heuristicStats != null)
		{
			// Clear all heuristic stats
			for (int p = 1; p < heuristicStats.length; ++p)
			{
				heuristicStats[p].init(0, 0.0, 0.0);
			}
		}
		
		rootNode.rootInit(context);
		
		if (rootNode.numLegalMoves() == 1)
		{
			// play faster if we only have one move available anyway
			if (autoPlaySeconds >= 0.0 && autoPlaySeconds < maxSeconds)
				stopTime = startTime + (long) (autoPlaySeconds * 1000);
		}
		
		lastActionHistorySize = context.trial().numMoves();
		
		lastNumPlayoutActions = 0;	// TODO if this variable actually becomes important, may want to make it Atomic
		
		// Store this in a separate variable because threading weirdness sometimes sets the class variable to null
		// even though some threads here still want to do something with it.
		final BaseNode rootThisCall = rootNode;
		
		// For each thread, queue up a job
		final CountDownLatch latch = new CountDownLatch(numThreads);
		final long finalStopTime = stopTime;	// Need this to be final for use in inner lambda
		for (int thread = 0; thread < numThreads; ++thread)
		{
			threadPool.submit
			(
				() -> 
				{
					try
					{
						numThreadsBusy.incrementAndGet();
						
						// Search until we have to stop
						while (numIterations.get() < maxIts && System.currentTimeMillis() < finalStopTime && !wantsInterrupt)
						{
							/*********************
								Selection Phase
							*********************/
							BaseNode current = rootThisCall;
							current.addVirtualVisit();
							current.startNewIteration(context);
							
							Context playoutContext = null;
							
							while (current.contextRef().trial().status() == null)
							{
								BaseNode prevNode = current;
								prevNode.getLock().lock();

								try
								{
									final int selectedIdx = selectionStrategy.select(this, current);
									BaseNode nextNode = current.childForNthLegalMove(selectedIdx);
									
									final Context newContext = current.traverse(selectedIdx);
									
									if (nextNode == null)
									{
										/*********************
												Expand
										 *********************/
										nextNode = 
												createNode
												(
													this, 
													current, 
													newContext.trial().lastMove(), 
													current.nthLegalMove(selectedIdx), 
													newContext
												);
										
										current.addChild(nextNode, selectedIdx);
										current = nextNode;
										current.addVirtualVisit();
										current.updateContextRef();
										
										if ((expansionFlags & HEURISTIC_INIT) != 0)
										{
											assert (heuristicFunction != null);
											nextNode.setHeuristicValueEstimates
											(
												AIUtils.heuristicValueEstimates(nextNode.playoutContext(), heuristicFunction)
											);
										}
										
										playoutContext = current.playoutContext();
										
										break;	// stop Selection phase
									}
									
									current = nextNode;
									current.addVirtualVisit();
									current.updateContextRef();
								}
								catch (final ArrayIndexOutOfBoundsException e)
								{
									System.err.println(describeMCTS());
									throw e;
								}
								finally
								{
									prevNode.getLock().unlock();
								}
							}
							
							Trial endTrial = current.contextRef().trial();
							int numPlayoutActions = 0;
							
							if (!endTrial.over() && playoutValueWeight > 0.0)
							{
								// Did not reach a terminal game state yet
								
								/********************************
											Play-out
								 ********************************/
								
								final int numActionsBeforePlayout = current.contextRef().trial().numMoves();
								
								endTrial = playoutStrategy.runPlayout(this, playoutContext);
								numPlayoutActions = (endTrial.numMoves() - numActionsBeforePlayout);
								
								lastNumPlayoutActions += 
										(playoutContext.trial().numMoves() - numActionsBeforePlayout);
							}
							else
							{
								// Reached a terminal game state
								playoutContext = current.contextRef();
							}
							
							/***************************
								Backpropagation Phase
							 ***************************/
							final double[] outcome = RankUtils.agentUtilities(playoutContext);
							backpropagationStrategy.update(this, current, playoutContext, outcome, numPlayoutActions);
							
							numIterations.incrementAndGet();
						}
						
						rootThisCall.cleanThreadLocals();
					}
					catch (final Exception e)
					{
						System.err.println("MCTS error in game: " + context.game().name());
						e.printStackTrace();	// Need to do this here since we don't retrieve runnable's Future result
					}
					finally
					{
						numThreadsBusy.decrementAndGet();
						latch.countDown();
					}
				}
			);
		}
		
		try
		{
			latch.await(stopTime - startTime + 2000L, TimeUnit.MILLISECONDS);
		}
		catch (final InterruptedException e)
		{
			e.printStackTrace();
		}

		lastNumMctsIterations = numIterations.get();
		
		final Move returnMove = finalMoveSelectionStrategy.selectMove(this, rootThisCall);
		int playedChildIdx = -1;
		
		if (!wantsInterrupt)
		{
			int moveVisits = -1;
			
			for (int i = 0; i < rootThisCall.numLegalMoves(); ++i)
			{
				final BaseNode child = rootThisCall.childForNthLegalMove(i);
	
				if (child != null)
				{
					if (rootThisCall.nthLegalMove(i).equals(returnMove))
					{
						final State state = rootThisCall.deterministicContextRef().state();
				        final int moverAgent = state.playerToAgent(state.mover());
						moveVisits = child.numVisits();
						lastReturnedMoveValueEst = child.expectedScore(moverAgent);
						playedChildIdx = i;
						
						break;
					}
				}
			}
			
			final int numRootIts = rootThisCall.numVisits();
			
			analysisReport = 
					friendlyName + 
					" made move after " +
					numRootIts +
					" iterations (selected child visits = " +
					moveVisits +
					", value = " +
					lastReturnedMoveValueEst +
					").";
		}
		else
		{
			analysisReport = null;
		}
		
		// We can already try to clean up a bit of memory here
		// NOTE: from this point on we have to use rootNode instead of rootThisCall again!
		if (!preserveRootNode)
		{
			if (!treeReuse)
			{
				rootNode = null;	// clean up entire search tree
			}
			else if (!wantsInterrupt)	// only clean up if we didn't pause the AI / interrupt it
			{
				if (playedChildIdx >= 0)
					rootNode = rootThisCall.childForNthLegalMove(playedChildIdx);
				else
					rootNode = null;
				
				if (rootNode != null)
				{
					rootNode.setParent(null);
					++lastActionHistorySize;
				}
			}
		}
		
		if (globalActionStats != null)
		{
			if (!treeReuse)
			{
				// Completely clear statistics if we're not reusing the tree
				globalActionStats.clear();
			}
			else
			{
				// Otherwise, decay statistics
				final Set<Entry<MoveKey, ActionStatistics>> entries = globalActionStats.entrySet();
				final Iterator<Entry<MoveKey, ActionStatistics>> it = entries.iterator();
				
				while (it.hasNext())
				{
					final Entry<MoveKey, ActionStatistics> entry = it.next();
					final ActionStatistics stats = entry.getValue();
					stats.visitCount *= globalActionDecayFactor;
					
					if (stats.visitCount < 10.f)
						it.remove();
					else
						stats.accumulatedScore *= globalActionDecayFactor;
				}
			}
		}
		
		if (globalNGramActionStats != null)
		{
			
			if (!treeReuse)
			{
				// Completely clear statistics if we're not reusing the tree
				globalNGramActionStats.clear();
			}
			else
			{
				// Otherwise, decay statistics
				final Set<Entry<NGramMoveKey, ActionStatistics>> entries = globalNGramActionStats.entrySet();
				final Iterator<Entry<NGramMoveKey, ActionStatistics>> it = entries.iterator();
				
				while (it.hasNext())
				{
					final Entry<NGramMoveKey, ActionStatistics> entry = it.next();
					final ActionStatistics stats = entry.getValue();
					stats.visitCount *= globalActionDecayFactor;
					
					if (stats.visitCount < 10.f)
						it.remove();
					else
						stats.accumulatedScore *= globalActionDecayFactor;
				}
			}
		}
		
		//System.out.println(numIterations + " MCTS iterations");
		return returnMove;
	}
	
	/**
	 * @param mcts
	 * @param parent
	 * @param parentMove
	 * @param parentMoveWithoutConseq
	 * @param context
	 * @return New node
	 */
	protected BaseNode createNode
	(
		final MCTS mcts, 
    	final BaseNode parent, 
    	final Move parentMove, 
    	final Move parentMoveWithoutConseq,
    	final Context context
    )
	{
		if ((currentGameFlags & GameType.Stochastic) == 0L || wantsCheatRNG())
		{
			if (useScoreBounds)
				return new ScoreBoundsNode(mcts, parent, parentMove, parentMoveWithoutConseq, context);
			else
				return new StandardNode(mcts, parent, parentMove, parentMoveWithoutConseq, context);
		}
		else
		{
			return new OpenLoopNode(mcts, parent, parentMove, parentMoveWithoutConseq, context.game());
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Sets number of seconds after which we auto-play if we only have one legal move.
	 * @param seconds
	 */
	public void setAutoPlaySeconds(final double seconds)
	{
		autoPlaySeconds = seconds;
	}
	
	/**
	 * Set whether or not to reuse tree from previous search processes
	 * @param treeReuse
	 */
	public void setTreeReuse(final boolean treeReuse)
	{
		this.treeReuse = treeReuse;
	}
	
	/**
	 * Set the number of threads to use for Tree Parallelisation
	 * @param numThreads
	 */
	public void setNumThreads(final int numThreads)
	{
		this.numThreads = numThreads;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Flags indicating what data we need to backpropagate
	 */
	public int backpropFlags()
	{
		return backpropFlags;
	}
	
	/**
	 * @return Learned (linear or tree) policy for Selection phase
	 */
	public Policy learnedSelectionPolicy()
	{
		return learnedSelectionPolicy;
	}
	
	/**
	 * @return Max length of N-grams of actions for which we collect statistics
	 */
	public int maxNGramLength()
	{
		return maxNGramLength;
	}
	
	/**
	 * @return Heuristics used by MCTS
	 */
	public Heuristics heuristics()
	{
		return heuristicFunction;
	}
	
	/**
	 * @return Play-out strategy used by this MCTS object
	 */
	public PlayoutStrategy playoutStrategy()
	{
		return playoutStrategy;
	}
	
	/**
	 * @return Init strategy for Q-values of unvisited nodes
	 */
	public QInit qInit()
	{
		return qInit;
	}
	
	/**
	 * @return Current root node
	 */
	public BaseNode rootNode()
	{
		return rootNode;
	}
	
	/**
	 * Sets the learned policy to use in Selection phase
	 * @param policy The policy.
	 */
	public void setLearnedSelectionPolicy(final Policy policy)
	{
		learnedSelectionPolicy = policy;
	}
	
	/**
	 * Sets whether we want to load heuristics from metadata
	 * @param val The value.
	 */
	public void setWantsMetadataHeuristics(final boolean val)
	{
		wantsMetadataHeuristics = val;
	}
	
	/**
	 * Sets whether we want to use pessimistic and optimistic score bounds for solving nodes
	 * @param val
	 */
	public void setUseScoreBounds(final boolean val)
	{
		useScoreBounds = val;
	}
	
	/**
	 * Sets the Q-init strategy
	 * @param init
	 */
	public void setQInit(final QInit init)
	{
		qInit = init;
	}
	
	/**
	 * Sets whether we want to preserve root node after running search
	 * @param preserveRootNode
	 */
	public void setPreserveRootNode(final boolean preserveRootNode)
	{
		this.preserveRootNode = preserveRootNode;
	}
	
	/**
	 * Sets the weight to use for playout value estimates
	 * @param playoutValueWeight
	 */
	public void setPlayoutValueWeight(final double playoutValueWeight)
	{
		if (playoutValueWeight < 0.0)
		{
			this.playoutValueWeight = 0.0;
			System.err.println("MCTS playoutValueWeight cannot be lower than 0.0!");
		}
		else if (playoutValueWeight > 1.0)
		{
			this.playoutValueWeight = 1.0;
			System.err.println("MCTS playoutValueWeight cannot be greater than 1.0!");
		}
		else
		{
			this.playoutValueWeight = playoutValueWeight;
		}
		
		if (this.playoutValueWeight < 1.0)		// We'll need heuristic values in nodes
			expansionFlags = expansionFlags | HEURISTIC_INIT;
	}
	
	/** 
	 * If we have heuristic value estimates in nodes, we assign this weight to playout outcomes, 
	 * and 1 minus this weight to the value estimate of node before playout.
	 * 
	 * 1.0 --> normal MCTS
	 * 0.5 --> AlphaGo
	 * 0.0 --> AlphaGo Zero
	 * 
	 * @return The weight
	 */
	public double playoutValueWeight()	// TODO probably this should become a property of AlphaGoBackprop
	{
		return playoutValueWeight;
	}
	
	/**
	 * @return Array of incremental stat trackers for heuristics (one per player)
	 */
	public IncrementalStats[] heuristicStats()
	{
		return heuristicStats;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Number of MCTS iterations performed during our last search
	 */
	public int getNumMctsIterations()
	{
		return lastNumMctsIterations;
	}
	
	/**
	 * @return Number of actions executed in play-outs during our last search
	 */
	public int getNumPlayoutActions()
	{
		return lastNumPlayoutActions;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean usesFeatures(final Game game)
	{
		return (learnedSelectionPolicy != null || playoutStrategy instanceof SoftmaxPolicyLinear);
	}
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		// Store state flags
		currentGameFlags = game.gameFlags();
		
		// Reset counters
		lastNumMctsIterations = -1;
		lastNumPlayoutActions = -1;
		
		// Reset tree reuse stuff
		rootNode = null;
		lastActionHistorySize = 0;
		
		// Instantiate feature sets for selection policy
		if (learnedSelectionPolicy != null)
		{
			learnedSelectionPolicy.initAI(game, playerID);
		}
		
		// May also have to instantiate feature sets for Playout policy if it doubles as an AI
		if (playoutStrategy instanceof AI)
		{
			if (playoutStrategy != learnedSelectionPolicy)
			{
				final AI aiPlayout = (AI) playoutStrategy;
				aiPlayout.initAI(game, playerID);
			}
		}
		
		// Init heuristics
		if (wantsMetadataHeuristics)
		{
			// Read heuristics from game metadata
			final metadata.ai.Ai aiMetadata = game.metadata().ai();
			if (aiMetadata != null && aiMetadata.heuristics() != null)
			{
				heuristicFunction = Heuristics.copy(aiMetadata.heuristics());
			}
			else
			{
				// construct default heuristic
				heuristicFunction = 
						new Heuristics
						(
							new HeuristicTerm[]
							{
								new Material(null, Float.valueOf(1.f), null, null),
								new MobilitySimple(null, Float.valueOf(0.001f))
							}
						);
			}
		}
		
		if (heuristicFunction != null)
			heuristicFunction.init(game);
		
		// Reset visualisation stuff
		lastReturnedMoveValueEst = 0.0;
		analysisReport = null;
		
		// Completely clear any global action statistics
		if (globalActionStats != null)	
			globalActionStats.clear();
		if (globalNGramActionStats != null)
			globalNGramActionStats.clear();
		
		if ((backpropFlags & BackpropagationStrategy.GLOBAL_HEURISTIC_STATS) != 0)
		{
			heuristicStats = new IncrementalStats[game.players().count() + 1];
			for (int p = 1; p < heuristicStats.length; ++p)
			{
				heuristicStats[p] = new IncrementalStats();
			}
		}
		else
		{
			heuristicStats = null;
		}
		
		if (threadPool != null)
			threadPool.shutdownNow();
		
		threadPool = Executors.newFixedThreadPool(numThreads, DaemonThreadFactory.INSTANCE);
	}
	
	@Override
	public void closeAI()
	{
		// This may help to clean up some memory
		rootNode = null;
		
		// Close trained selection policy
		if (learnedSelectionPolicy != null)
		{
			learnedSelectionPolicy.closeAI();
		}
		
		// May also have to close Playout policy if it doubles as an AI
		if (playoutStrategy instanceof AI)
		{
			if (playoutStrategy != learnedSelectionPolicy)
			{
				final AI aiPlayout = (AI) playoutStrategy;
				aiPlayout.closeAI();
			}
		}
		
		if (threadPool != null)
		{
			threadPool.shutdownNow();
			try
			{
				threadPool.awaitTermination(200L, TimeUnit.MILLISECONDS);
			} 
			catch (final InterruptedException e)
			{
				e.printStackTrace();
			}
			threadPool = null;
		}
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		final long gameFlags = game.gameFlags();
		
		// this MCTS implementation does not support simultaneous-move games
		if ((gameFlags & GameType.Simultaneous) != 0L)
			return false;
		
		if (learnedSelectionPolicy != null && !learnedSelectionPolicy.supportsGame(game))
			return false;
		
		return playoutStrategy.playoutSupportsGame(game);
	}
	
	@Override
	public double estimateValue()
	{
		return lastReturnedMoveValueEst;
	}
	
	@Override
	public String generateAnalysisReport()
	{
		return analysisReport;
	}
	
	@Override
	public AIVisualisationData aiVisualisationData()
	{
		if (rootNode == null)
			return null;

		if (rootNode.numVisits() == 0)
			return null;
		
		if (rootNode.deterministicContextRef() == null)
			return null;

		final int numChildren = rootNode.numLegalMoves();
		final FVector aiDistribution = new FVector(numChildren);
		final FVector valueEstimates = new FVector(numChildren);
		final FastArrayList<Move> moves = new FastArrayList<>();
		
		final State state = rootNode.deterministicContextRef().state();
		final int moverAgent = state.playerToAgent(state.mover());

		for (int i = 0; i < numChildren; ++i)
		{
			final BaseNode child = rootNode.childForNthLegalMove(i);

			if (child == null)
			{
				aiDistribution.set(i, 0);

				if (rootNode.numVisits() == 0)
					valueEstimates.set(i, 0.f);
				else
					valueEstimates.set(i, (float) rootNode.valueEstimateUnvisitedChildren(moverAgent));
			}
			else
			{
				aiDistribution.set(i, child.numVisits());
				valueEstimates.set(i, (float) child.expectedScore(moverAgent));
			}

			if (valueEstimates.get(i) > 1.f)
				valueEstimates.set(i, 1.f);
			else if (valueEstimates.get(i) < -1.f)
				valueEstimates.set(i, -1.f);

			moves.add(rootNode.nthLegalMove(i));
		}
		
		return new AIVisualisationData(aiDistribution, valueEstimates, moves);
	}
	
	//-------------------------------------------------------------------------
	
	/**
     * @param moveKey
     * @return global MCTS-wide action statistics for given move key
     */
    public ActionStatistics getOrCreateActionStatsEntry(final MoveKey moveKey)
    {
    	ActionStatistics stats = globalActionStats.get(moveKey);
    	
    	if (stats == null)
    	{
    		stats = new ActionStatistics();
    		globalActionStats.put(moveKey, stats);
    		//System.out.println("creating entry for " + moveKey + " in " + this);
    	}
    	
    	return stats;
    }
    
    /**
     * @param nGramMoveKey
     * @return global MCTS-wide N-gram action statistics for given N-gram move key,
     * 	or null if it doesn't exist yet
     */
    public ActionStatistics getNGramActionStatsEntry(final NGramMoveKey nGramMoveKey)
    {
    	return globalNGramActionStats.get(nGramMoveKey);
    }
    
    /**
     * @param nGramMoveKey
     * @return global MCTS-wide N-gram action statistics for given N-gram move key
     */
    public ActionStatistics getOrCreateNGramActionStatsEntry(final NGramMoveKey nGramMoveKey)
    {
    	ActionStatistics stats = globalNGramActionStats.get(nGramMoveKey);
    	
    	if (stats == null)
    	{
    		stats = new ActionStatistics();
    		globalNGramActionStats.put(nGramMoveKey, stats);
    		//System.out.println("creating entry for " + nGramMoveKey + " in " + this);
    	}
    	
    	return stats;
    }
    
    //-------------------------------------------------------------------------
	
	/**
	 * @param json
	 * @return MCTS agent constructed from given JSON object
	 */
	public static MCTS fromJson(final JSONObject json)
	{
		final SelectionStrategy selection = 
				SelectionStrategy.fromJson(json.getJSONObject("selection"));
		final PlayoutStrategy playout = 
				PlayoutStrategy.fromJson(json.getJSONObject("playout"));
		final BackpropagationStrategy backprop =
				BackpropagationStrategy.fromJson(json.getJSONObject("backpropagation"));
		final FinalMoveSelectionStrategy finalMove = 
				FinalMoveSelectionStrategy.fromJson(json.getJSONObject("final_move"));
		final MCTS mcts = new MCTS(selection, playout, backprop, finalMove);
		
		if (json.has("tree_reuse"))
		{
			mcts.setTreeReuse(json.getBoolean("tree_reuse"));
		}

		if (json.has("friendly_name"))
		{
			mcts.friendlyName = json.getString("friendly_name");
		}
		
		return mcts;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public FastArrayList<Move> lastSearchRootMoves()
	{
		return rootNode.movesFromNode();
	}
	
	@Override
	public FVector computeExpertPolicy(final double tau)
	{
		return rootNode.computeVisitCountPolicy(tau);
	}
	
	@Override
	public List<ExItExperience> generateExItExperiences()
	{
		return rootNode.generateExItExperiences();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lines
	 * @return Constructs an MCTS object from instructions in the 
	 * given array of lines
	 */
	public static MCTS fromLines(final String[] lines)
	{
		// Defaults - main parts
		SelectionStrategy selection = new UCB1();
		PlayoutStrategy playout = new RandomPlayout(200);
		BackpropagationStrategy backprop = new MonteCarloBackprop();
		FinalMoveSelectionStrategy finalMove = new RobustChild();

		// Defaults - some extras
		boolean treeReuse = false;
		boolean useScoreBounds = false;
		int numThreads = 1;
		Policy learnedSelectionPolicy = null;
		Heuristics heuristics = null;
		QInit qinit = QInit.PARENT;
		String friendlyName = "MCTS";
		double playoutValueWeight = 1.0;

		for (String line : lines)
		{
			final String[] lineParts = line.split(",");

			//-----------------------------------------------------------------
			// Main parts
			//-----------------------------------------------------------------
			if (lineParts[0].toLowerCase().startsWith("selection="))
			{
				if (lineParts[0].toLowerCase().endsWith("ucb1"))
				{
					selection = new UCB1();
					selection.customise(lineParts);
				}
				else if 
				(
					lineParts[0].toLowerCase().endsWith("ag0selection") || 
					lineParts[0].toLowerCase().endsWith("alphago0selection")
				)
				{
					selection = new AG0Selection();
					selection.customise(lineParts);
				}
				else if 
				(
					lineParts[0].toLowerCase().endsWith("noisyag0selection") || 
					lineParts[0].toLowerCase().endsWith("noisyalphago0selection")
				)
				{
					selection = new NoisyAG0Selection();
					selection.customise(lineParts);
				}
				else if (lineParts[0].toLowerCase().endsWith("progressivebias"))
				{
					selection = new ProgressiveBias();
					selection.customise(lineParts);
				}
				else if (lineParts[0].toLowerCase().endsWith("progressivehistory"))
				{
					selection = new ProgressiveHistory();
					selection.customise(lineParts);
				}
				else if (lineParts[0].toLowerCase().endsWith("ucb1grave"))
				{
					selection = new UCB1GRAVE();
					selection.customise(lineParts);
				}
				else if (lineParts[0].toLowerCase().endsWith("ucb1tuned"))
				{
					selection = new UCB1Tuned();
					selection.customise(lineParts);
				}
				else
				{
					System.err.println("Unknown selection strategy: " + line);
				}
			}
			else if (lineParts[0].toLowerCase().startsWith("playout="))
			{
				playout = PlayoutStrategy.constructPlayoutStrategy(lineParts);
			}
			else if (lineParts[0].toLowerCase().startsWith("backprop="))
			{
				if (lineParts[0].toLowerCase().endsWith("alphago"))
				{
					backprop = new AlphaGoBackprop();
				}
				else if (lineParts[0].toLowerCase().endsWith("heuristic"))
				{
					backprop = new HeuristicBackprop();
				}
				else if (lineParts[0].toLowerCase().endsWith("montecarlo"))
				{
					backprop = new MonteCarloBackprop();
				}
				else if (lineParts[0].toLowerCase().endsWith("qualitativebonus"))
				{
					backprop = new QualitativeBonus();
				}
			}
			else if (lineParts[0].toLowerCase().startsWith("final_move="))
			{
				if (lineParts[0].toLowerCase().endsWith("maxavgscore"))
				{
					finalMove = new MaxAvgScore();
					finalMove.customise(lineParts);
				}
				else if (lineParts[0].toLowerCase().endsWith("robustchild"))
				{
					finalMove = new RobustChild();
					finalMove.customise(lineParts);
				}
				else if 
				(
					lineParts[0].toLowerCase().endsWith("proportional") || 
					lineParts[0].toLowerCase().endsWith("proportionalexpvisitcount")
				)
				{
					finalMove = new ProportionalExpVisitCount(1.0);
					finalMove.customise(lineParts);
				}
				else
				{
					System.err.println("Unknown final move selection strategy: " + line);
				}
			}
			//-----------------------------------------------------------------
			// Extras
			//-----------------------------------------------------------------
			else if (lineParts[0].toLowerCase().startsWith("tree_reuse="))
			{
				if (lineParts[0].toLowerCase().endsWith("true"))
				{
					treeReuse = true;
				}
				else if (lineParts[0].toLowerCase().endsWith("false"))
				{
					treeReuse = false;
				}
				else
				{
					System.err.println("Error in line: " + line);
				}
			}
			else if (lineParts[0].toLowerCase().startsWith("use_score_bounds="))
			{
				if (lineParts[0].toLowerCase().endsWith("true"))
				{
					useScoreBounds = true;
				}
				else if (lineParts[0].toLowerCase().endsWith("false"))
				{
					useScoreBounds = false;
				}
				else
				{
					System.err.println("Error in line: " + line);
				}
			}
			else if (lineParts[0].toLowerCase().startsWith("num_threads="))
			{
				numThreads = Integer.parseInt(lineParts[0].substring("num_threads=".length()));
			}
			else if (lineParts[0].toLowerCase().startsWith("learned_selection_policy="))
			{
				if (lineParts[0].toLowerCase().endsWith("playout"))
				{
					// our playout strategy is our learned Selection policy
					learnedSelectionPolicy = (Policy) playout;
				}
				else if 
				(
					lineParts[0].toLowerCase().endsWith("softmax") 
					|| 
					lineParts[0].toLowerCase().endsWith("softmaxplayout")
					||
					lineParts[0].toLowerCase().endsWith("softmaxlinear")
				)
				{
					learnedSelectionPolicy = new SoftmaxPolicyLinear();
					learnedSelectionPolicy.customise(lineParts);
				}
				else if (lineParts[0].toLowerCase().endsWith("softmaxlogittree"))
				{
					learnedSelectionPolicy = new SoftmaxPolicyLogitTree();
					learnedSelectionPolicy.customise(lineParts);
				}
			}
			else if (lineParts[0].toLowerCase().startsWith("heuristics="))
			{
				heuristics = Heuristics.fromLines(lineParts);
			}
			else if (lineParts[0].toLowerCase().startsWith("qinit="))
			{
				qinit = QInit.valueOf(lineParts[0].substring("qinit=".length()).toUpperCase());
			}
			else if (lineParts[0].toLowerCase().startsWith("playout_value_weight="))
			{
				playoutValueWeight = Double.parseDouble(lineParts[0].substring("playout_value_weight=".length()));
			}
			else if (lineParts[0].toLowerCase().startsWith("friendly_name="))
			{
				friendlyName = lineParts[0].substring("friendly_name=".length());
			}
		}

		MCTS mcts = new MCTS(selection, playout, backprop, finalMove);

		mcts.setTreeReuse(treeReuse);
		mcts.setUseScoreBounds(useScoreBounds);
		mcts.setNumThreads(numThreads);
		mcts.setLearnedSelectionPolicy(learnedSelectionPolicy);
		mcts.setHeuristics(heuristics);
		mcts.setQInit(qinit);
		mcts.setPlayoutValueWeight(playoutValueWeight);
		mcts.friendlyName = friendlyName;

		return mcts;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return A string describing our MCTS configuration
	 */
	public String describeMCTS()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("Selection = " + selectionStrategy + "\n");
		sb.append("Playout = " + playoutStrategy + "\n");
		sb.append("Backprop = " + backpropagationStrategy + "\n");
		sb.append("friendly name = " + friendlyName + "\n");
		sb.append("tree reuse = " + treeReuse + "\n");
		sb.append("use score bounds = " + useScoreBounds + "\n");
		sb.append("qinit = " + qInit + "\n");
		sb.append("playout value weight = " + playoutValueWeight + "\n");
		sb.append("final move selection = " + finalMoveSelectionStrategy + "\n");
		sb.append("heuristics:\n");
		sb.append(heuristicFunction + "\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
     * Wrapper class for global (MCTS-wide) action statistics
     * (accumulated scores + visit count)
     * 
     * @author Dennis Soemers
     */
    public static class ActionStatistics
    {
    	/** Visit count (not int because we want to be able to decay) */
    	public float visitCount = 0.f;
    	
    	/** Accumulated score */
    	public float accumulatedScore = 0.f;
    	
    	@Override
    	public String toString()
    	{
    		return "[visits = " + visitCount + ", accum. score = " + accumulatedScore + "]";
    	}
    }
    
    /**
     * Object to be used as key for a move in hash tables.
     * 
     * @author Dennis Soemers
     */
    public static class MoveKey
    {
    	/** The full move object */
    	public final Move move;
    	
    	/** Depth at which move was played (only taken into account for passes and swaps) */
    	public final int moveDepth;
    	
    	/** Cached hashCode */
    	private final int cachedHashCode;
    	
    	/**
    	 * Constructor
    	 * @param move
    	 * @param depth Depth at which the move was played. Can be 0 if not known.
    	 * Only used to distinguish pass/swap moves at different levels of search tree.
    	 */
    	public MoveKey(final Move move, final int depth)
    	{
    		this.move = move;
    		this.moveDepth = depth;
    		final int prime = 31;
			int result = 1;
			
			if (move.isPass())
			{
				result = prime * result + depth + 1297;
			}
			else if (move.isSwap())
			{
				result = prime * result + depth + 587;
			}
			else
			{
				if (!move.isOrientedMove())
				{
					result = prime * result + (move.toNonDecision() + move.fromNonDecision());
				}
				else
				{
					result = prime * result + move.toNonDecision();
					result = prime * result + move.fromNonDecision();
				}
				
				result = prime * result + move.stateNonDecision();
			}
				
			result = prime * result + move.mover();
			
			cachedHashCode = result;
    	}

		@Override
		public int hashCode()
		{
			return cachedHashCode;
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
				return true;

			if (!(obj instanceof MoveKey))
				return false;
			
			final MoveKey other = (MoveKey) obj;
			if (move == null)
				return (other.move == null);
			
			if (move.mover() != other.move.mover())
				return false;
			
			final boolean movePass = move.isPass();
			final boolean otherMovePass = other.move.isPass();
			final boolean moveSwap = move.isSwap();
			final boolean otherMoveSwap = other.move.isSwap();
			
			if (movePass)
			{
				return (otherMovePass && moveDepth == other.moveDepth);
			}
			else if (moveSwap)
			{
				return (otherMoveSwap && moveDepth == other.moveDepth);
			}
			else
			{
				if (otherMovePass || otherMoveSwap)
					return false;
				
				if (move.isOrientedMove() != other.move.isOrientedMove())
					return false;
				
				if (move.isOrientedMove()) 
				{
					if (move.toNonDecision() != other.move.toNonDecision() || move.fromNonDecision() != other.move.fromNonDecision())
						return false;
				}
				else
				{
					boolean fine = false;
					
					if 
					(
						(move.toNonDecision() == other.move.toNonDecision() && move.fromNonDecision() == other.move.fromNonDecision())
						||
						(move.toNonDecision() == other.move.fromNonDecision() && move.fromNonDecision() == other.move.toNonDecision())
					)
					{
						fine = true;
					}
					
					if (!fine)
						return false;
				}
				
				return (move.stateNonDecision() == other.move.stateNonDecision());
			}
		}
		
		@Override
		public String toString()
		{
			return "[Move = " + move + ", Hash = " + cachedHashCode + "]";
		}
    }
    
    /**
     * Object to be used as key for an N-gram of moves in hash tables.
     * 
     * @author Dennis Soemers
     */
    public static class NGramMoveKey
    {
    	/** The array of full move object */
    	public final Move[] moves;
    	
    	/** Depth at which move was played (only taken into account for passes and swaps) */
    	private final int moveDepth;
    	
    	/** Cached hashCode */
    	private final int cachedHashCode;
    	
    	/**
    	 * Constructor
    	 * @param moves
    	 * @param depth Depth at which the first move of N-gram was played. Can be 0 if not known.
    	 * Only used to distinguish pass/swap moves at different levels of search tree.
    	 */
    	public NGramMoveKey(final Move[] moves, final int depth)
    	{
    		this.moves = moves;
    		this.moveDepth = depth;
    		final int prime = 31;
			int result = 1;
			
			for (int i = 0; i < moves.length; ++i)
			{
				final Move move = moves[i];
				if (move.isPass())
				{
					result = prime * result + depth + i + 1297;
				}
				else if (move.isSwap())
				{
					result = prime * result + depth + i + 587;
				}
				else
				{
					if (!move.isOrientedMove())
					{
						result = prime * result + (move.toNonDecision() + move.fromNonDecision());
					}
					else
					{
						result = prime * result + move.toNonDecision();
						result = prime * result + move.fromNonDecision();
					}
					
					result = prime * result + move.stateNonDecision();
				}
					
				result = prime * result + move.mover();
			}
			
			cachedHashCode = result;
    	}

		@Override
		public int hashCode()
		{
			return cachedHashCode;
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
				return true;

			if (!(obj instanceof NGramMoveKey))
				return false;
			
			final NGramMoveKey other = (NGramMoveKey) obj;
			
			if (moves.length != other.moves.length)
				return false;
			
			for (int i = 0; i < moves.length; ++i)
			{
				final Move move = moves[i];
				final Move otherMove = other.moves[i];
				
				if (move.mover() != otherMove.mover())
					return false;
				
				final boolean movePass = move.isPass();
				final boolean otherMovePass = otherMove.isPass();
				final boolean moveSwap = move.isSwap();
				final boolean otherMoveSwap = otherMove.isSwap();
				
				if (movePass)
				{
					return (otherMovePass && moveDepth == other.moveDepth);
				}
				else if (moveSwap)
				{
					return (otherMoveSwap && moveDepth == other.moveDepth);
				}
				else
				{
					if (otherMovePass || otherMoveSwap)
						return false;
					
					if (move.isOrientedMove() != otherMove.isOrientedMove())
						return false;
					
					if (move.isOrientedMove()) 
					{
						if (move.toNonDecision() != otherMove.toNonDecision() || move.fromNonDecision() != otherMove.fromNonDecision())
							return false;
					}
					else
					{
						boolean fine = false;
						
						if 
						(
							(move.toNonDecision() == otherMove.toNonDecision() && move.fromNonDecision() == otherMove.fromNonDecision())
							||
							(move.toNonDecision() == otherMove.fromNonDecision() && move.fromNonDecision() == otherMove.toNonDecision())
						)
						{
							fine = true;
						}
						
						if (!fine)
							return false;
						
						if (!(move.stateNonDecision() == otherMove.stateNonDecision()))
							return false;
					}
				}
			}
				
			return true;
		}
		
		@Override
		public String toString()
		{
			return "[Moves = " + Arrays.toString(moves) + ", Hash = " + cachedHashCode + "]";
		}
    }

}
