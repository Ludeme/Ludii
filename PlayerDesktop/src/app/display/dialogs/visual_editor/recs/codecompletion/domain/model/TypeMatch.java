package app.display.dialogs.visual_editor.recs.codecompletion.domain.model;

import app.display.dialogs.visual_editor.recs.codecompletion.controller.NGramController;
import app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.model.iTypeMatch;
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
        System.out.println("--------------------------------------");
        System.out.println(" # Game Description");
        System.out.println(gameDescription);
        // 1. get the picklist from the NGram model
        List<Instance> instancePicklist = controller.getPicklist(gameDescription);
        System.out.println(" # Ordered Picklist by N-Gram");
        for(Instance instance : instancePicklist) {
            System.out.println("Completion: " + instance.getPrediction());
        }

        System.out.println(" # Possible Symbols from grammar");
        for(Symbol symbol : possibleSymbols) {
            System.out.println("Symbol: " + symbol.name() + " token: " + symbol.token() + " type: " + symbol.ludemeType());
        }

        /*// 2. create a new picklist to output
        List<Symbol> picklist = new ArrayList<>();
        for(int i = 0; i < instancePicklist.size(); i++) {
            Instance cur = instancePicklist.get(i);
            // TODO: if the instance is contained in the possible symbols list, then add it to the picklist
            // this will preserve the order
        }

        // then in the end return picklist
        */
        return possibleSymbols;
    }
}
