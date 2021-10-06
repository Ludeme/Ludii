//package common;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.Map.Entry;
//
//import metrics.DistanceMetric;
//
///**
// * Evaluate the distance metric by the ratio of correctly classified games to
// * the games in the same folder. Call evaluate Distance measure Nearest Neighbour
// * @author Markus
// *
// */
//public class EvaluationDistanceMetric
//{
//	
//	/**
//	 * Evaluate the distance metric by the ratio of correctly classified games to
//	 * the games in the same folder.
//	 * @param distMetric
//	 * @param kNearestNeighbours
//	 * @return The ratio of correctly classified rulesets (according to our game classification). 
//	 */
//	public static double evaluateDistanceMeasureNearestNeighbours
//	(
//		final DistanceMetric distMetric, final int kNearestNeighbours,
//		final File targetDistanceMatrix, final File targetSortedDistanceMatrix, 
//		final File treeSplitstree
//	) 
//	{
//		final ArrayList<LudRul> candidates = DistanceUtils.getAllLudiiGameFilesAndRulesetCombination(false);
//		final ArrayList<LudRul> targets = new ArrayList<>(candidates);
//
//		final DistanceMatrix<LudRul, LudRul> distMatrix = new DistanceMatrix<>(candidates, targets);
//		fillSymetricDistanceMatrix(distMetric, candidates, distMatrix);
//
//		final HashMap<LudRul, String> folderAssignment = new HashMap<>();
//
//		for (final LudRul ludRul : candidates)
//		{
//			final ArrayList<Entry<Double, LudRul>> sorted = distMatrix.getSortedDistances(ludRul);
//			removeSameLudii(sorted, ludRul);
//			while (sorted.size() > kNearestNeighbours)
//				sorted.remove(sorted.size() - 1);
//
//			final String choice = voteForFolder(sorted);
//			folderAssignment.put(ludRul, choice);
//		}
//
//		final double correctlyAssigned = countCorrectlyAssigned(folderAssignment);
//		final double ratio = (correctlyAssigned / folderAssignment.size());
//
//		System.out.println("Correctly assigned: " + correctlyAssigned + "\\" + folderAssignment.size() + " : " + ratio);
//		distMatrix.printDistanceMatrixToFile("games", DistanceUtils.outputfolder, "gameToFolder.csv", ",");
//		distMatrix.printSortedDistanceMatricesToFile(DistanceUtils.outputfolder, "gameToFolderSorted.csv", ",");
//		distMatrix.generateSplitstreeFile(DistanceUtils.outputfolder, "distance" + distMetric.getClass().getName() + ".splits");
//		
//		return ratio;
//	}
//	
//	/**
//	 * Evaluate the distance metric by the ratio of correctly classified games to
//	 * the games in the same folder
//	 * 
//	 * @param distMetric
//	 * @param kNearestNeighbours
//	 * @return The ratio of correctly classified rulesets (according to our game classification).
//	 */
//	public static double evaluateDistanceMeasureNearestNeighbours
//	(
//			final DistanceMetric distMetric, final int kNearestNeighbours
//	)
//	{
//		return evaluateDistanceMeasureNearestNeighbours(distMetric, kNearestNeighbours, null, null, null);
//	}
//
//	/**
//	 * Returns the folder of the most often appearing folder of the Games in the
//	 * list. If two appear the same time, no prediction can be made which one is
//	 * chosen.
//	 * 
//	 * @param sorted Sorted list of 1-to-many distances (plus rulesets).
//	 * @return Which folder is chosen as best fit.
//	 */
//	private static String voteForFolder(final ArrayList<Entry<Double, LudRul>> sorted)
//	{
//		final HashMap<String, Double> sum = new HashMap<>();
//		final HashMap<String, Integer> counter = new HashMap<>();
//		for (final Entry<Double, LudRul> entry : sorted)
//		{
//			sum.put(entry.getValue().getCurrentFolderName(), Double.valueOf(0.0));
//			Integer val = counter.get(entry.getValue().getCurrentFolderName());
//			if (val == null)
//				val = Integer.valueOf(0);
//			val = Integer.valueOf(val.intValue() + 1);
//			counter.put(entry.getValue().getCurrentFolderName(), val);
//		}
//		
//		for (final Entry<Double, LudRul> entry : sorted)
//		{
//			double val = sum.get(entry.getValue().getCurrentFolderName()).doubleValue();
//			val = val / counter.get(entry.getValue().getCurrentFolderName()).doubleValue();
//			sum.put(entry.getValue().getCurrentFolderName(), Double.valueOf(val + entry.getKey().doubleValue()));
//		}
//
//		final ArrayList<Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>(sum.entrySet());
//		final ArrayList<Entry<String, Integer>> entriesC = new ArrayList<Entry<String, Integer>>(counter.entrySet());
//		sortEntries(entriesC);
//		sortEntries(entries);
//		
//		String choice = entriesC.get(0).getKey();
//		if (sorted.get(0).getKey().doubleValue() < 0.01)
//			choice = sorted.get(0).getValue().getCurrentFolderName();
//		
//		return choice;
//	}
//
//	/**
//	 * Looks through the assignment and compares with the correct assignment, which
//	 * is known by LudRul
//	 * 
//	 * @param folderAssignment
//	 * @return Number of correctly assigned rulesets.
//	 */
//	private static int countCorrectlyAssigned(final HashMap<LudRul, String> folderAssignment)
//	{
//		int correctlyAssigned = 0;
//		for (final Entry<LudRul, String> entry : folderAssignment.entrySet())
//		{
//			final String goalFolder = entry.getKey().getCurrentFolderName();
//			final String assignedFolder = entry.getValue();
//			System.out.println
//			(
//				entry.getKey().getGameNameIncludingOption(false) + " " + assignedFolder + " goal: " + goalFolder
//			);
//			if (goalFolder.equals(assignedFolder))
//				correctlyAssigned++;
//		}
//		return correctlyAssigned;
//	}
//
//	/**
//	 * Calculating the distances between all candidates and fill in the matrix.
//	 * 
//	 * @param dm
//	 * @param candidates
//	 * @param dma
//	 */
//	private static void fillSymetricDistanceMatrix
//	(
//		final DistanceMetric dm, final ArrayList<LudRul> candidates, 
//		final DistanceMatrix<LudRul, LudRul> dma
//	)
//	{
//		for (int i = 0; i < candidates.size(); i++)
//		{
//			System.out.println(i + "/" + candidates.size());
//			for (int j = i; j < candidates.size(); j++)
//			{
//				System.out.print(j + "/" + candidates.size() + " ");
//				final LudRul c = candidates.get(i);
//				final LudRul tar = candidates.get(j);
//				final Score distance = dm.distance(c, tar);
//				dma.put(c, tar, distance.score());
//				dma.put(tar, c, distance.score());
//			}
//			System.out.println();
//		}
//	}
//
//	/**
//	 * remove the instances of the same Game irrespective of chosen rule set
//	 * 
//	 * @param sorted
//	 * @param ludRul
//	 */
//	private static void removeSameLudii
//	(
//		final ArrayList<Entry<Double, LudRul>> sorted, final LudRul ludRul
//	)
//	{
//		for (int i = sorted.size() - 1; i >= 0; i--)
//		{
//			final File f1 = sorted.get(i).getValue().getFile();
//			final File f2 = ludRul.getFile();
//			if (f1.equals(f2))
//				sorted.remove(i);
//		}
//	}
//
//	/**
//	 * sorts the entries by their key
//	 * 
//	 * @param <T>
//	 * @param entries
//	 */
//	private static <T extends Comparable<T>> void sortEntries
//	(
//		final ArrayList<Entry<String, T>> entries
//	)
//	{
//		entries.sort(new Comparator<Entry<String, T>>()
//		{
//			@Override
//			public int compare(final Entry<String, T> arg0, final Entry<String, T> arg1)
//			{
//
//				return arg0.getValue().compareTo(arg1.getValue());
//			}
//		});
//	}
//
//}
