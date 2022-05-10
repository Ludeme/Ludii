package app.display.dialogs.visual_editor.LayoutManagement.LayoutManager;

import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import javax.swing.*;

import java.util.HashMap;

import static app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines.updateNodeDepth;

/**
 * TODO: implement application to the trees with different roots
 * @author nic0gin
 */

public class LayoutHandler {

    private iGraph graph;
    private int root;

    private LayoutMethod layout;

    public LayoutHandler(iGraph graph, int root)
    {
        this.graph = graph;
        this.root = root;
    }

    public void setLayoutMethod(int l)
    {
        switch (l)
        {
            case 0: layout = new FruchtermanReingold(graph, 0.5, 0.15, new Vector2D(5000, 5000));
                break;
            case 1: layout = new DFSBoxDrawing(graph, root,50);
                break;
            case 2: layout = new PLANET(graph, root,15);
                break;
            case 4: layout = new CFDP(graph, 0.5, 0.15, new Vector2D(5000, 5000));
                break;
            default: layout = new DFSBoxDrawing(graph, root, 5);
        }

    }

    public void setRoot(int root) {
        this.root = root;
    }

    public void setFDPTimer(Timer timer)
    {
        ((FruchtermanReingold) layout).setTimer(timer);
    }

    public void setCFDPTimer(Timer timer)
    {
        ((CFDP) layout).setTimer(timer);
    }

    // ################

    public void updateOffsets(double w)
    {
        updateWeightMap(((DFSBoxDrawing) layout).getOFFSET_MAP(), w);
    }

    public void updateOffsets(double w, int l)
    {
        updateWeightMap(((DFSBoxDrawing) layout).getOFFSET_MAP(), w, l);
    }

    public void updateSpread(double w)
    {
        updateWeightMap(((DFSBoxDrawing) layout).getSPREAD_MAP(), w);
    }

    public void updateSpread(double w, int l)
    {
        updateWeightMap(((DFSBoxDrawing) layout).getSPREAD_MAP(), w, l);
    }

    public void updateDistance(double w)
    {
        updateWeightMap(((DFSBoxDrawing) layout).getDISTANCE_MAP(), w);
    }

    public void updateDistance(double w, int l)
    {
        updateWeightMap(((DFSBoxDrawing) layout).getDISTANCE_MAP(), w, l);
    }

    public void updateAllWeights(double offset, double distance, double spread)
    {
        updateDistance(distance);
        updateOffsets(offset);
        updateSpread(spread);
    }

    public void updateAllWeights(double offset, double distance, double spread, int l)
    {
        updateDistance(distance, l);
        updateOffsets(offset, l);
        updateSpread(spread, l);
    }

    private void updateWeightMap(HashMap<Integer, Double> map, double w)
    {
        map.forEach((id,v) -> {
            map.put(id, w);
        });
    }

    public void updateWeightMap(HashMap<Integer, Double> map, double w, int l)
    {
        map.put(l, w);
    }

    // ################

    public void executeLayout()
    {
        // Prepare the graph

        // Calculate the depth for each node with respect with selected root
        // TODO implement int root into constructor
        int r = 1;
        updateNodeDepth(graph, r);

        layout.applyLayout();
    }

}
