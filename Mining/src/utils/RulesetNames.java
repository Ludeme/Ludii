package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.options.Ruleset;

/**
 * Helper class to extract standardised (game+ruleset) names from Game objects,
 * following this format:
 * 
 * - Amazons_Default
 * - Asalto_Asalto
 * - Alquerque_Murray
 * 
 * @author Dennis Soemers
 */
public class RulesetNames 
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
	public static String FILEPATH = "../../Ludii/Mining/res/concepts/input/GameRulesets.csv";

	/** List of game names loaded from CSV */
	private static List<String> gameNames = null;
	
	/** List of ruleset names loaded from CSV */
	private static List<String> rulesetNames = null;

	//-------------------------------------------------------------------------

	/**
	 * No constructor
	 */
	private RulesetNames()
	{
		// Do nothing
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return Game+ruleset name in format GameName_RulesetName
	 */
	public static String gameRulesetName(final Game game)
	{
		if (gameNames == null)
			loadData();
		
		final Ruleset ruleset = game.getRuleset();
		final String rulesetName = (ruleset == null) ? null : ruleset.heading();
		
		if (rulesetName == null)
		{
			for (int i = 0; i < gameNames.size(); i++)
				if (gameNames.get(i).equals(game.name()))
					return 
							(gameNames.get(i) + "_" + rulesetNames.get(i))
							.replaceAll(Pattern.quote(" "), "_")
							.replaceAll(Pattern.quote("("), "")
							.replaceAll(Pattern.quote(")"), "")
							.replaceAll(Pattern.quote("'"), "");
		}
		else
		{
			final String nameRuleset = ruleset.heading();
			final String startString = "Ruleset/";
			final String nameRulesetCSV = 
					nameRuleset.substring(startString.length(), ruleset.heading().lastIndexOf('(') - 1);
			
			return 
					(game.name() + "_" + nameRulesetCSV)
					.replaceAll(Pattern.quote(" "), "_")
					.replaceAll(Pattern.quote("("), "")
					.replaceAll(Pattern.quote(")"), "")
					.replaceAll(Pattern.quote("'"), "");
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Load our data from the CSV file
	 */
	private static void loadData()
	{
		try (final BufferedReader reader = new BufferedReader(new FileReader(new File(FILEPATH))))
		{
			gameNames = new ArrayList<String>();
			rulesetNames = new ArrayList<String>();
			
			for (String line; (line = reader.readLine()) != null; /**/)
			{
				final String[] lineSplit = line.split(Pattern.quote(","));
				final String gameName = lineSplit[0].replaceAll(Pattern.quote("\""), "");
				final String rulesetName = lineSplit[1].replaceAll(Pattern.quote("\""), "");
				
				gameNames.add(gameName);
				rulesetNames.add(rulesetName);
			}
		} 
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

}
