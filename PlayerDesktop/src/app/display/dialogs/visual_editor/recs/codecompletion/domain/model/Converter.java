package app.display.dialogs.visual_editor.recs.codecompletion.domain.model;

import app.display.dialogs.visual_editor.recs.utils.StringUtils;
import main.grammar.Description;
import main.grammar.Report;
import main.grammar.Token;
import main.options.UserSelections;

import java.util.ArrayList;

public class Converter {
    private static final String INDENT = " ";

    public static String toConstructor(final String desc) {
        Description test_desc = new Description(desc);
        parser.Parser.expandAndParse(test_desc, new UserSelections(new ArrayList<>()),new Report(),false);
        Token tokenTree = new Token(test_desc.expanded(), new Report());
        String converted = "("+tokenTree.name();
        System.out.println(converted);
        for(Token token : tokenTree.arguments()) {
            converted += " " + convertToken(token, 1);
        }
        converted += ")";

        //remove double spaces

        while (converted.contains("  ")) {
            converted = converted.replaceAll("  "," ");
        }

        return converted;
    }

    private static String convertToken(Token token, int depth) {
        String converted = "";
        if (token.isTerminal()) {
            String terminal = convertTerminalToConstructor(token);
            System.out.println(StringUtils.repeat(INDENT, depth) + terminal);
            converted += terminal;
        } else if(token.isClass()) {
            converted += "(" + token.name();
            System.out.println(StringUtils.repeat(INDENT, depth) + "(" +token.name());
            for(Token argument : token.arguments()) {
                converted += " " + convertToken(argument, depth + 1);
            }
            converted += " ) ";
        } else if(token.isArray()) {
            converted += "{";
            System.out.println(StringUtils.repeat(INDENT, depth) + token.name());
            for(Token argument : token.arguments()) {
                converted += convertToken(argument, depth + 1);
            }
            converted += "} ";
        }
        return converted;
    }

    private static String convertTerminalToConstructor(Token terminal) {

        String converted = "";

        if(isString(terminal)) {
            converted = "<string>";
        } else if(isInt(terminal)) {
            converted = "<int>";
        } else if(isFloat(terminal)) {
            converted = "<float>";
        } else if(isBoolean(terminal)) {
            converted = "<boolean>";
        } else if(isRange(terminal)) {
            converted = "<range>";
        } else {
            // must be a terminal ludeme like "None"
            converted = terminal.name();
        }
        return converted;
    }

    private static boolean isString(Token terminal) {
        String name = terminal.name();
        char[] charArray = name.toCharArray();
        if(charArray[0] == '"' && charArray[charArray.length - 1] == '"') {
            return true;
        }
        return false;
    }

    private static boolean isInt(Token terminal) {
        String name = terminal.name();
        char[] charArray = name.toCharArray();
        for(char c : charArray) {
            if(!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isFloat(Token terminal) {
        String name = terminal.name();
        char[] charArray = name.toCharArray();
        for(char c : charArray) {
            if(!Character.isDigit(c) || c == '.' || c == ',') {
                return false;
            }
        }
        return true;
    }

    private static boolean isRange(Token terminal) {
        String name = terminal.name();
        if(name.contains("..")) {
            return true;
        }
        return false;
    }

    private static boolean isBoolean(Token terminal) {
        String name = terminal.name();
        if(name.equals("True") || name.equals("False") || name.equals("true") || name.equals("false")) {
            return true;
        }
        return false;
    }
}
