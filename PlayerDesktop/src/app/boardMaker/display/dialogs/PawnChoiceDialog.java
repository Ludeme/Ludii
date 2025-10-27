package app.boardMaker.display.dialogs;

import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.westPanel.itemList.ItemList;
import app.boardMaker.handlers.Maker;
import game.types.play.RoleType;
import game.util.directions.CompassDirection;
import game.util.directions.DirectionUniqueName;
import graphics.svg.SVGLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PawnChoiceDialog extends JDialog {
    private ItemList itemList;
    private Maker maker;

    private JComboBox<RoleType> ownerBox;

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

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel,BoxLayout.Y_AXIS));
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel selectLabel = new JLabel("Select a pawn: ");
        p.add(selectLabel);
        listPanel.add(p);
        listPanel.add(Box.createVerticalStrut(5));
        listPanel.add(new PawnList());
        listPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        mainPanel.add(listPanel);

        mainPanel.add(Box.createVerticalStrut(5));

        p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Owner: ");
        p.add(label);
        ownerBox = createOwnerBox();
        ownerBox.setSelectedItem(RoleType.Each);
        p.add(ownerBox);
        mainPanel.add(p);

        mainPanel.add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new CreateButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                itemList.addPawn("nameField.getText()", (RoleType) ownerBox.getSelectedItem());
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

    private class PawnList extends JTabbedPane {
        List<String> folders;
        HashMap<String,List<String>> files;

        PawnList() {
            setPreferredSize(new Dimension(600,400));

            folders = new ArrayList<>();
            files = new HashMap<>();

            String[] allSVG = SVGLoader.listSVGs();
            String currentFolder = null;
            List<String> filesInFolder = new ArrayList<>();
            for (String svg : allSVG) {
                String[] subs = svg.strip().split("/");
                String folder = subs[2];
                if (!folder.equals(currentFolder)) {
                    if (currentFolder == null) {
                        currentFolder = folder;
                        folders.add(currentFolder);
                    } else {
                        files.put(currentFolder,filesInFolder);
                        currentFolder = folder;
                        folders.add(currentFolder);
                        filesInFolder = new ArrayList<>();
                    }
                }
                String file = subs[subs.length - 1].replace(".svg","");
                if (file.chars().noneMatch(Character::isDigit)) {
                    filesInFolder.add(file);
                }
                if (allSVG[allSVG.length - 1].equals(svg)) {
                    files.put(currentFolder,filesInFolder);
                }
            }

            for (String folder : folders) {
                JList list = new JList(files.get(folder).toArray(new String[0]));
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                list.setVisibleRowCount(-1);

                addTab(folder,new JScrollPane(list));
            }
        }
    }
}
