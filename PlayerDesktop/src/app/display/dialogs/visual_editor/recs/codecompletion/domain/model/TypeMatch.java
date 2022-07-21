package app.display.dialogs.visual_editor.recs.codecompletion.domain.model;

import app.display.dialogs.visual_editor.recs.codecompletion.controller.NGramController;
import app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.model.iTypeMatch;
import app.display.dialogs.visual_editor.recs.utils.StringUtils;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.List;


public class TypeMatch implements iTypeMatch {

    private static TypeMatch instance;

    public static TypeMatch getInstance() {
        if(instance == null) {
            instance = new TypeMatch();
        }
        return instance;
    }

    private TypeMatch() {

    }

    /**
     * This method takes in the game description as a string and a list of suggestions from the grammar
     * Then it
     *
     * @param gameDescription
     * @param possibleSymbols
     * @return
     */
    @Override
    public List<Symbol> typematch(String gameDescription, NGramController controller, List<Symbol> possibleSymbols) {
        boolean verbose = false;

        if(verbose)System.out.println("--------------------------------------");
        if(verbose)System.out.println(" # Game Description");
        if(verbose)System.out.println(gameDescription);
        // 1. get the picklist from the NGram model
        List<Instance> instancePicklist = controller.getPicklist(gameDescription);
        if(verbose)System.out.println(" # Ordered Picklist by N-Gram");
        for(Instance instanceList : instancePicklist) 
        {
            String prediction = instanceList.getPrediction();
            if(prediction.startsWith("(")) 
                prediction = prediction.replaceAll("\\(","");
            if(verbose)System.out.println("Completion: " + prediction);
        }

        if(verbose)System.out.println(" # Possible Symbols from grammar");
        for(Symbol symbol : possibleSymbols) {
            // if the type is predefined, need to check for boolean, string and number
            if(verbose)System.out.println("Symbol: " + symbol.name() + " token: " + symbol.token() + " type: " + symbol.ludemeType());
        }

        // 2. create a new picklist to output
        Symbol[] possibleSymbolsArray = possibleSymbols.toArray(new Symbol[0]);
        List<Symbol> picklist = new ArrayList<>();
        for(int i = 0; i < instancePicklist.size(); i++) {
            Instance curInstance = instancePicklist.get(i);

            String prediction = curInstance.getPrediction();
            if(prediction.startsWith("(")) {// in case it is a ludeme like (game with an opening bracket, remove it
                prediction = prediction.replaceAll("\\(","");
            }
            // if the instance is contained in the possible symbols list, then add it to the picklist
            for(int j = 0; j < possibleSymbolsArray.length; j++) {
                //System.out.println("I:"+i+" J: "+j);
                if(possibleSymbolsArray[j] == null) {
                    continue;
                }
                Symbol curSymbol = possibleSymbolsArray[j];
                String token = curSymbol.token();
                //System.out.println("Prediction: "+ prediction + " Name: " + token + " equals? " + StringUtils.equals(prediction,token));
                if(StringUtils.equals(prediction,token)) {
                    //add the symbol to the picklist, since this is done in the correct order of the instancelist it preserves the order
//                    for(Symbol symbol : picklist) {
//
//                    }
                    picklist.add(curSymbol);
                    // delete the symbol out of the array
                    possibleSymbolsArray[j] = null;
                }
            }
        }
        //add all symbols that are still in the possible symbols array to the picklist
        for(int i = 0; i < possibleSymbolsArray.length; i++) {
            if(possibleSymbolsArray[i] == null) {
                continue;
            }
            Symbol curSymbol = possibleSymbolsArray[i];
            picklist.add(curSymbol);
        }
        if(verbose) {
            System.out.println(" # Final Picklist, of length: " + picklist.size());
            for (int i = 0; i < picklist.size(); i++) {
                System.out.println(i + ". " + picklist.get(i));
            }
        }

        // then in the end return picklist

        return picklist;
    }
}
