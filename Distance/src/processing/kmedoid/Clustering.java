package processing.kmedoid;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import common.DistanceMatrix;
import common.LudRul;

public class Clustering implements LineDrawable
{

	private final int k;
	private double sse;
	private final LudRul[] medoids;

	public LudRul closest(
			final DistanceMatrix<LudRul, LudRul> dml, final LudRul compareTo
	)
	{
		LudRul closest = null;
		double minDistance = Integer.MAX_VALUE;
		for (final LudRul med : medoids)
		{
			final double d = dml.get(med, compareTo);
			if (d < minDistance)
			{
				minDistance = d;
				closest = med;
			}

		}
		return closest;

	}

	public Clustering(
			final ArrayList<LudRul> candidates,
			final DistanceMatrix<LudRul, LudRul> distanceMatrix, final int k
	)
	{

		final Random r = new Random();
		this.k = k;
		medoids = new LudRul[k];
		final ArrayList<LudRul> bag = new ArrayList<>(candidates);
		for (int i = 0; i < k; i++)
		{
			final int selection = r.nextInt(bag.size() - 1);
			medoids[i] = bag.get(selection);
			bag.set(selection, bag.get(bag.size() - 1));
			bag.get(bag.size() - 1);

		}
		sse = getSse(medoids, candidates, distanceMatrix);
		System.out.println("kmedoid clustering for k: " + k);
		System.out.println("SSE start:" + sse);

		boolean improvment = true;
		while (improvment)
		{
			double bestSwapScore = Integer.MAX_VALUE;
			LudRul bestNewMedoid = null;
			int worstOldMedoidIndex = -1;
			for (int i = 0; i < medoids.length; i++)
			{
				final LudRul[] medoidCopy = Arrays.copyOf(medoids,
						medoids.length);
				for (final LudRul c : bag)
				{
					medoidCopy[i] = c;
					final double newSse = getSse(medoidCopy, candidates,
							distanceMatrix);
					if (newSse < bestSwapScore)
					{
						bestSwapScore = newSse;
						bestNewMedoid = c;
						worstOldMedoidIndex = i;
					}
				}
			}
			System.out.println(bestSwapScore + " " + worstOldMedoidIndex + " "
					+ bestNewMedoid);
			if (bestSwapScore < sse)
			{
				improvment = true;
				sse = bestSwapScore;
				bag.remove(bestNewMedoid);
				bag.add(medoids[worstOldMedoidIndex]);
				medoids[worstOldMedoidIndex] = bestNewMedoid;
			} else
			{
				improvment = false;
			}

		}

		System.out.println("finished");
		// randomly

	}

	private static double getSse(
			final LudRul[] medoid, final ArrayList<LudRul> candidates,
			final DistanceMatrix<LudRul, LudRul> distanceMatrix
	)
	{
		final double[][] dm = distanceMatrix.getDistanceMatrix();
		double sum = 0.0;
		final int[] medoidIndices = new int[medoid.length];
		final HashMap<LudRul, Integer> tti = distanceMatrix.getTargetToIndex();
		for (int i = 0; i < medoidIndices.length; i++)
		{
			medoidIndices[i] = tti.get(medoid[i]).intValue();
		}
		for (final LudRul c : candidates)
		{
			double shortest = Integer.MAX_VALUE;
			final int cIndex = distanceMatrix.getCandidateToIndex().get(c)
					.intValue();
			final double[] distancesFromCandidates = dm[cIndex];
			for (final int i : medoidIndices)
			{
				if (distancesFromCandidates[i] < shortest)
				{
					shortest = distancesFromCandidates[i];
				}
			}
			sum += shortest * shortest;
		}
		return sum;
	}

	public LudRul[] getMedoid()
	{
		return medoids;
	}

	public int getK()
	{
		return k;
	}

	public double getSSE()
	{

		return sse;
	}

	public HashMap<LudRul, String> getMedoidAssignment(
			final ArrayList<LudRul> sortedCandidates,
			final DistanceMatrix<LudRul, LudRul> dm
	)
	{
		final HashMap<LudRul, String> assignment = new HashMap<>();
		for (final LudRul ludRul : sortedCandidates)
		{
			final LudRul clMedioid = closest(dm, ludRul);
			assignment.put(ludRul, clMedioid.getGameNameIncludingOption(true));
		}
		return assignment;
	}

	public static BufferedImage getSimilarityImage(
			final Clustering clustering, final DistanceMatrix<LudRul, LudRul> distanceMatrix,
			final ArrayList<LudRul> candidates
	)
	{

		final BufferedImage img = new BufferedImage(candidates.size(),
				candidates.size(), BufferedImage.TYPE_INT_RGB);
		final LudRul[] medioids = clustering.getMedoid();
		final HashMap<LudRul,Color> colorMedoid = new HashMap<>();
		final HashMap<LudRul,Color> colorClass = new HashMap<>();
		final Color colorMiss = Color.getHSBColor(3.f/4.f, 1.f, 1.f);
		for (int i = 0; i < medioids.length; i++)
		{
			final float angle = (3f/4f + ((float)i+1)/(medioids.length+1))%1f;
			final Color cm = Color.getHSBColor(angle, 1.f, 0.3f); //todo magic numbers
			final Color cc = Color.getHSBColor(angle, 1.f, 1.0f);
			colorMedoid.put(medioids[i], cm);
			colorClass.put(medioids[i], cc);
			
		}
		for (int i = 0; i < candidates.size(); i++)
		{
			for (int j = 0; j < candidates.size(); j++)
			{
				
				final LudRul ci = candidates.get(i);
				final LudRul cj = candidates.get(j);
				
				final LudRul medoidI = clustering.closest(distanceMatrix, ci );
				final LudRul medoidJ = clustering.closest(distanceMatrix, cj);
				
				/*boolean oneIsMedioid = false;
				if (colorMedoid.keySet().contains(ci)||colorMedoid.keySet().contains(cj)) {
					oneIsMedioid = true;
				}*/
				boolean bothAreMedioid = false;
				if (colorMedoid.keySet().contains(ci)&&(i==j)) {
					bothAreMedioid  = true;
				}
				
				final int colorRGB;
				if (medoidI==medoidJ) {
					if (bothAreMedioid)
						colorRGB = colorMedoid.get(medoidI).getRGB();
					else
						colorRGB = colorClass.get(medoidI).getRGB();
				}else {
					colorRGB = colorMiss.getRGB();
				}

				img.setRGB(i, j, colorRGB);

			}
		}
		return img;

	}

	@Override
	public int getX()
	{
		return k;
	}

	@Override
	public double getY()
	{
		return sse;
	}
}
