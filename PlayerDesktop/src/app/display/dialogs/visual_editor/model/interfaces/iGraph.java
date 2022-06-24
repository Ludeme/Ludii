package app.display.dialogs.visual_editor.model.interfaces;


import app.display.dialogs.visual_editor.model.Edge;

import java.util.HashMap;
import java.util.List;

/**
 * An interface to be adopted by a main graph
 * @author nic0gin
 */
public interface iGraph
{
    /**
     * Retrieves the root of the graph
     * @return
     */
    iGNode getRoot();

    /**
     * In case you want to change the current root node
     * @param id new root id
     */
    void setRoot(int id);

    void setRoot(iGNode node);

    /**
     *
     * @return
     */
    HashMap<Integer, List<Integer>> getAdjacencyList();

    /**
     *
     * @return
     */
    List<Edge> getEdgeList();

    /**
     *
     * @return
     */
    HashMap<Integer, iGNode> getNodeList(); //TODO: change to node interface

    /**
     * get node by id
     * @param id
     * @return node instance
     */
    iGNode getNode(int id);

    /**
     * Adds disconnected empty node to the graph
     * @return id
     */
    int addNode();

    int addNode(String label);

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
     * add edge
     * @param from
     * @param to
     * @param field
     */
    void addEdge(int from, int to, int field);

}
