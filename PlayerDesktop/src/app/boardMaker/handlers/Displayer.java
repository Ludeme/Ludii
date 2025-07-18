package app.boardMaker.handlers;

import java.awt.BorderLayout;
import java.awt.Dimension;

import app.boardMaker.display.panels.library.LibraryPanel;

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

	private LibraryPanel boardlist;
	
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
		if (previewPanel.visible()) {
			previewPanel.visibility(false);
			boardMakerPane.remove(previewPanel);
		}
		if (paramPanel.visible()) {
			paramPanel.visibility(false);
			boardMakerPane.remove(paramPanel);
		}
		
		boardMakerPane.add(tabbedBar,BorderLayout.NORTH);

		if (!boardPanel.visible()) {
			boardMakerPane.add(boardPanel,BorderLayout.CENTER);
			boardPanel.visibility(true);
		}
		if (!boardlist.visible()) {
			boardMakerPane.add(boardlist,BorderLayout.WEST);
			boardlist.visibility(true);
		}

		boardMakerPane.revalidate();
		boardMakerPane.repaint();
	}
	
	/**
	 * Switches to the creation view
	 */
	public void creationView() {
		if (boardPanel.visible()) {
			boardPanel.visibility(false);
			boardMakerPane.remove(boardPanel);
		}
		if (boardlist.visible()) {
			boardlist.visibility(false);
			boardMakerPane.remove(boardlist);
		}
		if (!paramPanel.visible()) {
			boardMakerPane.add(paramPanel,BorderLayout.WEST);
			paramPanel.visibility(true);
		}
		
		if (!previewPanel.visible()) {
			boardMakerPane.add(previewPanel,BorderLayout.CENTER);
			previewPanel.visibility(true);
		}

		boardMakerPane.revalidate();
		boardMakerPane.repaint();
	}
	
	/**
	 * Sets the different sizes of the panels
	 */
	public void setSizes() {
		Dimension size = new Dimension(boardMakerPane.getWidth() / 2, boardMakerPane.getHeight());
		System.out.println(size);
		boardPanel.setPreferredSize(size);
		boardPanel.setMinimumSize(size);
		
		previewPanel.setPreferredSize(size);
		previewPanel.setMinimumSize(size);
		
		size = new Dimension(boardMakerPane.getWidth() / 4, boardMakerPane.getHeight());
		System.out.println(size);
		paramPanel.setPreferredSize(size);
		paramPanel.setMaximumSize(size);

		boardlist.setPreferredSize(size);
		boardlist.setMaximumSize(size);
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

	public LibraryPanel getBoardList() {
		return boardlist;
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

	public void setBoardList(LibraryPanel bl) {
		boardlist = bl;
	}
}
