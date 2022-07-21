package app.display.dialogs.visual_editor.model;


import app.display.dialogs.visual_editor.LayoutManagement.Vector2D;
import app.display.dialogs.visual_editor.documentation.DocumentationReader;
import app.display.dialogs.visual_editor.documentation.HelpInformation;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Preprocessing;
import grammar.Grammar;
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
    private Symbol symbol;
    /** The Node Argument which created this node */
    private NodeArgument NODE_ARGUMENT_CREATOR;
    /** List of clauses this symbol encompasses */
    private List<Clause> clauses;
    /** Currently selected Clause by the user */
    private final LinkedHashMap<Symbol, List<Clause>> SYMBOL_CLAUSE_MAP = new LinkedHashMap<>(); // for each clause the symbols it contains
    private Clause selectedClause = null;
    /** Map of NodeArgument and its corresponding input */
    private LinkedHashMap<NodeArgument, Object> providedInputsMap;
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
    /** whether this node (and thus its children) are visible (collapsed) or not. */
    private boolean collapsed = false;
    /** whether this node is visible */
    private boolean visible = true;
    /** whether this node is a root of fixed node group */
    private boolean fixed = false;
    /** HelpInformation */
    private final HelpInformation helpInformation;
    /** Which package this node belongs to.
     * game, game.equipment, game.functions, game.rules
     */
    private final String PACKAGE_NAME;
    /** HashMap of NodeArguments keyed by the clause they correspond to */
    private final HashMap<Clause, List<NodeArgument>> nodeArguments;
    /** List of NodeArguments for the current Clause of the associated LudemeNodeComponent */
    private List<NodeArgument> currentNodeArguments;
    /** Whether this node is a 1D Collection-supply for a 2D-Collection */
    private boolean is1DCollectionNode = false;
    private boolean isDefineRoot = false;
    private DescriptionGraph defineGraph;
    private boolean isDefineNode = false;
    private LudemeNode macroNode = null;


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
        this(symbol, argument, null, x, y);
    }

    /**
     * Constructor for a new LudemeNode
     * @param symbol Symbol/Ludeme this node represents
     * @param argument The Node Argument which created this node
     * @param nodeArguments HashMap of NodeArguments keyed by the clause they correspond to (null if not available)
     * @param x x coordinate of this node in the graph
     * @param y y coordinate of this node in the graph
     */
    public LudemeNode(Symbol symbol, NodeArgument argument, HashMap<Clause, List<NodeArgument>> nodeArguments, int x, int y)
    {
        this.ID = LAST_ID++;
        this.symbol = symbol;
        this.NODE_ARGUMENT_CREATOR = argument;
        if(symbol.rule() == null)
            this.clauses = new ArrayList<>();
        else
            this.clauses = symbol.rule().rhs();
        this.x = x;
        this.y = y;
        this.width = 100; // width and height are hard-coded for now, are updated later
        this.height = 100;

        // Expand clauses if possible
        if(clauses != null)
        {
            expandClauses();
            if(clauses.size() > 0)
                this.selectedClause = clauses.get(0);
        }

        // Create a NodeArgument for each ClauseArg, if not provided as parameter
        HashMap<Clause, List<NodeArgument>> nodeArguments2 = nodeArguments;
		if(nodeArguments2 == null)
			nodeArguments2 = generateNodeArguments();
        this.nodeArguments = nodeArguments2;

        // get the NodeArguments of the currently selected clause
        currentNodeArguments = nodeArguments2.get(selectedClause());
        if(currentNodeArguments == null)
            currentNodeArguments = new ArrayList<>();

        // initialize the provided inputs map
        providedInputsMap = new LinkedHashMap<>();
        for(NodeArgument na : currentNodeArguments) providedInputsMap.put(na, null);

        // package name
        this.PACKAGE_NAME = initPackageName();

        clauses.sort(Comparator.comparing(Clause::toString));

        // Create a map of symbols and their corresponding clauses, used in the pick-a-constructor menu to group clauses by symbols
        for(Clause c : clauses)
            if(SYMBOL_CLAUSE_MAP.containsKey(c.symbol()))
                SYMBOL_CLAUSE_MAP.get(c.symbol()).add(c);
            else
            {
                List<Clause> l = new ArrayList<>();
                l.add(c);
                SYMBOL_CLAUSE_MAP.put(c.symbol(), l);
            }

        this.helpInformation = DocumentationReader.instance().help(this.symbol);
    }

    /**
     * Creates a LudemeNode which represents a 1D collection
     * @param na2DCollection NodeArgument which represents the 2D collection, and this 1D collection is part of
     * @param x x coordinate of this node in the graph
     * @param y y coordinate of this node in the graph
     */
    public LudemeNode(NodeArgument na2DCollection, int x, int y)
    {
        this.ID = LAST_ID++;
        this.is1DCollectionNode = true;
        this.x = x;
        this.y = y;
        this.width = 100; // width and height are hard-coded for now, are updated later
        this.height = 100;

        NodeArgument na1DCollection = na2DCollection.collection1DEquivalent(na2DCollection.args().get(0));
        this.symbol = na1DCollection.arg().symbol();
        this.clauses = new ArrayList<>();
        List<ClauseArg> clauseArgs = new ArrayList<>();
        clauseArgs.add(na1DCollection.arg());
        this.clauses.add(new Clause(this.symbol, clauseArgs, false));
        // Expand clauses if possible
        if(clauses != null)
        {
            expandClauses();
            if(clauses.size() > 0)
                this.selectedClause = clauses.get(0);
        }

        this.NODE_ARGUMENT_CREATOR = na2DCollection;
        this.nodeArguments = generateNodeArguments();
        currentNodeArguments = nodeArguments.get(selectedClause());

        // initialize the provided inputs map
        providedInputsMap = new LinkedHashMap<>();
        for(NodeArgument na : currentNodeArguments)
            providedInputsMap.put(na, null);

        clauses.sort(Comparator.comparing(Clause::toString));

        // package name
        this.PACKAGE_NAME = initPackageName();
        this.helpInformation = DocumentationReader.instance().help(symbol);
    }

    /**
     * Creates a Define-Graph Root LudemeNode
     * @param x
     * @param y
     * @param viableDefineRoots
     * @param isDefine
     */
    public LudemeNode(int x, int y, List<Symbol> viableDefineRoots, boolean isDefine)
    {
        assert isDefine;
        Symbol s = new Symbol(Symbol.LudemeType.Ludeme, "Define","define",null);
        List<ClauseArg> args = new ArrayList<>();
        args.add(new ClauseArg(Grammar.grammar().symbolsWithPartialKeyword("string").get(0), "Name", null, false, 0, 0));
        Clause c = new Clause(s, args, true);
        System.out.println();


        this.ID = LAST_ID++;
        this.x = x;
        this.y = y;
        this.symbol = s;
        this.clauses = new ArrayList<>();
        clauses.add(c);
        selectedClause = c;

        helpInformation = null;
        PACKAGE_NAME = "game";
        nodeArguments = new HashMap<>();
        List<NodeArgument> nas = new ArrayList<>();
        nas.add(new NodeArgument(c, args.get(0)));
        nas.add(new NodeArgument(s, c, viableDefineRoots));
        nodeArguments.put(c, nas);
        currentNodeArguments = nas;
        providedInputsMap = new LinkedHashMap<>();
        for(NodeArgument na : currentNodeArguments)
            providedInputsMap.put(na, null);

        isDefineRoot = true;
    }

    public LudemeNode(Symbol symbol, LudemeNode macroNode, DescriptionGraph defineGraph, List<NodeArgument> requiredParameters, int x, int y, boolean isDefine)
    {
        assert isDefine;
        this.ID = LAST_ID++;
        this.symbol = symbol;
        this.x = x;
        this.y = y;
        this.clauses = new ArrayList<>();
        this.defineGraph = defineGraph;
        Clause c = new Clause(symbol, new ArrayList<>(), true);
        clauses.add(c);
        selectedClause = c;
        this.helpInformation = null;
        PACKAGE_NAME = "define";
        this.nodeArguments = new LinkedHashMap<>();
        this.nodeArguments.put(clauses.get(0), requiredParameters);
        this.currentNodeArguments = requiredParameters;
        providedInputsMap = new LinkedHashMap<>();
        for(NodeArgument na : currentNodeArguments)
            providedInputsMap.put(na, null);
        isDefineNode = true;
        this.macroNode = macroNode;
    }

    /**
     * Updates the symbol of a define node.
     * Called when the define name was modified.
     * @param symbol1
     */
    public void updateDefineNode(Symbol symbol1)
    {
        assert isDefineNode();
        List<NodeArgument> oldArgs = nodeArguments.get(selectedClause());
        this.symbol = symbol1;
        this.clauses.remove(0);
        Clause c = new Clause(symbol1, new ArrayList<>(), true);
        this.clauses.add(c);
        this.selectedClause = c;
        nodeArguments.clear();
        nodeArguments.put(c, oldArgs);
    }

    /**
     * Updates the required parameters of a define node.
     * @param parameters
     */
    public void updateDefineNode(List<NodeArgument> parameters)
    {
        assert isDefineNode();
        this.nodeArguments.put(clauses.get(0), parameters);
        this.currentNodeArguments = parameters;

        LinkedHashMap<NodeArgument, Object> newInputMap = new LinkedHashMap<>();
        for(NodeArgument na : parameters)
            newInputMap.put(na, providedInputsMap.get(na));

        this.providedInputsMap = newInputMap;

        for(NodeArgument na : currentNodeArguments)
            providedInputsMap.put(na, null);
    }


    public void updateDefineNode(LudemeNode macroNode1)
    {
        this.macroNode = macroNode1;
    }
    /**
     *
     * @return The package name this symbol/node belongs to
     */
    private String initPackageName()
    {
        String packageName = "game";
        if(symbol.cls().getPackage() != null)
        {
            String[] splitPackage = symbol.cls().getPackage().getName().split("\\.");
            if(splitPackage.length == 1)
                packageName = splitPackage[0];
            else
                packageName = splitPackage[0] + "." + splitPackage[1];
        }
        return packageName;
    }


    /**
     *
     * @return the symbol this node represents
     */
    public Symbol symbol()
    {
        return symbol;
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
        for(Clause clause : clauses)
            if(clause.symbol() != symbol())
                newClauses.addAll(expandClauses(clause.symbol()));
            else
                newClauses.add(clause);
        clauses = newClauses;
    }

    private List<Clause> expandClauses(Symbol s)
    {
        List<Clause> clauses1 = new ArrayList<>();
        for(Clause clause : s.rule().rhs())
            if(clauses1.contains(clause) || this.clauses().contains(clause))
            {}
            else if(clause.symbol() == s)
                clauses1.add(clause);
            else
                clauses1.addAll(expandClauses(clause.symbol()));
        return clauses1;
    }

    /**
     *
     * @return the list of clauses this symbol encompasses
     */
    public List<Clause> clauses()
    {
        return clauses;
    }

    /**
     * Changes the selected clause of this node
     * @param selectedClause the selected clause to set
     */
    public void setSelectedClause(Clause selectedClause)
    {
        this.selectedClause = selectedClause;
        this.currentNodeArguments = nodeArguments.get(selectedClause());
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
     * @return the map of NodeArguments keyed by the input provided by the user
     */
    public LinkedHashMap<NodeArgument, Object> providedInputsMap()
    {
        return providedInputsMap;
    }

    public void setProvidedInput(NodeArgument arg, Object input)
    {
        if(providedInputsMap.containsKey(arg))
            providedInputsMap.put(arg, input);
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
        if(!collapsed)
            this.collapsed = false;
        // the complete subtree of this node becomes invisible if collapsed or visible if not collapsed
        setSubtreeVisible(!collapsed);
        if(collapsed)
            this.collapsed = true;
    }

    /**
     * Sets the visibility of this node's subtree
     * @param visible the visibility to set
     */
    private void setSubtreeVisible(boolean visible)
    {
        List<LudemeNode> currentChildren = new ArrayList<>(children);
        while(!currentChildren.isEmpty())
            for(LudemeNode child : new ArrayList<>(currentChildren))
            {
                currentChildren.remove(child);
                if(child.parent.collapsed() || (child != this && child.collapsed))
                {
                    continue;
                }
                child.setVisible(visible);
                currentChildren.addAll(child.children);
            }
    }

    /**
     *
     * @return whether this node is collapsed
     */
    public boolean collapsed(){
        return collapsed;
    }

    public boolean isSatisfied()
    {
        for(NodeArgument na : providedInputsMap.keySet())
            if(!na.optional() && providedInputsMap.get(na) == null)
                return false;
            else if(!na.optional() && providedInputsMap.get(na) instanceof Object[])
                for(Object o : (Object[])providedInputsMap.get(na))
                    if(o == null)
                        return false;
        return true;
    }

    public List<NodeArgument> unfilledRequiredArguments()
    {
        List<NodeArgument> arguments = new ArrayList<>();
        for(NodeArgument na : providedInputsMap.keySet())
            if(providedInputsMap.get(na) == Handler.PARAMETER_SYMBOL)
                arguments.add(na);
            else if(!na.optional() && providedInputsMap.get(na) == null) //  ! If optional inputs should be included as parameter, remove "!na.optional() &&"
                arguments.add(na);
            else if(providedInputsMap.get(na) != null && (providedInputsMap.get(na).equals("") || providedInputsMap.get(na).equals("<PARAMETER>")))
                arguments.add(na);
        return arguments;
    }

    public LinkedHashMap<Symbol, List<Clause>> symbolClauseMap()
    {
        return SYMBOL_CLAUSE_MAP;
    }


    public boolean is1DCollectionNode()
    {
        return is1DCollectionNode;
    }


    /**
     *
     * @return a HashMap of NodeArguments keyed by the clause they correspond to
     */
    private HashMap<Clause, List<NodeArgument>> generateNodeArguments()
    {
        HashMap<Clause, List<NodeArgument>> nodeArguments1 = new HashMap<>();
        if(clauses() == null)
            return nodeArguments1;
        for (Clause clause : clauses())
            nodeArguments1.put(clause, generateNodeArguments(clause));
        return nodeArguments1;
    }

    /**
     * Generates a list of lists of NodeArguments for a given Clause
     * @param clause Clause to generate the list of lists of NodeArguments for
     * @return List of lists of NodeArguments for the given Clause
     */
    private static List<NodeArgument> generateNodeArguments(Clause clause)
    {
        List<NodeArgument> nodeArguments1 = new ArrayList<>();
        if(clause.symbol().ludemeType().equals(Symbol.LudemeType.Predefined))
        {
            NodeArgument nodeArgument = new NodeArgument(clause);
            nodeArguments1.add(nodeArgument);
            return nodeArguments1;
        }
        List<ClauseArg> clauseArgs = clause.args();
        for(int i = 0; i < clauseArgs.size(); i++)
        {
            ClauseArg clauseArg = clauseArgs.get(i);
            // Some clauses have Constant clauseArgs followed by the constructor keyword. They should not be included in the InputArea
            if(nodeArguments1.isEmpty() && clauseArg.symbol().ludemeType().equals(Symbol.LudemeType.Constant))
                continue;
            NodeArgument nodeArgument = new NodeArgument(clause, clauseArg);
            nodeArguments1.add(nodeArgument);
            // if the clauseArg is part of a OR-Group, they all are added to the NodeArgument automatically, and hence can be skipped in the next iteration
            i = i + nodeArgument.originalArgs.size() - 1;
        }
        return nodeArguments1;
    }


    /**
     *
     * @return the List of NodeArguments for the current Clause of the associated LudemeNodeComponent
     */
    public List<NodeArgument> currentNodeArguments()
    {
        currentNodeArguments = nodeArguments.get(selectedClause());
        if(currentNodeArguments == null)
            currentNodeArguments = new ArrayList<>();
        return currentNodeArguments;
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

    @Override
    public boolean fixed()
    {
        return fixed;
    }

    public void setFixed(boolean fixed)
    {
        System.out.println("Fixed: " + fixed);
        this.fixed = fixed;
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
    public void setParent(LudemeNode ludemeNode)
    {
        this.parent = ludemeNode;

    }

    /**
     * Adds a child to this node
     * @param child the child to add
     */
    public void addChild(LudemeNode child)
    {
        // Checks if child nodes was already added
        if (!this.children.contains(child))
        {
            children.clear();
            providedInputsMap.forEach((k,v) -> {
                if (v instanceof LudemeNode)
                {
                    children.add((LudemeNode) v);
                }
                else if (v instanceof Object[])
                {
                    for (Object obj: (Object[]) v)
                    {
                        if (obj instanceof LudemeNode) children.add((LudemeNode) obj);
                    }
                }
            });
        }
    }

    /**
     * Removes a child from this node
     * @param children1 the child to remove
     */
    public void removeChildren(LudemeNode children1)
    {
        this.children.remove(children1);
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
        return toLud(false);
    }
    public String toLud(boolean isDefinePanel)
    {

        if(isDefineNode)
            return toLudDefineNode();

        StringBuilder sb = new StringBuilder();
        if(parentNode() != null)
            sb.append("\n");
        int depth1 = 0;
        LudemeNode n = this;
        while(n.parentNode() != null)
        {
            n = n.parentNode();
            depth1++;
        }
        for(int i = 0 ; i < depth1; i++)
            sb.append("\t");

        if(is1DCollectionNode())
        {
            // get collection
            Object[] collection = (Object[]) providedInputsMap().get(providedInputsMap().keySet().iterator().next());
            if(collection == null)
                return "{ } ";
            sb.append("{");
            for (Object in : collection)
            {
                if (in == null)
                    continue;
                if (in instanceof String)
                    sb.append("\"").append(in).append("\"");
                else if (in instanceof LudemeNode)
                    sb.append(((LudemeNode) in).toLud());
                else
                    sb.append(in);
                sb.append(" ");
            }
            sb.append("} ");
            return sb.toString();
        }

        sb.append("(");
        sb.append(tokenTitle());
        // append all inputs
        for (NodeArgument arg : providedInputsMap().keySet())
        {
            Object input = providedInputsMap().get(arg);
            if(input == null)
            {
                if(!arg.optional() && isDefinePanel)
                {
                    if(arg.arg().label() != null)
                        sb.append(" ").append(arg.arg().label()).append(":<PARAMETER>");
                    else
                        sb.append(" <PARAMETER> ");
                }
                continue;
            }
            sb.append(" ");
            if(arg.arg().label() != null)
                sb.append(arg.arg().label()).append(":");
            if(input instanceof LudemeNode)
                sb.append(((LudemeNode) input).toLud(isDefinePanel));
            else if(input instanceof Object[])
            {
                sb.append(collectionToLud((Object[]) input, arg, isDefinePanel));
            }
            else if(input instanceof String)
                sb.append("\"").append(input).append("\"");
            else
                sb.append(input);
        }
        if(numberOfMandatoryLudemeInputs() > 1)
        {
            for(int i = 0 ; i < depth1; i++)
            {
                sb.append("\t");
            }
            sb.append("\n");
        }
        sb.append(")");
        return sb.toString();
    }

    public String toLudDefineNode()
    {
        String rawLud = defineGraph.toLud();
        // fill parameters
        int currentI = 1;
        while(rawLud.contains("#"+currentI))
        {
            Object input;
            if(currentI-1 >= currentNodeArguments.size())
                input = null;
            else
                input = providedInputsMap().get(currentNodeArguments.get(currentI-1));
            String replacement = "";
            if(input instanceof LudemeNode)
                replacement = ((LudemeNode)input).toLud();
            else if(input instanceof Object[])
                replacement = collectionToLud((Object[])input,currentNodeArguments.get(currentI-1), false);
            else if(input instanceof String)
                replacement = "\"" + input + "\"";
            else if(input!=null)
                replacement = input.toString();
            // remove label if any input = null
            int indexOfCross = rawLud.indexOf("#");
            if(input == null & rawLud.charAt(indexOfCross-1) == ':')
            {
                // find last space before :
                int indexSpace = indexOfCross-2;
                while(rawLud.charAt(indexSpace) != ' ')
                    indexSpace--;
                String label = rawLud.substring(indexSpace, indexOfCross-1);
                rawLud = rawLud.replace(label+":#"+currentI, "#"+currentI);
            }
            rawLud = rawLud.replaceFirst("#"+currentI, replacement);
            currentI++;
        }
        // cut define fragments
        rawLud = rawLud.substring(1, rawLud.length()-1);
        rawLud = rawLud.substring(rawLud.indexOf("("));
        rawLud = rawLud.replaceAll("\\s+"," ");
        return rawLud;
    }

    public String collectionToLud(Object[] collection, NodeArgument arg, boolean isDefinePanel)
    {
        if(collection.length == 0 && arg.optional()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for(Object obj : collection)
        {
            if(obj == null)
                continue;
            if(obj instanceof LudemeNode)
                sb.append(((LudemeNode)obj).toLud(isDefinePanel)).append(" ");
            else if(obj instanceof String)
                sb.append("\"").append(obj).append("\"");
            else
                sb.append(obj).append(" ");
        }
        sb.append("}");
        return sb.toString();
    }

    public String toLudCodeCompletion(List<NodeArgument> argsOfInputField)
    {
        LudemeNode root = this;
        while(root.parentNode() != null)
            root = root.parentNode();

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
                    if(arg.arg().label() != null)
                        sb.append(arg.arg().label()).append(":");
                    sb.append("{ ");
                    for(Object obj : (Object[]) input)
                    {
                        if(obj == null)
                            continue;
                        if(obj instanceof LudemeNode)
                            sb.append(((LudemeNode)obj).toLud());
                        else
                            sb.append(obj).append(" ");
                    }
                }
                sb.append(" ").append(Preprocessing.COMPLETION_WILDCARD);
                // replace thisLud with sb
                return rootLud.replace(thisLud, sb.toString());
            }

            if(input == null)
                continue;
            sb.append(" ");
            if(arg.arg().label() != null)
                sb.append(arg.arg().label()).append(":");
            if(input instanceof LudemeNode)
                sb.append(((LudemeNode) input).toLud());
            else if(input instanceof Object[])
            {
                sb.append("{ ");
                for(Object obj : (Object[]) input)
                {
                    if(obj == null)
                        continue;
                    if(obj instanceof LudemeNode)
                        sb.append(((LudemeNode)obj).toLud());
                    else
                        sb.append(obj).append(" ");
                }
                sb.append("}");
            }
            else if(input instanceof String)
                sb.append("\"").append(input).append("\"");
            else
                sb.append(input);
        }
        sb.append(")");
        return rootLud.replace(thisLud, sb.toString());
    }

    private int numberOfMandatoryLudemeInputs()
    {
        int n = 0;
        for(NodeArgument arg : providedInputsMap().keySet())
        {
            if(!arg.optional() && !arg.isTerminal())
                n++;
        }
        return n;
    }

    /**
     * The title consists of the symbol and any Constants followed by the constructor
     * @return The title of this node
     */
    public String title()
    {
        if(selectedClause == null)
            return symbol().name();
        if(selectedClause.args() != null && selectedClause.args().isEmpty())
            return selectedClause.symbol().name();
        StringBuilder title = new StringBuilder(selectedClause().symbol().name());
        if(selectedClause().args() == null)
            return title.toString();
        // if selected clause starts with constants, add these to the title
        int index = 0;
        while(selectedClause().args().get(index).symbol().ludemeType().equals(Symbol.LudemeType.Constant))
        {
            title.append(" ").append(selectedClause().args().get(index).symbol().name());
            index++;
        }
        return title.toString();
    }


    private String tokenTitle()
    {
        if(selectedClause == null)
            return symbol().token();
        StringBuilder title = new StringBuilder(selectedClause().symbol().token());
        if(selectedClause().args() == null)
            return title.toString();
        if(selectedClause.args().size() == 0)
            return title.toString();
        // if selected clause starts with constants, add these to the title
        int index = 0;
        while(selectedClause().args().get(index).symbol().ludemeType().equals(Symbol.LudemeType.Constant))
        {
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
        copy.setSelectedClause(selectedClause());
        copy.setHeight(height());

        for(NodeArgument na : providedInputsMap.keySet())
        {
            Object input = providedInputsMap.get(na);
            if(input == null || input instanceof LudemeNode)
                continue; // if no input provided or it is LudemeNode, skip it
            boolean isLudemeCollection = false;
            if(input instanceof Object[])
                for (Object o : (Object[]) input)
                    if (o instanceof LudemeNode)
                    {
                        isLudemeCollection = true;
                        break;
                    }
            if(!isLudemeCollection)
                copy.setProvidedInput(na, input);
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
        if(helpInformation == null)
            return "";
        if(selectedClause == null)
            return "";
        if(selectedClause.symbol() == symbol())
            return helpInformation.description();
        else
            return DocumentationReader.instance().documentation().get(selectedClause.symbol()).description();
    }

    public String remark()
    {
        if(helpInformation == null)
            return "";
        if(selectedClause == null)
            return "";
        if(selectedClause.symbol() == symbol())
            return helpInformation.remark();
        else
            return DocumentationReader.instance().documentation().get(selectedClause.symbol()).remark();
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

    public DescriptionGraph defineGraph()
    {
        assert isDefineNode() || isDefineRoot();
        return defineGraph;
    }

    public boolean isDefineRoot()
    {
        return isDefineRoot;
    }

    public boolean isDefineNode()
    {
        return isDefineNode;
    }

    public LudemeNode macroNode()
    {
        return macroNode;
    }

    @Override
    public String toString(){
        return toLud();
    }



    //
    //    UNUSED DYNAMIC-CONSTRUCTOR VARIABLES & METHODS
    //

    /*

    // whether this node is dynamic or not.
    // a dynamic node has no pre-selected clause. by providing any arguments to the node, the list of
    // possible clauses is narrowed down to the ones that match the provided arguments.
    //
    private boolean dynamic = false;
    // Clauses that satisfy currently provided inputs
    private List<Clause> activeClauses = new ArrayList<>();
    // Clauses that do not satisfy currently provided inputs
    private List<Clause> inactiveClauses = new ArrayList<>();
    // NodeArguments that are currently provided
    private List<NodeArgument> providedNodeArguments = new ArrayList<>();
    // NodeArguments that can be provided to satisfy active clauses
    private List<NodeArgument> activeNodeArguments = new ArrayList<>();
    // NodeArguments that cannot be provided to satisfy active clauses
    private List<NodeArgument> inactiveNodeArguments = new ArrayList<>();




    //@return whether this node is dynamic
    public boolean dynamic()
    {
        return dynamic;
    }


    // @return The list of active clauses
    public List<Clause> activeClauses()
    {
        return activeClauses;
    }

    // @return The list of inactive clauses
    public List<Clause> inactiveClauses()
    {
        return inactiveClauses;
    }

    // @return The list of active Node Arguments
    public List<NodeArgument> activeNodeArguments()
    {
        return activeNodeArguments;
    }

    // @return The list of inactive Node Arguments
    public List<NodeArgument> inactiveNodeArguments()
    {
        return inactiveNodeArguments;
    }

    // @return The list of provided node arguments
    public List<NodeArgument> providedNodeArguments()
    {
        return providedNodeArguments;
    }


    //Sets this node to be dynamic or not
    //@param dynamic the dynamic to set
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


    IN CONSTRUCTOR:

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


    //@return whether it is possible to make this node dynamic
    public boolean dynamicPossible()
    {
        if(CLAUSES.size() == 1) return false;
        for(Clause clause : CLAUSES){
            if(clause.args() == null) continue;
            if(clause.args().size() > 1) return true;
        }
        return false;
    }

     */



}
