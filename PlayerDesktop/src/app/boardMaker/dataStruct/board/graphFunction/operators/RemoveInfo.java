package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.dim.DimConstant;
import game.functions.dim.DimFunction;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Remove;
import game.types.board.SiteType;
import other.topology.Edge;

import java.util.List;

public class RemoveInfo implements GraphInfo {
    private SiteType removedType;
    private GraphInfo graphFunction;
    private List<Integer> removedIndices;
    private List<Edge> edges;

    private GraphFunction function;

    public RemoveInfo(SiteType removed, GraphInfo gfct, List<Integer> indices, List<Edge> edges) {
        this.removedType = removed;
        this.graphFunction = gfct;
        this.removedIndices = indices;
        this.edges = edges;
        createFunction();
    }

    @Override
    public String description() {
        switch (removedType) {
            case Cell :
                return String.format("(remove %s cells:%s)",graphFunction.description(),parseList(removedIndices));

            case Vertex :
                return String.format("(remove %s vertices:%s)",graphFunction.description(),parseList(removedIndices));

            case Edge :
                return String.format("(remove %s edges:%s)",graphFunction.description(),parseListEdge(removedIndices));

            default :
                return "";
        }
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        switch (removedType) {
            case Cell :
                DimFunction[] c = new DimFunction[removedIndices.size()];
                for (int i = 0; i < removedIndices.size(); i++) {
                    c[i] = new DimConstant(removedIndices.get(i));
                }
                function = new Remove(graphFunction.function(),null,c,null,null,
                        null,null,true);
                break;

            case Edge :
                DimFunction[][] e = new DimFunction[removedIndices.size()][2];
                for (int i = 0; i < removedIndices.size(); i++) {
                    Edge edge = edges.get(removedIndices.get(i));
                    e[i] = new DimFunction[]{new DimConstant(edge.vA().index()),new DimConstant(edge.vB().index())};
                }
                function = new Remove(graphFunction.function(),null,null,null,e,
                        null,null,true);
                break;

            case Vertex :
                DimFunction[] v = new DimFunction[removedIndices.size()];
                for (int i = 0; i < removedIndices.size(); i++) {
                    v[i] = new DimConstant(removedIndices.get(i));
                }
                function = new Remove(graphFunction.function(),null,null,null,null,
                        null,v,true);
                break;

            default :
                function = null;
                break;
        }
    }

    private String parseList(List<Integer> indices) {
        StringBuilder s = new StringBuilder();
        s.append("{");
        for (Integer i : indices) {
            s.append(i).append(" ");
        }
        s.append("}");
        return s.toString();
    }

    private String parseListEdge(List<Integer> indices) {
        StringBuilder s = new StringBuilder();
        s.append("{");
        for (Integer i : indices) {
            s.append(String.format("{%d %d}",edges.get(i).vA().index(),edges.get(i).vB().index()));
        }
        s.append("}");
        return s.toString();
    }
}
