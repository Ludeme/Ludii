package app.display.dialogs.visual_editor.handler;

import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.grammar.Constructor;

import java.util.ArrayList;

public class Handler {

    // TODO: History for Undo/Redo

    public static DescriptionGraph gameDescriptionGraph;

    public static ArrayList<DescriptionGraph> history = new ArrayList<>();


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
        for(int childrenId: node.getChildren()){
            LudemeNode childrenNode = graph.getNode(childrenId);
            childrenNode.setParent(null);
        }
        if(node.getParentNode() != null) node.getParentNode().removeChildren(node);
        // TODO: Remove edges
    }
    public static void updateInput(DescriptionGraph graph, LudemeNode node, int index, Object input){
        if(index < node.getProvidedInputs().length) {
            //graph = graph.clone();
            node.setProvidedInput(index, input);
        }
    }
    public static void addEdge(DescriptionGraph graph, LudemeNode from, LudemeNode to){
        graph.addEdge(from.getId(), to.getId());
        // here form is the parent node
        from.addChildren(to);
        to.setParent(from);
    }
    public static void updatePosition(DescriptionGraph graph, LudemeNode node, int x, int y){
        node.setPos(x, y);
    }
    public static void updateCurrentConstructor(DescriptionGraph graph, LudemeNode node, Constructor c){
        node.setCurrentConstructor(c);
    }
    public static String getLudString(DescriptionGraph graph){
        return graph.toLud();
    }
}