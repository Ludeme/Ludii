package ai.mc;

import java.util.Random;

import ai.player.Darkboard;
import ai.player.Player;
import core.Chessboard;
import core.Move;
import core.uberposition.Uberposition;
import core.uberposition.ZobristHash;

public class MCGameSimulator {
	
	Random r = new Random();
	
	float danger[][] = new float[8][8];
	short tempMoves[] = new short[300];
	Player pl = new Player();
	public ai.mc.MCSTSNode nodeSeq[] = new ai.mc.MCSTSNode[100];
	public int nsIndex = 99;
	
	public void init(Uberposition u)
	{
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				danger[k][j] = u.protectionLevel(k, j);
			}
	}
	
	public float simulateGame(int set, int moveNumber)
	{
		boolean finished = false;
		int moveIndex = 0;
		int arrayIndex = nsIndex;
		Uberposition st = nodeSeq[++arrayIndex].state;
		int quiescence = 0;
		boolean cp;
		int pt;
		int cx=-1;
		int cy=-1;
		int cwhat;
		boolean followingPlan = true;
		
		while (!finished)
		{
			//find your move
			Move m = null;
			short mv[];
			
			if (followingPlan)
			{
				arrayIndex++;
				if (arrayIndex>=100) followingPlan = false;
				else 
				{
					m = ai.mc.MCSTSNode.short2Move(nodeSeq[arrayIndex].move);
					if (arrayIndex==99) followingPlan = false;
				}
			}
			if (!followingPlan) moveIndex++;
			
			if (m==null)
			{
				mv = ai.mc.MCSTSNode.getStateMoves(set, st, st.owner);
				if (mv.length<1) return st.eval3(set);
				if (quiescence>0)
				{
					int p = (cx<<3)|(cy);
					int applicable = 0;
					for (int k=0; k<mv.length; k++)
					{
						if ((mv[k]&0x003F)==p /*&& !pl.isShortMoveBanned(mv[k])*/) tempMoves[applicable++] = mv[k];
					}
					if (applicable>0) m = ai.mc.MCSTSNode.short2Move(tempMoves[r.nextInt(applicable)]);
					else m = ai.mc.MCSTSNode.short2Move(mv[r.nextInt(mv.length)]);
				} else m = ai.mc.MCSTSNode.short2Move(mv[r.nextInt(mv.length)]);

			}
			
			/*if (pl.isMoveBanned(m))
			{
				illegal++;
				if (illegal>4)
				{
					pl.unbanMoves();
					return st.eval3(set);
				}
				continue;
			}*/
			
			float legal = st.chanceOfLegality(m);
			if (r.nextFloat()>legal)
			{
				//illegal
				//st = st.evolveWithIllegalMove(set, m);
				//illegal++;
				//pl.banMove(m);
				return st.eval3(set)-1.0f;
			}
			float cap = st.chanceOfCapture(m);
			//if (m.piece==Chessboard.KING) cap /= 10.0f;
			float check = st.chanceOfCheck(m)*5;
			boolean ck = r.nextFloat()<check;
			float tries = st.chanceOfPawnTries(m);
			//do not actually check
			cp = r.nextFloat()<cap;
			pt = (r.nextFloat()<tries? 1 : 0);
			
			cwhat = Chessboard.NO_CAPTURE;
			if (cp && st.pawn!=null && st.piece!=null)
			{
				followingPlan = false;
				if (m.toX==cx && m.toY==cy) quiescence++; else quiescence=0;
				float pieceratio = st.piece[m.toX][m.toY]+(st.piece[m.toX][m.toY]+st.pawn[m.toX][m.toY]);
				if (r.nextFloat()>pieceratio) cwhat = Chessboard.CAPTURE_PAWN;
				else cwhat = Chessboard.CAPTURE_PIECE;
			} else { quiescence = 0; cp = false; }
			cx = (cp? m.toX : -1);
			cy = (cp? m.toY : -1);
			
			st = st.evolveWithPlayerMove(set, m, cx, cy, cwhat, Chessboard.NO_CHECK, Chessboard.NO_CHECK, pt);
			//pl.unbanMoves();
			
			int mypt = r.nextFloat()<st.chanceOfYourPawnTries()? 1 : 0;
			
			if ((cp || ck || pt>0)  && st.piece!=null)
			{
				cx = m.toX; cy = m.toY;
				float chanceOfRecapture = 1.0f;
				int captures = (quiescence-1)/2;
				if (ck) captures = 1;
				for (int k=0; k<captures; k++) chanceOfRecapture*=0.5f;
				if (r.nextFloat()<chanceOfRecapture)
				{
					followingPlan = false;
					quiescence++;
					if (st.allied[cx][cy]==Chessboard.KING) return st.eval3(set)-10.0f;
					st = st.evolveWithOpponentMove(set, mypt, cx, cy, Chessboard.NO_CHECK, Chessboard.NO_CHECK);
				} else
				{	
					quiescence = 0; cx = -1; cy = -1;
					st = st.evolveWithOpponentMove(set, mypt, -1, -1, Chessboard.NO_CHECK, Chessboard.NO_CHECK);
				}
			} else
			{
				quiescence = 0; cx = -1; cy = -1;
				int iterations = 0;
				if (r.nextFloat()<0.1f && iterations<1000)
				{
					float f = 1.0f;
					int xx = r.nextInt(8);
					int yy = r.nextInt(8);
					if (st.allied[xx][yy]!=Chessboard.EMPTY)
					{
						f -= danger[xx][yy];
						if (f<0.0f)
						{
							cx = xx; cy = yy; quiescence++; iterations = 10000;
						}
					}
					
					iterations++;
				}
				
				if (cx>=0 && st.allied[cx][cy]==Chessboard.KING) return st.eval3(set)-3.0f;
				
				st = st.evolveWithOpponentMove(set, mypt, cx, cy, Chessboard.NO_CHECK, Chessboard.NO_CHECK);
			}
			
			
			if (moveIndex>=moveNumber && quiescence==0) break;
		}
		
		return st.eval3(set);
	}
	
	public float simulateGameSequence(int set, int moveNumber, int gameNumber)
	{
		float total = 0.0f;
		//init(.state);
		for (int k=0; k<gameNumber; k++)
		{
			total += simulateGame(set,moveNumber);
		}
		return total/gameNumber;
	}
	
	public static void main(String args[])
	{
		String path = System.getProperty("user.home") + "/darkboard_data/";
		Darkboard.initialize(path);

		ai.mc.MCSTSNode n = new ai.mc.MCSTSNode();
		ai.mc.MCSTSNode ar[] = new ai.mc.MCSTSNode[1];
		ar[0] = n;
		n.state = new Uberposition(true,new Player());
		
		MCGameSimulator sim = new MCGameSimulator();

		ai.mc.MCSTSNode.moveTable = new ZobristHash<short[]>(14);
		sim.init(n.state);
		float val = 0.0f;
		int tries = 0;
		
		while (tries<10)
		{
			//float a = sim.simulateGame(0, n, 10, null);
			float a = sim.simulateGame(0,10);
			val = (val*tries+a)/(tries+1);
			tries++;
			// System.out.println(a);
			if (tries%1000==0) { System.out.println(val); tries = 0; }
		}
	}

}
