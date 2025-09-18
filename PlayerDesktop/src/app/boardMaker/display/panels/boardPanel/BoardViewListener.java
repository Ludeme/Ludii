package app.boardMaker.display.panels.boardPanel;

import app.boardMaker.handlers.Maker;
import app.boardMaker.menu.popup.boardpopup.BoardPopupMenu;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BoardViewListener extends MouseAdapter {
    private Maker maker;
    private BoardDrawSpace view;

    public BoardViewListener(Maker maker, BoardDrawSpace view) {
        this.maker = maker;
        this.view = view;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger() && maker.getCurrentBoard() != null) {
            BoardPopupMenu popup = new BoardPopupMenu(maker);
            popup.create();
            popup.show(e.getComponent(),e.getX(),e.getY());
        }
    }
}
