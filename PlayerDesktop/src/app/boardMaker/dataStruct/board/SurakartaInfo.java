package app.boardMaker.dataStruct.board;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.handlers.Maker;
import app.boardMaker.utils.Camera;
import bridge.Bridge;
import game.Game;
import game.equipment.Equipment;
import game.equipment.Item;
import game.equipment.container.board.Board;
import game.equipment.container.board.custom.SurakartaBoard;
import game.mode.Mode;
import game.players.Players;
import game.types.board.SiteType;
import other.context.Context;
import other.trial.Trial;
import util.PlaneType;
import view.container.ContainerStyle;
import view.container.styles.board.MancalaStyle;
import view.container.styles.board.SurakartaStyle;

import java.awt.*;
import java.awt.geom.Point2D;

public class SurakartaInfo implements ContainerInfo{
    private GraphInfo graphFunction;
    private int nloops;
    private int startLoops;

    private SurakartaBoard board;
    private Context context;

    private String boardSVG;

    private int cellRadius;
    private double boardScale;
    private Rectangle placement;

    private Maker maker;

    public SurakartaInfo(Maker maker, GraphInfo graphFunction, int nloops, int from) {
        this.maker = maker;
        this.graphFunction = graphFunction;
        this.nloops = nloops;
        this.startLoops = from;

        createBoard();
    }

    @Override
    public String description() {
        return "(surakartaBoard " + graphFunction.description() + " loops:" + nloops + " from:" + startLoops +
                " largeStack: " + maker.largeStack() + ")\n";
    }

    @Override
    public Board board() {
        return board;
    }

    @Override
    public String optionalMetadata() {
        return "(metadata \n(graphics {\n(board Style Surakarta)\n})\n)";
    }

    @Override
    public String getSVG(SiteType site) {
        return boardSVG;
    }

    private void createBoard() {
        board = new SurakartaBoard(graphFunction.function(),nloops,startLoops,maker.largeStack());
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
        ContainerStyle gameStyle = new SurakartaStyle(bridge,board);
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
    }

    @Override
    public void setPlacement(Rectangle p) {
        this.placement = p;
    }

    @Override
    public void shiftPlacement(Camera c) {
        placement = new Rectangle((int) (c.offX() + placement.getWidth()),
                (int) (-c.offY() + placement.getHeight()),
                (int) placement.getWidth(),
                (int) placement.getHeight());
    }
}
