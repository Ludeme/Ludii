package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class CelticInfo implements GraphInfo {
    private int nrow;
    private int ncol;

    public CelticInfo(int row, int col) {
        this.nrow = row;
        this.ncol = col;
    }

    @Override
    public String description() {
        return String.format("(celtic %d %d)",nrow,ncol);
    }
}
