package games;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

import game.Game;
import game.equipment.container.board.Board;
//import game.util.graph.Graph;
import other.GameLoader;
import other.topology.Cell;
import other.topology.Vertex;
//import game.util.graph.Vertex;
//import game.util.graph.Face;

//-----------------------------------------------------------------------------

/**
 * Check board distance metric.
 * 
 * @author cambolbro
 */
public class BoardDistanceTest
{
	@Test
	@SuppressWarnings("static-method")
	public void test()
	{
		final String[] gameNames = 
		{ 
			"Chess.lud",
			"Halma.lud",
			"Tic-Tac-Toe.lud",
			"Mu Torere.lud",
			"International Draughts.lud",
		};
		
		for (int a = 0; a < gameNames.length - 1; a++)
		{
			final Game gameA = GameLoader.loadGameFromName(gameNames[a]);
			final Board boardA = gameA.board();
			
			for (int b = a + 1; b < gameNames.length; b++)
			{
				final Game gameB = GameLoader.loadGameFromName(gameNames[b]);
				final Board boardB = gameB.board();
				
				System.out.println("\nComparing games " + gameNames[a] + " and " + gameNames[b] + "...");
				System.out.println("Game A graph: " + boardA.topology().vertices().size() + " verts.");	
				System.out.println("Game B graph: " + boardB.topology().vertices().size() + " verts.");
				System.out.println("Distance is: " + distance(boardA, boardB));
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	public static double distance(final Board boardA, final Board boardB)
	{
		// Get ordered list of degrees for vertices and faces or each board
		final List<Integer> verticesA = new ArrayList<>();
		final List<Integer> verticesB = new ArrayList<>();

		final List<Integer> cellsA = new ArrayList<>();
		final List<Integer> cellsB = new ArrayList<>();

//		for (final Vertex vertex : boardA.graph().vertices())
//			verticesA.add(Integer.valueOf(vertex.edges().size()));
//
//		for (final Vertex vertex : boardB.graph().vertices())
//			verticesB.add(Integer.valueOf(vertex.edges().size()));
//				
//		for (final Face face : boardA.graph().faces())
//			cellsA.add(Integer.valueOf(face.nbors().size()));
//
//		for (final Face face : boardB.graph().faces())
//			cellsB.add(Integer.valueOf(face.nbors().size()));

		for (final Vertex vertex : boardA.topology().vertices())
			verticesA.add(Integer.valueOf(vertex.orthogonal().size() + vertex.diagonal().size()));  //neighbours().size()));

		for (final Vertex vertex : boardB.topology().vertices())
			verticesB.add(Integer.valueOf(vertex.orthogonal().size() + vertex.diagonal().size()));  //.neighbours().size()));

		for (final Cell cell : boardA.topology().cells())
			cellsA.add(Integer.valueOf(cell.orthogonal().size() + cell.diagonal().size()));  //.neighbours().size()));

		for (final Cell cell : boardB.topology().cells())
			cellsB.add(Integer.valueOf(cell.orthogonal().size() + cell.diagonal().size()));  //.neighbours().size()));

		Collections.sort(verticesA);
		Collections.sort(verticesB);

		Collections.sort(cellsA);
		Collections.sort(cellsB);
		
		// Find closest of va:vb, va:fb, fa:va, fa:fb
		
		//System.out.println("verticesA: " + verticesA);
		//System.out.println("verticesB: " + verticesB);
		//System.out.println("cellsA: " + cellsA);
		//System.out.println("cellsB: " + cellsB);
		
		final double sim_V_V = similarity(verticesA, verticesB);
		final double sim_V_C = similarity(verticesA,    cellsB);
		final double sim_C_V = similarity(   cellsA, verticesB);
		final double sim_C_C = similarity(   cellsA,    cellsB);
		
//		final double wed_V_V = weightedEditDistance(verticesA, verticesB);
		
		final double score = Math.max(Math.max(Math.max(sim_V_V, sim_V_C), sim_C_V), sim_C_C);
		
		return score;
	}
	
	/**
	 * Destructively modifies lists.
	 * @return Similarity measure between two lists, in range 0..1.
	 */
	public static double similarity(final List<Integer> listAin, final List<Integer> listBin)
	{
		System.out.println("listAin: " + listAin);
		System.out.println("listBin: " + listBin);
		
		if (listAin.isEmpty() && listBin.isEmpty())
			return 1;  // perfect match!
		
		final List<Integer> listA = new ArrayList<>(listAin);
		final List<Integer> listB = new ArrayList<>(listBin);
		
//		System.out.println("listA before: " + listA);
//		System.out.println("listB before: " + listB);
		
		// Remove matching items from lists
		for (int a = listA.size() - 1; a >= 0; a--)
		{
			boolean remove = false;			
			for (int b = listB.size() - 1; b >= 0; b--)
			{
				if (listA.get(a) == listB.get(b))
				{
					// Remove this item from each list
					listB.remove(b);
					remove = true;
					break;
				}
			}
			if (remove)
				listA.remove(a);
		}
		
//		System.out.println("listA after: " + listA);
//		System.out.println("listB after: " + listB);
		
		double discrepancy = 0;

		for (final int a : listA)
		{
			if (listB.isEmpty())
			{
				discrepancy += a;
			}
			else
			{
				int minDiff = 1000;
//				for (final int b : listB)
				for (final int b : listBin)
				{	
					final int diff = Math.abs(a - b);
					if (diff < minDiff)
						minDiff = diff;
				}
				discrepancy += minDiff;
			}
		}
		
		for (final int b : listB)
		{
			if (listA.isEmpty())
			{
				discrepancy += b;
			}
			else
			{
				int minDiff = 1000;
//				for (final int a : listA)
				for (final int a : listAin)
				{
					final int diff = Math.abs(a - b);
					if (diff < minDiff)
						minDiff = diff;
				}
				discrepancy += minDiff;
			}
		}
		
		discrepancy /= (listA.size() + listB.size());
		
		final double scoreD = 1 - Math.log10(discrepancy + 1);

//		final List<Integer> listMin = (listA.size() <= listB.size()) ? listA : listB;
//		final List<Integer> listMax = (listA.size() <= listB.size()) ? listB : listA;
//		
//		for (final int a : listMax)
//		{
//			if (listMin.isEmpty())
//			{
//				discrepancy += a;
//			}
//			else
//			{
//				// Find closest match in the other list
//				int minDiff = 1000;
//				for (final int b : listMin)
//				{	
//					final int diff = Math.abs(a - b);
//					if (diff < minDiff)
//						minDiff = diff;
//				}
//				discrepancy += minDiff;
//			}
//		}
//		
//		discrepancy /= listMax.size();
//		
//		final double scoreD = 1 - Math.log10(discrepancy + 1);
//		
//		final double ratio = Math.min(listAin.size(), listBin.size()) 
//							 / 
//							 (double)Math.max(listAin.size(), listBin.size()); 
			
		final int MAX = 8;
		final double[] talliesA = new double[MAX + 1];
		final double[] talliesB = new double[MAX + 1];

		double totalA = 0;
		double totalB = 0;
		
		for (final int n : listAin)
			if (n > MAX) 
				talliesA[MAX]++; 
			else
				talliesA[n]++;
		
		for (final int n : listBin)
			if (n > MAX) 
				talliesB[MAX]++; 
			else
				talliesB[n]++;
		
		for (int n = 0; n < MAX + 1; n++)
		{
			totalA += talliesA[n];
			totalB += talliesB[n];
		}
		
		System.out.print("talliesA:");
		for (int n = 0; n < MAX + 1; n++)
			System.out.print(" " + talliesA[n]);
		System.out.println();
		
		System.out.print("talliesB:");
		for (int n = 0; n < MAX + 1; n++)
			System.out.print(" " + talliesB[n]);
		System.out.println();
		
		System.out.println("totalA=" + totalA + ", totalB=" + totalB + ".");

		totalA = 0;
		totalB = 0;
		for (int n = 0; n < MAX + 1; n++)
		{
			talliesA[n] = Math.log10(talliesA[n] + 1);
			talliesB[n] = Math.log10(talliesB[n] + 1);
			totalA += talliesA[n];
			totalB += talliesB[n];
		}
		
		System.out.print("talliesA:");
		for (int n = 0; n < MAX + 1; n++)
			System.out.print(" " + talliesA[n]);
		System.out.println();
		
		System.out.print("talliesB:");
		for (int n = 0; n < MAX + 1; n++)
			System.out.print(" " + talliesB[n]);
		System.out.println();
		
		System.out.println("totalA=" + totalA + ", totalB=" + totalB + ".");

		final double[] contributionsA = new double[MAX + 1];
		final double[] contributionsB = new double[MAX + 1];
		
		for (int n = 0; n < MAX + 1; n++)
		{
			contributionsA[n] = talliesA[n] / totalA;
			contributionsB[n] = talliesB[n] / totalB;
		}
		
		System.out.print("contributionsA:");
		for (int n = 0; n < MAX + 1; n++)
			System.out.print(String.format(" %.3f", Double.valueOf(contributionsA[n])));
		System.out.println();
		
		System.out.print("contributionsB:");
		for (int n = 0; n < MAX + 1; n++)
			System.out.print(String.format(" %.3f", Double.valueOf(contributionsB[n])));
		System.out.println();
		
		
//		double total = 0;
//		for (int n = 0; n < MAX + 1; n++)
//		{
//			//final int diff = Math.abs(bucketsA[n] - bucketsB[n]);
//			if (bucketsA[n] > 0 || bucketsB[n] > 0)
//				total += Math.min(bucketsA[n], bucketsB[n]) / Math.max(bucketsA[n], bucketsB[n]);
//		}
//		total /= (MAX + 1);
//		
//		final double scoreD = 1 - total;  //Math.log10(total + 1);
		
		final double ratio = Math.min(listAin.size(), listBin.size()) 
							 / 
							 (double)Math.max(listAin.size(), listBin.size()); 
		
		final double score = (scoreD + ratio) / 2.0;
		
		//System.out.println("total=" + total + ", scoreD=" + scoreD + ", ratio=" + ratio + ", score=" + score + ".");
		System.out.println("discrepancy=" + discrepancy + ", scoreD=" + scoreD + ", ratio=" + ratio + ", score=" + score + ".");
				
		return score;
	}

	/**
	 * Destructively modifies lists.
	 * @return Similarity measure between two lists, in range 0..1.
	 */
	public static double weightedEditDistance(final List<Integer> listAin, final List<Integer> listBin)
	{
		if (listAin.isEmpty() && listBin.isEmpty())
			return 1;  // perfect match!
		
		final List<Integer> listA = new ArrayList<>(listAin);
		final List<Integer> listB = new ArrayList<>(listBin);
		
		System.out.println("listA 1: " + listA);
		System.out.println("listB 1: " + listB);
		
		// Remove matching items from lists
		for (int a = listA.size() - 1; a >= 0; a--)
		{
			boolean remove = false;			
			for (int b = listB.size() - 1; b >= 0; b--)
			{
				if (listA.get(a) == listB.get(b))
				{
					// Remove this item from each list
					listB.remove(b);
					remove = true;
					break;
				}
			}
			if (remove)
				listA.remove(a);
		}

		System.out.println("listA 2: " + listA);
		System.out.println("listB 2: " + listB);
		
		// Remove most similar items from list, accumulating difference
		int difference = 0;
		
		final int iterations = Math.min(listA.size(), listB.size());
		
		for (int iteration = 0; iteration < iterations; iteration++)
		{
//			int bestA = -1;  // must be at least one item
//			int bestB = -1;  // must be at least one item
			int minDiff = 1000;
			
			// Find most similar items
			for (final int a : listA)
				for (final int b : listB)
				{
					final int diff = Math.abs(a - b);
					if (diff < minDiff)
					{
//						bestA = a;
//						bestB = b;
						minDiff = diff;
					}
				}
			difference += minDiff;
			
			for (int a = listA.size() - 1; a >= 0; a--)
			{
				boolean remove = false;			
				for (int b = listB.size() - 1; b >= 0; b--)
				{
					if (Math.abs(listA.get(a).intValue() - listB.get(b).intValue()) == minDiff)
					{
						// Remove this item from each list
						listB.remove(b);
						remove = true;
						break;
					}
				}
				if (remove)
				{
					listA.remove(a);
					break;
				}
			}
		}

		System.out.println("listA 3: " + listA);
		System.out.println("listB 3: " + listB);
		
		// Now just accumulate numbers in remaining list
		for (final int n : listA)
			difference += n;
		
		for (final int n : listB)
			difference += n;
		
//		final double totalSize = (listAin.size() + listBin.size());
		final double avgSize = (listAin.size() + listBin.size()) / 2.0;
		
		final double score = 1 - Math.log10(difference / avgSize);  //totalSize);  //10.0);
		
		
		System.out.println("difference=" + difference + ", score=" + score);
		
//		double discrepancy = 0;
//
//		discrepancy /= (listA.size() + listB.size());
//		
//		final double scoreD = 1 - Math.log10(discrepancy + 1);
//		
//		final double ratio = Math.min(listAin.size(), listBin.size()) 
//							 / 
//							 (double)Math.max(listAin.size(), listBin.size()); 
//		
//		final double score = (scoreD + ratio) / 2.0;
//		
//		//System.out.println("total=" + total + ", scoreD=" + scoreD + ", ratio=" + ratio + ", score=" + score + ".");
//		System.out.println("discrepancy=" + discrepancy + ", scoreD=" + scoreD + ", ratio=" + ratio + ", score=" + score + ".");
				
		return score;
	}

	//-------------------------------------------------------------------------
	
}
