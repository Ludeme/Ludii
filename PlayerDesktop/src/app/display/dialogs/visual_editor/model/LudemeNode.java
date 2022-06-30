package app.display.dialogs.visual_editor.model;


import app.display.dialogs.visual_editor.LayoutManagement.Vector2D;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Preprocessing;
import app.display.dialogs.visual_editor.documentation.DocumentationReader;
import app.display.dialogs.visual_editor.documentation.HelpInformation;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import java.util.*;

/**
 * Node representation of a ludeme in the current description
 * @author Filipp Dokienko
 */

public class LudemeNode implements iGNode
{

    /** ID of last node */
    private static int LAST_ID = 0;
    /** ID of this node */
    private final int ID;
    /** Symbol/Ludeme this node represents */
    private final Symbol SYMBOL;
    /** The Node Argument which created this node */
    private NodeArgument NODE_ARGUMENT_CREATOR;
    /** List of clauses this symbol encompasses */
    private List<Clause> CLAUSES;
    /** Currently selected Clause by the user */
    private LinkedHashMap<Symbol, List<Clause>> SYMBOL_CLAUSE_MAP = new LinkedHashMap<>(); // for each clause the symbols it contains
    private Clause selectedClause;
    /** Map of NodeArgument and its corresponding input */
    private LinkedHashMap<NodeArgument, Object> providedInputsMap;
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
    /** old position coordinates of this node needed for animation */
    private int oldX, oldY;
    /** new position coordinates of this node needed for animation */
    private int newX, newY;
    /** whether this node is dynamic or not.
     * a dynamic node has no pre-selected clause. by providing any arguments to the node, the list of
     * possible clauses is narrowed down to the ones that match the provided arguments.
     */
    private boolean dynamic = false;
    /** whether this node (and thus its children) are visible (collapsed) or not. */
    private boolean collapsed = false;
    /** whether this node is visible */
    private boolean visible = true;
    /** HelpInformation */
    private final HelpInformation helpInformation;
    /** Which package this node belongs to.
     * game, game.equipment, game.functions, game.rules
     */
    private final String PACKAGE_NAME;

    // Variables for dynamic nodes
    /** HashMap of NodeArguments keyed by the clause they correspond to */
    private final HashMap<Clause, List<NodeArgument>> nodeArguments;
    /** List of NodeArguments for the current Clause of the associated LudemeNodeComponent */
    private List<NodeArgument> currentNodeArguments;
    /** Clauses that satisfy currently provided inputs */
    private List<Clause> activeClauses = new ArrayList<>();
    /** Clauses that do not satisfy currently provided inputs */
    private List<Clause> inactiveClauses = new ArrayList<>();
    /** NodeArguments that are currently provided */
    private List<NodeArgument> providedNodeArguments = new ArrayList<>();
    /** NodeArguments that can be provided to satisfy active clauses */
    private List<NodeArgument> activeNodeArguments = new ArrayList<>();
    /** NodeArguments that cannot be provided to satisfy active clauses */
    private List<NodeArgument> inactiveNodeArguments = new ArrayList<>();

    /**
     * Constructor for a new LudemeNode
     * @param symbol Symbol/Ludeme this node represents
     * @param x x coordinate of this node in the graph
     * @param y y coordinate of this node in the graph
     */
    public LudemeNode(Symbol symbol, int x, int y)
    {
       this(symbol, null, x, y);
    }

    /**
     * Constructor for a new LudemeNode
     * @param symbol Symbol/Ludeme this node represents
     * @param argument The Node Argument which created this node
     * @param x x coordinate of this node in the graph
     * @param y y coordinate of this node in the graph
     */
    public LudemeNode(Symbol symbol, NodeArgument argument, int x, int y)
    {
        this.ID = LAST_ID++;
        this.SYMBOL = symbol;
        this.NODE_ARGUMENT_CREATOR = argument;
        this.CLAUSES = symbol.rule().rhs();
        this.x = x;
        this.y = y;
        this.width = 100; // width and height are hard-coded for now, updated later
        this.height = 100;
        if(CLAUSES != null)
        {
            expandClauses();
            if(CLAUSES.size() > 0)
            {
                this.selectedClause = CLAUSES.get(0);
                if(selectedClause.args() == null) {
                    //this.providedInputs = new Object[0];
                }
                else {
                    //this.providedInputs = new Object[selectedClause.args().size()];
                }
            }
            else
            {
                this.selectedClause = null;
                //this.providedInputs = new Object[0];
            }
        } else {
            this.selectedClause = null;
            //this.providedInputs = null;
        }
        if(CLAUSES == null || CLAUSES.size() == 0)
        {
            this.dynamic = false;
        }
        if(dynamicPossible())
        {
            this.dynamic = false; // TODO
        }

        nodeArguments = generateNodeArguments();
        if(dynamic())
        {
            activeClauses = new ArrayList<>(clauses());
            inactiveClauses = new ArrayList<>();
            providedNodeArguments = new ArrayList<>();
            activeNodeArguments = new ArrayList<>();
            for(List<NodeArgument> nas: nodeArguments.values()) activeNodeArguments.addAll(nas);
            inactiveNodeArguments = new ArrayList<>();
            currentNodeArguments = activeNodeArguments;
        }
        else
        {
            currentNodeArguments = nodeArguments.get(selectedClause());
        }

        providedInputsMap = new LinkedHashMap<>();
        for(NodeArgument na : currentNodeArguments) providedInputsMap.put(na, null);

        // package name
        if(symbol.cls().getPackage() == null) PACKAGE_NAME = "game";
        else
        {
            String[] splitPackage = symbol.cls().getPackage().getName().split("\\.");
            if(splitPackage.length == 1) PACKAGE_NAME = splitPackage[0];
            else
            {
                PACKAGE_NAME = splitPackage[0] + "." + splitPackage[1];
            }
        }


        for(Clause c : CLAUSES)
        {
            if(SYMBOL_CLAUSE_MAP.containsKey(c.symbol()))
            {
                SYMBOL_CLAUSE_MAP.get(c.symbol()).add(c);
            }
            else
            {
                List<Clause> l = new ArrayList<>();
                l.add(c);
                SYMBOL_CLAUSE_MAP.put(c.symbol(), l);
            }
        }

        this.helpInformation = DocumentationReader.instance().help(SYMBOL);
    }

    /**
     * Constructor for a new LudemeNode
     * @param symbol Symbol/Ludeme this node represents
     * @param argument The Node Argument which created this node
     * @param x x coordinate of this node in the graph
     * @param y y coordinate of this node in the graph
     */
    public LudemeNode(Symbol symbol, NodeArgument argument, HashMap<Clause, List<NodeArgument>> nodeArguments, int x, int y)
    {
        this.ID = LAST_ID++;
        this.SYMBOL = symbol;
        this.NODE_ARGUMENT_CREATOR = argument;
        this.CLAUSES = symbol.rule().rhs();
        this.x = x;
        this.y = y;
        this.nodeArguments = nodeArguments;
        this.width = 100; // width and height are hard-coded for now, updated later
        this.height = 100;

        if(CLAUSES != null)
        {
            expandClauses();
            if(CLAUSES.size() > 0)
            {
                this.selectedClause = CLAUSES.get(0);
            }
            else
            {
                this.selectedClause = null;
            }
        } else {
            this.selectedClause = null;
        }
        if(CLAUSES == null || CLAUSES.size() == 0)
        {
            this.dynamic = false;
        }
        if(dynamicPossible())
        {
            this.dynamic = false; // TODO
        }

        if(dynamic())
        {
            activeClauses = new ArrayList<>(clauses());
            inactiveClauses = new ArrayList<>();
            providedNodeArguments = new ArrayList<>();
            activeNodeArguments = new ArrayList<>();
            for(List<NodeArgument> nas: nodeArguments.values()) activeNodeArguments.addAll(nas);
            inactiveNodeArguments = new ArrayList<>();
            currentNodeArguments = activeNodeArguments;
        }
        else
        {
            currentNodeArguments = nodeArguments.get(selectedClause());
        }

        providedInputsMap = new LinkedHashMap<>();
        for(NodeArgument na : currentNodeArguments) providedInputsMap.put(na, null);

        // package name
        if(symbol.cls().getPackage() == null) PACKAGE_NAME = "game";
        else
        {
            String[] splitPackage = symbol.cls().getPackage().getName().split("\\.");
            if(splitPackage.length == 1) PACKAGE_NAME = splitPackage[0];
            else
            {
                PACKAGE_NAME = splitPackage[0] + "." + splitPackage[1];
            }
        }
        this.helpInformation = DocumentationReader.instance().help(SYMBOL);
    }

    /**
     *
     * @return the symbol this node represents
     */
    public Symbol symbol()
    {
        return SYMBOL;
    }

    /**
     *
     * @return The Node Argument which created this node
     */
    public NodeArgument creatorArgument()
    {
        return NODE_ARGUMENT_CREATOR;
    }

    /**
     * Called if another NodeArgument is provided with this node
     * @param argument
     */
    public void setCreatorArgument(NodeArgument argument)
    {
        this.NODE_ARGUMENT_CREATOR = argument;
    }

    /**
     * Clauses without a constructor (e.g. <match> in the "game" symbol) are expanded to the clauses of "match"
     */
    private void expandClauses()
    {
        List<Clause> newClauses = new ArrayList<>();
        for(Clause clause : CLAUSES)
        {
            if(clause.symbol() != symbol()) {
                newClauses.addAll(expandClauses(clause.symbol()));
            }
            else
            {
                newClauses.add(clause);
            }
        }
        CLAUSES = newClauses;
    }

    private List<Clause> expandClauses(Symbol s)
    {
        List<Clause> clauses = new ArrayList<>();
        for(Clause clause : s.rule().rhs())
        {
            if(clauses.contains(clause) || this.clauses().contains(clause)) continue;
            if(clause.symbol() == s) {
                clauses.add(clause);
            }
            else
            {
                clauses.addAll(expandClauses(clause.symbol()));
            }
        }
        return clauses;
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
        this.currentNodeArguments = nodeArguments.get(selectedClause());
        //this.providedInputs = new Object[selectedClause.args().size()];
        providedInputsMap = new LinkedHashMap<>();
        for(NodeArgument na : currentNodeArguments) providedInputsMap.put(na, null);
    }

    /**
     *
     * @return the currently selected clause
     */
    public Clause selectedClause()
    {
        return selectedClause;
    }

    /**
     *
     * @return the array of provided inputs
     */
    /*@Override
    public Object[] providedInputs()
    {
        return providedInputs;
    }*/

    public LinkedHashMap<NodeArgument, Object> providedInputsMap()
    {
        return providedInputsMap;
    }

    /**
     * Sets the provided inputs to the given array
     *  index the index of the supplied argument to set
     *  input the input to set
     */
    /*public void setProvidedInput(int index, Object input)
    {
        providedInputs[index] = input;
    }*/

    public void setProvidedInput(NodeArgument arg, Object input)
    {
        if(providedInputsMap.containsKey(arg))
        {
            providedInputsMap.put(arg, input);
        }
        else
        {
            System.out.println("Error: setProvidedInput called with invalid argument");
        }
    }

    public void setProvidedInput(ClauseArg arg, Object input)
    {
        // TODO:
    }

    /**
     * Sets this node to be dynamic or not
     * @param dynamic the dynamic to set
     */
    public void setDynamic(boolean dynamic)
    {
        if(dynamic && !dynamicPossible()) {
            this.dynamic=false;
            return;
        }
        this.dynamic = dynamic;
        if(dynamic)
        {
            // set current clause to biggest
            for(Clause c : CLAUSES)
            {
                if(c.args() == null) continue;
                if(c.args().size() > selectedClause.args().size())
                {
                    selectedClause = c;
                    //providedInputs = new Object[selectedClause.args().size()];
                }
            }
        }
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
        if(parentNode() == null)
        {
            this.collapsed = collapsed;
            return;
        }
        this.visible = !collapsed;
        if(!collapsed) this.collapsed = collapsed;
        // the complete subtree of this node becomes invisible if collapsed or visible if not collapsed
        setSubtreeVisible(!collapsed);
        if(collapsed) this.collapsed = collapsed;
        /*for(LudemeNode child : children)
        {
            child.setSubtreeVisible(!collapsed);
            //child.setSubtreeVisible(!collapsed);
        }*/
    }

    /**
     * Sets the visibility of this node's subtree
     * @param visible the visibility to set
     */
    private void setSubtreeVisible(boolean visible)
    {
        List<LudemeNode> currentChildren = new ArrayList<>(children);
        while(!currentChildren.isEmpty())
        {
            for(LudemeNode child : new ArrayList<>(currentChildren))
            {
                currentChildren.remove(child);
                if(child.parent.collapsed() || (child != this && child.collapsed)) continue;
                child.setVisible(visible);
                currentChildren.addAll(child.children);
            }
        }
    }

    /**
     *
     * @return whether this node is collapsed
     */
    public boolean collapsed(){
        return collapsed;
    }


    public LinkedHashMap<Symbol, List<Clause>> symbolClauseMap()
    {
        return SYMBOL_CLAUSE_MAP;
    }

    // Methods for Dynamic Nodes


    /**
     *
     * @return a HashMap of NodeArguments keyed by the clause they correspond to
     */
    private HashMap<Clause, List<NodeArgument>> generateNodeArguments()
    {
        HashMap<Clause, List<NodeArgument>> nodeArguments = new HashMap<>();
        for (Clause clause : clauses())
        {
            nodeArguments.put(clause, generateNodeArguments(clause));
        }
        return nodeArguments;
    }

    /**
     * Generates a list of lists of NodeArguments for a given Clause
     * @param clause Clause to generate the list of lists of NodeArguments for
     * @return List of lists of NodeArguments for the given Clause
     */
    private List<NodeArgument> generateNodeArguments(Clause clause)
    {
        List<NodeArgument> nodeArguments = new ArrayList<>();
        if(clause.symbol().ludemeType().equals(Symbol.LudemeType.Predefined)) // TODO [FLAG]      Add   && clause.args() == null)
        {
            NodeArgument nodeArgument = new NodeArgument(clause);
            nodeArguments.add(nodeArgument);
            return nodeArguments;
        }
        List<ClauseArg> clauseArgs = clause.args();
        for(int i = 0; i < clauseArgs.size(); i++)
        {
            ClauseArg clauseArg = clauseArgs.get(i);
            // Some clauses have Constant clauseArgs followed by the constructor keyword. They should not be included in the InputArea
            if(nodeArguments.isEmpty() && clauseArg.symbol().ludemeType().equals(Symbol.LudemeType.Constant))
                continue;
            NodeArgument nodeArgument = new NodeArgument(clause, clauseArg);
            nodeArguments.add(nodeArgument);
            // if the clauseArg is part of a OR-Group, they all are added to the NodeArgument automatically, and hence can be skipped in the next iteration
            i = i + nodeArgument.originalArgs.size() - 1;
        }
        return nodeArguments;
    }




    /**
     *
     * @return a HashMap of NodeArguments keyed by the clause they correspond to
     */
    public HashMap<Clause, List<NodeArgument>> nodeArguments()
    {
        return nodeArguments;
    }

    /**
     *
     * @return the List of NodeArguments for the current Clause of the associated LudemeNodeComponent
     */
    public List<NodeArgument> currentNodeArguments()
    {
        return nodeArguments().get(selectedClause());
    }


    /**
     * Sets the current node arguments
      * @param nodeArguments the node arguments to set
     */
    public void setCurrentNodeArguments(List<NodeArgument> nodeArguments)
    {
        this.nodeArguments.put(selectedClause(), nodeArguments);
    }

    /**
     *
     * @return The list of active clauses
     */
    public List<Clause> activeClauses()
    {
        return activeClauses;
    }

    /**
     *
     * @return The list of inactive clauses
     */
    public List<Clause> inactiveClauses()
    {
        return inactiveClauses;
    }

    /**
     *
     * @return The list of active Node Arguments
     */
    public List<NodeArgument> activeNodeArguments()
    {
        return activeNodeArguments;
    }

    /**
     *
     * @return The list of inactive Node Arguments
     */
    public List<NodeArgument> inactiveNodeArguments()
    {
        return inactiveNodeArguments;
    }

    /**
     *
     * @return The list of provided node arguments
     */
    public List<NodeArgument> providedNodeArguments()
    {
        return providedNodeArguments;
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

    @Override
    public Vector2D oldPos() {
        return new Vector2D(oldX, oldY);
    }

    @Override
    public Vector2D newPos() {
        return new Vector2D(newX, newY);
    }

    /**
     * Set the position of this node in the graph
     * @param pos the position to set
     */
    @Override
    public void setPos(Vector2D pos)
    {
        x = (int) Math.round(pos.x());
        y = (int) Math.round(pos.y());
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

    public void setOldPos(Vector2D pos)
    {
        this.oldX = (int) Math.round(pos.x());
        this.oldY = (int) Math.round(pos.y());
    }

    public void setNewPos(Vector2D pos)
    {
        this.newX = (int) Math.round(pos.x());
        this.newY = (int) Math.round(pos.y());
    }

    /**
     * Sets the parent of this node
     * @param ludemeNode the parent to set
     */
    public void setParent(LudemeNode ludemeNode)
    {
        this.parent = ludemeNode;
    }

    /**
     * Adds a child to this node
     * @param child the child to add
     */
    public void addChildren(LudemeNode child)
    {
        // Checks if child nodes was already added
        if (!this.children.contains(child))
        {
            this.children.add(child);
            // get order of new child in current constructor
            // TODO: something goes wrong for [optional] inputs
            int order = -1;

            for (ClauseArg arg : selectedClause.args())
            {
                if (arg.symbol().name().equals(child.symbol().name()))
                {
                    order = selectedClause.args().indexOf(arg);
                    break;
                }
            }

            childrenOrder.put(child, order);
            // placing child in correct order
            for (int i = this.children.size()-1; i > 0; i--)
            {
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
        if (id() == 12)
        {
            System.out.println("HHHmmm");
        }
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


    public String toLud()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(tokenTitle());
        // append all inputs
        for (NodeArgument arg : providedInputsMap().keySet())
        {
            Object input = providedInputsMap().get(arg);
            if(input == null) continue;
            sb.append(" ");
            if(input instanceof LudemeNode)
            {
                sb.append(((LudemeNode) input).toLud());
            }
            else if(input instanceof Object[])
            {
                sb.append("{ ");
                for(Object obj : (Object[]) input)
                {
                    if(obj == null) continue;
                    if(obj instanceof LudemeNode) sb.append(((LudemeNode)obj).toLud());
                    else sb.append(obj).append(" ");
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

    public String toLudCodeCompletion(List<NodeArgument> argsOfInputField)
    {
        LudemeNode root = this;
        while(root.parentNode() != null) root = root.parentNode();

        String rootLud = root.toLud();
        String thisLud = toLud();

        // compute code completion lud for this node
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(tokenTitle());
        // append all inputs
        for (NodeArgument arg : providedInputsMap().keySet())
        {
            Object input = providedInputsMap().get(arg);

            if(argsOfInputField.contains(arg))
            {
                if(input instanceof Object[])
                {
                    sb.append("{ ");
                    for(Object obj : (Object[]) input)
                    {
                        if(obj == null) continue;
                        if(obj instanceof LudemeNode) sb.append(((LudemeNode)obj).toLud());
                        else sb.append(obj).append(" ");
                    }
                }
                sb.append(" ").append(Preprocessing.COMPLETION_WILDCARD);
                // replace thisLud with sb
                return rootLud.replace(thisLud, sb.toString());
            }

            if(input == null) continue;
            sb.append(" ");
            if(input instanceof LudemeNode)
            {
                sb.append(((LudemeNode) input).toLud());
            }
            else if(input instanceof Object[])
            {
                sb.append("{ ");
                for(Object obj : (Object[]) input)
                {
                    if(obj == null) continue;
                    if(obj instanceof LudemeNode) sb.append(((LudemeNode)obj).toLud());
                    else sb.append(obj).append(" ");
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
        return rootLud.replace(thisLud, sb.toString());
    }


    /**
     * The title consists of the symbol and any Constants followed by the constructor
     * @return The title of this node
     */
    public String title()
    {
        if(selectedClause.args() != null && selectedClause.args().isEmpty()) return selectedClause.symbol().name();
        StringBuilder title = new StringBuilder(selectedClause().symbol().name());
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
        StringBuilder title = new StringBuilder(selectedClause().symbol().token());
        if(selectedClause().args() == null) return title.toString();
        if(selectedClause.args().size() == 0) return title.toString();
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
        return copy(0, 0);
    }

    /**
     * Copies a node
     * @param x_shift The x-shift of the new copied node
     * @param y_shift The y-shift of the new copied node
     * @return a copy of the node
     */
    public LudemeNode copy(int x_shift, int y_shift)
    {
        LudemeNode copy = new LudemeNode(symbol(), creatorArgument(),nodeArguments,x+x_shift, y+y_shift);
        copy.setCollapsed(collapsed());
        copy.setVisible(visible());
        copy.setDynamic(dynamic());
        copy.setSelectedClause(selectedClause());
        copy.setHeight(height());

        for(NodeArgument na : providedInputsMap.keySet())
        {
            Object input = providedInputsMap.get(na);
            if(input == null || input instanceof LudemeNode) continue; // if no input provided or it is LudemeNode, skip it
            boolean isLudemeCollection = false;
            if(input instanceof Object[])
            {
                for (Object o : (Object[]) input)
                    if (o instanceof LudemeNode)
                    {
                        isLudemeCollection = true;
                        break;
                    }
            }
            if(!isLudemeCollection) copy.setProvidedInput(na, input);
        }

        return copy;
    }

    /**
     *
     * @return The package name of the symbol
     */
    public String packageName()
    {
        return PACKAGE_NAME;
    }

    /**
     *
     * @return The HelpInformation
     */
    public HelpInformation helpInformation()
    {
        return helpInformation;
    }

    public String description()
    {
        if(helpInformation == null) return "";
        if(selectedClause.symbol() == symbol()) return helpInformation.description();
        else return DocumentationReader.instance().documentation().get(selectedClause.symbol()).description();
    }

    public String parameterDescription(ClauseArg arg)
    {
        if(helpInformation == null) return "";
        return helpInformation.parameter(arg);
    }

    public int x()
    {
        return x;
    }

    public int y()
    {
        return y;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    @Override
    public String toString(){
        return toLud();
    }
}
