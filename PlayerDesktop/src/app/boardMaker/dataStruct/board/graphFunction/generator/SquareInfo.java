package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.square.DiagonalsType;
import game.functions.graph.generators.basis.square.Square;
import game.functions.graph.generators.basis.square.SquareShapeType;

public class SquareInfo implements GraphInfo {
    private SquareShapeType shape;
    private int dim;
    private DiagonalsType diagonalsType;
    private Boolean pyramid;

    private GraphFunction function;

    public SquareInfo(SquareShapeType shape, int dim, DiagonalsType diagonalsType, Boolean pyramid) {
        this.shape = shape;
        this.dim = dim;
        this.diagonalsType = diagonalsType;
        this.pyramid = pyramid;
        createFunction();
    }

    @Override
    public String description() {
        if (diagonalsType == null) {
            return String.format("(square %s %d pyramidal:%b)",shape,dim,pyramid);
        } else {
            return String.format("(square %s %d diagonals:%s)",shape,dim,diagonalsType);
        }
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = Square.construct(shape,new DimConstant(dim),diagonalsType,pyramid);
    }
}
