package ai.mc;


import java.util.Random;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ai.player.Darkboard;
import ai.player.Player;
import core.Chessboard;
import core.Globals;
import core.Move;
import core.uberposition.Uberposition;
import core.uberposition.ZobristHash;

public class MCSTSNode {
	
	private static ExecutorService threadManager; //search thread manager
	protected static boolean isExecuting = false;
	protected static boolean isPondering = false;
	protected static boolean stop = false;
	protected static boolean viewerActivated = false;
	static boolean verbose = false;
	protected static Timer threadTimer = new Timer();
	protected static int nodeNumber = 10000;
	protected static MCSTSComponent viewer = null;
	protected static Random r = new Random();
	//-----------------------
	//HASH TABLES
	static ZobristHash<short[]> moveTable;
	static ZobristHash<ai.mc.MCSTSTransposition> transpositionTable;
	//-----------------------

	Uberposition state;
	long zobrist;
	long visitedStrategies;
	long successfulStrategies;
	int flags;
	int tacticalVisits = 0;
	int strategicVisits = 0;
	
	MCSTSNode bestChild = null;
	float bestChildValue = -100.0f;
	
	//float value = Float.NEGATIVE_INFINITY;
	float value = -1000.0f;
	float ownvalue = 0.0f;
	float linkvalue = 0.0f;
	float silentChance = 1.0f;
	
	short move;
	short childNumber;
	int moveIndex = -1;
	
	MCSTSNode children[];
	MCSTSNode parent;
	
	
	//public static float UCT_TACTICAL_CONSTANT_C = 0.5;
	public static float UCT_TACTICAL_CONSTANT_C = 2.0f;
	public static float UCT_STRATEGIC_CONSTANT_C = 0.5f;
	public static float STRATEGIC_PERSEVERANCE = 0.01f; //how badly we insist to reach a plan's max expectation
	public static float TACTICAL_CONSERVATION_CONSTANT = 0.5f;
	public static float STRATEGIC_BRANCHING_FACTOR = 1.5f;
	public static int STRATEGIC_DEPTH = 5;
	public static float STRATEGIC_THRESHOLD = 0.1f;
	public static float PRUNED_BESTCHILD_PENALTY = 0.1f; //how much you get penalized during selection if your best child has been pruned
	
	public static final int ALT_ILLEGAL = 0;
	public static final int ALT_CAPTURE = 1;
	public static final int ALT_PAWN = 2;
	public static final int ALT_CHECK = 3;
	
	public static final int FLAG_EXHAUSTED = 0x00000001; //node fully expanded, no more children to add
	public static final int FLAG_PRUNED =    0x00000002; //node is currently pruned, can't be visited
	
	public static MCSTSNode root;
	public static MCSTSNode roots[]; //each thread has its own 'root' containing some children
	
	public static boolean usePlans = true;
	
	public MCSTSNode()
	{
	}
	
	public MCSTSNode tacticalSelect(int set)
	{
		
		MCSTSNode result = null;

		if (tacticalVisits==0) result = this;
		else
		if (children==null || childNumber==0) result = null;
		else
		if (childNumber==1) result = children[0];
			
		float log = (float) Math.log(tacticalVisits+1);
		float bestVal = Float.NEGATIVE_INFINITY;
			
		for (int k=0; k<childNumber; k++)
		{
			//don't go where there are no nodes to be explored
			if (children[k].tacticalVisits>=children[k].strategicVisits) continue;
			float val = children[k].linkvalue+children[k].value+
				UCT_TACTICAL_CONSTANT_C*(float)Math.sqrt(log/(children[k].tacticalVisits+1.0));
			if (val>bestVal)
			{
				bestVal = val;
				result = children[k];
			}
		}

		
		if (result==this || result==null) return result;
		else return result.tacticalSelect(set);
	}
	
	public void tacticalEval2(int set)
	{
		float v;

		if (tacticalVisits>0 || parent==null) return; 
			
		Move m = short2Move(move);
		//state = (Uberposition)parent.state.getState(set,m);
		ownvalue = state.eval2(set);
		if (parent!=null) ownvalue = parent.ownvalue - state.risk(set);
		
		refineLink2(m);
		determineValue2();
		v = value;
			
		tacticalVisits++;
		
		if (parent!=null) 
		{
			parent.recalculate(this, v);
			parent.tacticalBackpropagate();
		}
	}
	
	public void tacticalEval(int set)
	{
		float v;

		if (tacticalVisits>0 || parent==null) return; 
			
		Move m = short2Move(move);
		//state = (Uberposition)parent.state.getState(set,m);
		ownvalue = state.eval(set);
		//ownvalue = state.evalAlt(set);
		refineLink(m);
		determineValue();
		v = value;
		
		if (value<root.ownvalue-0.1) flags |= FLAG_PRUNED; //prune yourself if too weak;
			
		tacticalVisits++;
		
		if (parent!=null) 
		{
			parent.recalculate(this, v);
			parent.tacticalBackpropagate();
		}
	}
	
	protected void refineLink(Move m)
	{
		float legal = parent.state.chanceOfLegality(m);
		float pawnTries = parent.state.chanceOfPawnTries(m);
		float cap = parent.state.chanceOfCapture(m);
		if (cap<0.05f) cap = 0.0f;
		float check = parent.state.chanceOfCheck(m);
		
		silentChance = legal*(1.0f-cap)*(1.0f-pawnTries);
		
		//alternatives = new MCNode[3];
		float altChances[] = new float[4];
		float altValues[] = new float[4];
		
		if (silentChance==1.0)
		{
			linkvalue = 0.0f;
			return;
		}
		
		int attackPower = state.attackPower(m.toX,m.toY);
		if (m.piece!=Chessboard.PAWN) attackPower++;
		
		altChances[ALT_ILLEGAL] = 1.0f-legal;
		altChances[ALT_CAPTURE] = legal*cap;
		altChances[ALT_PAWN] = legal*pawnTries;
		altChances[ALT_CHECK] = check;
		//silentChance = 1.0 - altChances[0] - altChances[1] - altChances[2];
		
		altValues[ALT_ILLEGAL] = ownvalue-0.05f;
		//altValues[ALT_PAWN] = (m.piece!=Chessboard.PAWN || attackPower<2? -1.0f : ownvalue-0.01f);
		if (m.piece==Chessboard.PAWN)
			altValues[ALT_PAWN] = (attackPower<3? -1.0f : ownvalue-0.01f);	
		else
			//altValues[ALT_PAWN] = (attackPower<3? -1.0f : ownvalue-0.01f);
			altValues[ALT_PAWN] = -1.0f;
		
		//altValues[ALT_PAWN] = (m.piece!=Chessboard.PAWN || attackPower<2? -1.0f : ownvalue-0.01f);
			//-1.0;
		//altValues[ALT_CAPTURE] = ownvalue+/*(1.0-parent.value)*/ 50.0f *cap*cap/(parent.state.pawnsLeft+parent.state.piecesLeft+1.0f)*(parent.state.evaluateWarSaga(m)-0.1f);
		altValues[ALT_CAPTURE] = (attackPower>=3? 1.0f : attackPower==2? ownvalue+0.1f: ownvalue-0.3f);
		if (altValues[ALT_CAPTURE]>1.0) altValues[ALT_CAPTURE] = 1.0f;
		altValues[ALT_CHECK] = (attackPower<1? -1.0f : ownvalue+0.01f);
		linkvalue = 0.0f;
		for (int k=0; k<4; k++) linkvalue += altValues[k]*altChances[k];
		linkvalue /= (altChances[0] + altChances[1] + altChances[2] + altChances[3]);
		if (linkvalue>1.0 || linkvalue<-1.0)
		{
			return;
		}
	}
	
	protected void refineLink2(Move m)
	{
		float legal = parent.state.chanceOfLegality(m);
		float pawnTries = parent.state.chanceOfPawnTries(m);
		float cap = parent.state.chanceOfCapture(m);
		float check = parent.state.chanceOfCheck(m);
		
		silentChance = legal*(1.0f-cap)*(1.0f-pawnTries);
		
		//alternatives = new MCNode[3];
		float altChances[] = new float[4];
		float altValues[] = new float[4];
		
		if (silentChance==1.0)
		{
			linkvalue = 0.0f;
			return;
		}
		
		int attackPower = parent.state.attackPower(m.toX,m.toY);
		
		altChances[ALT_ILLEGAL] = 1.0f-legal;
		altChances[ALT_CAPTURE] = legal*cap;
		altChances[ALT_PAWN] = legal*pawnTries;
		altChances[ALT_CHECK] = check;
		//silentChance = 1.0 - altChances[0] - altChances[1] - altChances[2];
		
		altValues[ALT_ILLEGAL] = ownvalue+0.01f;
		altValues[ALT_PAWN] = (m.piece!=Chessboard.PAWN || attackPower<1? ownvalue-state.pieceValue(m.toX,m.toY)*1.5f : ownvalue+0.05f);
		//altValues[ALT_PAWN] = ownvalue-state.pieceValue(m.toX,m.toY)*1.5;
			//-1.0;
		altValues[ALT_CAPTURE] = ownvalue+/*(1.0-parent.value)*/ 2.5f*(parent.state.evaluateWarSaga(m));
		//if (altValues[ALT_CAPTURE]>1.0) altValues[ALT_CAPTURE] = 1.0;
		altValues[ALT_CHECK] = (attackPower<1? ownvalue-state.pieceValue(m.toX,m.toY) : ownvalue-0.01f);
		linkvalue = 0.0f;
		for (int k=0; k<4; k++) linkvalue += altValues[k]*altChances[k];
		linkvalue /= (altChances[0] + altChances[1] + altChances[2] + altChances[3]);
		if (linkvalue>1.0 || linkvalue<-1.0)
		{
			return;
		}
	}
	
	public float treeValue()
	{
		float out = value;
		MCSTSNode node = parent;
		float average = value;
		float minimum = value;
		int nodes = 1;
		int malus = depth();
		while (node!=null)
		{
			/*float nonSilent = 1.0f-node.silentChance;
			float best = (1.0f-nonSilent)*(1.0f-TACTICAL_CONSERVATION_CONSTANT);
			float currentNode = 1.0f - nonSilent - best;
			out = nonSilent*node.linkvalue + best*out + currentNode*node.ownvalue;
			node = node.parent;*/
			float nonSilent = 1.0f-node.silentChance;
			float val = nonSilent*node.linkvalue + node.silentChance*node.ownvalue - 0.01f*malus;
			average = (average*nodes + val)/(nodes+1.0f);
			nodes++;
			malus--;
			if (val<minimum) minimum = val;
			node = node.parent;
		}
		//return out;
		return (average+minimum)/2.0f;
	}
	
	public int depth()
	{
		if (parent==null) return 0;
		else return (parent.depth()+1);
	}
	
	public Vector<Short> movesSoFar()
	{
		Vector<Short> out = new Vector<Short>();
		MCSTSNode node = this;
		while (node.parent!=null)
		{
			out.add(0, new Short(node.move));
			node = node.parent;
		}
		return out;
	}
	
	/**
	 * A dead end is a loop of nodes that goes nowhere and halts progress. We can detect
	 * it because a node gets pruned for being identical to the root and having a worse score.
	 * @param move
	 * @return
	 */
	public boolean deadEnd(short move)
	{
		bestChild = findChild(move);
		if (bestChild==null) return true;
		return deadEnd();
	}
	
	public boolean deadEnd()
	{
		MCSTSNode n = bestChild;
		while (n!=null)
		{
			if (n.zobrist==this.zobrist) return true;
			n = n.bestChild;
		}
		if (bestChild!=null) return (bestChild.deadEnd());
		else return false;
	}
	
	public void determineValue()
	{
		/*float nonSilent = (parent!=null? 1.0-parent.silentChance : 0.0);
		float best = (bestChild!=null? (1.0-nonSilent)*(1.0-TACTICAL_CONSERVATION_CONSTANT) : 0.0);
		if (bestChildValue<ownvalue && ((flags&FLAG_EXHAUSTED)==0)) best = 0.0; //don't let your children drag you down unless you've explored them all.
		float currentNode = 1.0 - nonSilent - best;
		
		float nonSilValue = (parent!=null? parent.linkvalue : 0.0);
		float bestVal = (bestChild!=null? bestChild.value : 0.0);*/
		float nonSilent = 1.0f-silentChance;
		float best = (bestChild!=null? (1.0f-nonSilent)*(1.0f-TACTICAL_CONSERVATION_CONSTANT) : 0.0f);
		if (bestChildValue<ownvalue && ((flags&FLAG_EXHAUSTED)==0)) best = 0.0f; //don't let your children drag you down unless you've explored them all.
		float currentNode = 1.0f - nonSilent - best;
		
		float nonSilValue = linkvalue;
		float bestVal = (bestChild!=null? bestChild.value : 0.0f);
		
		value = nonSilent*nonSilValue + best*bestVal + currentNode*ownvalue;
	}
	
	public void determineValue2()
	{
		float nonSilent = 1.0f-silentChance;
		float best = (bestChild!=null? (1.0f-nonSilent) : 0.0f);
		if (bestChildValue<ownvalue && ((flags&FLAG_EXHAUSTED)==0)) best = 0.0f; //don't let your children drag you down unless you've explored them all.
		float currentNode = 1.0f - nonSilent - best;
		
		float nonSilValue = linkvalue;
		float bestVal = (bestChild!=null? bestChild.value : 0.0f);
		
		value = nonSilent*nonSilValue + best*bestVal + currentNode*ownvalue;
	}
	
	public void recalculate(MCSTSNode child, float val)
	{
		boolean propagate = true;
		
		if (child==null) return;
		if (child==bestChild)
		{
			//if it decreased, check if it is still the best
			if (val<bestChildValue)
			{
				bestChildValue = val;
				for (int k=0; k<children.length; k++)
				{
					if (children[k]!=null && children[k].value>bestChildValue)
					{
						bestChild = children[k]; bestChildValue = children[k].value;
					}
				}
			}
		} else
		{
			if (child.value>bestChildValue)
			{
				bestChild = child; bestChildValue = child.value;
			} else propagate = false;
		}
		//this.value = this.ownvalue*CONSERVATION_CONSTANT+bestChildValue*(1.0-CONSERVATION_CONSTANT);
		determineValue();

		
		if (propagate && parent!=null) parent.recalculate(this,value);
	}
	
	public void tacticalBackpropagate()
	{ 
		tacticalVisits++;
		if (parent!=null) parent.tacticalBackpropagate();
	}
	
	public void tacticalCycle(int set)
	{
		MCSTSNode node = root.tacticalSelect(set);
		if (node!=null) node.tacticalEval(set);
	}
	
	/**
	 * 
	 * @param planNumber
	 * @return The number of nodes generated in the tree
	 */
	public MCSTSNode strategicSelect(int set, int planNumber)
	{

		//decide whether to continue or create a new branch
		
		float log = (float)Math.log(tacticalVisits+1.0);
		MCSTSNode best = null;
		float bestval = Float.NEGATIVE_INFINITY;
		
		int applicable = applicableChildren(planNumber);
		
		if (/*((flags&FLAG_EXHAUSTED)==0) && */
				(STRATEGIC_BRANCHING_FACTOR*Math.log(tacticalVisits+2.0)>applicable))
		{
			//create new branch
			MCSTSNode out = findAndAddMove(set,planNumber);
			if (out!=null) return out;
		}
		
		float prog[] = ai.mc.MCSTSTask.planProgress(zobrist, planNumber, this, null);
		for (int k=0; k<childNumber; k++)
		{
			MCSTSNode node = children[k];
			if ((children[k].flags&FLAG_PRUNED)!=0) continue;
			if ((node.visitedStrategies&(1<<planNumber))!=0 && ((node.successfulStrategies&(1<<planNumber))==0 || node.tacticalVisits==0))
			{
				float val = 0.0f;
				applicable++;
				//if we don't have a tactical score, use our own...
				//of course, if we don't have one either, then all children are on the same footing
				if (node.tacticalVisits>0) 
				{
					val = node.value; 
				} else val = this.value;
				if (usePlans && prog!=null) val += prog[children[k].moveIndex]*0.25;
				val += UCT_TACTICAL_CONSTANT_C*Math.sqrt(log/(node.tacticalVisits+1.0));
				if (children[k].bestChild!=null && (children[k].bestChild.flags&FLAG_PRUNED)!=0) val -= PRUNED_BESTCHILD_PENALTY;
				if (val>bestval)
				{
					bestval = val;
					best = node;
				}
			}
		}
		
		if (best==null)
		{
			return null;
		}

		return best;
				
	}
	
	public MCSTSNode newStrategicSelect(int set, int planNumber)
	{

		//decide whether to continue or create a new branch
		
		float log = (float)Math.log(tacticalVisits+1.0);
		MCSTSNode best = null;
		float bestval = Float.NEGATIVE_INFINITY;
		
		if (tacticalVisits==30 && ((flags&FLAG_EXHAUSTED)==0))
		{
			fillWithAllChildren(set);
		}
		
		int d = depth();
		
		
		float prog[] = ai.mc.MCSTSTask.planProgress(zobrist, planNumber, this, null);
		short mv[] = null;
		float p = ai.mc.MCSTSTask.planProgress(this, (short)-1, planNumber);
		if ((visitedStrategies&(1<<planNumber))==0 && d<=9)
		{
			visitedStrategies |= (1<<planNumber);
			mv = this.getStateMoves(set);
			prog = ai.mc.MCSTSTask.planProgress(zobrist, planNumber, this, mv);
			
			for (int k=0; k<mv.length; k++)
			{
				if (prog[k]>p || prog[k]==Float.POSITIVE_INFINITY)
				{
					MCSTSNode nod = addChild(set, mv[k], k);

					//nod.visitedStrategies |= (1<<planNumber);
					if (prog[k]==Float.POSITIVE_INFINITY) 
					{
					nod.successfulStrategies|=(1<<planNumber);
					}
				}

			}
		}
		
		for (int k=0; k<childNumber; k++)
		{
			MCSTSNode node = children[k];
			if ((children[k].flags&FLAG_PRUNED)!=0) continue;
			if (((node.successfulStrategies&(1<<planNumber))==0 || node.tacticalVisits==0))
			{
				float val = 0.0f;
				//if we don't have a tactical score, use our own...
				//of course, if we don't have one either, then all children are on the same footing
				if (node.tacticalVisits>0) 
				{
					val = node.value; 
				} else val = this.value;
				if (usePlans && prog!=null) val += prog[children[k].moveIndex]*0.1;
				if (usePlans && prog!=null && prog[children[k].moveIndex]<p) continue;
				val += UCT_TACTICAL_CONSTANT_C*Math.sqrt(log/(node.tacticalVisits+1.0));
				//if (children[k].bestChild!=null && (children[k].bestChild.flags&FLAG_PRUNED)!=0) val -= PRUNED_BESTCHILD_PENALTY;
				if (val>bestval)
				{
					bestval = val;
					best = node;
				}
			}
		}
		
		if (d>9) return best;
		
		if (best==null || childNumber<1)
		{
			if ((flags&FLAG_EXHAUSTED)!=0) return null;
			//create new child...
			if (mv==null) mv = getStateMoves(set);
			if (mv.length==childNumber)
			{
				flags |= FLAG_EXHAUSTED;
				return null;
			}
			int b[] = new int[mv.length];
			for (int k=0; k<childNumber; k++)
				b[children[k].moveIndex] = 1;
			int rand = r.nextInt(mv.length);
			for (int k=0; k<mv.length; k++)
			{
				int ind = (rand+k)%mv.length;
				if (b[ind]==0)
				{
					addChild(set,mv[ind],ind);
				}
			}
			
		}

		return best;
				
	}
	
	public short[] getStateMoves(int set)
	{
		short m[] = moveTable.get(zobrist);
		if (m!=null) return m;
		
		m = state.generateShortMoves(set, true, state.owner);
		moveTable.put(zobrist, m);
		return m;
	}
	
	public static short[] getStateMoves(int set, Uberposition u, Player p)
	{
		long z = u.getZobrist();
		short m[] = moveTable.get(z);
		if (m!=null) return m;
		
		m = u.generateShortMoves(set, true, p);
		moveTable.put(z, m);
		return m;
	}
	
	public MCSTSNode addChild(int set, short mov, int index)
	{
		if (children==null) children = new MCSTSNode[2];
		for (int k=0; k<childNumber; k++) if (children[k].move==mov) return children[k];
		if (childNumber==children.length)
		{
			//make a larger array
			int siz = childNumber*2;
			if (siz<2) siz = 2;
			MCSTSNode a[] = new MCSTSNode[siz];
			for (int j=0; j<childNumber; j++) { a[j] = children[j]; children[j] = null; }
			children = a;
		}
		MCSTSNode n = new MCSTSNode();
		n.state = state.evolveWithPlayerMove(set, short2Move(mov), -1, -1, Chessboard.NO_CAPTURE, Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0);
		n.state = n.state.evolveWithOpponentMove(set, 0, -1, -1, Chessboard.NO_CHECK, Chessboard.NO_CHECK);
		n.move = mov;
		n.moveIndex = index;
		n.parent = this;
		n.zobrist = Uberposition.getZobrist(zobrist, mov);
		children[childNumber++] = n;
		return n;
	}
	
	public MCSTSNode findChild(short mov)
	{
		if (children==null) return null;
		for (int j=0; j<childNumber; j++)
		{
			if (children[j].move==mov) return children[j];
		}
		return null;
	}
	
	/**
	 * Adds any missing children.
	 */
	public void fillWithAllChildren(int set)
	{
		if (state==null) return;
		short moves[] = getStateMoves(set);
		if (children==null) children = new MCSTSNode[moves.length];
		else if (children.length<moves.length)
		{
			MCSTSNode n[] = new MCSTSNode[moves.length];
			for (int k=0; k<children.length; k++) n[k] = children[k];
			children = n;
		}
		for (int k=0; k<moves.length; k++) addChild(set,moves[k],k);
		flags |= FLAG_EXHAUSTED;
			
	}
	
	public MCSTSNode pureMCSelect(MCGameSimulator sim, int set, int depth)
	{
		float log = (float)Math.log(tacticalVisits+2.0);
		
		MCSTSNode best = null;
		float bestval = Float.NEGATIVE_INFINITY;
		
		if (children==null || childNumber==0)
		{
			fillWithAllChildren(set);
			return this;
		}
		if (childNumber==0) { tacticalVisits++; return null;}
		int rn = r.nextInt(childNumber);
		for (int k=0; k<childNumber; k++)
		{
			MCSTSNode nd = children[(k+rn)%childNumber];
			float val = (float)((nd.tacticalVisits>0? nd.value : Float.POSITIVE_INFINITY)+1.0f*Math.sqrt(log/(nd.tacticalVisits+1.0)));
			if (val>bestval)
			{
				bestval = val;
				best = nd;
			}
		}
		
		if (best==null) return null;
		//if (parent!=null) best.pureMCEval(sim, set, depth);
		return (best.pureMCSelect(sim,set,depth));
	}
	
	public void pureMCEval(MCGameSimulator sim, int set, int depth)
	{
		MCSTSNode n = this;
		sim.nsIndex = 99;
		while (n!=null)
		{
			sim.nodeSeq[sim.nsIndex--] = n;
			n = n.parent;
		}
		float result = sim.simulateGameSequence(set, depth, 5);
		pureMCbackup(result);
	}
	
	public void pureMCbackup(float val)
	{
		value = (tacticalVisits==0? val : (value*tacticalVisits + val)/(tacticalVisits+1));
		tacticalVisits++;
		
		if (val==Float.NaN || value==Float.NaN)
		{
			return;
		}
		
		if (parent!=null) parent.pureMCbackup(val);
	}
	
	public MCSTSNode findAndAddMove(int set, int planNumber)
	{
		short bm = -1;
		float bestval = Float.NEGATIVE_INFINITY;
		int bestIndex = -1;
		float bestProgress = 0.0f;
		
		//we'll add tables later
		short mv[] = getStateMoves(set);
		float progress[] = ai.mc.MCSTSTask.planProgress(zobrist, planNumber, this, mv);

		for (int k=0; k<mv.length; k++)
		{
			short mov = mv[k];
			boolean ok = true;
			if ((flags&FLAG_EXHAUSTED)!=0)
			{
				ok = false;
				for (int c=0; c<childNumber; c++)
				{
					if ((children[c].visitedStrategies&(1<<planNumber))!=0) continue;
					if (children[c].move==mov)
					{
						ok = true; break;
					}
				}
			}
			else
			{
				for (int c=0; c<childNumber; c++)
				{
					if ((children[c].visitedStrategies&(1<<planNumber))==0) continue;
					//if ((children[c].successfulStrategies&(1<<planNumber))==1) continue;
					short m2 = children[c].move;
					if (mov==m2) { ok = false; break; }
				}
			}
			if (ok==false) continue;

			//float val = MCSTSTask.planProgress(this, planNumber);
			float val = progress[k];
			if (val>bestval)
			{
				bestval = val;
				bm = mov;
				bestProgress = progress[k]; //placeholder! Later on bestval will mean something else.
				bestIndex = k;
			}
			
			if (bestProgress==Float.POSITIVE_INFINITY) break; //can't top this...
		}
		
		if (bm>=0)
		{
			MCSTSNode nod = addChild(set,bm,bestIndex);
			nod.visitedStrategies |= (1<<planNumber);
			if (bestProgress==Float.POSITIVE_INFINITY)
				nod.successfulStrategies |= (1<<planNumber);
			return nod;
		} else 
		{
			//mark this node as sterile for the current plan
			//this.successfulStrategies |= (1<<planNumber);
			return null;
		}

	}
	
	public int applicableChildren(int planNumber)
	{
		if (children==null) return 0;
		int applicable = 0;
		for (int k=0; k<childNumber; k++)
		{
			MCSTSNode node = children[k];
			if (node==null)
			{
				node=null;
			}
			if ((node.flags&FLAG_PRUNED)!=0) continue;
			if ((node.visitedStrategies&(1<<planNumber))!=0 && (node.successfulStrategies&(1<<planNumber))==0) applicable++;
		}
		return applicable;
	}
	
	public int successfulChildren(int planNumber)
	{
		if (children==null) return 0;
		int applicable = 0;
		for (int k=0; k<childNumber; k++)
		{
			MCSTSNode node = children[k];
			if ((node.successfulStrategies&(1<<planNumber))!=0) applicable++;
		}
		return applicable;
	}
	
	
	public static Move short2Move(short i)
	{
		Move m = new Move();
		m.promotionPiece = Chessboard.QUEEN;
		m.piece = (byte)((i>>12)&7);
		m.fromX = (byte)((i>>9)&7);
		m.fromY = (byte)((i>>6)&7);
		m.toX = (byte)((i>>3)&7);
		m.toY = (byte)(i&7);
		return m;
	}
	
	public static short move2Short(Move m)
	{
		short s = (short)((m.piece<<12)|(m.fromX<<9)|(m.fromY<<6)|(m.toX<<3)|m.toY);
		return s;
	}
	
	public int moveRank(short m)
	{
		int rank = 0;
		MCSTSNode node = this.findChild(m);
		float v = node.value;
		
		for (int k=0; k<childNumber; k++)
		{
			if (children[k].move==m) continue;
			if (children[k].value>v) rank++;
		}
		
		return rank;
	}
	
	public short nthBestMove(int n)
	{
		for (int k=0; k<childNumber; k++)
		{
			if (moveRank(children[k].move)==n) return children[k].move;
		}
		
		return -1;
	}
	
	/**
	 * Flushes existing tree, typically because the referee's answer was not silent.
	 */
	public static void montecarloFlush()
	{
		if (viewer!=null && root!=null) viewer.setNode(null);
		root = null;
	}
	
	/**
	 * When the referee's answer is silent, we can keep the branch of the tree for the last move played.
	 * @param m
	 */
	public static void montecarloConfirmMove(int set, Move m)
	{
		if (root==null) return;
		MCSTSNode n = root.findChild(move2Short(m));
		if (n!=null)
		{
			if (n.state.scrap) n.state = (Uberposition)root.state.getState(set,m);
			root = n;
			root.parent = null;
			root.fillWithAllChildren(set);
		}
	}
	
	protected static Vector<Short>[] divideChildren(int threads)
	{
		Vector<Short> out[] = new Vector[threads];
		for (int k=0; k<out.length; k++) out[k] = new Vector<Short>();
		
		for (int k=0; k<root.childNumber; k++)
			out[k % out.length].add(new Short(root.children[k].move));
		
		return out;
	}
	
	protected static void setupRoots(int threadNumber)
	{
		Vector<Short> schedule[] = divideChildren(threadNumber);
		
		roots = new MCSTSNode[threadNumber];
		for (int k=0; k<threadNumber; k++) 
		{
			roots[k] = new MCSTSNode();
			roots[k].state = new Uberposition(root.state);
			roots[k].value = root.value;
			roots[k].ownvalue = root.ownvalue;
			roots[k].successfulStrategies = root.successfulStrategies;
			roots[k].visitedStrategies = root.visitedStrategies;
			roots[k].children = new MCSTSNode[schedule[k].size()];
			
			//we copy nodes from the root (we literally hijack its nodes)
			for (int j=0; j<schedule[k].size(); j++)
			{
				roots[k].children[j] = root.findChild(schedule[k].get(j).shortValue());
				roots[k].children[j].parent = roots[k]; //root still points to its children, but not the other way around
			}
			roots[k].childNumber = (short)schedule[k].size();
			roots[k].flags |= FLAG_EXHAUSTED; //and we specify the node cannot get any more children
		}
		
	}
	
	protected static void mergeRoots()
	{
		for (int k=0; k<root.childNumber; k++)
		{
			root.children[k].parent = root;
			root.tacticalVisits += root.children[k].tacticalVisits;
			root.strategicVisits += root.children[k].strategicVisits;
			if (root.children[k].value>root.bestChildValue)
			{
				root.bestChildValue = root.children[k].value;
				root.bestChild = root.children[k];
			}
		}
		root.determineValue();
	}
	
	public String toString()
	{
		String s = state.toString()+"\n";
		MCSTSNode node = this;
		while (node!=null)
		{
			if (move>0) s+=short2Move(node.move);
			if ((node.flags&FLAG_PRUNED)!=0) s+=" (P); ";
			else s+="; ";
			node = node.bestChild;
		}
		return s;
	}
	
	public static Move montecarloStrategicTreeSearch(int time, int threadNumber, Uberposition u, boolean plans)
	{
		//setup
		if (isExecuting)
		{
			if (!isPondering) return null;
			//else stop pondering!
		}
		isExecuting = true;
		isPondering = false;
		stop = false;
		usePlans = plans;
		
		nodeNumber = 10000;
		//nodeNumber = 2000;
		if (u!=null && u.owner!=null && u.owner.currentUmpire!=null && u.owner.currentUmpire.timedGame)
		{
			int tm = u.owner.currentUmpire.getTime();
			if (tm<60) nodeNumber = 5000;
			if (tm<30) nodeNumber = 2500;
			if (tm<15) nodeNumber = 1250;
			if (tm<5) nodeNumber = 500;
		}

		if(time < 3000) nodeNumber = 50; // Added to improve the speed of game

		if (moveTable==null) moveTable = new ZobristHash<short[]>(12);
		else moveTable.clear();
		if (transpositionTable==null) transpositionTable = new ZobristHash<ai.mc.MCSTSTransposition>(12);
		else transpositionTable.clear();

		ai.mc.MCSTSTask.flushTables();
		
		montecarloFlush(); //we'll see about preserving a part of the tree later on.
		
		if (viewer==null && Globals.hasGui && viewerActivated) viewer = MCSTSComponent.makeComponent();
		
		if (Uberposition.currentProcessorSetup()<threadNumber) Uberposition.setupForProcessors(threadNumber);
		
		if (threadManager==null)
		{
			threadManager = Executors.newCachedThreadPool();
		}
		
		if (root==null) 
		{
			root = new MCSTSNode();
			root.state = u;
			root.zobrist = root.state.getZobrist();
			root.fillWithAllChildren(0);
			root.ownvalue = root.state.eval(0);
			root.value = root.ownvalue;
			ai.mc.MCSTSTransposition tr = new ai.mc.MCSTSTransposition();
			tr.node = root;
			tr.treeValue = root.treeValue();
			transpositionTable.put(root.zobrist, tr);
			
		}
		else 
		{
			montecarloConfirmMove(0,short2Move(root.bestChild.move));
		}

		ai.mc.MCSTSTask.initTaskData();
		

		
		setupRoots(threadNumber);
		
		// now run the search...
		Future f[] = new Future[threadNumber];
		for (int k=0; k<threadNumber; k++) f[k] = threadManager.submit((Runnable)new ai.mc.MCSTSTask(k+1,time));
		threadTimer.schedule(new ai.mc.MCSTSTimerTask(), time);
		
		try
		{
			for (int k=0; k<threadNumber; k++) f[k].get();
			//TODO change this when we implement 'pondering' during opponent's move
			
			//threadManager.invokeAll(tasks);
		} catch (ExecutionException e) { e.printStackTrace(System.out); }
		catch (InterruptedException e2) { e2.printStackTrace(); }

		
		//done... merge the roots and return the best move
		isExecuting = false;
		stop = false;
		
		int visits = 0;
		for (int k=0; k<threadNumber; k++) visits += roots[k].tacticalVisits;
		if (verbose) System.out.println(""+visits+" nodes explored.");
		
		mergeRoots();
		
		if (viewer!=null) viewer.setNode(root);
		
		//MCSTSTask.getBestMove(root);
		
		boolean stop = false;
		if (root.bestChild==null) stop = true;
		else
		for (int k=0; k<root.childNumber; k++)
			if (root.children[k].value==Float.NaN) stop = true;
		
		if (stop)
		{
			// System.out.println("Error");
		}
		
		//System.out.println(root.bestChild);
		if(root.bestChild!=null) {
		if (root.deadEnd(root.bestChild.move))
			{
				//System.out.println("Dead end");
				short bestEffortMove = ai.mc.MCSTSTask.getBestPlanFulfillingMove();
				if (bestEffortMove!=-1)
					return short2Move(bestEffortMove);
				//else System.out.println("But no fix. :'(");
			}
		}
		
		//MCSTSTask.printPlanData();
		//MCSTSTask.printPlanFitness();
		//if (usePlans) 
		//return (short2Move(MCSTSTask.getBestMove(root)));
		//else 
		/*float bcv = root.bestChild.value;
		Vector<MCSTSNode> vc = new Vector<MCSTSNode>();
		//vc.add(root.bestChild);
		for (int k=0; k<root.childNumber; k++)
		{
			if (bcv-root.children[k].value<=0.03) vc.add(root.children[k]);
		}
		return (short2Move(vc.get(r.nextInt(vc.size())).move));*/
		
		//return (short2Move(root.bestChild.move));
		
		return (root.bestChild!=null? short2Move(root.bestChild.move): null);
	}
	
	public static Move pureMC(int time, int threadNumber, Uberposition u, int depth)
	{
		//setup
		if (isExecuting)
		{
			if (!isPondering) return null;
			//else stop pondering!
		}
		isExecuting = true;
		isPondering = false;
		stop = false;
		
		if (moveTable==null) moveTable = new ZobristHash<short[]>(12);
		else moveTable.clear();
		if (transpositionTable==null) transpositionTable = new ZobristHash<ai.mc.MCSTSTransposition>(12);
		else transpositionTable.clear();
		
		//MCSTSTask.flushTables();
		
		montecarloFlush(); //we'll see about preserving a part of the tree later on.
		
		if (viewer==null && Globals.hasGui && viewerActivated) viewer = MCSTSComponent.makeComponent();
		
		if (Uberposition.currentProcessorSetup()<threadNumber) Uberposition.setupForProcessors(threadNumber);
		
		if (threadManager==null)
		{
			threadManager = Executors.newCachedThreadPool();
		}
		
		if (root==null) 
		{
			root = new MCSTSNode();
			root.state = u;
			root.zobrist = root.state.getZobrist();
			root.fillWithAllChildren(0);
			root.ownvalue = root.state.eval(0);
			root.value = root.ownvalue;
			ai.mc.MCSTSTransposition tr = new ai.mc.MCSTSTransposition();
			tr.node = root;
			tr.treeValue = root.treeValue();
			transpositionTable.put(root.zobrist, tr);
			
		}
		else 
		{
			montecarloConfirmMove(0,short2Move(root.bestChild.move));
		}

		ai.mc.MCSTSTask.initTaskData();
		

		
		setupRoots(threadNumber);
		
		// now run the search...
		Future f[] = new Future[threadNumber];
		for (int k=0; k<threadNumber; k++) f[k] = threadManager.submit((Runnable)new MCPureTask(k+1,depth));
		threadTimer.schedule(new ai.mc.MCSTSTimerTask(), time);
		
		try
		{
			for (int k=0; k<threadNumber; k++) f[k].get();
			//TODO change this when we implement 'pondering' during opponent's move
			
			//threadManager.invokeAll(tasks);
		} catch (ExecutionException e) { e.printStackTrace(System.out); }
		catch (InterruptedException e2) { e2.printStackTrace(); }

		
		//done... merge the roots and return the best move
		isExecuting = false;
		stop = false;
		
		int visits = 0;
		for (int k=0; k<threadNumber; k++) visits += roots[k].tacticalVisits;
		if (verbose) System.out.println(""+visits+" nodes explored.");
		
		mergeRoots();
		
		if (viewer!=null) viewer.setNode(root);
		
		//MCSTSTask.getBestMove(root);
		
		MCSTSNode b = null; float best = Float.NEGATIVE_INFINITY;
		for (int k=0; k<root.childNumber; k++)
		{
			if (root.children[k].value>best)
			{
				b = root.children[k];
				best = b.value;
			}
		}
		if (b==null)
		{
			return null;
		}
		return short2Move(b.move);
	}
	
	public static void main(String args[])
	{
		String path = System.getProperty("user.home") + "/darkboard_data/";
		// System.out.println(path);
		Darkboard.initialize(path);
		
		Uberposition u = new Uberposition(true,new Player());
		float progress = u.attackProgress(4, 7, null);
		// System.out.println(u);
		// System.out.println(progress);
			Move mv[] = u.generateMoves(0, true, u.owner);
			for (int k=0; k<mv.length; k++)
			{
				float v = u.attackProgress(4, 7, mv[k]);
				if (v!=Float.POSITIVE_INFINITY && v>progress)
				{
					System.out.println(u.evolveWithPlayerMove(0, mv[k], -1, -1, Chessboard.NO_CAPTURE, Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0));
					// System.out.println(v);
				}
			
			}
	}
	
}
