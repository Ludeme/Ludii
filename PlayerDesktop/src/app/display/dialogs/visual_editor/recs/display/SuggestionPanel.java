package display;

import codecompletion.Ludeme;
import codecompletion.controller.Controller;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class SuggestionPanel {
    private final JTextPane textarea;
    private final int position;
    private final Controller controller;
    private final JList<Ludeme> list;
    private final JPopupMenu popupMenu;
    private final int insertionPosition;
    private int startBegunWord, endBegunWord;
    private boolean isBegunWord;

    public SuggestionPanel(JTextPane textarea, int position, Point location, Controller controller) {
        this.controller = controller;
        this.textarea = textarea;
        this.position = position;
        this.insertionPosition = position;
        startBegunWord = -1;
        endBegunWord = -1;
        isBegunWord = false;
        popupMenu = new JPopupMenu();
        popupMenu.removeAll();
        popupMenu.setOpaque(false);
        popupMenu.setBorder(null);
        popupMenu.add(list = createSuggestionList(position), BorderLayout.CENTER);
        popupMenu.show(textarea, location.x, textarea.getBaseline(0, 0) + location.y);
    }

    public void hide(SuggestionPanel suggestion) {
        popupMenu.setVisible(false);
        if(suggestion == this) {
            suggestion = null;
        }
    }

    private JList<Ludeme> createSuggestionList(final int position) {
        // create the contextString
        String contextString = textarea.getText().substring(0,position);
        String begunWord = "";
        //TODO change this to include the begun word
        //at the moment, cannot process words that have already begun, therefore cut them out of the context string
        if(contextString.length() > 0) {
            char lastChar = contextString.charAt(position - 1);
            //a new word was begun, if there was any sort of word separator (space, tab, enter) before
            if(lastChar != ' ' && lastChar != KeyEvent.VK_ENTER && lastChar != KeyEvent.VK_TAB) {
                //cut of begun word
                int lastSpacePosition = contextString.lastIndexOf(' ');
                if(lastSpacePosition == -1) {
                    lastSpacePosition = 0;
                }
                begunWord = contextString.substring(lastSpacePosition);
                contextString = textarea.getText().substring(0,lastSpacePosition);

                if(lastSpacePosition > 0) {
                    startBegunWord = lastSpacePosition + 1;
                } else {
                    startBegunWord = 0;
                }
                endBegunWord = position;
                isBegunWord = true;
            }
        }
        List<Ludeme> picklist = controller.getPicklist(contextString, begunWord,10);
        Ludeme[] data = picklist.toArray(new Ludeme[0]);
        JList<Ludeme> list = new JList<>(data);
        list.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    insertSelection();
                }
            }
        });
        return list;
    }

    public boolean insertSelection() {
        if (list.getSelectedValue() != null) {
            try {
                final String selectedSuggestion = list.getSelectedValue().toString();
                if(!isBegunWord) {
                    textarea.getDocument().insertString(insertionPosition, selectedSuggestion + " ", null);
                } else {
                    int len = endBegunWord - startBegunWord;
                    textarea.getDocument().remove(startBegunWord,len);
                    textarea.getDocument().insertString(startBegunWord, selectedSuggestion + " ", null);
                    startBegunWord = -1;
                    endBegunWord = -1;
                    isBegunWord = false;
                }
                hide(this);
                return true;
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }

    public void moveUp() {
        int index = Math.min(list.getSelectedIndex() - 1, 0);
        selectIndex(index);
    }

    public void moveDown() {
        int index = Math.min(list.getSelectedIndex() + 1, list.getModel().getSize() - 1);
        selectIndex(index);
    }

    private void selectIndex(int index) {
        final int position = textarea.getCaretPosition();
        list.setSelectedIndex(index);
        SwingUtilities.invokeLater(() -> textarea.setCaretPosition(position));
    }
}
