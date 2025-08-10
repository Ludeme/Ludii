package app.boardMaker.display.panels.westPanel.boardList;

import app.boardMaker.handlers.Maker;
import app.boardMaker.utils.BoardData;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardListML extends MouseAdapter {
    private Maker maker;
    private JTree tree;

    public BoardListML(Maker maker, JTree tree) {
        this.maker = maker;
        this.tree = tree;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (e.getClickCount() == 2) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node instanceof BoardListTreeNode) {
                    BoardData data = ((BoardListTreeNode) node).data();
                    maker.switchBoard(data);
                }
            }
        }
    }
}
