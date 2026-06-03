package app.boardMaker.utils;

import app.boardMaker.dataStruct.board.BoardRange;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.equipment.container.board.Track;
import game.equipment.container.board.custom.MancalaBoard;
import game.equipment.container.board.custom.SurakartaBoard;
import game.functions.graph.GraphFunction;
import game.util.graph.Graph;

import java.awt.geom.Point2D;

public class BoardUtils {
    /**
     * Computes the range of the graph (ie: min and max values along x and y axis)
     * @param graph the graph to compute range from
     * @return the range of the graph
     */
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
