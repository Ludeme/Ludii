package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.generators.basis.square.DiagonalsType;

public class RectangleInfo implements GraphInfo {
    private int nrow;
    private int ncol;
    private DiagonalsType diagonalsType;

    public RectangleInfo(int row, int col, DiagonalsType diagonalsType) {
        this.nrow = row;
        this.ncol = col;
        this.diagonalsType = diagonalsType;
    }

    @Override
    public String description() {
        return String.format("(rectangle %d %d diagonals:%s)",nrow,ncol,diagonalsType);
    }
}
