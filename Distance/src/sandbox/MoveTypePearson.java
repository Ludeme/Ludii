package sandbox;

public class MoveTypePearson
{

	/**
	 * This method measures the Pearson correlation coefficient of a list of
	 * games where each game is a list of moves.
	 * 
	 * @param gamesToMeasure
	 * @param spearman specifies if Pearson correlation coefficient should be
	 * calculated on ranked series
	 * @return
	 */
	/*
	private double[][] measurePearsonCorrelationDistances(final ArrayList<ArrayList<Double>> gamesToMeasure, final boolean spearman)
	{
		// construct quadratic correlation matrix
		final double[][] correlationMatrix = new double[gamesToMeasure.size()][gamesToMeasure.size()];

		// compute actual correlation matrix according to Pearson
		for (int i = 0; i < correlationMatrix.length; i++)
		{
			for (int j = i; j < correlationMatrix.length; j++)
			{
				if (i == j)
				{
					correlationMatrix[i][j] = 1;
					continue;
				}
				// if games are of different length, shortest game has n moves
				// take last n moves of longer game
				final int minLength = Math.min(gamesToMeasure.get(i).size(), gamesToMeasure.get(j).size());

				ArrayList<Double> gameI = gamesToMeasure.get(i);
				ArrayList<Double> gameJ = gamesToMeasure.get(j);

				if (spearman)
				{
					gameI = getRanks(gameI, minLength);
					gameJ = getRanks(gameJ, minLength);
				}

				final double meanI = getMean(gameI, minLength);
				final double meanJ = getMean(gameJ, minLength);

				final int offsetI = gameI.size() - minLength;
				final int offsetJ = gameJ.size() - minLength;

				double sumNumerator = 0;
				double sumDenumeratorX = 0;
				double sumDenumeratorY = 0;

				for (int k = 0; k < minLength; k++)
				{
					sumNumerator += (gameI.get(offsetI + k) - meanI) * (gameJ.get(offsetJ + k) - meanJ);
					sumDenumeratorX += (gameI.get(offsetI + k) - meanI) * (gameI.get(offsetI + k) - meanI);
					sumDenumeratorY += (gameJ.get(offsetJ + k) - meanJ) * (gameJ.get(offsetJ + k) - meanJ);
				}
				final double corr = sumNumerator / (Math.sqrt(sumDenumeratorX) * Math.sqrt(sumDenumeratorY));
				final double correctRange = (corr + 1) / 2;
				correlationMatrix[i][j] = 1 - correctRange;
				// fill both triangle matrices
			}
		}
		return correlationMatrix;
	}

	private double getMean(final ArrayList<Double> game, final int numberOfValues)
	{
		double mean = 0;
		for (int i = game.size() - numberOfValues; i < game.size(); i++)
		{
			mean += game.get(i);
		}
		return mean / numberOfValues;
	}

	private double getExpectedValue(final ArrayList<Double> scores)
	{
		double expected = 0;
		final double prob = 1 / scores.size();
		for (final Double d : scores)
		{
			expected += d * prob;
		}
		return expected;
	}

	private double getMedian(final ArrayList<Double> scores)
	{
		Collections.sort(scores);
		return scores.get(scores.size() / 2);
	}

	private ArrayList<Double> getRanks(final ArrayList<Double> game, final int numberOfValues)
	{
		final ArrayList<Double> result = new ArrayList<>();

		for (int i = game.size() - numberOfValues; i < game.size(); i++)
		{
			int count = 0;
			for (int j = game.size() - numberOfValues; j < game.size(); j++)
			{
				if (game.get(j) < game.get(i))
				{
					count++;
				}
			}
			result.add(Double.valueOf(count + 1));
		}
		return result;
	}


	

	
	public ArrayList<Double> averagePlayouts(final ArrayList<ArrayList<Double>> playoutsWinners)
	{
		final ArrayList<Double> averages = new ArrayList<>();

		int avgLength = 0;
		for (final ArrayList<Double> playout : playoutsWinners)
		{
			avgLength += playout.size();
		}
		avgLength /= playoutsWinners.size();

		// calculate expected value for each index
		for (int i = 0; i < avgLength; i++)
		{
			final ArrayList<Double> valuesForAverage = new ArrayList<>();
			for (final ArrayList<Double> playout : playoutsWinners)
			{
				if (i < playout.size())
				{
					valuesForAverage.add(playout.get(i));
				}
			}

			averages.add(this.getMean(valuesForAverage, valuesForAverage.size()));
		}
		return averages;
	}

	public double[][] getCorrelationMatrix(final boolean spearman, final ArrayList<ArrayList<Double>> games)
	{
		return measurePearsonCorrelationDistances(games, spearman);
	}

	public double[][] getBootstrappedCorrelationMatrix(final boolean spearman, final int numberOfSampledMatrices)
	{
		final File serializedGamesFolder = new File("../Player/src/player/experiments/playoutSerialized/");
		final TimeSeriesDistance measure = new TimeSeriesDistance();
		final ArrayList<String> orderOfGames = new ArrayList<>();
		final ArrayList<ArrayList<Double>> ts = new ArrayList<>();
		final ArrayList<double[][]> allMatrices = new ArrayList<>();

		final String folder = "../Common/res/lud/board/capture/mancala";
		final List<File> entries = CollectScoresOfGames.collectGamesForEvaluation(folder);
		final boolean ignore2019 = true;
		final boolean bootstrap = true;

		for (int i = 0; i < numberOfSampledMatrices; i++)
		{
			final TreeMap<String, ArrayList<Double>> games = measure.collectGamesForDistanceMeasuringFromSerialized(entries,
					serializedGamesFolder, ignore2019, bootstrap);
			for (final Map.Entry<String, ArrayList<Double>> entry : games.entrySet())
			{
				orderOfGames.add(entry.getKey());
				ts.add(entry.getValue());
			}
			final double[][] corrMatrix = measure.getCorrelationMatrix(spearman, ts);
			allMatrices.add(corrMatrix);
		}

		final double[][] averagedMatrix = new double[allMatrices.get(0).length][allMatrices.get(0).length];
		for (int i = 0; i < allMatrices.get(0).length; i++)
		{
			for (int j = 0; j < allMatrices.get(0).length; j++)
			{
				final ArrayList<Double> median = new ArrayList<>();
				for (final double[][] mat : allMatrices)
				{
					median.add(mat[i][j]);
				}
				averagedMatrix[i][j] = getMean(median, median.size());
			}
		}
		return averagedMatrix;
	}

	public ArrayList<Double> getWinningScores(final ArrayList<ArrayList<Double>> playerScores)
	{
		ArrayList<Double> winner = new ArrayList<>();
		for (int i = 0; i < playerScores.get(0).size(); i++)
		{
			if (playerScores.get(0).get(i) >= 0.99)
			{
				winner = playerScores.get(0);
				break;
			}
			else if (playerScores.get(1).get(i) >= 0.99)
			{
				winner = playerScores.get(1);
				break;
			}
		}
		return winner;
	}

	public void printDistanceMatrixToFile(final double[][] matrix, final File folder, final ArrayList<String> gameNames, final boolean full,
			final boolean spearman)
	{
		final DecimalFormat df = new DecimalFormat("#.####");

		final StringBuilder sb = new StringBuilder();
		sb.append("Games;");
		final int n = matrix.length;
		for (final String name : gameNames) // first line with game names
		{
			sb.append(name + ";");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");
		for (int i = 0; i < n; i++)
		{
			sb.append(gameNames.get(i) + ";");
			int k = i;
			if (full) // get full matrix, not only upper
			{
				k = 0;
			}
			for (int j = k; j < n; j++)
			{
				if (i == j)
				{
					sb.append(0 + ";");
					continue;
				}
				double distance = matrix[i][j];
				if (i > j)
				{
					distance = matrix[j][i];
				}
				sb.append(df.format(distance) + ";");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\n");
		}

		final String finalText = sb.toString().replace(".", ",");
		String corrType = "Pearson";
		if (spearman)
		{
			corrType = "Spearman";
		}
		String matrixType = "Upper";
		if (full)
		{
			matrixType = "Full";
		}
		final String fileName = "correlationMatrix" + corrType + matrixType + ".csv";
		final String path = folder + File.separator + fileName;
		try (PrintWriter out = new PrintWriter(path))
		{
			out.println(finalText);
		}
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	public void removeTrendFromSeries(final ArrayList<Double> series)
	{
		int i = 0;
		// only consider "winning" series which goes from 0.5 to 1 with an
		// overall trend of +0.5 (very simple)
		final double trendStep = 0.5 / series.size();
		for (final double step : series)
		{
			series.set(i, step - i * trendStep);
			i++;
		}
	}

	public static void main(final String[] args)
	{

		final File serializedGamesFolder = new File("../Player/src/player/experiments/playoutSerialized/");
		final TimeSeriesDistance measure = new TimeSeriesDistance();
		final ArrayList<String> families = new ArrayList<>();
		families.add("mancala");

		final ArrayList<String> orderOfGames = new ArrayList<>();
		final ArrayList<ArrayList<Double>> ts = new ArrayList<>();

		final boolean serialized = true;
		final boolean ignore2019 = true;
		final boolean bootstrap = true;
		if (!serialized)
		{
			final TreeMap<String, ArrayList<ArrayList<Double>>> games = measure
					.collectGamesForDistanceMeasuringOneByOne(families);

			for (final Map.Entry<String, ArrayList<ArrayList<Double>>> entry : games.entrySet())
			{
				orderOfGames.add(entry.getKey());
				final ArrayList<Double> winner = measure.getWinningScores(entry.getValue());
				measure.removeTrendFromSeries(winner);
				ts.add(winner);
			}
		}
		else
		{
			final String folder = "../Common/res/lud/board/capture/mancala";
			final List<File> entries = CollectScoresOfGames.collectGamesForEvaluation(folder);
			final TreeMap<String, ArrayList<Double>> games = measure.collectGamesForDistanceMeasuringFromSerialized(entries,
					serializedGamesFolder, ignore2019, bootstrap);
			for (final Map.Entry<String, ArrayList<Double>> entry : games.entrySet())
			{
				orderOfGames.add(entry.getKey());
				ts.add(entry.getValue());
			}
		}

		final boolean spearman = false;
		final boolean full = false;
		double[][] corrMatrix = null;
		if (bootstrap)
		{
			final int numberOfSampledMatrices = 60;
			corrMatrix = measure.getBootstrappedCorrelationMatrix(spearman, numberOfSampledMatrices);
		}
		else
		{
			corrMatrix = measure.getCorrelationMatrix(spearman, ts);
		}

		final File destFolder = new File("../Player/src/player/experiments/correlationMeasurements/");
		measure.printDistanceMatrixToFile(corrMatrix, destFolder, orderOfGames, full, spearman);

		
	}*/
}
