package app.display.dialogs.visual_editor.model.grammar.input;

public interface Input {
    public boolean isCollection();
    public boolean isTerminal();
    public boolean isOptional();
    public boolean isChoice();
    public String getName();
    public void setOptional(boolean optional);
    public void setCollection(boolean collection);
}
