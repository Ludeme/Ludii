package app.util;//

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.FileHandling;
import main.StringRoutines;
import main.collections.ListUtils;
import main.options.Option;
import main.options.Ruleset;
import other.GameLoader;

/**
 * To count the number of rulesets implemented.
 * 
 * @author Eric.Piette
 */
public final class CountRulesetsDone
{
	public static void main(final String[] args)
	{
		countRuleSets();
	}

	//-------------------------------------------------------------------------

	private static void countRuleSets()
	{
		int count = 0;
		int countOptionCombinations = 0;
		
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
			
			final List<List<String>> optionCategories = new ArrayList<List<String>>();

			for (int o = 0; o < game.description().gameOptions().numCategories(); o++)
			{
				final List<Option> options = game.description().gameOptions().categories().get(o).options();
				final List<String> optionCategory = new ArrayList<String>();

				for (int j = 0; j < options.size(); j++)
				{
					final Option option = options.get(j);
					optionCategory.add(StringRoutines.join("/", option.menuHeadings().toArray(new String[0])));
				}

				if (optionCategory.size() > 0)
					optionCategories.add(optionCategory);
			}

			List<List<String>> optionCombinations = ListUtils.generateTuples(optionCategories);
			//System.out.println(game.name() + " combi = " + optionCombinations.size());
			countOptionCombinations += optionCombinations.size();
			
			// Get all the rulesets of the game if it has some.
			if (rulesetsInGame != null && !rulesetsInGame.isEmpty())
			{
				for (int rs = 0; rs < rulesetsInGame.size(); rs++)
				{
					final Ruleset ruleset = rulesetsInGame.get(rs);
					if (!ruleset.optionSettings().isEmpty()&& !ruleset.heading().contains("Incomplete")) // We check if the ruleset is implemented.
					{
//						final Game rulesetGame = GameLoader.loadGameFromName(gameName, ruleset.optionSettings());
//							if(rulesetGame.computeRequirementReport())
//							{
//								System.out.println("WILL CRASH OR MISSING REQUIREMENT");
//								System.out.println(rulesetGame.name() + " RULESET + " + ruleset.heading());
//							}
						count++;
					}
				}
			}
			else
			{
				
//			if(game.computeRequirementReport())
//			{
//				System.out.println("WILL CRASH OR MISSING REQUIREMENT");
//				System.out.println(game.name());
//			}
				count++;
			}
		}
		
		System.out.println(count + " rulesets implemented");
		System.out.println(countOptionCombinations + " option combinations implemented");
	}
}