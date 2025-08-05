package app.boardMaker.display.panels.westPanel.library;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class Library extends JPanel {
    private Maker maker;
    private Displayer displayer;

    private JTree library;
    private JScrollPane treeView;

    public Library(Maker maker) {
        super(new BorderLayout());
        this.maker = maker;
        this.displayer = maker.getDisplayer();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Library");
        root.add(new BoardTree("Tilings"));
        library = new JTree(root);
        LibraryML ml = new LibraryML(maker,library);

        library.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        library.addMouseListener(ml);

        treeView = new JScrollPane(library);

        add(treeView,BorderLayout.CENTER);
    }
}
