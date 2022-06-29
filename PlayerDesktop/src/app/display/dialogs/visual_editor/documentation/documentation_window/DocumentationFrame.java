package app.display.dialogs.visual_editor.documentation.documentation_window;

import app.display.dialogs.visual_editor.documentation.DocumentationReader;
import grammar.Grammar;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;

public class DocumentationFrame extends JFrame
{
    private JTextField searchField = new JTextField();
    private HelpPanel currentHelpPanel;

    private Component searchNameSpace;
    private Component nameDescriptionSpace;
    private Component descriptionConstructorSpace;

    private List<Symbol> availableSymbols = new ArrayList<>();

    public static void main(String[] args)
    {
        DocumentationFrame frame = new DocumentationFrame();
        frame.setVisible(true);
    }

    public DocumentationFrame()
    {
        setTitle("Help");
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        availableSymbols.addAll(DocumentationReader.instance().documentation().keySet());

        add(searchField);
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
        setSize(searchField, 1.0, 0.05);

        searchNameSpace = Box.createRigidArea(new Dimension(0, 10));
        add(searchNameSpace);
        setSize(searchNameSpace, 0.0, 0.025);

        currentHelpPanel = new HelpPanel(this, Grammar.grammar().symbolsByName("Move").get(0));
        add(currentHelpPanel);

        //changeCurrentHelp(availableSymbols.get(55));

    }

    private void changeCurrentHelp(Symbol s)
    {
        remove(currentHelpPanel);
        currentHelpPanel = new HelpPanel(this, s);
        add(currentHelpPanel);
        repaint();
    }




    public void setSize(Component component, double percentageWidth, double percentageHeight)
    {
        int width = (int) (getWidth() * percentageWidth);
        int height = (int) (getHeight() * percentageHeight);
        setSize(component, width, height);
    }

    public void setSize(Component component, int width, int height)
    {
        component.setPreferredSize(new Dimension(width, height));
        component.setSize(component.getPreferredSize());
        component.setMaximumSize(component.getPreferredSize());
        component.setMinimumSize(component.getPreferredSize());
    }

}
