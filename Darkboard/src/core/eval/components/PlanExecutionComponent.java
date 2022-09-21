package core.eval.components;

import java.util.Vector;

import ai.planner.Plan;
import ai.planner.PlanDashboard;
import core.EvaluationFunction;
import core.EvaluationFunctionComponent;
import core.Metaposition;
import core.Move;

/**
 * This component measures fitness to the various Plans in the dashboard.
 * @author Nikola Novarlic
 *
 */
public class PlanExecutionComponent extends EvaluationFunctionComponent {

	PlanDashboard board;
	
	public PlanExecutionComponent(PlanDashboard d)
	{
		super();
		board = d;
		name = "Plan Execution";
	}


	public float evaluate(Metaposition start, Metaposition dest, Move m, Vector history)
	{
		float result = 0.0f;
		
		if (board==null) return result;
		
		int p = board.getPlanNumber();
		
		for (int k=0; k<p; k++)
		{
			Plan pl = board.getPlan(k);
			if (pl.active) result+=pl.evaluate(start,dest,m,history);
			if (generatesCustomReport())
			{
				if (EvaluationFunction.currentNode!=null)
					EvaluationFunction.currentNode.addInformation(Plan.description);
			}
		}
		
		return result;
	}
	
	public boolean generatesCustomReport()
	{
		return true;
	}
	
}
