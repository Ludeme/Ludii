package app.display.dialogs.visual_editor.recs.display;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.DocHandler;

import javax.swing.*;
import java.awt.*;

public class ProgressBar {
    private final String operationDescription;
    private JDialog dialog;
    private JProgressBar progressBar;
    private final int operationsMax;
    private final String operationName;
    private double progress;
    private JPanel panel;
    private JLabel label;


    public ProgressBar(String operationName, String operationDescription, int operationsMax) {
        this.operationsMax = operationsMax;
        this.operationName = operationName;
        this.operationDescription = operationDescription;
        init();
    }

    private void init(){
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        progress = 0.0;
        TextEditor textEditor = TextEditor.getInstance();
        if (textEditor == null) {
            dialog = new JDialog();
            dialog.setTitle(operationName);
        } else {
            dialog = new JDialog(textEditor.getFrame(), operationName);
        }
        panel = new JPanel();
        progressBar = new JProgressBar((int) progress);
        label = new JLabel(operationDescription);
        progressBar.setPreferredSize(new Dimension((int)(screenWidth*0.20), (int)(screenHeight*0.036)));
        progressBar.setStringPainted(true);
        label.setFont(new Font("Dialog", Font.BOLD, 20));
        label.setLabelFor(progressBar);
        panel.add(label);
        panel.add(progressBar);
        dialog.add(panel);
        Image img = new ImageIcon(DocHandler.getInstance().getLogoLocation()).getImage();
        dialog.setIconImage(img);
        dialog.setSize((int)(screenWidth*0.237), (int)(screenHeight*0.10));
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        dialog.requestFocus();
        dialog.setVisible(true);
    }

    /**
     * Sets the progress of the bar to the specified percent level
     * @param percent
     */
    public void updateProgress(double percent) {
        this.progress = percent*100;
        int progressInt = (int) progress;
        progressBar.setValue(progressInt);
        progressBar.setVisible(true);
        label.setVisible(true);
        //System.out.println(progressInt);
        dialog.invalidate();
        dialog.validate();
        dialog.repaint();
    }

    /**
     * Calculates the progress of the bar based on maxOperations and operations finished
     * @param operationsFinished
     */
    public void updateProgress(int operationsFinished) {
        double percent = (((double) operationsFinished) / ((double) operationsMax));
        updateProgress(percent);
    }

    public void close() {
        progressBar.setString("Finished");
        long delta = 500;
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        while (!((end - start) > delta)) {
            end = System.currentTimeMillis();
        }
        dialog.dispose();
    }
}
