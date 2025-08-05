package app.boardMaker.display.window;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import app.boardMaker.display.panels.westPanel.WestPanel;
import app.boardMaker.display.panels.boardPanel.BoardPanel;
import app.boardMaker.display.panels.paramPanel.ParamPanel;
import app.boardMaker.display.panels.previewPanel.PreviewPanel;
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

		createPanels();

		displayer.mainView();
		
		setOpaque(true);
	}
	
	public void createPanels() {
		BoardMakerTabbedBar tabbedBar = new BoardMakerTabbedBar(maker);
		tabbedBar.visibility(false);
				
		BoardPanel boardPanel = new BoardPanel(maker);
		boardPanel.visibility(false);
				
		ParamPanel paramPanel = new ParamPanel(displayer);
		paramPanel.visibility(false);
				
		PreviewPanel previewPanel = new PreviewPanel(maker);
		previewPanel.visibility(false);

		WestPanel westPanel = new WestPanel(maker);
		westPanel.visibility(false);
	}
}
