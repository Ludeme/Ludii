package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.utils.Vertex;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.tiling.Tiling;
import game.functions.graph.generators.basis.tiling.TilingType;
import game.util.graph.Poly;

import java.util.List;

/**
 * Class used to store information about the Tiling graph function
 */
public class SemiregularInfo implements GraphInfo {
    private TilingType tiling;
    private int dimA;
    private int dimB;
    private List<Vertex> vertices;

    private GraphFunction function;

    public SemiregularInfo(TilingType tiling, int dimA, int dimB) {
        this.tiling = tiling;
        this.dimA = dimA;
        this.dimB = dimB;
        createFunction();
    }

    public SemiregularInfo(List<Vertex> vertices, TilingType tiling) {
        this.vertices = vertices;
        this.tiling = tiling;
        createFunction();
    }


    @Override
    public String description() {
        if (vertices == null) {
            return String.format("(tiling %s %d %d)",tiling,dimA,dimB);
        } else {
            return String.format("(tiling %s (poly %s))",tiling,printVertices());
        }
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        if (vertices == null) {
            function = Tiling.construct(tiling,new DimConstant(dimA),new DimConstant(dimB));
        } else {
            Float[][] pts = new Float[vertices.size()][2];

            for (int i = 0; i < vertices.size(); i++) {
                pts[i][0] = (float) vertices.get(i).getX();
                pts[i][1] = (float) vertices.get(i).getY();
            }

            function = Tiling.construct(tiling,new Poly(pts,null),null);
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
