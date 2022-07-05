package app.display.dialogs.visual_editor.documentation;

import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import java.util.HashMap;

public class HelpInformation
{

    private final Symbol symbol;
    private String description;
    private HashMap<Clause, String> ctor = new HashMap<>(); // syntax
    private HashMap<Clause, String> examples = new HashMap<>(); // examples
    private HashMap<ClauseArg, String> parameters = new HashMap<>(); // arguments
    private String remark;

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

    public String remark()
    {
        return remark;
    }

    public HashMap<Clause, String> ctors()
    {
        return ctor;
    }

    public HashMap<Clause, String> examples()
    {
        return examples;
    }

    public String parameter(ClauseArg arg)
    {
        return parameters.get(arg);
    }

    public HashMap<ClauseArg, String> parameters()
    {
        return parameters;
    }

    public void setCtor(HashMap<Clause, String> ctor)
    {
        this.ctor = ctor;
    }

    public void setExamples(HashMap<Clause, String> examples)
    {
        this.examples = examples;
    }

    public void setParameters(HashMap<ClauseArg, String> parameters)
    {
        this.parameters = parameters;
    }

}
