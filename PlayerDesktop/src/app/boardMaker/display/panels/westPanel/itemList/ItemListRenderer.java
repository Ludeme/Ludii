package app.boardMaker.display.panels.westPanel.itemList;

import app.boardMaker.display.panels.westPanel.itemList.nodes.ItemListAddNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.Objects;

public class ItemListRenderer extends DefaultTreeCellRenderer {
    private JTree tree;

    public ItemListRenderer(JTree tree) {
        this.tree = tree;
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (leaf && isAdd(value)) {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(ItemListRenderer.class.getResource("/plus.png")));
            int size = tree.getRowHeight() == 0 ? 20 : tree.getRowHeight();
            ImageIcon resizedIcon = new ImageIcon(icon.getImage().getScaledInstance(size,size,Image.SCALE_DEFAULT));
            setIcon(resizedIcon);
        }

        return this;
    }

    private boolean isAdd(Object value) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        return node instanceof ItemListAddNode;
    }
}
