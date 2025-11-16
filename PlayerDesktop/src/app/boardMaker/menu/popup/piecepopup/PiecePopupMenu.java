package app.boardMaker.menu.popup.piecepopup;

import app.boardMaker.display.panels.pieceView.PieceView;
import app.boardMaker.handlers.Maker;
import game.equipment.component.Piece;
import game.types.play.RoleType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PiecePopupMenu extends JPopupMenu {
    private Maker maker;
    private PieceView view;

    public PiecePopupMenu(Maker maker, PieceView view) {
        this.maker = maker;
        this.view = view;
    }

    public void create() {
        ActionListener listener = new PiecePopupListener();

        JMenuItem item;
        JMenu submenu;
        JMenuItem submenuItem;

        submenu = new JMenu("Add piece");
        for (Piece piece : maker.state().currentPieceInfo().piecesUsed()) {
            if (piece.role() == RoleType.Each) {
                for (int i = 1; i <= maker.getPlayers(); i++) {
                    String pieceName = piece.name() + i;
                    submenuItem = new JMenuItem(pieceName);
                    submenuItem.setActionCommand(pieceName);
                    submenuItem.addActionListener(listener);
                    submenu.add(submenuItem);
                }
            } else if (piece.role() == RoleType.Shared) {
                submenuItem = new JMenuItem(piece.name());
                submenuItem.setActionCommand(piece.name());
                submenuItem.addActionListener(listener);
                submenu.add(submenuItem);
            } else {
                submenuItem = new JMenuItem(piece.name());
                submenuItem.setActionCommand(piece.name() + piece.role().ordinal());
                submenuItem.addActionListener(listener);
                submenu.add(submenuItem);
            }
        }
        add(submenu);

        add(new JSeparator(SwingConstants.HORIZONTAL));

        item = new JMenuItem("Remove piece");
        item.setActionCommand("remove");
        item.addActionListener(listener);
        add(item);
    }

    private class PiecePopupListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();

            switch (action) {
                case "remove" :
                    view.setRemoving();
                    view.repaint();
                    break;

                default :
                    view.setAdding(action);
                    view.setPiece(e.getActionCommand());
                    view.repaint();
                    break;
            }
        }
    }
}
