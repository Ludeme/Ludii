package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

import java.util.Locale;

public class SkewInfo implements GraphInfo {
    private float value;
    private GraphInfo graphFunction;

    public SkewInfo(float value, GraphInfo gfct) {
        this.value = value;
        this.graphFunction = gfct;
    }

    @Override
    public String description() {
        return String.format(Locale.ENGLISH,"(skew %.2f %s)",value,graphFunction.description());
    }

    public GraphInfo getGraphFunction() {
        return graphFunction;
    }
}
