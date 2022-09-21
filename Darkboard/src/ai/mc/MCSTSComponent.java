package ai.mc;

import java.util.Vector;

import core.Move;

public class MCSTSComponent{
	
	public static final int MINIMUM_ROW_HEIGHT = 10;
	// public static NumberFormatter nf = new NumberFormatter();

	ai.mc.MCSTSNode node;
	ai.mc.MCSTSNode nodeSequence[];
	int rows[][];
	
	public MCSTSComponent(ai.mc.MCSTSNode n)
	{

	}
	
	public void setNode(ai.mc.MCSTSNode n)
	{
		node = n;
		nodeSequence = buildDefaultSequence(node);
		buildDelimiters();
	}
	
	private ai.mc.MCSTSNode[] buildDefaultSequence(ai.mc.MCSTSNode parent)
	{
		Vector<ai.mc.MCSTSNode> v = new Vector<ai.mc.MCSTSNode>();
		
		if (parent!=null) v.add(parent);
		ai.mc.MCSTSNode mc = null;
		ai.mc.MCSTSNode p = parent;
		if (parent!=null)
		do
		{
			mc = p.bestChild;
			if (mc!=null)
			{
				v.add(mc);
				p = mc;
			}
		} while (mc!=null && v.size()<=3);

		ai.mc.MCSTSNode n[] = new ai.mc.MCSTSNode[v.size()];
		v.copyInto(n);
		return n;
	}
	
	public int columns()
	{
		return (nodeSequence==null? 0: nodeSequence.length);
	}
	
	public void paint()
	{
		
	}
	
	private void paintColumn(int c)
	{
		double col = columns()-1;
		int delim[] = rows[c];
		if (delim==null) return;
		
		double x1 = 1.0*c/col;
		double x2 = 1.0*(c+1.0)/col;
		
		int x10 = (int)x1; int x20 = (int)x2;
		int w = x20-x10;
		for (int k=0; k<delim.length-1; k++)
		{
		
		}
		
		for (int k=0; k<delim.length-1; k++)
		{
			Move move = ai.mc.MCSTSNode.short2Move(nodeSequence[c].children[k].move);
			String s="";
			if (move!=null) s = move.toString();
			if (nodeSequence[c].children[k]!=null)
			{
				ai.mc.MCSTSNode mc = nodeSequence[c].children[k];
				String v = ""+mc.value;
				if (v.length()>6) v = v.substring(0, 6);
				//s+=" v:"+(int)mc.visits+" val:"+v;
				s+=" v:"+mc.tacticalVisits+" val:"+v;
				//v = ""+nodeSequence[c].links[k].heuristicBias;
				v = ""+mc.ownvalue;
				if (v.length()>5) v = v.substring(0, 5);
				s+=" o:"+v;
				v = ""+nodeSequence[c].children[k].linkvalue;
				//v = ""+nodeSequence[c].children[k].state.risk(0);
				if (v.length()>6) v = v.substring(0, 6);
				s+=" r:"+v;
				v = ""+nodeSequence[c].children[k].silentChance;
				if (v.length()>6) v = v.substring(0, 6);
				
				s+=" %:"+v+" ("+nodeSequence[c].children[k].state.attackPower(move.toX, move.toY);
				/*for (int a=0; a<3; a++)
				{
					v = ""+nodeSequence[c].links[k].altChances[a];
					if (v.length()>4) v = v.substring(0, 4);
					s+=v; if (a!=2) s+=" "; else s+=")";
				}*/
			}
		}
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
	
	public int[] rowDelimiters(ai.mc.MCSTSNode n)
	{
		
		if (n.children==null || n.childNumber==0) return null;
		int out[] = new int[n.childNumber+1];
		int surplus = 1 - (n.childNumber*MINIMUM_ROW_HEIGHT);
		double visits[] = new double[n.childNumber];
		
		for (int k=0; k<visits.length; k++)
		{
			visits[k] = (n.children[k]!=null? n.children[k].tacticalVisits : 0.0);
		}
		
		double vistotal = 0.0;
		for (int k=0; k<visits.length; k++)
			vistotal += visits[k];
		
		if (vistotal<=0.0) return null;
		
		for (int k=0; k<visits.length; k++)
			visits[k] /= vistotal;
		
		double bottom = 0.0;
		out[0]=0;
		for (int k=0; k<n.childNumber; k++)
		{
			double size = (surplus>=0? MINIMUM_ROW_HEIGHT+surplus*visits[k] : 1.0*visits[k]);
			bottom += size;
			out[k+1] = (int)bottom;
		}
		
		return out;
	}
	
	public static MCSTSComponent makeComponent()
	{
		MCSTSComponent mc = new MCSTSComponent(null);
		return mc;
	}

	public void mousePressed() {
		// TODO Auto-generated method stub
		int x = 1; int y = 1;
		
		double where = 1.0;
		
		int c = (int)where;
		
		int r[] = rows[c];
		
		for (int k=1; k<r.length; k++)
			if (r[k]>=y)
			{
				ai.mc.MCSTSNode mc = nodeSequence[c].children[k-1];
				if (mc==null) return;
				
				//((MCStateNode)mc.links[0].child).expand2();
				Vector<ai.mc.MCSTSNode> v = new Vector<ai.mc.MCSTSNode>();
				ai.mc.MCSTSNode t = mc.parent;
				while (t!=null)
				{
					v.insertElementAt(t, 0);
					t = t.parent;
				}
				ai.mc.MCSTSNode[] sup = new ai.mc.MCSTSNode[v.size()];
				v.copyInto(sup);
				ai.mc.MCSTSNode[] sub = buildDefaultSequence(mc);
				nodeSequence = new ai.mc.MCSTSNode[sup.length+sub.length];
				System.arraycopy(sup, 0, nodeSequence, 0, sup.length);
				System.arraycopy(sub, 0, nodeSequence, sup.length, sub.length);
				if (nodeSequence.length>10)
				{
					ai.mc.MCSTSNode argh[] = new ai.mc.MCSTSNode[4];
					System.arraycopy(nodeSequence, 0, argh, 0, 10);
					nodeSequence = argh;
				}
				buildDelimiters();
				// repaint();
				return;
				
			}
		
		
	}

	public void mouseEntered() {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited() {
		// TODO Auto-generated method stub
		
	}

	public void mouseClicked() {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased() {
		// TODO Auto-generated method stub
		
	}

}
