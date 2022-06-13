package app.display.dialogs.visual_editor.view.panels;


import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LConnectionComponent;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs.LIngoingConnectionComponent;
import app.display.dialogs.visual_editor.view.panels.editor.ConnectionHandler;
import main.grammar.Symbol;

import java.util.List;

public interface IGraphPanel {
    void drawGraph(DescriptionGraph graph);
    DescriptionGraph graph();
    ConnectionHandler ch();
    LudemeNodeComponent nodeComponent(LudemeNode node);
    int selectedRootId();
    List<iGNode> selectedNodes();
    LudemeNode addNode(Symbol symbol, int x, int y, boolean connect);
    void showAllAvailableLudemes(int x, int y);
    void clickedOnNode(LudemeNodeComponent lnc);
    void removeNode(LudemeNode node);
    LayoutHandler getLayoutHandler();
    void updateGraph();
    void deselectEverything();
    void repaint();
}
