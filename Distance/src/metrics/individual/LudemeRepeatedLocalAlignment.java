
package metrics.individual;

import java.util.List;

import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;
import metrics.support.EditCost;

//-----------------------------------------------------------------------------

/**
 * Uses Smith Waterman Alignment, which is a local alignment of ludemes 
 * 
 * @author Markus
 */
public class LudemeRepeatedLocalAlignment implements DistanceMetric
{
	public static final double LOG_2 = Math.log(2);
	final EditCost ec;
	
	public LudemeRepeatedLocalAlignment(final EditCost thisec)
	{
		ec = thisec;
	}

	// -------------------------------------------------------------------------

	public LudemeRepeatedLocalAlignment()
	{
		this(new EditCost(5,-5,-1));
	}

	@Override
	public String getName()
	{
		return "LudemeRepeatedLocalAlignment" + "_" + ec.hit() +"_" + ec.miss() + "_"+ ec.gapPenalty();
	}
	
	@Override
	public Score distance(final LudRul candidate, final LudRul gameToCompareWith)
	{
		return distance(candidate.getDescriptionExpanded(), gameToCompareWith.getDescriptionExpanded());
	}

	@Override
	public Score distance(final String candidate, final String gameToCompareWith)
	{
		if (candidate.equals(gameToCompareWith))return new Score(0);
		final String dataCleanA = cleanString(candidate);
		final String[] wordsA = dataCleanA.split("\\s+");
		final String dataCleanB = cleanString(gameToCompareWith);
		final String[] wordsB = dataCleanB.split("\\s+");
		
		
		
		final double d = repeatedSmithWatermanAlignment(wordsA, wordsB, ec,0);
		final double maxCost = Math.max(wordsA.length, wordsB.length) * ec.hit();
		final double finalScore = 1 - d / maxCost;

		return new Score(finalScore);
	}

	@Override
	public Score distance(final Game gameA, final Game gameB)
	{

		return distance(gameA.description().expanded(), gameB.description().expanded());
	}

	// -------------------------------------------------------------------------

	/**
	 * Determines the cost to align the actions of two trials using the Smith
	 * Waterman Algorithm
	 * {@link "https://en.wikipedia.org/wiki/Smith%E2%80%93Waterman_algorithm"}
	 * 
	 * @param wordsA
	 * @param wordsB
	 * @param editCost
	 * @return
	 */
	@SuppressWarnings("all") //only way I found, to free the parameter without warning
	private int repeatedSmithWatermanAlignment(String[] wordsA, String[] wordsB, final EditCost editCost, final int score)
	{
		//this time around find the maximum value and then backtrack along the biggest numbers to 0
		if (wordsA.length==0||wordsB.length==0) return score;
		int maximumValue = -1;
		int maximumI = 0;
		int maximumJ = 0;
		
		int[][] scoreMat = new int[wordsA.length + 1][wordsB.length + 1];
		for (int i = 1; i < scoreMat.length; i++)
		{
			for (int j = 1; j < scoreMat[0].length; j++)
			{
				final int valueFromLeft = editCost.gapPenalty() + scoreMat[i - 1][j];
				final int valueFromTop = editCost.gapPenalty() + scoreMat[i][j - 1];

				int valueFromTopLeft;
				if (wordsA[i - 1].equals(wordsB[j - 1]))
					valueFromTopLeft = editCost.hit() + scoreMat[i - 1][j - 1];
				else
					valueFromTopLeft = editCost.miss() + scoreMat[i - 1][j - 1];

				final int finalVal = Math.max(0, Math.max(valueFromTopLeft, Math.max(valueFromTop, valueFromLeft)));
				scoreMat[i][j] = finalVal;

				if (finalVal > maximumValue) {
					maximumValue = finalVal;
					maximumI = i;
					maximumJ = j;
				}
					
			}
		}
		final int[] ij = findStartIJfromAllignmentMatrix(scoreMat,maximumI,maximumJ);
		if(maximumValue<editCost.hit()*3) {
			return score+maximumValue;
		}
		if (ij[0]==maximumI ||ij[1]==maximumJ) {
			return score;
		}
		//System.out.println("allignment is from " + ij[0] + "," + ij[1] + " to " + maximumI + "," + maximumJ);
		// DistanceMatrix.printOutAllignmentMatrix(wordsA,wordsB,distances,"allignment.csv");
		scoreMat = null;
		final String[] wordsACut = cutAwayAlligned(wordsA,ij[0]-1,maximumI-1);
		final String[] wordsBCut = cutAwayAlligned(wordsB,ij[1]-1,maximumJ-1);
		//if (wordsACut.length*1.05>wordsA.length&&wordsBCut.length*1.05>wordsB.length)
			//return score+maximumValue;
		wordsA=null; //warning even though im doing it to free memory, in this recursive approach
		wordsB=null;
		return repeatedSmithWatermanAlignment(wordsACut, wordsBCut, editCost, score+maximumValue);
	}

	private static String[] cutAwayAlligned(
			final String[] wordsA, final int minI,  final int maximumI
	)
	{
		final int firstLenght = minI;
		final int tailLength= wordsA.length-maximumI-1;
		final int newLength = minI+tailLength;
		final String[] cutted = new String[newLength];
		//String[] first = new String[0];
		//if (minI!=0) first= Arrays.copyOf(wordsA, minI);
		//final String[] second = new String[tailLength];
		//System.arraycopy(wordsA, maximumI+1, second, 0, tailLength);
		
		System.arraycopy(wordsA, 0, cutted, 0, firstLenght);
	    System.arraycopy(wordsA, maximumI+1,cutted, firstLenght, tailLength);
		return cutted;
	}

	/**
	 * travel along the maximum values starting from this cell
	 * @param scoreMat
	 * @param i
	 * @param j
	 * @return
	 */
	private int[] findStartIJfromAllignmentMatrix(
			final int[][] scoreMat, final int i, final int j
	)
	{
		//as we start in the maximum cell all candidates will be lower or same
		int nextI;
		int nextJ;
		int maxProgenitor;
		
		final int leftUp = scoreMat[i-1][j-1];
		//if (leftUp> maxProgenitor) { 
		maxProgenitor = leftUp;
		nextI = i-1;
		nextJ = j-1;
		//}
		final int up = scoreMat[i][j-1];
		if (up > maxProgenitor) {
			maxProgenitor = up;
			nextI = i;
			nextJ = j-1;
		}
		final int left = scoreMat[i-1][j];
		if (left > maxProgenitor) {
			maxProgenitor = left;
			nextI = i-1;
			nextJ = j;
		}
			
		if (maxProgenitor == 0) {
			return new int[] {i,j};
		}else {
			return findStartIJfromAllignmentMatrix(scoreMat, nextI,nextJ);
		}
		
	}

	/**
	 * @param contentData ...
	 * @return String with just single words and no double spaces.
	 */
	public String cleanString(final String contentData)
	{
		final String data = contentData;
		final String dataAlphabetic = data.replaceAll("[^A-Za-z0-9 ]", " ");

		// Maybe keep numbers, to ???
		// dataAlphabetic = data.replaceAll("[^A-Za-z ]"," ");

		final String dataSingleSpace = dataAlphabetic.trim().replaceAll(" +", " ");
		final String dataClean = dataSingleSpace.toLowerCase();

		return dataClean;
	}

	// -------------------------------------------------------------------------


	@Override
	public Score distance(final Game gameA, final List<Game> gameB, final int numberTrials, final int maxTurns, final double thinkTime, final String AIName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		return new LudemeRepeatedLocalAlignment();
	}

	@Override
	public boolean hasUserSelectionDialog() {
		return true;
	}
	
	@Override
	public DistanceMetric showUserSelectionDialog() {
		final EditCost thisec = EditCost.showUserSelectionDialog();
		return new LudemeRepeatedLocalAlignment(thisec);
		
	}
}
