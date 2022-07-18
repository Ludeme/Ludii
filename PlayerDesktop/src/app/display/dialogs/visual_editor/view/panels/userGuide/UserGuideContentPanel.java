package app.display.dialogs.visual_editor.view.panels.userGuide;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserGuideContentPanel extends JPanel
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -3924543171342069471L;
	private JLabel titleLabel = new JLabel();

    public UserGuideContentPanel(String title, List<UserGuideContent> contents)
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titleLabel.setText(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(titleLabel);
        add(Box.createVerticalStrut(15));

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
        add(Box.createVerticalStrut(15));


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

        setVisible(true);
    }
}
