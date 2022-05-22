package function_approx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import features.FeatureVector;
import features.WeightVector;
import gnu.trove.list.array.TFloatArrayList;
import main.collections.FVector;

/**
 * A linear function approximator
 * 
 * @author Dennis Soemers
 */
public class LinearFunction 
{
	
	//-------------------------------------------------------------------------
	
	/** Our vector of parameters / weights */
	protected WeightVector theta;
	
	/** Filepath for feature set corresponding to our parameters */
	protected String featureSetFile = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param theta
	 */
	public LinearFunction(final WeightVector theta)
	{
		this.theta = theta;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param featureVector
	 * @return Predicted value for a given feature vector
	 */
	public float predict(final FeatureVector featureVector)
	{
		return effectiveParams().dot(featureVector);
	}
	
	/**
	 * @return Vector of effective parameters, used for making predictions. For this
	 *         class, a reference to theta.
	 */
	public WeightVector effectiveParams()
	{
		return theta;
	}

	/**
	 * @return Reference to parameters vector that we can train. For this class,
	 *         a reference to theta.
	 */
	public WeightVector trainableParams()
	{
		return theta;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Replaces the linear function's param vector theta
	 * @param newTheta
	 */
	public void setTheta(final WeightVector newTheta)
	{
		theta = newTheta;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Filename for corresponding Feature Set
	 */
	public String featureSetFile()
	{
		return featureSetFile;
	}
	
	/**
	 * Sets the filename for the corresponding Feature Set
	 * @param featureSetFile
	 */
	public void setFeatureSetFile(final String featureSetFile)
	{
		this.featureSetFile = featureSetFile;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Writes linear function to the given filepath
	 * @param filepath
	 * @param featureSetFiles
	 */
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
		} 
		catch (final IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @param filepath
	 * @return Reads linear function from the given filepath
	 */
	public static LinearFunction fromFile(final String filepath)
	{
		try 
		(
			final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), "UTF-8"))
		)
		{
			final TFloatArrayList readFloats = new TFloatArrayList();
			String featureSetFile = null;
			
			while (true)
			{
				final String line = reader.readLine();
				
				if (line == null)
					break;
				
				if (line.startsWith("FeatureSet="))
					featureSetFile = line.substring("FeatureSet=".length());
				else
					readFloats.add(Float.parseFloat(line));
			}
			
			float[] floats = new float[readFloats.size()];
			
			for (int i = 0; i < floats.length; ++i)
			{
				floats[i] = readFloats.getQuick(i);
			}
			
			final LinearFunction func = new LinearFunction(new WeightVector(FVector.wrap(floats)));
			func.setFeatureSetFile(featureSetFile);
			
			return func;
		} 
		catch (final Exception e) 
		{
			System.err.println("exception while trying to load from filepath: " + filepath);
			e.printStackTrace();
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------

}
