package app.display.dialogs.visual_editor.LayoutManagement.interfaces;

import java.util.List;

/**
 * @author frehburg
 */
public interface iNode {

    /**
     * Adds a child to the children list
     * @param keyword
     */
    iNode addChild(String keyword) throws NullPointerException;

    /**
     * Returns the ludeme that this node represents
     * @return
     */
    String getKeyword();

    /**
     * Returns the variant of  implementation in the syntax
     * @return
     */
    String getImplementation();

    /**
     * Returns a list of all possible variants of implementation in the syntax
     * @return
     */
    List<String> getConstructors();

    /**
     * Returns the parent of the node if there exists one and throws a NullPointerException if there is none.
     * @return
     */
    iNode getParent();

    /**
     * Returns the list of children
     * @return
     */
    List<iNode> getChildren();

    /**
     * Returns the list of siblings (nodes with the same parent
     * @return
     */
    List<iNode> getSiblings();

    int getChildrenSize();

    /**
     * Returns the node type
     * @return
     */
    NodeType getNodeType();

    //TODO: two types of id... maybe include both? :/
    /**
     * Returns the id of the node
     * @return
     */
    String getStringId();

    /**
     * Returns the id of the node
     * @return
     */
    int getIntId();

    /**
     * Sets the variant of  implementation in the syntax to a new value implementation
     * @return
     */
    void setImplementation(String implementation);

    /**
     * Returns true, if the keyword is null, or x * " " (so also "")
     * or if the parent is null
     * or if the children list is null
     * @param node
     * @return
     */
    public boolean isNull(iNode node);

    /**
     * Returns an exact copy of this node. Note that a clone is never equal to the original node, becuase they have
     *      * different ids.
     * @return
     */
    iNode clone();

    boolean equals(Object o);

    String toString();

    void setKeyword(String keyword);
}
