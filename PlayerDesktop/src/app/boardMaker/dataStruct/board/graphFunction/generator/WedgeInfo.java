package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.shape.Wedge;

public class WedgeInfo implements GraphInfo {
    private int nrow;
    private int ncol;

    private GraphFunction function;

    public WedgeInfo(int row, int col) {
        this.nrow = row;
        this.ncol = col;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(wedge %d %d)",nrow,ncol);
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Wedge(new DimConstant(nrow),new DimConstant(ncol));
    }
}
