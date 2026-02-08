package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.quadhex.Quadhex;

/**
 * Class used to store information about the Quadhex graph function
 */
public class QuadhexInfo implements GraphInfo {
    private int dim;
    private boolean thirds;

    private GraphFunction function;

    public QuadhexInfo(int dim, boolean thirds) {
        this.dim = dim;
        this.thirds = thirds;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(quadhex %d thirds:%b)",dim,thirds);
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Quadhex(new DimConstant(dim),thirds);
    }
}
