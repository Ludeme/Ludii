package app.boardMaker.menu.popup.boardpopup;

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.dataStruct.board.graphFunction.operators.*;
import app.boardMaker.display.dialogs.MergeDialog;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import app.boardMaker.res.Transformations;
import app.boardMaker.utils.BoardUtils;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.floats.FloatConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.*;
import game.types.board.SiteType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BoardPopupMenuListener implements ActionListener {
    private Maker maker;
    private Displayer displayer;

    public BoardPopupMenuListener(Maker maker) {
        this.maker = maker;
        this.displayer = maker.getDisplayer();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action) {
            case "dual" :
                apply_dual();
                break;

            case "complete_full" :
                apply_complete(false);
                break;

            case "complete_indiv" :
                apply_complete(true);
                break;

            case "make_faces" :
                apply_make_faces();
                break;

            case "split_cross" :
                apply_split_cross();
                break;

            case "trim" :
                apply_trim();
                break;

            case "merge" :
                new MergeDialog(maker, Transformations.Merge);
                break;

            case "union" :
                new MergeDialog(maker, Transformations.Union);
                break;

            case "intersect" :
                new MergeDialog(maker, Transformations.Intersect);
                break;

            case "subdivide" :
                JSpinner divSpinner = new JSpinner(new SpinnerNumberModel(3,3,Integer.MAX_VALUE,1));
                int divOption = JOptionPane.showOptionDialog(null, divSpinner,"Minimum sides to subdivide",JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,null,null,null);
                if (divOption == JOptionPane.OK_OPTION) {
                    int nFaces = (Integer) divSpinner.getValue();
                    apply_subdivide(nFaces);
                }
                break;

            case "rotate" :
                int baseAngle = maker.state().currentBoardInfo().board().graphFunction() instanceof Rotate ?
                        (int) ((Rotate) maker.state().currentBoardInfo().board().graphFunction()).angle(maker.state().currentBoardInfo().context()) : 0;
                JSpinner rotSpinner = new JSpinner(new SpinnerNumberModel(baseAngle,0,360,1));
                int rotOption = JOptionPane.showOptionDialog(null, rotSpinner,"Angle to rotate",JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,null,null,null);
                if (rotOption == JOptionPane.OK_OPTION) {
                    int angle = (Integer) rotSpinner.getValue();
                    apply_rotation(angle);
                }
                break;

            case "scale" :
                float scaleX = maker.state().currentBoardInfo().board().graphFunction() instanceof Scale ?
                        ((Scale) maker.state().currentBoardInfo().board().graphFunction()).scaleX(maker.state().currentBoardInfo().context()) : 1;
                float scaleY =  maker.state().currentBoardInfo().board().graphFunction() instanceof Scale ?
                        ((Scale) maker.state().currentBoardInfo().board().graphFunction()).scaleY(maker.state().currentBoardInfo().context()) : 1;
                JPanel panel = new JPanel();
                panel.add(new JLabel("X: "));
                JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(scaleX,0.0,100.0,0.1));
                panel.add(xSpinner);
                panel.add(new JLabel("Y: "));
                JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(scaleY,0.0,100.0,0.1));
                panel.add(ySpinner);
                int scaleOption = JOptionPane.showOptionDialog(null, panel,"Value to scale",JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,null,null,null);
                if (scaleOption == JOptionPane.OK_OPTION) {
                    double valX = (Double) xSpinner.getValue();
                    double valY = (Double) ySpinner.getValue();
                    apply_scale(valX,valY);
                }
                break;

            case "skew" :
                double baseSkew = maker.state().currentBoardInfo().board().graphFunction() instanceof Skew ?
                        ((Skew) maker.state().currentBoardInfo().board().graphFunction()).amount() : 0;
                JSpinner skewSpinner = new JSpinner(new SpinnerNumberModel(baseSkew,-5.0,5.0,0.1));
                int skewOption = JOptionPane.showOptionDialog(null, skewSpinner,"Value to skew",JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,null,null,null);
                if (skewOption == JOptionPane.OK_OPTION) {
                    double value = (double) skewSpinner.getValue();
                    apply_skew((float) value);
                }
                break;

            case "clip" :
                displayer.getBoardPanel().setCreatePoly(true);
                displayer.getBoardPanel().setTransformation(Transformations.Clip);
                displayer.getBoardPanel().newPolygon();
                displayer.getBoardPanel().repaint();
                break;

            case "hole" :
                displayer.getBoardPanel().setCreatePoly(true);
                displayer.getBoardPanel().setTransformation(Transformations.Hole);
                displayer.getBoardPanel().newPolygon();
                displayer.getBoardPanel().repaint();
                break;

            case "keep" :
                displayer.getBoardPanel().setCreatePoly(true);
                displayer.getBoardPanel().setTransformation(Transformations.Keep);
                displayer.getBoardPanel().newPolygon();
                displayer.getBoardPanel().repaint();
                break;

            case "remove_v" :
                displayer.getBoardPanel().clearRemovedIndices();
                displayer.getBoardPanel().setRemove(true);
                displayer.getBoardPanel().setRemoveType(SiteType.Vertex);
                displayer.getBoardPanel().repaint();
                break;
            case "remove_e" :
                displayer.getBoardPanel().clearRemovedIndices();
                displayer.getBoardPanel().setRemove(true);
                displayer.getBoardPanel().setRemoveType(SiteType.Edge);
                displayer.getBoardPanel().repaint();
                break;
            case "remove_c" :
                displayer.getBoardPanel().clearRemovedIndices();
                displayer.getBoardPanel().setRemove(true);
                displayer.getBoardPanel().setRemoveType(SiteType.Cell);
                displayer.getBoardPanel().repaint();
                break;

            case "add_v" :
                displayer.getBoardPanel().clearAdd();
                displayer.getBoardPanel().setAdding(true);
                displayer.getBoardPanel().setAddType(SiteType.Vertex);
                displayer.getBoardPanel().repaint();
                break;
            case "add_e" :
                displayer.getBoardPanel().clearAdd();
                displayer.getBoardPanel().setAdding(true);
                displayer.getBoardPanel().setAddType(SiteType.Edge);
                displayer.getBoardPanel().repaint();
                break;
            case "add_c" :
                displayer.getBoardPanel().clearAdd();
                displayer.getBoardPanel().setAdding(true);
                displayer.getBoardPanel().setAddType(SiteType.Cell);
                displayer.getBoardPanel().repaint();
                break;
            default :
                break;
        }
    }

    private void apply_scale(double valX, double valY) {
        BoardInfo boardInfo = (BoardInfo) maker.state().currentBoardInfo();
        ScaleInfo newInfo;
        if (boardInfo.getGraphInfo() instanceof ScaleInfo) {
            newInfo = new ScaleInfo((float) valX, (float) valY,((ScaleInfo)boardInfo.getGraphInfo()).getGraphFunction());
        } else {
            newInfo = new ScaleInfo((float) valX, (float) valY,boardInfo.getGraphInfo());
        }
        boardInfo.setGraphInfo(newInfo);
        displayer.getBoardPanel().repaint();
    }

    private void apply_skew(float value) {
        BoardInfo boardInfo = (BoardInfo) maker.state().currentBoardInfo();
        SkewInfo newInfo;
        if (boardInfo.getGraphInfo() instanceof SkewInfo) {
            newInfo = new SkewInfo(value,((SkewInfo)boardInfo.getGraphInfo()).getGraphFunction());
        } else {
            newInfo = new SkewInfo(value,boardInfo.getGraphInfo());
        }
        boardInfo.setGraphInfo(newInfo);
        displayer.getBoardPanel().repaint();
    }

    private void apply_rotation(int angle) {
        BoardInfo boardInfo = (BoardInfo) maker.state().currentBoardInfo();
        RotateInfo newInfo;
        if (boardInfo.getGraphInfo() instanceof RotateInfo) {
            newInfo = new RotateInfo(angle,((RotateInfo)boardInfo.getGraphInfo()).getGraphFunction());
        } else {
            newInfo = new RotateInfo(angle,boardInfo.getGraphInfo());
        }
        boardInfo.setGraphInfo(newInfo);
        displayer.getBoardPanel().repaint();
    }

    private void apply_trim() {
        BoardInfo boardInfo = (BoardInfo) maker.state().currentBoardInfo();
        TrimInfo newInfo = new TrimInfo(boardInfo.getGraphInfo());
        boardInfo.setGraphInfo(newInfo);
        displayer.getBoardPanel().repaint();
    }

    private void apply_split_cross() {
        BoardInfo boardInfo = (BoardInfo) maker.state().currentBoardInfo();
        SplitCrossInfo newInfo = new SplitCrossInfo(boardInfo.getGraphInfo());
        boardInfo.setGraphInfo(newInfo);
        displayer.getBoardPanel().repaint();
    }

    private void apply_make_faces() {
        BoardInfo boardInfo = (BoardInfo) maker.state().currentBoardInfo();
        MakeFaceInfo newInfo = new MakeFaceInfo(boardInfo.getGraphInfo());
        boardInfo.setGraphInfo(newInfo);
        displayer.getBoardPanel().repaint();
    }

    private void apply_dual() {
        BoardInfo boardInfo = (BoardInfo) maker.state().currentBoardInfo();
        DualInfo newInfo = new DualInfo(boardInfo.getGraphInfo());
        boardInfo.setGraphInfo(newInfo);
        displayer.getBoardPanel().repaint();
    }

    private void apply_complete(boolean b) {
        BoardInfo boardInfo = (BoardInfo) maker.state().currentBoardInfo();
        CompleteInfo newInfo = new CompleteInfo(boardInfo.getGraphInfo(),b);
        boardInfo.setGraphInfo(newInfo);
        displayer.getBoardPanel().repaint();
    }

    private void apply_subdivide(int nfaces) {
        BoardInfo boardInfo = (BoardInfo) maker.state().currentBoardInfo();
        SubdivideInfo newInfo = new SubdivideInfo(boardInfo.getGraphInfo(),nfaces);
        boardInfo.setGraphInfo(newInfo);
        displayer.getBoardPanel().repaint();
    }
}
