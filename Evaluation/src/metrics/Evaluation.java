package metrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import metrics.designer.IdealDuration;
import metrics.quality.BoardCoverage;
import metrics.quality.BoardCoverageAvg;
import metrics.quality.BranchingFactorAvg;
import metrics.quality.BranchingFactorIncrease;
import metrics.quality.BranchingFactorMax;
import metrics.quality.DecisionFactor;
import metrics.quality.DecisionMoves;
import metrics.quality.DiceRolled;
import metrics.quality.MoveDistance;
import metrics.quality.PieceNumberChange;
import metrics.quality.PositionalRepetition;
import metrics.quality.ScoreDifference;
import metrics.quality.SituationalRepetition;
import metrics.viability.AdvantageP1;
import metrics.viability.Balance;
import metrics.viability.Completion;
import metrics.viability.Drawishness;
import metrics.viability.DurationMoves;
import metrics.viability.DurationTurns;
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
		metrics.add(new DurationTurns());
		metrics.add(new DurationMoves());
		metrics.add(new Timeouts());
		metrics.add(new Drawishness());
		metrics.add(new AdvantageP1());
		
		// Quality
		metrics.add(new BoardCoverage());
		metrics.add(new BoardCoverageAvg());
		metrics.add(new BranchingFactorAvg());
		metrics.add(new BranchingFactorIncrease());
		metrics.add(new BranchingFactorMax());
		metrics.add(new PieceNumberChange());
		metrics.add(new DiceRolled());
		metrics.add(new ScoreDifference());
		metrics.add(new PositionalRepetition());
		metrics.add(new MoveDistance());
		metrics.add(new SituationalRepetition());
		metrics.add(new DecisionMoves());
		metrics.add(new DecisionFactor());
		
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
