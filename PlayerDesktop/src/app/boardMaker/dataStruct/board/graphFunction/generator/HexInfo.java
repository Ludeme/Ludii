package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.generators.basis.hex.HexShapeType;

public class HexInfo implements GraphInfo {
    private HexShapeType shape;
    private int dimA;
    private int dimB;

    public HexInfo(HexShapeType shape, int dimA, int dimB) {
        this.shape = shape;
        this.dimA = dimA;
        this.dimB = dimB;
    }

    @Override
    public String description() {
        return String.format("(hex %s %d %d)", shape, dimA,dimB);
    }
}
