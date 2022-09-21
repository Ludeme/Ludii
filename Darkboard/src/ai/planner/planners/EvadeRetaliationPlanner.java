package ai.planner.planners;

import ai.planner.PlanDashboard;
import ai.planner.Planner;
import ai.planner.plans.LeaveSquarePlan;
import core.Chessboard;
import core.Metaposition;
import core.Move;

/**
 * This Planner component generates appropriate plans when the player
 * captures something, encouraging to leave that square with haste if
 * possible. Such plans are deleted when the player suffers a capture.
 * @author Nikola Novarlic
 *
 */
public class EvadeRetaliationPlanner extends Planner {
	
	
	public EvadeRetaliationPlanner(PlanDashboard d)
	{
		super(d);
	}
	
	public void evolveAfterMove(Metaposition root, Metaposition ev, Move m,
			int cap, int capx, int capy, int check1, int check2, int tries)
	{
		if (cap!=Chessboard.NO_CAPTURE)
		{
			LeaveSquarePlan lsp = new LeaveSquarePlan(owner,m.toX,m.toY);
			lsp.ttl = 5; //plan duration
			dashboard.addPlan(lsp);
		}
	}
	
	public void evolveAfterOpponentMove(Metaposition root, Metaposition ev,
			int capx, int capy, int check1, int check2, int tries)
	{
		try
		{
			for (int k=0; k<dashboard.getPlanNumber(); k++)
			{
				if (dashboard.getPlan(k).getClass().equals(Class.forName("ai.planner.plans.LeaveSquarePlan")))
				{
					LeaveSquarePlan lsp = (LeaveSquarePlan) dashboard.getPlan(k);
					if (lsp.getSx()==capx && lsp.getSy()==capy)
					{
						dashboard.deletePlan(lsp);
						k--;
					}
				}
			}
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

	}

}
