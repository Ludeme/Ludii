package app.display.dialogs.visual_editor.model.interfaces;

import app.display.dialogs.visual_editor.LayoutManagement.Vector2D;

import java.util.List;

/**
 * An interface of a node used for layout graph
 * @author nic0gin
 */

public interface iGNode {

    /**
     * Returns node id
     * @return node id
     */
    int id();

    /**
     * Returns id of a parent node
     * @return id of a parent node
     */
    int parent();

    /**
     * Returns list of children id's order by connection components
     * @return list of children id's
     */
    List<Integer> children();

    /**
     * Returns position of a node
     * @return position vector of a node
     */
    Vector2D pos();

    /**
     * Set position vector of a node
     * @param pos position vector
     */
    void setPos(Vector2D pos);

    /**
     * Get width of a node box
     * @return width
     */
    int width();

    /**
     * Get height of a node box
     * @return height
     */
    int height();

    /**
     * Set depth of a node
     * @param depth depth
     */
    void setDepth(int depth);

    /**
     * Get depth of a node
     * @return depth
     */
    int depth();

    /**
     * Get boolean flag to check if node is fixed
     * @return boolean flag
     */
    boolean fixed();

    /**
     * Get boolean flag to check if node is collapsed
     * @return boolean flag
     */
    boolean collapsed();

}
