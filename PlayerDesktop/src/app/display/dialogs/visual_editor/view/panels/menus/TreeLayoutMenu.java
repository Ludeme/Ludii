package app.display.dialogs.visual_editor.view.panels.menus;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.panels.editor.EditorSidebar;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;

import javax.swing.*;

public class TreeLayoutMenu extends JMenu
{
    /**
     * Constructor
     */
    public TreeLayoutMenu(EditorMenuBar menuBar)
    {
        super("Layout");

        menuBar.addJMenuItem(this, "Open Layout Settings", e -> {
            EditorSidebar.getEditorSidebar().setVisible(true);
            EditorSidebar.getEditorSidebar().setLayoutTabSelected();
        });

        add(new JSeparator());

        menuBar.addJCheckBoxMenuItem(this, "Animation", Handler.animation, e -> {
            LayoutSettingsPanel.getLayoutSettingsPanel().animatePlacement().setSelected(((JCheckBoxMenuItem) e.getSource()).isSelected());
            Handler.animation = ((JCheckBoxMenuItem) e.getSource()).isSelected();
        });
        menuBar.addJCheckBoxMenuItem(this, "Auto Placement", Handler.autoplacement, e -> {
            LayoutSettingsPanel.getLayoutSettingsPanel().autoPlacement().setSelected(((JCheckBoxMenuItem) e.getSource()).isSelected());
            Handler.autoplacement = ((JCheckBoxMenuItem) e.getSource()).isSelected();
        });

        add(new JSeparator());

        menuBar.addJMenuItem(this, "Help", null);

    }


}
