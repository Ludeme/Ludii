package search.minimax;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TLongArrayList;
import main.collections.FVector;
import main.collections.FastArrayList;
import metadata.ai.heuristics.Heuristics;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.state.State;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicy;
import utils.data_structures.ScoredIndex;

/**
 * AI based on Unbounded Best-First Search, using trained action evaluations to complete the heuristic scores.
 * [...]
 * 
 * @author cyprien
 */

public class LazyUBFM extends UBFM
{
	
	/** Weight of the action evaluation when linearly combined with the heuristic score */
	private static float actionEvaluationWeight = 0.1f;
		
	/** Set to true to record analyticData */
	public boolean performAnalysis = false;

	protected String dataSaveAdress = "analytic_data/default.sav";
	
	//-------------------------------------------------------------------------

	/** A learned policy to use in for the action evaluation */
	protected SoftmaxPolicy learnedSelectionPolicy = null; 
	
	/** A boolean to know if it is the first turn the AI is playing on this game. If so, it will just use
	 *  a basic BFS approach to have an idea of the heuristics range. */
	boolean firstTurn;
	
	/** Different fields to have an idea of how to combine action evaluations and heuristic scores properly */
	float estimatedHeuristicScoresRange;
	float maxActionLogit = Float.NEGATIVE_INFINITY;
	float minActionLogit = Float.POSITIVE_INFINITY;
	float estimatedActionLogitRange;
	float actionLogitSum;
	float actionLogitComputations;
	float estimatedActionLogitMean;
	
	//-------------------------------------------------------------------------
	
	/** Variables not actually used for the reasoning: */

	/** Recording the values assosciated to the actions encountered */
	protected TFloatArrayList actionEvaluations;
	
	/** Recording the difference in the heuristic evaluation as consequence of the possible moves */
	protected TFloatArrayList scoreEvolutions;
	
	/** Recording the correlation coefficient between the ranking according to the action evaluations and the ranking according to the heuristic scores of the resulting states */
	protected TFloatArrayList rankingCorrelations;
	
	/** Comparative datas regarding action evaluations and score evolutions that will be written in a file */
	protected StringBuffer analyticData = new StringBuffer("[]");
	
	
	//-------------------------------------------------------------------------
	
	public static LazyUBFM createLazyUBFM ()
	{
		return new LazyUBFM();
	}
	
	/**
	 * Constructor:
	 */
	public LazyUBFM ()
	{
		super();
		setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(0f));
		friendlyName = "Lazy UBFM";
		return;
	}
	
	/**
	 * Constructor
	 * @param heuristics
	 */
	public LazyUBFM(final Heuristics heuristics)
	{
		super(heuristics);
		setLearnedSelectionPolicy(new SoftmaxFromMetadataSelection(0f));
		friendlyName = "Lazy UBFM";
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Move selectAction
	(
		final Game game, 
		final Context context, 
		final double maxSeconds,
		final int maxIterations,
		final int maxDepth
	)
	{
		
		actionEvaluations = new TFloatArrayList();
		scoreEvolutions = new TFloatArrayList();
		rankingCorrelations = new TFloatArrayList();
		
		final Move bestMove = super.selectAction(game, context, maxSeconds, maxIterations, maxDepth);
		
		if (performAnalysis)
		{
			System.out.println(analyticObservations());
			try {
			   FileWriter myWriter = new FileWriter("/home/cyprien/Documents/M1/Internship/"+dataSaveAdress);
			   myWriter.write(analyticData.toString());
			   myWriter.close();
			   System.out.println("Successfully wrote to the file.");
			} catch (IOException e)
			{
			   System.out.println("An error occurred.");
			   e.printStackTrace();
			}
		};
		
		firstTurn = false;
		
		estimatedHeuristicScoresRange = maxHeuristicEval - minHeuristicEval;
		estimatedActionLogitRange = maxActionLogit - minActionLogit;
		estimatedActionLogitMean = actionLogitSum / actionLogitComputations;
		
		return bestMove;
	}
	
	@Override
	protected FVector estimateMoveValues
	(
		final FastArrayList<Move> legalMoves,
		final Context context,
		final int maximisingPlayer,
		final TLongArrayList nodeHashes,
		final int depth,
		final long stopTime
	)
	{

		final State state = context.state();
		final int mover = state.playerToAgent(state.mover());
		final Game game = context.game();
		
		final float heuristicScore = getContextValue(context, maximisingPlayer, nodeHashes, mover);
		
		if (savingSearchTreeDescription)
			searchTreeOutput.append("("+stringOfnodeHashes(nodeHashes)+","+Float.toString(heuristicScore)+","+((mover==maximisingPlayer)? 1:2)+"),\n");
		
		final int numLegalMoves = legalMoves.size();
		final FVector moveScores = new FVector(numLegalMoves);
	
		for (int i = 0; i < numLegalMoves; ++i)
		{
			final Move m = legalMoves.get(i);
			
			final float actionValue = (float) learnedSelectionPolicy.computeLogit(context, m);
			
			actionLogitSum += actionValue;
			actionLogitComputations += 1;
			maxActionLogit = Math.max(actionValue, maxActionLogit);
			minActionLogit = Math.min(actionValue, minActionLogit);
			
			moveScores.set(i, actionValue);
		};
		
		if (performAnalysis && !(wantsInterrupt || (System.currentTimeMillis() >= stopTime)))
		{
			final FVector moveHeuristicScores = new FVector(numLegalMoves);
			
			for (int i = 0; i < numLegalMoves; ++i)
			{
				final Move m = legalMoves.get(i);
				
				final Context contextCopy = new TempContext(context);
				game.apply(contextCopy, m);
				final float nextHeuristicScore = getContextValue(contextCopy, maximisingPlayer, nodeHashes, mover);
				
				moveHeuristicScores.set(i, nextHeuristicScore);
				
				scoreEvolutions.add(normalise(nextHeuristicScore)-normalise(heuristicScore));
				actionEvaluations.add((mover==maximisingPlayer)? moveScores.get(i): -moveScores.get(i));
			};
			
			if (numLegalMoves > 1)
			{
				final FastArrayList<Integer> tempMovesListIndices = new FastArrayList<Integer>(legalMoves.size());
				for (int i=0; i<numLegalMoves; i++)
				{
					tempMovesListIndices.add(i);
				}
				final int[] sortedMoveIndices = new int[numLegalMoves];
				for (int i=0; i<numLegalMoves; i++)
				{
					sortedMoveIndices[i] = tempMovesListIndices.removeSwap(ThreadLocalRandom.current().nextInt(tempMovesListIndices.size()));
				}
				
				final List<ScoredIndex> evaluatedMoveIndices = new ArrayList<ScoredIndex>(numLegalMoves);
				final List<ScoredIndex> scoredMoveIndices = new ArrayList<ScoredIndex>(numLegalMoves);
				
				for (int i=0; i<numLegalMoves; i++)
				{
					evaluatedMoveIndices.add(new ScoredIndex(i, moveScores.get(i)));
					scoredMoveIndices.add(new ScoredIndex(i, moveHeuristicScores.get(i)));
				}
				
				if (mover==maximisingPlayer)
					Collections.sort(scoredMoveIndices);
				else
					Collections.sort(scoredMoveIndices, Collections.reverseOrder());
				
				Collections.sort(evaluatedMoveIndices);
				
				float covarianceSum = 0f;
				float varianceSum1 = 0f;
				float varianceSum2 = 0f;
				
				for (float rank1=0; rank1<numLegalMoves; rank1++)
				{
					// rank variables must be float for the calculations
					int i = scoredMoveIndices.get((int) rank1).index;
					float rank2 = -1;
					for (int j=0; j<numLegalMoves; j++)
					{
						if (evaluatedMoveIndices.get(j).index == i)
							rank2 = j;
					}
					assert rank2 != -1f;
					
					covarianceSum += (rank1/(numLegalMoves-1)-0.5)*(rank2/(numLegalMoves-1)-0.5);
					
					varianceSum1 += Math.pow((rank1/(numLegalMoves-1)-0.5), 2);
					varianceSum2 += Math.pow((rank2/(numLegalMoves-1)-0.5), 2);
					
					//System.out.println(covarianceSum);
				}
				
				float correlation = (float) (covarianceSum / Math.sqrt(varianceSum1*varianceSum2));
				
				rankingCorrelations.add(correlation);
			}
		}
		
		if (firstTurn)
			// Uses the classical UBFM approach on the first turn.
			return super.estimateMoveValues(legalMoves, context, maximisingPlayer, nodeHashes, depth, stopTime);
		else
		{
			final int sign = (maximisingPlayer == mover)? 1 : -1 ;
				
			for (int i=0; i<numLegalMoves; i++)
			{
				final double r = Math.random(); // just for occasional display
				if (debugDisplay)
					if (r<0.05)
						System.out.printf("action score is %.6g and heuristicScore is %.6g ",moveScores.get(i),heuristicScore);
				
				// (*2 because the maximal gap is half of the range)
				moveScores.set(i, (actionEvaluationWeight * (moveScores.get(i)-estimatedActionLogitMean) * sign * estimatedHeuristicScoresRange * 2)  /  estimatedActionLogitRange);
				moveScores.set(i,moveScores.get(i)  +  heuristicScore);
				
				if (debugDisplay)
					if (r<0.05)
						System.out.printf("-> eval is %.6g\n",moveScores.get(i));
			}
		
			return moveScores;
		}
	}
	
	private float normalise(float heuristicScore) // just for display
	{
		
		float maxValue = 5f ; //FIXME
		float minValue = -5f ;
		
		if (heuristicScore > maxValue)
			return maxValue;
		else if (heuristicScore < minValue)
			return minValue;
		else
			return heuristicScore;
	}

	//-------------------------------------------------------------------------
	
	public void initAI(final Game game, final int playerID)
	{
		super.initAI(game, playerID);
		
		// Instantiate feature sets for selection policy
		if (learnedSelectionPolicy != null)
			learnedSelectionPolicy.initAI(game, playerID);
		
		firstTurn = true;
		actionLogitComputations = 0;
		actionLogitSum = 0;
		maxActionLogit = Float.NEGATIVE_INFINITY;
		minActionLogit = Float.POSITIVE_INFINITY;
		
		return;
	}
	
	public String analyticObservations()
	{
		final StringBuffer res = new StringBuffer();
		
		final int nbEntries = scoreEvolutions.size();
		
		assert nbEntries == actionEvaluations.size();
		
		float actionEvaluationsSum = 0f;
		float actionEvaluationsAbsSum = 0f;
		float scoreEvolutionsSum = 0f;
		float scoreEvolutionsAbsSum = 0f;
		float rankingCorrelationSum = 0f;
		
		for (int i=0; i<nbEntries; i++)
		{
			actionEvaluationsSum += actionEvaluations.get(i);
			scoreEvolutionsSum += scoreEvolutions.get(i);
			actionEvaluationsAbsSum += Math.abs(actionEvaluations.get(i));
			scoreEvolutionsAbsSum += Math.abs(scoreEvolutions.get(i));
		}
		for (int i=0; i<rankingCorrelations.size(); i++)
		{
			rankingCorrelationSum += rankingCorrelations.get(i);
		}
		
		float actionEvaluationsMean = actionEvaluationsSum/nbEntries;
		float scoreEvolutionsMean = scoreEvolutionsSum/nbEntries;
		
		float rankingCorrelationMean = rankingCorrelationSum/rankingCorrelations.size();
		
		float covarianceSum = 0f;
		float actionEvaluationsVarianceSum = 0f;
		float scoreEvolutionsVarianceSum = 0f;

		for (int i=0; i<nbEntries; i++)
		{
			covarianceSum += (actionEvaluations.get(i)-actionEvaluationsMean)*(scoreEvolutions.get(i)-scoreEvolutionsMean);
			actionEvaluationsVarianceSum += Math.pow((actionEvaluations.get(i)-actionEvaluationsMean),2);
			scoreEvolutionsVarianceSum += Math.pow((scoreEvolutions.get(i)-scoreEvolutionsMean),2);
		};
		
		float covariance = covarianceSum / nbEntries;
		float actionEvaluationsVariance = actionEvaluationsVarianceSum / nbEntries;
		float scoreEvolutionsVariance = scoreEvolutionsVarianceSum / nbEntries;
		float correlationCoeficient = (float) (covariance / Math.sqrt(actionEvaluationsVariance*scoreEvolutionsVariance));
		
		res.append("\nNumber of actionEvaluations entries: ");
		res.append(Integer.toString(nbEntries));
		res.append("\nAverage abs action evaluation: ");
		res.append(Float.toString(actionEvaluationsAbsSum/nbEntries));
		res.append("\nAverage abs score evolution: ");
		res.append(Float.toString(scoreEvolutionsAbsSum/nbEntries));
		res.append("\nCorrelation coeficient: ");
		res.append(Float.toString(correlationCoeficient));
		res.append("\nRatio: ");
		res.append(Float.toString(scoreEvolutionsAbsSum/actionEvaluationsAbsSum));
		res.append("\nRanking correlation: ");
		res.append(Float.toString(rankingCorrelationMean));
		
		analyticData.deleteCharAt(analyticData.length()-1);
		analyticData.append(
							"["+Float.toString(actionEvaluationsMean)+
							","+Float.toString(scoreEvolutionsMean)+
							","+Float.toString(correlationCoeficient)+
							","+Float.toString(rankingCorrelationMean)+"],\n]"
							);
		
		return res.toString();
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.isStochasticGame())
			return false;
		
		if (game.hiddenInformation())
			return false;
		
		if (game.hasSubgames())		// Cant properly init most heuristics
			return false;
		
		if (!(game.isAlternatingMoveGame()))
			return false;
		
		return ((game.metadata().ai().features() != null) || (game.metadata().ai().trainedFeatureTrees() != null));
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Sets the learned policy to use in Selection phase
	 * @param policy The policy.
	 */
	public void setLearnedSelectionPolicy(final SoftmaxPolicy policy)
	{
		learnedSelectionPolicy = policy;
	}
	
	/**
	 * Sets the weight of the action evaluation in the context evaluations.
	 * @param value the weight
	 */
	public void setActionEvaluationWeight(final float value)
	{
		actionEvaluationWeight = value;
	}

}
