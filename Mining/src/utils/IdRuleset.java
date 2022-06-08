package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.options.Ruleset;
import utils.concepts.db.ExportDbCsvConcepts;

/**
 * To get the id from the db for a game object compiled with a ruleset.
 * 
 * @author Eric.Piette
 */
public class IdRuleset
{
	/** The path of the csv with the id of the rulesets for each game. */
	private static final String GAME_RULESET_PATH = "/concepts/input/GameRulesets.csv";

	/**
	 * @param game The game object.
	 * @return The id from the csv exported from the db.
	 */
	public static int get(final Game game)
	{
		final List<String> gameNames = new ArrayList<String>();
		final List<String> rulesetsNames = new ArrayList<String>();
		final TIntArrayList ids = new TIntArrayList();
		String rulesetName = null;
		try
		(
			final InputStream in = ExportDbCsvConcepts.class.getResourceAsStream(GAME_RULESET_PATH);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(in));		
		)
		{
			String line = reader.readLine();
			while (line != null)
			{
				String lineNoQuote = line.replaceAll(Pattern.quote("\""), "");

				int separatorIndex = lineNoQuote.indexOf(',');
				final String gameName = lineNoQuote.substring(0, separatorIndex);
				gameNames.add(gameName);
				lineNoQuote = lineNoQuote.substring(gameName.length() + 1);

				separatorIndex = lineNoQuote.indexOf(',');
				rulesetName = lineNoQuote.substring(0, separatorIndex);
				rulesetsNames.add(rulesetName);
				lineNoQuote = lineNoQuote.substring(rulesetName.length() + 1);
				final int id = Integer.parseInt(lineNoQuote);
				ids.add(id);
				// System.out.println(gameName + " --- " + rulesetName + " --- " + id);

				line = reader.readLine();
			}
			reader.close();
			
			final Ruleset ruleset = game.getRuleset();
			rulesetName = (ruleset == null) ? null : ruleset.heading();
			
			if (rulesetName == null)
			{
				for (int i = 0; i < gameNames.size(); i++)
					if (gameNames.get(i).equals(game.name()))
						return ids.get(i);
			}
			else
			{
				final String name_ruleset = ruleset.heading();
				final String startString = "Ruleset/";
				final String name_ruleset_csv = name_ruleset.substring(startString.length(),
						ruleset.heading().lastIndexOf('(') - 1);
				
				for (int i = 0; i < gameNames.size(); i++)
					if (gameNames.get(i).equals(game.name()) && rulesetsNames.get(i).equals(name_ruleset_csv))
						return ids.get(i);
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		catch (final NullPointerException e)
		{
			System.err.println("Try cleaning your Eclipse projects!");
			e.printStackTrace();
		}

		
		System.err.println("NOT FOUND");
		System.err.println("gameName = " + game.name());
		System.err.println("rulesetName = " + rulesetName);

		return Constants.UNDEFINED;
	}

}
