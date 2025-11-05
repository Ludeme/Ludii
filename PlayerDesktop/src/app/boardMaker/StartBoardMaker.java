package app.boardMaker;

import app.DesktopApp;
import app.boardMaker.handlers.Maker;

/**
 * A class to start the board maker
 */

public class StartBoardMaker
{
	private static Maker maker = null;
	
	public static void create(DesktopApp app) {
		maker = new Maker(app);
		maker.createBoardMaker();
	}
}
