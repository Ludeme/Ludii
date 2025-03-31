package app.boardMaker;

import app.boardMaker.handlers.Maker;

/**
 * A class to start the board maker
 */

public class StartBoardMaker
{
	private static Maker maker = null;
	
	public static void create() {
		maker = new Maker();
		maker.createBoardMaker();
	}
}
