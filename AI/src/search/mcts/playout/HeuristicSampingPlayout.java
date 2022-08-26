package search.mcts.playout;

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
import other.move.Move;
import other.playout.HeuristicSamplingMoveSelector;
import other.trial.Trial;
import search.mcts.MCTS;
import other.move.MoveScore;

/**
 * Playout strategy that selects actions that lead to successor states that
 * maximise a heuristic score from the mover's perspective.
 * 
 * We extend the AI abstract class because this means that the outer MCTS
 * will also let us init, which allows us to load heuristics from metadata
 * if desired. Also means this thing can play games as a standalone AI.
 *
 * @author Eric.Piette (based on code of Dennis Soemers and Cameron Browne)
 */
public class HeuristicSampingPlayout extends AI implements PlayoutStrategy
{
	//-------------------------------------------------------------------------
	
	/** 
	 * Auto-end playouts in a draw if they take more turns than this, Negative value means
	 * no limit.
	 * 
	 * TODO if we have heuristics anyway, might make sense to use them for a non-draw eval..
	 */	
	protected int playoutTurnLimit = -1;
	
	/** Filepath from which we want to load heuristics. Null if we want to load automatically from game's metadata */
	protected final String heuristicsFilepath;
	
	/** Heuristic-based PlayoutMoveSelector */
	protected HeuristicSamplingMoveSelector moveSelector =new HeuristicSamplingMoveSelector();
	
	/** Score we give to winning opponents in paranoid searches in states where game is still going (> 2 players) */
	private static final float PARANOID_OPP_WIN_SCORE = 10000.f;
	private static final float WIN_SCORE = 10000.f;
	
	/** We skip computing heuristics with absolute weight value lower than this */
	public static final float ABS_HEURISTIC_WEIGHT_THRESHOLD = 0.01f;
	
	/** Denominator of heuristic threshold fraction, i.e. 1/2, 1/4, 1/8, etc. */
	private int fraction = 2;
	
	/** Whether to apply same-turn continuation. */
	private boolean continuation = true;
	
	/** Our heuristic value function estimator */
	private Heuristics heuristicValueFunction = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Default constructor: no cap on actions in playout, heuristics from metadata
	 */
	public HeuristicSampingPlayout()
	{
		playoutTurnLimit = -1;			// No limit
		heuristicsFilepath = null;
	}
	
	/**
	 * Constructor
	 * @param heuristicsFilepath Filepath for file specifying heuristics to use
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public HeuristicSampingPlayout(final String heuristicsFilepath) throws FileNotFoundException, IOException
	{
		this.playoutTurnLimit = -1;		// No limit
		this.heuristicsFilepath = heuristicsFilepath;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Trial runPlayout(final MCTS mcts, final Context context)
	{
		return context.game().playout(context, null, 1.0, moveSelector, -1, playoutTurnLimit, ThreadLocalRandom.current());
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean playoutSupportsGame(final Game game)
	{
		if (game.isDeductionPuzzle())
			return (playoutTurnLimit() > 0);
		else
			return true;
	}

	@Override
	public void customise(final String[] inputs)
	{
		// Nothing to do here.
	}
	
	/**
	 * @return The turn limit we use in playouts
	 */
	public int playoutTurnLimit()
	{
		return playoutTurnLimit;
	}

	@Override
	public int backpropFlags()
	{
		return 0;
	}
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		if (heuristicsFilepath == null)
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
				heuristicValueFunction = 
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
		else
		{
			heuristicValueFunction = moveSelector.heuristicValueFunction();
			
			if (heuristicValueFunction == null)
			{
				String heuristicsStr;
				try
				{
					heuristicsStr = FileHandling.loadTextContentsFromFile(heuristicsFilepath);
					heuristicValueFunction = 
						(Heuristics)compiler.Compiler.compileObject
						(
							heuristicsStr, 
							"metadata.ai.heuristics.Heuristics",
							new Report()
						);
				} 
				catch (final IOException e)
				{
					e.printStackTrace();
					return;
				}
			}
		}
		
		if (heuristicValueFunction != null)
		{
			heuristicValueFunction.init(game);
			moveSelector.setHeuristics(heuristicValueFunction);
		}
	}

	@Override
	public Move selectAction
	(
		final Game game, final Context context, final double maxSeconds, 
		final int maxIterations, final int maxDepth
	)
	{
		final MoveScore moveScore = evaluateMoves(game, context);
		final Move move = moveScore.move();
		if (move == null)
			System.out.println("** No best move.");
		return move;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param player The player.
	 * @param context The context.
	 * 
	 * @return Opponents of given player
	 */
	@SuppressWarnings("static-method")
	public int[] opponents(final int player, final Context context)
	{
		final int numPlayersInGame = context.game().players().count();
		final int[] opponents = new int[numPlayersInGame - 1];

		int idx = 0;
		
		if (context.game().requiresTeams())
		{
			final int tid = context.state().getTeam(player);
			for (int p = 1; p <= numPlayersInGame; p++)
				if (context.state().getTeam(p) != tid)
					opponents[idx++] = p;
		}
		else
		{
			for (int p = 1; p <= numPlayersInGame; ++p)
			{
				if (p != player)
					opponents[idx++] = p;
			}
		}
		
		return opponents;
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
		FastArrayList<Move> playerMoves   = game.moves(context).moves();
		FastArrayList<Move> selectedMoves = new FastArrayList<Move>();
	
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

	MoveScore evaluateMoves(final Game game, final Context context)
	{
		FastArrayList<Move> moves = selectMoves(game, context, fraction);
		
		float bestScore = Float.NEGATIVE_INFINITY;
		Move bestMove = moves.get(0);
		
		final int mover = context.state().mover();
		
		//Context contextCurrent = context;
		
		for (Move move: moves) 
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
				for (final int opp : opponents(mover, context))
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
}
