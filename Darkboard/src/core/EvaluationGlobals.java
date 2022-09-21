/*
 * Created on 7-apr-06
 *
 */
package core;

import java.util.Vector;

import ai.evolve.SimpleWeightSet;
import ai.player.Player;

/**
 * @author Nikola Novarlic
 *
 */
public class EvaluationGlobals {
	
	public boolean OUTPUT_MOVE_TREE = false;
	public boolean SAVE_MOVE_TREE_TO_DISK = false;
	public boolean ALWAYS_FORCE_DEEP_SEARCH = false;
	public static final int DEFAULT_MOVE_DANGER_FALLOFF = 30;
	
	public static SimpleWeightSet weights = null;
	
	public boolean usingExtendedGameTree = false;
	
	public int protectionMatrix[][] = new int[8][8];
	public int leastProtectingPiece[][] = new int[8][8];
	public float bonusMatrix[][] = new float[8][8];
	public boolean enemyKingMatrix[][] = new boolean[8][8];
	public float pieceValues[] = new float[6];
	public float pawnIncrements[] = new float[8];
	public int pawnSquaresInFiles[] = new int[8];
	
	public int pawnMinLocation[] = new int[8];
	public int pawnMaxLocation[] = new int[8];
	
	public boolean stopSquareFromOpponentMove = false;
	public int stopSquareX, stopSquareY;

	public static float dangerModifiers[] =
	{10.0f, 9.5f, 9.0f, 8.5f, 8.0f, 7.5f, 7.0f, 6.5f, 6.0f, 5.5f, 5.0f, 4.5f, 4.0f, 3.5f, 3.0f, 2.5f, 2.0f, 1.6f, 1.4f, 1.3f, 1.2f, 1.15f, 1.1f, 1.09f, 1.08f, 1.07f, 1.06f,
		1.05f, 1.04f, 1.03f, 1.02f };
	public static float protectionModifiers[] = {0.4f,0.4f,0.3f,0.25f,0.15f,0.35f};	
	public float currentProtectionModifiers[] = new float[6];

	public int pawns,knights,bishops,rooks,queens,totalMaterial,materialDelta;
	public float materialRatio;
	public int check1, check2, capturex, capturey, ptries;
	public core.Metaposition testbedBoard;
	public static int masks[] = {0xFE,0xFD,0xFB,0xF7,0xEF,0xDF,0xBF,0x7F};
	public static byte zerobyte[];
	public static int zeroint[];
	public static float zerofloat[];
	public static float dangerFunction[]; //precalculated function for better performance...
	public static int kingWhere[][] = new int[8][8];
	public static boolean directions[] = new boolean[8];
	public static int vectors[][];
	public static int vectorsBlack[][];
	public static int capVectors[][];
	public static int capVectorsBlack[][];
	public static int vectorsY[][];
	public static int vectorsBlackY[][];
	public static int capVectorsY[][];
	public static int capVectorsBlackY[][];
	public int evaluatedPositions;
	public int minimaxDepth = 2;
	public int pruningNewMovesAmount; //max number of unexplored moves to search
	public int pruningOldMovesAmount; //max number of already explored moves to search
	public float bestMoveValue = 0.0f;
	public double opponentDensityData[][][] = new double[8][8][7];
	public double opponentProtectionData[][] = new double[8][8];

	public int opponentControlledSquares;
	public int opponentTotalAge; //sum of all age ratings on the chessboard.
	public float kingLocationBonusWeight = 0.0f;

	
	public static float currentDangerModifier;
	public static Vector powerMoves = new Vector(); //a power move is believed to capture an enemy piece on the basis of a previous illegal move.
	//Example: Ra1-a8 illegal -> Ra1-a7 becomes a power move (unless a7 is guaranteed empty) and the engine will assume a capture if chosen.
	
	/* This field contains the risk weight to be used in the evaluation function. The
	 * value of a position is a weighed avg of its actual maximax value and the minimum value
	 * found on the way there. This is the weight. When the AI is winning, it tends to favor
	 * the high value (taking more risks), and when it's losing, it plays more conservatively
	 * by giving more importance to the min value.
	 */
	public float maximaxPositionWeight = 0.5f; 
	public float oneMinusMaximaxPositionWeight = 0.5f; //for performance reasons...
	
	public core.SquareEvaluator whiteEvaluator = new core.SquareEvaluator(true);
	public core.SquareEvaluator blackEvaluator = new core.SquareEvaluator(false);
	public core.SquareEvaluator currentEvaluator;
	public core.EvaluationTable evaluationTable = new core.EvaluationTable();
	public int evaluationTableSize=0;
	
	public core.EvaluationTree moveTree;
	public int timePressure = 0; //an indicator of how hard-pressed for time we are...
	
	public static final float kingLocationModifiers[][] =
	{
		{1.0f, 1.2f, 1.4f, 1.6f, 1.6f, 1.4f, 1.2f, 1.0f},
		{1.2f, 1.7f, 2.0f, 2.4f, 2.4f, 2.0f, 1.7f, 1.2f},
		{1.4f, 2.0f, 2.7f, 3.5f, 3.5f, 2.7f, 2.0f, 1.4f},
		{1.6f, 2.4f, 3.5f, 4.2f, 4.2f, 3.5f, 2.4f, 1.6f},
		{1.6f, 2.4f, 3.5f, 4.2f, 4.2f, 3.5f, 2.4f, 1.6f},
		{1.4f, 2.0f, 2.7f, 3.5f, 3.5f, 2.7f, 2.0f, 1.4f},
		{1.2f, 1.7f, 2.0f, 2.4f, 2.4f, 2.0f, 1.7f, 1.2f},
		{1.0f, 1.2f, 1.4f, 1.6f, 1.6f, 1.4f, 1.2f, 1.0f},
	};
	
	public byte kingLocationX, kingLocationY;
	
	public EvaluationGlobals(Player p)
	{
		testbedBoard = new core.Metaposition(p);
	}

	public static void init()
	{
		vectors = new int[7][];
		for (int k=0; k<=5; k++)
			vectors[k] = Chessboard.getPieceMovementVectorX(k,true);
		vectorsBlack = new int[7][];
		for (int k=0; k<=5; k++)
		vectorsBlack[k] = Chessboard.getPieceMovementVectorX(k,false);
		capVectors = new int[7][];
		for (int k=0; k<=5; k++)
		capVectors[k] = Chessboard.getPieceCaptureVectorX(k,true);
		capVectorsBlack = new int[7][];
		for (int k=0; k<=5; k++)
		capVectorsBlack[k] = Chessboard.getPieceCaptureVectorX(k,false);

		vectorsY = new int[7][];
		for (int k=0; k<=5; k++)
			vectorsY[k] = Chessboard.getPieceMovementVectorY(k,true);
		vectorsBlackY = new int[7][];
		for (int k=0; k<=5; k++)
		vectorsBlackY[k] = Chessboard.getPieceMovementVectorY(k,false);
		capVectorsY = new int[7][];
		for (int k=0; k<=5; k++)
		capVectorsY[k] = Chessboard.getPieceCaptureVectorY(k,true);
		capVectorsBlackY = new int[7][];
		for (int k=0; k<=5; k++)
		capVectorsBlackY[k] = Chessboard.getPieceCaptureVectorY(k,false);
			
		zerobyte = new byte[64];
		zeroint = new int[64];
		zerofloat = new float[64];
			
		for (int k=0; k<64; k++)
		{
			zerobyte[k] = (byte) 0;
			zeroint[k] = 0;
			zerofloat[k] = 0.0f;
		}
			
		dangerFunction = new float[2048];
		calculateDangerFunction(EvaluationGlobals.DEFAULT_MOVE_DANGER_FALLOFF); //default 25 move falloff...
			
		weights = new SimpleWeightSet();
			
	}
	
	/*
	 * The danger function associates a danger value to a certain number of moves,
	 */
	private static void calculateDangerFunction(int falloff)
	{
		if (falloff>=EvaluationGlobals.dangerFunction.length || falloff<=0) return;
		
		for (int k=0; k<falloff; k++)
		{
			EvaluationGlobals.dangerFunction[k] = 1.0f * k / falloff; //a linear function, for now...
		}
		
		for (int k=falloff; k<EvaluationGlobals.dangerFunction.length; k++) EvaluationGlobals.dangerFunction[k] = 1.0f;
	}

	public void clearBonusMatrix()
	{
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
		bonusMatrix[k][j] = 0.0f;
	}
	
	public void initEvaluationEngine(core.Metaposition sc)
	{
		sc.bonus = 0.0f;
		EvaluationFunction.currentNode = null;
		sc.updateTotalAge();
		currentEvaluator = (sc.isWhite()? whiteEvaluator : blackEvaluator);
		calculateStaticValues(sc);
		sc.calculateFriendlyMaterial();	
		EvaluationGlobals.currentDangerModifier = sc.dangerModifier();
		sc.computeKingLocation();
		sc.calculateKingLocationBonus();	
		sc.calculateRiskWeight();
		
		if (core.Globals.usePools) sc.owner.pool.computeModelingData(sc.owner);
		
		for (int k=0; k<6; k++) currentProtectionModifiers[k] = (1.0f-EvaluationGlobals.protectionModifiers[k])*(sc.piecesLeft+sc.pawnsLeft)/15.0f;
	}
	
	public void calculateStaticValues(core.Metaposition situation)
	{
		int left = situation.pawnsLeft + situation.piecesLeft;
		int missing = 15 - left;
		
		for (int k=0; k<5; k++) pieceValues[k] = weights.weights[k];
		for (int k=0; k<missing; k++)
			for (int j=0; j<5; j++) pieceValues[j]*=weights.weights[j+5]; //increments
		
		float pInc = 1.0f; 
		for (int k=0; k<6; k++)
		{
			pawnIncrements[k] = pInc;
			pInc*= weights.weights[SimpleWeightSet.W_ADJACENT_PAWN_I];
		}
	}
	
	/**
	 * Calculates the timePressure value. The higher, the more hard-pressed for time we are.
	 * A value of 10 or higher will switch to the faster algorithm.
	 * @param timed
	 * @param time
	 * @param oppTime
	 * @param base
	 * @param increment
	 */
	public void calculateTimePressure(boolean timed, int time, int oppTime, int base, int increment)
	{
		if (!timed)
		{
			timePressure = 0;
			return;
		}
		
		timePressure = 0;
		if (increment == 0) timePressure += 2;
		if (increment == 1) timePressure += 1;
		
		float ratio = 1.0f * time / base;
		if (ratio<0.75f) timePressure += 1;
		if (ratio<0.5f) timePressure += 1;
		if (ratio<0.25f) timePressure += 2;
		if (ratio<0.125f) timePressure += 3;
		if (ratio<0.0625f) timePressure += 4;
		
		float oppratio = 1.0f * time / oppTime;
		if (oppratio<0.75f) timePressure *= 1.3;
		if (oppratio<0.5f) timePressure *= 1.7;
		if (oppratio<0.25f) timePressure *= 2.5f;
	}

}
