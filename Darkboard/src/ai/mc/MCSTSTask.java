package ai.mc;

import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

import core.Chessboard;
import core.uberposition.ZobristHash;

public class MCSTSTask implements Callable<Object>, Runnable{
	
	public static int MAX_PLANS = 64;
	
	private static int planOccurrences[] = new int[MAX_PLANS];
	private static float planValues[] = new float[MAX_PLANS];
	private static float planMaxExpectedValues[] = new float[MAX_PLANS];
	private static ZobristHash<float[]> planTable[];
	private static MCSTSNode planBestFits[] = new MCSTSNode[MAX_PLANS];
	private static float planBestFitness[] = new float[MAX_PLANS];
	
	private static int totalOccurrences = 0;
	private static ReentrantLock sharedWriteLock = new ReentrantLock();
	
	private static Vector<Short> nextMoves = new Vector<Short>();
	private static float guaranteedFitness = Float.NEGATIVE_INFINITY;
	private static int movesLeft = 0;
	
	int setNumber; //your ID
	int time;
	float counter = 0.0f;

	
	Random r = new Random();
	public static Random r2 = new Random();
	
	public MCSTSTask(int id, int t)
	{
		setNumber = id;
		time = t;
	}
	
	public static void flushTables()
	{
		if (planTable==null) planTable = new ZobristHash[MAX_PLANS];
		for (int k=0; k<MAX_PLANS; k++)
		{
			if (planTable[k]==null) planTable[k] = new ZobristHash<float[]>(8);
			else planTable[k].clear();
		}
	}
	
	public static void printPlanData()
	{
		for (int k=0; k<MAX_PLANS; k++)
		{
			System.out.println("Plan "+k+", "+planOccurrences[k]+"x, "+planValues[k]+"/"+planMaxExpectedValues[k]);
		}
	}
	
	public static void initTaskData()
	{
		for (int k=0; k<MAX_PLANS; k++)
		{
			guaranteedFitness = Float.NEGATIVE_INFINITY;
			planOccurrences[k] = 0;
			planBestFits[k] = null;
			planBestFitness[k] = Float.NEGATIVE_INFINITY;
			planValues[k] = MCSTSNode.root.ownvalue-0.5f;
			planMaxExpectedValues[k] = maxExpectedPlanValue(MCSTSNode.root, k);
			
			if (planProgress(MCSTSNode.root,(short)-1, k)==Float.POSITIVE_INFINITY)
				MCSTSNode.root.successfulStrategies |= (1<<k);
		}
		totalOccurrences = 0;
	}

	private void updatePlanData(int plan, float value)
	{
		sharedWriteLock.lock();
		planOccurrences[plan]++;
		totalOccurrences++;
		if (value>planValues[plan]) planValues[plan] = value;
		sharedWriteLock.unlock();
	}
	
	private int selectPlan(MCSTSNode node)
	{
		//use UCT here, too
		int bestPlan = 0;
		float bestValue = Float.NEGATIVE_INFINITY;
		float log = (float)Math.log(totalOccurrences+1);
		for (int k=0; k<MAX_PLANS; k++)
		{
			if (planMaxExpectedValues[k]==0.0f) continue;
			if ((node.successfulStrategies&(1<<k))!=0) continue;
			float v = planValues[k];
			float val = (float)(v /*+ (planMaxExpectedValues[k]-v)*MCSTSNode.STRATEGIC_PERSEVERANCE*/ + 5.0*Math.sqrt(log/(planOccurrences[k]+1.0)));
			if (val>bestValue)
			{
				bestValue = val;
				bestPlan = k;
			}
		}
		return bestPlan;
	}
	
	static float planProgress(MCSTSNode node, short move, int plan)
	{
		if (!MCSTSNode.usePlans)  return r2.nextFloat(); 
		return node.state.attackProgress(plan/8, plan%8, (move<0? null: MCSTSNode.short2Move(move)));
	}
	
	public static float[] planProgress(long z, int plan, MCSTSNode n, short[] moves)
	{
		float out[] = planTable[plan].get(z);
		if (out!=null) return out;
		if (moves==null) return null;
		
		out = new float[moves.length];
		for (int k=0; k<moves.length; k++)
		{
			out[k] = planProgress(n,moves[k],plan);
		}
		planTable[plan].put(z, out);
		return out;
	}
	
	public static float maxExpectedPlanValue(MCSTSNode node, int plan)
	{
		int x = plan/8; int y = plan%8;
		if (node.state.piece[x][y]+node.state.pawn[x][y]>0.1) return 1.0f; else 
		{
			int y2 = (node.state.white? y-1 : y+1);
			if (y2>=0 && y2<8 && node.state.allied[x][y2]==Chessboard.PAWN) return 1.0f;
			else return 0.0f;
		}
		//return ((node.state.piece!=null? node.state.piece[x][y]/node.state.pieceTotal : 0.0f)*3.0f + 
			//	(node.state.pawn!=null? node.state.pawn[x][y]/node.state.pawnTotal : 0.0f)*2.5f)/2.0f;
	}
	
	public static float maxInvestment(int plan, float progress)
	{
		float out = planMaxExpectedValues[plan]*progress/12.0f;
		if (out>planMaxExpectedValues[plan]) out = planMaxExpectedValues[plan];
		return out;
	}
	
	public static void updatePlanFitness(MCSTSNode n, int plan, float progress)
	{
		if (progress==Float.POSITIVE_INFINITY) return;
		float fitness = n.treeValue()+maxInvestment(plan,progress);
		
		//not enough progress, not worthy of being stored
		if (progress-planProgress(MCSTSNode.root,(short)-1,plan)<0.0) return;
		
		//if this plan can't guarantee at least as much fitness as the current best plan within
		//the specified amount of moves, discard it.
		if (guaranteedFitness!=Float.NEGATIVE_INFINITY)
		{
			int d = n.depth();
			MCSTSNode n2 = n;
			if (d>movesLeft)
				for (int k=0; k<d-movesLeft; k++) n2 = n2.parent; //reach the same level as the current plan
			if (n2.treeValue()+maxInvestment(plan, planProgress(n2, (short)-1,plan))<=guaranteedFitness) return;
		}
		
		if (fitness>planBestFitness[plan])
		{
			sharedWriteLock.lock();
			if (fitness>planBestFitness[plan])
			{
				planBestFitness[plan] = fitness;
				planBestFits[plan] = n;
			}
			sharedWriteLock.unlock();
		}
	}
	
	public static void printPlanFitness()
	{
		for (int k=0; k<MAX_PLANS; k++)
		{
			if (planBestFits[k]!=null)
			{
				System.out.println("Best fit for plan "+(k/8)+" "+(k%8)+": "+planBestFitness[k]);
				System.out.println("o:"+planBestFits[k].ownvalue+" v:"+planBestFits[k].value+" tree:"+planBestFits[k].treeValue());
				System.out.println(planBestFits[k].state);
			}
		}
	}
	
	public static short getPlanMove(int plan)
	{
		MCSTSNode node = planBestFits[plan];
		
		while (node.parent!=null && node.parent.parent!=null) node = node.parent;
		
		return node.move;
	}
	
	public static short getPlanMove(MCSTSNode n)
	{
		while (n.parent!=null && n.parent.parent!=null) n = n.parent;
		return n.move;
	}
	
	
	public static short getBestMove(MCSTSNode node)
	{
		float v = node.treeValue();
		float bestC = Float.NEGATIVE_INFINITY;
		MCSTSNode m = null;
		for (int k=0; k<node.childNumber; k++)
		{
			float val = node.children[k].treeValue();
			if (val>bestC) { bestC = val; m = node.children[k]; }
		}
		if (bestC>v+MCSTSNode.STRATEGIC_THRESHOLD)
		{
			System.out.println("Tactical decision: "+MCSTSNode.short2Move(m.move));
			nextMoves = null;
			guaranteedFitness = Float.NEGATIVE_INFINITY;
			movesLeft = 0;
			return m.move; //tactical decision
		}
		int bestPlan = -1;
		bestC = Float.NEGATIVE_INFINITY;
		for (int k=0; k<MAX_PLANS; k++)
		{
			if (planBestFitness[k]>bestC) { bestC = planBestFitness[k]; bestPlan = k; }
		}
		if (bestPlan==-1) 
		{
			if (nextMoves!=null && nextMoves.size()>0)
			{
				//use previously built plan - no better one has been found
				// System.out.println("Sticking to old plan");
				short out = nextMoves.remove(0);
				movesLeft--;
				return out;
			}
			// System.out.println("Using tactical bestChild");
			return node.bestChild.move;
		}
		
		short mv = getPlanMove(bestPlan);
		System.out.println("Strategic decision: "+MCSTSNode.short2Move(mv));
		System.out.println("Best fit for plan "+(bestPlan/8)+" "+(bestPlan%8)+": "+planBestFitness[bestPlan]);
		System.out.println("o:"+planBestFits[bestPlan].ownvalue+" v:"+planBestFits[bestPlan].value+" tree:"+planBestFits[bestPlan].treeValue());
		System.out.println(planBestFits[bestPlan].state);
		nextMoves = planBestFits[bestPlan].movesSoFar();
		nextMoves.remove(0);
		movesLeft = nextMoves.size();
		guaranteedFitness = planBestFitness[bestPlan];
		return mv;
	}
	
	public static short getBestPlanFulfillingMove()
	{
		MCSTSNode bestNode = null;
		float bestValue = Float.NEGATIVE_INFINITY;
		
		for (ai.mc.MCSTSTransposition t : MCSTSNode.transpositionTable)
		{
			if (t.node.successfulStrategies!=0 && t.treeValue>bestValue && t.node.move!=0)
			{
				bestNode = t.node;
				bestValue = t.treeValue;
			}
		}
		if (bestNode==null) return -1;
		else 
		{
			//System.out.println("Best Plan Move:");
			//System.out.println(bestNode.state);
			return (getPlanMove(bestNode));
		}
	}
	
	public static void flushPlans()
	{
		guaranteedFitness = Float.NEGATIVE_INFINITY;
		movesLeft = 0;
		nextMoves = null;
	}
	

	public Object call() 
	{
		int k=0;
		int effective=0;
		//while (!MCSTSNode.stop)

		//System.out.println("Beginning, thread "+setNumber);
		//try {Thread.sleep(3000);} catch (Exception x) {}
		while(k<MCSTSNode.nodeNumber)
		{
			int plan = -1;
			MCSTSNode node = MCSTSNode.roots[setNumber-1];
			MCSTSNode node2 = null;
			
			while (true)
			{
				if (plan==-1) plan = selectPlan(node);
				//node2 = node.strategicSelect(setNumber, plan);
				node2 = node.newStrategicSelect(setNumber, plan);
				if (node2==null || node2.tacticalVisits==0) break;
				
				if ((node2.successfulStrategies&(1<<plan))!=0) break;
				//if (planProgress(node2, plan)==float.POSITIVE_INFINITY) { break;}
				node = node2;
			}
			k++;
			if (node2==null) continue;
			node2.tacticalEval(setNumber);
			updatePlanData(plan,node2.value);
			updatePlanFitness(node2,plan,planProgress(node2, (short)-1, plan));

			ai.mc.MCSTSTransposition trans = MCSTSNode.transpositionTable.get(node2.zobrist);
			float tv = node2.treeValue();
			
			try
			{
			sharedWriteLock.lock();
			
			if (trans!=null)
			{
				if (trans.treeValue<tv)
				{
					trans.node.flags |= MCSTSNode.FLAG_PRUNED;
					trans.treeValue = tv;
					trans.node = node2;
				} else node2.flags |= MCSTSNode.FLAG_PRUNED;

			} else
			{
				trans = new ai.mc.MCSTSTransposition();
				trans.treeValue = tv;
				trans.node = node2;
				MCSTSNode.transpositionTable.put(node2.zobrist, trans);
			}
			} finally {sharedWriteLock.unlock();}
			effective++;
			 
		}
		
		if (MCSTSNode.verbose) System.out.println("Thread "+setNumber+", "+k+" nodes ("+effective+" effective)");
		return null;
	}

	public void run() {
		
			call();
	}

}
