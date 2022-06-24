package app.display.dialogs.visual_editor.view.panels;

import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;
import app.display.dialogs.visual_editor.view.panels.header.HeaderPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorSidebar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainPanel extends JPanel {

    //JPanel editor_panel = new EditorPanel(5000, 5000);
    private JScrollPane panel;
    private EditorPanel editor_panel;

    public MainPanel(EditorPanel editor_panel){
        setLayout(new BorderLayout());

        add(new HeaderPanel(), BorderLayout.NORTH);
        this.editor_panel = editor_panel;
        panel = new JScrollPane(editor_panel);
        //panel.getVerticalScrollBar().setValue(editor_panel.getHeight()/2);
        //panel.getHorizontalScrollBar().setValue(editor_panel.getWidth()/2);

        JPanel splitPanel = new JPanel();
        splitPanel.setLayout(new BorderLayout());
        splitPanel.add(panel, BorderLayout.CENTER);
        splitPanel.add(new EditorSidebar(), BorderLayout.EAST);
        add(splitPanel, BorderLayout.CENTER);

        MouseAdapter ma = new MouseAdapter() {

            private Point origin;

            @Override
            public void mousePressed(MouseEvent e) {
                origin = new Point(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (origin != null && !editor_panel.isSELECTION_MODE()) {
                    JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, editor_panel);
                    if (viewPort != null) {
                        int deltaX = origin.x - e.getX();
                        int deltaY = origin.y - e.getY();

                        Rectangle view = viewPort.getViewRect();
                        view.x += deltaX;
                        view.y += deltaY;

                        editor_panel.scrollRectToVisible(view);
                    }
                }
            }

        };



        editor_panel.addMouseListener(ma);
        editor_panel.addMouseMotionListener(ma);

        //add(options_panel, BorderLayout.EAST);
    }

    public JScrollPane getPanel() {
        return panel;
    }

    public void setView(int x, int y) {
        JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, editor_panel);
        if (viewPort != null) {

            Rectangle view = viewPort.getViewRect();
            view.x = x;
            view.y = y;

            editor_panel.scrollRectToVisible(view);
        }
    }
}
