package metrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import metrics.designer.IdealDuration;
import metrics.quality.boardCoverage.BoardCoverage;
import metrics.quality.boardCoverage.BoardCoverageAvg;
import metrics.quality.branchingFactor.BranchingFactorAvg;
import metrics.quality.branchingFactor.BranchingFactorChange;
import metrics.quality.branchingFactor.BranchingFactorMax;
import metrics.quality.decisionMoves.DecisionFactorAvg;
import metrics.quality.decisionMoves.DecisionMoves;
import metrics.quality.moveDistance.MoveDistanceAvg;
import metrics.quality.pieceNumber.PieceNumberChange;
import metrics.quality.scoreDifference.ScoreDifferenceEnd;
import metrics.quality.stateRepetition.PositionalRepetition;
import metrics.quality.stateRepetition.SituationalRepetition;
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
		metrics.add(new BranchingFactorChange());
		metrics.add(new BranchingFactorMax());
		metrics.add(new PieceNumberChange());
		
		metrics.add(new ScoreDifferenceEnd());
		metrics.add(new PositionalRepetition());
		metrics.add(new MoveDistanceAvg());
		metrics.add(new SituationalRepetition());
		metrics.add(new DecisionMoves());
		metrics.add(new DecisionFactorAvg());
		
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
