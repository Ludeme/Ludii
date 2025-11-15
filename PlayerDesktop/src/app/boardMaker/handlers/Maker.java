package app.boardMaker.handlers;

import javax.swing.*;

import app.DesktopApp;
import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.state.BoardMakerState;
import app.boardMaker.display.panels.westPanel.itemList.ItemList;
import app.boardMaker.dataStruct.board.BoardData;
import app.boardMaker.utils.BoardUtils;
import app.boardMaker.utils.Camera;
import app.loading.GameLoading;
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
import java.awt.geom.Point2D;

/**
 * General handler of the board maker app.
 */

public class Maker
{
	private Displayer displayer;
	private Bridge bridge;
	private DesktopApp mainApp;

	private String gamename = "";
	private String filename;
	private int players = 2;
	private ModeType mode = ModeType.Alternating;
	private SiteType siteType = SiteType.Cell;
	private SiteType shownSite = SiteType.Cell;
	private boolean largeStack = false;

	private double boardratio = 1.0;
	private double boardScale;
	private int cellRadius;

	private BoardData currentBoard;
	private BoardMakerState state;

	private ItemList itemList;
	
	public Maker(DesktopApp app) {
		displayer = new Displayer(this);
		bridge = new Bridge();
		this.mainApp = app;

		state = new BoardMakerState();
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
	public void setBoard(ContainerInfo board) {
		if (state.currentBoardInfo().board() == null) {
			itemList.addBoard(board);
			itemList.reload(itemList.boardRoot());
		} else {
			itemList.updateBoard(board);
		}
		state.setCurrentBoard(board);
	}

	/**
	 * Switch which board is displayed
	 * @param idx the index of the selected board
	 */
	public void switchBoard(int idx) {
		state.setCurrentIdx(idx);
		displayer.getBoardPanel().revalidate();
		displayer.getBoardPanel().repaint();
	}

	/**
	 * Draws the necessary image for the given board
	 * @param data the data about the board to draw
	 */
	public void drawBoard(BoardData data, Container view, Camera camera) {
		Board board = BoardUtils.copyBoard(data.getBoard(),this);
		data.setBoard(board);

		Game game = new Game(gamename, new Players(players), new Mode(mode), new Equipment(new Item[] {board}), null);
		game.create();

		game.setMetadata(null);

		Context context = new Context(game, new Trial(game));
		data.setContext(context);
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
			if ((!setup() && shownSite == SiteType.Cell) || (setup() && siteType == SiteType.Cell)) {
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
		cellRadius = gameStyle.cellRadiusPixels();
		boardScale = gameStyle.containerScale();
		Point2D center = new Point2D.Double(0.5,0.5);
		Point origin = new Point(0,0);
		if (camera != null) {
			origin.x = camera.offX();
			origin.y = -camera.offY();
		}

		Rectangle realPlacement = new Rectangle((int) (origin.getX() + placement.getWidth() * (1.0 - boardScale) * center.getX()),
						(int) (origin.getY() + placement.getHeight() * (1.0 - boardScale) * center.getY()),
						(int) (placement.getWidth() * boardScale),
						(int) (placement.getHeight() * boardScale));
		data.setPlacement(realPlacement);
	}

	//--------------------------------------------------------------------------------

	/**
	 * Gets the Displayer
	 * @return the displayer
	 */
	public Displayer getDisplayer() {
		return displayer;
	}

	public BoardMakerState state() {
		return state;
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

	public ItemList getItemList() {
		return itemList;
	}

	public SiteType shownSite() {
		return shownSite;
	}

	public int cellradius() {
		return cellRadius;
	}
	//--------------------------------------------------------------------------------
	
	/**
	 * Sets basic informations about the game
	 * @param name the name of the game
	 * @param players the number of player
	 * @param mode the mode of the game
	 */
	public void setGameInfo(String name, Integer players, ModeType mode, SiteType site) {
		this.gamename = name;
		filename = gamename + ".lud";
		this.players = players;
		this.mode = mode;
		this.siteType = site;
		this.shownSite = site;
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

	public void setBoardList(ItemList bl) {
		itemList = bl;
	}

	public void showSite(SiteType site) {
		this.shownSite = site;
	}

	public boolean setup() {
		return displayer.getBoardPanel().setup();
	}

	public double boardScale() {
		return boardScale;
	}

	public void writeAndPlay() {
		writeDescription();
		playGame();
	}

	private void playGame() {
		GameLoading.loadGameFromFilePath(mainApp,filename);
		displayer.getFrame().dispose();
	}

	private void writeDescription() {
		Writer.write(filename,this);
	}
}
