package app.boardMaker.dataStruct.state;

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.piece.PieceInfo;
import game.types.board.SiteType;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing the state of the Boardmaker
 */
public class BoardMakerState {
    // List of created boards
    private List<ContainerInfo> boards;
    // List of created set of pieces
    private List<PieceInfo> pieces;
    // Index of current board and piece set selected
    private int currentIdx;

    public BoardMakerState() {
        boards = new ArrayList<>();
        pieces = new ArrayList<>();
        currentIdx = 0;
    }

    public void setCurrentIdx(int i) {
        currentIdx = i;
    }

    public int currentIdx() {
        return currentIdx;
    }

    public void addEmptyBoard() {
        BoardInfo empty = new BoardInfo();
        boards.add(empty);
        pieces.add(new PieceInfo());
    }

    public void setCurrentBoard(ContainerInfo board) {
        boards.add(currentIdx,board);
        boards.remove(currentIdx + 1);
    }

    public ContainerInfo currentBoardInfo() {
        return boards.get(currentIdx);
    }

    public PieceInfo currentPieceInfo() {
        return pieces.get(currentIdx);
    }

    public PieceInfo pieceInfo(int idx) {
        return pieces.get(idx);
    }

    public String currentSVG(SiteType type) {
        return boards.get(currentIdx).getSVG(type);
    }
}
