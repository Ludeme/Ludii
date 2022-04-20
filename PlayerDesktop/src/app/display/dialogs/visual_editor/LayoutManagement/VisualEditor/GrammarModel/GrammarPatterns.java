package app.display.dialogs.visual_editor.LayoutManagement.VisualEditor.GrammarModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrammarPatterns {

    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("// (.+?)\\n", Pattern.DOTALL);
        String grammar = "//------------------------------------------------------------------------------\n" +
                "// game\n" +
                "\n" +
                "<game>     ::= (game <string> <players> [<mode>] <equipment> <rules.rules>) | \n" +
                "               (game <string>) | <match>\n" +
                "\n";
        Matcher matcher = pattern.matcher(grammar);
        if(matcher.find())
        {
            System.out.println(matcher.group(1));
        }
        else
        {
            System.out.println("No match found");
        }

    }

}
