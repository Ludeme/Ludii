package app.boardMaker.display.panels.boardPanel;

import app.boardMaker.handlers.Maker;
import app.boardMaker.utils.BoardData;
import app.boardMaker.utils.Camera;
import app.utils.SVGUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BoardDrawSpace extends JPanel {
    private Maker maker;
    private BoardViewListener bvl;
    private Camera camera;

    public BoardDrawSpace(Maker maker) {
        super(new BorderLayout());

        this.maker = maker;
        this.camera = new Camera(this);
        bvl = new BoardViewListener(maker,this);

        addMouseListener(camera);
        addMouseMotionListener(camera);

        addMouseListener(bvl);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, getWidth(), getHeight());

        BoardData currentBoard = maker.getCurrentBoard();
        if (currentBoard != null) {
            maker.drawBoard(currentBoard,this);
            BufferedImage image = SVGUtil.createSVGImage(currentBoard.getSVG(), getWidth(),getHeight());
            g2d.drawImage(image,camera.offX(), -camera.offY(), null);
        }
    }
}
