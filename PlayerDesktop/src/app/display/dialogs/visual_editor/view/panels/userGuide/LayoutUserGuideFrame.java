package app.display.dialogs.visual_editor.view.panels.userGuide;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Pop-up frame with helper information about layout management system
 * Heavily based on UserGuideFrame
 * @author nic0gin
 */
public class LayoutUserGuideFrame extends JFrame
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 8439659036667946213L;
	List<UserGuideContentPanel> panels = new ArrayList<>();
    UserGuideContentPanel currentPanel;
    JScrollPane scrollPane;
    int currentIndex;

    /**
     * Constructor
     */
    public LayoutUserGuideFrame()
    {
        // set frame properties
        setTitle("Layout User Guide");
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
        String paragraph;
        UserGuideContent content;
        List<UserGuideContent> contents;
        UserGuideContentPanel panel;

        // Layout menu
        image = "layout_menu.png";
        paragraph = "<html> " +
                "<i>Arrange Graph</i>: Arranges layout of graph on the current panel. <br>" +
                " <br>" +
                "<i>Undo Placement</i>: Moves nodes on their previous position - before layout arranged by user. <br>" +
                " <br>" +
                "<i>Redo Placement</i>: Cancels actions of <i>Undo Placement</i>. <br>" +
                " <br>" +
                "<i>Animation</i>: Animate transition of nodes' positions when arranging the graph. <br>" +
                " <br>" +
                "<i>Auto Placement</i>: Nodes are placed automatically according to arrangement procedure <br>" +
                " with pre-specified layout configurations. <br>" +
                " <br>" +
                "<i>Preserve Configurations</i>: Update of layout configurations to match user-placement. <br>" +
                " <br>" +
                "<i>Open Layout Settings</i>: Opens sidebar with extended layout settings." +
                "</html>";

        content = new UserGuideContent(paragraph, image);
        panel = new UserGuideContentPanel("Layout menu", content);
        panels.add(panel);


        // Advanced layout settings
        image = "sliders.png";
        paragraph = "<html>" +
                "In the sidebar layout settings it possible to manually adjust metrics. <br>" +
                "By moving the sliders layout will be automatically updated. <br>" +
                " <br>" +
                "<i>Offset</i>: Relative position of subtree nodes regarding its root. <br>" +
                "0.0 - nodes are lower than root, 1.0 - nodes are higher than root. <br>" +
                " <br>" +
                "<i>Distance</i>: Distance between subtree nodes from its root. <br>" +
                "0.0 - minimum distance, 1.0 - maximum distance. <br>" +
                " <br>" +
                "<i>Spread</i>: inner spread of subtree nodes. <br>" +
                "0.0 - minimum spread, 1.0 - maximum spread. <br>" +
                " <br>" +
                "<i>Compactness</i>: with increase of value empty space is minimised. <br>" +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents = new ArrayList<>();
        contents.add(content);

        image = "layout_btns.png";
        paragraph = "<html>" +
                "The sidebar also provides extra functionalities for graph arrangement. <br>" +
                "The next page discusses each button in detail." +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents.add(content);

        panel = new UserGuideContentPanel("Sidebar: Advanced Layout Settings", contents);
        panels.add(panel);


        // Functionalities of sidebar buttons
        image = "layout_btns.png";
        paragraph = "<html>" +
                "<i>Arrange Graph</i>: Executes arrangement procedure; tries to preserve induced layout metrics. <br>" +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents = new ArrayList<>();
        contents.add(content);

        image = "align_vertically.png";
        paragraph = "<html>"+
                "<i>Align Vertically</i>: Aligns selected nodes to Y-coordinate of left-most selected node. <br>" +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents.add(content);

        image = "align_horizontally.png";
        paragraph = "<html>"+
                "<i>Align Horizontally</i>: Aligns selected nodes to X-coordinate of left-most selected node. <br>" +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents.add(content);

        image = "group_subtree.png";
        paragraph = "<html>"+
                "<i>Group subtree</i>: When subtree is selected (by double-click on a sub-root) it can be grouped into hyper-node, <br>" +
                "i.e. when arranging layout it is treated as a new single node. <br>" +
                "</html>";
        content = new UserGuideContent(paragraph, image);
        contents.add(content);

        panel = new UserGuideContentPanel("Sidebar buttons", contents);
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
