package app.boardMaker.handlers;

import app.boardMaker.dataStruct.board.ContainerInfo;
import game.equipment.container.board.Board;
import game.equipment.container.board.custom.MancalaBoard;
import game.equipment.container.board.custom.SurakartaBoard;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.Basis;
import game.functions.graph.generators.basis.brick.DiamondOrPrismOnBrick;
import game.functions.graph.generators.basis.brick.SpiralOnBrick;
import game.functions.graph.generators.basis.brick.SquareOrRectangleOnBrick;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;

public class Writer {

    public static void write(String filename, Maker maker) {
        try {
            File file = new File(filename);
            file.createNewFile();

            FileWriter writer = new FileWriter(filename);
            StringBuilder description = new StringBuilder();
            createDescription(description,maker);
            writer.write(description.toString());
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void createDescription(StringBuilder description, Maker maker) {
        description.append("(game \"");
        description.append(maker.getName()).append("\"\n");

        description.append("(players ").append(maker.getPlayers()).append(")\n");

        description.append("(equipment {\n");
        createBoardDescription(description,maker.getCurrentBoard().getContainerInfo());
        description.append("})\n");

        description.append("(rules \n");
        if (!maker.getCurrentBoard().pieceInfo().piecesPlacedbyName().isEmpty()) {
            description.append("(start {\n");
            HashMap<String,List<Integer>> piecesPlaced = maker.getCurrentBoard().pieceInfo().piecesPlacedbyName();
            for (String piece : piecesPlaced.keySet()) {
                description.append("(place \"" + piece + "\" {");
                for (Integer site : piecesPlaced.get(piece)) {
                    description.append(" " + site + " ");
                }
                description.append("})\n");
            }
            description.append("})\n");
        }
        description.append("(play (forEach Piece))\n");
        description.append("(end (if (no Moves Next) (result Mover win)))\n");
        description.append(")\n");

        description.append(")\n");
    }

    private static void createBoardDescription(StringBuilder description, ContainerInfo info) {
        description.append(info.description());
        description.append("\n");
    }
}
