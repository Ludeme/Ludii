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
     * Returns a list of edges in graph
     * @return list of edges
     */
    List<Edge> getEdgeList();

    /**
     * Returns a list on nodes in graph
     * @return hashmap where key stands for a node id and value is a node object
     */
    HashMap<Integer, iGNode> getNodeList();

    /**
     * get node by id
     * @param id node id
     * @return node instance
     */
    iGNode getNode(Integer id);

    /**
     * Adds instance of a node to the graph
     * @param node valid instance of a node
     * @return node id
     */
    Integer addNode(iGNode node);

    /**
     * Removes instance of a node from the graph
     * @param node valid instance of a node
     */
    void removeNode(iGNode node);

    /**
     * add edge
     * @param from parent node
     * @param to child node
     */
    void addEdge(Integer from, Integer to);

    /**
     * remove edge
     * @param from starting node of an edge
     * @param to end node of an
     */
    void removeEdge(Integer from, Integer to);

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
    void addConnectedComponentRoot(Integer root);

    /**
     * Remove root id from the connected components list
     * Call on adding a parent node
     * @param root id
     */
    void removeConnectedComponentRoot(Integer root);

    /**
     * returns id of a selected root
     * @return selected root/sub-root
     */
    Integer selectedRoot();

    /**
     * sets selected root
     * @param root selected root/sub-root
     */
    void setSelectedRoot(Integer root);

    /**
     * sets main root of graph
     * @param root node instance (typically 'game' node)
     */
    void setRoot(iGNode root);

    /**
     * returns instance of main graph root
     * @return node instance
     */
    iGNode getRoot();

}
