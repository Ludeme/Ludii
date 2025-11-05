package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.generators.basis.tiling.TilingType;

public class SemiregularInfo implements GraphInfo {
    private TilingType tiling;
    private int dimA;
    private int dimB;

    public SemiregularInfo(TilingType tiling, int dimA, int dimB) {
        this.tiling = tiling;
        this.dimA = dimA;
        this.dimB = dimB;
    }

    @Override
    public String description() {
        return String.format("(tiling %s %d %d)",tiling,dimA,dimB);
    }
}
