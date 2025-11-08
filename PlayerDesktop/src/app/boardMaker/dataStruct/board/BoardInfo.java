package app.boardMaker.dataStruct.board;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.handlers.Maker;

public class BoardInfo implements ContainerInfo{
    private GraphInfo graphInfo;

    private Maker maker;

    public BoardInfo(Maker maker, GraphInfo graphInfo) {
        this.maker = maker;
        this.graphInfo = graphInfo;
    }

    @Override
    public String description() {
        return "(board " + graphInfo.description() + " use:" + maker.getSiteType()
                + " largeStack:" + maker.largeStack() + ")\n";
    }

    public void setGraphInfo(GraphInfo graphFunction) {
        this.graphInfo = graphFunction;
    }

    public GraphInfo getGraphInfo() {
        return graphInfo;
    }
}
