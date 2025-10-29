package app.boardMaker.display.panels.westPanel.itemList;

import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListAddNode;
import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListBoardNode;
import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListPawnNode;
import app.boardMaker.handlers.Maker;
import app.boardMaker.res.ItemType;
import app.boardMaker.utils.BoardData;
import game.equipment.component.Piece;
import game.functions.graph.operators.Merge;
import game.types.play.RoleType;
import game.util.directions.CompassDirection;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemList extends JPanel {
    private Maker maker;

    private JScrollPane boardListView;

    private JTree boardListTree;
    private ItemListRenderer renderer;

    private DefaultMutableTreeNode boardRoot;
    private DefaultMutableTreeNode pawnRoot;

    private HashMap<String, BoardData> boards;
    private HashMap<String, Piece> pieces;

    protected ItemListBoardNode selectedBoardNode;


    public ItemList(Maker maker) {
        super(new BorderLayout());

        this.maker = maker;
        boards = new HashMap<>();
        pieces = new HashMap<>();

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Items");
        createChildren(root);
        boardListTree = new JTree(root);
        renderer = new ItemListRenderer(boardListTree);
        boardListTree.setCellRenderer(renderer);

        maker.setBoardList(this);

        ItemListML ml = new ItemListML(maker,this);

        boardListTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        boardListTree.addMouseListener(ml);

        boardListView = new JScrollPane(boardListTree);

        add(boardListView,BorderLayout.CENTER);
    }

    public DefaultMutableTreeNode boardRoot() {
        return boardRoot;
    }

    public void reload(DefaultMutableTreeNode node) {
        ((DefaultTreeModel) boardListTree.getModel()).reload(node);
    }

    public HashMap<String,BoardData> boards() {
        return boards;
    }

    public void addBoard(BoardData board) {
        if (selectedBoardNode == null) {
            String input = JOptionPane.showInputDialog(maker.getDisplayer().getFrame(),"Choose a name for the board: ","Name selection",JOptionPane.PLAIN_MESSAGE);
            ItemListBoardNode node = new ItemListBoardNode(input, board);
            boardRoot.insert(node,boardRoot.getChildCount() - 1);
            selectedBoardNode = node;
            boards.put(input,board);
        } else {
            if (selectedBoardNode.data() == null) {
                String input = JOptionPane.showInputDialog(maker.getDisplayer().getFrame(),"Choose a name for the board: ","Name selection",JOptionPane.PLAIN_MESSAGE);
                selectedBoardNode.setName(input);
                selectedBoardNode.setData(board);
                boards.put(input,board);
            } else {
                if (board.getBoard().graphFunction() instanceof Merge) {
                    selectedBoardNode = null;
                    addBoard(board);
                }
                selectedBoardNode.setData(board);
                String name = selectedBoardNode.name();
                boards.replace(name,board);
            }
        }
    }

    public BoardData get(String key) {
        return boards.get(key);
    }

    private void createChildren(DefaultMutableTreeNode root) {
        boardRoot = new DefaultMutableTreeNode("Boards");
        boardRoot.add(new ItemListAddNode(ItemType.Board, this,maker));
        root.add(boardRoot);
        pawnRoot = new DefaultMutableTreeNode("Pawns");
        pawnRoot.add(new ItemListAddNode(ItemType.Pawn, this,maker));
        root.add(pawnRoot);
    }

    public void addEmptyBoard() {
        ItemListBoardNode emptyNode = new ItemListBoardNode("Empty board",null);
        boardRoot.insert(emptyNode,boardRoot.getChildCount() - 1);
        selectedBoardNode = emptyNode;
        ((DefaultTreeModel)boardListTree.getModel()).reload(boardRoot);
        maker.switchBoard(null);
    }

    public JTree tree() {
        return boardListTree;
    }

    public void addPawn(String name, RoleType owner) {
        ItemListPawnNode pawn = new ItemListPawnNode(name,owner);
        pawnRoot.insert(pawn, pawnRoot.getChildCount() - 1);
        addToPieces(name,owner);
        ((DefaultTreeModel)boardListTree.getModel()).reload(pawnRoot);
    }

    private void addToPieces(String name, RoleType owner) {
        if (owner == RoleType.Each) {
            for (int i = 1; i <= maker.getPlayers(); i++) {
                pieces.put(name + i,new Piece(name + i,owner,null,null,null,null,null,null));
            }
        } else if (owner == RoleType.Shared) {
            pieces.put(name,new Piece(name,owner,null,null,null,null,null,null));
        } else {
            pieces.put(name + owner.ordinal(),new Piece(name + owner.ordinal(),owner,null,null,null,null,null,null));
        }
    }

    public HashMap<String, Piece> pieces() {
        return pieces;
    }
}
