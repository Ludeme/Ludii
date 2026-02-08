package app.boardMaker.utils;

/**
 * Edge for custom graph creation
 */
public class Edge {
    private Vertex start;
    private Vertex end;

    private int startIdx;
    private int endIdx;

    public Edge(Vertex start,int startIdx, Vertex end, int endIdx) {
        this.start = start;
        this.end = end;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
    }

    public Vertex start() {
        return start;
    }

    public Vertex end() {
        return end;
    }

    public int getStartIdx() {
        return startIdx;
    }

    public int getEndIdx() {
        return endIdx;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Edge)) {
            return false;
        }

        Edge other = (Edge) obj;
        return (start.equals(other.start()) && end.equals(other.end())) ||
                (end.equals(other.start()) && start.equals(other.end()));
    }

    @Override
    public String toString() {
        return String.format("{%d %d}",startIdx,endIdx);
    }
}
