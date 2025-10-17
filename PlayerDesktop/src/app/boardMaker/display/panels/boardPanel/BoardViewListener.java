package app.boardMaker.display.panels.boardPanel;

import app.boardMaker.handlers.Maker;
import app.boardMaker.menu.popup.boardpopup.BoardPopupMenu;
import app.boardMaker.utils.CoordinatesUtil;
import game.types.board.SiteType;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class BoardViewListener extends MouseAdapter {
    private Maker maker;
    private BoardDrawSpace view;

    public BoardViewListener(Maker maker, BoardDrawSpace view) {
        this.maker = maker;
        this.view = view;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (maker.getCurrentBoard() == null) {
            return;
        }

        Point click = new Point(e.getX() - view.camera().offX(),e.getY() + view.camera().offY());

        if (view.createPoly()) {
            if (e.getButton() == 1) {
                view.addPolygonVertex(click);
            } else if (e.getButton() == 3) {
                view.removePolygonVertex(click);
            }
        } else if (view.isRemoving()) {
            if (e.getButton() == 1) {
                view.addIndex(click);
            } else if (e.getButton() == 3) {
                view.removeIndex(click);
            }
        } else if (view.isAdding()) {
            if (e.getButton() == 1) {
                view.addVertex(click);
            } else if (e.getButton() == 3) {
                view.removeVertex(click);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (maker.getCurrentBoard() == null || view.createPoly() || view.isRemoving() || view.isAdding()) {
            return;
        }

        Point click = new Point(e.getX(),e.getY());
        if (e.isPopupTrigger() && clickOnBoard(click,maker.getCurrentBoard().getPlacement())) {
            BoardPopupMenu popup = new BoardPopupMenu(maker);
            popup.create();
            popup.show(e.getComponent(),e.getX(),e.getY());
        }
    }

    private boolean clickOnBoard(Point click, Rectangle placement) {
        if (click.getX() > placement.getX() && click.getX() < placement.getMaxX()) {
            if (click.getY() > placement.getY() && click.getY() < placement.getMaxY()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        view.requestFocusInWindow();
    }
}
