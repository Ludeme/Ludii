package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.utils.Vertex;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.square.DiagonalsType;
import game.functions.graph.generators.basis.square.Square;
import game.functions.graph.generators.basis.square.SquareShapeType;
import game.util.graph.Poly;

import java.util.List;

public class SquareInfo implements GraphInfo {
    private SquareShapeType shape;
    private int dim;
    private DiagonalsType diagonalsType;
    private Boolean pyramid;
    private List<Vertex> vertices;

    private GraphFunction function;

    public SquareInfo(SquareShapeType shape, int dim, DiagonalsType diagonalsType, Boolean pyramid) {
        this.shape = shape;
        this.dim = dim;
        this.diagonalsType = diagonalsType;
        this.pyramid = pyramid;
        createFunction();
    }

    public SquareInfo(List<Vertex> vertices, DiagonalsType diagonalsType) {
        this.vertices = vertices;
        this.diagonalsType = diagonalsType;
        createFunction();
    }

    @Override
    public String description() {
        if (vertices == null) {
            if (diagonalsType == null) {
                return String.format("(square %s %d pyramidal:%b)",shape,dim,pyramid);
            } else {
                return String.format("(square %s %d diagonals:%s)",shape,dim,diagonalsType);
            }
        } else {
            return String.format("(square (poly %s) diagonals:%s)",printVertices(),diagonalsType);
        }
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        if (vertices == null) {
            function = Square.construct(shape,new DimConstant(dim),diagonalsType,pyramid);
        } else {
            Float[][] pts = new Float[vertices.size()][2];

            for (int i = 0; i < vertices.size(); i++) {
                pts[i][0] = (float) vertices.get(i).getX();
                pts[i][1] = (float) vertices.get(i).getY();
            }

            function = Square.construct(new Poly(pts,null),null,diagonalsType);
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
