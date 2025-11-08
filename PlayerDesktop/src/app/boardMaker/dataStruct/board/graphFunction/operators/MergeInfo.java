package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class MergeInfo implements GraphInfo {
    private GraphInfo first;
    private GraphInfo second;

    public MergeInfo(GraphInfo gfct1, GraphInfo gfct2) {
        this.first = gfct1;
        this.second = gfct2;
    }

    @Override
    public String description() {
        return String.format("(merge %s %s)",first.description(),second.description());
    }
}
