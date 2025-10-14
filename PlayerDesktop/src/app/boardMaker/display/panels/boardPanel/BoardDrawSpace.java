package app.boardMaker.display.panels.boardPanel;

import app.boardMaker.handlers.Maker;
import app.boardMaker.res.Transformations;
import app.boardMaker.utils.BoardData;
import app.boardMaker.utils.Camera;
import app.boardMaker.utils.CoordinatesUtil;
import app.utils.SVGUtil;
import game.types.board.SiteType;
import other.topology.TopologyElement;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
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

    private boolean remove = false;
    private SiteType removeType;
    private List<Integer> removedIndices;

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

    public Camera camera() {
        return camera;
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
            } else if (remove) {
                String text = "Press Enter to confirm.\nPress Esc. to cancel.";
                drawText(g2d,text,0,getHeight()-text.split("\n").length * g2d.getFontMetrics().getHeight() - 5);

                List<? extends TopologyElement> elements = currentBoard.getBoard().topology().getGraphElements(removeType);
                for (int i = 0; i < elements.size(); i++) {
                    if (removedIndices.contains(i)) {
                        g2d.setColor(Color.red);
                    } else {
                        g2d.setColor(Color.gray);
                    }
                    Point pt = CoordinatesUtil.screenPosn(elements.get(i).centroid(),currentBoard.getPlacement());
                    g2d.fillOval(pt.x - dotSize/2,pt.y - dotSize/2,dotSize,dotSize);
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
            if (pt.equals(click) || (Math.sqrt(Math.pow(pt.x - click.x,2) + Math.pow(pt.y - click.y,2))) < (double) dotSize / 2) {
                return;
            }
        }
        vertices.add(click);
        current = click;
        repaint();
    }

    public void removeVertex(Point click) {
        for (Point pt : vertices) {
            if (pt.equals(click) || (Math.sqrt(Math.pow(pt.x - click.x,2) + Math.pow(pt.y - click.y,2))) < (double) dotSize / 2) {
                vertices.remove(pt);
                if (pt.equals(current) && !vertices.isEmpty()) {
                    current = vertices.getLast();
                } else {
                    current = null;
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

    public void setRemove(boolean b) {
        remove = b;
    }

    public void setRemoveType(SiteType siteType) {
        removeType = siteType;
    }

    public void clearRemoving() {
        removedIndices = new ArrayList<>();
        if (remove) {
            remove = false;
            removeType = null;
        }
    }

    public boolean isRemoving() {
        return remove;
    }

    public void addIndex(Point click) {
        List<? extends TopologyElement> elements = maker.getCurrentBoard().getBoard().topology().getGraphElements(removeType);

        for (int i = 0; i < elements.size(); i++) {
            Point2D cent = elements.get(i).centroid();
            Point pt = CoordinatesUtil.screenPosn(cent,maker.getCurrentBoard().getPlacement());
            if ((Math.sqrt(Math.pow(pt.getX() - click.x,2) + Math.pow(pt.getY() - click.y,2))) < (double) dotSize / 2) {
                if (!removedIndices.contains(i)) {
                    removedIndices.add(i);
                    repaint();
                }
                return;
            }
        }
    }

    public void removeIndex(Point click) {
        List<? extends TopologyElement> elements = maker.getCurrentBoard().getBoard().topology().getGraphElements(removeType);

        for (int i = 0; i < elements.size(); i++) {
            Point2D cent = elements.get(i).centroid();
            Point pt = CoordinatesUtil.screenPosn(cent,maker.getCurrentBoard().getPlacement());
            if ((Math.sqrt(Math.pow(pt.getX() - click.x,2) + Math.pow(pt.getY() - click.y,2))) < (double) dotSize / 2) {
                if (removedIndices.contains(i)) {
                    removedIndices.remove((Integer) i);
                    repaint();
                }
                return;
            }
        }
    }

    public List<Integer> indices() {
        return removedIndices;
    }

    public SiteType removedType() {
        return removeType;
    }
}
