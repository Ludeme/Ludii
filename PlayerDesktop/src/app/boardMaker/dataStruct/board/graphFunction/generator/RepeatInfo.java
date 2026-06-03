package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.utils.Vertex;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.shape.Repeat;
import game.util.graph.Poly;

import java.util.List;
import java.util.Locale;

/**
 * Class used to store information about the Repeat graph function
 */
public class RepeatInfo implements GraphInfo {
    // List of vertices representing the shape to repeat
    private List<Vertex> vertices;
    // Number of repetitions
    private int rep;
    // Number of items to repeat
    private int items;
    private float[] itemStep;
    private float[] repStep;
    
    private GraphFunction function;

    public RepeatInfo(List<Vertex> poly, int rep, int items, float[] itemStep, float[] repStep) {
        this.vertices = poly;
        this.rep = rep;
        this.items = items;
        this.itemStep = itemStep;
        this.repStep = repStep;
        createFunction();
    }

    @Override
    public String description() {
        return String.format("(repeat %d %d step:%s (poly %s))",rep,items,describeStep(),printVertices());
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = new Repeat(new DimConstant(rep),new DimConstant(items),makeStep(),makePoly(),null);
    }

    private Poly makePoly() {
        Float[][] pts = new Float[vertices.size()][2];

        for (int i = 0; i < vertices.size(); i++) {
            pts[i][0] = (float) vertices.get(i).getX();
            pts[i][1] = (float) vertices.get(i).getY();
        }

        return new Poly(pts,null);
    }

    private Float[][] makeStep() {
        Float[][] step = new Float[2][2];
        step[0] = new Float[]{itemStep[0],itemStep[1]};
        step[1] = new Float[]{repStep[0],repStep[1]};
        return step;
    }

    private String printVertices() {
        StringBuilder s = new StringBuilder();
        s.append("{");
        for (Vertex v : vertices) {
            s.append(String.format(" {%d %d} ",(int) v.getX(), (int) v.getY()));
        }
        s.append("}");
        return s.toString();
    }

    private String describeStep() {
        StringBuilder s = new StringBuilder();
        s.append("{");
        s.append(String.format(Locale.ENGLISH,"{%.2f %.2f} {%.2f %.2f}",itemStep[0],itemStep[1],repStep[0],repStep[1]));
        s.append("}");
        return s.toString();
    }
}
