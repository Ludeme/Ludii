package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

import java.util.Locale;

public class ShiftInfo implements GraphInfo {
    private float dx;
    private float dy;
    private GraphInfo graphFunction;

    public ShiftInfo(float dx, float dy, GraphInfo gfct) {
        this.dx = dx;
        this.dy = dy;
        this.graphFunction = gfct;
    }

    @Override
    public String description() {
        return String.format(Locale.ENGLISH, "(shift %.2f %.2f %s)",dx,dy,graphFunction.description());
    }
}
