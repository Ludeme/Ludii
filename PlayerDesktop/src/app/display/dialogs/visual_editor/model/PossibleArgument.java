package app.display.dialogs.visual_editor.model;

import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.List;

public class PossibleArgument
{
    private final Clause clause;

    private final List<PossibleArgument> possibleArguments = new ArrayList<>();

    public PossibleArgument(Clause clause)
    {
        this.clause = clause;
        computePossibleArguments();
    }

    private void computePossibleArguments()
    {
        if(clause.args() != null)
            return;

        Symbol symbol = clause.symbol();
        for(Clause c : symbol.rule().rhs())
        {
            possibleArguments.add(new PossibleArgument(c));
        }

    }

    public Clause clause()
    {
        return clause;
    }

    @Override
    public String toString()
    {
        return clause.toString();
    }



}
