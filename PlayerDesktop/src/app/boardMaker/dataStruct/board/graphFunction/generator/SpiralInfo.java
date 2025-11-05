package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;

public class SpiralInfo implements GraphInfo {
    private int turns;
    private int sites;
    private boolean clockwise;

    public SpiralInfo(int turns,int sites,boolean clockwise) {
        this.turns = turns;
        this.sites = sites;
        this.clockwise = clockwise;
    }

    @Override
    public String description() {
        return String.format("(spiral turns:%d sites:%d clockwise:%b)",turns,sites,clockwise);
    }
}
