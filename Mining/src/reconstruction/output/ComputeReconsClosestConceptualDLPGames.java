package reconstruction.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.FileHandling;
import main.UnixPrintWriter;
import main.options.Ruleset;
import other.GameLoader;
import other.concept.Concept;

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

		// Get the CSV.
		System.out.println("*******Get all concepts from DB*******");
		final TIntArrayList rulesetsdIds = new TIntArrayList();
		final TIntArrayList conceptIds = new TIntArrayList();
		final TDoubleArrayList conceptValues = new TDoubleArrayList();
		try (BufferedReader br = new BufferedReader(new FileReader(conceptsFilePath))) 
		{
			String line = br.readLine();
			while (line != null)
			{
				String lineNoQuote = line.replaceAll(Pattern.quote("\""), "");
				if(!lineNoQuote.contains("NULL"))
				{
					int separatorIndex = lineNoQuote.indexOf(',');
					final String rulesetName = lineNoQuote.substring(0, separatorIndex);
					lineNoQuote = lineNoQuote.substring(rulesetName.length() + 1);
					
					separatorIndex = lineNoQuote.indexOf(',');
					final String idRulesets = lineNoQuote.substring(0, separatorIndex);
					rulesetsdIds.add(Integer.parseInt(idRulesets));
					lineNoQuote = lineNoQuote.substring(idRulesets.length() + 1);
					
					separatorIndex = lineNoQuote.indexOf(',');
					String idConcepts = lineNoQuote.substring(0, separatorIndex);
					conceptIds.add(Integer.parseInt(idConcepts));
					lineNoQuote = lineNoQuote.substring(idConcepts.length() + 1);
	
					String valuesConcepts = lineNoQuote;
					conceptValues.add(Double.parseDouble(valuesConcepts.length() > DOUBLE_PRECISION ? valuesConcepts.substring(0, DOUBLE_PRECISION) : valuesConcepts));
				}
				line = br.readLine();
			}
			br.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("*******Done*******");
		
		// Get rulesets and ids.
		System.out.println("*******Get all rulesets names + ids *******");
		final List<String> rulesets = new ArrayList<String>();
		for(int i = 0; i < 5000; i++)
			rulesets.add("");
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
						if (!ruleset.optionSettings().isEmpty() && !ruleset.heading().contains("Incomplete")) // We check if the ruleset is implemented and it is not one to recons.
						{
							final Game rulesetGame = GameLoader.loadGameFromName(gameName, ruleset.optionSettings());
							final List<String> ids = rulesetGame.metadata().info().getId();
							if(!ids.isEmpty())
							{
								final int id = Integer.parseInt(ids.get(0));
								rulesets.set(id, rulesetGame.name() + " " + ruleset.heading());
							}
						}
					}
				}
				else
				{
					final List<String> ids = game.metadata().info().getId();
					if(!ids.isEmpty())
					{
						final int id = Integer.parseInt(ids.get(0));
						rulesets.set(id, game.name());
					}
				}
				if(index % 20 == 0)
					System.out.println(index + " rulesets id checked.");
			}
		}
//		for(int i = 0; i < rulesets.size(); i++)
//		{
//			System.out.println(rulesets.get(i) + " - Id:" + i);
//		}
		System.out.println("*******Done*******");

		// Gets the concepts for each game in a list.
		List<TDoubleArrayList> conceptsPerGame = new ArrayList<TDoubleArrayList>();
		
		System.out.println("*******Get all concepts values for each id. *******");
		for(int i = 0; i < rulesets.size(); i++)
		{
			final String gameName = rulesets.get(i);
			TDoubleArrayList gameConcepts = new TDoubleArrayList();
			for(int j = 0; j < Concept.values().length+1; j++)
				gameConcepts.add(-1);
			
			if(!gameName.isEmpty())
			{
				final int id = i;
				for(int j = 0; j < rulesetsdIds.size(); j++)
					if(rulesetsdIds.get(j) == id)
						gameConcepts.set(conceptIds.get(j), conceptValues.get(j));
				
			}
			conceptsPerGame.add(gameConcepts);
			System.out.println(gameConcepts);
		}
		System.out.println("*******Done*******");
	}
}
