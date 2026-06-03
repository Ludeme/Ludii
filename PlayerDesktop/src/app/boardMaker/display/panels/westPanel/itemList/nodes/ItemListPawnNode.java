package app.boardMaker.display.panels.westPanel.itemList.nodes;

import game.equipment.component.Piece;
import game.types.play.RoleType;
import game.util.directions.CompassDirection;

import javax.swing.tree.DefaultMutableTreeNode;

public class ItemListPawnNode extends DefaultMutableTreeNode {
    private String pieceName;
    private RoleType owner;

    public ItemListPawnNode(String name, RoleType owner) {
        pieceName = name;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return pieceName;
    }
}
