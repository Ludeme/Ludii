package app.boardMaker.display.panels.pieceView;

import app.boardMaker.handlers.Maker;
import app.boardMaker.menu.popup.piecepopup.PiecePopupMenu;
import app.boardMaker.utils.BoardData;
import app.boardMaker.utils.CoordinatesUtil;
import app.boardMaker.utils.DrawingUtils;
import app.boardMaker.utils.PieceInfo;
import app.utils.SVGUtil;
import game.equipment.component.Piece;
import graphics.ImageUtil;
import graphics.svg.SVGtoImage;
import main.GameNames;
import main.StringRoutines;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import other.topology.TopologyElement;
import util.SettingsColour;
import view.component.ComponentStyle;
import view.component.custom.PieceStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PieceView extends JPanel {
    private Maker maker;

    private Rectangle placement;

    private boolean adding = false;
    private boolean removing = false;

    private String pieceToAdd;
    private List<Integer> sitesSelected;

    public PieceView(Maker maker) {
        this.maker = maker;

        sitesSelected = new ArrayList<>();

        addMouseListener(new PieceViewListener());
        addKeyListener(new PieceViewKeyListener());
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

        double boardScale = maker.boardScale();
        placement = new Rectangle((int) ((getWidth() - size) / 2 + size * (1 - boardScale) * 0.5),
                (int) ((getHeight() - size) / 2 + size * (1 - boardScale) * 0.5),
                (int) (size * boardScale), (int) (size * boardScale));

        List<? extends TopologyElement> elements = currentBoard.getBoard().topology().getGraphElements(maker.getSiteType());
        HashMap<Integer,Piece> pieces = currentBoard.pieceInfo().piecesPlaced();

        if (!pieces.isEmpty()) {
            System.out.println("Drawing pieces");
            drawPieces(g2d,pieces,elements);
        }

        if (!(adding || removing)) {
            return;
        }

        g2d.setColor(Color.black);
        String text = "Press Enter to confirm.\nPress Esc. to cancel.";
        DrawingUtils.horizontallyCenteredText(g2d,text,getWidth(),getHeight()-text.split("\n").length * g2d.getFontMetrics().getHeight() - 5);

        for (int i = 0; i < elements.size(); i++) {
            if ((currentBoard.pieceInfo().piecesPlaced().containsKey((Integer) i) && adding)
                    || (removing && !currentBoard.pieceInfo().piecesPlaced().containsKey((Integer) i))) {
                continue;
            }
            if (sitesSelected.contains(i)) {
                g2d.setColor(Color.green);
            } else {
                g2d.setColor(Color.gray);
            }
            TopologyElement e = elements.get(i);
            Point2D coord = CoordinatesUtil.screenPosn(e.centroid(),placement);
            int dotSize = maker.getDisplayer().getBoardPanel().dotSize();
            g2d.fillOval(((int) coord.getX()) - dotSize/2, ((int) coord.getY()) - dotSize/2, dotSize, dotSize);
        }
    }

    private void drawPieces(Graphics2D g2d, HashMap<Integer,Piece> pieces, List<? extends TopologyElement> elements) {
        for (int i = 0; i < elements.size(); i++) {
            Piece piece = pieces.get(i);
            Point2D centroid = elements.get(i).centroid();
            Point emplacement = CoordinatesUtil.screenPosn(centroid,placement);
            if (piece != null) {
                BufferedImage pieceImage = getPieceImage(piece);
                g2d.drawImage(pieceImage,emplacement.x - pieceImage.getWidth()/2, emplacement.y - pieceImage.getHeight()/2, null);
            }
        }
    }

    private BufferedImage getPieceImage(Piece piece) {
        int size = maker.cellradius();
        SVGGraphics2D g2d = new SVGGraphics2D(size, size);
        String path = ImageUtil.getImageFullPath(piece.getNameWithoutNumber());

        Color edgeColor = Color.black;
        Color fillColor;
        String trailingNumber = StringRoutines.getTrailingNumbers(piece.name());
        if (trailingNumber.isEmpty() || Integer.parseInt(trailingNumber) == 0) {
            fillColor = SettingsColour.ORIGINAL_PLAYER_COLOURS[0];
        } else {
            fillColor = SettingsColour.ORIGINAL_PLAYER_COLOURS[Integer.parseInt(trailingNumber)];
        }
        SVGtoImage.loadFromFilePath(g2d,path,new Rectangle(0,0,size,size),edgeColor,fillColor,0);
        return SVGUtil.createSVGImage(g2d.getSVGElement(),size,size);
    }

    public void setRemoving() {
        removing = true;
    }

    public void setAdding(String pieceName) {
        adding = true;
        pieceToAdd = pieceName;
    }

    public void resetValues() {
        adding = false;
        removing = false;
        pieceToAdd = null;
        sitesSelected.clear();
    }

    private class PieceViewListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            Point click = new Point(e.getX(),e.getY());

            if (adding || removing) {
                List<? extends TopologyElement> elements = maker.getCurrentBoard().getBoard().topology().getGraphElements(maker.getSiteType());
                for (int i = 0; i < elements.size(); i++) {
                    TopologyElement elem = elements.get(i);
                    Point coord = CoordinatesUtil.screenPosn(elem.centroid(),placement);
                    if (click.distance(coord) < maker.getDisplayer().getBoardPanel().dotSize() / 2) {
                        if (e.getButton() == MouseEvent.BUTTON1 && !sitesSelected.contains(i)) {
                            sitesSelected.add(i);
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            sitesSelected.remove((Integer) i);
                        }
                        repaint();
                        return;
                    }
                }
            }
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

        @Override
        public void mouseEntered(MouseEvent e) {
            PieceView.this.requestFocusInWindow();
        }
    }

    private class PieceViewKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_ENTER) {
                if (adding) {
                    addPieces();
                } else if (removing) {
                    removePieces();
                }
                resetValues();
                repaint();
            } else if (key == KeyEvent.VK_ESCAPE) {
                resetValues();
                repaint();
            }
        }

        private void removePieces() {
            PieceInfo info = maker.getCurrentBoard().pieceInfo();

            for (Integer site : sitesSelected) {
                info.removePiece(site);
            }
        }

        private void addPieces() {
            PieceInfo info = maker.getCurrentBoard().pieceInfo();
            Piece piece = maker.getItemList().pieces().get(pieceToAdd);

            for (Integer site : sitesSelected) {
                info.addPiece(site,piece);
            }
        }
    }
}
