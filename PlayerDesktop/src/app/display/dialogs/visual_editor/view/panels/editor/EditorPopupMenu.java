package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.GraphPanel;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EditorPopupMenu extends JPopupMenu {

    public EditorPopupMenu(IGraphPanel graphPanel) {
        JMenuItem newLudeme = new JMenuItem("New Ludeme");
        JMenuItem duplicateScreen = new JMenuItem("Duplicate Screen");
        JMenuItem repaintScreen = new JMenuItem("Repaint");

        JMenu lmMenu = new JMenu("Arrange Graph");
        JMenuItem compact = new JMenuItem("Compact hierarchy");
        JMenuItem radial = new JMenuItem("Radial layout");
        JMenuItem fdp = new JMenuItem("FDP layout");
        JMenuItem cfdp = new JMenuItem("CFDP layout");

        newLudeme.addActionListener(e -> {
            graphPanel.showAllAvailableLudemes(getX(), getY());
        });

        duplicateScreen.addActionListener(e -> {
            JFrame frame = new JFrame("Define Editor");
            EditorPanel editorPanel = new EditorPanel(5000,5000);
            frame.setContentPane(editorPanel);
            editorPanel.drawGraph(graphPanel.getGraph());
            frame.setVisible(true);
            frame.setPreferredSize(frame.getPreferredSize());
            frame.setSize(1200,800);
        });

        compact.addActionListener(e -> {
            LayoutHandler lm = graphPanel.getLayoutHandler();
            lm.setLayoutMethod(1);
            lm.executeLayout();

            graphPanel.drawGraph(graphPanel.getGraph());
        });

        radial.addActionListener(e -> {
            LayoutHandler lm = graphPanel.getLayoutHandler();
            lm.setLayoutMethod(2);
            lm.executeLayout();
            graphPanel.drawGraph(graphPanel.getGraph());
        });

        fdp.addActionListener(e -> {
            int fdp_delay = 20;
            LayoutHandler lm = graphPanel.getLayoutHandler();
            lm.setLayoutMethod(0);
            Timer timer = new Timer(fdp_delay, new LayoutUpdate(graphPanel));
            lm.setFDPTimer(timer);
            timer.start();
        });

        cfdp.addActionListener(e -> {
            LayoutHandler lm = graphPanel.getLayoutHandler();
            lm.setLayoutMethod(3);
            lm.executeLayout();
            graphPanel.drawGraph(graphPanel.getGraph());
        });

        lmMenu.add(compact);
        lmMenu.add(radial);
        lmMenu.add(fdp);
        lmMenu.add(cfdp);

        repaintScreen.addActionListener(e -> {
            graphPanel.repaint();
                });

        add(newLudeme);
        add(lmMenu);
        add(duplicateScreen);
        add(repaintScreen);
    }

    private static class LayoutUpdate implements ActionListener
    {

        private final IGraphPanel graphPanel;

        public LayoutUpdate(IGraphPanel graphPanel)
        {
            this.graphPanel = graphPanel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            graphPanel.getLayoutHandler().executeLayout();
            //graphPanel.drawGraph(graphPanel.getGraph());
            graphPanel.updateGraph();
        }
    }

}
