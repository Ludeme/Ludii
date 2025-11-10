package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.tri.Tri;
import game.functions.graph.generators.basis.tri.TriShapeType;

public class TriangleInfo implements GraphInfo {
    private TriShapeType shape;
    private int dimA;
    private int dimB;

    private GraphFunction function;

    public TriangleInfo(TriShapeType shape, int dimA, int dimB) {
        this.shape = shape;
        this.dimA = dimA;
        this.dimB = dimB;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(tri %s %d %d)",shape,dimA,dimB);
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = Tri.construct(shape,new DimConstant(dimA),new DimConstant(dimB));
    }
}
