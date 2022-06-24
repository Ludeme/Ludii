package app.display.dialogs.visual_editor.view;

import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import java.util.HashMap;

public class HelpInformation
{

    private Symbol symbol;
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

    public String ctor(Clause clause)
    {
        return ctor.get(clause);
    }

    public HashMap<Clause, String> ctors()
    {
        return ctor;
    }

    public String example(Clause clause)
    {
        return examples.get(clause);
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




    private String usage;
    private HashMap<String, String> parameter_description;
    private String[] example;




    public HelpInformation(String description, String usage, HashMap<String, String> parameter_description, String[] example)
    {
        this.description = description;
        this.usage = usage;
        this.parameter_description = parameter_description;
        this.example = example;
    }


    public String usage()
    {
        return usage;
    }

    public HashMap<String, String> parameter_description()
    {
        return parameter_description;
    }

    public String[] example()
    {
        return example;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Description: " + description + "\n");
        sb.append("Usage: " + usage + "\n");
        sb.append("Parameters: \n");
        for (String key : parameter_description.keySet())
        {
            sb.append("\t" + key + ": " + parameter_description.get(key) + "\n");
        }
        sb.append("Example: \n");
        for (String example : example)
        {
            sb.append("\t" + example + "\n");
        }
        return sb.toString();
    }

    public String toHTML()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<h3>Description</h3>");
        sb.append(description);
        sb.append("<h3>Usage</h3>");
        sb.append(usage.replace("<", "&lt;").replace(">", "&gt;"));
        sb.append("<h3>Parameters</h3>");
        sb.append("<ul>");
        for (String key : parameter_description.keySet())
        {
            String value = parameter_description.get(key).replace("<", "&lt;").replace(">", "&gt;");
            sb.append("<li>" + key.replace("<", "&lt;").replace(">", "&gt;") + ": " + value + "</li>");
        }
        sb.append("</ul>");
        sb.append("<h3>Example</h3>");
        sb.append("<ul>");
        for (String example : example)
        {
            String value = example.replace("<", "&lt;").replace(">", "&gt;");
            sb.append("<li>" + value + "</li>");
        }
        sb.append("</ul>");
        sb.append("</html>");
        return sb.toString();
    }

}
