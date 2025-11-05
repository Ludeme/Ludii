package app.boardMaker.dataStruct.piece;

import game.equipment.component.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PieceInfo {
    private HashMap<Integer, Piece> piecesPlacedbySite;
    public HashMap<String, List<Integer>> piecesPlacedbyName;

    public PieceInfo() {
        piecesPlacedbySite = new HashMap<>();
        piecesPlacedbyName = new HashMap<>();
    }

    public void addPiece(int site, Piece piece) {
        piecesPlacedbySite.put(site,piece);
        if (piecesPlacedbyName.containsKey(piece.name())) {
            piecesPlacedbyName.get(piece.name()).add(site);
        } else {
            ArrayList<Integer> list = new ArrayList<>();
            list.add(site);
            piecesPlacedbyName.put(piece.name(),list);
        }
    }

    public void removePiece(int site) {
        Piece piece = piecesPlacedbySite.get(site);
        piecesPlacedbySite.remove(site);
        List<Integer> list = piecesPlacedbyName.get(piece.name());
        list.remove((Integer) site);
        if (list.isEmpty()) {
            piecesPlacedbyName.remove(piece.name());
        }
    }

    public HashMap<Integer,Piece> piecesPlacedbySite() {
        return piecesPlacedbySite;
    }

    public HashMap<String,List<Integer>> piecesPlacedbyName() {
        return piecesPlacedbyName;
    }
}
