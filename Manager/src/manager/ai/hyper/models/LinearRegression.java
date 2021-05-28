package manager.ai.hyper.models;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import gnu.trove.list.array.TIntArrayList;
import manager.Manager;

public class LinearRegression implements BaseModel
{

	//-------------------------------------------------------------------------
	
	@Override
	public String modelName() 
	{
		return "LinearRegression";
	}

	//-------------------------------------------------------------------------
	
	@Override
	public double[] predictAI(final Manager manager, final List<String> flags, final TIntArrayList flagsValues, final String[] agentStrings) 
	{
		final double[] agentPredictions = {0.0, 0.0, 0.0, 0.0};

		for (int agentIndex = 0; agentIndex < agentStrings.length; agentIndex++)
		{
			double predictedScore = 0;
			
			// Open CSV, and record all the values.
			final Map<String, Double> entries = new HashMap<String, Double>();
			
			// Load the csv model file.
			final String filePath = "/" + modelName() + "/" + agentStrings[agentIndex] + ".csv";
			System.out.println(filePath);
			final InputStream in = LinearRegression.class.getResourceAsStream(filePath);
			
			if (in != null)
			{
				try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8")))
				{
					for (String line; (line = reader.readLine()) != null; /**/)
					{
						final String[] lineSplit = line.split(Pattern.quote(","));
						if (lineSplit.length > 1)
							entries.put(lineSplit[1], Double.valueOf(lineSplit[0]));
						else
							predictedScore = Double.valueOf(lineSplit[0]).doubleValue();
					}
				} 
				catch (final IOException e)
				{
					e.printStackTrace();
				}
				
				// Calculate the score for this agent.
				for (final Map.Entry<String,Double> entry : entries.entrySet()) 
				{
					for (int i = 0; i < flags.size(); i++)
					{
						if (flags.get(i).equals(entry.getKey()))
						{
							final int flagIndex = flagsValues.get(i);
							final int flagValue = manager.ref().context().game().booleanConcepts().get(flagIndex) ? 1 : 0;
							predictedScore += entry.getValue().doubleValue() * flagValue;
							break;
						}
					}
				}
			}
			else
			{
				System.out.println("Failed to load agent prediction CSV for " + modelName() + ", " + agentStrings[agentIndex]);
			}

			agentPredictions[agentIndex] = predictedScore;
		}
		
		return agentPredictions;
	}
	
	//-------------------------------------------------------------------------

}
