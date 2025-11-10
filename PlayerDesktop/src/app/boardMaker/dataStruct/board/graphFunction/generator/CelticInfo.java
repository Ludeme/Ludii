package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.celtic.Celtic;

public class CelticInfo implements GraphInfo {
    private int nrow;
    private int ncol;

    private GraphFunction function;

    public CelticInfo(int row, int col) {
        this.nrow = row;
        this.ncol = col;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(celtic %d %d)",nrow,ncol);
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Celtic(new DimConstant(nrow),new DimConstant(ncol));
    }
}
