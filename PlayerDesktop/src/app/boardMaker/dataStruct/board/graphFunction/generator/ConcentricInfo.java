package app.boardMaker.dataStruct.board.graphFunction.generator;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
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
