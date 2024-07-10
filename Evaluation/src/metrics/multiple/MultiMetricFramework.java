package metrics.multiple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.math.LinearRegression;
import metrics.Evaluation;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

@SuppressWarnings("static-method")
public abstract class MultiMetricFramework extends Metric
{
	
	//-------------------------------------------------------------------------
	
	/** For incremental computation */
	protected ArrayList<Double[]> metricValueLists = new ArrayList<>();
	
	/** For incremental computation */
	protected ArrayList<Double> currValueList = new ArrayList<Double>();
	
	//-------------------------------------------------------------------------

	public enum MultiMetricValue 
	{
		Average,
		Median,
		Max,
		Min,
		Variance,
		
		ChangeAverage,
		ChangeSign,
		ChangeLineBestFit,
		ChangeNumTimes,
		
		MaxIncrease,
		MaxDecrease
	}
	
	//-------------------------------------------------------------------------
	
	public MultiMetricFramework
	(
		final String name, final String notes, final double min, final double max, 
		final Concept concept, final MultiMetricValue multiMetricValue
	) 
	{
		super(name, notes, min, max, concept, multiMetricValue);
	}
	
	//-------------------------------------------------------------------------
	
	public abstract Double[] getMetricValueList(final Evaluation evaluation, final Trial trial, final Context context); 
	
	//-------------------------------------------------------------------------
	
	public double[][] getMetricValueLists(final Game game, final Evaluation evaluation, final Trial[] trials, final RandomProviderState[] randomProviderStates)
	{
		final ArrayList<Double[]> metricValueLists = new ArrayList<>();
		
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			final Context context = Utils.setupNewContext(game, rngState);
			metricValueLists.add(getMetricValueList(evaluation, trial, context));
		}

		return metricValueLists.toArray(new double[0][0]);
	}
	
	//-------------------------------------------------------------------------
	
	public double metricAverage(final double[][] metricValues)
	{
		double metricAverageFinal = 0.0;
		for (final double[] valueList : metricValues)
		{
			double metricAverage = 0.0;
			if (valueList.length > 0)
				for (final double value : valueList)
					metricAverage += value / valueList.length;

			metricAverageFinal += metricAverage;
		}
		return metricAverageFinal / metricValues.length;
	}
	
	public double metricMedian(final double[][] metricValues)
	{
		double metricMedianFinal = 0.0;
		for (final double[] valueList : metricValues)
		{
			double metricMedian = 0.0;
			if (valueList.length > 1)
			{
				Arrays.sort(valueList);
				metricMedian = valueList[valueList.length/2];
			}
					
			metricMedianFinal += metricMedian;
		}
		return metricMedianFinal / metricValues.length;
	}
	
	public double metricMax(final double[][] metricValues)
	{
		double metricMaxFinal = 0.0;
		for (final double[] valueList : metricValues)
		{
			double metricMax = 0.0;
			for (final double value : valueList)
				metricMax = Math.max(metricMax, value);

			metricMaxFinal += metricMax;
		}
		return metricMaxFinal / metricValues.length;
	}
	
	public double metricMin(final double[][] metricValues)
	{
		double metricMinFinal = 0.0;
		for (final double[] valueList : metricValues)
		{
			double metricMin = 0.0;
			for (final double value : valueList)
				metricMin = Math.min(metricMin, value);

			metricMinFinal += metricMin;
		}
		return metricMinFinal / metricValues.length;
	}
	
	public double metricVariance(final double[][] metricValues)
	{
		double metricVarianceFinal = 0.0;
		for (final double[] valueList : metricValues)
		{
			double metricVariance = 0.0;
			if (valueList.length > 1)
			{
				double metricAverage = 0.0;
				for (final double value : valueList)
					metricAverage += value / valueList.length;
				
				for (final double value : valueList)
					metricVariance += Math.pow(value - metricAverage, 2) / valueList.length;
			}

			metricVarianceFinal += metricVariance;
		}
		return metricVarianceFinal / metricValues.length;
	}
	
	public double metricMaxIncrease(final double[][] metricValues)
	{
		double metricMaxFinal = 0.0;
		for (final double[] valueList : metricValues)
		{
			double metricMax = 0.0;
			if (valueList.length > 1)
			{
				double lastValue = valueList[0];
				for (final double value : valueList)
				{
					final double change = value - lastValue;
					metricMax = Math.max(metricMax, change);
					lastValue = value;
				}
			}
			
			metricMaxFinal += metricMax;
		}
		return metricMaxFinal / metricValues.length;
	}
	
	public double metricMaxDecrease(final double[][] metricValues)
	{
		double metricMaxFinal = 0.0;
		for (final double[] valueList : metricValues)
		{
			double metricMax = 0.0;
			if (valueList.length > 1)
			{
				double lastValue = valueList[0];
				for (final double value : valueList)
				{
					final double change = value - lastValue;
					metricMax = Math.min(metricMax, change);
					lastValue = value;
				}
			}
			
			metricMaxFinal += metricMax;
		}
		return metricMaxFinal / metricValues.length;
	}
	
	/** The slope of the least squares line of best fit. */
	public double metricChangeLineBestFit(final double[][] metricValues)
	{
		double metricChangeFinal = 0.0;
		for (final double[] valueList : metricValues)
		{
			double linearRegressionSlope = 0.0;
			if (valueList.length > 1)
			{
				final double[] xAxis = IntStream.range(0, valueList.length).asDoubleStream().toArray();
				final LinearRegression linearRegression = new LinearRegression(xAxis, valueList);
				linearRegressionSlope = linearRegression.slope();
			}

			metricChangeFinal += linearRegressionSlope;
		}
		return metricChangeFinal / metricValues.length;
	}
	
	/** The average increase */
	public double metricChangeAverage(final double[][] metricValues)
	{
		double metricChangeFinal = 0.0;
		for (final double[] valueList : metricValues)
		{
//			double metricChange = 0.0;
//			double lastValue = valueList[0].doubleValue();
//			for (final Double value : valueList)
//			{
//				final double change = value.doubleValue() - lastValue;
//				metricChange += change / (valueList.length-1);
//				lastValue = value.doubleValue();
//			}
			
			double metricChange = 0.0;
			if (valueList.length > 1)
			{
				final double firstValue = valueList[0];
				final double lastValue = valueList[valueList.length-1];
				metricChange = (lastValue - firstValue) / (valueList.length-1);
			}
			
			metricChangeFinal += metricChange;
		}
		return metricChangeFinal / metricValues.length;
	}
	
	/** The average number of times the value increased versus decreased. */
	public double metricChangeSign(final double[][] metricValues)
	{
		double metricChangeFinal = 0.0;
		for (final double[] valueList : metricValues)
		{
			double metricChange = 0.0;
			if (valueList.length > 1)
			{
				double lastValue = valueList[0];
				for (final double value : valueList)
				{
					double change = value - lastValue;
					if (change > 0)
						change = 1;
					else if (change < 0)
						change = -1;
					else
						change = 0;
					metricChange += change / (valueList.length-1);
					lastValue = value;
				}
			}
			
			metricChangeFinal += metricChange;
		}
		return metricChangeFinal / metricValues.length;
	}
	
	/** The average number of times the direction changed. */
	public double metricChangeNumTimes(final double[][] metricValues)
	{
		double metricChangeFinal = 0.0;
		for (final double[] valueList : metricValues)
		{
			double metricChange = 0.0;
			if (valueList.length > 1)
			{
				double valueChangeDirection = 0.0;	// If 1.0 then increasing, if -1.0 then decreasing
				double lastValue = valueList[0];
				
				for (final double value : valueList)
				{
					double direction = 0.0;
					if (value > lastValue)
						direction = 1.0;
					if (value < lastValue)
						direction = -1.0;
					if (direction != 0.0 && valueChangeDirection != direction)
						metricChange += 1 / (valueList.length-1);
					valueChangeDirection = direction;
					
					lastValue = value;
				}
			}
			
			metricChangeFinal += metricChange;
		}
		return metricChangeFinal / metricValues.length;
	}
	
	//-------------------------------------------------------------------------
	
	private Double computeMultiMetric(final double[][] metricValues)
	{
		switch (multiMetricValue())
		{
			case Average: return Double.valueOf(metricAverage(metricValues));
			case Median: return Double.valueOf(metricMedian(metricValues));
			case Max: return Double.valueOf(metricMax(metricValues));
			case Min: return Double.valueOf(metricMin(metricValues));
			case Variance: return Double.valueOf(metricVariance(metricValues));
			
			case ChangeAverage: return Double.valueOf(metricChangeAverage(metricValues));
			case ChangeSign: return Double.valueOf(metricChangeSign(metricValues));
			case ChangeLineBestFit: return Double.valueOf(metricChangeLineBestFit(metricValues));
			case ChangeNumTimes: return Double.valueOf(metricChangeNumTimes(metricValues));
			
			case MaxIncrease: return Double.valueOf(metricMaxIncrease(metricValues));
			case MaxDecrease: return Double.valueOf(metricMaxDecrease(metricValues));
			
			default: return null;
		}
	}
	
	@Override
	public Double apply
	(
		final Game game,
		final Evaluation evaluation,
		final Trial[] trials,
		final RandomProviderState[] randomProviderStates
	)
	{
		// Zero player games cannot be computed.
		if (game.hasSubgames() || game.isSimultaneousMoveGame() || game.players().count() == 0)
			return null;
		
		final double[][] metricValues = getMetricValueLists(game, evaluation, trials, randomProviderStates);
		return computeMultiMetric(metricValues);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void observeFinalState(final Context context)
	{
		// We've finished building one list of values
		metricValueLists.add(currValueList.toArray(new Double[0]));
		currValueList = new ArrayList<Double>();
	}
	
	@Override
	public double finaliseMetric(final Game game, final int numTrials)
	{
		final Double[][] metricValues = metricValueLists.toArray(new Double[0][0]);
		final double[][] primitives = new double[metricValues.length][];
		for (int i = 0; i < metricValues.length; ++i)
		{
			primitives[i] = new double[metricValues[i].length];
			for (int j = 0; j < primitives[i].length; ++j)
			{
				primitives[i][j] = metricValues[i][j] == null ? Double.NaN : metricValues[i][j].doubleValue();
			}
		}
		return computeMultiMetric(primitives).doubleValue();
	}
	
	//-------------------------------------------------------------------------
	
}
