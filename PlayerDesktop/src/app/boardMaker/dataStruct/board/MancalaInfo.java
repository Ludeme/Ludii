package app.boardMaker.dataStruct.board;

import app.boardMaker.handlers.Maker;
import bridge.Bridge;
import game.Game;
import game.equipment.Equipment;
import game.equipment.Item;
import game.equipment.container.board.Board;
import game.equipment.container.board.custom.MancalaBoard;
import game.mode.Mode;
import game.players.Players;
import game.types.board.SiteType;
import game.types.board.StoreType;
import other.context.Context;
import other.trial.Trial;
import util.PlaneType;
import view.container.ContainerStyle;
import view.container.styles.BoardStyle;
import view.container.styles.board.MancalaStyle;
import view.container.styles.board.graph.GraphStyle;

import java.awt.*;

public class MancalaInfo implements ContainerInfo{
    private int nrow;
    private int ncol;
    private StoreType storeType;
    private int nstores;

    private MancalaBoard board;
    private Context context;
    private String boardSVG;
    private int cellRadius;

    private Maker maker;

    public MancalaInfo(Maker maker, int row, int col, StoreType type, int stores) {
        this.maker = maker;

        nrow = row;
        ncol = col;
        storeType = type;
        nstores = stores;

        createBoard();
    }

    @Override
    public String description() {
        return "(mancalaBoard " + nrow + " " + ncol + " store:" + storeType + " numStores:" + nstores +
                " largeStack:" + maker.largeStack() + ")\n";
    }

    @Override
    public String getSVG(SiteType site) {
        return boardSVG;
    }

    @Override
    public Board board() {
        return board;
    }

    private void createBoard() {
        board = new MancalaBoard(nrow,ncol,storeType,nstores,maker.largeStack(),null,null);
        buildBoard();
    }

    private void buildBoard() {
        Game game = new Game(maker.getName(), new Players(maker.getPlayers()), new Mode(maker.getMode()),
                new Equipment(new Item[] {board}), null);
        game.create();

        game.setMetadata(null);

        context = new Context(game, new Trial(game));
        Bridge bridge = new Bridge();

        int boardsize = Math.min(maker.getDisplayer().getBoardPanel().getHeight(),
                maker.getDisplayer().getBoardPanel().getWidth());
        Rectangle placement = new Rectangle(0,0,boardsize,boardsize);
        ContainerStyle gameStyle = new MancalaStyle(bridge,board);
        gameStyle.setPlacement(context,placement);
        gameStyle.render(PlaneType.BOARD,context);
        boardSVG = gameStyle.containerSVGImage();

        cellRadius = gameStyle.cellRadiusPixels();
    }
}
