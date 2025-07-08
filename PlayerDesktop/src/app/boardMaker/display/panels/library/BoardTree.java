package app.boardMaker.display.panels.library;

import javax.swing.tree.DefaultMutableTreeNode;

public class BoardTree extends DefaultMutableTreeNode {
    /**
     * Creates a tree containing available boards
     * @param rootname the name of the root
     */
    public BoardTree(String rootname) {
        super(rootname);

        createNodes();
    }

    /**
     * Creates the nodes in BoardTree
     */
    private void createNodes() {
        DefaultMutableTreeNode category = null;

        category = new DefaultMutableTreeNode("Basic");
        createBasicNodes(category);
        add(category);

        category = new DefaultMutableTreeNode("Mancala");
        createMancalaNodes(category);
        add(category);

        category = new DefaultMutableTreeNode("Surakarta");
        createSurakartaNodes(category);
        add(category);
    }

    private void createBasicNodes(DefaultMutableTreeNode cat) {
        BoardInfo info = new BoardInfo("Brick","BrickPanel");
        DefaultMutableTreeNode board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new BoardInfo("Celtic","CelticPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new BoardInfo("Concentric","ConcentricPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new BoardInfo("Hexagonal","HexPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new BoardInfo("Quadhex","QuadhexPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new BoardInfo("Rectangle","RectanglePanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new BoardInfo("Spiral","SpiralPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new BoardInfo("Square","SquarePanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new BoardInfo("Tiling","TilingPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new BoardInfo("Triangle","TrianglePanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new BoardInfo("Wedge","WedgePanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);
    }

    private void createMancalaNodes(DefaultMutableTreeNode cat) {
        BoardInfo info = new BoardInfo("Mancala","MancalaPanel");
        DefaultMutableTreeNode board = new DefaultMutableTreeNode(info);
        cat.add(board);
    }

    private void createSurakartaNodes(DefaultMutableTreeNode cat) {
        BoardInfo info = new BoardInfo("Surakarta - Rectangle","SurakartaRPanel");
        DefaultMutableTreeNode board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new BoardInfo("Surakarta - Triangle","SurakartaTPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);
    }
}
