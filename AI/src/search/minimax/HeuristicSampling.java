package search.minimax;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import expert_iteration.ExItExperience;
import expert_iteration.ExItExperience.ExItExperienceState;
import expert_iteration.ExpertPolicy;
import game.Game;
import main.FileHandling;
import main.collections.FVector;
import main.collections.FastArrayList;
import main.grammar.Report;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilitySimple;
import other.context.Context;
import other.move.Move;
import other.move.MoveScore;
import utils.data_structures.transposition_table.TranspositionTable;

/**
 * Flat search that does heuristic sampling per turn, i.e. chooses T random moves
 * and selects the one with the highest heuristic evaluation when applied.  
 * 
 * @author cambolbro
 */
public class HeuristicSampling extends ExpertPolicy
{
	
	//-------------------------------------------------------------------------
	
	/** Value we use to initialise alpha ("negative infinity", but not really) */
	private static final float ALPHA_INIT = -1000000.f;
	
	/** Value we use to initialise beta ("positive infinity", but not really) */
	private static final float BETA_INIT = -ALPHA_INIT;
	
	/** Score we give to winning opponents in paranoid searches in states where game is still going (> 2 players) */
	private static final float PARANOID_OPP_WIN_SCORE = 10000.f;
	private static final float WIN_SCORE = 10000.f;
	
	/** We skip computing heuristics with absolute weight value lower than this */
	public static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.01f;
	
	/** Our heuristic value function estimator */
	private Heuristics heuristicValueFunction = null;
	
	/** If true, we read our heuristic function to use from game's metadata */
	private final boolean heuristicsFromMetadata;
	
	/** We'll automatically return our move after at most this number of seconds if we only have one move */
	protected double autoPlaySeconds = 0.0;
	
	/** Estimated score of the root node based on last-run search */
	protected float estimatedRootScore = 0.f;
	
	/** The maximum heuristic eval we have ever observed */
	protected float maxHeuristicEval = 0.f;
	
	/** The minimum heuristic eval we have ever observed */
	protected float minHeuristicEval = 0.f;
	
	/** String to print to Analysis tab of the Ludii app */
	protected String analysisReport = null;
	
	/** Current list of moves available in root */
	protected FastArrayList<Move> currentRootMoves = null;
	
	/** The last move we returned. Need to memorise this for Expert Iteration with AlphaBeta */
	protected Move lastReturnedMove = null;
	
	/** Root context for which we've last performed a search */
	protected Context lastSearchedRootContext = null;
	
	/** Value estimates of moves available in root */
	protected FVector rootValueEstimates = null;
	
	/** The number of players in the game we're currently playing */
	protected int numPlayersInGame = 0;
	
	/** Remember if we proved a win in one of our searches */
	protected boolean provedWin = false;
	
	/** Needed for visualisations */
	protected float rootAlphaInit = ALPHA_INIT;
	
	/** Needed for visualisations */
	protected float rootBetaInit = BETA_INIT;
	
	/** Sorted (hopefully cleverly) list of moves available in root node */
	protected FastArrayList<Move> sortedRootMoves = null;
	
	/** If true at end of a search, it means we searched full tree (probably proved a draw) */
	protected boolean searchedFullTree = false;
	
	/** Transposiiton Table */
	protected TranspositionTable transpositionTable = null;
	
	/** Denominator of heuristic threshold fraction, i.e. 1/2, 1/4, 1/8, etc. */
	private int fraction = 2;
	
	/** Whether to apply same-turn continuation. */
	private boolean continuation = true;
		
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public HeuristicSampling()
	{
		heuristicsFromMetadata = true;
		setFriendlyName();
	}
	
	/**
	 * Constructor
	 * @param heuristicsFilepath
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public HeuristicSampling(final Heuristics heuristics)
	{
		heuristicValueFunction = heuristics;
		heuristicsFromMetadata = false;
		setFriendlyName();
	}
	
	/**
	 * Constructor
	 * @param heuristicsFilepath
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public HeuristicSampling(final Heuristics heuristics, final int fraction)
	{
		heuristicValueFunction = heuristics;
		heuristicsFromMetadata = false;
		this.fraction = fraction;
		setFriendlyName();
	}
	
	/**
	 * Constructor
	 * @param heuristicsFilepath
	 */
	public HeuristicSampling(final String heuristicsFilepath) throws FileNotFoundException, IOException
	{
		final String heuristicsStr = FileHandling.loadTextContentsFromFile(heuristicsFilepath);
		heuristicValueFunction = (Heuristics)compiler.Compiler.compileObject
										(
											heuristicsStr, 
											"metadata.ai.heuristics.Heuristics",
											new Report()
										);
		heuristicsFromMetadata = false;
		setFriendlyName();
	}
	
	//-------------------------------------------------------------------------
	// Getters and Setters
	
	public Heuristics heuristics()
	{
		return heuristicValueFunction;
	}
	
	public int threshold()
	{
		return fraction;
	}
	
	public void setThreshold(final int value)
	{
		fraction = value;
		setFriendlyName();
	}
	
	public boolean continuation()
	{
		return continuation;
	}
	
	public void setContinuation(final boolean value)
	{
		continuation = value;
		setFriendlyName();
	}
	
	//-------------------------------------------------------------------------

	void setFriendlyName()
	{
		friendlyName = "HS (1/" + fraction + ")" + (continuation ? "*" : "");
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
		final MoveScore moveScore = evaluateMoves(game, context);
		final Move move = moveScore.move();
		if (move == null)
			System.out.println("** No bext move.");
		return move;
	}
	
	//-------------------------------------------------------------------------

	MoveScore evaluateMoves(final Game game, final Context context)
	{
		final FastArrayList<Move> moves = selectMoves(game, context, fraction);
		
		float bestScore = Float.NEGATIVE_INFINITY;
		Move bestMove = moves.get(0);
		
		final int mover = context.state().mover();
		
		//Context contextCurrent = context;
		
		for (final Move move: moves) 
		{
			final Context contextCopy = new Context(context);
			game.apply(contextCopy, move);
			
			if (contextCopy.trial().status() != null) 
			{
				// Check if move is a winner
				final int winner = contextCopy.state().playerToAgent(contextCopy.trial().status().winner());
					
				if (winner == mover)
					return new MoveScore(move, WIN_SCORE);  // return winning move immediately
					
				if (winner != 0)
					continue;  // skip losing move
			}
			
			float score = 0;
			if (continuation && contextCopy.state().mover() == mover)
			{
				//System.out.println("Recursing...");
				return new MoveScore(move, evaluateMoves(game, contextCopy).score());
			}
			else
			{
				score = heuristicValueFunction.computeValue
						(
							contextCopy, mover, ABS_HEURISTIC_WEIGHT_THRESHOLD
						);
				for (final int opp : opponents(mover))
				{
					if (context.active(opp))
						score -= heuristicValueFunction.computeValue(contextCopy, opp, ABS_HEURISTIC_WEIGHT_THRESHOLD);
					else if (context.winners().contains(opp))
						score -= PARANOID_OPP_WIN_SCORE;
				}
				score += (float)(ThreadLocalRandom.current().nextInt(1000) / 1000000.0);
			}
		
			if (score > bestScore)
			{
				bestScore = score;
				bestMove  = move;
			}
		}
		
		return new MoveScore(bestMove, bestScore);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game    Current game.
	 * @param context Current context.
	 * @param fraction  Number of moves to select.
	 * @return Randomly chosen subset of moves.
	 */
	public static FastArrayList<Move> selectMoves(final Game game, final Context context, final int fraction)
	{
		final FastArrayList<Move> playerMoves   = game.moves(context).moves();
		final FastArrayList<Move> selectedMoves = new FastArrayList<Move>();
	
		final int target = Math.max(2, (playerMoves.size() + 1) / fraction);
		
		if (target >= playerMoves.size()) 
			return playerMoves;
		
		while (selectedMoves.size() < target)
		{
			final int r = ThreadLocalRandom.current().nextInt(playerMoves.size());
			selectedMoves.add(playerMoves.get(r));
			playerMoves.remove(r);
		}
		
		return selectedMoves;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param player
	 * @return Opponents of given player
	 */
	public int[] opponents(final int player)
	{
		final int[] opponents = new int[numPlayersInGame - 1];
		int idx = 0;
		
		for (int p = 1; p <= numPlayersInGame; ++p)
		{
			if (p != player)
				opponents[idx++] = p;
		}
		
		return opponents;
	}
	
	/**
	 * Converts a score into a value estimate in [-1, 1]. Useful for visualisations.
	 * 
	 * @param score
	 * @param alpha 
	 * @param beta 
	 * @return Value estimate in [-1, 1] from unbounded (heuristic) score.
	 */
	public double scoreToValueEst(final float score, final float alpha, final float beta)
	{
		if (score == alpha)
			return -1.0;
		
		if (score == beta)
			return 1.0;
		
		// Map to range [-0.8, 0.8] based on most extreme heuristic evaluations
		// observed so far.
		return -0.8 + (0.8 - -0.8) * ((score - minHeuristicEval) / (maxHeuristicEval - minHeuristicEval));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		if (heuristicsFromMetadata)
		{
			// Read heuristics from game metadata
			final metadata.ai.Ai aiMetadata = game.metadata().ai();
			if (aiMetadata != null && aiMetadata.heuristics() != null)
			{
				heuristicValueFunction = Heuristics.copy(aiMetadata.heuristics());
			}
			else
			{
				// construct default heuristic
				heuristicValueFunction = new Heuristics(new HeuristicTerm[]{
						new Material(null, Float.valueOf(1.f), null, null),
						new MobilitySimple(null, Float.valueOf(0.001f))
				});
			}
		}
		
		if (heuristicValueFunction != null)
			heuristicValueFunction.init(game);
		
		// reset these things used for visualisation purposes
		estimatedRootScore = 0.f;
		maxHeuristicEval = 0.f;
		minHeuristicEval = 0.f;
		analysisReport = null;
		
		currentRootMoves = null;
		rootValueEstimates = null;
		
		// and these things for ExIt
		lastSearchedRootContext = null;
		lastReturnedMove = null;
		
		numPlayersInGame = game.players().count();
		
		if (game.usesNoRepeatPositionalInGame() || game.usesNoRepeatPositionalInTurn())
			transpositionTable = null;
		else
			transpositionTable = new TranspositionTable(12);
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.players().count() <= 1)
			return false;
		
//		if (game.isStochasticGame())
//			return false;
		
		if (game.hiddenInformation())
			return false;
		
		return game.isAlternatingMoveGame();
	}
	
	@Override
	public double estimateValue()
	{
		return scoreToValueEst(estimatedRootScore, rootAlphaInit, rootBetaInit);
	}
	
	@Override
	public String generateAnalysisReport()
	{
		return analysisReport;
	}
	
	@Override
	public AIVisualisationData aiVisualisationData()
	{
		if (currentRootMoves == null || rootValueEstimates == null)
			return null;
		
		final FVector aiDistribution = rootValueEstimates.copy();
		aiDistribution.subtract(aiDistribution.min());
		
		return new AIVisualisationData(aiDistribution, rootValueEstimates, currentRootMoves);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public FastArrayList<Move> lastSearchRootMoves()
	{
		final FastArrayList<Move> moves = new FastArrayList<Move>(currentRootMoves.size());
		for (final Move move : currentRootMoves)
			moves.add(move);
		return moves;
	}
	
	@Override
	public FVector computeExpertPolicy(final double tau)
	{
		final FVector distribution = FVector.zeros(currentRootMoves.size());
		distribution.set(currentRootMoves.indexOf(lastReturnedMove), 1.f);
		distribution.softmax();
		return distribution;
	}
	
	@Override
	public ExItExperience generateExItExperience()
	{
		final FastArrayList<Move> actions = new FastArrayList<Move>(currentRootMoves.size());
		for (int i = 0; i < currentRootMoves.size(); ++i)
		{
			final Move m = new Move(currentRootMoves.get(i));
			m.setMover(currentRootMoves.get(i).mover());
    		m.then().clear();	// Can't serialise these, and won't need them
    		actions.add(m);
		}
		
    	return new ExItExperience
    			(
    				new ExItExperienceState(lastSearchedRootContext),
    				actions,
    				computeExpertPolicy(1.0),
    				FVector.zeros(actions.size())
    			);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lines
	 * @return Constructs an Alpha-Beta Search object from instructions in the 
	 * given array of lines
	 */
	public static HeuristicSampling fromLines(final String[] lines)
	{
		String friendlyName = "Alpha-Beta";
		String heuristicsFilepath = null;

		for (final String line : lines)
		{
			final String[] lineParts = line.split(",");

			if (lineParts[0].toLowerCase().startsWith("heuristics="))
			{
				heuristicsFilepath = lineParts[0].substring("heuristics=".length());
			}
			else if (lineParts[0].toLowerCase().startsWith("friendly_name="))
			{
				friendlyName = lineParts[0].substring("friendly_name=".length());
			}
		}
		
		HeuristicSampling alphaBeta = null;
		
		if (heuristicsFilepath != null)
		{
			try
			{
				alphaBeta = new HeuristicSampling(heuristicsFilepath);
			} 
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		
		if (alphaBeta == null)
			alphaBeta = new HeuristicSampling();

		alphaBeta.friendlyName = friendlyName;

		return alphaBeta;
	}
	
	//-------------------------------------------------------------------------

}
