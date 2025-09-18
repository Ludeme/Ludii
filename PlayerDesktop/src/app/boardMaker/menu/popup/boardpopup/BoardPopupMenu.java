package app.boardMaker.menu.popup.boardpopup;

import app.boardMaker.handlers.Maker;

import javax.swing.*;

public class BoardPopupMenu extends JPopupMenu {
    private Maker maker;

    public BoardPopupMenu(Maker maker) {
        this.maker = maker;
    }

    public void create() {
        JMenuItem item = new JMenuItem("Dual");
        add(item);
    }
}
