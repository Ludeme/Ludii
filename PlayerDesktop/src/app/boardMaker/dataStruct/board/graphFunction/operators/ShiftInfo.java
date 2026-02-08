package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.floats.FloatConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Shift;

import java.util.Locale;

/**
 * Class used to store information about the Shift graph function
 */
public class ShiftInfo implements GraphInfo {
    private float dx;
    private float dy;
    private GraphInfo graphFunction;

    private GraphFunction function;

    public ShiftInfo(float dx, float dy, GraphInfo gfct) {
        this.dx = dx;
        this.dy = dy;
        this.graphFunction = gfct;
        createFunction();
    }

    @Override
    public String description() {
        return String.format(Locale.ENGLISH, "(shift %.2f %.2f %s)",dx,dy,graphFunction.description());
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Shift(new FloatConstant(dx),new FloatConstant(dy),null,graphFunction.function());
    }
}
