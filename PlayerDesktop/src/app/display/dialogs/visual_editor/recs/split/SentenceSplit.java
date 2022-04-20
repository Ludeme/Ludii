package app.display.dialogs.visual_editor.recs.split;

import java.util.ArrayList;
import java.util.List;

public final class SentenceSplit {
    private final static boolean GENERIC = true;
    private final static boolean DEBUG = false;
    private final static String TAB = "\t";
    /**
     * This method will split a text of natural language into its words and
     * symbols.
     * It will split at all spaces and punctuation.
     */
    public static List<String> splitText(String text) {
        List<String> split = new ArrayList<>();
        String currentWord = "";
        //used to allow spaces in strings, so if the program 'enters' a string, it will not split on spaces
        boolean inString = false;
        for(int i = 0; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            switch(currentChar) {
                case '"':
                    if(inString) {
                        inString = false;
                        currentWord += currentChar;
                        if(GENERIC) {
                            split.add("STRING");
                        } else {
                            split.add(currentWord);
                        }
                        currentWord = "";
                    } else {
                        inString = true;
                        currentWord += currentChar;
                    }
                    break;
                case ' ':
                    if(!currentWord.equals("") && !inString) {
                        split.add(currentWord);
                        currentWord = "";
                    } else if(inString){
                        currentWord += currentChar;
                    }
                    break;
                case ')':
                case ',':
                case '.':
                case '!':
                case '?':
                case ';':
                case '{':
                case '}':
                case '[':
                case ']':
                    if(!inString) {
                        if(!currentWord.equals(""))split.add(currentWord);
                        currentWord = "";
                        String symbol = ""+currentChar;
                        split.add(symbol);
                    } else {
                        currentWord += currentChar;
                    }
                    break;
                case '\t':
                case '\n':break;
                default:
                    currentWord += currentChar;
                    break;
            }
            System.currentTimeMillis();
            if(currentWord.equals(" )")) {
                if(DEBUG)System.out.println("ERRORR---------------------");
            }
        }
        if(!currentWord.equals(""))split.add(currentWord);
        return split;
    }
}
