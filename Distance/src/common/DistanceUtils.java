package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import game.Game;
import main.StringRoutines;
import main.collections.ListUtils;
import main.options.Option;
import main.options.Ruleset;
import metrics.DistanceMetric;
import metrics.groupBased.EndConditionLudemeSuffixTree;
import metrics.groupBased.JensenShannonTFIDFDivergence;
import metrics.groupBased.MultiMetricAvgDistance;
import metrics.groupBased.SuffixTreeDistance;
import metrics.individual.CosineSimilarity;
import metrics.individual.FeatureDistance;
import metrics.individual.JaccardSimilarity;
import metrics.individual.JensenShannonDivergence;
import metrics.individual.Levenshtein;
import metrics.individual.LudemeGlobalAlignment;
import metrics.individual.LudemeLocalAlignment;
import metrics.individual.LudemeRepeatedLocalAlignment;
import metrics.individual.ZhangShasha;
import metrics.moveBased.MoveTypeJensonShannon;
import metrics.suffix_tree.Alphabet;
import metrics.suffix_tree.Letteriser;
import metrics.suffix_tree.SuffixTreeCollapsed;
import metrics.support.DistanceProgressListener;
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
		generateOutput(orderOfGames, distanceMatrix, outputFolder, fileName+"with",
				false);
		generateOutput(orderOfGames, distanceMatrix, outputFolder, fileName+"withOut",
				true);
	}

	private static void generateOutput(
			final List<String> orderOfGames, final double[][] distanceMatrix,
			final File outputFolder, final String fileName,
			boolean ignoreGameName
	)
	{
		final int numberOfTaxa = orderOfGames.size();
		final ArrayList<String> lines = new ArrayList<String>();
		lines.add(String.format("#nexus\n\n" + "BEGIN Taxa;\n"
				+ "DIMENSIONS ntax=%d;\n" + "TAXLABELS", Integer.valueOf(numberOfTaxa)));

		for (int i = 0; i < numberOfTaxa; i++)
		{

			String gameName = orderOfGames.get(i).replace(" ", "_");
			gameName = gameName.replace("[", "_");
			gameName = gameName.replace("]", "_");
			gameName = gameName.replace(",", "");
			gameName = gameName.replace("=", "");
			gameName = Normalizer.normalize(gameName, Normalizer.Form.NFKD);
			gameName = gameName.replace("\\?", "");
			if (ignoreGameName)gameName = "" + (i+1);
			final String toAdd = String
					.format("[%d] '%s'", Integer.valueOf(i + 1), gameName)
					.replace("(", "").replace(")", "").replace("'", "");
			lines.add(toAdd);
		}
		lines.add(String.format(";\n" + "END; [Taxa]\n" + "\n"
				+ "BEGIN Distances;\n" + "DIMENSIONS ntax=%d;\n"
				+ "FORMAT labels=no diagonal triangle=upper;\n" + "MATRIX",
				Integer.valueOf(numberOfTaxa)));

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
	public static Trial[] generateRandomTrialsFromGame(
			final Game game, final int NUM_PLAYOUTS, final int NUM_MAX_MOVES
	)
	{
		final Trial[] allTrials = new Trial[NUM_PLAYOUTS];
		
		for (int i = 0; i < NUM_PLAYOUTS; i++)
		{
			final Trial trialA = new Trial(game);
			final Context context = new Context(game, trialA);
			game.start(context);
			game.playout(context, null, 1.0, null, 0, NUM_MAX_MOVES, ThreadLocalRandom.current());
			
			allTrials[i] = trialA;
		}

		return allTrials;
	}

	/**
	 * <p>
	 * "\\Evaluation\\src\\distance\\out\\test.txt" would return "out"
	 * 
	 * @param file
	 * @return the name of the current folder this file is in. adds parent folder if its named other
	 */
	public static String getCurrentFolderName(final File file)
	{
		final String splitRegex = Pattern
				.quote(System.getProperty("file.separator"));
		final String[] splittedFileName = file.getAbsolutePath()
				.split(splitRegex);
		String folderName = splittedFileName[splittedFileName.length - 2];
		
		if (folderName.equals("other"))
			folderName = folderName+"_"+splittedFileName[splittedFileName.length - 3];
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
		return getAllLudiiGameFiles(FolderLocations.ludFolder);
	}
	
	/**
	 * 
	 * @param startFolder if null returns all games, otherwise ensures that
	 *                     path contains targetFolder
	 * @return a list of games which contain the target folder
	 */
	public static ArrayList<File>
			getAllLudiiGameFiles(final File startFolder)
	{
		if (startFolder==null)return getAllLudiiGameFiles();
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

					if (path.contains("Common/res/lud/plex"))
						continue;

					if (path.contains("Common/res/lud/wishlist"))
						continue;

					if (path.contains("Common/res/lud/userWishlist"))
						continue;
					if (path.contains("Common/res/lud/WishlistDLP"))
						continue;

					if (path.contains("Common/res/lud/wip"))
						continue;

					if (path.contains("Common/res/lud/test"))
						continue;

					if (path.contains("Common/res/lud/bad"))
						continue;

					if (path.equals("../Common/res/lud/bad_playout"))
						continue;

					gameDirs.add(fileEntry);
				} else if (fileEntry.getName().contains(".lud"))
				{
					entries.add(fileEntry);
				}
			}
		}
		return entries;
	}

	

	/**
	 * @param forceRecalc
	 * @param dpl TODO
	 * @return all possible ludii ruleset combinations currently in the system
	 */
	public static ArrayList<LudRul>
			getAllLudiiGameFilesAndRulesetCombination(final boolean forceRecalc, final File startFolder, final DistanceProgressListener dpl)
	{
		return getAllLudiiGameFilesAndRulesetCombination(FolderLocations.resourceFolder,startFolder,
				forceRecalc, dpl);
	}

	/**
	 * 
	 * @param forceRecalc
	 * @param dpl TODO
	 * @return all possible ludii ruleset combinations currently in the system
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<LudRul> getAllLudiiGameFilesAndRulesetCombination(
			final File resourceFold,final File startFolder, final boolean forceRecalc, final DistanceProgressListener dpl
	)
	{
		
		final ArrayList<File> files = DistanceUtils.getAllLudiiGameFiles(startFolder);
		final int hk = getFilesHashcode(files);
		final String filePath = resourceFold.getAbsolutePath() + File.separator
				+ "ludiiGameFilesOptionCombi_"+startFolder.getName() + "_" + hk+ ".ser";
		ArrayList<LudRul> list = null;
		try
		{
			list = (ArrayList<LudRul>) deserialise(filePath);
			if (list != null && !checkListSize(list,files))
			{
				list = null;
			}
		} catch (final ClassCastException e)
		{
			e.printStackTrace();
			list = null;
		}

		if (list != null && !forceRecalc)
			return list;
		list = new ArrayList<>();
		
		if (!forceRecalc&&folderParentOfOther(FolderLocations.ludFolder,startFolder)) {
			final ArrayList<LudRul> completeLudFolder = getAllLudiiGameFilesAndRulesetCombination(
					resourceFold,FolderLocations.ludFolder, false, dpl);
			final ArrayList<LudRul> toRemove = new ArrayList<>();
			outer:for (final LudRul ludRul : completeLudFolder)
			{
				final Path path1 = Paths.get(ludRul.getFile().getAbsolutePath());
				for (final File file : files)
				{	
					final Path path2 = Paths.get(file.getAbsolutePath());
					try
					{
						if (Files.isSameFile(path1,path2))continue outer;
					} catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
				toRemove.add(ludRul);
			}
			completeLudFolder.removeAll(toRemove);
			list.addAll(completeLudFolder);
		}else {
			for (int i = 0; i < files.size(); i++)
			{
				if (dpl!=null)dpl.update(i, files.size());
				System.out.println(i + "\\" + files.size() + "\\" + list.size()
						+ " combinations collected sofar");
				final File file = files.get(i);
				try
				{
					getAllPossibleLudRulFromFile(list, file);
				} catch (final Exception e)
				{
					System.out.println("error with " + file);
					e.printStackTrace();
				}
				

			}
		}
		
		

		serialise(list, filePath, false);
		return list;
	}

	private static boolean folderParentOfOther( final File parentFolder,final File childFolder)
	{
		try
		{
			final String pathChild = childFolder.getCanonicalPath();
			final String pathParent = parentFolder.getCanonicalPath();
			if (pathChild.startsWith(pathParent)&&!pathParent.equals(pathChild))
				return true;
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public static void getAllPossibleLudRulFromFile(
			final ArrayList<LudRul> listToAddTo, final File file
	)
	{
		final List<List<String>> allPossibleRuleSet = DistanceUtils
				.getAllPossibleRuleSetsFromLudFile(file);
		for (final List<String> singleRuleSet : allPossibleRuleSet)
		{
			final LudRul entry = new LudRul(file, singleRuleSet);
			listToAddTo.add(entry);
		}
	}

	/**
	 * 
	 * @param files
	 * @return hashCode relating to the file names. (not their paths)
	 */
	private static int getFilesHashcode(final ArrayList<File> files)
	{
		final ArrayList<String> names = new ArrayList<>(files.size());
		for (final File file : files)
		{
			names.add(file.getName());
		}
		return names.hashCode();
	}

	private static boolean checkListSize(
			final ArrayList<LudRul> list, final ArrayList<File> files
	)
	{
		final HashSet<File> memoryFiles = new HashSet<>();
		for (final LudRul ludRul : list)
		{
			memoryFiles.add(ludRul.getFile());
		}
		return (files.size()==memoryFiles.size());
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
		final ArrayList<List<String>> ruleSetList;
		final HashSet<List<String>> ruleSetSet = new HashSet<>();
		final List<Ruleset> rulesets = game.description().rulesets();
		for (final Ruleset ruleset : rulesets)
		{
			ruleSetSet.add(ruleset.optionSettings());
		}
		ruleSetList = new ArrayList<>(ruleSetSet);
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
	 * @param zipCompress TODO
	 */
	public static <T> void serialise(final T object, final String filePath, final boolean zipCompress)
	{
		if (zipCompress) {
			try (final FileOutputStream fout = new FileOutputStream(filePath);
					GZIPOutputStream gzip = new GZIPOutputStream(fout);
					ObjectOutputStream oos = new ObjectOutputStream(gzip)
					)
			{
				oos.writeObject(object);
				oos.flush();
				oos.close();
				
			} catch (final IOException e)
			{
				e.printStackTrace();
			}
		}else {
			try (final FileOutputStream fout = new FileOutputStream(filePath);
					final ObjectOutputStream oos = new ObjectOutputStream(fout);)
			{
				oos.writeObject(object);
				oos.flush();
				oos.close();
			} catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		

	}

	/**
	 * deserialises the object, casting needs to be done outside this method
	 * 
	 * @param filePath
	 */
	public static Object deserialise(final String filePath)
	{
		Object object = null;
		try (final FileInputStream fint = new FileInputStream(filePath);
				final ObjectInputStream oos = new ObjectInputStream(fint);)
		{
			object = oos.readObject();
			oos.close();
			
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
			getFolderToPossibleGameOptionCombination(final File startFolder)
	{
		final HashMap<String, List<LudRul>> folderToGameOptionCombinations = new HashMap<>();
		final ArrayList<LudRul> list = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,startFolder, null);

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
	 * @param numPlayouts 
	 * @param numMaxMoves 
	 */
	public static void exportActionFrequenciesForWeka(final File output,final File startFolder, final int numPlayouts, final int numMaxMoves)
	{
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,startFolder, null);
		
		final HashSet<String> wordList = new HashSet<>();
		final HashMap<LudRul, Map<String, Integer>> allFrequencies = new HashMap<>();
		final HashMap<LudRul, Map<String, Double>> allDistributions = new HashMap<>();
		for (int i = 0; i < candidates.size(); i++)
		{
			System.out.println("Processed " + i + " of " + candidates.size());
			final LudRul ludRul = candidates.get(i);
			
			final Trial[] trials = DistanceUtils.generateRandomTrialsFromGame(ludRul.getGame(), numPlayouts, numMaxMoves);
			final Map<String, Integer> frequency = MoveTypeJensonShannon.getFrequencies(Letteriser.lowRes,ludRul.getGame(),trials);
			final Map<String, Double> distribution = JensenShannonDivergence.frequencyToDistribution(frequency);
			
			wordList.addAll(frequency.keySet());
			allDistributions.put(ludRul, distribution);
			allFrequencies.put(ludRul, frequency);
		}
		final boolean addInstanceName = false;
		storeAsCSV(addInstanceName, wordList, allDistributions, output,
				"wekaActionDistribution"+ startFolder.getName() + ".csv");
		storeAsCSV(addInstanceName, wordList, allFrequencies, output,
				"wekaActionFrequency"+ startFolder.getName() +".csv");
	}
	
	public static void exportLudiiFrequenciesForWeka(
			ArrayList<LudRul> candidates, File outputFolder, DistanceProgressListener pl
	)
	{
		final HashSet<String> wordList = new HashSet<>();
		final HashMap<LudRul, Map<String, Integer>> allFrequencies = new HashMap<>();
		final HashMap<LudRul, Map<String, Double>> allDistributions = new HashMap<>();
		for (int i = 0; i < candidates.size(); i++)
		{
			pl.update(i, candidates.size());
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
		pl.update(candidates.size(), candidates.size());
		 boolean addInstanceName = false;
		storeAsCSV(addInstanceName, wordList, allDistributions, outputFolder,
				"wekaLudiDistribution"+ ".csv");
		storeAsCSV(addInstanceName, wordList, allFrequencies, outputFolder,
				"wekaLudiFrequency"+ ".csv");
		addInstanceName = true;
		storeAsCSV(addInstanceName, wordList, allDistributions, outputFolder,
				"wekaLudiDistribution_IncludingInstanceName" + ".csv");
		storeAsCSV(addInstanceName, wordList, allFrequencies, outputFolder,
				"wekaLudiFrequency_IncludingInstanceName" +".csv");
	}
	/**
	 * Export all the frequencies and the distribution of all ludemes in all
	 * Ludii games within the folder (and those folders) to a CSV file, which then can be
	 * imported in Weka.
	 */
	public static void exportLudiiFrequenciesForWeka(final File outputFolder,final File startFolder,DistanceProgressListener dpl)
	{
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,startFolder, null);
		exportLudiiFrequenciesForWeka(candidates, outputFolder, dpl);
		
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
				sb.append(game.getGameNameIncludingOption(false).replace(",", "")
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
			sb.append(game.getCurrentClassName() + "\n");
		}
		final String path = folder.getAbsolutePath() + File.separator
				+ filename;

		try (final PrintWriter out = new PrintWriter(path);)
		{
			out.println(sb.toString());
			out.flush();
			out.close();
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	public static <T> void storeAsCsv(final List<String> headers, final List<List<T>> values, final String folder, final String filename, final char seperator) {
		final StringBuilder sb = new StringBuilder();
		for (final String headersNames : headers)
		{
			sb.append(headersNames + seperator);
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("\n");
		for (final List<T> list : values)
		{
			for (final T words : list)
			{
				sb.append(words.toString() + seperator);
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
		}
		
		final String path = folder + File.separator
				+ filename;

		try (final PrintWriter out = new PrintWriter(path);)
		{
			out.println(sb.toString());
			out.flush();
			out.close();
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return all distance metrics implemented (which do NOT need preprocessing)
	 */
	public static List<DistanceMetric> getAllDistanceMetricesWithoutPreprocessing()
	{
		final ArrayList<DistanceMetric> metrices = new ArrayList<>();
		
		metrices.add(new JensenShannonDivergence());
		metrices.add(new CosineSimilarity());
		metrices.add(new JaccardSimilarity());
		metrices.add(new LudemeRepeatedLocalAlignment());
		metrices.add(new LudemeLocalAlignment());
		metrices.add(new LudemeGlobalAlignment());
		metrices.add(new MoveTypeJensonShannon(Letteriser.lowRes,40,60));
		
		metrices.add(new Levenshtein()); 
		metrices.add(new ZhangShasha());
		metrices.add(new SuffixTreeDistance(Letteriser.lowRes,40,60));
		metrices.add(new FeatureDistance());
		//metrices.add(new MoveTypeTrialsAssignmentCost(new EditCost()));
		
		return metrices;
	}

	/**
	 * 
	 * @return all distance metrics implemented (which do need preprocessing)
	 */
	public static List<DistanceMetric> getAllDistanceMetricesWithPreprocessing()
	{
final ArrayList<DistanceMetric> metrices = new ArrayList<>();
		
		metrices.add(JensenShannonTFIDFDivergence.createPlaceHolder());
		metrices.add(MultiMetricAvgDistance.createPlaceHolder());
		metrices.add(EndConditionLudemeSuffixTree.createPlaceHolder());
		
		
		return metrices;
	}
	
	/**
	 * 
	 * @param candidates
	 * @param outputtmpfolder
	 * @param string
	 */
	public static SuffixTreeCollapsed generateKeyWordSuffixTreeCollapsed(
			final ArrayList<LudRul> candidates,
			final File outputtmpfolder, final String string
	)
	{
		final StringCleaner cleaner = StringCleaner.cleanKeepUnderScoreAndColon;
		final int nInstances = candidates.size();
		final String[][] keyWords = new String[nInstances][];
		for (int i = 0; i < nInstances; i++)
		{
			final LudRul ludRul = candidates.get(i);
			final String fullexpansion = ludRul.getDescriptionExpanded();
			//System.out.println(ludRul.getGameNameIncludingOption(true));
			//System.out.println(fullexpansion);
			final String fullexpansionFlattened = DistanceUtils.flatten(fullexpansion);
			System.out.println(fullexpansionFlattened);
			storeString(FolderLocations.resourceTmpFolder + "/" + ludRul.getGameNameIncludingOption(true) + "org.txt",fullexpansion);
			storeString(FolderLocations.resourceTmpFolder + "/" + ludRul.getGameNameIncludingOption(true) + ".txt",fullexpansionFlattened);
			keyWords[i] = cleaner.cleanAndSplit(fullexpansionFlattened);
			System.out.println(i + " " + nInstances);
		}
		final HashSet<String> wordList = new HashSet<>();
		for (final String[] strings : keyWords)
		{
			wordList.addAll(Arrays.asList(strings));
		}
		final Alphabet a = new Alphabet(wordList);
		
		final SuffixTreeCollapsed st = new SuffixTreeCollapsed();
		st.setAlphabet(a);
		for (int i = 0; i < keyWords.length; i++)
		{
			System.out.println(i + " " + keyWords.length);
			final String[] string2 = keyWords[i];
			st.insertIntoTree(a.encode(string2));
		}
		st.assessAllOccurences();
		st.printSizes();
		return st;
		
	}

	/**
	 * writes the file into the path
	 * @param path (has to include file suffix.)
	 * @param string
	 */
	public static void storeString(
			final String path, final String string
	)
	{
		try (final PrintWriter out = new PrintWriter(path))
		{
				out.print(string);
				out.flush();
				out.close();
			
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
		
	}

	/**
	 * replace "(word" with "word_" and the closing bracked with "_word"
	 * @param fullexpansionSource The full expanded source.
	 */
	public static String flatten(final String fullexpansionSource)
	{
		String fullexpansion = fullexpansionSource.replace(":", ": ");
		fullexpansion = fullexpansion.replace("\n", "");
		final StringBuilder sb = new StringBuilder((int) (fullexpansion.length()*1.2));
		final char[] ca = fullexpansion.toCharArray();
		final LinkedList<LinkedList<Character>> containerWords = new LinkedList<>();
		
		for (int i = 0; i < ca.length; i++)
		{
			char c = ca[i];
			
			if (c=='(') {
				c = ca[++i];
				final LinkedList<Character> containerword = new LinkedList<>();
				while(!Character.isWhitespace(c)&&c!=')') {
					
					containerword.add(Character.valueOf(c));
					c = ca[++i];
				}
				if (c==')') {
					sb.append('_');
					for (final Character ch : containerword) { 
			            sb.append(ch); 
			        }
					sb.append('_');
					continue;
				}else {
					containerWords.addLast(containerword);
					for (final Character ch : containerword) { 
			            sb.append(ch); 
			        }
					sb.append('_');
				}
				 
				
				
			}
			if (c==')') {
				sb.append(" ");
				sb.append('_');
				final LinkedList<Character> cw = containerWords.removeLast();
				for (final Character ch : cw) { 
		            sb.append(ch); 
		        } 
				
				sb.append(' ');
				continue;
			}
			sb.append(c);
		}
		
		if (containerWords.size()!=0) {
			System.out.println("something wrong in DistanceUtils flatten");
		}
		
		return sb.toString();
	}

	/**
	 * just a quick method such that getFirstLudiiGameFileStartingWith("Chess") gets you chess
	 * @param name The affix for the game name search for
	 * @return the first game found, which name is starting with String name
	 */
	public static LudRul getFirstLudiiGameFileStartingWith(final String name)
	{
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.boardFolder, null);
		
		
		for (int i = candidates.size()-1; i >= 0; i--)
		{
			final String candidateName = candidates.get(i).getGameNameIncludingOption(true);
			if (candidateName.startsWith(name)) {
				return candidates.get(i);
			}
		}
		return null;
	}

	public static ArrayList<LudRul> getContainingLudRuls(final File[] f, final boolean forceRecalculation, final DistanceProgressListener dpl)
	{
		final ArrayList<LudRul> newOnes = new ArrayList<>();
		for (final File file : f)
		{
			if (file.isDirectory()) {
				final ArrayList<LudRul> list = getAllLudiiGameFilesAndRulesetCombination(forceRecalculation, file, dpl);
				newOnes.addAll(list);
			}else if (file.isFile()) {
				getAllPossibleLudRulFromFile(newOnes, file);
			}
		}
		return newOnes;
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
