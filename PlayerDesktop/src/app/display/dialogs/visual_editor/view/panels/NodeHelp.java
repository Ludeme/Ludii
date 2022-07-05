package app.display.dialogs.visual_editor.view.panels;

import app.display.dialogs.visual_editor.model.LudemeNode;
import main.grammar.Clause;
import main.grammar.ClauseArg;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

/**
 * Displays the help information for a node.
 */

public class NodeHelp extends JDialog
{

    private final LudemeNode node;
    private final JLabel parameterDescriptions = new JLabel();

    public NodeHelp(LudemeNode node)
    {
        this.node = node;
        setTitle("Help: " + node.symbol().name());
        setSize(300, 300);
        setLocationRelativeTo(null);
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));


        // add symbol title
        JLabel symbolTitle = new JLabel(node.symbol().name());
        // add description
        JLabel description = new JLabel(node.description());
        // add dropdown of clauses
        JLabel clauses = new JLabel("Clauses:");
        JList<String> clauseList = new JList<>();
        String[] clauseStrings = new String[node.clauses().size()];
        HashMap<String, Clause> clauseMap = new HashMap<>();
        for (int i = 0; i < node.clauses().size(); i++)
        {
            clauseStrings[i] = node.clauses().get(i).toString();
            clauseMap.put(clauseStrings[i], node.clauses().get(i));
        }
        clauseList.setListData(clauseStrings);
        JComboBox<String> clauseDropdown = new JComboBox<>(clauseStrings);
        clauseDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
            {
                Clause selectedClause = clauseMap.get(clauseDropdown.getSelectedItem());
                updateParameterPanel(selectedClause);
            }
        });

        updateParameterPanel(node.clauses().get(0));

        add(symbolTitle);
        add(description);
        add(clauses);
        add(clauseDropdown);
        add(parameterDescriptions);

        setVisible(true);
    }

    private void updateParameterPanel(Clause c)
    {
        StringBuilder newDescription = new StringBuilder();
        if(c.args() == null)
        {
            parameterDescriptions.setText("");
            repaint();
            return;
        }
        for (ClauseArg arg : c.args())
            newDescription.append(arg.toString()).append(": ").append(node.helpInformation().parameter(arg)).append("\n");

        newDescription = new StringBuilder(newDescription.toString().replaceAll("<", "&lt;"));
        newDescription = new StringBuilder(newDescription.toString().replaceAll(">", "&gt;"));
        newDescription = new StringBuilder(newDescription.toString().replaceAll("\n", "<br>"));
        newDescription = new StringBuilder("<html>" + newDescription + "</html>");
        parameterDescriptions.setText(newDescription.toString());
        repaint();
    }

}
