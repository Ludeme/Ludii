package common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

import metrics.DistanceMetric;
import processing.similarity_matrix.AssignmentSettings;
import utils.data_structures.support.DistanceProgressListener;

/**
 * Evaluate the distance metric by the ratio of correctly classified games to
 * the games in the same folder. Call evaluate Distance measure Nearest
 * Neighbour
 * 
 * @author Markus
 *
 */
public class EvaluatorDistanceMetric
{
	/**
	 * Evaluate the distance metric by the ratio of correctly classified games
	 * to the games in the same folder.
	 * 
	 * @param distMetric
	 * @param ass 
	 * @param pl may be null
	 * @return The ratio of correctly classified rulesets (according to our game
	 *         classification).
	 */
	public static AssignmentStats evaluateDistanceMeasureNearestNeighbours(
			final DistanceMetric distMetric, final ArrayList<LudRul> candidates,
			final AssignmentSettings ass, DistanceProgressListener pl, boolean forceRecalculation
	)
	{
		final DistanceMatrix<LudRul, LudRul> distMatrix = getDistanceMatrix(
				candidates, distMetric, forceRecalculation, pl);

		
		final HashMap<LudRul, String> classAssignment = getClassAssignment(candidates,
				distMatrix, ass);
		final AssignmentStats asst = getAssignmentStats(candidates, ass, classAssignment);
		
		System.out.println(asst.toString());
		
		System.out.println("Correctly assigned: " + asst.getCorrectlyAssigned() + "\\"
				+ classAssignment.size() + " : " + asst.getCorrectlyAssignedRate());
		
		return asst;
	}

	public static DistanceMatrix<LudRul, LudRul> getAssigned(
			final ArrayList<LudRul> candidates,
			final HashMap<LudRul, String> classAssignment
	)
	{
		final DistanceMatrix<LudRul, LudRul> assignedMatrix = new DistanceMatrix<>(
				candidates, candidates);
		for (final LudRul ludRul : candidates)
		{
			for (final LudRul ludRul2 : candidates)
			{
				double d = 1.0;
				if (classAssignment.get(ludRul)
						.equals(classAssignment.get(ludRul2)))
					d = 0.0;

				assignedMatrix.put(ludRul, ludRul2, d);
			}
		}
		return assignedMatrix;
	}
	
	public static HashMap<LudRul, String> getClassAssignment(
			final ArrayList<LudRul> candidates, final DistanceMatrix<LudRul, LudRul> distMatrix, final AssignmentSettings ass
	)
	{
		final HashMap<LudRul, String> classAssignment = new HashMap<>();

		final HashSet<LudRul> toIgnore = getNoisyCandidates(ass,candidates,
				distMatrix);
		final int kNearestNeighbours = ass.getNearestNeighbour();

		for (final LudRul ludRul : candidates)
		{
			final ArrayList<Entry<Double, LudRul>> sorted = distMatrix
					.getSortedDistances(ludRul);
			removeSameLudii(sorted, ludRul);
			removeNoise(toIgnore, sorted);
			while (sorted.size() > kNearestNeighbours)
				sorted.remove(sorted.size() - 1);

			final String choice = voteForFolder(sorted);
			classAssignment.put(ludRul, choice);
		}
		return classAssignment;
	}

	private static HashSet<LudRul> getNoisyCandidates(
			final AssignmentSettings ass, final ArrayList<LudRul> candidates,
			final DistanceMatrix<LudRul, LudRul> distMatrix
	)
	{
		final HashSet<LudRul> noise = new HashSet<>();
		final ClassGroundTruth cgt = ass.getClassGroundTruth();
		final int nNeigh = ass.getNearestNeighbourNoise();
		if (nNeigh==0)return noise;
		
		for (final LudRul ludRul : candidates)
		{
			final ArrayList<Entry<Double, LudRul>> sorted = distMatrix
					.getSortedDistances(ludRul);
			removeSameLudii(sorted, ludRul);
			final ArrayList<String> folderNames = new ArrayList<>(nNeigh);
			final HashSet<String> names = new HashSet<>(nNeigh);
			for (int i = 0; i < nNeigh; i++)
			{
				final LudRul neigh = sorted.get(i).getValue();
				final String currentName = cgt.getClass(neigh);
				folderNames.add(currentName);
				names.add(currentName);
			}
			String maximumVote = "";
			int maximumCount = 0;
			for (final String string : names)
			{
				final int occurrences = Collections.frequency(folderNames,
						string);
				if (occurrences > maximumCount)
				{
					maximumCount = occurrences;
					maximumVote = string;
				}
			}
			if (!maximumVote.equals(ludRul.getCurrentClassName()))
				noise.add(ludRul);

		}
		return noise;
	}

	/**
	 * 
	 * @param distMetric
	 * @param candidates
	 * @param progressListener 
	 * @return a distance matrix, which has all the distances filled in
	 */
	private static DistanceMatrix<LudRul, LudRul> getFilledDistanceMatrix(
			final DistanceMetric distMetric, final ArrayList<LudRul> candidates, final DistanceProgressListener progressListener
	)
	{
		final ArrayList<LudRul> targets = new ArrayList<LudRul>(candidates);
		final DistanceMatrix<LudRul, LudRul> dmat = new DistanceMatrix<>(
				candidates, targets);
		fillSymetricDistanceMatrix(distMetric, candidates, dmat,progressListener);
		return dmat;
	}

	/**
	 * Evaluate the distance metric by the ratio of correctly classified games
	 * to the games in the same folder
	 * 
	 * @param startFolder
	 * 
	 * @param distMetric
	 * @return The ratio of correctly classified rulesets (according to our game
	 *         classification).
	 */
	public static AssignmentStats evaluateDistanceMeasureNearestNeighbours(
			final File startFolder, final DistanceMetric distMetric,
			final AssignmentSettings ass
	)
	{
		final ArrayList<LudRul> candidates = DistanceUtils
				.getAllLudiiGameFilesAndRulesetCombination(false, startFolder, null);
		return evaluateDistanceMeasureNearestNeighbours(distMetric,
				candidates, ass,null,false);
	}

	

	/**
	 * Returns the folder of the most often appearing folder of the Games in the
	 * list. If two appear the same time, no prediction can be made which one is
	 * chosen.
	 * 
	 * @param sorted Sorted list of 1-to-many distances (plus rulesets).
	 * @return Which folder is chosen as best fit.
	 */
	private static String voteForFolder(
			final ArrayList<Entry<Double, LudRul>> sorted
	)
	{
		final HashMap<String, Double> sum = new HashMap<>();
		final HashMap<String, Integer> counter = new HashMap<>();
		for (final Entry<Double, LudRul> entry : sorted)
		{
			sum.put(entry.getValue().getCurrentClassName(),
					Double.valueOf(0.0));
			Integer val = counter.get(entry.getValue().getCurrentClassName());
			if (val == null)
				val = Integer.valueOf(0);
			val = Integer.valueOf(val.intValue() + 1);
			counter.put(entry.getValue().getCurrentClassName(), val);
		}

		for (final Entry<Double, LudRul> entry : sorted)
		{
			double val = sum.get(entry.getValue().getCurrentClassName())
					.doubleValue();
			val = val / counter.get(entry.getValue().getCurrentClassName())
					.doubleValue();
			sum.put(entry.getValue().getCurrentClassName(),
					Double.valueOf(val + entry.getKey().doubleValue()));
		}

		final ArrayList<Entry<String, Double>> entries = new ArrayList<Entry<String, Double>>(
				sum.entrySet());
		final ArrayList<Entry<String, Integer>> entriesC = new ArrayList<Entry<String, Integer>>(
				counter.entrySet());
		sortEntries(entriesC);
		sortEntries(entries);
		Collections.reverse(entriesC);
		String choice = entriesC.get(0).getKey();
		if (entries.size()>1) {
			if (entriesC.get(0).getValue().equals(entriesC.get(1).getValue())) {
				//at least two classes have the same amount of votes, choose closer
				HashSet<String> acceptableClasses = new HashSet<>();
				for (Entry<String, Integer> entry : entriesC)
				{
					if (entry.getValue().equals(entriesC.get(0).getValue())) {
						acceptableClasses.add(entry.getKey());
					}
				}
				
				for (Entry<Double,LudRul> game : sorted)
				{
					if (acceptableClasses.contains(game.getValue().getCurrentClassName())) {
						choice = game.getValue().getCurrentClassName();
						return choice;
					}
				}
			}
		}
		
		if (sorted.get(0).getKey().doubleValue() < 0.01)
			choice = sorted.get(0).getValue().getCurrentClassName();

		return choice;
	}


	/**
	 * Calculating the distances between all candidates and fill in the matrix.
	 * 
	 * @param dm
	 * @param candidates
	 * @param dma
	 * @param progressListener 
	 */
	private static void fillSymetricDistanceMatrix(
			final DistanceMetric dm, final ArrayList<LudRul> candidates,
			final DistanceMatrix<LudRul, LudRul> dma, final DistanceProgressListener progressListener
	)
	{
		final int totalComparisions = (candidates.size()*(candidates.size()+1))/2;
		int counter = 0;
		if (progressListener!=null)progressListener.update(true,candidates.size(),counter/(double)totalComparisions,counter,totalComparisions);
		
		for (int i = 0; i < candidates.size(); i++)
		{
			System.out.println(i + "/" + candidates.size());
			for (int j = i; j < candidates.size(); j++)
			{
				System.out.print(j + "/" + candidates.size() + " ");
				final LudRul c = candidates.get(i);
				final LudRul tar = candidates.get(j);
				final Score distance = dm.distance(c, tar);
				dma.put(c, tar, distance.score());
				dma.put(tar, c, distance.score());
				counter++;
				if (progressListener!=null)progressListener.update(true,candidates.size(),counter/totalComparisions,counter,totalComparisions);
			}
			System.out.println();
		}
	}

	private static void removeNoise(
			final HashSet<LudRul> toIgnore,
			final ArrayList<Entry<Double, LudRul>> sorted
	)
	{
		for (int i = sorted.size() - 1; i >= 0; i--)
		{
			if (toIgnore.contains(sorted.get(i).getValue()))
			{
				sorted.remove(i);
			}
		}
	}

	/**
	 * remove the instances of the same Game irrespective of chosen rule set
	 * 
	 * @param sorted
	 * @param ludRul
	 */
	private static void removeSameLudii(
			final ArrayList<Entry<Double, LudRul>> sorted, final LudRul ludRul
	)
	{
		for (int i = sorted.size() - 1; i >= 0; i--)
		{
			
			final String f1 = sorted.get(i).getValue().getGameNameWithoutOption();
			final String f2 = ludRul.getGameNameWithoutOption();
			if (f1.equals(f2))
				sorted.remove(i);
		}
	}

	/**
	 * sorts the entries by their key
	 * 
	 * @param <T>
	 * @param entries
	 */
	private static <T extends Comparable<T>> void sortEntries(
			final ArrayList<Entry<String, T>> entries
	)
	{
		entries.sort(new Comparator<Entry<String, T>>()
		{
			@Override
			public int compare(
					final Entry<String, T> arg0, final Entry<String, T> arg1
			)
			{

				return arg0.getValue().compareTo(arg1.getValue());
			}
		});
	}

	/**
	 * 
	 * @param candidates
	 * @param cm
	 * @param assigned
	 * @param classAssignment 
	 * @return a distance matrix where 0.5 means both value the same.
	 */
	public static DistanceMatrix<LudRul, LudRul> getCorrectlyAssigned(
			final ArrayList<LudRul> candidates,
			final DistanceMatrix<LudRul, LudRul> cm,
			final DistanceMatrix<LudRul, LudRul> assigned, final HashMap<LudRul,String> classAssignment
	)
	{
		final DistanceMatrix<LudRul, LudRul> deltaMat = new DistanceMatrix<>(
				candidates, candidates);
		for (int i = 0; i < candidates.size(); i++)
		{
			final LudRul ludRul = candidates.get(i);
			final String folder1 = ludRul.getCurrentClassName();
			final String assigned1 = classAssignment.get(ludRul);
			final boolean firstCorrectlyAssigned = folder1.equals(assigned1);
			for (int j = i; j < candidates.size(); j++)
			{
				final LudRul ludRul2 = candidates.get(j);
				final String folder2 = ludRul2.getCurrentClassName();
				final String assigned2 = classAssignment.get(ludRul2);
				//final boolean secondCorrectlyAssigned = folder2.equals(assigned2);
				final boolean shouldBeSameFolder = folder2.equals(folder1);
				final boolean areSameFolderAssigned = assigned1.equals(assigned2);
				if (shouldBeSameFolder&&areSameFolderAssigned) {
					if (firstCorrectlyAssigned) {
						deltaMat.put(ludRul, ludRul2, 0.0);
						deltaMat.put(ludRul2, ludRul, 0.0);
					}else {
						deltaMat.put(ludRul, ludRul2, 0.1);
						deltaMat.put(ludRul2, ludRul, 0.1);
					}
					
				}
				if (!shouldBeSameFolder&&areSameFolderAssigned) {
					deltaMat.put(ludRul, ludRul2, 0.6);
					deltaMat.put(ludRul2, ludRul, 0.6);
				}
				if (shouldBeSameFolder&&!areSameFolderAssigned) {
					deltaMat.put(ludRul, ludRul2, 0.3);
					deltaMat.put(ludRul2, ludRul, 0.3);
				}
				if (!shouldBeSameFolder&&!areSameFolderAssigned) {
					deltaMat.put(ludRul, ludRul2, 1.0);
					deltaMat.put(ludRul2, ludRul, 1.0);
				}
				
			}
		}
		return deltaMat;
	}

	/**
	 * 
	 * @param candidates
	 * @param cm
	 * @param assigned
	 * @param classAssignment 
	 * @return a distance matrix where 0.5 means both value the same.
	 */
	public static DistanceMatrix<LudRul, LudRul> getCorrectlyAssigned2(
			final ArrayList<LudRul> candidates,
			final DistanceMatrix<LudRul, LudRul> cm,
			final DistanceMatrix<LudRul, LudRul> assigned, final HashMap<LudRul,String> classAssignment
	)
	{
		final DistanceMatrix<LudRul, LudRul> deltaMat = new DistanceMatrix<>(
				candidates, candidates);
		for (int i = 0; i < candidates.size(); i++)
		{
			final LudRul ludRul = candidates.get(i);
			final String folder1 = ludRul.getCurrentClassName();
			final String assigned1 = classAssignment.get(ludRul);
			final boolean firstCorrectlyAssigned = folder1.equals(assigned1);
			for (int j = i; j < candidates.size(); j++)
			{
				final LudRul ludRul2 = candidates.get(j);
				final String folder2 = ludRul2.getCurrentClassName();
				final String assigned2 = classAssignment.get(ludRul2);
				final boolean secondCorrectlyAssigned = folder2.equals(assigned2);
				final boolean shouldBeSameFolder = folder2.equals(folder1);
				//final boolean areSameFolderAssigned = assigned1.equals(assigned2);
				if (firstCorrectlyAssigned&&secondCorrectlyAssigned) {
					if (shouldBeSameFolder) {
						deltaMat.put(ludRul, ludRul2, 0.0);
						deltaMat.put(ludRul2, ludRul, 0.0);
					}else {
						deltaMat.put(ludRul, ludRul2, 1.0);
						deltaMat.put(ludRul2, ludRul, 1.0);
					}
					continue;
				}
				if (firstCorrectlyAssigned) {
					if (shouldBeSameFolder) {
						deltaMat.put(ludRul, ludRul2, 0.8);
						deltaMat.put(ludRul2, ludRul, 0.8);
					}else {
						deltaMat.put(ludRul, ludRul2, 1.0);
						deltaMat.put(ludRul2, ludRul, 1.0);
					}
					continue;
				}
				if (secondCorrectlyAssigned) {
					if (shouldBeSameFolder) {
						deltaMat.put(ludRul, ludRul2, 0.7);
						deltaMat.put(ludRul2, ludRul, 0.7);
					}else {
						deltaMat.put(ludRul, ludRul2, 1.0);
						deltaMat.put(ludRul2, ludRul, 1.0);
					}
					continue;
				}
				if (shouldBeSameFolder) {
					deltaMat.put(ludRul, ludRul2, 0.3);
					deltaMat.put(ludRul2, ludRul, 0.3);
				}else {
					deltaMat.put(ludRul, ludRul2, 0.2);
					deltaMat.put(ludRul2, ludRul, 0.2);
				}
			}
		}
		return deltaMat;
	}

	@SuppressWarnings("unchecked")
	public static DistanceMatrix<LudRul, LudRul> getDistanceMatrix(
			final ArrayList<LudRul> candidates, final DistanceMetric metric, final boolean forceRecalc, final DistanceProgressListener progressListener
	)
	{
		final String name = metric.getName() + "_"
				+ candidates.size() + "_" + candidates.hashCode() + ".dist";
		final String filePath = FolderLocations.resourceMetricsFolder.getAbsolutePath()
				+ "/" + name;
		final DistanceMatrix<LudRul, LudRul> dm;
		Object object = null;
		if (!forceRecalc)
			object = DistanceUtils.deserialise(filePath);
		if (object != null && object instanceof DistanceMatrix<?, ?>)
			dm = (DistanceMatrix<LudRul, LudRul>) object;
		else
		{
			dm = getFilledDistanceMatrix(metric, candidates,progressListener);
			DistanceUtils.serialise(dm, filePath, false);
		}
		return dm;
	}

	public static AssignmentStats getAssignmentStats(
			final ArrayList<LudRul> sortedCandidates,
			final AssignmentSettings ass,
			final HashMap<LudRul, String> classAssignment
	)
	{
		final AssignmentStats as = new AssignmentStats(sortedCandidates,ass.getClassGroundTruth(),classAssignment);
		return as;
	}

	public static DistanceMatrix<LudRul, LudRul> getClassAssigned(
			final ArrayList<LudRul> candidates, final DistanceMatrix<LudRul, LudRul> cm,
			final DistanceMatrix<LudRul, LudRul> assigned,
			final HashMap<LudRul, String> classAssignment
	)
	{
		final DistanceMatrix<LudRul, LudRul> classMat = new DistanceMatrix<>(
				candidates, candidates);
		final HashSet<String> folderClasses = new HashSet<>();
		final HashMap<String,Double> classToColor = new HashMap<>();
		for (final LudRul ludRul : candidates)
		{
			folderClasses.add(ludRul.getCurrentClassName());
		}
		
		final ArrayList<String> order = new ArrayList<>(folderClasses);
		Collections.shuffle(order, new Random(0));
		
		final double stepSize = 1.0/(folderClasses.size()+3);
		final Iterator<String> it = order.iterator();
		double step = 0;
		for (step = stepSize; it.hasNext();step+=stepSize) {
			classToColor.put(it.next(), Double.valueOf(step));
		}
		step+=stepSize;
		
		
		
		for (int i = 0; i < candidates.size(); i++)
		{
			final LudRul ludRul = candidates.get(i);
			final String folder1 = ludRul.getCurrentClassName();
			final String assigned1 = classAssignment.get(ludRul);
			final boolean firstCorrectlyAssigned = folder1.equals(assigned1);
			for (int j = i; j < candidates.size(); j++)
			{
				final LudRul ludRul2 = candidates.get(j);
				final String folder2 = ludRul2.getCurrentClassName();
				final String assigned2 = classAssignment.get(ludRul2);
				final boolean secondCorrectlyAssigned = folder2.equals(assigned2);
				final boolean shouldBeSameFolder = folder2.equals(folder1);
				final boolean areSameFolderAssigned = assigned1.equals(assigned2);
				if (shouldBeSameFolder&&areSameFolderAssigned) {
					if (firstCorrectlyAssigned) {
						classMat.put(ludRul, ludRul2, classToColor.get(folder1).doubleValue());
						classMat.put(ludRul2, ludRul, classToColor.get(folder1).doubleValue());
					}else {
						classMat.put(ludRul, ludRul2, classToColor.get(assigned1).doubleValue());
						classMat.put(ludRul2, ludRul, classToColor.get(assigned1).doubleValue());
					}
					
				}
				if ((!shouldBeSameFolder&&areSameFolderAssigned)||(shouldBeSameFolder&&!areSameFolderAssigned)) {
					if (firstCorrectlyAssigned) {
						classMat.put(ludRul, ludRul2,  classToColor.get(assigned2).doubleValue());
						classMat.put(ludRul2, ludRul,  classToColor.get(assigned2).doubleValue());
					}else if (secondCorrectlyAssigned) {
						classMat.put(ludRul, ludRul2,  classToColor.get(assigned1).doubleValue());
						classMat.put(ludRul2, ludRul,  classToColor.get(assigned1).doubleValue());
					}else {
						classMat.put(ludRul, ludRul2, step);
						classMat.put(ludRul2, ludRul, step);
					}	
				}
				if (!shouldBeSameFolder&&!areSameFolderAssigned) {
					classMat.put(ludRul, ludRul2, 1.0);
					classMat.put(ludRul2, ludRul, 1.0);
				}
			}
		}
		return classMat;
	}

	public static DistanceMatrix<LudRul, LudRul> getLineAdjustedDistance(
			final ArrayList<LudRul> candidates, final DistanceMatrix<LudRul, LudRul> dm
	)
	{
		final DistanceMatrix<LudRul, LudRul> lineAdjusted = new DistanceMatrix<>(candidates, candidates);
		
		final double[][] target = lineAdjusted.getDistanceMatrix();
		final double[][] src = dm.getDistanceMatrix();
		for (int i = 0; i < candidates.size(); i++)
		{
			final double[] lineSrc = src[i];
			final double[] lineTarget = target[i];
			double mamimum = Integer.MIN_VALUE;
			double minimum = Integer.MAX_VALUE;
			for (int j = 0; j < candidates.size(); j++)
			{
				if (lineSrc[j]<minimum)minimum=lineSrc[j];
				if (lineSrc[j]>mamimum)mamimum=lineSrc[j];
			}
			final double deltaInv = 1.0/(mamimum-minimum);
			for (int j = 0; j < candidates.size(); j++)
			{
				final double srcVal = lineSrc[j];
				lineTarget[j] = (srcVal-minimum)*deltaInv;
			}
		}
		
		return lineAdjusted;
	}

	@SuppressWarnings("unchecked")
	public static DistanceMatrix<LudRul, LudRul> loadDistanceMatrix(
			final ArrayList<LudRul> candidates,
			final DistanceMetric metric
	)
	{
		final String name = metric.getClass().getName() + "_"
				+ candidates.size() + "_" + candidates.hashCode() + ".dist";
		final String filePath = FolderLocations.resourceMetricsFolder.getAbsolutePath()
				+ "/" + name;
		Object object = null;
		
			object = DistanceUtils.deserialise(filePath);
		if (object != null && object instanceof DistanceMatrix<?, ?>)
			return (DistanceMatrix<LudRul, LudRul>) object;
		return null;
	}

}
