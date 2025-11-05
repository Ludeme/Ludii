package app.boardMaker.display.panels.westPanel.itemList.nodes;

import app.boardMaker.dataStruct.board.BoardData;

import javax.swing.tree.DefaultMutableTreeNode;

public class ItemListBoardNode extends DefaultMutableTreeNode {
    private String name;
    private BoardData boardData;

    public ItemListBoardNode(String name, BoardData data) {
        this.name = name;
        this.boardData = data;
    }

    public BoardData data() {
        return boardData;
    }

    public String name() {return name;}

    @Override
    public String toString() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setData(BoardData board) {
        this.boardData = board;
    }
}
