package ai.mc;

import ai.mc.MCLink;
import core.Move;

/**
 * Monte Carlo node.
 * @author Nikola Novarlic
 *
 */
public abstract class MCNode {
	
	MCNode parent;
	MCLink parentLink;
	boolean chanceNode;
	double value = 0.0;
	double ownvalue = Double.NEGATIVE_INFINITY;
	double visits = 0.0;
	boolean expanded = false;
	MCLink links[];

	ai.mc.MCStrategyNode currentUpperStrategy; //may change with each visit - upper-level strategy being executed
	MCNode beginningOfCurrentUpperStrategy; //same, it is an ancestor of this node
	ai.mc.MCStrategyNode nextStrategy; //same
	
	public MCNode select() { return null; }
	public boolean needsExpansion() { return false; }
	public MCNode expand() { return null; }
	public double simulate() { return 0.0; }
	public void backpropagate(double d, double weight) {}
	public Move findMostVisited() { return null; }

	
	public MCNode findRoot()
	{
		return (parent==null? this: parent.findRoot());
	}
	
	public MCNode findMostVisitedChild()
	{
		MCNode m = null;
		double visits = 0;
		
		if (links==null) return null;
		
		for (int k=0; k<links.length; k++)
		{
			if (links[k].child!=null && links[k].child.visits>visits)
			{
				visits = links[k].child.visits;
				m = links[k].child;
			}
		}
		
		return m;
	}

}
