package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;

import javax.swing.*;

public class EditorSidebar extends JTabbedPane
{

    public EditorSidebar()
    {

        // adding layout settings tab
        JComponent layoutPanel = new LayoutSettingsPanel(Handler.editorPanel);
        addTab("Layout settings", layoutPanel);

        // add other tabs here...

    }

}
