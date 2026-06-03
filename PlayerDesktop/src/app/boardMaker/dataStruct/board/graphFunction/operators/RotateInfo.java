package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.floats.FloatConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Rotate;

import java.util.Locale;

/**
 * Class used to store information about the Rotate graph function
 */
public class RotateInfo implements GraphInfo {
    private float angle;
    private GraphInfo graphFunction;

    private GraphFunction function;

    public RotateInfo(float angle, GraphInfo gfct) {
        this.angle = angle;
        this.graphFunction = gfct;
        createFunction();
    }

    @Override
    public String description() {
        return String.format(Locale.ENGLISH,"(rotate %.2f %s)",angle,graphFunction.description());
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Rotate(new FloatConstant(angle),graphFunction.function());
    }

    public GraphInfo getGraphFunction() {
        return graphFunction;
    }
}
