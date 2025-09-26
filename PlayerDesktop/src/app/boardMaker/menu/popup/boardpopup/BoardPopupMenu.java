package app.boardMaker.menu.popup.boardpopup;

import app.boardMaker.handlers.Maker;

import javax.swing.*;
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

        item = new JMenuItem("Make Faces");
        item.setActionCommand("make_faces");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Split crossings");
        item.setActionCommand("split_cross");
        item.addActionListener(listener);
        add(item);

        item = new JMenuItem("Trim");
        item.setActionCommand("trim");
        item.addActionListener(listener);
        add(item);
    }
}
