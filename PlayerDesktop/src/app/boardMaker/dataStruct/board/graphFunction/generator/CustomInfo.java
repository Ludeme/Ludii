package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.utils.Edge;
import app.boardMaker.utils.Vertex;

import java.util.List;

public class CustomInfo implements GraphInfo {
    private List<Vertex> vertices;
    private List<Edge> edges;

    public CustomInfo(List<Vertex> vertices, List<Edge> edges) {
        this.vertices = vertices;
        this.edges = edges;
    }

    @Override
    public String description() {
        return String.format("(graph vertices:%s edges:%s)",printList(vertices),printList(edges));
    }

    private String printList(List<?> list) {
        StringBuilder s = new StringBuilder();
        s.append("{");
        for (Object o : list) {
            s.append(o);
        }
        s.append("}");
        return s.toString();
    }
}
