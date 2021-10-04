package metrics.individual;

import java.util.BitSet;
import java.util.List;

import common.DistanceUtils;
import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;

public class FeatureDistance implements DistanceMetric
{

	@Override
	public String getName()
	{
		return "ConceptDistance";
	}
	
	@Override
	public Score distance(final Game gameA, final Game gameB)
	{
		return distance(gameA.computeBooleanConcepts(),gameB.computeBooleanConcepts());
	}

	@Override
	public Score distance(final LudRul gameA, final LudRul gameB)
	{
		return distance(gameA.getGameConcepts(),gameB.getGameConcepts());
	}

	private Score distance(final BitSet gc1, final BitSet gc2)
	{
		
		
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

	@Override
	public DistanceMetric getDefaultInstance()
	{
		
		return new FeatureDistance();
	}

}
