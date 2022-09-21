package ai.mc;
import java.util.Vector;

public class MCComponent {
	
	public static final int MINIMUM_ROW_HEIGHT = 10;


	ai.mc.MCNode node;
	ai.mc.MCNode nodeSequence[];
	int rows[][];
	
	public MCComponent(ai.mc.MCNode n)
	{
		node = n;
		nodeSequence = buildDefaultSequence(node);
		buildDelimiters();
	}
	
	public void setNode(ai.mc.MCNode n)
	{
		node = n;
		nodeSequence = buildDefaultSequence(node);
		buildDelimiters();
	}
	
	private ai.mc.MCNode[] buildDefaultSequence(ai.mc.MCNode parent)
	{
		Vector<ai.mc.MCNode> v = new Vector<ai.mc.MCNode>();
		
		if (parent!=null) v.add(parent);
		ai.mc.MCNode mc = null;
		ai.mc.MCNode p = parent;
		if (parent!=null)
		do
		{
			mc = (ai.mc.MCNode)p.findMostVisitedChild();
			if (mc!=null)
			{
				v.add(mc);
				p = mc;
			}
		} while (mc!=null && v.size()<=9);

		ai.mc.MCNode n[] = new ai.mc.MCNode[v.size()];
		v.copyInto(n);
		return n;
	}
	
	public int columns()
	{
		return (nodeSequence==null? 0: nodeSequence.length);
	}
	

	private void buildDelimiters()
	{
		int c = columns();
		rows = new int[c][];
		for (int k=0; k<c; k++)
		{
			rows[k] = rowDelimiters(nodeSequence[k]);
		}
	}
	
	public int[] rowDelimiters(ai.mc.MCNode n)
	{
		
		if (n.links==null || n.links.length==0) return null;
		int out[] = new int[n.links.length+1];
		double visits[] = new double[n.links.length];
		
		for (int k=0; k<visits.length; k++)
		{
			visits[k] = (n.links[k].child!=null? n.links[k].child.visits : 0.0);
		}
		
		double vistotal = 0.0;
		for (int k=0; k<visits.length; k++)
			vistotal += visits[k];
		
		if (vistotal<=0.0) return null;
		
		for (int k=0; k<visits.length; k++)
			visits[k] /= vistotal;
		
		double bottom = 0.0;
		out[0]=0;
		for (int k=0; k<n.links.length; k++)
		{
			double size = 0;
			bottom += size;
			out[k+1] = (int)bottom;
		}
		
		return out;
	}
	
	public static MCComponent[] makeComponent()
	{
		
		
		
		
		
		MCComponent[] out = {};
		//MCComponent[] out = {mc,mc,mc};
		
		return out;
	}

}
