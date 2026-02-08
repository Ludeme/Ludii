package app.boardMaker;

import app.DesktopApp;
import app.boardMaker.handlers.Maker;

/**
 * A class to start the board maker
 */

public class StartBoardMaker
{
	private static Maker maker = null;

	/**
	 * Creates the Boardmaker instance
	 * @param app the main instance of Ludii
	 */
	public static void create(DesktopApp app) {
		maker = new Maker(app);
		maker.createBoardMaker();
	}
}
