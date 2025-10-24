package app.boardMaker.display.panels.westPanel.itemList;

import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListAddNode;
import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListBoardNode;
import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListPawnNode;
import app.boardMaker.handlers.Maker;
import app.boardMaker.res.ItemType;
import app.boardMaker.utils.BoardData;
import game.types.play.RoleType;
import game.util.directions.CompassDirection;

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
    private DefaultMutableTreeNode pawnRoot;

    private HashMap<String, BoardData> boards;
    protected ItemListBoardNode selectedBoardNode;


    public ItemList(Maker maker) {
        super(new BorderLayout());

        this.maker = maker;
        boards = new HashMap<>();

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

    public void addBoard(String name, BoardData board) {
        if (selectedBoardNode == null) {
            ItemListBoardNode node = new ItemListBoardNode(name, maker.getCurrentBoard());
            boardRoot.insert(node,boardRoot.getChildCount() - 1);
            selectedBoardNode = node;
        } else {
            if (selectedBoardNode.data() == null) {
                selectedBoardNode.setName(name);
                selectedBoardNode.setData(board);
            } else {
                String oldname = selectedBoardNode.name();
                selectedBoardNode.setName(name);
                selectedBoardNode.setData(board);
                boards.remove(oldname);
            }
        }
        boards.put(name,board);
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

    public void addPawn(String name, RoleType owner, CompassDirection direction) {
        ItemListPawnNode pawn = new ItemListPawnNode(name,owner,direction);
        pawnRoot.insert(pawn, pawnRoot.getChildCount() - 1);
        ((DefaultTreeModel)boardListTree.getModel()).reload(pawnRoot);
    }
}
