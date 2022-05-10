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
import java.util.Arrays;
import java.util.List;

public class LInputField extends JComponent {

    /*
        A LInputField can either have 1x InputInformation -> Standard Field
                                list of InputInformation -> "Merged" Field
     */

    private final LudemeNodeComponent LNC;
    List<InputInformation> inputInformationList = new ArrayList<>();
    boolean isSingle;

    private static final boolean DEBUG = true;

    public List<LInputField> children = new ArrayList<>(); // list of children in case of collection
    public LInputField parent = null;


    JComponent inputFieldComponent;
    LConnectionComponent connectionComponent;

    public LInputField(LudemeNodeComponent ludemeNodeComponent, InputInformation inputInformation){
        this.LNC = ludemeNodeComponent;
        inputInformationList.add(inputInformation);
        isSingle = true;
        constructInputField(inputInformation);
    }

    public LInputField(LudemeNodeComponent ludemeNodeComponent, List<InputInformation> inputInformationList){
        if(DEBUG) System.out.println("[LIF] constructing " + ludemeNodeComponent.getLudemeNode().getLudeme().getName());
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
        this.parent = parent;
    }

    private void constructCollectionField(LInputField parent){
        JLabel label = new JLabel(parent.getInputInformation().getInput().getName());
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(label);
        LInputButton removeItemButton = new LInputButton(DesignPalette.COLLECTION_REMOVE_ICON_ACTIVE, DesignPalette.COLLECTION_REMOVE_ICON_HOVER);

        add(Box.createHorizontalStrut(10));
        add(removeItemButton);

        removeItemButton.addActionListener(e -> {
            removeCollectionItem();
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
                    addCollectionItem();
                });
            }

            add(Box.createHorizontalStrut(5));
            connectionComponent = new LConnectionComponent(this, label.getPreferredSize().height, (int) (label.getPreferredSize().height * 0.4), false);
            add(connectionComponent);
            inputFieldComponent = connectionComponent;
        }

    }

    private void addCollectionItem(){
        // new item added below last collection item or this (parent)
        LInputField last = null;
        if(children.isEmpty()) last = this;
        else last = children.get(children.size()-1);
        LNC.getInputArea().addInputFieldBelow(new LInputField(LInputField.this), last);
    }
    private void removeCollectionItem(){
        IGraphPanel graphPanel = LNC.getGraphPanel();

        // get current provided input array
        LudemeNode[] oldProvidedInputs = (LudemeNode[]) LNC.getLudemeNode().getProvidedInputs()[getInputIndex()];
        // find this inputfields index in the provided inputs
        int indexToRemove = parent.children.indexOf(this) + 1;
        // decrease provided inputs array size
        LudemeNode[] newProvidedInputs = new LudemeNode[oldProvidedInputs.length - 1];
        for(int i = 0; i < indexToRemove; i++){
            newProvidedInputs[i] = oldProvidedInputs[i];
        }
        for(int i = indexToRemove+1; i < oldProvidedInputs.length; i++){
            newProvidedInputs[i - 1] = oldProvidedInputs[i];
        }

        System.out.println("\u001B[32m"+"Calling from LIF 178"+"\u001B[0m");
        Handler.updateInput(graphPanel.getGraph(), LNC.getLudemeNode(), getInputIndex(), newProvidedInputs);

        graphPanel.removeConnection(LNC.getLudemeNode(), this.getConnectionComponent());

        LNC.getInputArea().removeField(this);
        parent.children.remove(this);
    }


    /*
         Optional arguments merge
     */
    private void constructInputField(List<InputInformation> inputInformationList){
        removeAll();

        JLabel label = new JLabel("Additional Arguments");
        if(inputInformationList.get(0).isOptional()) label = new JLabel("Optional Arguments");
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);

        setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(label);

        add(Box.createHorizontalStrut(5));
        connectionComponent = new LConnectionComponent(this, label.getPreferredSize().height, (int) (label.getPreferredSize().height * 0.4), false);
        add(connectionComponent);
        inputFieldComponent = connectionComponent;

    }

    // Updates the provided input list in the model whenever the mouse moves over this input field TODO: Not a good way to solve this
    MouseListener userInputListener = new MouseAdapter() {
        @Override
        public void mouseExited(MouseEvent e) {
            super.mouseMoved(e);
            updateUserInputs();
        }
    };

    public void updateUserInputs(){
        if(isSingle) {
            System.out.println("[LIF] Updated input " + getInputIndex() + " to " + getUserInput());
            System.out.println("\u001B[32m"+"Calling from LIF 221"+"\u001B[0m");
            Handler.updateInput(LNC.getGraphPanel().getGraph(), LNC.getLudemeNode(), getInputIndex(), getUserInput());
        }
    }

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

        return null;
    }

    public void setUserInput(Object input){
        if(inputInformationList.size() > 1) {
            // TODO: My words: "Incorrect use here", but I do not remember why
        }
        if (getInputInformation().isCollection() && input instanceof LudemeNode[]) {
            // collection inputs are connected to multiple nodes
            LudemeNode[] connectedTo = (LudemeNode[]) input;
            IGraphPanel graphPanel = LNC.getGraphPanel();

            for(int i = 1; i < connectedTo.length; i++){
                // create a new input field for each node
                addCollectionItem();
            }

            for(int i = 0; i < connectedTo.length; i++){
                LudemeNode node = connectedTo[i];
                if(node != null) {
                    // get correct connection component
                    LConnectionComponent connectionComponentChild;
                    int childrenIndex = i-1;
                    if (childrenIndex < 0){
                        connectionComponentChild = connectionComponent;
                    } else {
                        connectionComponentChild = children.get(childrenIndex).getConnectionComponent();
                    }
                    graphPanel.addConnection(connectionComponentChild, graphPanel.getNodeComponent(node).getIngoingConnectionComponent());
                }
            }
        }
        else if(inputFieldComponent == connectionComponent){
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

        setToSingle(index).setUserInput(input);
        repaint();
        System.out.println("abcc  " + Arrays.toString(LNC.getLudemeNode().getProvidedInputs()));
    }

    public LInputField setToSingle(Ludeme ludeme){
        for(InputInformation i : inputInformationList){
            if(i.getPossibleLudemeInputs().contains(ludeme)){
                if(DEBUG) System.out.println("[LIF]: Setting " + ludeme + " to single");
                return setToSingle(i);
            }
        }
        return null;
    }

    public LInputField setToSingle(int inputIndex){
        for(InputInformation ii : inputInformationList){
            if(ii.getIndex() == inputIndex){
                System.out.println("[LIF]: Setting " + ii + "(index="+inputIndex+") to single");
                return setToSingle(ii);
            }
        }
        return this;
    }

    public LInputField setToSingle(InputInformation inputInformation){

        if(DEBUG) System.out.println("[LIF]: ^Setting " + inputInformation + " to single");

        // new single input field is above the "merged" one
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

        // new single input field is below the "merged" one
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

        // new single input field is between two "merged" ones
        LInputField newInputField = new LInputField(LNC, inputInformation);
        // find which input information belongs above/below the new one
        List<InputInformation> above_ii = new ArrayList<>();
        List<InputInformation> below_ii = new ArrayList<>();
        for(InputInformation ii : inputInformationList){
            if(ii.getIndex() < inputInformation.getIndex()){
                above_ii.add(ii);
            } else if(ii.getIndex() > inputInformation.getIndex()){
                below_ii.add(ii);
            }
        }
        LInputField above_lif;
        LInputField below_lif;
        if(above_ii.size() == 1){
            above_lif = new LInputField(LNC, above_ii.get(0));
        }
        else if(above_ii.size() == 0){
            above_lif = null;
        }
        else {
            above_lif = new LInputField(LNC, above_ii);
        }
        if(below_ii.size() == 1){
            below_lif = new LInputField(LNC, below_ii.get(0));
        }
        else if (below_ii.size() == 0){
            below_lif = null;
        }
        else {
            below_lif = new LInputField(LNC, below_ii);
        }
        if(above_lif != null) LNC.getInputArea().addInputFieldAbove(above_lif, this);
        LNC.getInputArea().addInputFieldAbove(newInputField, this);
        if(below_lif != null) LNC.getInputArea().addInputFieldAbove(below_lif, this);
        LNC.getInputArea().removeField(this);
        repaint();
        return newInputField;
    }

    public void addInputInformation(InputInformation inputInformation){
        inputInformationList.add(inputInformation);
    }

    public void removeInputInformation(InputInformation inputInformation){
        inputInformationList.remove(inputInformation);
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
