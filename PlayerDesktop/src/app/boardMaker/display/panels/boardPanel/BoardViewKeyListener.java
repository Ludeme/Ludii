package app.boardMaker.display.panels.boardPanel;

import app.boardMaker.handlers.Maker;
import app.boardMaker.res.Transformations;
import app.boardMaker.utils.BoardUtils;
import app.boardMaker.utils.CoordinatesUtil;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.dim.DimFunction;
import game.functions.floats.FloatConstant;
import game.functions.floats.FloatFunction;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.*;
import game.types.board.SiteType;
import game.util.graph.Poly;
import game.util.graph.Vertex;
import other.topology.Edge;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class BoardViewKeyListener extends KeyAdapter {
    private Maker maker;
    private BoardDrawSpace view;

    public BoardViewKeyListener(Maker maker, BoardDrawSpace view) {
        this.maker = maker;
        this.view = view;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_ENTER) {
            if (view.createPoly()) {
                apply_transformation(view.transformation(),view.poly());
                view.clearPoly();
            } else if (view.isRemoving()) {
                apply_remove(view.indices());
                view.clearRemoving();
            } else if (view.isAdding()) {
                apply_add();
                view.clearAdd();
            }
            view.repaint();
        } else if (key == KeyEvent.VK_ESCAPE) {
            if (view.createPoly()) {
                view.clearPoly();
            } else if (view.isRemoving()) {
                view.clearRemoving();
            } else if (view.isAdding()) {
                view.clearAdd();
            }
            view.repaint();
        }
    }

    private void apply_add() {
        Board oldBoard = maker.getCurrentBoard().getBoard();
        GraphFunction newFunction;
        if (view.addedType == SiteType.Vertex) {
            FloatFunction[][] vertices = new FloatFunction[view.addedVertices.size()][2];
            for (int i = 0; i < view.addedVertices.size(); i++) {
                Point2D pt = view.addedVertices.get(i);
                vertices[i] = new FloatConstant[] {new FloatConstant((float)pt.getX())
                        ,new FloatConstant((float)pt.getY())};
            }
            newFunction = new Add(oldBoard.graphFunction(),vertices,null,null,null,null,null,false);
        } else if (view.addedType == SiteType.Edge) {
            List<Vertex> vertices = oldBoard.graph().vertices();
            List<FloatFunction[][]> coordEdges = new ArrayList<>();
            for (Integer[] edge : view.addedEdges) {
                int vA = edge[0], vB = edge[1];
                Point2D pt1;
                if (vA >= vertices.size()) {
                    vA = vA - vertices.size();
                    pt1 = view.addedVertices.get(vA);
                } else {
                    pt1 = vertices.get(vA).pt2D();
                }
                Point2D pt2;
                if (vB >= vertices.size()) {
                    vB = vB - vertices.size();
                    pt2 = view.addedVertices.get(vB);
                } else {
                    pt2 = vertices.get(vB).pt2D();
                }
                coordEdges.add(new FloatFunction[][]{{new FloatConstant((float)pt1.getX()), new FloatConstant((float)pt1.getY())}
                            , {new FloatConstant((float)pt2.getX()), new FloatConstant((float)pt2.getY())}});

            }
            newFunction = new Add(oldBoard.graphFunction(),null,coordEdges.toArray(new FloatFunction[0][][]),
                    null,null,null,null,false);
        } else {
            List<Vertex> vertices = oldBoard.graph().vertices();
            List<FloatFunction[][]> coordCells = new ArrayList<>();
            for (List<Integer> cell : view.addedCells) {
                FloatFunction[][] coords = new FloatFunction[cell.size()][2];
                for (int i = 0; i < cell.size(); i++) {
                    int id = cell.get(i);
                    if (id < vertices.size()) {
                        Point2D pt = vertices.get(id).pt2D();
                        coords[i] = new FloatFunction[]{new FloatConstant((float) pt.getX()),
                                new FloatConstant((float) pt.getY())};
                    } else {
                        Point2D pt = view.addedVertices.get(id - vertices.size());
                        coords[i] = new FloatFunction[]{new FloatConstant((float) pt.getX()),
                                new FloatConstant((float) pt.getY())};
                    }
                }
                coordCells.add(coords);
            }
            newFunction = new Add(oldBoard.graphFunction(),null,null,null,
                    null,coordCells.toArray(new FloatFunction[0][][]),null,false);
        }
        update_board(newFunction,oldBoard);
    }

    private void apply_remove(List<Integer> indices) {
        Board oldBoard = maker.getCurrentBoard().getBoard();
        GraphFunction newFunction;
        if (view.removedType() == SiteType.Cell) {
            DimFunction[] cells = new DimFunction[indices.size()];
            for (int i = 0; i < indices.size(); i++) {
                cells[i] = new DimConstant(indices.get(i));
            }
            newFunction = new Remove(oldBoard.graphFunction(),null,cells,null,null,null,null,true);
        } else if (view.removedType() == SiteType.Vertex) {
            DimFunction[] vertices = new DimFunction[indices.size()];
            for (int i = 0; i < indices.size(); i++) {
                vertices[i] = new DimConstant(indices.get(i));
            }
            newFunction = new Remove(oldBoard.graphFunction(),null,null,null,null,null,vertices,true);
        } else {
            DimFunction[][] edges = new DimFunction[indices.size()][2];
            List<Edge> edgeList = maker.getCurrentBoard().getBoard().topology().edges();
            for (int i = 0; i < indices.size(); i++) {
                Edge edge = edgeList.get(indices.get(i));
                edges[i] = new DimFunction[]{new DimConstant(edge.vA().index()),new DimConstant(edge.vB().index())};
            }
            newFunction = new Remove(oldBoard.graphFunction(),null,null,null,edges,null,null,true);
        }
        update_board(newFunction,oldBoard);
    }

    private void apply_transformation(Transformations transformation, List<Point> vertices) {
        if (transformation == Transformations.Clip) {
            Board oldBoard = maker.getCurrentBoard().getBoard();
            Poly poly = makePolygon(vertices);
            GraphFunction newFunction = new Clip(oldBoard.graphFunction(),poly);
            update_board(newFunction,oldBoard);
        } else if (transformation == Transformations.Hole) {
            Board oldBoard = maker.getCurrentBoard().getBoard();
            Poly poly = makePolygon(vertices);
            GraphFunction newFunction = new Hole(oldBoard.graphFunction(),poly);
            update_board(newFunction,oldBoard);
        } else if (transformation == Transformations.Keep) {
            Board oldBoard = maker.getCurrentBoard().getBoard();
            Poly poly = makePolygon(vertices);
            GraphFunction newFunction = new Keep(oldBoard.graphFunction(),poly);
            update_board(newFunction,oldBoard);
        }
    }

    private Poly makePolygon(List<Point> vertices) {
        Float[][] pts = new Float[vertices.size()][2];
        for (int i = 0; i < vertices.size(); i++) {
            Point2D pt = CoordinatesUtil.boardPosn(vertices.get(i),maker.getCurrentBoard().getPlacement(),maker.getCurrentBoard().getBoard().graph(),view.camera());

            pts[i][0] = (float) pt.getX();
            pts[i][1] = (float) pt.getY();
        }
        return new Poly(pts, null);
    }

    private void update_board(GraphFunction function, Board old) {
        Board newBoard = BoardUtils.change_function(function,old,maker);
        maker.getCurrentBoard().setBoard(newBoard);
        maker.getDisplayer().getBoardPanel().repaint();
    }
}
