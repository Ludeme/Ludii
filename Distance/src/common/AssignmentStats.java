package common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;



public class AssignmentStats
{

	private HashMap<String, Quadrupel> classCount;
	private double correctlyAssignedRate;
	private double falslyAssignedRate;
	private int size;
	private int correctlyAssigned;
	private int falslyAssigned;
	DistanceMatrix<String, String> confusionMatrix;
	

	public AssignmentStats(
			final ArrayList<LudRul> candidates,
			final ClassGroundTruth classGroundTruth,
			final HashMap<LudRul, String> classAssignment
	)
	{
		
		final ArrayList<LudRul> cSort = new ArrayList<>(candidates);
		sortByNameAndRuleSet(cSort);

		final HashMap<LudRul, String> groundTruth = classGroundTruth
				.getClass(candidates);
		fillStats(candidates, groundTruth, classAssignment);
		
		
	}

	private static void sortByNameAndRuleSet(final ArrayList<LudRul> cSort)
	{
		Collections.sort(cSort, new Comparator<LudRul>()
		{

			@Override
			public int compare(final LudRul o1, final LudRul o2)
			{
				return o1.getGameNameIncludingOption(false)
						.compareTo(o2.getGameNameIncludingOption(false));
			}
		});
	}

	private void fillStats(
			final ArrayList<LudRul> candidates,
			final HashMap<LudRul, String> groundTruth,
			final HashMap<LudRul, String> classAssignment
	)
	{
		final ArrayList<String> classes = getPossibleClasses(classAssignment,
				groundTruth);
		final HashMap<String, Integer> classesIndex = new HashMap<>(
				classes.size());
		int count = 0;
		for (final Iterator<String> iterator = classes.iterator(); iterator
				.hasNext();)
		{

			final String className = iterator.next();
			classesIndex.put(className, Integer.valueOf(count));
			count++;
		}

		classCount = initClassCount(classes, candidates.size());

		
		
		confusionMatrix = new DistanceMatrix<>(
				classes, classes);
		for (final LudRul candidate : candidates)
		{
			final String target = groundTruth.get(candidate);
			final String assigned = classAssignment.get(candidate);

			if (target.equals(assigned))
			{
				correctlyAssigned++;

				final Quadrupel tq = classCount.get(target);
				tq.addTP();
				tq.addP();

				confusionMatrix.increment(target, target, 1.0);

			} else
			{
				falslyAssigned++;

				final Quadrupel aq = classCount.get(assigned);
				aq.addFP();

				final Quadrupel tq = classCount.get(target);
				tq.addFN();
				tq.addP();

				confusionMatrix.increment(target, assigned, 1.0);

			}
		}

		correctlyAssignedRate = ((double) correctlyAssigned)
				/ candidates.size();
		falslyAssignedRate = ((double) falslyAssigned) / candidates.size();
		size = candidates.size();

		printStats();
		// Correctly Classified Instances 51 14.8688 %
		// Incorrectly Classified Instances 292 85.1312 %
		// TP Rate FP Rate Precision Recall F-Measure MCC ROC Area PRC Area
		// Class

	}

	private void printStats(	
	)
	{
		System.out.println(this.toString());
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("Number of Instances: " + size + "\n");
		sb.append("Correctly Assigned:  " + correctlyAssigned + "\n");
		sb.append("Falsly Assigned:     " + falslyAssigned+ "\n");
		sb.append("Correctly Assigned Rate:  " + String.format("%1.3f ", Double.valueOf(correctlyAssignedRate)) + "\n");
		sb.append("Falsly Assigned Rate:     " + String.format("%1.3f ", Double.valueOf(falslyAssignedRate))+ "\n");
		
		sb.append("\n");
		atteachHeader(sb);
		
		ArrayList<Entry<String, Quadrupel>> sorted = getSortedByP();
		for (final Entry<String, Quadrupel> entry : sorted)
		{

			System.out.println(sb.toString());
			final String classN = entry.getKey();
			final Quadrupel classQ = entry.getValue();
			sb.append(String.format("%-14s ", classN));
			//ystem.out.print(classN + ": \t\t");
		
			print(sb,"P", classQ.getP());
			print(sb,"N", classQ.getN());
			
			print(sb,"TPR", classQ.getTPR());
			print(sb,"TNR", classQ.getTNR());
			
			print(sb,"FPR", classQ.getFPR());
			print(sb,"FNR", classQ.getFNR());

			print(sb,"TP", classQ.getTP());
			print(sb,"FP", classQ.getFP());
			
			print(sb,"TN", classQ.getTN());
			print(sb,"FN", classQ.getFN());

			print(sb,"PPV", classQ.getPPV());
			print(sb,"NPV", classQ.getNPV());
			
			print(sb,"FDR", classQ.getFDR());
			print(sb,"FOR", classQ.getFOR());
			print(sb,"PT", classQ.getPT());
			sb.append("\n");
		}
		
		
		return sb.toString();
	}

	private ArrayList<Entry<String, Quadrupel>> getSortedByP()
	{
		ArrayList<Entry<String, Quadrupel>> sorted = new ArrayList<>(classCount.entrySet());
		Collections.sort(sorted, new Comparator<Entry<String, Quadrupel>>()
		{
			@Override
			public int compare(
					Entry<String, Quadrupel> o1, Entry<String, Quadrupel> o2
			)
			{
				int compare = Integer.compare(o1.getValue().getP(),o2.getValue().getP());
				if (compare!=0)return -compare;
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		return sorted;
	}

	private static void atteachHeader(StringBuilder sb)
	{
		sb.append(String.format("%-13s ", "Genre"));
		
		sb.append(String.format("%4s", "P"));
		
		sb.append(String.format("%4s", "N"));
		
		sb.append(String.format("%6s", "TPR"));
		
		sb.append(String.format("%6s", "TNR"));
		
		sb.append(String.format("%6s", "FPR"));
		
		sb.append(String.format("%6s", "FNR"));
		
		
			
		sb.append(String.format("%4s", "TP"));
		
		sb.append(String.format("%4s", "FP"));
		
		sb.append(String.format("%4s", "TN"));
		
		sb.append(String.format("%4s", "FN"));

		
		sb.append(String.format("%6s", "PPV"));
		
		sb.append(String.format("%6s", "NPV"));
		
		
		
		sb.append(String.format("%6s", "FDR"));
		
		sb.append(String.format("%6s", "FOR"));
		
		sb.append(String.format("%6s", "PT"));
		sb.append("\n");
	}

	

	private static void print(StringBuilder sb,final String name, final double v)
	{
		if (Double.isNaN(v)) {
			sb.append("  NaN ");
			return; 
		}
		String s = String.format("%3.3f ", Double.valueOf(v));
		sb.append(s);
	}
	private static void print(StringBuilder sb,final String name, final int v)
	{
		String s = String.format("%3d ", Integer.valueOf(v));
		sb.append(s);
	}

	private HashMap<String, Quadrupel> initClassCount(
			final ArrayList<String> classes, final int noInstances
	)
	{
		final HashMap<String, Quadrupel> counter = new HashMap<>();
		for (final String className : classes)
		{
			counter.put(className, new Quadrupel(noInstances));
		}
		return counter;
	}

	private static ArrayList<String> getPossibleClasses(
			final HashMap<LudRul, String> classAssignment,
			final HashMap<LudRul, String> groundTruth
	)
	{
		final HashSet<String> classes = new HashSet<>();
		classes.addAll(classAssignment.values());
		classes.addAll(groundTruth.values());
		return new ArrayList<>(classes);
	}

	private class Quadrupel
	{
		private int numberOfInstances = 0;
		int FN = 0;
		private int P = 0;
		private int FP = 0;
		private int TP = 0;

		public Quadrupel(final int numberOfInstances)
		{
			this.numberOfInstances = numberOfInstances;
		}

	

		public double getPT()
		{
			double nominator = getTNR() - 1.0;
			nominator += Math.sqrt(getTPR() * (-getTNR() + 1));
			final double denominator = (getTPR() + getTNR() - 1.0);
			return nominator / denominator;
		}

		public int getTN()
		{
			final int tn = (int) Math.round(getTNR() * getN());
			return tn;
		}

		public double getFDR()
		{
			return 1.0 - getPPV();
		}

		public double getFNR()
		{
			return 1.0 - getTPR();
		}

		public int getTP()
		{
			return TP;
		}

		public double getNPV()
		{
			return 1 - getFOR();
		}

		double getFOR()
		{

			return FN / (double) (FN + getTN());
		}

		public double getPPV()
		{

			return TP / (double) (TP + FP);
		}

		public double getTNR()
		{
			return 1 - getFPR();
		}

		public double getFPR()
		{
			return FP / (double) getN();
		}

		public int getFP()
		{
			return FP;
		}

		public int getFN()
		{
			return FN;
		}

		public double getTPR()
		{
			return TP / (double) P;
		}

		public int getN()
		{
			return numberOfInstances - P;
		}

		public int getP()
		{

			return P;
		}

		public void addFN()
		{
			FN++;
		}

		public void addP()
		{
			P++;

		}

		public void addFP()
		{
			FP++;

		}

		public void addTP()
		{
			TP++;
		}

		public String get(String statisticType)
		{
			switch (statisticType)
			{
			case " ":
				break;
				case "TP": return "" + this.getTP();
				case "FP": return "" + this.getFP();
				
				case "TPR": return "" + this.getTPR();
				case "TNR": return "" + this.getTNR();

				case "P": return "" + this.getP();
				case "N": return "" + this.getN();
				
				case "TN": return "" + this.getTN();
				case "FN": return "" + this.getFN();

				case "PPV": return "" + this.getPPV();
				case "NPV": return "" + this.getNPV();
				case "FNR": return "" + this.getFNR();
				case "FPR": return "" + this.getFPR();
				case "FDR": return "" + this.getFDR();
				case "FOR": return "" + this.getFOR();
				case "PT": return "" + this.getPT();
			default:
				break;
			}
			
			return "";
		}

	}
	public double getCorrectlyAssignedRate()
	{
		return correctlyAssignedRate;
	}

	public double getFalslyAssignedRate()
	{
		return falslyAssignedRate;
	}

	public int getSize() {
		return size;
	}

	public int getCorrectlyAssigned()
	{
		return correctlyAssigned;
	}

	public void exportToCSV(File folder, String fileName)
	{
		List<String> headers = getHeaders();
		ArrayList<List<String>> values = new ArrayList<>();
		ArrayList<Entry<String, Quadrupel>> sorted = getSortedByP();
		for (Entry<String, Quadrupel> arrayList : sorted)
		{
			
			ArrayList<String> line = new ArrayList<>();
			values.add(line);
			line.add(arrayList.getKey());
			Quadrupel quadrupel = arrayList.getValue();
			Iterator<String> it = headers.iterator();
			it.next();
			while (it.hasNext())
			{
				String statisticType = it.next();
				String value = quadrupel.get(statisticType);
				line.add(value);
			}
		}
		
		DistanceUtils.storeAsCsv(headers , values, folder.getAbsolutePath(), fileName + ".csv", ';');
		
	}

	private static List<String> getHeaders()
	{
		ArrayList<String> headers = new ArrayList<>();
		headers.add("Genre");
		
		headers.add("TP");
		
		headers.add("FP");
		
		headers.add("TPR");
		
		headers.add("TNR");
		
		headers.add("FPR");
		
		headers.add("FNR");
		
		headers.add("P");
		
		headers.add("N");
		
		
		
		headers.add("TN");
		
		headers.add("FN");

		
		headers.add("PPV");
		
		headers.add("NPV");
		
	
		
		headers.add("FDR");
		
		headers.add("FOR");
		
		headers.add("PT");
		return headers;
	}

	public static String getLegend()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\n\n\n\n\n\n\n");
		sb.append("    "+String.format("%-4s", "P")+String.format("%30s", "positive")+"\n");
		
		sb.append("    "+String.format("%-4s", "N")+String.format("%30s", "negative")+"\n");
		
		sb.append("    "+String.format("%-4s", "TPR")+String.format("%30s", "true positive rate")+"\n");
		
		sb.append("    "+String.format("%-4s", "TNR")+String.format("%30s", "true negative rate")+"\n");
		
		sb.append("    "+String.format("%-4s", "FPR")+String.format("%30s", "false positive rate")+"\n");
		
		sb.append("    "+String.format("%-4s", "FNR")+String.format("%30s", "false negative rate")+"\n");
		
		
			
		sb.append("    "+String.format("%-4s", "TP")+String.format("%30s", "true positive")+"\n");
		
		sb.append("    "+String.format("%-4s", "FP")+String.format("%30s", "false positive")+"\n");
		
		sb.append("    "+String.format("%-4s", "TN")+String.format("%30s", "true negative")+"\n");
		
		sb.append("    "+String.format("%-4s", "FN")+String.format("%30s", "false negative")+"\n");

		
		sb.append("    "+String.format("%-4s", "PPV")+String.format("%30s", "positive predicitive value")+"\n");
		
		sb.append("    "+String.format("%-4s", "NPV")+String.format("%30s", "negative predictive value")+"\n");
		
		
		sb.append("    "+String.format("%-4s", "FDR")+String.format("%30s", "false discovery rate")+"\n");
		
		sb.append("    "+String.format("%-4s", "FOR")+String.format("%30s", "false omission rate")+"\n");
		
		sb.append("    "+String.format("%-4s", "PT")+String.format("%30s", "prevalence threshold")+"    \n");
		return sb.toString();
	}
	
	public String getConfusionMatrixString() {
		StringBuilder sb = new StringBuilder();
		ArrayList<Entry<String, Quadrupel>> sorted = getSortedByP();
		atteachConfusionHeader(sb,sorted);
		
		
		for (Entry<String, Quadrupel> row : sorted)
		{
			String rowClass = row.getKey();
			sb.append(String.format("%-13s  ", rowClass));
			
			for (Entry<String, Quadrupel> column : sorted) {
				String columnClass = column.getKey();
				int hits = (int) confusionMatrix.get(rowClass, columnClass);
				String hitsString = String.format("%3d ", Integer.valueOf(hits));
				sb.append(hitsString + "  ");
				
			}
			sb.append(String.format("%3d ", Integer.valueOf(row.getValue().getTP()+row.getValue().getFN())));
			sb.append("\n");
			
		}
		sb.append(String.format("%-13s  ", "total"));
		for (Entry<String, Quadrupel> column : sorted) {
			sb.append(String.format("%3d ", Integer.valueOf(column.getValue().getTP()+column.getValue().getFP())));
			sb.append("  ");
		}
		
		return sb.toString();
	}
	
	private static void atteachConfusionHeader(StringBuilder sb, ArrayList<Entry<String,Quadrupel>> sorted)
	{
		sb.append(String.format("%-13s ", "Genre"));
		
		for (Entry<String, Quadrupel> column : sorted) {
			String toWrite = ""; 
			String name = column.getKey();
			if (name.contains("_")&&name.contains("other")) {
				String[] splitted = name.split("_");
				toWrite = splitted[0].substring(0,1) + "_" + splitted[1].substring(0,Math.min(2, splitted[1].length()));
			}
			else toWrite = name.substring(0,Math.min(4,name.length()));
			sb.append(String.format("%4s  ", toWrite));
			
		}
		sb.append(String.format("%4s  ", "tot"));
		sb.append("\n");
		
		
	}
}
