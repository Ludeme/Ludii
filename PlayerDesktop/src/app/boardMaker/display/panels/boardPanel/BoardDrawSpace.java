package app.boardMaker.display.panels.boardPanel;

import app.boardMaker.dataStruct.board.BoardData;
import app.boardMaker.dataStruct.board.BoardRange;
import app.boardMaker.dataStruct.state.BoardMakerState;
import app.boardMaker.handlers.Maker;
import app.boardMaker.res.Transformations;
import app.boardMaker.utils.*;
import app.utils.SVGUtil;
import game.types.board.SiteType;
import other.topology.TopologyElement;
import other.topology.Vertex;

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

    private boolean adding = false;
    protected SiteType addedType;
    protected List<Point2D> addedVertices;
    protected List<Integer[]> addedEdges;
    private Integer[] curEdge;
    private List<Integer> curCell;
    protected List<List<Integer>> addedCells;

    protected int dotSize = 10;


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
            /*maker.drawBoard(currentBoard,this,camera);
            BufferedImage image = SVGUtil.createSVGImage(currentBoard.getSVG(), getWidth(),getHeight());
            g2d.drawImage(image,camera.offX(), -camera.offY(), null);

            if (createPoly) {
                String text = "Press Enter to confirm.\nPress Esc. to cancel.";
                DrawingUtils.horizontallyCenteredText(g2d,text,getWidth(),getHeight()-text.split("\n").length * g2d.getFontMetrics().getHeight() - 5);

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
                DrawingUtils.horizontallyCenteredText(g2d,text,getWidth(),getHeight()-text.split("\n").length * g2d.getFontMetrics().getHeight() - 5);

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
            } else if (adding) {
                String text = "Press Enter to confirm.\nPress Esc. to cancel.";
                if (addedType == SiteType.Cell) {
                    text = "Click on the first vertex of a cell to close it.\n" + text;
                }
                DrawingUtils.horizontallyCenteredText(g2d,text,getWidth(),getHeight()-text.split("\n").length * g2d.getFontMetrics().getHeight() - 5);

                List<Vertex> elements = currentBoard.getBoard().topology().vertices();
                BoardRange scale = BoardUtils.computeRange(currentBoard.getBoard().graph());

                g2d.setColor(Color.black);
                g2d.setStroke(new BasicStroke(3));
                for (Integer[] edge : addedEdges) {
                    Point[] pts = new Point[2];
                    for (int j = 0; j < edge.length; j++) {
                        if (edge[j] < elements.size()) {
                            pts[j] = CoordinatesUtil.screenPosn(elements.get(edge[j]).centroid(), currentBoard.getPlacement());
                        } else {
                            pts[j] = CoordinatesUtil.screenPosn(CoordinatesUtil.normalizeThenCenter(addedVertices.get(edge[j] - elements.size()), scale), currentBoard.getPlacement());
                        }
                    }
                    g2d.drawLine(pts[0].x, pts[0].y, pts[1].x, pts[1].y);
                }
                g2d.setColor(Color.black);
                for (List<Integer> cell : addedCells) {
                    for (int i = 0; i < cell.size(); i++) {
                        Point[] pts = new Point[2];
                        int id1 = cell.get(i), id2 = i == cell.size()-1 ? cell.getFirst() : cell.get(i+1);
                        if (id1 < elements.size()) {
                            pts[0] = CoordinatesUtil.screenPosn(elements.get(id1).centroid(), currentBoard.getPlacement());
                        } else {
                            pts[0] = CoordinatesUtil.screenPosn(CoordinatesUtil.normalizeThenCenter(addedVertices.get(id1 - elements.size()), scale), currentBoard.getPlacement());
                        }
                        if (id2 < elements.size()) {
                            pts[1] = CoordinatesUtil.screenPosn(elements.get(id2).centroid(), currentBoard.getPlacement());
                        } else {
                            pts[1] = CoordinatesUtil.screenPosn(CoordinatesUtil.normalizeThenCenter(addedVertices.get(id2 - elements.size()), scale), currentBoard.getPlacement());
                        }
                        g2d.drawLine(pts[0].x, pts[0].y, pts[1].x, pts[1].y);
                    }
                }
                g2d.setColor(Color.yellow);
                for (int i = 0; curCell != null && i < curCell.size() - 1; i++) {
                    Point[] pts = new Point[2];
                    int id1 = curCell.get(i), id2 = curCell.get(i+1);
                    if (id1 < elements.size()) {
                        pts[0] = CoordinatesUtil.screenPosn(elements.get(id1).centroid(), currentBoard.getPlacement());
                    } else {
                        pts[0] = CoordinatesUtil.screenPosn(CoordinatesUtil.normalizeThenCenter(addedVertices.get(id1 - elements.size()), scale), currentBoard.getPlacement());
                    }
                    if (id2 < elements.size()) {
                        pts[1] = CoordinatesUtil.screenPosn(elements.get(id2).centroid(), currentBoard.getPlacement());
                    } else {
                        pts[1] = CoordinatesUtil.screenPosn(CoordinatesUtil.normalizeThenCenter(addedVertices.get(id2 - elements.size()), scale), currentBoard.getPlacement());
                    }
                    g2d.drawLine(pts[0].x, pts[0].y, pts[1].x, pts[1].y);
                }

                for (int i = 0; i < elements.size(); i++) {
                    if ((curEdge != null && curEdge[0] == i) || (curCell != null && curCell.getLast() == i)) {
                        g2d.setColor(Color.blue);
                    } else if (curCell != null && curCell.getFirst() == i) {
                        g2d.setColor(Color.red);
                    } else {
                        g2d.setColor(Color.gray);
                    }
                    Point pt = CoordinatesUtil.screenPosn(elements.get(i).centroid(), currentBoard.getPlacement());
                    g2d.fillOval(pt.x - dotSize / 2, pt.y - dotSize / 2, dotSize, dotSize);
                }

                for (int i = 0; i < addedVertices.size(); i++) {
                    if ((curEdge != null && curEdge[0] - elements.size() == i) || (curCell != null && curCell.getLast() - elements.size() == i)) {
                        g2d.setColor(Color.blue);
                    } else if (curCell != null && curCell.getFirst() - elements.size() == i) {
                        g2d.setColor(Color.red);
                    } else {
                        g2d.setColor(Color.green);
                    }
                    Point pt = CoordinatesUtil.screenPosn(CoordinatesUtil.normalizeThenCenter(addedVertices.get(i),scale),currentBoard.getPlacement());
                    g2d.fillOval(pt.x - dotSize/2,pt.y - dotSize/2,dotSize,dotSize);
                }
            }*/
        }

        BoardMakerState state = maker.state();
        if (state.currentBoardInfo().board() == null) {
            return;
        }
        BufferedImage image = SVGUtil.createSVGImage(state.currentSVG(maker.shownSite()), getWidth(),getHeight());
        g2d.drawImage(image,camera.offX(), -camera.offY(), null);
        state.currentBoardInfo().shiftPlacement(camera);
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

    public void addPolygonVertex(Point click) {
        for (Point pt : vertices) {
            if (pt.equals(click) || (Math.sqrt(Math.pow(pt.x - click.x,2) + Math.pow(pt.y - click.y,2))) < (double) dotSize / 2) {
                return;
            }
        }
        vertices.add(click);
        current = click;
        repaint();
    }

    public void removePolygonVertex(Point click) {
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
            if ((Math.sqrt(Math.pow(pt.getX() - click.x - camera().offX(),2) + Math.pow(pt.getY() - click.y + camera.offY(),2))) < (double) dotSize / 2) {
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
            if ((Math.sqrt(Math.pow(pt.getX() - click.x - camera().offX(),2) + Math.pow(pt.getY() - click.y + camera.offY(),2))) < (double) dotSize / 2) {
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

    public void setAdding(boolean b) {
        adding = b;
    }

    public void setAddType(SiteType siteType) {
        addedType = siteType;
    }

    public boolean isAdding() {
        return adding;
    }

    public void addVertex(Point click) {
        Point2D coord = CoordinatesUtil.boardPosn(click,maker.getCurrentBoard().getPlacement(),maker.getCurrentBoard().getBoard().graph(),camera);
        BoardRange scale = BoardUtils.computeRange(maker.getCurrentBoard().getBoard().graph());
        List<Vertex> elements = maker.getCurrentBoard().getBoard().topology().vertices();
        if (addedType == SiteType.Vertex) {
            for (int i = 0; i < addedVertices.size(); i++) {
                Point screen = CoordinatesUtil.screenPosn(CoordinatesUtil.normalizeThenCenter(addedVertices.get(i),scale),maker.getCurrentBoard().getPlacement());
                if (click.distance(screen) < (double) dotSize /2) {
                    return;
                }
            }
            addedVertices.add(coord);
            repaint();
        } else if (addedType == SiteType.Edge) {
            //Click on vertex of the board
            for (int i = 0; i < elements.size(); i++) {
                Point screen = CoordinatesUtil.screenPosn(elements.get(i).centroid(),maker.getCurrentBoard().getPlacement());
                screen.x = screen.x - camera.offX();
                screen.y = screen.y + camera.offY();
                if (click.distance(screen) < (double) dotSize / 2) {
                    if (curEdge == null) {
                        curEdge = new Integer[2];
                        curEdge[0] = i;
                        repaint();
                        return;
                    } else {
                        if (curEdge[0] != i) {
                            curEdge[1] = i;
                            addedEdges.add(curEdge);
                            curEdge = null;
                            repaint();
                        }
                        return;
                    }
                }
            }
            //Click outside the board
            for (int i = 0; i < addedVertices.size(); i++) {
                Point screen = CoordinatesUtil.screenPosn(CoordinatesUtil.normalizeThenCenter(addedVertices.get(i),scale),maker.getCurrentBoard().getPlacement());
                screen.x = screen.x - camera.offX();
                screen.y = screen.y + camera.offY();
                // On an already added vertex
                if (click.distance(screen) < (double) dotSize /2) {
                    if (curEdge == null) {
                        curEdge = new Integer[2];
                        curEdge[0] = i + elements.size();
                        repaint();
                        return;
                    } else {
                        if (curEdge[0] != i + elements.size()) {
                            curEdge[1] = i + elements.size();
                            addedEdges.add(curEdge);
                            curEdge = null;
                            repaint();
                        }
                        return;
                    }
                }
            }
            // New vertex
            int index = addedVertices.size();
            addedVertices.add(coord);
            if (curEdge == null) {
                curEdge = new Integer[2];
                curEdge[0] = index + elements.size();
                repaint();
            } else {
                if (curEdge[0] != index + elements.size()) {
                    curEdge[1] = index + elements.size();
                    addedEdges.add(curEdge);
                    curEdge = null;
                    repaint();
                }
            }
        } else if (addedType == SiteType.Cell) {
            // Vertex of the board
            for (int i = 0; i < elements.size(); i++) {
                Point screen = CoordinatesUtil.screenPosn(elements.get(i).centroid(),maker.getCurrentBoard().getPlacement());
                screen.x = screen.x - camera.offX();
                screen.y = screen.y + camera.offY();
                if (click.distance(screen) < (double) dotSize / 2) {
                    if (curCell == null) {
                        curCell = new ArrayList<>();
                        curCell.add(i);
                        repaint();
                        return;
                    } else {
                        if (!curCell.contains(i)) {
                            curCell.add(i);
                            repaint();
                        } else if (curCell.getFirst() == i) {
                            addedCells.add(curCell);
                            curCell = null;
                            repaint();
                        }
                        return;
                    }
                }
            }
            // Vertex already present outside the board
            for (int i = 0; i < addedVertices.size(); i++) {
                Point screen = CoordinatesUtil.screenPosn(CoordinatesUtil.normalizeThenCenter(addedVertices.get(i),scale),maker.getCurrentBoard().getPlacement());
                screen.x = screen.x - camera.offX();
                screen.y = screen.y + camera.offY();
                if (click.distance(screen) < (double) dotSize /2) {
                    if (curCell == null) {
                        curCell = new ArrayList<>();
                        curCell.add(i + elements.size());
                        repaint();
                        return;
                    } else {
                        if (!curCell.contains(i + elements.size())) {
                            curCell.add(i + elements.size());
                            repaint();
                        } else if (curCell.getFirst() == i + elements.size()) {
                            addedCells.add(curCell);
                            curCell = null;
                            repaint();
                        }
                        return;
                    }
                }
            }
            // New vertex
            int index = addedVertices.size();
            addedVertices.add(coord);
            if (curCell == null) {
                curCell = new ArrayList<>();
                curCell.add(index + elements.size());
                repaint();
            } else {
                if (!curCell.contains(index + elements.size())) {
                    curCell.add(index + elements.size());
                    repaint();
                }
            }
        }
    }

    public void removeVertex(Point click) {
        BoardRange scale = BoardUtils.computeRange(maker.getCurrentBoard().getBoard().graph());
        List<Vertex> elements = maker.getCurrentBoard().getBoard().topology().vertices();
        if (addedType == SiteType.Vertex) {
            for (int i = 0; i < addedVertices.size(); i++) {
                Point screen = CoordinatesUtil.screenPosn(CoordinatesUtil.normalizeThenCenter(addedVertices.get(i),scale),maker.getCurrentBoard().getPlacement());
                screen.x = screen.x - camera.offX();
                screen.y = screen.y + camera.offY();
                if (click.distance(screen) < (double) dotSize / 2) {
                    addedVertices.remove(addedVertices.get(i));
                    repaint();
                    return;
                }
            }
        } else if (addedType == SiteType.Edge) {
            if (curEdge != null) {
                curEdge = null;
                repaint();
            }
        } else if (addedType == SiteType.Cell) {
            for (int i = 0; i < elements.size(); i++) {
                Point screen = CoordinatesUtil.screenPosn(elements.get(i).centroid(),maker.getCurrentBoard().getPlacement());
                screen.x = screen.x - camera.offX();
                screen.y = screen.y + camera.offY();
                if (click.distance(screen) < (double) dotSize / 2) {
                    if (curCell != null && curCell.contains(i)) {
                        curCell.remove((Integer) i);
                        if (curCell.isEmpty()) {
                            curCell = null;
                        }
                        repaint();
                        return;
                    }
                }
            }
            // Vertex already present outside the board
            for (int i = 0; i < addedVertices.size(); i++) {
                Point screen = CoordinatesUtil.screenPosn(CoordinatesUtil.normalizeThenCenter(addedVertices.get(i),scale),maker.getCurrentBoard().getPlacement());
                screen.x = screen.x - camera.offX();
                screen.y = screen.y + camera.offY();
                if (click.distance(screen) < (double) dotSize /2) {
                    if (curCell != null && curCell.contains(i + elements.size())) {
                        curCell.remove((Integer) (i+ elements.size()));
                        if (curCell.isEmpty()) {
                            curCell = null;
                        }
                        for (List<Integer> cell : addedCells) {
                            for (Integer id : cell) {
                                if (id == i + elements.size()) {
                                    repaint();
                                    return;
                                }
                            }
                        }
                        addedVertices.remove(i);
                        repaint();
                        return;
                    }
                }
            }
        }
    }

    public void clearAdd() {
        if (adding) {
            adding = false;
            addedType = null;
        }
        curEdge = null;
        curCell = null;
        addedVertices = new ArrayList<>();
        addedCells = new ArrayList<>();
        addedEdges = new ArrayList<>();
    }
}
