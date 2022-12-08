package supplementary.experiments.game_files;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import features.spatial.Walk;
import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.options.Ruleset;
import other.GameLoader;

/**
 * Main method to update AI Metadata with identified top features.
 * 
 * @author Dennis Soemers
 */
public class UpdateAIMetadataTopFeatures
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Games we should skip since they never end anyway (in practice), but do
	 * take a long time.
	 */
	private static final String[] SKIP_GAMES = new String[]
			{
				"Chinese Checkers.lud",
				"Li'b al-'Aqil.lud",
				"Li'b al-Ghashim.lud",
				"Mini Wars.lud",
				"Pagade Kayi Ata (Sixteen-handed).lud",
				"Taikyoku Shogi.lud"
			};
	
	//-------------------------------------------------------------------------
	
	/**
	 * Updates our AI metadata
	 * @param argParse
	 */
	private static void updateMetadata(final CommandLineArgParse argParse)
	{
		String topFeaturesOutDirPath = argParse.getValueString("--top-features-out-dir");
		topFeaturesOutDirPath = topFeaturesOutDirPath.replaceAll(Pattern.quote("\\"), "/");
		if (!topFeaturesOutDirPath.endsWith("/"))
			topFeaturesOutDirPath += "/";
		
		final File topFeaturesOutDir = new File(topFeaturesOutDirPath);
		
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
		
		final List<String> gameNames = new ArrayList<String>();
		final List<String> rulesetNames = new ArrayList<String>();
		
		for (final String gameName : allGameNames)
		{
			final String[] gameNameSplit = gameName.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			final String shortGameName = gameNameSplit[gameNameSplit.length - 1];
			
			boolean skipGame = false;
			for (final String game : SKIP_GAMES)
			{
				if (shortGameName.endsWith(game))
				{
					skipGame = true;
					break;
				}
			}
			
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
				
				if (game.hiddenInformation())
					continue;
				
				if (Walk.allGameRotations(game).length == 0)
					continue;
				
				if (game.players().count() == 0)
					continue;
				
				if (game.isSimultaneousMoveGame())
					continue;
				
				gameNames.add("/" + shortGameName);
				rulesetNames.add(fullRulesetName);
			}
		}
		
		// TODO
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method to update all our metadata
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Updates all our AI metadata to include identified top features."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--top-features-out-dir")
				.help("Output directory with identified top features.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--ai-defs-dir")
				.help("Directory containing AI metadata .def files.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.withDefault("../Common/res/def_ai"));
		
		argParse.addOption(new ArgOption()
				.withNames("--luds-dir")
				.help("Directory that contains the /lud/** directory.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.withDefault("../Common/res"));
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		updateMetadata(argParse);
	}
	
	//-------------------------------------------------------------------------

}
