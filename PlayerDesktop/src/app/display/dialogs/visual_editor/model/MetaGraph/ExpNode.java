package app.display.dialogs.visual_editor.model.MetaGraph;


import app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.DrawingFrame;
import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;

import java.util.ArrayList;
import java.util.List;

import static app.display.dialogs.visual_editor.LayoutManagement.LayoutConfigs.NODE_SIZE;


/**
 * Rough implementation of a node
 * @author nic0gin
 */

public class ExpNode implements iGNode
{
    private String label;

    private Vector2D pos;
    private List<Integer> childNodes;
    private int parent;

    private final int id;
    private static int node_count = 1;

    private int height;
    private int width;

    private int depth;

    public ExpNode()
    {
        id = node_count++;
        label = "";
        setup();
    }

    public ExpNode(String data)
    {
        id = node_count++;
        this.label = data;
        setup();
    }

    private void setup()
    {
        childNodes = new ArrayList<>();
        pos = DrawingFrame.getRandomScreenPos();

        height = NODE_SIZE;
        width = NODE_SIZE;
    }

    //### Implementation of interface methods ###

    @Override
    public int id() {
        return id;
    }

    @Override
    public int parent() {
        return parent;
    }

    @Override
    public List<Integer> children() {
        return childNodes;
    }

    @Override
    public List<Integer> siblings() {
        return null;
    }

    @Override
    public Vector2D pos() {
        return pos;
    }

    @Override
    public void setPos(Vector2D v) {
        this.pos = new Vector2D(v.getX(), v.getY());
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    @Override
    public int depth()
    {
        return depth;
    }

    //### Additional functionality ###

    public void addChildNode(int id) {
        childNodes.add(id);
    }

    public void addParent(int id) {
        parent = id;
    }

    public String getLabel() {
        return label;
    }

    public Vector2D getTransPos()
    {
        return new Vector2D(pos.getX() + DrawingFrame.getWIDTH() / 2,
                pos.getY() + DrawingFrame.getHEIGHT() / 2);
    }
}
