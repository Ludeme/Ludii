package app.display.dialogs.visual_editor.view.panels.editor.textEditor;

import app.display.dialogs.visual_editor.handler.Handler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TextEditor extends JPanel
{

    JTextArea textArea = new JTextArea("");

        public TextEditor()
        {
            super();
            setLayout(new BorderLayout());
            textArea.setText("");
            textArea.addFocusListener(new FocusListener()
            {
                @Override
                public void focusLost(FocusEvent e)
                {
                    textArea.setEditable(true);
                }

                @Override
                public void focusGained(FocusEvent e)
                {
                    textArea.setEditable(false);
                }
            });
            updateLud();
            add(textArea, BorderLayout.CENTER);
        }

        public void updateLud()
        {
            textArea.setText(Handler.toLud());
        }

}
