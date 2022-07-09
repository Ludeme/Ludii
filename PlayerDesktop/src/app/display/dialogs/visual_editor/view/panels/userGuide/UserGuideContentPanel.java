package app.display.dialogs.visual_editor.view.panels.userGuide;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserGuideContentPanel extends JPanel
{

    private JLabel titleLabel = new JLabel();

    public UserGuideContentPanel(String title, List<UserGuideContent> contents)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titleLabel.setText(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel);
        add(Box.createVerticalStrut(20));

        for(UserGuideContent content : contents)
        {
            for(Image image : content.images())
            {
                JLabel imageLabel = new JLabel(new ImageIcon(image));
                add(imageLabel);
            }
            add (Box.createVerticalStrut(4));
            JLabel paragraphLabel = new JLabel(content.paragraph());
            paragraphLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            add(paragraphLabel);
            add (Box.createVerticalStrut(18));
        }

        setVisible(true);
    }

    public UserGuideContentPanel(String title, UserGuideContent content)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titleLabel.setText(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel);

        for(Image image : content.images())
        {
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            add(imageLabel);
        }
        add (Box.createVerticalStrut(4));
        JLabel paragraphLabel = new JLabel(content.paragraph());
        paragraphLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        add(paragraphLabel);
        add (Box.createVerticalStrut(8));

        setVisible(true);
    }
}
