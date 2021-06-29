package metrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import metrics.designer.IdealDuration;
import metrics.multiple.MultiMetricFramework.MultiMetricValue;
import metrics.multiple.metrics.BoardSitesOccupied;
import metrics.multiple.metrics.BranchingFactor;
import metrics.multiple.metrics.DecisionFactor;
import metrics.multiple.metrics.MoveDistance;
import metrics.multiple.metrics.PieceNumber;
import metrics.multiple.metrics.ScoreDifference;
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
import metrics.single.outcome.Timeouts;
import metrics.single.stateEvaluation.decisiveness.DecisivenessTurns;
import metrics.single.stateRepetition.PositionalRepetition;
import metrics.single.stateRepetition.SituationalRepetition;

//-----------------------------------------------------------------------------

/**
 * Access point for evaluation functionality.
 * 
 * @author cambolbro and matthew.stephenson
 */
public class Evaluation
{
	private final List<Metric> dialogMetrics = new ArrayList<>();
	{
		// Single
		dialogMetrics.add(new AdvantageP1());
		dialogMetrics.add(new Balance());
		dialogMetrics.add(new BoardCoverageDefault());
		dialogMetrics.add(new Completion());
		dialogMetrics.add(new DecisionMoves());
		dialogMetrics.add(new Drawishness());
		dialogMetrics.add(new Timeouts());
		dialogMetrics.add(new StateSpaceComplexity());
		
		// Designer
		dialogMetrics.add(new IdealDuration());
		
		dialogMetrics.add(new PieceNumber(MultiMetricValue.ChangeAverage, null));
		dialogMetrics.add(new PieceNumber(MultiMetricValue.ChangeLineBestFit, null));
		dialogMetrics.add(new PieceNumber(MultiMetricValue.ChangeNumTimes, null));
		dialogMetrics.add(new PieceNumber(MultiMetricValue.ChangeSign, null));
	}
	
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
//		conceptMetrics.add(new ClarityNarrowness());
//		conceptMetrics.add(new ClarityVariance());
		conceptMetrics.add(new DecisivenessTurns());
//		conceptMetrics.add(new DecisivenessThreshold());
//		conceptMetrics.add(new LeadChange());
//		conceptMetrics.add(new Stability());
		
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

		// Multi -----------------------------------------------------------------------
		
		// Board Sites Occupied
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.Average, null));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.Median, null));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.Max, null));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.Min, null));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.Variance, null));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.ChangeAverage, null));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.ChangeSign, null));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.ChangeLineBestFit, null));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.ChangeNumTimes, null));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.MaxIncrease, null));
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.MaxDecrease, null));
		
		// Branching Factor
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.Average, null));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.Median, null));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.Max, null));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.Min, null));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.Variance, null));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.ChangeAverage, null));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.ChangeSign, null));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.ChangeLineBestFit, null));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.ChangeNumTimes, null));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.MaxIncrease, null));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.MaxDecrease, null));
		
		// Decision Factor
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.Average, null));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.Median, null));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.Max, null));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.Min, null));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.Variance, null));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.ChangeAverage, null));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.ChangeSign, null));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.ChangeLineBestFit, null));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.ChangeNumTimes, null));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.MaxIncrease, null));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.MaxDecrease, null));
		
		// Drama (uses state evaluation)
//		conceptMetrics.add(new Drama(MultiMetricValue.Average, null));
//		conceptMetrics.add(new Drama(MultiMetricValue.Median, null));
//		conceptMetrics.add(new Drama(MultiMetricValue.Max, null));
//		conceptMetrics.add(new Drama(MultiMetricValue.Min, null));
//		conceptMetrics.add(new Drama(MultiMetricValue.Variance, null));
//		conceptMetrics.add(new Drama(MultiMetricValue.ChangeAverage, null));
//		conceptMetrics.add(new Drama(MultiMetricValue.ChangeSign, null));
//		conceptMetrics.add(new Drama(MultiMetricValue.ChangeLineBestFit, null));
//		conceptMetrics.add(new Drama(MultiMetricValue.ChangeNumTimes, null));
//		conceptMetrics.add(new Drama(MultiMetricValue.MaxIncrease, null));
//		conceptMetrics.add(new Drama(MultiMetricValue.MaxDecrease, null));
		
		// Move Distance
		conceptMetrics.add(new MoveDistance(MultiMetricValue.Average, null));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.Median, null));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.Max, null));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.Min, null));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.Variance, null));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.ChangeAverage, null));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.ChangeSign, null));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.ChangeLineBestFit, null));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.ChangeNumTimes, null));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.MaxIncrease, null));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.MaxDecrease, null));
		
		// Move Evaluation (uses state evaluation)
//		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.Average, null));
//		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.Median, null));
//		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.Max, null));
//		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.Min, null));
//		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.Variance, null));
//		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.ChangeAverage, null));
//		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.ChangeSign, null));
//		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.ChangeLineBestFit, null));
//		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.ChangeNumTimes, null));
//		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.MaxIncrease, null));
//		conceptMetrics.add(new MoveEvaluation(MultiMetricValue.MaxDecrease, null));
		
		// Piece Number
		conceptMetrics.add(new PieceNumber(MultiMetricValue.Average, null));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.Median, null));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.Max, null));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.Min, null));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.Variance, null));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.ChangeAverage, null));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.ChangeSign, null));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.ChangeLineBestFit, null));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.ChangeNumTimes, null));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.MaxIncrease, null));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.MaxDecrease, null));
		
		// Score Difference
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.Average, null));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.Median, null));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.Max, null));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.Min, null));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.Variance, null));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.ChangeAverage, null));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.ChangeSign, null));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.ChangeLineBestFit, null));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.ChangeNumTimes, null));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.MaxIncrease, null));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.MaxDecrease, null));
		
		// State Evaluation Difference (uses state evaluation)
//		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.Average, null));
//		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.Median, null));
//		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.Max, null));
//		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.Min, null));
//		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.Variance, null));
//		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.ChangeAverage, null));
//		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.ChangeSign, null));
//		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.ChangeLineBestFit, null));
//		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.ChangeNumTimes, null));
//		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.MaxIncrease, null));
//		conceptMetrics.add(new StateEvaluationDifference(MultiMetricValue.MaxDecrease, null));
	}

	//-------------------------------------------------------------------------

	public List<Metric> dialogMetrics()
	{
		return Collections.unmodifiableList(conceptMetrics);
	}
	
	public List<Metric> conceptMetrics()
	{
		return Collections.unmodifiableList(conceptMetrics);
	}

	//-------------------------------------------------------------------------

}
