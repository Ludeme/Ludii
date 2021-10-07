package metrics.moveBased;

import java.util.List;

import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;

public class CombinedConcepts implements DistanceMetric
{

	private final DistanceMetric moveConceptNGramEstimator;
	private final DistanceMetric gameConceptEstimator;

	private final double weightMoveConcepts;
	private final double weightGameConcepts;

	public CombinedConcepts(int n, double weightMoveConcepts, double weightGameConcepts)
	{
		moveConceptNGramEstimator = new MoveConceptNGramCosineSimilarity(n);
		gameConceptEstimator = new GameConceptsOverlap();

		this.weightMoveConcepts = weightMoveConcepts;
		this.weightGameConcepts = weightGameConcepts;
	}

	@Override
	public Score distance(Game gameA, Game gameB)
	{
		final double moveConceptScore = moveConceptNGramEstimator.distance(gameA, gameB).score();
		final double gameConceptScore = gameConceptEstimator.distance(gameA, gameB).score();

		return new Score(weightMoveConcepts * moveConceptScore + weightGameConcepts * gameConceptScore);
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
