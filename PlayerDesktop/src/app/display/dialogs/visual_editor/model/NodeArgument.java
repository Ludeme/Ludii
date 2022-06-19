package app.display.dialogs.visual_editor.model;

import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the required Argument (ClauseArg) of a "row" in the node
 * If ClauseArg is part of a OR-Group, then this is a list, otherwise it just contains the ClauseArg.
 * LInputField is used to graphically represent NodeArgument
 * @author Filipp Dokienko
 */

public class NodeArgument
{
    /** The clause this NodeArgument is part of */
    private final Clause CLAUSE;
    /** The list of ClauseArgs this NodeArgument encompasses */
    private final List<ClauseArg> ARGS;
    /** The index of the first ClauseArg in the list */
    private final int INDEX_FIRST;
    /** The index of the last ClauseArg in the list */
    private final int INDEX_LAST;
    /** The list of all indices of all ClauseArgs of this NodeArgument */
    private List<Integer> INDICES;
    /** The list of possible Symbols for this NodeArgument */
    private final List<Symbol> POSSIBLE_SYMBOL_INPUTS;
    /** The list of Symbols that may be provided as input for this NodeArgument
     *  Structural Symbols are not included in this list, but rather expanded to their rules.
     */
    private final List<Symbol> POSSIBLE_SYMBOL_INPUTS_EXPANDED;
    /** If this is a Terminal NodeArgument, this indicates whether it should be displayed as a separate node */
    private boolean SEPARATE_NODE;

    /**
     * Constructor for NodeInput
     * @param clause the clause this NodeArgument is part of
     * @param arg the ClauseArg this NodeArgument represents
     */
    public NodeArgument(Clause clause, ClauseArg arg)
    {
        CLAUSE = clause;
        // add argument to list
        ARGS = new ArrayList<>();
        ARGS.add(arg);
        // if arg is part of OR-Group, add other components of OR-Group to it.
        if(arg.orGroup() != 0)
        {
            int group = arg.orGroup();
            int index = clause.args().indexOf(arg)+1;
            while(index < clause.args().size() && clause.args().get(index).orGroup() == group)
            {
                ARGS.add(clause.args().get(index));
                index++;
            }
        }
        if(clause.args() == null)
        {
            INDEX_FIRST = 0;
            INDEX_LAST = 0;
        }
        else
        {
            INDEX_FIRST = clause.args().indexOf(arg);
            INDEX_LAST = clause.args().indexOf(arg) + ARGS.size() - 1;
        }

        this.POSSIBLE_SYMBOL_INPUTS = possibleSymbolInputs(args());
        this.POSSIBLE_SYMBOL_INPUTS_EXPANDED = possibleSymbolInputsExpanded(args());
        SEPARATE_NODE = false;
    }

    /**
     * Constructor for NodeInput which is a Terminal
     * @param clause the clause this NodeArgument is part of
     */
    public NodeArgument(Clause clause)
    {
        CLAUSE = clause;
        // add argument to list
        ARGS = new ArrayList<>();
        ARGS.add(new ClauseArg(clause.symbol(), null, false, 0, 0));
        INDEX_FIRST = 0;
        INDEX_LAST = 0;
        POSSIBLE_SYMBOL_INPUTS = new ArrayList<>();
        POSSIBLE_SYMBOL_INPUTS_EXPANDED = new ArrayList<>();
        SEPARATE_NODE = true;
    }

    /**
     *
     * @param arguments the list of ClauseArgs this NodeArgument encompasses
     * @return list of Symbols that may be provided as input for this NodeArgument with Structural symbols expanded
     */
    private List<Symbol> possibleSymbolInputsExpanded(List<ClauseArg> arguments)
    {
        List<Symbol> possibleSymbolInputs = new ArrayList<>();
        List<Clause> visitedClauses = new ArrayList<>();
        for(ClauseArg arg : arguments)
        {
            if(arg.symbol().ludemeType().equals(Symbol.LudemeType.Constant)) continue;
            possibleSymbolInputs.addAll(possibleSymbolInputsExpanded(visitedClauses, arg));
        }
        return possibleSymbolInputs.stream().distinct().collect(Collectors.toList()); // return duplicates
    }

    /**
     * Finds all possible Symbols that may be provided as input for a given Clause if its Structural
     * @param visited the list of Clauses that have already been visited
     * @param clause the Clause to find possible Symbols for
     * @return list of Symbols that may be provided as input for the given Clause
     */
    private List<Symbol> possibleSymbolInputsExpanded(List<Clause> visited, Clause clause)
    {
        List<Symbol> possibleSymbolInputs = new ArrayList<>();
        // if clause is already visited, return empty list
        if(visited.contains(clause)) return possibleSymbolInputs;
        visited.add(clause);
        // ignore constant symbols
        if(clause.symbol().ludemeType().equals(Symbol.LudemeType.Constant)) return possibleSymbolInputs;
        // if clause is not structural, just return its symbol
        if(!clause.symbol().ludemeType().equals(Symbol.LudemeType.Structural))
        {
            possibleSymbolInputs.add(clause.symbol());
            return possibleSymbolInputs;
        }
        // if the clause has no constructor, expand to its own clauses
        if(clause.args() == null)
        {
            for(Clause clause2 : clause.symbol().rule().rhs())
            {
                possibleSymbolInputs.addAll(possibleSymbolInputsExpanded(visited, clause2));
            }
        }
        else
        {
            for(ClauseArg arg : clause.args())
            {
                possibleSymbolInputs.addAll(possibleSymbolInputsExpanded(visited, arg));
            }
        }
        return possibleSymbolInputs;
    }

    /**
     * Finds all possible Symbols that may be provided as input for a given Clause Argument
     * If an argument has no constructor, then it is expanded to its own possible clauses. However, each clause may only be visited/expanded once
     * @param visited list of Clauses that have already been visited to not visit them again
     * @param arg the ClauseArg to get possible Symbols for
     * @return list of Symbols that may be provided as input for a given Clause Argument
     */
    private List<Symbol> possibleSymbolInputsExpanded(List<Clause> visited, ClauseArg arg)
    {
        List<Symbol> possibleSymbolInputs = new ArrayList<>();
        // Ignore constant symbols
        if(arg.symbol().ludemeType().equals(Symbol.LudemeType.Constant)) return possibleSymbolInputs;
        if(arg.symbol().ludemeType().equals(Symbol.LudemeType.Primitive))
        {
            // TODO:
            System.out.println("Catched primitive symbol!");
            return possibleSymbolInputs;
        }
        //System.out.println("Expanding arg: " + arg);

        // If the argument has no rules, but another returnType, then expand it instead
        if(arg.symbol().rule().rhs().size() == 0 && arg.symbol().returnType() != arg.symbol())
        {
            ClauseArg arg2 = new ClauseArg(arg.symbol().returnType(), arg.label(), arg.optional(), arg.orGroup(), arg.andGroup());
            return possibleSymbolInputsExpanded(visited, arg2);
        }
        if(arg.symbol().ludemeType().equals(Symbol.LudemeType.Structural) && !isTerminal(arg.symbol()))
        {
            for(Clause clause : arg.symbol().rule().rhs())
            {
                if(clause.symbol().ludemeType().equals(Symbol.LudemeType.Structural)) {
                    possibleSymbolInputs.addAll(possibleSymbolInputsExpanded(visited, clause));
                }
                else
                {
                    possibleSymbolInputs.add(clause.symbol());
                }
            }
        }
        else
        {
            possibleSymbolInputs.add(arg.symbol());
        }
        return possibleSymbolInputs;
    }

    /**
     *
     * @return list of Symbols that may be provided as input for this NodeArgument
     */
    public List<Symbol> possibleSymbolInputsExpanded()
    {
        return POSSIBLE_SYMBOL_INPUTS_EXPANDED;
    }

    /**
     *
     * @param arguments the list of ClauseArgs this NodeArgument encompasses
     * @return list of Symbols that may be provided as input for this NodeArgument
     */
    private List<Symbol> possibleSymbolInputs(List<ClauseArg> arguments)
    {
        List<Symbol> possibleSymbolInputs = new ArrayList<>();
        for(ClauseArg arg : arguments)
        {
            if(!possibleSymbolInputs.contains(arg.symbol())) possibleSymbolInputs.add(arg.symbol());
        }
        return possibleSymbolInputs;
    }

    /**
     * TODO: Add comment
     * @param symbol
     * @return
     */
    private boolean isTerminal(Symbol symbol)
    {
        if(symbol.isTerminal()) return true;
        if(symbol.rule().rhs().size() == 0) return false; // TODO: check whether correct
        for(Clause clause : symbol.rule().rhs())
        {
            if(!clause.symbol().ludemeType().equals(Symbol.LudemeType.Constant))
            {
                return false;
            }
        }
        return true;
    }

    public boolean isTerminal()
    {
        if(separateNode()) return true;
        return isTerminal(arg().symbol());
    }

    public boolean terminalDropdown()
    {
        if(arg().symbol().rule() == null) return false;
        for(Clause clause : arg().symbol().rule().rhs())
        {
            if(!clause.symbol().ludemeType().equals(Symbol.LudemeType.Constant))
            {
                return false;
            }
        }
        return true;
    }

    public List<Symbol> constantInputs(){
        List<Symbol> constantInputs = new ArrayList<>();
        for(Clause clause : arg().symbol().rule().rhs())
        {
            if(clause.symbol().ludemeType().equals(Symbol.LudemeType.Constant))
            {
                constantInputs.add(clause.symbol());
            }
        }
        return constantInputs;
    }

    /**
     *
     * @return list of Symbols that may be provided as input for this NodeArgument
     */
    public List<Symbol> possibleSymbolInputs()
    {
        return POSSIBLE_SYMBOL_INPUTS;
    }

    /**
     *
     * @return The clause of this NodeArgument
     */
    public Clause clause()
    {
        return CLAUSE;
    }

    /**
     * @return the list of ClauseArgs this NodeInput encompasses
     */
    public List<ClauseArg> args()
    {
        return ARGS;
    }

    /**
     * @return the first ClauseArg this NodeInput encompasses
     */
    public ClauseArg arg()
    {
        if(ARGS.size() == 0) return null;
        return ARGS.get(0);
    }


    /**
     * @return Size of the list of ClauseArgs this NodeInput encompasses
     */
    public int size()
    {
        return ARGS.size();
    }

    /**
     *
     * @return the index of the first ClauseArg in the list
     */
    public int indexFirst()
    {
        return INDEX_FIRST;
    }

    /**
     *
     * @return the index of the last ClauseArg in the list
     */
    public int indexLast()
    {
        return INDEX_LAST;
    }

    /**
     *
     * @return whether this NodeArgument is optional
     */
    public boolean optional()
    {
        return arg().optional();
    }

    /**
     *
     * @return whether this NodeArgument is a collection
     */
    public boolean collection()
    {
        return arg().nesting() > 0;
    }

    /**
     *
     * @return whether this NodeArgument includes multiple ClauseArgs among which the user can choose
     */
    public boolean choice()
    {
        return size() > 1;
    }

    public void setSeparateNode(boolean separateNode) {
        this.SEPARATE_NODE = separateNode;
    }

    // TODO: Add comment
    public boolean separateNode()
    {
        return SEPARATE_NODE;
    }

    /**
     *
     * @return the ludemeType of the first ClauseArg in the list
     */
    public Symbol.LudemeType ludemeType()
    {
        return arg().symbol().ludemeType();
    }

    /**
     *
     * @param o
     * @return whether this NodeArgument is equal to another
     */
    @Override
    public boolean equals(Object o){
        if(o instanceof NodeArgument)
        {
            NodeArgument other = (NodeArgument) o;
            return other.clause().equals(clause()) && other.args().equals(args());
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "[ " + INDEX_FIRST + ", " + ARGS.stream().map(ClauseArg::toString).collect(Collectors.joining(", ")) + " ]";
    }

}
