package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.types.board.SiteType;
import other.topology.Edge;

import java.util.List;

public class RemoveInfo implements GraphInfo {
    private SiteType removedType;
    private GraphInfo graphFunction;
    private List<Integer> removedIndices;
    private List<Edge> edges;

    public RemoveInfo(SiteType removed, GraphInfo gfct, List<Integer> indices, List<Edge> edges) {
        this.removedType = removed;
        this.graphFunction = gfct;
        this.removedIndices = indices;
        this.edges = edges;
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
