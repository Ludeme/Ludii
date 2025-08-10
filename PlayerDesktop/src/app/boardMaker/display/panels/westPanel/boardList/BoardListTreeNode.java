package app.boardMaker.display.panels.westPanel.boardList;

import app.boardMaker.utils.BoardData;

import javax.swing.tree.DefaultMutableTreeNode;

public class BoardListTreeNode extends DefaultMutableTreeNode {
    private String name;
    private BoardData boardData;

    public BoardListTreeNode(String name, BoardData data) {
        this.name = name;
        this.boardData = data;
    }

    public BoardData data() {
        return boardData;
    }

    @Override
    public String toString() {
        return name;
    }
}
