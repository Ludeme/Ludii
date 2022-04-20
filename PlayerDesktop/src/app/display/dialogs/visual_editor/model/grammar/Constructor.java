package app.display.dialogs.visual_editor.model.grammar;


import app.display.dialogs.visual_editor.model.grammar.input.Input;

import java.util.List;

public class Constructor {
    private final List<Input> INPUTS;
    private final String GRAMMAR;
    private String NAME = "";

    public Constructor(List<Input> inputs){
        this.INPUTS = inputs;
        this.GRAMMAR = "None";
    }

    public Constructor(List<Input> inputs, String grammar){
        this.INPUTS = inputs;
        this.GRAMMAR = grammar;
    }

    public void setName(String name){ this.NAME = name; }
    public String getName(){ return NAME; }

    public List<Input> getInputs(){
        return INPUTS;
    }
    public String getGrammar(){ return GRAMMAR; }

    @Override
    public String toString(){
        if(!NAME.isBlank()) return NAME;
        return getInputs().toString().substring(1, getInputs().toString().length() - 1);
    }
}
