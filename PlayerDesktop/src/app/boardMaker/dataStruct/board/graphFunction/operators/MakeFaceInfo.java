package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.MakeFaces;

/**
 * Class used to store information about the Makeface graph function
 */
public class MakeFaceInfo implements GraphInfo {
    private GraphInfo graphFunction;

    private GraphFunction function;

    public MakeFaceInfo(GraphInfo gfct) {
        this.graphFunction = gfct;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(makeFaces %s)",graphFunction.description());
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new MakeFaces(graphFunction.function());
    }
}
