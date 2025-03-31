package app.boardMaker.handlers;

import javax.swing.SwingUtilities;

/**
 * General handler of the board maker app.
 */

public class Maker
{
	private Displayer displayer;
	
	public Maker() {
		displayer = new Displayer();
	}
	
	/**
	 * Creates the board maker
	 */
	public void createBoardMaker() {
		SwingUtilities.invokeLater(new Runnable()
		{
			
			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				displayer.createWindow();
			}
		});
	}
	
	//--------------------------------------------------------------------------------
	
	/**
	 * Gets the Displayer
	 * @return the displayer
	 */
	public Displayer getDisplayer() {
		return displayer;
	}
	
	//--------------------------------------------------------------------------------
}
