package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Complete;

public class CompleteInfo implements GraphInfo {
    private GraphInfo graphFunction;
    private boolean eachCell;

    private GraphFunction function;

    public CompleteInfo(GraphInfo graphFunction, boolean eachCell) {
        this.graphFunction = graphFunction;
        this.eachCell = eachCell;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(complete %s eachCell:%b)",graphFunction.description(),eachCell);
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Complete(graphFunction.function(),eachCell);
    }
}
