package app.display.dialogs.visual_editor.model.interfaces;

import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;

import java.util.List;

/**
 * An interface of a node used for layout graph
 * @author nic0gin
 */

public interface iGNode {

    int getId();

    int getParent();

    List<Integer> getChildren();

    List<Integer> getSiblings();

    Vector2D getPos();

    void setPos(Vector2D pos);

    int getWidth();

    int getHeight();

    void setDepth(int depth);

    int getDepth();

}
