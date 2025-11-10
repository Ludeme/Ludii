package app.boardMaker.display.panels.graphView;

import app.boardMaker.display.panels.paramPanel.tilings.OptionPanel;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import app.boardMaker.utils.Camera;
import app.boardMaker.utils.Edge;
import app.boardMaker.utils.Vertex;
import main.collections.Pair;

import javax.swing.*;
import java.awt.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphView extends JPanel {
    private Camera camera;
    private Maker maker;
    private Displayer displayer;
    private GraphListener gl;
    private OptionPanel op;

    private int inc = 50;
    private int dotSize = 10;

    private Vertex hoveredVertex;
    private Vertex edgeStart;

    private List<Vertex> vertexes;
    private List<Edge> edges;
    private Map<String,Edge> edgeFromVertex;

    public GraphView(Maker maker, OptionPanel op) {
        this.maker = maker;
        this.displayer = maker.getDisplayer();
        this.op = op;

        camera = new Camera(this);

        vertexes = new ArrayList<>();
        edges = new ArrayList<>();
        edgeFromVertex = new HashMap<>();

        addMouseListener(camera);
        addMouseMotionListener(camera);
    }

    public void setListener(GraphListener listener) {
        gl = listener;

        addMouseListener(gl);
        addMouseMotionListener(gl);
    }

    public Camera getCamera() {
        return camera;
    }

    public int inc() {
        return inc;
    }

    public int dotSize() {
        return dotSize;
    }

    public void setHoveredVertex(Vertex v) {
        if (hoveredVertex == null && v != null) {
            hoveredVertex = v;
            repaint();
        } else if (hoveredVertex != null && v == null) {
            hoveredVertex = v;
            repaint();
        }
    }

    public Vertex hoveredVertex() {
        return hoveredVertex;
    }

    public void setEdgeStart(Vertex v) {
        edgeStart = v;
        if (v != null) {
            repaint();
        }
    }

    public Vertex edgeStart() {
        return edgeStart;
    }

    public void addVertex(Vertex vertex) {
        if (!vertexes.contains(vertex)) {
            vertexes.add(vertex);
            displayer.getPreviewPanel().setBoard(op.createInfo());
            displayer.getPreviewPanel().repaint();
        }
    }

    public void removeHovered() {
        vertexes.remove(hoveredVertex);
        hoveredVertex = null;
        displayer.getPreviewPanel().setBoard(op.createInfo());
        displayer.getPreviewPanel().repaint();
    }

    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void addEdge(Edge edge) {
        if (!edges.contains(edge)) {
            edges.add(edge);
            String key = edge.start().toString() + edge.end().toString(),
                    reversedKey = edge.end().toString() + edge.start().toString();
            edgeFromVertex.put(key,edge);
            edgeFromVertex.put(reversedKey,edge);
            displayer.getPreviewPanel().setBoard(op.createInfo());
            displayer.getPreviewPanel().repaint();
        }
    }

    public void removeEdge(Vertex start, Vertex end) {
        String key = start.toString() + end.toString(),
                reversedKey = end.toString() + start.toString();
        Edge toRemove = edgeFromVertex.get(key);
        edgeFromVertex.remove(key);
        edgeFromVertex.remove(reversedKey);
        edges.remove(toRemove);
        displayer.getPreviewPanel().setBoard(op.createInfo());
        displayer.getPreviewPanel().repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.lightGray);
        g2d.setStroke(new BasicStroke(1));
        for (int i = -inc; i <= Math.max(getHeight(),getWidth()) + inc; i += inc) {
            // Vertical lines
            g2d.drawLine(i + (camera.offX() % inc),-inc,i + (camera.offX() % inc),getHeight() + inc);
            // Horizontal lines
            g2d.drawLine(-inc,getHeight() - i - (camera.offY() % inc),getWidth() + inc,getHeight() - i - (camera.offY() % inc));
        }

        g2d.setColor(Color.black);
        g2d.setStroke(new BasicStroke(5));
        for (Edge e : edges) {
            int x1 = (int) (e.start().getX() * inc + camera.offX());
            int x2 = (int) (e.end().getX() * inc + camera.offX());

            int y1 = (int) (getHeight() - e.start().getY() * inc - camera.offY());
            int y2 = (int) (getHeight() - e.end().getY() * inc - camera.offY());

            g2d.drawLine(x1, y1, x2, y2);
        }

        for (Vertex v : vertexes) {
            if (v.equals(hoveredVertex)) {
                g2d.setColor(Color.green);
            } else if (v.equals(edgeStart)) {
                if (gl.buildMode() == GraphBuildingModes.ADDEDGE) {
                    g2d.setColor(Color.blue);
                } else if (gl.buildMode() == GraphBuildingModes.REMOVEEDGE) {
                    g2d.setColor(Color.red);
                }
            } else {
                g2d.setColor(Color.black);
            }

            int x = (int) (v.getX() * inc + camera.offX());
            int y = (int) (getHeight() - v.getY() * inc - camera.offY());

            g2d.fillOval(x-dotSize/2,y-dotSize/2,dotSize,dotSize);
        }
    }
}
