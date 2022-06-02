package supplementary.experiments.game_files;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import features.spatial.Walk;
import game.Game;
import main.FileHandling;
import main.options.Ruleset;
import other.GameLoader;

/**
 * A little helper class with a main method to loop through
 * all games and do something with them (usually print some
 * information about certain games, can change this whenever
 * I want to do something new).
 * 
 * @author Dennis Soemers
 */
public class LoopThroughGames 
{
	
	public static void main(final String[] args)
	{
		final String[] allGameNames = Arrays.stream(FileHandling.listGames()).filter(s -> (
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/simulation/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/proprietary/"))
			)).toArray(String[]::new);
		
		for (final String gameName : allGameNames)
		{
			//final String[] gameNameSplit = gameName.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			//final String shortGameName = gameNameSplit[gameNameSplit.length - 1];
			
			boolean skipGame = false;
//			for (final String game : SKIP_GAMES)
//			{
//				if (shortGameName.endsWith(game))
//				{
//					skipGame = true;
//					break;
//				}
//			}
			
			if (skipGame)
				continue;
			
			final Game gameNoRuleset = GameLoader.loadGameFromName(gameName);
			final List<Ruleset> gameRulesets = new ArrayList<Ruleset>(gameNoRuleset.description().rulesets());
			gameRulesets.add(null);
			boolean foundRealRuleset = false;
			
			for (final Ruleset ruleset : gameRulesets)
			{
				final Game game;
				String fullRulesetName = "";
				if (ruleset == null && foundRealRuleset)
				{
					// Skip this, don't allow game without ruleset if we do have real implemented ones
					continue;
				}
				else if (ruleset != null && !ruleset.optionSettings().isEmpty())
				{
					fullRulesetName = ruleset.heading();
					foundRealRuleset = true;
					game = GameLoader.loadGameFromName(gameName, fullRulesetName);
				}
				else if (ruleset != null && ruleset.optionSettings().isEmpty())
				{
					// Skip empty ruleset
					continue;
				}
				else
				{
					game = gameNoRuleset;
				}
				
				if (game.hasSubgames())
					continue;
				
				if (game.isDeductionPuzzle())
					continue;
				
				if (game.isSimulationMoveGame())
					continue;
				
				if (!game.isAlternatingMoveGame())
					continue;
				
				if (game.isStacking())
					continue;
				
				if (game.isBoardless())
					continue;
				
				if (Walk.allGameRotations(game).length <= 1)
				{
					System.out.println("Rotations for " + gameName + " (" + fullRulesetName + ") = " + Arrays.toString(Walk.allGameRotations(game)));
				}
			}
		}
	}

}
