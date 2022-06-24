package app.display.dialogs.visual_editor.view.panels;


import app.display.dialogs.visual_editor.LayoutManagement.LayoutHandler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.editor.ConnectionHandler;
import main.grammar.Symbol;

import java.util.List;

public interface IGraphPanel {
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
    LudemeNode addNode(Symbol symbol, int x, int y, boolean connect);
    void addNode(LudemeNode node);
    void showAllAvailableLudemes(int x, int y);
    void clickedOnNode(LudemeNodeComponent lnc);
    void removeNode(LudemeNode node);
    LayoutHandler getLayoutHandler();
    void updateGraph();
    void deselectEverything();
    void repaint();
    void addSelectionIndex(int index);
}
