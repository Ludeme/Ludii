package metrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import metrics.designer.IdealDuration;
import metrics.quality.BoardCoverage;
import metrics.quality.BranchingFactor;
import metrics.quality.DiceRolled;
import metrics.quality.MoveDistance;
import metrics.quality.PieceNumberChange;
import metrics.quality.ScoreDifference;
import metrics.quality.StateRepetition;
import metrics.viability.AdvantageP1;
import metrics.viability.Balance;
import metrics.viability.Completion;
import metrics.viability.Drawishness;
import metrics.viability.Duration;
import metrics.viability.Timeouts;

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
		metrics.add(new Duration());
		metrics.add(new Timeouts());
		metrics.add(new Drawishness());
		metrics.add(new AdvantageP1());
		
		// Quality
		metrics.add(new BoardCoverage());
		metrics.add(new BranchingFactor());
		metrics.add(new PieceNumberChange());
		metrics.add(new DiceRolled());
		metrics.add(new ScoreDifference());
		metrics.add(new StateRepetition());
		metrics.add(new MoveDistance());
		
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
