package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

import java.util.Locale;

public class RotateInfo implements GraphInfo {
    private float angle;
    private GraphInfo graphFunction;

    public RotateInfo(float angle, GraphInfo gfct) {
        this.angle = angle;
        this.graphFunction = gfct;
    }

    @Override
    public String description() {
        return String.format(Locale.ENGLISH,"(rotate %.2f %s)",angle,graphFunction.description());
    }

    public GraphInfo getGraphFunction() {
        return graphFunction;
    }
}
