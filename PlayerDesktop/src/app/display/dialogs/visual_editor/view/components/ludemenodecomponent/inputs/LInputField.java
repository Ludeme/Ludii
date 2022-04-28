package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.input.Input;
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

public class LInputField extends JComponent {

    /*
        A LInputField can either have 1x InputInformation -> Standard Field
                                list of InputInformation -> "Merged" Field
     */

    private final LudemeNodeComponent LNC;
    List<InputInformation> inputInformationList = new ArrayList<>();
    boolean isSingle;

    List<LInputField> children = new ArrayList<>(); // list of children in case of collection

    JComponent inputFieldComponent;
    LConnectionComponent connectionComponent;

    public LInputField(LudemeNodeComponent ludemeNodeComponent, InputInformation inputInformation){
        this.LNC = ludemeNodeComponent;
        inputInformationList.add(inputInformation);
        isSingle = true;
        constructInputField(inputInformation);

    }

    public LInputField(LudemeNodeComponent ludemeNodeComponent, List<InputInformation> inputInformationList){
        System.out.println("constructing " + ludemeNodeComponent.getLudemeNode().getLudeme().getName());
        this.LNC = ludemeNodeComponent;
        this.inputInformationList = inputInformationList;
        isSingle = false;
        constructInputField(inputInformationList);
    }

    public LInputField(LInputField parent){
        this.LNC = parent.LNC;
        this.isSingle = parent.isSingle;
        this.inputInformationList = parent.inputInformationList;
        constructCollectionField(parent);
        parent.children.add(this);
    }

    private void constructCollectionField(LInputField parent){
        JLabel label = new JLabel(parent.getInputInformation().getInput().getName());
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(label);
        LInputButton addItemButton = new LInputButton(DesignPalette.COLLECTION_REMOVE_ICON_ACTIVE, DesignPalette.COLLECTION_REMOVE_ICON_HOVER);

        add(Box.createHorizontalStrut(10));
        add(addItemButton);

        addItemButton.addActionListener(e -> {
                  LNC.getInputArea().removeField(this);
                });


        add(Box.createHorizontalStrut(5));
        connectionComponent = new LConnectionComponent(this, label.getPreferredSize().height, (int) (label.getPreferredSize().height * 0.4), false);
        add(connectionComponent);
        inputFieldComponent = connectionComponent;
    }

    private void constructInputField(InputInformation inputInformation){
        removeAll();

        Input input = inputInformation.getInput();

        JLabel label = new JLabel(input.getName());
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);



        if(input.isTerminal()){
            inputFieldComponent = ((TerminalInput)input).getComponent();
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

            if(input.isOptional()){
                LInputButton addOptionalArgumentButton = new LInputButton(DesignPalette.OPTIONAL_ICON_ACTIVE, DesignPalette.OPTIONAL_ICON_HOVER);

                // TODO: Button Listener

                add(Box.createHorizontalStrut(10));
                add(label);
                add(Box.createHorizontalStrut(5));
                add(addOptionalArgumentButton);

            }

            else if (input.isChoice()) {
                label.setText("Choice");
                LInputButton addChoiceButton = new LInputButton(DesignPalette.CHOICE_ICON_ACTIVE, DesignPalette.CHOICE_ICON_HOVER);

                // TODO: Hover
                // TODO: Button Listener

                add(Box.createHorizontalStrut(10));
                add(addChoiceButton);

            }

            else if(input.isCollection()){
                LInputButton addItemButton = new LInputButton(DesignPalette.COLLECTION_ICON_ACTIVE, DesignPalette.COLLECTION_ICON_HOVER);

                add(Box.createHorizontalStrut(10));
                add(addItemButton);

                addItemButton.addActionListener(e -> {
                    System.out.println("CLICKED ADD");
                    /* TODO:
                    if(children.isEmpty()) {
                        LNC.getInputArea().addInputFieldBelow(new LInputField(LInputField.this), LInputField.this);
                    } else {
                        LNC.getInputArea().addInputFieldBelow(new LInputField(LInputField.this), LInputField.this.children.get(LInputField.this.children.size() - 1));
                    }*/
                    LNC.getInputArea().addInputFieldBelow(new LInputField(LInputField.this), LInputField.this);
                });
            }

            add(Box.createHorizontalStrut(5));
            connectionComponent = new LConnectionComponent(this, label.getPreferredSize().height, (int) (label.getPreferredSize().height * 0.4), false);
            add(connectionComponent);
            inputFieldComponent = connectionComponent;
        }

    }

    /*
         Optional arguments merge
     */
    private void constructInputField(List<InputInformation> inputInformationList){
        removeAll();

        JLabel label = new JLabel("Optional Arguments");
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);

        setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(label);

        add(Box.createHorizontalStrut(5));
        connectionComponent = new LConnectionComponent(this, label.getPreferredSize().height, (int) (label.getPreferredSize().height * 0.4), false);
        add(connectionComponent);
        inputFieldComponent = connectionComponent;

    }

    // Updates the provided input list in the model whenever the mouse moves over this input field
    MouseListener userInputListener = new MouseAdapter() {
        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseMoved(e);
            if(isSingle) {
                Handler.updateInput(LNC.getGraphPanel().getGraph(), LNC.getLudemeNode(), getInputIndex(), getUserInput());
                System.out.println("Updated input " + getInputIndex() + " to " + getUserInput());
            }
        }
    };

    /**
     * Returns the user supplied input for an input field
     * @return
     */
    public Object getUserInput(){
        if(inputInformationList.size() > 1) System.out.println("!!!! INCORRECT USE HERE");
        if(inputFieldComponent == connectionComponent){
            // then its ludeme input
            return connectionComponent.getConnectedTo().getLudemeNode();
        }
        if(inputFieldComponent instanceof JTextField) return ((JTextField)inputFieldComponent).getText();
        if(inputFieldComponent instanceof JSpinner) return ((JSpinner)inputFieldComponent).getValue();
        if(inputFieldComponent instanceof JComboBox) return ((JComboBox)inputFieldComponent).getSelectedItem();

        // TODO: What about ludeme inputs? required?

        return null;
    }

    public void setUserInput(Object input){
        if(inputInformationList.size() > 1) System.out.println("!!!! INCORRECT USE HERE");
        if(inputFieldComponent == connectionComponent){
            // then its ludeme input
            IGraphPanel graphPanel = LNC.getGraphPanel();
            graphPanel.addConnection(connectionComponent, graphPanel.getNodeComponent((LudemeNode) input).getIngoingConnectionComponent());
        }
        if(inputFieldComponent instanceof JTextField) ((JTextField)inputFieldComponent).setText((String)input);
        if(inputFieldComponent instanceof JSpinner) ((JSpinner)inputFieldComponent).setValue(input);
        if(inputFieldComponent instanceof JComboBox) ((JComboBox)inputFieldComponent).setSelectedItem(input);
    }

    public void setUserInput(Object input, int index){
        if(isSingle) {
            setUserInput(input);
            return;
        }
        if(inputFieldComponent != connectionComponent){
            System.out.println("there is some error");
        }


        setToSingle(index).setUserInput(input);
        repaint();

    }

    public LInputField setToSingle(Ludeme ludeme){
        // find index of ludeme
        for(InputInformation i : inputInformationList){
            if(i.getPossibleLudemeInputs().contains(ludeme)){
                return setToSingle(i.getIndex());
            }
        }
        return null;
    }

    public LInputField setToSingle(int inputIndex){
        InputInformation inputInformation = null;
        for(InputInformation ii : inputInformationList){
            if(ii.getIndex() == inputIndex){
                inputInformation = ii;
            }
        }
        if(inputInformation == inputInformationList.get(0)){
            LInputField newInputField = new LInputField(LNC, inputInformation);
            LNC.getInputArea().addInputFieldAbove(newInputField, this);
            inputInformationList.remove(0);
            if(inputInformationList.size() == 1){
                constructInputField(inputInformationList.get(0));
            } else {
                constructInputField(inputInformationList);
            }
            repaint();
            return newInputField;
        }
        if(inputInformation == inputInformationList.get(inputInformationList.size()-1)){
            LInputField newInputField = new LInputField(LNC, inputInformation);
            LNC.getInputArea().addInputFieldBelow(newInputField, this);
            inputInformationList.remove(inputInformationList.size()-1);
            if(inputInformationList.size() == 1){
                constructInputField(inputInformationList.get(0));
            } else {
                constructInputField(inputInformationList);
            }
            repaint();
            return newInputField;
        }
        System.out.println(inputInformation + "; " + this);
        return this;
    }


    public int getInputIndex(){
        if(!isSingle) System.out.println("!!!! INCORRECT USE HERE");
        return inputInformationList.get(0).getIndex();
    }

    public List<Ludeme> getRequiredLudemes(){
        if(isSingle) return inputInformationList.get(0).getPossibleLudemeInputs();

        List<Ludeme> requiredLudemes = new ArrayList<>();
        for(InputInformation inputInformation : inputInformationList){
            requiredLudemes.addAll(inputInformation.getPossibleLudemeInputs());
        }
        return requiredLudemes;
    }

    public LConnectionComponent getConnectionComponent(){
        return connectionComponent;
    }

    public LudemeNodeComponent getLudemeNodeComponent(){
        return LNC;
    }

    public InputInformation getInputInformation(){
        return inputInformationList.get(0);
    }

    public List<InputInformation> getInputInformations(){
        return inputInformationList;
    }

    public List<Integer> getInputIndices(){
        List<Integer> indices = new ArrayList<>();
        for(InputInformation inputInformation : inputInformationList) {
            indices.add(inputInformation.getIndex());
        }
        return indices;
    }

    public boolean isSingle(){
        return isSingle;
    }

    @Override
    public String toString(){
        return "Input Field of " + inputInformationList;
    }


}
