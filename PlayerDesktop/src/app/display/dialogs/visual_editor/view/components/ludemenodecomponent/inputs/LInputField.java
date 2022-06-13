package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
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
        A LInputField can either have 1x NodeArgument -> Standard Field
                                list of NodeArgument -> "Merged" Field
     */

    private final LudemeNodeComponent LNC;
    List<NodeArgument>nodeArgumentList = new ArrayList<>();
    boolean isSingle;
    public boolean isOptional = false;

    private static final boolean DEBUG = true;

    public List<LInputField> children = new ArrayList<>(); // list of children in case of collection
    public LInputField parent = null;


    JComponent inputFieldComponent;
    LConnectionComponent connectionComponent;
    public JLabel label;

    public LInputField(LudemeNodeComponent ludemeNodeComponent, NodeArgument nodeArgument){
        this.LNC = ludemeNodeComponent;
        nodeArgumentList.add(nodeArgument);
        if(nodeArgument.optional()) isOptional = true;
        isSingle = true;
        constructInputField(nodeArgument);
        setOpaque(false);
    }

    public LInputField(LudemeNodeComponent ludemeNodeComponent, List<NodeArgument> nodeArgumentList){
        if(DEBUG) System.out.println("[LIF] constructing " + ludemeNodeComponent.node().symbol().name());
        this.LNC = ludemeNodeComponent;
        this.nodeArgumentList = nodeArgumentList;
        for(NodeArgument nodeArgument : nodeArgumentList){
            isOptional = true;
            if(!nodeArgument.optional()) {
                isOptional = false;
                break;
            }
        }
        isSingle = false;
        constructInputField(nodeArgumentList);
        setOpaque(false);
    }

    public LInputField(LInputField parent){
        this.LNC = parent.LNC;
        this.isSingle = parent.isSingle;
        this.nodeArgumentList = parent.nodeArgumentList;
        constructCollectionField(parent);
        parent.children.add(this);
        this.parent = parent;
        setOpaque(false);
    }

    private void constructCollectionField(LInputField parent){
        if(parent.getNodeArgument().arg().label() == null){
            label = new JLabel(parent.getNodeArgument().arg().symbol().name());
        } else {
            label = new JLabel(parent.getNodeArgument().arg().label());
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

    private void constructInputField(NodeArgument nodeArgument){
        removeAll();

        NodeArgument input = nodeArgument;
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
            inputFieldComponent.setPreferredSize(new Dimension(((int)((LNC.width()-label.getPreferredSize().width)*0.8)),inputFieldComponent.getPreferredSize().height));
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
        LNC.inputArea().addInputFieldBelow(new LInputField(LInputField.this), last);
    }
    private void removeCollectionItem(){
        IGraphPanel graphPanel = LNC.graphPanel();

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
            Handler.updateInput(graphPanel.graph(), LNC.node(), getInputIndex(), newProvidedInputs);
        }

        graphPanel.ch().removeConnection(LNC.node(), this.getConnectionComponent());

        LNC.inputArea().removeField(this);
        parent.children.remove(this);
    }


    /*
         Optional arguments merge
     */
    private void constructInputField(List<NodeArgument> nodeArgumentList){
        removeAll();

        label = new JLabel("Additional Arguments");
        boolean isOptionalNotAdditional = true;
        for(NodeArgument nodeArgument : nodeArgumentList){
            if(!nodeArgument.optional()){
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
            Handler.updateInput(LNC.graphPanel().graph(), LNC.node(), getInputIndex(), getUserInput());
        }
    }

    /**
     * Returns the user supplied input for an input field
     * @return
     */
    public Object getUserInput(){
        if(nodeArgumentList.size() > 1) System.out.println("!!!! INCORRECT USE HERE");
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
        if(nodeArgumentList.size() > 1) {
            // TODO: My words: "Incorrect use here", but I do not remember why
        }
        if (getNodeArgument().collection() && input instanceof LudemeNode[]) {
            // collection inputs are connected to multiple nodes
            LudemeNode[] connectedTo = (LudemeNode[]) input;
            IGraphPanel graphPanel = LNC.graphPanel();

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
                    graphPanel.ch().addConnection(connectionComponentChild, graphPanel.nodeComponent(node).ingoingConnectionComponent());
                }
            }
        }
        else if(inputFieldComponent == connectionComponent){
            // then its ludeme input
            IGraphPanel graphPanel = LNC.graphPanel();
            graphPanel.ch().addConnection(connectionComponent, graphPanel.nodeComponent((LudemeNode) input).ingoingConnectionComponent());
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
        for(NodeArgument ii : nodeArgumentList){
            if(ii.possibleSymbolInputsExpanded().contains(symbol)){
                if(DEBUG) System.out.println("[LIF]: Setting " + symbol + " to single");
                return setToSingle(ii);
            }
        }
        return null;
    }

    public LInputField setToSingle(int inputIndex){
        for(NodeArgument ii : nodeArgumentList){
            if(ii.indexFirst() == inputIndex){
                System.out.println("[LIF]: Setting " + ii + "(index="+inputIndex+") to single");
                return setToSingle(ii);
            }
        }
        return this;
    }

    public LInputField setToSingle(NodeArgument nodeArgument){

        if(DEBUG) System.out.println("[LIF]: ^Setting " + nodeArgument + " to single");

        // new single input field is above the "merged" one
        if(nodeArgument == nodeArgumentList.get(0)){
            LInputField newInputField = new LInputField(LNC, nodeArgument);
            LNC.inputArea().addInputFieldAbove(newInputField, this);
            nodeArgumentList.remove(0);
            if(nodeArgumentList.size() == 1){
                constructInputField(nodeArgumentList.get(0));
            } else {
                constructInputField(nodeArgumentList);
            }
            repaint();
            return newInputField;
        }

        // new single input field is below the "merged" one
        if(nodeArgument == nodeArgumentList.get(nodeArgumentList.size()-1)){
            LInputField newInputField = new LInputField(LNC, nodeArgument);
            LNC.inputArea().addInputFieldBelow(newInputField, this);
            nodeArgumentList.remove(nodeArgumentList.size()-1);
            if(nodeArgumentList.size() == 1){
                constructInputField(nodeArgumentList.get(0));
            } else {
                constructInputField(nodeArgumentList);
            }
            repaint();
            return newInputField;
        }

        // new single input field is between two "merged" ones
        LInputField newInputField = new LInputField(LNC, nodeArgument);
        // find which input information belongs above/below the new one
        List<NodeArgument> above_ii = new ArrayList<>();
        List<NodeArgument> below_ii = new ArrayList<>();
        for(NodeArgument ii : nodeArgumentList){
            if(ii.indexFirst() < nodeArgument.indexFirst()){
                above_ii.add(ii);
            } else if(ii.indexFirst() > nodeArgument.indexFirst()){
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
        if(above_lif != null) LNC.inputArea().addInputFieldAbove(above_lif, this);
        LNC.inputArea().addInputFieldAbove(newInputField, this);
        if(below_lif != null) LNC.inputArea().addInputFieldAbove(below_lif, this);
        LNC.inputArea().removeField(this);
        repaint();
        return newInputField;
    }

    public void addNodeArgument(NodeArgument nodeArgument){
        nodeArgumentList.add(nodeArgument);
    }

    public void removeNodeArgument(NodeArgument nodeArgument){
        nodeArgumentList.remove(nodeArgument);
    }

    public int getInputIndex(){
        if(!isSingle) System.out.println("!!!! INCORRECT USE HERE");
        return nodeArgumentList.get(0).indexFirst();
    }

    public List<Symbol> getRequiredSymbols(){
        if(isSingle) return nodeArgumentList.get(0).possibleSymbolInputsExpanded();

        List<Symbol> requiredLudemes = new ArrayList<>();
        for(NodeArgument nodeArgument : nodeArgumentList){
            requiredLudemes.addAll(nodeArgument.possibleSymbolInputsExpanded());
        }
        System.out.println(this + " nodeArgument: getPossibleSymbolInputs: " + requiredLudemes);

        return requiredLudemes;
    }

    public LConnectionComponent getConnectionComponent(){
        return connectionComponent;
    }

    public LudemeNodeComponent getLudemeNodeComponent(){
        return LNC;
    }

    public NodeArgument getNodeArgument(){
        return nodeArgumentList.get(0);
    }

    public List<NodeArgument> getNodeArguments(){
        return nodeArgumentList;
    }

    public List<Integer> getInputIndices(){
        List<Integer> indices = new ArrayList<>();
        for(NodeArgument nodeArgument : nodeArgumentList) {
            indices.add(nodeArgument.indexFirst());
        }
        return indices;
    }

    public boolean isSingle(){
        return isSingle;
    }

    @Override
    public String toString(){
        return "Input Field of " + nodeArgumentList;
    }


    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);
    }

}
