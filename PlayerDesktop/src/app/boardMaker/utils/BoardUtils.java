package app.boardMaker.utils;

import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.equipment.container.board.Track;
import game.functions.graph.GraphFunction;

public class BoardUtils {
    public static Board copyBoard(Board old, Maker maker) {
        Board newBoard;
        if (old.tracks().isEmpty()) {
            newBoard = new Board(old.graphFunction(), null,
                    null,null,null,
                    maker.getSiteType(),maker.largeStack());
        } else {
            newBoard = new Board(old.graphFunction(), old.tracks().getFirst(),
                    old.tracks().toArray(new Track[0]),null,null,
                    maker.getSiteType(),maker.largeStack());
        }

        return newBoard;
    }

    public static Board change_function(GraphFunction newFunc, Board old, Maker maker) {
        Board newBoard;
        if (old.tracks().isEmpty()) {
            newBoard = new Board(newFunc,null,
                    null,null,null,
                    maker.getSiteType(),maker.largeStack());
        } else {
            newBoard = new Board(newFunc,old.tracks().getFirst(),
                    old.tracks().toArray(new Track[0]),null,null,
                    maker.getSiteType(),maker.largeStack());
        }

        return newBoard;
    }
}
