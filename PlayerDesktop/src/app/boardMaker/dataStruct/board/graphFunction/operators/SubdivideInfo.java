package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class SubdivideInfo implements GraphInfo {
    private GraphInfo graphInfo;
    private int min;

    public SubdivideInfo(GraphInfo gfct, int min) {
        this.graphInfo = gfct;
        this.min = min;
    }

    @Override
    public String description() {
        return String.format("(subdivide %s min:%d)",graphInfo.description(),min);
    }
}
