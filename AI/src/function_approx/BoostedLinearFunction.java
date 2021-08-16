package function_approx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import features.WeightVector;
import gnu.trove.list.array.TFloatArrayList;
import main.collections.FVector;

/**
 * A linear function approximator that uses another linear function for boosting
 * (the effective params of this approximator are the sum of the trainable
 * params and the effective params of the boosting function).
 * 
 * @author Dennis Soemers
 */
public class BoostedLinearFunction extends LinearFunction
{

	//-------------------------------------------------------------------------

	/** Function of which we use the effective params for boosting */
	protected final LinearFunction booster;

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param theta Trainable parameters vector
	 * @param booster Linear function of which we add the parameters to our trainable parameters
	 */
	public BoostedLinearFunction(final WeightVector theta, final LinearFunction booster)
	{
		super(theta);
		this.booster = booster;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Vector of effective parameters, used for making predictions. For this
	 *         class, the trainable params plus the effective params of the booster.
	 */
	@Override
	public WeightVector effectiveParams()
	{
		final FVector params = booster.effectiveParams().allWeights().copy();
		params.add(trainableParams().allWeights());
		return new WeightVector(params);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Writes Linear function to the given filepath.
	 */
	@Override
	public void writeToFile(final String filepath, final String[] featureSetFiles)
	{
		try (final PrintWriter writer = new PrintWriter(filepath, "UTF-8"))
		{
			for (int i = 0; i < theta.allWeights().dim(); ++i)
			{
				writer.println(theta.allWeights().get(i));
			}
			
			for (final String fsf : featureSetFiles)
			{
				writer.println("FeatureSet=" + new File(fsf).getName());
			}
			
			writer.println("Effective Params:");
			final FVector effectiveParams = effectiveParams().allWeights();
			for (int i = 0; i < effectiveParams.dim(); ++i)
			{
				writer.println(effectiveParams.get(i));
			}
		} 
		catch (final IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @param filepath 
	 * @param booster 
	 * @return Reads linear function from the given filepath.
	 */
	public static BoostedLinearFunction boostedFromFile(final String filepath, final LinearFunction booster)
	{
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(filepath), "UTF-8")))
		{
			final TFloatArrayList readFloats = new TFloatArrayList();
			String featureSetFile = null;
			String line;
			
			while (true)
			{
				line = reader.readLine();
				
				if (line == null)
				{
					break;
				}
				
				if (line.startsWith("FeatureSet="))
				{
					featureSetFile = line.substring("FeatureSet=".length());
				}
				else if (line.equals("Effective Params:"))
				{
					break;
				}
				else
				{
					readFloats.add(Float.parseFloat(line));
				}
			}
			
			float[] floats = new float[readFloats.size()];
			
			for (int i = 0; i < floats.length; ++i)
			{
				floats[i] = readFloats.getQuick(i);
			}
			
			LinearFunction boosterFunc = booster;
			if (boosterFunc == null)
			{
				// Don't have a booster, so create a dummy linear function as booster
				// such that the total effective params remain the same
				
				final TFloatArrayList effectiveParams = new TFloatArrayList();
				
				// we're first expecting a line saying "Effective Params:"
				if (!line.equals("Effective Params:"))
				{
					System.err.println("Error in BoostedLinearFunction::boostedFromFile file! "
							+ "Expected line: \"Effective Params:\"");
				}
				
				line = reader.readLine();
				
				while (line != null)
				{
					effectiveParams.add(Float.parseFloat(line));
					line = reader.readLine();
				}
				
				float[] boosterFloats = new float[effectiveParams.size()];
				
				for (int i = 0; i < boosterFloats.length; ++i)
				{
					boosterFloats[i] = effectiveParams.getQuick(i) - floats[i];
				}
				
				boosterFunc = new LinearFunction(new WeightVector(FVector.wrap(boosterFloats)));
			}
			
			final BoostedLinearFunction func = new BoostedLinearFunction(new WeightVector(FVector.wrap(floats)), boosterFunc);
			func.setFeatureSetFile(featureSetFile);
			
			return func;
		} 
		catch (final IOException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------

}
