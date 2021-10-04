package distance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.DistanceMatrix;
import common.DistanceUtils;
import common.EvaluatorDistanceMetric;
import common.FolderLocations;
import common.LudRul;
import common.Score;
import common.SimilarityMatrix;
import compiler.Compiler;
import game.Game;
import game.types.board.RelationType;
import game.types.board.SiteType;
import main.FileHandling;
import main.grammar.Description;
import main.math.Point3D;
//import org.junit.Test;
import metrics.DistanceMetric;
import metrics.groupBased.MultiMetricAvgDistance;
import metrics.groupBased.SuffixTreeDistance;
import metrics.individual.JensenShannonDivergence;
import metrics.individual.LudemeGlobalAlignment;
import metrics.individual.LudemeLocalAlignment;
import metrics.suffix_tree.GraphComposer;
import metrics.suffix_tree.Letteriser;
import metrics.suffix_tree.SuffixTree;
import metrics.suffix_tree.SuffixTreeCollapsed;
import metrics.suffix_tree.TreeBuildingIngredients;
import other.GameLoader;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.trial.Trial;
import processing.kmedoid.KmedoidClustering;
import processing.similarity_matrix.AssignmentSettings;
import processing.similarity_matrix.Visualiser;

/**
 * Test file for distance metrics.
 * 
 * @author markus
 */
public class TestDistancesMarkus
{
	@SuppressWarnings("unused")
	private static final int NUM_PLAYOUTS = 30;
	@SuppressWarnings("unused")
	private static final int NUM_MAX_MOVES = 50;

	public static void main(final String[] args)
	{
		final TestDistancesMarkus app = new TestDistancesMarkus();
		System.out.println("�");
		// app.testRandomDistanceMetrics();
		//app.exportWekaCSV();
		//testSuffixTreeMemory();
		//testLetteriser();
		//testAllAlphabets();
		
		//app.testSuffixTree();
		
		//final SuffixTreeExpanded st = new SuffixTreeExpanded(c, 20,50, Letteriser.lowRes);
		/*final Trial[] trials = DistanceUtils.generateRandomTrialsFromGame(c.getGame(), 1, 800);
		final String[] words = Letteriser.lowRes.getWords(trials);
		final TreeBuildingIngredients ingredients = Letteriser.lowRes.createTreeBuildingIngredients(trials);
		System.out.println(ingredients.getAlphabet());
		final SuffixTreeCollapsed stc = new SuffixTreeCollapsed();
		stc.setAlphabet(ingredients.getAlphabet());
		stc.insertAllIntoTree(ingredients.getConvertedTrials());
		stc.assessAllOccurences();
		stc.exportGml(FolderLocations.outputTmpFolder, "game", false);
		stc.printSizes();*/
		// st.printSupport();
		//System.out.println(c.getGameNameIncludingOption(false));
		// GameLoading.loadGameFromName(c.getFile().getAbsolutePath(),c.getRuleSet(),true);
		/*
		 * final ArrayList<LudRul> candidates2 = DistanceUtils
		 * .getAllLudiiGameFilesAndRulesetCombination(false,DistanceUtils.
		 * boardFolder); final ArrayList<LudRul> candidates3 = DistanceUtils
		 * .getAllLudiiGameFilesAndRulesetCombination(false,DistanceUtils.
		 * ludFolder);
		 */
		
		 
		 
		 //app.testSuffixTreeSeveralInsertions();
		 //app.testSuffixTreeSeveralInsertions();
		 
		//app.testSuffixTreeAllKeywords();
		// app.testSuffixTreeDistance();
		app.testSimilarityMatrixOMetrics();
		// app.testAllDistanceMetrics();
		// app.generateKmeans();
		// final Double score = Double.valueOf(EvaluationDistanceMetric
		// .evaluateDistanceMeasureNearestNeighbours(new
		// JensenShannonDivergence(),
		// 3));
		// System.out.println(score);

		// app.createSimilarityMatrix();
	}

	@SuppressWarnings("unused")
	private static void testAllAlphabets()
	{
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.boardFolder, null);
		final List<String> header = new ArrayList<String>();
		header.add("name"); header.add("alphabetSize"); header.add("letters");
		final List<List<String>> values = new ArrayList<List<String>>();
		
		final List<List<String>> values2 = new ArrayList<List<String>>();
		
		for (int i = 0; i < candidates.size(); i++)
		{
			final LudRul ludRul = candidates.get(i);
			System.out.println("Alphabet Collected for: " + i + "/" + candidates.size() + " currently at: " + ludRul.getGameNameIncludingOption(true));
			
			final ArrayList<String> value = new ArrayList<>();
			values.add(value);
			value.add(ludRul.getGameNameIncludingOption(true));
			final Trial[] trials = DistanceUtils.generateRandomTrialsFromGame(ludRul.getGame(), 30, 200);
			final Letteriser let = Letteriser.lowRes;
			
			final TreeBuildingIngredients tbi = let.createTreeBuildingIngredients(ludRul.getGame(),trials);
			
			
			final Set<String> wordList = tbi.getAlphabet().getWordList();
			value.add(wordList.size()+"");
			value.add(wordList.toString());
			
			final ArrayList<String> value2 = new ArrayList<>();
			
			final Letteriser let2 = Letteriser.moveDist;
			final Game game = ludRul.getGame();
			final TreeBuildingIngredients tbi2 = let2.createTreeBuildingIngredients(game,trials);
			final Set<String> wordList2 = tbi2.getAlphabet().getWordList();
			values2.add(value2);
			value2.add(ludRul.getGameNameIncludingOption(true));
			value2.add(wordList2.size()+"");
			value2.add(wordList2.toString());
		}
		
		DistanceUtils.storeAsCsv(header, values, FolderLocations.outputTmpFolder.getAbsolutePath(), "letteriserLow.csv", ';');
		DistanceUtils.storeAsCsv(header, values2, FolderLocations.outputTmpFolder.getAbsolutePath(), "letteriserMove.csv", ';');
		
		
		
	}

	public static void testLetteriser()
	{	
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.boardFolder, null);
		final long t1 = System.currentTimeMillis();
		for (final LudRul ludRul : candidates)
		{
			if (ludRul.getGameNameIncludingOption(false).startsWith("Bao Ki Arabu")) {
				candidates.clear();
				candidates.add(ludRul);
				break;
			}
		}
		for (final LudRul ludRul : candidates)
		{
			final LudRul c = ludRul;
			System.out.println(c.getGameNameIncludingOption(true));
			final Trial t = DistanceUtils.generateRandomTrialsFromGame(c.getGame(), 1, 400)[0];
			final Game game = c.getGame();
			final Letteriser l = Letteriser.moveDist;
			l.getWords(game,t);
			final Context context = new Context(c.getGame(), t);
			final Topology topology = context.board().topology();
			
			System.out.println("centrePoint" + context.board().topology().centrePoint());
			final ArrayList<TopologyElement> allGraphElements = topology.getAllGraphElements();
			for (int i = 0; i < allGraphElements.size(); i++)
			{
				final TopologyElement te = allGraphElements.get(i);
				System.out.println(i + "   " + te.centroid3D().x() + " " + te.centroid3D().y() + " "+ te.centroid3D().z());
			}
			context.board().topology().preGenerateDistanceToEachElementToEachOther(SiteType.Cell, RelationType.Orthogonal);
			context.board().topology().preGenerateDistanceToEachElementToEachOther(SiteType.Vertex, RelationType.All);
			context.board().topology().preGenerateDistanceToEachElementToEachOther(SiteType.Edge, RelationType.All);
			
			/*
			final int[][] dd = c.getGame().distancesToRegions();
			for (final int[] is : dd)
			{
				for (final int i : is)
				{
					System.out.print(i + " ");
				}
				System.out.println();
			}
			System.out.println(dd);
			/*
			final int[][] cellDist = context.board().topology().distancesToOtherSite(SiteType.Cell);
			final int[][] vertexDist = context.board().topology().distancesToOtherSite(SiteType.Vertex);
			final int[][] edgeDist = context.board().topology().distancesToOtherSite(SiteType.Edge);
			*/
			/*for (final int[] is : ddd)
			{
				for (final int i : is)
				{
					System.out.print(i + " ");
				}
				System.out.println();
			}
			System.out.println(dd);*/
			
			
			final java.awt.geom.Point2D.Double centre = topology.centrePoint();
			final List<Move> m = t.generateCompleteMovesList();
			for (final Move move : m)
			{
				System.out.println(move);
				for (final Action action : move.actions())
				{
					System.out.println(action);
					System.out.println(action.actionType());
					//action.toMoveFormat(context);
					
					
					if (action.isDecision()) {
						final double d3d =  getDistance(centre,allGraphElements,action.from(),action.to());
						final int dist = 0; 
						/*
						switch (action.fromType())
						{
						case Cell:
 							dist  = getDistance(cellDist,action.from(),action.to());
							break;
						case Edge:
							dist = getDistance(edgeDist,action.from(),action.to());
							break;
						case Vertex:
							dist = getDistance(vertexDist,action.from(),action.to());
							break;
						default:
							dist = 0;
							break;

						
						}*/
					 
					
						System.out.println(action.from() +" " + action.to() + " " + dist + " 3dDist " + d3d);
					}
					
				}
			}
			
			System.out.println("joa");
		}
		final long t2 = System.currentTimeMillis();
		System.out.println("took " + (t2-t1) + " to loop through");
		System.out.println("took " + (t2-t1) + " to loop through");
		//final LudRul c = candidates.get(candidates.size()-92);
		
		
		
		
	}

	private static double getDistance(
			final java.awt.geom.Point2D.Double centre, final ArrayList<TopologyElement> allGraphElements, final int from, final int to
	)
	{
		final boolean fromInList = (from>=0&&from <allGraphElements.size());
		final boolean toInList = (to>=0&&to<allGraphElements.size());
		
		Point3D fr0;
		Point3D fr1;
		if (fromInList&&toInList) {
			 fr0 = allGraphElements.get(from).centroid3D();
			 fr1 = allGraphElements.get(to).centroid3D();
			 return fr0.distance(fr1);
		}
		if (toInList) {
			fr1 = allGraphElements.get(to).centroid3D();
			return -fr1.distance(centre);
		}
		if (fromInList) {
			fr0 = allGraphElements.get(from).centroid3D();
			return fr0.distance(centre);
		}
		return 0;
	}



	@SuppressWarnings("unused")
	private static void testSuffixTreeMemory()
	{
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.boardFolder, null);
		final ArrayList<SuffixTreeCollapsed> allSufTree = new ArrayList<>();
		final Runtime runtime = Runtime.getRuntime();
		
		
		for (int i = 0; i < candidates.size(); i++)
		{
			
			final LudRul ludRul = candidates.get(i);
			System.out.println(i + "/" + candidates.size() + " " + ludRul.getGameNameIncludingOption(true));
			final Trial[] trials = DistanceUtils.generateRandomTrialsFromGame(ludRul.getGame(), 200, 400);
			final SuffixTreeCollapsed sf = new SuffixTreeCollapsed(ludRul.getGameNameIncludingOption(true), null , trials, Letteriser.lowRes);
			allSufTree.add(sf);
			final long memory = runtime.totalMemory() - runtime.freeMemory();
	        System.out.println("Used memory is bytes: " + memory);
	        System.out.println("Used memory is megabytes: "
	              + (memory)/1024/1024);
		}
		//its a pass yayyay
	}

	// -------------------------------------------------------------------------

	private final File outputfolder = new File("../Distance/out/");

// @Test
	public void createSimilarityMatrix()
	{
		final File folder = new File(outputfolder.getAbsolutePath()
				+ "/allSimilarities/");
		SimilarityMatrix.createSelfSimilarityMatrices(folder, null);
	}

	// @Test
	public void exportWekaCSV()
	{
		
		DistanceUtils.exportActionFrequenciesForWeka(outputfolder,
				FolderLocations.boardFolder, 100, 50);
		DistanceUtils.exportLudiiFrequenciesForWeka(outputfolder,
				FolderLocations.boardFolder,null);
	}

//	@Test
	public void testAllDistanceMetrics()
	{
		final ArrayList<DistanceMetric> metrices = new ArrayList<>();
		final HashMap<DistanceMetric, Double> scores = new HashMap<>();

		metrices.addAll(DistanceUtils.getAllDistanceMetricesWithoutPreprocessing());
		final AssignmentSettings ass = new AssignmentSettings(3, 3);
		for (final DistanceMetric distanceMetric : metrices)
		{
			System.out.println("Check " + distanceMetric.getClass().getName());

			final Double score = Double.valueOf(EvaluatorDistanceMetric
					.evaluateDistanceMeasureNearestNeighbours(
							FolderLocations.boardFolder, distanceMetric,
							ass).getCorrectlyAssignedRate());
			scores.put(distanceMetric, score);
			distanceMetric.releaseResources();
		}

		for (final DistanceMetric distanceMetric : metrices)
		{
			System.out.println(distanceMetric.getClass().getName() + ": "
					+ scores.get(distanceMetric));
		}
	}

//	@Test
	public void testJensenShannonDivergence() throws IOException
	{
		final ArrayList<File> games = getAllLudiiGameFiles(0, 800);
		final ArrayList<String> names = new ArrayList<>();
		final JensenShannonDivergence jsd = new JensenShannonDivergence();

		final double[][] dist = new double[games.size()][games.size()];
		for (int i = 0; i < games.size(); i++)
		{
			final Game game = getGameFromFile(games.get(i));

			names.add(game.name());
			System.out.println(i + "/" + games.size() + " " + game.name());
			for (int j = i + 1; j < games.size(); j++)
			{

				final Game game2 = getGameFromFile(games.get(j));
				System.out.println(i + "/" + games.size() + " " + game.name()
						+ "  " + j + "/" + games.size() + " " + game2.name());
				final Score score = jsd.distance(game, game2);
				dist[i][j] = score.score();
				dist[j][i] = score.score();
			}
		}

		DistanceMatrix.printSquareDistanceMatrixToFile("Games", names, dist,
				outputfolder, "DistancesJensonsShannon.csv", ",");
		DistanceUtils.generateSplitTreeInput(names, dist, outputfolder,
				"splitsJensonsShannon.txt");

	}

	// @Test
	public void testLudemeSmithWatermanAlignment() throws IOException
	{
		final ArrayList<File> games = getAllLudiiGameFiles(0, 800);
		final ArrayList<String> names = new ArrayList<>();
		final DistanceMetric jsd = new LudemeGlobalAlignment();

		final double[][] dist = new double[games.size()][games.size()];
		for (int i = 0; i < games.size(); i++)
		{
			final Game game = getGameFromFile(games.get(i));

			names.add(game.name());
			System.out.println(i + "/" + games.size() + " " + game.name());
			for (int j = i + 1; j < games.size(); j++)
			{
				final Game game2 = getGameFromFile(games.get(j));

				final Score score = jsd.distance(game, game2);
				dist[i][j] = score.score();
				dist[j][i] = score.score();
				System.out.println(i + "/" + games.size() + " " + game.name()
						+ "  " + j + "/" + games.size() + " " + game2.name()
						+ " :" + score.score());
			}
		}

		DistanceMatrix.printSquareDistanceMatrixToFile("Games", names, dist,
				outputfolder, "DistancesJensonsShannon.csv", ",");
		DistanceUtils.generateSplitTreeInput(names, dist, outputfolder,
				"splitsJensonsShannon.txt");
	}

//	@Test
	public void testLudemeSmithWatermanAlignmentBetweenTwo() throws IOException
	{
		final Game game1 = GameLoader.loadGameFromName("Tic-Tac-Four.lud");
		final Game game2 = GameLoader.loadGameFromName("Tic-Tac-Toe.lud");

		final DistanceMetric dm = new LudemeGlobalAlignment();
		System.out.println(dm.distance(game1, game2).score());

		final DistanceMetric dm2 = new LudemeLocalAlignment();
		System.out.println(dm2.distance(game1, game2).score());
	}

	

//	@Test
	public void testRandomDistanceMetrics()
	{
		final ArrayList<DistanceMetric> metrices = new ArrayList<>();
		final HashMap<DistanceMetric, Double> scores = new HashMap<>();

		metrices.addAll(DistanceUtils.getAllDistanceMetricesWithoutPreprocessing());
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.boardFolder, null);

		Collections.shuffle(candidates);
		while (candidates.size() > 50)
			candidates.remove(candidates.size() - 1);

		final AssignmentSettings ass = new AssignmentSettings(3, 3);

		for (final DistanceMetric distanceMetric : metrices)
		{
			System.out.println("Check " + distanceMetric.getClass().getName());

			final double ev = EvaluatorDistanceMetric
					.evaluateDistanceMeasureNearestNeighbours(distanceMetric,
							candidates, ass,null,false).getCorrectlyAssignedRate();

			scores.put(distanceMetric, Double.valueOf(ev));
			distanceMetric.releaseResources();
		}

		for (final DistanceMetric distanceMetric : metrices)
		{
			System.out.println(distanceMetric.getClass().getName() + ": "
					+ scores.get(distanceMetric));
		}
	}

//	@Test
	public void testSimilarityMatrixOMetrics()
	{
		
		
		
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.boardFolder, null);
		final ArrayList<LudRul> toRemove = new ArrayList<>();
		candidates.addAll(DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.knightFolder, null));
		
		toRemove.addAll(DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.otherLeapingFolder, null));
		toRemove.addAll(DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.otherWarFolder, null));
		candidates.removeAll(toRemove);
		final DistanceMetric metric = new MultiMetricAvgDistance(candidates);
		//final DistanceMetric metric = new JensenShannonTFIDFDivergence(candidates);
		//final DistanceMetric metric = new MoveTypeJensonShannon(Letteriser.moveDist,40,200);
		//final DistanceMetric metric = new CosineSimilarity();
		final boolean recalc = true;
		//final DistanceMetric metric = new JensenShannonDivergence();
		//final DistanceMetric metric = new JaccardSimilarity();
		
		System.out.println(candidates);
		final DistanceMatrix<LudRul, LudRul> distanceMatrix;
		
		distanceMatrix = EvaluatorDistanceMetric.getDistanceMatrix(candidates,
				metric, recalc, null );
		final AssignmentSettings ass = new AssignmentSettings(3, 3);
		final Visualiser v = new Visualiser(metric.getName(),candidates, distanceMatrix, ass);
		v.setVisible(true);
		metric.releaseResources();

	}

//@Test
	public static void generateKmeans()
	{
		//final DistanceMetric metric = new SuffixTreeDistance(Letteriser.lowRes,
			//	100, 50);
		final DistanceMetric metric = new JensenShannonDivergence();
		final File folder = FolderLocations.boardFolder;
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false, folder, null);
		final DistanceMatrix<LudRul, LudRul> distanceMatrix;
		distanceMatrix = EvaluatorDistanceMetric.getDistanceMatrix(candidates,
				metric, false, null);
		final int minK = 1;
		final int maxK = 12;
		final KmedoidClustering km = new KmedoidClustering();
		km.generateClusterings(candidates, distanceMatrix, minK, maxK);
		km.printKtoSSE();
	}

	private ArrayList<File> getAllLudiiGameFiles(
			final int startIndex, final int endIndex
	)
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<>();
		gameDirs.add(startFolder);

		// Find the .lud files (and not .def)
		final ArrayList<File> entries = new ArrayList<>();
		int counter = 0;
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
					if (!fileEntry.getPath().contains("sow"))
						continue;

					if (counter >= startIndex && counter < endIndex)
						entries.add(fileEntry);

					counter++;

				}
			}
		}
		return entries;
	}

	@SuppressWarnings("unused")
	private ArrayList<Game> getAllLudiiGames(
			final int startIndex, final int endIndex
	)
	{
		final ArrayList<File> lfiles = getAllLudiiGameFiles(startIndex,
				endIndex);
		final ArrayList<Game> games = new ArrayList<>(lfiles.size());

		for (int i = startIndex; i < lfiles.size() && i < endIndex; i++)
		{

			final File file = lfiles.get(i);
			final Game game = getGameFromFile(file);
			System.out.println("Compiling game " + i + " "
					+ Math.min(lfiles.size(), endIndex));
		}
		return games;
	}

	private Game getGameFromFile(final File file)
	{
		final String filepath = file.getPath();

		String descA;
		Game gameA = null;
		try
		{
			descA = FileHandling.loadTextContentsFromFile(filepath);
			gameA = (Game)Compiler.compileTest(new Description(descA), false);

		} catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
		return gameA;
	}

	// @Test
	@SuppressWarnings("unused")
	private boolean testSuffixTree()
	{
		final ArrayList<String> testWords = new ArrayList<>();
		final boolean compose = false;
		// words from http://jeff560.tripod.com/words4.html
		testWords.add("AAAAAAAAAAAA");
		testWords.add("CACAO");
		testWords.add("TARAMASALATA");
		testWords.add("IONONNONIONI");
		testWords.add("FLOODDOORROOMMOONLIGHTERS");
		testWords.add("PEEKEENEENEE");
		testWords.add("ADCOMSUBORDCOMPHIBSPAC");
		testWords.add("BANNANNNANANNNANNANANNNNNA");
		//testWords.add("BANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNABANNANNNANANNNANNANANNNNNA");

		final GraphComposer gc = new GraphComposer();
		
		for (int i = 0; i < testWords.size(); i++)
		{
			final String testWord = testWords.get(i);
			final long time1 = System.currentTimeMillis();
			final SuffixTree st2 = new SuffixTreeCollapsed(testWord);
			//st2.printSupport();

			final long time2 = System.currentTimeMillis();
			String fileName = testWord;
			if (testWord.length() > 30)
				fileName = testWord.substring(0, 30);
			if (compose) {
				gc.compose(FolderLocations.outputfolder, fileName + i, st2, false);
				gc.compose(FolderLocations.outputfolder, fileName + i + "suf", st2,
						true);
			}
			
			st2.printAlphabet();

			long timeBrute = 0;
			long timeTree = 0;
			for (int j = 0; j < testWord.length(); j++)
			{
				for (int k = j + 1; k <= testWord.length(); k++)
				{
					final String testSubString = testWord.substring(j, k);

					int countTestSubstring = 0;
					final long timed1 = System.currentTimeMillis();
					for (int l = 0; l + testSubString.length() <= testWord
							.length(); l++)
					{
						final String compareSubString = testWord.substring(l,
								l + testSubString.length());
						if ((testSubString.equals(compareSubString)))
						{
							countTestSubstring++;
						}
					}
					final long timed2 = System.currentTimeMillis();
					// System.out.println(testSubString + " appears " +
					// countTestSubstring + " times");
					final int sufCount = st2.getNumOccurences(testSubString);
					final long timed3 = System.currentTimeMillis();
					timeBrute += timed2 - timed1;
					timeTree += timed3 - timed2;
					// System.out.println(testSubString + " is counted by suffix
					// tree " + sufCount + " times");
					if (sufCount != countTestSubstring)
					{
						System.out.println("error");
						final int sufCounttest = st2.getNumOccurences(testSubString);
						System.out.println("error recount" + sufCounttest + " should be: " + countTestSubstring);
					}
				}
			}
			System.out.println("treeOccurence took: " + timeTree);
			System.out.println("bruteOccurence took: " + timeBrute);

			final long dSuffBuildTime = time2 - time1;
			System.out.println("tree buliding took: " + dSuffBuildTime);

		}

		return false;
	}

	@SuppressWarnings("unused")
	private void testSuffixTreeAllKeywords()
	{	
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.boardFolder, null); 
		//final SuffixTreeExpanded st = DistanceUtils.generateKeyWordSuffixTree(StringCleaner.cleanAll,candidates,FolderLocations.outputTmpFolder, "suffixKeyWordsNaiveSplit");
		//st.exportGml(FolderLocations.outputTmpFolder,"keywordsNaiveSplit",false);
		final SuffixTreeCollapsed stc = DistanceUtils.generateKeyWordSuffixTreeCollapsed(candidates,FolderLocations.outputTmpFolder, "suffixKeyWordsNaiveSplit");
		stc.printSupport();
	}

	@SuppressWarnings("unused")
	private void testSuffixTreeDistance()
	{
		final DistanceMetric metric = new SuffixTreeDistance(Letteriser.lowRes,
				100, 40);
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,
						FolderLocations.boardFolder, null);
		final Random r = new Random(2l);
		while (candidates.size() > 91 && false)
		{
			candidates.remove(0 * candidates.size());
		}
		while (candidates.size() > 200 && true)
		{
			candidates.remove(candidates.size() - 1);
		}
		System.out.println(candidates);
		final DistanceMatrix<LudRul, LudRul> distanceMatrix;
		distanceMatrix = EvaluatorDistanceMetric.getDistanceMatrix(candidates,
				metric, true, null);
		final AssignmentSettings ass = new AssignmentSettings(3, 3);
		final Visualiser v = new Visualiser(metric.getName(),candidates, distanceMatrix, ass);
		v.setVisible(true);
		metric.releaseResources();
	}

	// -------------------------------------------------------------------------

	// @test
	public static void testSuffixTreeSeveralInsertions()
	{
		final ArrayList<String> testWords = new ArrayList<>();

		// words from http://jeff560.tripod.com/words4.html
		testWords.add("AAAAAAAAAAAAAAAAA");
		testWords.add("CACAO");
		testWords.add("TARAMASALATA");
		testWords.add("IONONNONIONI");
		testWords.add("FLOODDOORROOMMOONLIGHTERS");
		testWords.add("PEEKEENEENEE");
		testWords.add("ADCOMSUBORDCOMPHIBSPAC");
		testWords.add("BANNANNNANANNNANNANANNNNNA");
		final ArrayList<String> testWords2 = new ArrayList<>(testWords);
		final Random r = new Random(0);
		
		final int numberOfShuffeld = 0;
		for (int i = 0; i < numberOfShuffeld; i++)
		{
			System.out.println(i + " of " +numberOfShuffeld + "shuffeld");
			for (final String string : testWords2)
			{
				final List<String> letters = Arrays.asList(string.split(""));
				Collections.shuffle(letters,r);
				final StringBuilder shuffled = new StringBuilder();
				for (final String letter : letters)
				{
					shuffled.append(letter);
				}

				testWords.add(shuffled.toString());
			}
		}
		final int numberOfGenerated = 500;
		final int lengthOfGenerated = 500;
		for (int i = 0; i < numberOfGenerated; i++)
		{
			final StringBuilder sb = new StringBuilder(500);
			for (int j = 0; j < lengthOfGenerated; j++)
			{
				sb.append((char)(r.nextInt('Z' - 'A') + 'A'));
			}
			testWords.add(sb.toString());
		}

		final long time0 = System.currentTimeMillis();
		final SuffixTreeCollapsed tt = new SuffixTreeCollapsed(
				testWords.subList(0, testWords.size()));
		tt.exportGml(FolderLocations.outputfolder, "combi", false);
		final long time00 = System.currentTimeMillis();
		final long buildTime = time00 - time0;

		
		final HashSet<String> ngrams = new HashSet<String>();
		final List<String> checkwords = new ArrayList<>(testWords);
		Collections.shuffle(checkwords,new Random(1));
		for (int i = 0; i<testWords.size() && i < 10; i++)
		{

			final String testWord = testWords.get(i);
			for (int j = 0; j < testWord.length(); j++)
			{
				for (int k = j + 1; k <= testWord.length(); k++)
				{
					ngrams.add(testWord.substring(j, k));
				}
			}
			System.out.println(i + " of 50 testwords ngrams added");
		}
		// check support innfective
		long timeSumBrute = 0l;
		long timeSumSuf = 0l;
		int counter = 0;

		for (final String searchWord : ngrams)
		{
			final String updatedRegex = "(?=(" + searchWord + ")).";
			final Pattern p = Pattern.compile(updatedRegex);
			int slowCount = 0;
			for (final String string : testWords)
			{
				final long time1 = System.currentTimeMillis();
				final Matcher m = p.matcher(string);

				while (m.find())
				{
					slowCount += 1;
				}
				final long time2 = System.currentTimeMillis();
				timeSumBrute += time2 - time1;
			}
			final long time3 = System.currentTimeMillis();
			final int coolcount = tt.getNumOccurences(searchWord);
			final long time4 = System.currentTimeMillis();
			timeSumSuf += time4 - time3;

			if (coolcount != slowCount)
			{
				System.out.println("error with " + searchWord);
				System.out.println("should " + slowCount + " is" + coolcount);
				final int recount = tt.getNumOccurences(searchWord);
				System.out.println("recount " + recount);
			}
			System.out.println(++counter + " of " + ngrams.size()
					+ " ngram occurences checked");
		}
		System.out.println("occurence check suf" + timeSumSuf);
		System.out.println("occurence check brut" + timeSumBrute);
		System.out.println("Tree buildTime" + buildTime);
		tt.printAlphabet();
		tt.printFullWord();
	}

}
