package app.boardMaker.display.panels.westPanel.itemList;

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.piece.PieceInfo;
import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListAddNode;
import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListBoardNode;
import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListPawnNode;
import app.boardMaker.handlers.Maker;
import app.boardMaker.res.ItemType;
import app.boardMaker.dataStruct.board.BoardData;
import game.equipment.component.Piece;
import game.functions.graph.operators.Merge;
import game.types.play.RoleType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.HashMap;

public class ItemList extends JPanel {
    private Maker maker;

    private JScrollPane boardListView;

    private JTree boardListTree;
    private ItemListRenderer renderer;

    private DefaultMutableTreeNode boardRoot;

    private HashMap<String,ContainerInfo> boardMap;

    protected ItemListBoardNode selectedBoard;


    public ItemList(Maker maker) {
        super(new BorderLayout());

        this.maker = maker;
        boardMap = new HashMap<>();

        boardRoot = new DefaultMutableTreeNode("Boards");
        boardRoot.add(new ItemListAddNode(ItemType.Board, this,maker));
        boardListTree = new JTree(boardRoot);
        renderer = new ItemListRenderer(boardListTree);
        boardListTree.setCellRenderer(renderer);

        maker.setBoardList(this);

        ItemListML ml = new ItemListML(maker,this);

        boardListTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        boardListTree.addMouseListener(ml);

        boardListView = new JScrollPane(boardListTree);

        add(boardListView,BorderLayout.CENTER);
        addEmptyBoard();
    }

    public DefaultMutableTreeNode boardRoot() {
        return boardRoot;
    }

    public void reload(DefaultMutableTreeNode node) {
        ((DefaultTreeModel) boardListTree.getModel()).reload(node);
    }

    public HashMap<String,ContainerInfo> boards() {
        return boardMap;
    }

    public void addBoard(ContainerInfo board) {
        String input = JOptionPane.showInputDialog(maker.getDisplayer().getFrame(),"Choose a name for the board: ","Name selection",JOptionPane.PLAIN_MESSAGE);
        if (selectedBoard == null) {
            ItemListBoardNode node = new ItemListBoardNode(input, board);
            boardRoot.insert(node,boardRoot.getChildCount() - 1);
            selectedBoard = node;
            boardMap.put(input,board);
        } else {
            selectedBoard.setName(input);
            selectedBoard.setBoardInfo(board);
            boardMap.put(input,board);
        }
        createPieceSubTree();
        JOptionPane.showMessageDialog(maker.getDisplayer().getFrame(),
                String.format("The board %s has been created and can be accessed in the \"Boards\" tab",input),
                "New board",JOptionPane.PLAIN_MESSAGE);
    }

    private void createPieceSubTree() {
        DefaultMutableTreeNode pieceRoot = new DefaultMutableTreeNode("Pieces");
        selectedBoard.add(pieceRoot);
        ItemListAddNode addNode = new ItemListAddNode(ItemType.Pawn,this,maker);
        pieceRoot.add(addNode);
        reload(selectedBoard);
    }

    public void updateBoard(ContainerInfo board) {
        selectedBoard.setBoardInfo(board);
        String name = selectedBoard.name();
        boardMap.replace(name,board);
    }

    public ContainerInfo get(String key) {
        return boardMap.get(key);
    }

    public void addEmptyBoard() {
        ItemListBoardNode emptyNode = new ItemListBoardNode("Empty board",new BoardInfo());
        int idx = boardRoot.getChildCount() - 1;
        boardRoot.insert(emptyNode,idx);
        selectedBoard = emptyNode;
        ((DefaultTreeModel)boardListTree.getModel()).reload(boardRoot);
        maker.state().addEmptyBoard();
        maker.switchBoard(idx);
    }

    public JTree tree() {
        return boardListTree;
    }

    public void addPawn(String name, RoleType owner, DefaultMutableTreeNode parent) {
        ItemListPawnNode pawn = new ItemListPawnNode(name,owner);
        parent.insert(pawn, parent.getChildCount() - 1);
        int idx = boardRoot.getIndex(parent.getParent());
        addToPieces(name,owner,idx);
        ((DefaultTreeModel)boardListTree.getModel()).reload(parent);
    }

    private void addToPieces(String name, RoleType owner, int index) {
        /*if (owner == RoleType.Each) {
            for (int i = 1; i <= maker.getPlayers(); i++) {
                pieces.put(name + i,new Piece(name + i,owner,null,null,null,null,null,null));
            }
        } else if (owner == RoleType.Shared) {
            pieces.put(name,new Piece(name,owner,null,null,null,null,null,null));
        } else {
            pieces.put(name + owner.ordinal(),new Piece(name + owner.ordinal(),owner,null,null,null,null,null,null));
        }*/
        PieceInfo pieceInfo = maker.state().pieceInfo(index);
        pieceInfo.piecesUsed().add(new Piece(name,owner,null,null,null,null,null,null));
    }
}
