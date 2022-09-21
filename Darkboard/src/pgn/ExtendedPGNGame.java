/*
 * Created on 16-set-05
 *
 */
package pgn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import core.Chessboard;
import core.Move;
import umpire.local.FENData;
import umpire.local.LocalUmpire;
import umpire.local.StepwiseLocalUmpire;

/**
 * @author Nikola Novarlic
 *
 */
public class ExtendedPGNGame implements Serializable {
	
	//standard PNG tags
	String event = "ICC w16";
	String site = "";
	String date = "";
	String time = "";
	String round = "1";
	String white = "Player 1";
	String black = "Player 2";
	String result = "*";
	String ICCResult = "*";
	String variant = "";
	int whiteElo = -1;
	int blackElo = -1;
	//String filtered = "no"
	
	protected static int counter = 0;
	
	Hashtable<String, String> tags = new Hashtable<String, String>();
	
	Vector whiteMoves;
	Vector blackMoves;
	
	Vector changeListeners = new Vector();
	
	public String associatedString="";
	
	public class PGNMoveData implements Serializable 
	{
		public Move finalMove = null;
		public int capturex, capturey, capturewhat, check1, check2, pawntries = -1;
		Vector<Move> failedMoves = null;
		
		public void addFailedMove(Move m)
		{
			if (failedMoves==null) failedMoves = new Vector<Move>();
			failedMoves.add(m);
		}
		
		public int getFailedMoveNumber()
		{
			return (failedMoves==null? 0 : failedMoves.size());
		}
		
		public Move getFailedMove(int k)
		{
			return (failedMoves==null || k<0 || k>=failedMoves.size()? null: failedMoves.get(k));
		}
	}
	
	public static String utilityParseInt(int i)
	{
		String s = String.valueOf(i);
		if (i<10) s = "0"+s;
		return s;
	}
	
	public ExtendedPGNGame()
	{
		whiteMoves = new Vector();
		blackMoves = new Vector();
		
		Calendar rightNow = Calendar.getInstance();
		setDate(rightNow.get(Calendar.YEAR) + "." + utilityParseInt(rightNow.get(Calendar.MONTH)+1) + "." +
		utilityParseInt(rightNow.get(Calendar.DAY_OF_MONTH)));
		setTime(utilityParseInt(rightNow.get(Calendar.HOUR_OF_DAY)) + ":" + utilityParseInt(rightNow.get(Calendar.MINUTE))
		+ ":" + utilityParseInt(rightNow.get(Calendar.SECOND)));
		
		
	}
	
	public void addMove(boolean white, Move m, int cx, int cy, int cwhat, int ch1, int ch2, int tries)
	{
		PGNMoveData data = new PGNMoveData();
		data.finalMove = m;
		data.capturex = cx;
		data.capturey = cy;
		data.capturewhat = cwhat;
		data.check1 = ch1;
		data.check2 = ch2;
		//System.out.println("ADDING MOVE, CHECKS = "+data.check1+" "+data.check2);
		data.pawntries = tries;
		
		if (white) whiteMoves.add(data); else blackMoves.add(data);
		
		firePGNChange();
	}
	
	public PGNMoveData getMove(boolean white, int index)
	{
		Vector v = (white? whiteMoves : blackMoves);
		if (index>=v.size()) return null;
		return ((PGNMoveData)v.get(index));
	}
	
	public int getMoveNumber()
	{
		return whiteMoves.size();
	}
	
	public PGNMoveData getLatestMove(boolean white)
	{
		Vector v = (white? whiteMoves : blackMoves);
		return ((PGNMoveData)v.lastElement());
	}
	
	public String outputPGNTag(String tag, String value)
	{
		if (tag==null) return "";
		if (value==null) value = "??";
		return ("["+tag+" \""+value+"\"]\n");
	}
	
	public boolean isPlayerWhite(String name)
	{
		return (getWhite().equals(name));
	}
	
	public String toString()
	{
		String result="";
		String pgnComments;
		StepwiseLocalUmpire u = new StepwiseLocalUmpire(null,null);
		FENData f = null;
		try
		{
			String s = tags.get("FEN");
			f = (s!=null?new FENData(s):null);
			
		} catch (Exception e){System.out.println("bah");}
		
		u.stepwiseInit(f, null);
		
		//add tags first
		result += outputPGNTag("Event",getEvent());
		result += outputPGNTag("Site",getSite());
		result += outputPGNTag("Date",getDate());
		result += outputPGNTag("Round",getRound());
		result += outputPGNTag("White",getWhite());
		result += outputPGNTag("Black",getBlack());
		result += outputPGNTag("Result",getResult());
		result += outputPGNTag("Variant",getVariant());
		result += outputPGNTag("Time",getTime());
		//now write any accessory tags
		Enumeration<String> e = tags.keys();
		while (e.hasMoreElements()) 
		{
			String t = e.nextElement();
			result+= outputPGNTag(t,tags.get(t));
		}
		
		result += "\n";
		
		for (int k=0; k<whiteMoves.size(); k++)
		{
			pgnComments = addExtendedPGNComments(k,true,u);
			result += ("" + (k+1) + ".   " + u.getPGNMoveString(getMove(true,k).finalMove,true,true)+" ");
			result += pgnComments;
			result += "\n";
			
			if (k<blackMoves.size())
			{
				pgnComments = addExtendedPGNComments(k,false,u);
				result += ("     " + u.getPGNMoveString(getMove(false,k).finalMove,false,true)+" ");
				result += pgnComments;
				result += "\n";				
			}
		}
		
		if (getResult()!=null) result += getResult();
		result+="\n\n"; //makes merging files easier.
		
		return result;
	}
	
	/**
	 * Generates a PGN Comment interpreted by extended PGN, like {(Xh5:Ra8,exa8=Q)}
	 * @param index
	 * @param white
	 */
	public String addExtendedPGNComments(int index, boolean white, LocalUmpire game)
	{
		String result = "{(";
		PGNMoveData move = getMove(white,index);
		boolean previousTag = false;
		//add umpire comments first.
		if (move.capturewhat!=Chessboard.NO_CAPTURE)
		{
			previousTag = true;
			result+="X";
			result+=Move.squareString(move.capturex,move.capturey);
		}
		if (move.check1!=Chessboard.NO_CHECK || move.check2!=Chessboard.NO_CHECK)
		{
			if (previousTag) result+=",";
			previousTag = true;
			result+="C";
			if (move.check1==Chessboard.CHECK_FILE || move.check2==Chessboard.CHECK_FILE)
				result+="F";
			if (move.check1==Chessboard.CHECK_LONG_DIAGONAL || move.check2==Chessboard.CHECK_LONG_DIAGONAL)
				result+="L";
			if (move.check1==Chessboard.CHECK_KNIGHT || move.check2==Chessboard.CHECK_KNIGHT)
				result+="N";
			if (move.check1==Chessboard.CHECK_RANK || move.check2==Chessboard.CHECK_RANK)
				result+="R";
			if (move.check1==Chessboard.CHECK_SHORT_DIAGONAL || move.check2==Chessboard.CHECK_SHORT_DIAGONAL)
				result+="S";
		}
		if (move.pawntries>0)
		{
			if (previousTag) result+=",";
			previousTag = true;
			result+=("P"+move.pawntries);			
		}
		result+=":";
		//write down illegal move attempts...
		if (move.failedMoves!=null)
		for (int k=0; k<move.failedMoves.size(); k++)
			{
				Move m = (Move)move.failedMoves.get(k);
				if (k>0) result += ",";
				result+=game.getPGNMoveString(m,white,false);
			}
		result+=")}";
		return result;
	}
	
	/**
	 * Replays the game on the umpire, so that it contains the final board state
	 * @param lu
	 */
	public void replayGame(LocalUmpire lu)
	{
		lu.resetBoard();
		for (int k=0; k<getMoveNumber(); k++)
		{
			lu.moveCount++;
			PGNMoveData data = getMove(true,k);
			if (data!=null) lu.doMove(data.finalMove,0); else return;
			data = getMove(false,k);
			if (data!=null) lu.doMove(data.finalMove,1); else return;
		}
	}
	
	/**
	 * Returns the appropriate file name for this game.
	 * @return
	 */
	public String getFileName()
	{
		String result;
		result = getWhite() + "_" + getBlack() + "_" + getDate() + "_" + (counter++) + ".pgn";
		return result;
	}
	
	public void saveToFile()
	{
		// Change path to save to initial directory 
		String PGNpath = getFileName();
		File f = new File("./" + PGNpath);
		saveToFile(f);
				
		// Display current PGN
		// System.out.println("begin-pgn");
		try (BufferedReader br = new BufferedReader(new FileReader("./" + PGNpath))) {
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch(Exception E) {
			 System.out.println("File doesn't exist..");
		}
		// System.out.println("end-pgn");
	}
	
	public void saveToFile(File f)
	{
		FileOutputStream stream;
		try
		{
			f.delete();
			f.createNewFile();
			stream = new FileOutputStream(f);
			stream.write(toString().getBytes());
			stream.close();
			
		} catch (Exception e) { e.printStackTrace(); }		
	}
	
	public void addTag(String k, String v)
	{
		if (tags.containsKey(k)) tags.remove(k);
		tags.put(k, v);
	}
	
	public String getTag(String k)
	{
		return tags.get(k);
	}

	/**
	 * @return
	 */
	public String getBlack() {
		return black;
	}

	/**
	 * @return
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @return
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * @return
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @return
	 */
	public String getRound() {
		return round;
	}

	/**
	 * @return
	 */
	public String getSite() {
		return site;
	}

	/**
	 * @return
	 */
	public String getVariant() {
		return variant;
	}

	/**
	 * @return
	 */
	public String getWhite() {
		return white;
	}

	/**
	 * @param string
	 */
	public void setBlack(String string) {
		black = string;
		//addTag("Black",string);
	}

	/**
	 * @param string
	 */
	public void setDate(String string) {
		date = string;
		//addTag("Date",string);
	}

	/**
	 * @param string
	 */
	public void setEvent(String string) {
		event = string;
		//addTag("Event",string);
	}

	/**
	 * @param string
	 */
	public void setResult(String string) {
		result = string;
		//addTag("Result",string);
	}

	/**
	 * @param string
	 */
	public void setRound(String string) {
		round = string;
		//addTag("Round",string);
	}

	/**
	 * @param string
	 */
	public void setSite(String string) {
		site = string;
		//addTag("Site",string);
	}

	/**
	 * @param string
	 */
	public void setVariant(String string) {
		variant = string;
		//addTag("Variant",string);
	}

	/**
	 * @param string
	 */
	public void setWhite(String string) {
		white = string;
		//addTag("White",string);
	}

	/**
	 * @return
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @param string
	 */
	public void setTime(String string) {
		time = string;
	}

	/**
	 * @return
	 */
	public String getICCResult() {
		return ICCResult;
	}

	/**
	 * @param string
	 */
	public void setICCResult(String string) {
		ICCResult = string;
	}
	
	public int getWhiteElo() {
		return whiteElo;
	}

	public void setWhiteElo(int whiteElo) {
		this.whiteElo = whiteElo;
	}

	public int getBlackElo() {
		return blackElo;
	}

	public void setBlackElo(int blackElo) {
		this.blackElo = blackElo;
	}
	
	public int getBestElo()
	{
		if (result.equals("1-0")) return whiteElo;
		else if (result.equals("0-1")) return blackElo;
		else if (whiteElo>blackElo) return whiteElo;
		else return blackElo;
	}

	public boolean isCheckmate()
	{
		return (getICCResult().indexOf("checkmate")!=-1);
	}
	
	public boolean isStalemate()
	{
		return (getICCResult().indexOf("stalemate")!=-1);
	}
	
	public void addListener(pgn.PGNChangeListener l)
	{
		changeListeners.add(l);
	}
	
	public void removeListener(pgn.PGNChangeListener l)
	{
		changeListeners.remove(l);
	}
	
	public void firePGNChange()
	{
		for (int k=0; k<changeListeners.size(); k++)
		{
			((pgn.PGNChangeListener)changeListeners.get(k)).pgnChangeNotify(this);
		}
	}

}
