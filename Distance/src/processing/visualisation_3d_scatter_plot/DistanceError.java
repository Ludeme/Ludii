package processing.visualisation_3d_scatter_plot;

import java.util.ArrayList;
import java.util.HashMap;

import common.LudRul;

public class DistanceError
{

	public double[] individualError;
	public double totalCost;
	private double maxError;
	HashMap<LudRul, Float> errorMap ;
	private double minError;
	private double errorRange;
	

	public DistanceError(double totalCost, ArrayList<LudRul> nodes, double[] error)
	{
		errorMap = new HashMap<>(nodes.size());
		
		this.totalCost = totalCost;
		this.individualError = error;
		this.minError = Integer.MAX_VALUE;
		this.maxError = 0;
		for (double d : error)
		{
			if (d>maxError)maxError = d;
			if (d<minError)minError = d;
		}
		errorRange = maxError-minError;
		for (int i = 0; i < nodes.size(); i++)
		{
			LudRul ludRul = nodes.get(i);
			errorMap.put(ludRul, Float.valueOf((float)error[i]));
		}
	}

	public float getBrightness(LudRul lr)
	{
		double val2 = (errorMap.get(lr).doubleValue()-minError)/errorRange;
		
		double val = 1.f - (0.80f*(val2));
		return (float) (val);
	}

	public static float getBrightness(
			HashMap<LudRul, Double> individualError2, LudRul ns,Double min, Double max
	)
	{
		double val2 = (individualError2.get(ns).doubleValue()-min.doubleValue())/(max.doubleValue()-min.doubleValue());
		
		double val = 1.f - (0.80f*(val2));
		return (float) (val);
	}

}
