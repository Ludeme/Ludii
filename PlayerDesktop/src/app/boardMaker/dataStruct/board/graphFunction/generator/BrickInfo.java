package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.generators.basis.brick.BrickShapeType;

public class BrickInfo implements GraphInfo {
    private BrickShapeType shape;
    private int dimA;
    private int dimB;
    private boolean trim;

    public BrickInfo(BrickShapeType shape, int dimA, int dimB, boolean trim) {
        this.shape = shape;
        this.dimA = dimA;
        this.dimB = dimB;
        this.trim = trim;
    }

    @Override
    public String description() {
        return String.format("(brick %s %d %d trim:%b)",shape,dimA,dimB,trim);
    }
}
