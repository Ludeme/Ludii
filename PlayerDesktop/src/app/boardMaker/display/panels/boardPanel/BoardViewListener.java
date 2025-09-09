package app.boardMaker.display.panels.boardPanel;

import app.boardMaker.handlers.Maker;

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
    public void mouseClicked(MouseEvent e) {
        System.out.println("Mouse clicked at: " + view.getMousePosition());

        view.revalidate();
        view.repaint();
    }

}
