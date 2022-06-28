package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import game.rules.meta.no.No;

import java.util.*;

public class RemovedNodesAction implements IUserAction
{
    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final List<LudemeNode> removedNodes;
    private List<RemovedNodeAction> removedNodeActions = new ArrayList<>();

    private HashMap<LudemeNode, LinkedHashMap<NodeArgument, Object>> copiedInputs = new HashMap<>();
    private HashMap<LudemeNode, LinkedHashMap<NodeArgument, Integer>> copiedNodeInputIds = new HashMap<>();
    private HashMap<LudemeNode, LinkedHashMap<NodeArgument, Object[]>> copiedCollectionNodeIds = new HashMap<>();
    private HashMap<Integer, LudemeNode> nodeId = new HashMap<>();

    private boolean isUndone = false;

    public RemovedNodesAction(IGraphPanel graphPanel, List<LudemeNode> removedNodes)
    {
        this.graphPanel = graphPanel;
        this.graph = graphPanel.graph();
        this.removedNodes = new ArrayList<>(removedNodes);

        // sort removed nodes such that their depth descends
        Collections.sort(removedNodes, (n1, n2) -> n2.depth() - n1.depth());

        for(LudemeNode node : removedNodes)
        {
            copiedInputs.put(node, copyInputs(node));
            copiedNodeInputIds.put(node, copyIds(node));
            copiedCollectionNodeIds.put(node, copyCollectionIds(node));
        }
    }

    private LinkedHashMap<NodeArgument, Object> copyInputs(LudemeNode node)
    {
        LinkedHashMap<NodeArgument, Object> copiedInputs = new LinkedHashMap<>(node.providedInputsMap());
        for(NodeArgument arg : node.providedInputsMap().keySet())
        {
            if(copiedInputs.get(arg) instanceof Object[])
            {
                Object[] copy = Arrays.copyOf((Object[])copiedInputs.get(arg), ((Object[])copiedInputs.get(arg)).length);
                copiedInputs.put(arg, null);
                copiedInputs.put(arg, copy);
            }
        }
        return copiedInputs;
    }

    private LinkedHashMap<NodeArgument, Integer> copyIds(LudemeNode node)
    {
        LinkedHashMap<NodeArgument, Integer> copiedIds = new LinkedHashMap<>();
        LinkedHashMap<NodeArgument, Object> inputs = copiedInputs.get(node);
        for(NodeArgument arg : inputs.keySet())
        {
            if(inputs.get(arg) instanceof LudemeNode)
            {
                LudemeNode inputNode = (LudemeNode)inputs.get(arg);
                copiedIds.put(arg, inputNode.id());
                nodeId.put(inputNode.id(), inputNode);
            }
        }

        return copiedIds;
    }

    private LinkedHashMap<NodeArgument, Object[]> copyCollectionIds(LudemeNode node)
    {
        LinkedHashMap<NodeArgument, Object[]> copiedIds = new LinkedHashMap<>();
        LinkedHashMap<NodeArgument, Object> inputs = copiedInputs.get(node);
        for(NodeArgument arg : inputs.keySet())
        {
            if(inputs.get(arg) instanceof Object[])
            {
                Object[] copy = Arrays.copyOf((Object[])inputs.get(arg), ((Object[])inputs.get(arg)).length);
                for(int i = 0; i < copy.length; i++)
                {
                    if(copy[i] instanceof LudemeNode)
                    {
                        LudemeNode inputNode = (LudemeNode)copy[i];
                        copy[i] = inputNode.id();
                        nodeId.put(inputNode.id(), inputNode);
                    }
                }
                copiedIds.put(arg, copy);
            }
        }
        return copiedIds;
    }

    /**
     * @return The type of the action
     */
    @Override
    public ActionType actionType() {
        return ActionType.REMOVED_NODE;
    }

    /**
     * @return The graph panel that was affected by the action
     */
    @Override
    public IGraphPanel graphPanel() {
        return graphPanel;
    }

    /**
     * @return The description graph that was affected by the action
     */
    @Override
    public DescriptionGraph graph() {
        return graph;
    }

    /**
     * @return Whether the action was undone
     */
    @Override
    public boolean isUndone() {
        return isUndone;
    }


    private void addAllNodes(List<LudemeNode> nodes)
    {
        for(LudemeNode node : nodes)
        {
            Handler.addNode(graph, node);
        }
    }

    private void assignParent(LudemeNode node)
    {
        if(node.parentNode() != null) return;
        for(LudemeNode n : removedNodes)
        {
            LinkedHashMap<NodeArgument, Integer> ids = copiedNodeInputIds.get(n);
            if(ids.containsValue(node.id()))
            {
                node.setParent(n);
                Handler.addEdge(graph, n, node, node.creatorArgument());
                return;
            }
        }
    }

    private void assignInputs(LudemeNode node)
    {
        LinkedHashMap<NodeArgument, Object> inputs = copiedInputs.get(node);
        for(NodeArgument arg : inputs.keySet()) {
            Object input = inputs.get(arg);
            if(input == null) continue;
            if(input instanceof LudemeNode) continue;
            else if(input instanceof Object[])
            {
                //Handler.updateInput(graph, removedNode, arg, input);
                for(int i = 1; i < ((Object[])input).length; i++) graphPanel.notifyCollectionAdded(graphPanel.nodeComponent(node), arg, i);
                boolean isLudemeCollection = false;
                for(Object o : (Object[])input)
                {
                    if(o instanceof LudemeNode)
                    {
                        isLudemeCollection = true;
                        break;
                    }
                }
                LinkedHashMap<NodeArgument, Object[]> collectionIds = copiedCollectionNodeIds.get(node);
                Object[] cid = collectionIds.get(arg);
                for(int i = 0; i < cid.length; i++)
                {
                    if(isLudemeCollection) {
                        if(cid[i] == null) continue;
                        Handler.updateCollectionInput(graph, node, arg, nodeId.get((Integer)cid[i]), i);
                        Handler.addEdge(graph, node, nodeId.get((Integer) cid[i]), arg, i);
                    }
                    else Handler.updateCollectionInput(graph, node, arg, ((Object[]) input)[i], i);
                }
            }
            else Handler.updateInput(graph, node, arg, input);
            node.setProvidedInput(arg, inputs.get(arg));
        }
    }

    public void undo() {
        addAllNodes(removedNodes);
        for(LudemeNode n : removedNodes) assignParent(n);
        for(LudemeNode n : removedNodes) assignInputs(n);

        // add last edges to non-removed components
        if(removedNodes.get(0).parentNode() != null) Handler.addEdge(graph, removedNodes.get(0).parentNode(),removedNodes.get(0), removedNodes.get(0).creatorArgument());
        /*for(LudemeNode n : removedNodes)
            if(n.parentNode() != null && !removedNodes.contains(n.parentNode()))
                Handler.addEdge(graph, n.parentNode(),n, n.creatorArgument());*/

        /*List<LudemeNodeComponent> lncs = new ArrayList<>();
        for(LudemeNode n : removedNodes)
        {
            lncs.add(graphPanel.nodeComponent(n));
        }*/
       // graphPanel.updateCollapsed(lncs);

        isUndone = false;
    }
    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        Handler.removeNodes(graph, removedNodes);
    }
}
