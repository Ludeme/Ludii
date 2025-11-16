package app.boardMaker.display.panels.boardPanel;

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.dataStruct.board.graphFunction.operators.*;
import app.boardMaker.handlers.Maker;
import app.boardMaker.res.Transformations;
import app.boardMaker.utils.BoardUtils;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.dim.DimFunction;
import game.functions.floats.FloatConstant;
import game.functions.floats.FloatFunction;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.*;
import game.types.board.SiteType;
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
        BoardInfo boardInfo = (BoardInfo) maker.state().currentBoardInfo();
        AddInfo newInfo;
        if (view.addedType == SiteType.Vertex) {
            Float[][] vertices = new Float[view.addedVertices.size()][2];
            for (int i = 0; i < view.addedVertices.size(); i++) {
                Point2D pt = view.addedVertices.get(i);
                vertices[i] = new Float[] {(float)pt.getX()
                        ,(float)pt.getY()};
            }
            newInfo = new AddInfo(view.addedType,boardInfo.getGraphInfo(),vertices,null,null);
        } else if (view.addedType == SiteType.Edge) {
            List<Vertex> vertices = boardInfo.board().graph().vertices();
            List<Float[][]> coordEdges = new ArrayList<>();
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
                coordEdges.add(new Float[][]{{(float)pt1.getX(), (float)pt1.getY()}
                            , {(float)pt2.getX(), (float)pt2.getY()}});

            }
            newInfo = new AddInfo(view.addedType,boardInfo.getGraphInfo(),null,coordEdges,null);
        } else {
            List<Vertex> vertices = boardInfo.board().graph().vertices();
            List<Float[][]> coordCells = new ArrayList<>();
            for (List<Integer> cell : view.addedCells) {
                Float[][] coords = new Float[cell.size()][2];
                for (int i = 0; i < cell.size(); i++) {
                    int id = cell.get(i);
                    if (id < vertices.size()) {
                        Point2D pt = vertices.get(id).pt2D();
                        coords[i] = new Float[]{(float) pt.getX(),
                                (float) pt.getY()};
                    } else {
                        Point2D pt = view.addedVertices.get(id - vertices.size());
                        coords[i] = new Float[]{(float) pt.getX(),
                                (float) pt.getY()};
                    }
                }
                coordCells.add(coords);
            }
            newInfo = new AddInfo(view.addedType,boardInfo.getGraphInfo(),null,null, coordCells);
        }
        boardInfo.setGraphInfo(newInfo);
        view.repaint();
    }

    private void apply_remove(List<Integer> indices) {
        BoardInfo boardInfo = (BoardInfo) maker.state().currentBoardInfo();
        GraphInfo newInfo;
        List<Edge> edgeList = maker.state().currentBoardInfo().board().topology().edges();
        newInfo = new RemoveInfo(view.removedType(),boardInfo.getGraphInfo(),indices,edgeList);
        boardInfo.setGraphInfo(newInfo);
    }

    private void apply_transformation(Transformations transformation, List<Point> vertices) {
        if (transformation == Transformations.Clip) {
            BoardInfo info = (BoardInfo) maker.state().currentBoardInfo();
            ClipInfo newInfo = new ClipInfo(info.getGraphInfo(),vertices);
            newInfo.createFunction(info.placement(),info.range(),view.camera());
            info.setGraphInfo(newInfo);
            maker.getDisplayer().getBoardPanel().repaint();
        } else if (transformation == Transformations.Hole) {
            BoardInfo info = (BoardInfo) maker.state().currentBoardInfo();
            HoleInfo newInfo = new HoleInfo(info.getGraphInfo(),vertices);
            newInfo.createFunction(info.placement(),info.range(),view.camera());
            info.setGraphInfo(newInfo);
            maker.getDisplayer().getBoardPanel().repaint();
        } else if (transformation == Transformations.Keep) {
            BoardInfo info = (BoardInfo) maker.state().currentBoardInfo();
            KeepInfo newInfo = new KeepInfo(info.getGraphInfo(),vertices);
            newInfo.createFunction(info.placement(),info.range(),view.camera());
            info.setGraphInfo(newInfo);
            maker.getDisplayer().getBoardPanel().repaint();
        }
    }
}
