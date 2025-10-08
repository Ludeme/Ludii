package app.boardMaker.display.panels.boardPanel;

import app.boardMaker.handlers.Maker;
import app.boardMaker.menu.popup.boardpopup.BoardPopupMenu;
import app.boardMaker.utils.Camera;
import app.boardMaker.utils.CoordinatesUtil;
import other.topology.Vertex;
import util.LocationUtil;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class BoardViewListener extends MouseAdapter {
    private Maker maker;
    private BoardDrawSpace view;
    private Camera camera;

    private BufferedImage image;

    public BoardViewListener(Maker maker, BoardDrawSpace view, Camera camera) {
        this.maker = maker;
        this.view = view;
        this.camera = camera;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (maker.getCurrentBoard() == null) {
            return;
        }
        Point pt = new Point(e.getX(),e.getY());
        System.out.println(pt);
        System.out.println(CoordinatesUtil.boardPosn(pt,maker.getCurrentBoard().getPlacement(), maker.getCurrentBoard().getBoard().graph()));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger() && maker.getCurrentBoard() != null) {
            BoardPopupMenu popup = new BoardPopupMenu(maker);
            popup.create();
            popup.show(e.getComponent(),e.getX(),e.getY());
        }
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }
}
