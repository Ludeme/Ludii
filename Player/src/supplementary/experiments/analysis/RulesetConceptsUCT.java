package supplementary.experiments.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import gnu.trove.list.array.TDoubleArrayList;
import main.collections.ArrayUtils;

/**
 * Helper class to load and use the data stored in our RulesetConceptsUCT.csv file.
 * Note that this file is in a private repo, so users with only the public repo
 * will be unable to load this.
 * 
 * @author Dennis Soemers
 */
public class RulesetConceptsUCT 
{
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Filepath for our CSV file. Since this only works with access to private repo 
	 * anyway, the filepath has been hardcoded for use from Eclipse.
	 * 
	 * This would usually be private and final, but making it public and non-final
	 * is very useful for editing the filepath when running on cluster (where LudiiPrivate
	 * is not available).
	 */
	public static String FILEPATH = "../../LudiiPrivate/DataMiningScripts/Sklearn/res/Input/RulesetConceptsUCT.csv";
	
	/** Names of our columns */
	private static String[] columnNames = null;
	
	/** Map to store all our data, from ruleset names to vectors of values */
	private static Map<String, TDoubleArrayList> map = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * No constructor
	 */
	private RulesetConceptsUCT()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param rulesetName
	 * @param columnName
	 * @return Value for given ruleset name and column name
	 */
	public static double getValue(final String rulesetName, final String columnName)
	{
		if (map == null)
			loadData();
		
		return getValue(rulesetName, ArrayUtils.indexOf(columnName, columnNames));
	}
	
	/**
	 * @param rulesetName
	 * @param columnIdx
	 * @return Value for given ruleset name and column index
	 */
	public static double getValue(final String rulesetName, final int columnIdx)
	{
		if (map == null)
			loadData();
		
		final TDoubleArrayList vector = map.get(rulesetName);
		if (vector == null)
		{
			System.out.println("no data for " + rulesetName);
			return Double.NaN;
		}
		
		return vector.getQuick(columnIdx);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Load our data from the CSV file
	 */
	private static void loadData()
	{
		try (final BufferedReader reader = new BufferedReader(new FileReader(new File(FILEPATH))))
		{
			columnNames = reader.readLine().split(Pattern.quote(","));
			map = new HashMap<String, TDoubleArrayList>();
			
			for (String line; (line = reader.readLine()) != null; /**/)
			{
				final String[] lineSplit = line.split(Pattern.quote(","));
				final String rulesetName = lineSplit[0];
				
				final TDoubleArrayList vector = new TDoubleArrayList();
				for (int i = 1; i < lineSplit.length; ++i)
				{
					if (lineSplit[i].isEmpty())
						vector.add(Double.NaN);
					else if (lineSplit[i].equals("null"))
						vector.add(Double.NaN);
					else
						vector.add(Double.parseDouble(lineSplit[i]));
				}
				map.put(rulesetName, vector);
			}
		} 
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

}
