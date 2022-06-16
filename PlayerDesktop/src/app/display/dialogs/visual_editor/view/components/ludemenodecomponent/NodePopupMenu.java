package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;


import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;

import javax.swing.*;
import java.util.Arrays;

public class NodePopupMenu extends JPopupMenu {
    public NodePopupMenu(LudemeNodeComponent nodeComponent, IGraphPanel graphPanel) {
        JMenuItem delete = new JMenuItem("Delete");
        JMenuItem dynamic = new JMenuItem("(Un)Set Dynamic");
        JMenuItem observe = new JMenuItem("Observe");
        JMenuItem collapse = new JMenuItem("Collapse");
        JMenuItem duplicate = new JMenuItem("Duplicate");

        add(delete);
        add(dynamic);
        add(observe);
        add(collapse);
        add(duplicate);

        duplicate.addActionListener(e -> {
            int x_shift = 0;
            int y_shift = nodeComponent.getHeight() + (int)(nodeComponent.getHeight() * 0.1);
            LudemeNode copy = nodeComponent.node().copy();
            graphPanel.addNode(copy);
            graphPanel.repaint();
        });

        collapse.addActionListener(e -> {
            nodeComponent.node().setCollapsed(!nodeComponent.node().collapsed());
            graphPanel.nodeComponent(nodeComponent.node().parentNode()).updatePositions(); // update position to notify about collapsed child
            graphPanel.repaint();
        });

        delete.addActionListener(e -> {
            if(nodeComponent.selected() && graphPanel.selectedLnc().size() > 1)
            {
                for(LudemeNodeComponent lnc : graphPanel.selectedLnc())
                {
                    if(!graphPanel.graph().isDefine() &&  graphPanel.graph().getRoot() == lnc.node()) continue; // cant remove root node
                    graphPanel.removeNode(lnc.node());
                }
            }
            else {
                if(!graphPanel.graph().isDefine() &&  graphPanel.graph().getRoot() == nodeComponent.node()) return; // cant remove root node
                graphPanel.removeNode(nodeComponent.node());
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
            message += "Dynamic: " + node.dynamic() + "\n";
            message += "Provided Inputs: " + Arrays.toString(node.providedInputs()) + "\n";
            message += "Provided LIFs: " + nodeComponent.inputArea().providedInputFields + "\n";
            message += "Active C: (" + nodeComponent.inputArea().activeClauses.size() + ") " +nodeComponent.inputArea().activeClauses + "\n";
            message += "Inactive C: (" + + nodeComponent.inputArea().inactiveClauses.size() + ") " + nodeComponent.inputArea().inactiveClauses + "\n";
            message += "Width: " + nodeComponent.width() + "\n";

            JOptionPane.showMessageDialog((EditorPanel) graphPanel, message);
        });

    }
}
