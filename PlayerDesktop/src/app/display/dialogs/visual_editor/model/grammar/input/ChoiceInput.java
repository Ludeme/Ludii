package app.display.dialogs.visual_editor.model.grammar.input;

import java.util.List;

public class ChoiceInput implements Input {
    private final String NAME;
    private final List<Input> INPUTS;
    private boolean OPTIONAL = false;
    private boolean COLLECTION = false;

    public ChoiceInput(String name, List<Input> inputs){
        this.NAME = name;
        this.INPUTS = inputs;
        this.OPTIONAL = false;
    }

    @Override
    public boolean isCollection() {
        return COLLECTION;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public boolean isOptional() {
        return OPTIONAL;
    }

    @Override
    public boolean isChoice() {
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void setOptional(boolean optional) {
        OPTIONAL = optional;
    }

    @Override
    public void setCollection(boolean collection) {
        COLLECTION = collection;
    }

    public List<Input> getInputs(){ return INPUTS; }

    @Override
    public String toString(){
        StringBuilder s = new StringBuilder("(");
        for(Input input : INPUTS){
            s.append(input.toString()).append(" | ");
        }
        s = new StringBuilder(s.substring(0, s.length() - 3));
        s.append(")");
        String ss = s.toString();
        if(OPTIONAL) ss = "[" + ss + "]";
        return ss;
    }

}
