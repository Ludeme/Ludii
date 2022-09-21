package umpire.local;

import java.util.StringTokenizer;


/**
 * FEN (Forsythe-Edwards Notation) is a compact notation for describing
 * chess positions using a character string. For example, the starting
 * position in chess is
 * 
 * rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
 * 
 * This class extracts data from FEN strings.
 * @author Nikola Novarlic
 *
 */
public class FENData {
	
	int board[][];
	boolean whiteTurn;
	boolean castlingwk = false;
	boolean castlingwq = false;
	boolean castlingbk = false;
	boolean castlingbq = false;
	int enPassantX = -1;
	int enPassantY = -1;
	
	int halfMove = 0;
	int fullMove = 1;
	
	String FEN = "";
	
	/**
	 * Thrown when dealing with bad FEN's.
	 * @author giampi
	 *
	 */
	public class MalformedFENException extends Exception
	{
		String f;
		String r;
		
		public MalformedFENException(String fen, String reason)
		{
			f = fen; r = reason;
		}
		
		public String getMessage()
		{
			return "Bad FEN: "+r+".";
		}
	}
	
	public FENData(String fen) throws MalformedFENException
	{
		FEN = fen;
		StringTokenizer st = new StringTokenizer(fen);
		if (st.countTokens()!=6)
			throw (new MalformedFENException(fen,"incorrect field count"));
		
		for (int k=0; k<6; k++)
		{
			String token = st.nextToken();
			switch (k)
			{
			case 0: board = boardLayoutFromFEN(token); break;
			case 1:
				if (token.equals("w")) whiteTurn=true;
				else if (token.equals("b")) whiteTurn=false;
				else throw (new MalformedFENException(fen,"field only accepts 'w' or 'b'"));
				break;
			case 2:
				if (token.equals("-"))
				{}
				else
				for (int j=0; j<token.length(); j++)
				{
					char c = token.charAt(j);
					switch (c)
					{
					case 'K': castlingwk=true; break;
					case 'Q': castlingwq=true; break;
					case 'k': castlingbk=true; break;
					case 'q': castlingbq=true; break;
					default: throw (new MalformedFENException(fen,"illegal castling character"));
					}
				}
				break;
			case 3:
				if (token.equals("-"))
				{}
				else if (token.length()!=2) throw (new MalformedFENException(fen,"illegal en passant string"));
				else
				{
					enPassantX = token.charAt(0)-'a';
					enPassantY = token.charAt(1)-'1';
					if (enPassantX>7 || enPassantY>7 || enPassantX<0 || enPassantY<0)
						throw (new MalformedFENException(fen,"illegal en passant square"));
				}
				break;
			case 4:
				halfMove = Integer.parseInt(token);
				break;
			case 5:
				fullMove = Integer.parseInt(token);
				break;
			}
		}
		
	}
	
	public int[][] boardLayoutFromFEN(String fen) throws MalformedFENException
	{
		int result[][] = new int[8][8];
		for (int k=0; k<8; k++) for (int j=0; j<8; j++) result[k][j] = umpire.local.LocalUmpire.EMPTY;
		
		byte file,rank;
		file = 0;
		rank = 7;

		for (int k=0; k<fen.length(); k++)
		{
			char c = fen.charAt(k);
			char piece=0;
			switch (c)
			{
				case 'K': piece = umpire.local.LocalUmpire.WK; break;
				case 'P': piece = umpire.local.LocalUmpire.WP; break;
				case 'Q': piece = umpire.local.LocalUmpire.WQ; break;
				case 'R': piece = umpire.local.LocalUmpire.WR; break;
				case 'B': piece = umpire.local.LocalUmpire.WB; break;
				case 'N': piece = umpire.local.LocalUmpire.WN; break;
				case 'k': piece = umpire.local.LocalUmpire.BK; break;
				case 'p': piece = umpire.local.LocalUmpire.BP; break;
				case 'q': piece = umpire.local.LocalUmpire.BQ; break;
				case 'r': piece = umpire.local.LocalUmpire.BR; break;
				case 'b': piece = umpire.local.LocalUmpire.BB; break;
				case 'n': piece = umpire.local.LocalUmpire.BN; break;
				case '1': file+=1; break;
				case '2': file+=2; break;
				case '3': file+=3; break;
				case '4': file+=4; break;
				case '5': file+=5; break;
				case '6': file+=6; break;
				case '7': file+=7; break;
				case '8': file+=8; break;
				
				case '/':
				if (rank==0)
				{
					throw (new MalformedFENException(fen,"too many ranks"));
				} else
				if (file!=8)
				{
					throw (new MalformedFENException(fen,"incorrect rank width"));
				}
				else
				{
					file=0; rank--;
				}
				break;
			}
			
			if (piece!=0)
			{
				result[file++][rank] = piece;
				piece = 0;
			}
			
		}
		
		if (rank!=0 || file!=8)
		{
			throw (new MalformedFENException(fen,"unexpected end of string"));
		}

		return result;
	}
	
	public String toString()
	{
		return FEN;
	}

	public int[][] getBoard() {
		return board;
	}

	public boolean isCastlingbk() {
		return castlingbk;
	}

	public boolean isCastlingbq() {
		return castlingbq;
	}

	public boolean isCastlingwk() {
		return castlingwk;
	}

	public boolean isCastlingwq() {
		return castlingwq;
	}

	public int getEnPassantX() {
		return enPassantX;
	}

	public int getEnPassantY() {
		return enPassantY;
	}

	public int getFullMove() {
		return fullMove;
	}

	public int getHalfMove() {
		return halfMove;
	}

	public boolean isWhiteTurn() {
		return whiteTurn;
	}

	public String getFEN() {
		return FEN;
	}

}
