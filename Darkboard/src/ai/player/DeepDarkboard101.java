package ai.player;

import ai.opening.OpeningTreeNode;
import core.Chessboard;
import core.Move;

public class DeepDarkboard101 extends DeepDarkboard10 {
	
	OpeningTreeNode wtn, btn;
	OpeningTreeNode currentNode;
	OpeningTreeNode fallbackNodes;
	int illegalCounter = 0;
	boolean firstMove = true;

	public DeepDarkboard101(boolean w, OpeningTreeNode wh, OpeningTreeNode bl)
	{
		super(w);
		
		playerName = "Deep Darkboard (Profiler)";
		wtn = wh;
		btn = bl;
		currentNode = (w? wh : bl);
		
		receivedUmpireMessages = true; //to disable normal opening play
	}
	
	public DeepDarkboard101(boolean w, OpeningTreeNode wh, OpeningTreeNode bl, String n)
	{
		super(w);
		
		playerName  = "Profiler "+n;
		wtn = wh;
		btn = bl;
		currentNode = (w? wh : bl);
		
		receivedUmpireMessages = true; //to disable normal opening play
	}
	
	public Move getNextMove()
	{
		if (currentNode==null) return super.getNextMove();
		if (currentNode.moveChildren==null || currentNode.moveChildren.size()==0)
		{
			currentNode = null;
			return super.getNextMove();
		}

		OpeningTreeNode best = null;
		double bestValue = -1.0;
		
		best = currentNode.chooseRandomChild();
		// System.out.println("Next move ++++++++++++++++" + best.m);
		/*for (int k=0; k<currentNode.moveChildren.size(); k++)
		{
			OpeningTreeNode otn = currentNode.moveChildren.get(k);
			if (otn.m == null || this.isMoveBanned(otn.m)) continue;
			double v = otn.value+moveRandomizer.nextFloat()*0.05;
			if (v>bestValue)
			{
				bestValue = v;
				best = otn;
			}
		}*/
		
		if (best==null)
		{
			currentNode = null;
			return super.getNextMove();
		}
		
		// System.out.println("Playing opening");
		currentNode = currentNode.selectChild(best.m);
		// System.out.println("Next move ++++++++++++++++" + currentNode.selectBestChild().m);
		lastMove = best.m;
		this.db.lastMove = best.m;
		return best.m;
		
	}
	
	public String communicateLegalMove(int capture, int oppTries, int oppCheck, int oppCheck2)
	{
		if (currentNode!=null)
		{
			int cx = -1; int cy = -1;
			if (capture!=Chessboard.NO_CAPTURE) { cx = lastMove.toX; cy = lastMove.toY; }
			// if(currentNode.selectBestChild() != null) // to be removed
			currentNode = currentNode.selectMessage((byte)oppCheck, (byte)oppCheck2, (byte)cx, (byte)cy, (byte)oppTries, false);
			//if (currentNode!=null) backpropagation.add(currentNode);
		}
		return super.communicateLegalMove(capture, oppTries, oppCheck, oppCheck2);
	}
	
	public String communicateUmpireMessage(int capX, int capY, int tries, int check, int check2, int captureType)
	{
		if (currentNode!=null && (isWhite||!firstMove))
		{
			// if(currentNode.selectBestChild() != null) // to be removed
			currentNode = currentNode.selectMessage((byte)check, (byte)check2, (byte)capX, (byte)capY, (byte)tries, false);
			//if (currentNode!=null) backpropagation.add(currentNode);
		}
		firstMove = false; //aligns black to the first move
		return super.communicateUmpireMessage(capX, capY, tries, check, check2, captureType);
	}
}
