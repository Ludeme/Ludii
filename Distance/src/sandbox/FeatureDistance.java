package sandbox;

import metrics.DistanceMetric;

public abstract class FeatureDistance implements DistanceMetric
{
/*
	@Override
	public String getName()
	{
		return "FeatureDistance";
	}
	
	@Override
	public Score distance(final Game gameA, final Game gameB)
	{
		return distance(gameA.computeGameConcepts(),gameB.computeGameConcepts());
	}

	@Override
	public Score distance(final LudRul gameA, final LudRul gameB)
	{
		return distance(gameA.getGameConcepts(),gameB.getGameConcepts());
	}

	private Score distance(final BitSet gc1, final BitSet gc2)
	{
		
		final int numberConcepts = GameConcept.class.getDeclaredFields().length;
		final BitSet andBitset = new BitSet(gc1.size());
		andBitset.or(gc1);
		andBitset.and(gc2);
		final BitSet orBitset = new BitSet(gc1.size());
		orBitset.or(gc1);
		orBitset.or(gc2);
		
		final double div = 1.0-(double)andBitset.cardinality()/orBitset.cardinality() ;
		return new Score(div);
	}

	@Override
	public Score distance(
			final Game gameA, final List<Game> gameB, final int numberTrials, final int maxTurns,
			final double thinkTime, final String AIName
	)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Score distance(final String description1, final String description2)
	{
		// TODO Auto-generated method stub
		return null;
	}
*/
}
