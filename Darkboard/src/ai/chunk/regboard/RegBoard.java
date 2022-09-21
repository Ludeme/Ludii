package ai.chunk.regboard;

import java.util.StringTokenizer;
import java.util.Vector;

import ai.player.Darkboard;
import core.Chessboard;
import core.Metaposition;
import core.Move;
import reader.MiniReader;
import reader.MiniReader.ReaderTag;

/**
 * A RegBoard is the chessboard equivalent to a regular expression.
 * It allows chessboards to be matched against the RegBoard, and can have
 * moves associated with it. An algorithm can be expressed as a series of
 * RegBoards. Rotation and mirroring are supported.
 * 
 * A RegBoard is comprised of a Main Quadrant, which is the area that will
 * be matched on the actual chessboard, square by square. Secondary
 * Quadrants are the rest of the chessboard and each of them is treated as
 * a single square. 
 * @author Nikola Novarlic
 *
 */
public class RegBoard /*implements TableModel */{
	
	public class RegBoardPOV
	{
		//only one rotation option can be true
		public boolean mirrorH, mirrorV, rotateLeft, rotateRight, rotate180;
		
		public RegBoardPOV(boolean mh, boolean mv, boolean rl, boolean rr, boolean r180)
		{
			mirrorH = mh; mirrorV = mv; rotateLeft = rl; rotateRight = rr; rotate180 = r180;
			if (rotateLeft) rotateRight = rotate180 = false;
			if (rotateRight) rotate180 = false;
		}
	}
	
	RegBoardPOV defaultPOV = new RegBoardPOV(false,false,false,false,false);
	
	//constants
	public static final int ANY = 0;
	public static final int OWN_KING = 1;
	public static final int OWN_QUEEN = 2;
	public static final int OWN_ROOK = 3;
	public static final int OWN_BISHOP = 4;
	public static final int OWN_KNIGHT = 5;
	public static final int OWN_PAWN = 6;
	public static final int ENEMY_KING = 7;
	public static final int ENEMY_QUEEN = 8;
	public static final int ENEMY_ROOK = 9;
	public static final int ENEMY_BISHOP = 10;
	public static final int ENEMY_KNIGHT = 11;
	public static final int ENEMY_PAWN = 12;
	public static final int EMPTY = 13;
	
	//quadrant constants
	public static final int MAIN_QUADRANT = 0;
	public static final int CORNER_QUADRANT = 1;
	public static final int UP_QUADRANT = 2;
	public static final int DOWN_QUADRANT = 3;
	public static final int LEFT_QUADRANT = 4;
	public static final int RIGHT_QUADRANT = 5;
	
	public static final int TOPLEFT_QUADRANT = 0;
	public static final int BOTTOMLEFT_QUADRANT = 1;
	public static final int BOTTOMRIGHT_QUADRANT = 2;
	public static final int TOPRIGHT_QUADRANT = 3;
	
	//table stuff
	public static final String moveColumns[] = {"X1","Y1","X2","Y2"};
	// Vector<TableModelListener> listeners = new Vector();
	
	//main quadrant
	int mainQuadrant[][];
	int width;
	int height;

	//secondary quadrants
	int cornerQuadrants[];
	int upQuadrants[];
	int downQuadrants[];
	int leftQuadrants[];
	int rightQuadrants[];
	
	//mirror/rotation options
	boolean canMirrorH, canMirrorV;
	boolean canRotateLeft, canRotateRight, canRotate180;
	
	//moves
	private Vector<String[]> moves = new Vector();
	
	public RegBoard(int x, int y)
	{
		width = x;
		height = y;
		
		mainQuadrant = new int[x][y];
		for (int k=0; k<width; k++)
			for (int j=0; j<height; j++) mainQuadrant[k][j] = (1 << ANY);
		cornerQuadrants = new int[4];
		for (int k=0; k<4; k++) cornerQuadrants[k] = (1 << ANY);
		upQuadrants = new int[width];
		downQuadrants = new int[width];
		for (int k=0; k<width; k++) upQuadrants[k] = downQuadrants[k] = (1 << ANY);
		leftQuadrants = new int[height];
		rightQuadrants = new int[height];
		for (int k=0; k<height; k++) leftQuadrants[k] = rightQuadrants[k] = (1 << ANY);
		
		canMirrorH = canMirrorV = true;
		canRotateLeft = canRotateRight = canRotate180;
	}
	
	public RegBoard(ReaderTag rt)
	{
		loadFromTags(rt);
	}
	
	public RegBoard(String tags)
	{
		loadFromTags(new MiniReader(tags).parse());
	}
	
	/**
	 * Loads a RegBoard from a parsed ReaderTag structure
	 * @param rt
	 */
	public void loadFromTags(ReaderTag rt)
	{
		if (!rt.tag.equals("regboard")) return;
		for (int k=0; k<rt.subtags.size(); k++)
		{
			ReaderTag sub = rt.subtags.get(k);
			if (sub.tag.equals("width")) width = Integer.parseInt(sub.value);
			if (sub.tag.equals("height")) height = Integer.parseInt(sub.value);
			if (sub.tag.equals("mirror-h")) canMirrorH = Boolean.parseBoolean(sub.value);
			if (sub.tag.equals("mirror-v")) canMirrorV = Boolean.parseBoolean(sub.value);
			if (sub.tag.equals("rotate-left")) canRotateLeft = Boolean.parseBoolean(sub.value);
			if (sub.tag.equals("rotate-right")) canRotateRight = Boolean.parseBoolean(sub.value);
			if (sub.tag.equals("rotate-180")) canRotate180 = Boolean.parseBoolean(sub.value);
			if (sub.tag.equals("main-quadrant"))
			{
				mainQuadrant = new int[width][height];
				StringTokenizer st = new StringTokenizer(sub.value);
				for (int y=0; y<height; y++)
					for (int x=0; x<width; x++)
						mainQuadrant[x][y] = valueFromString(st.nextToken());
			}
			if (sub.tag.equals("corner-quadrant"))
			{
				cornerQuadrants = new int[4];
				StringTokenizer st = new StringTokenizer(sub.value);
				for (int y=0; y<4; y++) cornerQuadrants[y] = valueFromString(st.nextToken());
			}
			if (sub.tag.equals("up-quadrant"))
			{
				upQuadrants = new int[width];
				StringTokenizer st = new StringTokenizer(sub.value);
				for (int y=0; y<width; y++) upQuadrants[y] = valueFromString(st.nextToken());
			}
			if (sub.tag.equals("down-quadrant"))
			{
				downQuadrants = new int[width];
				StringTokenizer st = new StringTokenizer(sub.value);
				for (int y=0; y<width; y++) downQuadrants[y] = valueFromString(st.nextToken());
			}
			if (sub.tag.equals("left-quadrant"))
			{
				leftQuadrants = new int[height];
				StringTokenizer st = new StringTokenizer(sub.value);
				for (int y=0; y<height; y++) leftQuadrants[y] = valueFromString(st.nextToken());
			}
			if (sub.tag.equals("right-quadrant"))
			{
				rightQuadrants = new int[height];
				StringTokenizer st = new StringTokenizer(sub.value);
				for (int y=0; y<height; y++) rightQuadrants[y] = valueFromString(st.nextToken());
			}
			if (sub.tag.equals("moves"))
			{
				for (int mv=0; mv<sub.subtags.size(); mv++)
				{
					ReaderTag rt2 = sub.subtags.get(mv);
					if (rt2.tag.equals("move"))
					{
						StringTokenizer st = new StringTokenizer(rt2.value);
						String a1,a2,a3,a4;
						a1 = st.nextToken();
						a2 = st.nextToken();
						a3 = st.nextToken();
						a4 = st.nextToken();
						this.addMove(a1, a2, a3, a4);
					}
				}
				
			}
		}
	}
	
	public static boolean isAt(int value, int what)
	{
		return ((value & (1 << what))!=0);
	}
	
	public int getWidth(RegBoardPOV pov)
	{
		if (pov==null) return width;
		if (pov.rotateLeft || pov.rotateRight) return height;
		return width;
	}
	
	public int getHeight(RegBoardPOV pov)
	{
		if (pov==null) return height;
		if (pov.rotateLeft || pov.rotateRight) return width;
		return height;
	}
	
	public int getMainQuadValue(int x, int y, RegBoardPOV p)
	{
		RegBoardPOV pov = (p!=null? p : defaultPOV);
		
		int coordX, coordY;
		
		coordX = x; coordY = y;
		
		if (pov.mirrorH) coordX = width - x - 1;
		if (pov.mirrorV) coordY = height - y - 1;
		/*if (pov.rotateLeft)
		{ 
			coordY = x;
			coordX = width - y - 1;
		}*/
		
		return mainQuadrant[coordX][coordY];
	}
	
	public void setMainQuadValue(int x, int y, int value, RegBoardPOV p)
	{
		RegBoardPOV pov = (p!=null? p : defaultPOV);
		
		int coordX, coordY;
		
		coordX = x; coordY = y;
		
		if (pov.mirrorH) coordX = width - x - 1;
		if (pov.mirrorV) coordX = height - y - 1;
		/*if (pov.rotateLeft)
		{ 
			coordY = x;
			coordX = width - y - 1;
		}*/
		
		mainQuadrant[coordX][coordY] = value;
	}
	
	public int getSecondaryQuadValue(int type, int number, RegBoardPOV p)
	{
		RegBoardPOV pov = (p!=null? p : defaultPOV);
		int n = number;
		
		switch (type)
		{
		case (CORNER_QUADRANT):
			if (pov.mirrorH) n = 3 - n;
			if (pov.mirrorV) n = (n%2 != 0 ? n-1 : n+1);
			return cornerQuadrants[n];
		
		case (UP_QUADRANT):
			if (pov.mirrorH) n = width - n - 1;
			return (pov.mirrorV? downQuadrants[n]: upQuadrants[n]);
		
		case (DOWN_QUADRANT):
			if (pov.mirrorH) n = width - n - 1;
			return (pov.mirrorV? upQuadrants[n]: downQuadrants[n]);
		
		case (LEFT_QUADRANT):
			if (pov.mirrorV) n = height - n - 1;
			return (pov.mirrorH? rightQuadrants[n] : leftQuadrants[n]);
		
		case (RIGHT_QUADRANT):
			if (pov.mirrorV) n = height - n - 1;
			return (pov.mirrorH? leftQuadrants[n] : rightQuadrants[n]);
		}
		
		return 0;
		
	}
	
	public void setSecondaryQuadValue(int type, int number, int value, RegBoardPOV p)
	{
		RegBoardPOV pov = (p!=null? p : defaultPOV);
		int n = number;
		
		switch (type)
		{
		case (CORNER_QUADRANT):
			if (pov.mirrorH) n = 3 - n;
			if (pov.mirrorV) n = (n%2 != 0 ? n-1 : n+1);
			cornerQuadrants[n] = value;
			break;
		
		case (UP_QUADRANT):
			if (pov.mirrorH) n = width - n - 1;
			upQuadrants[n] = value;
			break;
		
		case (DOWN_QUADRANT):
			if (pov.mirrorH) n = width - n - 1;
			downQuadrants[n] = value;
			break;
		
		case (LEFT_QUADRANT):
			if (pov.mirrorV) n = height - n - 1;
			leftQuadrants[n] = value;
			break;
		
		case (RIGHT_QUADRANT):
			if (pov.mirrorV) n = height - n - 1;
			rightQuadrants[n] = value;
			break;
		}
		
	}
	
	protected boolean isValueCompatibleWithMetapositionValue(int value, Metaposition m, int x, int y)
	{
		if (isAt(value,ANY)) return true;
		int piece = m.getFriendlyPiece(x, y);
		if (piece==Chessboard.KING) return (isAt(value,OWN_KING));
		if (piece==Chessboard.QUEEN) return (isAt(value,OWN_QUEEN));
		if (piece==Chessboard.ROOK) return (isAt(value,OWN_ROOK));
		if (piece==Chessboard.BISHOP) return (isAt(value,OWN_BISHOP));
		if (piece==Chessboard.KNIGHT) return (isAt(value,OWN_KNIGHT));
		if (piece==Chessboard.PAWN) return (isAt(value,OWN_PAWN));
		
		//not a friendly piece... make sure any possible type is in the RegBoard
		if (m.canContain((byte)x, (byte)y, (byte)Chessboard.PAWN) && !isAt(value,ENEMY_PAWN)) return false;
		if (m.canContain((byte)x, (byte)y, (byte)Chessboard.KNIGHT) && !isAt(value,ENEMY_KNIGHT)) return false;
		if (m.canContain((byte)x, (byte)y, (byte)Chessboard.BISHOP) && !isAt(value,ENEMY_BISHOP)) return false;
		if (m.canContain((byte)x, (byte)y, (byte)Chessboard.ROOK) && !isAt(value,ENEMY_ROOK)) return false;
		if (m.canContain((byte)x, (byte)y, (byte)Chessboard.QUEEN) && !isAt(value,ENEMY_QUEEN)) return false;
		if (m.canContain((byte)x, (byte)y, (byte)Chessboard.KING) && !isAt(value,ENEMY_KING)) return false;
		if (m.canContain((byte)x, (byte)y, (byte)Chessboard.EMPTY) && !isAt(value,EMPTY)) return false;
		
		return true;
		
	}
	
	protected boolean checkSecondaryQuadrant(int val, int x1, int y1, int x2, int y2, Metaposition m, RegBoardPOV pov)
	{
		for (int x=x1; x<=x2; x++)
			for (int y=y1; y<=y2; y++)
				if (!isValueCompatibleWithMetapositionValue(val, m, x, y)) return false;
	
		return true;
	}
	
	int[] match(Metaposition m, RegBoardPOV pov)
	{
		int result[] = new int[2];
		
		result[0] = result[1] = -1;
		
		int width = this.getWidth(pov);
		int height = this.getHeight(pov);
		
		
		for (int x=0; x<9-width; x++)
			for (int y=0; y<9-height; y++)
			{
				boolean stop = false;
				//check main quadrant
				for (int offx=0; offx<width; offx++)
				{
					for (int offy=0; offy<height; offy++)
					{
						int val = getMainQuadValue(offx,offy,pov);
						if (!isValueCompatibleWithMetapositionValue(val,m,x+offx,y+offy)) stop = true;
						if (stop) break;
					}
					if (stop) break;
				}
				//check secondary quadrants
				if (stop) continue;
				//System.out.println("Main quadrant match "+x+" "+y+" "+pov.mirrorH+" "+pov.mirrorV);
				
				if (!checkSecondaryQuadrant(getSecondaryQuadValue(CORNER_QUADRANT,BOTTOMLEFT_QUADRANT,pov),0,0,x-1,y-1,m,pov)) continue;
				if (!checkSecondaryQuadrant(getSecondaryQuadValue(CORNER_QUADRANT,BOTTOMRIGHT_QUADRANT,pov),x+width,0,7,y-1,m,pov)) continue;
				if (!checkSecondaryQuadrant(getSecondaryQuadValue(CORNER_QUADRANT,TOPRIGHT_QUADRANT,pov),x+width,y+height,7,7,m,pov)) continue;
				if (!checkSecondaryQuadrant(getSecondaryQuadValue(CORNER_QUADRANT,TOPLEFT_QUADRANT,pov),0,y+height,x-1,7,m,pov)) continue;
				//System.out.println("Checking UP/DOWN");
				for (int k=0; k<width; k++)
				{
					if (!checkSecondaryQuadrant(getSecondaryQuadValue(UP_QUADRANT,k,pov),x+k,y+height,x+k,7,m,pov)) { stop = true; break;}
					if (!checkSecondaryQuadrant(getSecondaryQuadValue(DOWN_QUADRANT,k,pov),x+k,0,x+k,y-1,m,pov)) { stop = true; break; }
				}
				if (stop) continue;
				
				//System.out.println("Checking LEFT/RIGHT");
				for (int k=0; k<height; k++)
				{
					if (!checkSecondaryQuadrant(getSecondaryQuadValue(LEFT_QUADRANT,k,pov),0,y+k,x-1,y+k,m,pov)) { stop = true; break;}
					if (!checkSecondaryQuadrant(getSecondaryQuadValue(RIGHT_QUADRANT,k,pov),x+width,y+k,7,y+k,m,pov)) { stop = true; break;}
				}
				if (stop) continue;
				
				result[0] = x; result[1] = y;
				return result;
			}
		
		return result;
	}
	
	public int[] match2(Metaposition m, RegBoardPOV out)
	{
		RegBoardPOV pov = new RegBoardPOV(false,false,false,false,false);
		
		int[] result = match(m,pov);
		if (result[0]>=0) 
			{
				out.mirrorH = false; out.mirrorV = false;
				return result;
			}
		
		if (canMirrorH)
		{
			pov.mirrorH = true;
			result = match(m,pov);
			if (result[0]>=0) 
			{
				out.mirrorH = true; out.mirrorV = false;
				return result;
			}
		}
		
		if (canMirrorV)
		{
			pov.mirrorH = false;
			pov.mirrorV = true;
			result = match(m,pov);
			if (result[0]>=0) 
			{
				out.mirrorH = false; out.mirrorV = true;
				return result;
			}
		}
		
		if (canMirrorH && canMirrorV)
		{
			pov.mirrorH = true;
			pov.mirrorV = true;
			result = match(m,pov);
			if (result[0]>=0) 
			{
				out.mirrorH = true; out.mirrorV = true;
				return result;
			}
		}
		
		result = new int[2];
		result[0] = result[1] = -1;
		return result;
	}
	
	public int getMoveNumber()
	{
		return moves.size();
	}
	
	
	public void addMove(String x, String y, String x2, String y2)
	{
		String i[] = new String[4];
		i[0] = x; i[1] = y; i[2] = x2; i[3] = y2;
		moves.add(i);
		notifyTableListeners();
	}
	
	public String[] getMove(int k)
	{
		return moves.get(k);
	}
	
	public void removeMove(int k)
	{
		moves.remove(k);
		notifyTableListeners();
	}
	
	public Move getMove(Metaposition m, int moveid, int[] origin, RegBoardPOV pov)
	{
		Move move = new Move();
		
		String movedata[] = getMove(moveid);
		
		int offset[] = new int[4];
		for (int k=0; k<4; k++) offset[k] = parseMoveString(movedata[k],m,origin,pov,k%2==0);
		int x = (pov.mirrorH? getWidth(pov)-offset[0]-1 : offset[0]) + origin[0];
		int y = (pov.mirrorV? getHeight(pov)-offset[1]-1 : offset[1]) + origin[1];
		int x2 = (pov.mirrorH? getWidth(pov)-offset[2]-1 : offset[2]) + origin[0];
		int y2 = (pov.mirrorV? getHeight(pov)-offset[3]-1 : offset[3]) + origin[1];
		/*int x = (pov.mirrorH? -offset[0] : offset[0]) + origin[0];
		int y = (pov.mirrorV? -offset[1] : offset[1]) + origin[1];
		int x2 = (pov.mirrorH? -offset[2] : offset[2]) + origin[0];
		int y2 = (pov.mirrorV? -offset[3] : offset[3]) + origin[1];*/
		
		move.piece = m.getFriendlyPiece(x, y);
		move.fromX = (byte)x;
		move.fromY = (byte)y;
		move.toX = (byte)x2;
		move.toY = (byte)y2;
		
		return move;
	}
	
	/**
	 * A move string can be of the type...
	 * PARAM(+-)PARAM(+-)PARAM... etc
	 * where PARAM is either a number or a letter like
	 * R, meaning the coordinate of the first friendly rook
	 * @param what
	 * @param m
	 * @param origin
	 * @param pov
	 * @param horiz
	 * @return
	 */
	public int parseMoveString(String what, Metaposition m, int[] origin, RegBoardPOV pov, boolean horiz)
	{
		int index = 0;
		int number = 1;
		int total = 0;
		char sign = '+';
		while (index<what.length())
		{
			switch (what.charAt(index))
			{
			case '-': number = 1; sign = '-'; break;
			case '+': number = 1; sign = '+'; break;
			case '0': number = 0; total = (sign=='+'? total+number : total - number); break;
			case '1': number *= 1; total = (sign=='+'? total+number : total - number); break;
			case '2': number *= 2; total = (sign=='+'? total+number : total - number); break;
			case '3': number *= 3; total = (sign=='+'? total+number : total - number); break;
			case '4': number *= 4; total = (sign=='+'? total+number : total - number); break;
			case '5': number *= 5; total = (sign=='+'? total+number : total - number); break;
			case '6': number *= 6; total = (sign=='+'? total+number : total - number); break;
			case '7': number *= 7; total = (sign=='+'? total+number : total - number); break;
			case '8': number *= 8; total = (sign=='+'? total+number : total - number); break;
			case '9': number *= 9; total = (sign=='+'? total+number : total - number); break;
			case 'K':
			case 'Q':
			case 'R':
			case 'B':
			case 'N':
			case 'P':
				int k=-1;
				switch (what.charAt(index))
				{
				case 'K': k = m.getSquareWithPiece(Chessboard.KING); break;
				case 'Q': k = m.getSquareWithPiece(Chessboard.QUEEN); break;
				case 'R': k = m.getSquareWithPiece(Chessboard.ROOK); break;
				case 'B': k = m.getSquareWithPiece(Chessboard.BISHOP); break;
				case 'N': k = m.getSquareWithPiece(Chessboard.KNIGHT); break;
				case 'P': k = m.getSquareWithPiece(Chessboard.PAWN); break;
				}
				if (k<0) break;
				int x = k / 8; int y = k % 8;
				int b[] = metapositionToRegBoardCoord(m,x,y,origin,pov);
				
				if (pov.mirrorH) b[0] += (getWidth(pov)-1);
				if (pov.mirrorV) b[1] += (getHeight(pov)-1);
				
				number = (horiz? b[0] : b[1]);
				
				total = (sign=='+'? total+number : total - number);
				break;
			}
		index++;
		}
		return total;
	}
	
	public int[] metapositionToRegBoardCoord(Metaposition m, int x, int y, int origin[], RegBoardPOV pov)
	{
		int result[] = new int[2];
		
		result[0] = x - origin[0];
		result[1] = y - origin[1];
		
		if (pov.mirrorH) result[0] *= -1;
		if (pov.mirrorV) result[1] *= -1;
		
		return result;
	}
	
	public void changeSize(int newX, int newY)
	{
		if (newX<=0 || newY<=0) return;
		
		width = newX;
		height = newY;
		
		mainQuadrant = new int[width][height];
		for (int k=0; k<width; k++)
			for (int j=0; j<height; j++) mainQuadrant[k][j] = (1 << ANY);
		cornerQuadrants = new int[4];
		for (int k=0; k<4; k++) cornerQuadrants[k] = (1 << ANY);
		upQuadrants = new int[width];
		downQuadrants = new int[width];
		for (int k=0; k<width; k++) upQuadrants[k] = downQuadrants[k] = (1 << ANY);
		leftQuadrants = new int[height];
		rightQuadrants = new int[height];
		for (int k=0; k<height; k++) leftQuadrants[k] = rightQuadrants[k] = (1 << ANY);
		
		canMirrorH = canMirrorV = true;
		canRotateLeft = canRotateRight = canRotate180;
		
	}

	static String stringForValue(int val)
	{
		String s = "";
		
		if (isAt(val,ANY)) return "*";
		if (val==0) return "0";
		
		if (isAt(val,OWN_KING)) s += "K";
		if (isAt(val,OWN_QUEEN)) s += "Q";
		if (isAt(val,OWN_ROOK)) s += "R";
		if (isAt(val,OWN_BISHOP)) s += "B";
		if (isAt(val,OWN_KNIGHT)) s += "N";
		if (isAt(val,OWN_PAWN)) s += "P";
		if (isAt(val,ENEMY_KING)) s += "k";
		if (isAt(val,ENEMY_QUEEN)) s += "q";
		if (isAt(val,ENEMY_ROOK)) s += "r";
		if (isAt(val,ENEMY_BISHOP)) s += "b";
		if (isAt(val,ENEMY_KNIGHT)) s += "n";
		if (isAt(val,ENEMY_PAWN)) s += "p";
		if (isAt(val,EMPTY)) s += "~";
		
		return s;
	}
	
	static int valueFromString(String s)
	{
		if (s.indexOf("*")>=0) return (1 << ANY);
		if (s.indexOf("0")>=0) return 0;
		
		int result = 0;
		
		if (s.indexOf("K")>=0) result |= (1 << OWN_KING);
		if (s.indexOf("Q")>=0) result |= (1 << OWN_QUEEN);
		if (s.indexOf("R")>=0) result |= (1 << OWN_ROOK);
		if (s.indexOf("B")>=0) result |= (1 << OWN_BISHOP);
		if (s.indexOf("N")>=0) result |= (1 << OWN_KNIGHT);
		if (s.indexOf("P")>=0) result |= (1 << OWN_PAWN);
		if (s.indexOf("k")>=0) result |= (1 << ENEMY_KING);
		if (s.indexOf("q")>=0) result |= (1 << ENEMY_QUEEN);
		if (s.indexOf("r")>=0) result |= (1 << ENEMY_ROOK);
		if (s.indexOf("b")>=0) result |= (1 << ENEMY_BISHOP);
		if (s.indexOf("n")>=0) result |= (1 << ENEMY_KNIGHT);
		if (s.indexOf("p")>=0) result |= (1 << ENEMY_PAWN);
		if (s.indexOf("~")>=0) result |= (1 << EMPTY);
		
		return result;
	}
	
	String stringForMove(int m)
	{
		String move[] = getMove(m);
		return "<move>"+move[0]+" "+move[1]+" "+move[2]+" "+move[3]+"</move>\n";
	}
	
	
	/**
	 * Format = <TAG>value</TAG>
	 */
	public String toString()
	{
		String s = "<regboard>\n";
		s+="<width>"+width+"</width>\n";
		s+="<height>"+height+"</height>\n";
		
		s+="<mirror-h>"+canMirrorH+"</mirror-h>\n";
		s+="<mirror-v>"+canMirrorV+"</mirror-v>\n";
		s+="<rotate-left>"+canRotateLeft+"</rotate-left>\n";
		s+="<rotate-right>"+canRotateRight+"</rotate-right>\n";
		s+="<rotate-180>"+canRotate180+"</rotate-180>\n";
		
		s+="<main-quadrant>";
		for (int y=0; y<height; y++)
			for (int x=0; x<width; x++)
			{
				s+= stringForValue(getMainQuadValue(x, y, null));
				s+= " ";
			}
		s+="</main-quadrant>\n";
		
		s+="<corner-quadrant>";
		for (int x=0; x<4; x++)
			s+=(stringForValue(getSecondaryQuadValue(CORNER_QUADRANT, x, null))+" ");
		s+="</corner-quadrant>\n";
		s+="<up-quadrant>";
		for (int x=0; x<width; x++)
			s+=(stringForValue(getSecondaryQuadValue(UP_QUADRANT, x, null))+" ");
		s+="</up-quadrant>\n";
		s+="<down-quadrant>";
		for (int x=0; x<width; x++)
			s+=(stringForValue(getSecondaryQuadValue(DOWN_QUADRANT, x, null))+" ");
		s+="</down-quadrant>\n";
		s+="<left-quadrant>";
		for (int x=0; x<height; x++)
			s+=(stringForValue(getSecondaryQuadValue(LEFT_QUADRANT, x, null))+" ");
		s+="</left-quadrant>\n";
		s+="<right-quadrant>";
		for (int x=0; x<height; x++)
			s+=(stringForValue(getSecondaryQuadValue(RIGHT_QUADRANT, x, null))+" ");
		s+="</right-quadrant>\n";
		
		//moves
		s+="<moves>";
		for (int k=0; k<getMoveNumber(); k++)
			s+=stringForMove(k);
		s+="</moves>\n";
		
		s+="</regboard>";
			
		return s;
	}
	
	protected void notifyTableListeners()
	{
		/*
		for (int k=0; k<listeners.size(); k++)
			listeners.get(k).tableChanged(new TableModelEvent(this,TableModelEvent.HEADER_ROW));
		*/
	}
	
	public static void main(String args[])
	{
		Darkboard.initialize(args[0]);
		System.out.println(new RegBoard(3,2).toString());
	}

	public void addTableModelListener() {
		// if (!listeners.contains(l)) listeners.add(l);
		
	}

	public Class<?> getColumnClass(int columnIndex) {
		return String.class;
	}

	public int getColumnCount() {
		return 4;
	}

	public String getColumnName(int columnIndex) {
		return moveColumns[columnIndex];
	}

	public int getRowCount() {
		return moves.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return moves.get(rowIndex)[columnIndex];
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	public void removeTableModelListener() {
		// listeners.remove(l);
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		moves.get(rowIndex)[columnIndex] = (String)aValue;
	}
}
