/*
 * Created on 2-nov-05
 *
 */
package core;

/**
 * @author Nikola Novarlic
 *
 */
public class SquareEvaluator {
	
	/**
	 * A 128x64 array containing evaluation bonuses for each configuration of enemy pieces
	 * on each square on the chessboard. (6 pieces + 1 empty = 2^7 = 128 combinations)
	 */
	public float opponentSquareValues[][] = new float[128][64];
	
	
	public SquareEvaluator()
	{
	}
	
	public SquareEvaluator(boolean white)
	{
		float pawns[] = new float[64];
		float knights[] = new float[64];
		float bishops[] = new float[64];
		float rooks[] = new float[64];
		float queens[] = new float[64];
		float kings[] = new float[64];
		float empty[] = new float[64];
		
		float pawnValues[] = {-0.0f, -2.0f, -1.0f, -0.25f, -0.1f, -0.05f, -0.02f, -0.0f};
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int index = k*8 + j;
				
				pawns[index] = (white? pawnValues[j] : pawnValues[7-j] );
				/*knights[index] = -0.01f;
				bishops[index] = -0.01f;
				rooks[index] = -0.02f;
				queens[index] = -0.03f;
				kings[index] = -0.02f;
				empty[index] = 0.04f;*/
				knights[index] = -0.01f;
				bishops[index] = -0.01f;
				rooks[index] = -0.02f;
				queens[index] = -0.025f;
				kings[index] = -0.005f;
				empty[index] = 0.0f;				
			}
		
		
		composeFromValues(pawns,knights,bishops,rooks,queens,kings,empty);
	}
	
	/**
	 * There are 128 combinations of enemy pieces/emptiness of the chessboard. Instead of
	 * summing the modifiers for each square, piece and position, we sum them beforehand to save lots of time.
	 * @param pawns
	 * @param knights
	 * @param bishops
	 * @param rooks
	 * @param queens
	 * @param kings
	 * @param empty
	 */
	public void composeFromValues(float pawns[], float knights[], float bishops[], float
		rooks[], float queens[], float kings[], float empty[])
	{
		float array[][] = new float[7][];
		
		array[Chessboard.PAWN] = pawns;
		array[Chessboard.KNIGHT] = knights;
		array[Chessboard.BISHOP] = bishops;
		array[Chessboard.ROOK] = rooks;
		array[Chessboard.QUEEN] = queens;
		array[Chessboard.KING] = kings;
		array[Chessboard.EMPTY] = empty;
		
		for (int k=0; k<128; k++)
		{
			for (int piece = 0; piece<7; piece++)
			{
				if ((k & (1<<piece)) != 0)
				{
					for (int j=0; j<64; j++) opponentSquareValues[k][j] += array[piece][j];
				}
			}
			//System.out.println(opponentSquareValues[k][9]);
		}
	}

}
