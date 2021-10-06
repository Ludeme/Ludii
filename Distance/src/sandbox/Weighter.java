package sandbox;

public abstract class Weighter
{

	public static final Weighter noWeight = getNoWeight();
	public static final Weighter inverseWeight = getInverseWeight();
	public static final Weighter increaseWeight = getIncreaseWeight();

	public abstract double weight(int depth, int maxDepth);

	private static Weighter getIncreaseWeight()
	{
		return new Weighter() {

			@Override
			public double weight(final int depth, final int maxDepth)
			{
				return depth; //the root has depth 0
			}
			
		};
	}

	private static Weighter getInverseWeight()
	{
		return new Weighter()
		{
			
			@Override
			public double weight(final int depth, final int maxDepth)
			{
				if (depth==0)return 0;
				return 1.0/(depth); //the root has depth 0
			}
		};
	}

	private static Weighter getNoWeight()
	{
		return new Weighter()
		{
			
			@Override
			public double weight(final int depth,final int maxDepth)
			{
				return 1.0;
			}
		};
	}
}
