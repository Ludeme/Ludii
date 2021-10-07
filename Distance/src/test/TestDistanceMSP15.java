package test;

import java.io.File;
import java.util.ArrayList;

import common.DistanceMatrix;
import common.Score;
import game.Game;
import metrics.DistanceMetric;
import metrics.moveBased.CombinedConcepts;
import other.GameLoader;

/**
 * Test file for move based distance metrics
 */
public class TestDistanceMSP15
{
	// ---------------------------------------------------------
	// --- VARIABLES FOR TESTING MOVE BASED GAME DISTANCES ---
	// ---------------------------------------------------------
	// Determine the number of trials in a simulation.
	public static final int NUM_TRIALS = 100;
	//
	// Write down the names of the games you want to compare.
	public static final String[] GAMES =
	{
		"Tic-Tac-Toe",
		"Breakthrough", 
		"Yavalath", 
		"Hex", 
		"Mu Torere", 
		"Round Merels", 
		"Chess", 
		"English Draughts",
		"Taikyoku Shogi"

//		// "Armenian Draughts",
//		"Brazilian Draughts",
//		"Cage",
//		"Canadian Draughts",
//		"Coc-Inbert",
//		"Dam",
//		"Dama (Italy)",
//		"Dama (Philippines)",
//		"Dama",
//		"Damas",
//		"Damenspiel",
//		"Damspel",
//		"Diagonal Draughts",
//		"Doov",
//		"English Draughts",
//		"Game of Solomon",
//		//"Guerrilla Checkers",
//		"HexDame",
//		"International Draughts",
//		"Jekab",
//		"Lasca",
//		"Main Dam",
//		"Makvoer",
//		"Maleys",
//		"Moo",
//		"Pleasant Draughts",
//		"Shashki",
//		"The Babylonian",
//		"Unnamed Dutch Draughts Game"
	};
	//
	// Location of the precomputed trials
	public static final String CWD = new File("").getAbsolutePath();
	//
	// --------------------------------------------------------


	public static void main(String[] args)
	{
		final ArrayList<Game> candidates = getBenchmarkGames();
		final ArrayList<Game> targets = new ArrayList<>(candidates);
		final DistanceMatrix<Game, Game> distMatrix = new DistanceMatrix<>(candidates, targets);

//		DistanceMetric metric = new MoveConceptOverlap();
//		DistanceMetric metric = new MoveConceptFrequencyCosineSimilarity();
//		DistanceMetric metric = new MoveConceptNGramCosineSimilarity(1); // 1-grams, equal to
																			// MoveConceptFrequencyCosineSimilarity
																			// except that moves without concepts are
																			// discarded
//		DistanceMetric metric = new MoveConceptNGramCosineSimilarity(2); // 2-grams
//		DistanceMetric metric = new MoveConceptNGramCosineSimilarity(3); // 3-grams
//		DistanceMetric metric = new MoveConceptNGramCosineSimilarity(5); // 5-grams
//		DistanceMetric metric = new GameConceptsOverlap();
		
		// move and game concepts combined (MoveConceptNGramCosineSimilarity and GameConceptsOverlap)
		final DistanceMetric metric = new CombinedConcepts(3, 0.5, 0.5);

		/**
		 * one problem with MoveConceptNGramCosineSimilarity: difficult to use on games
		 * such as "Round Merels", which have only 2 moves with concepts assigned to them,
		 * if one chooses n to be higher than #moves_with_concepts
		 */

		fillSymetricDistanceMatrix(metric, candidates, distMatrix);
		distMatrix.printDistanceMatrixToFile("Benchmark Games", new File(CWD + "/log/"),
				"combined_benchmark_games_100trials.csv", ", ");

	}

	public static ArrayList<Game> getBenchmarkGames()
	{
		final ArrayList<Game> games = new ArrayList<>();
		for (final String game : GAMES)
		{
			games.add(GameLoader.loadGameFromName(game + ".lud"));
		}

		return games;
	}

	/**
	 * Calculating the distances between all candidates and fill in the matrix.
	 * 
	 * @param dm
	 * @param candidates
	 * @param dma
	 * @author Markus
	 */
	public static void fillSymetricDistanceMatrix(final DistanceMetric dm, final ArrayList<Game> candidates,
			final DistanceMatrix<Game, Game> dma)
	{
		for (int i = 0; i < candidates.size(); i++)
		{
			System.out.println(i + "/" + candidates.size());
			for (int j = i; j < candidates.size(); j++)
			{
				System.out.print(j + "/" + candidates.size() + " ");
				final Game c = candidates.get(i);
				final Game tar = candidates.get(j);
				final Score distance = dm.distance(c, tar);
				dma.put(c, tar, distance.score());
				dma.put(tar, c, distance.score());
			}
			System.out.println();
		}
	}
}
