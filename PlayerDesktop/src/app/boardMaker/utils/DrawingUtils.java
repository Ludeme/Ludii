package app.boardMaker.utils;

import java.awt.*;

public class DrawingUtils {
    /**
     * Draws a text in an horizontally centered box
     * @param g2d Graphics object of the panel
     * @param text The full text to display
     * @param width Width of the panel
     * @param y Coordinate of the start of the box
     */
    public static void horizontallyCenteredText(Graphics2D g2d, String text, int width, int y) {
        FontMetrics metrics = g2d.getFontMetrics();
        for (String line : text.split("\n")) {
            g2d.drawString(line,(width - metrics.stringWidth(line))/2,y += g2d.getFontMetrics().getHeight());
        }
    }
}
