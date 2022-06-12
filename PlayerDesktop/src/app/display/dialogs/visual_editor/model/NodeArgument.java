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
    /** The list of possible Symbols for this NodeArgument */
    private final List<Symbol> POSSIBLE_SYMBOL_INPUTS;
    /** The list of Symbols that may be provided as input for this NodeArgument
     *  Structural Symbols are not included in this list, but rather expanded to their rules.
     */
    private final List<Symbol> POSSIBLE_SYMBOL_INPUTS_EXPANDED;

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
        INDEX_FIRST = clause.args().indexOf(arg);
        INDEX_LAST = clause.args().indexOf(arg) + ARGS.size() - 1;

        this.POSSIBLE_SYMBOL_INPUTS = possibleSymbolInputs(args());

        System.out.println("Symbol: " + arg.symbol());
        System.out.println("POSSIBLE_SYMBOL_INPUTS: " + POSSIBLE_SYMBOL_INPUTS);

        this.POSSIBLE_SYMBOL_INPUTS_EXPANDED = possibleSymbolInputsExpanded(args());
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
    private List<Symbol> possibleSymbolInputsExpanded(List<Clause> visited, Clause clause){
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
        System.out.println("Expanding clause: " + clause);
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
        if(arg.symbol().ludemeType().equals(Symbol.LudemeType.Structural) && !isTerminal(arg.symbol()))
        {
            for(Clause clause : arg.symbol().rule().rhs())
            {
                System.out.println("Further expanding clause: " + clause);
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

    private boolean isTerminal(Symbol symbol)
    {
        if(symbol.isTerminal()) return true;
        for(Clause clause : symbol.rule().rhs())
        {
            if(!clause.symbol().ludemeType().equals(Symbol.LudemeType.Constant))
            {
                return false;
            }
        }
        return true;
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
