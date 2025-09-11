package app.boardMaker.display.panels.graphView;

import app.boardMaker.utils.Edge;
import app.boardMaker.utils.Vertex;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GraphListener extends MouseAdapter {
    private GraphView view;
    private GraphBuildingModes buildMode = GraphBuildingModes.ADDVERTEX;

    public GraphListener(GraphView view) {
        this.view = view;
    }

    public void setBuildMode(GraphBuildingModes mode) {
        buildMode = mode;
    }

    public GraphBuildingModes buildMode() {
        return buildMode;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (buildMode) {
            case ADDVERTEX :
                int x = e.getX();
                int y = view.getHeight() - e.getY();

                // Coordinates on the grid
                double correctedX = x - view.getCamera().offX();
                double correctedY = y - view.getCamera().offY();

                double vx = correctedX / view.inc();
                double vy = correctedY / view.inc();

                view.addVertex(new Vertex(vx, vy));
                break;

            case REMOVEVERTEX :
                view.removeHovered();
                break;

            case ADDEDGE :
                if (view.edgeStart() == null) {
                    // Set first point of edge
                    if (view.hoveredVertex() == null) {
                        // Add new vertex (TBD)
                    } else {
                        view.setEdgeStart(view.hoveredVertex());
                    }
                } else {
                    // Set second point
                    if (view.hoveredVertex() == null) {
                        // Add new vertex (TBD)
                    } else {
                        Edge edge = new Edge(view.edgeStart(),view.hoveredVertex());
                        view.addEdge(edge);
                        view.setEdgeStart(null);
                    }
                }
                break;

            case REMOVEEDGE :
                if (view.edgeStart() == null) {
                    if (view.hoveredVertex() != null) {
                        view.setEdgeStart(view.hoveredVertex());
                    }
                } else {
                    if (view.hoveredVertex() != null) {
                        view.removeEdge(view.edgeStart(),view.hoveredVertex());
                        view.setEdgeStart(null);
                    }
                }
                break;

            default :
                break;
        }

        view.revalidate();
        view.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = view.getHeight() - e.getY();

        for (Vertex v : view.getVertexes()) {
            int centerX = (int) (v.getX() * view.inc() + view.getCamera().offX());
            int centerY = (int) (v.getY() * view.inc() + view.getCamera().offY());

            double distance = Math.sqrt((mouseX - centerX) * (mouseX - centerX) +
                    (mouseY - centerY) * (mouseY - centerY));
            if (distance < (double) view.dotSize() / 2.0) {
                view.setHoveredVertex(v);
                return;
            }
        }

        view.setHoveredVertex(null);
    }
}
