package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class UnionInfo implements GraphInfo {
    private GraphInfo first;
    private GraphInfo second;

    public UnionInfo(GraphInfo gfct1, GraphInfo gfct2) {
        this.first = gfct1;
        this.second = gfct2;
    }

    @Override
    public String description() {
        return String.format("(union %s %s)",first.description(),second.description());
    }
}
