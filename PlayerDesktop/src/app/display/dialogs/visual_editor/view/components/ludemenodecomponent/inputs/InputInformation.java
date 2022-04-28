package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;


import app.display.dialogs.visual_editor.model.grammar.Constructor;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.input.ChoiceInput;
import app.display.dialogs.visual_editor.model.grammar.input.Input;
import app.display.dialogs.visual_editor.model.grammar.input.LudemeInput;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InputInformation {

    private final int INDEX; // index of input in constructor
    private final Constructor CONSTRUCTOR;
    private final Input INPUT;
    private final List<Ludeme> LUDEME_INPUTS;

    public InputInformation(Constructor constructor, Input input){
        this.CONSTRUCTOR = constructor;
        this.INPUT = input;
        this.INDEX = getIndex(constructor, input);
        this.LUDEME_INPUTS = getPossibleLudemeInputs(input);
    }

    private int getIndex(Constructor constructor, Input input){
        return constructor.getInputs().indexOf(input);
    }

    private List<Ludeme> getPossibleLudemeInputs(Input input){
        List<Ludeme> possibleLudemeInputs = new ArrayList<>();

        if(input.isChoice()){
            for(Input in : ((ChoiceInput)input).getInputs()){
                possibleLudemeInputs.add(((LudemeInput) in).getRequiredLudeme());
            }
            return possibleLudemeInputs;
        }
        if(input instanceof LudemeInput){
            LudemeInput l_input = (LudemeInput) input;

            if(l_input.getRequiredLudeme().isHidden()){
            /*
                for(Constructor c : l_input.getRequiredLudeme().getConstructors()){
                    for(Input in : c.getInputs()){
                        Ludeme l = ((LudemeInput) in).getRequiredLudeme();
                        possibleLudemeInputs.add(l);
                    }
                }
             */
                possibleLudemeInputs.addAll(getNonHiddenLudemes(l_input.getRequiredLudeme()));
            } else {
                possibleLudemeInputs.add(l_input.getRequiredLudeme());
            }
            return possibleLudemeInputs;
        }

        return possibleLudemeInputs;
    }

    private List<Ludeme> getNonHiddenLudemes(Ludeme ludeme){
        if(ludeme.isHidden()) {
            List<Ludeme> ludeme_inputs = new ArrayList<>();
            for (Constructor c : ludeme.getConstructors()) {
                for (Input in : c.getInputs()) {
                    if(!ludeme_inputs.contains(((LudemeInput) in).getRequiredLudeme())) {
                        ludeme_inputs.add(((LudemeInput) in).getRequiredLudeme());
                    }
                }
            }
            for (Ludeme l : new ArrayList<Ludeme>(ludeme_inputs)) {
                if (l.isHidden()) {
                    ludeme_inputs.remove(l);
                    ludeme_inputs.addAll(getNonHiddenLudemes(l));
                }
            }
            //remove duplicates in list
            return ludeme_inputs.stream().distinct().collect(Collectors.toList());
        }
        return null;
    }

    public List<Ludeme> getPossibleLudemeInputs(){
        return LUDEME_INPUTS;
    }

    public boolean isOptional(){
        return INPUT.isOptional();
    }

    public boolean isCollection(){
        return INPUT.isCollection();
    }

    public boolean isChoice(){
        return INPUT.isChoice();
    }

    public int getIndex(){
        return INDEX;
    }

    public Input getInput(){
        return INPUT;
    }

    public Constructor getConstructor(){
        return CONSTRUCTOR;
    }

    @Override
    public String toString(){
        return INPUT.toString();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof InputInformation){
            InputInformation i = (InputInformation) o;
            return i.getInput().equals(INPUT) && i.getConstructor().equals(CONSTRUCTOR) && i.getIndex() == INDEX;
        }
        return false;
    }

}
