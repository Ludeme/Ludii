package app.boardMaker.handlers;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import app.boardMaker.display.panels.westPanel.boardList.BoardList;
import app.boardMaker.display.panels.westPanel.boardList.BoardListTreeNode;
import app.boardMaker.utils.BoardData;
import bridge.Bridge;
import game.types.board.SiteType;
import game.types.play.ModeType;

/**
 * General handler of the board maker app.
 */

public class Maker
{
	private Displayer displayer;
	private Bridge bridge;

	private String gamename = "";
	private int players = 2;
	private ModeType mode = ModeType.Alternating;
	private SiteType siteType = SiteType.Cell;
	private boolean largeStack = false;

	private BoardData currentBoard;

	private BoardList boardList;
	
	public Maker() {
		displayer = new Displayer(this);
		bridge = new Bridge();
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

	public void addBoard() {
		currentBoard = displayer.getPreviewPanel().getBoardData().copy();

		DefaultMutableTreeNode root = boardList.root();
		root.add(new BoardListTreeNode("board"+root.getChildCount(), currentBoard));
		boardList.reload();
	}

	public void switchBoard(BoardData data) {
		currentBoard = data;
		displayer.getBoardPanel().revalidate();
		displayer.getBoardPanel().repaint();
	}

	//--------------------------------------------------------------------------------

	/**
	 * Gets the Displayer
	 * @return the displayer
	 */
	public Displayer getDisplayer() {
		return displayer;
	}

	public BoardData getCurrentBoard() {
		return currentBoard;
	}

	public Bridge getBridge() {
		return bridge;
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

	public SiteType getSiteType() {
		return siteType;
	}
	
	public boolean largeStack() {
		return largeStack;
	}

	public BoardList getBoardList() {
		return boardList;
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
	 * Sets the selected style of the board
	 * @param type the style of the board
	 */
	public void setSiteType(SiteType type) {
		this.siteType = type;
	}
	
	/**
	 * Sets the large stack parameter of the board
	 * @param b whether to allow large stacks or not
	 */
	public void setStack(boolean b) {
		largeStack = b;
	}

	public void setBoardList(BoardList bl) {
		boardList = bl;
	}
}
