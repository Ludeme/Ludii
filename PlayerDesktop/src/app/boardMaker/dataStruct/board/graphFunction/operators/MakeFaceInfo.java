package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class MakeFaceInfo implements GraphInfo {
    private GraphInfo graphFunction;

    public MakeFaceInfo(GraphInfo gfct) {
        this.graphFunction = gfct;
    }

    @Override
    public String description() {
        return String.format("(makeFaces %s)",graphFunction.description());
    }
}
