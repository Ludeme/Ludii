package app.boardMaker.display.panels.westPanel.itemList.nodes;

import app.boardMaker.dataStruct.board.BoardData;
import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.ContainerInfo;

import javax.swing.tree.DefaultMutableTreeNode;

public class ItemListBoardNode extends DefaultMutableTreeNode {
    private String name;
    private BoardData boardData;
    private ContainerInfo boardInfo;

    public ItemListBoardNode(String name, BoardData data) {
        this.name = name;
        this.boardData = data;
    }
    public ItemListBoardNode(String name, ContainerInfo board) {
        this.name = name;
        this.boardInfo = board;
    }

    public BoardData data() {
        return boardData;
    }
    public ContainerInfo getBoardInfo() {
        return boardInfo;
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
    public void setBoardInfo(ContainerInfo board) {
        this.boardInfo = board;
    }
}
