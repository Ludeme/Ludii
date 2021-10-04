package sandbox;

import java.util.ArrayList;
import java.util.List;

import common.DistanceUtils;
import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;
import metrics.individual.JensenShannonDivergence;
import metrics.support.EditCost;
import metrics.support.TrialHelper;
import other.action.Action;
import other.trial.Trial;

public class MoveTypeTrialsAssignmentCost implements DistanceMetric
{
	@Override
	public String getName()
	{
		return "MoveTypeTrialsAssignmentCost";
	}

	private final EditCost editCost;

	public MoveTypeTrialsAssignmentCost(final EditCost editCost)
	{
		this.editCost = editCost;
	}


	private Score distance(
			final Game gameA, final Game gameB, final Trial[] trialsA,
			final Trial[] trialsB
	)
	{
		/*
		 * First find the cost of aligning each trial of game A to each trial of
		 * game B Then find the minimum pairing cost using the hungarian method
		 * Find the maximal possible alignment cost to skale down between 0 and
		 * 1 Return the ratio between minimum pairing cost and maximal possible
		 * alignment cost
		 * 
		 */
		final int[][] assigmentCostAB = getAlignmentCostSmithWaterman(trialsA,
				trialsB, editCost);

		final int[] rowMinima = new int[assigmentCostAB.length];
		final int[] columnMinima = new int[assigmentCostAB[0].length];

		createColumnMinima(columnMinima, assigmentCostAB);
		substractColumnMinimaFromColumnElements(columnMinima, assigmentCostAB);

		createColumnMinima(rowMinima, assigmentCostAB);
		substractRowMinimaFromRowElements(rowMinima, assigmentCostAB);

		@SuppressWarnings("unused")
		final JensenShannonDivergence jsd = new JensenShannonDivergence();
		// final Map<String, Integer> frequencyA = getFrequencies(trialsA);
		// final Map<String, Integer> frequencyB = getFrequencies(trialsB);
		// final TreeMap<String, Double> distributionA =
		// jsd.frequencyToDistribution(frequencyA);
		// final TreeMap<String, Double> distributionB =
		// jsd.frequencyToDistribution(frequencyB);

		// final double dist = jsd.jensenShannonDivergence(distributionA,
		// distributionB);

		return new Score(0);
	}

	private void substractRowMinimaFromRowElements(
			final int[] rowMinima, final int[][] assigmentCostAB
	)
	{
		// TODO Auto-generated method stub

	}

	private void substractColumnMinimaFromColumnElements(
			final int[] columnMinima, final int[][] assigmentCostAB
	)
	{
		// TODO Auto-generated method stub

	}

	private void createColumnMinima(
			final int[] columnMinima, final int[][] assigmentCostAB
	)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Score distance(final Game gameA, final Game gameB)
	{
		final int numPlayouts = 30;
		final int numMaxMoves = 40;
		final Trial[] trialsA = DistanceUtils.generateRandomTrialsFromGame(gameA,
				numPlayouts, numMaxMoves);
		final Trial[] trialsB = DistanceUtils.generateRandomTrialsFromGame(gameB,
				numPlayouts, numMaxMoves);

		return distance(gameA, gameB, trialsA, trialsB);
	}

	/**
	 * Determines the cost to align the actions of two respective trials using
	 * the Smith Waterman Algorithm and returns a distance matrix
	 * {@link "https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm"}
	 * 
	 * @param trialsA
	 * @param trialsB
	 * @param eCost
	 * @return
	 */
	private static int[][] getAlignmentCostSmithWaterman
	(
		final Trial[] trialsA, final Trial[] trialsB, final EditCost eCost
	)
	{
		final int[][] differences = new int[trialsA.length][trialsB.length];
		int maximum = 0;
		for (int i = 0; i < trialsA.length; i++)
		{
			for (int j = 0; j < trialsB.length; j++)
			{
				final int val = smithWatermanAlignment(trialsA[i], trialsB[j],
						eCost);
				differences[i][j] = val;

				if (val > maximum)
					maximum = val;

			}

		}
		final boolean print = false;
		for (int i = 0; i < trialsA.length; i++)
		{
			for (int j = 0; j < trialsB.length; j++)
			{
				differences[i][j] = -differences[i][j] + maximum;
				if (print)
					System.out.print(differences[i][j] + ",");
			}
			if (print)
				System.out.println();
		}
		return differences;
	}

	/**
	 * Determines the cost to align the actions of two trials using the Smith
	 * Waterman Algorithm
	 * {@link "https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm"}
	 * 
	 * @param trialA
	 * @param trialB
	 * @param eCost
	 * @return
	 */
	private static int smithWatermanAlignment
	(
		final Trial trialA, final Trial trialB, final EditCost eCost
	)
	{

		final ArrayList<Action> actionsA = TrialHelper.listAllActions(trialA);
		final ArrayList<Action> actionsB = TrialHelper.listAllActions(trialB);
		

		int maximumValue = 0;
		final int[][] distances = new int[actionsA.size() + 1][actionsB.size()
				+ 1];
		for (int i = 1; i < distances.length; i++)
		{
			for (int j = 1; j < distances[0].length; j++)
			{
				final int valueFromLeft = eCost.gapPenalty()
						+ distances[i - 1][j];
				final int valueFromTop = eCost.gapPenalty()
						+ distances[i][j - 1];

				int valueFromTopLeft;
				if (TrialHelper.isEqualType(actionsA.get(i - 1), actionsB.get(j - 1)))
					valueFromTopLeft = eCost.hit() + distances[i - 1][j - 1];
				else
					valueFromTopLeft = eCost.miss()
							+ distances[i - 1][j - 1];

				final int finalVal = Math.max(0, Math.max(valueFromTopLeft,
						Math.max(valueFromTop, valueFromLeft)));
				distances[i][j] = finalVal;

				if (finalVal > maximumValue)
					maximumValue = finalVal;
			}
		}
		return maximumValue;

	}

	@Override
	public Score
			distance(final LudRul candidate, final LudRul gameToCompareWith)
	{
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

}
