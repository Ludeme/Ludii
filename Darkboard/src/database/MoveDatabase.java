/*
 * Created on 30-set-05
 *
 */
package database;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import core.Move;

/**
 * @author Nikola Novarlic
 *
 */
public class MoveDatabase /*implements Serializable*/ {
	
	Vector moveData;
	public int gameSize=0;
	
	public class MoveComparator implements Comparator,Serializable
	{ 
		public int compare(Object o1, Object o2)
		{
			MoveData dp1 = (MoveData)o1;
			MoveData dp2 = (MoveData)o2;
			if (dp1.count!=dp2.count) return dp2.count-dp1.count;
			if (dp1.avgAppearance!=dp2.avgAppearance) return (int)(dp1.avgAppearance-dp2.avgAppearance);
			
			return (0);
		}
		public boolean equals(Object obj) { return false; }
	}
	
	public static MoveComparator mc;
	
	public class MoveData implements Serializable
	{
		public Move m;
		public int count=0;
		public double avgAppearance=0.0;
		public MoveDatabase owner;
		
		public void addCount() { count++; }
		public void addCount(int moveCount)
		{
			avgAppearance = (avgAppearance*count + moveCount)/(count+1); //maintain average
			count++;
		}
		public String toString()
		{
			String perc = (owner!=null && owner.gameSize!=0? ""+(100.0*count/owner.gameSize)+"%" : "");
			return (m!=null? m.toString(): "")+" x"+count+" ("+avgAppearance+" avg) "+perc;
		}
	}
	
	public int getMoveNumber() { return moveData.size(); }
	public Move getMove(int index) 
	{ 
		if (index<0 || index>=getMoveNumber()) return null;
		return getData(index).m; 
	}
	public boolean isThereMove(Move m)
	{
		for (int k=0; k<moveData.size(); k++)
		{
			if (getMove(k).equals(m)) return true;
		}
		return false;
	}
	public void addMove(Move mv)
	{
		if (!isThereMove(mv))
		{
			MoveData md = new MoveData();
			md.m = mv;
			md.owner = this;
			moveData.add(md);
		}
	}
	public MoveData getData(int index)
	{
		return ((MoveData)moveData.get(index));
	}
	public MoveData getData(Move m)
	{
		for (int k=0; k<moveData.size(); k++)
		{
			if (getMove(k).equals(m)) return getData(k);
		}		
		return null;
	}
	
	public void sortDatabase()
	{
		Collections.sort(moveData,mc);
	}
	
	public MoveDatabase()
	{
		moveData = new Vector();
		
		if (mc==null) mc = new MoveComparator();
	}
	
	public String toString()
	{
		String s = "";
		for (int k=0; k<moveData.size(); k++)
		{
			s += getData(k).toString() + "\n";
		}
		return s;
	}

}
