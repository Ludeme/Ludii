package app.boardMaker.display.panels.boardPanel;

import app.boardMaker.handlers.Maker;
import app.boardMaker.res.Transformations;
import app.boardMaker.utils.BoardUtils;
import app.boardMaker.utils.CoordinatesUtil;
import game.equipment.container.board.Board;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Clip;
import game.functions.graph.operators.Hole;
import game.functions.graph.operators.Keep;
import game.util.graph.Poly;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.List;

public class BoardViewKeyListener extends KeyAdapter {
    private Maker maker;
    private BoardDrawSpace view;

    public BoardViewKeyListener(Maker maker, BoardDrawSpace view) {
        this.maker = maker;
        this.view = view;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_ENTER) {
            apply_transformation(view.transformation(),view.poly());
            view.clearPoly();
            view.repaint();
        } else if (key == KeyEvent.VK_ESCAPE) {
            view.clearPoly();
            view.repaint();
        }
    }

    private void apply_transformation(Transformations transformation, List<Point> vertices) {
        if (transformation == Transformations.Clip) {
            Board oldBoard = maker.getCurrentBoard().getBoard();
            Poly poly = makePolygon(vertices);
            GraphFunction newFunction = new Clip(oldBoard.graphFunction(),poly);
            update_board(newFunction,oldBoard);
        } else if (transformation == Transformations.Hole) {
            Board oldBoard = maker.getCurrentBoard().getBoard();
            Poly poly = makePolygon(vertices);
            GraphFunction newFunction = new Hole(oldBoard.graphFunction(),poly);
            update_board(newFunction,oldBoard);
        } else if (transformation == Transformations.Keep) {
            Board oldBoard = maker.getCurrentBoard().getBoard();
            Poly poly = makePolygon(vertices);
            GraphFunction newFunction = new Keep(oldBoard.graphFunction(),poly);
            update_board(newFunction,oldBoard);
        }
    }

    private Poly makePolygon(List<Point> vertices) {
        Float[][] pts = new Float[vertices.size()][2];

        for (int i = 0; i < vertices.size(); i++) {
            Point2D pt = CoordinatesUtil.boardPosn(vertices.get(i),maker.getCurrentBoard().getPlacement(),maker.getCurrentBoard().getBoard().graph());

            pts[i][0] = (float) pt.getX();
            pts[i][1] = (float) pt.getY();
            System.out.println(pts[i][0]+" "+pts[i][1]);
        }
        return new Poly(pts, null);
    }

    private void update_board(GraphFunction function, Board old) {
        Board newBoard = BoardUtils.change_function(function,old,maker);
        maker.getCurrentBoard().setBoard(newBoard);
        maker.getDisplayer().getBoardPanel().repaint();
    }
}
