package agentPrediction.external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import game.Game;
import manager.Manager;
import manager.ai.AIUtil;
import other.concept.Concept;
import other.concept.ConceptComputationType;
import other.concept.ConceptDataType;

public class AgentPredictionExternal 
{

	//-------------------------------------------------------------------------
	
	public static void predictBestAgent(final Manager manager, final String modelName, final int playerIndexToUpdate, final boolean classificationModel)
	{
		final String bestPredictedAgentName = AgentPredictionExternal.predictBestAgentName(manager, modelName, classificationModel);
		
		manager.getPlayerInterface().selectAnalysisTab();
		manager.getPlayerInterface().addTextToAnalysisPanel("Best Predicted Agent is " + bestPredictedAgentName + "\n");
		manager.getPlayerInterface().addTextToAnalysisPanel("//-------------------------------------------------------------------------\n");
		
		final JSONObject json = new JSONObject().put("AI",
				new JSONObject()
				.put("algorithm", bestPredictedAgentName)
				);
		
		if (playerIndexToUpdate > 0)
			AIUtil.updateSelectedAI(manager, json, playerIndexToUpdate, bestPredictedAgentName);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Name of the best predicted agent from our pre-trained set of models.
	 */
	private static String predictBestAgentName(final Manager manager, final String modelName, final boolean classificationModel)
	{
		final Game game  = manager.ref().context().game();
		String sInput = null;
		String sError = null;

        try 
        {
        	final String conceptNameString = "RulesetName," + compilationConceptNameString();
        	final String conceptValueString = "UNUSED," + compilationConceptValueString(game);
        	
        	// Classification prediction, just the agent name.
        	if (classificationModel)
        	{
	            final Process p = Runtime.getRuntime().exec("python3 ../../LudiiPrivate/DataMiningScripts/Sklearn/GetBestPredictedAgent.py " + modelName + " " + "Classification" + " " + conceptNameString + " " + conceptValueString);
	
	            // Read file output
	            final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
	            while ((sInput = stdInput.readLine()) != null) 
	            {
	            	System.out.println(sInput);
	            	if (sInput.contains("PREDICTION"))
	            		return sInput.split("=")[1];
	            }
	            
	            // Read any errors.
	            final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	            while ((sError = stdError.readLine()) != null) 
	            {
	            	System.out.println("Python Error\n");
	                System.out.println(sError);
	            }
        	}
        	// Regression prediction, get the predicted value for each valid agent.
        	else
        	{
        		// Record the predicted value for each agent.
        		final List<String> allValidAgentNames = AIUtil.allValidAgentNames(game);
        		final ArrayList<Double> allValidAgentPredictedValues = new ArrayList<>();
        		for (final String agentName : allValidAgentNames)
        		{
        			 final Process p = Runtime.getRuntime().exec("python3 ../../LudiiPrivate/DataMiningScripts/Sklearn/GetBestPredictedAgent.py " + modelName + " " + agentName.replaceAll(" ", "_") + " " + conceptNameString + " " + conceptValueString);
        				
     	            // Read file output
     	            final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
     	            System.out.println("Predicting for " + agentName);
     	            while ((sInput = stdInput.readLine()) != null) 
     	            {
     	            	System.out.println(sInput);
     	            	if (sInput.contains("PREDICTION"))
     	            	{
     	            		final Double predictedValue = Double.valueOf(sInput.split("=")[1]);
     	            		allValidAgentPredictedValues.add(predictedValue);
     	            		manager.getPlayerInterface().addTextToAnalysisPanel("Predicted win-rate for " + agentName + ": " + predictedValue + "\n");
     	            	}
     	            }
     	            
     	            // Read any errors.
     	            final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
     	            while ((sError = stdError.readLine()) != null) 
     	            {
     	            	System.out.println("Python Error\n");
     	                System.out.println(sError);
     	            }
        		}
        		
        		// Select the agent with the best predicted score.
        		String bestAgentName = "Random";
        		double bestPredictedValue = -1.0;
        		for (int i = 0; i < allValidAgentNames.size(); i++)
        		{
        			if (allValidAgentPredictedValues.get(i) > bestPredictedValue)
        			{
        				bestAgentName = allValidAgentNames.get(i);
        				bestPredictedValue = allValidAgentPredictedValues.get(i);
        			}
        		}
        		return bestAgentName;
        	}
        }
        catch (final IOException e) 
        {
            e.printStackTrace();
        }
    
		return "Random";
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The concepts as a string with comma between them.
	 */
	private static String compilationConceptNameString()
	{
		final Concept[] concepts = Concept.values();
		final StringBuffer sb = new StringBuffer();
		for(final Concept concept: concepts)
			if(concept.computationType().equals(ConceptComputationType.Compilation))
				sb.append(concept.name()+",");
	
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game The game compiled.
	 * @return The concepts as boolean values with comma between them.
	 */
	private static String compilationConceptValueString(final Game game)
	{
		final Concept[] concepts = Concept.values();
		final StringBuffer sb = new StringBuffer();
		for(final Concept concept: concepts)
			if(concept.computationType().equals(ConceptComputationType.Compilation))
				if(concept.dataType().equals(ConceptDataType.BooleanData))
					sb.append((game.booleanConcepts().get(concept.id()) ? "1" : "0")).append(",");
				else
					sb.append((game.nonBooleanConcepts().get(Integer.valueOf(concept.id())))).append(",");
	
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
