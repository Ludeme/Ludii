package app.display.dialogs.visual_editor.recs.display;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.DocHandler;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.LudiiGameDatabase;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class OpenGameFromFileDialog {

    private final JFrame frame;
    private final TextEditor textEditor;
    private JDialog dialog;
    private JLabel label;
    private JComboBox<String> comboBox;
    private JButton button;
    private JPanel panel;
    private Listener listener;

    public OpenGameFromFileDialog(TextEditor textEditor) {
        this.textEditor = textEditor;
        this.frame = textEditor.getFrame();
        init();
    }

    private void init() {
        listener = new Listener();
        dialog = new JDialog(frame,"Open Game from File");
        panel = new JPanel(new BorderLayout());
        LudiiGameDatabase db = LudiiGameDatabase.getInstance();
        List<String> names = db.getNames();
        String[] items = names.toArray(new String[0]);
        comboBox = new JComboBox<>(items);
        comboBox.setEditable(true);

        panel.add(comboBox, BorderLayout.CENTER);

        button = new JButton("Open Game");
        button.addActionListener(listener);

        panel.add(button,BorderLayout.SOUTH);

        label = new JLabel("Select game to open");

        panel.add(label,BorderLayout.NORTH);

        dialog.add(panel);

        dialog.setSize(new Dimension(420,340));
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(null);
        Image img = new ImageIcon(DocHandler.getInstance().getLogoLocation()).getImage();
        dialog.setIconImage(img);
        dialog.setVisible(true);
    }

    private class Listener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int gameID = comboBox.getSelectedIndex();
            LudiiGameDatabase db = LudiiGameDatabase.getInstance();
            List<String> locations = db.getLocations();
            String location = locations.get(gameID);
            if(location == null || location.equals("")) {
                dialog.dispose();
                return;
            }
            textEditor.openGameFromFile(location);
            dialog.dispose();
        }
    }
}
