package metrics.multiple;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import metrics.Metric;
import metrics.Utils;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

public abstract class MultiMetricFramework extends Metric
{

	public enum MultiMetricValue 
	{
		Average,
		Max,
		Min,
		Variance,
		Change
	}
	
	//-------------------------------------------------------------------------
	
	public MultiMetricFramework
	(
		final String name, final String notes, final String credit, final MetricType type, 
		final double min, final double max, final double defaultValue, final Concept concept, final MultiMetricValue multiMetricValue
	) 
	{
		super(name, notes, credit, type, min, max, defaultValue, concept, multiMetricValue);
	}
	
	//-------------------------------------------------------------------------
	
	public abstract Double[] getMetricValueList(final Trial trial, final Context context); 
	
	//-------------------------------------------------------------------------
	
	public Double[][] getMetricValueLists(final Game game, final Trial[] trials, final RandomProviderState[] randomProviderStates)
	{
		final ArrayList<Double[]> metricValueLists = new ArrayList<>();
		
		for (int trialIndex = 0; trialIndex < trials.length; trialIndex++)
		{
			final Trial trial = trials[trialIndex];
			final RandomProviderState rngState = randomProviderStates[trialIndex];
			final Context context = Utils.setupNewContext(game, rngState);
			metricValueLists.add(getMetricValueList(trial, context));
		}

		return metricValueLists.toArray(new Double[0][0]);
	}
	
	//-------------------------------------------------------------------------
	
	public double metricAverage(final Double[][] metricValues)
	{
		double metricAverageFinal = 0.0;
		for (final Double[] valueList : metricValues)
		{
			double metricAverage = 0.0;
			for (final Double value : valueList)
				metricAverage += value.doubleValue() / valueList.length;

			metricAverageFinal += metricAverage;
		}
		return metricAverageFinal / metricValues.length;
	}
	
	public double metricMedian(final Double[][] metricValues)
	{
		double metricMedianFinal = 0.0;
		for (final Double[] valueList : metricValues)
		{
			Arrays.sort(valueList);
			double metricMedian = valueList[valueList.length/2];
			metricMedianFinal += metricMedian;
		}
		return metricMedianFinal / metricValues.length;
	}
	
	public double metricMax(final Double[][] metricValues)
	{
		double metricMaxFinal = 0.0;
		for (final Double[] valueList : metricValues)
		{
			double metricMax = 0.0;
			for (final Double value : valueList)
				metricMax = Math.max(metricMax, value.doubleValue());

			metricMaxFinal += metricMax;
		}
		return metricMaxFinal / metricValues.length;
	}
	
	public double metricMin(final Double[][] metricValues)
	{
		double metricMinFinal = 0.0;
		for (final Double[] valueList : metricValues)
		{
			double metricMin = 0.0;
			for (final Double value : valueList)
				metricMin = Math.min(metricMin, value.doubleValue());

			metricMinFinal += metricMin;
		}
		return metricMinFinal / metricValues.length;
	}
	
	public double metricVariance(final Double[][] metricValues)
	{
		double metricVarianceFinal = 0.0;
		for (final Double[] valueList : metricValues)
		{
			double metricAverage = 0.0;
			for (final Double value : valueList)
				metricAverage += value.doubleValue() / valueList.length;
			
			double metricVariance = 0.0;
			for (final Double value : valueList)
				metricVariance += Math.pow(value.doubleValue() - metricAverage, 2) / valueList.length;

			metricVarianceFinal += metricVariance;
		}
		return metricVarianceFinal / metricValues.length;
	}
	
	public double metricChange(final Double[][] metricValues)
	{
		double metricChangeFinal = 0.0;
		for (final Double[] valueList : metricValues)
		{
			double metricChange = 0.0;
			final double lastValue = valueList[0].doubleValue();
			for (final Double value : valueList)
				metricChange = (value.doubleValue() - lastValue) / (valueList.length-1);
			
			metricChangeFinal += metricChange;
		}
		return metricChangeFinal / metricValues.length;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double apply
	(
			final Game game,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		Double[][] metricValues = getMetricValueLists(game, trials, randomProviderStates);
		
		switch (multiMetricValue())
		{
			case Average: return metricAverage(metricValues);
			case Change: return metricChange(metricValues);
			case Max: return metricMax(metricValues);
			case Min: return metricMin(metricValues);
			case Variance: return metricVariance(metricValues);
			default: return -1;
		}
	}
	
}
