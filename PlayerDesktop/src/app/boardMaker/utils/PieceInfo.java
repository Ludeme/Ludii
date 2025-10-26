package app.boardMaker.utils;

import game.equipment.component.Piece;

import java.util.HashMap;

public class PieceInfo {
    private HashMap<Integer, Piece> piecesPlaced;

    public PieceInfo() {
        piecesPlaced = new HashMap<>();
    }

    public void addPiece(int site, Piece piece) {
        piecesPlaced.put(site,piece);
    }

    public void removePiece(int site) {
        piecesPlaced.remove(site);
    }

    public HashMap<Integer,Piece> piecesPlaced() {
        return piecesPlaced;
    }
}
