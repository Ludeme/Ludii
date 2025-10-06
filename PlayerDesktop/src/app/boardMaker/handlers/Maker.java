package app.boardMaker.handlers;

import javax.swing.*;

import app.boardMaker.display.panels.westPanel.boardList.BoardList;
import app.boardMaker.utils.BoardData;
import app.boardMaker.utils.BoardUtils;
import bridge.Bridge;
import game.Game;
import game.equipment.Equipment;
import game.equipment.Item;
import game.equipment.container.board.Board;
import game.equipment.container.board.custom.MancalaBoard;
import game.equipment.container.board.custom.SurakartaBoard;
import game.mode.Mode;
import game.players.Players;
import game.types.board.SiteType;
import game.types.play.ModeType;
import other.context.Context;
import other.trial.Trial;
import util.PlaneType;
import view.container.ContainerStyle;
import view.container.styles.BoardStyle;
import view.container.styles.board.MancalaStyle;
import view.container.styles.board.SurakartaStyle;
import view.container.styles.board.graph.GraphStyle;

import java.awt.*;

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
	private double boardratio = 1.0;

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

	/**
	 * Adds a newly created board
	 */
	public void addBoard(BoardData board) {
		currentBoard = board.copy();

		boardList.addBoard("board"+boardList.root().getChildCount());
		boardList.reload();
	}

	/**
	 * Switch which board is displayed
	 * @param data the data about the board to display
	 */
	public void switchBoard(BoardData data) {
		currentBoard = data;
		displayer.getBoardPanel().revalidate();
		displayer.getBoardPanel().repaint();
	}

	/**
	 * Draws the necessary image for the given board
	 * @param data the data about the board to draw
	 */
	public void drawBoard(BoardData data, Container view) {
		Board board = BoardUtils.copyBoard(data.getBoard(),this);

		Game game = new Game(gamename, new Players(players), new Mode(mode), new Equipment(new Item[] {board}), null);
		game.create();

		game.setMetadata(null);

		Context context = new Context(game, new Trial(game));
		Bridge bridge = getBridge();

		ContainerStyle gameStyle;

		int boardsize = Math.min(view.getHeight(),
				(int) (view.getWidth() * boardratio));

		Rectangle placement = new Rectangle(0,0,boardsize,boardsize);

		if (board instanceof MancalaBoard) {
			gameStyle = new MancalaStyle(bridge,board);
			gameStyle.setPlacement(context,placement);
			gameStyle.render(PlaneType.BOARD,context);

			data.setOtherSVG(gameStyle.containerSVGImage());
		} else if (board instanceof SurakartaBoard) {
			gameStyle = new SurakartaStyle(bridge,board);
			gameStyle.setPlacement(context,placement);
			gameStyle.render(PlaneType.BOARD,context);

			data.setOtherSVG(gameStyle.containerSVGImage());
		} else {
			if (siteType == SiteType.Cell) {
				gameStyle = new BoardStyle(bridge,board);
				gameStyle.setPlacement(context,placement);
				gameStyle.render(PlaneType.BOARD,context);
				data.setBoardSVG(gameStyle.containerSVGImage());
			} else {
				gameStyle = new GraphStyle(bridge,board,context);
				gameStyle.setPlacement(context,placement);
				gameStyle.render(PlaneType.BOARD,context);
				data.setBoardSVG(gameStyle.containerSVGImage());
			}
		}

		data.setPlacement(placement);
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
