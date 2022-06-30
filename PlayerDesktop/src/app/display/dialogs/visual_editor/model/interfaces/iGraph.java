package app.display.dialogs.visual_editor.model.interfaces;


import app.display.dialogs.visual_editor.model.Edge;

import java.util.HashMap;
import java.util.List;

/**
 * An interface to be adopted by displayable graph
 * @author nic0gin
 */
public interface iGraph
{

    /**
     *
     * @return
     */
    List<Edge> getEdgeList();

    /**
     *
     * @return
     */
    HashMap<Integer, iGNode> getNodeList(); //TODO: do we need this?

    /**
     * get node by id
     * @param id
     * @return node instance
     */
    iGNode getNode(int id);

    /**
     * Adds instance of a node to the graph
     * @param node valid instance of a node
     * @return id
     */
    int addNode(iGNode node);

    /**
     * Removes instance of a node from the graph
     * @param node valid instance of a node
     * @return id
     */
    int removeNode(iGNode node);

    /**
     * Removes instance of a node from the graph
     * @param id valid id of a node
     * @return id
     */
    int removeNode(int id);

    /**
     * add edge
     * @param from parent node
     * @param to child node
     */
    void addEdge(int from, int to);

    void removeEdge(int from, int to);

    void removeEdge(int containsId);

    /**
     *
     * @return list of roots id of all connected components in the graph
     */
    List<Integer> connectedComponentRoots();

    /**
     * Add root id to the connected components list
     * Add node to the list on creation, on removal and on the deletion of parent connection
     * @param root id
     */
    void addConnectedComponentRoot(int root);

    /**
     * Remove root id from the connected components list
     * Call on adding a parent node
     * @param root id
     */
    void removeConnectedComponentRoot(int root);

    /**
     *
     * @return selected root/sub-root
     */
    Integer selectedRoot();

    /**
     *
     * @param root selected root/sub-root
     */
    void setSelectedRoot(Integer root);

    void setRoot(iGNode root);

    iGNode getRoot();

}
