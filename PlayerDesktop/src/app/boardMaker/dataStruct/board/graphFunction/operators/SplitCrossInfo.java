package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.SplitCrossings;

/**
 * Class used to store information about the SplitCrossings graph function
 */
public class SplitCrossInfo implements GraphInfo {
    GraphInfo graphFunction;

    private GraphFunction function;

    public SplitCrossInfo(GraphInfo gfct) {
        this.graphFunction = gfct;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(splitCrossings %s)",graphFunction.description());
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new SplitCrossings(graphFunction.function());
    }
}
