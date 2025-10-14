package app.boardMaker.menu.popup.boardpopup;

import app.boardMaker.display.components.sliders.RotationSlider;
import app.boardMaker.handlers.Maker;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuListener;

public class BoardPopupMenu extends JPopupMenu {
    private Maker maker;
    private BoardPopupMenuListener listener;

    public BoardPopupMenu(Maker maker) {
        this.maker = maker;

        listener = new BoardPopupMenuListener(maker);
    }

    public void create() {
        JMenuItem item;
        JMenu submenu;

        item = new JMenuItem("Clip");
        item.setActionCommand("clip");
        item.addActionListener(listener);
        add(item);

        submenu = new JMenu("Complete");
        JMenuItem submenuItem = new JMenuItem("Full");
        submenuItem.setActionCommand("complete_full");
        submenuItem.addActionListener(listener);
        submenu.add(submenuItem);
        submenuItem = new JMenuItem("Individual");
        submenuItem.setActionCommand("complete_indiv");
        submenuItem.addActionListener(listener);
        submenu.add(submenuItem);
        add(submenu);

        item = new JMenuItem("Dual");
        item.setActionCommand("dual");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Hole");
        item.setActionCommand("hole");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Intersect");
        item.setActionCommand("intersect");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Keep");
        item.setActionCommand("keep");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Make Faces");
        item.setActionCommand("make_faces");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Merge");
        item.setActionCommand("merge");
        item.addActionListener(listener);
        add(item);

        submenu = new JMenu("Remove");
        submenuItem = new JMenuItem("Vertex");
        submenuItem.setActionCommand("remove_v");
        submenuItem.addActionListener(listener);
        submenu.add(submenuItem);
        submenuItem = new JMenuItem("Edge");
        submenuItem.setActionCommand("remove_e");
        submenuItem.addActionListener(listener);
        submenu.add(submenuItem);
        submenuItem = new JMenuItem("Cell");
        submenuItem.setActionCommand("remove_c");
        submenuItem.addActionListener(listener);
        submenu.add(submenuItem);
        add(submenu);

        item = new JMenuItem("Rotate");
        item.setActionCommand("rotate");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Scale");
        item.setActionCommand("scale");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Skew");
        item.setActionCommand("skew");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Split crossings");
        item.setActionCommand("split_cross");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Subdivide cell");
        item.setActionCommand("subdivide");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Trim");
        item.setActionCommand("trim");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Union");
        item.setActionCommand("union");
        item.addActionListener(listener);
        add(item);
    }
}
