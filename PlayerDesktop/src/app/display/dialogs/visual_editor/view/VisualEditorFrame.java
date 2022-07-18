package app.display.dialogs.visual_editor.view;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import app.display.dialogs.visual_editor.view.panels.menus.EditorMenuBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class VisualEditorFrame extends JFrame
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 8610284030834361704L;
	// Frame Properties
    private static final String TITLE = "Ludii Visual Editor";
    private static final Dimension DEFAULT_FRAME_SIZE = DesignPalette.DEFAULT_FRAME_SIZE;
    private static final ImageIcon FRAME_ICON = DesignPalette.LUDII_ICON;


    public VisualEditorFrame()
    {
        Handler.visualEditorFrame = this;
        // set frame properties
        setTitle(TITLE);
        setIconImage(FRAME_ICON.getImage());
        setSize(DEFAULT_FRAME_SIZE);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        VisualEditorPanel panel = new VisualEditorPanel();
        add(panel);

        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                // [UNCOMMENT FILIP]
                //DocHandler.getInstance().close();
                //StartVisualEditor.controller().close();
                super.windowClosing(e);
            }
        });

        EditorMenuBar menuBar = new EditorMenuBar();
        setJMenuBar(menuBar);
        setVisible(true);
    }
}
