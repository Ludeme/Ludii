package app.boardMaker.dataStruct.piece;

import game.equipment.component.Piece;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A class to represent a set of pieces
 */
public class PieceInfo {
    // Maps the coordinates of sites and the pieces placed on them
    private HashMap<String, String> piecesPlacedbySite;
    // Maps a piece and the coordinates of all the sites it is placed on
    private HashMap<String, List<String>> piecesPlacedbyName;
    // List of pieces used in the set
    private List<Piece> piecesUsed;

    public PieceInfo() {
        piecesPlacedbySite = new HashMap<>();
        piecesPlacedbyName = new HashMap<>();
        piecesUsed = new ArrayList<>();
    }

    /**
     * Place a piece on a site
     * @param site the coordinates of the site
     * @param pieceName internal name of the piece
     */
    public void placePiece(String site, String pieceName) {
        piecesPlacedbySite.put(site,pieceName);
        if (piecesPlacedbyName.containsKey(pieceName)) {
            piecesPlacedbyName.get(pieceName).add(site);
        } else {
            ArrayList<String> list = new ArrayList<>();
            list.add(site);
            piecesPlacedbyName.put(pieceName,list);
        }
    }

    /**
     * Removes a piece from a site
     * @param site the site to remove from
     */
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

    /**
     * Creates the description of the start rules
     * @return the start rules
     */
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

    /**
     * Creates the description of pieces in the equipment
     * @return the pieces definition
     */
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
