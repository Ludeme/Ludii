package app.display.dialogs.visual_editor.view.panels.editor;

import app.display.dialogs.visual_editor.LayoutManagement.LayoutHandler;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;

import javax.swing.*;
import java.util.HashMap;

public class EditorPopupMenu extends JPopupMenu {

    public EditorPopupMenu(IGraphPanel graphPanel, int x, int y) {
        JMenuItem newLudeme = new JMenuItem("New Ludeme");
        JMenuItem duplicateScreen = new JMenuItem("Duplicate Screen");
        JMenuItem repaintScreen = new JMenuItem("Repaint");
        JMenuItem paste = new JMenuItem("Paste");

        add(paste);

        paste.addActionListener(e -> {
            if(Handler.copyList() != null)
            {
                int x_shift = x - Handler.copyList().get(0).position().x;
                int y_shift = y - Handler.copyList().get(0).position().y;
                HashMap<LudemeNode, LudemeNode> copies = new HashMap<>(); // <original, copy>
                for(LudemeNodeComponent lnc : Handler.copyList())
                {
                    // copy each node
                    if(!graphPanel.graph().isDefine() && graphPanel.graph().getRoot() == lnc.node()) continue; // cant duplicate root node

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
        });

        JMenu lmMenu = new JMenu("Graph Layout");
        JMenuItem compact = new JMenuItem("Arrange graph");
        JMenuItem settings = new JMenuItem("Layout Settings");

        newLudeme.addActionListener(e -> {
            graphPanel.showAllAvailableLudemes(getX(), getY());
        });

        duplicateScreen.addActionListener(e -> {
            JFrame frame = new JFrame("Define Editor");
            EditorPanel editorPanel = new EditorPanel(5000,5000);
            frame.setContentPane(editorPanel);
            editorPanel.drawGraph(graphPanel.graph());
            frame.setVisible(true);
            frame.setPreferredSize(frame.getPreferredSize());
            frame.setSize(1200,800);
        });

        compact.addActionListener(e -> {
            LayoutHandler.applyOnPanel(graphPanel);
        });

        settings.addActionListener(e -> {
            LayoutSettingsPanel.getSettingsFrame(graphPanel);
        });

        lmMenu.add(compact);
        lmMenu.add(settings);

        repaintScreen.addActionListener(e -> {
            graphPanel.repaint();
                });

        add(newLudeme);
        add(lmMenu);
        add(duplicateScreen);
        add(repaintScreen);
    }

}
