package app.boardMaker.display.panels.previewPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import app.boardMaker.dataStruct.board.BoardData;
import app.utils.SVGUtil;
import game.equipment.container.board.Board;

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
				maker.drawBoard(data,this,null);

				BufferedImage image = SVGUtil.createSVGImage(data.getSVG(), getWidth(), getHeight());
				g2d.drawImage(image, 0, 0, null);

			}
		}
	}
}
