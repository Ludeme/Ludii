package optimisers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Can create Optimizers based on strings / files
 * 
 * @author Dennis Soemers
 */
public class OptimiserFactory 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor should not be used.
	 */
	private OptimiserFactory()
	{
		// not intended to be used
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param string String representation of optimizer, 
	 * 	or filename from which to load optimizer
	 * 
	 * @return Created AI
	 */
	public static Optimiser createOptimiser(final String string)
	{
		if (string.equalsIgnoreCase("SGD"))
		{
			return new SGD(0.05f);
		}
		else if (string.equalsIgnoreCase("RMSProp"))
		{
			return new DeepmindRMSProp();
		}
		else if (string.equalsIgnoreCase("AMSGrad"))
		{
			// the Karpathy constant: 
			// https://twitter.com/karpathy/status/801621764144971776
			return new AMSGrad(3E-4f);
		}
		
		// try to interpret the given string as a resource or some other 
		// kind of file
		final URL optimiserURL = OptimiserFactory.class.getResource(string);
		File optimiserFile = null;
		
		if (optimiserURL != null)
		{
			optimiserFile = new File(optimiserURL.getFile());
		} 
		else
		{
			optimiserFile = new File(string);
		}
		
		String[] lines = new String[0];
		
		if (optimiserFile.exists())
		{
			try (BufferedReader reader = new BufferedReader(
					new FileReader(optimiserFile)))
			{
				final List<String> linesList = new ArrayList<String>();
				
				String line = reader.readLine();
				while (line != null)
				{
					linesList.add(line);
				}
				
				lines = linesList.toArray(lines);
			} 
			catch (final IOException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			// assume semicolon-separated lines directly passed as 
			// command line arg
			lines = string.split(";");
		}
		
		final String firstLine = lines[0];
		if (firstLine.startsWith("optimiser="))
		{
			final String optimiserName = 
					firstLine.substring("optimiser=".length());
			
			if (optimiserName.equalsIgnoreCase("SGD"))
			{
				// UCT is the default implementation of MCTS, 
				// so both cases are the same
				return SGD.fromLines(lines);
			}
			else if (optimiserName.equalsIgnoreCase("RMSProp"))
			{
				return DeepmindRMSProp.fromLines(lines);
			}
			else if (optimiserName.equalsIgnoreCase("AMSGrad"))
			{
				return AMSGrad.fromLines(lines);
			}
			else
			{
				System.err.println("Unknown optimizer name: " + optimiserName);
			}
		}
		else
		{
			System.err.println(
					"Expecting Optimizer file to start with \"optimiser=\", "
					+ "but it starts with " + firstLine);
		}
		
		System.err.println(String.format(
				"Warning: cannot convert string \"%s\" to Optimiser; "
				+ "defaulting to vanilla SGD.", 
				string));
		return new SGD(0.05f);
	}
	
	//-------------------------------------------------------------------------

}
