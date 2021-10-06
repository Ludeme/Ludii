package processing.similarity_matrix;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import common.LudRul;
import metrics.support.TrialHelper;
import other.action.Action;
import other.trial.Trial;

/**
 * Methods to create similarity matrixes. 
 * https://en.wikipedia.org/wiki/Self-similarity_matrix
 * @author Markus
 *
 */
public class SimilarityMatrix
{

	
	public static void
			similarityMatrix(final Trial g1, final Trial g2, final File f)
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
			ImageIO.write(img, "BMP", f);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * creates a self similarity matrix of the action types of a trial and stores it to file
	 * @param g1
	 * @param f
	 */
	public static void
			similarityMatrix(final Trial g1, final File f)
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
	 * and stores it to file
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
	 * creates a binary similarity matrix between two game descriptions
	 * and stores it to file
	 * 
	 * @param g1
	 * @param f
	 */
	public static void
			similarityMatrix(final LudRul g1, final LudRul g2, final File f)
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
}
