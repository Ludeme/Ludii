package utils;

import java.io.File;

/**
 * Some utilities related to files in experiments
 * 
 * @author Dennis Soemers
 */
public class ExperimentFileUtils 
{
	
	//-------------------------------------------------------------------------
	
	/** Format used for filepaths in sequences of files (with increasing indices) */
	private static final String fileSequenceFormat = "%s_%05d.%s";
	
	//-------------------------------------------------------------------------
	
	private ExperimentFileUtils()
	{
		// should not instantiate
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the filepath of the form {baseFilepath}_{index}.{extension} with
	 * the minimum integer value >= 0 for {index} such that a File with that filepath
	 * does not yet exist.
	 * 
	 * For example, suppose baseFilepath = "/Directory/File", and extension = "csv".
	 * Then, this method will return:
	 * 	- "/Directory/File_0.csv" if that File does not yet exist, or
	 * 	- "/Directory/File_1.csv" if that is the first File that does not yet exist, or
	 * 	- "/Directory/File_2.csv" if that is the first File that does not yet exist, etc.
	 * 
	 * It is assumed that leading zeros have been added to {index} to make sure that it has at least
	 * 5 digits (not included in example above)
	 * 
	 * This method can be used to easily figure out the filepath for the next File to write
	 * to in a sequence of related files (for example, different checkpoints of a learned vector of weights).
	 * 
	 * @param baseFilepath
	 * @param extension
	 * @return Next file path.
	 */
	public static String getNextFilepath(final String baseFilepath, final String extension)
	{
		int index = 0;
		String result = String.format(fileSequenceFormat, baseFilepath, Integer.valueOf(index), extension);
		
		while (new File(result).exists())
		{
			++index;
			result = String.format(fileSequenceFormat, baseFilepath, Integer.valueOf(index), extension);
		}
		
		return result;
	}
	
	/**
	 * Returns the filepath of the form {baseFilepath}_{index}.{extension} with
	 * the maximum integer value >= 0 for {index} such that a File with that filepath
	 * exists, or null if no such File exists. Starts checking for index = 0,
	 * then for index = 1 if 0 already exists, then index = 2, etc. This means
	 * that the method may return incorrect results if files were created with an
	 * index sequence different from 0, 1, 2, ...
	 * 
	 * For example, suppose baseFilepath = "/Directory/File", and extension = "csv".
	 * Then, this method will return:
	 * 	- "/Directory/File_0.csv" if 0 is the greatest index such that such a File exists, or
	 * 	- "/Directory/File_1.csv" if 1 is the greatest index such that such a File exists, or
	 * 	- "/Directory/File_2.csv" if that file also exists, etc.
	 * 
	 * Leading zeros will be added to {index} to make sure that it has at least
	 * 5 digits (not included in example above)
	 * 
	 * This method can be used to easily get the latest file in a sequence of related files
	 * (for example, different checkpoints of a learned vector of weights), or null if
	 * we do not yet have any such files.
	 * 
	 * @param baseFilepath
	 * @param extension
	 * @return Last file path.
	 */
	public static String getLastFilepath(final String baseFilepath, final String extension)
	{
		int index = 0;
		String result = null;
		
		while (new File(String.format(fileSequenceFormat, baseFilepath, Integer.valueOf(index), extension)).exists())
		{
			result = String.format(fileSequenceFormat, baseFilepath, Integer.valueOf(index), extension);
			++index;
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------

}
