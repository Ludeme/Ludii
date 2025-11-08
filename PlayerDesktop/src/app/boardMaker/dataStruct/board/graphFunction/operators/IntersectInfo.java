package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class IntersectInfo implements GraphInfo {
    private GraphInfo first;
    private GraphInfo second;

    public IntersectInfo(GraphInfo gfct1, GraphInfo gfct2) {
        this.first = gfct1;
        this.second = gfct2;
    }

    @Override
    public String description() {
        return String.format("(intersect %s %s)",first.description(),second.description());
    }
}
