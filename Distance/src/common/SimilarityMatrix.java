package common;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.imageio.ImageIO;

import other.action.Action;
import other.trial.Trial;
import processing.similarity_matrix.ColorScheme;
import utils.data_structures.support.TrialHelper;

public class SimilarityMatrix
{

	public static void similarityMatrix(
			final Trial g1, final Trial g2, final File f
	)
	{
		final ArrayList<Action> words1 = TrialHelper.listAllActions(g1);
		final ArrayList<Action> words2 = TrialHelper.listAllActions(g2);

		final BufferedImage img = new BufferedImage(words2.size(),
				words1.size(), BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < words1.size(); i++)
		{
			final Action actionA = words1.get(i);
			for (int j = i; j < words2.size(); j++)
			{
				final Action actionB = words2.get(j);
				int color = Color.BLACK.getRGB();

				if (!TrialHelper.isEqualType(actionA, actionB))
					color = Color.WHITE.getRGB();
				img.setRGB(j, i, color);
				if (i != j)
					img.setRGB(i, j, color);
			}
		}

		try
		{
			ImageIO.write(img, "PNG", f);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void similarityMatrix(final Trial g1, final File f)
	{
		final ArrayList<Action> words1 = TrialHelper.listAllActions(g1);
		final ArrayList<Action> words2 = words1;

		final BufferedImage img = new BufferedImage(words2.size(),
				words1.size(), BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < words1.size(); i++)
		{
			final Action actionA = words1.get(i);
			for (int j = i; j < words2.size(); j++)
			{
				final Action actionB = words2.get(j);
				int color = Color.BLACK.getRGB();

				if (!TrialHelper.isEqualType(actionA, actionB))
					color = Color.WHITE.getRGB();
				img.setRGB(j, i, color);
				if (i != j)
					img.setRGB(i, j, color);
			}
		}

		try
		{
			ImageIO.write(img, "BMP", f);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * creates a binary similarity matrix with itself
	 * 
	 * @param g1
	 * @param f
	 */
	public static void similarityMatrix(final LudRul g1, final File f)
	{
		final String[] words1 = g1.getDescriptionSplit();
		final String[] words2 = words1;

		final BufferedImage img = new BufferedImage(words2.length,
				words1.length, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < words1.length; i++)
		{
			for (int j = i; j < words2.length; j++)
			{
				int color = Color.BLACK.getRGB();
				if (!words1[i].equals(words2[j]))
					color = Color.WHITE.getRGB();
				img.setRGB(j, i, color);
				if (i != j)
					img.setRGB(i, j, color);
			}
		}

		try
		{
			ImageIO.write(img, "BMP", f);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * creates a binary similarity matrix with itself
	 * 
	 * @param g1
	 * @param f
	 */
	public static void similarityMatrix(
			final LudRul g1, final LudRul g2, final File f
	)
	{
		final String[] words1 = g1.getDescriptionSplit();
		final String[] words2 = g2.getDescriptionSplit();

		final BufferedImage img = new BufferedImage(words2.length,
				words1.length, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < words1.length; i++)
		{
			for (int j = 0; j < words2.length; j++)
			{
				int color = Color.BLACK.getRGB();
				if (!words1[i].equals(words2[j]))
					color = Color.WHITE.getRGB();
				img.setRGB(j, i, color);

			}
		}

		try
		{
			ImageIO.write(img, "BMP", f);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * creates a self similarity matrix for every ludii game found
	 * 
	 * @param outPut
	 */
	public static void createSelfSimilarityMatrices(final File outPut, final File startFolder)
	{
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false,startFolder, null);
		for (int i = 0; i < candidates.size(); i++)
		{

			final LudRul g1 = candidates.get(i);
			final String name = g1.getGameNameIncludingOption(false).replace("/",
					"_");
			similarityMatrix(g1, new File(outPut.getAbsolutePath()
					+ "/allSimilarities/" + name + ".bmp"));
			System.out.println(i + "/" + candidates.size());
		}

	}

	/**
	 * Stores the similarity matrix in the file. Distances should be between
	 * [0.0;1.0]
	 * 
	 * @param dm
	 * @param candidates
	 * @param f
	 * @param format     "PNG" is recommended
	 */
	public static void createAndStoreMetricSimilarityMatrix(
			final DistanceMatrix<LudRul, LudRul> dm,
			final ArrayList<LudRul> candidates,
			final File f,
			final String format
	)
	{
		final ArrayList<LudRul> sCandidates = sortCandidates(candidates);

		final BufferedImage img = getSimilarityImage(dm, sCandidates);	
		storeImage(f, format, img);
	}

	private static void storeImage(
			final File f, final String format, final BufferedImage img
	)
	{
		try
		{
			ImageIO.write(img, format, f);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param dm
	 * @param candidates
	 * @return a visualitymatrix depending on the distances
	 */
	public static BufferedImage getSimilarityImage(
			final DistanceMatrix<LudRul, LudRul> dm,
			final ArrayList<LudRul> candidates
	)
	{
		final BufferedImage img = new BufferedImage(candidates.size(),
				candidates.size(), BufferedImage.TYPE_INT_RGB);
		final ColorScheme cs = ColorScheme.getDefault();
		for (int i = 0; i < candidates.size(); i++)
		{
			for (int j = 0; j < candidates.size(); j++)
			{
				final double d = dm.get(candidates.get(i), candidates.get(j));
				
				final int colorRGB = cs.getColorRGBFromDistance(d);
				//img.setRGB(j, i, colorRGB);
				img.setRGB(i, j, colorRGB);

			}
		}
		return img;
	}

	/**
	 * sorts candidates by their folder then by name
	 * 
	 * @param candidates
	 * @return a new list of candidates sorted by their folder and name
	 */
	public static ArrayList<LudRul> sortCandidates(
			final ArrayList<LudRul> candidates
	)
	{
		final ArrayList<LudRul> sortedCandidates = new ArrayList<>(candidates);
		Collections.sort(sortedCandidates, new Comparator<LudRul>()
		{
			@Override
			public int compare(final LudRul o1, final LudRul o2)
			{
				final int sortVal = o1.getCurrentClassName()
						.compareTo(o2.getCurrentClassName());
				if (sortVal != 0)
					return sortVal;
				return o1.getGameNameIncludingOption(false)
						.compareTo(o2.getGameNameIncludingOption(false));
			}
		});
		return sortedCandidates;
	}

	/**
	 * 
	 * @param candidates
	 * @return a distance matrix which contains 0.0 if two games are part of the
	 *         same cluster. 1.0 if different cluster
	 */
	public static DistanceMatrix<LudRul, LudRul> getClusterMatrix(
			final ArrayList<LudRul> candidates
	)
	{
		final DistanceMatrix<LudRul, LudRul> clusterMatrix = new DistanceMatrix<>(
				candidates, candidates);
		for (final LudRul ludRul : candidates)
		{
			for (final LudRul ludRul2 : candidates)
			{
				double d = 1.0;
				if (ludRul.getCurrentClassName()
						.equals(ludRul2.getCurrentClassName()))
					d = 0.0;

				clusterMatrix.put(ludRul, ludRul2, d);
			}
		}
		return clusterMatrix;
	}

	public static ArrayList<LudRul> sortCandidates(
			final LudRul cand, final ArrayList<LudRul> candidates,
			final DistanceMatrix<LudRul, LudRul> distanceMatrix
	)
	{
		final ArrayList<LudRul> newSort = new ArrayList<>(candidates);
		newSort.sort(new Comparator<LudRul>()
		{

			@Override
			public int compare(final LudRul o1, final LudRul o2)
			{
				final double d1 = distanceMatrix.get(o1, cand);
				final double d2 = distanceMatrix.get(o2, cand);
				final int comp = Double.compare(d1, d2);
				if (comp!=0)return comp;
				return o1.getGameNameIncludingOption(false).compareTo(o2.getGameNameIncludingOption(false));
			}
		});
		return newSort;
	}
}
