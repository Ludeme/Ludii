package metrics.moveBased;

import java.util.BitSet;
import java.util.List;

import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;

public class GameConceptsOverlap implements DistanceMetric
{

	@Override
	public Score distance(Game gameA, Game gameB)
	{
		final BitSet booleanConceptsA = gameA.booleanConcepts();
		final BitSet booleanConceptsB = gameB.booleanConcepts();

		// should be computed when simulating the trials in SimulatorApp.java using the
		// Context Object
//		Map<Integer, String> nonBooleanConceptsA = gameA.nonBooleanConcepts();
//		Map<Integer, String> nonBooleanConceptsB = gameB.nonBooleanConcepts();

		return overlap(booleanConceptsA, booleanConceptsB);
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
	 * @param booleanConceptsA all boolean game concepts of GameA
	 * @param booleanConceptsB all boolean game concepts of GameB
	 * @return a distance Score between both Games A and B: (1 - degreeOfOverlap)
	 */
	private Score overlap(BitSet booleanConceptsA, BitSet booleanConceptsB)
	{
		final BitSet intersection = (BitSet) booleanConceptsA.clone();
		intersection.and(booleanConceptsB);

		final BitSet union = (BitSet) booleanConceptsA.clone();
		union.or(booleanConceptsB);

		return new Score(1.0 - ((double) intersection.cardinality() / union.cardinality()));
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
