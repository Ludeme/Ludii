package app.display.dialogs.visual_editor.view.panels.header;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class HeaderPanel extends JPanel {
    public HeaderPanel(){
        setLayout(new BorderLayout());
        setBackground(Color.RED);

        add(new EditorPickerPanel(), BorderLayout.LINE_START);
        add(new ToolsPanel(), BorderLayout.LINE_END);

        setBackground(Color.WHITE);

        int preferredHeight = getPreferredSize().height;
        setPreferredSize(new Dimension(getPreferredSize().width, preferredHeight+20));

    }

}
