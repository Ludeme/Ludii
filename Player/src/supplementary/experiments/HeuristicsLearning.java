package supplementary.experiments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FastArrayList;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.CentreProximity;
import metadata.ai.heuristics.terms.ComponentValues;
import metadata.ai.heuristics.terms.CornerProximity;
import metadata.ai.heuristics.terms.CurrentMoverHeuristic;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.InfluenceAdvanced;
import metadata.ai.heuristics.terms.Intercept;
import metadata.ai.heuristics.terms.LineCompletionHeuristic;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilityAdvanced;
import metadata.ai.heuristics.terms.OwnRegionsCount;
import metadata.ai.heuristics.terms.PlayerRegionsProximity;
import metadata.ai.heuristics.terms.PlayerSiteMapCount;
import metadata.ai.heuristics.terms.RegionProximity;
import metadata.ai.heuristics.terms.Score;
import metadata.ai.heuristics.terms.SidesProximity;
import other.AI;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import other.state.State;
import other.trial.Trial;
import search.minimax.BestFirstSearch;
import search.minimax.DescentBFS;
import utils.data_structures.transposition_table.TranspositionTable.ABTTData;

/**
 * 
 * Script to learn a set of heuristic terms and weight for a game, for it to be
 * used by minimax based algorithm like Alpha-Beta search or UBFS.
 * 
 * @author cyprien
 *
 */

public class HeuristicsLearning
{
	
	private static final BestFirstSearch BestFirstSearch = null;

	private Boolean debugDisplays = true;
	
	/** Duration of the whole training in seconds: */
	private Double trainingDuration = 10.;
	
	/** Game played: */
	private String gameName = "Breakthrough";
	
	/** Thinking time of the AI in seconds: */
	private float thinkingTime = 1f;
	
	//-------------------------------------------------------------------------


	HeuristicTerm[] allHeuristicTerms = new HeuristicTerm[]
		{
			new CentreProximity(null,null,null),
			new ComponentValues(null, null, null, null), 
			new CornerProximity(null, null, null),
			new CurrentMoverHeuristic(null, null), 
			new InfluenceAdvanced(null, null),
			new LineCompletionHeuristic(null, null, null),
			new Material(null,null, null, null), 
			new MobilityAdvanced(null, null),
			new OwnRegionsCount(null, null), 
			new PlayerRegionsProximity(null, null, null, null),
			new PlayerSiteMapCount(null,null), 
			new RegionProximity(null, null, null, null),
			new PlayerSiteMapCount(null, null),
			new RegionProximity(null, null, null, null), 
			new Score(null, null), 
			new SidesProximity(null, null, null)
		};
	
	//-------------------------------------------------------------------------
	
	
	/** Training data X (list of vectors containing the value of each heuristics):*/
	private List<float[]> trainingDataX;
	
	/** Training data Y (list of floats representing the value of each states):*/
	private List<Float> trainingDataY;
	
	private Game game;
	
	private int maximisingPlayer = 1;
	
	int numPlayers;
	
	//-------------------------------------------------------------------------
	
	public void main ()
	{
		
		final long startTime = System.currentTimeMillis();
		long stopTime = startTime + (long) (trainingDuration * 1000);
		
		final int nbHeuristics = allHeuristicTerms.length;
		
		final Heuristics heuristics = new Heuristics(allHeuristicTerms);
		
		game = GameLoader.loadGameFromName(gameName+".lud");
		numPlayers = game.players().count();
		
		
		final float[] heuristicWeights = new float[nbHeuristics];
		// TODO: initialise weights with random values
		
		
		int numIteration = 1;
		
		while (System.currentTimeMillis() < stopTime)
		{

			//-----------------------------------------------------------------
			// Setting up a playout:
			
			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);
			
			final List<AI> agents = new ArrayList<>();
			
			// Filling the heuristic function's weight:
			for (int i=0; i<nbHeuristics; i++)
			{
				allHeuristicTerms[i].setWeight(heuristicWeights[i]);
			}
			
			final DescentBFS AI = DescentBFS.generateDescentBFS(heuristics);
			
			agents.add(null);
			
			agents.add(AI);
			agents.add(AI);

			game.start(context);

			AI.initAI(game, maximisingPlayer);
			
			if (debugDisplays) System.out.printf("Beginning the ith playout:",numIteration);
			
			game.playout(context, agents, thinkingTime, null, -1, 200, ThreadLocalRandom.current());
			
			//-----------------------------------------------------------------
			// Generating training set for the linear regression:
			
			generateTrainingSet( AI );
			
			
			//-----------------------------------------------------------------
			// Updating the heuristicWeights with Machine Learning: TODO
			if (debugDisplays) System.out.println("Updating the heuristicWeights with Machine Learning:");
			
			
			numIteration += 1;
		}
		
		//---------------------------------------------------------------------
		// Saving weights in a file: (TODO)
		
		
		
	}
	
	/**
	 * Uses a transposition table to calculate the value assosciated to states and fill the training data.
	 * Will use a recursive function to evaluate the visted nodes with minimax.
	 * @param TT
	 */
	protected void generateTrainingSet (final BestFirstSearch AI)
	{
		if (debugDisplays) System.out.println("Generating training set for the linear regression:");
		
		trainingDataX.clear();
		trainingDataY.clear();
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		
		fillTrainingData(BestFirstSearch, context);
		
		return;
	}

	
	/**
	 * Recursive function to fill the training data.
	 * The value of a state is it's minimax value in the search tree ("tree learning").
	 * @param TT
	 * @param context
	 */
	protected float fillTrainingData
	(
		final BestFirstSearch AI,
		final Context context
	)
	{
		final float value;
		
		if (context.trial().over() || !context.active(maximisingPlayer))
		{
			// terminal node (at least for maximising player)
			value = (float) RankUtils.agentUtilities(context)[maximisingPlayer];
		}
		else
		{
			final FastArrayList<Move> legalMoves = game.moves(context).moves();
			final int nbMoves = legalMoves.size();
			final State state = context.state();
			final int mover = state.playerToAgent(state.mover());
			
			final List<Float> childrenValues = new ArrayList<Float>();

			for (int i=0; i<nbMoves; i++)
			{
				final Context contextCopy = AI.copyContext(context);
				final Move move = legalMoves.get(i);
				
				game.apply(contextCopy, move);
				
				final State newState = contextCopy.state();
				final long zobrist = newState.fullHash();
				final ABTTData tableData = AI.transpositionTable.retrieve(zobrist);
				
				if (tableData != null)
				{
					// Recursive call:
					final float childValue = fillTrainingData(AI, context);
					
					childrenValues.add(childValue);
				}
			}
			
			if (childrenValues.size()>0)
			{
				float myMax = childrenValues.get(0); // actually a min if the opposent is playing
				
				for (float childValue : childrenValues)
				{
					if (((childValue>myMax)&&(mover==maximisingPlayer))||((childValue<myMax)&&(mover!=maximisingPlayer)))
					{
						myMax = childValue;
					}
				};
				
				value = myMax;
			}
			else
			{
				value = 0;
			}
			
			// Adding entry with heuristic value differences:
			
			final float[] trainingEntry = new float[allHeuristicTerms.length];
			final float oppScoreMultiplier = 1f / numPlayers;	// this gives us nicer heuristics around 0
			
			for (int i=0; i<allHeuristicTerms.length; i++)
			{
				trainingEntry[i] = 0f;
				for (int player = 1; player <= numPlayers; ++player)
				{
					if (player == maximisingPlayer)
						trainingEntry[i] += allHeuristicTerms[i].computeValue(context,player,-1);
					else
						trainingEntry[i] -= allHeuristicTerms[i].computeValue(context,player,-1)*oppScoreMultiplier;
				}
			}
			
			trainingDataX.add(trainingEntry);
			trainingDataY.add(value);
		}
		
		return 1f;
	}
	
}
