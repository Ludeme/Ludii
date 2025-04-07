package app.boardMaker.display.window;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import app.boardMaker.display.panels.boardPanel.BoardPanel;
import app.boardMaker.display.panels.paramPanel.ParamPanel;
import app.boardMaker.display.panels.pawnPanel.PawnPanel;
import app.boardMaker.display.panels.previewPanel.PreviewPanel;
import app.boardMaker.display.panels.welcomePanel.WelcomePanel;
import app.boardMaker.display.tabbedBar.BoardMakerTabbedBar;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;

/**
 * Main panel of the board maker
 */
public class BoardMakerPane extends JPanel
{
	
	private Displayer displayer;
	private Maker maker;
	
	//-----------------------------------------------------------
	
	public BoardMakerPane(Maker maker) {
		super(new BorderLayout());
		
		this.displayer = maker.getDisplayer();
		this.maker = maker;
		
		displayer.setBoardMakerPane(this);
		
		WelcomePanel welcomePanel = new WelcomePanel(maker);
		displayer.setWelcomePanel(welcomePanel);
		welcomePanel.visibility(true);

		
		add(welcomePanel,BorderLayout.CENTER);
		
		setOpaque(true);
	}
	
	public void createPanels() {
		BoardMakerTabbedBar tabbedBar = new BoardMakerTabbedBar(maker);
		displayer.setTabbedBar(tabbedBar);
				
		BoardPanel boardPanel = new BoardPanel(displayer);
		displayer.setBoardPanel(boardPanel);
		boardPanel.visibility(false);
				
		ParamPanel paramPanel = new ParamPanel(displayer);
		displayer.setParamPanel(paramPanel);
		paramPanel.visibility(false);
				
		PawnPanel pawnPanel = new PawnPanel(displayer);
		displayer.setPawnPanel(pawnPanel);
		pawnPanel.visibility(false);
				
		PreviewPanel previewPanel = new PreviewPanel(displayer);
		displayer.setPreviewPanel(previewPanel);
		previewPanel.visibility(false);
	}
}
