package processing.kmedoid;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;

/**
 * creates a panel wich shows a line graph
 * source https://stackoverflow.com/questions/8693342/drawing-a-simple-line-graph-in-java
 * @author Markus
 *
 */
public class LineDrawer extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
    private final int padding = 25;
    private final int labelPadding = 25;
    private final Color lineColor = new Color(44, 102, 230, 180);
    private final Color pointColor = new Color(100, 100, 100, 180);
    private final Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private final int pointWidth = 4;
    private final int numberYDivisions = 10;
    private final List<LineDrawable> clusterings;

    
    public LineDrawer(final ArrayList<LineDrawable> clusterings)
	{	
    	this.clusterings = new ArrayList<LineDrawable>(clusterings);
    	Collections.sort(clusterings,Comparator.comparingInt(LineDrawable::getX));
    	
	}
   

	@Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double maxScore = getMaxScore();
        double minScore = getMinScore();
        minScore = minScore;
        //maxScore = minScore+1000;
        final double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (clusterings.size() - 1);
        final double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (maxScore- minScore);

        final List<Point> graphPoints = new ArrayList<>();
        for (int i = 0; i < clusterings.size(); i++) {
            final int x1 = (int) (i * xScale + padding + labelPadding);
            final int y1 = (int) ((maxScore - clusterings.get(i).getY()) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
        }

        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        // create hatch marks and grid lines for y axis.
        for (int i = 0; i < numberYDivisions + 1; i++) {
            final int x0 = padding + labelPadding;
            final int x1 = pointWidth + padding + labelPadding;
            final int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            final int y1 = y0;
            if (clusterings.size() > 0) {
                g2.setColor(gridColor);
                g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
                g2.setColor(Color.BLACK);
                int yLabelInt = (int) ((int) (((minScore + (maxScore - minScore) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0);
                final String yLabel =  yLabelInt+ "";
                final FontMetrics metrics = g2.getFontMetrics();
                final int labelWidth = metrics.stringWidth(yLabel);
                g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
            }
            g2.drawLine(x0, y0, x1, y1);
        }

        // and for x axis
        for (int i = 0; i < clusterings.size(); i++) {
            if (clusterings.size() > 1) {
                final int x0 = i * (getWidth() - padding * 2 - labelPadding) / (clusterings.size() - 1) + padding + labelPadding;
                final int x1 = x0;
                final int y0 = getHeight() - padding - labelPadding;
                final int y1 = y0 - pointWidth;
                if ((i % ((int) ((clusterings.size() / 20.0)) + 1)) == 0) {
                    g2.setColor(gridColor);
                    g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
                    g2.setColor(Color.BLACK);
                    final String xLabel = clusterings.get(i).getX() + "";
                    final FontMetrics metrics = g2.getFontMetrics();
                    final int labelWidth = metrics.stringWidth(xLabel);
                    g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                }
                g2.drawLine(x0, y0, x1, y1);
            }
        }

        // create x and y axes 
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        final Stroke oldStroke = g2.getStroke();
        g2.setColor(lineColor);
        g2.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            final int x1 = graphPoints.get(i).x;
            final int y1 = graphPoints.get(i).y;
            final int x2 = graphPoints.get(i + 1).x;
            final int y2 = graphPoints.get(i + 1).y;
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(oldStroke);
        g2.setColor(pointColor);
        for (int i = 0; i < graphPoints.size(); i++) {
            final int x = graphPoints.get(i).x - pointWidth / 2;
            final int y = graphPoints.get(i).y - pointWidth / 2;
            final int ovalW = pointWidth;
            final int ovalH = pointWidth;
            g2.fillOval(x, y, ovalW, ovalH);
        }
    }

//    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension(width, heigth);
//    }
    private double getMinScore() {
        double minScore = Double.MAX_VALUE;
        for (final LineDrawable clustering : clusterings)
		{
        	minScore = Math.min(minScore, clustering.getY());
		}
      
        return minScore;
    }

    private double getMaxScore() {
        double maxScore = Double.MIN_VALUE;
        for (final LineDrawable clustering : clusterings) {
            maxScore = Math.max(maxScore, clustering.getY());
        }
        return maxScore;
    }

}
