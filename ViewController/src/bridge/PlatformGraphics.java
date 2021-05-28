package bridge;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import other.context.Context;
import other.location.Location;
import util.ImageInfo;

/**
 * Interface for linking with the Graphics of different platforms (e.g. PlayerDesktop/DesktopGraphics)
 * 
 * @author Matthew.Stephenson
 */
public interface PlatformGraphics 
{
	/**
	 * Returns the Full location of the component associated with the image clicked on.
	 */
	Location locationOfClickedImage(Point pt);
	
	/**
	 * Draws a component based on the specified ImageInfo.
	 * @param g2d
	 * @param context
	 * @param imageInfo
	 */
	void drawComponent(Graphics2D g2d, final Context context, ImageInfo imageInfo);
	
	/**
	 * Draws the game board.
	 * @param g2d
	 * @param boardDimensions
	 */
	void drawBoard(final Context context, Graphics2D g2d, Rectangle2D boardDimensions);
	
	/**
	 * Draws the game board's graph.
	 * @param g2d
	 * @param boardDimensions
	 */
	void drawGraph(final Context context, Graphics2D g2d, Rectangle2D boardDimensions);
	
	/**
	 * 
	 * @param g2d
	 * @param boardDimensions
	 */
	void drawConnections(final Context context, Graphics2D g2d, Rectangle2D boardDimensions);

	/**
	 * Draws a single specified image.
	 * @param componentStyle 
	 */
	void drawSVG(final Context context, Graphics2D g2d, SVGGraphics2D svg, ImageInfo imageInfo);
}
