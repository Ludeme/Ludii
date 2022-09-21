package ai.opening;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;
import java.util.Vector;

import core.Chessboard;
import core.Move;
import pgn.ExtendedPGNGame;
import pgn.ExtendedPGNGame.PGNMoveData;

public class OpeningTreeNode implements Serializable {
	
	public static double ALPHA = 0.1;
	public static int MIN_VISITS = 1; //you need to have encountered a position at least twice before it is counted
	public static Random r = new Random();
	
	public Move m;
	
	public byte check1;
	public byte check2;
	public byte cx;
	public byte cy;
	public byte tries;
	
	public int visits = 0;
	public int mctsvisits = 0;
	public double value = 0.5f;
	
	public Vector<OpeningTreeNode> moveChildren = null;
	public Vector<Float> probs = null;
	
	public Vector<OpeningTreeNode> messageChildren = null;
	
	public void changeProbabilities(int sel, boolean samePriority)
	{
		if (moveChildren==null) moveChildren = new Vector<OpeningTreeNode>();
		if (probs==null) probs = new Vector<Float>();
		
		for (int k=0; k<moveChildren.size(); k++)
		{
			float f = probs.get(k);
			if (sel==k)
			{
				if (samePriority) f = (float)(((f*visits)+1.0)/(visits+1.0));
				else f = (float)(1.0*ALPHA+(f*(1.0-ALPHA)));
			} else
			{
				if (samePriority) f = (float)((f*visits)/(visits+1.0));
				else f = (float)(f*(1.0-ALPHA));
			}
			probs.set(k, new Float(f));
		}
		
		visits++;
	}
	
	public OpeningTreeNode addChild(Move m, boolean samePriority)
	{
		if (moveChildren==null) moveChildren = new Vector<OpeningTreeNode>();
		if (probs==null) probs = new Vector<Float>();
		OpeningTreeNode otn = new OpeningTreeNode();
		otn.m = m;
		moveChildren.add(otn);
		probs.add(new Float(0.0f));
		changeProbabilities(moveChildren.size()-1,samePriority);
		return otn;
	}
	
	public OpeningTreeNode selectChild(Move m)
	{
		if (moveChildren==null) return null;
		for (int k=0; k<moveChildren.size(); k++)
			if (m.equals(moveChildren.get(k).m)) return moveChildren.get(k);
		
		return null;
	}
	
	public OpeningTreeNode selectChild(Move m, boolean samePriority)
	{
		if (moveChildren==null) moveChildren = new Vector<OpeningTreeNode>();
		if (probs==null) probs = new Vector<Float>();
		
		for (int k=0; k<moveChildren.size(); k++)
			if (m.equals(moveChildren.get(k).m)) 
			{
				changeProbabilities(k, samePriority);
				return moveChildren.get(k);
			}
		
		return addChild(m,samePriority);
	}
	
	public OpeningTreeNode selectMessage(byte ch1, byte ch2, byte capx, byte capy, byte tr, boolean create)
	{
		if (messageChildren==null) messageChildren = new Vector<OpeningTreeNode>();
		
		for (int k=0; k<messageChildren.size(); k++)
		{
			OpeningTreeNode otn = messageChildren.get(k);
			if (otn.check1==ch1 && otn.check2==ch2 && otn.cx==capx && otn.cy==capy && otn.tries==tr)
				return otn;
		}
		if (!create) return null;
		
		OpeningTreeNode otn2 = new OpeningTreeNode();
		otn2.check1 = ch1;
		otn2.check2 = ch2;
		otn2.cx = capx;
		otn2.cy = capy;
		otn2.tries = tr;
		messageChildren.add(otn2);
		return otn2;
	}
	
	public OpeningTreeNode chooseRandomChild()
	{
		if (moveChildren==null || visits<MIN_VISITS) return null;
		
		if (moveChildren.size()==0) return null;
		
		if (moveChildren.size()==1) return moveChildren.get(0);
		float f = r.nextFloat();
		for (int k=0; k<moveChildren.size(); k++)
		{
			float f2 = probs.get(k);
			if (f<=f2) return moveChildren.get(k);
			f -= f2;
		}
		return moveChildren.get(moveChildren.size()-1);
	}
	
	public void addGame(ExtendedPGNGame pgn, boolean white, int moveLimit, boolean samePriority)
	{
		PGNMoveData data;
		byte ch1 = Chessboard.NO_CHECK;
		byte ch2 = Chessboard.NO_CHECK;
		byte cx = -1;
		byte cy = -1;
		byte tr = 0;
		
		OpeningTreeNode otn = this;
		
		for (int k=0; k<pgn.getMoveNumber(); k++)
		{
			if (k>=moveLimit) return;
			
			for (int j=0; j<2; j++)
			{
				boolean w = j==0;
				data = pgn.getMove(w, k);
				if (data==null || data.finalMove==null) return;
				if (white==false && k==0 && j==0) continue; //skip white's first move if you are black
				if (w==white)
				{
					otn = otn.selectChild(data.finalMove, samePriority);
				} 
				ch1 = (byte)data.check1;
				ch2 = (byte)data.check2;
				cx = -1; cy = -1;
				if (data.capturewhat!=Chessboard.NO_CAPTURE)
				{
					cx = (byte)data.capturex;
					cy = (byte)data.capturey;
				}
				tr = (byte)data.pawntries;
				otn = otn.selectMessage(ch1, ch2, cx, cy, tr, true);
				
			}
		}
	}
	
	public void save(String s)
	{
		File f = new File(s);
		try
		{
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.close();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public OpeningTreeNode selectBestChild()
	{
		OpeningTreeNode o = null;
		double best = Double.NEGATIVE_INFINITY;
		
		if (moveChildren==null) return null;
		
		for (int k=0; k<moveChildren.size(); k++)
			if (moveChildren.get(k).mctsvisits>best)
			{
				o = moveChildren.get(k);
				best = o.mctsvisits;
			}
		
		return o;
	}
	
	public void prune()
	{
		if (moveChildren!=null)
		{
			OpeningTreeNode otn = selectBestChild();
			if (otn!=null)
			{
				for (int k=0; k<moveChildren.size(); k++)
					if (moveChildren.get(k)!=otn)
					{
						moveChildren.remove(k); k--;
					}
			}
			
			for (int k=0; k<moveChildren.size(); k++)
			{
				moveChildren.get(k).prune();
			}
		}
		
		if (messageChildren!=null)
		{
			for (int k=0; k<messageChildren.size(); k++)
			{
				messageChildren.get(k).prune();
			}
		}
		
	}
	
	public OpeningTreeNode selectBestMessage()
	{
		OpeningTreeNode o = null;
		double best = Double.NEGATIVE_INFINITY;
		
		if (messageChildren==null) return null;
		
		for (int k=0; k<messageChildren.size(); k++)
			if (messageChildren.get(k).mctsvisits>best)
			{
				o = messageChildren.get(k);
				best = o.mctsvisits;
			}
		
		return o;
	}
	
	public String toString()
	{
		String s = "";
		if (m!=null) s+="Move: "+m+" ";
		s+="Visits: "+mctsvisits+" ";
		s+="Value: "+value+" ";
		s+="Best: "+bestVariant();
		return s;
	}
	
	public String bestVariant()
	{
		String s = "";
		if (m!=null) s+=m.toString()+" ";
		OpeningTreeNode otn = selectBestChild();
		if (otn==null) otn = selectBestMessage();
		if (otn!=null) s+=otn.bestVariant();
		return s;
	}

}
