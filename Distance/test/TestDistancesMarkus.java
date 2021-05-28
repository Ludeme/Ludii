import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import common.DistanceMatrix;
import common.DistanceUtils;
import common.EvaluationDistanceMetric;
import common.LudRul;
import common.Score;
import game.Game;
import compiler.Compiler;
import main.FileHandling;
import main.grammar.Description;
import metrics.CosineSimilarity;
//import org.junit.Test;
import metrics.DistanceMetric;
import metrics.JensenShannonDivergence;
import metrics.LudemeNeedlemanWunshAlignment;
import metrics.LudemeSmithWatermanAlignment;
import metrics.MoveTypeJensonShannon;
import metrics.support.SimilarityMatrix;
import other.GameLoader;
import other.trial.Trial;

/**
 * Test file for distance metrics.
 * @author markus
 */
public class TestDistancesMarkus
{
	private static final int NUM_PLAYOUTS = 30;
	private static final int NUM_MAX_MOVES = 50;
	private final File outputfolder = new File("../Distance/out/");

	//-------------------------------------------------------------------------
	
	//@Test
	public void createSimilarityMatrix()
	{
		
		final ArrayList<LudRul> candidates = DistanceUtils.getAllLudiiGameFilesAndRulesetCombination(false);
		for (int i = 0; i < candidates.size(); i++)
		{
			
			final LudRul g1 = candidates.get(i);
			final String name = g1.getGameNameIncludingOption().replace("/", "_");
			SimilarityMatrix.similarityMatrix(g1, new File(outputfolder.getAbsolutePath()+"/allSimilarities/"+name +".bmp"));
			System.out.println(i + "/" + candidates.size());
		}
		final LudRul g1 = candidates.get((int) (Math.random()*candidates.size()));
		final LudRul g2 = candidates.get((int) (Math.random()*candidates.size()));
		SimilarityMatrix.similarityMatrix(g1, new File(outputfolder.getAbsolutePath()+"/"+ g1.getGameNameIncludingOption() +".bmp"));
		SimilarityMatrix.similarityMatrix(g2, new File(outputfolder.getAbsolutePath()+"/"+ g2.getGameNameIncludingOption()+".bmp"));
		SimilarityMatrix.similarityMatrix(g1, g2, new File(outputfolder.getAbsolutePath()+"/"+ g1.getGameNameIncludingOption()+g2.getGameNameIncludingOption()+".bmp"));
		final Trial[] trials = DistanceUtils.getRandomTrialsFromGame(g1.getGame(), 2, 40);
		SimilarityMatrix.similarityMatrix(trials[0],  new File(outputfolder.getAbsolutePath()+"/"+ g1.getGameNameIncludingOption()+"Trials.bmp"));
		//SimilarityMatrix.similarityMatrix(trials[0], trials[1], new File(outputfolder.getAbsolutePath()+"/"+ g1.getGameNameIncludingOption()+"Trials.bmp"));
	}
	
//	@Test
	public void exportLudiiWekaCSV()
	{
		DistanceUtils.exportAllBoardGamesForWeka(outputfolder);
	}

//	@Test
	public void testAllDistanceMetrics()
	{
		final ArrayList<DistanceMetric> metrices = new ArrayList<>();
		final HashMap<DistanceMetric, Double> scores = new HashMap<>();
		//metrices.add(new MoveTypeTrialsAssignmentCost(new EditCost()));
		// metrices.add(new JensenShannonDivergence());
		 metrices.add(new CosineSimilarity());
		 //metrices.add(new LudemeSmithWatermanAlignment());
		 //metrices.add(new LudemeNeedlemanWunshAlignment());
		// metrices.add(new MoveTypeJensonShannon());
		// metrices.add(new ZhangShasha());
		//metrices.add(new Levenshtein());

		for (final DistanceMetric distanceMetric : metrices)
		{
			System.out.println("Check " + distanceMetric.getClass().getName());
			scores.put(distanceMetric,
					Double.valueOf(
							EvaluationDistanceMetric.evaluateDistanceMeasureNearestNeighbours(distanceMetric, 3)));
			distanceMetric.releaseResources();
		}

		for (final DistanceMetric distanceMetric : metrices)
		{
			System.out.println(distanceMetric.getClass().getName() + ": " + scores.get(distanceMetric));
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
				System.out.println(i + "/" + games.size() + " " + game.name() + 
						"  " + j + "/" + games.size() + " " + game2.name());
				final Score score = jsd.distance(game, game2);
				dist[i][j] = score.score();
				dist[j][i] = score.score();
			}
		}

		DistanceMatrix.printSquareDistanceMatrixToFile("Games", names, dist, outputfolder,
				"DistancesJensonsShannon.csv", ",");
		DistanceUtils.generateSplitTreeInput(names, dist, outputfolder, "splitsJensonsShannon.txt");

	}

//	@Test
	public void testLudemeSmithWatermanAlignmentBetweenTwo() throws IOException
	{
		final Game game1 = GameLoader.loadGameFromName("Tic-Tac-Four.lud");
		final Game game2 = GameLoader.loadGameFromName("Tic-Tac-Toe.lud");

		final DistanceMetric dm = new LudemeNeedlemanWunshAlignment();
		System.out.println(dm.distance(game1, game2).score());

		final DistanceMetric dm2 = new LudemeSmithWatermanAlignment();
		System.out.println(dm2.distance(game1, game2).score());
	}

//	@Test
	public void testLudemeSmithWatermanAlignment() throws IOException
	{
		final ArrayList<File> games = getAllLudiiGameFiles(0, 800);
		final ArrayList<String> names = new ArrayList<>();
		final DistanceMetric jsd = new LudemeNeedlemanWunshAlignment();

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
				System.out.println(i + "/" + games.size() + " " + game.name() + "  " + j + "/" + games.size() + " "
						+ game2.name() + " :" + score.score());
			}
		}

		DistanceMatrix.printSquareDistanceMatrixToFile("Games", names, dist, outputfolder,
				"DistancesJensonsShannon.csv", ",");
		DistanceUtils.generateSplitTreeInput(names, dist, outputfolder, "splitsJensonsShannon.txt");
	}

//	@Test
	public void testMoveTypeJensonsShannonDivergence() throws IOException
	{
		final ArrayList<File> games = getAllLudiiGameFiles(0, 800);
		final ArrayList<String> names = new ArrayList<>();
		final MoveTypeJensonShannon mtjsd = new MoveTypeJensonShannon();

		final double[][] dist = new double[games.size()][games.size()];
		for (int i = 0; i < games.size(); i++)
		{
			final Game game = getGameFromFile(games.get(i));
			final Trial[] trialA = DistanceUtils.getRandomTrialsFromGame(game, NUM_PLAYOUTS, NUM_MAX_MOVES);

			names.add(game.name());
			for (int j = i + 1; j < games.size(); j++)
			{

				final Game game2 = getGameFromFile(games.get(j));
				System.out.println(i + "/" + games.size() + " " + game.name() + "  " + j + "/" + games.size() + " "
						+ game2.name());
				final Trial[] trialB = DistanceUtils.getRandomTrialsFromGame(game2, NUM_PLAYOUTS, NUM_MAX_MOVES);
				final Score score = mtjsd.distance(trialA, trialB);
				dist[i][j] = score.score();
				dist[j][i] = score.score();
			}
		}

		DistanceMatrix.printSquareDistanceMatrixToFile("Games", names, dist, outputfolder,
				"distancesMoveTypeJensonsShannon.csv", ".");
		DistanceUtils.generateSplitTreeInput(names, dist, outputfolder, "splitsMoveTypeJensonsShannon.txt");

	}

//	private static ArrayList<Game> getAllLudiiGames(final int startIndex, final int endIndex)
//	{
//		final ArrayList<File> lfiles = getAllLudiiGameFiles(startIndex, endIndex);
//		final ArrayList<Game> games = new ArrayList<>(lfiles.size());
//
//		for (int i = startIndex; i < lfiles.size() && i < endIndex; i++)
//		{
//
//			final File file = lfiles.get(i);
//			final Game game = getGameFromFile(file);
//			System.out.println("Compiling game " + i + " " + Math.min(lfiles.size(), endIndex));
//		}
//		return games;
//	}

	private static Game getGameFromFile(final File file)
	{
		final String filepath = file.getPath();

		String descA;
		Game gameA = null;
		try
		{
			descA = FileHandling.loadTextContentsFromFile(filepath);
			gameA = (Game)Compiler.compileTest(new Description(descA), false);

		}
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return gameA;
	}

	private static ArrayList<File> getAllLudiiGameFiles(final int startIndex, final int endIndex)
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
					final String path = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");

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
				}
				else if (fileEntry.getName().contains(".lud"))
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
	
	//-------------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		final TestDistancesMarkus app = new TestDistancesMarkus();
		app.testAllDistanceMetrics();
		//app.createSimilarityMatrix();
	}
	
}
