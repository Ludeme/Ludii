package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import game.functions.booleans.BooleanConstant;
import game.functions.dim.DimConstant;
import game.functions.dim.DimFunction;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.shape.concentric.Concentric;
import game.functions.graph.generators.shape.concentric.ConcentricShapeType;

import java.util.List;

public class ConcentricInfo implements GraphInfo {
    private ConcentricShapeType shape;
    private int nsides;
    private List<Integer> nCellsPerRing;
    private int nrings;
    private int nsteps;
    private boolean midpoints;
    private boolean joinMidpoints;
    private boolean joinCorners;
    private boolean stagger;

    private GraphFunction function;

    public ConcentricInfo(ConcentricShapeType shape, int nsides, List<Integer> cells,int nrings, int nsteps,
                          boolean midpoints, boolean joinMidpoints, boolean joinCorners, boolean stagger) {
        this.shape = shape;
        this.nsides = nsides;
        this.nCellsPerRing = cells;
        this.nrings = nrings;
        this.nsteps = nsteps;
        this.midpoints = midpoints;
        this.joinMidpoints = joinMidpoints;
        this.joinCorners = joinCorners;
        this.stagger = stagger;
        createFunction();
    }

    @Override
    public String description() {
        if (shape != null) {
            return String.format("(concentric %s rings:%d steps:%d midpoints:%b joinMidpoints:%b joinCorners:%b stagger:%b)",
                    shape,nrings,nsteps,midpoints,joinMidpoints,joinCorners,stagger);
        } else if (nCellsPerRing != null) {
            return String.format("(concentric %s rings:%d steps:%d midpoints:%b joinMidpoints:%b joinCorners:%b stagger:%b)",
                    printList(nCellsPerRing),nrings,nsteps,midpoints,joinMidpoints,joinCorners,stagger);
        } else {
            return String.format("(concentric sides:%d rings:%d steps:%d midpoints:%b joinMidpoints:%b joinCorners:%b stagger:%b)",
                    nsides,nrings,nsteps,midpoints,joinMidpoints,joinCorners,stagger);
        }
    }

    @Override
    public GraphFunction function() {
        return function;
    }

    private void createFunction() {
        function = Concentric.construct(shape,nsides == -1 ? null : new DimConstant(nsides),
                nCellsPerRing == null ? null : createDimensions(),new DimConstant(nrings),new DimConstant(nsteps),
                new BooleanConstant(midpoints), new BooleanConstant(joinMidpoints), new BooleanConstant(joinCorners),
                new BooleanConstant(stagger));
    }

    private DimFunction[] createDimensions() {
        DimConstant[] dimensions = new DimConstant[nCellsPerRing.size()];
        for (int i = 0; i < nCellsPerRing.size(); i++) {
            dimensions[i] = new DimConstant(nCellsPerRing.get(i));
        }
        return dimensions;
    }

    private String printList(List<Integer> nCellsPerRing) {
        StringBuilder s = new StringBuilder();
        s.append("{");
        for (Integer i : nCellsPerRing) {
            s.append(i);
            s.append(" ");
        }
        s.append("}");
        return s.toString();
    }
}
