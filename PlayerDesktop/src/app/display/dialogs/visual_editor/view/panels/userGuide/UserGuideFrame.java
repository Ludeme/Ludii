package app.display.dialogs.visual_editor.view.panels.userGuide;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the user guide for the visual editor
 */

public class UserGuideFrame extends JFrame
{

    List<UserGuideContentPanel> panels = new ArrayList<>();
    UserGuideContentPanel currentPanel;
    JScrollPane scrollPane;
    int currentIndex;

    /**
     * Constructor
     */
    public UserGuideFrame()
    {
        // set frame properties
        setTitle("User Guide");
        setSize(new Dimension(750, 440));
        setLocationRelativeTo(Handler.visualEditorFrame);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setBackground(DesignPalette.BACKGROUND_EDITOR());

        initialiseContents();

        currentIndex = 0;
        currentPanel = panels.get(currentIndex);
        scrollPane = new JScrollPane(currentPanel);

        // three buttons: previous, next, close
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(DesignPalette.BACKGROUND_EDITOR());

        JButton previousButton = new JButton("Previous");
        previousButton.addActionListener(e -> previousPanel());
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(e -> nextPanel());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        buttonPanel.add(previousButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(closeButton);


        add(buttonPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);



        setResizable(false);
        setVisible(true);

        // add user guide panel
        //add(new UserGuidePanel());
    }

    private void initialiseContents()
    {

        String image;
        List<String> images = new ArrayList<>();
        String paragraph = "";
        UserGuideContent content;
        List<UserGuideContent> contents;
        UserGuideContentPanel panel;

        // What are nodes?
        image = "game_tooltip.png";
        paragraph = "<html> " +
                "A node is a graphical element representing a Ludeme. <br>" +
                "Ludemes describe concepts related to rules and equipment. <br>" +
                "By hovering above the node's title, a description of the concept is displayed. <br>" +
                "<br>" +
                "Most Ludemes need to be provided with arguments.<br>" +
                "<br>" +
                "By creating and supplying nodes, a game can be described." +
                "</html>";

        content = new UserGuideContent(paragraph, image);
        panel = new UserGuideContentPanel("What are nodes?", content);
        panels.add(panel);


        // Constructors
        image = "game_node_constructors.png";
        paragraph = "<html>" +
                "A node can have different constructors. To access the list of constructors,<br>" +
                "click on the top right button of the node." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents = new ArrayList<>();
        contents.add(content);

        image = "piece_argument_tooltip.png";
        paragraph = "<html>" +
                "You can hover over an argument field to display a short description of it." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents.add(content);

        panel = new UserGuideContentPanel("Constructors", contents);
        panels.add(panel);


        // Non-Terminal Arguments
        image = "established_connection.png";
        paragraph = "<html>" +
                "Non-terminal arguments are arguments that can be connected to another node.<br>" +
                "To connect a non-terminal argument to a node, click on the node and click onto the <br>" +
                "node to connect to." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents = new ArrayList<>();
        contents.add(content);

        image = "equipment_establish_connection_with_tip.png";
        paragraph = "<html>"+
                "Alternatively, you can click on an empty area to connect to a new node." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents.add(content);
        panel = new UserGuideContentPanel("Non-Terminal Arguments", contents);
        panels.add(panel);


        // Optional Arguments
        image = "game_node.png";
        paragraph = "<html>" +
                "Optional arguments are arguments that can be connected to a node, but are not required.<br>" +
                "Optional Non-Terminal Arguments are indicated by a blue connection component, whereas <br>" +
                "mandatory Non-Terminal Arguments are indicated by a red connection component." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents = new ArrayList<>();
        contents.add(content);

        image = "terminal_optional.png";
        paragraph = "<html>" +
                "Terminal Optional Arguments (such as strings or numbers) have a \"x\" or \"+\" symbol <br>to disable/enable the argument." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents.add(content);
        panel = new UserGuideContentPanel("Optional Arguments", contents);
        panels.add(panel);


        // Choice Arguments
        image = "choice.png";
        paragraph = "<html>" +
                "Choice Arguments indicate that the current argument may be replaced by another. Upon clicking <br>" +
                "on the choice icon, you can select among all possible alternatives." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        panel = new UserGuideContentPanel("Choice Arguments", content);
        panels.add(panel);


        // Collection Arguments
        image = "equipment_collection.png";
        paragraph = "<html>" +
                "Collection Arguments mean that you can supply a list of the given argument. To add an additional <br>" +
                "element to the list, click on the <b>\"+\"</b> icon. To remove an element, click on the <b>\"-\"</b> icon." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        panel = new UserGuideContentPanel("Collection Arguments", content);
        panels.add(panel);


        // Collapsing
        image = "game_eq_collapsed.png";
        paragraph = "<html>" +
                "Nodes can be collapsed into their parent by <i>CTRL+W</i> or via the right-click menu.<br>" +
                "To expand use <i>CTRL+E</i> or the expand icon." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        panel = new UserGuideContentPanel("Collapsing", content);
        panels.add(panel);

        // Select Subtree
        image = "double_click.png";
        paragraph = "<html>" +
                "You can select a subtree by double-clicking on the node. <br>" +
                "Actions, such as copying, can be performed on all selected nodes at once." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        panel = new UserGuideContentPanel("Select Subtree", content);
        panels.add(panel);


        // Compile & Play

        image = "toolbar.png";
        paragraph = "<html>" +
                "To compile and play the game, click on the <b>Play<\b> button on the toolbar <br>" +
                "situated at the top-right of the editor." +
                "</html>";

        contents = new ArrayList<>();
        content = new UserGuideContent(paragraph, image);
        contents.add(content);

        image = "auto_compile.png";
        paragraph = "<html>" +
                "When the auto-compile feature is turned on, the game is compiled at every change to the <br>" +
                "description. <b>Warning!: This can be computationally expensive!<\b> <br>" +
                "When the game is not compilable, the Play button appears red and offers disclosure." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents.add(content);

        panel = new UserGuideContentPanel("Compile & Play", contents);
        panels.add(panel);
    }

    private void nextPanel()
    {
        if(currentIndex < panels.size() - 1)
        {
            currentIndex++;
            remove(scrollPane);
            currentPanel = panels.get(currentIndex);
            scrollPane = new JScrollPane(currentPanel);
            add(scrollPane, BorderLayout.CENTER);
            revalidate();
            repaint();

        }
    }

    private void previousPanel()
    {
        if(currentIndex > 0)
        {
            currentIndex--;
            remove(scrollPane);
            currentPanel = panels.get(currentIndex);
            scrollPane = new JScrollPane(currentPanel);
            add(scrollPane, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }


}
