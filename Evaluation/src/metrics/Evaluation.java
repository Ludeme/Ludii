package metrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import metrics.designer.IdealDuration;
import metrics.multiple.MultiMetricFramework.MultiMetricValue;
import metrics.multiple.metrics.BoardCoverage;
import metrics.multiple.metrics.BranchingFactor;
import metrics.multiple.metrics.DecisionFactor;
import metrics.multiple.metrics.MoveDistance;
import metrics.multiple.metrics.PieceNumber;
import metrics.multiple.metrics.ScoreDifference;
import metrics.single.AdvantageP1;
import metrics.single.Balance;
import metrics.single.BoardCoverageTotal;
import metrics.single.Completion;
import metrics.single.DecisionMoves;
import metrics.single.Drawishness;
import metrics.single.Timeouts;
import metrics.single.length.DurationMoves;
import metrics.single.length.DurationTurns;
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
	private final List<Metric> metrics = new ArrayList<>();
	{
		// Viability
		metrics.add(new Balance());
		metrics.add(new Completion());
		metrics.add(new DurationTurns());
		metrics.add(new DurationMoves());
		metrics.add(new Timeouts());
		metrics.add(new Drawishness());
		metrics.add(new AdvantageP1());
		
		// Quality
		metrics.add(new BoardCoverageTotal());
		metrics.add(new BoardCoverage(MultiMetricValue.Average, null));
		metrics.add(new BranchingFactor(MultiMetricValue.Average, null));
		metrics.add(new PieceNumber(MultiMetricValue.Average, null));
		
		metrics.add(new ScoreDifference(MultiMetricValue.Average, null));
		metrics.add(new PositionalRepetition());
		metrics.add(new MoveDistance(MultiMetricValue.Average, null));
		metrics.add(new SituationalRepetition());
		metrics.add(new DecisionMoves());
		metrics.add(new DecisionFactor(MultiMetricValue.Average, null));
		
		// Designer
		metrics.add(new IdealDuration());
	}

	//-------------------------------------------------------------------------

	public List<Metric> metrics()
	{
		return Collections.unmodifiableList(metrics);
	}

	//-------------------------------------------------------------------------

}
