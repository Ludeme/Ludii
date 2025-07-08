package app.boardMaker.display.panels.library;

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
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node.isLeaf()) {
                System.out.println(node.getUserObject() instanceof BoardInfo);
                String classname = ((BoardInfo) node.getUserObject()).getClassname();
                try {
                    String path = "app.boardMaker.display.panels.paramPanel.tilings.";
                    JPanel panel = (JPanel) Class.forName(path + classname).getConstructor(Maker.class).newInstance(maker);
                    displayer.getParamPanel().setPanel(panel);
                    displayer.creationView();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
