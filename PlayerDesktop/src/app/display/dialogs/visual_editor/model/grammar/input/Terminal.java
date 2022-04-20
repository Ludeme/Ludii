package app.display.dialogs.visual_editor.model.grammar.input;

public class Terminal {
    private final String NAME;
    public Terminal(String name){
        this.NAME = name;
    }
    public String getName(){
        return NAME;
    }
    @Override
    public String toString(){
        return NAME;
    }
}
