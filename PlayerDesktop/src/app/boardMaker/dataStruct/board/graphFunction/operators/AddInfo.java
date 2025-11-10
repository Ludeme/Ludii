package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.floats.FloatConstant;
import game.functions.floats.FloatFunction;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Add;
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

    private GraphFunction function;

    public AddInfo(SiteType added, GraphInfo gfct, List<Vertex> vertices, List<Edge> edges, List<List<Vertex>> cells) {
        this.added = added;
        this.graphFunction = gfct;
        this.vertices = vertices;
        this.edges = edges;
        this.cells = cells;
        createFunction();
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

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        switch (added) {
            case Cell :
                FloatFunction[][][] c = new FloatFunction[cells.size()][][];
                for (int i = 0; i < cells.size(); i++) {
                    List<Vertex> cell = cells.get(i);
                    FloatFunction[][] array = new FloatFunction[cell.size()][2];
                    for (int j = 0; j < cell.size(); j++) {
                        Vertex vertex = cell.get(j);
                        array[j][0] = new FloatConstant((float) vertex.centroid().getX());
                        array[j][1] = new FloatConstant((float) vertex.centroid().getY());
                    }
                    c[i] = array;
                }
                function = new Add(graphFunction.function(),null,null,null,
                        null,c,null,false);
                break;

            case Edge :
                FloatFunction[][][] e = new FloatFunction[edges.size()][2][2];
                for (int i = 0; i < edges.size(); i++) {
                    Edge edge = edges.get(i);
                    e[i][0][0] = new FloatConstant((float) edge.vA().centroid().getX());
                    e[i][0][1] = new FloatConstant((float) edge.vA().centroid().getY());
                    e[i][1][0] = new FloatConstant((float) edge.vB().centroid().getX());
                    e[i][1][1] = new FloatConstant((float) edge.vB().centroid().getY());
                }
                function = new Add(graphFunction.function(),null,e,null,
                        null,null,null,false);
                break;

            case Vertex :
                FloatFunction[][] v = new FloatFunction[vertices.size()][2];
                for (int i = 0; i < vertices.size(); i++) {
                    Vertex vertex = vertices.get(i);
                    v[i] = new FloatFunction[]{new FloatConstant((float) vertex.centroid().getX()),
                            new FloatConstant((float) vertex.centroid().getY())};
                }
                function = new Add(graphFunction.function(),v,null,null,
                        null,null,null,false);
                break;

            default :
                function = null;
                break;
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
