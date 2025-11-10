package app.boardMaker.dataStruct.board;

import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.handlers.Maker;
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

public class SurakartaInfo implements ContainerInfo{
    private GraphInfo graphFunction;
    private int nloops;
    private int startLoops;

    private SurakartaBoard board;
    private Context context;
    private String boardSVG;
    private int cellRadius;

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
    }
}
