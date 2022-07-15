package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;

import java.awt.Point;

public class ImmutablePoint {
    public int x, y;
    public ImmutablePoint(int x, int y){
        this.x = x;
        this.y = y;
    }
    public ImmutablePoint(Point p){
        this.x = (int) p.getX();
        this.y = (int) p.getY();
    }

    public void update(Point p){
        this.x = (int) p.getX();
        this.y = (int) p.getY();
    }

    public String toString(){
        return "[" + x + ", " + y + "]";
    }
}
