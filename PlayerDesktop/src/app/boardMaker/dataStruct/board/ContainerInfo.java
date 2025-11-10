package app.boardMaker.dataStruct.board;

import game.equipment.container.board.Board;
import game.types.board.SiteType;

public interface ContainerInfo {
    public String description();
    public String getSVG(SiteType site);
    public Board board();
}
