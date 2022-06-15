package app.display.dialogs.visual_editor.view;

import java.util.HashMap;

public class HelpInformation
{
    private String description;
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

    public String description()
    {
        return description;
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
