package app.boardMaker.display.panels.westPanel.itemList;

import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListAddNode;
import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListBoardNode;
import app.boardMaker.handlers.Maker;
import app.boardMaker.dataStruct.board.BoardData;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ItemListML extends MouseAdapter {
    private Maker maker;
    private JTree tree;
    private ItemList itemList;

    public ItemListML(Maker maker, ItemList itemList) {
        this.maker = maker;
        this.tree = itemList.tree();
        this.itemList = itemList;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (e.getClickCount() == 2) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node instanceof ItemListBoardNode) {
                    itemList.selectedBoard = (ItemListBoardNode) node;
                    int idx = itemList.boardRoot().getIndex(node);
                    maker.switchBoard(idx);
                } else if (node instanceof ItemListAddNode) {
                    ItemListAddNode addNode = (ItemListAddNode) node;
                    addNode.addItem();
                }
            }
        }
    }
}
