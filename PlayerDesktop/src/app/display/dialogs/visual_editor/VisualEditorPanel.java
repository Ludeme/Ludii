package app.display.dialogs.visual_editor;

import javax.swing.*;

import app.PlayerApp;
import app.display.dialogs.visual_editor.recs.codecompletion.controller.NGramController;
import app.display.dialogs.visual_editor.view.MainFrame;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;

//-----------------------------------------------------------------------------

/**
 * Visual editor view.
 * @author cambolbro
 */
public class VisualEditorPanel
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private final PlayerApp app;
	
	//-------------------------------------------------------------------------
	
	public VisualEditorPanel(final PlayerApp app)
	{
		this.app = app;
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignored){

		}

		controller = new NGramController(7);
		editPanel = new EditorPanel(5000,5000);
		MainFrame f = new MainFrame(editPanel);
	}

	private static EditorPanel editPanel;
	private static NGramController controller;


	public static NGramController controller() {
		return controller;
	}

}
