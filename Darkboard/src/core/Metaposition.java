/*
 * Created on 5-ott-05
 *
 */
package core;


import java.util.Vector;

import ai.evolve.SimpleWeightSet;
import ai.mc.MCState;
import ai.planner.Discardable;
import ai.player.Darkboard;
import ai.player.Player;

/**
 * @author Nikola Novarlic
 * A compact class for deeper exploration. Each byte contains information about one square,
 * so that the structure is far smaller than a Chessboard. The leftmost bit tells whether
 * the square is occupied by a friendly piece or not (1 if it is), the remaining 7 tell
 * whether it is possible for a piece of the given type to be there (or the piece itself,
 * if it's friendly).
 * 
 * Also, we reuse Metapositions by "releasing" them into a vector when we are done
 * with them. And then we clear them and reassign them.
 */
public class Metaposition implements Discardable, MCState {
	
	public Player owner;
	
	public byte squares[] = new byte[64];
	public char ageMatrix[] = new char[64];
	public int totalAge;
	byte otherInfo[] = new byte[5];
	byte pawnInfo[] = new byte[8]; //each byte is a column, bits 2-4 are max pawns in column, bits 5-7 are min pawns
	public byte pawnsLeft = 8;
	public byte piecesLeft = 7;
	float bonus = 0.0f;
	
	static Vector chessboardVector = new Vector();
	
	private static Move minimaxBestMove;
	private static float minimaxMinValue;
	private static float minimaxPositionValue;

	public static byte squareIndex(byte x, byte y) { return (byte)(x*8 + y); }
	public boolean isFriendlyPiece(byte x, byte y)
	{
		return ((squares[x*8 + y] & 0x80) != 0);
	}
	public boolean isFriendlyPiece(int x, int y)
	{
		return isFriendlyPiece((byte)x,(byte)y);
	}
	public byte getFriendlyPiece(byte x, byte y)
	{
		byte b = squares[x*8 + y];
		if ((b & 0x80)==0) return (byte)Chessboard.EMPTY;
		else return (byte)(b & 0x7F);
	}
	public byte getFriendlyPiece(int x, int y)
	{
		return getFriendlyPiece((byte)x,(byte)y);
	}
	public int getPawnsForFile(int x)
	{
		int out = 0;
		for (int k=0; k<8; k++)
			if (getFriendlyPiece(x,k)==Chessboard.PAWN) out++;
		return out;
	}
	public int getSquareWithPiece(int piece)
	{
		for (int k=0; k<64; k++)
		{
			byte b = squares[k];
			if ((b & 0x80)!=0 && (byte)(b & 0x7F) == (byte)piece) return k;
		}
		return -1;
	}
	public void getSquaresWithPiece(int piece, int s[])
	{
		int index=0;
		for (int j=0; j<s.length; j++) s[j]=-1;
		for (int k=0; k<64; k++)
		{
			byte b = squares[k];
			if ((b & 0x80)!=0 && (byte)(b & 0x7F) == (byte)piece) s[index++]=k;
		}
	}
	public void getSquaresWithMajorPieces(int s[])
	{
		int index=0;
		for (int j=0; j<s.length; j++) s[j]=-1;
		for (int k=0; k<64; k++)
		{
			byte b = squares[k];
			if ((b & 0x80)!=0 && ((byte)(b & 0x7F) == (byte)Chessboard.ROOK
				|| (byte)(b & 0x7F) == (byte)Chessboard.QUEEN)) s[index++]=k;
		}
	}
	
	public int getSquareWithEnemyPiece(int piece)
	{
		byte mask = (byte)(1 << piece);
		for (int k=0; k<64; k++)
		{
			byte b = squares[k];
			if ((b & 0x80)==0 && (b & mask)!=0) return k;
		}
		return -1;
	}
	
	public boolean canContain(byte x, byte y, byte piece)
	{
		byte b = squares[x*8 + y];
		if ((b & 0x80)!=0) return false; //friend
		return ((b & (1 << piece)) != 0);
	}
	public boolean mayBeEmpty(byte x, byte y)
	{
		if (isFriendlyPiece(x,y)) return false;
		return canContain(x,y,(byte)Chessboard.EMPTY);
	}
	public boolean canOnlyContainKing(byte x, byte y)
	{
		byte b = squares[x*8+y];
		return (b==1<<Chessboard.KING || b==((1<<Chessboard.KING)|(1<<Chessboard.EMPTY)));
	}
	public boolean mayBeEmpty(int x, int y)
	{
		return mayBeEmpty((byte)x,(byte)y);
	}
	public boolean isEmpty(byte x, byte y)
	{
		return squares[x*8 + y] == 64;
	}
	public boolean isEmpty(int x, int y)
	{
		return isEmpty((byte)x,(byte)y);
	}
	public boolean canContainEnemyKing(byte x, byte y)
	{
		return canContain(x,y,(byte)Chessboard.KING);
	}
	public boolean canContainEnemyPawn(byte x, byte y)
	{
		return canContain(x,y,(byte)Chessboard.PAWN);
	}
	public boolean canContainEnemyPawn(int x, int y)
	{
		return canContain((byte)x,(byte)y,(byte)Chessboard.PAWN);
	}
	public void containsAnythingButPawn(byte x, byte y)
	{
		squares[x*8 + y] = 126;
	}
	public boolean surelyContainsEnemyPawn(byte x, byte y)
	{
		return squares[x*8 + y]==1;
	}
	public void setFriendlyPiece(byte x, byte y, byte piece)
	{
		squares[x*8 + y] = (byte)((1<<7) | piece);
	}
	public void setFriendlyPiece(int x, int y, int piece)
	{
		setFriendlyPiece((byte)x,(byte)y,(byte)piece);
	}
	public void setUnknown(byte x, byte y)
	{
		squares[x*8 + y] = 127; //01111111
	}
	public void setPiecePossible(byte x, byte y, byte piece)
	{
		squares[x*8 + y] |= (1<<piece);
	}
	public void setPiecePossible(int x, int y, int piece)
	{
		setPiecePossible((byte)x,(byte)y,(byte)piece);
	}
	public void setPieceImpossible(byte x, byte y, byte piece)
	{
		squares[x*8 + y] &= EvaluationGlobals.masks[piece];
	}
	public void setPieceImpossible(int x, int y, int piece)
	{
		setPieceImpossible((byte)x,(byte)y,(byte)piece);
	}	
	public void setEmpty(byte x, byte y)
	{
		squares[x*8 + y] = 64;
	}
	public byte getMinPawns(byte x)
	{
		return (byte)(pawnInfo[x]&7);
	}
	public byte getMaxPawns(byte x)
	{
		return (byte)((pawnInfo[x]>>3)&7);
	}
	public int getPossiblePawnLocations(byte x)
	{
		int result = 0;
		for (byte j=0; j<8; j++)
		{
			if (canContainEnemyPawn(x,j)) result++;
		}
		return result;
	}
	public void setMinPawns(byte x, byte howMany)
	{
		if (howMany<0) howMany = 0;
		pawnInfo[x] = (byte)((pawnInfo[x]&0xF8)|(howMany&7));
	}
	public void setMaxPawns(byte x, byte howMany)
	{
		if (howMany<0) howMany = 0;
		pawnInfo[x] = (byte)((pawnInfo[x]&0xC7)|((howMany&7)<<3));
		if (howMany==0)
		{
			//no more pawns are possible in this column. Update the chessboard.
			disposeOfPawns(x);
		}
	}
	public int getPieceNumber()
	{
		int k=0;
		for (int x=0; x<64; x++) 
			if ((squares[x] & 0x80)!=0) k++;
		return k;
	}
	public void disposeOfPawns(byte x)
	{
		for (byte y=0; y<8; y++)
		{
			if (canContainEnemyPawn(x,y))
				setPieceImpossible(x,y,(byte)Chessboard.PAWN);
		}
	}
	public void disposeOfPawns()
	{
		for (byte y=0; y<8; y++) disposeOfPawns(y);
	}
	public void disposeOfPieces()
	{
		for (byte x=0; x<8; x++)
			for (byte y=0; y<8; y++) 
			{
				if (!isFriendlyPiece(x,y))
				{
					setPieceImpossible(x,y,(byte)Chessboard.KNIGHT);
					setPieceImpossible(x,y,(byte)Chessboard.BISHOP);
					setPieceImpossible(x,y,(byte)Chessboard.ROOK);
					setPieceImpossible(x,y,(byte)Chessboard.QUEEN);
				}
			}
	}
	
	public void setAge(byte t)
	{
		otherInfo[4] = t;
	}
	
	public byte getAge()
	{
		return otherInfo[4];
	}
	
	//we remember the opponent's pawn tries to make appropriate inferences in the event of a capture
	public void setOpponentPawnTries(byte t)
	{
		otherInfo[2] = t;
	}
	public byte getOpponentPawnTries()
	{
		return otherInfo[2];
	}
	public void setPawnTries(byte t)
	{
		otherInfo[3] = t;
	}
	public byte getPawnTries()
	{
		return otherInfo[3];
	}
	public void setCastleStatus(byte b)
	{
		//0, can castle left and right; 1, can castle left; 2, can right; 3, can't.
		otherInfo[0] = b;
	}
	public byte getCastleStatus()
	{
		return (otherInfo[0]);
	}
	public void leftRookMoved()
	{
		byte b = getCastleStatus();
		if (b==0 || b==2) setCastleStatus((byte)2); else setCastleStatus((byte)3);
	}
	public void rightRookMoved()
	{
		byte b = getCastleStatus();
		if (b==0 || b==1) setCastleStatus((byte)1); else setCastleStatus((byte)3);
	}
	public void kingMoved()
	{
		setCastleStatus((byte)3);
	}
	public void setWhite(boolean b)
	{
		otherInfo[1] = (byte)(b? 0 : 1);
	}
	public boolean isWhite()
	{
		return (otherInfo[1]==0);
	}
	public byte getFirstRank()
	{
		return (byte)(isWhite()? 0 : 7);
	}
	public byte getSecondRank()
	{
		return (byte)(isWhite()? 1 : 6);
	}
	public byte getLastRank()
	{
		return (byte)(isWhite()? 7 : 0);
	}
	public byte getSecondLastRank()
	{
		return (byte)(isWhite()? 6 : 1);
	}
	public void setAge(int x, int y, int howMuch)
	{
		totalAge += (howMuch - ageMatrix[x*8 + y]);
		ageMatrix[x*8 + y] = (char)howMuch;
	}
	public void ageSquare(int x, int y, int howMuch)
	{
		totalAge += howMuch;
		ageMatrix[x*8 + y] += howMuch;
	}
	public Metaposition(Player own)
	{
		owner = own;
	}
	
	public static Metaposition getChessboard(Player owner)
	{
		return new Metaposition(owner);
	}
	
	public static Metaposition getChessboard(Metaposition copy)
	{
		Metaposition out = new Metaposition(copy.owner);
		
		System.arraycopy(copy.squares,0,out.squares,0,64);
		System.arraycopy(copy.ageMatrix,0,out.ageMatrix,0,64);
				
		//for (int k=0; k<copy.squares.length; k++) out.squares[k] = copy.squares[k];
		for (int k=0; k<copy.otherInfo.length; k++) out.otherInfo[k] = copy.otherInfo[k];
		for (int k=0; k<copy.pawnInfo.length; k++) out.pawnInfo[k] = copy.pawnInfo[k];
		out.pawnsLeft = copy.pawnsLeft;
		out.piecesLeft = copy.piecesLeft;
		out.bonus = copy.bonus;
		out.totalAge = copy.totalAge;
		out.owner = copy.owner;
		
		return out;
	}
	
	public void setup(boolean white)
	{
		setWhite(white);
		setCastleStatus((byte)0);
		for (byte k=0; k<8; k++)
		{
			setMinPawns(k,(byte)1);
			setMaxPawns(k,(byte)1);
		}
		
		clear();
		
		for (int k=0; k<8; k++) setFriendlyPiece(k,getSecondRank(),Chessboard.PAWN);
		setFriendlyPiece(0,getFirstRank(),Chessboard.ROOK);
		setFriendlyPiece(7,getFirstRank(),Chessboard.ROOK);
		setFriendlyPiece(1,getFirstRank(),Chessboard.KNIGHT);
		setFriendlyPiece(6,getFirstRank(),Chessboard.KNIGHT);
		setFriendlyPiece(2,getFirstRank(),Chessboard.BISHOP);
		setFriendlyPiece(5,getFirstRank(),Chessboard.BISHOP);
		setFriendlyPiece(3,getFirstRank(),Chessboard.QUEEN);
		setFriendlyPiece(4,getFirstRank(),Chessboard.KING);
		
		for (int k=0; k<8; k++) setPiecePossible(k,getSecondLastRank(),Chessboard.PAWN);
		setPiecePossible(0,getLastRank(),Chessboard.ROOK);
		setPiecePossible(7,getLastRank(),Chessboard.ROOK);
		setPiecePossible(1,getLastRank(),Chessboard.KNIGHT);
		setPiecePossible(6,getLastRank(),Chessboard.KNIGHT);
		setPiecePossible(2,getLastRank(),Chessboard.BISHOP);
		setPiecePossible(5,getLastRank(),Chessboard.BISHOP);
		setPiecePossible(3,getLastRank(),Chessboard.QUEEN);
		setPiecePossible(4,getLastRank(),Chessboard.KING);		
		
		for (int k=0; k<8; k++)
			for (int j=2; j<6; j++) setPiecePossible(k,j,Chessboard.EMPTY);
			
		for (int k=0; k<8; k++)
		{
			ageMatrix[k*8 + getLastRank()] = 10;
			ageMatrix[k*8 + getSecondLastRank()] = 10;
		}
		
	}
	
	/*public void release()
	{
		chessboardVector.add(this);
	}*/
	
	public void clear()
	{
		// System.arraycopy(EvaluationGlobals.zerobyte,0,squares,0,64);
		for (int k=0; k<64; k++) squares[k] = 0;
	}
	
	protected static void clearKingMatrix()
	{
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) EvaluationGlobals.kingWhere[k][j] = 0;
	}
	
	protected static void addToKingMatrix(byte x, byte y)
	{
		EvaluationGlobals.kingWhere[x][y]++;
	}
	
	protected void addTargetSquaresToKingMatrix(Move m,int check, int capture)
	{
		//we add the squares controlled by the moving piece
		int dx = (m.toX > m.fromX? -1 : (m.toX==m.fromX? 0 : 1)); //reversed deltas.
		int dy = (m.toY > m.fromY? -1 : (m.toY==m.fromY? 0 : 1));
		int x,y;
		int piece = getFriendlyPiece(m.toX,m.toY);
		int movementX[] = Chessboard.getPieceCaptureVectorX(piece,this.isWhite());
		int movementY[] = Chessboard.getPieceCaptureVectorY(piece,this.isWhite());
		boolean movesOnce = Chessboard.movesOnlyOnce(piece); //Knight and Pawn
		
		for (int k=0; k<movementX.length; k++)
		{
			//when pursuing a direction, we NEVER go back from where the piece moved
			//(the king can't be there), and we ONLY go forward if there was a capture.
			int incrementX = movementX[k];
			int incrementY = movementY[k];
			//System.out.println("Testing "+incrementX+" "+incrementY);
			boolean cont = true;
			x = m.toX; y = m.toY;
			if (incrementX==dx && incrementY==dy && (m.piece!=Chessboard.PAWN || m.toY!=getLastRank())) cont = false; //don't go back unless you were a pawn and have just promoted!
			if (capture==Chessboard.NO_CAPTURE && !movesOnce && 
				incrementX==-dx && incrementY==-dy) cont = false; //don't go forward unless you captured!
			if (incrementY==0 && check!=Chessboard.CHECK_RANK) cont = false;
			if (incrementX==0 && check!=Chessboard.CHECK_FILE) cont = false;
			if (incrementX!=0 && incrementY!=0 && (check==Chessboard.CHECK_FILE ||
				check==Chessboard.CHECK_RANK)) cont = false; //only appropriate check directions.
			//System.out.println("Still testing "+incrementX+" "+incrementY);
			while (cont)
			{
				x+=incrementX; y+=incrementY;
				if (x<0 || y<0 || x>=8 || y>=8) break;
				//System.out.println("Testing "+x+" "+y);
				if (canContainEnemyKing((byte)x,(byte)y))
				{
					//a candidate square. If diagonal, check whether it is the right diagonal.
					if (check==Chessboard.CHECK_LONG_DIAGONAL || check==Chessboard.CHECK_SHORT_DIAGONAL)
					{
						if (Chessboard.isSquareCompatibleWithDiagonalCheck(x,y,check,incrementX,
							incrementY)) addToKingMatrix((byte)x,(byte)y);
					} else addToKingMatrix((byte)x,(byte)y);
				}
				if (!mayBeEmpty((byte)x,(byte)y)) cont = false; //stop if you encounter an obstacle.
				if (movesOnce) cont = false; //...or if the piece is a knight or pawn.
			}
			
		}
		
		
	}
	
	protected void processDiscoveryCheck(Move m, int check, int capture)
	{
		//a discovery check proceeds as follows: we examine each one of the 8 directions
		//except for the move's direction and its opposite. If we find a compatible friendly piece,
		//we mark the opposite direction as possible.
		int dx = (m.toX > m.fromX? -1 : (m.toX==m.fromX? 0 : 1)); //reversed deltas.
		int dy = (m.toY > m.fromY? -1 : (m.toY==m.fromY? 0 : 1));
		int x,y;
		int movementX[] = Chessboard.queenOffsetX;
		int movementY[] = Chessboard.queenOffsetY;
		
		for (int k=0; k<8; k++) EvaluationGlobals.directions[k] = true;
		
		for (int k=0; k<8; k++)
		{
			int incrementX = movementX[k];
			int incrementY = movementY[k];
			if (incrementX==dx && incrementY==dy) EvaluationGlobals.directions[k] = false;
			if (incrementX==-dx && incrementY==-dy) EvaluationGlobals.directions[k] = false;
			if (check==Chessboard.CHECK_RANK && incrementY!=0) EvaluationGlobals.directions[k] = false;
			if (check==Chessboard.CHECK_FILE && incrementX!=0) EvaluationGlobals.directions[k] = false;
			if ((incrementX==0 || incrementY==0) && (check==Chessboard.CHECK_LONG_DIAGONAL ||
				check==Chessboard.CHECK_SHORT_DIAGONAL)) EvaluationGlobals.directions[k] = false;
				
			if (!EvaluationGlobals.directions[k]) continue;
			
			EvaluationGlobals.directions[k] = false; //now false by default...
			
			//compatible direction, but you need a compatible piece in order to call it a candidate.
			x = m.fromX; y = m.fromY;
			boolean cont = true;
			while (cont)
			{
				x+=incrementX; y+=incrementY;
				if (x<0 || y<0 || x>=8 || y>=8) break;
				if (isFriendlyPiece(x,y))
				{
					byte piece = getFriendlyPiece(x,y);
					if (Move.pieceCompatible(piece,check)) 
					{
						EvaluationGlobals.directions[k] = true; break;
					} else break;
				} else if (!mayBeEmpty(x,y)) break;
			}
			
		}
		
		//now, go through the compatible directions and add kings in the opposite directions!
		for (int k=0; k<8; k++)
		{
			if (!EvaluationGlobals.directions[k]) continue;
			int incrementX = -movementX[k];
			int incrementY = -movementY[k];
			x = m.fromX; y = m.fromY;
			boolean cont = true;
			while (cont)
			{
				x+=incrementX; y+=incrementY;
				if (x<0 || y<0 || x>=8 || y>=8) break;
				if (canContainEnemyKing((byte)x,(byte)y))
				{
					//if a diagonal check, make sure it's the right diagonal...
					if (check==Chessboard.CHECK_LONG_DIAGONAL || check==Chessboard.CHECK_SHORT_DIAGONAL)
					{
						if (Chessboard.isSquareCompatibleWithDiagonalCheck(x,y,check,incrementX,
							incrementY)) addToKingMatrix((byte)x,(byte)y);
					} else addToKingMatrix((byte)x,(byte)y); 
				}  
				if (!mayBeEmpty(x,y)) break;
			}
						
			
		}
		
	}
	
	protected void updateEnemyKing(Move m, int check1, int check2, int capture)
	{	
		if (check1==Chessboard.NO_CHECK) 
		{
			restrictEnemyKingNoCheck();
			return;
		} //don't waste any time...
		
		clearKingMatrix();
		
		boolean doubleCheck = (check1!=Chessboard.NO_CHECK && check2!=Chessboard.NO_CHECK);
		for (int k=0; k<2; k++)
		{
			int ch = (k==0? check1 : check2);
			if (ch == Chessboard.NO_CHECK) continue;
			
			//determine whether the moved piece is compatible with the check type
			boolean compatible = Move.pieceCompatible(getFriendlyPiece(m.toX,m.toY),ch);
			
			//if the piece is compatible, it must be responsible for the check UNLESS
			//it is a pawn (because pawns are weird!). Pawns can advance and check, or
			//discover a bishop which checks; we have no way to know which one.
			if (compatible)
			{
				addTargetSquaresToKingMatrix(m,ch,capture);
			}
			if (!compatible || (m.piece==Chessboard.PAWN && (ch==Chessboard.CHECK_LONG_DIAGONAL
				|| ch==Chessboard.CHECK_SHORT_DIAGONAL)))
			{
				processDiscoveryCheck(m,ch,capture);
			}
		}
		//now update the metaposition with the content of the king matrix...
		int minimum = (doubleCheck? 2 : 1); //a double check means the candidates must
			//satisfy both check sets (only 1 square will).
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (canContainEnemyKing((byte)k,(byte)j) && EvaluationGlobals.kingWhere[k][j]<minimum)
					setPieceImpossible((byte)k,(byte)j,(byte)Chessboard.KING);
			}
	}
	
	public void restrictEnemyKingNoCheck()
	{
		computeProtectionMatrix(false);
		//no check...
		for (byte k=0; k<8; k++)
		{
			for (byte j=0; j<8; j++)
			{
				if (canContainEnemyKing(k,j) && owner.globals.protectionMatrix[k][j]>0)
					setPieceImpossible(k,j,(byte)Chessboard.KING);
			}
		}
		/*for (byte k=0; k<8; k++)
		{
			for (byte j=0; j<8; j++)
			{
				byte p = getFriendlyPiece(k,j);
				
				if (p==Chessboard.EMPTY) continue;
				int movementX[] = Chessboard.getPieceCaptureVectorX(p,this.isWhite());
				int movementY[] = Chessboard.getPieceCaptureVectorY(p,this.isWhite());
				if (movementX==null) System.out.println(p);
				boolean movesOnce = Chessboard.movesOnlyOnce(p); //Knight, Pawn, King
				for (int f=0; f<movementX.length; f++)
				{
					int dx = movementX[f];
					int dy = movementY[f];
					int x = k+dx; int y = j+dy;
					while (true)
					{
						if (x<0 || y<0 || x>=8 || y>=8) break;
						setPieceImpossible((byte)x,(byte)y,(byte)Chessboard.KING);
						if (!isEmpty(x,y)) break;
						if (movesOnce) break;
						x+=dx; y+=dy;
					}
					
				}
			}
		}*/
	}
	
	public static Metaposition evolveAfterMove(Metaposition root, Move m,
		int cap, int capx, int capy, int check1, int check2, int tries)
	{
		Metaposition sc = getChessboard(root);
		
		byte start = squareIndex(m.fromX,m.fromY);
		byte end = squareIndex(m.toX,m.toY);
		
		sc.squares[end] = sc.squares[start];
		sc.setEmpty(m.fromX,m.fromY);
		
		int index = m.fromX*8 + m.fromY;
		sc.totalAge -= sc.ageMatrix[index];
		sc.ageMatrix[index] = 0;
		
		sc.setFriendlyPiece(m.toX,m.toY,(m.piece==Chessboard.PAWN &&
			m.toY==sc.getLastRank())? m.promotionPiece : m.piece);
		
		if (m.piece==Chessboard.KING)
		{
			sc.kingMoved();
			if (m.fromY == m.toY && m.fromY == sc.getFirstRank())
			{
				if (m.toX==m.fromX-2)
				{
					//left castling
					sc.setEmpty((byte)0,sc.getFirstRank());
					sc.setFriendlyPiece((byte)(m.toX+1),sc.getFirstRank(),(byte)Chessboard.ROOK);
				}
				if (m.toX==m.fromX+2)
				{
					//right castling
					sc.setEmpty((byte)7,sc.getFirstRank());
					sc.setFriendlyPiece((byte)(m.toX-1),sc.getFirstRank(),(byte)Chessboard.ROOK);
				}
			}
		}
		
		if (m.piece==Chessboard.ROOK)
		{
			if (m.fromX==0 && m.fromY==sc.getFirstRank()) sc.leftRookMoved();
			if (m.fromX==7 && m.fromY==sc.getFirstRank()) sc.rightRookMoved();
		}
		
		if (m.piece!=Chessboard.KNIGHT && m.piece!=Chessboard.KING)
		{
			byte dx = (byte)(m.toX>m.fromX? 1 : (m.toX==m.fromX? 0 : -1));
			byte dy = (byte)(m.toY>m.fromY? 1 : (m.toY==m.fromY? 0 : -1));
			byte x = (byte)(m.fromX+dx);
			byte y = (byte)(m.fromY+dy);
			while (x<8 && y<8 && x>=0 && y>=0)
			{
				index = x*8 + y;
				sc.totalAge -= sc.ageMatrix[index];
				sc.ageMatrix[index] = 0;
				if ((x==m.toX && y==m.toY)) break;
				sc.setEmpty(x,y);
				x+=dx; y+=dy;
			}
		} else
		{
			index = m.toX*8 + m.toY;
			sc.totalAge -= sc.ageMatrix[index];
			sc.ageMatrix[index] = 0;
		}
		
		if (cap==Chessboard.CAPTURE_PAWN || (cap==Chessboard.CAPTURE_PIECE && sc.piecesLeft==0))
		{ 
			sc.pawnsLeft--;
			if (cap==Chessboard.CAPTURE_PAWN)
			{
				sc.setMaxPawns(m.toX,(byte)(sc.getMaxPawns(m.toX)-1));
				sc.setMinPawns(m.toX,(byte)(sc.getMinPawns(m.toX)-1));

			}
			if (sc.pawnsLeft==0) 
			{
				sc.disposeOfPawns();
				for (byte x=0; x<8; x++) 
				{ 
					sc.setMaxPawns(x,(byte)0); sc.setMinPawns(x,(byte)0); 
				}
			} //delete them all
		}
		if (cap==Chessboard.CAPTURE_PIECE && sc.piecesLeft>0)
		{
			sc.piecesLeft--;
			//if (sc.piecesLeft==0) sc.disposeOfPieces(); //no more pieces!
		}
		
		if (cap!=Chessboard.NO_CAPTURE && (sc.piecesLeft+sc.pawnsLeft)==0)
		{
			sc.disposeOfPawns();
			sc.disposeOfPieces();
		}
		
		sc.setOpponentPawnTries((byte)tries);
		
		sc.updateEnemyKing(m,check1,check2,cap);
		
		return sc;
	}
	
	protected void updateOpponent(int capx, int capy, int c1, int c2, int tries)
	{
		int movx[];
		int movy[];
		boolean w = isWhite();
		boolean once;
		boolean couldMove,pawnControl,rookControl,kingControl;
		boolean capture = (capx>=0);
		int dx, dy, x, y;
		int vecs[][] = null;
		int vecsY[][] = null;
		int limitY = 0;
		
		if (c1==Chessboard.CHECK_FILE || c1==Chessboard.CHECK_RANK)
			if (piecesLeft==0)
			{
				//quite obviously a pawn has been promoted
				piecesLeft++;
				pawnsLeft--;
				if (pawnsLeft==0) disposeOfPawns();
			}

		owner.globals.testbedBoard.clear();
		computeProtectionMatrix(false);
		
		//System.out.println("!!! "+capx+" "+capy);
		
		if (capture) 
		{
			squares[capx*8 + capy] = 0; //applicable pieces will be filled; EMPTY will not.
			
			vecs = (!w? EvaluationGlobals.capVectors : EvaluationGlobals.capVectorsBlack);
			vecsY = (!w? EvaluationGlobals.capVectorsY : EvaluationGlobals.capVectorsBlackY);
		} else 
		{
			vecs = (!w? EvaluationGlobals.vectors : EvaluationGlobals.vectorsBlack);
			vecsY = (!w? EvaluationGlobals.vectorsY : EvaluationGlobals.vectorsBlackY);
		} 
		
		//calculate where pawns are...
		for (byte k=0; k<8; k++)
		{
			owner.globals.pawnMinLocation[k] = 10;
			owner.globals.pawnMaxLocation[k] = -1;
			if (getMinPawns(k)>0)
			{
				for (byte p=1; p<7; p++)
				{
					if (canContain(k,p,(byte)Chessboard.PAWN))
					{
						if (p<owner.globals.pawnMinLocation[k]) owner.globals.pawnMinLocation[k] = p;
						if (p>owner.globals.pawnMaxLocation[k]) owner.globals.pawnMaxLocation[k] = p;
					}
				}
			}
		}
		
		boolean leftPawnCandidate = false;
		boolean rightPawnCandidate = false; //pawn captures...
		
		byte startingPiece = 0;
		//startingPiece is used to initialize the for cycle of enemy pieces in each square.
		//It normally starts at 0, meaning that every piece is evolved, from pawns (0)
		//onwards. However, if a capture takes place and the opponent had no pawn tries on
		//his turn, we do not consider pawns.
		if (capture && getOpponentPawnTries()<1) startingPiece = 1;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (!isFriendlyPiece(k,j) && 
						(!owner.globals.stopSquareFromOpponentMove || owner.globals.stopSquareX!=k || owner.globals.stopSquareY!=j))
				{
					couldMove = false;
					for (byte piece=startingPiece; piece<=5; piece++)
					{
						if (canContain((byte)k,(byte)j,piece))
						{
							movx = vecs[piece];
							movy = vecsY[piece];
							once = Chessboard.movesOnlyOnce(piece);
							pawnControl = (piece==Chessboard.PAWN && j==getSecondLastRank() && !capture);
							kingControl = (piece==Chessboard.KING);
							for (int dir=0; dir<movx.length; dir++)
							{
								dx = movx[dir]; dy = movy[dir];
								x = k+dx; y = j+dy;
								rookControl = (dx==0 && !once && owner.globals.pawnMaxLocation[x]!=-1); //vertical movement on files with pawns, (rook & queen)
								if (rookControl)
								{
									//Rook control happens when our piece starts its move from
									//outside of the pawn envelope and tries to move all the way
									//through it.
									if (j>=owner.globals.pawnMaxLocation[x] && dy<0) limitY = owner.globals.pawnMinLocation[x]-1;
									else if (j<=owner.globals.pawnMinLocation[x] && dy>0) limitY = owner.globals.pawnMaxLocation[x]+1;
									else rookControl = false; //if the rook is INSIDE the pawn envelope, we cannot
									//rule out movement either way (the pawn might be ahead or behind the rook).
								}
								while (true)
								{
									//System.out.println("Trying "+x+" "+y+" "+piece);
									if (x<0 || y<0 || x>=8 || y>=8) break;
									if (rookControl && y==limitY) break; //prevent rooks and queens from moving through their own pawns.
									//if (!mayBeEmpty(x,y) && (getFriendlyPiece(x,y)==Chessboard.EMPTY || !capture)) break;
									if (!mayBeEmpty(x,y) && (x!=capx || y!=capy)) break;
									if (kingControl && (owner.globals.protectionMatrix[x][y]>0) && (x!=capx || y!=capy)) break; //don't go over protected squares unless it's a capture.
									//if (kingControl && capture) { System.out.println("KING: "+x+" "+y); }
									if (capture) //only legal capture moves
									{
										if (x!=capx || y!=capy)
										{
											if (once) break;
											x+=dx; y+=dy;
											continue;
										} else
										{
											if (piece==0) //pawn capture
											{
												if (k<x) leftPawnCandidate = true;
												else rightPawnCandidate = true;
											}
										}
									}
									couldMove = true;
									//System.out.println("Adding "+x+" "+y+" "+piece);
									owner.globals.testbedBoard.setPiecePossible((byte)x,(byte)y,piece);
									if (pawnControl)
									{
										pawnControl=false;
										x+=dx; y+=dy;
										continue; //pawn gets a "second chance"
									}
									if (!mayBeEmpty(x,y)) break;
									if (once) break;
									x+=dx; y+=dy;

								}
								
								
							}
							if (couldMove) owner.globals.testbedBoard.setPiecePossible((byte)k,(byte)j,(byte)Chessboard.EMPTY);
						
						}
					}
				}
			}
			
		//if opponent has pawns in first rank, upgrade them to any piece type.
		for (byte k=0; k<8; k++)
		{
			if (canContainEnemyPawn(k,getFirstRank()))
			{
				containsAnythingButPawn(k,getFirstRank());
			}
		}
			
		//if we suffered a capture, let's see which pieces could have done that...
		if (capture)
		{
			byte cx,cy;
			cx = (byte)capx;
			cy = (byte)capy;
			//if a pawn could have done that, update the pawn count in that file.

			if (owner.globals.testbedBoard.canContainEnemyPawn(cx,cy))
			{
				boolean surelyContainsPawn = surelyContainsEnemyPawn(cx,cy);
				setMaxPawns(cx,(byte)(getMaxPawns(cx)+1));
				if (surelyContainsPawn)
				{
					//also up min pawns...
					setMinPawns(cx,(byte)(getMinPawns(cx)+1));
				}
				//now find where it could have come from...
				byte myx;
				if (leftPawnCandidate)
				{
					myx = (byte)(cx-1);
					setMinPawns(myx,(byte)(getMinPawns(myx)-1));
					if (surelyContainsPawn) setMaxPawns(myx,(byte)(getMaxPawns(myx)-1));
				}
				if (rightPawnCandidate)
				{
					myx = (byte)(cx+1);
					setMinPawns(myx,(byte)(getMinPawns(myx)-1));
					if (surelyContainsPawn) setMaxPawns(myx,(byte)(getMaxPawns(myx)-1));
				}
			}
		
		}
			
		//now, calculate an OR for every square in testbed and in this.
		for (int k=0; k<64; k++)
		{
			//if only the king is left, it must have moved, so it can't be where it was before.
			if (piecesLeft==0 && pawnsLeft==0) squares[k] &= EvaluationGlobals.masks[Chessboard.KING];
			this.squares[k] |= owner.globals.testbedBoard.squares[k]; 
			//if the square can contain something not pretty, "age" it to mark it with additional risk.
			if ((squares[k] & 0x80)==0)
			{
				if ((squares[k] & 0x3F)!=0) 
				{
					ageMatrix[k]++;
					totalAge++;
				} //possible enemy piece, age it
				else
					if ((squares[k] & 0x40)==0)
					{ 
						totalAge += (EvaluationGlobals.DEFAULT_MOVE_DANGER_FALLOFF - ageMatrix[k]);
						ageMatrix[k] = EvaluationGlobals.DEFAULT_MOVE_DANGER_FALLOFF; 
					} 
				
					//certain enemy piece, age it lots to encourage investigation even in the future
				else
				{
					totalAge -= ageMatrix[k];
					ageMatrix[k] = 0; 
				} //empty, no matter how long it's been, we don't need to explore it.
			}
		} 
		
		computeKingLocation();
		//if (c1!=Chessboard.NO_CHECK) {
			computeProtectionMatrix(false); 
			for (byte k=0; k<8; k++)
				for (byte j=0; j<8; j++)
				{
					if (owner.globals.protectionMatrix[k][j]>0 && canContain(k,j,(byte)Chessboard.KING))
						setPieceImpossible(k,j,(byte)Chessboard.KING);
						
					if (tries==0 && getFriendlyPiece(k,j)==Chessboard.PAWN && j!=0 && j!=7)
					{
						
						//if we have no pawn tries, we can mark all pawn capture targets as empty,
						//except those that are lined up with our King...
						int index;
						if (k>0)
						{
							Move m = new Move(); m.fromX = k; m.toX = (byte)(k-1); m.fromY = j; m.toY = (byte)(isWhite()? j+1 : j-1 );
							if (getFriendlyPiece(m.toX,m.toY)==Chessboard.EMPTY && !mayBeProtectingKing(k,j,m))
							{
								setEmpty(m.toX,m.toY); 
								index = m.toX*8 + m.toY;
								totalAge -= ageMatrix[index];
								ageMatrix[index] = 0;
							} 
						}
						if (k<7)
						{
							Move m = new Move(); m.fromX = k; m.toX = (byte)(k+1); m.fromY = j; m.toY = (byte)(isWhite()? j+1 : j-1 );
							if (getFriendlyPiece(m.toX,m.toY)==Chessboard.EMPTY && !mayBeProtectingKing(k,j,m))
							{
								setEmpty(m.toX,m.toY); 
								index = m.toX*8 + m.toY;
								totalAge -= ageMatrix[index];
								ageMatrix[index] = 0;
							} 
						}
					}
				}
		//}
	}

	public static Metaposition evolveAfterOpponentMove(Metaposition root,
		int capx, int capy, int check1, int check2, int tries)
	{
		Metaposition sc = getChessboard(root);
		sc.setPawnTries((byte)tries);
		//sc.doAge();
		

		
		
		sc.updateOpponent(capx,capy,check1,check2,tries);
		//sc.restrictEnemyKingNoCheck(); //always applies, there is never check after an opponent move
		
		sc.setAge((byte)(root.getAge()+1));
		
		if (capx==0 && capy==sc.getFirstRank()) sc.leftRookMoved();
		if (capx==7 && capy==sc.getFirstRank()) sc.rightRookMoved();
		
		return sc;
	}
	
	public static Metaposition evolveAfterIllegalMove(Metaposition root, Move m, int capx, int capy, int c1, int c2, int tries)
	{
		//System.out.println("ILLEGAL "+m);
		Metaposition m2 = getChessboard(root);
		
		m2.removePowerMove(m);
		
		if (c1!=Chessboard.NO_CHECK || c2!=Chessboard.NO_CHECK) return m2;
		
		if (m.piece==Chessboard.KING && m2.pawnsLeft+m2.piecesLeft==0)
		{
			//only enemy king is left. An illegal king move can reveal much.
			for (byte k=0; k<8; k++)
				for (byte j=0; j<8; j++)
				{
					if (m2.canContainEnemyKing(k,j))
					{
						int dx = k - m.toX;
						int dy = j - m.toY;
						if (dx<0) dx = -dx; if (dy<0) dy = -dy;
						int dist = (dx>dy? dx : dy);
						if (dist!=1) m2.setPieceImpossible(k,j,Chessboard.KING);
					}
				}
		}
		
		if (m.piece==Chessboard.KING || m.piece==Chessboard.KNIGHT) return m2; //King case is very  difficult, we give up.
		m2.computeKingLocation();
		
		//if we were under time pressure, we are even worse off now...
		root.owner.globals.timePressure *= 1.4;
		
		if (m2.mayBeProtectingKing(m.fromX,m.fromY,m))
		{
			//System.out.println("BUT IS PROTECTING KING");
		}
		else
		{
			//no excuse!
			int deltax = m.toX - m.fromX;
			int deltay = m.toY - m.fromY;
			if (m.piece==Chessboard.PAWN)
			{
				
				if ((deltay==-1 || deltay==1) && deltax==0)
				{
					m2.setPieceImpossible(m.toX,m.toY,(byte)Chessboard.EMPTY);
					//m2.setAge(m.toX,m.toY,EvaluationGlobals.DEFAULT_MOVE_DANGER_FALLOFF);
					m2.setAge(m.toX,m.toY,0);
				}
			} else
			if (deltax==2 || deltax==-2 || deltay==2 || deltay==-2)
			{
				//moved 2 squares, there must be a piece in the middle square.
				int dx = (deltax>0? 1 : deltax==0? 0 : -1);
				int dy = (deltay>0? 1 : deltay==0? 0 : -1);
				m2.setPieceImpossible((byte)(m.fromX+dx),(byte)(m.fromY+dy),(byte)Chessboard.EMPTY);
				m2.setAge(m.fromX+dx,m.fromY+dy,EvaluationGlobals.DEFAULT_MOVE_DANGER_FALLOFF);
			} else
			{
				//addEnemyTrack(m);
				m2.createPowerMove(m);
			}
			
		}
		
		return m2;
	}
	
	public void createPowerMove(Move m)
	{
		int dx = (m.toX > m.fromX ? 1 : m.toX==m.fromX ? 0 : -1);
		int dy = (m.toY > m.fromY ? 1 : m.toY==m.fromY ? 0 : -1);
		
		int powerSquareX = m.toX;
		int powerSquareY = m.toY;
		
		//System.out.println("L POW MOV");
		
		while (true)
		{
			powerSquareX-=dx; powerSquareY-=dy;
			if (powerSquareX==m.fromX && powerSquareY==m.fromY) return;
			
			if (!isEmpty(powerSquareX,powerSquareY))
			{
				Move m2 = new Move();
				m2.fromX = m.fromX; m2.fromY = m.fromY; m2.piece = m.piece;
				m2.promotionPiece = m.promotionPiece;
				m2.toX = (byte)powerSquareX; m2.toY = (byte)powerSquareY;
				EvaluationGlobals.powerMoves.add(m2);
				//System.out.println("POWER MOVE "+m2);
				return;
			}
			
		}
	}
	
	public boolean isPowerMove(Move m)
	{
		for (int k=0; k<EvaluationGlobals.powerMoves.size(); k++) if (EvaluationGlobals.powerMoves.get(k).equals(m)) return true;
		return false;
	}
	
	public void removePowerMove(Move m)
	{
		for (int k=0; k<EvaluationGlobals.powerMoves.size(); k++) if (EvaluationGlobals.powerMoves.get(k).equals(m))
		{
			EvaluationGlobals.powerMoves.remove(k);
			return; 
		}
	}
	
	public String toString()
	{
		return getRepresentation(Chessboard.PAWN);
	}
	
	public String getRepresentation(int piece)
	{
		String result = "";

		
		for (int j=7; j>=0; j--)
		{
			for (int k=0; k<8; k++)
			{
				if (isEmpty(k,j)) result+=" "; else
				if (!mayBeEmpty(k,j) && getFriendlyPiece(k,j)==Chessboard.EMPTY) { if (canContain((byte)k,(byte)j,(byte)piece)) result+="+"; else result+="x";}
				else
				result+=(isFriendlyPiece(k,j)? pieceLetter2(getFriendlyPiece(k,j)) : (canContain((byte)k,(byte)j,(byte)piece)?"*": "-") );
			}
			result+="\n";
		}	
		return result;
	}
	
	
	public static String pieceLetter2(int p)
	{
		switch (p)
		{
			case Chessboard.PAWN: return "P";
			case Chessboard.BISHOP: return "B";
			case Chessboard.ROOK: return "R";
			case Chessboard.KNIGHT: return "N";
			case Chessboard.QUEEN: return "Q";
			case Chessboard.KING: return "K";
		}
		return "";
	}
	
	public Metaposition generateMostLikelyOpponentMove(Move lastMove)
	{
		Metaposition s = evolveAfterOpponentMove(this,-1,-1,Chessboard.NO_CHECK,Chessboard.NO_CHECK,0);
		return s;
	}
	
	public boolean moveOnCheckTrajectory(Move m)
	{
		if (owner.globals.check1 == Chessboard.NO_CHECK) return true;
		
		for (int k=0; k<2; k++)
		{
			int type = (k==0? owner.globals.check1 : owner.globals.check2);
			
			if (type == Chessboard.CHECK_FILE && m.toX!=owner.globals.kingLocationX) return false;
			if (type == Chessboard.CHECK_RANK && m.toY!=owner.globals.kingLocationY) return false;
			if ((type==Chessboard.CHECK_LONG_DIAGONAL || type==Chessboard.CHECK_SHORT_DIAGONAL)
				&& (m.toX+m.toY != owner.globals.kingLocationX+owner.globals.kingLocationY) && (m.toX-m.toY != owner.globals.kingLocationX-owner.globals.kingLocationY)) return false;
			 
			if ((type==Chessboard.CHECK_LONG_DIAGONAL || type==Chessboard.CHECK_SHORT_DIAGONAL))
			{
				//quick dirty hack to determine if a check is compatible with short/long diagonal
				int diagCheckCounter = 0;
				if (type==Chessboard.CHECK_LONG_DIAGONAL) diagCheckCounter++;
				if ((m.toX+m.toY == owner.globals.kingLocationX+owner.globals.kingLocationY)) diagCheckCounter++;
				if ((owner.globals.kingLocationX<4 && owner.globals.kingLocationY<4) || (owner.globals.kingLocationX>=4 && owner.globals.kingLocationY>=4)) diagCheckCounter++;
				if (diagCheckCounter%2 == 1) return false;
			}
			
			if (type==Chessboard.CHECK_KNIGHT)
			{
				int dx = m.toX - owner.globals.kingLocationX;
				int dy = m.toY - owner.globals.kingLocationY;
				int p = dx*dy; 
				if (p!=2 && p!=-2) return false;
			}
		}
		
		return true;
	}

	public boolean moveCompatibleWithChecks(Move m)
	{
		return (m.piece == Chessboard.KING || moveOnCheckTrajectory(m));
	}
	
	public Vector generateMoves(boolean topLevel, Player pl)
	{
		Vector v = new Vector();
		byte piece;
		boolean once,pawnControl,rookControl,capture;
		Move m;
		
		int vecs[][];
		int vecsY[][];
		int movx[];
		int movy[];
		int dx,dy,x,y;
		int limitY = 0;
		
		boolean w = isWhite();
		vecs = (w? EvaluationGlobals.vectors : EvaluationGlobals.vectorsBlack);
		vecsY = (w? EvaluationGlobals.vectorsY : EvaluationGlobals.vectorsBlackY);
		byte tries = getPawnTries();
		
		if (topLevel && owner.globals.check1!=Chessboard.NO_CHECK) computeKingLocation();

		for (byte k=0; k<8; k++)
		{
			owner.globals.pawnMinLocation[k] = 10;
			owner.globals.pawnMaxLocation[k] = -1;
			if (getMinPawns(k)>0)
			{
				for (byte p=1; p<7; p++)
				{
					if (canContain(k,p,(byte)Chessboard.PAWN))
					{
						if (p<owner.globals.pawnMinLocation[k]) owner.globals.pawnMinLocation[k] = p;
						if (p>owner.globals.pawnMaxLocation[k]) owner.globals.pawnMaxLocation[k] = p;
					}
				}
			}
		}
		
		for (byte k=0; k<8; k++)
		{
			for (byte j=0; j<8; j++)
			{
				piece = getFriendlyPiece(k,j);
				if (piece==Chessboard.EMPTY) continue;
				movx = vecs[piece];
				movy = vecsY[piece];
				once = Chessboard.movesOnlyOnce(piece);
				pawnControl = (piece==Chessboard.PAWN && j==getSecondRank());
				for (int dir=0; dir<movx.length; dir++)
				{
					dx = movx[dir]; dy = movy[dir];
					x = k+dx; y = j+dy;
					rookControl = (dx==0 && /*!once &&*/ owner.globals.pawnMaxLocation[x]!=-1); //vertical movement on files with pawns, (rook & queen)
					if (rookControl)
					{
						//Rook control happens when our piece starts its move from
						//outside of the pawn envelope and tries to move all the way
						//through it.
						if (j>=owner.globals.pawnMaxLocation[x] && dy<0) limitY = owner.globals.pawnMinLocation[x]-1;
						else if (j<=owner.globals.pawnMinLocation[x] && dy>0) limitY = owner.globals.pawnMaxLocation[x]+1;
						else rookControl = false; //if the rook is INSIDE the pawn envelope, we cannot
						//rule out movement either way (the pawn might be ahead or behind the rook).
					} 
					while (true)
					{
						//System.out.println("Trying "+x+" "+y+" "+piece);
						if (x<0 || y<0 || x>=8 || y>=8) break;
						if (rookControl && y==limitY) break; //prevent rooks and queens from moving through their own pawns.
						if (getFriendlyPiece(x,y)!=Chessboard.EMPTY) break;
						//System.out.println("Adding "+x+" "+y+" "+piece);

						m = new Move();
						m.piece = piece; m.fromX = k; m.fromY = j; m.toX = (byte)x; m.toY = (byte)y;
						if (piece==Chessboard.PAWN && m.toY == getLastRank()) m.promotionPiece = Chessboard.QUEEN;
						if (piece==Chessboard.PAWN && !mayBeEmpty(x,y)) break; //pawns do not go to occupied squares with their normal movement.
						if (!topLevel || ((pl==null || !pl.isMoveBanned(m)) && moveCompatibleWithChecks(m))) v.add(m);
						if (!mayBeEmpty(x,y)) break;
						if (pawnControl)
						{
							pawnControl=false;
							x+=dx; y+=dy;
							continue; //pawn gets a "second chance"
						}
						if (once) break;
						x+=dx; y+=dy;

					}
				}		
				//pawn tries...
				if (topLevel && tries>0 && piece==Chessboard.PAWN)
				{		
					for (int tr=0; tr<2; tr++)
					{
						byte px = (byte) (tr==0? k-1 : k+1);
						byte py = (byte) (w? j+1 : j-1);
						if (px>=0 && px<8 && py>=0 && py<8 && getFriendlyPiece(px,py)==Chessboard.EMPTY /*&& !isEmpty(px,py)*/)
						{
							Move pt = new Move();
							pt.piece = Chessboard.PAWN; pt.promotionPiece = Chessboard.QUEEN;
							pt.fromX = k; pt.fromY = j; pt.toX = px; pt.toY = py;
							if (pl==null || !pl.isMoveBanned(pt)) v.add(pt);	
						}
					}
				}
			}
		}
		
		int cs = getCastleStatus();
		int rank = (w? 0 : 7);
		if (cs==0 || cs==1)
		{
			if (getFriendlyPiece(0,rank)==Chessboard.ROOK && getFriendlyPiece(1,rank)==Chessboard.EMPTY && getFriendlyPiece(2,rank)==Chessboard.EMPTY && getFriendlyPiece(3,rank)==Chessboard.EMPTY && getFriendlyPiece(4,rank)==Chessboard.KING)
			{
				Move c = new Move();
				c.piece = Chessboard.KING; c.fromY = (byte)rank; c.toY = (byte)rank;
				c.fromX = 4; c.toX = 2; 
				if (pl==null || !pl.isMoveBanned(c)) v.add(c);
			}
		}
		if (cs==0 || cs==2)
		{
			if (getFriendlyPiece(7,rank)==Chessboard.ROOK && getFriendlyPiece(5,rank)==Chessboard.EMPTY && getFriendlyPiece(6,rank)==Chessboard.EMPTY && getFriendlyPiece(4,rank)==Chessboard.KING)
			{
				Move c = new Move();
				c.piece = Chessboard.KING; c.fromY = (byte)rank; c.toY = (byte)rank;
				c.fromX = 4; c.toX = 6; 
				if (pl==null || !pl.isMoveBanned(c)) v.add(c);
				v.add(c);
			}
		}
		
		return v;
	}

	private void clearEnemyKingMatrix()
	{
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				owner.globals.enemyKingMatrix[k][j] = false;
			}
	}
	
	/**
	 * Fills the king matrix assuming there will be no check, so we'll clear everything that
	 * currently falls inside our protection matrix and expand on what is left. If the thing
	 * only contains one square or singleton kings, we are in stalemate danger.
	 *
	 */
	private int fillEnemyKingMatrixAssumingNoCheck(Metaposition start, Metaposition dest, Move m)
	{
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				if (start.canContainEnemyKing(k,j) && owner.globals.protectionMatrix[k][j]<1)
				{
					for (byte h=-1; h<=1; h++)
						for (byte v=-1; v<=1; v++)
						{
							byte x = (byte)(k+h); byte y = (byte)(j+v);
							if (x>=0 && y>=0 && x<8 && y<8 /*&& dest.canContainEnemyKing(x,y)*/ && owner.globals.protectionMatrix[x][y]<1 && dest.mayBeEmpty(x,y)) owner.globals.enemyKingMatrix[k][j] = true;
						}
				}
			}		
			
		int count = 0;
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				if (owner.globals.enemyKingMatrix[k][j]) count++;
			}			
			
		return count;
	}
	
	private int fillEnemyKingMatrixWithPossibleCheck(Metaposition start, Metaposition dest, Move m)
	{
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				if (start.canContainEnemyKing(k,j))
				{
					for (byte h=-1; h<=1; h++)
						for (byte v=-1; v<=1; v++)
						{
							byte x = (byte)(k+h); byte y = (byte)(j+v);
							if (x>=0 && y>=0 && x<8 && y<8 /*&& dest.canContainEnemyKing(x,y)*/ && owner.globals.protectionMatrix[x][y]<1 && dest.mayBeEmpty(x,y)) owner.globals.enemyKingMatrix[k][j] = true;
						}
				}
			}
		int count = 0;
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				if (owner.globals.enemyKingMatrix[k][j]) count++;
			}			
			
		return count;	
	}
	
	/**
	 * We award a bonus that is inversely proportional to the number of squares where the enemy
	 * king might be. This bonus begins when the opponent has less than 7 pieces/pawns left and
	 * gains strength as those are further reduced. When the opponent has 0 pieces left, our
	 * stalemate prevention system kicks in, strongly punishing chessboards with isolated kings.
	 * @return
	 */
	public float computeEnemyKingBonus(Metaposition start, Metaposition dest, Move m)
	{
		if (piecesLeft+pawnsLeft>=7) return 0.0f;
		
		dest.computeProtectionMatrix(false);
		
		float intensity = 1.0f /*- (piecesLeft+pawnsLeft)/6.0f*/;
		//intensity*=intensity;
		
		//fill the enemy king matrix
		int count = 0;
		clearEnemyKingMatrix();
		count = fillEnemyKingMatrixAssumingNoCheck(start,dest,m);
		float locBonus1 = kingLocationBonus();
		
		if (count==0)
		{
			//interesting, the no check assumption is wrong, the king had nowhere to hide.
			//Potential mate ahead!
			if (owner.globals.protectionMatrix[m.toX][m.toY]>0) return 5000.0f-500.0f*dest.getAge(); //could be checkmate
				else return -2000.0f; //feeding your own piece to the king? No way!
		}
		
		if ((count==1 && piecesLeft==0) || (piecesLeft<=2 && singletonKing())) return -2500.0f; //potential stalemate
		
		clearEnemyKingMatrix();
		count = fillEnemyKingMatrixWithPossibleCheck(start,dest,m);
		float locBonus2 = kingLocationBonus();
		
		//choose the lower location bonus...
		float locBonus = (locBonus1<locBonus2? locBonus1 : locBonus2);
			
		//check if the move was safe in the first place
		int neighborsWithKing = 0;
		for (byte k=-1; k<=1; k++)
			for (byte j=-1; j<=1; j++)
			{
				byte x = (byte)(m.toX+k);
				byte y = (byte)(m.toY+j);
				if (x<0 || y<0 || x>=8 || y>=8) continue;
				if ((k!=0 || j!=0) && start.canContainEnemyKing(x,y)) neighborsWithKing++;
			}
		if (piecesLeft==0 && pawnsLeft==0 && m.piece!=Chessboard.KING && owner.globals.protectionMatrix[m.toX][m.toY]<1 && neighborsWithKing>0)
			//intensity*= -5.0f-1.0*neighborsWithKing/(count!=0? count : 1);
			return -500.0f;

		//return 35.0f*intensity/count;
		return /*kingLocationBonus()*/ locBonus *intensity * owner.globals.kingLocationBonusWeight;
	}
	
	public boolean singletonKing()
	{
		int offsetsx[] = Chessboard.queenOffsetX;
		int offsetsy[] = Chessboard.queenOffsetY;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (owner.globals.enemyKingMatrix[k][j])
				{
					boolean safe = false;
					for (int l=0; l<8; l++)
					{
						int x = k+offsetsx[l];
						int y = j+offsetsy[l];
						if (x>=0 && y>=0 && y<8 && x<8 && owner.globals.enemyKingMatrix[x][y]) safe = true;
					}
					if (!safe) return true;
				}
			}
			
		return false;
	}
	
	private float kingLocationBonus()
	{
		float result = 0.0f;
		float code = 0.0f;
		int count = 0;
		int top, left, bottom, right;
		
		top = left = 7; right = bottom = 0;
		
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				if (owner.globals.enemyKingMatrix[k][j])
				{
					count++;
					if (k<left) left = k; if (k>right) right = k;
					if (j<top) top = j; if (j>bottom) bottom = j;
					code += EvaluationGlobals.kingLocationModifiers[k][j];
				}
			}
		
		if (left==right || top==bottom) code*=0.85;
		code *= 0.5;
		if (code>60.0f) code = 60.0f;
		result = (60.0f - code) /* / count */;
		return result*0.05f;
	}
	
	public void computeKingLocation()
	{
		for (byte k=0; k<8; k++)
		for (byte j=0; j<8; j++)
			if (getFriendlyPiece(k,j)==Chessboard.KING) 
			{
				owner.globals.kingLocationX = k; owner.globals.kingLocationY = j; return;
			}
	}
	
	public boolean mayBeProtectingKing(int x, int y, Move m)
	{
		int dirX, dirY;
		
		if (x==owner.globals.kingLocationX)
		{
			if (m.toX==m.fromX) return false;
			dirX = 0; dirY = (y>owner.globals.kingLocationY? 1 : -1);
		} else if (y==owner.globals.kingLocationY)
		{
			if (m.toY==m.fromY) return false;
			dirY = 0; dirX = (x>owner.globals.kingLocationX? 1 : -1);
		} else if (x+y==owner.globals.kingLocationX+owner.globals.kingLocationY) //top left diagonal, deltas have diff signs
		{
			if (m.toX+m.toY==x+y) return false;
			dirX = (x>owner.globals.kingLocationX? 1 : -1);
			dirY = -dirX;
		} else if (x-y==owner.globals.kingLocationX-owner.globals.kingLocationY)
		{
			if (m.toX-m.toY==x-y) return false;
			dirX = (x>owner.globals.kingLocationX? 1 : -1);
			dirY = dirX;
		} else return false;
		
		int tx, ty;
		boolean confirmed = false;
		
		//first, make sure you can actually protect the king...
		tx = x - dirX; ty = y - dirY;
		while (true)
		{
			if (tx<0 || ty<0 || tx>=8 || ty>=8) break;
			if (getFriendlyPiece(tx,ty)==Chessboard.KING)
			{
				confirmed = true; break;
			}
			if (!mayBeEmpty(tx,ty)) break;
			tx -= dirX; ty -= dirY;
		}
		if (!confirmed) return false;
		
		//now make sure there are enemy piece traces in the opposite direction...
		tx = x + dirX; ty = y + dirY;
		while (true)
		{
			if (tx<0 || ty<0 || tx>=8 || ty>=8) return false;
			if (getFriendlyPiece(tx,ty)!=Chessboard.EMPTY) return false;
			if (canContain((byte)tx,(byte)ty,(byte)Chessboard.QUEEN)) return true;
			if (canContain((byte)tx,(byte)ty,(byte)Chessboard.ROOK) && (dirX==0 || dirY==0)) return true;
			if (canContain((byte)tx,(byte)ty,(byte)Chessboard.BISHOP) && (dirX!=0 && dirY!=0)) return true;		
			tx += dirX; ty += dirY;
		}
	}
	
	private EnemyPieceTrack getEnemyTrack(int k)
	{
		//if (k<0 || k>=enemyTracks.size()) return null;
		//return (EnemyPieceTrack)(enemyTracks.get(k));
		return null;
	}
	
	private void addEnemyTrack(Move m)
	{
		//check if there are other tracks that are either subsets or supersets of this...
		/*EnemyPieceTrack ept = new EnemyPieceTrack(m);
		
		for (int k=0; k<enemyTracks.size(); k++)
		{
			EnemyPieceTrack test = getEnemyTrack(k);
			if (ept.isSupersetOf(test)) return; //we already have something better in the list.
			if (ept.isSubsetOf(test))
			{
				//replace the old track with the new one.
				enemyTracks.setElementAt(ept,k);
				computeBonusMatrix();
				return;
			}
		}
		enemyTracks.add(ept);
		computeBonusMatrix();*/
	}
	
	private void computeBonusMatrix()
	{
		/*clearBonusMatrix();
			
		for (int k=0; k<enemyTracks.size(); k++)
		{
			EnemyPieceTrack ept = getEnemyTrack(k);
			int num = ept.getSquareNumber();
			for (int j=0; j<num; j++)
			{
				EvaluationGlobals.bonusMatrix[ept.getSquareX(j)][ept.getSquareY(j)] += 1.0f / num;
			}
		}*/
	}
	
	private float calculateMoveBonus(Move m, int age)
	{
		float result = 0.0f;
		float coefficient = 2.5f;
		
		for (int k=1; k<age; k++) coefficient*=0.7f;
		
		if (m.piece!=Chessboard.KNIGHT)
		{
			int dx = (m.toX>m.fromX? 1 : m.toX==m.fromX? 0 : -1);
			int dy = (m.toY>m.fromY? 1 : m.toY==m.fromY? 0 : -1);
			
			int x = m.fromX+dx; int y = m.fromY+dy;
			while (true)
			{
				result+=owner.globals.bonusMatrix[x][y];
				if (x==m.toX && y==m.toY) break;
				x+=dx; y+=dy;
			}
			
		} else result+=owner.globals.bonusMatrix[m.toX][m.toY];
		
		return result*coefficient;
	}
	
	public void computeProtectionMatrix(boolean soft)
	{
		int vecs[][], vecsY[][];
		int movx[], movy[];
		int x,y,dx,dy;
		int limitY = 0;
		boolean once;
		boolean rookControl;
		
		if (isWhite())
		{
			EvaluationGlobals.init();
			vecs = EvaluationGlobals.capVectors; vecsY = EvaluationGlobals.capVectorsY;
		} else
		{
			EvaluationGlobals.init();
			vecs = EvaluationGlobals.capVectorsBlack; vecsY = EvaluationGlobals.capVectorsBlackY;
		}
		
		owner.globals.opponentControlledSquares = 0;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++) 
			{
				owner.globals.protectionMatrix[k][j] = 0;
				owner.globals.leastProtectingPiece[k][j] = 10000;
			}  
			
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int piece = getFriendlyPiece(k,j);	
				if (piece!=Chessboard.EMPTY)
				{
					once = Chessboard.movesOnlyOnce(piece);
					movx = vecs[piece];
					movy = vecsY[piece];
					once = Chessboard.movesOnlyOnce(piece);
					for (int dir=0; dir<movx.length; dir++)
					{
						dx = movx[dir]; dy = movy[dir];
						x = k+dx; y = j+dy;
						rookControl = (dx==0 && !once && owner.globals.pawnMaxLocation[x]!=-1); //vertical movement on files with pawns, (rook & queen)
						if (rookControl)
						{
							//Rook control happens when our piece starts its move from
							//outside of the pawn envelope and tries to move all the way
							//through it.
							if (j>=owner.globals.pawnMaxLocation[x] && dy<0) limitY = owner.globals.pawnMinLocation[x]-1;
							else if (j<=owner.globals.pawnMinLocation[x] && dy>0) limitY = owner.globals.pawnMaxLocation[x]+1;
							else rookControl = false; //if the rook is INSIDE the pawn envelope, we cannot
							//rule out movement either way (the pawn might be ahead or behind the rook).
						}
						while (true)
						{
							//System.out.println("Trying "+x+" "+y+" "+piece);
							if (x<0 || y<0 || x>=8 || y>=8) break;
							if (rookControl && y==limitY) break; //prevent rooks and queens from moving through their own pawns.
							
							owner.globals.protectionMatrix[x][y]++;
							if (piece==Chessboard.KING || piece < owner.globals.leastProtectingPiece[x][y])
								owner.globals.leastProtectingPiece[x][y] = piece;
							
							if (soft)
							{
								if (!mayBeEmpty(x,y)) break;
							}
							else
							{ 
								if (!isEmpty(x,y) && !canOnlyContainKing((byte)x,(byte)y)) break; 
							} 
							if (once) break;
							x+=dx; y+=dy;

						}
					}	
				} else
				{
					if (!isEmpty(k,j))
					{
						owner.globals.opponentControlledSquares++;
					}
				}
			}
	}
	
	private void doAge()
	{
		for (int k=63; k>=0; k--)
		{
			if ((squares[k] & 0x80) == 0) squares[k]++;
		}
	}
	
	private void doAgeWithCheck()
	{
		for (int k=63; k>=0; k--)
		{
			if ((squares[k] & 0x80) == 0 && squares[k]!=255) squares[k]++;
		}
	}
	
	public String getProtectionMatrix()
	{
		String result = "";

		
		for (int j=7; j>=0; j--)
		{
			for (int k=0; k<8; k++)
			{
				result+=owner.globals.protectionMatrix[k][j];
			}
			result+="\n";
		}	
		return result;
	}
	
	public String getAndPrintProtectionMatrix(boolean v)
	{
		computeProtectionMatrix(v);
		return getProtectionMatrix();
	}
	
	public int kingProximityBonus(int kx, int ky)
	{
		int bestProximity = 1000;
		
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				if ((kx!=k || ky!=j) && isFriendlyPiece(k,j))
				{
					int prx = (k>kx? k-kx : kx-k);
					int pry = (j>ky? j-ky : ky-j);
					int pr = (prx>pry? prx : pry);
					if (pr<bestProximity) bestProximity = pr;
				}
			}
			
		if (bestProximity==1000) return 0;
		else return (7-bestProximity);
	}
	
	/**
	 * Safety ranges from 0 (we are definitely going to lose it) to 1 (no problemo).
	 * Requires the protection matrix to have been calculated beforehand.
	 * @param x
	 * @param y
	 * @param wasMoved
	 * @return
	 */
	public float pieceSafety(int x, int y, boolean wasMoved,boolean capture)
	{
		float safety;
		float dangerRating = dangerRating(x,y);
		float minDanger = dangerRating*0.33f*(piecesLeft+pawnsLeft+1.0f)/16.0f;
		
		//The protection matrix evaluates how many pieces are protecting any squares,
		//and which is the least-valued piece that does.
		int protection = owner.globals.protectionMatrix[x][y];
		boolean protectedByLessImportantPiece = owner.globals.leastProtectingPiece[x][y] <= getFriendlyPiece(x,y);
		
		if (dangerRating == 0.0f) return 1.0f;
		
		if (wasMoved) dangerRating *= 1.5;
		
		if (dangerRating>0.0f && protection==0) if (dangerRating<0.25f) dangerRating = 0.25f;
		
		//if (owner.experimental && dangerRating==2.0f) return 0.0f;
		
		if (getFriendlyPiece(x,y)!=Chessboard.KING) for (int times=0; times<protection; times++) dangerRating *= 
			EvaluationGlobals.weights.weights[SimpleWeightSet.W_RISK_MODIFIER_I]*/*currentDangerModifier*/ owner.globals.currentProtectionModifiers[owner.globals.leastProtectingPiece[x][y]];
		if (protectedByLessImportantPiece) dangerRating *= 0.8f;
		if (dangerRating>1.0f) dangerRating = 1.0f;
		if (dangerRating<minDanger) dangerRating = minDanger;
		safety = 1.0f - dangerRating;
		if (capture & dangerRating>0.0f) safety = 0.0f;
		//safety*=safety;
		if (safety<0.0f) safety = 0.0f;
		
		return safety;
	}
	
	public float dangerRating(int x, int y)
	{
		if (owner.globals.opponentControlledSquares == 0) return 0.0f;
		
		float result = 0.0f;
		float pawnBonus = 0.0f;
		int index = 8*x + y;
		int movPiece = getFriendlyPiece(x,y);
		int squareCount=0;
		int pawnDelta = (isWhite()? 1 : -1);
		boolean added;
		
		int deltax[] = Chessboard.queenOffsetX;
		int deltay[] = Chessboard.queenOffsetY;
		
		for (int k=0; k<deltax.length; k++)
		{
			int dx = deltax[k]; int dy = deltay[k];
			int tx = x+dx; int ty = y+dy;
			for (int tries=0; true; tries++)
			{
				if (tx<0 || ty<0 || tx>=8 || ty>=8) break;
				int pc = getFriendlyPiece(tx,ty);
				if (pc!=Chessboard.EMPTY) break;
				int index2 = tx*8 + ty;
				byte value = squares[index2];
				added = false;
				if ((value & (1<<Chessboard.QUEEN)) != 0) {/*squareCount++;*/ squareCount+=ageMatrix[index2]; added = true;} 
				else
				if (((value & (1<<Chessboard.ROOK)) != 0) && k<4) {/*squareCount++;*/ squareCount+=ageMatrix[index2]; added = true;}
				else
				if (((value & (1<<Chessboard.BISHOP)) != 0) && k>=4) {/*squareCount++;*/ squareCount+=ageMatrix[index2]; added = true;} 
				else
				if (((value & (1<<Chessboard.PAWN)) != 0) && k>=4 && tries==0 && ty==pawnDelta) 
				{
					/*squareCount++;*/
					//if (owner.experimental) if (getMinPawns((byte)tx)>0 && pc!=Chessboard.PAWN) return 2.0f;
					
					squareCount+=ageMatrix[index2]; added = true;
					pawnBonus+=2.5f*getMaxPawns((byte)tx)/owner.globals.pawnSquaresInFiles[tx];
				}
				else
				if (((value & (1<<Chessboard.KING)) != 0) && tries==0 && movPiece!=Chessboard.KING) {/*squareCount++;*/  squareCount+=ageMatrix[index2]; added = true;} 
				
				if ((value & (1<<Chessboard.EMPTY)) == 0)
				{
					if (added) return 1.0f;
					else break;
				}
				
				tx+=dx; ty+=dy;
			}
		}
		
		result = 1.0f*squareCount/totalAge + pawnBonus;
		//result = 1.0f*squareCount/opponentControlledSquares + pawnBonus;
		//result *= currentDangerModifier;
		result*=3.0f*(pawnsLeft+piecesLeft+1.0f)/16.0f;
		if (result > 1.0f) result = 1.0f;
		if (result < 0.0f) result = 0.0f;
		
		return result;
	}
	
	/*public void minimaxEvaluate(Move m, int depth,EvaluationData outData, boolean topLevel, float bonus)
	{	
		if (depth<=1)
		{
			outData.positionValue = outData.minValue = currentEvaluationFunction.evaluate(this,generateMostLikelyEvolution(m),m)+bonus;
			return;
		}

		//sort it with static evaluation, alpha-beta, killer moves blah blah...
		float value, minvalue;
		float statvalue = 0;
		value = -100000000;
		Move bestMove = null;
		Metaposition evolve=null; 
		Metaposition evolve2=null; 
		if (!topLevel)
		{
			evolve = generateMostLikelyEvolution(m);
			evolve2 = evolve.generateMostLikelyOpponentMove(m);
		} else
		{
			evolve2 = this;
		}
		
		statvalue = currentEvaluationFunction.evaluate(evolve,evolve2,m);
		
		if (m!=null && evolve!=null)
		{
			if (value<=-200.0f || value>=400.0f)
			{
				//this chessboard is rotten, no matter how good it may evolve. The game may not last that long.
				outData.positionValue = outData.minValue = statvalue;
				if (topLevel) minimaxBestMove = m;
				return;
			}
		}
		
		Vector v = evolve2.generateMoves(topLevel);
		
		for (int k=0; k<v.size(); k++)
		{
			float bonusAdd = 0.0f;
			Move tst = (Move)v.get(k);
			//if (evolve!=null && ((tst.piece==Chessboard.PAWN && tst.toX!=tst.fromX)
			//	|| (tst.piece!=Chessboard.PAWN && !evolve.mayBeEmpty(tst.toX,tst.toY)))) bonusAdd=weights.weights[SimpleWeightSet.W_PAWN_TRY_MODIFIER];
			
			evolve2.minimaxEvaluate(tst,depth-1,outData,false,bonus+bonusAdd);
			if (outData.positionValue>value)
			{
				bestMove = (Move)v.get(k);
				value = outData.positionValue;
			}
		}
		
		outData.positionValue = (topLevel? value : statvalue*0.9f + value*0.1f);
		if (topLevel) minimaxBestMove = bestMove;
		
	}*/
	
	/**
	 * While the fast game tree works decently most of the time, in certain situations
	 * the situation can change so suddenly from one move to the next, especially during
	 * the endgame, that it is best to evaluate a chessboard before expanding it. This
	 * is essential, for example, in preventing nasty stalemates.
	 * @return
	 */
	private boolean shouldBeStaticallyEvaluated()
	{
		//if (piecesLeft+pawnsLeft<3) return true;
		
		return false;
	}
	
	public void updateTotalAge()
	{
		int total = 0;
		
		for (int k=0; k<64; k++)
			total += ageMatrix[k];
			
		this.totalAge = total;
	}
	
	/**
	 * Danger ratings are magnified by this amount, depending on the situation
	 * @return
	 */
	public float dangerModifier()
	{
		int delta = owner.globals.totalMaterial - pawnsLeft - piecesLeft;
		float modifier = 0.0f;
		if (delta>15) modifier = 0.5f;
		if (delta<-15) modifier = 10.0f;
		//return dangerModifiers[delta+15];
		return 1.0f*(piecesLeft+pawnsLeft)/15.0f;
	}
	
	public void calculateFriendlyMaterial()
	{
		owner.globals.pawns = owner.globals.knights = owner.globals.bishops = owner.globals.rooks = owner.globals.queens = owner.globals.materialDelta = 0;
		for (int k=0; k<8; k++) owner.globals.pawnSquaresInFiles[k] = 0;
		
		for (byte k=0; k<8; k++)
			for (byte j=0; j<8; j++)
			{
				byte p = getFriendlyPiece(k,j);
				switch (p)
				{
					case Chessboard.PAWN: owner.globals.pawns++; break;
					case Chessboard.KNIGHT: owner.globals.knights++; break;
					case Chessboard.BISHOP: owner.globals.bishops++; break;
					case Chessboard.ROOK: owner.globals.rooks++; break;
					case Chessboard.QUEEN: owner.globals.queens++; break;
				}
			}
			
		owner.globals.totalMaterial = owner.globals.pawns + owner.globals.knights + owner.globals.bishops + owner.globals.rooks + owner.globals.queens;
	}
	
	public void calculateKingLocationBonus()
	{
		int left = piecesLeft+pawnsLeft;
		if (left>=7 || left>owner.globals.totalMaterial) 
		{
			//don't seek the king if there are too many enemy pieces or you are losing
			owner.globals.kingLocationBonusWeight = 0.0f;
			return;
		} 
		
		if (owner.globals.check1!=Chessboard.NO_CHECK || owner.globals.capturex>=0)
		{
			//when responding to umpire message, we disable the king-seeking algorithm
			owner.globals.kingLocationBonusWeight = 0.0f;
			return;
		}
		
		switch (piecesLeft+pawnsLeft)
		{
			case 0: owner.globals.kingLocationBonusWeight = 1.0f; break;
			case 1: owner.globals.kingLocationBonusWeight = 0.2f; break;
			case 2: owner.globals.kingLocationBonusWeight = 0.1f; break;
			case 3: owner.globals.kingLocationBonusWeight = 0.05f; break;
			case 4: owner.globals.kingLocationBonusWeight = 0.05f; break;
			case 5: owner.globals.kingLocationBonusWeight = 0.02f; break;
		}
	}
	
	public void calculateRiskWeight()
	{
		owner.globals.maximaxPositionWeight = 0.5f + (owner.globals.totalMaterial-piecesLeft-pawnsLeft)*0.03f;
		if (owner.globals.maximaxPositionWeight>0.66f) owner.globals.maximaxPositionWeight = 0.66f;
		if (owner.globals.maximaxPositionWeight<0.33f) owner.globals.maximaxPositionWeight = 0.33f;
		
		owner.globals.maximaxPositionWeight -= 0.3f;
		
		owner.globals.oneMinusMaximaxPositionWeight = 1.0f - owner.globals.maximaxPositionWeight; //we compute this only once!
	}
	public Player getOwner() {
		return owner;
	}
	public void setOwner(Player owner) {
		this.owner = owner;
	}
	public int canKeep(Move m) {
		// TODO Auto-generated method stub
		
		int offset = (isWhite()? 1 : -1);
		int start = (isWhite()? 2 : 0);
		int end = (isWhite()? 8 : 6);
		
		//sending a piece where it can be captured by a pawn is a NO
		//for (int k=0; k<8; k++)
		//	for (int j=start; j<end; j++)
		for (int k=m.toX; k<=m.toX; k++)
			for (int j=m.toY; j<=m.toY; j++)
			{
				int pc = getFriendlyPiece(k, j);
				if (pc!=Chessboard.EMPTY && pc!=Chessboard.PAWN && pc!=Chessboard.KING)
				{
					int next = j+offset;
					if (next<0 || next>7) continue;
					if (k>0 && canContain((byte)(k-1), (byte)(next), (byte)Chessboard.PAWN) && getMinPawns((byte)(k-1))>0) 
					{
						return Discardable.NO;
					}
					if (k<7 && canContain((byte)(k+1), (byte)(next), (byte)Chessboard.PAWN) && getMinPawns((byte)(k+1))>0) 
					{
						return Discardable.NO;
					}
				}
			}
		
		//computeProtectionMatrix(true);
		
		//if (m.piece!=Chessboard.KING && owner.globals.protectionMatrix[m.toX][m.toY]<1) return Discardable.MAYBE;
		
		
		return Discardable.YES;
	}
	
	/**
	 * Updates a square in the event of receiving information on what hides there.
	 * @param x
	 * @param y
	 * @param what
	 */
	public void revealSquare(int x, int y, int what)
	{
		if (getFriendlyPiece(x, y)!=Chessboard.EMPTY) return;
		
		squares[squareIndex((byte)x, (byte)y)] = 0;
		setPiecePossible(x, y, what);
		if (what==Chessboard.EMPTY) setAge(x, y, 0);
		if (what==Chessboard.KING)
		{
			for (int a=0; a<8; a++)
				for (int b=0; b<8; b++)
					if (getFriendlyPiece(a, b)==Chessboard.EMPTY && (x!=a || y!=b)) setPieceImpossible(a, b, Chessboard.KING);
		}
		
	}
	
	public boolean isSubsetOf(Metaposition m)
	{
		if (piecesLeft!=m.piecesLeft) return false;
		if (pawnsLeft!=m.pawnsLeft) return false;
		
		for (int k=0; k<64; k++)
		{
			if (squares[k]==m.squares[k]) continue;
			if ((squares[k] & 0x80)!=(m.squares[k] & 0x80)) return false;
			if ((m.squares[k] & 0x80)!=0) return false;
			
			//subset can be restrictive, but cannot have pieces that superset does not
			//read: if the bits are different and this board has the 1, it can't be a subset
			if (((squares[k]^m.squares[k]) & squares[k]) != 0) return false;
			
		}
		
		return true;
	}
	
	public boolean isSupersetOf(Metaposition m)
	{
		return (m.isSubsetOf(this));
	}
	
	public boolean isEmpty()
	{
		for (int k=0; k<64; k++) if (squares[k]==0) return true;
		return false;
	}
	
	public static Metaposition union(Metaposition a, Metaposition b)
	{
		Metaposition out = getChessboard(a);
		
		for (int k=0; k<64; k++) 
			{
				out.squares[k] = (byte)(a.squares[k] | b.squares[k]);
				out.ageMatrix[k] = (a.ageMatrix[k]>b.ageMatrix[k]? a.ageMatrix[k] : b.ageMatrix[k]);
			}
		
		return out;
	}
	
	public static Metaposition evolveAfterMoveWithChecks(Metaposition root, Move m,
			int cap, int capx, int capy, int check1, int check2, int tries)
		{
			Metaposition sc = getChessboard(root);
			
			byte start = squareIndex(m.fromX,m.fromY);
			byte end = squareIndex(m.toX,m.toY);
			
			if (cap==Chessboard.NO_CAPTURE && !root.mayBeEmpty(m.toX, m.toY)) return null;
			if (cap==Chessboard.CAPTURE_PAWN && !root.canContainEnemyPawn(capx, capy)) return null;
			if (cap==Chessboard.CAPTURE_PIECE && 
					!root.canContain((byte)capx, (byte)capy, (byte)Chessboard.KNIGHT) &&
					!root.canContain((byte)capx, (byte)capy, (byte)Chessboard.BISHOP) &&
					!root.canContain((byte)capx, (byte)capy, (byte)Chessboard.ROOK) &&
					!root.canContain((byte)capx, (byte)capy, (byte)Chessboard.QUEEN)) return null;
			
			sc.squares[end] = sc.squares[start];
			sc.setEmpty(m.fromX,m.fromY);
			
			int index = m.fromX*8 + m.fromY;
			sc.totalAge -= sc.ageMatrix[index];
			sc.ageMatrix[index] = 0;
			
			sc.setFriendlyPiece(m.toX,m.toY,(m.piece==Chessboard.PAWN &&
				m.toY==sc.getLastRank())? m.promotionPiece : m.piece);
			
			if (m.piece==Chessboard.KING)
			{
				sc.kingMoved();
				if (m.fromY == m.toY && m.fromY == sc.getFirstRank())
				{
					if (m.toX==m.fromX-2)
					{
						//left castling
						sc.setEmpty((byte)0,sc.getFirstRank());
						sc.setFriendlyPiece((byte)(m.toX+1),sc.getFirstRank(),(byte)Chessboard.ROOK);
					}
					if (m.toX==m.fromX+2)
					{
						//right castling
						sc.setEmpty((byte)7,sc.getFirstRank());
						sc.setFriendlyPiece((byte)(m.toX-1),sc.getFirstRank(),(byte)Chessboard.ROOK);
					}
				}
			}
			
			if (m.piece==Chessboard.ROOK)
			{
				if (m.fromX==0 && m.fromY==sc.getFirstRank()) sc.leftRookMoved();
				if (m.fromX==7 && m.fromY==sc.getFirstRank()) sc.rightRookMoved();
			}
			
			if (m.piece!=Chessboard.KNIGHT && m.piece!=Chessboard.KING)
			{
				byte dx = (byte)(m.toX>m.fromX? 1 : (m.toX==m.fromX? 0 : -1));
				byte dy = (byte)(m.toY>m.fromY? 1 : (m.toY==m.fromY? 0 : -1));
				byte x = (byte)(m.fromX+dx);
				byte y = (byte)(m.fromY+dy);
				while (x<8 && y<8 && x>=0 && y>=0)
				{
					index = x*8 + y;
					sc.totalAge -= sc.ageMatrix[index];
					sc.ageMatrix[index] = 0;
					if ((x==m.toX && y==m.toY)) break;
					sc.setEmpty(x,y);
					x+=dx; y+=dy;
				}
			} else
			{
				index = m.toX*8 + m.toY;
				sc.totalAge -= sc.ageMatrix[index];
				sc.ageMatrix[index] = 0;
			}
			
			if (cap==Chessboard.CAPTURE_PAWN || (cap==Chessboard.CAPTURE_PIECE && sc.piecesLeft==0))
			{ 
				sc.pawnsLeft--;
				if (cap==Chessboard.CAPTURE_PAWN)
				{
					sc.setMaxPawns(m.toX,(byte)(sc.getMaxPawns(m.toX)-1));
					sc.setMinPawns(m.toX,(byte)(sc.getMinPawns(m.toX)-1));

				}
				if (sc.pawnsLeft==0) 
				{
					sc.disposeOfPawns();
					for (byte x=0; x<8; x++) 
					{ 
						sc.setMaxPawns(x,(byte)0); sc.setMinPawns(x,(byte)0); 
					}
				} //delete them all
			}
			if (cap==Chessboard.CAPTURE_PIECE && sc.piecesLeft>0)
			{
				sc.piecesLeft--;
				//if (sc.piecesLeft==0) sc.disposeOfPieces(); //no more pieces!
			}
			
			if (cap!=Chessboard.NO_CAPTURE && (sc.piecesLeft+sc.pawnsLeft)==0)
			{
				sc.disposeOfPawns();
				sc.disposeOfPieces();
			}
			
			sc.setOpponentPawnTries((byte)tries);
			
			sc.updateEnemyKing(m,check1,check2,cap);
			
			return sc;
		}
	
	public Vector<Move> possibleOpponentMoves(int capx, int capy, int c1, int c2, int tries, int sx, int sy, int type)
	{
		Vector<Move> out = new Vector<Move>();
		
			int movx[];
			int movy[];
			boolean w = isWhite();
			boolean once;
			boolean couldMove,pawnControl,rookControl,kingControl;
			boolean capture = (capx>=0);
			int dx, dy, x, y;
			int vecs[][] = null;
			int vecsY[][] = null;
			int limitY = 0;
			
			computeProtectionMatrix(false);
			
			//System.out.println("!!! "+capx+" "+capy);
			
			if (capture) 
			{
				//squares[capx*8 + capy] = 0; //applicable pieces will be filled; EMPTY will not.
				
				vecs = (!w? EvaluationGlobals.capVectors : EvaluationGlobals.capVectorsBlack);
				vecsY = (!w? EvaluationGlobals.capVectorsY : EvaluationGlobals.capVectorsBlackY);
			} else 
			{
				vecs = (!w? EvaluationGlobals.vectors : EvaluationGlobals.vectorsBlack);
				vecsY = (!w? EvaluationGlobals.vectorsY : EvaluationGlobals.vectorsBlackY);
			} 
			
			//calculate where pawns are...
			for (byte k=0; k<8; k++)
			{
				owner.globals.pawnMinLocation[k] = 10;
				owner.globals.pawnMaxLocation[k] = -1;
				if (getMinPawns(k)>0)
				{
					for (byte p=1; p<7; p++)
					{
						if (canContain(k,p,(byte)Chessboard.PAWN))
						{
							if (p<owner.globals.pawnMinLocation[k]) owner.globals.pawnMinLocation[k] = p;
							if (p>owner.globals.pawnMaxLocation[k]) owner.globals.pawnMaxLocation[k] = p;
						}
					}
				}
			}
			
			boolean leftPawnCandidate = false;
			boolean rightPawnCandidate = false; //pawn captures...
			
			byte startingPiece = 0;
			//startingPiece is used to initialize the for cycle of enemy pieces in each square.
			//It normally starts at 0, meaning that every piece is evolved, from pawns (0)
			//onwards. However, if a capture takes place and the opponent had no pawn tries on
			//his turn, we do not consider pawns.
			if (capture && getOpponentPawnTries()<1) startingPiece = 1;
			
			for (int k=sx; k<=sx; k++)
				for (int j=sy; j<=sy; j++)
				{
					if (!isFriendlyPiece(k,j))
					{
						couldMove = false;
						for (byte piece=(byte)type; piece<=(byte)type; piece++)
						{
							if (canContain((byte)k,(byte)j,piece))
							{
								movx = vecs[piece];
								movy = vecsY[piece];
								once = Chessboard.movesOnlyOnce(piece);
								pawnControl = (piece==Chessboard.PAWN && j==getSecondLastRank() && !capture);
								kingControl = (piece==Chessboard.KING);
								for (int dir=0; dir<movx.length; dir++)
								{
									dx = movx[dir]; dy = movy[dir];
									x = k+dx; y = j+dy;
									rookControl = (dx==0 && !once && owner.globals.pawnMaxLocation[x]!=-1); //vertical movement on files with pawns, (rook & queen)
									if (rookControl)
									{
										//Rook control happens when our piece starts its move from
										//outside of the pawn envelope and tries to move all the way
										//through it.
										if (j>=owner.globals.pawnMaxLocation[x] && dy<0) limitY = owner.globals.pawnMinLocation[x]-1;
										else if (j<=owner.globals.pawnMinLocation[x] && dy>0) limitY = owner.globals.pawnMaxLocation[x]+1;
										else rookControl = false; //if the rook is INSIDE the pawn envelope, we cannot
										//rule out movement either way (the pawn might be ahead or behind the rook).
									}
									while (true)
									{
										//System.out.println("Trying "+x+" "+y+" "+piece);
										if (x<0 || y<0 || x>=8 || y>=8) break;
										if (rookControl && y==limitY) break; //prevent rooks and queens from moving through their own pawns.
										//if (!mayBeEmpty(x,y) && (getFriendlyPiece(x,y)==Chessboard.EMPTY || !capture)) break;
										if (!mayBeEmpty(x,y) && (x!=capx || y!=capy)) break;
										if (kingControl && (owner.globals.protectionMatrix[x][y]>0) && (x!=capx || y!=capy)) break; //don't go over protected squares unless it's a capture.
										//if (kingControl && capture) { System.out.println("KING: "+x+" "+y); }
										if (capture) //only legal capture moves
										{
											if (x!=capx || y!=capy)
											{
												if (once) break;
												x+=dx; y+=dy;
												continue;
											} else
											{
												if (piece==0) //pawn capture
												{
													if (k<x) leftPawnCandidate = true;
													else rightPawnCandidate = true;
												}
											}
										}
										couldMove = true;
										//System.out.println("Adding "+x+" "+y+" "+piece);
										//owner.globals.testbedBoard.setPiecePossible((byte)x,(byte)y,piece);
										Move found = new Move();
										found.fromX = (byte)sx; found.fromY = (byte)sy;
										found.toX = (byte)x; found.toY = (byte)y;
										found.piece = (byte)type;
										
										out.add(found);
										
										if (pawnControl)
										{
											pawnControl=false;
											x+=dx; y+=dy;
											continue; //pawn gets a "second chance"
										}
										if (!mayBeEmpty(x,y)) break;
										if (once) break;
										x+=dx; y+=dy;

									}
									
									
								}
								//if (couldMove) owner.globals.testbedBoard.setPiecePossible((byte)k,(byte)j,(byte)Chessboard.EMPTY);
							
							}
						}
					}
				}
			
		return out;
	}
	
	//MCState implementation
	
	public float chanceOfMessage(int set, Move m, int msg, int submsg) {
		// TODO Auto-generated method stub
		return 1.0f;
	}
	public float eval(int set) {
		// TODO Auto-generated method stub
		Darkboard o = (Darkboard)owner;
		return o.evaluate(this, null, this, null);
	}
	public float eval(int set, Move m, MCState after) {
		// TODO Auto-generated method stub
		Darkboard o = (Darkboard)owner;
		return o.evaluate(this, m, (Metaposition)after, null);
	}
	public Move[] getMoves(int set) {
		// TODO Auto-generated method stub
		Vector<Move> v = this.generateMoves(true, this.owner);
		Move[] out = new Move[v.size()];
		v.copyInto(out);
		return out;
	}
	public MCState[] getStates(int set, Move[] moves) {
		// TODO Auto-generated method stub
		Metaposition[] mp = new Metaposition[moves.length];
		for (int k=0; k<moves.length; k++)
		{
			mp[k] = evolveAfterMove(this, moves[k], Chessboard.NO_CAPTURE, -1, -1, 
					Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0);
			
			mp[k] = mp[k].generateMostLikelyOpponentMove(moves[k]);
		}
		return mp;
	}
	public MCState getState(int set, Move move) {
		// TODO Auto-generated method stub
		Metaposition mp = evolveAfterMove(this, move, Chessboard.NO_CAPTURE, -1, -1, 
				Chessboard.NO_CHECK, Chessboard.NO_CHECK, 0);
		mp = mp.generateMostLikelyOpponentMove(move);
		return mp;
	}
	public float getHeuristicBias(int set, Move m) {
		// TODO Auto-generated method stub
		return 0.0f;
	}
	public boolean isBroken() 
	{
		return false;
	}
}
