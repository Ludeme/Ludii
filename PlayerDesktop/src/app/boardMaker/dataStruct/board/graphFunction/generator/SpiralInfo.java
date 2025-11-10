package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.shape.Spiral;

public class SpiralInfo implements GraphInfo {
    private int turns;
    private int sites;
    private boolean clockwise;

    private GraphFunction function;

    public SpiralInfo(int turns,int sites,boolean clockwise) {
        this.turns = turns;
        this.sites = sites;
        this.clockwise = clockwise;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(spiral turns:%d sites:%d clockwise:%b)",turns,sites,clockwise);
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Spiral(new DimConstant(turns),new DimConstant(sites),clockwise);
    }
}
