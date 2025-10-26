package app.boardMaker.utils;

import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.equipment.container.board.custom.MancalaBoard;
import game.equipment.container.board.custom.SurakartaBoard;
import game.types.board.SiteType;
import other.context.Context;

import java.awt.*;

public class BoardData {
    private String boardSVG;
    private String otherSVG;

    private Board board;
    private Context context;
    private Rectangle placement;
    private BoardRange range;

    private PieceInfo pieceInfo;

    private Maker maker;

    public BoardData(Maker maker) {
        this.maker = maker;
        pieceInfo = new PieceInfo();
    }

    public BoardData copy() {
        BoardData copy = new BoardData(maker);

        copy.setBoard(board);
        copy.setBoardSVG(boardSVG);
        copy.setOtherSVG(otherSVG);
        copy.setPlacement(placement);
        copy.setContext(context);
        copy.setPieceInfo(pieceInfo);

        return copy;
    }

    public void setPieceInfo(PieceInfo pieceInfo) {
        this.pieceInfo = pieceInfo;
    }

    public PieceInfo pieceInfo() {
        return pieceInfo;
    }

    public void setBoard(Board board) {
        this.board = board;
        if (board == null) {
            boardSVG = null;
            otherSVG = null;
        }
    }

    public void setBoardSVG(String boardSVG) {
        this.boardSVG = boardSVG;
    }

    public void setOtherSVG(String otherSVG) {
        this.otherSVG = otherSVG;
    }

    public Board getBoard() {
        return board;
    }

    public String getSVG() {
        if (board instanceof MancalaBoard) {
            return otherSVG;
        } else if (board instanceof SurakartaBoard) {
            return otherSVG;
        } else {
            return boardSVG;
        }
    }

    public void setPlacement(Rectangle placement) {
        this.placement = placement;
    }

    public Rectangle getPlacement() {
        return placement;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
