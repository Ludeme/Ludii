package search.flat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.FileHandling;
import main.collections.FastArrayList;
import main.grammar.Report;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilitySimple;
import other.AI;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.move.MoveScore;

/**
 * Flat search that does heuristic sampling per turn, i.e. chooses T random moves
 * and selects the one with the highest heuristic evaluation when applied.  
 * 
 * @author cambolbro and Dennis Soemers
 */
public class HeuristicSampling extends AI
{
	
	//-------------------------------------------------------------------------
	
	/** Score we give to winning opponents in paranoid searches in states where game is still going (> 2 players) */
	private static final float PARANOID_OPP_WIN_SCORE = 10000.f;
	private static final float WIN_SCORE = 10000.f;
	
	/** We skip computing heuristics with absolute weight value lower than this */
	public static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.01f;
	
	/** Our heuristic value function estimator */
	private Heuristics heuristicValueFunction = null;
	
	/** If true, we read our heuristic function to use from game's metadata */
	private final boolean heuristicsFromMetadata;
	
	/** The number of players in the game we're currently playing */
	protected int numPlayersInGame = 0;
	
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
	 */
	public HeuristicSampling(final int fraction)
	{
		heuristicsFromMetadata = true;
		this.fraction = fraction;
		setFriendlyName();
	}
	
	/**
	 * Constructor
	 */
	public HeuristicSampling(final Heuristics heuristics)
	{
		heuristicValueFunction = heuristics;
		heuristicsFromMetadata = false;
		setFriendlyName();
	}
	
	/**
	 * Constructor
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
		
		for (final Move move : moves) 
		{
			final Context contextCopy = new TempContext(context);
			game.apply(contextCopy, move);
			
			if (!contextCopy.active(mover))
			{
				if (contextCopy.winners().contains(mover))
					return new MoveScore(move, WIN_SCORE);  // Return winning move immediately
				else if (contextCopy.losers().contains(mover))
					continue;	// Skip losing move
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
			playerMoves.removeSwap(r);
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
		
		numPlayersInGame = game.players().count();
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
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lines
	 * @return Constructs a Heuristic Sampling object from instructions in the 
	 * given array of lines
	 */
	public static HeuristicSampling fromLines(final String[] lines)
	{
		String friendlyName = "HeuristicSampling";
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
		
		HeuristicSampling heuristicSampling = null;
		
		if (heuristicsFilepath != null)
		{
			try
			{
				heuristicSampling = new HeuristicSampling(heuristicsFilepath);
			} 
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		
		if (heuristicSampling == null)
			heuristicSampling = new HeuristicSampling();

		heuristicSampling.friendlyName = friendlyName;

		return heuristicSampling;
	}
	
	//-------------------------------------------------------------------------

}
