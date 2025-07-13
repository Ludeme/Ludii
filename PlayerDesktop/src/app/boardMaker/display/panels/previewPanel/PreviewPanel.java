package app.boardMaker.display.panels.previewPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import app.boardMaker.utils.BoardData;
import app.utils.SVGUtil;
import bridge.Bridge;
import game.Game;
import game.equipment.Equipment;
import game.equipment.Item;
import game.equipment.container.board.Board;
import game.equipment.container.board.custom.MancalaBoard;
import game.equipment.container.board.custom.SurakartaBoard;
import game.functions.graph.GraphFunction;
import game.mode.Mode;
import game.players.Players;
import game.types.board.SiteType;
import other.context.Context;
import other.trial.Trial;
import util.PlaneType;
import view.container.ContainerStyle;
import view.container.styles.BoardStyle;
import view.container.styles.board.MancalaStyle;
import view.container.styles.board.SurakartaStyle;
import view.container.styles.board.graph.GraphStyle;

/**
 * A class for the preview of the board during its creation
 */

public class PreviewPanel extends JTabbedPane
{
	private Displayer displayer;
	private Maker maker;

	private boolean visible;
    private boolean hasChanged;
	private double boardRatio = 1.0;

	private BoardData data;
	
	public PreviewPanel(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;

		data = new BoardData(maker);

		displayer.setPreviewPanel(this);

		addTab("Preview", new PreviewDrawSpace());
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
	
	public void setBoard(Board board) {
        hasChanged = true;
		data.setBoard(board);
	}

	public BoardData getBoardData() {
		return data;
	}

	public Board getBoard() {
		return data.getBoard();
	}

	public String getSVG() {
		return data.getSVG();
	}

	private void drawPreview(Graphics2D g2d, int width, int height) {
		Board board = data.getBoard();

		Game game = new Game(maker.getName(), new Players(maker.getPlayers()), new Mode(maker.getMode()), new Equipment(new Item[] {board}), null);
		game.create();

		game.setMetadata(null);

		Context context = new Context(game, new Trial(game));
		Bridge bridge = maker.getBridge();

		ContainerStyle gameStyle;

		if (board instanceof MancalaBoard) {
			gameStyle = new MancalaStyle(bridge,board);
			gameStyle.setPlacement(context,new Rectangle(0,0,width,height));
			gameStyle.render(PlaneType.BOARD,context);

			data.setOtherSVG(gameStyle.containerSVGImage());
		} else if (board instanceof SurakartaBoard) {
			gameStyle = new SurakartaStyle(bridge,board);
			gameStyle.setPlacement(context,new Rectangle(0,0,width,height));
			gameStyle.render(PlaneType.BOARD,context);

			data.setOtherSVG(gameStyle.containerSVGImage());
		} else {
			gameStyle = new BoardStyle(bridge,board);
			gameStyle.setPlacement(context,new Rectangle(0,0,width,height));
			gameStyle.render(PlaneType.BOARD,context);
			data.setCellSVG(gameStyle.containerSVGImage());

			gameStyle = new GraphStyle(bridge,board,context);
			gameStyle.setPlacement(context,new Rectangle(0,0,width,height));
			gameStyle.render(PlaneType.BOARD,context);
			data.setGraphSVG(gameStyle.containerSVGImage());
		}

		BufferedImage image = SVGUtil.createSVGImage(data.getSVG(), width, height);
		g2d.drawImage(image, 0,
				0, null);
	}

	class PreviewDrawSpace extends JPanel {
		public PreviewDrawSpace() {
			super(new BorderLayout());
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			Graphics2D g2d = (Graphics2D) g;

			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, getWidth(), getHeight());

			int boardSize = Math.min(getHeight(), (int)(getWidth() * boardRatio));

			if (data.getBoard() != null) {
				// Need this to avoid a bug where redrawing the window makes the board smaller
				if (hasChanged) {
					drawPreview(g2d,boardSize,boardSize);
					hasChanged = false;
				} else {
					BufferedImage image = SVGUtil.createSVGImage(data.getSVG(), getWidth(), getHeight());
					g2d.drawImage(image, 0,
							0, null);
				}
			}
		}
	}
}
