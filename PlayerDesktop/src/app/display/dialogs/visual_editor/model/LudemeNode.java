package app.display.dialogs.visual_editor.model;


import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iLudemeNode;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import java.util.*;

/**
 * Node representation of a ludeme in the current description
 * @author Filipp Dokienko
 */

public class LudemeNode implements iLudemeNode, iGNode
{

    /** ID of last node */
    private static int LAST_ID = 0;
    /** ID of this node */
    private final int ID;
    /** Symbol/Ludeme this node represents */
    private final Symbol SYMBOL;
    /** List of clauses this symbol encompasses */
    private final List<Clause> CLAUSES;
    /** Currently selected Clause by the user */
    private Clause selectedClause;
    /** Inputs to the Clause Arguments of the currently selected Clause provided by the user */
    private Object[] providedInputs;
    /** HashMap of Nodes this node is connected to (as a parent) and their order */
    private final HashMap<LudemeNode, Integer> childrenOrder = new HashMap<>();
    /** Depth in the graph/tree */
    private int depth = 0;
    /** Width and height of the node in its graphical representation */
    private int width,height;
    /** This node's parent */
    private LudemeNode parent;
    /** List of children of this node */
    private final List<LudemeNode> children = new ArrayList<>();
    /** x and y coordinates of this node in the graph */
    private int x,y;
    /** whether this node is dynamic or not.
     * a dynamic node has no pre-selected clause. by providing any arguments to the node, the list of
     * possible clauses is narrowed down to the ones that match the provided arguments.
     */
    private boolean dynamic = false;
    /**
     * whether this node (and thus its children) are visible (collapsed) or not.
     */
    private boolean collapsed = false;
    /**
     * whether this node is visible
     */
    private boolean visible = true;

    /**
     * Constructor for a new LudemeNode
     * @param symbol Symbol/Ludeme this node represents
     * @param x x coordinate of this node in the graph
     * @param y y coordinate of this node in the graph
     */
    public LudemeNode(Symbol symbol, int x, int y)
    {

        System.out.println("Creating new LudemeNode: " + symbol.grammarLabel());

        this.ID = LAST_ID++;
        this.SYMBOL = symbol;
        this.CLAUSES = symbol.rule().rhs();
        this.x = x;
        this.y = y;
        this.width = 100; // width and height are hard-coded for now, updated later
        this.height = 100;
        if(CLAUSES != null)
        {
            if(CLAUSES.size() > 0)
            {
                this.selectedClause = CLAUSES.get(0);
                this.providedInputs = new Object[selectedClause.args().size()];
            }
            else
            {
                this.selectedClause = null;
                this.providedInputs = new Object[0];
            }
        } else {
            this.selectedClause = null;
            this.providedInputs = null;
        }
        if(dynamic && !dynamicPossible()) dynamic = false;
    }

    /**
     *
     * @return the symbol this node represents
     */
    @Override
    public Symbol symbol()
    {
        return SYMBOL;
    }

    /**
     *
     * @return the list of clauses this symbol encompasses
     */
    public List<Clause> clauses()
    {
        return CLAUSES;
    }

    /**
     * Changes the selected clause of this node
     * @param selectedClause the selected clause to set
     */
    public void setSelectedClause(Clause selectedClause)
    {
        this.selectedClause = selectedClause;
        this.providedInputs = new Object[selectedClause.args().size()];
    }

    /**
     *
     * @return the currently selected clause
     */
    @Override
    public Clause selectedClause()
    {
        return selectedClause;
    }

    /**
     *
     * @return the array of provided inputs
     */
    @Override
    public Object[] providedInputs()
    {
        return providedInputs;
    }

    /**
     * Sets the provided inputs to the given array
     * @param index the index of the supplied argument to set
     * @param input the input to set
     */
    @Override
    public void setProvidedInput(int index, Object input)
    {
        providedInputs[index] = input;
    }

    /**
     * Sets this node to be dynamic or not
     * @param dynamic the dynamic to set
     */
    public void setDynamic(boolean dynamic)
    {
        this.dynamic = dynamic;
    }

    /**
     *
     * @return whether it is possible to make this node dynamic
     */
    public boolean dynamicPossible()
    {
        if(CLAUSES.size() == 1) return false;
        for(Clause clause : CLAUSES){
            if(clause.args() == null) continue;
            if(clause.args().size() > 1) return true;
        }
        return false;
    }

    /**
     *
     * @return whether this node is dynamic
     */
    public boolean dynamic()
    {
        return dynamic;
    }

    /**
     * Sets this node to be visible or not
     * @param visible the visible to set
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     *
     * @return whether this node is visible in the GUI
     */
    public boolean visible(){
        return visible;
    }

    /**
     * Sets this node to be collapsed or not
     * @param collapsed the collapsed to set
     */
    public void setCollapsed(boolean collapsed)
    {
        if(parentNode() == null) return;
        this.collapsed = collapsed;
        this.visible = !collapsed;
        // the complete subtree of this node becomes invisible if collapsed or visible if not collapsed
        for(LudemeNode child : children)
        {
            child.setSubtreeVisible(!collapsed);
        }
    }

    /**
     * Sets the visibility of this node's subtree
     * @param visible the visibility to set
     */
    private void setSubtreeVisible(boolean visible)
    {
        if(visible && collapsed) return;
        setVisible(visible);
        for(LudemeNode child : children)
        {
            child.setSubtreeVisible(visible);
        }
    }

    /**
     *
     * @return whether this node is collapsed
     */
    public boolean collapsed(){
        return collapsed;
    }

    /**
     *
     * @return the id of this node
     */
    @Override
    public int id()
    {
        return ID;
    }

    /**
     *
     * @return the id of this node's parent
     */
    @Override
    public int parent()
    {
        return parent.id();
    }

    /**
     *
     * @return the parent node
     */
    public LudemeNode parentNode()
    {
        return parent;
    }

    /**
     *
     * @return the list of children of this node
     */
    @Override
    public List<Integer> children()
    {
        List<Integer> children_ids = new ArrayList<>();
        for(LudemeNode c : children) children_ids.add(c.id());
        return children_ids;
    }

    /**
     *
     * @return the list of siblings of this node
     */
    @Override
    public List<Integer> siblings()
    {
        // TODO implement
        return null;
    }

    /**
     *
     * @return the position of this node in the graph
     */
    @Override
    public Vector2D pos()
    {
        return new Vector2D(x, y);
    }

    /**
     * Set the position of this node in the graph
     * @param pos the position to set
     */
    @Override
    public void setPos(Vector2D pos)
    {
        x = (int) pos.getX();
        y = (int) pos.getY();
    }

    /**
     * Set the width of this node in the graph
     * @param width the width to set
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     *
     * @return the width of this node in the graph
     */
    @Override
    public int width()
    {
        return width;
    }

    /**
     * Set the height of this node in the graph
     * @param height the height to set
     */
    public void setHeight(int height)
    {
        this.height = height;
    }

    /**
     *
     * @return the height of this node in the graph
     */
    @Override
    public int height()
    {
        return height;
    }

    /**
     * Set the depth of this node
     * @param depth the depth to set
     */
    @Override
    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    /**
     *
     * @return the depth of this node
     */
    @Override
    public int depth()
    {
        return depth;
    }

    /**
     *
     * @return The depth of this node computed manually
     */
    public int depthManual()
    {
        int depth = 0;
        LudemeNode current = this;
        while(current.parentNode() != null){
            depth++;
            current = current.parentNode();
        }
        return depth;
    }

    /**
     * Set the position of this node
     * @param x the x coordinate to set
     * @param y the y coordinate to set
     */
    public void setPos(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the parent of this node
     * @param ludemeNode the parent to set
     */
    @Override
    public void setParent(iLudemeNode ludemeNode)
    {
        this.parent = (LudemeNode) ludemeNode;
    }

    /**
     * Adds a child to this node
     * @param children the child to add
     */
    public void addChildren(LudemeNode children)
    {
        // Checks if child nodes was already added
        if (!this.children.contains(children))
        {
            this.children.add(children);
            // get order of new child in current constructor
            // TODO: something goes wrong for [optional] inputs
            int order = -1;

            for (ClauseArg arg : selectedClause.args())
            {
                if (arg.symbol().name().equals(children.symbol().name()))
                {
                    order = selectedClause.args().indexOf(arg);
                    break;
                }
            }

            childrenOrder.put(children, order);
            // placing child in correct order
            for (int i = this.children.size()-1; i > 0; i--) {
                if (childrenOrder.get(this.children.get(i-1)) > childrenOrder.get(this.children.get(i)))
                {
                    // swap i-1 and i
                    Collections.swap(this.children, i-1, i);
                }
            }
        }
    }

    /**
     * Removes a child from this node
     * @param children the child to remove
     */
    public void removeChildren(LudemeNode children)
    {
        this.children.remove(children);
    }

    /**
     *
     * @return List of children of this node
     */
    public List<LudemeNode> childrenNodes()
    {
        return children;
    }


    /**
     *
     * @return The .lud equivalent representation of this node
     */
    @Override
    public String stringRepresentation()
    {
        StringBuilder sb = new StringBuilder();
        // append token of this node's symbol
        sb.append("(").append(tokenTitle());
        // append all inputs
        for(Object input : providedInputs)
        {
            if(input == null) continue; // if no input provided, skip it
            sb.append(" ");

            if(input instanceof LudemeNode)
            {
                sb.append(((LudemeNode) input).stringRepresentation());
            }
            else if(input instanceof LudemeNode[])
            {
                sb.append("{ ");
                for(LudemeNode node : (LudemeNode[]) input) {
                    if(node == null) continue;
                    sb.append(node.stringRepresentation()).append(" ");
                }
                sb.append("}");
            }
            else if(input instanceof String)
            {
                sb.append("\"").append(input).append("\"");
            }
            else
            {
                sb.append(input);
            }
        }
        sb.append(")");
        return sb.toString();
    }


    public String stringRepresentationUntilInputIndex(int index, String marker)
    {
        StringBuilder sb = new StringBuilder();
        // append token of this node's symbol
        sb.append("(").append(tokenTitle());
        // append all inputs
        for(int i = 0; i < index; i++)
        {
        Object input = providedInputs[i];
            if(input == null) continue; // if no input provided, skip it
            sb.append(" ");

            if(input instanceof LudemeNode)
            {
                sb.append(((LudemeNode) input).stringRepresentation());
            }
            else if(input instanceof LudemeNode[])
            {
                sb.append("{ ");
                for(LudemeNode node : (LudemeNode[]) input) {
                    if(node == null) continue;
                    sb.append(node.stringRepresentation()).append(" ");
                }
                sb.append("}");
            }
            else if(input instanceof String)
            {
                sb.append("\"").append(input).append("\"");
            }
            else
            {
                sb.append(input);
            }
        }
        sb.append(" " + marker);
        sb.append(")");
        return sb.toString();
    }

    public String codeCompletionGameDescription(LudemeNode stopAt, int untilIndex, String marker)
    {
        if(stopAt == this) return stringRepresentationUntilInputIndex(untilIndex, marker);
        StringBuilder sb = new StringBuilder();
        // append token of this node's symbol
        sb.append("(").append(tokenTitle());
        // append all inputs
        for(int i = 0; i < providedInputs().length; i++)
        {
            Object input = providedInputs()[i];
            if(input == null) continue; // if no input provided, skip it
            sb.append(" ");

            if(input instanceof LudemeNode)
            {
                if(input == stopAt)
                {
                    sb.append(((LudemeNode) input).stringRepresentationUntilInputIndex(untilIndex, marker));
                }
                else {
                    sb.append(((LudemeNode) input).codeCompletionGameDescription(stopAt, untilIndex, marker));
                }
            }
            else if(input instanceof LudemeNode[])
            {
                sb.append("{ ");
                for(LudemeNode node : (LudemeNode[]) input) {
                    if(node == null) continue;
                    if(node == stopAt)
                    {
                        sb.append((node).stringRepresentationUntilInputIndex(untilIndex, marker)).append(" ");
                    }
                    else {
                        sb.append((node).codeCompletionGameDescription(stopAt, untilIndex, marker)).append(" ");
                    }
                }
                sb.append("}");
            }
            else if(input instanceof String)
            {
                sb.append("\"").append(input).append("\"");
            }
            else
            {
                sb.append(input);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * The title consists of the symbol and any Constants followed by the constructor
     * @return The title of this node
     */
    public String title()
    {
        StringBuilder title = new StringBuilder(symbol().name());
        if(selectedClause().args() == null) return title.toString();
        // if selected clause starts with constants, add these to the title
        int index = 0;
        while(selectedClause().args().get(index).symbol().ludemeType().equals(Symbol.LudemeType.Constant)){
            title.append(" ").append(selectedClause().args().get(index).symbol().name());
            index++;
        }
        return title.toString();
    }


    private String tokenTitle()
    {
        StringBuilder title = new StringBuilder(symbol().token());
        if(selectedClause().args() == null) return title.toString();
        // if selected clause starts with constants, add these to the title
        int index = 0;
        while(selectedClause().args().get(index).symbol().ludemeType().equals(Symbol.LudemeType.Constant)){
            title.append(" ").append(selectedClause().args().get(index).symbol().name());
            index++;
        }
        return title.toString();
    }

    /**
     * Copies a node
     * @return a copy of the node
     */
    public LudemeNode copy()
    {
        LudemeNode copy = new LudemeNode(symbol(), x, y);
        copy.setVisible(visible());
        copy.setCollapsed(collapsed());
        copy.setDynamic(dynamic());
        copy.setSelectedClause(selectedClause());
        copy.setHeight(height());

        /*if(includeInputs)
        {
            for(int index = 0; index < providedInputs().length; index++)
            {
                Object input = providedInputs()[index];
                System.out.println("adding input " + input);
                if(input instanceof LudemeNode)
                {
                    LudemeNode inputNode = ((LudemeNode) input).copy(includeInputs);
                    copy.setProvidedInput(index, inputNode);
                }
                else if(input instanceof LudemeNode[])
                {
                    LudemeNode[] inputNodeCollection = new LudemeNode[((LudemeNode[]) input).length];
                    for(int j = 0; j < ((LudemeNode[]) input).length; j++)
                    {
                        inputNodeCollection[j] = (((LudemeNode[]) input)[j]).copy(includeInputs);
                    }
                    copy.setProvidedInput(index, inputNodeCollection);
                }
                else
                {
                    copy.setProvidedInput(index, input);
                }
            }
            System.out.println("----" + Arrays.toString(copy.providedInputs()));
        }
        System.out.println("[COPY] copied " + copy.title()); */
        return copy;
    }

    /**
     * Copies a node
     * @param x_shift The x-shift of the new copied node
     * @param y_shift The y-shift of the new copied node
     * @return a copy of the node
     */
    public LudemeNode copy(int x_shift, int y_shift)
    {
        LudemeNode copy = new LudemeNode(symbol(), x+x_shift, y+y_shift);
        copy.setVisible(visible());
        copy.setCollapsed(collapsed());
        copy.setDynamic(dynamic());
        copy.setSelectedClause(selectedClause());
        copy.setHeight(height());

        return copy;
    }

    @Override
    public String toString(){
        return stringRepresentation();
    }
}
