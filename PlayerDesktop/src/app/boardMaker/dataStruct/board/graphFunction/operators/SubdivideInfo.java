package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Subdivide;

/**
 * Class used to store information about the Subdivide graph function
 */
public class SubdivideInfo implements GraphInfo {
    private GraphInfo graphInfo;
    private int min;

    private GraphFunction function;

    public SubdivideInfo(GraphInfo gfct, int min) {
        this.graphInfo = gfct;
        this.min = min;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(subdivide %s min:%d)",graphInfo.description(),min);
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Subdivide(graphInfo.function(),new DimConstant(min));
    }
}
