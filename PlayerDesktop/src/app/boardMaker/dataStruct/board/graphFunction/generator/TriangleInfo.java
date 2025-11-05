package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.generators.basis.tri.TriShapeType;

public class TriangleInfo implements GraphInfo {
    private TriShapeType shape;
    private int dimA;
    private int dimB;

    public TriangleInfo(TriShapeType shape, int dimA, int dimB) {
        this.shape = shape;
        this.dimA = dimA;
        this.dimB = dimB;
    }

    @Override
    public String description() {
        return String.format("(tri %s %d %d)",shape,dimA,dimB);
    }
}
