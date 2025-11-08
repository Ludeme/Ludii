package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class TrimInfo implements GraphInfo {
    private GraphInfo graphFunction;

    public TrimInfo(GraphInfo gfct) {
        this.graphFunction = gfct;
    }

    @Override
    public String description() {
        return String.format("(trim %s)",graphFunction.description());
    }
}
