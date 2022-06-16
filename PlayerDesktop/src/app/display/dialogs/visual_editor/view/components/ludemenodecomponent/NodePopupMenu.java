package app.display.dialogs.visual_editor.view.components.ludemenodecomponent;


import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.recs.codecompletion.Ludeme;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
            if(nodeComponent.selected() && graphPanel.selectedLnc().size() > 1)
            {
                HashMap<LudemeNode, LudemeNode> copies = new HashMap<>(); // <original, copy>
                for(LudemeNodeComponent lnc : graphPanel.selectedLnc())
                {
                    // copy each node
                    if(!graphPanel.graph().isDefine() && graphPanel.graph().getRoot() == lnc.node()) continue; // cant duplicate root node
                    int x_shift = 0;
                    int y_shift = lnc.getHeight() + (int)(lnc.getHeight()*0.1);
                    LudemeNode copy = lnc.node().copy(x_shift, y_shift);
                    copies.put(lnc.node(), copy);
                    // add the copy to the graph
                    graphPanel.addNode(copy);
                }
                // now fill inputs
                for(LudemeNode original : copies.keySet())
                {
                    LudemeNode copy = copies.get(original);
                    Object[] inputs = original.providedInputs();

                    for(int i = 0; i < inputs.length; i++)
                    {
                        Object input = inputs[i];
                        if(input instanceof LudemeNode)
                        {
                            LudemeNode inputNode = (LudemeNode)input;
                            LudemeNode copyInputNode = copies.get(inputNode);
                            if(copyInputNode != null)
                            {
                                copy.setProvidedInput(i, copyInputNode);
                            }
                        }
                        else if(input instanceof LudemeNode[])
                        {
                            LudemeNode[] copyInputs = new LudemeNode[((LudemeNode[])input).length];
                            for(int j = 0; j < copyInputs.length; j++)
                            {
                                LudemeNode inputNode = ((LudemeNode[])input)[j];
                                LudemeNode copyInputNode = copies.get(inputNode);
                                if(copyInputNode != null)
                                {
                                    copyInputs[j] = copyInputNode;
                                }
                            }
                            copy.setProvidedInput(i, copyInputs);
                        }
                        else
                        {
                            copy.setProvidedInput(i, input);
                        }
                    }
                }
                // deselect all previously selected nodes
                graphPanel.deselectEverything();
                // update provided inputs of all copies TODO: inefficient
                for(LudemeNode original : copies.keySet())
                {
                    LudemeNode copy = copies.get(original);
                    LudemeNodeComponent copyLnc = graphPanel.nodeComponent(copy);
                    // select the copy
                    graphPanel.addNodeToSelections(copyLnc);
                    copyLnc.updateProvidedInputs();
                }
            }
            else
            {
                if(!graphPanel.graph().isDefine() && graphPanel.graph().getRoot() == nodeComponent.node()) return; // cant duplicate root node
                int x_shift = 0;
                int y_shift = nodeComponent.getHeight() + (int)(nodeComponent.getHeight() * 0.1);
                LudemeNode copy = nodeComponent.node().copy(x_shift, y_shift);
                graphPanel.addNode(copy);
                graphPanel.nodeComponent(copy).updateProvidedInputs();
                graphPanel.repaint();
            }

        });

        collapse.addActionListener(e -> {
            nodeComponent.node().setCollapsed(!nodeComponent.node().collapsed());
            graphPanel.nodeComponent(nodeComponent.node().parentNode()).updatePositions(); // update position to notify about collapsed child
            graphPanel.repaint();
        });

        delete.addActionListener(e -> {
            // TODO: maybe a handler for that?
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
