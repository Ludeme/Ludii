package metrics.suffix_tree;

public abstract class DistanceFunction
{

	protected static final DistanceFunction Manhattan = getManhattan();
	protected static final DistanceFunction JACCARD = getJaccard();

	public abstract double[] distance(double weight, int count1, int count2);

	private static DistanceFunction getJaccard()
	{
		return new DistanceFunction()
		{
			@Override
			public double[] distance(final double weight, final int count1, final int count2)
			{
				double d;
				if (count1>count2) {
					d = (count1-count2);
					d /= count1;
				}else {
					d = (count2-count1);
					d /= count2;
				}
				
				return new double[]{weight*d,1.0};
			}
		};
	}

	private static DistanceFunction getManhattan()
	{
		return new DistanceFunction()
		{
			@Override
			public double[] distance(final double weight, final int count1, final int count2)
			{
				return new double[]{weight*Math.abs(count1-count2),1};
			}
		};
	}
}
