package ai.planner;

import ai.player.Darkboard;
import core.Metaposition;
import core.Move;

/**
 * A Planner is a high-level facility that reacts to umpire 
 * messages and edits the PlanDashboard accordingly, generating,
 * editing and deleting plans.
 * @author Nikola Novarlic
 *
 */
public class Planner {
	
	protected PlanDashboard dashboard;
	public Darkboard owner;
	
	public Planner(PlanDashboard d)
	{
		init(d);
	}
	
	protected void init(PlanDashboard d)
	{
		dashboard = d;
		owner = d.owner;
	}
	
	public void evolveAfterMove(Metaposition root, Metaposition ev, Move m,
			int cap, int capx, int capy, int check1, int check2, int tries)
	{
		
	}
	
	public void evolveAfterOpponentMove(Metaposition root, Metaposition ev,
			int capx, int capy, int check1, int check2, int tries)
	{
		
	}
	
	public void evolveAfterIllegalMove(Metaposition root, Metaposition ev, Move m/*, int capx, int capy, int c1, int c2, int tries*/)
	{
		
	}

}
