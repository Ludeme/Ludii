package app.boardMaker.menu.popup.boardpopup;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.equipment.container.board.Track;
import game.functions.graph.GraphFunction;
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
            case "Dual" :
                Board oldBoard = maker.getCurrentBoard().getBoard();
                GraphFunction newFunction = new Dual(oldBoard.graphFunction());
                Board newBoard;
                if (oldBoard.tracks().isEmpty()) {
                    newBoard = new Board(newFunction,null,
                            null,null,null,
                            maker.getSiteType(),maker.largeStack());
                } else {
                    newBoard = new Board(newFunction,oldBoard.tracks().getFirst(),
                            oldBoard.tracks().toArray(new Track[0]),null,null,
                            maker.getSiteType(),maker.largeStack());
                }
                maker.getCurrentBoard().setBoard(newBoard);
                maker.drawBoard(maker.getCurrentBoard());
                displayer.getBoardPanel().repaint();
                break;
            default :
                break;
        }
    }
}
