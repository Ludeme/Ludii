package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class QuadhexInfo implements GraphInfo {
    private int dim;
    private boolean thirds;

    public QuadhexInfo(int dim, boolean thirds) {
        this.dim = dim;
        this.thirds = thirds;
    }

    @Override
    public String description() {
        return String.format("(quadhex %d thirds:%b)",dim,thirds);
    }
}
