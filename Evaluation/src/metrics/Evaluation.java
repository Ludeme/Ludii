package metrics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import metrics.designer.IdealDuration;
import metrics.multiple.boardCoverage.BoardCoverageAvg;
import metrics.multiple.branchingFactor.BranchingFactorAvg;
import metrics.multiple.branchingFactor.BranchingFactorChange;
import metrics.multiple.branchingFactor.BranchingFactorMax;
import metrics.multiple.decisionFactor.DecisionFactorAvg;
import metrics.multiple.moveDistance.MoveDistanceAvg;
import metrics.multiple.pieceNumber.PieceNumberChange;
import metrics.multiple.scoreDifference.ScoreDifferenceEnd;
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
