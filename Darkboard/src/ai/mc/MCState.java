package ai.mc;

import core.Move;

public interface MCState {
	
	public static final int MSG_SILENT = 0;
	public static final int MSG_ILLEGAL = 1;
	public static final int MSG_CAPTURE = 2;
	public static final int MSG_CHECK = 3;
	public static final int MSG_PAWN = 4;
	public static final int MSG_CAP_CH = 5;
	public static final int MSG_CAP_PAWN = 6;
	public static final int MSG_CH_PAWN = 7;
	public static final int MSG_CAP_CH_PAWN = 8;

	
	float eval(int set);
	float eval(int set, Move m, MCState after);
	
	float chanceOfMessage(int set, Move m, int msg, int submsg);
	public float getHeuristicBias(int set, Move m);
	
	Move[] getMoves(int set);
	MCState[] getStates(int set, Move[] moves);
	MCState getState(int set, Move move);
	
	public boolean isBroken();

}
