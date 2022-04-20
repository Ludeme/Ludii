package app.display.dialogs.visual_editor.LayoutManagement.GraphFactory;

import app.display.dialogs.visual_editor.model.MetaGraph.ExpGraph;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

/**
 * Contains hard-coded implementations of graph geometric shapes
 * @author nic0gin
 */

public class GraphGeometry extends GraphCreator{

    public GraphGeometry () {
        //TODO: extend functionality with ENUM
        //getGraphCube();
    }

    @Override
    public iGraph createGraph() {
        return getTicTacToeTree();
    }

    public iGraph getGraphCube() {
        iGraph graph = new ExpGraph();
        int A1 = graph.addNode();
        int B1 = graph.addNode();
        int C1 = graph.addNode();
        int D1 = graph.addNode();

        int A2 = graph.addNode();
        int B2 = graph.addNode();
        int C2 = graph.addNode();
        int D2 = graph.addNode();

        graph.addEdge(A1, B1);
        graph.addEdge(B1, C1);
        graph.addEdge(C1, D1);
        graph.addEdge(A1, D1);

        graph.addEdge(A2, B2);
        graph.addEdge(B2, C2);
        graph.addEdge(C2, D2);
        graph.addEdge(A2, D2);

        graph.addEdge(A1, A2);
        graph.addEdge(B1, B2);
        graph.addEdge(C1, C2);
        graph.addEdge(D1, D2);

        return graph;
    }

    public iGraph getGraphTesseract() {
        iGraph graph = new ExpGraph();
        int A1 = graph.addNode();
        int B1 = graph.addNode();
        int C1 = graph.addNode();
        int D1 = graph.addNode();

        int A2 = graph.addNode();
        int B2 = graph.addNode();
        int C2 = graph.addNode();
        int D2 = graph.addNode();

        graph.addEdge(A1, B1);
        graph.addEdge(B1, C1);
        graph.addEdge(C1, D1);
        graph.addEdge(A1, D1);

        graph.addEdge(A2, B2);
        graph.addEdge(B2, C2);
        graph.addEdge(C2, D2);
        graph.addEdge(A2, D2);

        graph.addEdge(A1, A2);
        graph.addEdge(B1, B2);
        graph.addEdge(C1, C2);
        graph.addEdge(D1, D2);

        int A3 = graph.addNode();
        int B3 = graph.addNode();
        int C3 = graph.addNode();
        int D3 = graph.addNode();

        int A4 = graph.addNode();
        int B4 = graph.addNode();
        int C4 = graph.addNode();
        int D4 = graph.addNode();

        graph.addEdge(A3, B3);
        graph.addEdge(B3, C3);
        graph.addEdge(C3, D3);
        graph.addEdge(A3, D3);

        graph.addEdge(A4, B4);
        graph.addEdge(B4, C4);
        graph.addEdge(C4, D4);
        graph.addEdge(A4, D4);

        graph.addEdge(A3, A4);
        graph.addEdge(B3, B4);
        graph.addEdge(C3, C4);
        graph.addEdge(D3, D4);

        // ###

        graph.addEdge(A1, A3);
        graph.addEdge(B1, B3);
        graph.addEdge(C1, C3);
        graph.addEdge(D1, D3);

        graph.addEdge(A2, A4);
        graph.addEdge(B2, B4);
        graph.addEdge(C2, C4);
        graph.addEdge(D2, D4);



        return graph;
    }

    public iGraph getGraphPyramid() {
        iGraph graph = new ExpGraph();

        int A1 = graph.addNode();

        int B1 = graph.addNode();
        int C1 = graph.addNode();
        int D1 = graph.addNode();
        int E1 = graph.addNode();

        int A2 = graph.addNode();

        graph.addEdge(A1, B1);
        graph.addEdge(A1, C1);
        graph.addEdge(A1, D1);
        graph.addEdge(A1, E1);

        graph.addEdge(B1, C1);
        graph.addEdge(C1, D1);
        graph.addEdge(D1, E1);
        graph.addEdge(E1, B1);

        graph.addEdge(A2, B1);
        graph.addEdge(A2, C1);
        graph.addEdge(A2, D1);
        graph.addEdge(A2, E1);

        return graph;
    }

    public iGraph getTicTacToeTree() {
        iGraph graph = new ExpGraph();

        int A1 = graph.addNode("game");
        graph.setRoot(A1);

        int A2 = graph.addNode("\"Tic-Tac-Toe\" ");
        graph.addEdge(A1, A2);

        int A3 = graph.addNode("players");
        graph.addEdge(A1, A3);
        int A4 = graph.addNode("2");
        graph.addEdge(A3, A4);

        int A5 = graph.addNode("equipment");
        graph.addEdge(A1, A5);
        int A51 = graph.addNode("board");
        graph.addEdge(A5, A51);
        int A6 = graph.addNode("square");
        graph.addEdge(A51, A6);
        int A7 = graph.addNode("3");
        graph.addEdge(A6, A7);

        int A8 = graph.addNode("piece");
        graph.addEdge(A5, A8);
        int A9 = graph.addNode("\"Disc\"");
        graph.addEdge(A8, A9);
        int A10 = graph.addNode("P1");
        graph.addEdge(A8, A10);

        int A11 = graph.addNode("piece");
        graph.addEdge(A5, A11);
        int B1 = graph.addNode("\"Cross\"");
        graph.addEdge(A11, B1);
        int B2 = graph.addNode("P2");
        graph.addEdge(A11, B2);

        int B3 = graph.addNode("rules");
        graph.addEdge(A1, B3);

        int B4 = graph.addNode("play");
        graph.addEdge(B3, B4);
        int B5 = graph.addNode("move");
        graph.addEdge(B4, B5);
        int B6 = graph.addNode("Add");
        graph.addEdge(B5, B6);
        int B7 = graph.addNode("to");
        graph.addEdge(B5, B7);
        int B8 = graph.addNode("sites");
        graph.addEdge(B7, B8);
        int B9 = graph.addNode("Empty");
        graph.addEdge(B8, B9);

        int B10 = graph.addNode("end");
        graph.addEdge(B3, B10);
        int B11 = graph.addNode("if");
        graph.addEdge(B10, B11);
        int C1 = graph.addNode("is");
        graph.addEdge(B11, C1);
        int C2 = graph.addNode("Line");
        graph.addEdge(C1, C2);
        int C3 = graph.addNode("3");
        graph.addEdge(C1, C3);

        int C4 = graph.addNode("result");
        graph.addEdge(B10, C4);
        int C5 = graph.addNode("Mover");
        graph.addEdge(C4, C5);
        int C6 = graph.addNode("Win");
        graph.addEdge(C4, C6);

        return graph;
    }


}
