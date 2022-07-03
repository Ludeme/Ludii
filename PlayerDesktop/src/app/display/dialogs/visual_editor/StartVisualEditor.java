package app.display.dialogs.visual_editor;

import javax.swing.*;

import app.PlayerApp;
import app.display.dialogs.visual_editor.recs.codecompletion.controller.NGramController;
import app.display.dialogs.visual_editor.view.MainFrame;
import app.display.dialogs.visual_editor.view.VisualEditorFrame;
import app.display.dialogs.visual_editor.view.panels.editor.gameEditor.GameGraphPanel;

//-----------------------------------------------------------------------------

/**
 * Visual editor view.
 * @author cambolbro
 */
public class StartVisualEditor
{
	public static PlayerApp app;

	@SuppressWarnings("unused")
	//-------------------------------------------------------------------------

	public static void main(String[] args)
	{
		new StartVisualEditor(null);
	}

	public StartVisualEditor(final PlayerApp app)
	{

		StartVisualEditor.app = app;
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception ignored){}

		controller = new NGramController(5);
		//MainFrame f = new MainFrame(editPanel);
		VisualEditorFrame f = new VisualEditorFrame();
		f.requestFocus();
	}

	private static NGramController controller;


	public static NGramController controller() 
	{
		return controller;
	}

}
