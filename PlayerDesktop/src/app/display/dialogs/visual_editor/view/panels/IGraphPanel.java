package app.display.dialogs.visual_editor.view.panels;


import app.display.dialogs.visual_editor.LayoutManagement.LayoutHandler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.view.components.AddArgumentPanel;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.editor.ConnectionHandler;
import main.grammar.Clause;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public interface IGraphPanel {


    // When Handler changes the graph

    /** Initializes the graph panel. */
    void initialize(JScrollPane parentScrollPane);
    /** Whether this panel is of a define graph. */
    boolean isDefineGraph();
    /** Notifies the panel that a node was added to the graph. */
    void notifyNodeAdded(LudemeNode node, boolean connect);
    /** Notifies the panel that a node was removed from the graph. */
    void notifyNodeRemoved(LudemeNodeComponent lnc);
    /** Notifies the panel that an edge between two nodes was established, outgoing from a given input field index */
    void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, int inputFieldIndex);
    /** Notifies the panel that an edge between two nodes was established, outgoing from an input field with a given NodeArgument */
    void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, NodeArgument inputFieldArgument);
    /** Notifies the panel that a collection edge between two nodes was removed, outgoing from an input field with a given NodeArgument */
    void notifyEdgeAdded(LudemeNodeComponent from, LudemeNodeComponent to, NodeArgument inputFieldArgument, int elementIndex);
    /** Notifies the panel that an edge between two nodes was removed */
    void notifyEdgeRemoved(LudemeNodeComponent from, LudemeNodeComponent to);
    /** Notifies the panel that an edge between two nodes was removed , of a collection */
    void notifyEdgeRemoved(LudemeNodeComponent from, LudemeNodeComponent to, int elementIndex);
    /** Notifies the panel that a node was collapsed/expanded */
    void notifyCollapsed(LudemeNodeComponent lnc, boolean collapsed);
    /** Notifies the panel that a node's inputs were updated */
    void notifyInputsUpdated(LudemeNodeComponent lnc);
    /** Notifies the panel that a collection element was added to a node */
    void notifyCollectionAdded(LudemeNodeComponent lnc, NodeArgument inputFieldArgument, int elementIndex);
    /*+ Notifies the panel that a node's collection element was removed */
    void notifyCollectionRemoved(LudemeNodeComponent lnc, NodeArgument inputFieldArgument, int elementIndex);
    /** Notifies the panel that the node's selected clause was changed */
    void notifySelectedClauseChanged(LudemeNodeComponent lnc, Clause clause);
    /** Notifies the panel that a node's optional terminal input was activated/deactivated */
    void notifyTerminalActivated(LudemeNodeComponent lnc, NodeArgument inputFieldArgument, boolean activated);
    /** Notifies the panel about an updated collapsed-status of a list of nodes */
    void updateCollapsed(List<LudemeNodeComponent> lncs);
    /** Notifies the panel which nodes are not compilable */
    void notifyUncompilable(List<LudemeNodeComponent> lncs);
    /** Returns the ScrollPane this panel is in */
    JScrollPane parentScrollPane();
    JPanel panel();

    /** Returns the AddArgumentPanel used to establish connections */
    AddArgumentPanel addConnectionPanel();
    /** Returns the AddArgumentPanel used to create new nodes */
    AddArgumentPanel addNodePanel();
    /** Creates a LudemeNodeComponent for a given node */
    void addLudemeNodeComponent(LudemeNode node, boolean connect);

    /** Whether the graph is currently busy (e.g. a node is being added) */
    boolean isBusy();
    /** Updates the busy-status */
    void setBusy(boolean b);
    /** Returns the DescriptionGraph this Panel represents */
    DescriptionGraph graph();
    /** Returns the ConnectionHandler for this panel */
    ConnectionHandler connectionHandler();
    /** Finds the LudemeNodeComponent for a given LudemeNode */
    LudemeNodeComponent nodeComponent(LudemeNode node);
    /** */
    void enterSelectionMode();
    /** */
    boolean isSelectionMode();
    /** */
    Rectangle exitSelectionMode();
    /** Adds a node to the list of selected nodes */
    void addNodeToSelections(LudemeNodeComponent lnc);
    /** List of selected nodes */
    List<iGNode> selectedNodes();
    /** List of selected nodes */
    List<LudemeNodeComponent> selectedLnc();
    /** Selects all nodes */
    void selectAllNodes();
    /** Unselects all nodes */
    void deselectEverything();
    /** Displays all available ludemes that may be created */
    void showAllAvailableLudemes(int x, int y);
    /** Notifies the panel that the user clicked on a node */
    void clickedOnNode(LudemeNodeComponent lnc);
    /** The LayoutHandler */
    LayoutHandler getLayoutHandler();
    /** Updates the Graph (position of nodes, etc.) */
    void updateGraph();
    /** Updates the position of the nodes */
    void updateNodePositions();
    /** */
    void syncNodePositions();
    /** Repaints the graph */
    void repaint();
    /** */
    void addSelectionIndex(int index);
}
