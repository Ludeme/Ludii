package agentPrediction.external;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import manager.Manager;
import metrics.Evaluation;
import utils.concepts.ComputePlayoutConcepts;

public class MetricPredictionExternal 
{

	public static void predictMetrics(final Manager manager, final String modelFilePath, final boolean compilationOnly)
	{
		final Game game = manager.ref().context().game();
		
		final long startTime = System.currentTimeMillis();
		
		if (!compilationOnly)
			ComputePlayoutConcepts.updateGame(game, new Evaluation(), 10, -1, 1, "Random", true);

		final double ms = (System.currentTimeMillis() - startTime);
		System.out.println("Playouts computation done in " + ms + " ms.");
		
		final List<String> allMetricNames = new ArrayList<>();
		final File folder = new File("../../LudiiPrivate/DataMiningScripts/Sklearn/res/trainedModels/" + modelFilePath);
		final File[] listOfFiles = folder.listFiles();
		for (final File file : listOfFiles)
			allMetricNames.add(file.getName());
		
		final String bestPredictedAgentName = AgentPredictionExternal.predictBestAgentName(manager, allMetricNames, modelFilePath, false, compilationOnly);
		
		manager.getPlayerInterface().selectAnalysisTab();
		manager.getPlayerInterface().addTextToAnalysisPanel("Best Predicted Agent/Heuristic is " + bestPredictedAgentName + "\n");
		manager.getPlayerInterface().addTextToAnalysisPanel("//-------------------------------------------------------------------------\n");
	}
	
	//-------------------------------------------------------------------------
	
}
