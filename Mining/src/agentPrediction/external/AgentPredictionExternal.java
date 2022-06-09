package agentPrediction.external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import game.Game;
import manager.Manager;
import manager.ai.AIRegistry;
import manager.ai.AIUtil;
import metadata.ai.heuristics.Heuristics;
import metrics.Evaluation;
import other.concept.Concept;
import other.concept.ConceptComputationType;
import other.concept.ConceptDataType;
import utils.AIUtils;
import utils.concepts.ComputePlayoutConcepts;

/**
 * Predict the best agent/heuristic using external python models.
 * 
 * Models are produced by the "generate_agent_heuristic_prediction_models()" function in the Sklearn "Main.py" file.
 *
 * @author Matthew.Stephenson
 */
public class AgentPredictionExternal 
{

	/**
	 * Predict the best agent/heuristic using external python models.
	 * @param manager
	 * @param modelFilePath
	 * @param playerIndexToUpdate
	 * @param classificationModel
	 * @param heuristics
	 * @param compilationOnly
	 */
	public static void predictBestAgent(final Manager manager, final String modelFilePath, final int playerIndexToUpdate, final boolean classificationModel, final boolean heuristics, final boolean compilationOnly)
	{
		final Game game = manager.ref().context().game();
		
		final long startTime = System.currentTimeMillis();
		
		if (!compilationOnly)
			ComputePlayoutConcepts.updateGame(game, new Evaluation(), 10, -1, 1, "Random", true);

		final double ms = (System.currentTimeMillis() - startTime);
		System.out.println("Playouts computation done in " + ms + " ms.");

		List<String> allModelNames = AIRegistry.generateValidAgentNames(game);
		if (heuristics)
			allModelNames = Arrays.asList(AIUtils.allHeuristicNames());
		
		final String bestPredictedAgentName = AgentPredictionExternal.predictBestAgentName(manager, allModelNames, modelFilePath, classificationModel, compilationOnly);
		
		manager.getPlayerInterface().selectAnalysisTab();
		manager.getPlayerInterface().addTextToAnalysisPanel("Best Predicted Agent/Heuristic is " + bestPredictedAgentName + "\n");
		manager.getPlayerInterface().addTextToAnalysisPanel("//-------------------------------------------------------------------------\n");
		
		if (!heuristics)
		{
			if (playerIndexToUpdate > 0)
			{
				final JSONObject json = new JSONObject().put("AI",
						new JSONObject()
						.put("algorithm", bestPredictedAgentName)
						);
				
				AIUtil.updateSelectedAI(manager, json, playerIndexToUpdate, bestPredictedAgentName);
			}
		}
		else
		{
			if (manager.aiSelected()[playerIndexToUpdate].ai() != null)
			{
				final Heuristics heuristic = AIUtils.convertStringtoHeuristic(bestPredictedAgentName);
				manager.aiSelected()[playerIndexToUpdate].ai().setHeuristics(heuristic);
				manager.aiSelected()[playerIndexToUpdate].ai().initAI(manager.ref().context().game(), playerIndexToUpdate);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Name of the best predicted agent from our pre-trained set of models.
	 */
	private static String predictBestAgentName(final Manager manager, final List<String> allValidLabelNames, final String modelFilePath, final boolean classificationModel, final boolean compilationOnly)
	{
		final Game game  = manager.ref().context().game();
		String sInput = null;
		String sError = null;

        try 
        {
        	final String conceptNameString = "RulesetName," + conceptNameString(compilationOnly);
        	final String conceptValueString = "UNUSED," + conceptValueString(game, compilationOnly);
        	
        	// Classification prediction, just the agent name or probability for each.
        	if (classificationModel)
        	{
        		final String arg1 = modelFilePath;
        		final String arg2 = "Classification";
        		final String arg3 = conceptNameString;
        		final String arg4 = conceptValueString;
	            final Process p = Runtime.getRuntime().exec("python3 ../../LudiiPrivate/DataMiningScripts/Sklearn/External/GetBestPredictedAgent.py " + arg1 + " " + arg2 + " " + arg3 + " " + arg4);
	
	            // Read file output
	            final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
	            while ((sInput = stdInput.readLine()) != null) 
	            {
	            	System.out.println(sInput);
	            	if (sInput.contains("PREDICTION"))
	            	{
	            		// Check if returning probabilities for each class.
	            		try
	            		{
	            			final String[] classNamesAndProbas = sInput.split("=")[1].split("_:_");
	            			final String[] classNames = classNamesAndProbas[0].split("_;_");
	            			for (int i = 0; i < classNames.length; i++)
	            				classNames[i] = classNames[i];
	            			final String[] valueStrings = classNamesAndProbas[1].split("_;_");
	            			final Double[] values = new Double[valueStrings.length];
	            			for (int i = 0; i < valueStrings.length; i++)
	            				values[i] = Double.valueOf(valueStrings[i]);
	            			if (classNames.length != values.length)
	            				System.out.println("ERROR! Class Names and Values should be the same length.");
	            			
	            			double highestProbabilityValue = -1.0;
	            			String highestProbabilityName = "Random";
	            			for (int i = 0; i < classNames.length; i++)
	            			{
	            				manager.getPlayerInterface().addTextToAnalysisPanel("Predicted probability for " + classNames[i] + ": " + values[i] + "\n");
	            				if (values[i].doubleValue() > highestProbabilityValue)
	            				{
	            					highestProbabilityValue = values[i].doubleValue();
	            					highestProbabilityName = classNames[i];
	            				}
	            			}
	            			
	            			return highestProbabilityName;
	            		}
	            		catch (final Exception e)
	            		{
	            			return sInput.split("=")[1];
	            		}
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
        	
        	// Regression prediction, get the predicted value for each valid agent.
        	else
        	{
        		// Record the predicted value for each agent.
        		final ArrayList<Double> allValidAgentPredictedValues = new ArrayList<>();
        		for (final String agentName : allValidLabelNames)
        		{
        			final String arg1 = modelFilePath;
            		final String arg2 =  agentName.replaceAll(" ", "_");
            		final String arg3 = conceptNameString;
            		final String arg4 =  conceptValueString;
            		final Process p = Runtime.getRuntime().exec("python3 ../../LudiiPrivate/DataMiningScripts/Sklearn/External/GetBestPredictedAgent.py " + arg1 + " " + arg2 + " " + arg3 + " " + arg4);
            		
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
        		for (int i = 0; i < allValidLabelNames.size(); i++)
        		{
        			if (allValidAgentPredictedValues.get(i).doubleValue() > bestPredictedValue)
        			{
        				bestAgentName = allValidLabelNames.get(i);
        				bestPredictedValue = allValidAgentPredictedValues.get(i).doubleValue();
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
	public static String conceptNameString(final boolean compilationOnly)
	{
		final Concept[] concepts = compilationOnly ? Concept.values() : Concept.portfolioConcepts();
		final StringBuffer sb = new StringBuffer();
		for (final Concept concept: concepts)
			if (!compilationOnly || concept.computationType().equals(ConceptComputationType.Compilation))
				sb.append(concept.name()+",");
	
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game The game compiled.
	 * @return The concepts as boolean values with comma between them.
	 */
	public static String conceptValueString(final Game game, final boolean compilationOnly)
	{
		final Concept[] concepts = compilationOnly ? Concept.values() : Concept.portfolioConcepts();
		final StringBuffer sb = new StringBuffer();
		for (final Concept concept: concepts)
			if (!compilationOnly || concept.computationType().equals(ConceptComputationType.Compilation))
				if (concept.dataType().equals(ConceptDataType.BooleanData))
					sb.append((game.booleanConcepts().get(concept.id()) ? "1" : "0")).append(",");
				else
					sb.append((game.nonBooleanConcepts().get(Integer.valueOf(concept.id())))).append(",");
	
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
