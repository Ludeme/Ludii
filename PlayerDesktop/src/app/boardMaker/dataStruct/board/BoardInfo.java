package app.boardMaker.dataStruct.board;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.handlers.Maker;

public class BoardInfo implements ContainerInfo{
    private GraphInfo graphFunction;

    private Maker maker;

    public BoardInfo(Maker maker, GraphInfo graphFunction) {
        this.maker = maker;
        this.graphFunction = graphFunction;
    }

    @Override
    public String description() {
        return "(board " + graphFunction.description() + " use:" + maker.getSiteType()
                + " largeStack:" + maker.largeStack() + ")\n";
    }
}
