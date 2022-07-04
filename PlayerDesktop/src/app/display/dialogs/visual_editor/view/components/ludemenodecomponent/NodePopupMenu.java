package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.NodeHelp;
import app.display.dialogs.visual_editor.view.panels.editor.gameEditor.GameGraphPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NodePopupMenu extends JPopupMenu {
    public NodePopupMenu(LudemeNodeComponent nodeComponent, IGraphPanel graphPanel) {
        JMenuItem delete = new JMenuItem("Delete");
        JMenuItem observe = new JMenuItem("Observe");
        JMenuItem collapse = new JMenuItem("Collapse");
        JMenuItem duplicate = new JMenuItem("Duplicate");
        JMenuItem copyBtn = new JMenuItem("Copy");

        int iconDiameter = (int)(copyBtn.getPreferredSize().getHeight()*0.75);

        ImageIcon copyI = new ImageIcon(Handler.currentPalette().COPY_ICON.getImage().getScaledInstance(iconDiameter, iconDiameter, Image.SCALE_SMOOTH));
        ImageIcon duplicateI = new ImageIcon(Handler.currentPalette().DUPLICATE_ICON.getImage().getScaledInstance(iconDiameter, iconDiameter, Image.SCALE_SMOOTH));
        ImageIcon deleteI = new ImageIcon(Handler.currentPalette().DELETE_ICON.getImage().getScaledInstance(iconDiameter, iconDiameter, Image.SCALE_SMOOTH));
        ImageIcon collapseI = new ImageIcon(Handler.currentPalette().COLLAPSE_ICON().getImage().getScaledInstance(iconDiameter, iconDiameter, Image.SCALE_SMOOTH));

        copyBtn.setIcon(copyI);
        duplicate.setIcon(duplicateI);
        collapse.setIcon(collapseI);
        delete.setIcon(deleteI);

        JMenuItem fix = new JMenuItem("Fix group");
        fix.addActionListener(e -> {
            graphPanel.graph().getNode(graphPanel.graph().selectedRoot()).setFixed(true);
            graphPanel.repaint();
        });

        JMenuItem unfix = new JMenuItem("Unfix group");
        unfix.addActionListener(e -> {
            graphPanel.graph().getNode(graphPanel.graph().selectedRoot()).setFixed(false);
            graphPanel.repaint();
        });


        if(graphPanel.graph().getRoot() != nodeComponent.node())
        {
            add(copyBtn);
            add(duplicate);
            add(collapse);

            if (graphPanel.graph().selectedRoot() != -1 &&
                    graphPanel.graph().getNode(graphPanel.graph().selectedRoot()).fixed())
            {
                add(unfix);
            }
            else if (graphPanel.graph().selectedRoot() != -1)
            {
                add(fix);
            }
            add(delete);
        }



        //        add(dynamic);



        copyBtn.addActionListener(e -> {
            List<LudemeNode> copy = new ArrayList<>();
            if(nodeComponent.selected() && graphPanel.selectedLnc().size() > 0)
            {
                for(LudemeNodeComponent lnc : graphPanel.selectedLnc()) copy.add(lnc.node());
            }
            else
            {
                copy.add(nodeComponent.node());
            }
            // remove root node from copy list
            //copy.remove(graphPanel.graph().getRoot());

            Handler.copy(graphPanel.graph(), copy);
        });

        duplicate.addActionListener(e -> {

            if(nodeComponent.selected() && graphPanel.selectedLnc().size() > 1)
            {
                Handler.duplicate(graphPanel.graph());
            }
            else
            {
                List<LudemeNode> nodes = new ArrayList<>();
                nodes.add(nodeComponent.node());
                Handler.duplicate(graphPanel.graph(), nodes);
            }
        });

        collapse.addActionListener(e -> {
            Handler.collapseNode(graphPanel.graph(), nodeComponent.node(), true);
        });

        delete.addActionListener(e -> {
            // TODO: maybe a handler for that?
            if(nodeComponent.selected() && graphPanel.selectedLnc().size() > 1)
            {
                List<LudemeNode> nodes = new ArrayList<>();
                for(LudemeNodeComponent lnc : graphPanel.selectedLnc())
                {
                    nodes.add(lnc.node());
                }
                Handler.removeNodes(graphPanel.graph(), nodes);
            }
            else {
                if(!graphPanel.graph().isDefine() &&  graphPanel.graph().getRoot() == nodeComponent.node()) return; // cant remove root node
                Handler.removeNode(graphPanel.graph(), nodeComponent.node());
            }
        });

        observe.addActionListener(e -> {
            LudemeNode node = nodeComponent.node();
            String message = "";
            message += "ID: " + node.id() + "\n";
            message += "Name: " + node.symbol().name() + "\n";
            message += node.toLud() + "\n";
            message += "Grammar Label: " + node.symbol().grammarLabel() + "\n";
            message += "Constructor: " + node.selectedClause() + "\n";
            message += "# Clauses: " + node.clauses().size() + "\n";
            message += "Creator: " + node.creatorArgument() + "\n";
            message += "Package: " + node.packageName() + "\n";
            message += "Provided Inputs: " + (node.providedInputsMap().values()) + "\n";
            message += "Fields: " + nodeComponent.inputArea().currentInputFields + "\n";
            message += "Unsatisfied : " + Handler.isComplete(graphPanel.graph()) + "\n";
            message += ".lud : " + graphPanel.graph().toLud() + "\n";
            message += ".lud 2 : " + node.toLud(true) + "\n";


            if(graphPanel.graph().isDefine()) message += "Parameters: " + Handler.defineParameters(graphPanel.graph()) + "\n";



            JOptionPane.showMessageDialog(graphPanel.panel(), message);
        });

        JMenuItem help = new JMenuItem("Help");
        help.addActionListener(e -> new NodeHelp(nodeComponent.node()));
        add(observe);
        add(help);

    }
}
