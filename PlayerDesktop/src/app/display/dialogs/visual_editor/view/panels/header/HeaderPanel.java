package app.display.dialogs.visual_editor.view.panels.header;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class HeaderPanel extends JPanel {
    public HeaderPanel(){

        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/fonts/Roboto/Roboto-Black.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/fonts/Roboto/Roboto-Bold.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/fonts/Roboto/Roboto-Light.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/fonts/Roboto/Roboto-Medium.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/fonts/Roboto/Roboto-Regular.ttf")));
        } catch (IOException | FontFormatException e) {
            System.out.println(e.getMessage());
        }

        setLayout(new BorderLayout());
        setBackground(Color.RED);

        add(new EditorPickerPanel(), BorderLayout.LINE_START);
        add(new ToolsPanel(), BorderLayout.LINE_END);

        setBackground(Color.WHITE);

        int preferredHeight = getPreferredSize().height;
        setPreferredSize(new Dimension(getPreferredSize().width, preferredHeight+20));

    }

}
