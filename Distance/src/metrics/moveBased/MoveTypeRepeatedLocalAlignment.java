
package metrics.moveBased;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import common.DistanceUtils;
import common.LudRul;
import common.Score;
import game.Game;
import metrics.DistanceMetric;
import metrics.MoveBased;
import metrics.suffix_tree.Letteriser;
import metrics.support.EditCost;
import other.action.Action;
import other.move.Move;
import other.trial.Trial;

//-----------------------------------------------------------------------------

/**
 * Uses Smith Waterman Alignment, which is a local alignment of ludemes 
 * 
 * @author Markus
 */
public class MoveTypeRepeatedLocalAlignment implements DistanceMetric, MoveBased
{
	final int numPlayouts;
	final int numMaxMoves;
	final EditCost ec;
	@SuppressWarnings("unused")
	private final Letteriser letteriser;

	static HashMap<LudRul, String[]> storedTrials = new HashMap<>();
	
	public MoveTypeRepeatedLocalAlignment(
			final Letteriser letteriser, final int numPlayouts, final int numMaxMoves, final EditCost ec
	)
	{
		this.letteriser = letteriser;
		this.numPlayouts = numPlayouts;
		this.numMaxMoves = numMaxMoves;
		this.ec = ec;
	}

	public MoveTypeRepeatedLocalAlignment()
	{
		this(Letteriser.lowRes,10,20,new EditCost());
	}

	@Override
	public String getName()
	{
		return "MoveTypeRepeatedLocalAlignment" + "_" + numPlayouts + "_" + numMaxMoves + "_" + ec.hit() +"_" + ec.miss() + ec.gapPenalty();
	}

	@Override
	public void releaseResources()
	{
		storedTrials.clear();
	}

	@Override
	public Score distance(final LudRul candidate, final LudRul gameToCompareWith)
	{
		String[] trialsA = storedTrials.get(candidate);
		if (trialsA == null)
		{
			final Trial[] actualTrialsA = DistanceUtils.generateRandomTrialsFromGame(candidate.getGame(), numPlayouts, numMaxMoves);
			trialsA = getWordsFromTrials(actualTrialsA);
			storedTrials.put(candidate, trialsA);
		}
		String[] trialsB = storedTrials.get(gameToCompareWith);
		if (trialsB == null)
		{
			final Trial[] actualTrialsB = DistanceUtils.generateRandomTrialsFromGame(candidate.getGame(), numPlayouts, numMaxMoves);
			trialsB = getWordsFromTrials(actualTrialsB);
			storedTrials.put(gameToCompareWith, trialsB);
		}

		return distance(trialsA, trialsB);
	}

	@Override
	public Score distance(final Game gameA, final Game gameB)
	{

		final Trial[] trialsA = DistanceUtils.generateRandomTrialsFromGame(gameA, numPlayouts, numMaxMoves);
		final Trial[] trialsB = DistanceUtils.generateRandomTrialsFromGame(gameB, numPlayouts, numMaxMoves);
		final String[] actualTrialsA = getWordsFromTrials(trialsA);
		final String[] actualTrialsB = getWordsFromTrials(trialsB);
		
		return distance(actualTrialsA, actualTrialsB);
	}

	public Score distance(final String[] wordsA, final String[] wordsB)
	{

		final double d = repeatedSmithWatermanAlignment(wordsA, wordsB, ec,0);
		final double maxCost = Math.max(wordsA.length, wordsB.length) * ec.hit();
		final double finalScore = 1 - d / maxCost;

		return new Score(finalScore);
		

	}

	public static String[] getWordsFromTrials(final Trial trial)
	{
		final Trial[] trials = {trial};
		return getWordsFromTrials(trials) ;
	}
	public static String[] getWordsFromTrials(final Trial[] trials)
	{
		final ArrayList<String> words = new ArrayList<>(trials.length*100);
		for (final Trial trial : trials)
		{
			final List<Move> moveList = trial.generateCompleteMovesList();
			for (final Move m : moveList)
			{
				for (final Action a : m.actions())
				{
					final Class<? extends Action> className = a.getClass();
					final String word = className.toString();

					words.add(word);

				}
			}
		}
		
		String[] wordArray = new String[words.size()];
		wordArray = words.toArray(wordArray);
		return wordArray;
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
	 */@SuppressWarnings("all") //only way to remove all
	private int repeatedSmithWatermanAlignment(String[] wordsA, String[] wordsB, final EditCost editCost, final int score)
	{
		//this time around find the maximum value and then backtrack along the biggest numbers to 0
		if (wordsA.length==0||wordsB.length==0) return score;
		int maximumValue = -1;
		int maximumI = 0;
		int maximumJ = 0;
		
		final int[][] scoreMat = new int[wordsA.length + 1][wordsB.length + 1];
		for (int i = 1; i < scoreMat.length; i++)
		{
			final int[] leftColumn = scoreMat[i - 1];
			final int[] currentColumn = scoreMat[i];
			final String currentWord = wordsA[i - 1];
			
			for (int j = 1; j < scoreMat[0].length; j++)
			{
				
				final int valueFromLeft = editCost.gapPenalty() + leftColumn[j];
				
				final int valueFromTop = editCost.gapPenalty() + currentColumn[j - 1];

				int valueFromTopLeft;
				if (currentWord.equals(wordsB[j - 1]))
					valueFromTopLeft = editCost.hit() + leftColumn[j - 1];
				else
					valueFromTopLeft = editCost.miss() + leftColumn[j - 1];

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
		System.out.println("allignment is from " + ij[0] + "," + ij[1] + " to " + maximumI + "," + maximumJ + "  sumVal:" + (score+maximumValue) + " curVal:" + maximumValue);
		//DistanceMatrix.printOutAlignmentMatrix(wordsA,wordsB,scoreMat);
		
		final String[] wordsACut = cutAwayAlligned(wordsA,ij[0]-1,maximumI-1);
		final String[] wordsBCut = cutAwayAlligned(wordsB,ij[1]-1,maximumJ-1);
		wordsA=null;
		wordsB=null;
		return repeatedSmithWatermanAlignment(wordsACut, wordsBCut, editCost, score+maximumValue);
	}

	private static String[] cutAwayAlligned(
			final String[] wordsA, final int minI,  final int maximumI
	)
	{
		
		final int frontLength = minI;
		final int tailLength= wordsA.length-maximumI-1;
		final int newLength = frontLength+tailLength;
		final String[] cutted = new String[newLength];
		
		System.arraycopy(wordsA, 0, cutted, 0, frontLength);
	    System.arraycopy(wordsA, maximumI+1, cutted, frontLength, tailLength);
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

	

	@Override
	public Score distance(final Game gameA, final List<Game> gameB, final int numberTrials, final int maxTurns, final double thinkTime, final String AIName)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Score distance(final String description1, final String description2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DistanceMetric getDefaultInstance()
	{
		return new MoveTypeRepeatedLocalAlignment();
	}
	
	@Override
	public boolean hasUserSelectionDialog() {
		return true;
	}

	@Override
	public DistanceMetric showUserSelectionDialog()
	{
		final MoveBased.Inits inits = MoveBased.showUserPlayoutAndMaxMovesSettings();
		final EditCost ecNew = EditCost.showUserSelectionDialog();
		return new MoveTypeRepeatedLocalAlignment(inits.letteriser, inits.numPlayouts, inits.numMaxMoves,ecNew);
	}

}
