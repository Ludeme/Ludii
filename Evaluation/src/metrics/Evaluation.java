package metrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import metrics.designer.IdealDuration;
import metrics.multiple.MultiMetricFramework.MultiMetricValue;
import metrics.multiple.metrics.BoardSitesOccupied;
import metrics.multiple.metrics.BranchingFactor;
import metrics.multiple.metrics.DecisionFactor;
import metrics.multiple.metrics.Drama;
import metrics.multiple.metrics.MoveDistance;
import metrics.multiple.metrics.MoveEvaluation;
import metrics.multiple.metrics.PieceNumber;
import metrics.multiple.metrics.ScoreDifference;
import metrics.multiple.metrics.StateEvaluationDifference;
import metrics.single.boardCoverage.BoardCoverageDefault;
import metrics.single.boardCoverage.BoardCoverageFull;
import metrics.single.boardCoverage.BoardCoverageUsed;
import metrics.single.complexity.DecisionMoves;
import metrics.single.complexity.GameTreeComplexity;
import metrics.single.complexity.StateSpaceComplexity;
import metrics.single.duration.DurationActions;
import metrics.single.duration.DurationMoves;
import metrics.single.duration.DurationTurns;
import metrics.single.outcome.AdvantageP1;
import metrics.single.outcome.Balance;
import metrics.single.outcome.Completion;
import metrics.single.outcome.Drawishness;
import metrics.single.outcome.OutcomeUniformity;
import metrics.single.outcome.Timeouts;
import metrics.single.stateEvaluation.LeadChange;
import metrics.single.stateEvaluation.Stability;
import metrics.single.stateEvaluation.clarity.ClarityNarrowness;
import metrics.single.stateEvaluation.clarity.ClarityVariance;
import metrics.single.stateEvaluation.decisiveness.DecisivenessMoves;
import metrics.single.stateEvaluation.decisiveness.DecisivenessThreshold;
import metrics.single.stateRepetition.PositionalRepetition;
import metrics.single.stateRepetition.SituationalRepetition;
import other.concept.Concept;

//-----------------------------------------------------------------------------

/**
 * Access point for evaluation functionality.
 * 
 * @author cambolbro and matthew.stephenson
 */
public class Evaluation
{
	public static final int MAX_ENTRIES = (int) Math.pow(2, 20);
	
	// Cached state evaluations
	private final LinkedHashMap<Long, Double> stateEvaluationCache = new LinkedHashMap<Long, Double>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(final Map.Entry<Long, Double> eldest) 
		{
			return size() > MAX_ENTRIES;
	    }
	};
	
	private final LinkedHashMap<Long, Double> stateAfterMoveEvaluationCache = new LinkedHashMap<Long, Double>()
	{
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(final Map.Entry<Long, Double> eldest) 
		{
			return size() > MAX_ENTRIES;
		}
	};
	
	private final List<Metric> dialogMetrics = new ArrayList<>();
	{
		// Outcome
		dialogMetrics.add(new AdvantageP1());
		dialogMetrics.add(new Balance());
		dialogMetrics.add(new Completion());
		dialogMetrics.add(new Drawishness());
		dialogMetrics.add(new Timeouts());
		
		// Other
		dialogMetrics.add(new BoardCoverageDefault());
		dialogMetrics.add(new DecisivenessMoves());
		
		// Designer
		dialogMetrics.add(new IdealDuration());
	}
	
	//-------------------------------------------------------------------------
	
	private final List<Metric> reconstructionMetrics = new ArrayList<>();
	{
		reconstructionMetrics.add(new DurationTurns());
		reconstructionMetrics.add(new DecisionMoves());
		reconstructionMetrics.add(new BoardCoverageDefault());
		reconstructionMetrics.add(new AdvantageP1());
		reconstructionMetrics.add(new Balance());
		reconstructionMetrics.add(new Completion());
		reconstructionMetrics.add(new Drawishness());
		reconstructionMetrics.add(new OutcomeUniformity());
		
		reconstructionMetrics.add(new PieceNumber(MultiMetricValue.Average, Concept.PieceNumberAverage));
		reconstructionMetrics.add(new BoardSitesOccupied(MultiMetricValue.Average, Concept.BoardSitesOccupiedAverage));
		reconstructionMetrics.add(new BranchingFactor(MultiMetricValue.Average, Concept.BranchingFactorAverage));
		reconstructionMetrics.add(new MoveDistance(MultiMetricValue.Average, Concept.DecisionFactorAverage));
	}
	
	//-------------------------------------------------------------------------
	
	private final List<Metric> conceptMetrics = new ArrayList<>();
	{
		// Single -----------------------------------------------------------------------
		
		// Duration
		conceptMetrics.add(new DurationActions());
		conceptMetrics.add(new DurationMoves());
		conceptMetrics.add(new DurationTurns());
		
		// State Repetition
		conceptMetrics.add(new PositionalRepetition());
		conceptMetrics.add(new SituationalRepetition());
		
		// State Evaluation
		conceptMetrics.add(new ClarityNarrowness());
		conceptMetrics.add(new ClarityVariance());
		conceptMetrics.add(new DecisivenessMoves());
		conceptMetrics.add(new DecisivenessThreshold());
		conceptMetrics.add(new LeadChange());
		conceptMetrics.add(new Stability());
		
		// Complexity
		conceptMetrics.add(new DecisionMoves());
		conceptMetrics.add(new GameTreeComplexity());
		conceptMetrics.add(new StateSpaceComplexity());
		
		// Board Coverage
		conceptMetrics.add(new BoardCoverageDefault());
		conceptMetrics.add(new BoardCoverageFull());
		conceptMetrics.add(new BoardCoverageUsed());
		
		// Outcome
		conceptMetrics.add(new AdvantageP1());
		conceptMetrics.add(new Balance());
		conceptMetrics.add(new Completion());
		conceptMetrics.add(new Drawishness());
		conceptMetrics.add(new Timeouts());
		conceptMetrics.add(new OutcomeUniformity());

		// Multi -----------------------------------------------------------------------
		
		// Drama (uses state evaluation)
		conceptMetrics.add(new Drama(MultiMetricValue.Average, Concept.DramaAverage));
		conceptMetrics.add(new Drama(MultiMetricValue.Median, Concept.DramaMedian));
		conceptMetrics.add(new Drama(MultiMetricValue.Max, Concept.DramaMaximum));
		conceptMetrics.add(new Drama(MultiMetricValue.Min, Concept.DramaMinimum));
		conceptMetrics.add(new Drama(MultiMetricValue.Variance, Concept.DramaVariance));
		conceptMetrics.add(new Drama(MultiMetricValue.ChangeAverage, Concept.DramaChangeAverage));
		conceptMetrics.add(new Drama(MultiMetricValue.ChangeSign, Concept.DramaChangeSign));
		conceptMetrics.add(new Drama(MultiMetricValue.ChangeLineBestFit, Concept.DramaChangeLineBestFit));
		conceptMetrics.add(new Drama(MultiMetricValue.ChangeNumTimes, Concept.DramaChangeNumTimes));
		conceptMetrics.add(new Drama(MultiMetricValue.MaxIncrease, Concept.DramaMaxIncrease));
		conceptMetrics.add(new Drama(MultiMetricValue.MaxDecrease, Concept.DramaMaxDecrease));
		
		// Move Evaluation (uses state evaluation)
		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.Average, Concept.MoveEvaluationAverage));
		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.Median, Concept.MoveEvaluationMedian));
		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.Max, Concept.MoveEvaluationMaximum));
		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.Min, Concept.MoveEvaluationMinimum));
		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.Variance, Concept.MoveEvaluationVariance));
		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.ChangeAverage, Concept.MoveEvaluationChangeAverage));
		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.ChangeSign, Concept.MoveEvaluationChangeSign));
		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.ChangeLineBestFit, Concept.MoveEvaluationChangeLineBestFit));
		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.ChangeNumTimes, Concept.MoveEvaluationChangeNumTimes));
		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.MaxIncrease, Concept.MoveEvaluationMaxIncrease));
		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.MaxDecrease, Concept.MoveEvaluationMaxDecrease));
		
		// State Evaluation Difference (uses state evaluation)
		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.Average, Concept.StateEvaluationDifferenceAverage));
		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.Median, Concept.StateEvaluationDifferenceMedian));
		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.Max, Concept.StateEvaluationDifferenceMaximum));
		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.Min, Concept.StateEvaluationDifferenceMinimum));
		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.Variance, Concept.StateEvaluationDifferenceVariance));
		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.ChangeAverage, Concept.StateEvaluationDifferenceChangeAverage));
		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.ChangeSign, Concept.StateEvaluationDifferenceChangeSign));
		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.ChangeLineBestFit, Concept.StateEvaluationDifferenceChangeLineBestFit));
		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.ChangeNumTimes, Concept.StateEvaluationDifferenceChangeNumTimes));
		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.MaxIncrease, Concept.StateEvaluationDifferenceMaxIncrease));
		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.MaxDecrease, Concept.StateEvaluationDifferenceMaxDecrease));
		
		// Board Sites Occupied
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.Average, Concept.BoardSitesOccupiedAverage));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.Median, Concept.BoardSitesOccupiedMedian));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.Max, Concept.BoardSitesOccupiedMaximum));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.Min, Concept.BoardSitesOccupiedMinimum));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.Variance, Concept.BoardSitesOccupiedVariance));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.ChangeAverage, Concept.BoardSitesOccupiedChangeAverage));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.ChangeSign, Concept.BoardSitesOccupiedChangeSign));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.ChangeLineBestFit, Concept.BoardSitesOccupiedChangeLineBestFit));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.ChangeNumTimes, Concept.BoardSitesOccupiedChangeNumTimes));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.MaxIncrease, Concept.BoardSitesOccupiedMaxIncrease));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.MaxDecrease, Concept.BoardSitesOccupiedMaxDecrease));
		
		// Branching Factor
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.Average, Concept.BranchingFactorAverage));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.Median, Concept.BranchingFactorMedian));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.Max, Concept.BranchingFactorMaximum));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.Min, Concept.BranchingFactorMinimum));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.Variance, Concept.BranchingFactorVariance));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.ChangeAverage, Concept.BranchingFactorChangeAverage));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.ChangeSign, Concept.BranchingFactorChangeSign));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.ChangeLineBestFit, Concept.BranchingFactorChangeLineBestFit));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.ChangeNumTimes, Concept.BranchingFactorChangeNumTimesn));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.MaxIncrease, Concept.BranchingFactorChangeMaxIncrease));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.MaxDecrease, Concept.BranchingFactorChangeMaxDecrease));
		
		// Decision Factor
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.Average, Concept.DecisionFactorAverage));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.Median, Concept.DecisionFactorMedian));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.Max, Concept.DecisionFactorMaximum));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.Min, Concept.DecisionFactorMinimum));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.Variance, Concept.DecisionFactorVariance));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.ChangeAverage, Concept.DecisionFactorChangeAverage));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.ChangeSign, Concept.DecisionFactorChangeSign));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.ChangeLineBestFit, Concept.DecisionFactorChangeLineBestFit));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.ChangeNumTimes, Concept.DecisionFactorChangeNumTimes));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.MaxIncrease, Concept.DecisionFactorMaxIncrease));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.MaxDecrease, Concept.DecisionFactorMaxDecrease));
		
		// Move Distance
		conceptMetrics.add(new MoveDistance(MultiMetricValue.Average, Concept.MoveDistanceAverage));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.Median, Concept.MoveDistanceMedian));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.Max, Concept.MoveDistanceMaximum));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.Min, Concept.MoveDistanceMinimum));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.Variance, Concept.MoveDistanceVariance));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.ChangeAverage, Concept.MoveDistanceChangeAverage));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.ChangeSign, Concept.MoveDistanceChangeSign));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.ChangeLineBestFit, Concept.MoveDistanceChangeLineBestFit));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.ChangeNumTimes, Concept.MoveDistanceChangeNumTimes));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.MaxIncrease, Concept.MoveDistanceMaxIncrease));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.MaxDecrease, Concept.MoveDistanceMaxDecrease));
		
		// Piece Number
		conceptMetrics.add(new PieceNumber(MultiMetricValue.Average, Concept.PieceNumberAverage));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.Median, Concept.PieceNumberMedian));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.Max, Concept.PieceNumberMaximum));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.Min, Concept.PieceNumberMinimum));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.Variance, Concept.PieceNumberVariance));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.ChangeAverage, Concept.PieceNumberChangeAverage));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.ChangeSign, Concept.PieceNumberChangeSign));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.ChangeLineBestFit, Concept.PieceNumberChangeLineBestFit));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.ChangeNumTimes, Concept.PieceNumberChangeNumTimes));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.MaxIncrease, Concept.PieceNumberMaxIncrease));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.MaxDecrease, Concept.PieceNumberMaxDecrease));
		
		// Score Difference
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.Average, Concept.ScoreDifferenceAverage));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.Median, Concept.ScoreDifferenceMedian));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.Max, Concept.ScoreDifferenceMaximum));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.Min, Concept.ScoreDifferenceMinimum));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.Variance, Concept.ScoreDifferenceVariance));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.ChangeAverage, Concept.ScoreDifferenceChangeAverage));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.ChangeSign, Concept.ScoreDifferenceChangeSign));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.ChangeLineBestFit, Concept.ScoreDifferenceChangeLineBestFit));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.ChangeNumTimes, Concept.ScoreDifferenceChangeNumTimes));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.MaxIncrease, Concept.ScoreDifferenceMaxIncrease));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.MaxDecrease, Concept.ScoreDifferenceMaxDecrease));
	}

	//-------------------------------------------------------------------------

	public List<Metric> dialogMetrics()
	{
		return Collections.unmodifiableList(dialogMetrics);
	}
	
	public List<Metric> reconstructionMetrics()
	{
		return Collections.unmodifiableList(reconstructionMetrics);
	}
	
	public List<Metric> conceptMetrics()
	{
		return Collections.unmodifiableList(conceptMetrics);
	}

	//-------------------------------------------------------------------------
	
	public double getStateEvaluationCacheValue(final long key)
	{
		// put is needed to update eldest value.
		stateEvaluationCache.put(key, stateEvaluationCache.get(key).doubleValue());
		return stateEvaluationCache.get(key).doubleValue();
	}
	
	public double getStateAfterMoveEvaluationCache(final long key)
	{
		// put is needed to update eldest value.
		stateAfterMoveEvaluationCache.put(key, stateAfterMoveEvaluationCache.get(key).doubleValue());
		return stateAfterMoveEvaluationCache.get(key).doubleValue();
	}
	
	public void putStateEvaluationCacheValue(final long key, final double value)
	{
		stateEvaluationCache.put(key, value);
	}
	
	public void putStateAfterMoveEvaluationCache(final long key, final double value)
	{
		stateAfterMoveEvaluationCache.put(key, value);
	}
	
	public boolean stateEvaluationCacheContains(final long key)
	{
		return stateEvaluationCache.containsKey(key);
	}
	
	public boolean stateAfterMoveEvaluationCacheContains(final long key)
	{
		return stateAfterMoveEvaluationCache.containsKey(key);
	}

	//-------------------------------------------------------------------------
	
}
