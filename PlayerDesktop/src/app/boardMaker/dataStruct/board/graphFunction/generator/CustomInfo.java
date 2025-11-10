package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.utils.Edge;
import app.boardMaker.utils.Vertex;
import game.functions.graph.GraphFunction;
import game.util.graph.Graph;

import java.util.List;

public class CustomInfo implements GraphInfo {
    private List<Vertex> vertices;
    private List<Edge> edges;

    private GraphFunction function;

    public CustomInfo(List<Vertex> vertices, List<Edge> edges) {
        this.vertices = vertices;
        this.edges = edges;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(graph vertices:%s edges:%s)",printList(vertices),printList(edges));
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        Float[][] v = new Float[vertices.size()][2];
        Integer[][] e = new Integer[edges.size()][2];
        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            v[i] = new Float[]{(float) vertex.getX(), (float) vertex.getY()};
        }
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            e[i] = new Integer[]{edge.getStartIdx(),edge.getEndIdx()};
        }
        function = new Graph(v,e);
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
