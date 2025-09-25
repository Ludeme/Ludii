package app.boardMaker.menu.popup.boardpopup;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.equipment.container.board.Track;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.Complete;
import game.functions.graph.operators.Dual;

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

            default :
                break;
        }
    }

    private void apply_dual() {
        Board oldBoard = maker.getCurrentBoard().getBoard();
        GraphFunction newFunction = new Dual(oldBoard.graphFunction());
        change_function(newFunction, oldBoard);
    }

    private void change_function(GraphFunction function, Board old) {
        Board newBoard;
        if (old.tracks().isEmpty()) {
            newBoard = new Board(function,null,
                    null,null,null,
                    maker.getSiteType(),maker.largeStack());
        } else {
            newBoard = new Board(function,old.tracks().getFirst(),
                    old.tracks().toArray(new Track[0]),null,null,
                    maker.getSiteType(),maker.largeStack());
        }
        maker.getCurrentBoard().setBoard(newBoard);
        maker.drawBoard(maker.getCurrentBoard());
        displayer.getBoardPanel().repaint();
    }

    private void apply_complete(boolean b) {
        Board oldBoard = maker.getCurrentBoard().getBoard();
        GraphFunction newFunction = new Complete(oldBoard.graphFunction(),b);
        change_function(newFunction, oldBoard);
    }
}
