package app.display.dialogs.visual_editor.model;

import app.display.dialogs.visual_editor.documentation.DocumentationReader;
import app.display.dialogs.visual_editor.documentation.HelpInformation;
import grammar.Grammar;
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
    private final List<ClauseArg> args;
    /** Arguments prior to expanding */
    public final List<ClauseArg> originalArgs = new ArrayList<>();
    /** The index of the ClauseArg in the Clause */
    private final int INDEX;
    /** List of possible Arguments as Input */
    private final List<PossibleArgument> possibleArguments;
    /** The list of Symbols that may be provided as input for this NodeArgument
     *  Structural Symbols are not included in this list, but rather expanded to their rules.
     */
    private final List<Symbol> possibleSymbolInputs;
    private String parameterDescription = null;

    /**
     * Constructor for NodeInput
     * @param clause the clause this NodeArgument is part of
     * @param arg the ClauseArg this NodeArgument represents
     */
    public NodeArgument(Clause clause, ClauseArg arg)
    {
        this.CLAUSE = clause;
        this.INDEX = clause.args().indexOf(arg);

        // add argument to list
        this.args = new ArrayList<>();
        this.args.add(arg);
        // if arg is part of OR-Group, add other components of OR-Group to it.
        if(arg.orGroup() != 0)
        {
            int group = arg.orGroup();
            int index = clause.args().indexOf(arg)+1;
            while(index < clause.args().size() && clause.args().get(index).orGroup() == group)
            {
                this.args.add(clause.args().get(index));
                index++;
            }
        }

        for(ClauseArg c : new ArrayList<>(this.args))
            if(c.symbol().returnType() != c.symbol())
                for(String[] f : Grammar.grammar().getFunctions())
                    if(f[0].equals(c.symbol().name()))
                    {
                        // find the symbol that matches f[1]
                        List<Symbol> candidates = new ArrayList<>();
                        if(Grammar.grammar().symbolsWithPartialKeyword(f[1]) != null) candidates.addAll(Grammar.grammar().symbolsWithPartialKeyword(f[1]));
                        if(Grammar.grammar().symbolsWithPartialKeyword(f[1].substring(0, f[1].length()-1)) != null) candidates.addAll(Grammar.grammar().symbolsWithPartialKeyword(f[1].substring(0, f[1].length()-1)));
                        Symbol match = null;
                        for(Symbol s : candidates)
                            if(s.grammarLabel().equals(f[1]) && s.rule().rhs().size() > 0)
                            {
                                match = s;
                                break;
                            }
                        if(match == null) match = c.symbol().returnType();
                        int nesting = c.nesting();
                        ClauseArg newArg = new ClauseArg(match, c.actualParameterName(), c.label(), c.optional(), c.orGroup(), c.andGroup());
                        newArg.setNesting(nesting);
                        args.remove(c);
                        args.add(newArg);
                        break;
                    }

        originalArgs.addAll(this.args);

        // if contains a choice between a 1d collection or not, keep the 1d collection and remove the other
        if(this.args.size() == 2)
            if(args.get(0).symbol().equals(args.get(1).symbol()))
            {
                if(args.get(0).nesting() == 0 && args.get(1).nesting() == 1)
                    this.args.remove(0);
                else if(args.get(0).nesting() == 1 && args.get(1).nesting() == 0)
                    this.args.remove(1);
            }
        else if(this.args.size() == 3)
        {
            ClauseArg arg1 = args.get(0);
            ClauseArg arg2 = args.get(1);
            ClauseArg arg3 = args.get(2);
            if(arg1.symbol().equals(arg2.symbol()) && arg1.symbol().equals(arg3.symbol()))
            {
                if(arg1.nesting() == 0 && arg2.nesting() == 1 && arg3.nesting() == 2)
                    this.args.remove(arg1);
                else if(arg1.nesting() == 1 && arg2.nesting() == 2 && arg3.nesting() == 0)
                    this.args.remove(arg3);
                else if(arg1.nesting() == 2 && arg2.nesting() == 0 && arg3.nesting() == 1)
                    this.args.remove(arg2);
            }
        }


        this.possibleArguments = new ArrayList<>();
        for(ClauseArg ca : args)
            computePossibleArguments(ca);

        Set<Symbol> possibleArguments = new HashSet<>();
        for(PossibleArgument pa : this.possibleArguments)
            possibleArguments.addAll(expandPossibleArgument(pa));
        for(ClauseArg a : args())
            if(terminalDropdown(a))
                possibleArguments.add(a.symbol());

        Set<Symbol> possibleSymbols = new HashSet<>();
        for(Symbol s : possibleArguments)
            if(!s.ludemeType().equals(Symbol.LudemeType.Constant))
                possibleSymbols.add(s);

        possibleSymbolInputs = new ArrayList<>(possibleSymbols);
        parameterDescription = readHelp();
    }

    /**
     * Constructor for PreDefined Arguments
     * @param clause the clause this NodeArgument is part of
     */
    public NodeArgument(Clause clause)
    {
        CLAUSE = clause;
        // add argument to list
        args = new ArrayList<>();
        args.add(new ClauseArg(clause.symbol(), null, null, false, 0, 0));
        args.get(0).setNesting(clause.args().get(0).nesting());
        INDEX = 0;
        this.possibleArguments = new ArrayList<>();
        possibleSymbolInputs = new ArrayList<>();
    }

    /**
     * Creates a NodeArgument with a given list of possible symbol inputs
     * Used for Define nodes
     */
    public NodeArgument(Symbol symbol, Clause clause, List<Symbol> viableInputs)
    {
        CLAUSE = clause;
        args = new ArrayList<>();
        INDEX = 1;

        ClauseArg ca = new ClauseArg(symbol, "Macro", null, false, 0, 0);
        args.add(ca);

        this.possibleArguments = new ArrayList<>();
        for(Symbol s : viableInputs)
            computePossibleArguments(s);

        Set<Symbol> possibleArguments = new HashSet<>();
        for(PossibleArgument pa : this.possibleArguments) possibleArguments.addAll(expandPossibleArgument(pa));
        for(ClauseArg a : args()) if(terminalDropdown(a)) possibleArguments.add(a.symbol());

        Set<Symbol> possibleSymbols = new HashSet<>();
        for(Symbol s : possibleArguments)
        {
            if(s.ludemeType().equals(Symbol.LudemeType.Constant)) continue;
            possibleSymbols.add(s);
        }
        this.possibleSymbolInputs = new ArrayList<>(possibleSymbols);
    }

    private NodeArgument create1DEquivalent(ClauseArg ca)
    {
        ClauseArg ca1d = new ClauseArg(ca.symbol(), ca.actualParameterName(), ca.label(), false, 0, 0);
        ca1d.setNesting(1);
        return new NodeArgument(this.CLAUSE, ca1d);
    }

    public NodeArgument collection1DEquivalent(ClauseArg arg)
    {
        return create1DEquivalent(arg);
    }

    private Set<Symbol> expandPossibleArgument(PossibleArgument pa)
    {
        Set<Symbol> symbols = new HashSet<>();
        if(pa.possibleArguments().size() == 0)
            symbols.add(pa.clause().symbol());
        else
            for(PossibleArgument p : pa.possibleArguments())
                symbols.addAll(expandPossibleArgument(p));

        return symbols;
    }

    private void computePossibleArguments(ClauseArg arg)
    {
        if(arg.symbol() == null)
            System.out.println();
        if(arg.symbol().rule() == null)
            return;
        for(Clause c : arg.symbol().rule().rhs())
            possibleArguments.add(new PossibleArgument(c));
    }

    private void computePossibleArguments(Symbol s)
    {
        if(s.rule() == null)
            return;
        for(Clause c : s.rule().rhs())
            possibleArguments.add(new PossibleArgument(c));
    }

    /**
     *
     * @return list of Symbols that may be provided as input for this NodeArgument
     */
    public List<Symbol> possibleSymbolInputsExpanded()
    {
        return possibleSymbolInputs;
    }


    private boolean terminalDropdown(ClauseArg arg)
    {
        if(arg.symbol().rule() == null) return false;
        if(arg.symbol().rule().rhs().size() == 0) return false;
        for(Clause clause : arg.symbol().rule().rhs())
            if(!clause.symbol().ludemeType().equals(Symbol.LudemeType.Constant))
                return false;
        return true;
    }

    public boolean isTerminal()
    {
        if(arg().symbol().ludemeType().equals(Symbol.LudemeType.Predefined))
            return true;
        else
            return terminalDropdown();
    }

    public boolean terminalDropdown()
    {
        return terminalDropdown(arg());
    }

    public boolean canBePredefined()
    {
        Symbol s = arg().symbol();
        if(s.rule() == null || s.rule().rhs().size() == 0)
            return false;
        for(Clause clause : s.rule().rhs())
        {
            if(clause.args() != null && clause.args().size() > 0)
                continue;
            if(clause.args() == null && clause.symbol().ludemeType().equals(Symbol.LudemeType.Primitive))
                return true;
        }
        return false;
    }

    public List<Symbol> constantInputs()
    {
        List<Symbol> constantInputs = new ArrayList<>();
        for(Clause clause : arg().symbol().rule().rhs())
            if(clause.symbol().ludemeType().equals(Symbol.LudemeType.Constant))
                constantInputs.add(clause.symbol());
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
        return args;
    }

    /**
     * @return the first ClauseArg this NodeInput encompasses
     */
    public ClauseArg arg()
    {
        if(args.size() == 0)
            return null;
        return args.get(0);
    }


    /**
     * @return Size of the list of ClauseArgs this NodeInput encompasses
     */
    public int size()
    {
        return args.size();
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
        if(arg() == null)
            return false;
        return arg().optional();
    }

    /**
     *
     * @return whether this NodeArgument is a collection
     */
    public boolean collection()
    {
        if(arg() == null)
            return false;
        return arg().nesting() > 0;
    }

    /**
     *
     * @return whether this NodeArgument is a 2D-Collection
     */
    public boolean collection2D()
    {
        if(arg() == null)
            return false;
        return arg().nesting() == 2;
    }

    /**
     *
     * @return whether this NodeArgument includes multiple ClauseArgs among which the user can choose
     */
    public boolean choice()
    {
        return size() > 1;
    }

    public void setActiveChoiceArg(ClauseArg activeArg)
    {
        ClauseArg temp = args.get(0);
        int index = args.indexOf(activeArg);
        args.set(0, activeArg);
        args.set(index, temp);
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
        if(hi == null || hi.parameter(arg()) == null)
            return null;
        return hi.parameter(arg());
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
        return "[ " + INDEX + ", " + args.stream().map(ClauseArg::toString).collect(Collectors.joining(", ")) + " ]";
    }

}
