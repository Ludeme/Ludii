package app.boardMaker.utils;

import app.boardMaker.dataStruct.board.BoardRange;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Class providing functions to convert coordinates between different system.
 * IE: absolute graph coordinates, screen coordinates, board coordinates (normalized and centered on the board)
 */
public class CoordinatesUtil {
    /**
     * Compute the position on the screen of a normalised and centered point on the board
     * @param boardPos the position on the board plane
     * @param placement the placement of the board
     * @return the position on the screen
     */
    public static Point screenPosn(Point2D boardPos, Rectangle placement) {
        Point screenPos = new Point();

        screenPos.x = (int) (placement.x + boardPos.getX() * placement.width);
        screenPos.y = (int) ((placement.y * 2 + placement.height) - (placement.y + boardPos.getY() * placement.height));

        return screenPos;
    }

    /**
     * Computes the position of a point on the screen in the plane of the board
     * @param screenPos the coordinates on the screen
     * @param placement the placement of the board
     * @param range the range of the board
     * @param camera the camera of the view
     * @return
     */
    public static Point2D boardPosn(Point screenPos, Rectangle placement, BoardRange range, Camera camera) {
        Point2D centeredPos = new Point2D.Double();

        if (camera == null) {
            camera = new Camera(null);
        }
        
        centeredPos.setLocation((screenPos.getX() + camera.offX() - placement.x) / placement.getWidth(),
                (placement.y + placement.getHeight() - screenPos.getY() + camera.offY()) / placement.getHeight());

        double maxX_n = normalize(range.getMaxX(), range.getMin(), range.getMax());
        double maxY_n = normalize(range.getMaxY(), range.getMin(), range.getMax());
        double minX_n = normalize(range.getMinX(), range.getMin(), range.getMax());
        double minY_n = normalize(range.getMinY(), range.getMin(), range.getMax());

        Point2D normedPos = new Point2D.Double();
        normedPos.setLocation(centeredPos.getX() + ((maxX_n + minX_n)/ 2.0 - 0.5),
                centeredPos.getY() + ((maxY_n + minY_n) / 2.0 - 0.5));

        Point2D boardPos = new Point2D.Double();
        boardPos.setLocation(normedPos.getX() * (range.getMax() - range.getMin()) + range.getMin(),
                normedPos.getY() * (range.getMax() - range.getMin()) + range.getMin());

        return boardPos;
    }

    public static double normalize(double x, double min, double max) {
        return (x - min)/(max - min);
    }

    /**
     * Converts a point from the graph coordinates system to the board coordinates system
     * @param pos the point to scale
     * @param scale the scale range
     * @return the position of the point relative to the board
     */
    public static Point2D normalizeThenCenter(Point2D pos, BoardRange scale) {
        Point2D.Double normalized = new Point2D.Double(normalize(pos.getX(), scale.getMin(), scale.getMax())
                ,normalize(pos.getY(), scale.getMin(), scale.getMax()));

        double maxX_n = normalize(scale.getMaxX(), scale.getMin(), scale.getMax());
        double maxY_n = normalize(scale.getMaxY(), scale.getMin(), scale.getMax());
        double minX_n = normalize(scale.getMinX(), scale.getMin(), scale.getMax());
        double minY_n = normalize(scale.getMinY(), scale.getMin(), scale.getMax());

        return new Point2D.Double(normalized.x - ((maxX_n + minX_n) / 2.0 - 0.5)
                ,normalized.y - ((maxY_n + minY_n) / 2.0 - 0.5));
    }
}
