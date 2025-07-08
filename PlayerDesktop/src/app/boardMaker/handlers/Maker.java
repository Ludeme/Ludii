package app.boardMaker.handlers;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.SwingUtilities;

import app.boardMaker.res.StyleType;
import app.utils.SVGUtil;
import bridge.Bridge;
import game.Game;
import game.equipment.Equipment;
import game.equipment.Item;
import game.equipment.container.board.Board;
import game.functions.graph.GraphFunction;
import game.mode.Mode;
import game.players.Players;
import game.types.play.ModeType;
import other.context.Context;
import other.trial.Trial;
import util.PlaneType;
import view.container.styles.BoardStyle;
import view.container.styles.board.MancalaStyle;
import view.container.styles.board.SurakartaStyle;
import view.container.styles.board.graph.GraphStyle;

/**
 * General handler of the board maker app.
 */

public class Maker
{
	private Displayer displayer;
	
	private String gamename;
	private int players;
	private ModeType mode;
	
	// Styletype selected in the game tab of the tabbed bar
	private StyleType selectedStyle = StyleType.BoardStyle;
	private boolean isMancala = false;
	private boolean isSurakarta = false;
	private boolean largeStack = false;
	
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
	
	public void drawBoard(Graphics2D g2d, GraphFunction graph, Board board,int width, int height) {
		Board gameBoard;
		StyleType gameStyle;
		if (isMancala) {
			gameBoard = board;
			gameStyle = StyleType.MancalaStyle;
		} else if (isSurakarta) {
			gameBoard = board;
			gameStyle = StyleType.SurakartaStyle;
		} else {
			gameBoard = new Board(graph, null, null, null, null, null, null);
			gameStyle = selectedStyle;
		}
		
		Game game = new Game(gamename, new Players(players), new Mode(mode), new Equipment(new Item[] {gameBoard}), null);
		game.create();
		game.setMetadata(null);
		
		Context context = new Context(game, new Trial(game));
		Bridge bridge = new Bridge();
		
		String svg;
		switch (gameStyle)
		{
		case BoardStyle:
			BoardStyle style = new BoardStyle(bridge, gameBoard);
			style.setPlacement(context, new Rectangle(0, 0, width, height));
			style.render(PlaneType.BOARD, context);
			
			svg = style.containerSVGImage();
			break;
		case GraphStyle:
			GraphStyle gstyle = new GraphStyle(bridge, gameBoard, context);
			gstyle.setPlacement(context, new Rectangle(0, 0, width, height));
			gstyle.render(PlaneType.BOARD, context);
			
			svg = gstyle.containerSVGImage();
			break;
		case MancalaStyle:
			MancalaStyle mstyle = new MancalaStyle(bridge, gameBoard);
			mstyle.setPlacement(context, new Rectangle(0, 0, width, height));
			mstyle.render(PlaneType.BOARD, context);
			
			svg = mstyle.containerSVGImage();
			break;
		case SurakartaStyle:
			SurakartaStyle sstyle = new SurakartaStyle(bridge, gameBoard);
			sstyle.setPlacement(context, new Rectangle(0, 0, width, height));
			sstyle.render(PlaneType.BOARD, context);
			
			svg = sstyle.containerSVGImage();
			break;
		default:
			svg = "";
			break;
		}
		
		if (svg == null || svg.equals("")) {
			return;
		}
						
		displayer.getPreviewPanel().setSVG(svg);
		displayer.getBoardPanel().setSVG(svg);
		
		g2d.drawImage(SVGUtil.createSVGImage(svg, width, height), 0, 0, null);
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
	
	public boolean largeStack() {
		return largeStack;
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
	 * @param style the style of the board
	 */
	public void setStyle(StyleType style) {
		this.selectedStyle = style;
	}
	
	/**
	 * Sets the large stack parameter of the board
	 * @param b whether to allow large stacks or not
	 */
	public void setStack(boolean b) {
		largeStack = b;
	}
	
	/**
	 * Defines if the board is a mancala board
	 * @param b
	 */
	public void setMancala(boolean b) {
		isMancala = b;
	}
	
	/**
	 * Defines if the board is a surakarta board
	 * @param b
	 */
	public void setSurakarta(boolean b) {
		isSurakarta = b;
	}
}
