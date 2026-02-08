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

/**
 * Class used to store information about the Concentric graph function
 */
public class AddInfo implements GraphInfo {
    // Type of graph element to be added
    private SiteType added;
    // The graph function to be modified
    private GraphInfo graphFunction;
    // Vertices to add, only used when added == Vertex
    private Float[][] vertices;
    // List of edges to add
    private List<Float[][]> edges;
    // List of cells to add
    private List<Float[][]> cells;
    // The current function
    private GraphFunction function;

    public AddInfo(SiteType added, GraphInfo gfct, Float[][] vertices, List<Float[][]> edges, List<Float[][]> cells) {
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
                    Float[][] cell = cells.get(i);
                    FloatFunction[][] array = new FloatFunction[cell.length][2];
                    for (int j = 0; j < cell.length; j++) {
                        Float[] vertex = cell[j];
                        array[j][0] = new FloatConstant(vertex[0]);
                        array[j][1] = new FloatConstant(vertex[1]);
                    }
                    c[i] = array;
                }
                function = new Add(graphFunction.function(),null,null,null,
                        null, c,null,false);
                break;

            case Edge :
                FloatFunction[][][] e = new FloatFunction[edges.size()][2][2];
                for (int i = 0; i < edges.size(); i++) {
                    Float[][] edge = edges.get(i);
                    e[i][0][0] = new FloatConstant(edge[0][0]);
                    e[i][0][1] = new FloatConstant(edge[0][1]);
                    e[i][1][0] = new FloatConstant(edge[1][0]);
                    e[i][1][1] = new FloatConstant(edge[1][1]);
                }
                function = new Add(graphFunction.function(),null,e,null,
                        null,null,null,false);
                break;

            case Vertex :
                FloatFunction[][] v = new FloatFunction[vertices.length][2];
                for (int i = 0; i < vertices.length; i++) {
                    Float[] vertex = vertices[i];
                    v[i] = new FloatFunction[]{new FloatConstant(vertex[0]),
                            new FloatConstant(vertex[1])};
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
        for (int i = 0; i < vertices.length; i++) {
            Float[] v = vertices[i];
            s.append(String.format(Locale.ENGLISH,"{%.3f %.3f}",v[0],v[1]));
            if (i != vertices.length - 1) {
                s.append(" ");
            }
        }
        return s.toString();
    }

    private String parseEdges() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < edges.size(); i++) {
            Float[][] e = edges.get(i);
            Float[] va = e[0], vb = e[1];
            s.append("{");
            s.append(String.format(Locale.ENGLISH,"{%.3f %.3f} {%.3f %.3f}",
                    va[0],va[1],vb[0],vb[1]));
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
            Float[][] c = cells.get(i);
            s.append("{");
            for (int j = 0; j < c.length; j++) {
                Float[] v = c[j];
                s.append(String.format(Locale.ENGLISH,"{%.3f %.3f}",v[0],v[1]));
                if (j != c.length - 1) {
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
