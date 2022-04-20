package app.display.dialogs.visual_editor.model.grammar.parser;

import java.util.ArrayList;
import java.util.List;

public class Grammar {
    public final String NAME;
    public List<String> constructors = new ArrayList<>();

    public Grammar(String name){
        this.NAME = name;
    }

    public void addConstructor(String constructor){
        constructors.add(constructor);
    }

    @Override
    public String toString(){
        return NAME + ":  " + constructors.toString();
    }

}
