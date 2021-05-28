package search.mcts.playout;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.FileHandling;
import main.grammar.Report;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilitySimple;
import other.AI;
import other.context.Context;
import other.move.Move;
import other.playout.HeuristicMoveSelector;
import other.trial.Trial;
import search.mcts.MCTS;

/**
 * Playout strategy that selects actions that lead to successor states that
 * maximise a heuristic score from the mover's perspective.
 * 
 * We extend the AI abstract class because this means that the outer MCTS
 * will also let us init, which allows us to load heuristics from metadata
 * if desired. Also means this thing can play games as a standalone AI.
 *
 * @author Dennis Soemers
 */
public class HeuristicPlayout extends AI implements PlayoutStrategy
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
	protected HeuristicMoveSelector moveSelector = new HeuristicMoveSelector();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Default constructor: no cap on actions in playout, heuristics from metadata
	 */
	public HeuristicPlayout()
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
	public HeuristicPlayout(final String heuristicsFilepath) throws FileNotFoundException, IOException
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
		// TODO
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
		Heuristics heuristicValueFunction;
		
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
		// TODO Auto-generated method stub
		System.err.println("Need to implement HeuristicPlayout::selectAction() to let it play as standalone AI!");
		return null;
	}
	
	//-------------------------------------------------------------------------

}
