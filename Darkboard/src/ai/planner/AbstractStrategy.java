package ai.planner;

import java.util.StringTokenizer;
import java.util.Vector;

import ai.chunk.regboard.RegBoardArray;
import ai.planner.Strategy.FlightPlan;
import ai.player.Darkboard;
import ai.tools.search.Path;
import core.Chessboard;
import core.Metaposition;
import reader.MiniReader.ReaderTag;

public class AbstractStrategy {
	
	boolean acceptablePiece[] = new boolean[7];
	boolean aimed;
	
	Area sourceArea;
	Area destinationArea;
	
	int maxMoves;
	
	public AbstractStrategy(ReaderTag tags, RegBoardArray boards)
	{
		if (!tags.tag.equals("plan")) return;
		
		String s = tags.subtags.get(0).value;
		
		StringTokenizer st = new StringTokenizer(s);
		while (st.countTokens()>0)
		{
			String t1 = st.nextToken();
			if (t1.equals("FROM"))
			{
				int from = Integer.parseInt(st.nextToken());
				if (!st.nextToken().equals("TO")) return;
				int to = Integer.parseInt(st.nextToken());
				String allowed = st.nextToken();
				
				sourceArea = new ai.planner.RegBoardArea(boards.getBoard(from));
				destinationArea = new ai.planner.RegBoardArea(boards.getBoard(to));
				
				for (int k=0; k<7; k++) acceptablePiece[k] = false;
				
				if (allowed.indexOf('P')!=-1) acceptablePiece[Chessboard.PAWN] = true;
				if (allowed.indexOf('N')!=-1) acceptablePiece[Chessboard.KNIGHT] = true;
				if (allowed.indexOf('B')!=-1) acceptablePiece[Chessboard.BISHOP] = true;
				if (allowed.indexOf('R')!=-1) acceptablePiece[Chessboard.ROOK] = true;
				if (allowed.indexOf('Q')!=-1) acceptablePiece[Chessboard.QUEEN] = true;
				if (allowed.indexOf('K')!=-1) acceptablePiece[Chessboard.KING] = true;
				if (allowed.indexOf('-')!=-1) acceptablePiece[Chessboard.EMPTY] = true;
			}
		}
	}
	
	public ai.planner.Strategy makeStrategy(Darkboard db, Vector<int[]> constraints, Metaposition m, Path outPath)
	{
		Vector<int[]> s = sourceArea.getAreaSquares(m);
		Vector<int[]> d = destinationArea.getAreaSquares(m);
		
		int bestwhat = 100000000;
		int bestx = -1;
		int besty = -1;
		ai.planner.Strategy bestStrategy = null;
		Path bestPath = null;
		
		for (int k=0; k<s.size(); k++)
		{
			int a[] = s.get(k);
			int piece = m.getFriendlyPiece(a[0], a[1]);
			if (!acceptablePiece[piece]) continue;

			ai.planner.Strategy concrete = new ai.planner.Strategy(db);
			
			concrete.setStartingContext(m);
			FlightPlan fp = concrete.new FlightPlan(a[0],a[1],d);
			fp.aimed = aimed;
			concrete.orders.add(fp);
			int dist = concrete.distance(new Vector(),m,null,m);
			if (dist>maxMoves) continue;
			
			//add constraints - basically, pieces that need to stay where they are (because,
			//for example, they are involved in other parts of the same, bigger plan).
			if (constraints!=null) 
			{
				for (int j=0; j<constraints.size(); j++)
				{
					int con[] = constraints.get(j);
					
					//if the constraint refers to the piece we are trying to move, ignore it
					if (con[0]==a[0] && con[1]==a[1]) continue;
					
					FlightPlan fp2 = concrete.new FlightPlan(con[0],con[1],con[0],con[1]);
					concrete.orders.add(fp2);
				}
			}
			
			Path p = concrete.getBestPath(m);
			if (p==null) continue;
			
			if (dist<bestwhat)
			{
				bestStrategy = concrete; bestwhat = dist;
				bestPath = p;
				bestx = a[0]; besty = a[1];
			}
			
		}
		
		
		if (bestStrategy!=null && constraints!=null)
		{
			//remove existing constraint on this piece, if necessary
			for (int k=0; k<constraints.size(); k++)
			{
				int c[] = constraints.get(k);
				if (c[0]==bestx && c[1]==besty)
				{
					constraints.remove(c);
					break;
				}
			}
			
			//add new constraint on this piece for the next piece of the plan
			int cn[] = {bestx,besty};
			constraints.add(cn);
		}
		
		if (outPath!=null && bestPath!=null)
		{
			outPath.copy(bestPath);
		}
		

		
		return bestStrategy;
	}

}
