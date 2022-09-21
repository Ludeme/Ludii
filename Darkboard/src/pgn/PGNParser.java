/*
 * Created on 29-set-05
 *
 */
package pgn;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import core.Move;
import database.GameDatabase;
import pgn.ExtendedPGNGame.PGNMoveData;
import umpire.local.FENData;
import umpire.local.StepwiseLocalUmpire;

/**
 * @author Nikola Novarlic
 * This won't parse a generic PGN file.
 *
 */
public class PGNParser {
	
	//InputStream source;
	Reader source;
	StepwiseLocalUmpire umpire;
	private char out[] = new char[16384];
	private char buffer[] = new char[16384];
	public String accumulator=null;
	public boolean recordGameString = false;
	private FENData fen;
	int length;
	private String extern;
	static byte a[] = new byte[1];
	
	public PGNParser(Reader s)
	{
		source = s;
		umpire = new StepwiseLocalUmpire(null,null);		
	}
	
	public PGNParser(InputStream stream)
	{
		source = new InputStreamReader(stream);
		umpire = new StepwiseLocalUmpire(null,null);
	}
	
	public PGNParser(String s)
	{
		source = new StringReader(s);
		umpire = new StepwiseLocalUmpire(null,null);
	}
	
	private int readNext()
	{
		
		if (source==null) return -1;
		try
		{
			length++;
			int data = source.read();
			
			if (extern!=null) { a[0]=(byte)data; extern += new String(a); }
			return data;
		} catch (IOException e) { e.printStackTrace(); return -1; }
	}
	
	private String readNextToken()
	{
		
		int result;
		char c;
		int index = 0;
		
		do { result = readNext(); if (result==-1) return null; c = (char)result; } 
			while (c<=32);
			
		if (c==34) //quotes
		{
			do { 
				result = readNext(); 
				if (result==-1) return null; 
				c = (char)result; 
				if (c!=34) out[index++] = c;
				}
			while (c!=34);
			return new String(out,0,index);
		}
		
		if (c==123) //open curly brace
		{
			do { 
				result = readNext(); 
				if (result==-1) return null; 
				c = (char)result; 
				out[index++] = c;
				}
			while (c!=125); //put curly brace at the end...
			return new String(out,0,index);			
		}
		
		if (c==91 || c==93 || c==123 || c==125) //[] {}
		{
			if (c==91) return "[";
			if (c==93) return "]";
			if (c==123) return "{";
			if (c==125) return "}";
		}
		
		//else, we just read everything up to the next whitespace.
		do
		{
			out[index++] = c;
			result = readNext();
			if (result!=-1) c = (char)result;
		} while (result!=-1 && c>32);
		return new String(out,0,index);
	}
	
	private boolean parsePGNTag(ExtendedPGNGame game)
	{
		String tagName = readNextToken();
		String tagValue = readNextToken();
		String bracket = readNextToken();
		if (tagName==null || tagValue==null || bracket==null || !bracket.equals("]"))
		{
			// System.out.println("PGN TAG ERROR");
			return false;
		}
		
		//System.out.println("Tag "+tagName+" "+tagValue);
		
		if (tagName.equals("White")) game.setWhite(tagValue);
		else
		if (tagName.equals("Black")) game.setBlack(tagValue);
		else
		if (tagName.equals("Date")) game.setDate(tagValue);
		else
		if (tagName.equals("Event")) 
		{
			game.setEvent(tagValue);
			if (tagValue.indexOf("w16")==-1) return false;
		} 
		else
		if (tagName.equals("Result")) game.setResult(tagValue);
		else
		if (tagName.equals("Site")) game.setSite(tagValue);
		else
		if (tagName.equals("Time")) game.setTime(tagValue);
		else
		if (tagName.equals("Variant")) game.setVariant(tagValue);
		else
		if (tagName.equals("ICCResult")) game.setICCResult(tagValue);
		else
		if (tagName.equals("WhiteElo")) game.setWhiteElo(Integer.parseInt(tagValue));
		else
		if (tagName.equals("BlackElo")) game.setBlackElo(Integer.parseInt(tagValue));
		else
		game.addTag(tagName, tagValue); //generic tag
		
		if (tagName.equals("FEN"))
			try
		{
			fen = new FENData(tagValue);	
		} catch (Exception e) {}
		
		return true;
	}
	
	public ExtendedPGNGame parseNextGame()
	{
		ExtendedPGNGame game = new ExtendedPGNGame();
		PGNMoveData last = null;
		boolean parsingIntro = true;
		boolean finished = false;
		boolean whiteTurn = true;
		boolean interpret = true;
		boolean umpireInitialized = false;
		String token;
		Move m;
		
		umpire.resetBoard();
		length=0;
		if (recordGameString) try { extern = new String(); /*source.mark(100000);*/ } catch (Exception e) {}
		else extern = null;
		try
		{
			
			while (!finished)
			{
				token = readNextToken();
				if (token!=null) game.associatedString+=token;
				if (token==null) return null;
				else
				if (token.equals("[")) { 
					if (parsePGNTag(game)==false) { interpret = false; } 
				}
				else
				if (token.endsWith("}")) {
					
					
					if (token.startsWith("(") && token.endsWith(")}")
							&& token.indexOf(":")!=-1)
					{
						//Extended PGN comment
						String reducedToken = token.substring(token.indexOf(":")+1, token.length()-2);
						//I'm not interested in knowing the illegal moves right now... just how many of them
						if (reducedToken.length()>0) System.out.println(reducedToken);
						int number = (reducedToken.length()>0? 1 : 0);
						for (int k=0; k<reducedToken.length(); k++) if (reducedToken.charAt(k)==',') number++;
						for (int k=0; k<number; k++) last.addFailedMove(new Move());
						//if (number!=0) System.out.print(number);
					}
					
				} //comment
				else
				if (token.equals("]")) 
				{
					interpret = false; //error
				}
				else
				if (token.equals("1-0") || token.equals("0-1") || token.equals("1/2-1/2") || token.equals("*")) 
				{
					finished = true; //a game will end with its result...
				}
				else
				if (token.endsWith(".")) {  } //move number, like "1.", absolutely useless to a computer
				else
				{
					if (interpret)
					{
						if (!umpireInitialized)
						{
							umpire.stepwiseInit(fen, null);
							umpireInitialized=true;
						}
						m = umpire.getMoveFromPGN(token,whiteTurn);
						game.addMove(whiteTurn,m,umpire.capX,umpire.capY,umpire.capture,umpire.check[0],umpire.check[1],umpire.tries);
						last = game.getLatestMove(whiteTurn);
						whiteTurn = !whiteTurn;
					}
				}
				
			}
			//System.out.println(game.getBlack());
			if (recordGameString)
			{
				game.associatedString = extern;
				//recover game text
				//source.reset();
				//length = source.read(buffer,0,length);
				//game.associatedString = new String(buffer,0,length);
			}
			return game;
		} catch (Exception e)
		{
			e.printStackTrace();
			if (game!=null)
			{
				System.out.println("Game Date: "+game.getDate());
				System.out.println("Game Time: "+game.getTime());
				System.out.println("White: "+game.getWhite());
				System.out.println("Black: "+game.getBlack());
			}
			return null;
		}
	}
	
	
	public static double getTryPerMoveRatio(GameDatabase gd, String who)
	{
		int tries = 0;
		int moves = 0;
		
		for (int k=0; k<gd.gameNumber(); k++)
		{
			ExtendedPGNGame g = gd.getGame(k);
			boolean getWhite = g.getWhite().equals(who);
			boolean getBlack = g.getBlack().equals(who);
			
			for (int j=0; j<g.getMoveNumber(); j++)
			{
				PGNMoveData d = g.getMove(true, j);
				if (getWhite && d!=null)
				{
					moves++; tries+=((d.failedMoves!=null? d.failedMoves.size() : 0)+1);
				}
				d = g.getMove(false, j);
				if (getBlack && d!=null)
				{
					moves++; tries+=((d.failedMoves!=null? d.failedMoves.size() : 0)+1);
				}
			}
		}
		
		return 1.0*tries/moves;
	}

}
