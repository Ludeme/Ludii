package app.display.dialogs.visual_editor.LayoutManagement;

/**
 * 2-dimensional real-valued vector with basic operations
 * @author nic0gin
 */

public class Vector2D
{

    private double x,y;

    public Vector2D(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public Vector2D normalize() {
        return div(euclideanNorm());
    }

    public Vector2D mult(double c) {
        return new Vector2D(x*c, y*c);
    }

    public Vector2D mult(Vector2D u) {
        return new Vector2D(x*u.x, y*u.y);
    }

    public Vector2D div(double c) {
        return new Vector2D(x/c, y/c);
    }

    public Vector2D add(double c) {
        return new Vector2D(x+c, y+c);
    }

    public Vector2D sub(double c) {
        return new Vector2D(x-c, y-c);
    }

    public Vector2D add(Vector2D u) {
        return new Vector2D(x+u.x, y+u.y);
    }

    public Vector2D sub(Vector2D u) {
        return new Vector2D(x-u.x, y-u.y);
    }

    public double euclideanNorm() {
        return Math.sqrt(x*x + y*y);
    }

    public double x()
    {
        return x;
    }

    public double y() {
        return y;
    }


}
