package app.boardMaker.handlers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.mail.internet.NewsAddress;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import app.boardMaker.display.panels.boardPanel.BoardPanel;
import app.boardMaker.display.panels.paramPanel.ParamPanel;
import app.boardMaker.display.panels.pawnPanel.PawnPanel;
import app.boardMaker.display.panels.previewPanel.PreviewPanel;
import app.boardMaker.display.panels.welcomePanel.WelcomePanel;
import app.boardMaker.display.tabbedBar.BoardMakerTabbedBar;
import app.boardMaker.display.window.BoardMakerFrame;
import app.boardMaker.display.window.BoardMakerPane;

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
	/** Welcome panel */
	private WelcomePanel welcomePanel;
	
	public Displayer(Maker maker) {
		this.maker = maker;
	}
	
	public void createWindow() {
		frame = new BoardMakerFrame(maker);
		frame.requestFocus();
	}
	
	/**
	 * Switches to the main view with the boardpanel, toolbar and (possibly) pawn list visible
	 */
	public void mainView() {
		if (welcomePanel.visible()) {
			welcomePanel.visibility(false);
			boardMakerPane.remove(welcomePanel);
		}
		if (previewPanel.visible()) {
			previewPanel.visibility(false);
			boardMakerPane.remove(previewPanel);
		}
		if (paramPanel.visible()) {
			paramPanel.visibility(false);
			boardMakerPane.remove(paramPanel);
		}
		
		boardMakerPane.add(tabbedBar,BorderLayout.NORTH);
		
		boardMakerPane.add(boardPanel,BorderLayout.CENTER);
		boardPanel.visibility(true);
		
		if (boardPanel.hasBoard()) {
			boardMakerPane.add(pawnPanel,BorderLayout.WEST);
			pawnPanel.visibility(true);
		}
		
		boardMakerPane.revalidate();
		boardMakerPane.repaint();
	}
	
	/**
	 * Switches to the creation view with the previewpanel and parampanel visible
	 */
	public void creationView() {
		if (boardPanel.visible()) {
			boardPanel.visibility(false);
			boardMakerPane.remove(boardPanel);
		}
		if (boardPanel.hasBoard() || pawnPanel.visible()) {
			pawnPanel.visibility(false);
			boardMakerPane.remove(pawnPanel);
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
		
		boardPanel.setPreferredSize(size);
		boardPanel.setMinimumSize(size);
		
		previewPanel.setPreferredSize(size);
		previewPanel.setMinimumSize(size);
		
		size = new Dimension(boardMakerPane.getWidth() / 4, boardMakerPane.getHeight());
		
		paramPanel.setPreferredSize(size);
		paramPanel.setMaximumSize(size);
		
		pawnPanel.setPreferredSize(size);
		pawnPanel.setMaximumSize(size);
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
	
	public WelcomePanel getWelcomePanel() {
		return welcomePanel;
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
	
	public void setWelcomePanel(WelcomePanel wp) {
		welcomePanel = wp;
	}
}
