package agentPrediction.internal;

import java.util.ArrayList;
import java.util.List;

import agentPrediction.internal.models.BaseModel;
import gnu.trove.list.array.TIntArrayList;
import manager.Manager;
import other.concept.Concept;

public class AgentPredictionInternal
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Predicts the best AI, from a given prediction model.
	 */
	public static void predictAI(final Manager manager, final BaseModel predictionModel)
	{
		manager.getPlayerInterface().selectAnalysisTab();
		final String[] agentStrings = {"AlphaBeta", "MC-GRAVE", "Random", "UCT"};
		
		// Record all the concept Names and Values
		final List<String> flags = new ArrayList<String>();
		final TIntArrayList flagsValues = new TIntArrayList();
		for (final Concept concept : Concept.values())
		{
			flags.add(concept.name());
			flagsValues.add(concept.id());
		}
		
		// Calculate the predicted score for each agent.
		final double[] agentPredictions = predictionModel.predictAI(manager, flags, flagsValues, agentStrings);
			
		// Give best agent prediction.
		double bestAgentScore = -999999999;
		String bestAgentName = "None";
		for (int agentIndex = 0; agentIndex < agentStrings.length; agentIndex++)
		{
			if (agentPredictions[agentIndex] > bestAgentScore)
			{
				bestAgentScore = agentPredictions[agentIndex];
				bestAgentName = agentStrings[agentIndex];
			}
			manager.getPlayerInterface().addTextToAnalysisPanel("Predicted win-rate for " + agentStrings[agentIndex] + ": " + agentPredictions[agentIndex] + "\n");
		}
		
		manager.getPlayerInterface().addTextToAnalysisPanel("Best predicted agent is " + bestAgentName + ", with a win-rate of " + bestAgentScore + "\n");
	}
	
	//-------------------------------------------------------------------------

}
