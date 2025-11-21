package app.boardMaker.dataStruct.piece;

import game.equipment.component.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PieceInfo {
    private HashMap<String, String> piecesPlacedbySite;
    private HashMap<String, List<String>> piecesPlacedbyName;
    private List<Piece> piecesUsed;

    public PieceInfo() {
        piecesPlacedbySite = new HashMap<>();
        piecesPlacedbyName = new HashMap<>();
        piecesUsed = new ArrayList<>();
    }

    public void addPiece(String site, String pieceName) {
        piecesPlacedbySite.put(site,pieceName);
        if (piecesPlacedbyName.containsKey(pieceName)) {
            piecesPlacedbyName.get(pieceName).add(site);
        } else {
            ArrayList<String> list = new ArrayList<>();
            list.add(site);
            piecesPlacedbyName.put(pieceName,list);
        }
    }

    public void removePiece(String site) {
        String piece = piecesPlacedbySite.get(site);
        piecesPlacedbySite.remove(site);
        List<String> list = piecesPlacedbyName.get(piece);
        list.remove(site);
        if (list.isEmpty()) {
            piecesPlacedbyName.remove(piece);
        }
    }

    public HashMap<String,String> piecesPlacedbySite() {
        return piecesPlacedbySite;
    }

    public HashMap<String,List<String>> piecesPlacedbyName() {
        return piecesPlacedbyName;
    }

    public String setupDescription() {
        StringBuilder s = new StringBuilder();

        if (piecesPlacedbyName.isEmpty()) {
            return "";
        }

        s.append("(start {\n");
        for (String name : piecesPlacedbyName.keySet()) {
            s.append("(place \"" + name + "\" {");
            for (String site : piecesPlacedbyName.get(name)) {
                s.append(String.format(" \"%s\" ",site));
            }
            s.append("})\n");
        }
        s.append("})\n");

        return s.toString();
    }

    public String pieceDescription() {
        StringBuilder s = new StringBuilder();

        for (Piece p : piecesUsed) {
            s.append(String.format("(piece \"%s\" %s)\n",p.name(),p.role()));
        }

        return s.toString();
    }

    public List<Piece> piecesUsed() {
        return piecesUsed;
    }
}
