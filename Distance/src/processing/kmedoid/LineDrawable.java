package processing.kmedoid;

import processing.visualisation_3d_scatter_plot.DistanceError;

public interface LineDrawable
{
	public int getX();
	public double getY();

	public static LineDrawable getLineDrawable(int i,DistanceError currentError)
	{
		return getNewLineDrawable(i,Math.round(currentError.totalCost));
		
	}

	public static LineDrawable getNewLineDrawable(int i, double totalCost)
	{
		return new LineDrawable()
		{
			@Override
			public double getY()
			{
				return totalCost;
			}
			
			@Override
			public int getX()
			{
				return i;
			}
		};
	}}
