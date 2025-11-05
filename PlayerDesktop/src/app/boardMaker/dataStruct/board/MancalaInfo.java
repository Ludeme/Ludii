package app.boardMaker.dataStruct.board;

import app.boardMaker.handlers.Maker;
import game.types.board.StoreType;

public class MancalaInfo implements ContainerInfo{
    private int nrow;
    private int ncol;
    private StoreType storeType;
    private int nstores;

    private Maker maker;

    public MancalaInfo(Maker maker, int row, int col, StoreType type, int stores) {
        this.maker = maker;

        nrow = row;
        ncol = col;
        storeType = type;
        nstores = stores;
    }

    @Override
    public String description() {
        return "(mancalaBoard " + nrow + " " + ncol + " store:" + storeType + " numStores:" + nstores +
                " largeStack:" + maker.largeStack() + ")\n";
    }
}
