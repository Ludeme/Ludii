package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.square.DiagonalsType;
import game.functions.graph.generators.shape.Rectangle;

public class RectangleInfo implements GraphInfo {
    private int nrow;
    private int ncol;
    private DiagonalsType diagonalsType;

    private GraphFunction function;

    public RectangleInfo(int row, int col, DiagonalsType diagonalsType) {
        this.nrow = row;
        this.ncol = col;
        this.diagonalsType = diagonalsType;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(rectangle %d %d diagonals:%s)",nrow,ncol,diagonalsType);
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = Rectangle.construct(new DimConstant(nrow),new DimConstant(ncol),diagonalsType);
    }
}
