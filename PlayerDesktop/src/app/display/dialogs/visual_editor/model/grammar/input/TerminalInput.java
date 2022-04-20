package app.display.dialogs.visual_editor.model.grammar.input;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TerminalInput implements Input{

    private final String NAME;
    private final TerminalInputType TYPE;
    private final List<Terminal> TERMINALS; // only relevant for TerminalInputType = DROPDOWN
    private boolean OPTIONAL = false;
    private boolean COLLECTION = false;

    public TerminalInput(String name, TerminalInputType type){
        this.NAME = name;
        this.TYPE = type;
        this.TERMINALS =  new ArrayList<>();
    }

    public TerminalInput(String name, TerminalInputType type, List<Terminal> terminals){
        this.NAME = name;
        this.TYPE = type;
        this.TERMINALS = terminals;
        this.OPTIONAL = false;
    }


    public JComponent getComponent(){
        switch(TYPE){
            case STRING:
                return new JTextField(); // Returns a textfield
            case INTEGER:
                return new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1)); // returns a spinner TODO: whats minimum/maximum?
            case DROPDOWN:
                JComboBox<Terminal> comboBox = new JComboBox<>();
                for(Terminal t : TERMINALS)
                    comboBox.addItem(t);
                return comboBox;
        }
        return null;
    }

    @Override
    public boolean isCollection() {
        return COLLECTION;
    }

    @Override
    public boolean isTerminal() {
        return true;
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

    @Override
    public void setOptional(boolean optional) {
        OPTIONAL = optional;
    }

    @Override
    public void setCollection(boolean collection) {
        COLLECTION = collection;
    }

    @Override
    public String toString(){
        if(isOptional()){
            return "[" + getName() + "]";
        }
        return NAME;
    }
}
