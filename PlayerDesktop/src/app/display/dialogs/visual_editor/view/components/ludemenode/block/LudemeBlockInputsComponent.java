package app.display.dialogs.visual_editor.view.components.ludemenode.block;

import app.display.dialogs.visual_editor.model.grammar.input.Input;
import app.display.dialogs.visual_editor.view.components.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenode.interfaces.ILudemeNodeInputs;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LudemeBlockInputsComponent extends JPanel implements ILudemeNodeInputs {

    private final int INPUT_COMPONENT_HEIGHT = 36;
    private List<InputComponent> componentList = new ArrayList<>();
    private final LudemeBlock LUDEME_BLOCK;
    //private final int PADDING = 5; // padding between arg0 and headerComponent

    public LudemeBlockInputsComponent(LudemeBlock ludemeBlock){
        this.LUDEME_BLOCK = ludemeBlock;
        initialize();
    }

    public List<InputComponent> getComponentList(){
        return componentList;
    }

    private void initialize(){
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);
        setBackground(DesignPalette.BACKGROUND_LUDEME_BODY);

        // for every input in current constructor add input field
        for(Input i : LUDEME_BLOCK.getCurrentConstructor().getInputs()){
            InputComponent ic = new InputComponent(LUDEME_BLOCK, i);
            ic.setAlignmentX(Component.LEFT_ALIGNMENT);
            componentList.add(ic);
            add(ic);
            LUDEME_BLOCK.setHeight(LUDEME_BLOCK.getHeight()+ic.getHeight());
            revalidate();
        }

        setSize(LUDEME_BLOCK.WIDTH_CENTER,getPreferredSize().height);

        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        revalidate();
        repaint();
        setVisible(true);
    }

    public void update(){
        componentList = new ArrayList<>();
        removeAll();
        initialize();
    }

}
