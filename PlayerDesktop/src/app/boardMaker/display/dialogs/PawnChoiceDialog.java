package app.boardMaker.display.dialogs;

import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.westPanel.itemList.ItemList;
import app.boardMaker.handlers.Maker;
import game.types.play.RoleType;
import game.util.directions.CompassDirection;
import game.util.directions.DirectionUniqueName;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class PawnChoiceDialog extends JDialog {
    private ItemList itemList;
    private Maker maker;

    private JTextField nameField;
    private JComboBox<RoleType> ownerBox;
    private JComboBox<CompassDirection> directionBox;

    public PawnChoiceDialog(ItemList itemList, Maker maker) {
        this.itemList = itemList;
        this.maker = maker;

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle("Choose a piece");

        JPanel mainPanel = createMainPanel();

        setContentPane(mainPanel);
        requestFocus();

        pack();
        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));

        mainPanel.add(Box.createVerticalStrut(5));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Name: ");
        p.add(label);
        nameField = new JTextField(10);
        p.add(nameField);
        mainPanel.add(p);

        mainPanel.add(Box.createVerticalStrut(5));

        p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel("Owner: ");
        p.add(label);
        ownerBox = createOwnerBox();
        ownerBox.setSelectedItem(RoleType.Each);
        p.add(ownerBox);
        mainPanel.add(p);

        mainPanel.add(Box.createVerticalStrut(5));

        p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel("Direction: ");
        p.add(label);
        directionBox = new JComboBox<>(CompassDirection.values());
        directionBox.setSelectedItem(null);
        p.add(directionBox);
        mainPanel.add(p);

        mainPanel.add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new CreateButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                itemList.addPawn(nameField.getText(), (RoleType) ownerBox.getSelectedItem(), (CompassDirection) directionBox.getSelectedItem());
                dispose();
            }
        }));
        buttonPanel.add(new CancelButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }));
        mainPanel.add(buttonPanel);

        wrapper.add(mainPanel,BorderLayout.PAGE_START);

        return wrapper;
    }

    private JComboBox<RoleType> createOwnerBox() {
        ArrayList<RoleType> owners = new ArrayList<>();

        for (int i = 1; i <= maker.getPlayers(); i++) {
            owners.add(RoleType.roleForPlayerId(i));
        }

        owners.add(RoleType.Neutral);
        owners.add(RoleType.Each);
        owners.add(RoleType.Shared);

        return new JComboBox<>(owners.toArray(new RoleType[0]));
    }
}
