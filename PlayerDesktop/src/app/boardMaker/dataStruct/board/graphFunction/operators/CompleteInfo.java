package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class CompleteInfo implements GraphInfo {
    private GraphInfo graphFunction;
    private boolean eachCell;

    public CompleteInfo(GraphInfo graphFunction, boolean eachCell) {
        this.graphFunction = graphFunction;
        this.eachCell = eachCell;
    }

    @Override
    public String description() {
        return String.format("(complete %s eachCell:%b)",graphFunction.description(),eachCell);
    }
}
