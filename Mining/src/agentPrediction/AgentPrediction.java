package agentPrediction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import game.Game;
import other.concept.Concept;
import other.concept.ConceptComputationType;
import other.concept.ConceptDataType;

public class AgentPrediction 
{

	/**
	 * @return Name of the best predicted agent from our pre-trained set of models.
	 */
	public static String predictBestAgentName(final Game game)
	{
		String sInput = null;
		String sError = null;

        try {
            
        	final String modelName = "RandomForestClassifier";
        	final String conceptNameString = "RulesetName," + compilationConceptNameString();
        	final String conceptValueString = "UNUSED," + compilationConceptValueString(game);
        	
            final Process p = Runtime.getRuntime().exec("python3 ../../LudiiPrivate/DataMiningScripts/Sklearn/GetBestPredictedAgent.py " + modelName + " " + conceptNameString + " " + conceptValueString);

            // Read file output
            final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((sInput = stdInput.readLine()) != null) 
            {
            	// String returned in the form " ['PREDICTEDAGENT=Alpha Beta'] "
            	if (sInput.contains("PREDICTEDAGENT"))
            		return sInput.split("'")[1].split("=")[1];
            }
            
            // Read any errors.
            final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((sError = stdError.readLine()) != null) 
            {
            	System.out.println("Python Error\n");
                System.out.println(sError);
            }
        }
        catch (final IOException e) 
        {
            e.printStackTrace();
        }
    
		return "Random";
	}
	
	/**
	 * @return The concepts as a string with comma between them.
	 */
	public static String compilationConceptNameString()
	{
		final Concept[] concepts = Concept.values();
		final StringBuffer sb = new StringBuffer();
		for(final Concept concept: concepts)
			if(concept.computationType().equals(ConceptComputationType.Compilation))
				sb.append(concept.name()+",");
	
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	/**
	 * @param game The game compiled.
	 * @return The concepts as boolean values with comma between them.
	 */
	public static String compilationConceptValueString(final Game game)
	{
		final Concept[] concepts = Concept.values();
		final StringBuffer sb = new StringBuffer();
		for(final Concept concept: concepts)
			if(concept.computationType().equals(ConceptComputationType.Compilation))
				if(concept.dataType().equals(ConceptDataType.BooleanData))
					sb.append((game.booleanConcepts().get(concept.id()) ? "1" : "0")).append(",");
				else
					sb.append((game.nonBooleanConcepts().get(concept.id()))).append(",");
	
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
}
