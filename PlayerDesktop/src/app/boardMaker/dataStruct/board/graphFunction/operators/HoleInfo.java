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

/**
 * Class used to store information about the Hole graph function
 */
public class HoleInfo implements GraphInfo {
    // List of ON SCREEN vertices defining the hole zone
    private List<Point> vertices;
    // List of vertices in the board coordinates system defining the hole zone
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
            s.append(String.format(Locale.ENGLISH," {%.3f %.3f} ", v[0], v[1]));
        }
        s.append("}");
        return s.toString();
    }
}
