package app.display.dialogs.visual_editor.model.grammar.input;


import app.display.dialogs.visual_editor.model.grammar.Ludeme;

public class LudemeInput implements Input{

    private final String NAME;
    private final Ludeme REQUIRED_LUDEME;
    private boolean OPTIONAL = false;
    private boolean COLLECTION = false;

    public LudemeInput(String name, Ludeme requiredLudeme){
        this.NAME = name;
        this.REQUIRED_LUDEME = requiredLudeme;
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
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    public Ludeme getRequiredLudeme(){
        return REQUIRED_LUDEME;
    }

    public void setOptional(boolean optional){
        OPTIONAL = optional;
    }

    public void setCollection(boolean collection){
        COLLECTION = collection;
    }

    @Override
    public String toString(){
        String s = "<"+NAME+">";
        if(OPTIONAL){
            s = "["+s+"]";
        }
        if(COLLECTION){
            s = "{"+s+"}";
        }
        return s;
    }

}
