package app.boardMaker.handlers;

import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.piece.PieceInfo;

import java.io.File;
import java.io.FileWriter;

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
        description.append(String.format("(game \"%s\"\n",maker.getName()));

        description.append(String.format("(players %d)\n",maker.getPlayers()));

        description.append("(equipment {\n");
        createBoardDescription(description,maker.state().currentBoardInfo());
        createPieceDescription(description,maker.state().currentPieceInfo());
        description.append("})\n");

        description.append("(rules \n");
        createPieceSetupDescription(description,maker.state().currentPieceInfo());
        description.append("(play (forEach Piece))\n");
        description.append("(end (if (no Moves Next) (result Mover win)))\n");
        description.append(")\n");

        description.append(")\n");

        description.append(maker.state().currentBoardInfo().optionalMetadata());
    }

    private static void createBoardDescription(StringBuilder description, ContainerInfo info) {
        description.append(info.description());
    }

    private static void createPieceSetupDescription(StringBuilder description, PieceInfo info) {
        description.append(info.setupDescription());
    }

    private static void createPieceDescription(StringBuilder description, PieceInfo info) {
        description.append(info.pieceDescription());
    }
}
