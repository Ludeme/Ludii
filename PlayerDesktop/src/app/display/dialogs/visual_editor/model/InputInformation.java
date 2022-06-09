package app.display.dialogs.visual_editor.model;



import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InputInformation {

    private final int INDEX; // index of input in constructor


    private final Clause CLAUSE;
    private final NodeInput NODE_INPUT;
    private final List<Symbol> SYMBOL_INPUTS;

    public InputInformation(Clause clause, NodeInput nodeInput){
        this.CLAUSE = clause;
        this.NODE_INPUT = nodeInput;
        this.INDEX = getIndex(clause, nodeInput.arg());
        this.SYMBOL_INPUTS = getPossibleSymbolInputs(nodeInput);
    }

    private int getIndex(Clause clause, ClauseArg argument){
        return clause.args().indexOf(argument);
    }

    private List<Symbol> getPossibleSymbolInputs(NodeInput argument){
        List<Symbol> inputs = new ArrayList<>();
        for(ClauseArg arg : argument.args()){
            System.out.println("---- " + arg);
            inputs.add(arg.symbol());
        }
        return inputs.stream().distinct().collect(Collectors.toList()); // remove duplicates
    }

    /*private List<Ludeme> getPossibleLudemeInputs(Input input){
        List<Ludeme> possibleLudemeInputs = new ArrayList<>();



        if(input.isChoice()){
            for(Input in : ((ChoiceInput)input).getInputs()){
                if(!possibleLudemeInputs.contains(((LudemeInput) in).getRequiredLudeme())) possibleLudemeInputs.add(((LudemeInput) in).getRequiredLudeme());
            }
            for(Ludeme l : new ArrayList<>(possibleLudemeInputs)){
                possibleLudemeInputs.addAll(getSingleInputLudemes(l));
            }
            System.out.println("###2: " + possibleLudemeInputs);
            return possibleLudemeInputs;
        }
        if(input instanceof LudemeInput){
            LudemeInput l_input = (LudemeInput) input;
            if(l_input.getRequiredLudeme().isHidden()){
                possibleLudemeInputs.addAll(getNonHiddenLudemes(l_input.getRequiredLudeme()));
            } else {
                possibleLudemeInputs.add(l_input.getRequiredLudeme());
            }
            for(Ludeme l : new ArrayList<>(possibleLudemeInputs)){
                possibleLudemeInputs.addAll(getSingleInputLudemes(l));
            }
            System.out.println("###2: " + possibleLudemeInputs);
            return possibleLudemeInputs;
        }
        return possibleLudemeInputs;
    }

    private List<Ludeme> getSingleInputLudemes(Ludeme l){
        List<Ludeme> ludeme_inputs = new ArrayList<>();

        for(Constructor c : l.getConstructors()){
            if(c.getInputs().size() == 1 && c.getInputs().get(0) instanceof LudemeInput){
                Ludeme ludeme = ((LudemeInput) c.getInputs().get(0)).getRequiredLudeme();
                if(!ludeme_inputs.contains(ludeme)) ludeme_inputs.add(ludeme);
            }
        }
        System.out.println("###: " + ludeme_inputs);

        return ludeme_inputs;
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
    }*/


    public List<Symbol> getPossibleSymbolInputs(){
        return SYMBOL_INPUTS;
    }

    public boolean optional(){
        return NODE_INPUT.arg().optional();
    }

    public boolean collection(){
        return NODE_INPUT.arg().nesting() > 0;
    }

    public boolean choice(){
        return NODE_INPUT.size() > 1;
    }

    public Clause clause(){
        return CLAUSE;
    }

    public NodeInput nodeInput(){
        return NODE_INPUT;
    }

    public int getIndex(){
        return INDEX;
    }


    @Override
    public String toString(){
        return "[" + INDEX + ", " + nodeInput() + "| " + clause() + "]";
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof InputInformation){
            InputInformation i = (InputInformation) o;
            return i.nodeInput().equals(nodeInput()) && i.clause().equals(clause()) && i.getIndex() == INDEX;
        }
        return false;
    }

}
