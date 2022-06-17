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
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A component represent a ClauseArgument / NodeArgument in the LudemeNodeComponent
 * If a LInputField contains 1x NodeArgument: It's a standard field
 * If a LInputField contains more than 1 NodeArgument: It's a "merged" field, the user can select one of the NodeArguments
 * @author Filipp Dokienko
 */

public class LInputField extends JComponent
{
    /** LudemeNodeComponent that contains this LInputField */
    private final LudemeNodeComponent LNC;
    /** List of NodeArguments that can be selected */
    List<NodeArgument>nodeArgumentList = new ArrayList<>();
    /** Whether this LInputField contains only one NodeArgument */
    boolean isSingle;
    /** Whether this LInputField is optional */
    public boolean isOptional = false;

    /** For NodeArguments that are collections, there is a "parent" LInputField, from which "children" LInputFields are created */
    public LInputField parent = null;
    public List<LInputField> children = new ArrayList<>();

    /** Label of this LInputField */
    private JLabel label;
    /** Label indicating whether this LInputField is optional */
    private final JLabel optionalLabel = new JLabel("(optional)");

    JComponent inputFieldComponent;
    LConnectionComponent connectionComponent;


    private static final boolean DEBUG = true;

    /**
     * Constructor for a LInputField that contains only one NodeArgument
     * @param ludemeNodeComponent
     * @param nodeArgument
     */
    public LInputField(LudemeNodeComponent ludemeNodeComponent, NodeArgument nodeArgument){
        this.LNC = ludemeNodeComponent;
        nodeArgumentList.add(nodeArgument);
        isSingle = true;
        if(nodeArgument.optional()) isOptional = true;
        constructInputField(nodeArgument);
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
    }

    public LInputField(LInputField parent){
        this.LNC = parent.LNC;
        this.isSingle = parent.isSingle;
        this.nodeArgumentList = parent.nodeArgumentList;
        constructCollectionField(parent);
        parent.children.add(this);
        this.parent = parent;
    }


    /**
     * Given a NodeArgument, this method constructs a LInputField
     * @param nodeArgument NodeArgument that is represented by this LInputField
     */
    private void constructInputField(NodeArgument nodeArgument)
    {
        removeAll(); // reset field
        ClauseArg arg = nodeArgument.arg();

        label = new JLabel(arg.symbol().name());
        if(arg.label() != null)
            label = new JLabel(arg.label());

        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);

        // Terminal Node Arguments have no connection component
        if(!nodeArgument.separateNode() && nodeArgument.isTerminal())
        {
            inputFieldComponent = getTerminalComponent(nodeArgument);
            inputFieldComponent.setPreferredSize(new Dimension(((int)((LNC.width()-label.getPreferredSize().width)*0.8)),inputFieldComponent.getPreferredSize().height));
            inputFieldComponent.addPropertyChangeListener(userInputListener_propertyChange);
            inputFieldComponent.addKeyListener(userInputListener_keyListener);

            setLayout(new FlowLayout(FlowLayout.LEFT));
            add(Box.createHorizontalStrut(10)); // TODO: Set in DesignPalette
            add(label);
            add(inputFieldComponent);
        }
        // Non-Terminal Node Arguments have a connection component
        else
        {
            setLayout(new FlowLayout(FlowLayout.RIGHT));
            if(nodeArgument.optional()) add(optionalLabel);
            add(label);

            // if it's an optional argument, make the font italic
            if(nodeArgument.optional())
            {
                optionalLabel.setFont(DesignPalette.LUDEME_INPUT_FONT_ITALIC);
                optionalLabel.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);
                add(optionalLabel);
            }
            else if (nodeArgument.choice())
            {
                label.setText("Choice");
                LInputButton addChoiceButton = new LInputButton(DesignPalette.CHOICE_ICON_ACTIVE, DesignPalette.CHOICE_ICON_HOVER);

                add(Box.createHorizontalStrut(10)); // TODO: Set in DesignPalette
                add(addChoiceButton);
            }
            else if(nodeArgument.collection())
            {
                LInputButton addItemButton = new LInputButton(DesignPalette.COLLECTION_ICON_ACTIVE, DesignPalette.COLLECTION_ICON_HOVER);

                add(Box.createHorizontalStrut(10)); // TODO: Set in DesignPalette
                add(addItemButton);

                addItemButton.addActionListener(e -> {
                    addCollectionItem();
                });
            }

            add(Box.createHorizontalStrut(5)); // TODO: Set in DesignPalette

            // create connection component to connect to another LudemeConnectionComponent
            connectionComponent = new LConnectionComponent(this, label.getPreferredSize().height, (int) (label.getPreferredSize().height * 0.4), false);
            add(connectionComponent);
            inputFieldComponent = connectionComponent;
        }

    }

    /**
     * Given a list of NodeArguments, create one LInputField where the user can decide what NodeArgument to use
     *      Use-Case 1: If the node is dynamic, add all currently available NodeArguments
     *      Use-Case 2: If there are multiple consequent optional arguments, merge them into one field
     * @param nodeArgumentList List of NodeArguments that are represented by this LInputField
     */
    private void constructInputField(List<NodeArgument> nodeArgumentList){
        removeAll();
        // check whether this is a list of only optional arguments
        boolean isOptionalNotAdditional = true;
        for(NodeArgument nodeArgument : nodeArgumentList){
            if(!nodeArgument.optional()){
                isOptionalNotAdditional = false;
            }
        }
        if(isOptionalNotAdditional) label = new JLabel("Optional Arguments");
        else label = new JLabel("Additional Arguments");
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);

        setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(label);

        add(Box.createHorizontalStrut(5));
        connectionComponent = new LConnectionComponent(this, label.getPreferredSize().height, (int) (label.getPreferredSize().height * 0.4), false);
        add(connectionComponent);
        inputFieldComponent = connectionComponent;

    }

    /**
     * Adds a new collection child to LInputField parent
     * @param parent LInputField that is the parent of the new collection child
     */
    private void constructCollectionField(LInputField parent)
    {
        NodeArgument nodeArgument = parent.nodeArgument();
        ClauseArg arg = nodeArgument.arg();

        label = new JLabel(arg.symbol().name());
        if(arg.label() != null)
            label = new JLabel(arg.label());

        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);

        setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(label);

        // Button to remove this collection child
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

    /**
     * Adds this LInputField (children of a collection) below the last child of the parent
     */
    private void addCollectionItem()
    {
        // new item added below last collection item or this (parent)
        LInputField last = null;
        if(children.isEmpty()) last = this;
        else last = children.get(children.size()-1);
        LNC.inputArea().addInputFieldBelow(new LInputField(LInputField.this), last);
        // update provided inputs in handler
        if(LNC.node().providedInputs()[inputIndexFirst()] instanceof LudemeNode[] collection)
        {
            if(collection.length < children.size() + 1)
            {
                Handler.addCollectionElement(LNC.graphPanel().graph(), LNC.node(), inputIndexFirst());
            }
        }
        if(LNC.node().providedInputs()[inputIndexFirst()] == null)
        {
            Handler.addCollectionElement(LNC.graphPanel().graph(), LNC.node(), inputIndexFirst());
        }
    }

    /**
     * Removes this LInputField (children of a collection)
     */
    private void removeCollectionItem()
    {
        // Remove from model
        Handler.removeCollectionElement(LNC.graphPanel().graph(), LNC.node(), inputIndexFirst(), parent.children.indexOf(this) + 1);

        LNC.graphPanel().connectionHandler().removeConnection(LNC.node(), this.getConnectionComponent());

        LNC.inputArea().removeField(this);
        parent.children.remove(this);
    }

    /**
     *
     * @param argument
     * @return A JComponent for a given NordeArgument which is terminal
     */
    private JComponent getTerminalComponent(NodeArgument argument)
    {
        ClauseArg arg = argument.arg();

        // If the argument refers only to Terminal Constants, create a dropdown menu
        if(argument.terminalDropdown())
        {
            JComboBox<Symbol> dropdown = new JComboBox<>();
            dropdown.setFont(DesignPalette.LUDEME_INPUT_FONT);
            dropdown.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);
            for(Symbol s : argument.constantInputs())
            {
                dropdown.addItem(s);
            }
            return dropdown;
        }

        // Otherwise, for every possible Predefined LudemeType create according JComponent
        // TODO: Add remaining
        switch(arg.symbol().name())
        {
            case "Integer":
                return new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
            case "String":
                return new JTextField();
            default:
                return new JTextField("??"); // TODO: This should never happen!
        }
    }

    /**
     * Listens for changes to a terminal component and updates the model accordingly
     */
    PropertyChangeListener userInputListener_propertyChange = new PropertyChangeListener()
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            updateUserInputs();
        }
    };

    /**
     * Listens for changes via keys to a terminal component and updates the model accordingly
     */
    KeyListener userInputListener_keyListener = new KeyListener()
    {
        @Override
        public void keyTyped(KeyEvent e)
        {
            updateUserInputs();
        }

        @Override
        public void keyPressed(KeyEvent e)
        {
            updateUserInputs();
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
        }
    };

    /**
     * Updates the model with the current user input
     * Only works for single input fields
     */
    public void updateUserInputs()
    {
        Handler.updateInput(LNC.graphPanel().graph(), LNC.node(), inputIndexFirst(), getUserInput());
    }

    /**
     *
     * @return the user supplied input for an input field
     * Only works for single input fields
     */
    public Object getUserInput()
    {
        if(!isSingle) return null;
        if(inputFieldComponent == connectionComponent) // Ludeme Input
        {
            return connectionComponent.getConnectedTo().node();
        }
        // Terminal Inputs
        if(inputFieldComponent instanceof JTextField) return ((JTextField)inputFieldComponent).getText();
        if(inputFieldComponent instanceof JSpinner) return ((JSpinner)inputFieldComponent).getValue();
        if(inputFieldComponent instanceof JComboBox) return ((JComboBox)inputFieldComponent).getSelectedItem();

        return null;
    }

    // TODO
    public void setUserInput(Object input)
    {
        if(nodeArgumentList.size() > 1) {
            // TODO: My words: "Incorrect use here", but I do not remember why
        }
        if (nodeArgument().collection() && input instanceof LudemeNode[]) {
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
                    graphPanel.connectionHandler().addConnection(connectionComponentChild, graphPanel.nodeComponent(node).ingoingConnectionComponent());
                }
            }
        }
        else if(inputFieldComponent == connectionComponent){
            // then its ludeme input
            IGraphPanel graphPanel = LNC.graphPanel();
            graphPanel.connectionHandler().addConnection(connectionComponent, graphPanel.nodeComponent((LudemeNode) input).ingoingConnectionComponent());
        }
        if(inputFieldComponent instanceof JTextField) ((JTextField)inputFieldComponent).setText((String)input);
        if(inputFieldComponent instanceof JSpinner) ((JSpinner)inputFieldComponent).setValue(input);
        if(inputFieldComponent instanceof JComboBox) ((JComboBox<?>)inputFieldComponent).setSelectedItem(input);
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

    /**
     * Adds an additional NodeArgument to this input field.
     * @param nodeArgument The NodeArgument to add.
     */
    public void addNodeArgument(NodeArgument nodeArgument)
    {
        nodeArgumentList.add(nodeArgument);
    }

    /**
     *
     * @return The input index of the first NodeArgument in this LInputField
     */
    public int inputIndexFirst()
    {
        return nodeArgumentList.get(0).indexFirst();
    }

    /**
     *
     * @return A list of symbols this input field can be connected to
     */
    public List<Symbol> possibleSymbols()
    {
        if(isSingle) return nodeArgumentList.get(0).possibleSymbolInputsExpanded();

        List<Symbol> possibleSymbols = new ArrayList<>();
        for(NodeArgument nodeArgument : nodeArgumentList)
        {
            possibleSymbols.addAll(nodeArgument.possibleSymbolInputsExpanded());
        }
        System.out.println(this + " nodeArgument: getPossibleSymbolInputs: " + possibleSymbols);

        return possibleSymbols;
    }


    /**
     *
     * @return the connection component used to provide an input to a non-terminal input field
     *        or null if this input field represents a terminal input field
     */
    public LConnectionComponent getConnectionComponent()
    {
        return connectionComponent;
    }

    /**
     *
     * @return the LudemeNodeComponent that this input field is part of
     */
    public LudemeNodeComponent ludemeNodeComponent()
    {
        return LNC;
    }

    /**
     *
     * @return the node argument that this input field is representing
     */
    public NodeArgument nodeArgument()
    {
        return nodeArgumentList.get(0);
    }

    /**
     *
     * @return the list of NodeArguments that are merged into this input field
     */
    public List<NodeArgument> nodeArguments()
    {
        return nodeArgumentList;
    }

    /**
     *
     * @return List of indices of the NodeArguments this input field compromises
     */
    public List<Integer> getInputIndices()
    {
        List<Integer> indices = new ArrayList<>();
        for(NodeArgument nodeArgument : nodeArgumentList) {
            indices.addAll(nodeArgument.indices());
        }
        return indices;
    }

    /**
     *
     * @return whether this input field is a single input field
     */
    public boolean isSingle()
    {
        return isSingle;
    }

    /**
     *
     * @return the JLabel of the inputfield
     */
    public JLabel label()
    {
        return label;
    }

    @Override
    public String toString()
    {
        return "Input Field of " + nodeArgumentList;
    }


    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        // TODO: Required for scaling
        //label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        //label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);
    }

}
