package app.boardMaker.display.panels.westPanel.library;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LibraryML extends MouseAdapter {
    private Maker maker;
    private Displayer displayer;

    private JTree tree;

    public LibraryML(Maker maker, JTree tree) {
        this.maker = maker;
        this.displayer = maker.getDisplayer();
        this.tree = tree;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 && !maker.setup()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node.isLeaf()) {
                String classname = ((LibraryBoardInfo) node.getUserObject()).getClassname();
                try {
                    if (classname.equals("CustomShapePanel") || classname.equals("CustomGraphPanel")) {
                        displayer.creationViewPoly();
                    } else {
                        displayer.creationView();
                    }
                    String path = "app.boardMaker.display.panels.paramPanel.tilings.";
                    JPanel panel = (JPanel) Class.forName(path + classname).getConstructor(Maker.class).newInstance(maker);
                    displayer.getParamPanel().setPanel(panel);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
