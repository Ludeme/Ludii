package app.display.dialogs.visual_editor.model;


import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iLudemeNode;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
    private boolean dynamic = false; // TODO: Not hard-coded

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
        if(CLAUSES != null) {
            this.selectedClause = CLAUSES.get(0);
            this.providedInputs = new Object[CLAUSES.get(0).args().size()];
        } else {
            this.selectedClause = null;
            this.providedInputs = null;
        }
        /* TODO: Fix such that this is not needed. Issue: Structural nodes should not be added to the graph
        if(symbol.rule().rhs() != null) this.selectedClause = symbol.rule().rhs().get(0);
        while(selectedClause.args() == null) {
            selectedClause = selectedClause.symbol().rule().rhs().get(0);
        }
        this.providedInputs = new Object[selectedClause.args().size()];
         */
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

    // TODO: Should be probably in iGNode ?

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
        this.parent = (LudemeNode) ludemeNode; // TODO: should it be casted?
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
     * @return The .lud equivalent representation of this node
     */
    @Override
    public String stringRepresentation()
    {
        return ""; // TODO
    }

    @Override
    public String toString(){
        return stringRepresentation();
    }
}
