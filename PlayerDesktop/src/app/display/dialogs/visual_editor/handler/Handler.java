package app.display.dialogs.visual_editor.handler;

import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.MainPanel;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;
import app.display.dialogs.visual_editor.view.panels.editor.tabPanels.LayoutSettingsPanel;
import app.display.dialogs.visual_editor.view.panels.header.ToolsPanel;
import main.grammar.Clause;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.awt.*;

public class Handler {

    // TODO: History for Undo/Redo

    public static DescriptionGraph gameDescriptionGraph;

    // Single EditorPanel
    public static EditorPanel editorPanel;

    // Single ToolsPanel
    public static ToolsPanel toolsPanel;

    public static LayoutSettingsPanel lsPanel;

    public static ArrayList<DescriptionGraph> history = new ArrayList<>();

    public static MainPanel mainPanel;

    private static List<LudemeNodeComponent> copyList = new ArrayList<>();

    private static final boolean DEBUG = true;


    private static void addToHistory(DescriptionGraph graph){
        //history.set(history.size()-1, history.get(history.size()-1).clone());
        //history.add(graph);
    }

    private static void resetHistory(DescriptionGraph graph){
        //history.clear();
        //history.add(graph);
    }

    public static void addNode(DescriptionGraph graph, LudemeNode node){
        //graph = graph.clone();
        graph.addNode(node);
        addToHistory(graph);
    }
    public static void removeNode(DescriptionGraph graph, LudemeNode node){
        //graph = graph.clone();
        graph.removeNode(node);
        //addToHistory(graph);
        for(int childrenId: node.children()){
            LudemeNode childrenNode = graph.getNode(childrenId);
            childrenNode.setParent(null);
        }
        if(node.parentNode() != null) node.parentNode().removeChildren(node);
        // TODO: Remove edges
    }
    public static void updateInput(DescriptionGraph graph, LudemeNode node, int index, Object input){
        if(index < node.providedInputs().length) {
            //graph = graph.clone();
            if(DEBUG) System.out.println("[HANDLER] Updating input of " + node.symbol().name() + ", " + index + " to " + input);
            node.setProvidedInput(index, input);
            if(DEBUG) System.out.println("[HANDLER] Provided Inputs: " + Arrays.toString(node.providedInputs()));
        }
    }

    public static void addCollectionElement(DescriptionGraph graph, LudemeNode node, int inputIndex)
    {
        if(DEBUG) System.out.println("[HANDLER] Adding collection element of " + node.title() + ", " + inputIndex);
        LudemeNode[] oldCollection = (LudemeNode[]) node.providedInputs()[inputIndex];
        if(oldCollection == null)
        {
            updateInput(graph, node, inputIndex, new LudemeNode[2]);
            return;
        }
        LudemeNode[] newCollection = new LudemeNode[oldCollection.length + 1];
        System.arraycopy(oldCollection, 0, newCollection, 0, oldCollection.length);
        updateInput(graph, node, inputIndex, newCollection);
    }

    /**
     * if a collection element was removed, update the provided input array
     * @param graph
     * @param node
     * @param inputIndex Index of collection argument in clause
     * @param elementIndex Index of collection element
     */
    public static void removeCollectionElement(DescriptionGraph graph, LudemeNode node, int inputIndex, int elementIndex)
    {
        if(DEBUG) System.out.println("[HANDLER] Removed collection element of " + node.symbol().name() + ", " + inputIndex + " at " + elementIndex);

        Object[] oldCollection = (Object[]) node.providedInputs()[inputIndex];
        if(oldCollection == null) return;
        Object[] newCollection = new Object[oldCollection.length - 1];
        for(int i = 0; i < elementIndex; i++)
        {
            newCollection[i] = oldCollection[i];
        }
        for(int i = elementIndex + 1; i < oldCollection.length; i++)
        {
            newCollection[i - 1] = oldCollection[i];
        }
        updateInput(graph, node, inputIndex, newCollection);
    }

    public static void addEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to){
        graph.addEdge(from.id(), to.id());
        // here form is the parent node
        from.addChildren(to);
        to.setParent(from);
    }
    public static void updatePosition(DescriptionGraph graph, LudemeNode node, int x, int y){
        node.setPos(x, y);
    }


    public static void updateCurrentClause(DescriptionGraph graph, LudemeNode node, Clause c){
        if(c.args() == null) node.setSelectedClause(c.symbol().rule().rhs().get(0));
        else node.setSelectedClause(c);
    }

    public static void setCollapsed(DescriptionGraph graph, LudemeNodeComponent lnc, boolean collapsed)
    {
        lnc.setCollapsed(collapsed);
    }
    public static String getLudString(DescriptionGraph graph){
        return graph.toLud();
    }

    public static void centerViewport(int x, int y)
    {
        if (mainPanel != null)
        {
            Rectangle view = mainPanel.getPanel().getViewport().getViewRect();
            //mainPanel.setView(x-view.width/2, y-view.height/2);
        }
    }

    public static void setMainPanel(MainPanel mainPanel) {
        Handler.mainPanel = mainPanel;
    }

    public static void activateSelectionMode()
    {
        editorPanel.enterSelectionMode();
    }

    public static void deactivateSelectionMode()
    {
        editorPanel.exitSelectionMode();
        editorPanel.repaint();
    }

    public static void turnOffSelectionBtn()
    {
        toolsPanel.deactivateSelection();
        toolsPanel.repaint();
        toolsPanel.revalidate();
    }

    public static void selectNode(LudemeNodeComponent node)
    {
        editorPanel.addNodeToSelections(node);
    }

    public static void setAutoplacement(boolean var) {editorPanel.setAutoplacement(var);}

    public static List<LudemeNodeComponent> copyList()
    {
        return copyList;
    }

    public static void setCopyList(List<LudemeNodeComponent> copyList)
    {
        Handler.copyList = copyList;
    }



}