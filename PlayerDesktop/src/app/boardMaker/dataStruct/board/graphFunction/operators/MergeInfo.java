package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Merge;

import java.util.ArrayList;

public class MergeInfo implements GraphInfo {
    private GraphInfo first;
    private GraphInfo second;

    private GraphFunction function;

    public MergeInfo(GraphInfo gfct1, GraphInfo gfct2) {
        this.first = gfct1;
        this.second = gfct2;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(merge {%s %s})",first == null ? "" : first.description(),second == null ? "" : second.description());
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
        function = new Merge(fcts.toArray(new GraphFunction[0]),false);
    }
}
