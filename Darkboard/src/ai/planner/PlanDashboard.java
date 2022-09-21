package ai.planner;

import java.util.Vector;

import ai.player.Darkboard;
import core.Metaposition;
import core.Move;

public class PlanDashboard {
	
	Vector plans = new Vector();
	Vector planners = new Vector();
	public Darkboard owner;
	
	public PlanDashboard(Darkboard d)
	{
		owner = d;
	}
	
	public void addPlan(Plan p)
	{
		//System.out.println("Adding plan: "+ p.toString());
		plans.add(p);
		p.isAdded();
	}
	
	public int getPlanNumber()
	{
		return plans.size();
	}
	
	public Plan getPlan(int k)
	{
		return (Plan)(plans.get(k));
	}
	
	public void deletePlan(Plan p)
	{
		//System.out.println("Removing plan: "+ p.toString());
		plans.remove(p);
	}
	
	public void addPlan(ai.planner.Planner p)
	{
		planners.add(p);
	}
	
	public int getPlannerNumber()
	{
		return planners.size();
	}
	
	public ai.planner.Planner getPlanner(int k)
	{
		return (ai.planner.Planner)(planners.get(k));
	}
	
	public void deletePlanner(ai.planner.Planner p)
	{
		planners.remove(p);
	}
	
	//Update the planners with the latest information

	public void evolveAfterMove(Metaposition root, Metaposition ev, Move m,
			int cap, int capx, int capy, int check1, int check2, int tries)
	{
		for (int k=0; k<getPlannerNumber(); k++)
			getPlanner(k).evolveAfterMove(root,ev,m,cap,capx,capy,check1,check2,tries);
	
		for (int j=0; j<getPlanNumber(); j++)
		{
			Plan p = getPlan(j);
			p.age++;
			p.ttl--;
			p.modifier*=p.falloff;
			if (p.ttl==0 || !p.active)
			{
				plans.remove(p);
				j--;
			} else getPlan(j).evolveAfterMove(root,ev,m,cap,capx,capy,check1,check2,tries);
		}
		
	}
	
	public void evolveAfterOpponentMove(Metaposition root, Metaposition ev,
			int capx, int capy, int check1, int check2, int tries)
	{
		for (int k=0; k<getPlannerNumber(); k++)
			getPlanner(k).evolveAfterOpponentMove(root,ev,capx,capy,check1,check2,tries);
		
		for (int k=0; k<getPlanNumber(); k++)
			if (getPlan(k).active) getPlan(k).evolveAfterOpponentMove(root,ev,capx,capy,check1,check2,tries);
	
	}
	
	public void evolveAfterIllegalMove(Metaposition root, Metaposition ev, Move m/*, int capx, int capy, int c1, int c2, int tries*/)
	{
		for (int k=0; k<getPlannerNumber(); k++)
			getPlanner(k).evolveAfterIllegalMove(root,ev,m);
		
		for (int k=0; k<getPlanNumber(); k++)
			if (getPlan(k).active) getPlan(k).evolveAfterIllegalMove(root,ev,m);
	}
	
	public Move findPlanExecutingMove(Vector moves, Metaposition start, float threshold)
	{
		Move best = null;
		int bestPriority = -1;
		float bestMatch = 0.0f;
		
		for (int k=0; k<moves.size(); k++)
		{
			Move m = (Move)moves.get(k);
			for (int j=0; j<getPlanNumber(); j++)
			{
				Plan p = getPlan(j);
				if (!p.active) continue;
				float match = p.isApplicable(start,m)*p.modifier;
				if (match>threshold)
				{
					if (p.priority>bestPriority || (p.priority==bestPriority
						&& match>bestMatch))
					{
						best = m;
						bestPriority = p.priority;
						bestMatch = match;
					}
				}
			}
		}
		
		return best;
	}

}
