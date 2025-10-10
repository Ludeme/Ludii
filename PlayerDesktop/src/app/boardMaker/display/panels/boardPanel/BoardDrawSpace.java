package app.boardMaker.display.panels.boardPanel;

import app.boardMaker.handlers.Maker;
import app.boardMaker.res.Transformations;
import app.boardMaker.utils.BoardData;
import app.boardMaker.utils.Camera;
import app.utils.SVGUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class BoardDrawSpace extends JPanel {
    private Maker maker;
    private BoardViewListener bvl;
    private BoardViewKeyListener bvkl;
    private Camera camera;

    private boolean createPoly = false;
    private Transformations transformation;

    private List<Point> vertices;
    private Point current;

    private int dotSize = 10;

    public BoardDrawSpace(Maker maker) {
        super(new BorderLayout());

        this.maker = maker;
        this.camera = new Camera(this);
        bvl = new BoardViewListener(maker,this);
        bvkl = new BoardViewKeyListener(maker,this);

        addMouseListener(camera);
        addMouseMotionListener(camera);

        addMouseListener(bvl);
        addKeyListener(bvkl);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, getWidth(), getHeight());

        BoardData currentBoard = maker.getCurrentBoard();
        if (currentBoard != null) {
            maker.drawBoard(currentBoard,this,camera);
            BufferedImage image = SVGUtil.createSVGImage(currentBoard.getSVG(), getWidth(),getHeight());
            g2d.drawImage(image,camera.offX(), -camera.offY(), null);

            if (createPoly) {
                String text = "Press Enter to confirm.\nPress Esc. to cancel.";
                drawText(g2d,text,0,getHeight()-text.split("\n").length * g2d.getFontMetrics().getHeight() - 5);

                if (vertices.size() >= 2) {
                    g2d.setColor(Color.black);
                    for (int i = 0; i < vertices.size(); i++) {
                        g2d.setColor(Color.black);

                        int x1 = vertices.get(i).x + camera.offX();
                        int y1 = vertices.get(i).y - camera.offY();
                        int x2 = vertices.get((i + 1) % vertices.size()).x + camera.offX();
                        int y2 = vertices.get((i + 1) % vertices.size()).y - camera.offY();

                        g2d.setStroke(new BasicStroke(5));
                        g2d.drawLine(x1, y1, x2, y2);
                    }
                }

                for (Point pt : vertices) {
                    if (pt.equals(current)) {
                        g2d.setColor(Color.red);
                    } else {
                        g2d.setColor(Color.blue);
                    }
                    g2d.fillOval(pt.x - dotSize/2 + camera.offX(), pt.y - dotSize/2 - camera.offY(), dotSize,dotSize);
                }
            }
        }
    }

    private void drawText(Graphics2D g2d, String text, int x, int y) {
        FontMetrics metrics = g2d.getFontMetrics();
        for (String line : text.split("\n")) {
            g2d.drawString(line,x + (getWidth() - metrics.stringWidth(line))/2,y += g2d.getFontMetrics().getHeight());
        }
    }

    public void setCreatePoly(boolean b) {
        createPoly = b;
    }

    public boolean createPoly() {
        return createPoly;
    }

    public void setTransformation(Transformations t) {
        transformation = t;
    }

    public Transformations transformation() {
        return transformation;
    }

    public void newPolygon() {
        vertices = new ArrayList<>();
    }

    public void addVertex(Point click) {
        for (Point pt : vertices) {
            if (pt.equals(click) || (Math.sqrt(Math.pow(pt.x - click.x,2) + Math.pow(pt.y - click.y,2))) < dotSize / 2) {
                return;
            }
        }
        vertices.add(click);
        current = click;
        repaint();
    }

    public void removeVertex(Point click) {
        for (Point pt : vertices) {
            if (pt.equals(click) || (Math.sqrt(Math.pow(pt.x - click.x,2) + Math.pow(pt.y - click.y,2))) < dotSize / 2) {
                vertices.remove(pt);
                if (pt.equals(current)) {
                    current = vertices.getLast();
                }
                repaint();
                return;
            }
        }
    }

    public void clearPoly() {
        vertices = null;
        current = null;
        createPoly = false;
        transformation = null;
    }

    public List<Point> poly() {
        return vertices;
    }
}
