package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.tiling.Tiling;
import game.functions.graph.generators.basis.tiling.TilingType;

public class SemiregularInfo implements GraphInfo {
    private TilingType tiling;
    private int dimA;
    private int dimB;

    private GraphFunction function;

    public SemiregularInfo(TilingType tiling, int dimA, int dimB) {
        this.tiling = tiling;
        this.dimA = dimA;
        this.dimB = dimB;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(tiling %s %d %d)",tiling,dimA,dimB);
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = Tiling.construct(tiling,new DimConstant(dimA),new DimConstant(dimB));
    }
}
