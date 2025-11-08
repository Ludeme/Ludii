package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class SplitCrossInfo implements GraphInfo {
    GraphInfo graphFunction;

    public SplitCrossInfo(GraphInfo gfct) {
        this.graphFunction = gfct;
    }

    @Override
    public String description() {
        return String.format("(splitCrossings %s)",graphFunction.description());
    }
}
