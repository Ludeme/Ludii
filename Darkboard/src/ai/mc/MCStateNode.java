package ai.mc;

import core.Move;

public class MCStateNode extends MCNode {
	
	public static double UCT_CONSTANT_C = 0.5;
	
	public static double CONSERVATION_CONSTANT = 0.5;

	public MCState state = null;
	//double chance = 0.0;
	
	public int activeChildren = 0;
	public boolean active = true;
	MCStateNode bestChild = null;
	public double bestChildValue = Double.NEGATIVE_INFINITY;

	
	public MCStateNode()
	{
		chanceNode = false;
	}
	
	public MCStateNode(MCState s)
	{
		chanceNode = false;
		state = s;
	}
	
	public MCNode select() 
	{ 
		int best = -1;
		double bestval = Double.NEGATIVE_INFINITY;
		double log = Math.log(visits+1);
		double strategyBias[] = null;
		
		if (state!=null && state.isBroken()) return null;
		
		if (!expanded || links.length==0) return this;
		
		if (currentUpperStrategy!=null)
		{
			strategyBias = currentUpperStrategy.strategy.progressInformation(state, links);
		}
		
		for (int k=0; k<links.length; k++)
		{
			MCNode n = links[k].child;
			double visits = (n==null? 1.0 : n.visits+1);
			double v = (n==null? this.value : n.value);
			double strat = (strategyBias!=null? strategyBias[k]*0.5: 0.0);
			
			double val = links[k].heuristicBias+v+strat+UCT_CONSTANT_C*Math.sqrt(log/visits);
			if (val>bestval)
			{
				bestval = val;
				best = k;
			}
		}
		
		MCNode bestNode = links[best].child;
		
		if (bestNode==null)
		{
			MCStateNode mcs = new MCStateNode();
			mcs.state = state.getState(0,links[best].move);
			//mcs.chance = state.chanceOfMessage(links[best].move, MCState.MSG_SILENT, 0);
			links[best].setChild(mcs);
			bestNode = mcs;
		}
		
		//TODO INSERT EXPENSIVE REFINEMENTS TO NODE/LINK ACCURACY AFTER X VISITS HERE
		if (/*bestNode.visits>=10.0 &&*/ !links[best].refined()) links[best].refine();
		
		boolean somethingElse = links[best].somethingElseHappens();
		if (somethingElse) return links[best].child;
		
		
		
		if (strategyBias!=null && strategyBias[best]==1.0)
		{
			//this move completes current objective
			currentUpperStrategy.upPropagation(bestNode, true, false, bestNode.value);
			bestNode.currentUpperStrategy = this.currentUpperStrategy.nextStrategy;
			bestNode.beginningOfCurrentUpperStrategy = bestNode;
			//if (bestNode.currentUpperStrategy==null) return bestNode; //do not continue if the plan is done.
		} else
		{
			bestNode.currentUpperStrategy = this.currentUpperStrategy;
			bestNode.beginningOfCurrentUpperStrategy = this.beginningOfCurrentUpperStrategy;
		}
		
		return bestNode.select(); 
	}
	
	public boolean needsExpansion() { return (!expanded && ownvalue!=Double.NEGATIVE_INFINITY); }
	
	public MCNode expand() 
	{ 
		if (state==null) return null;
		Move[] mov = state.getMoves(0);
		links = new MCLink[mov.length];
		for (int k=0; k<mov.length; k++)
		{
			links[k] = new MCLink(this);
			/*MCStateNode mcs = new MCStateNode();
			mcs.state = st[k];
			mcs.chance = state.chanceOfMessage(mov[k], MCState.MSG_SILENT, 0);
			links[k].setChild(mcs);*/
			links[k].setMove(mov[k]);
			links[k].heuristicBias = state.getHeuristicBias(0,mov[k]);
		}
		expanded = true;
		return select(); 
	}
	
	public double simulate() 
	{ 
		if (state==null) return 0.0;
		//MCStateNode mc = (MCStateNode)parent;
		//if (mc.state==null) return 0.0;
		ownvalue = state.eval(0);
		//ownvalue = mc.state.eval(parentLink.move,state);
		return ownvalue; 
	}
	
	public void backpropagate(double d, double weight)
	{
		double vis = (visits>0.0? visits+20.0 : visits);
		value = (value*vis+d*weight)/(vis+weight);
		visits+=1.0;
		//if (parent!=null) parent.backpropagate(d,(parent.chanceNode? weight : weight*chance));
		if (parent!=null) 
		{
			parent.backpropagate(d,weight*0.5);
		}
	}
	
	public Move findMostVisited()
	{
		Move m = null;
		double visits = 0;
		
		if (links==null) return null;
		
		for (int k=0; k<links.length; k++)
		{
			if (links[k].child!=null && links[k].child.visits>visits)
			{
				visits = links[k].child.visits;
				m = links[k].move;
			}
		}
		
		return m;
	}
	
	public MCNode select2()
	{
		if (!expanded) return this;
		if (!active) return null;
		
		int best = -1;
		double bestval = Double.NEGATIVE_INFINITY;
		double log = Math.log(visits+1);
		
		for (int k=0; k<links.length; k++)
		{
			if (links[k]==null) continue;
			MCNode n = links[k].child;
			if (n==null) continue;
			double visits = n.visits+1;
			double v = n.value;
			
			double val = links[k].heuristicBias+v+UCT_CONSTANT_C*Math.sqrt(log/visits);
			if (val>bestval)
			{
				bestval = val;
				best = k;
			}
		}
		
		if (best!=-1) return links[best].child.select();
		else return null;
	}
	
	public void expand2()
	{
		if (expanded || !active || state==null) return;
		
		expanded = true;
		
		Move[] moves = state.getMoves(0);
		
		links = new MCLink[moves.length];
		for (int k=0; k<links.length; k++)
		{
			links[k] = new MCLink(this);
			links[k].setMove(moves[k]);
			MCStateNode mcs = new MCStateNode();
			mcs.state = state.getState(0,moves[k]);
			links[k].setChild(mcs);
			links[k].refine();
			
			//double c = links[k].silentChance;
			mcs.ownvalue = mcs.state.eval(0);
			mcs.value = mcs.ownvalue;
			if (mcs.ownvalue>bestChildValue)
			{
				bestChild = mcs;
				bestChildValue = mcs.ownvalue;
			}
		}
		if (bestChild!=null)
		{
			//this.value = this.ownvalue*CONSERVATION_CONSTANT+bestChildValue*(1.0-CONSERVATION_CONSTANT);
			determineValue();
			if (parent!=null) ((MCStateNode)parent).recalculate(this);
		}
		backprop2();
	}
	
	public void determineValue()
	{
		double nonSilent = (parentLink!=null? 1.0-parentLink.silentChance : 0.0);
		double best = (bestChild!=null? (1.0-nonSilent)*(1.0-CONSERVATION_CONSTANT) : 0.0);
		double currentNode = 1.0 - nonSilent - best;
		
		double nonSilValue = (parentLink!=null? parentLink.theRestValue : 0.0);
		double bestVal = (bestChild!=null? bestChildValue : 0.0);
		
		value = nonSilent*nonSilValue + best*bestVal + currentNode*ownvalue;
	}
	
	public void recalculate(MCNode child)
	{
		boolean propagate = true;
		if (child==null) return;
		if (child==bestChild)
		{
			//if it decreased, check if it is still the best
			if (child.value<bestChildValue)
			{
				bestChildValue = child.value;
				for (int k=0; k<links.length; k++)
				{
					if (links[k]!=null && links[k].child!=null && links[k].child.value>bestChildValue)
					{
						bestChild = (MCStateNode)links[k].child; bestChildValue = links[k].child.value;
					}
				}
			}
		} else
		{
			if (child.value>bestChildValue)
			{
				bestChild = (MCStateNode)child; bestChildValue = child.value;
			} else propagate = false;
		}
		//this.value = this.ownvalue*CONSERVATION_CONSTANT+bestChildValue*(1.0-CONSERVATION_CONSTANT);
		determineValue();
		if (propagate && parent!=null) ((MCStateNode)parent).recalculate(this);
	}
	
	public void backprop2()
	{
		visits++;
		if (parent!=null) ((MCStateNode)parent).backprop2();
	}
	

	
}
