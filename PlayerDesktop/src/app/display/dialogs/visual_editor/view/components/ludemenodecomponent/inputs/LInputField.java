package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.input.Input;
import app.display.dialogs.visual_editor.model.grammar.input.LudemeInput;
import app.display.dialogs.visual_editor.model.grammar.input.Terminal;
import app.display.dialogs.visual_editor.model.grammar.input.TerminalInput;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import gameDistance.utils.apted.node.Node;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

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
    public boolean isOptional = false;

    private static final boolean DEBUG = true;

    public List<LInputField> children = new ArrayList<>(); // list of children in case of collection
    public LInputField parent = null;


    JComponent inputFieldComponent;
    LConnectionComponent connectionComponent;
    public JLabel label;

    public LInputField(LudemeNodeComponent ludemeNodeComponent, InputInformation inputInformation){
        this.LNC = ludemeNodeComponent;
        inputInformationList.add(inputInformation);
        if(inputInformation.optional()) isOptional = true;
        isSingle = true;
        constructInputField(inputInformation);
        setOpaque(false);
    }

    public LInputField(LudemeNodeComponent ludemeNodeComponent, List<InputInformation> inputInformationList){
        if(DEBUG) System.out.println("[LIF] constructing " + ludemeNodeComponent.node().symbol().name());
        this.LNC = ludemeNodeComponent;
        this.inputInformationList = inputInformationList;
        for(InputInformation inputInformation : inputInformationList){
            isOptional = true;
            if(!inputInformation.optional()) {
                isOptional = false;
                break;
            }
        }
        isSingle = false;
        constructInputField(inputInformationList);
        setOpaque(false);
    }

    public LInputField(LInputField parent){
        this.LNC = parent.LNC;
        this.isSingle = parent.isSingle;
        this.inputInformationList = parent.inputInformationList;
        constructCollectionField(parent);
        parent.children.add(this);
        this.parent = parent;
        setOpaque(false);
    }

    private void constructCollectionField(LInputField parent){
        if(parent.getInputInformation().nodeInput().arg().label() == null){
            label = new JLabel(parent.getInputInformation().nodeInput().arg().symbol().name());
        } else {
            label = new JLabel(parent.getInputInformation().nodeInput().arg().label());
        }
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

        NodeInput input = inputInformation.nodeInput();
        Symbol inputSymbol = input.arg().symbol();
        Symbol.LudemeType inputType = inputSymbol.ludemeType();

        /* TODO: Re-enable
        if(input.size() == 1 && inputType.equals(Symbol.LudemeType.Primitive) || inputType.equals(Symbol.LudemeType.Primitive) && !input.arg().optional()){ // TODO: What about type "predefined"?
            input = new NodeInput(inputSymbol.rule().rhs().get(0), inputSymbol.rule().rhs().get(0).args().get(0));
        } else if(input.size() == 1 && inputSymbol.name().equals("Integer")){
            input = new NodeInput(inputSymbol.rule().rhs().get(30), inputSymbol.rule().rhs().get(30).args().get(0));
        } else if(input.size() == 1 && inputSymbol.name().equals("Dimension")){
            input = new NodeInput(inputSymbol.rule().rhs().get(6), inputSymbol.rule().rhs().get(6).args().get(0));
        }*/

        if(input.arg().label() != null){
            label = new JLabel(input.arg().label());
        } else {
            label = new JLabel(input.arg().symbol().name());
        }
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);


        if(inputSymbol.isTerminal()){ // TODO: does that work?
            inputFieldComponent = getTerminalComponent(input.arg());
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

            if(input.arg().optional()){
                LInputButton addOptionalArgumentButton = new LInputButton(DesignPalette.OPTIONAL_ICON_ACTIVE, DesignPalette.OPTIONAL_ICON_HOVER);

                // TODO: Button Listener

                add(Box.createHorizontalStrut(10));
                add(label);
                add(Box.createHorizontalStrut(5));
                add(addOptionalArgumentButton);

            }

            else if (input.size() > 1) {
                label.setText("Choice");
                LInputButton addChoiceButton = new LInputButton(DesignPalette.CHOICE_ICON_ACTIVE, DesignPalette.CHOICE_ICON_HOVER);

                // TODO: Hover
                // TODO: Button Listener

                add(Box.createHorizontalStrut(10));
                add(addChoiceButton);

            }
            else if(input.arg().nesting() > 0){
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

    private JComponent getTerminalComponent(ClauseArg arg){
        if(!arg.symbol().isTerminal()) return null;
        switch(arg.symbol().name()){
            case "Integer":
                return new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
            case "String":
                return new JTextField();
            default:
                if(arg.symbol().ludemeType().equals(Symbol.LudemeType.Structural)) { // TODO: Eh, keine ahnung
                    JComboBox<Symbol> comboBox = new JComboBox<>();
                    if(true) {
                        System.out.println(arg);
                        return new JTextField("!!");
                    }
                    for (ClauseArg ca : arg.symbol().rule().rhs().get(0).args()) {
                        comboBox.addItem(ca.symbol());
                    }
                    return comboBox;
                }
                else return new JTextField("??");
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
        LudemeNode[] oldProvidedInputs = (LudemeNode[]) LNC.node().providedInputs()[getInputIndex()];
        if(oldProvidedInputs != null) {
            // find this inputfields index in the provided inputs
            int indexToRemove = parent.children.indexOf(this) + 1;
            // decrease provided inputs array size
            LudemeNode[] newProvidedInputs = new LudemeNode[oldProvidedInputs.length - 1];
            for (int i = 0; i < indexToRemove; i++) {
                newProvidedInputs[i] = oldProvidedInputs[i];
            }
            for (int i = indexToRemove + 1; i < oldProvidedInputs.length; i++) {
                newProvidedInputs[i - 1] = oldProvidedInputs[i];
            }

            System.out.println("\u001B[32m" + "Calling from LIF 178" + "\u001B[0m");
            Handler.updateInput(graphPanel.getGraph(), LNC.node(), getInputIndex(), newProvidedInputs);
        }

        graphPanel.getCh().removeConnection(LNC.node(), this.getConnectionComponent());

        LNC.getInputArea().removeField(this);
        parent.children.remove(this);
    }


    /*
         Optional arguments merge
     */
    private void constructInputField(List<InputInformation> inputInformationList){
        removeAll();

        label = new JLabel("Additional Arguments");
        boolean isOptionalNotAdditional = true;
        for(InputInformation inputInformation : inputInformationList){
            if(!inputInformation.optional()){
                isOptionalNotAdditional = false;
            }
        }
        if(isOptionalNotAdditional) label = new JLabel("Optional Arguments");
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
            Handler.updateInput(LNC.getGraphPanel().getGraph(), LNC.node(), getInputIndex(), getUserInput());
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
            return connectionComponent.getConnectedTo().node();
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
        if (getInputInformation().collection() && input instanceof LudemeNode[]) {
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
                    graphPanel.getCh().addConnection(connectionComponentChild, graphPanel.getNodeComponent(node).getIngoingConnectionComponent());
                }
            }
        }
        else if(inputFieldComponent == connectionComponent){
            // then its ludeme input
            IGraphPanel graphPanel = LNC.getGraphPanel();
            graphPanel.getCh().addConnection(connectionComponent, graphPanel.getNodeComponent((LudemeNode) input).getIngoingConnectionComponent());
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
        System.out.println("abcc  " + Arrays.toString(LNC.node().providedInputs()));
    }

    public LInputField setToSingle(Symbol symbol){
        for(InputInformation ii : inputInformationList){
            if(ii.getPossibleSymbolInputs().contains(symbol)){
                if(DEBUG) System.out.println("[LIF]: Setting " + symbol + " to single");
                return setToSingle(ii);
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

    public List<Symbol> getRequiredSymbols(){
        if(isSingle) return inputInformationList.get(0).getPossibleSymbolInputs();

        List<Symbol> requiredLudemes = new ArrayList<>();
        for(InputInformation inputInformation : inputInformationList){
            requiredLudemes.addAll(inputInformation.getPossibleSymbolInputs());
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


    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);
    }

}
