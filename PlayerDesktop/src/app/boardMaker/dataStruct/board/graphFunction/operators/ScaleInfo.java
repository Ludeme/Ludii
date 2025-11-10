package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.floats.FloatConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Scale;

import java.util.Locale;

public class ScaleInfo implements GraphInfo {
    private float sx;
    private float sy;
    private GraphInfo graphFunction;

    private GraphFunction function;

    public ScaleInfo(float sx, float sy, GraphInfo gfct) {
        this.sx = sx;
        this.sy = sy;
        this.graphFunction = gfct;
        createFunction();
    }

    @Override
    public String description() {
        return String.format(Locale.ENGLISH,"(scale %.2f %.2f %s)",sx,sy,graphFunction.description());
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Scale(new FloatConstant(sx),new FloatConstant(sy),null,graphFunction.function());
    }

    public GraphInfo getGraphFunction() {
        return graphFunction;
    }
}
