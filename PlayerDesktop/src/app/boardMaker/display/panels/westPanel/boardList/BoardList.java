package app.boardMaker.display.panels.westPanel.boardList;

import app.boardMaker.handlers.Maker;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class BoardList extends JPanel {
    private Maker maker;

    private JTree boardListTree;
    private JScrollPane boardListView;

    private DefaultMutableTreeNode root;

    public BoardList(Maker maker) {
        super(new BorderLayout());

        this.maker = maker;

        root = new DefaultMutableTreeNode("Boards");
        boardListTree = new JTree(root);

        maker.setBoardList(this);

        BoardListML ml = new BoardListML(maker,boardListTree);

        boardListTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        boardListTree.addMouseListener(ml);

        boardListView = new JScrollPane(boardListTree);

        add(boardListView,BorderLayout.CENTER);
    }

    public DefaultMutableTreeNode root() {
        return root;
    }

    public void reload() {
        ((DefaultTreeModel) boardListTree.getModel()).reload();
    }
}
