package app.display.dialogs.visual_editor.documentation.documentation_window;

import main.grammar.Clause;
import main.grammar.ClauseArg;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class ExpandableConstructorPanel extends JPanel
{

    private final HelpPanel hp;
    private final Clause clause;

    private final JPanel header;

    JPanel body;

    public ExpandableConstructorPanel(HelpPanel hp, Clause clause)
    {
        this.hp = hp;
        this.clause = clause;
        setLayout(new BorderLayout());

        setMinimumSize(new Dimension(hp.getWidth(), getMinimumSize().height));

        header = new JPanel()
        {
            {
                JButton expandButton = new JButton("v");
                JLabel nameLabel = new JLabel(clause().toString());
                setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
                add(expandButton);
                add(nameLabel);
                expandButton.addActionListener(e -> {
                    if(body.isVisible())
                    {
                        body.setVisible(false);
                        expandButton.setText("v");
                        setMaximumSize(getPreferredSize());
                        hp.updateEcp(ExpandableConstructorPanel.this);
                    }
                    else
                    {
                        body.setVisible(true);
                        expandButton.setText("^");
                        setMaximumSize(getPreferredSize());
                        hp.updateEcp(ExpandableConstructorPanel.this);
                    }
                });
            }
        };

        add(header, BorderLayout.NORTH);
        body = new JPanel()
        {
            {
                JLabel argumentsHtml = new JLabel(parameterDescriptionHTML());
                add(argumentsHtml);
                setVisible(false);
            }
        };
        add(body, BorderLayout.WEST);
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(getPreferredSize());

    }

    private Clause clause()
    {
        return clause;
    }

    private String parameterDescriptionHTML()
    {
        if(clause().args() == null)
            return "";
        HashMap<ClauseArg, String> descriptions = hp.helpInformation().parameters();
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        for(ClauseArg arg : clause().args())
        {
            String argString = arg.toString();
            argString = argString.replaceAll("<", "&lt;");
            argString = argString.replaceAll(">", "&gt;");
            sb.append(argString);
            sb.append(": ");
            sb.append(descriptions.get(arg));
            sb.append("<br>");
        }
        sb.append("</html>");
        return sb.toString();
    }

}
