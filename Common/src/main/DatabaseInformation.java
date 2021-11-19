package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInformation
{

	//-------------------------------------------------------------------------
	
	private static final String RESOURCE_PATH = "/help/GameRulesets.csv";
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param ruleset
	 * @return the database name for a given ruleset.
	 */
	public static String getRulesetDBName(final String rulesetHeading)
	{
		try
		{
			final String[] rulesetNameArray = rulesetHeading.split("[/(]");
			String rulesetNameString = "";
			for (int i = 1; i < rulesetNameArray.length-1; i++)
				rulesetNameString += rulesetNameArray[i] + "(";
			rulesetNameString = rulesetNameString.substring(0, rulesetNameString.length()-1);
			return rulesetNameString.trim();
		}
		catch (final Exception e)
		{
			return rulesetHeading;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param gameName
	 * @param rulesetName
	 * @return the database Id for a given ruleset.
	 */
	public static int getRulesetId(final String gameName, final String rulesetName)
	{
		try (final InputStream resource = DatabaseInformation.class.getResourceAsStream(RESOURCE_PATH))
		{
			if (resource != null) 
			{
				try 
				(
					final InputStreamReader isr = new InputStreamReader(resource, "UTF-8");
					final BufferedReader rdr = new BufferedReader(isr)
				)
				{
					final List<Integer> allRulesetIdsForGame = new ArrayList<>();
					
					String line;
					while ((line = rdr.readLine()) != null)
					{
						final String[] lineArray = line.replaceAll("\"", "").split(",");
						
						if (lineArray[1].equals(gameName))
						{
							allRulesetIdsForGame.add(Integer.valueOf(lineArray[2]));
						
							if (lineArray[3].equals(rulesetName) || lineArray[3].equals(getRulesetDBName(rulesetName)))
								return Integer.valueOf(lineArray[2]);		
						}
					}
					
					// Check if there is only one ruleset for this game.
					if (rulesetName.length() == 0 && allRulesetIdsForGame.size() == 1)
						return allRulesetIdsForGame.get(0);
				}
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		return -1;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param gameName
	 * @return the database Id for a given game.
	 */
	public static int getGameId(final String gameName)
	{
		try (final InputStream resource = DatabaseInformation.class.getResourceAsStream(RESOURCE_PATH))
		{
			if (resource != null) 
			{
				try 
				(
					final InputStreamReader isr = new InputStreamReader(resource, "UTF-8");
					final BufferedReader rdr = new BufferedReader(isr)
				)
				{
					String line;
					while ((line = rdr.readLine()) != null)
					{
						final String[] lineArray = line.replaceAll("\"", "").split(",");
						
						if (lineArray[1].equals(gameName))
							return Integer.valueOf(lineArray[0]);
					}
				}
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		return -1;
	}
	
	//-------------------------------------------------------------------------
	
}
