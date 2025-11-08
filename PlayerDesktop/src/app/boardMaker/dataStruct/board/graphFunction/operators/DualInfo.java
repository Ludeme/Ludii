package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class DualInfo implements GraphInfo {
    private GraphInfo graphFunction;

    public DualInfo(GraphInfo gfct) {
        this.graphFunction = gfct;
    }

    @Override
    public String description() {
        return String.format("(dual %s)",graphFunction.description());
    }
}
