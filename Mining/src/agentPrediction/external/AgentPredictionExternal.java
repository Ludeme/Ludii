package agentPrediction.external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import manager.ai.AIRegistry;
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
	 * @param game
	 * @param modelFilePath
	 * @param classificationModel
	 * @param heuristics
	 * @param compilationOnly
	 */
	public static Map<String,Double> predictBestAgent(final Game game, final String modelFilePath, final boolean classificationModel, final boolean heuristics, final boolean compilationOnly)
	{
		final long startTime = System.currentTimeMillis();
		
		if (!compilationOnly)
			ComputePlayoutConcepts.updateGame(game, new Evaluation(), 10, -1, 1, "Random", true);
		else
			ComputePlayoutConcepts.updateGame(game, new Evaluation(), 0, -1, 1, "Random", true);	// Still need to run this with zero trials to get compilation concepts

		final double ms = (System.currentTimeMillis() - startTime);
		System.out.println("Playouts computation done in " + ms + " ms.");

		List<String> allModelNames = AIRegistry.generateValidAgentNames(game);
		if (heuristics)
			allModelNames = Arrays.asList(AIUtils.allHeuristicNames());
		
		return AgentPredictionExternal.predictBestAgentName(game, allModelNames, modelFilePath, classificationModel, compilationOnly);
		
//		if (!heuristics)
//		{
//			if (playerIndexToUpdate > 0)
//			{
//				final JSONObject json = new JSONObject().put("AI",
//						new JSONObject()
//						.put("algorithm", bestPredictedAgentName)
//						);
//				
//				AIUtil.updateSelectedAI(manager, json, playerIndexToUpdate, bestPredictedAgentName);
//			}
//		}
//		else
//		{
//			if (manager.aiSelected()[playerIndexToUpdate].ai() != null)
//			{
//				final Heuristics heuristic = AIUtils.convertStringtoHeuristic(bestPredictedAgentName);
//				manager.aiSelected()[playerIndexToUpdate].ai().setHeuristics(heuristic);
//				manager.aiSelected()[playerIndexToUpdate].ai().initAI(manager.ref().context().game(), playerIndexToUpdate);
//			}
//		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Name of the best predicted agent from our pre-trained set of models.
	 */
	public static Map<String,Double> predictBestAgentName(final Game game, final List<String> allValidLabelNames, final String modelFilePath, final boolean classificationModel, final boolean compilationOnly)
	{
		final Map<String, Double> agentPredictions = new HashMap<>();
		
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
	            			
	            			for (int i = 0; i < classNames.length; i++)
	            			{
	            				agentPredictions.put(classNames[i], values[i]);
	            			}
	            			return agentPredictions;
	            		}
	            		catch (final Exception e)
	            		{
	            			e.printStackTrace();
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
     	            		agentPredictions.put(agentName, predictedValue);
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

        		return agentPredictions;
        	}
        }
        catch (final IOException e) 
        {
            e.printStackTrace();
        }
    
		return agentPredictions;
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
	
	/**
	 * @param modelName should be the name of the model to use (same as from GUI), e.g. "BayesianRidge"
	 * @param useClassifier
	 * @param useHeuristics
	 * @param useCompilationOnly
	 */
	public static String getModelPath(final String modelName, final boolean useClassifier, final boolean useHeuristics, final boolean useCompilationOnly)
	{
		String modelFilePath = modelName;
		if (useClassifier)
			modelFilePath += "-Classification";
		else
			modelFilePath += "-Regression";
		if (useHeuristics)
			modelFilePath += "-Heuristics";
		else
			modelFilePath += "-Agents";
		if (useCompilationOnly)
			modelFilePath += "-True";
		else
			modelFilePath += "-False";
		
		return modelFilePath;
	}
	
	//-------------------------------------------------------------------------
	
}
