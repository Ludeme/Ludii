package app.boardMaker.utils;

import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.equipment.container.board.Track;
import game.functions.graph.GraphFunction;
import game.util.graph.Graph;
import main.collections.Pair;

import java.awt.geom.Point2D;

public class BoardUtils {
    public static Board copyBoard(Board old, Maker maker) {
        Board newBoard;
        if (old.tracks().isEmpty()) {
            newBoard = new Board(old.graphFunction(), null,
                    null,null,null,
                    maker.getSiteType(),maker.largeStack());
        } else {
            newBoard = new Board(old.graphFunction(), old.tracks().getFirst(),
                    old.tracks().toArray(new Track[0]),null,null,
                    maker.getSiteType(),maker.largeStack());
        }

        return newBoard;
    }

    public static Board change_function(GraphFunction newFunc, Board old, Maker maker) {
        Board newBoard;
        if (old.tracks().isEmpty()) {
            newBoard = new Board(newFunc,null,
                    null,null,null,
                    maker.getSiteType(),maker.largeStack());
        } else {
            newBoard = new Board(newFunc,old.tracks().getFirst(),
                    old.tracks().toArray(new Track[0]),null,null,
                    maker.getSiteType(),maker.largeStack());
        }

        return newBoard;
    }

    public static BoardRange computeRange(Graph graph) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < graph.vertices().size(); i++) {
            final Point2D centroid = graph.vertices().get(i).pt2D();

            final double cx = centroid.getX();
            final double cy = centroid.getY();

            if (cx < minX)
                minX = cx;
            if (cy < minY)
                minY = cy;

            if (cx > maxX)
                maxX = cx;
            if (cy > maxY)
                maxY = cy;
        }

        return new BoardRange(minX,maxX,minY,maxY);
    }
}
