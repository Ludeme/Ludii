package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;

import javax.swing.*;

/**
 * Sidebar panel of visual editor
 * @author nic0gin
 */
public class EditorSidebar extends JTabbedPane
{
    private static EditorSidebar editorSidebar;

    /**
     * Constructor
     */
    private EditorSidebar()
    {
        setVisible(Handler.sidebarVisible);

        // adding layout settings tab
        LayoutSettingsPanel layoutPanel = LayoutSettingsPanel.getLayoutSettingsPanel();
        Handler.lsPanel = layoutPanel;
        addTab("Layout settings", layoutPanel);
        setSelectedComponent(layoutPanel);

        // add other tabs here...

        // invisible by default
        setVisible(false);
    }

    /**
     * Get a single instance of the editor sidebar
     */
    public static EditorSidebar getEditorSidebar()
    {
        if (editorSidebar == null) editorSidebar = new EditorSidebar();
        return editorSidebar;
    }

    public void setSidebarVisible(boolean visible)
    {
        editorSidebar.setVisible(visible);
        editorSidebar.repaint();
    }

    public void setLayoutTabSelected() {
        int LAYOUT_TAB_INDEX = 0;
        editorSidebar.setSelectedIndex(LAYOUT_TAB_INDEX);}

}
