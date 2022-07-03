package app.display.dialogs.visual_editor;

import javax.swing.*;

import app.PlayerApp;
import app.display.dialogs.visual_editor.recs.codecompletion.controller.NGramController;
import app.display.dialogs.visual_editor.view.MainFrame;
import app.display.dialogs.visual_editor.view.panels.editor.gameEditor.EditorPanel;

//-----------------------------------------------------------------------------

/**
 * Visual editor view.
 * @author cambolbro
 */
public class VisualEditorPanel
{
	public static PlayerApp app;

	@SuppressWarnings("unused")
	//-------------------------------------------------------------------------

	public static void main(String[] args)
	{
		new VisualEditorPanel(null);
	}

	public VisualEditorPanel(final PlayerApp app)
	{

		VisualEditorPanel.app = app;
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception ignored){}

		controller = new NGramController(5);
		editPanel = new EditorPanel(10000,10000);
		MainFrame f = new MainFrame(editPanel);
		f.requestFocus();
	}

	private static EditorPanel editPanel;
	private static NGramController controller;


	public static NGramController controller() 
	{
		return controller;
	}

}
