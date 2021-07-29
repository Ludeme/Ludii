package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Helps to store distances between instances of two classes (i.e. ludi game
 * files, but also strings are allowed) , without keeping tracks of the indicies
 * Type needs to have toString() implemented as way to distinguish between
 * instances. If no distance is put in 0 is assumed
 *
 * @author Markus
 *
 */
public class DistanceMatrix<C, T>
{
	private final double[][] distanceMatrix;
	private final HashMap<C, Integer> candidateToIndex;
	private final HashMap<T, Integer> targetToIndex;
	private final ArrayList<C> indexToCandidate;
	private final ArrayList<T> indexToTarget;

	/**
	 * A m*n distance matrix where candidates is the rows and targets is the columns
	 * 
	 * @param candidates
	 * @param targets
	 */
	public DistanceMatrix(final List<C> candidates, final List<T> targets)
	{
		distanceMatrix = new double[candidates.size()][targets.size()];
		candidateToIndex = new HashMap<>();
		targetToIndex = new HashMap<>();
		indexToCandidate = new ArrayList<>();
		indexToTarget = new ArrayList<>();

		for (int i = 0; i < candidates.size(); i++)
		{
			final C candidate = candidates.get(i);
			candidateToIndex.put(candidate, Integer.valueOf(i));
			indexToCandidate.add(candidate);
		}
		for (int i = 0; i < targets.size(); i++)
		{
			final T target = targets.get(i);
			targetToIndex.put(target, Integer.valueOf(i));
			indexToTarget.add(target);
		}
	}

	/**
	 * returns for a candidate the distances to every target sorted ascending by
	 * distance
	 * 
	 * @param candidate
	 * @return A sorted ascending list of entries by distance
	 */
	public ArrayList<Entry<Double, T>> getSortedDistances(final C candidate)
	{
		final ArrayList<Entry<Double, T>> nameDistance = new ArrayList<>();

		final double[] ds = distanceMatrix[candidateToIndex.get(candidate).intValue()];
		for (int j = 0; j < ds.length; j++)
		{
			final Double d = Double.valueOf(ds[j]);
			final T target = indexToTarget.get(j);
			nameDistance.add(new AbstractMap.SimpleEntry<Double, T>(d, target));
		}

		nameDistance.sort(new Comparator<Entry<Double, T>>()
		{
			@Override
			public int compare(final Entry<Double, T> o1, final Entry<Double, T> o2)
			{
				return Double.compare(o1.getKey().doubleValue(), o2.getKey().doubleValue());
			}
		});
		return nameDistance;

	}

	/**
	 * add distances to the matrix
	 * 
	 * @param candidate
	 * @param target
	 * @param distance
	 */
	public void put(final C candidate, final T target, final double distance)
	{
		final int index1 = candidateToIndex.get(candidate).intValue();
		final int index2 = targetToIndex.get(target).intValue();
		distanceMatrix[index1][index2] = distance;
	}

	/**
	 * creates a csv file, which shows the sorted distances for every row. Therefore
	 * the column descriptions repeat themselves
	 * 
	 * @param folder
	 * @param fileName
	 * @param decimalSymbol
	 */
	public void printSortedDistanceMatricesToFile(final File folder, final String fileName, final String decimalSymbol)
	{
		boolean append = false; // first time false then true

		for (int i = 0; i < indexToCandidate.size(); i++)
		{
			final ArrayList<String> gameName1 = new ArrayList<>();
			gameName1.add(indexToCandidate.get(i).toString());
			final C smth = indexToCandidate.get(i);
			String goalAssignment = "games";
			if (smth instanceof LudRul)
				goalAssignment = "goal_" + ((LudRul) smth).getCurrentFolderName();
			final ArrayList<String> gameName2 = new ArrayList<>();
			final ArrayList<Entry<Double, String>> nameDistance = new ArrayList<>();

			final double[] ds = distanceMatrix[i];
			for (int j = 0; j < ds.length; j++)
			{
				final Double d = Double.valueOf(ds[j]);
				final String name = indexToTarget.get(j).toString();
				nameDistance.add(new AbstractMap.SimpleEntry<Double, String>(d, name));
			}
			nameDistance.sort(new Comparator<Entry<Double, String>>()
			{
				@Override
				public int compare(final Entry<Double, String> o1, final Entry<Double, String> o2)
				{
					return Double.compare(o1.getKey().doubleValue(), o2.getKey().doubleValue());
				}
			});

			final double[][] distances = new double[1][nameDistance.size()];

			for (int j = 0; j < nameDistance.size(); j++)
			{
				final Entry<Double, String> entry = nameDistance.get(j);
				gameName2.add(entry.getValue());
				distances[0][j] = entry.getKey().doubleValue();
			}

			DistanceMatrix.printDistanceMatrixToFile(goalAssignment, gameName1, gameName2, distances, folder, fileName,
					decimalSymbol, append);
			append = true;
		}
	}

	/**
	 * Prints the distance matrix to the file
	 * 
	 * @title The word in cell 0,0
	 * @param folder
	 * @param fileName
	 * @param decimalSymbol
	 */
	public void printDistanceMatrixToFile
	(
			final String title, final File folder, final String fileName, 
			final String decimalSymbol
	)
	{
		final ArrayList<String> rowNames = new ArrayList<>();
		final ArrayList<String> columnNames = new ArrayList<>();

		for (final C candidate : indexToCandidate)
			rowNames.add(candidate.toString());

		for (final T target : indexToTarget)
			columnNames.add(target.toString());

		printDistanceMatrixToFile
		(
			title, rowNames, columnNames, distanceMatrix, folder, 
			fileName, decimalSymbol, false
		);
	}

	/**
	 * Prints the distance matrix to a csv file
	 * 
	 * @param gameNames1
	 * @param gameNames2
	 * @param matrix
	 * @param folder
	 * @param fileName
	 * @param append
	 */
	//@SuppressWarnings("resource")
	public static void printDistanceMatrixToFile
	(
			final String title, final ArrayList<String> gameNames1, 
			final ArrayList<String> gameNames2, final double[][] matrix, 
			final File folder, final String fileName, final String decimalSymbol,
			final boolean append
	)
	{
		final DecimalFormat df = new DecimalFormat("#.####");

		final StringBuilder sb = new StringBuilder();
		sb.append(title + ";");
		final int n = matrix.length;
		for (final String name : gameNames2) // first line with game names
			sb.append(name + ";");
		
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");

		for (int i = 0; i < n; i++)
		{
			sb.append(gameNames1.get(i) + ";");
			int k = i;
			if (true) // get full matrix, not only upper
				k = 0;
			
			for (int j = k; j < matrix[0].length; j++)
			{
				final double distance = matrix[i][j];
				sb.append(df.format(distance) + ";");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("\n");
			
		}
		sb.append("\n");

		final String finalText = sb.toString().replace(".", decimalSymbol);
		final String path = folder + File.separator + fileName;

		try (final FileWriter out2 = new FileWriter(new File(path), append))
		{
			out2.append(finalText);
		} 
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Prints the square distance matrix to a csv file
	 * 
	 * @param title     the word in cell 0,0
	 * @param matrix
	 * @param folder
	 * @param gameNames
	 * @param fileName
	 */
	public static void printSquareDistanceMatrixToFile
	(
			final String title, final ArrayList<String> gameNames, 
			final double[][] matrix, final File folder,
			final String fileName, final String decimalSymbol
	)
	{
		assert (matrix.length != matrix[0].length);

		printDistanceMatrixToFile
		(
			title, gameNames, gameNames, matrix, folder, fileName, 
			decimalSymbol, false
		);
	}

	/**
	 * Print out distances of an alignment matrix (note test again if its on purpose
	 * that the first row and column is not printed )
	 * 
	 * @param wordsA
	 * @param wordsB
	 * @param distances
	 */
	public static void printOutAlignmentMatrix
	(
		final String[] wordsA, final String[] wordsB, final int[][] distances
	)
	{
		final ArrayList<String> lA = new ArrayList<>();
		lA.add("");
		final ArrayList<String> lB = new ArrayList<>();
		lB.add("");
		for (int i = 0; i < wordsB.length; i++)
		{
			final String string2 = wordsB[i];
			lB.add(i + "_" + string2);
		}
		for (int i = 0; i < wordsA.length; i++)
		{
			final String string2 = wordsA[i];
			lA.add(i + "_" + string2);
		}
		final DistanceMatrix<String, String> dm = new DistanceMatrix<>(lA, lB);
		for (int i = 1; i < lA.size(); i++)
		{
			final String wa = lA.get(i);
			for (int j = 1; j < lB.size(); j++)
			{
				final String wb = lB.get(j);
				dm.put(wa, wb, distances[i][j]);
			}
		}
		dm.printDistanceMatrixToFile("Words", DistanceUtils.outputfolder, "allignment.csv", ",");
	}

	public void generateSplitstreeFile(final File outputfolder, final String fileName)
	{
		if (indexToCandidate.size()!=indexToTarget.size()) {
			System.out.println("Distance matrix not symetrical");
			return;
		}
		final ArrayList<String> rowNames = new ArrayList<>();

		for (final C candidate : indexToCandidate)
		{
			rowNames.add(candidate.toString());
		}
		DistanceUtils.generateSplitTreeInput(rowNames, distanceMatrix, outputfolder, fileName);
	}

}
