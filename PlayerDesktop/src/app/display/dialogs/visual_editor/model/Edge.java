package app.display.dialogs.visual_editor.model;

/**
 * Stores information about edge endpoints and its graphics
 * @author nic0gin
 */

public class Edge {

    private final Integer nodeA;
    private final Integer nodeB;

    public Edge(Integer nodeA, Integer nodeB)
    {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
    }

    public Integer getNodeA()
    {
        return nodeA;
    }

    public Integer getNodeB()
    {
        return nodeB;
    }

    public boolean equals(Edge e)
    {
        return this.nodeA == e.getNodeA() && this.nodeB == e.getNodeB();
    }
}
