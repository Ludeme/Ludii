package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Dual;

public class DualInfo implements GraphInfo {
    private GraphInfo graphFunction;

    private GraphFunction function;

    public DualInfo(GraphInfo gfct) {
        this.graphFunction = gfct;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(dual %s)",graphFunction.description());
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Dual(graphFunction.function());
    }
}
