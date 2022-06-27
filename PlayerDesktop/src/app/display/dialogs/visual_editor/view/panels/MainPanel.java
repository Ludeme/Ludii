package app.display.dialogs.visual_editor.view.panels;

import app.display.dialogs.visual_editor.LayoutManagement.Vector2D;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;
import app.display.dialogs.visual_editor.view.panels.header.HeaderPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorSidebar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

        setFocusable(true);
        // key listener check if ctrl is pressed/released
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == 17)
                {
                    LudemeNodeComponent.cltrPressed = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (e.getKeyCode() == 17)
                {
                    LudemeNodeComponent.cltrPressed = false;
                }
            }
        });

        setKeyBinding(editor_panel);

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

    private void setKeyBinding(IGraphPanel graphPanel)
    {
        JPanel panel = graphPanel.panel();


        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control Z"), "undo");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control Y"), "redo");
        //panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control S"), "save");
        //panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control O"), "open");
        //panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control N"), "new");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control C"), "copy");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control V"), "paste");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0), "paste");
        //panel.getInputMap().put(KeyStroke.getKeyStroke("control x"), "cut");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control A"), "selectAll");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control D"), "delete");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control H"), "documentation");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control I"), "info");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control L"), "layout");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control W"), "collapse");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control E"), "expand");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control R"), "run");
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control shift D"), "duplicate");



        panel.getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Handler.undo();
            }
        });

        panel.getActionMap().put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Handler.redo();
            }
        });

        panel.getActionMap().put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Handler.copy(graphPanel.graph());
            }
        });

        panel.getActionMap().put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Handler.paste(graphPanel.graph(), -1, -1);
            }
        });

        panel.getActionMap().put("collapse", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Handler.collapse(graphPanel.graph());
            }
        });

        panel.getActionMap().put("expand", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Handler.expand(graphPanel.graph());
            }
        });

        panel.getActionMap().put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Handler.remove(graphPanel.graph());
            }
        });

        panel.getActionMap().put("selectAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Handler.selectAll();
            }
        });

        panel.getActionMap().put("duplicate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Handler.duplicate(graphPanel.graph());
            }
        });

    }

    public Dimension getViewPort()
    {
        return panel.getViewport().getSize();
    }

}
