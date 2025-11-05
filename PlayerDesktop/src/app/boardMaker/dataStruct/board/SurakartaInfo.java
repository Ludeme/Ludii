package app.boardMaker.dataStruct.board;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.handlers.Maker;

public class SurakartaInfo implements ContainerInfo{
    private GraphInfo graphFunction;
    private int nloops;
    private int startLoops;

    private Maker maker;

    public SurakartaInfo(Maker maker, GraphInfo graphFunction, int nloops, int from) {
        this.maker = maker;
        this.graphFunction = graphFunction;
        this.nloops = nloops;
        this.startLoops = from;
    }

    @Override
    public String description() {
        return "(surakartaBoard " + graphFunction.description() + " loops:" + nloops + " from:" + startLoops +
                " largeStack: " + maker.largeStack() + ")\n";
    }
}
