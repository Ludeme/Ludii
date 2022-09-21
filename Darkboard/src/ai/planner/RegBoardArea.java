package ai.planner;

import java.util.Vector;

import ai.chunk.regboard.RegBoard;
import ai.chunk.regboard.RegBoard.RegBoardPOV;
import core.Metaposition;

/**
 * Uses a RegBoard to find an area on the board. Some 'active' squares based on that offset are then returned.
 * @author Nikola Novarlic
 *
 */
public class RegBoardArea implements Area {
	
	RegBoard board;
	Vector<int[]> activeSquares; //these are the squares in the regboard that are actually returned as the Area

	public RegBoardArea(RegBoard rba)
	{
		board = rba;
		
		for (int k=0; k<rba.getMoveNumber(); k++)
		{
			String s[] = rba.getMove(k);
			
			int sq[] = new int[2];
			sq[0] = Integer.parseInt(s[0]);
			sq[1] = Integer.parseInt(s[1]);
			
			activeSquares.add(sq);
		}
	}
	
	
	public Vector<int[]> getAreaSquares(Metaposition m) {
		
		Vector<int[]> result = new Vector();
		
		RegBoardPOV pov = board.new RegBoardPOV(false,false,false,false,false);
		
		int offset[] = board.match2(m, pov);
		
		if (offset[0]<0) return result;
		
		for (int k=0; k<activeSquares.size(); k++)
		{
			int sq[] = activeSquares.get(k);
			
			int x = (pov.mirrorH? board.getWidth(pov)-sq[0]-1+offset[0]: sq[0]+offset[0]);
			int y = (pov.mirrorV? board.getHeight(pov)-sq[1]-1+offset[1]: sq[1]+offset[1]);
			int add[] = {x,y};
			if (x>=0 && y>=0 && x<8 && y<8) result.add(add);
		}
		
		return result;
	}

	public int getSquareNumber(Metaposition m) {
		// TODO Auto-generated method stub
		return activeSquares.size();
	}

	public boolean isIncluded(int x, int y, Metaposition m) {
		// TODO Auto-generated method stub
		return false;
	}

}
