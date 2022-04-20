package app.display.dialogs.visual_editor.model.grammar.parser;

import app.display.dialogs.visual_editor.model.grammar.Constructor;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.input.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Parser {


    private ArrayList<Ludeme> ludemes = new ArrayList<>(); // TODO refactor into HashMap !!! This way we'll save on runtime
    private final List<Grammar> GRAMMAR = createGrammar();
    private final boolean DEBUG = false;
    private final String GRAMMAR_CHARACTERS = "[]<>(){}|,:"; //List.of('[',']','<','>','{','}','|',':','(',')');
    private final String CAPITAL_NON_GRAMMAR_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final String LOWER_NON_GRAMMAR_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    

    public Parser(){
        // first parse all grammars which consist of Terminals only
        for(Grammar g : GRAMMAR){
            boolean onlyTerminals = true;
            for(String constructor : g.constructors){

                if(!onlyTerminals) break;

                if(CAPITAL_NON_GRAMMAR_LETTERS.indexOf(constructor.charAt(0)) == -1){
                    onlyTerminals = false;
                    continue;
                }
                for(char grammarChar: GRAMMAR_CHARACTERS.toCharArray()){
                    if(constructor.indexOf(grammarChar) != -1){
                        onlyTerminals = false;
                        break;
                    }
                }
            }
            if(onlyTerminals){
                createTerminalOnlyLudeme(g);
                terminalLudmes.add(g.NAME);
            }
        }
        for(Grammar g : GRAMMAR){
            if(terminalLudmes.contains(g.NAME)) continue;
            if(g.NAME.equals("automove") || g.NAME.equals("gravity") || g.NAME.equals("meta.swap") || g.NAME.equals("pin") || g.NAME.equals("split") || g.NAME.equals("was") || g.NAME.equals("is")) continue;
            if(nameExists(g.NAME) == null) {
                if(DEBUG) if (g.NAME.equals("string")) System.out.println("[!!!] String found");
                createLudeme(g.NAME);
            } else {
                finishLudeme(nameExists(g.NAME),g.constructors);
            }
        }
    }


    private Ludeme nameExists(String name){
        for(Ludeme l : ludemes){
            if(l.NAME.equals(name)) return l;
        }
        return null;
    }

    private void finishLudeme(Ludeme l, List<String> constructors){
        for(String c : constructors){
            l.addConstructor(readConstructorString(l.NAME,c));
        }
    }

    private Ludeme createLudeme(String name){
        // get constructors
        List<String> constructorStrings = null;
        for(Grammar g : GRAMMAR){
            if(g.NAME.equals(name)){
                constructorStrings = g.constructors;
                break;
            }
        }
        if(constructorStrings == null) {
            if(DEBUG) System.out.println("EROOR: couldnt find model.grammar with name " + name);
            return null;
        }

        // find ludeme or create
        Ludeme lb = nameExists(name);
        if(lb == null){
            lb = new Ludeme(name);
            addLudeme(lb);
        }

        ArrayList<Constructor> constructors = new ArrayList<>();
        for(String constructorString : constructorStrings){
            csStack.add(constructorString);
            lb.addConstructor(readConstructorString(name, constructorString));
            csStack.remove(constructorString);
        }
        return lb;
    }


    private List<Grammar> createGrammar(){
        List<Grammar> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("resources/easy.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if(records.size() > 0){
                    Grammar previous_grammar = records.get(records.size()-1);
                    String current_name = values[0].substring(1,values[0].length()-1);
                    if(previous_grammar.NAME.equals(current_name)){
                        if(values[1].startsWith("(")){
                            previous_grammar.addConstructor(values[1].substring(1,values[1].length()-1));
                        }
                        else {
                            previous_grammar.addConstructor(values[1]);
                        }
                    }
                    else{
                        Grammar new_grammar = new Grammar(current_name);
                        if(values[1].startsWith("(")){
                            new_grammar.addConstructor(values[1].substring(1,values[1].length()-1));
                        }
                        else {
                            new_grammar.addConstructor(values[1]);
                        }
                        records.add(new_grammar);
                    }
                }
                else {
                    Grammar new_grammar = new Grammar(values[0].substring(1,values[0].length()-1));

                    if(values[1].startsWith("(")){
                        new_grammar.addConstructor(values[1].substring(1,values[1].length()-1));
                    }
                    else {
                        new_grammar.addConstructor(values[1]);
                    }
                    records.add(new_grammar);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        records.remove(0); // remove header
        return records;
    }

    public List<Grammar> getGRAMMAR() {
        return GRAMMAR;
    }

    private List<Ludeme> actualLudems = new ArrayList<>();


    public List<Ludeme> getLudemes(){
        return ludemes;
    }

    private List<String> nameStack = new ArrayList<>();
    private List<String> csStack = new ArrayList<>();

    private Ludeme getLudeme(String name){
        for(Ludeme l : ludemes){
            if(l.getName().equals(name)) return l;
        }
        // does not exist -> create
        if(DEBUG) System.out.println("couldnt find " + name + "; create!");
        Ludeme l = new Ludeme(name);
        addLudeme(l);
        return l;
    }

    private List<String> terminalLudmes = new ArrayList<>();

    private Ludeme createTerminalOnlyLudeme(Grammar g){
        ArrayList<Terminal> terminals = new ArrayList<>();
        for(String c : g.constructors) terminals.add(new Terminal(c));
        TerminalInput input = new TerminalInput(g.NAME, TerminalInputType.DROPDOWN, terminals);
        Ludeme l = new Ludeme(g.NAME,List.of(new Constructor(List.of(input))));
        addLudeme(l);
        return l;
    }

    private void addLudeme(Ludeme l){
        if(!ludemes.contains(l)) {
            if(DEBUG)  System.out.println("[+] ADDING: " + l.getName());
            ludemes.add(l);
        }
    }


    private Constructor readConstructorString(String name, String constructorString){
        if(DEBUG) System.out.println("Reading " + constructorString);
        String fullConstructorString = constructorString;

        // if constructor string contains name, trim it
        boolean containsName = constructorStringStartsWithLudemeName(name, constructorString);
        if(!name.equals("string")  && !name.equals("int") && containsName) {
            if(DEBUG) System.out.println("[!] contains name: " + name + ", " + constructorString);
            constructorString = constructorString.substring(constructorString.indexOf(" ")+1);
        }

        // if contained name and next word starts with capital letter, it identifies the constructor
        String constructorName = null;
        while(containsName && CAPITAL_NON_GRAMMAR_LETTERS.indexOf(constructorString.charAt(0)) != -1){
            constructorName = constructorString.substring(0,constructorString.indexOf(" "));
            constructorString = constructorString.substring(constructorString.indexOf(" ")+1);
        }

        // read inputs
        ArrayList<Input> inputs = readInputs(constructorString);

        Constructor constructor = new Constructor(inputs, fullConstructorString);
        if(constructorName != null) constructor.setName(constructorName);

        return constructor;
    }


    private ArrayList<Input> readInputs(String constructorString){
        if(DEBUG)  System.out.println("working on constructor string: " + constructorString);
        ArrayList<Input> inputs = new ArrayList<>();
        // while constructorString is not empty, it contains inputs
        // TODO
        constructorString = constructorString.replaceAll("\\{\\{","{");
        constructorString = constructorString.replaceAll("\\}\\}","}");
        while(constructorString.length() > 0){
            if(DEBUG)  System.out.println("    -     " + constructorString);


            if(constructorString.startsWith("int")){
                if(DEBUG)  System.out.println("[INT]");
                inputs.add(readInput(constructorString));
                constructorString = constructorString.replaceFirst("int","");
                continue;
            }
            if(constructorString.startsWith("string")){
                if(DEBUG) System.out.println("[STRING]");
                inputs.add(readInput(constructorString));
                constructorString = constructorString.replaceFirst("string","");
                continue;
            }
            if(!constructorString.startsWith("{") && !constructorString.startsWith("(") && !constructorString.startsWith("[") && !constructorString.startsWith("<")){
                // TODO
                constructorString = constructorString.substring(constructorString.indexOf(":")+1);
            }

            switch (constructorString.charAt(0)) {
                case '{': {
                    inputs.add(readInput(constructorString));
                    constructorString = constructorString.substring(constructorString.indexOf('}') + 1);
                    //if(constructorString.length() > 0 && constructorString.charAt(0) == '}') constructorString = constructorString.substring(1);
                    break;
                }
                case '<': {
                    inputs.add(readInput(constructorString));
                    if(DEBUG) System.out.print("trimmed from " + constructorString + " to ");
                    int endIndex = constructorString.indexOf('>');

                    if(constructorString.length() > endIndex+1 && constructorString.charAt(endIndex+1) == '>'){
                        endIndex++;
                    }
                    if(endIndex == 1){
                        endIndex = constructorString.substring(endIndex+1).indexOf(">")+1+endIndex;
                    }
                    constructorString = constructorString.substring(endIndex+1);

                    if(DEBUG) System.out.println(constructorString);
                    break;
                }
                case '[': {
                    String inputString = constructorString.substring(1,constructorString.indexOf(']'));

                    if(inputString.contains("|")){ // then it's a choice
                        String[] inputStrings = inputString.split("\\|");
                        ArrayList<Input> currentInputs = new ArrayList<>();
                        for(String is : inputStrings){
                            currentInputs.add(readInput(is));
                        }
                        Input input = new ChoiceInput("Choice",currentInputs);
                        input.setOptional(true);
                        inputs.add(input);
                    }
                    else {
                        inputString = inputString.trim();
                        Input input = readInput(inputString);
                        input.setOptional(true);
                        inputs.add(input);
                    }
                    constructorString = constructorString.substring(constructorString.indexOf(']') + 1);
                    break;
                }
                case '(': {
                    String inputString = constructorString.substring(1,constructorString.indexOf(')'));
                    String[] inputStrings = inputString.split("\\|");
                    ArrayList<Input> currentInputs = new ArrayList<>();
                    for(String is : inputStrings){
                        currentInputs.add(readInput(is));
                    }
                    Input input = new ChoiceInput("choice (?)",currentInputs);
                    inputs.add(input);
                    constructorString = constructorString.substring(constructorString.indexOf(')') + 1);
                    break;
                }
            }
            constructorString = constructorString.trim();
        }

        return inputs;
    }


    // either ludeme or terminal. can be collection
    private Input readInput(String inputString){
        inputString = inputString.trim();
        int endIndex;

        if(inputString.startsWith("int")){
            return new TerminalInput("int", TerminalInputType.INTEGER);
        }
        if(inputString.startsWith("string")){
            return new TerminalInput("string", TerminalInputType.STRING);
        }

        String inputName = null;
        if(inputString.charAt(0) != '<' && inputString.charAt(0) != '{'){
            inputName = hasInputName(inputString.substring(0));
            if(inputName!=null) inputString = inputString.replaceFirst(inputName+":", "");
        }
        else {
            inputName = hasInputName(inputString.substring(1));
            if(inputName!=null) inputString = inputString.replaceFirst(inputName+":", "");
        }


        switch (inputString.charAt(0)) {
            case '<': {
                endIndex = inputString.indexOf('>');

                if(inputString.charAt(1) == '>'){
                    endIndex = inputString.substring(endIndex+1).indexOf(">")+endIndex+1;
                }
                if(inputString.length() > endIndex+1 && inputString.charAt(endIndex+1)=='>'){
                    endIndex+=1;
                }
                if (endIndex == -1) {
                    if(DEBUG) System.out.println("ERROR, endIndex = -1");
                }
                String ludemeName = inputString.substring(1, endIndex);
                Input input = new LudemeInput(ludemeName, getLudeme(ludemeName));
                if(inputName != null){
                    input = new LudemeInput(inputName, getLudeme(ludemeName));
                }
                return input;
            }

            case '{': {
                if(inputString.contains("{{")) inputString = inputString.replaceFirst("\\{\\{","{");
                // then next char must be '>' or i (for int)
                char nextChar = inputString.charAt(1);
                if (nextChar == '<') {
                    // is ludeme
                    endIndex = inputString.indexOf('>');
                    if(inputString.charAt(1) == '>'){
                        endIndex = inputString.substring(endIndex+1).indexOf(">")+endIndex+1;
                    }
                    if(inputString.length() > endIndex+1 && inputString.charAt(endIndex+1)=='>'){
                        endIndex+=1;
                    }
                    if (endIndex == -1) {
                        if(DEBUG) System.out.println("ERROR, endIndex = -1");
                    }
                    if(DEBUG) if (endIndex == -1) System.out.println("ERROR, endIndex = -1");
                    String ludemeName = inputString.substring(2, endIndex);
                    LudemeInput input = new LudemeInput(ludemeName, getLudeme(ludemeName));
                    if(inputName != null){
                        input = new LudemeInput(inputName, getLudeme(ludemeName));
                    }
                    input.setCollection(true);
                    return input;
                } else if (nextChar == 'i' || nextChar == 'I') {

                    Input input = new TerminalInput("int", TerminalInputType.INTEGER);
                    if(inputName != null){
                        input = new TerminalInput(inputName, TerminalInputType.INTEGER);
                    }

                    input.setCollection(true);
                    return input;
                }
                break;
            }
        }
        if(DEBUG) System.out.println("error: no input found, " + inputString);
        return null;
    }

    private String hasInputName(String s){
        String inputName = null;
        if(s.contains(":") && LOWER_NON_GRAMMAR_LETTERS.indexOf(s.charAt(0)) != -1){
            // before : there may be no closing symbol!
            int index = s.indexOf(":");
            if(!s.substring(0,index).contains(">") && !s.substring(0,index).contains("]") && !s.substring(0,index).contains("}") && !s.substring(0,index).contains(")")) inputName = s.substring(0,s.indexOf(":"));
            //inputString = inputString.substring(inputString.indexOf(":")+1);
            // TODO: variable for terminal input
        }
        return inputName;
    }

    /**
     * Checks whether the given constructor string starts with the ludeme name
     * 2 cases:
     *          case 1: e.g. (ludeme)name = board
     *                  constructorString = (board ...)
     *          case 2: e.g. (ludeme)name = board.track
     *                   constructorString = (track ...)
     * @param name Name of the Ludeme
     * @param constructorString String representing the constructor
     * @return Whether the String representing the starts with the name of the Ludeme
     */
    private boolean constructorStringStartsWithLudemeName(String name, String constructorString){
        // 1: check whether constructorString starts with ludeme name
        boolean startsWithName = false;
        // 2 cases:   1) name: board , constructorSring: (board ...)   2) name: board.track , constructorString: (track ...)
        // case 2
        if(name.contains(".")){
            String[] nameSplit = name.split("\\.");
            return constructorString.startsWith(nameSplit[nameSplit.length-1]);
        }
        // case 1
        else {
            return constructorString.startsWith(name);
        }
    }
    

}
