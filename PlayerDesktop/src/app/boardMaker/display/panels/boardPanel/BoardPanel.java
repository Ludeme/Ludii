package app.boardMaker.display.panels.boardPanel;

import javax.swing.JTabbedPane;

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

	private BoardDrawSpace view;

	public BoardPanel(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;

		displayer.setBoardPanel(this);

		view = new BoardDrawSpace(maker);
		addTab("View",view);
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}

	public void setCreatePoly(boolean b) {
		view.setCreatePoly(b);
	}

	public void setTransformation(Transformations transformations) {
		view.setTransformation(transformations);
	}

	public void newPolygon() {
		view.newPolygon();
	}

	public void setRemove(boolean b) {
		view.setRemove(b);
	}

	public void setRemoveType(SiteType siteType) {
		view.setRemoveType(siteType);
	}

	public void clearRemovedIndices() {
		view.clearRemoving();
	}

    public void setAdding(boolean b) {
		view.setAdding(b);
    }

	public void setAddType(SiteType siteType) {
		view.setAddType(siteType);
	}

	public void clearAdd() {
		view.clearAdd();
	}

}
