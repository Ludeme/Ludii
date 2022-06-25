package app.display.dialogs.visual_editor.view.panels;


import app.display.dialogs.visual_editor.LayoutManagement.LayoutHandler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.editor.ConnectionHandler;
import main.grammar.Clause;
import main.grammar.Symbol;

import java.util.List;

public interface IGraphPanel {


    void notifyNodeAdded(LudemeNode node, boolean connect);
    void notifyNodeRemoved(LudemeNodeComponent lnc);
    void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, int inputFieldIndex);
    void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, NodeArgument inputFieldArgument);
    void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, NodeArgument inputFieldArgument, int elementIndex);
    void notifyEdgeRemoved(LudemeNodeComponent from, LudemeNodeComponent to);
    void notifyCollapsed(LudemeNodeComponent lnc, boolean collapsed);

    void notifySelectedClauseChanged(LudemeNodeComponent lnc, Clause clause);


    boolean isBusy();
    void setBusy(boolean b);
    void drawGraph(DescriptionGraph graph);
    DescriptionGraph graph();
    ConnectionHandler connectionHandler();
    LudemeNodeComponent nodeComponent(LudemeNode node);
    int selectedRootId();
    void addNodeToSelections(LudemeNodeComponent lnc);
    List<iGNode> selectedNodes();
    List<LudemeNodeComponent> selectedLnc();
    void showAllAvailableLudemes(int x, int y);
    void clickedOnNode(LudemeNodeComponent lnc);
    LayoutHandler getLayoutHandler();
    void updateGraph();
    void updateNodePositions();
    void deselectEverything();
    void repaint();
    void addSelectionIndex(int index);
}
