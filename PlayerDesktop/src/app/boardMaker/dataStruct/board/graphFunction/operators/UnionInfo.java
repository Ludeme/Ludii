package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Union;

import java.util.ArrayList;

/**
 * Class used to store information about the Union graph function
 */
public class UnionInfo implements GraphInfo {
    private GraphInfo first;
    private GraphInfo second;

    private GraphFunction function;

    public UnionInfo(GraphInfo gfct1, GraphInfo gfct2) {
        this.first = gfct1;
        this.second = gfct2;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(union {%s %s})",first == null ? "" : first.description(),second == null ? "" : second.description());
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        ArrayList<GraphFunction> fcts = new ArrayList<>();
        if (first != null) {
            fcts.add(first.function());
        }
        if (second != null) {
            fcts.add(second.function());
        }
        function = new Union(fcts.toArray(new GraphFunction[0]),false);
    }
}
