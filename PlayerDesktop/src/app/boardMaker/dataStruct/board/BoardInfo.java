package app.boardMaker.dataStruct.board;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.handlers.Maker;
import app.boardMaker.utils.BoardUtils;
import app.boardMaker.utils.Camera;
import bridge.Bridge;
import game.Game;
import game.equipment.Equipment;
import game.equipment.Item;
import game.equipment.container.board.Board;
import game.mode.Mode;
import game.players.Players;
import game.types.board.SiteType;
import other.context.Context;
import other.trial.Trial;
import util.PlaneType;
import view.container.ContainerStyle;
import view.container.styles.BoardStyle;
import view.container.styles.board.graph.GraphStyle;

import java.awt.*;
import java.awt.geom.Point2D;

public class BoardInfo implements ContainerInfo{
    private GraphInfo graphInfo;

    private Board board;
    private Context context;

    private String boardSVG;
    private String graphSVG;

    private int cellRadius;
    private double boardScale;
    private Rectangle boardPlacement;
    private BoardRange range;

    private Maker maker;

    public BoardInfo(Maker maker, GraphInfo graphInfo) {
        this.maker = maker;
        this.graphInfo = graphInfo;
        createBoard();
    }

    public BoardInfo(Maker maker) {
        this.maker = maker;
    }

    public BoardInfo() {
    }

    @Override
    public String description() {
        return "(board " + graphInfo.description() + " use:" + maker.getSiteType()
                + " largeStack:" + maker.largeStack() + ")\n";
    }

    public void setGraphInfo(GraphInfo graphFunction) {
        this.graphInfo = graphFunction;
        createBoard();
    }

    public GraphInfo getGraphInfo() {
        return graphInfo;
    }

    public Board getBoard() {
        return board;
    }

    public int getCellRadius() {
        return cellRadius;
    }

    public String getSVG(SiteType type) {
        if (type == SiteType.Cell) {
            return boardSVG;
        } else {
            return graphSVG;
        }
    }

    @Override
    public Board board() {
        return board;
    }

    @Override
    public String optionalMetadata() {
        return "";
    }

    private void createBoard() {
        board = new Board(graphInfo.function(),null,null,null,null,
                maker.getSiteType(),maker.largeStack());
        buildBoard();
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public Rectangle placement() {
        return boardPlacement;
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
        ContainerStyle gameStyle = new BoardStyle(bridge,board);
        gameStyle.setPlacement(context,placement);
        gameStyle.render(PlaneType.BOARD,context);
        boardSVG = gameStyle.containerSVGImage();
        gameStyle = new GraphStyle(bridge,board,context);
        gameStyle.setPlacement(context,placement);
        gameStyle.render(PlaneType.BOARD,context);
        graphSVG = gameStyle.containerSVGImage();

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
    public void shiftPlacement(Camera c, Container view) {
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
}
