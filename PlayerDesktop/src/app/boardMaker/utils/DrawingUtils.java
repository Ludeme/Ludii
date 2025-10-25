package app.boardMaker.utils;

import java.awt.*;

public class DrawingUtils {
    public static void horizontallyCenteredText(Graphics2D g2d, String text, int width, int y) {
        FontMetrics metrics = g2d.getFontMetrics();
        for (String line : text.split("\n")) {
            g2d.drawString(line,(width - metrics.stringWidth(line))/2,y += g2d.getFontMetrics().getHeight());
        }
    }
}
