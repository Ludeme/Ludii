package metrics.moveBased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;

/**
 * Move based distance metric. Returns 1 - degree of overlap of all occurring
 * move concepts as distance estimation.
 */
public class MoveConceptOverlap implements DistanceMetric
{
	
	/**
	 * For testing, whether to print out details about the compared games
	 */
	private final boolean printDetails;

	/**
	 * stores all move concepts, s.t. they need to be loaded from the files only
	 * once
	 */
	private final HashMap<String, BitSet> moveConceptsByGame;

	public MoveConceptOverlap()
	{
		printDetails = false;
		moveConceptsByGame = new HashMap<>();
	}

	public MoveConceptOverlap(boolean printDetails)
	{
		this.printDetails = printDetails;
		moveConceptsByGame = new HashMap<>();
	}

	@Override
	public Score distance(Game gameA, Game gameB)
	{

		final BitSet moveConceptsA = getMoveConcepts(gameA);
		final BitSet moveConceptsB = getMoveConcepts(gameB);

		final Score score = overlap(moveConceptsA, moveConceptsB);

		if (printDetails)
		{
			System.out.println(gameA.name() + ": " + moveConceptsA);
			System.out.println(gameB.name() + ": " + moveConceptsB);
			System.out.println(score.score());
		}
		return score;
	}

	@Override
	public Score distance(LudRul gameA, LudRul gameB)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Score distance(Game gameA, List<Game> gameB, int numberTrials, int maxTurns, double thinkTime, String AIName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Computes 1 - the overlap of both BitSets in range 0...1.
	 * 
	 * @param moveConceptsA all move concepts occurring in GameA
	 * @param moveConceptsB all move concepts occurring in GameB
	 * @return a distance Score between both Games A and B: (1 - degreeOfOverlap)
	 */
	private Score overlap(BitSet moveConceptsA, BitSet moveConceptsB)
	{
		final BitSet intersection = (BitSet) moveConceptsA.clone();
		intersection.and(moveConceptsB);
		
		final BitSet union = (BitSet) moveConceptsA.clone();
		union.or(moveConceptsB);

		return new Score(1.0 - ((double) intersection.cardinality() / union.cardinality()));
	}

	/**
	 * Returns all move concepts occurring in all trials of the Game game. If those
	 * are not stored in the HashMap moveConceptsByGame yet, they will be loaded
	 * from the .csv files.
	 * 
	 * Moves without any concept assigned to them will not be considered.
	 * 
	 * @param game the game to load the move concepts for
	 * @return BitSet containing all MoveConcepts occuring in all trials of the game
	 */
	private BitSet getMoveConcepts(Game game)
	{
		if (moveConceptsByGame.containsKey(game.name()))
		{
			return moveConceptsByGame.get(game.name());
		}

		// if BitSet is not stored yet, load it from the .csv files
		final BitSet moveConcepts = new BitSet();
		try
		{
			final File folder = new File("log/" + game.name() + ".lud" + "/Random/");
			for (final File csv : folder.listFiles(f -> f.isFile() && f.getName().endsWith(".csv")))
			{
				final BufferedReader reader = new BufferedReader(new FileReader(csv));
				String line = "";
				while ((line = reader.readLine()) != null)
				{
					for (final String s : line.split(", "))
					{
						if (!s.isEmpty())
						{
							moveConcepts.set(Integer.parseInt(s));
						}
					}
				}

				reader.close();
			}

		} catch (final FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		moveConceptsByGame.put(game.name(), moveConcepts);
		return moveConcepts;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Score distance(String expandedDescription1, String expandedDescription2)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
