package metrics.single;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Percentage of board sites which a piece was placed on at some point.
 * 
 * @author matthew.stephenson
 */
public class BoardCoverage extends Metric
{

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public BoardCoverage()
	{
		super
		(
			"Board Coverage", 
			"Percentage of board sites which a piece was placed on at some point.", 
			"Core Ludii metric.", 
			MetricType.OUTCOMES,
			0.0, 
			1.0,
			0.5,
			Concept.BoardCoverage
		);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		// System.out.println(game.booleanConcepts().get(Concept.Vertex.id()));
		// System.out.println(game.booleanConcepts().get(Concept.Edge.id()));
		// System.out.println(game.booleanConcepts().get(Concept.Cell.id()));
		
		double numSitesCovered = 0;
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			// Get trial and RNG information
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			
			// Setup a new instance of the game
			final Context context = Utils.setupNewContext(game, rngState);
			
			// Record the index of all sites covered in this trial.
			final Set<TopologyElement> sitesCovered = new HashSet<TopologyElement>();
			
			sitesCovered.addAll(Utils.boardSitesCovered(context));
			for (int i = trial.numInitialPlacementMoves(); i < trial.numMoves(); i++)
			{
				context.game().apply(context, trial.getMove(i));
				sitesCovered.addAll(Utils.boardSitesCovered(context));
			}
			
			numSitesCovered += ((double) sitesCovered.size()) / game.board().topology().getAllUsedGraphElements(game).size();
		}

		return numSitesCovered / trials.length;
	}

}
