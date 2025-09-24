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

			if (data.getBoard() != null) {
				// Need this to avoid a bug where redrawing the window makes the board smaller
				if (hasChanged) {
					maker.drawBoard(data);
					hasChanged = false;
				}
				BufferedImage image = SVGUtil.createSVGImage(data.getSVG(), getWidth(), getHeight());
				g2d.drawImage(image, 0, 0, null);

			}
		}
	}
}
