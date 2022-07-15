package app.display.dialogs.visual_editor.model;

import app.display.dialogs.visual_editor.documentation.DocumentationReader;
import app.display.dialogs.visual_editor.documentation.HelpInformation;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    /** The index of the ClauseArg in the Clause */
    private final int INDEX;
    /** List of possible Arguments as Input */
    private final List<PossibleArgument> POSSIBLE_ARGS;
    /** The list of Symbols that may be provided as input for this NodeArgument
     *  Structural Symbols are not included in this list, but rather expanded to their rules.
     */
    private final List<Symbol> POSSIBLE_SYMBOL_INPUTS;
    /** If this is a Terminal NodeArgument, this indicates whether it should be displayed as a separate node */
    private boolean SEPARATE_NODE;
    private String parameterDescription = null;

    public final ClauseArg originalArg;
    public final List<ClauseArg> originalArgs = new ArrayList<>();

    /**
     * Constructor for NodeInput
     * @param clause the clause this NodeArgument is part of
     * @param arg the ClauseArg this NodeArgument represents
     */
    public NodeArgument(Clause clause, ClauseArg arg)
    {
        this.originalArg = arg;
        this.CLAUSE = clause;
        this.INDEX = clause.args().indexOf(arg);

        if(arg.symbol().returnType() != arg.symbol()) arg = new ClauseArg(arg.symbol().returnType(), arg.actualParameterName(), arg.label(), arg.optional(), arg.orGroup(), arg.andGroup());


        // add argument to list
        this.ARGS = new ArrayList<>();
        this.ARGS.add(arg);
        // if arg is part of OR-Group, add other components of OR-Group to it.
        if(arg.orGroup() != 0)
        {
            int group = arg.orGroup();
            int index = clause.args().indexOf(arg)+1;
            while(index < clause.args().size() && clause.args().get(index).orGroup() == group)
            {
                this.ARGS.add(clause.args().get(index));
                index++;
            }
        }

        originalArgs.addAll(this.ARGS);

        // if this is a choice between collection or not, default to collection
        if(this.ARGS.size() == 2)
        {
            if(this.ARGS.get(0).symbol().equals(this.ARGS.get(1).symbol()))
            {
                // get index of non-collection
                int index = 0;
                if(this.ARGS.get(0).andGroup()==0) index = 1;
                this.ARGS.remove(index);
            }
        }

        this.POSSIBLE_ARGS = new ArrayList<>();
        for(ClauseArg ca : ARGS)
            computePossibleArguments(ca);

        Set<Symbol> possibleArguments = new HashSet<>();
        for(PossibleArgument pa : POSSIBLE_ARGS) possibleArguments.addAll(expandPossibleArgument(pa));


        Set<Symbol> possibleSymbols = new HashSet<>();
        for(Symbol s : possibleArguments)
        {
            if(s.ludemeType().equals(Symbol.LudemeType.Constant)) continue;
            possibleSymbols.add(s);
        }

        POSSIBLE_SYMBOL_INPUTS = new ArrayList<>(possibleSymbols);

        SEPARATE_NODE = false;

        parameterDescription = readHelp();
    }

    private Set<Symbol> expandPossibleArgument(PossibleArgument PA)
    {
        Set<Symbol> symbols = new HashSet<>();

        if(PA.possibleArguments().size() == 0) symbols.add(PA.clause().symbol());
        else
        {
            for(PossibleArgument p : PA.possibleArguments())
            {
                symbols.addAll(expandPossibleArgument(p));
            }
        }

        return symbols;
    }

    private void computePossibleArguments(ClauseArg arg)
    {
        if(arg.symbol().rule() == null)
        {
            return;
        }
        for(Clause c : arg.symbol().rule().rhs()) {
            POSSIBLE_ARGS.add(new PossibleArgument(c));
        }
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
        ARGS.add(new ClauseArg(clause.symbol(), null, null, false, 0, 0));
        originalArg = null;
        INDEX = 0;
        this.POSSIBLE_ARGS = new ArrayList<>();
        POSSIBLE_SYMBOL_INPUTS = new ArrayList<>();
        SEPARATE_NODE = true;
    }

    /**
     *
     * @return list of Symbols that may be provided as input for this NodeArgument
     */
    public List<Symbol> possibleSymbolInputsExpanded()
    {
        return POSSIBLE_SYMBOL_INPUTS;
    }

    /**
     * TODO: Add comment
     * @param symbol
     * @return
     */
    private boolean isTerminal(Symbol symbol)
    {
        // TODO [FLAG]    Added two lines below
        if(symbol.ludemeType().equals(Symbol.LudemeType.Predefined)) return true;
        else if(true) return false;
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
     * @return the index of the ClauseArg in the clause
     */
    public int index()
    {
        return INDEX;
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

    public void setActiveChoiceArg(ClauseArg activeArg)
    {
        ClauseArg temp = ARGS.get(0);
        int index = ARGS.indexOf(activeArg);
        ARGS.set(0, activeArg);
        ARGS.set(index, temp);
        parameterDescription = readHelp();
    }

    public String parameterDescription()
    {
        return parameterDescription;
    }

    private String readHelp()
    {
        DocumentationReader dr = DocumentationReader.instance();
        HelpInformation hi = dr.documentation().get(CLAUSE.symbol());
        if(hi.parameter(arg()) == null) return null;
        String help = hi.parameter(arg());
        return help;
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
        return "[ " + INDEX + ", " + ARGS.stream().map(ClauseArg::toString).collect(Collectors.joining(", ")) + " ]";
    }

}
