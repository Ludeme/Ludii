package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.brick.Brick;
import game.functions.graph.generators.basis.brick.BrickShapeType;

public class BrickInfo implements GraphInfo {
    private BrickShapeType shape;
    private int dimA;
    private int dimB;
    private boolean trim;

    private GraphFunction function;

    public BrickInfo(BrickShapeType shape, int dimA, int dimB, boolean trim) {
        this.shape = shape;
        this.dimA = dimA;
        this.dimB = dimB;
        this.trim = trim;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(brick %s %d %d trim:%b)",shape,dimA,dimB,trim);
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = Brick.construct(shape,new DimConstant(dimA), new DimConstant(dimB),trim);
    }
}
