/*
 * Created on 8-apr-05
 *
 */
package ai.player;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Vector;

import ai.evolve.SimpleWeightSet;
import ai.mc.MCForest;
import ai.opponent.Metapool;
import ai.opponent.MetapositionPool;
import ai.opponent.ProfileUpdater;
import ai.planner.Discardable;
import ai.planner.PlanDashboard;
import ai.planner.planners.EvadeRetaliationPlanner;
import core.Chessboard;
import core.EvaluationFunction;
import core.EvaluationGlobals;
import core.EvaluationTree;
import core.ExtendedEvaluationTree;
import core.Globals;
import core.Metaposition;
import core.Move;
import core.eval.DefaultEvaluationFunction;
import core.eval.KPKEvaluationFunction;
import core.eval.KQKEvaluationFunction;
import core.eval.KRKEvaluationFunction;
import core.eval.KRRKEvaluationFunction;
import core.eval.KingOnlyEvaluationFunction;
import core.uberposition.Uberposition;
import database.Opening;
import database.OpeningBook;
import database.PieceDensityInfo;
import database.PlayerModel;
import gui.test.MetapositionPoolFrame;
import umpire.local.LocalUmpire;

/**
 * This child of AIPlayer implements the actual standard AI Player used
 * to play serious Kriegspiel.
 * @author Nikola Novarlic
 *
 */
public class Darkboard extends AIPlayer {
	
	public static Darkboard dbArchetype = null;
	
	//private static EvaluationTable evaluationTable = new EvaluationTable();
	//private static int evaluationTableSize=0;
	public EvaluationComparator listSorter = new EvaluationComparator();
	
	//public EvaluationTree moveTree;
	public Move minimaxBestMove;
	public float minimaxPositionValue;
	public float minimaxMinValue;
	public float bestMoveValue;
	
	public float alpha, oneMinusAlpha;
	
	Metapool pool2;
	
	/**
	 * This variable is increased when the engine detects a move loop,
	 * and decreased when it doesn't. Sometimes it is necessary to
	 * reverse the latest move for a logical reason (e.g. hit'n'run).
	 * Only when this variable goes over a certain threshold do we have a loop.
	 */
	public int loopThreshold = 0;

	protected Vector enemyTracks = new Vector();
	public Vector moveVector = new Vector(); //all the moves so far
	
	public int nodeNumber = 5000;
	
	public PlanDashboard dashboard;
	
	//if non-null, it means the last move was decided in an algorithmic fashion.
	EvaluationFunction algorithmicFunction = null;
	
	
	public class MoveEvaluation implements Cloneable
	{
		public Move move;
		public float value; //the value offset this move is believed to bring
		public float testValue; //the starting value of the position the move was tried on
		public float staticValue;
		public Metaposition firstEvolution=null; //chessboard containing the first case when move is encountered
		public ExtendedEvaluationTree tree=null;
		
		public Object clone()
		{
			try
			{
				return super.clone();
		
			} catch (Exception e) { return null; }
		}
	}
	
	public class EvaluationComparator implements Comparator
	{
		public int compare (Object o1, Object o2)
		{
			float diff = ((MoveEvaluation)o2).value - ((MoveEvaluation)o1).value;
			if (diff>0.0f) return 1;
			if (diff<0.0f) return -1;
			return 0;
		}
	}	
	
	public MoveEvaluation getEvaluation(Move m)
	{
		
		return globals.evaluationTable.moves[(m.toX<<3)|m.toY][(m.fromX<<3)|m.fromY][m.piece];
	}
	
	public MoveEvaluation addEvaluation(Move m, float val, float startingVal, Metaposition sc)
	{
		MoveEvaluation result = new MoveEvaluation();
		result.move = m;
		result.value = val;
		result.testValue = startingVal;
		result.firstEvolution = sc;
		if (val>=-50.0f && val<=50.0f)
		{
			globals.evaluationTable.moves[(m.toX<<3)|m.toY][(m.fromX<<3)|m.fromY][m.piece] = result;
			globals.evaluationTableSize++;
		}
		return result;
	}
	
	

	public int myPruningNewMovesAmount=5; //max number of unexplored moves to search
	public int myPruningOldMovesAmount=5; //max number of already explored moves to search
	
	
	
	
	
	SimpleWeightSet simpleParameters;
	
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
	

	public Darkboard(boolean w)
	{
		//System.out.println("Loading AI player");

		simpleParameters = new SimpleWeightSet();
		dashboard = new PlanDashboard(this);

		this.isWhite = w;
		
		playerName = "Darkboard";
		
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
		
		complexBoard = new Uberposition(isWhite,this); //!!!
		
		if (Globals.usePools)
		{
			pool = new MetapositionPool(this,new Player(),100);
			if (Globals.hasGui && Globals.debug) new MetapositionPoolFrame(pool);
		}
		
		//pool2 = new Metapool(simplifiedBoard);
		
		initEvaluationFunction();
		
		this.dumpObject();
	}
	
	/**
	 * Default evaluation functions.
	 *
	 */
	protected void initEvaluationFunction()
	{
		evaluationFunctionVector.add(new KPKEvaluationFunction());
		evaluationFunctionVector.add(new KRRKEvaluationFunction());
		evaluationFunctionVector.add(new KRKEvaluationFunction(this));
		
		evaluationFunctionVector.add(new KQKEvaluationFunction());
		evaluationFunctionVector.add(new KingOnlyEvaluationFunction());
		evaluationFunctionVector.add(new DefaultEvaluationFunction(this));
	
		dashboard.addPlan(new EvadeRetaliationPlanner(dashboard));
		//dashboard.addPlan(new StrategicPlanner(dashboard));
	
	}
	
	/**
	 * Evaluates a metaposition with the appropriate evaluation function.
	 */
	public float evaluate(Metaposition start, Move m, Metaposition dest, Vector history)
	{	
		return (evaluate(start,m,dest,findAppropriateFunction(start),history));
	}
	
	public float evaluate(Metaposition start, Move m, Metaposition dest,EvaluationFunction eval, Vector history)
	{
		try
		{
			return (eval!=null? eval.evaluate(start,dest,m,history) : 0.0f);
		} catch (Exception e) { return 0.0f; }
		
	}
	
	/**
	 * Scans the function list to find the first matching eval function
	 * @param sc
	 * @return
	 */
	public EvaluationFunction findAppropriateFunction(Metaposition sc)
	{	
		
		globals.materialDelta = globals.totalMaterial - sc.piecesLeft - sc.pawnsLeft;
		globals.materialRatio = 1.0f * globals.totalMaterial / (sc.piecesLeft + sc.pawnsLeft);
		sc.calculateFriendlyMaterial();
		
		for (int k=0; k<evaluationFunctionVector.size(); k++)
		{
			EvaluationFunction f = (EvaluationFunction) evaluationFunctionVector.get(k);
			if (f.canHandleSituation(sc,capx,capy,check1,check2,tries))
			{
				return f;
			}
		}
		return null;
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
			globals.weights = this.simpleParameters;
			
			//try an automatic move first
			Move auto = findAppropriateFunction(simplifiedBoard).getAutomaticMove(simplifiedBoard);
			if (auto!=null) 
			{
				//System.out.println("AUTOMOVE");
				lastMove = auto;
				return auto;
			}
			
			if (!lastMoveIllegal)
			{
				this.communicateObject("New move: "+moveNumber, null, MESSAGE_TYPE_INFO);
			}
			
			if (lastMoveIllegal)
			//if (false)
			{
				//don't compute everything again from scratch... try the next best move from the list...
				lastMove = selectMove(capx,capy,check1,check2,tries); 
				//System.out.println("BEST MOVE : "+lastMove);
				return (lastMove);
			}
			
			//stick to the opening you chose as long as the referee is silent
			if (!receivedUmpireMessages && chosenOpening!=null && moveNumber2<chosenOpening.getMoveNumber()) 
			{
				lastMove = chosenOpening.getMove(this.moveNumber2);
				communicateObject("Opening book: "+lastMove.toString()+", length "+chosenOpening.getMoveNumber(), null, MESSAGE_TYPE_MOVE);
			}
			else
			{
				EvaluationFunction ef = findAppropriateFunction(simplifiedBoard);
				//System.out.println(ef.getName());
				lastMove = selectMove(capx,capy,check1,check2,tries);
				
				Metaposition met = ef.generateMostLikelyEvolution(simplifiedBoard,lastMove);
				
				/*if (lastMove!=null && met.canKeep(lastMove)==Discardable.NO)
				{
					System.out.println(simplifiedBoard.getRepresentation(Chessboard.PAWN));
					System.out.println(lastMove+" is a MISTAKE");
				}*/
				
				
				/*if (met!=null)
				{
					KRKEvaluationFunction.verbose = true;
					System.out.println(ef.evaluate(simplifiedBoard,
						met,lastMove));
					KRKEvaluationFunction.verbose = false;
					//System.out.println(met.getAndPrintProtectionMatrix(true));
					System.out.println(simplifiedBoard.getRepresentation(Chessboard.KING));
				}*/
			} 
		} catch (Exception e)
		{
			communicateObject("Exception: "+e.toString(), e.getMessage(), MESSAGE_TYPE_ERROR);
			e.printStackTrace();
		}
		
		//if (lastMove!=null && moveTree!=null) System.out.println(moveTree.getBestSequence());
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
		
		//for (int k=0; k<bannedMoves.size(); k++)
			//System.out.println(bannedMoves.get(k));
		
		receivedUmpireMessages = true;
		
		Metaposition n = Metaposition.evolveAfterIllegalMove(simplifiedBoard,m,capx,capy,check1,check2,tries);
		
		//-----------------------
		complexBoard = complexBoard.evolveWithIllegalMove(0,m);
		//System.out.println(complexBoard);
		//-----------------------
		
		dashboard.evolveAfterIllegalMove(simplifiedBoard,n,m);
		
		if (algorithmicFunction!=null)
		{
			algorithmicFunction.algorithmicMoveRejected(simplifiedBoard,m,n);
			algorithmicFunction = null;
		}
		
		//pool2.evolveWithIllegalMove(simplifiedBoard, m, capx, capy, check1, check2, tries);
		
		simplifiedBoard = n;
		//System.out.println(simplifiedBoard.getRepresentation(Chessboard.KING));
		
		if (Globals.usePools) pool.updateWithIllegalMove(simplifiedBoard, lastMove);
		
	}
	
	private void banMoveSequence(Move m)
	{
		int dx, dy;
		banMove(m);
		/*
		if (m.piece==Chessboard.KING || m.piece==Chessboard.KNIGHT) return;
		if (m.piece==Chessboard.PAWN)
		{
			return;
		}
		dx = (m.toX>m.fromX? 1 : m.toX==m.fromX ? 0 : -1);
		dy = (m.toY>m.fromY? 1 : m.toY==m.fromY ? 0 : -1);
		
		int x,y;
		x = m.toX; y = m.toY;
		while (true)
		{
			x += dx; y += dy;
			if (x<0 || y<0 || x>=8 || y>=8 || !simplifiedBoard.mayBeEmpty(x,y)) return;
			
			Move mv = new Move();
			mv.fromX = m.fromX; mv.fromY = m.fromY; mv.piece = m.piece; mv.promotionPiece = m.promotionPiece;
			mv.toX = (byte)x; mv.toY = (byte)y;
			banMove(mv);
		}*/
	}
	
	/**
	 * Updates the internal representation after having been told the latest move was legal.
	 */
	public String communicateLegalMove(int capture, int oppTries, int oppCheck, int oppCheck2)
	{
		super.communicateLegalMove(capture, oppTries, oppCheck, oppCheck2);
		
		double diff[] = new double[6];
		lastMoveIllegal = false;
		bestMoveIndex = 1;
		moveNumber2++;
		if (capture!=Chessboard.NO_CAPTURE || oppTries!=0 || oppCheck!=Chessboard.NO_CHECK)
			receivedUmpireMessages = true;
			
		unbanMoves();
		moveVector.add(lastMove);

		Metaposition m = evolveAfterMoveTopLevel(simplifiedBoard,lastMove,
				capture,lastMove.toX,lastMove.toY,oppCheck,oppCheck2,oppTries);
		
		//-----------------------
		complexBoard = complexBoard.evolveWithPlayerMove(0,lastMove, lastMove.toX, lastMove.toY, 
				capture, oppCheck, oppCheck2, oppTries);
		//System.out.println(complexBoard);
		//-----------------------
		
		dashboard.evolveAfterMove(simplifiedBoard,m,lastMove,capture,lastMove.toX,
				lastMove.toY,oppCheck,oppCheck2,oppTries);
		
		if (algorithmicFunction!=null)
		{
			algorithmicFunction.algorithmicMoveAccepted(simplifiedBoard,lastMove,m);
			algorithmicFunction = null;
		}
		
		if (Globals.usePools) 
		{
			pool.updateWithPlayerMove(simplifiedBoard, lastMove, m, capture, 
				(capture!=Chessboard.NO_CAPTURE? lastMove.toX : -1), 
				(capture!=Chessboard.NO_CAPTURE? lastMove.toY : -1), oppCheck, oppCheck2, oppTries);
			
			double accuracy = pool.accuracy((LocalUmpire)currentUmpire);
			//pool2.evolveWithPlayerMove(simplifiedBoard, lastMove, m, capture, (capture!=Chessboard.NO_CAPTURE? lastMove.toX : -1), 
				//(capture!=Chessboard.NO_CAPTURE? lastMove.toY : -1), oppCheck, oppCheck, oppTries);
			
			//double accuracy = pool.protectionError((LocalUmpire)currentUmpire);
			
			Globals.sampler.addSample(moveNumber2-1, accuracy);
		}
		
		simplifiedBoard = m; 
		
		return "";
	}
	
	/**
	 * The opponent moved and this is what we know.
	 */
	public String communicateUmpireMessage(int capX, int capY, int tries, int check, int check2, int captureType)
	{	
		super.communicateUmpireMessage(capX, capY, tries, check, check2, captureType);
		
		int cap = (capX<0? Chessboard.NO_CAPTURE : 
			(simplifiedBoard.getFriendlyPiece(capX, capY)==Chessboard.PAWN? Chessboard.CAPTURE_PAWN : Chessboard.CAPTURE_PIECE));
		
		loopThreshold--;
		if (loopThreshold<0) loopThreshold=0;
		
		capx=capX; capy=capY; this.tries=tries; this.check1=check; this.check2=check2;
		
		if (capX!=-1 || tries!=0 || check!=Chessboard.NO_CHECK)
			receivedUmpireMessages = true;
		
		Metaposition n = evolveAfterOpponentMoveTopLevel(simplifiedBoard,capX,capY,
				check,check2,tries);
		
		//-----------------------
		complexBoard = complexBoard.evolveWithOpponentMove(0,tries, capX, capY, check, check2);
		//System.out.println(complexBoard);
		//-----------------------
		
		dashboard.evolveAfterOpponentMove(simplifiedBoard,n,capX,capY,check,check2,tries);
		
		//pool2.evolveWithOpponentMove(simplifiedBoard, n, capX, capY, check1, check2, tries);
		
		
		
		//System.out.println(simplifiedBoard.getRepresentation(Chessboard.KING));
		
		globals.calculateTimePressure(currentUmpire.timedGame,currentUmpire.getTime(),
			currentUmpire.getOpponentTime(),currentUmpire.getStartTime(),currentUmpire.getTimeIncrement());
	
		
		if (Globals.usePools) pool.updateWithOpponentMove(simplifiedBoard, n, cap, capX, capY, check, check2, tries);
		
		simplifiedBoard = n;
		
		//System.out.println("ACCURACY: "+pool.accuracy(((StepwiseLocalUmpire)this.currentUmpire)));
		return "";
	}


	/**
	 * @return
	 */
	public SimpleWeightSet getSimpleParameters() {
		return simpleParameters;
	}

	/**
	 * @param set
	 */
	public void setSimpleParameters(SimpleWeightSet set) {
		simpleParameters = set;
	}
	
	public boolean shouldAskDraw()
	{
		return (shouldAskDraw(currentUmpire.getFiftyMoves()));
	}

	
	public void minimaxTopLevel(Metaposition start, int maxPositions)
	{
		Vector v = start.generateMoves(true,this);
		Vector evaluations = new Vector();
		
		for (int k=0; k<v.size(); k++)
		{
			
		}
		
	}


	public void minimax(Metaposition start, Move m, float startingValue, int maxPositions, boolean topLevel, Metaposition precalcChessboard, EvaluationTree father, EvaluationTree current, Vector hist)
	{
		Metaposition evolve; 
		Metaposition evolve2;
		Vector h = (Vector)hist.clone();
		float minv;
		EvaluationTree tree = (current!=null? current : (globals.usingExtendedGameTree? new ExtendedEvaluationTree() : new EvaluationTree()));
		if (globals.usingExtendedGameTree) EvaluationFunction.currentNode = (ExtendedEvaluationTree)tree;
		EvaluationFunction ef = findAppropriateFunction(start);
		
		if (ef==null) return;
		
		tree.m = m;
		//tree.sc = precalcChessboard;
		if (!topLevel)
		{
			evolve = (precalcChessboard==null? ef.generateMostLikelyEvolution(start,m) : precalcChessboard);
			evolve2 = evolve.generateMostLikelyOpponentMove(m);
			h.add(m);
			tree.staticvalue = startingValue;
			tree.sc = precalcChessboard;
			if (globals.usingExtendedGameTree)
			{
				((ExtendedEvaluationTree)tree).func = ef;
				((ExtendedEvaluationTree)tree).history = h;
			}
		} else
		{
			evolve2 = start;
			startingValue = evaluate(evolve2,null,evolve2,ef,h);
			tree.sc = start;
			tree.staticvalue = startingValue;
			tree.sc = precalcChessboard;
			if (globals.usingExtendedGameTree)
			{
				((ExtendedEvaluationTree)tree).func = ef;
				((ExtendedEvaluationTree)tree).history = h;
			}

				//tree.staticvalue = tree.value = startingValue;
		}
		
		Vector v = evolve2.generateMoves(topLevel,this);
		Vector newMoves = new Vector();
		Vector oldMoves = new Vector();
		
		if (experimental)
		{
			for (int k=0; k<v.size(); k++)
			{
				Metaposition hyp = ef.generateMostLikelyEvolution(start, (Move)v.get(k));
				if (hyp.canKeep((Move)v.get(k))==Discardable.NO)
				{
					v.remove(k);
					k--;
				}
			}
		}
		
		//separate the moves: those which already have a place in the evaluation table
		//and those which don't.
		for (int k=0; k<v.size(); k++)
		{
			Move mv = (Move)v.get(k);
			MoveEvaluation me = getEvaluation(mv);
			if (me!=null) me = (MoveEvaluation)me.clone();
			if (me!=null) {
				oldMoves.add(me); me.firstEvolution = ef.generateMostLikelyEvolution(evolve2,mv); } 
				else 
				{
					ExtendedEvaluationTree t=null;
					if (globals.usingExtendedGameTree)
					{
						t = new ExtendedEvaluationTree();
						EvaluationFunction.currentNode = t;
						t.history = h;
					}
					Metaposition next = ef.generateMostLikelyEvolution(evolve2,mv);
					float newMoveEval = evaluate(evolve2,mv,next,ef,h);
					me = addEvaluation(mv,newMoveEval-startingValue,startingValue,next);
					me.staticValue = newMoveEval;
					me.tree = t;
					newMoves.add(me);
				} 
		}
		
		//every evaluation is one position we had to compute. Subtract it.
		int newSize = (newMoves.size()>globals.pruningNewMovesAmount? globals.pruningNewMovesAmount : newMoves.size());
		int otherPositions = (oldMoves.size()>globals.pruningOldMovesAmount? globals.pruningOldMovesAmount : oldMoves.size());
		int totalBranches = newSize + otherPositions;
		maxPositions -= newMoves.size()+otherPositions;
		int positionsForEach = (totalBranches>0? maxPositions / totalBranches : 0);
		Vector chessboardStorage = new Vector();
		
		Collections.sort(oldMoves,listSorter);
		Collections.sort(newMoves,listSorter);
		
		//put the chessboard evolutions into the vector, or they will be overwritten by recursive calls... found this the hard way...
		for (int k=0; k<newSize; k++) chessboardStorage.add(((MoveEvaluation)newMoves.get(k)).firstEvolution);
		for (int k=0; k<otherPositions; k++) chessboardStorage.add(((MoveEvaluation)oldMoves.get(k)).firstEvolution);

		float value, avgvalue, minvalue, currentMinValue;
		value = avgvalue = -100000000;
		minvalue = 0.0f;
		if (startingValue<minvalue) minvalue = startingValue;
		currentMinValue = minvalue;
		Move bestMove = null;
		
		float minValForExpansion = ef.getMinValueForExpansion();
		float maxValForExpansion = ef.getMaxValueForExpansion();
		
		for (int k=0; k<totalBranches; k++)
		{
			MoveEvaluation eval = (k<newSize? ((MoveEvaluation)newMoves.get(k)) :
			((MoveEvaluation)oldMoves.get(k-newSize)));
			Move testedMove = eval.move;
			//Metaposition nextEvolution = eval.firstEvolution;
			Metaposition nextEvolution = (Metaposition) chessboardStorage.get(k);
			//if it's an old move (already in hash table) we re-evaluate it.
			float staticValue = 0.0f;
			if (k<newSize)
			{
				staticValue = eval.staticValue;
			}
			else
			{
				if (globals.usingExtendedGameTree)
				{
					eval.tree = new ExtendedEvaluationTree();
					EvaluationFunction.currentNode = eval.tree;
					eval.tree.func = ef;
				} 
				staticValue = evaluate(evolve2,testedMove,nextEvolution,h);
				eval.staticValue = staticValue;
			}
			
			if (positionsForEach>1 && staticValue>=minValForExpansion && staticValue<=maxValForExpansion) //don't expand positions that are very bad or very good
			{
				minimax(evolve2,testedMove,staticValue,positionsForEach,false,nextEvolution,tree,eval.tree,h);
			}
			else
			{
				//EvaluationTree et = new EvaluationTree();
				EvaluationTree et = eval.tree;
				if (et==null) et = new EvaluationTree();
				et.m = testedMove;
				et.staticvalue = staticValue;
				//et.sc = precalcChessboard;
				et.sc = eval.firstEvolution;
				et.value = staticValue;
				tree.addChild(et);	
			}
		}
		
		avgvalue = -1000000;
		for (int k=0; k<tree.getChildNumber(); k++)
		{
			//examine each child in turn...
			EvaluationTree pick = tree.getChild(k);
			float averageValue = pick.value;
			if (averageValue > avgvalue)
			{
				avgvalue = averageValue;
				bestMove = pick.m;
			}
		}
		
		//tree.value = avgvalue;
		tree.value = (tree.staticvalue)*alpha+avgvalue*oneMinusAlpha;
		tree.bestChild = bestMove;
		if (father!=null) father.addChild(tree);
			
		if (topLevel) 
		{
			minimaxBestMove = bestMove;
			minimaxPositionValue = avgvalue;
			minimaxMinValue = currentMinValue;
			bestMoveValue = avgvalue;
			globals.moveTree = tree;
		} 
	}
	
	
	public void exhaustiveMinimax(Metaposition start, Move m, float startingValue, int maxPositions, boolean topLevel, Metaposition precalcChessboard, EvaluationTree father, EvaluationTree current, Vector hist)
	{
		Metaposition evolve; 
		Metaposition evolve2;
		Vector h = (Vector)hist.clone();
		float minv;
		EvaluationTree tree = (current!=null? current : (globals.usingExtendedGameTree? new ExtendedEvaluationTree() : new EvaluationTree()));
		if (globals.usingExtendedGameTree) EvaluationFunction.currentNode = (ExtendedEvaluationTree)tree;
		EvaluationFunction ef = findAppropriateFunction(start);
		
		if (ef==null) return;
		
		tree.m = m;
		//tree.sc = precalcChessboard;
		if (!topLevel)
		{
			evolve = (precalcChessboard==null? ef.generateMostLikelyEvolution(start,m) : precalcChessboard);
			tree.staticvalue = startingValue;
			tree.sc = precalcChessboard;
			if (globals.usingExtendedGameTree)
				((ExtendedEvaluationTree)tree).func = ef;
			/*if (evolve==null) 
			{
				tree.value = startingValue;
				return;
			}*/
			evolve2 = evolve.generateMostLikelyOpponentMove(m);
			h.add(m);

		} else
		{
			evolve2 = start;
			startingValue = evaluate(evolve2,null,evolve2,ef,h);
			tree.sc = start;
			//tree.staticvalue = tree.value = startingValue;
		}
		
		Vector v = evolve2.generateMoves(topLevel,this);
		Vector moves = new Vector();

		for (int k=0; k<v.size(); k++)
		{
			Move mv = (Move)v.get(k);
			ExtendedEvaluationTree t=null;
			if (globals.usingExtendedGameTree)
			{
				t = new ExtendedEvaluationTree();
				
			}
			Metaposition next = ef.generateMostLikelyEvolution(evolve2,mv);
			MoveEvaluation me = new MoveEvaluation();
			me.firstEvolution = next;
			me.tree = t;
			me.move = mv;
			if (next!=null) moves.add(me);
		} 

		maxPositions -= moves.size();
		int positionsForEach = (moves.size()>0? maxPositions / moves.size() : 0);
		Vector chessboardStorage = new Vector();

		float value, avgvalue, minvalue, currentMinValue;
		value = avgvalue = -100000000;
		minvalue = 0.0f;
		if (startingValue<minvalue) minvalue = startingValue;
		currentMinValue = minvalue;
		Move bestMove = null;
		
		float minValForExpansion = ef.getMinValueForExpansion();
		float maxValForExpansion = ef.getMaxValueForExpansion();
		
		for (int k=0; k<moves.size(); k++)
		{
			MoveEvaluation eval = (MoveEvaluation)moves.get(k);
			Move testedMove = eval.move;
			Metaposition nextEvolution = eval.firstEvolution;
			//if it's an old move (already in hash table) we re-evaluate it.
			float staticValue = 0.0f;

			if (globals.usingExtendedGameTree)
			{
				eval.tree = new ExtendedEvaluationTree();
				eval.tree.func = ef;
				EvaluationFunction.currentNode = eval.tree;
			} 
			staticValue = ef.evaluate(evolve2,nextEvolution,testedMove,h); 
			eval.staticValue = staticValue;
			
			if (positionsForEach>1 && staticValue>=minValForExpansion && staticValue<=maxValForExpansion) //don't expand positions that are very bad or very good
			{
				exhaustiveMinimax(evolve2,testedMove,staticValue,positionsForEach,false,nextEvolution,tree,eval.tree,h);
			}
			else
			{
				//EvaluationTree et = new EvaluationTree();
				EvaluationTree et = eval.tree;
				if (et==null) et = new EvaluationTree();
				et.m = testedMove;
				et.staticvalue = staticValue;
				//et.sc = precalcChessboard;
				et.sc = eval.firstEvolution;
				et.value = staticValue;
				tree.addChild(et);	
			}
		}
		
		avgvalue = -1000000;
		for (int k=0; k<tree.getChildNumber(); k++)
		{
			//examine each child in turn...
			EvaluationTree pick = tree.getChild(k);
			float averageValue = pick.value;
			if (averageValue > avgvalue)
			{
				avgvalue = averageValue;
				bestMove = pick.m;
			}
		}
		
		//tree.value = avgvalue;
		tree.value = (tree.staticvalue)*alpha+avgvalue*oneMinusAlpha;
		tree.bestChild = bestMove;
		if (father!=null) father.addChild(tree);
			
		if (topLevel) 
		{
			minimaxBestMove = bestMove;
			minimaxPositionValue = avgvalue;
			minimaxMinValue = currentMinValue;
			bestMoveValue = avgvalue;
			globals.moveTree = tree;
		} 
	}	
	
	public Move minimax(int depth)
	{
		globals.evaluatedPositions = 0;
		//here is where you can trigger different evaluators depending on patterns on the chessboard.
		globals.pruningNewMovesAmount = 200;
		globals.pruningOldMovesAmount = 200;
		return bestMove(depth==2? 1000 : 5000);
	}
	
	/*public Metaposition evolveAfterOpponentMoveTopLevel(Metaposition root,
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
		
		return sc;
	}*/
	
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
					//System.out.println("ZERO KING!");
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
	
	public Move bestMove(int maxPositions)
	{
		globals.evaluatedPositions = 0;
		globals.evaluationTable.clear();
		globals.evaluationTableSize = 0;
		globals.currentEvaluator = (this.isWhite? globals.whiteEvaluator : globals.blackEvaluator);
		simplifiedBoard.setAge((byte)0);
		
		EvaluationFunction ef = findAppropriateFunction(simplifiedBoard);
		maxPositions *= ef.getNodeMultFactor(simplifiedBoard);
		
		if (ef.useKillerPruning()) minimax(simplifiedBoard,null,0.0f,maxPositions,true,null,null,null,new Vector());
		else exhaustiveMinimax(simplifiedBoard,null,0.0f,maxPositions,true,null,null,null,new Vector());
		
		if (globals.OUTPUT_MOVE_TREE && globals.moveTree!=null) System.out.println(globals.moveTree.getBestSequence());
		if (globals.OUTPUT_MOVE_TREE && globals.SAVE_MOVE_TREE_TO_DISK && globals.moveTree!=null) globals.moveTree.saveToFile();
		globals.bestMoveValue = minimaxPositionValue;
		
		if (minimaxBestMove!=null)
		{
			String s = "Move tree: ";
			if (globals.moveTree!=null) s+=globals.moveTree.getBestSequence(); else s+=minimaxBestMove.toString();
			communicateObject(s, null, MESSAGE_TYPE_MOVE);
		}
		
		return minimaxBestMove;
	}

	/**
	 * This explores everything, including illegal moves.
	 * @param capx
	 * @param capy
	 * @param check1
	 * @param check2
	 * @param tries
	 * @return
	 */
	public Move selectMoveSpecial(int capx, int capy, int check1, int check2, int tries, Vector moveList)
	{
		Move bestMove = null;
		globals.initEvaluationEngine(simplifiedBoard);
		
		EvaluationFunction ef = findAppropriateFunction(simplifiedBoard);
		
		return null;
	}
	
	
	/**
	 * We select the move in different ways depending on whether the chessboard is quiescent.
	 * @param capx
	 * @param capy
	 * @param check1
	 * @param check2
	 * @param tries
	 * @return
	 */
	public Move selectMove(int capx, int capy, int check1, int check2, int tries)
	{ 
		Move bestMove = null;
		this.algorithmicFunction = null;
		
		globals.initEvaluationEngine(simplifiedBoard);
		
		globals.pruningNewMovesAmount = 5;
		//pruningOldMovesAmount = 3;
		globals.pruningOldMovesAmount = 10;
		EvaluationFunction ef = findAppropriateFunction(simplifiedBoard);
		alpha = ef.getExplorationAlpha(simplifiedBoard);
		oneMinusAlpha = 1.0f-alpha;
		
		Move algo = ef.getAlgorithmicMove(simplifiedBoard);
		if (algo!=null)
		{
			this.algorithmicFunction = ef;
			return algo;
		}

		
		//if (weights==null) return null;

		
		try
		{

			
			/*
			 * There are 2 special move categories, reactions and plan executing moves.
			 * Reactions are 'reflexes', automatically invoked upon umpire messages (e.g retaliate on captures)
			 * Plan executing moves bring a plan to fruition.
			 */
			if (globals.ptries>0 || globals.capturex>=0 || check1!=Chessboard.NO_CHECK)
			{
				bestMove = simpleReactionEvaluate();
				if (bestMove!=null)
					communicateObject("Reaction: "+bestMove.toString(), null, MESSAGE_TYPE_MOVE);
			} else bestMove = dashboard.findPlanExecutingMove(simplifiedBoard.generateMoves(true,this),simplifiedBoard,0.25f);
			
			if (experimental && bestMove==null)
			{
				//bestMove = MCForest.forest.doMC(simplifiedBoard, 3000);
				//bestMove = MCForest.forest.doMC(complexBoard, 3000);
				bestMove = MCForest.forest.doMC2(complexBoard);
				if (bestMove==null) System.out.println("MCFOREST INEFFECTIVE");
			}
			
			if (bestMove==null)
			{ 
	
				
				
			if (globals.ALWAYS_FORCE_DEEP_SEARCH || (capx==-1 && check1==Chessboard.NO_CHECK && tries<1 && globals.timePressure<10))
			{
				bestMove = bestMove(nodeNumber-globals.timePressure*300);

			}
			//return minimax(2);
			//if (this.shouldBeStaticallyEvaluated()) return minimax(3);
			else bestMove = minimax(globals.minimaxDepth);
			}
			//System.out.println("BEST = "+bestMove);
			if ((globals.ptries>0 || globals.capturex>=0) && bestMove!=null)
			{
				boolean valid = (bestMove.piece==Chessboard.PAWN && bestMove.toX != bestMove.fromX)
					|| (bestMove.piece!=Chessboard.PAWN && bestMove.toX==globals.capturex && bestMove.toY==globals.capturey);
				
				//if (!valid) System.out.println("SHOULD HAVE CAPTURED! t="+ptries+", c="+capturex+","+capturey+" m="+bestMove);
			}
			
			//if there are no umpire messages, resolve any loops
			if (ef.useLoopDetector() && capx==-1 && check1==Chessboard.NO_CHECK && tries<1 && !globals.ALWAYS_FORCE_DEEP_SEARCH) bestMove = resolveLoops(bestMove);
			 
		} catch (Exception e)
		{
			e.printStackTrace();
			communicateObject("Exception: "+e.toString(), e, MESSAGE_TYPE_ERROR);
			bestMove=null;
		}
		
		if (bestMove==null)
		{
			// System.out.println("EEEEEKKKK!!!!");
			bannedMoves.clear();
			//System.out.println(this.getRepresentation(Chessboard.KING));
			Vector v = simplifiedBoard.generateMoves(true,this);
			for (int k=0; k<v.size(); k++) {
				// System.out.println("Source of error");
				System.out.println(v.get(k));
			}
			System.out.println(currentUmpire.toString());
			if (v.size()>0)
				bestMove = (Move) v.get(new Random().nextInt(v.size()));
		}
		
		if (bestMove==null)
		{
			for (int k=0; k<bannedMoves.size(); k++) {
				System.out.println(bannedMoves.get(k));
			}
		}
		//System.out.println("Everything is okay here");
		return bestMove;
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
	
	/*protected Move resolveLoops(Move m)
	{
		Move bestMove = m;
		int depth = 6;
		int tries = 0;
		int seekdepth = globals.minimaxDepth+1;
		Vector banned = new Vector();
		
		boolean loop = loopDetector(bestMove,depth);
		if (loop)
		{
			loopThreshold+=2;
			if (loopThreshold<3) return m;
			if (loopThreshold>3) loopThreshold = 3;
		} else 
		{
			//loopThreshold--;
			return m;
		}
		System.out.println("LOOP DETECTED");
		while (loopDetector(bestMove,depth))
		{
			if (tries==0)
			{
				//bestMove = minimax(seekdepth);
				//seekdepth++;
				globals.initEvaluationEngine(simplifiedBoard);
				globals.pruningNewMovesAmount = 100;
				globals.pruningOldMovesAmount = 3;
				//bestMove = bestMove(10000);
				bestMove = minimax(globals.minimaxDepth+1);
			} else
			{	
				Vector v = simplifiedBoard.generateMoves(true,this);
				bestMove = (Move)v.get((new Random()).nextInt(v.size()));
				break;
			}

			tries++;
		}
		
		for (int k=0; k<banned.size(); k++) bannedMoves.remove(banned.get(k));
		
		return bestMove;
	}*/
	
	protected Move resolveLoops(Move m)
	{
		Move bestMove = m;
		int depth = 6;
		int tries = 0;
		int seekdepth = globals.minimaxDepth+1;
		Vector banned = new Vector();
		
		boolean loop = loopDetector(bestMove,depth);
		if (loop)
		{
			loopThreshold+=2;
			if (loopThreshold<3) return m;
			if (loopThreshold>3) loopThreshold = 3;
		} else 
		{
			//loopThreshold--;
			return m;
		}
		//System.out.println("LOOP DETECTED");
		this.communicateObject("Loop detected with move "+m.toString(), null, MESSAGE_TYPE_INFO);
		
		//Strategy s = new Strategy(this);
		//Path p = s.planAttack(simplifiedBoard, 3);
		//System.out.println("Loop at: "+currentUmpire.toString());
		//System.out.println(p);
		
		while (loopDetector(bestMove,depth))
		{
			if (tries==0)
			{
				if (MCForest.forest.valid()) return MCForest.forest.findNextBest();
				
				if (globals.moveTree!=null)
				{
					globals.moveTree.sortChildren(false);
					for (int index=1; index<globals.moveTree.getChildNumber(); index++)
					{
						Move tr = globals.moveTree.getChild(index).m;
						if (!loopDetector(tr,depth)) 
						{
							//System.out.println("LOOP BROKEN, "+tr);
							this.communicateObject("Loop broken with "+tr.toString(), null, MESSAGE_TYPE_MOVE);
							return tr;
						}
					}
				}
				//bestMove = minimax(seekdepth);
				//seekdepth++;
				globals.initEvaluationEngine(simplifiedBoard);
				globals.pruningNewMovesAmount = 100;
				globals.pruningOldMovesAmount = 3;
				//bestMove = bestMove(10000);
				bestMove = minimax(globals.minimaxDepth+1);
			} else
			{	
				Vector v = simplifiedBoard.generateMoves(true,this);
				bestMove = (Move)v.get((new Random()).nextInt(v.size()));
				break;
			}

			tries++;
		}
		
		for (int k=0; k<banned.size(); k++) bannedMoves.remove(banned.get(k));
		
		return bestMove;
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
		// System.out.println("Starting opponent modeling thread");
		new Thread(ProfileUpdater.globalUpdater).start();
		// System.out.println("Loading White Opening Book");
		Globals.whiteOpeningBook = OpeningBook.load(new File(Globals.PGNPath + "/whiteopen.opn"));
		// System.out.println("Loading Black Opening Book");
		Globals.blackOpeningBook = OpeningBook.load(new File(Globals.PGNPath + "/blackopen.opn"));
		// System.out.println("Loading Piece Density Statistics");
		Globals.density = PieceDensityInfo.load(new File(Globals.PGNPath+"/density.dns"));
		Globals.whiteModel[0] = PlayerModel.load(new File(Globals.PGNPath+"/genericwhitemodel.mdl"));
		Globals.blackModel[0] = PlayerModel.load(new File(Globals.PGNPath+"/genericblackmodel.mdl"));
		Globals.whiteModel[1] = PlayerModel.load(new File(Globals.PGNPath+"/weakwhitemodel.mdl"));
		Globals.blackModel[1] = PlayerModel.load(new File(Globals.PGNPath+"/weakblackmodel.mdl"));
		Globals.whiteModel[2] = PlayerModel.load(new File(Globals.PGNPath+"/strongwhitemodel.mdl"));
		Globals.blackModel[2] = PlayerModel.load(new File(Globals.PGNPath+"/strongblackmodel.mdl"));
		
		/*
		Globals.blackOpeningBook = new OpeningBook();
		Opening c = new Opening(false);
		Move mv = new Move();
		mv.fromY = 6;
		mv.toY = 5;
		mv.fromX = 4;
		mv.toX = 4;
		mv.piece = Chessboard.PAWN;
		mv.promotionPiece = 6;
		c.addMove(mv);
		Move mv2 = new Move();
		mv2.fromY = 6;
		mv2.toY = 4;
		mv2.fromX = 3;
		mv2.toX = 3;
		mv2.piece = Chessboard.PAWN;
		mv2.promotionPiece = 6;
		c.addMove(mv2);
		Globals.blackOpeningBook.addOpening(c);
		// System.out.println(Globals.whiteOpeningBook.toString());
		
		Opening c1 = new Opening(false);
		Move mv11 = new Move();
		mv11.fromY = 6;
		mv11.toY = 4;
		mv11.fromX = 6;
		mv11.toX = 6;
		mv11.piece = Chessboard.PAWN;
		mv11.promotionPiece = 6;
		c1.addMove(mv11);
		Move mv12 = new Move();
		mv12.fromY = 6;
		mv12.toY = 5;
		mv12.fromX = 1;
		mv12.toX = 1;
		mv12.piece = Chessboard.PAWN;
		mv12.promotionPiece = 6;
		c1.addMove(mv12);
		Globals.blackOpeningBook.addOpening(c1);
		System.out.println(Globals.blackOpeningBook.toString());
		*/
		/*
		for(int i = 0; i < 100; i++) {
			for(int j = 0; j < Globals.blackOpeningBook.getOpening(i).getMoveNumber(); j++) {
				System.out.println(Globals.blackOpeningBook.getOpening(i).getMove(j).fromY);
				System.out.println(Globals.blackOpeningBook.getOpening(i).getMove(j).toY);
				System.out.println(Globals.blackOpeningBook.getOpening(i).getMove(j).fromX);
				System.out.println(Globals.blackOpeningBook.getOpening(i).getMove(j).toX);
				System.out.println(Globals.blackOpeningBook.getOpening(i).getMove(j).piece);
				System.out.println(Globals.blackOpeningBook.getOpening(i).getMove(j).promotionPiece);
				System.out.println("-----");
			}
			System.out.println("***************");
		}
		*/
		
		// System.out.print(Globals.whiteOpeningBook.toString());
		/*
		for(int i = 0; i < Globals.whiteOpeningBook.openings.size(); i++)
		{
			System.out.print(Globals.whiteOpeningBook.openings.elementAt(i).toString());
		}
		*/
		//Load evaluation functions into the system, specialized --> general.
		//Globals.evaluationFunctionVector.add(new KRKEvaluationFunction());
		Globals.evaluationFunctionVector.add(new KPKEvaluationFunction());
		Globals.evaluationFunctionVector.add(new DefaultEvaluationFunction(null));
		
		EvaluationGlobals.init();
		
		dbArchetype = new Darkboard(true);
	}


}
