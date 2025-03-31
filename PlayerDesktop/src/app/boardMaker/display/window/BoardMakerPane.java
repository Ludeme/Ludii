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

/**
 * Main panel of the board maker
 */
public class BoardMakerPane extends JPanel
{
	
	private Displayer displayer;
	
	
	
	//-----------------------------------------------------------
	
	public BoardMakerPane(Displayer displayer) {
		super(new BorderLayout());
		
		this.displayer = displayer;
		
		displayer.setBoardMakerPane(this);
		
		// Creation of all panels appearing on the main panel
		BoardMakerTabbedBar tabbedBar = new BoardMakerTabbedBar();
		displayer.setTabbedBar(tabbedBar);
		
		BoardPanel boardPanel = new BoardPanel(displayer);
		displayer.setBoardPanel(boardPanel);
		
		ParamPanel paramPanel = new ParamPanel(displayer);
		displayer.setParamPanel(paramPanel);
		
		PawnPanel pawnPanel = new PawnPanel(displayer);
		displayer.setPawnPanel(pawnPanel);
		
		PreviewPanel previewPanel = new PreviewPanel(displayer);
		displayer.setPreviewPanel(previewPanel);
		
		WelcomePanel welcomePanel = new WelcomePanel(displayer);
		displayer.setWelcomePanel(welcomePanel);
		
		add(welcomePanel,BorderLayout.CENTER);
		
		setOpaque(true);
	}
}
