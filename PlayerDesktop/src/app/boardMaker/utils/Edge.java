package app.boardMaker.utils;

public class Edge {
    private Vertex start;
    private Vertex end;

    public Edge(Vertex start, Vertex end) {
        this.start = start;
        this.end = end;
    }

    public Vertex start() {
        return start;
    }

    public Vertex end() {
        return end;
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
        return start + " " + end;
    }
}
