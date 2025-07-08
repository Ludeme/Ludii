package app.boardMaker.display.panels.previewPanel;

import java.awt.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import app.boardMaker.display.panels.boardPanel.BoardDisplay;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import app.utils.SVGUtil;
import game.equipment.container.board.Board;
import game.functions.graph.GraphFunction;

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

	private GraphFunction graph;
	private Board board;
	private String svg;
	
	public PreviewPanel(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;

		addTab("Preview", new PreviewDrawSpace());
		displayer.setPreviewPanel(this);
	}
	
	public void switchStyle() {
		hasChanged = true;
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
	
	public void setBoard(GraphFunction graph) {
        hasChanged = true;
		this.graph = graph;
	}
	
	public void setBoard(Board board) {
        hasChanged = true;
		this.board = board;
	}
	
	public void setSVG(String svg) {
		this.svg = svg;
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

			if (graph != null || board != null) {
				// Need this to avoid a bug where redrawing the window makes the board smaller
				if (hasChanged) {
					maker.drawBoard(g2d,graph,board,boardSize,boardSize);
					hasChanged = false;
				} else {
					g2d.drawImage(SVGUtil.createSVGImage(svg, getWidth(), getHeight()), 0, 0, null);
				}
			}
		}
	}
}
