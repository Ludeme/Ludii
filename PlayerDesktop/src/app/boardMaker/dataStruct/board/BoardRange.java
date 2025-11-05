package app.boardMaker.dataStruct.board;

public class BoardRange {
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private double max;
    private double min;

    public BoardRange(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;

        max = maxX;
        min = minX;
        if (maxX - minX < maxY - minY)
        {
            min = minY;
            max = maxY;
        }
    }

    public double getMax() {
        return max;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMin() {
        return min;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }
}
