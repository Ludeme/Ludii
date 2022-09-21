package ai.chunk.regboard;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Vector;

import ai.chunk.regboard.RegBoard.RegBoardPOV;
import ai.player.Darkboard;
import core.Globals;
import core.Metaposition;
import core.Move;
import reader.MiniReader;
import reader.MiniReader.ReaderTag;

/**
 * A set of RegBoard objects that, together, define a strategy, for
 * examples Boyce's KRK algorithm.
 * @author Nikola Novarlic
 *
 */
public class RegBoardArray {
	
	Vector<RegBoard> boards = new Vector();
	
	public RegBoardArray(String filepath)
	{
		// System.out.println(filepath);
		try
		{
			// FileInputStream fis = new FileInputStream(filepath);
			// InputStream fis = RegBoard.class.getResourceAsStream("boyce.txt");
			InputStream fis = main.Darkboard.class.getResourceAsStream("darkboard_data/boyce.txt");
			MiniReader mr = new MiniReader(fis);
			ReaderTag rt = mr.parse();
			if (!rt.tag.equals("regboard-array")) return;
			for (int k=0; k<rt.subtags.size(); k++)
			{
				boards.add(new RegBoard(rt.subtags.get(k)));
			}
			
		} catch (Exception e) { e.printStackTrace();}
	}
	
	public RegBoardArray(InputStream is)
	{
		try
		{
			MiniReader mr = new MiniReader(is);
			ReaderTag rt = mr.parse();
			if (!rt.tag.equals("regboard-array")) return;
			for (int k=0; k<rt.subtags.size(); k++)
			{
				boards.add(new RegBoard(rt.subtags.get(k)));
			}
			
		} catch (Exception e) { e.printStackTrace();}
	}
	
	public RegBoard getBoard(int k)
	{
		return boards.get(k);
	}
	
	public int getBoardNumber()
	{
		return boards.size();
	}
	
	public String toString()
	{
		String s = "<regboard-array>\n";
		for (int k=0; k<boards.size(); k++) s+=boards.get(k).toString();
		s+="\n</regboard-array>";
		return s;
	}
	
	public Move getMoveSuggestion(Metaposition m, Vector<Move> bannedMoves)
	{
		RegBoardPOV pov = new RegBoard(1,1).new RegBoardPOV(false,false,false,false,false);
		
		for (int k=0; k<boards.size(); k++)
		{
			RegBoard rb = boards.get(k);
			//System.out.println("Testing board "+k);
			int offset[] = rb.match2(m, pov);
			if (offset[0]>=0)
			{
				//match!
				for (int j=0; j<rb.getMoveNumber(); j++)
				{
					Move move = rb.getMove(m, j, offset, pov);
					//check if it's banned
					boolean banned = false;
					for (int tr=0; tr<bannedMoves.size(); tr++)
					{
						if (bannedMoves.get(tr).equals(move)) banned = true;
					}
					if (!banned && move.fromX>=0 && move.fromX<8 && move.fromY>=0
							&& move.fromY<8 && move.toX>=0 && move.toX<8 &&
							move.toY>=0 && move.toY<8) return move;
				}
			}
		}
		return null;
	}
	
	public static void main(String args[])
	{
		Darkboard.initialize(args[0]);
		// System.out.println(new RegBoardArray(Globals.PGNPath+"boyce.txt").toString());
	}

}
