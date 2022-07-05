package app.display.dialogs.visual_editor.documentation.documentation_window;

import app.display.dialogs.visual_editor.documentation.DocumentationReader;
import app.display.dialogs.visual_editor.documentation.HelpInformation;
import main.grammar.Clause;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;


public class HelpPanel extends JPanel
{

    private final HelpInformation hi;
    private final JLabel nameLabel;
    private final JLabel description;
    private JLabel constructorLabel;
    private final Component nameDescriptionSpace;
    private final Component descriptionConstructorSpace;
    private final Component constructorConstructorsSpace;

    final java.util.List<ExpandableConstructorPanel> ecps = new ArrayList<>();

    private final DocumentationFrame df;

    public HelpPanel(DocumentationFrame df, Symbol symbol)
    {
        this.df = df;
        DocumentationReader dr = DocumentationReader.instance();
        this.hi = dr.documentation().get(symbol);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setMinimumSize(new Dimension(df.getWidth(), getMinimumSize().height));

        nameLabel = new JLabel(symbol.name());
        add(nameLabel);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        nameDescriptionSpace = Box.createRigidArea(new Dimension(0, 10));
        df.setSize(nameDescriptionSpace, 0.0, 0.02);
        add(nameDescriptionSpace);

        description = new JLabel(hi.description());
        add(description);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        descriptionConstructorSpace = Box.createRigidArea(new Dimension(0, 10));
        df.setSize(descriptionConstructorSpace, 0.0, 0.03);
        add(descriptionConstructorSpace);

        add(new JLabel("Constructors"));

        constructorConstructorsSpace = Box.createRigidArea(new Dimension(0, 10));
        df.setSize(constructorConstructorsSpace, 0.0, 0.01);
        add(constructorConstructorsSpace);



        for(Clause c : symbol.rule().rhs())
        {
            if(c.args() == null) continue;
            ExpandableConstructorPanel ecp = new ExpandableConstructorPanel(this, c);
            ecp.setAlignmentX(Component.LEFT_ALIGNMENT);
            ecps.add(ecp);
        }

        for(ExpandableConstructorPanel ecp : ecps)
        {
            add(ecp);
        }

    }

    public HelpInformation helpInformation()
    {
        return hi;
    }

    public List<ExpandableConstructorPanel> ecps()
    {
        return ecps;
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        df.repaint();
    }

    public void updateEcp(ExpandableConstructorPanel ecp)
    {
        ecp.setMaximumSize(ecp.getPreferredSize());
        repaint();
    }

}

