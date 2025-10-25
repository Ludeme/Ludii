package app.boardMaker.display.panels.pieceView;

import app.boardMaker.handlers.Maker;
import app.boardMaker.menu.popup.piecepopup.PiecePopupMenu;
import app.boardMaker.utils.BoardData;
import app.boardMaker.utils.CoordinatesUtil;
import app.boardMaker.utils.DrawingUtils;
import app.utils.SVGUtil;
import other.topology.TopologyElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class PieceView extends JPanel {
    private Maker maker;

    private boolean adding = false;
    private boolean removing = false;

    public PieceView(Maker maker) {
        this.maker = maker;

        addMouseListener(new PieceViewListener());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setBackground(Color.white);
        g2d.clearRect(0,0,getWidth(),getHeight());

        if (maker.getCurrentBoard() == null) {
            return;
        }

        BoardData currentBoard = maker.getCurrentBoard();
        maker.drawBoard(currentBoard,this,null);
        int size = Math.min(getWidth(),getHeight());
        BufferedImage image = SVGUtil.createSVGImage(currentBoard.getSVG(), size,size);
        g2d.drawImage(image, (getWidth() - size) / 2, (getHeight() - size) / 2, null);

        if (!(adding || removing)) {
            return;
        }

        g2d.setColor(Color.black);
        String text = "Press Enter to confirm.\nPress Esc. to cancel.";
        DrawingUtils.horizontallyCenteredText(g2d,text,getWidth(),getHeight()-text.split("\n").length * g2d.getFontMetrics().getHeight() - 5);

        double boardScale = maker.boardScale();
        Rectangle placement = new Rectangle((int) ((getWidth() - size) / 2 + size * (1 - boardScale) * 0.5),
                (int) ((getHeight() - size) / 2 + size * (1 - boardScale) * 0.5),
                (int) (size * boardScale), (int) (size * boardScale));

        g2d.setColor(Color.gray);
        List<? extends TopologyElement> elements = currentBoard.getBoard().topology().getGraphElements(maker.getSiteType());
        for (TopologyElement e : elements) {
            Point2D coord = CoordinatesUtil.screenPosn(e.centroid(),placement);
            int dotSize = maker.getDisplayer().getBoardPanel().dotSize();
            g2d.fillOval(((int) coord.getX()) - dotSize/2, ((int) coord.getY()) - dotSize/2, dotSize, dotSize);
        }
    }

    public void setRemoving() {
        removing = true;
    }

    public void setAdding() {
        adding = true;
    }

    private class PieceViewListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            Point click = new Point(e.getX(),e.getY());


        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (adding || removing) {
                return;
            }

            if (e.isPopupTrigger()) {
                PiecePopupMenu popup = new PiecePopupMenu(maker, PieceView.this);
                popup.create();
                popup.show(e.getComponent(),e.getX(),e.getY());
            }
        }
    }
}
