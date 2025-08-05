package app.boardMaker.utils;

import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.equipment.container.board.custom.MancalaBoard;
import game.equipment.container.board.custom.SurakartaBoard;
import game.types.board.SiteType;

import java.awt.image.BufferedImage;

public class BoardData {
    private String cellSVG;
    private String graphSVG;
    private String otherSVG;

    private Board board;

    private Maker maker;

    public BoardData(Maker maker) {
        this.maker = maker;
    }

    public BoardData copy() {
        BoardData copy = new BoardData(maker);

        copy.setBoard(board);
        copy.setCellSVG(cellSVG);
        copy.setGraphSVG(graphSVG);
        copy.setOtherSVG(otherSVG);

        return copy;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setCellSVG(String cellSVG) {
        this.cellSVG = cellSVG;
    }

    public void setGraphSVG(String graphSVG) {
        this.graphSVG = graphSVG;
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
            if (maker.getSiteType() == SiteType.Cell) {
                return cellSVG;
            } else {
                return graphSVG;
            }
        }
    }

    public String getCellSVG() {
        return cellSVG;
    }

    public String getGraphSVG() {
        return graphSVG;
    }

    public String getOtherSVG() {
        return otherSVG;
    }
}
