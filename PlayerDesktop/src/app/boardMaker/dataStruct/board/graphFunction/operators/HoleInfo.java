package app.boardMaker.dataStruct.board.graphFunction.operators;

import app.boardMaker.dataStruct.board.BoardRange;
import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.utils.Camera;
import app.boardMaker.utils.CoordinatesUtil;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Clip;
import game.functions.graph.operators.Hole;
import game.util.graph.Poly;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Locale;

public class HoleInfo implements GraphInfo {
    private List<Point> vertices;
    private Float[][] updatedVertices;
    private GraphInfo graphFunction;

    private GraphFunction function;

    public HoleInfo(GraphInfo gfct, List<Point> vertices) {
        this.vertices = vertices;
        this.graphFunction = gfct;
    }

    @Override
    public String description() {
        return String.format("(hole %s (poly %s))",graphFunction.description(),printVertices());
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    public void createFunction(Rectangle placement, BoardRange range, Camera camera) {
        updatedVertices = new Float[vertices.size()][2];
        for (int i = 0; i < vertices.size(); i++) {
            Point2D pt = CoordinatesUtil.boardPosn(vertices.get(i),placement,range,camera);
            updatedVertices[i][0] = (float) pt.getX();
            updatedVertices[i][1] = (float) pt.getY();
        }

        function = new Hole(graphFunction.function(),new Poly(updatedVertices,null));
    }

    private String printVertices() {
        StringBuilder s = new StringBuilder();
        s.append("{");
        for (Float[] v : updatedVertices) {
            s.append(String.format(Locale.ENGLISH," {%.2f %.2f} ", v[0], v[1]));
        }
        s.append("}");
        return s.toString();
    }
}
