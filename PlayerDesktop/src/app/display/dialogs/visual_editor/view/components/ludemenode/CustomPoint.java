package app.display.dialogs.visual_editor.view.components.ludemenode;

import java.awt.*;

public class CustomPoint {
    public int x, y;
    public CustomPoint(int x, int y){
        this.x = x;
        this.y = y;
    }
    public CustomPoint(Point p){
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
