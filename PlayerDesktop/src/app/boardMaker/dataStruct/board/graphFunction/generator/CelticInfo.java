package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.utils.Vertex;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.celtic.Celtic;
import game.util.graph.Poly;

import java.util.List;

/**
 * Class used to store information about the Celtic graph function
 */
public class CelticInfo implements GraphInfo {
    private int nrow;
    private int ncol;
    // List of the vertices of the polygon shape
    private List<Vertex> vertices;

    private GraphFunction function;

    public CelticInfo(int row, int col) {
        this.nrow = row;
        this.ncol = col;
        createFunction();
    }

    public CelticInfo(List<Vertex> vertices) {
        this.vertices = vertices;
        createFunction();
    }

    @Override
    public String description() {
        if (vertices == null) {
            return String.format("(celtic %d %d)",nrow,ncol);
        } else {
            return String.format("(celtic (poly %s))",printVertices());
        }
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        if (vertices == null) {
            function = new Celtic(new DimConstant(nrow),new DimConstant(ncol));
        } else {
            Float[][] pts = new Float[vertices.size()][2];

            for (int i = 0; i < vertices.size(); i++) {
                pts[i][0] = (float) vertices.get(i).getX();
                pts[i][1] = (float) vertices.get(i).getY();
            }

            function = new Celtic(new Poly(pts,null),null);
        }
    }

    private String printVertices() {
        StringBuilder s = new StringBuilder();
        s.append("{");
        for (Vertex v : vertices) {
            s.append(String.format(" {%d %d} ",(int) v.getX(), (int) v.getY()));
        }
        s.append("}");
        return s.toString();
    }
}
