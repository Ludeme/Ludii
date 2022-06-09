package display;

import codecompletion.controller.Controller;
import codecompletion.domain.filehandling.DocHandler;
import codecompletion.domain.filehandling.GameFileHandler;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLightLaf;
import utils.NGramUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Look and feel from https://search.maven.org/artifact/com.formdev/flatlaf/2.2/jar
 * https://www.formdev.com/flatlaf/#getting_started 5/16/22
 *
 * Picklist visualization from:
 * https://stackoverflow.com/questions/10873748/how-to-show-autocomplete-as-i-type-in-jtextarea
 * by user: https://stackoverflow.com/users/928711/guillaume-polet
 * on 5/16/22
 */
public class TextEditor {
    private static int N;
    private final String gameDescription;
    private JFrame frame;
    private  JButton button;
    private JTextPane textArea;
    private Controller controller;
    private Listener listener;
    private JPanel panel;
    private JScrollPane scrollPane;
    private JMenuBar menuBar;
    private TextLineNumber textLineNumber;
    private JFileChooser fileChooser;
    private SuggestionPanel suggestion;

    private String gameName;
    private String fileLocation;
    private boolean lightMode;

    //


    // Singleton
    private static TextEditor textEditor;

    public static TextEditor getInstance() {
        return textEditor;
    }

    public static void createInstance(int N) {
        if(textEditor == null) {
            textEditor = new TextEditor(N);
        }
    }

    private TextEditor(int N) {
        gameName = "New Game";
        this.gameDescription = "";
        this.N = N;
        lightMode = true;
        init();
    }

    private void init() {
        setMode(lightMode);
        controller = new Controller(N);
        fileChooser = new JFileChooser("res/");
        frame = new JFrame("Editing: "+gameName);
        listener = new Listener();
        button = new JButton("Compile");
        button.addActionListener(listener);
        textArea = new JTextPane();
        MakeUndoable.makeUndoable(textArea);
        textArea.setFont(new Font(Font.MONOSPACED,Font.BOLD, 22));
        setColorScheme(lightMode);
        textArea.addKeyListener(new SuggestionListener());
        textArea.setText(gameDescription);
        textLineNumber = new TextLineNumber(textArea);
        scrollPane = new JScrollPane(textArea);
        scrollPane.setRowHeaderView(textLineNumber);
        panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        panel.add(button,BorderLayout.SOUTH);

        //menu
        menuBar = new JMenuBar();
        menuBar.setMinimumSize(new Dimension(1280,30));
        JMenu file = new JMenu("File");
        JMenuItem openNewGame = new JMenuItem("Open New Game");
        openNewGame.addActionListener(listener);
        file.add(openNewGame);
        JMenuItem openGameFromFile = new JMenuItem("Open Game from File");
        openGameFromFile.addActionListener(listener);
        file.add(openGameFromFile);
        JMenuItem save = new JMenuItem("Save");
        save.addActionListener(listener);
        file.add(save);
        JMenuItem saveAs = new JMenuItem("Save as");
        saveAs.addActionListener(listener);
        file.add(saveAs);
        JMenuItem load = new JMenuItem("Load Game");
        load.addActionListener(listener);
        file.add(load);

        JMenu options = new JMenu("Options");
        JMenuItem appearance = new JMenuItem("Change appearance");
        appearance.addActionListener(listener);
        options.add(appearance);
        JMenuItem codeCompletion = new JMenuItem("Change Code Completion Model");
        codeCompletion.addActionListener(listener);
        options.add(codeCompletion);

        menuBar.add(file);
        menuBar.add(options);


        frame.setJMenuBar(menuBar);

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        Image img = new ImageIcon(DocHandler.getInstance().getLogoLocation()).getImage();
        frame.setIconImage(img);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            /**
             * Will be executed when the frame is closed
             * @param e
             */
            public void windowClosing(WindowEvent e) {
                controller.close();
            }
        });
        frame.setSize(new Dimension(1280,720));
        frame.setVisible(true);

    }

    protected void showSuggestionLater() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showSuggestion();
            }

        });
    }

    protected void showSuggestion() {
        hideSuggestion();
        final int position = textArea.getCaretPosition();
        Point location;
        try {
            location = textArea.modelToView(position).getLocation();
        } catch (BadLocationException e2) {
            e2.printStackTrace();
            return;
        }
        suggestion = new SuggestionPanel(textArea, position, location, controller);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textArea.requestFocusInWindow();
            }
        });
    }

    private void hideSuggestion() {
        if (suggestion != null) {
            suggestion.hide(suggestion);
        }
    }

    public void openGameFromFile(String location) {
        String gameDescription = GameFileHandler.readGame(location);
        textArea.setText(gameDescription.substring(1,gameDescription.length()));
    }

    private void setColorScheme(boolean lightMode) {
        if(textArea != null) {
            if (lightMode) {
                textArea.setBackground(Color.decode("#ffffff"));
            } else {//dark mode
                textArea.setBackground(Color.decode("#2b2b2b"));
            }
            //automatically handles the color scheme
            ((AbstractDocument) textArea.getDocument()).setDocumentFilter(new ColorDocumentFilter(this));
            textArea.setCaretPosition(0);
        }
    }

    public void setMode(boolean lightMode) {
        this.lightMode = lightMode;
        if(lightMode) {
            lightMode();
        } else {
            darkMode();
        }

        if(frame != null) {
            SwingUtilities.updateComponentTreeUI(frame);
        }
        setColorScheme(lightMode);
    }
    private void lightMode() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }
    }

    private void darkMode() {
        // Dark LAF
        try {
            UIManager.setLookAndFeel( new FlatDarculaLaf());
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public JTextPane getTextArea() {
        return textArea;
    }

    public boolean isLightMode() {
        return lightMode;
    }

    private class SuggestionListener implements KeyListener {

        /**
         * Invoked when a key has been typed.
         * See the class description for {@link KeyEvent} for a definition of
         * a key typed event.
         *
         * @param e
         */
        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER && suggestion != null) {
                System.out.println("LAST TYPED: "+e.getKeyChar() + " " + (e.getKeyChar() != ' '));
                if (suggestion.insertSelection()) {
                    e.consume();
                    final int position = textArea.getCaretPosition();
                    SwingUtilities.invokeLater(() -> {
                        try {
                            textArea.getDocument().remove(position - 1, 1);
                        } catch (BadLocationException e1) {
                            e1.printStackTrace();
                        }
                    });
                    suggestion = null;
                }
            }
        }

        /**
         * Invoked when a key has been pressed.
         * See the class description for {@link KeyEvent} for a definition of
         * a key pressed event.
         *
         * @param e
         */
        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_SPACE && e.isControlDown()) {
                //CTRL + SPACE: show suggestion
                showSuggestionLater();
            }
            if(e.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
                //CTRL + S: save
                listener.save();
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                System.out.println("hello");
                hideSuggestion();
                suggestion = null;
            }
        }

        /**
         * Invoked when a key has been released.
         * See the class description for {@link KeyEvent} for a definition of
         * a key released event.
         *
         * @param e
         */
        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_DOWN && suggestion != null) {
                suggestion.moveDown();
            } else if (e.getKeyCode() == KeyEvent.VK_UP && suggestion != null) {
                suggestion.moveUp();
            } else if (Character.isLetterOrDigit(e.getKeyChar())) {
                showSuggestionLater();
            } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && suggestion != null) {
                hideSuggestion();
                showSuggestionLater();
            }
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                textArea.setText(textArea.getText().replaceAll("\r\n","\n"));
            }
        }
    }

    private class Listener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String s = e.getActionCommand();
            if (s.equals(button.getActionCommand())) {
                // set the text of the label to the text of the field
                //TODO make this method compile the current gamedescription
                ProgressBar pb = new ProgressBar("Compiling...","Currently this feature is WIP.",100);

            } else if (s.equals("Open New Game")) {
                askToSave(false);
            } else if (s.equals("Save")) {
                // if saved before, save there else perform save as
                if(fileLocation == null) {
                    saveAs();
                } else {
                    save();
                }
            } else if (s.equals("Save as")) {
                // open dialog to select storage location
                saveAs();
            } else if (s.equals("Load Game")) {
                // open dialog to select storage location
                askToSave(true);
            } else if (s.equals("Change appearance")) {
                AppearanceDialog appearanceDialog = new AppearanceDialog(TextEditor.this);
            } else if (s.equals("Change Code Completion Model")) {
                CodeCompletionDialog ccDialog = new CodeCompletionDialog(frame,controller);
            } else if (s.equals("Open Game from File")) {
                OpenGameFromFileDialog gameFromFileDialog = new OpenGameFromFileDialog(TextEditor.this);
            }
        }
        private void load() {
            fileChooser = new JFileChooser("res/");
            fileChooser.setDialogTitle("Choose a game to load in");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.addChoosableFileFilter(ludFilter);
            fileChooser.setAcceptAllFileFilterUsed(false);
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File directory = fileChooser.getSelectedFile();
                fileLocation = directory.getPath();
            }
            if(fileLocation == null) {
                return;
            }
            String gameDescription = GameFileHandler.readGame(fileLocation);
            textArea.setText(gameDescription.substring(1,gameDescription.length()));
            textArea.setCaretPosition(0);
        }

        private void save() {
            if(fileLocation == null) {
                saveAs();
            } else {
                String gameDescription = textArea.getText();
                gameName = NGramUtils.getGameName(gameDescription);
                gameName = gameName == null ? "New Game" : gameName;
                if(fileLocation.endsWith(".lud")) {
                    GameFileHandler.writeGame(gameDescription,fileLocation);
                } else {
                    GameFileHandler.writeGame(gameDescription,fileLocation+"\\"+gameName+".lud");
                }
                JDialog d = new JDialog(frame,"Saved Game Successfully");
                JButton okButton = new JButton("Okay");
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        d.dispose();
                    }
                });
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        d.dispose();
                    }
                };
                timer.schedule(task, 1250);
                d.add(okButton);
                d.setLocationRelativeTo(null);
                d.setSize(500,180);
                d.setResizable(false);
                d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                d.setVisible(true);
            }
        }

        private void saveAs() {
            fileChooser = new JFileChooser("res/");
            fileChooser.setDialogTitle("Choose a directory to save your game.");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File directory = fileChooser.getSelectedFile();
                fileLocation = directory.getPath();
            }
            save();
        }

        public void askToSave(boolean load) {
            if(textArea.getText().equals("")) {
                if(load) {
                    load();
                }
                return;
            }
            Dialog d = new Dialog(frame, "Do you want to save your game?");
            JPanel dPanel = new JPanel(new GridLayout(1,2));
            JButton yes = new JButton("Yes");
            yes.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    save();
                    d.dispose();
                    textArea.setText("");
                    if(load) {
                        load();
                    }
                }
            });
            dPanel.add(yes);
            JButton no = new JButton("No");
            no.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    d.dispose();
                    textArea.setText("");
                    if(load) {
                        load();
                    }
                }
            });
            dPanel.add(no);
            d.add(dPanel);
            d.setSize(new Dimension(500,150));
            d.setResizable(false);
            d.setLocationRelativeTo(null);
            d.setVisible(true);
        }
    }

    private static FileFilter ludFilter = new FileFilter() {
        public String getDescription() {
            return "Ludii Game Files (*.lud)";
        }

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            } else {
                return f.getName().toLowerCase().endsWith(".lud");
            }
        }
    };
}
