package app.display.dialogs.visual_editor.model;

/**
 * Stores information about edge endpoints and its graphics
 * @author nic0gin
 */

public class Edge {

    private final int nodeA;
    private final int nodeB;

    public Edge(int nodeA, int nodeB)
    {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
    }

    public int getNodeA()
    {
        return nodeA;
    }

    public int getNodeB()
    {
        return nodeB;
    }

    public boolean equals(Edge e)
    {
        return this.nodeA == e.getNodeA() && this.nodeB == e.getNodeB();
    }
}
