package app.display.dialogs.visual_editor.model.UserActions;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import java.util.*;

public class RemovedNodesAction implements IUserAction
{
    private final IGraphPanel graphPanel;
    private final DescriptionGraph graph;
    private final List<LudemeNode> removedNodes;
    private final List<LudemeNode> removedNodesSorted;


    private final HashMap<LudemeNode, LinkedHashMap<NodeArgument, Object>> copiedInputs = new HashMap<>();
    private final HashMap<LudemeNode, LinkedHashMap<NodeArgument, Integer>> copiedNodeInputIds = new HashMap<>();
    private final HashMap<LudemeNode, LinkedHashMap<NodeArgument, Object[]>> copiedCollectionNodeIds = new HashMap<>();
    private final HashMap<Integer, LudemeNode> nodeId = new HashMap<>();

    public RemovedNodesAction(IGraphPanel graphPanel, List<LudemeNode> removedNodes)
    {
        this.graphPanel = graphPanel;
        this.graph = graphPanel.graph();
        this.removedNodes = new ArrayList<>(removedNodes);
        this.removedNodesSorted = new ArrayList<>(removedNodes);

        // sort removed nodes such that their depth descends
        removedNodesSorted.sort((n1, n2) -> n2.depth() - n1.depth());

        for(LudemeNode node : removedNodesSorted)
        {
            copiedInputs.put(node, copyInputs(node));
            copiedNodeInputIds.put(node, copyIds(node));
            copiedCollectionNodeIds.put(node, copyCollectionIds(node));
        }
    }

    private static LinkedHashMap<NodeArgument, Object> copyInputs(LudemeNode node)
    {
        LinkedHashMap<NodeArgument, Object> copiedInputs1 = new LinkedHashMap<>(node.providedInputsMap());
        for(NodeArgument arg : node.providedInputsMap().keySet())
        {
            if(copiedInputs1.get(arg) instanceof Object[])
            {
                Object[] copy = Arrays.copyOf((Object[])copiedInputs1.get(arg), ((Object[])copiedInputs1.get(arg)).length);
                copiedInputs1.put(arg, copy);
            }
        }
        return copiedInputs1;
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


    private void addAllNodes(List<LudemeNode> nodes)
    {
        for(LudemeNode node : nodes)
            Handler.addNode(graph, node);
    }

    private void assignParent(LudemeNode node)
    {
        if(node.parentNode() != null) return;
        for(LudemeNode n : removedNodesSorted)
        {
            LinkedHashMap<NodeArgument, Integer> ids = copiedNodeInputIds.get(n);
            if(ids.containsValue(node.id()))
            {
                node.setParent(n);
                Handler.addEdge(graph, n, node, node.creatorArgument());
                return;
            }
        }
        for(LudemeNode ln : copiedCollectionNodeIds.keySet())
        {
            LinkedHashMap<NodeArgument, Object[]> ids = copiedCollectionNodeIds.get(ln);
            for(NodeArgument arg : ids.keySet())
            {
                if(Arrays.asList(ids.get(arg)).contains(node.id()))
                {
                    node.setParent(ln);
                    return;
                }
            }
        }
    }

    private void assignInputs(LudemeNode node)
    {
        LinkedHashMap<NodeArgument, Object> inputs = copiedInputs.get(node);
        for(NodeArgument arg : inputs.keySet())
        {
            Object input = inputs.get(arg);
            if(input == null || input instanceof LudemeNode)
            {
                continue;
            }
            else if(input instanceof Object[])
            {
                //Handler.updateInput(graph, removedNode, arg, input);
                for(int i = 1; i < ((Object[])input).length; i++)
                {
                    Handler.addCollectionElement(graph, node, arg);
                }
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
                    if(isLudemeCollection)
                    {
                        if(cid[i] == null)
                        {
                            continue;
                        }
                        Handler.updateCollectionInput(graph, node, arg, nodeId.get((Integer)cid[i]), i);
                        Handler.addEdge(graph, node, nodeId.get((Integer) cid[i]), arg, i);
                    }
                    else
                    {
                        Handler.updateCollectionInput(graph, node, arg, ((Object[]) input)[i], i);
                    }
                }
            }
            else Handler.updateInput(graph, node, arg, input);
            node.setProvidedInput(arg, inputs.get(arg));
        }
    }

    @Override
	public void undo()
    {
        addAllNodes(removedNodesSorted);
        for(LudemeNode n : removedNodesSorted)
            assignParent(n);
        for(LudemeNode n : removedNodesSorted)
            assignInputs(n);

        // add last edges to non-removed components
        //if(removedNodes.get(0).parentNode() != null) Handler.addEdge(graph, removedNodes.get(0).parentNode(),removedNodes.get(0), removedNodes.get(0).creatorArgument());
        for(LudemeNode n : removedNodesSorted)
            if(n.parentNode() != null && !removedNodesSorted.contains(n.parentNode()))
                Handler.addEdge(graph, n.parentNode(),n, n.creatorArgument());

        List<LudemeNodeComponent> lncs = new ArrayList<>();
        for(LudemeNode n : removedNodesSorted)
            lncs.add(graphPanel.nodeComponent(n));
        graphPanel.updateCollapsed(lncs);
    }
    /**
     * Redoes the action
     */
    @Override
    public void redo() {
        Handler.removeNodes(graph, removedNodes);
    }
}
