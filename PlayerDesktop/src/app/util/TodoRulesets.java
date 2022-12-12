package app.util;

import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.FileHandling;
import main.options.Ruleset;
import other.GameLoader;

/**
 * To print the rulesets not implemented in the games which are not Reconstructed or Incomplete
 * 
 * @author Eric.Piette
 */
public final class TodoRulesets
{
	public static void main(final String[] args)
	{
		missingRulesets();
	}

	//-------------------------------------------------------------------------

	private static void missingRulesets()
	{
		int countNotIncomplete = 0;
		int countIncomplete = 0;
		final String[] gameNames = FileHandling.listGames();

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

			if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction/pending/"))
				continue;
			
			if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction/validation/"))
				continue;

			final Game game = GameLoader.loadGameFromName(gameName);
			final List<Ruleset> rulesetsInGame = game.description().rulesets();
			
			// Get all the rulesets of the game if it has some.
			if (rulesetsInGame != null && !rulesetsInGame.isEmpty())
			{
				for (int rs = 0; rs < rulesetsInGame.size(); rs++)
				{
					final Ruleset ruleset = rulesetsInGame.get(rs);
					if (ruleset.optionSettings().isEmpty()) // We check if the ruleset is implemented.
					{
						System.out.println("TODO: " + game.name() + " " + ruleset.heading());
						if(ruleset.heading().contains("Incomplete"))
							countIncomplete++;
						else
							countNotIncomplete++;
					}
				}
			}
		}

		System.out.println(countNotIncomplete + " complete rulesets TODO.");
		System.out.println(countIncomplete + " incomplete rulesets TODO.");
	}
}