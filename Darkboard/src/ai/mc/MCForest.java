package ai.mc;

import core.Globals;
import core.Move;

public class MCForest extends Thread {

	ai.mc.MCNode[] roots;
	ai.mc.MCNode[] currentStack;
	ai.mc.MCNode[] incompleteStack; //only up to the point when strategy nodes are actually reached
	
	int stackLength;
	
	boolean running = false;
	
	MCComponent gui[] = null;
	
	public static MCForest forest = new MCForest(3);
	
	public MCForest(int stack)
	{
		stackLength = stack;
		roots = new ai.mc.MCNode[stack];
		
		
		//currentStack = new MCNode[stack];
		//incompleteStack = new MCNode[stack];
		
		//roots[stackLength-1] = new MCStateNode(start);
		
		//this.start();
	}
	
	public synchronized Move doMC(ai.mc.MCState start, long time)
	{
		//if (running) return null; //maybe raise an exception
		for (int k=0; k<stackLength-1; k++) roots[k] = new ai.mc.MCStrategyNode(null,null,null);
		if (stackLength>1) ai.mc.MCStrategyFunctions.initializeHighestLevelFunctions((ai.mc.MCStrategyNode)roots[0], start);
		roots[stackLength-1] = new ai.mc.MCStateNode(start);
		running = true;
		
		if (gui==null) gui = MCComponent.makeComponent();
		
		//this.notifyAll();
		//try { Thread.sleep(time); } catch (InterruptedException ie) { ie.printStackTrace(); }
		for (int k=0; k<20000; k++) doMCCycle();
		
		interruptRun();
		
		Move m = roots[stackLength-1].findMostVisited();
		
		for (int k=0; k<stackLength; k++)
			gui[k].setNode(roots[k]);
		
		return m;
	}
	
	public void doMCCycle()
	{
		ai.mc.MCNode node = null;
		
		for (int k=0; k<roots.length; k++)
		{
			//selection
			node = roots[k].select();

			if (node==null) return;
			/*if (k==0 && stackLength>1 && ((MCStrategyNode)node).strategy==null)
			{
				k = stackLength-2;
				continue;
			}*/
			//expansion
			if (node.needsExpansion()) node = node.expand();
			
			if (k!=roots.length-1)
			{
				roots[k+1].currentUpperStrategy = roots[k].nextStrategy;
				roots[k+1].beginningOfCurrentUpperStrategy = roots[k+1];
			}
		}
		
		if (node==null) return;
		
		double outcome;
		
		//simulation
		if (!node.needsExpansion() && node.ownvalue!=Double.NEGATIVE_INFINITY && node.parentLink!=null) //stopped before a leaf due to event, is not the root
		{
			/*if (node.currentUpperStrategy==null) outcome = node.value; //plan is done
			else*/ outcome = node.parentLink.theRestValue;
		}
		else
		{
			outcome = node.simulate();
		}
		//back propagation
		node.backpropagate(outcome,1.0);
		if (node.currentUpperStrategy!=null) node.currentUpperStrategy.upPropagation(node, true, true, outcome);
	}
	
	public Move doMC2(ai.mc.MCState start)
	{
		for (int k=0; k<stackLength-1; k++) roots[k] = new ai.mc.MCStrategyNode(null,null,null);
		if (stackLength>1) ai.mc.MCStrategyFunctions.initializeHighestLevelFunctions((ai.mc.MCStrategyNode)roots[0], start);
		roots[stackLength-1] = new ai.mc.MCStateNode(start);
		roots[stackLength-1].ownvalue = start.eval(0);
		roots[stackLength-1].value = roots[stackLength-1].ownvalue;
		
		if (gui==null && Globals.hasGui) gui = MCComponent.makeComponent();
		
		if (Globals.hasGui)
			for (int k=0; k<stackLength; k++)
				gui[k].setNode(null);

		ai.mc.MCNode node = null;
		
		for (int k=0; k<1000; k++)
		{
		
			node = ((ai.mc.MCStateNode)roots[stackLength-1]).select2();
			if (node==null) 
			{
				continue;
			}
			((ai.mc.MCStateNode)node).expand2();
		
		}

		ai.mc.MCNode result = ((ai.mc.MCStateNode)roots[stackLength-1]).bestChild;
		
		if (Globals.hasGui)
			for (int k=0; k<stackLength; k++)
				gui[k].setNode(roots[k]);
		
		if (result!=null) return result.parentLink.move;
		else 
		{
			roots[stackLength-1] = null;
			return null;
		}
	}
	
	public boolean valid()
	{
		return (roots!=null && roots[stackLength-1]!=null);
	}
	
	public Move findNextBest()
	{
		ai.mc.MCStateNode n = ((ai.mc.MCStateNode)roots[stackLength-1]).bestChild;
		if (n!=null)
		{
			n.value = Double.NEGATIVE_INFINITY;
			((ai.mc.MCStateNode)roots[stackLength-1]).recalculate(n);
			return ((ai.mc.MCStateNode)roots[stackLength-1]).bestChild.parentLink.move;
		} else return null;
	}

	public void run() 
	{
		// TODO Auto-generated method stub
		while (true)
		{
			while (running)
			{
				doMCCycle();
			}
			
			try
			{ sleep(50); } catch (InterruptedException ie) { ie.printStackTrace(); }
		}
		
	}
	
	public void interruptRun()
	{
		running = false;
	}

}
