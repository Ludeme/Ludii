package app.boardMaker.display.panels.boardPanel;

import javax.swing.JTabbedPane;

import app.boardMaker.display.panels.pieceView.PieceView;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import app.boardMaker.res.Transformations;
import game.types.board.SiteType;

/**
 * Class representing the panel where the board is displayed
 */

public class BoardPanel extends JTabbedPane
{
	private Displayer displayer;
	private Maker maker;

	private boolean visible;
	private boolean setup = false;

	private BoardDrawSpace boardView;
	private PieceView pieceView;

	public BoardPanel(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;

		displayer.setBoardPanel(this);

		boardView = new BoardDrawSpace(maker);
		pieceView = new PieceView(maker);

		addTab("View", boardView);
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}

	public void setCreatePoly(boolean b) {
		boardView.setCreatePoly(b);
	}

	public void setTransformation(Transformations transformations) {
		boardView.setTransformation(transformations);
	}

	public void newPolygon() {
		boardView.newPolygon();
	}

	public void setRemove(boolean b) {
		boardView.setRemove(b);
	}

	public void setRemoveType(SiteType siteType) {
		boardView.setRemoveType(siteType);
	}

	public void clearRemovedIndices() {
		boardView.clearRemoving();
	}

    public void setAdding(boolean b) {
		boardView.setAdding(b);
    }

	public void setAddType(SiteType siteType) {
		boardView.setAddType(siteType);
	}

	public void clearAdd() {
		boardView.clearAdd();
	}

	public void showBoard() {
		setComponentAt(0,boardView);
		setup = false;

		revalidate();
		repaint();
	}

	public void showSetup() {
		setComponentAt(0,pieceView);
		setup = true;

		revalidate();
		repaint();
	}

	public boolean setup() {
		return setup;
	}

    public int dotSize() {
		return boardView.dotSize;
    }
}
