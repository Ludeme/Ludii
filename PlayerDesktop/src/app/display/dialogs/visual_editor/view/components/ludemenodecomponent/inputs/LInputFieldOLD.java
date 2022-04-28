package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.input.ChoiceInput;
import app.display.dialogs.visual_editor.model.grammar.input.Input;
import app.display.dialogs.visual_editor.model.grammar.input.LudemeInput;
import app.display.dialogs.visual_editor.model.grammar.input.TerminalInput;
import app.display.dialogs.visual_editor.view.components.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an input field used to supply an argument to a ludeme.
 * It is displayed in the LudemeNodeComponent.
 */

public class LInputFieldOLD extends JComponent{


    private final List<Input> INPUTS;
    private final List<Integer> INPUT_INDICES;
    private final Input INPUT;

    private final LudemeNodeComponent LNC;



    private JComponent inputFieldComponent;
    private LConnectionComponent connectionComponent;

    public LInputFieldOLD(LudemeNodeComponent ludemeNodeComponent, Input input, int inputIndex){
        this.INPUT = input;
        this.LNC = ludemeNodeComponent;
        this.INPUTS = List.of(input);
        this.INPUT_INDICES = List.of(inputIndex);

        addInputField();

        revalidate();
        repaint();
    }

    public LInputFieldOLD(LudemeNodeComponent ludemeNodeComponent, List<Input> inputs, List<Integer> indices){
        this.INPUTS = inputs;
        this.INPUT_INDICES = indices;
        this.INPUT = null;
        this.LNC = ludemeNodeComponent;

    }

    private void addInputField() {

        /*
             CASES:
                1. Optional: (a) Label: "add optional argument" , (b) icon button
                2. Choice: (a) Label: "choice", (b) icon button, (c) connection component || when connected -> (a) Label = ludeme name
                3. Collection: (a) Label: "add <name>", (b) icon button

                All of these are strictly non-terminal
         */

        JLabel label = new JLabel(INPUT.getName());
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);

        if(INPUT.isTerminal()){
            inputFieldComponent = ((TerminalInput)INPUT).getComponent();
            inputFieldComponent.setPreferredSize(new Dimension(((int)((LNC.getWidth()-label.getPreferredSize().width)*0.8)),inputFieldComponent.getPreferredSize().height));
            inputFieldComponent.addMouseListener(userInputListener);

            setLayout(new FlowLayout(FlowLayout.LEFT));
            add(Box.createHorizontalStrut(10));
            add(label);
            add(inputFieldComponent);
        }
        else {
            setLayout(new FlowLayout(FlowLayout.RIGHT));
            add(label);

            if(INPUT.isOptional()){
                LInputButton addOptionalArgumentButton = new LInputButton(DesignPalette.OPTIONAL_ICON_ACTIVE, DesignPalette.OPTIONAL_ICON_HOVER);

                // TODO: Button Listener

                add(Box.createHorizontalStrut(10));
                add(label);
                add(Box.createHorizontalStrut(5));
                add(addOptionalArgumentButton);

            }

            else if (INPUT.isChoice()) {
                label.setText("Choice");
                LInputButton addChoiceButton = new LInputButton(DesignPalette.CHOICE_ICON_ACTIVE, DesignPalette.CHOICE_ICON_HOVER);

                // TODO: Hover
                // TODO: Button Listener

                add(Box.createHorizontalStrut(10));
                add(addChoiceButton);
            }
            // TODO: Collection
            add(Box.createHorizontalStrut(5));
            connectionComponent = new LConnectionComponent(null, label.getPreferredSize().height, (int) (label.getPreferredSize().height * 0.4), false);
            add(connectionComponent);
        }
    }

    public int getInputIndex() {
        if(INPUT != null){
            return INPUT_INDICES.get(0);
        }
        return -404;
    }

    public List<Integer> getInputIndices(){
        return INPUT_INDICES;
    }

    public Input getInput() {
        return INPUT;
    }

    public List<Input> getInputs(){
        return INPUTS;
    }

    /**
     * If this input field requires ludemes, it will return a list of all possible ludemes that may be supplied to the field as input.
     * @return List of possible ludemes
     */
    public List<Ludeme> getRequiredLudemes(){
        List<Ludeme> requiredLudemes = new ArrayList<>();

        if(INPUT.isTerminal()) {
          return requiredLudemes;
        }
        if(INPUT instanceof LudemeInput) {
            requiredLudemes.add(((LudemeInput) INPUT).getRequiredLudeme());
            return requiredLudemes;
        }
        if(INPUT instanceof ChoiceInput) {
            for(Input input : ((ChoiceInput) INPUT).getInputs()) {
                requiredLudemes.add(((LudemeInput) input).getRequiredLudeme());
            }
            return requiredLudemes;
        }
        return null;
    }

    /**
     * Returns the user supplied input for an input field
     * @return
     */
    public Object getUserInput(){
        if(inputFieldComponent == null){
            // then its ludeme input
            return connectionComponent.getConnectedTo().getLudemeNode();
        }
        if(inputFieldComponent instanceof JTextField) return ((JTextField)inputFieldComponent).getText();
        if(inputFieldComponent instanceof JSpinner) return ((JSpinner)inputFieldComponent).getValue();
        if(inputFieldComponent instanceof JComboBox) return ((JComboBox)inputFieldComponent).getSelectedItem();

        // TODO: What about ludeme inputs? required?

        return null;
    }

    // Updates the provided input list in the model whenever the mouse moves over this input field
    MouseListener userInputListener = new MouseAdapter() {
        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseMoved(e);
            Handler.updateInput(LNC.getGraphPanel().getGraph(), LNC.getLudemeNode(), getInputIndex(), getUserInput());
            System.out.println("Updated input " + getInputIndex() + " to " + getUserInput());
        }
    };

    public void setUserInput(Object input){
        if(inputFieldComponent == null){
            // then its ludeme input
            IGraphPanel graphPanel = LNC.getGraphPanel();
            graphPanel.addConnection(connectionComponent, graphPanel.getNodeComponent((LudemeNode) input).getIngoingConnectionComponent());
        }
        if(inputFieldComponent instanceof JTextField) ((JTextField)inputFieldComponent).setText((String)input);
        if(inputFieldComponent instanceof JSpinner) ((JSpinner)inputFieldComponent).setValue(input);
        if(inputFieldComponent instanceof JComboBox) ((JComboBox)inputFieldComponent).setSelectedItem(input);
    }

    public LConnectionComponent getConnectionComponent(){
        return connectionComponent;
    }

    public LudemeNodeComponent getLuDemeNodeComponent(){
        return LNC;
    }

}
