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
        JMenuItem item = new JMenuItem("Dual");
        item.setActionCommand("Dual");
        item.addActionListener(listener);
        add(item);
    }
}
