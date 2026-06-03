package app.boardMaker.display.panels.westPanel.itemList.nodes;

import app.boardMaker.display.dialogs.PawnChoiceDialog;
import app.boardMaker.display.panels.westPanel.itemList.ItemList;
import app.boardMaker.handlers.Maker;
import app.boardMaker.res.ItemType;

import javax.swing.tree.DefaultMutableTreeNode;

public class ItemListAddNode extends DefaultMutableTreeNode {
    private ItemType type;

    private ItemList itemList;
    private Maker maker;

    public ItemListAddNode(ItemType type, ItemList itemList, Maker maker) {
        this.type = type;
        this.itemList = itemList;
        this.maker = maker;
    }

    @Override
    public String toString() {
        switch (type) {
            case Board :
                return "Add new empty board";
            case Pawn :
                return "Add new piece";
            default :
                return "";
        }
    }

    public void addItem() {
        if (type == ItemType.Board) {
            itemList.addEmptyBoard();
        } else if (type == ItemType.Pawn) {
            new PawnChoiceDialog(itemList, maker, (DefaultMutableTreeNode) getParent());
        }
    }
}
