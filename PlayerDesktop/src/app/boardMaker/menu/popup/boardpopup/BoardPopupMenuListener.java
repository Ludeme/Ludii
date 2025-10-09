package app.boardMaker.menu.popup.boardpopup;

import app.boardMaker.display.dialogs.MergeDialog;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import app.boardMaker.res.Transformations;
import app.boardMaker.utils.BoardUtils;
import game.equipment.container.board.Board;
import game.equipment.container.board.Track;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.*;

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
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(3,3,Integer.MAX_VALUE,1));
                int option = JOptionPane.showOptionDialog(null,spinner,"Minimum sides to subdivide",JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,null,null,null);
                if (option == JOptionPane.OK_OPTION) {
                    int nFaces = (Integer) spinner.getValue();
                    apply_subdivide(nFaces);
                }
                break;

            default :
                break;
        }
    }

    private void apply_trim() {
        Board oldBoard = maker.getCurrentBoard().getBoard();
        GraphFunction newFunction = new Trim(oldBoard.graphFunction());
        update_board(newFunction, oldBoard);
    }

    private void apply_split_cross() {
        Board oldBoard = maker.getCurrentBoard().getBoard();
        GraphFunction newFunction = new SplitCrossings(oldBoard.graphFunction());
        update_board(newFunction, oldBoard);
    }

    private void apply_make_faces() {
        Board oldBoard = maker.getCurrentBoard().getBoard();
        GraphFunction newFunction = new MakeFaces(oldBoard.graphFunction());
        update_board(newFunction, oldBoard);
    }

    private void apply_dual() {
        Board oldBoard = maker.getCurrentBoard().getBoard();
        GraphFunction newFunction = new Dual(oldBoard.graphFunction());
        update_board(newFunction, oldBoard);
    }

    private void update_board(GraphFunction function, Board old) {
        Board newBoard = BoardUtils.change_function(function,old,maker);
        maker.getCurrentBoard().setBoard(newBoard);
        displayer.getBoardPanel().repaint();
    }

    private void apply_complete(boolean b) {
        Board oldBoard = maker.getCurrentBoard().getBoard();
        GraphFunction newFunction = new Complete(oldBoard.graphFunction(),b);
        update_board(newFunction, oldBoard);
    }

    private void apply_subdivide(int nfaces) {
        Board oldBoard = maker.getCurrentBoard().getBoard();
        GraphFunction newFunction = new Subdivide(oldBoard.graphFunction(),new DimConstant(nfaces));
        update_board(newFunction,oldBoard);
    }
}
