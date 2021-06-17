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
import metrics.single.AdvantageP1;
import metrics.single.Balance;
import metrics.single.Completion;
import metrics.single.DecisionMoves;
import metrics.single.Drawishness;
import metrics.single.Timeouts;
import metrics.single.boardCoverage.BoardCoverageDefault;
import metrics.single.complexity.GameTreeComplexity;
import metrics.single.complexity.StateSpaceComplexity;
import metrics.single.duration.DurationMoves;
import metrics.single.duration.DurationTurns;
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
	}
	
	private final List<Metric> conceptMetrics = new ArrayList<>();
	{
		// Single
		conceptMetrics.add(new DurationMoves());
		conceptMetrics.add(new DurationTurns());
		conceptMetrics.add(new PositionalRepetition());
		conceptMetrics.add(new SituationalRepetition());
		conceptMetrics.add(new AdvantageP1());
		conceptMetrics.add(new Balance());
		conceptMetrics.add(new BoardCoverageDefault());
		conceptMetrics.add(new Completion());
		conceptMetrics.add(new DecisionMoves());
		conceptMetrics.add(new Drawishness());
		conceptMetrics.add(new GameTreeComplexity());
		conceptMetrics.add(new Timeouts());

		// Multi
		conceptMetrics.add(new BoardSitesOccupied(MultiMetricValue.Average, null));
		conceptMetrics.add(new BranchingFactor(MultiMetricValue.Average, null));
		conceptMetrics.add(new PieceNumber(MultiMetricValue.Average, null));
		conceptMetrics.add(new ScoreDifference(MultiMetricValue.Average, null));
		conceptMetrics.add(new MoveDistance(MultiMetricValue.Average, null));
		conceptMetrics.add(new DecisionFactor(MultiMetricValue.Average, null));
	}

	//-------------------------------------------------------------------------

	public List<Metric> dialogMetrics()
	{
		return Collections.unmodifiableList(dialogMetrics);
	}
	
	public List<Metric> conceptMetrics()
	{
		return Collections.unmodifiableList(conceptMetrics);
	}

	//-------------------------------------------------------------------------

}
