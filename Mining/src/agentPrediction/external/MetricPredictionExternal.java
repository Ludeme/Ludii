package agentPrediction.external;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import game.Game;
import metrics.Evaluation;
import utils.concepts.ComputePlayoutConcepts;

public class MetricPredictionExternal 
{

	/**
	 * Predict the values for each metric using external Python models.
	 * @param game
	 * @param modelFilePath
	 * @param compilationOnly
	 */
	public static Map<String,Double> predictMetrics(final Game game, final String modelFilePath, final boolean compilationOnly)
	{
		final long startTime = System.currentTimeMillis();
		
		if (!compilationOnly)
			ComputePlayoutConcepts.updateGame(game, new Evaluation(), 10, -1, 1, "Random", true);
		else
			ComputePlayoutConcepts.updateGame(game, new Evaluation(), 0, -1, 1, "Random", true);	// Still need to run this with zero trials to get compilation concepts

		final double ms = (System.currentTimeMillis() - startTime);
		System.out.println("Playouts computation done in " + ms + " ms.");
		
		final List<String> allMetricNames = new ArrayList<>();
		final File folder = new File("../../LudiiPrivate/DataMiningScripts/Sklearn/res/trainedModels/" + modelFilePath);
		final File[] listOfFiles = folder.listFiles();
		for (final File file : listOfFiles)
			allMetricNames.add(file.getName());
		
		return AgentPredictionExternal.predictBestAgentName(game, allMetricNames, modelFilePath, false, compilationOnly);
	}
	
	//-------------------------------------------------------------------------
	
}
