package app.display.dialogs.visual_editor.documentation;

import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import java.util.HashMap;

public class HelpInformation
{

    private final Symbol symbol;
    private String description;
    private final HashMap<Clause, String> ctor = new HashMap<>(); // syntax
    private final HashMap<Clause, String> examples = new HashMap<>(); // examples
    private final HashMap<ClauseArg, String> parameters = new HashMap<>(); // arguments

    public HelpInformation(Symbol symbol)
    {
        this.symbol = symbol;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void addCtor(Clause clause, String syntax)
    {
        ctor.put(clause, syntax);
    }

    public void addExample(Clause clause, String example)
    {
        examples.put(clause, example);
    }

    public void addParameter(ClauseArg arg, String description)
    {
        parameters.put(arg, description);
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public Symbol symbol()
    {
        return symbol;
    }

    public String description()
    {
        return description;
    }

    public String parameter(ClauseArg arg)
    {
        return parameters.get(arg);
    }

    public HashMap<ClauseArg, String> parameters()
    {
        return parameters;
    }

}
