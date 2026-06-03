package app.boardMaker.display.panels.westPanel.library;

import javax.swing.tree.DefaultMutableTreeNode;

public class LibraryTree extends DefaultMutableTreeNode {
    /**
     * Creates a tree containing available boards
     * @param rootname the name of the root
     */
    public LibraryTree(String rootname) {
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
        LibraryBoardInfo info = new LibraryBoardInfo("Brick","BrickPanel");
        DefaultMutableTreeNode board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Celtic","CelticPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Concentric","ConcentricPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Custom","CustomGraphPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Custom shape","CustomShapePanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Hexagonal","HexPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Quadhex","QuadhexPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Rectangle","RectanglePanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Regular","RegularPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Repeat","RepeatPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Semi-regular","TilingPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Spiral","SpiralPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Square","SquarePanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Triangle","TrianglePanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Wedge","WedgePanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);
    }

    private void createMancalaNodes(DefaultMutableTreeNode cat) {
        LibraryBoardInfo info = new LibraryBoardInfo("Mancala","MancalaPanel");
        DefaultMutableTreeNode board = new DefaultMutableTreeNode(info);
        cat.add(board);
    }

    private void createSurakartaNodes(DefaultMutableTreeNode cat) {
        LibraryBoardInfo info = new LibraryBoardInfo("Surakarta - Rectangle","SurakartaRPanel");
        DefaultMutableTreeNode board = new DefaultMutableTreeNode(info);
        cat.add(board);

        info = new LibraryBoardInfo("Surakarta - Triangle","SurakartaTPanel");
        board = new DefaultMutableTreeNode(info);
        cat.add(board);
    }
}
