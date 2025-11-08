package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

import java.util.Locale;

public class ScaleInfo implements GraphInfo {
    private float sx;
    private float sy;
    private GraphInfo graphFunction;

    public ScaleInfo(float sx, float sy, GraphInfo gfct) {
        this.sx = sx;
        this.sy = sy;
        this.graphFunction = gfct;
    }

    @Override
    public String description() {
        return String.format(Locale.ENGLISH,"(scale %.2f %.2f %s)",sx,sy,graphFunction.description());
    }

    public GraphInfo getGraphFunction() {
        return graphFunction;
    }
}
