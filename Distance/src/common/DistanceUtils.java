package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import game.Game;
import main.StringRoutines;
import main.collections.ListUtils;
import main.options.Option;
import main.options.Ruleset;
import metrics.JensenShannonDivergence;
import other.GameLoader;
import other.context.Context;
import other.trial.Trial;

/**
 * Utilities for distance metrics.
 * 
 * @author markus
 */
public class DistanceUtils
{

	/**
	 * the folder related to distance measurements
	 */
	final static File outputfolder = new File("../Distance/out/");
	final static File resourceFolder = new File("../Distance/res/");

	/**
	 * Composes the input file for the program splitstree4 "Software for
	 * computing phylogenetic networks"
	 * 
	 * @author Zhanghyi
	 * @param orderOfGames   The corresponding names to the distance matrix
	 * @param distanceMatrix
	 * @param outputFolder
	 * @param fileName
	 */
	public static void generateSplitTreeInput(
			final List<String> orderOfGames, final double[][] distanceMatrix,
			final File outputFolder, final String fileName
	)
	{

		final int numberOfTaxa = orderOfGames.size();
		final ArrayList<String> lines = new ArrayList<String>();
		lines.add(String.format("#nexus\n\n" + "BEGIN Taxa;\n"
				+ "DIMENSIONS ntax=%d;\n" + "TAXLABELS", "" + numberOfTaxa));

		for (int i = 0; i < numberOfTaxa; i++)
		{

			String gameName = orderOfGames.get(i).replace(" ", "_");
			gameName = Normalizer.normalize(gameName, Normalizer.Form.NFKD);
			gameName = gameName.replace("\\?", "");
			final String toAdd = String
					.format("[%d] '%s'", "" + (i + 1), gameName)
					.replace("(", "").replace(")", "").replace("'", "");
			lines.add(toAdd);
		}
		lines.add(String.format(";\n" + "END; [Taxa]\n" + "\n"
				+ "BEGIN Distances;\n" + "DIMENSIONS ntax=%d;\n"
				+ "FORMAT labels=no diagonal triangle=upper;\n" + "MATRIX",
				numberOfTaxa + ""));

		final DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(
				Locale.US);
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(',');
		final DecimalFormat df = new DecimalFormat("#.####", otherSymbols);
		for (int i = 0; i < numberOfTaxa; i++)
		{
			String rowString = "";

			for (int j = 0; j < numberOfTaxa; j++)
			{
				if (i == j)
				{
					rowString += (0 + " ");
					continue;
				}
				if (i > j)
				{
					rowString += "    ";
					continue;
				}

				final double distance = distanceMatrix[i][j];
				final String numberToAdd = df.format(distance);
				rowString += numberToAdd + " ";
			}
			rowString += "\n";
			lines.add(rowString);

		}
		lines.add(";\nEND; [Distances]\n");

		final String path = outputFolder + File.separator + fileName;

		try (final PrintWriter out = new PrintWriter(path))
		{
			for (final String l : lines)
			{
				out.println(l);
			}
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Creates random playouts of a game.
	 * 
	 * @param game
	 * @param NUM_PLAYOUTS
	 * @param NUM_MAX_MOVES
	 * @return random playouts of a game.
	 */
	public static Trial[] getRandomTrialsFromGame(
			final Game game, final int NUM_PLAYOUTS, final int NUM_MAX_MOVES
	)
	{
		final Trial[] allTrials = new Trial[NUM_PLAYOUTS];

		for (int i = 0; i < NUM_PLAYOUTS; i++)
		{
			final Trial trialA = new Trial(game);
			final Context context = new Context(game, trialA);
			game.start(context);
			game.playout(context, null, 1.0, null, 0, NUM_MAX_MOVES,
					ThreadLocalRandom.current());
			allTrials[i] = trialA;
		}

		return allTrials;
	}

	/**
	 * <p>
	 * "\\Evaluation\\src\\distance\\out\\test.txt" would return "out"
	 * 
	 * @param file
	 * @return the name of the current folder this file is in
	 */
	public static String getCurrentFolderName(final File file)
	{
		final String splitRegex = Pattern
				.quote(System.getProperty("file.separator"));
		final String[] splittedFileName = file.getAbsolutePath()
				.split(splitRegex);
		final String folderName = splittedFileName[splittedFileName.length - 2];
		return folderName;
	}

	/**
	 * @param f
	 * @return the possible options as suggested by the .lud file
	 */
	public static List<List<String>>
			getAllPossibleOptionsFromGameFile(final File f)
	{
		final Game game = GameLoader.loadGameFromFile(f);
		return getAllPossibleOptionsFromGame(game);
	}

	/**
	 * copy from OnePlayoutPerGameTestWithOptions
	 * <p>
	 * return the possible options
	 * 
	 * @param game
	 * @return AllPossibleOptionsFromGame
	 */
	public static List<List<String>>
			getAllPossibleOptionsFromGame(final Game game)
	{

		final List<List<String>> optionCategories = new ArrayList<List<String>>();

		for (int o = 0; o < game.description().gameOptions()
				.numCategories(); o++)
		{
			final List<Option> options = game.description().gameOptions()
					.categories().get(o).options();
			final List<String> optionCategory = new ArrayList<String>();

			for (int i = 0; i < options.size(); i++)
			{
				final Option option = options.get(i);
				optionCategory.add(StringRoutines.join("/",
						option.menuHeadings().toArray(new String[0])));
			}

			if (optionCategory.size() > 0)
				optionCategories.add(optionCategory);
		}

		final List<List<String>> optionCombinations = ListUtils
				.generateTuples(optionCategories);
		return optionCombinations;
	}

	/**
	 * 
	 * @return a list of all Ludii game files
	 */
	public static ArrayList<File> getAllLudiiGameFiles()
	{
		return getAllLudiiGameFiles(null);
	}

	/**
	 * 
	 * @param targetFolder if null returns all games, otherwise ensures that
	 *                     path contains targetFolder
	 * @return a list of games which contain the target folder
	 */
	public static ArrayList<File>
			getAllLudiiGameFiles(final String targetFolder)
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<>();
		gameDirs.add(startFolder);

		// Find the .lud files (and not .def)
		final ArrayList<File> entries = new ArrayList<>();

		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					final String path = fileEntry.getPath()
							.replaceAll(Pattern.quote("\\"), "/");

					if (path.equals("../Common/res/lud/plex"))
						continue;

					if (path.equals("../Common/res/lud/wishlist"))
						continue;

					if (path.equals("../Common/res/lud/userWishlist"))
						continue;

					if (path.equals("../Common/res/lud/wip"))
						continue;

					if (path.equals("../Common/res/lud/test"))
						continue;

					if (path.equals("../Common/res/lud/bad"))
						continue;

					if (path.equals("../Common/res/lud/bad_playout"))
						continue;

					gameDirs.add(fileEntry);
				} else if (fileEntry.getName().contains(".lud"))
				{

					if (targetFolder != null
							&& !fileEntry.getPath().contains(targetFolder))
						continue;

					// if (counter>=startIndex&&counter<endIndex)
					entries.add(fileEntry);

				}
			}
		}
		return entries;
	}

	/**
	 * Update LudiRule combination files As all Ludeme and Ruleset combinations
	 * are stored in a file, this file needs to be updated, when new games are
	 * added.
	 */
	public static void recalculateAllLudiiGameFilesAndRulesetCombination()
	{
		getAllLudiiGameFilesAndRulesetCombination(outputfolder, true);
	}

	/**
	 * @param forceRecalc
	 * @return all possible ludii ruleset combinations currently in the system
	 */
	public static ArrayList<LudRul>
			getAllLudiiGameFilesAndRulesetCombination(final boolean forceRecalc)
	{
		return getAllLudiiGameFilesAndRulesetCombination(resourceFolder,
				forceRecalc);
	}

	/**
	 * 
	 * @param forceRecalc
	 * @return all possible ludii ruleset combinations currently in the system
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<LudRul> getAllLudiiGameFilesAndRulesetCombination(
			final File folder, final boolean forceRecalc
	)
	{
		final String filePath = folder.getAbsolutePath() + File.separator
				+ "ludiiGameFilesOptionCombi.ser";

		ArrayList<LudRul> list = null;
		try
		{
			list = (ArrayList<LudRul>) deserialise(filePath);
			if (list != null)
			{
//				final LudRul lr = list.get(list.size() - 1); // test if type is
//																// correct
				// TODO: Use lr somehow.
			}
		} catch (final ClassCastException e)
		{
			e.printStackTrace();
			list = null;
		}

		if (list != null && !forceRecalc)
			return list;
		list = new ArrayList<>();
		final ArrayList<File> files = DistanceUtils.getAllLudiiGameFiles();

		for (int i = 0; i < files.size(); i++)
		{
			System.out.println(i + "\\" + files.size() + "\\" + list.size()
					+ " combinations collected sofar");
			final File file = files.get(i);
			final List<List<String>> allPossibleRuleSet = DistanceUtils
					.getAllPossibleRuleSetsFromLudFile(file);
			for (final List<String> singleRuleSet : allPossibleRuleSet)
			{
				final LudRul entry = new LudRul(file, singleRuleSet);
				list.add(entry);
			}

		}

		serialise(list, filePath);
		return list;
	}

	/**
	 * Returns the rulesets the game related to the lud file suggests
	 * 
	 * @param file
	 * @return
	 */
	private static List<List<String>>
			getAllPossibleRuleSetsFromLudFile(final File file)
	{
		final Game game = GameLoader.loadGameFromFile(file);
		final ArrayList<List<String>> ruleSetList = new ArrayList<>();
		final List<Ruleset> rulesets = game.description().rulesets();
		for (final Ruleset ruleset : rulesets)
		{
			ruleSetList.add(ruleset.optionSettings());
		}
		if (rulesets.size() == 0)
			ruleSetList.add(new ArrayList<String>());
		return ruleSetList;
	}

	/**
	 * seriales the object to the path
	 * 
	 * @param <T>
	 * @param object
	 * @param filePath
	 */
	private static <T> void serialise(final T object, final String filePath)
	{
		try (final FileOutputStream fout = new FileOutputStream(filePath);
				final ObjectOutputStream oos = new ObjectOutputStream(fout);)
		{
			oos.writeObject(object);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * deserialises the object, casting needs to be done outside this method
	 * 
	 * @param filePath
	 * @return
	 */
	private static Object deserialise(final String filePath)
	{
		Object object = null;
		try (final FileInputStream fint = new FileInputStream(filePath);
				final ObjectInputStream oos = new ObjectInputStream(fint);)
		{
			object = oos.readObject();
		} catch (IOException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return object;
	}

	/**
	 * Maps the folder to the containing games and each ruleset combination
	 * 
	 * @return map of folder to the containing ruleset combination
	 */
	public static HashMap<String, List<LudRul>>
			getFolderToPossibleGameOptionCombination()
	{
		final HashMap<String, List<LudRul>> folderToGameOptionCombinations = new HashMap<>();
		final ArrayList<LudRul> list = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false);

		for (final LudRul entry : list)
		{
			final String folder = DistanceUtils
					.getCurrentFolderName(entry.getFile());
			List<LudRul> s = folderToGameOptionCombinations.get(folder);
			if (s == null)
			{
				s = new ArrayList<LudRul>();
				folderToGameOptionCombinations.put(folder, s);
			}
			s.add(entry);
		}

		return folderToGameOptionCombinations;
	}

	/**
	 * combines the game name and the game name to a string
	 * 
	 * @param candidate
	 * @return a String containing the game name and the selected rule options
	 */
	public static String getGameNameIncludingOption(
			final Entry<File, List<String>> candidate
	)
	{
		return candidate.getKey().getName() + candidate.getValue().toString();
	}

	/**
	 * Export all the frequencies and the distribution of all ludemes in all
	 * Ludii games within the game folder to a CSV file, which then can be
	 * imported in Weka.
	 */
	public static void exportAllBoardGamesForWeka(final File folder)
	{
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false);
		candidates.removeIf(new Predicate<LudRul>()
		{
			@Override
			public boolean test(final LudRul arg0)
			{
				return !arg0.getFile().getAbsolutePath().contains("board");
			}
		});
		final HashSet<String> wordList = new HashSet<>();
		final HashMap<LudRul, Map<String, Integer>> allFrequencies = new HashMap<>();
		final HashMap<LudRul, Map<String, Double>> allDistributions = new HashMap<>();
		for (int i = 0; i < candidates.size(); i++)
		{
			System.out.println("Processed " + i + " of " + candidates.size());
			final LudRul ludRul = candidates.get(i);
			final String desc = ludRul.getDescriptionExpanded();
			final Map<String, Integer> frequencyA = JensenShannonDivergence
					.getFrequencies(desc);
			final Map<String, Double> distributionA = JensenShannonDivergence
					.frequencyToDistribution(frequencyA);
			wordList.addAll(frequencyA.keySet());
			allDistributions.put(ludRul, distributionA);
			allFrequencies.put(ludRul, frequencyA);
		}
		final boolean addInstanceName = false;
		storeAsCSV(addInstanceName, wordList, allDistributions, folder,
				"wekaDistribution.csv");
		storeAsCSV(addInstanceName, wordList, allFrequencies, folder,
				"wekaFrequency.csv");
	}

	/**
	 * Stores the frequencies or distribution of every word for every Ludeme
	 * Rule Combination into a CSV file
	 * 
	 * @param <T>              Works for Integer or Double
	 * @param addInstanceName  True if the CSV should contain the name of the
	 *                         Ludii game
	 * @param wordList         The complete vocabulary. (This contains also
	 *                         Strings)
	 * @param allDistributions The distribution or frequencies for every Ludeme
	 *                         Rule combination
	 * @param folder
	 * @param filename
	 */
	private static <T> void storeAsCSV(
			final boolean addInstanceName, final HashSet<String> wordList,
			final HashMap<LudRul, Map<String, T>> allDistributions,
			final File folder, final String filename
	)
	{
		final ArrayList<String> worldArray = new ArrayList<>(wordList);
		Collections.sort(worldArray);
		final StringBuilder sb = new StringBuilder();
		if (addInstanceName)
			sb.append("name,");
		for (final String string : worldArray)
		{
			sb.append(string + ",");
		}
		sb.append("class\n");

		for (final LudRul game : allDistributions.keySet())
		{
			if (addInstanceName)
				sb.append(game.getGameNameIncludingOption().replace(",", "")
						.replace(" ", "").replace("'", "") + ",");
			final Map<String, T> dist = allDistributions.get(game);
			for (final String string : worldArray)
			{
				final T value = dist.get(string);
				if (value == null)
					sb.append("0" + ",");
				else
					sb.append(value + ",");

			}
			sb.append(game.getCurrentFolderName() + "\n");
		}
		final String path = folder.getAbsolutePath() + File.separator
				+ filename;

		try (final PrintWriter out = new PrintWriter(path);)
		{
			out.println(sb.toString());
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

//	/**
//	 * Gets all ludi game files and returns a map, which connects game name to the respective folder
//	 * @return
//	 */
//	public static Map<String, String> createGameNameToFolderMap()
//	{
//		final ArrayList<File> list = getAllLudiiGameFiles();
//		
//		final Map<String,String> map = new HashMap<>();
//		
//		for (final File file : list) {
//			final String gameName = getGameNameFromFile();
//			final String folderName =getCurrentFolderName(file);
//			
//			map.put(gameName, folderName);
//		}
//		return map;
//	}

//	public static Map<String, List<File>> getFolderToContainingLudFilesMap()
//	{
//		final ArrayList<File> files = DistanceUtils.getAllLudiiGameFiles();
//		
//		final Map<String,List<File>> folderNameToGameFiles = new HashMap<>();
//		
//		for (final File file : files) {
//			;
//			final String folder = DistanceUtils.getCurrentFolderName(file);
//			List<File> containingFiles = folderNameToGameFiles.get(folder);
//			if (containingFiles==null) {
//				containingFiles= new ArrayList<File>();
//				folderNameToGameFiles.put(folder, containingFiles);
//			}
//			
//		}
//		return folderNameToGameFiles;
//	}
}
