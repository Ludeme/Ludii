package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Skew;

import java.util.Locale;

public class SkewInfo implements GraphInfo {
    private float value;
    private GraphInfo graphFunction;

    private GraphFunction function;

    public SkewInfo(float value, GraphInfo gfct) {
        this.value = value;
        this.graphFunction = gfct;
        createFunction();
    }

    @Override
    public String description() {
        return String.format(Locale.ENGLISH,"(skew %.2f %s)",value,graphFunction.description());
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Skew(value,graphFunction.function());
    }

    public GraphInfo getGraphFunction() {
        return graphFunction;
    }
}
