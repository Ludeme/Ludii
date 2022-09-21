package ai.planner;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Vector;

import ai.player.Darkboard;
import ai.tools.search.Path;
import ai.tools.search.PiecePathfinder;
import core.Chessboard;
import core.EvaluationFunction;
import core.Metaposition;
import core.Move;

/**
 * A Strategy is an instance of an AbstractStrategy applied to
 * a specific situation (context).
 * @author Nikola Novarlic
 *
 */
public class Strategy extends Plan {
	
	public class FlightPlan
	{
		int x;
		int y;
		Vector<int[]> possibleDestinations = new Vector();
		boolean aimed = false; //when true, there is only one destination and the piece needs only threaten it
		
		public FlightPlan(int sx, int sy, int ex, int ey)
		{
			x = sx; y = sy;
			int d[] = {ex,ey};
			possibleDestinations.add(d);
		}
		
		public FlightPlan(int sx, int sy, Vector<int[]> dests)
		{
			x = sx; y = sy;
			for (int k=0; k<dests.size(); k++) possibleDestinations.add(dests.get(k));
		}
	}
	
	public class DistanceData
	{
		Metaposition start;
		Metaposition end;
		Move m;
		int distance;
		float value;
	}
	
	public class DistanceComparator implements Comparator
	{
		Strategy owner;
		
		public DistanceComparator(Strategy o)
		{
			owner = o;
		}
		
		public int compare(Object o1, Object o2)
		{
			DistanceData m1 = (DistanceData)o1;
			DistanceData m2 = (DistanceData)o2;
			
			int d1 = m1.distance;
			int d2 = m2.distance;
			
			if (d1!=d2) return (d1-d2);
			
			EvaluationFunction ef = owner.owner.findAppropriateFunction(m1.start);
			
			if (m1.end==null)
			{
				m1.end = ef.generateMostLikelyEvolution(m1.start, m1.m);
				
			}
			if (m2.end==null)
			{
				m2.end = ef.generateMostLikelyEvolution(m2.start, m2.m);
			}
			
			if (m1.value==-10000000000.0f)
			{
				m1.value = owner.owner.evaluate(m1.start, m1.m, m1.end, new Vector());
			}
			if (m2.value==-10000000000.0f)
			{
				m2.value = owner.owner.evaluate(m2.start, m2.m, m2.end, new Vector());
			}
			
			if (m1.value<m2.value) return 1;
			if (m2.value<m1.value) return -1;
			return 0;
			
		}
		public boolean equals(Object obj) { return false; }
	}

	AbstractStrategy masterPlan;
	Metaposition startingContext;
	int startingDistance = -1;
	
	int deadline = -1; //number of moves to meet this objective
	
	public Vector<FlightPlan> orders = new Vector();
	Vector<Move> history = new Vector();
	Vector<Move> future = new Vector();
	
	public static Random randomizer = new Random();
	
	public Strategy(Darkboard own)
	{
		super(own);
		modifier = 6.0f;
	}
	
	public void isAdded()
	{
		setStartingContext(owner.simplifiedBoard);
		startingDistance = distance(history,getStartingContext(),null,getStartingContext());
		
		/*Path p = getBestPath(startingContext);
		if (p==null) System.out.println("NO PATH");
		else for (int k=0; k<p.moves.size(); k++) System.out.println(p.moves.get(k));*/
	}
	
	protected float fitness(Metaposition start, Metaposition end, Move m, Vector hist)
	{
		int distance = distance(hist,start,m,end);
		float ratio = 1.0f * distance / startingDistance /*/ hist.size()*/;
		return (1.0f-ratio);
	}
	
	public String toString()
	{
		return "Strategic plan";
	}
	
	public void evolveAfterMove(Metaposition root, Metaposition ev, Move m,
			int cap, int capx, int capy, int check1, int check2, int tries)
	{
		//update the position of your pieces...
		for (int k=0; k<orders.size(); k++)
		{
			int pos[] = currentPositionOfPiece(orders.get(k),m);
			orders.get(k).x = pos[0];
			orders.get(k).y = pos[1];
		}
		int nd = distance(history,ev,null,ev);
		if (nd<startingDistance)
		{
			startingDistance = nd;
			modifier *= 1.5f;
		}
		// System.out.println("NEW DISTANCE: "+startingDistance);
	}

	public void evolveAfterOpponentMove(Metaposition root, Metaposition ev,
			int capx, int capy, int check1, int check2, int tries)
	{
		//if the opponent captured one of the pieces involved... for now just remove everything
		for (int k=0; k<orders.size(); k++)
		{
			if (orders.get(k).x==capx && orders.get(k).y==capy)
			{
				owner.dashboard.deletePlan(this);
				return;
			}
		}
	}
	
	public void instantiateFromMetaposition(Metaposition mp)
	{
		setStartingContext(mp);
		//generate flight plans...
		
		orders.clear();
		
		//test plan...
		//FlightPlan fp1 = new FlightPlan(7,0,0,0);
		/*FlightPlan fp1 = new FlightPlan(0,0,7,0);
		orders.add(fp1);
		//FlightPlan fp2 = new FlightPlan(0,0,0,6);
		FlightPlan fp2 = new FlightPlan(7,0,0,0);
		orders.add(fp2);*/
		
		int rank = (owner.isWhite? 0: 7);
		int bonus = (owner.isWhite? 1: -1);
		
		Vector<int[]> dest = new Vector();
		int x1[] = {1,rank}; int x2[] = {1,rank+bonus}; int x3[] = {1,rank+bonus*2};
		dest.add(x1); dest.add(x2); dest.add(x3);
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int pc = mp.getFriendlyPiece(k, j);
				if (pc==Chessboard.ROOK || pc==Chessboard.QUEEN)
				{
					FlightPlan fp1 = new FlightPlan(k,j,dest);
					orders.add(fp1);
				}
			}
		
	}
	
	/**
	 * Given a starting situation, computes the best path to fulfilling the strategy
	 * @param start
	 * @return
	 */
	public Path getBestPath(Metaposition start, boolean canSplit)
	{
		int dist = distance(new Vector(),start,null,start);
		
		Path p = getBestPath(start,new Path(),dist);
		if (p!=null) return p;
		if (canSplit)
		{
			//now... no path was found, maybe we were too impatient to
			//get all the pieces there at once. Halve your objectives,
			//meet those and then meet the remaining half.
			if (orders.size()<2) return null; //need 2 or more FlightPlans.
			int remove = orders.size()/2;
			Vector<FlightPlan> v = new Vector();
			for (int k=0; k<remove; k++)
			{
				FlightPlan fp = orders.get(0);
				v.add(fp);
				orders.remove(0);
			}
			//find a path that meets the remaining objectives
			Path secondChance = getBestPath(start,true);
			//restore all objectives
			for (int k=0; k<remove; k++)
			{
				FlightPlan fp = v.get(0);
				orders.add(fp);
			}
			if (secondChance==null) return null;
			Metaposition afterFirstPart = runPath(start,secondChance);
			//Path secondChancePartTwo = getBestPath(afterFirstPart,true);
			int distance = distance(secondChance.moves,afterFirstPart,null,afterFirstPart);
			Path secondChancePartTwo = getBestPath(afterFirstPart,secondChance,distance);
			if (secondChancePartTwo!=null) return secondChancePartTwo;
			
		}
		
		return null;
	}
	
	public Path getBestPath(Metaposition start)
	{
		return getBestPath(start,true);
	}
	
	public Path getBestPath(Metaposition start, Path partial, int currentDistance)
	{
		Vector<Move> moves = start.generateMoves(true, owner);
		
		
		Vector<DistanceData> distances = new Vector();
		
		if (partial.moves.size()>20 || currentDistance>12) return null;
		
		for (int k=0; k<moves.size(); k++)
		{
			DistanceData dd = new DistanceData();
			dd.start = start;
			dd.value = -10000000000.0f;
			dd.m = moves.get(k);
			dd.end = owner.findAppropriateFunction(start).generateMostLikelyEvolution(start, dd.m);
			
			if (dd.end==null) continue;
			
			//see if this move passes muster according to tactical manager
			if (dd.end.canKeep(dd.m)==Discardable.YES)
			{
				dd.distance = distance(partial.moves, dd.start, dd.m, dd.end);
				
				//only add if it makes distance go down
				if (dd.distance<currentDistance) distances.add(dd);
		
			}
		}
		
		Collections.sort(distances, new DistanceComparator(this));
		
		for (int k=0; k<distances.size(); k++)
		{
			DistanceData dd = distances.get(k);
			Path p = new Path(partial,dd.m);
			
			if (dd.distance==0)
			{
				return p;
			}
			
			/*if (dd.distance>=currentDistance)
			{
				//no progress found... generate a subplan
				Vector<int[]> obstacles = obstaclesToPlan(start,partial,currentDistance,true);
				//eliminate random obstacles...
				Random r = new Random();
				while (obstacles.size()>0)
				{
					int ob[] = obstacles.get(r.nextInt(obstacles.size()));
					obstacles.remove(ob);
					
					Vector<int[]> destinations = whereToRelocateObstacle(start, partial, ob[0], ob[1], currentDistance);
					if (destinations.size()>0)
					{
						//generate the best plan to remove the obstacle...
						Strategy s2 = new Strategy(this.owner);
						FlightPlan fp = new FlightPlan(ob[0],ob[1],destinations);
						s2.startingContext = start;
						s2.orders.add(fp);
						Path pat = s2.getBestPath(start,false);
						if (pat!=null)
						{
							//check that the path is indeed better...
							Metaposition fin = runPath(start,pat);
							Path global = new Path(partial,pat);
							int nwDist = distance(global.moves,fin,null,fin);
							if (nwDist==0) return global;
							if (nwDist<currentDistance) return getBestPath(fin,global,nwDist);
						}
					}
				}
				return null;
			}*/
			
			
			
			Metaposition end = dd.end;
			if (end!=null)
			{
				end = end.generateMostLikelyOpponentMove(null);
			}
			
			Path out = getBestPath(end,p,dd.distance);
			//here you can decide whether you want to keep looking for more matches or just return this...
			if (out!=null) return out;
		}
		
		return null;
		
	}
	
	public void reactToOpponentMove(int capX, int capY, int check1, int check2, int tries)
	{
		
	}
	
	/**
	 * Compute the current position of previously determined pieces
	 * @param fp
	 * @return
	 */
	protected int[] currentPositionOfPiece(FlightPlan fp, Vector<Move> hist, Move last)
	{
		int result[] = {fp.x,fp.y};
		
		int size = hist.size();
		if (last!=null) size++;
		
		for (int k=0; k<size; k++)
		{
			Move m = (k==hist.size()? last : hist.get(k));
			if (m.fromX==result[0] && m.fromY==result[1])
			{
				result[0] = m.toX; result[1] = m.toY;
			}
			//one special case... castling
			if (m.piece==Chessboard.KING && m.toX==m.fromX+2 && result[0]==7 && result[1]==getStartingContext().getFirstRank())
			{
				result[0] = m.toX+1;
			}
			if (m.piece==Chessboard.KING && m.toX==m.fromX-2 && result[0]==0 && result[1]==getStartingContext().getFirstRank())
			{
				result[0] = m.toX-1;
			}
		}
		
		return result;
	}
	
	/**
	 * The opposite to currentPositionOfPiece. This goes backwards in time...
	 * @param x
	 * @param y
	 * @param hist
	 * @param last
	 * @return
	 */
	protected int[] oldPositionOfPiece(int x, int y, Vector<Move> hist, Move last)
	{
		int result[] = {x,y};
		
		int size = hist.size();
		if (last!=null) size++;
		
		for (int k=0; k<size; k++)
		{
			Move m;
			if (last!=null)
			{
				if (k==0) m = last; else m = hist.get(hist.size()-k);
			} else
			{
				m = hist.get(hist.size()-k-1);
			}
			
			if (m.toX==result[0] && m.toY==result[1])
			{
				result[0] = m.fromX; result[1] = m.fromY;
			}
			//one special case... castling
			if (m.piece==Chessboard.KING && m.toX==m.fromX+2 && result[0]==m.toX+1 && result[1]==getStartingContext().getFirstRank())
			{
				result[0] = 7;
			}
			if (m.piece==Chessboard.KING && m.toX==m.fromX-2 && result[0]==m.toX-1 && result[1]==getStartingContext().getFirstRank())
			{
				result[0] = 0;
			}
		}
		
		return result;
	}
	
	protected int[] currentPositionOfPiece(FlightPlan fp, Move m)
	{
		return currentPositionOfPiece(fp, history, m);
	}
	
	protected int distance(Vector<Move> hist, Metaposition before, Move m, Metaposition after)
	{
		int result = 0;
		
		for (int k=0; k<orders.size(); k++)
		{
			FlightPlan fp = orders.get(k);
			int[] pos = currentPositionOfPiece(fp,hist,m);
			if (!fp.aimed) result += PiecePathfinder.distance(after, pos[0], pos[1], fp.possibleDestinations, 100);
			else result += PiecePathfinder.distance(after, pos[0], pos[1], fp.possibleDestinations, 100,
				fp.possibleDestinations.get(0)[0],fp.possibleDestinations.get(0)[1]);
		}
		
		return result;
	}
	
	/**
	 * Find the obstacles that keep the plan from progressing...
	 * @return
	 */
	protected Vector<int[]> obstaclesToPlan(Metaposition start, Path history, int distance, boolean excludeTargets)
	{
		Metaposition copy = Metaposition.getChessboard(start);
		
		Vector<int[]> v = new Vector();
		Vector<int[]> result = new Vector();
		
		for (int k=0; k<orders.size(); k++)
		{
			FlightPlan fp = orders.get(k);
			if (excludeTargets) v.add(this.currentPositionOfPiece(fp, history.moves, null));
		}
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int piece = copy.getFriendlyPiece(k, j);
				if (piece==Chessboard.EMPTY) continue;
				boolean skip = false;
				for (int t=0; t<v.size(); t++)
				{
					int[] location = v.get(t);
					if (location[0]==k && location[1]==j) skip = true;
				}
				if (skip) continue;
				
				//now check if removing this piece would lower distance...
				copy.setEmpty((byte)k, (byte)j);
				int dist = distance(history.moves,copy,null,copy);
				if (dist<distance) 
				{
					int abc[] = {k,j};
					result.add(abc);
				}
				copy.setFriendlyPiece(k, j, piece);
				
			}
		//if (result.size()==0 && excludeTargets) return obstaclesToPlan(start, history, distance, false);
		return result;
	}
	
	protected Vector<int[]> whereToRelocateObstacle(Metaposition start, Path history, int x, int y, int distance)
	{
		Metaposition copy = Metaposition.getChessboard(start);
		Vector<int[]> result = new Vector();
		
		int piece = copy.getFriendlyPiece(x, y);
		copy.setEmpty((byte)x, (byte)y);
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int p2 = copy.getFriendlyPiece(k, j);
				if (p2==Chessboard.EMPTY)
				{
					copy.setFriendlyPiece(k, j, piece);
					int dist = distance(history.moves,copy,null,copy);
					if (dist<distance) 
					{
						int abc[] = {k,j};
						result.add(abc);
					}
					copy.setEmpty((byte)k, (byte)j);
				}
			}
		return result;
	}
	
	public Metaposition runPath(Metaposition m, Path p)
	{
		
		for (int k=0; k<p.moves.size(); k++)
		{
			m = owner.findAppropriateFunction(m).generateMostLikelyEvolution(m, p.moves.get(k));
			m = m.generateMostLikelyOpponentMove(p.moves.get(k));
		}
		
		return m;
	}
	
	/**
	 * This function is the basic evaluator of attack plans. Given a target square and the desired
	 * number of pieces that should be brought to attack it, it returns an estimate of how many moves
	 * it will take to set up the attack and the orders that need to be issued in order for this to happen.
	 * @param m
	 * @param targetX
	 * @param targetY
	 * @param attackStrength
	 * @param outPlan
	 * @return
	 */
	public int attackFeasibility(Metaposition m, int targetX, int targetY, int attackStrength, Vector<FlightPlan> outPlan)
	{
		int result = 0;
		Vector<int[]> pieces = new Vector();
		Vector<int[]> destinations = new Vector();
		
		int dst[] = {targetX,targetY};
		destinations.add(dst);
		
		//if target is a bad target, don't even start...
		if (m.isEmpty(targetX, targetY)) return 1000000;
		int count = 0;
		for (int k=-1; k<=1; k++)
		{
			for (int j=-1; j<=1; j++)
			{
				for (int t=1; t<=2; t++)
				{
					int tx = targetX+k*t;
					int ty = targetY+j*t;
					if (tx<0 || ty<0 || tx>7 || ty>7) break;
					if (m.getFriendlyPiece(tx, ty)!=Chessboard.EMPTY) 
					{
						if (k==0 && j==0) return 1000000;
						count++; break;
					}
				}
			}
		}
		if (count>=2) return 1000000;
		
		//System.out.println("Evaluating "+targetX+" "+targetY);
		
		//find the x pieces whose distance from the target is shortest.
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				
				int piece = m.getFriendlyPiece(k, j);
				if (piece!=Chessboard.EMPTY && piece!=Chessboard.PAWN)
				{
					int dist = PiecePathfinder.distance(m, k, j, destinations, 100);
					if (dist>=0)
					{
						int elem[] = {k,j,dist};
						boolean added = false;
						//insert in candidate vector
						for (int i=0; i<pieces.size(); i++)
						{
							if (dist<pieces.get(i)[2])
							{
								pieces.insertElementAt(elem, i);
								added = true;
								break;
							}
						}
						if (!added) pieces.insertElementAt(elem, pieces.size());
					}
				}
			}
		
		//now take the shortest distances
		//if (attackStrength>pieces.size()) attackStrength = pieces.size();
		if (attackStrength>pieces.size()) return -1;
		
		for (int k=0; k<attackStrength; k++)
		{
			int elem[] = pieces.get(k);
			result+=(elem[2]-1);
			FlightPlan fp = new FlightPlan(elem[0],elem[1],targetX,targetY);
			fp.aimed = true;
			outPlan.add(fp);
		}
		
		return result;
	}
	
	public Path deployAttack(Metaposition m, int x, int y, Vector<FlightPlan> plan)
	{
		this.setStartingContext(m);
		orders.clear();
		for (int k=0; k<plan.size(); k++) this.orders.add(plan.get(k));
		
		Path p = getBestPath(m);
		if (p!=null && p.moves!=null)
			for (int k=0; k<p.moves.size(); k++)
				System.out.println(p.moves.get(k));
		return p;
		
		//System.out.println("Target: "+x+" "+y);
		/*if (p!=null && p.moves!=null)
		for (int k=0; k<p.moves.size(); k++)
			System.out.println(p.moves.get(k));*/
	}
	
	public Path planAttack(Metaposition m, int x, int y, int minStrength)
	{
		Vector<FlightPlan> outPlan = new Vector();
		int dist = attackFeasibility(m, x, y, minStrength, outPlan);
		if (dist<0) return null;
		
		return deployAttack(m,x,y,outPlan);
	}
	
	public Path planAttack(Metaposition m, int minStrength)
	{
		int bestX = -1;
		int bestY = -1;
		int bestDistance = 100000;
		Vector<FlightPlan> bestPlan = null;
		
		int matrix[][] = new int[8][8];
		Vector<FlightPlan> plans[][] = new Vector[8][8];
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) matrix[k][j] = 1000000;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				Vector<FlightPlan> outPlan = new Vector();
				if (m.getFriendlyPiece(k, j)==Chessboard.EMPTY)
				{
					int dist = attackFeasibility(m, k, j, minStrength, outPlan);
					matrix[k][j] = dist;
					plans[k][j] = outPlan;
					//System.out.println("Attack on "+k+" "+j+" -> "+dist);
					if (dist>=0 && dist<bestDistance)
					{
						bestX = k; bestY = j; bestDistance = dist; bestPlan = outPlan;
					}
				}
			}
		
		while (bestX!=-1 && bestDistance<1000000)
		{
			// System.out.println("Attack on "+bestX+" "+bestY);
			Path p = deployAttack(m,bestX,bestY,bestPlan);
			if (p!=null) return p;
			
			matrix[bestX][bestY] = 1000000;
			bestDistance = 1000000;
			
			for (int k=0; k<8; k++)
				for (int j=0; j<8; j++)
					if (matrix[k][j]<bestDistance)
					{
						bestDistance = matrix[k][j];
						bestX = k; bestY = j; bestPlan = plans[k][j];
					}
			
		}
		
		return null;
	}
	
	protected void randomizeVector(Vector v)
	{
		int s = v.size();
		for (int k=0; k<v.size(); k++)
		{
			int a = randomizer.nextInt(s);
			int b = randomizer.nextInt(s);
			Object o1 = v.get(a);
			Object o2 = v.get(b);
			v.set(a, o2);
			v.set(b, o1);
		}
	}

	public void setStartingContext(Metaposition startingContext) {
		this.startingContext = startingContext;
	}

	public Metaposition getStartingContext() {
		return startingContext;
	}
	
}
