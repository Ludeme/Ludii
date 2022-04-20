package app.display.dialogs.visual_editor.view.components.ludemenode.block;


import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.input.ChoiceInput;
import app.display.dialogs.visual_editor.model.grammar.input.Input;
import app.display.dialogs.visual_editor.model.grammar.input.LudemeInput;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LudemeBlockOutgoingConnectionsComponent extends JPanel {

    private List<LudemeConnectionComponent> connectionComponentList = new ArrayList();

    private final LudemeBlock LUDEME_BLOCK;

    public LudemeBlockOutgoingConnectionsComponent(LudemeBlock ludemeBlock, List<InputComponent> inputComponents){
        this.LUDEME_BLOCK = ludemeBlock;
        initialize(inputComponents);
    }

    public List<LudemeConnectionComponent> getConnectionComponentList(){
        return connectionComponentList;
    }

    private void initialize(List<InputComponent> inputComponents){
        connectionComponentList = new ArrayList();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setSize(new Dimension(LUDEME_BLOCK.WIDTH_SIDE, LUDEME_BLOCK.getHeight()));
        for(int i = 0; i < inputComponents.size(); i++){
            Input in = inputComponents.get(i).INPUT;
            int inputComponentHeight = (int) inputComponents.get(i).getPreferredSize().getHeight();
            if(!in.isTerminal()){
                List<Ludeme> list_of_required_ludemes;
                if(in.isChoice()){
                    list_of_required_ludemes = new ArrayList<>();
                    for(Input c_in : ((ChoiceInput) in).getInputs()){
                        if(!c_in.isTerminal()) list_of_required_ludemes.add(((LudemeInput) c_in).getRequiredLudeme());
                    }
                }
                else {
                    list_of_required_ludemes = List.of(((LudemeInput) in).getRequiredLudeme());
                }
                LudemeConnectionComponent lc = new LudemeConnectionComponent(LUDEME_BLOCK, list_of_required_ludemes, LUDEME_BLOCK.WIDTH_SIDE,inputComponentHeight, LUDEME_BLOCK.WIDTH_SIDE/2, true);
                connectionComponentList.add(lc);
                lc.setAlignmentX(Component.CENTER_ALIGNMENT);
                add(lc);
            }
            else{
                add(Box.createRigidArea(new Dimension(LUDEME_BLOCK.WIDTH_SIDE, inputComponentHeight)));
            }
            revalidate();
        }
        // set background of connectionsComponent transparent
        setOpaque(false);
        revalidate();
        repaint();
    }

    public void update(List<InputComponent> inputComponents){
        removeAll();
        initialize(inputComponents);
    }

}
