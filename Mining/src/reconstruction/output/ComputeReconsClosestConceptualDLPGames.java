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
import main.Constants;
import main.FileHandling;
import main.StringRoutines;
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
	final static String conceptsFilePath = "./res/recons/input/RulesetConceptsUCT.csv";
	
	// The rulesets reconstructed.
	final static String pathReconstructed    = "./res/recons/output/";
	
	// The precision of the double to use.
	final static int DOUBLE_PRECISION = 5;

	// Double value used to represented null value concepts.
	final static double NULL_VALUE_CONCEPT = -999;

	// Double value used to represented null value concepts.
	final static String output = "ClosestTop10_And_ExpectedMetricValues.csv";
	
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
		final List<Concept> reconstructionConcepts = new ArrayList<Concept>();
		reconstructionConcepts.add(Concept.DurationTurns);
		reconstructionConcepts.add(Concept.DurationTurnsStdDev);
		reconstructionConcepts.add(Concept.DurationTurnsNotTimeouts);
		reconstructionConcepts.add(Concept.DecisionMoves);
		reconstructionConcepts.add(Concept.BoardCoverageDefault);
		reconstructionConcepts.add(Concept.AdvantageP1);
		reconstructionConcepts.add(Concept.Balance);
		reconstructionConcepts.add(Concept.Completion);
		reconstructionConcepts.add(Concept.Timeouts);
		reconstructionConcepts.add(Concept.Drawishness);
		reconstructionConcepts.add(Concept.PieceNumberAverage);
		reconstructionConcepts.add(Concept.BoardSitesOccupiedAverage);
		reconstructionConcepts.add(Concept.BranchingFactorAverage);
		reconstructionConcepts.add(Concept.DecisionFactorAverage);
		
		final String[] gameNames = FileHandling.listGames();

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
				System.out.println(index + " games checked.");
		}
//		for(int i = 0; i < rulesets.size(); i++)
//		{
//			System.out.println(rulesets.get(i) + " - Id:" + i);
//		}
		System.out.println("*******Done*******");

		// Gets the concepts for each game in a list.
		System.out.println("*******Get all concepts values for each id. *******");
		List<TDoubleArrayList> conceptsPerGame = new ArrayList<TDoubleArrayList>();
		for(int i = 0; i < rulesets.size(); i++)
		{
			final String gameName = rulesets.get(i);
			TDoubleArrayList gameConcepts = new TDoubleArrayList();
			for(int j = 0; j < Concept.values().length+1; j++)
				gameConcepts.add(NULL_VALUE_CONCEPT); // -999 just to replace the null values.
			
			if(!gameName.isEmpty())
			{
				final int id = i;
				for(int j = 0; j < rulesetsdIds.size(); j++)
					if(rulesetsdIds.get(j) == id)
					{
						gameConcepts.set(conceptIds.get(j), conceptValues.get(j));
					}
			}
			conceptsPerGame.add(gameConcepts);
		}
		System.out.println("*******Done*******");
		
		// Compute the 10 closest rulesets for each reconstruction.
		System.out.println("*******Get all 10 closest rulesets for each reconstruction. *******");
		try (final PrintWriter writer = new UnixPrintWriter(new File(output), "UTF-8"))
		{
			// Write Header of the csv.
			final List<String> headersToWrite = new ArrayList<String>();
			headersToWrite.add("Reconstruction Ruleset");
			for(int i = 0 ; i < 10; i++)
				headersToWrite.add("closest" + i);
			for(Concept concept : reconstructionConcepts)
			{
				headersToWrite.add(concept.name() + "_Expected");
				headersToWrite.add(concept.name() + "_Current");
			}
			writer.println(StringRoutines.join(",", headersToWrite));
			
			for(int i = 0; i < rulesets.size(); i++)
			{
				final String rulesetName = rulesets.get(i);
				if(rulesetName.contains("Reconstructed"))
				{
					final TDoubleArrayList gameConceptsReconstructed = conceptsPerGame.get(i);
					final TIntArrayList top10Distance = new TIntArrayList();
					final List<List<String>> top10Closest = new ArrayList<List<String>>();
					final List<TIntArrayList> top10Ids = new ArrayList<TIntArrayList>();
					for(int j = 0; j < 10; j++)
					{
						top10Distance.add(Constants.INFINITY * -1);
						top10Closest.add(new ArrayList<String>());
						top10Ids.add(new TIntArrayList());
					}
					
					// Check all other rulesets...
					for(int j = 0; j < rulesets.size(); j++)
					{
						// ... which are not reconstructed.
						if(!rulesets.get(j).contains("Reconstructed"))
						{
							final TDoubleArrayList gameConceptsNotReconstructed = conceptsPerGame.get(j);
							
							// Compute Distance.
							int distance = 0;
							for(int k = 0; k < gameConceptsReconstructed.size(); k++)
							{
								final double conceptValueReconstructed = gameConceptsReconstructed.get(k);
								final double conceptValueNotReconstructed = gameConceptsNotReconstructed.get(k);
								if(conceptValueReconstructed != NULL_VALUE_CONCEPT && conceptValueNotReconstructed != NULL_VALUE_CONCEPT)
								{
									if(conceptValueReconstructed == conceptValueNotReconstructed)
										distance++;
									else
										distance--;
								}
							}
							
							// Check if we already have this distance.
							boolean newDistance = true;
							for(int k = 0; k < top10Distance.size(); k++)
							{
								if(distance == top10Distance.get(k))
								{
									top10Closest.get(k).add(rulesets.get(j));
									top10Ids.get(k).add(j);
									newDistance = false;
									break;
								}
							}
							
							if(newDistance)
							{
								// Get minimum current distance.
								int min = top10Distance.get(0);
								int indexMin = 0;
								for(int k = 1; k < top10Distance.size(); k++)
								{
									if(min > top10Distance.get(k))
									{
										min = top10Distance.get(k);
										indexMin = k;
									}
								}
								
								// Check if the min is lower than new distance to add it.
								if(distance > top10Distance.get(indexMin))
								{
									top10Distance.set(indexMin, distance);
									top10Closest.get(indexMin).clear();
									top10Closest.get(indexMin).add(rulesets.get(j));
									top10Ids.get(indexMin).clear();
									top10Ids.get(indexMin).add(j);
								}
							}
						}
					}
					
					System.out.println("***For " + rulesetName + " ***");
					System.out.println("10 closest rulesets are: ");
					for(int j = 0; j < top10Closest.size(); j++)
						System.out.println("Distance = " + top10Distance.get(j) + " RULESET = " + top10Closest.get(j));
					System.out.println();
					
					// Compute average expected values for reconstruction metrics.

					// Write results in the csv.
					final List<String> lineToWrite = new ArrayList<String>();
					lineToWrite.add(rulesetName);
					for(List<String> closestGames : top10Closest)
						if(!closestGames.isEmpty())
							lineToWrite.add(closestGames.get(0));
						else	
							lineToWrite.add("");
					
					for(Concept concept: reconstructionConcepts)
					{
						final int idConcept = concept.id();
						double sumValues = 0;
						int numValues = 0;
						for(int j = 0; j < top10Ids.size(); j++)
						{
							TIntArrayList ids = top10Ids.get(j);
							for(int k = 0 ; k < ids.size(); k++)
							{
								numValues++;
								sumValues+= conceptsPerGame.get(ids.get(k)).get(idConcept);
							}
						}
						final double averageValue = sumValues/numValues;
						
						System.out.println("For concept " + concept);
						System.out.println("Average expected value is " + averageValue);
						System.out.println("Current Value is " + conceptsPerGame.get(i).get(idConcept));
						System.out.println();

						lineToWrite.add(averageValue + "");
						lineToWrite.add(conceptsPerGame.get(i).get(idConcept) + "");
					}

					writer.println(StringRoutines.join(",", lineToWrite));
					
					System.out.println("\n********************************************************\n");
				}
					
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		System.out.println("*******Done*******");
		
	}
}
