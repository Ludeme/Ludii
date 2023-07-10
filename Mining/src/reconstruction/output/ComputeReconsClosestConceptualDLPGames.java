package reconstruction.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.FileHandling;
import main.UnixPrintWriter;
import main.options.Ruleset;
import other.GameLoader;

/**
 * @author Eric.Piette
 *
 */
public class ComputeReconsClosestConceptualDLPGames {

	// Load ruleset avg common true concepts from specific directory.
	final static String conceptsFilePath = "./res/recons/input/RulesetConcepts.csv";
	
	// The rulesets reconstructed.
	final static String pathReconstructed    = "./res/recons/output/";
	
	// The precision of the double to use.
	final static int DOUBLE_PRECISION = 5;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method.
	 * @param args
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public static void main(final String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{
		computeReconsClosest();
	}
	
	/**
	 * Compute the conceptual closest games of all the generated reconstructions.
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	private static void computeReconsClosest() throws FileNotFoundException, UnsupportedEncodingException
	{
		final String[] gameNames = FileHandling.listGames();
		final String output = "ClosestGames.csv";
		final List<Game> rulesets = new ArrayList<Game>();
		final TIntArrayList rulesetsids = new TIntArrayList();
		
		try (final PrintWriter writer = new UnixPrintWriter(new File(output), "UTF-8"))
		{
			// Look at each ruleset.
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
	
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction/pending"))
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
								rulesetsids.add(Integer.parseInt(ids.get(0)));
								rulesets.add(rulesetGame);
								System.out.println(rulesetGame.name() + " " + ruleset.heading() + " found");
							}
						}
					}
				}
				else
				{
					final List<String> ids = game.metadata().info().getId();
					if(!ids.isEmpty())
					{
						rulesetsids.add(Integer.parseInt(ids.get(0)));
						rulesets.add(game);
						System.out.println(game.name() + " found");
					}
				}
			}
		}
		
		for(int i = 0; i < rulesets.size(); i++)
		{
			System.out.println(rulesets.get(i).name() + " - Id:" + rulesetsids.get(i));
		}
	}
}
