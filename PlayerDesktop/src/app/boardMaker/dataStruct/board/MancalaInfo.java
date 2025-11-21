package app.boardMaker.dataStruct.board;

import app.boardMaker.handlers.Maker;
import app.boardMaker.utils.BoardUtils;
import app.boardMaker.utils.Camera;
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
import java.awt.geom.Point2D;

public class MancalaInfo implements ContainerInfo{
    private int nrow;
    private int ncol;
    private StoreType storeType;
    private int nstores;

    private MancalaBoard board;
    private Context context;

    private String boardSVG;

    private int cellRadius;
    private double boardScale;
    private Rectangle boardPlacement;
    private BoardRange range;

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
        return String.format("(mancalaBoard %d %d store:%s numStores:%d%s)\n",nrow,ncol,storeType,nstores
                ,maker.largeStack() ? " largeStack:true" : "");
    }

    @Override
    public String getSVG(SiteType site) {
        return boardSVG;
    }

    @Override
    public Board board() {
        return board;
    }

    @Override
    public String optionalMetadata() {
        return "(metadata \n(graphics {\n(board Style Mancala)\n})\n)";
    }

    private void createBoard() {
        board = new MancalaBoard(nrow,ncol,storeType,nstores,maker.largeStack(),null,null);
        buildBoard();
    }

    @Override
    public BoardRange range() {
        return range;
    }

    @Override
    public double cellRadius() {
        return cellRadius;
    }

    @Override
    public double scale() {
        return boardScale;
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
        boardScale = gameStyle.containerScale();

        Point2D center = new Point2D.Double(0.5,0.5);
        Point origin = new Point(0,0);

        Rectangle realPlacement = new Rectangle((int) (origin.getX() + placement.getWidth() * (1.0 - boardScale) * center.getX()),
                (int) (origin.getY() + placement.getHeight() * (1.0 - boardScale) * center.getY()),
                (int) (placement.getWidth() * boardScale),
                (int) (placement.getHeight() * boardScale));
        setPlacement(realPlacement);

        range = BoardUtils.computeRange(board.graph());
    }

    @Override
    public void setPlacement(Rectangle p) {
        this.boardPlacement = p;
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public Rectangle placement() {
        return boardPlacement;
    }

    @Override
    public void shiftPlacement(Camera c,Container view) {
        int ox = 0, oy = 0;
        if (c != null) {
            ox = c.offX();
            oy = -c.offY();
        }
        int boardsize = Math.min(view.getHeight(),
                view.getWidth());
        boardPlacement = new Rectangle((int) (ox + boardsize * (1.0 - boardScale) * 0.5),
                (int) (oy + boardsize * (1.0 - boardScale) * 0.5),
                (int) (boardsize * boardScale),
                (int) (boardsize * boardScale));
    }
}
