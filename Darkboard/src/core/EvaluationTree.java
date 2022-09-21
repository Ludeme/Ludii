/*
 * Created on 12-nov-05
 *
 */
package core;

import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;


/**
 * @author Nikola Novarlic
 *
 */
public class EvaluationTree{
	
	public static class ChildSorter implements Comparator
	{ 
		public int compare(Object o1, Object o2)
		{
			EvaluationTree dp1 = (EvaluationTree)o1;
			EvaluationTree dp2 = (EvaluationTree)o2;
			float v1 = dp1.value + dp1.minvalue;
			float v2 = dp2.value + dp2.minvalue;
			if (v2>v1) return 1;
			if (v2<v1) return -1;
			return 0;
		}
		public boolean equals(Object obj) { return false; }
	}
	
	private static ChildSorter cs = new ChildSorter();
	
	
	public core.Metaposition sc = null;
	public Move m = null;
	public Move bestChild = null;
	public float value;
	public float minvalue;
	public float staticvalue;
	
	Vector children = new Vector();
	
	public static File associatedFile = null;
	private static FileWriter writer = null;
	
	public EvaluationTree()
	{
		if (associatedFile==null)
		{
			associatedFile = new File(getClass().getResource("tree.log").getPath()); //new File("/home/nikola/Desktop/tree.log");
			try
			{
				writer = new FileWriter(associatedFile,true);
			} catch (Exception e)
			{
				e.printStackTrace();
				return;
			}
		}
	}
	
	public void saveToFile()
	{
		if (writer==null) return;
		try
		{
			writer.write(this.toString());
		} catch (Exception e)
		{
			e.printStackTrace();	
		}
	}
	
	public int getChildNumber() { return children.size(); }
	public EvaluationTree getChild(int k) { if (k<0 || k>=getChildNumber()) return null; 
		else return (EvaluationTree)children.get(k); }
	public EvaluationTree getChild(Move mov)
	{
		for (int k=0; k<getChildNumber(); k++)
		{
			EvaluationTree et = getChild(k);
			if (et.m.equals(mov)) return et;
		}
		return null;
	}
		
	public void addChild(EvaluationTree eval) { children.add(eval); }
	
	public void sortChildren(boolean recursively)
	{
		Collections.sort(children,cs);
		if (recursively) for (int k=0; k<getChildNumber(); k++) getChild(k).sortChildren(true);
	}
	
	public String toString()
	{
		//return getString(0);
		return (m!=null? m.toString() : "root");
	}
	
	public String getString(int depth)
	{
		String result = "";
		for (int k=0; k<depth; k++) result+="-";
		result+=(m!=null? m.toString() : "null") + " ";
		result+="v="+value+" min="+minvalue+" st="+staticvalue+"\n";
		for (int k=0; k<getChildNumber(); k++) result+=getChild(k).getString(depth+1);
		return result;
	}
	
	public String getBestSequence()
	{
		return getBestSequence(0);
	}
	
	public String getBestSequence(int k)
	{
		String result = "";
		if (m!=null)
		{
			result += /*k + ". "+*/m.toString()/*+ " v="+value+" min="+minvalue+" st="+staticvalue+"\n";*/+";";
		} else
		{
			//result += k + ". -null-"+ " v="+value+" min="+minvalue+" st="+staticvalue+"\n";
		}
		if (bestChild!=null)
		{
			EvaluationTree et = getChild(bestChild);
			if (et!=null) result += et.getBestSequence(k+1);
		}
		return result;
	}
	
	public String getExtendedRepresentation()
	{
		String result = "";
		result += toString()+"\n";
		result += "Value: "+this.value+"\n";
		result += "Static: "+this.staticvalue+"\n";
		
		return result;
	}
	
	
	//Implementation of TreeModel
	public void addTreeModelListener()
	{
	}
	
	public Object getChild(Object parent, int index)
	{
		return ((EvaluationTree)parent).getChild(index);
	}
	
	public int getChildCount(Object parent)
	{
		return ((EvaluationTree)parent).getChildNumber();
	}
	
	public void removeChild(int k)
	{
		children.remove(k);
	}
	
	public int getIndexOfChild(Object parent, Object child)
	{
		for (int k=0; k<getChildCount(parent); k++)
			if (getChild(parent,k)==child) return k;
			
		return -1;
	}

	public Object getRoot() { return this; }
	
	public boolean isLeaf(Object node)
	{
		return (getChildCount(node)==0);
	}
	
	public void removeTreeModelListener()
	{
	}
	
	public void valueForPathChanged()
	{
	}
}
