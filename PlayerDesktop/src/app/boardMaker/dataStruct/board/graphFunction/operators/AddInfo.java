package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.types.board.SiteType;
import other.topology.Edge;
import other.topology.Vertex;

import java.util.List;
import java.util.Locale;

public class AddInfo implements GraphInfo {
    private SiteType added;
    private GraphInfo graphFunction;
    private List<Vertex> vertices;
    private List<Edge> edges;
    private List<List<Vertex>> cells;

    public AddInfo(SiteType added, GraphInfo gfct, List<Vertex> vertices, List<Edge> edges, List<List<Vertex>> cells) {
        this.added = added;
        this.graphFunction = gfct;
        this.vertices = vertices;
        this.edges = edges;
        this.cells = cells;
    }

    @Override
    public String description() {
        switch (added) {
            case Cell :
                return String.format("(add %s cells:{%s})",graphFunction.description(),parseCells());

            case Edge :
                return String.format("(add %s edges:{%s})",graphFunction.description(),parseEdges());

            case Vertex :
                return String.format("(add %s vertices:{%s})",graphFunction.description(),parseVertices());

            default :
                return "";
        }
    }

    private String parseVertices() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < vertices.size(); i++) {
            Vertex v = vertices.get(i);
            s.append(String.format(Locale.ENGLISH,"{%.2f %.2f}",v.centroid().getX(),v.centroid().getY()));
            if (i != vertices.size() - 1) {
                s.append(" ");
            }
        }
        return s.toString();
    }

    private String parseEdges() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            Vertex va = e.vA(), vb = e.vB();
            s.append("{");
            s.append(String.format(Locale.ENGLISH,"{%.2f %.2f} {%.2f %.2f}",
                    va.centroid().getX(),va.centroid().getY(),vb.centroid().getX(),vb.centroid().getY()));
            s.append("}");
            if (i != edges.size() - 1) {
                s.append(" ");
            }
        }
        return s.toString();
    }

    private String parseCells() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            List<Vertex> c = cells.get(i);
            s.append("{");
            for (int j = 0; j < c.size(); j++) {
                Vertex v = c.get(j);
                s.append(String.format(Locale.ENGLISH,"{%.2f %.2f}",v.centroid().getX(),v.centroid().getY()));
                if (j != c.size() - 1) {
                    s.append(" ");
                }
            }
            s.append("}");
            if (i != cells.size() - 1) {
                s.append(" ");
            }
        }
        return s.toString();
    }
}
