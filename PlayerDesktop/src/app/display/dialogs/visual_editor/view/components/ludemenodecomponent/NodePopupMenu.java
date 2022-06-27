package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.NodeHelp;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NodePopupMenu extends JPopupMenu {
    public NodePopupMenu(LudemeNodeComponent nodeComponent, IGraphPanel graphPanel) {
        JMenuItem delete = new JMenuItem("Delete");
        JMenuItem dynamic = new JMenuItem("(Un)Set Dynamic");
        JMenuItem observe = new JMenuItem("Observe");
        JMenuItem collapse = new JMenuItem("Collapse");
        JMenuItem duplicate = new JMenuItem("Duplicate");
        JMenuItem copyBtn = new JMenuItem("Copy");

        int iconHeight = (int)(copyBtn.getPreferredSize().getHeight()*0.75);

        ImageIcon copyI = new ImageIcon(DesignPalette.COPY_ICON.getImage().getScaledInstance(iconHeight, iconHeight, Image.SCALE_SMOOTH));
        ImageIcon duplicateI = new ImageIcon(DesignPalette.DUPLICATE_ICON.getImage().getScaledInstance(iconHeight, iconHeight, Image.SCALE_SMOOTH));
        ImageIcon deleteI = new ImageIcon(DesignPalette.DELETE_ICON.getImage().getScaledInstance(iconHeight, iconHeight, Image.SCALE_SMOOTH));
        ImageIcon collapseI = new ImageIcon(DesignPalette.COLLAPSE_ICON.getImage().getScaledInstance(iconHeight, iconHeight, Image.SCALE_SMOOTH));

        copyBtn.setIcon(copyI);
        duplicate.setIcon(duplicateI);
        collapse.setIcon(collapseI);
        delete.setIcon(deleteI);

        if(graphPanel.graph().getRoot() != nodeComponent.node()) {
            add(copyBtn);
            add(duplicate);
            add(collapse);
            add(observe);
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
            copy.remove(graphPanel.graph().getRoot());

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

        dynamic.addActionListener(e -> {
            nodeComponent.changeDynamic();
        });

        observe.addActionListener(e -> {
            LudemeNode node = nodeComponent.node();
            String message = "";
            message += "ID: " + node.id() + "\n";
            message += "Name: " + node.symbol().name() + "\n";
            message += "Constructor: " + node.selectedClause() + "\n";
            message += "Package: " + node.packageName() + "\n";
            message += "Dynamic: " + node.dynamic() + "\n";
            message += "Provided Inputs: " + (node.providedInputsMap().values()) + "\n";
            message += "Fields: " + nodeComponent.inputArea().currentInputFields + "\n";
            message += "Provided LIFs: " + node.providedNodeArguments() + "\n";
            message += "Active LIFs: " + node.activeNodeArguments() + "\n";
            message += "Active C: (" + node.activeClauses().size() + ") " +node.activeClauses() + "\n";
            //message += "Inactive C: (" + + nodeComponent.inputArea().inactiveClauses.size() + ") " + nodeComponent.inputArea().inactiveClauses + "\n";
            message += "Width: " + nodeComponent.width() + "\n";

            JOptionPane.showMessageDialog((EditorPanel) graphPanel, message);
        });

        JMenuItem help = new JMenuItem("Help");
        help.addActionListener(e -> new NodeHelp(nodeComponent.node()));
        add(help);
    }
}
