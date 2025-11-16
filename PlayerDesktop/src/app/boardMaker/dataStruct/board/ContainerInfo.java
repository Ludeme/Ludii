package app.boardMaker.dataStruct.board;

import app.boardMaker.utils.Camera;
import game.equipment.container.board.Board;
import game.types.board.SiteType;
import other.context.Context;

import java.awt.*;

public interface ContainerInfo {
    public String description();
    public String getSVG(SiteType site);
    public Board board();
    public String optionalMetadata();
    public void setPlacement(Rectangle p);
    public void shiftPlacement(Camera c,Container view);
    public Rectangle placement();
    public Context context();
    public BoardRange range();
}
