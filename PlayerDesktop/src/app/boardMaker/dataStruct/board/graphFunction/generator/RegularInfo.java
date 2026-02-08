package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.shape.Regular;
import game.functions.graph.generators.shape.ShapeStarType;

/**
 * Class used to store information about the Regular graph function
 */
public class RegularInfo implements GraphInfo {
    private boolean star;
    private int sides;

    private GraphFunction function;

    public RegularInfo(boolean star,int sides) {
        this.star = star;
        this.sides = sides;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(regular %s%d)",star ? "Star " : "" ,sides);
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Regular(star ? ShapeStarType.Star : null,new DimConstant(sides));
    }
}
