package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class WedgeInfo implements GraphInfo {
    private int nrow;
    private int ncol;

    public WedgeInfo(int row, int col) {
        this.nrow = row;
        this.ncol = col;
    }

    @Override
    public String description() {
        return String.format("(wedge %d %d)",nrow,ncol);
    }
}
