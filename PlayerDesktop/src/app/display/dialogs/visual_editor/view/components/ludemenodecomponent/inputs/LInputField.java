package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import static app.display.dialogs.visual_editor.view.DesignPalette.INPUTFIELD_PADDING_LEFT_TERMINAL;
import static app.display.dialogs.visual_editor.view.DesignPalette.INPUTFIELD_PADDING_RIGHT_NONTERMINAL;

/**
 * A component representing one or more NodeArguments
 * @author Filipp dokienko
 */

public class LInputField extends JComponent
{
    /** LInputAreaNew that this LInputFieldNew is associated with */
    private final LInputArea LIA;
    /** NodeArguments that this LInputField is associated with */
    private List<NodeArgument> nodeArguments;
    /** The JComponent the user interacts with to provide input */
    private JComponent fieldComponent;
    /** If the LInputField is not terminal, it has a connection point which is used to connect to other Nodes */
    private LConnectionComponent connectionComponent = null;
    /** If the LInputField is part of a collection (its element), the root/first element of that collection is the parent */
    private LInputField parent = null;
    /** If the LInputField is the root of a collection, store its elements/children in a list */
    private List<LInputField> children = new ArrayList<>();
    /** Label of the InputField */
    private final JLabel label = new JLabel();
    private final JLabel optionalLabel = new JLabel("(optional)");
    private LInputButton uncollapseButton = new LInputButton(DesignPalette.UNCOLLAPSE_ICON, DesignPalette.UNCOLLAPSE_ICON_HOVER);
    private static float buttonWidthPercentage = 1f;

    /**
     * Constructor for a single or merged input field
     * @param LIA Input Area this field belongs to
     * @param nodeArguments NodeArgument(s) this field represents
     */
    public LInputField(LInputArea LIA, List<NodeArgument> nodeArguments)
    {
        this.LIA = LIA;
        this.nodeArguments = nodeArguments;
        optionalLabel.setFont(DesignPalette.LUDEME_INPUT_FONT_ITALIC);
        optionalLabel.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);


        if(nodeArguments.size() == 1)
            construct(nodeArguments.get(0));
        else
            construct(nodeArguments);
    }


    /**
     * Constructor for a single input field
     * @param LIA Input Area this field belongs to
     * @param nodeArgument NodeArgument this field represents
     */
    public LInputField(LInputArea LIA, NodeArgument nodeArgument) {
        this.LIA = LIA;
        this.nodeArguments = new ArrayList<>();
        this.nodeArguments.add(nodeArgument);
        optionalLabel.setFont(DesignPalette.LUDEME_INPUT_FONT_ITALIC);
        optionalLabel.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);

        construct(nodeArgument);
    }

    /**
     * Constructor for a input field which is part of a collection
     * @param parentCollectionInputField Parent InputField of collection
     */
    public LInputField(LInputField parentCollectionInputField)
    {
        this.LIA = parentCollectionInputField.inputArea();
        this.parent = parentCollectionInputField;
        this.nodeArguments = new ArrayList<>(parentCollectionInputField.nodeArguments());
        parent.addChildren(this);
        construct(nodeArgument(0));
    }

    /**
     * Constructs the JComponent representing the LInputField for a single NodeArgument
     * @param nodeArgument
     */
    private void construct(NodeArgument nodeArgument)
    {
        // reset the component
        removeAll();
        // set label text
        label.setText(nodeArgument.arg().symbol().name());
        // if its a choice, add an indication icon
        if(nodeArgument.choice())
        {
            label.setIcon(DesignPalette.CHOICE_ICON_ACTIVE);
            label.setText("Choice");
        }
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);

        // If collection
        if(parent != null)
        {
            constructCollection(parent);
        }
        else if(nodeArgument.isTerminal())
        {
            // If the selected NodeArgument is a terminal NodeArgument stemming from a merged input field (i.e. optional or dynamic)
            // (nodeArguments.get(0).separateNode())
            // Add an option to remove this argument again
            constructTerminal(nodeArgument, nodeArgument.separateNode() || nodeArgument.optional());
        }
        else
        {
            constructNonTerminal(nodeArgument);
        }
    }

    /**
     * Construct the input field for a list of node arguments (optional or dynamic)
     * @param nodeArguments list of node arguments
     */
    private void construct(List<NodeArgument> nodeArguments)
    {
        // reset the component
        removeAll();
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        label.setText("Arguments");
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(DesignPalette.FONT_LUDEME_INPUTS_COLOR);
        add(label);
        // add optional label
        if(optional()) add(optionalLabel);
        add(Box.createHorizontalStrut(INPUTFIELD_PADDING_RIGHT_NONTERMINAL));
        connectionComponent = new LConnectionComponent(this, false);
        fieldComponent = connectionComponent;
        add(connectionComponent);
    }

    /**
     * Constructs a LInputField for a terminal NodeArgument
     * @param nodeArgument NodeArgument to construct a LInputField for
     * @param removable Whether the LInputField can be removed
     */
    private void constructTerminal(NodeArgument nodeArgument, boolean removable)
    {
        fieldComponent = generateTerminalComponent(nodeArgument);
        // set size
        fieldComponent.setPreferredSize(terminalComponentSize());
        // add listeners to update provided inputs when modified
        fieldComponent.addPropertyChangeListener(userInputListener_propertyChange);
        fieldComponent.addKeyListener(userInputListener_keyListener);

        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(Box.createHorizontalStrut(INPUTFIELD_PADDING_LEFT_TERMINAL)); // padding to the left
        add(label);
        add(fieldComponent);
        if(nodeArgument.collection())
        {
            LInputButton addItemButton = new LInputButton(DesignPalette.COLLECTION_ICON_ACTIVE, DesignPalette.COLLECTION_ICON_HOVER);
            addItemButton.setPreferredSize(new Dimension((int) (fieldComponent.getPreferredSize().height*buttonWidthPercentage), (int) (fieldComponent.getPreferredSize().height*buttonWidthPercentage)));
            addItemButton.setSize(addItemButton.getPreferredSize());

            // resize terminal field accordingly to fit
            fieldComponent.setPreferredSize(new Dimension(fieldComponent.getPreferredSize().width-addItemButton.getPreferredSize().width, fieldComponent.getPreferredSize().height));
            fieldComponent.setSize(fieldComponent.getPreferredSize());

            add(addItemButton);

            addItemButton.addActionListener(e -> {
                addCollectionItem();
            });
        }
        if(removable) {
            JLabel removeLabel = new JLabel("X");
            add(removeLabel);
            removeLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    inputArea().removedConnection(LInputField.this);
                }
            });
        }
    }

    /**
     * Constructs a LInputField for a non-terminal NodeArgument
     * @param nodeArgument NodeArgument to construct a LInputField for
     */
    private void constructNonTerminal(NodeArgument nodeArgument)
    {
        // create connection component
        if(connectionComponent == null)
            connectionComponent = new LConnectionComponent(this, false);
        fieldComponent = connectionComponent; // user interacts with the connection component


        setLayout(new FlowLayout(FlowLayout.RIGHT));
        if(nodeArgument.optional()) add(optionalLabel);
        add(label);
        if(nodeArgument.collection())
        {
            LInputButton addItemButton = new LInputButton(DesignPalette.COLLECTION_ICON_ACTIVE, DesignPalette.COLLECTION_ICON_HOVER);
            addItemButton.setPreferredSize(new Dimension((int) (fieldComponent.getPreferredSize().height*buttonWidthPercentage), (int) (fieldComponent.getPreferredSize().height*buttonWidthPercentage)));
            addItemButton.setSize(addItemButton.getPreferredSize());
            
            add(Box.createHorizontalStrut(INPUTFIELD_PADDING_LEFT_TERMINAL));
            add(addItemButton);

            addItemButton.addActionListener(e -> {
                addCollectionItem();
            });
        }
        add(Box.createHorizontalStrut(INPUTFIELD_PADDING_RIGHT_NONTERMINAL)); // padding to the right, distance between label and connection component

        if(collapsed())
        {
            uncollapseButton.setPreferredSize(new Dimension((int) (connectionComponent.getSize().width*buttonWidthPercentage), (int) (connectionComponent.getSize().height*buttonWidthPercentage)));
            uncollapseButton.setSize(uncollapseButton.getPreferredSize());
            uncollapseButton.setActive();

            uncollapseButton.addActionListener(e -> {
                remove(uncollapseButton);
                add(connectionComponent);
                connectionComponent().connectedTo().setCollapsed(false);
                connectionComponent().connectedTo().setVisible(true);
                inputArea().LNC().graphPanel().repaint();
            });
            add(uncollapseButton);
        }
        else
        {
            add(connectionComponent);
        }
    }

    /**
     * Constructs a new collection child/element input field , below the last child of the collection root/parent
     * @param parent
     */
    private void constructCollection(LInputField parent)
    {
        NodeArgument nodeArgument = parent.nodeArgument(0);
        if(nodeArgument.isTerminal())
        {
            constructCollectionTerminal(nodeArgument);
        }
        else
        {
            constructCollectionNonTerminal(nodeArgument);
        }

    }

    private void constructCollectionNonTerminal(NodeArgument nodeArgument)
    {
        if(connectionComponent == null)
            connectionComponent = new LConnectionComponent(this, false);

        fieldComponent = connectionComponent; // user interacts with the connection component

        setLayout(new FlowLayout(FlowLayout.RIGHT));
        if(nodeArgument.optional()) add(optionalLabel);
        add(label);

        LInputButton removeItemButton = new LInputButton(DesignPalette.COLLECTION_REMOVE_ICON_ACTIVE, DesignPalette.COLLECTION_REMOVE_ICON_HOVER);
        removeItemButton.setPreferredSize(new Dimension( (int) (fieldComponent.getPreferredSize().height*buttonWidthPercentage) , (int) (fieldComponent.getPreferredSize().height*buttonWidthPercentage) ) );
        removeItemButton.setSize(removeItemButton.getPreferredSize());

        add(Box.createHorizontalStrut(INPUTFIELD_PADDING_LEFT_TERMINAL));
        add(removeItemButton);

        removeItemButton.addActionListener(e -> {
            removeCollectionItem();
        });

        add(Box.createHorizontalStrut(INPUTFIELD_PADDING_RIGHT_NONTERMINAL)); // padding to the right, distance between label and connection component

        if(collapsed())
        {
            add(uncollapseButton);
            uncollapseButton.setActive();
            uncollapseButton.setPreferredSize(new Dimension((int) (connectionComponent.getSize().width*buttonWidthPercentage), (int) (connectionComponent.getSize().height*buttonWidthPercentage)));
            uncollapseButton.setSize(uncollapseButton.getPreferredSize());

            uncollapseButton.addActionListener(e -> {
                remove(uncollapseButton);
                add(connectionComponent);
                connectionComponent().connectedTo().setCollapsed(false);
                connectionComponent().connectedTo().setVisible(true);
                inputArea().LNC().graphPanel().repaint();
            });
        }
        else if(!nodeArgument.isTerminal())
        {
            add(connectionComponent);
        }
    }

    private void constructCollectionTerminal(NodeArgument nodeArgument)
    {
        fieldComponent = generateTerminalComponent(nodeArgument);
        // set size
        fieldComponent.setPreferredSize(terminalComponentSize());
        // add listeners to update provided inputs when modified
        fieldComponent.addPropertyChangeListener(userInputListener_propertyChange);
        fieldComponent.addKeyListener(userInputListener_keyListener);

        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(Box.createHorizontalStrut(INPUTFIELD_PADDING_LEFT_TERMINAL)); // padding to the left
        add(label);
        add(fieldComponent);

        LInputButton removeItemButton = new LInputButton(DesignPalette.COLLECTION_REMOVE_ICON_ACTIVE, DesignPalette.COLLECTION_REMOVE_ICON_HOVER);
        removeItemButton.setPreferredSize(new Dimension( (int) (fieldComponent.getPreferredSize().height*buttonWidthPercentage) , (int) (fieldComponent.getPreferredSize().height*buttonWidthPercentage) ) );
        removeItemButton.setSize(removeItemButton.getPreferredSize());
        fieldComponent.setPreferredSize(new Dimension(fieldComponent.getPreferredSize().width-removeItemButton.getPreferredSize().width, fieldComponent.getPreferredSize().height));
        fieldComponent.setSize(fieldComponent.getPreferredSize());

        add(removeItemButton);

        removeItemButton.addActionListener(e -> {
            removeCollectionItem();
        });

    }

    /**
     * Adds a children collection input field
     */
    private void addCollectionItem()
    {
        LInputField last; // get last children/element of collection
        if(children.isEmpty()) last = this;
        else last = children.get(children.size()-1);
        inputArea().addInputFieldBelow(new LInputField(this), last); // add children field below last element
        // update provided inputs in handler
        if(isTerminal())
        {
            inputArea().drawInputFields();
            return;
        }
        if(inputArea().LNC().node().providedInputs()[inputIndexFirst()] instanceof Object[])
        {
            Object[] collection = (Object[]) inputArea().LNC().node().providedInputs()[inputIndexFirst()] ;
            if(collection.length < children.size() + 1)
            {
                Handler.addCollectionElement(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), inputIndexFirst());
            }
        }
        if(inputArea().LNC().node().providedInputs()[inputIndexFirst()] == null)
        {
            Handler.addCollectionElement(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), inputIndexFirst());
        }
        inputArea().drawInputFields();
    }

    /**
     * Removes this LInputField (children/element of a collection)
     */
    private void removeCollectionItem()
    {
        Handler.removeCollectionElement(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), inputIndexFirst(), parent.children().indexOf(this) + 1);
        inputArea().LNC().graphPanel().connectionHandler().removeConnection(inputArea().LNC().node(), this.connectionComponent());
        inputArea().LNC().inputArea().removeInputField(this);
        parent.children.remove(this);
        inputArea().drawInputFields();
    }

    /**
     *
     * @param nodeArgument
     * @return a JComponent representing the terminal NodeArgument
     */
    private JComponent generateTerminalComponent(NodeArgument nodeArgument)
    {
        ClauseArg arg = nodeArgument.arg();
        // A DropDown menu of constant symbols
        if(nodeArgument.terminalDropdown())
        {
            JComboBox<Symbol> dropdown = new JComboBox<>();
            for(Symbol symbol : nodeArgument.constantInputs())
            {
                dropdown.addItem(symbol);
            }
            return dropdown;
        }
        // A TextField
        if(arg.symbol().name().equals("String"))
        {
            return new JTextField();
        }
        // A Integer Spinner
        if(arg.symbol().name().equals("Integer"))
        {
            return new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
        }
        // A floating point Spinner
        if(arg.symbol().name().equals("Float"))
        {
            return new JSpinner(new SpinnerNumberModel(1, 0.0, Float.MAX_VALUE, 0.1));
        }
        return new JTextField("Could not generate component: " + arg.symbol().name());
    }


    /**
     * Removes a NodeArgument from the list of NodeArguments
     * @param nodeArgument NodeArgument to remove
     */
    public void removeNodeArgument(NodeArgument nodeArgument)
    {
        nodeArguments.remove(nodeArgument);
        if(nodeArguments.size() == 1) reconstruct();
    }

    /**
     * Adds a NodeArgument to the list of NodeArguments
     * @param nodeArgument
     */
    public void addNodeArgument(NodeArgument nodeArgument)
    {
        if(nodeArguments().contains(nodeArgument)) return;
        if(nodeArgument.size() == 1)
        {
            nodeArguments.add(nodeArgument);
            reconstruct();
        }
        else nodeArguments.add(nodeArgument);
    }

    /**
     * Reconstructs the InputField
     * Used when the NodeArgument list changes from merged to single or vice versa
     */
    public void reconstruct()
    {
        if(nodeArguments.size() == 1)
            construct(nodeArguments.get(0));
        else
            construct(nodeArguments);
    }

    /**
     * Notifies the InputField that the Node it is connected to was collapsed
     */
    public void notifyCollapsed()
    {
        System.out.println("The connection of " + this + " was collapsed!");
        reconstruct();
    }

    /**
     * Listens for changes to a terminal component and updates the model accordingly
     */
    PropertyChangeListener userInputListener_propertyChange = evt -> updateUserInputs();

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
        }

        @Override
        public void keyReleased(KeyEvent e)
        {
        }
    };

    /**
     * Sets the input field to the given value
     * @param input input to set the input field to
     */
    public void setUserInput(Object input)
    {
        if(input == null) return;
        if(nodeArguments.size() > 1) {
            // TODO: My words: "Incorrect use here", but I do not remember why
        }

        if(nodeArgument(0).collection() && input instanceof Object[])
        {
            IGraphPanel graphPanel = inputArea().LNC().graphPanel();
            Object[] inputs = (Object[]) input;
            for(int i = 1; i < inputs.length; i++)
            {
                addCollectionItem();
            }
            for(int i = 0; i < inputs.length; i++)
            {
                Object input_i = inputs[i];
                if(input_i == null) continue;
                if(input_i instanceof LudemeNode)
                {
                    // get correct collection component
                    LConnectionComponent connectionComponentChild;
                    if (i == 0)
                        connectionComponentChild = connectionComponent;
                    else
                        connectionComponentChild = children.get(i-1).connectionComponent();
                    graphPanel.connectionHandler().addConnection(connectionComponentChild, graphPanel.nodeComponent(((LudemeNode)input_i)).ingoingConnectionComponent());
                }
                else
                {
                    if (i == 0)
                        setUserInput(input_i);
                    else
                        children().get(i-1).setUserInput(input_i);
                }
            }
        }
        else if(fieldComponent == connectionComponent)
        {
            // then its ludeme input
            IGraphPanel graphPanel = inputArea().LNC().graphPanel();
            graphPanel.connectionHandler().addConnection(connectionComponent, graphPanel.nodeComponent((LudemeNode) input).ingoingConnectionComponent());
        }
        else {
            System.out.println("else: " + input);
            if (fieldComponent instanceof JTextField) ((JTextField) fieldComponent).setText((String) input);
            if (fieldComponent instanceof JSpinner) ((JSpinner) fieldComponent).setValue(input);
            if (fieldComponent instanceof JComboBox) ((JComboBox<?>) fieldComponent).setSelectedItem(input);
        }
    }

    /**
     * Updates the model with the current user input
     * Only works for single input fields
     */
    private void updateUserInputs()
    {
        if(inputArea().LNC().graphPanel().isBusy()) return;
        if(nodeArguments.get(0).collection()) {
            if (LIA.LNC().node().providedInputs()[inputIndexFirst()] == null) {
                Object[] in = new Object[1];
                in[0] = getUserInput();
                Handler.updateInput(LIA.LNC().graphPanel().graph(), LIA.LNC().node(), inputIndexFirst(), in);
            }
            else
            {
                int index = 0;
                if(parent != null) index = parent.children.indexOf(this)+1;
                Handler.setCollectionInput(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), inputIndexFirst(), getUserInput(), index);
            }
        }
        else {
            Handler.updateInput(LIA.LNC().graphPanel().graph(), LIA.LNC().node(), inputIndexFirst(), getUserInput());
        }
    }

    /**
     *
     * @return the user supplied input for an input field
     */
    public Object getUserInput()
    {
        if(isMerged()) return null;
        if(fieldComponent == connectionComponent) // Ludeme Input
        {
            return connectionComponent.connectedTo().node();
        }

        // Terminal Inputs
        if(fieldComponent instanceof JTextField) return ((JTextField)fieldComponent).getText();
        if(fieldComponent instanceof JSpinner) return ((JSpinner)fieldComponent).getValue();
        if(fieldComponent instanceof JComboBox) return ((JComboBox)fieldComponent).getSelectedItem();

        return null;
    }

    /**
     *
     * @return The preferred size of the terminal LInputField
     */
    private Dimension terminalComponentSize()
    {
        int width = (int) ((LIA.LNC().width()-label.getPreferredSize().width) * 0.8); // 80% of the empty width of the LInputArea
        int height = fieldComponent.getPreferredSize().height;
        return new Dimension(width, height);
    }

    /**
     *
     * @return Whether this input field is a terminal input field
     */
    public boolean isTerminal()
    {
        if(isMerged()) return false; // merged input fields cannot be terminals
        return nodeArgument(0).isTerminal();
    }


    /**
     *
     * @return the list of NodeArguments that this LInputField is associated with
     */
    public List<NodeArgument> nodeArguments()
    {
        return nodeArguments;
    }

    /**
     * @param i index of the NodeArgument that this LInputField is associated with
     * @return the NodeArgument that this LInputField is associated with at index i
     */
    public NodeArgument nodeArgument(int i)
    {
        return nodeArguments.get(i);
    }

    /**
     *
     * @return Whether this LInputFieldNew is associated with more than one NodeArgument
     */
    public boolean isMerged()
    {
        return nodeArguments.size() > 1;
    }

    /**
     *
     * @return whether this input field is optional
     */
    public boolean optional()
    {
        for(NodeArgument nodeArgument : nodeArguments)
        {
            if(!nodeArgument.optional()) return false;
        }
        return true;
    }

    /**
     *
     * @return the connection component used to provide an input to a non-terminal input field
     *        or null if this input field represents a terminal input field
     */
    public LConnectionComponent connectionComponent()
    {
        return connectionComponent;
    }

    /**
     *
     * @return the input area that this LInputField is associated with
     */
    public LInputArea inputArea()
    {
        return LIA;
    }

    /**
     *
     * @return the index of the first node argument
     */
    public int inputIndexFirst()
    {
        return nodeArgument(0).index();
    }

    /**
     *
     * @return list of all node argument indices
     */
    public List<Integer> inputIndices() {
        List<Integer> indices = new ArrayList<>();
        for (NodeArgument nodeArgument : nodeArguments) {
            indices.add(nodeArgument.index());
        }
        return indices;
    }

    /**
     *
     * @return a list of Symbols that can be provided as input to this LInputField
     */
    public List<Symbol> possibleSymbolInputs()
    {
        if(isTerminal()) return null;
        if(!isMerged()) return nodeArgument(0).possibleSymbolInputsExpanded();
        List<Symbol> possibleSymbolInputs = new ArrayList<>();
        for(NodeArgument nodeArgument : nodeArguments)
        {
            possibleSymbolInputs.addAll(nodeArgument.possibleSymbolInputsExpanded());
        }
        return possibleSymbolInputs;
    }

    /**
     *
     * @return The parent/root input field of a collection
     */
    public LInputField parent()
    {
        return parent;
    }

    /**
     *
     * @return The list of elements/children of a collection
     */
    public List<LInputField> children()
    {
        return children;
    }

    /**
     * Adds a children/element input field to the collection
     * @param child Element to add
     */
    private void addChildren(LInputField child)
    {
        children.add(child);
    }

    private boolean collapsed()
    {
        if(connectionComponent().connectedTo() == null) return false;
        return connectionComponent().connectedTo().node().collapsed();
    }

    /**
     *
     * @return the label of this LInputField
     */
    public JLabel label()
    {
        return label;
    }

    @Override
    public String toString()
    {
        return "LIF: " + label.getText() + ", " + nodeArguments;
    }
}
