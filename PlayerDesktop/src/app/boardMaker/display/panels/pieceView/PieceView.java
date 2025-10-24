package app.boardMaker.display.panels.pieceView;

import app.boardMaker.handlers.Maker;
import app.boardMaker.utils.BoardData;
import app.utils.SVGUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PieceView extends JPanel {
    private Maker maker;

    public PieceView(Maker maker) {
        this.maker = maker;
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
    }
}
