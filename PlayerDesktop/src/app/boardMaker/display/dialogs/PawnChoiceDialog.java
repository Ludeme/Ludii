package app.boardMaker.display.dialogs;

import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.westPanel.itemList.ItemList;
import app.boardMaker.handlers.Maker;
import app.utils.SVGUtil;
import game.types.play.RoleType;
import game.util.directions.CompassDirection;
import game.util.directions.DirectionUniqueName;
import graphics.ImageUtil;
import graphics.svg.SVGLoader;
import graphics.svg.SVGtoImage;
import org.jfree.graphics2d.svg.SVGGraphics2D;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PawnChoiceDialog extends JDialog {
    private ItemList itemList;
    private Maker maker;

    private JComboBox<RoleType> ownerBox;
    private String selectedPawn = "Dot";

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
                itemList.addPawn(selectedPawn, (RoleType) ownerBox.getSelectedItem());
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
                String[] subs = svg.substring(1).split("/");
                String folder = subs[1];
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

            PawnListCellRenderer renderer = new PawnListCellRenderer();

            for (String folder : folders) {
                if (folder.equals("toolButtons")) {
                    continue;
                }
                if (files.get(folder).isEmpty()) {
                    continue;
                }
                JList<String> list = new JList<>(files.get(folder).toArray(new String[0]));
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                list.setVisibleRowCount(-1);
                list.setCellRenderer(renderer);
                list.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        selectedPawn = ((JList)e.getSource()).getSelectedValue().toString();
                    }
                });

                addTab(folder,new JScrollPane(list));
            }
        }
    }

    private class PawnListCellRenderer extends JLabel implements ListCellRenderer{
        PawnListCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setVerticalAlignment(CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            String filename = ImageUtil.getImageFullPath((String) value);
            int size = list.getWidth() / 10;
            SVGGraphics2D g2d = new SVGGraphics2D(size, size);
            SVGtoImage.loadFromFilePath(g2d,filename,new Rectangle(0,0,size,size),Color.black,Color.white,0);
            BufferedImage image = SVGUtil.createSVGImage(g2d.getSVGElement(),size,size);
            if (image != null) {
                ImageIcon icon = new ImageIcon(SVGUtil.createSVGImage(g2d.getSVGElement(),size,size));
                setIcon(icon);
            }

            setText((String) value);
            return this;
        }
    }
}
