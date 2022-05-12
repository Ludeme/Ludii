package app.display.dialogs.visual_editor.model.grammar;


import app.display.dialogs.visual_editor.model.grammar.input.Input;
import app.display.dialogs.visual_editor.model.grammar.input.LudemeInput;

import java.util.ArrayList;
import java.util.List;

public class Ludeme {
    public final String NAME;
    private boolean HIDDEN = true; // TODO
    private boolean checkedHidden = false;
    public List<Constructor> CONSTRUCTORS = new ArrayList<>();


    public Ludeme(String name, List<Constructor> constructors){
        this.NAME = name;
        this.CONSTRUCTORS = constructors;
    }

    public Ludeme(String name){
        this.NAME = name;
    }

    public String getName() {
        return NAME;
    }

    // TODO: Change name of method (should be getName())
    public String getClearName(){
        if(NAME.contains(".")) {
            return NAME.substring(NAME.lastIndexOf(".")+1);
        }
        return NAME;
    }

    // TODO: TO FIX when getting constructor of end.forEach appears NullPointException
    public List<Constructor> getConstructors(){
        return CONSTRUCTORS;
    }
    public void addConstructor(Constructor c){
        CONSTRUCTORS.add(c);
    }

    public boolean isHidden(){
        // check whether hidden TODO: Not correct way actually :(
        if(!checkedHidden) {
            for (Constructor c : getConstructors()) {
                if (HIDDEN == false) break;
                if (c.getInputs().size() > 1) {
                    this.HIDDEN = false;
                    break;
                }
                for (Input in : c.getInputs()) {
                    if (!(in instanceof LudemeInput) || in.isCollection() || in.isOptional()) {
                        this.HIDDEN = false;
                        break;
                    }
                }
            }
            checkedHidden = true;
        }
        return HIDDEN;
    }

    @Override
    public String toString(){
        return getClearName();
    }
}
