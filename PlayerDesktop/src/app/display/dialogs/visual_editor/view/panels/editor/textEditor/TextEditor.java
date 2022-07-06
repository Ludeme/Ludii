package app.display.dialogs.visual_editor.view.panels.editor.textEditor;

import app.display.dialogs.visual_editor.handler.Handler;

import javax.swing.*;
import java.awt.*;

public class TextEditor extends JPanel
{

    JLabel label = new JLabel("");

        public TextEditor()
        {
            super();
            setLayout(new BorderLayout());
            label.setText("");
            add(label, BorderLayout.CENTER);
        }

        @Override
        public void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            String lud = Handler.toLud();
            if(lud != null)
                label.setText(lud);
            else
                label.setText("");
        }

}
