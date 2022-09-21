package ai.player;

import java.util.Random;
import java.util.Vector;

import ai.mc.MCSTSNode;
import ai.mc.MCSTSTask;
import core.Chessboard;
import core.Globals;
import core.Metaposition;
import core.Move;
import core.uberposition.Uberposition;
import database.Opening;
import database.OpeningBook;


/**
* This child of AIPlayer implements the actual standard AI Player used
* to play serious Kriegspiel.
* @author Nikola Novarlic
*
*/
public class DeepDarkboard10 extends AIPlayer {
	
	public static DeepDarkboard10 dbArchetype = null;
	public boolean usePlans = true;
	
	/**
	 * This variable is increased when the engine detects a move loop,
	 * and decreased when it doesn't. Sometimes it is necessary to
	 * reverse the latest move for a logical reason (e.g. hit'n'run).
	 * Only when this variable goes over a certain threshold do we have a loop.
	 */
	public int loopThreshold = 0;

	protected Vector enemyTracks = new Vector();
	public Vector moveVector = new Vector(); //all the moves so far
	Darkboard db; //we keep a copy of Darkboard around to handle the endgame... it does that well.
	

	Random moveRandomizer;
	Opening chosenOpening;
	boolean lastMoveIllegal; //true if the last move was not legal
	public boolean receivedUmpireMessages = false; //whether the player has received umpire messages yet
	public int moveNumber2 = 0;
	int capx,capy,check1,check2,tries;
	int bestMoveIndex; 
	/*in order to save time, we do not recalculate everything on each illegal move. Instead,
	 * we just fire the second, third, etc. best move in the list until we either succeed, or 
	 * cross a certain threshold.*/
	
	public static final int GAME_OPENING = 0;
	public static final int GAME_MIDGAME = 1;
	public static final int GAME_FINAL = 2;
	int gameStage = GAME_OPENING;
	double totalMaterial;
	
	public int firstRank;
	public int pawnOffset;
	
	Vector evaluationFunctionVector = new Vector();

	public DeepDarkboard10(boolean w)
	{
		//System.out.println("Loading AI player");

		this.isWhite = w;
		
		playerName = "Deep Darkboard 1.0";
		
		db = new Darkboard(w);
		
		moveRandomizer = new Random();
		
		//totalMaterial = totalMaterial();
		firstRank = (isWhite? 1 : 6);
		pawnOffset = (isWhite? 1 : -1);
		
		lastMoveIllegal = false;
		bestMoveIndex = 0;
		
		//choose the appropriate opening book
		OpeningBook ob = (isWhite? Globals.whiteOpeningBook : Globals.blackOpeningBook);
		if (ob!=null)
		{
			//System.out.println("Loading opening");
			chosenOpening = ob.getOpening(moveRandomizer.nextInt(ob.size()));
			//System.out.println(chosenOpening);
		}
		
		simplifiedBoard = Metaposition.getChessboard(this);
		simplifiedBoard.setup(isWhite);
		
		complexBoard = new Uberposition(isWhite,this);
		MCSTSNode.montecarloFlush();
	}
	
	/* Overriding of Player functions. */
	
	/**
	 * Notifies the player that we are not starting from the usual position.
	 */
	public void startFromAlternatePosition(Metaposition m)
	{
		super.startFromAlternatePosition(m);
		
		this.receivedUmpireMessages = true; //disable the opening book!
	}
	
	/**
	 * Calculates the best move!
	 */
	public Move getNextMove()
	{		
		try
		{
			//stick to the opening you chose as long as the referee is silent
			if (!receivedUmpireMessages && chosenOpening!=null && moveNumber2<chosenOpening.getMoveNumber()) 
			{
				lastMove = chosenOpening.getMove(this.moveNumber2);
				communicateObject("Opening book: "+lastMove.toString()+", length "+chosenOpening.getMoveNumber(), null, MESSAGE_TYPE_MOVE);
			}
			else
			{
				lastMove = null;
				if (capx>=0 || check1!=Chessboard.NO_CHECK || tries>0) lastMove = simpleReactionEvaluate();
				if (lastMove==null && simplifiedBoard.pawnsLeft==0 && simplifiedBoard.piecesLeft==0) 
				{
					db.currentUmpire = this.currentUmpire;
					lastMove = db.getNextMove();
				}
				if (lastMove==null) lastMove = MCSTSNode.montecarloStrategicTreeSearch(2000, Globals.threadNumber, complexBoard,usePlans);
				if (lastMove==null)
				{
					db.currentUmpire = this.currentUmpire;
					lastMove = db.getNextMove();
				}
			} 
		} catch (Exception e)
		{
			communicateObject("Exception: "+e.toString(), e.getMessage(), MESSAGE_TYPE_ERROR);
			e.printStackTrace();
		}
		db.lastMove = lastMove;
		return (lastMove);
	}
	
	/**
	 * Reacts to the notification of an illegal move.
	 */
	public void communicateIllegalMove(Move m) 
	{
		
		super.communicateIllegalMove(m);
		
		//to be improved, you can also calculate some info.
		lastMoveIllegal = true;
		//Metaposition.banMove(m);
		
		//we ban the move, and any move that goes further than that.
		banMoveSequence(m);
		
		MCSTSNode.montecarloFlush();
		MCSTSTask.flushPlans();
		
		receivedUmpireMessages = true;
		
		db.currentUmpire = this.currentUmpire;
		db.communicateIllegalMove(m);
		
		Metaposition n = Metaposition.evolveAfterIllegalMove(simplifiedBoard,m,capx,capy,check1,check2,tries);
		
		//-----------------------
		complexBoard = complexBoard.evolveWithIllegalMove(0,m);
		//System.out.println(complexBoard);
		//-----------------------

		simplifiedBoard = n;		
	}
	
	private void banMoveSequence(Move m)
	{
		banMove(m);
	}
	
	/**
	 * Updates the internal representation after having been told the latest move was legal.
	 */
	public String communicateLegalMove(int capture, int oppTries, int oppCheck, int oppCheck2)
	{
		super.communicateLegalMove(capture, oppTries, oppCheck, oppCheck2);
		
		lastMoveIllegal = false;
		bestMoveIndex = 1;
		moveNumber2++;
		if (capture!=Chessboard.NO_CAPTURE || oppTries!=0 || oppCheck!=Chessboard.NO_CHECK)
		{
			receivedUmpireMessages = true;
			MCSTSNode.montecarloFlush();
			MCSTSTask.flushPlans();
		}	
		unbanMoves();
		moveVector.add(lastMove);
		
		db.currentUmpire = this.currentUmpire;
		db.communicateLegalMove(capture, oppTries, oppCheck, oppCheck2);

		Metaposition m = evolveAfterMoveTopLevel(simplifiedBoard,lastMove,
				capture,lastMove.toX,lastMove.toY,oppCheck,oppCheck2,oppTries);
		
		//-----------------------
		complexBoard = complexBoard.evolveWithPlayerMove(0,lastMove, lastMove.toX, lastMove.toY, 
				capture, oppCheck, oppCheck2, oppTries);
		//System.out.println(complexBoard);
		//-----------------------
		
		simplifiedBoard = m; 
		
		return "";
	}
	
	/**
	 * The opponent moved and this is what we know.
	 */
	public String communicateUmpireMessage(int capX, int capY, int tries, int check, int check2, int captureType)
	{	
		super.communicateUmpireMessage(capX, capY, tries, check, check2, captureType);
		db.currentUmpire = this.currentUmpire;
		db.communicateUmpireMessage(capX, capY, tries, check, check2, captureType);
		
		int cap = (capX<0? Chessboard.NO_CAPTURE : 
			(simplifiedBoard.getFriendlyPiece(capX, capY)==Chessboard.PAWN? Chessboard.CAPTURE_PAWN : Chessboard.CAPTURE_PIECE));
		
		loopThreshold--;
		if (loopThreshold<0) loopThreshold=0;
		
		capx=capX; capy=capY; this.tries=tries; this.check1=check; this.check2=check2;
		
		if (capX!=-1 || tries!=0 || check!=Chessboard.NO_CHECK)
		{
			receivedUmpireMessages = true;
			MCSTSNode.montecarloFlush();
			MCSTSTask.flushPlans();
		}
		// System.out.printf("%d%d", capX, capY);
		Metaposition n = evolveAfterOpponentMoveTopLevel(simplifiedBoard,capX,capY,
				check,check2,tries);
		
		//-----------------------
		complexBoard = complexBoard.evolveWithOpponentMove(0,tries, capX, capY, check, check2);
		//System.out.println(complexBoard);
		//-----------------------
		
		simplifiedBoard = n;
		return "";
	}

	
	public boolean shouldAskDraw()
	{
		return (shouldAskDraw(currentUmpire.getFiftyMoves()));
	}
	
	public Metaposition evolveAfterOpponentMoveTopLevel(Metaposition root,
			int capx, int capy, int check1, int check2, int tries)
			{
				globals.powerMoves.clear();
				 
				globals.capturex = capx;
				globals.capturey = capy;
				globals.check1 = check1;
				globals.check2 = check2;
				globals.ptries = tries;
				
				globals.clearBonusMatrix();
				enemyTracks.clear();
				Metaposition sc = Metaposition.evolveAfterOpponentMove(root,capx,capy,check1,check2,tries);
				
				if (sc.getSquareWithEnemyPiece(Chessboard.KING)<0)
				{
					//evolution error/bug, we've lost the king, fill with kings where applicable
					// System.out.println("ZERO KING!");
					sc.computeProtectionMatrix(true);
					for (int k=0; k<8; k++)
						for (int j=0; j<8; j++)
							if (globals.protectionMatrix[k][j]<1 &&
							sc.getFriendlyPiece(k,j)==Chessboard.EMPTY)
							{
								sc.setPiecePossible(k,j,Chessboard.KING);
							}
				}
	
				
				return sc;
			}
	
	public Metaposition evolveAfterMoveTopLevel(Metaposition root, Move m,
	int cap, int capx, int capy, int check1, int check2, int tries)
	{
		return Metaposition.evolveAfterMove(root,m,cap,capx,capy,check1,check2,tries);
	}
	
	/**
	 * Detects if the agent has lost its mind on a move loop...
	 * @param m
	 * @return
	 */
	protected boolean loopDetector(Move m, int depth)
	{
		//if (2-2==0) return false;
		
		if (moveVector.size()<1 || m==null /*|| piecesLeft+pawnsLeft<3*/) return false;
		
		if (depth>moveVector.size()) depth = moveVector.size();
		
		for (int k=0; k<depth; k++)
		{
			Move m2 = (Move) moveVector.get(moveVector.size()-k-1);
			if (m.equals(m2)) return true;
			if (m.piece==m2.piece && m2.fromX==m.toX && m2.fromY==m.toY && m2.toX==m.fromX
				&& m2.toY==m.fromY) return true; //undoing a recent move is generally bad...
		}
		
		return false;
	}
	
	public Move simpleReactionEvaluate()
	{
		Vector v = simplifiedBoard.generateMoves(true,this);
		
		for (int k=0; k<v.size(); k++)
		{
			Move mo = (Move)v.get(k);
			if (mo.toString().equals("O-O") || mo.toString().equals("O-O-O"))
			{
				v.remove(k);
				k--;
			}
		}
		
		boolean checkwithcapture = false;
		
		if (globals.check1!=Chessboard.NO_CHECK)
		{
			for (int k=0; k<v.size(); k++)
			{
				Move tst = (Move)v.get(k);
				boolean valid = false;
				if (simplifiedBoard.moveOnCheckTrajectory(tst)) valid = true;
				if (!valid)
				{
					v.remove(tst);
					k--;
				} else
				{
					if ((tst.piece!=Chessboard.PAWN || tst.fromX!=tst.toX) && tst.toX==globals.capturex && tst.toY==globals.capturey) checkwithcapture = true;
				}
			}
			if (checkwithcapture)
			{
				for (int k=0; k<v.size(); k++)
				{
					Move tst = (Move)v.get(k);
					boolean valid = false;
					if ((tst.piece!=Chessboard.PAWN || tst.fromX!=tst.toX) && tst.toX==globals.capturex && tst.toY==globals.capturey) valid = true;
					if (!valid)
					{
						v.remove(tst);
						k--;
					}
				}
			}
		}
		else
		//remove all moves but those that allow capture...
		for (int k=0; k<v.size(); k++)
		{
			Move tst = (Move)v.get(k);
			boolean valid = false;
			if (tst.piece==Chessboard.PAWN && tst.toX!=tst.fromX) valid = true;
			if (tst.piece!=Chessboard.PAWN && tst.toX==globals.capturex && tst.toY==globals.capturey) valid = true;
			if (!valid) 
			{
				v.remove(tst);
				k--;
			}
		}
		
		if (v.size()<1) return null;
		
		Move bestMove = (Move)v.get(0);
		float bestValue = /*currentEvaluationFunction.evaluate(this,this.generateMostLikelyEvolution(bestMove),bestMove)*/ -1.0f;
		
		for (int k=0; k<v.size(); k++)
		{
			Move m = (Move)v.get(k);
			float val = /*currentEvaluationFunction.evaluate(this,this.generateMostLikelyEvolution(m),m)*/ 0.0f;
			switch (m.piece)
			{
			case Chessboard.KING: val = 10.0f; break;
			case Chessboard.PAWN: val = 8.0f; break;
			case Chessboard.KNIGHT: val = 7.0f; break;
			case Chessboard.BISHOP: val = 5.0f; break;
			case Chessboard.ROOK: val = 3.0f; break;
			case Chessboard.QUEEN: val = 1.0f; break;
			}
			if (val>bestValue)
			{
				bestMove = m; bestValue = val;
			}
		}
		
		return bestMove;
		
	}

	public boolean shouldAskDraw(int fiftyMoves)
	{
		if (fiftyMoves<50) return false;
		simplifiedBoard.calculateFriendlyMaterial();
		
		return (globals.pawns+globals.knights+globals.bishops+globals.rooks+globals.queens==0);
	}


	public static void initialize(String path)
	{
		Globals.PGNPath = path;
	}


}
