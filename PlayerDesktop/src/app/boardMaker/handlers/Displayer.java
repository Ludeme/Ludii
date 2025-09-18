package app.boardMaker.handlers;

import java.awt.BorderLayout;
import java.awt.Dimension;

import app.boardMaker.display.panels.westPanel.WestPanel;

import app.boardMaker.display.panels.boardPanel.BoardPanel;
import app.boardMaker.display.panels.paramPanel.ParamPanel;
import app.boardMaker.display.panels.pawnPanel.PawnPanel;
import app.boardMaker.display.panels.previewPanel.PreviewPanel;
import app.boardMaker.display.tabbedBar.BoardMakerTabbedBar;
import app.boardMaker.display.window.BoardMakerFrame;
import app.boardMaker.display.window.BoardMakerPane;

import javax.swing.*;

/**
 * Graphics handler of the desktop app
 */

public class Displayer
{
	private Maker maker;
	
	/** Main frame */
	private BoardMakerFrame frame;
	/** Parent pane of the board maker */
	private BoardMakerPane boardMakerPane;
	/** Main tabbed bar */
	private BoardMakerTabbedBar tabbedBar;
	/** Board display panel */
	private BoardPanel boardPanel;
	/** Board parameter panel */
	private ParamPanel paramPanel;
	/** Panel containing list of pawns */
	private PawnPanel pawnPanel;
	/** Panel containing the board preview */
	private PreviewPanel previewPanel;

	private WestPanel westPanel;
	
	public Displayer(Maker maker) {
		this.maker = maker;
	}
	
	public void createWindow() {
		frame = new BoardMakerFrame(maker);
		frame.requestFocus();
		setSizes();
	}
	
	/**
	 * Switches to the main view
	 */
	public void mainView() {
		boardMakerPane.invalidate();
		boardMakerPane.removeAll();

		previewPanel.visibility(false);
		paramPanel.visibility(false);

		boardMakerPane.add(tabbedBar,BorderLayout.NORTH);

		boardPanel.visibility(true);
		boardMakerPane.add(boardPanel,BorderLayout.CENTER);

		westPanel.visibility(true);
		boardMakerPane.add(westPanel,BorderLayout.WEST);

		boardMakerPane.revalidate();
		boardMakerPane.repaint();
	}
	
	/**
	 * Switches to the creation view
	 */
	public void creationView() {
		boardMakerPane.invalidate();
		boardMakerPane.removeAll();

		boardMakerPane.add(tabbedBar,BorderLayout.NORTH);

		boardPanel.visibility(false);
		westPanel.visibility(false);

		paramPanel.visibility(true);
		boardMakerPane.add(paramPanel,BorderLayout.WEST);

		previewPanel.visibility(true);
		boardMakerPane.add(previewPanel,BorderLayout.CENTER);

		boardMakerPane.revalidate();
		boardMakerPane.repaint();
	}

	/**
	 * Switches to the creation view for polygonal board shape
	 */
	public void creationViewPoly() {
		boardMakerPane.invalidate();
		boardMakerPane.removeAll();

		boardMakerPane.add(tabbedBar,BorderLayout.NORTH);

		boardPanel.visibility(false);
		westPanel.visibility(false);

		paramPanel.visibility(true);
		boardMakerPane.add(paramPanel,BorderLayout.WEST);

		previewPanel.visibility(true);
		boardMakerPane.add(previewPanel,BorderLayout.EAST);

		boardMakerPane.revalidate();
		boardMakerPane.repaint();
	}
	
	/**
	 * Sets the different sizes of the panels
	 */
	public void setSizes() {
		Dimension size = new Dimension(boardMakerPane.getWidth() / 2, boardMakerPane.getHeight());

		boardPanel.setPreferredSize(size);
		boardPanel.setMinimumSize(size);
		
		previewPanel.setPreferredSize(size);
		previewPanel.setMinimumSize(size);
		
		size = new Dimension(boardMakerPane.getWidth() / 4, boardMakerPane.getHeight());

		paramPanel.setPreferredSize(size);
		paramPanel.setMaximumSize(size);

		westPanel.setPreferredSize(size);
		westPanel.setMaximumSize(size);
	}
	
	//----------------------------------------------------------
	
	public BoardMakerFrame getFrame() {
		return frame;
	}
	
	public BoardMakerPane getBoardMakerPane() {
		return boardMakerPane;
	}
	
	public BoardMakerTabbedBar getTabbedBar() {
		return tabbedBar;
	}
	
	public BoardPanel getBoardPanel() {
		return boardPanel;
	}
	
	public ParamPanel getParamPanel() {
		return paramPanel;
	}
	
	public PawnPanel getPawnPanel() {
		return pawnPanel;
	}
	
	public PreviewPanel getPreviewPanel() {
		return previewPanel;
	}

	public WestPanel getBoardList() {
		return westPanel;
	}

	public JTabbedPane getCurrentDisplay() {
		if (previewPanel.visible()) {
			return previewPanel;
		} else {
			return boardPanel;
		}
	}
	
	//----------------------------------------------------------
	public void setBoardMakerPane(BoardMakerPane bmp) {
		boardMakerPane = bmp;
	}
	
	public void setTabbedBar(BoardMakerTabbedBar tb) {
		tabbedBar = tb;
	}
	
	public void setBoardPanel(BoardPanel bp) {
		boardPanel = bp;
	}
	
	public void setParamPanel(ParamPanel pp) {
		paramPanel = pp;
	}
	
	public void setPawnPanel(PawnPanel pp) {
		pawnPanel = pp;
	}
	
	public void setPreviewPanel(PreviewPanel pp) {
		previewPanel = pp;
	}

	public void setWestPanel(WestPanel bl) {
		westPanel = bl;
	}
}
