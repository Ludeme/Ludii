package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import main.grammar.Clause;
import main.grammar.ClauseArg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the required Argument (ClauseArg) of a "row" in the node
 * If ClauseArg is part of a OR-Group, then this is a list, otherwise it just contains the ClauseArg
 * @author Filipp Dokienko
 */

public class NodeInput {
    private final List<ClauseArg> ARGS;

    public NodeInput(Clause clause, ClauseArg arg){
        // add argument to list
        ARGS = new ArrayList<>();
        ARGS.add(arg);
        // if arg is part of OR-Group, add other components of OR-Group to it.
        if(arg.orGroup() != 0) {
            int group = arg.orGroup();
            int index = clause.args().indexOf(arg)+1;
            while(clause.args().get(index).orGroup() == group){
                ARGS.add(clause.args().get(index));
                index++;
            }
        }
    }

    public List<ClauseArg> args(){
        return ARGS;
    }

    public ClauseArg arg(){
        return ARGS.get(0);
    }

    public int size(){
        return ARGS.size();
    }

    @Override
    public String toString(){
        return ARGS.stream().map(ClauseArg::toString).collect(Collectors.joining(", "));
    }

}
