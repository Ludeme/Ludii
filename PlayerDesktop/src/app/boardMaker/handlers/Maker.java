package app.boardMaker.handlers;

import javax.swing.SwingUtilities;

import app.boardMaker.res.StyleType;
import game.types.play.ModeType;

/**
 * General handler of the board maker app.
 */

public class Maker
{
	private Displayer displayer;
	
	private String gamename;
	private int players;
	private ModeType mode;
	
	private StyleType style = StyleType.BoardStyle;
	
	public Maker() {
		displayer = new Displayer(this);
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
	
	/**
	 * Gets the name of the game
	 * @return the name of the game
	 */
	public String getName() {
		return gamename;
	}
	
	/**
	 * Gets the number of player
	 * @return the number of player
	 */
	public int getPlayers() {
		return players;
	}
	
	/**
	 * Gets the mode of the game
	 * @return the game mode
	 */
	public ModeType getMode() {
		return mode;
	}
	
	//--------------------------------------------------------------------------------
	
	/**
	 * Sets basic informations about the game
	 * @param name the name of the game
	 * @param players the number of player
	 * @param mode the mode of the game
	 */
	public void setGameInfo(String name, Integer players, ModeType mode) {
		this.gamename = name;
		this.players = players;
		this.mode = mode;
	}
	
	/**
	 * Sets the style of the board
	 * @param style the style of the board
	 */
	public void setStyle(StyleType style) {
		this.style = style;
	}
}
