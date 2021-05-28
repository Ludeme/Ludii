package utils.concepts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.options.Ruleset;
import other.GameLoader;
import other.concept.Concept;
import other.concept.ConceptDataType;
import utils.DBGameInfo;

/**
 * Method to create the csv files listing all the game concepts used by each
 * game.
 *
 * @author Eric Piette
 */
public class ExportGameConcepts
{
	private static final String DOCUMENTED_DLP_LIST_PATH = "res/concepts/input/documentedRulesets.csv";

	private static final String DLP_LIST_PATH = "res/concepts/input/dlpRulesets.csv";

	// CSV to export
	private final static List<String> noHumanCSV = new ArrayList<String>();
	private final static List<String> humanCSV = new ArrayList<String>();
	private final static List<String> noHumanDocumentedDLPCSV = new ArrayList<String>();
	private final static List<String> noHumanDLPCSV = new ArrayList<String>();
	private final static List<String> noHumanNotDLPCSV = new ArrayList<String>();

	// DLP data
	private final static List<String> documentedDLPGames = new ArrayList<String>();
	private final static List<List<String>> documentedDLPRulesets = new ArrayList<List<String>>();
	private final static List<String> DLPGames = new ArrayList<String>();
	private final static List<List<String>> DLPRulesets = new ArrayList<List<String>>();

	//-------------------------------------------------------------------------

	/**
	 * Main method.
	 * 
	 * @param args
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void main(final String[] args) throws IllegalArgumentException, IllegalAccessException
	{
		// Get the DLP data.
		getDocumentedDLPRulesets();
		getDLPRulesets();

		// Compilation of all the games.
		final String[] allGameNames = FileHandling.listGames();
		for (int index = 0; index < allGameNames.length; index++)
		{
			final String gameName = allGameNames[index];
			if (FileHandling.shouldIgnoreLudAnalysis(gameName))
				continue;

			System.out.println("Compilation of : " + gameName);
			final Game game = GameLoader.loadGameFromName(gameName);

			exportFor(false, false, false, false, game, gameName); // Non Human, non DLP.
			exportFor(true, false, false, false, game, gameName); // Human, non DLP.
			exportFor(false, true, false, false, game, gameName); // Non Human, only documented DLP rulesets.
			exportFor(false, false, true, false, game, gameName); // Non Human, only DLP games.
			exportFor(false, false, false, true, game, gameName); // Non Human, only non DLP games.
		}

		// for Human CSV: We write the row to count the concepts on.
		final TIntArrayList flagsCount = new TIntArrayList();
		for (int i = 0; i < humanCSV.get(0).split(",").length - 2; i++)
			flagsCount.add(0);
		for (int i = 1; i < humanCSV.size(); i++)
		{
			final String humanString = humanCSV.get(i);
			final String[] splitString = humanString.split(",");
			for (int j = 2; j < splitString.length; j++)
				if (!splitString[j].isEmpty())
					flagsCount.set(j - 2, flagsCount.get(j - 2) + 1);
		}
		final StringBuffer stringToWrite = new StringBuffer();
		stringToWrite.append("Num Concepts,");
		stringToWrite.append(",");
		for (int j = 0; j < flagsCount.size(); j++)
			stringToWrite.append(flagsCount.get(j) + ",");
		stringToWrite.deleteCharAt(stringToWrite.length() - 1);
		humanCSV.add(1, stringToWrite.toString());

		// for Human CSV: remove the column with concepts never used.
		final TIntArrayList columnToRemove = new TIntArrayList();
		final String[] countConcepts = humanCSV.get(1).split(",");
		for (int i = 0; i < countConcepts.length; i++)
			if (countConcepts[i].equals("0"))
				columnToRemove.add(i);
		for (int i = 0; i < humanCSV.size(); i++)
		{
			final String[] stringSplit = humanCSV.get(i).split(",");
			final String[] newStringSplit = new String[stringSplit.length - columnToRemove.size()];
			int index = 0;
			for (int j = 0; j < stringSplit.length; j++)
				if (!columnToRemove.contains(j))
				{
					newStringSplit[index] = stringSplit[j];
					index++;
				}
			humanCSV.set(i, StringRoutines.join(",", newStringSplit));
		}

		final String fileNameNoHuman = "LudiiGameConcepts";
		final String outputFilePathNoHuman = "./res/concepts/output/" + fileNameNoHuman + ".csv";
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputFilePathNoHuman), "UTF-8"))
		{
			for (final String toWrite : noHumanCSV)
				writer.println(StringRoutines.join(",", toWrite));
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		final String fileNameHuman = "LudiiGameConceptsHUMAN";
		final String outputFilePathHuman = "./res/concepts/output/" + fileNameHuman + ".csv";
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputFilePathHuman), "UTF-8"))
		{
			for (final String toWrite : humanCSV)
				writer.println(StringRoutines.join(",", toWrite));
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		final String fileNameNoHumanDocumentedDLP = "LudiiGameConceptsDocumentedDLP";
		final String outputFilePathNoHumanDocumentedDLP = "./res/concepts/output/" + fileNameNoHumanDocumentedDLP
				+ ".csv";
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputFilePathNoHumanDocumentedDLP), "UTF-8"))
		{
			for (final String toWrite : noHumanDocumentedDLPCSV)
				writer.println(StringRoutines.join(",", toWrite));
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		final String fileNameNoHumanDLP = "LudiiGameConceptsDLP";
		final String outputFilePathNoHumanDLP = "./res/concepts/output/" + fileNameNoHumanDLP + ".csv";
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputFilePathNoHumanDLP), "UTF-8"))
		{
			for (final String toWrite : noHumanDLPCSV)
				writer.println(StringRoutines.join(",", toWrite));
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		final String fileNameNoHumanNotDLP = "LudiiGameConceptsNonDLP";
		final String outputFilePathNoHumanNotDLP = "./res/concepts/output/" + fileNameNoHumanNotDLP + ".csv";
		try (final PrintWriter writer = new UnixPrintWriter(new File(outputFilePathNoHumanNotDLP), "UTF-8"))
		{
			for (final String toWrite : noHumanNotDLPCSV)
				writer.println(StringRoutines.join(",", toWrite));
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * To run the code according to human or DLP.
	 * 
	 * @param HUMAN_VERSION  To get the human version.
	 * @param DOCUMENTED_DLP To look only the documented DLP games.
	 * @param DLP            To look only the DLP games.
	 * @param NonDLP         To look only the non DLP games.
	 */
	public static void exportFor(final boolean HUMAN_VERSION, final boolean DOCUMENTED_DLP, final boolean DLP,
			final boolean NonDLP, final Game game,
			final String gameName)
	{
		final TIntArrayList booleanConceptsID = new TIntArrayList();
		final TIntArrayList nonBooleanConceptsID = new TIntArrayList();

		// We create the header row.
		final List<String> headers = new ArrayList<String>();
		headers.add("Game Name");
		if (HUMAN_VERSION)
			headers.add("Num Flags On");

		// We get the boolean concepts.
		for (final Concept concept : Concept.values())
			if (concept.dataType().equals(ConceptDataType.BooleanData))
			{
				booleanConceptsID.add(concept.id());
				headers.add(concept.name());
			}

		for (final Concept concept : Concept.values())
			if (!concept.dataType().equals(ConceptDataType.BooleanData))
			{
				headers.add(concept.name());
				nonBooleanConceptsID.add(concept.id());
			}

		// In human version we count the game with a flag on.
		final TIntArrayList countGamesFlagOn = new TIntArrayList();
		for (int i = 0; i < booleanConceptsID.size(); i++)
			countGamesFlagOn.add(0);

		final List<List<String>> booleanConceptsOn = new ArrayList<List<String>>();

		// Some filters in case of DLP games.
		if (DOCUMENTED_DLP && !documentedDLPGames.contains(game.name()))
			return;

		if (DLP && !DLPGames.contains(game.name()))
			return;

		if (NonDLP && DLPGames.contains(game.name()))
			return;

		// We got the games (with rulesets) to look at.
		final List<Game> rulesetsToLook = new ArrayList<Game>();
		final List<String> rulesetNamesToLook = new ArrayList<String>();

		final List<Ruleset> rulesets = game.description().rulesets();
		if (rulesets != null && !rulesets.isEmpty())
		{
			for (int rs = 0; rs < rulesets.size(); rs++)
			{
				final Ruleset ruleset = rulesets.get(rs);

				if (!ruleset.optionSettings().isEmpty())
				{
					final String startString = "Ruleset/";
					final String name_ruleset_csv = ruleset.heading().substring(startString.length(),
							ruleset.heading().lastIndexOf('(') - 1);

					final Game rulesetGame = GameLoader.loadGameFromName(gameName, ruleset.optionSettings());
					final String name_ruleset = ruleset.heading();
					System.out.println("Compilation of " + rulesetGame.name() + " RULESET = " + name_ruleset);

					if (DOCUMENTED_DLP || DLP)
					{
						boolean found = false;
						for (final List<String> list : (DOCUMENTED_DLP ? documentedDLPRulesets : DLPRulesets))
						{
							if (list.get(0).equals(game.name()))
							{
								for (int i = 1; i < list.size(); i++)
									if (name_ruleset_csv.equals(list.get(i)))
									{
										rulesetsToLook.add(rulesetGame);
										rulesetNamesToLook.add(DBGameInfo.getUniqueName(rulesetGame));
										// rulesetNamesToLook.add(game.name() + "_" + list.get(i));
										found = true;
										break;
									}

							}
							if (found)
								break;
						}
					}
					else
					{
						rulesetsToLook.add(rulesetGame);
						rulesetNamesToLook.add(DBGameInfo.getUniqueName(rulesetGame));
					}
				}
			}
		}
		else
		{
			if (DOCUMENTED_DLP || DLP)
			{
				for (final List<String> list : (DOCUMENTED_DLP ? documentedDLPRulesets : DLPRulesets))
				{
					if (list.get(0).equals(game.name())) // 0 because only one ruleset
					{
						rulesetsToLook.add(game);
						rulesetNamesToLook.add(DBGameInfo.getUniqueName(game));
						break;
					}
				}
			}
			else
			{
				rulesetsToLook.add(game);
				rulesetNamesToLook.add(DBGameInfo.getUniqueName(game));
			}
		}

		// We get the concepts of each game.
		for (int indexGamesToLook = 0; indexGamesToLook < rulesetsToLook.size(); indexGamesToLook++)
		{
			final Game game_ruleset = rulesetsToLook.get(indexGamesToLook);
			final String game_ruleset_name = rulesetNamesToLook.get(indexGamesToLook);
			final List<String> flagsOn = new ArrayList<String>();
			flagsOn.add(game_ruleset_name.replaceAll("'", "").replaceAll(",", ""));
			int count = 0;
			for (int i = 0; i < booleanConceptsID.size(); i++)
			{
				if (game_ruleset.booleanConcepts().get(booleanConceptsID.get(i)))
				{
					flagsOn.add((HUMAN_VERSION) ? "Yes" : "1");
					count++;
					countGamesFlagOn.set(i, countGamesFlagOn.get(i) + 1);
				}
				else
					flagsOn.add((HUMAN_VERSION) ? "" : "0");
			}

			// if human version we add a column for the count of the
			// flags on in the second one.
			if (HUMAN_VERSION)
			{
				flagsOn.add("");
				for (int j = flagsOn.size() - 1; j > 1; j--)
					flagsOn.set(j, flagsOn.get(j - 1));
				flagsOn.set(1, count + "");
			}

			// We export the non boolean concepts.
			for (int i = 0; i < nonBooleanConceptsID.size(); i++)
			{
				final Integer idConcept = Integer.valueOf(nonBooleanConceptsID.get(i));
				flagsOn.add(game_ruleset.nonBooleanConcepts().get(idConcept));
			}

			/** Apply Format Game Name. */
//			if (!HUMAN_VERSION)
//			{
//				flagsOn.set(0, flagsOn.get(0).replaceAll(Pattern.quote(" "), "_"));
//				flagsOn.set(0, flagsOn.get(0).replaceAll(Pattern.quote("("), ""));
//				flagsOn.set(0, flagsOn.get(0).replaceAll(Pattern.quote(")"), ""));
//				flagsOn.set(0, flagsOn.get(0).replaceAll(Pattern.quote(","), ""));
//				flagsOn.set(0, flagsOn.get(0).replaceAll(Pattern.quote("\""), ""));
//				flagsOn.set(0, flagsOn.get(0).replaceAll(Pattern.quote("'"), ""));
//				flagsOn.set(0, flagsOn.get(0).replaceAll(Pattern.quote("["), ""));
//				flagsOn.set(0, flagsOn.get(0).replaceAll(Pattern.quote("]"), ""));
//			}

			booleanConceptsOn.add(flagsOn);
		}

		if (!HUMAN_VERSION && !DOCUMENTED_DLP && !NonDLP && !DLP && noHumanCSV.isEmpty())
			noHumanCSV.add(StringRoutines.join(",", headers));
		else if (HUMAN_VERSION && !DOCUMENTED_DLP && !NonDLP && !DLP && humanCSV.isEmpty())
			humanCSV.add(StringRoutines.join(",", headers));
		else if (!HUMAN_VERSION && DOCUMENTED_DLP && !NonDLP && !DLP && noHumanDocumentedDLPCSV.isEmpty())
			noHumanDocumentedDLPCSV.add(StringRoutines.join(",", headers));
		else if (!HUMAN_VERSION && DLP && !DOCUMENTED_DLP && !NonDLP && noHumanDLPCSV.isEmpty())
			noHumanDLPCSV.add(StringRoutines.join(",", headers));
		else if (!HUMAN_VERSION && !DLP && !DOCUMENTED_DLP && NonDLP && noHumanNotDLPCSV.isEmpty())
			noHumanNotDLPCSV.add(StringRoutines.join(",", headers));

		// Write row for each the game.
		for (final List<String> flagsOn : booleanConceptsOn)
			if (!HUMAN_VERSION && !DOCUMENTED_DLP && !DLP && !NonDLP)
				noHumanCSV.add(StringRoutines.join(",", flagsOn));
			else if (HUMAN_VERSION && !DOCUMENTED_DLP && !DLP && !NonDLP)
				humanCSV.add(StringRoutines.join(",", flagsOn));
			else if (!HUMAN_VERSION && DOCUMENTED_DLP && !DLP && !NonDLP)
				noHumanDocumentedDLPCSV.add(StringRoutines.join(",", flagsOn));
			else if (!HUMAN_VERSION && DLP && !DOCUMENTED_DLP && !NonDLP)
				noHumanDLPCSV.add(StringRoutines.join(",", flagsOn));
			else if (!HUMAN_VERSION && !DLP && !DOCUMENTED_DLP && NonDLP)
				noHumanNotDLPCSV.add(StringRoutines.join(",", flagsOn));
	}

	//---------------------------------------------------------------------

	/**
	 * To get the documented DLP rulesets.
	 */
	public static void getDocumentedDLPRulesets()
	{
		
		try (final BufferedReader reader = new BufferedReader(new FileReader("./" + DOCUMENTED_DLP_LIST_PATH)))
		{
			String line = reader.readLine();
			while (line != null)
			{
				final String name = line.substring(1, line.length() - 1);
				final int separatorIndex = name.indexOf(',');
				final String game_ruleset = name.substring(0, separatorIndex - 1) + "_"
						+ name.substring(separatorIndex + 2, name.length());
				final String game_name = game_ruleset.substring(0, game_ruleset.indexOf('_'));
				final String ruleset_name = game_ruleset.substring(game_ruleset.indexOf('_') + 1,
						game_ruleset.length());
				documentedDLPGames.add(game_name);

				boolean found = false;
				for (final List<String> list : documentedDLPRulesets)
				{
					if (list.get(0).equals(game_name))
					{
						found = true;
						list.add(ruleset_name);
						break;
					}
				}
				if (!found)
				{
					documentedDLPRulesets.add(new ArrayList<String>());
					documentedDLPRulesets.get(documentedDLPRulesets.size() - 1).add(game_name);
					documentedDLPRulesets.get(documentedDLPRulesets.size() - 1).add(ruleset_name);
				}

				line = reader.readLine();
			}
			reader.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * To get the documented DLP rulesets.
	 */
	public static void getDLPRulesets()
	{
		try (final BufferedReader reader = new BufferedReader(new FileReader("./" + DLP_LIST_PATH)))
		{
			String line = reader.readLine();
			while (line != null)
			{
				final String name = line.substring(1, line.length() - 1);
				final int separatorIndex = name.indexOf(',');
				final String game_ruleset = name.substring(0, separatorIndex - 1) + "_"
						+ name.substring(separatorIndex + 2, name.length());
				final String game_name = game_ruleset.substring(0, game_ruleset.indexOf('_'));
				final String ruleset_name = game_ruleset.substring(game_ruleset.indexOf('_') + 1,
						game_ruleset.length());
				DLPGames.add(game_name);

				boolean found = false;
				for (final List<String> list : DLPRulesets)
				{
					if (list.get(0).equals(game_name))
					{
						found = true;
						list.add(ruleset_name);
						break;
					}
				}
				if (!found)
				{
					DLPRulesets.add(new ArrayList<String>());
					DLPRulesets.get(DLPRulesets.size() - 1).add(game_name);
					DLPRulesets.get(DLPRulesets.size() - 1).add(ruleset_name);
				}

				line = reader.readLine();
			}
			reader.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

}
