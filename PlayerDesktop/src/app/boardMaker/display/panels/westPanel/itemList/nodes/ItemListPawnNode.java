package app.boardMaker.display.panels.westPanel.itemList.nodes;

import game.equipment.component.Piece;
import game.types.play.RoleType;
import game.util.directions.CompassDirection;

import javax.swing.tree.DefaultMutableTreeNode;

public class ItemListPawnNode extends DefaultMutableTreeNode {
    private Piece piece;

    public ItemListPawnNode(String name, RoleType owner, CompassDirection direction) {
        piece = new Piece(name,owner,direction,null,null,null,null,null);
    }

    public Piece piece() {
        return piece;
    }

    @Override
    public String toString() {
        return piece.name();
    }
}
