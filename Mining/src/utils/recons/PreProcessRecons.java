package utils.recons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.options.Ruleset;
import other.GameLoader;

/**
 * To format all rulesets
 * 
 * @author Eric.Piette
 */
public final class PreProcessRecons
{
	public static void main(final String[] args)
	{
		generateCSVOneLineRulesetAndId();
	}

	//-------------------------------------------------------------------------

	//**
	
	private static void generateCSVOneLineRulesetAndId()
	{
		final String[] gameNames = FileHandling.listGames();
		final String output = "RulesetFormatted.csv";

		try (final PrintWriter writer = new UnixPrintWriter(new File(output), "UTF-8"))
		{
			for (int index = 0; index < gameNames.length; index++)
			{
				final String gameName = gameNames[index];
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/"))
					continue;
	
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
					continue;
	
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/"))
					continue;
	
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
					continue;
	
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("subgame"))
					continue;
	
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction"))
					continue;
	
				final Game game = GameLoader.loadGameFromName(gameName);
				final List<Ruleset> rulesetsInGame = game.description().rulesets();
				
				// Get all the rulesets of the game if it has some.
				if (rulesetsInGame != null && !rulesetsInGame.isEmpty())
				{
					for (int rs = 0; rs < rulesetsInGame.size(); rs++)
					{
						final Ruleset ruleset = rulesetsInGame.get(rs);
						if (!ruleset.optionSettings().isEmpty()) // We check if the ruleset is implemented.
						{
							final Game rulesetGame = GameLoader.loadGameFromName(gameName, ruleset.optionSettings());
							final List<String> ids = rulesetGame.metadata().info().getId();
							if(!ids.isEmpty())
							{
								final String rulesetId = ids.get(0); 
								System.out.println("Game: " + game.name() + " RulesetName = " + rulesetGame.getRuleset().heading() + " RulesetID = " + rulesetId);
								final String formattedDesc = StringRoutines.formatOneLineDesc(rulesetGame.description().expanded());
								final List<String> lineToWrite = new ArrayList<String>();
								lineToWrite.add(game.name());
								lineToWrite.add(rulesetGame.getRuleset().heading());
								lineToWrite.add(rulesetId);
								lineToWrite.add(formattedDesc);
								writer.println(StringRoutines.join(",", lineToWrite));
							}
						}
					}
				}
				else
				{
					final List<String> ids = game.metadata().info().getId();
					if(!ids.isEmpty())
					{
						final String rulesetId = ids.get(0); 
						System.out.println("Game: " + game.name() + " RulesetID = " + rulesetId);
						final String formattedDesc = StringRoutines.formatOneLineDesc(game.description().expanded());
						final List<String> lineToWrite = new ArrayList<String>();
						lineToWrite.add(game.name());
						lineToWrite.add("ONLY ONE RULESET");
						lineToWrite.add(rulesetId);
						lineToWrite.add(formattedDesc);
						writer.println(StringRoutines.join(",", lineToWrite));
					}
				}
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		System.out.println("CSV GENERATED");
		
	}
	

}