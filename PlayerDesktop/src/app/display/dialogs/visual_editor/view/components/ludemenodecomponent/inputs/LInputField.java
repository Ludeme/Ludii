package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.designPalettes.DesignPalette;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A component representing one or more NodeArguments
 * @author Filipp dokienko
 */

public class LInputField extends JComponent
{
    /** LInputAreaNew that this LInputFieldNew is associated with */
    private final LInputArea LIA;
    /** NodeArguments that this LInputField is associated with */
    private final List<NodeArgument> nodeArguments;
    /** The JComponent the user interacts with to provide input */
    private JComponent fieldComponent;
    /** If the LInputField is not terminal, it has a connection point which is used to connect to other Nodes */
    private LConnectionComponent connectionComponent = null;
    /** If the LInputField is part of a collection (its element), the root/first element of that collection is the parent */
    private LInputField parent = null;
    /** If the LInputField is the root of a collection, store its elements/children in a list */
    private final List<LInputField> children = new ArrayList<>();
    /** Label of the InputField */
    private final JLabel label = new JLabel();
    private final JLabel optionalLabel = new JLabel("(optional)");
    private final JLabel terminalOptionalLabel = new JLabel("+");
    private final LInputButton expandButton = new LInputButton(Handler.currentPalette().UNCOLLAPSE_ICON(), Handler.currentPalette().UNCOLLAPSE_ICON_HOVER());
    private final LInputButton addItemButton = new LInputButton(Handler.currentPalette().COLLECTION_ICON_ACTIVE(), Handler.currentPalette().COLLECTION_ICON_HOVER());
    private final LInputButton removeItemButton = new LInputButton(Handler.currentPalette().COLLECTION_REMOVE_ICON_ACTIVE(), Handler.currentPalette().COLLECTION_REMOVE_ICON_HOVER());
    private final LInputButton choiceButton = new LInputButton(Handler.currentPalette().CHOICE_ICON_ACTIVE(), Handler.currentPalette().CHOICE_ICON_HOVER());
    private static final float buttonWidthPercentage = 1f;
    private boolean active = true;

    /**
     * Constructor for a single or merged input field
     * @param LIA Input Area this field belongs to
     * @param nodeArguments NodeArgument(s) this field represents
     */
    public LInputField(LInputArea LIA, List<NodeArgument> nodeArguments)
    {
        this.LIA = LIA;
        this.nodeArguments = nodeArguments;

        loadButtons();
        construct();
    }

    /**
     * Constructor for a single input field
     * @param LIA
     * @param nodeArgument
     */
    public LInputField(LInputArea LIA, NodeArgument nodeArgument)
    {
        this.LIA = LIA;
        this.nodeArguments = new ArrayList<>();
        this.nodeArguments.add(nodeArgument);

        loadButtons();
        construct();
    }

    /**
     * Constructor for an input field which is part of a collection
     * @param parentCollectionInputField Parent InputField of collection
     */
    public LInputField(LInputField parentCollectionInputField)
    {
        this.LIA = parentCollectionInputField.inputArea();
        this.parent = parentCollectionInputField;
        this.nodeArguments = new ArrayList<>(parentCollectionInputField.nodeArguments());

        loadButtons();

        parent.addChildren(this);
        construct(nodeArgument(0));
    }


    /**
     * Loads all buttons their listeners
     */
    private void loadButtons()
    {
        optionalLabel.setFont(DesignPalette.LUDEME_INPUT_FONT_ITALIC);
        optionalLabel.setForeground(Handler.currentPalette().FONT_LUDEME_INPUTS_COLOR());
        optionalLabel.setText("(optional)");

        if(terminalOptionalLabel.getMouseListeners().length == 0)
            terminalOptionalLabel.addMouseListener(terminalOptionalLabelListener);

        expandButton.setSize(expandButton.getPreferredSize());
        expandButton.setActive();
        expandButton.addActionListener(expandButtonListener);

        addItemButton.addActionListener(addItemButtonListener);
        removeItemButton.addActionListener(removeItemButtonListener);

        choiceButton.addActionListener(choiceButtonListener);
    }

    private final ActionListener expandButtonListener = e ->
    {
        remove(expandButton);
        add(connectionComponent);
        Handler.collapseNode(inputArea().LNC().graphPanel().graph(), connectionComponent().connectedTo().node(), false);
    };

    private final ActionListener addItemButtonListener = e -> addCollectionItem();
    private final ActionListener removeItemButtonListener = e -> removeCollectionItem();
    private final ActionListener choiceButtonListener = e -> {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem[] items = new JMenuItem[nodeArgument(0).size()];

        for(int i = 0; i < nodeArgument(0).size(); i++)
        {
            items[i] = new JMenuItem(nodeArgument(0).args().get(i).toString());
            int finalI = i;
            items[i].addActionListener(e1 ->
            {
                // ask user for confirmation
                if(children.size()+1 >= Handler.SENSITIVITY_COLLECTION_REMOVAL)
                {
                    int userChoice = JOptionPane.showConfirmDialog(null, "When changing the argument " + (children.size() + 1) + " collection elements will be reset.", "Remove nodes", JOptionPane.OK_CANCEL_OPTION);
                    if(userChoice != JOptionPane.OK_OPTION)
                    {
                        return;
                    }
                }

                inputArea().LNC().graphPanel().setBusy(true);
                // if was collection, remove all children
                if(children.size() > 0)
                    removeAllChildren();

                nodeArgument(0).setActiveChoiceArg(nodeArgument(0).args().get(finalI));
                notifyActivated();


                if (getUserInput() != null)
                    if(getUserInput() instanceof LudemeNode)
                        Handler.removeEdge(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(),(LudemeNode) getUserInput());
                    else
                        Handler.updateInput(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), nodeArgument(0), null);
                reconstruct();
                inputArea().LNC().graphPanel().setBusy(false);
                inputArea().repaint();
            });
            popup.add(items[i]);
        }

        popup.show(choiceButton, 0, 0);
    };

    private void removeAllChildren()
    {
        boolean handlerRecording = Handler.recordUserActions;
        if(handlerRecording)
            Handler.recordUserActions = false;
        for (LInputField child : new ArrayList<>(children))
            removeChildrenEdge(child);
        for(LInputField child : new ArrayList<>(children))
            try
            {
                inputArea().removeInputField(child);
            }
            catch (Exception ignored) {}
        children.clear();
        inputArea().drawInputFields();
        if(handlerRecording)
            Handler.recordUserActions = true;
    }

    private void removeChildrenEdge(LInputField child)
    {
        if(child.connectionComponent != null && child.connectionComponent.connectedTo() != null)
            Handler.removeEdge(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), child.connectionComponent.connectedTo().node(), child.elementIndex());
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
        if(nodeArgument.arg().actualParameterName() != null)
            label.setText(nodeArgument.arg().actualParameterName());
        else
            label.setText(nodeArgument.arg().symbol().name());
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(Handler.currentPalette().FONT_LUDEME_INPUTS_COLOR());

        label.setToolTipText(nodeArgument(0).parameterDescription());
        optionalLabel.setToolTipText(nodeArgument(0).parameterDescription());

        if(nodeArgument.optional())
            active = false;

        // If collection
        if(parent != null)
            constructCollection(parent);
        else if(nodeArgument.collection2D())
            constructNonTerminal(nodeArgument);
        else if(nodeArgument.canBePredefined())
            constructHybrid(nodeArgument);
        else if(nodeArgument.isTerminal())
            // If the selected NodeArgument is a terminal NodeArgument stemming from a merged input field (i.e. optional or dynamic)
            // (nodeArguments.get(0).separateNode())
            // Add an option to remove this argument again
            constructTerminal(nodeArgument, nodeArgument.optional());
        else
            constructNonTerminal(nodeArgument);

    }

    /**
     * Construct the input field for a list of node arguments (optional or dynamic)
     */
    private void construct()
    {
        if(nodeArguments.size() == 1)
        {
            construct(nodeArgument(0));
            return;
        }
        // reset the component
        removeAll();
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        // add optional label
        if(optional())
            add(optionalLabel);
        label.setText("Arguments");
        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        label.setForeground(Handler.currentPalette().FONT_LUDEME_INPUTS_COLOR());
        add(label);
        add(Box.createHorizontalStrut(DesignPalette.INPUTFIELD_PADDING_RIGHT_NONTERMINAL));
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

        // check whether a input already exists to auto-fill it
        Object input = inputArea().LNC().node().providedInputsMap().get(nodeArgument);

        fieldComponent = generateTerminalComponent(nodeArgument);
        updateUserInputs();
        initializeFieldComponent();


        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(Box.createHorizontalStrut(DesignPalette.INPUTFIELD_PADDING_LEFT_TERMINAL)); // padding to the left
        add(label);
        add(fieldComponent);

        if(nodeArgument.choice())
        {
            choiceButton.setPreferredSize(buttonSize());
            choiceButton.setSize(choiceButton.getPreferredSize());

            // resize terminal field accordingly to fit
            fieldComponent.setPreferredSize(new Dimension(fieldComponent.getPreferredSize().width-choiceButton.getPreferredSize().width, fieldComponent.getPreferredSize().height));
            fieldComponent.setSize(fieldComponent.getPreferredSize());
            add(choiceButton);
            adjustFieldComponentSize(choiceButton);
        }


        if(nodeArgument.collection())
        {
            addItemButton.setPreferredSize(buttonSize());
            addItemButton.setSize(addItemButton.getPreferredSize());
            add(addItemButton);
            adjustFieldComponentSize(addItemButton);
        }
        if(removable)
        {
            add(terminalOptionalLabel);
            adjustFieldComponentSize(terminalOptionalLabel);
            if(inputArea().LNC().node().providedInputsMap().get(nodeArgument(0)) != null)
            {
                notifyActivated();
            }
            else
            {
                fieldComponent.setEnabled(false);
                label.setEnabled(false);
                terminalOptionalLabel.setText("+");
            }
        }

        if(input != null)
        {
            if(nodeArgument.collection())
            {
                Object[] inputArray = (Object[]) input;
                int elementIndex = 0;
                if(parent != null)
                {
                    elementIndex = parent.children.indexOf(this) + 1;
                }
                if(inputArray[elementIndex] != null)
                {
                    setUserInput(inputArray[elementIndex]);
                }
            }
            else
            {
                setUserInput(input);
            }
        }

        if(input == null && removable)
        {
            fieldComponent.setEnabled(false);
            addItemButton.setEnabled(false);
            active = false;
        }

    }

    private void initializeFieldComponent()
    {
        // set size
        fieldComponent.setPreferredSize(terminalComponentSize());

        // add listeners to update provided inputs when modified

        if(fieldComponent instanceof JTextField)
        {
            for(KeyListener listener : fieldComponent.getKeyListeners().clone())
            {
                fieldComponent.removeKeyListener(listener);
            }
            fieldComponent.addKeyListener(userInputListener_keyListener);
        }
        else if(fieldComponent instanceof JSpinner)
        {
            for(ChangeListener listener : ((JSpinner)fieldComponent).getChangeListeners().clone())
            {
                ((JSpinner) fieldComponent).removeChangeListener(listener);
            }
            ((JSpinner) fieldComponent).addChangeListener(userInputListener_change);
        }
        else if(fieldComponent instanceof JComboBox)
        {
            for(ActionListener listener : ((JComboBox<?>)fieldComponent).getActionListeners().clone())
            {
                ((JComboBox<?>) fieldComponent).removeActionListener(listener);
            }
            ((JComboBox<?>) fieldComponent).addActionListener(userInputListener_dropdown);
        }
        else
        {
            for(PropertyChangeListener listener : fieldComponent.getPropertyChangeListeners().clone())
            {
                fieldComponent.removePropertyChangeListener(listener);
            }
            fieldComponent.addPropertyChangeListener(userInputListener_propertyChange);
        }

        loadFieldComponentColours();

    }

    private void loadFieldComponentColours()
    {
        if(fieldComponent == connectionComponent)
        {
            return;
        }
        if(fieldComponent instanceof JTextField)
        {
            fieldComponent.setBackground(Handler.currentPalette().INPUT_FIELD_BACKGROUND());
            fieldComponent.setForeground(Handler.currentPalette().INPUT_FIELD_FOREGROUND());
        }
        else if(fieldComponent instanceof JSpinner)
        {

            ((JSpinner) fieldComponent).getEditor().getComponent(0).setBackground(Handler.currentPalette().INPUT_FIELD_BACKGROUND());
            ((JSpinner) fieldComponent).getEditor().getComponent(0).setForeground(Handler.currentPalette().INPUT_FIELD_FOREGROUND());
        }
        else if(fieldComponent instanceof JComboBox)
        {
            ((JComboBox<?>) fieldComponent).getEditor().getEditorComponent().setBackground(Handler.currentPalette().INPUT_FIELD_BACKGROUND());
            ((JComboBox<?>) fieldComponent).getEditor().getEditorComponent().setForeground(Handler.currentPalette().INPUT_FIELD_FOREGROUND());
        }
        else
        {
            fieldComponent.setBackground(Handler.currentPalette().INPUT_FIELD_BACKGROUND());
            fieldComponent.setForeground(Handler.currentPalette().INPUT_FIELD_FOREGROUND());
        }
        Border b = new LineBorder(Handler.currentPalette().INPUT_FIELD_BORDER_COLOUR(), 1);
        fieldComponent.setBorder(b);
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
        if(nodeArgument.optional())
            add(optionalLabel);
        add(label);

        if(nodeArgument.choice())
        {
            choiceButton.setPreferredSize(buttonSize());
            choiceButton.setSize(choiceButton.getPreferredSize());
            add(choiceButton);
        }

        if(nodeArgument.collection())
        {
            addItemButton.setPreferredSize(buttonSize());
            addItemButton.setSize(addItemButton.getPreferredSize());
            
            add(Box.createHorizontalStrut(DesignPalette.INPUTFIELD_PADDING_RIGHT_NONTERMINAL));
            add(addItemButton);
        }
        add(Box.createHorizontalStrut(DesignPalette.INPUTFIELD_PADDING_RIGHT_NONTERMINAL)); // padding to the right, distance between label and connection component

        if(collapsed())
        {
            expandButton.setActive();
            expandButton.setPreferredSize(buttonSize());
            expandButton.setSize(expandButton.getPreferredSize());
            add(expandButton);
        }
        else
        {
            add(connectionComponent);
        }
        if(nodeArgument.optional() && nodeArgument.collection())
        {
            add(Box.createHorizontalStrut(DesignPalette.INPUTFIELD_PADDING_RIGHT_NONTERMINAL));
            add(terminalOptionalLabel);
            adjustFieldComponentSize(terminalOptionalLabel);
        }
    }

    public void constructHybrid(NodeArgument nodeArgument)
    {

        Object input = inputArea().LNC().node().providedInputsMap().get(nodeArgument);

        // create connection component
        if(connectionComponent == null)
            connectionComponent = new LConnectionComponent(this, false);

        fieldComponent = generateTerminalComponent(nodeArgument);
        updateUserInputs();
        initializeFieldComponent();

        setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(label);
        add(fieldComponent);

        if(nodeArgument.choice())
        {
            choiceButton.setPreferredSize(buttonSize());
            choiceButton.setSize(choiceButton.getPreferredSize());
            add(choiceButton);
            adjustFieldComponentSize(choiceButton);
        }

        if(nodeArgument.collection())
        {
            addItemButton.setPreferredSize(buttonSize());
            addItemButton.setSize(addItemButton.getPreferredSize());

            add(addItemButton);
            adjustFieldComponentSize(addItemButton);
        }

        if(collapsed())
        {
            expandButton.setActive();
            expandButton.setPreferredSize(buttonSize());
            expandButton.setSize(expandButton.getPreferredSize());
            add(expandButton);
            adjustFieldComponentSize(expandButton);
        }
        else
        {
            add(connectionComponent);
            adjustFieldComponentSize(connectionComponent);
        }

        if(input != null)
        {
            if(nodeArgument.collection())
            {
                Object[] inputArray = (Object[]) input;
                int elementIndex = 0;
                if(parent != null)
                {
                    elementIndex = parent.children.indexOf(this) + 1;
                }
                if(inputArray[elementIndex] != null)
                {
                    setUserInput(inputArray[elementIndex]);
                }
            }
            else
            {
                setUserInput(input);
            }
        }

        if(input == null && nodeArgument.optional())
        {
            fieldComponent.setEnabled(false);
            addItemButton.setEnabled(false);
            active = false;
        }

    }


    private void adjustFieldComponentSize(Component otherComponent)
    {
        fieldComponent.setPreferredSize(new Dimension(fieldComponent.getPreferredSize().width - otherComponent.getPreferredSize().width, fieldComponent.getPreferredSize().height));
        fieldComponent.setSize(fieldComponent.getPreferredSize());
    }

    /**
     * Constructs a new collection child/element input field , below the last child of the collection root/parent
     * @param parent
     */
    private void constructCollection(LInputField parent)
    {
        NodeArgument nodeArgument = parent.nodeArgument(0);
        if(nodeArgument.canBePredefined() && !nodeArgument.collection2D())
            constructHybridCollection(nodeArgument);
        else if(!nodeArgument.isTerminal() || nodeArgument.collection2D())
            constructCollectionNonTerminal(nodeArgument);
        else
            constructCollectionTerminal(nodeArgument);

    }

    private void constructCollectionNonTerminal(NodeArgument nodeArgument)
    {
        if(connectionComponent == null)
            connectionComponent = new LConnectionComponent(this, false);

        fieldComponent = connectionComponent; // user interacts with the connection component

        setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(label);
        removeItemButton.setPreferredSize(buttonSize());
        removeItemButton.setSize(removeItemButton.getPreferredSize());

        add(Box.createHorizontalStrut(DesignPalette.INPUTFIELD_PADDING_LEFT_TERMINAL));
        add(removeItemButton);

        add(Box.createHorizontalStrut(DesignPalette.INPUTFIELD_PADDING_RIGHT_NONTERMINAL)); // padding to the right, distance between label and connection component

        if(collapsed())
        {
            expandButton.setActive();
            expandButton.setPreferredSize(buttonSize());
            expandButton.setSize(expandButton.getPreferredSize());
            add(expandButton);
        }

        add(connectionComponent);

    }

    private void constructCollectionTerminal(NodeArgument nodeArgument)
    {
        // check whether a input already exists to auto-fill it
        Object input = inputArea().LNC().node().providedInputsMap().get(nodeArgument);

        fieldComponent = generateTerminalComponent(nodeArgument);
        updateUserInputs();
        initializeFieldComponent();

        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(Box.createHorizontalStrut(DesignPalette.INPUTFIELD_PADDING_LEFT_TERMINAL)); // padding to the left
        add(label);
        add(fieldComponent);

        removeItemButton.setPreferredSize(buttonSize());
        removeItemButton.setSize(removeItemButton.getPreferredSize());
        add(removeItemButton);
        adjustFieldComponentSize(removeItemButton);

        if(input != null)
        {
            if(nodeArgument.collection())
            {
                Object[] inputArray = (Object[]) input;
                int elementIndex = 0;
                if(parent != null) elementIndex = parent.children.indexOf(this) + 1;
                if(inputArray[elementIndex] != null)
                    setUserInput(inputArray[elementIndex]);
            }
            else
            {
                setUserInput(input);
            }
        }

    }

    public void constructHybridCollection(NodeArgument nodeArgument)
    {
        // create connection component
        if(connectionComponent == null)
            connectionComponent = new LConnectionComponent(this, false);

        fieldComponent = generateTerminalComponent(nodeArgument);
        updateUserInputs();
        initializeFieldComponent();

        setLayout(new FlowLayout(FlowLayout.RIGHT));
        add(label);
        add(fieldComponent);

        removeItemButton.setPreferredSize(buttonSize());
        removeItemButton.setSize(removeItemButton.getPreferredSize());

        add(removeItemButton);
        adjustFieldComponentSize(removeItemButton);

        if(collapsed())
        {
            expandButton.setActive();
            expandButton.setPreferredSize(buttonSize());
            expandButton.setSize(expandButton.getPreferredSize());
            add(expandButton);
            adjustFieldComponentSize(expandButton);
        }
        else
        {
            add(connectionComponent);
            adjustFieldComponentSize(connectionComponent);
        }
    }

    /**
     * Adds a children collection input field
     */
    public void addCollectionItem()
    {
        Handler.addCollectionElement(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), nodeArgument(0));
    }

    public void notifyCollectionAdded()
    {
        LInputField last; // get last children/element of collection
        if(children.isEmpty()) last = this;
        else last = children.get(children.size()-1);
        inputArea().addInputFieldBelow(new LInputField(this), last); // add children field below last element
        inputArea().drawInputFields();
    }

    /**
     * Removes this LInputField (children/element of a collection)
     */
    private void removeCollectionItem()
    {
        Handler.removeCollectionElement(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), nodeArgument(0), parent.children().indexOf(this) + 1);
    }

    public void notifyCollectionRemoved()
    {
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
            if(inputArea().LNC().isPartOfDefine())
                dropdown.addItem(Handler.PARAMETER_SYMBOL);
            for(Symbol symbol : nodeArgument.constantInputs())
                dropdown.addItem(symbol);
            return dropdown;
        }
        // A TextField
        if(arg.symbol().name().equals("String"))
            return new JTextField();
        // A Integer Spinner
        if(arg.symbol().name().equals("Integer") || arg.symbol().name().equals("int") || arg.symbol().token().equals("dim"))
        {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
            ((DefaultFormatter) ((JFormattedTextField) spinner.getEditor().getComponent(0)).getFormatter()).setCommitsOnValidEdit(true);
            return spinner;
        }
        // A floating point Spinner
        if(arg.symbol().token().equals("float"))
        {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0.01, Float.MAX_VALUE, 0.1));
            ((DefaultFormatter) ((JFormattedTextField) spinner.getEditor().getComponent(0)).getFormatter()).setCommitsOnValidEdit(true);
            return spinner;
        }

        if(arg.symbol().token().equals("boolean"))
        {
            JComboBox<Symbol> dropdown = new JComboBox<>();
            if(inputArea().LNC().isPartOfDefine()) dropdown.addItem(Handler.PARAMETER_SYMBOL);
            dropdown.addItem(new Symbol(Symbol.LudemeType.Constant, "True", "True", null));
            dropdown.addItem(new Symbol(Symbol.LudemeType.Constant, "False", "False", null));
            return dropdown;
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
     * Reconstructs the InputField
     * Used when the NodeArgument list changes from merged to single or vice versa
     */
    public void reconstruct()
    {
        if(nodeArguments.size() == 1)
            construct(nodeArguments.get(0));
        else
            construct();
    }

    /**
     * Notifies the InputField that the Node it is connected to was collapsed
     */
    public void notifyCollapsed()
    {
        System.out.println("The connection of " + this + " was collapsed!");
        reconstruct();
    }

    public boolean isActive()
    {
        return active;
    }

    public void activate()
    {
        if(!isTerminal())
            return;
        Handler.activateOptionalTerminalField(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), nodeArgument(0), true);
    }

    public void notifyActivated()
    {
        active = true;
        fieldComponent.setEnabled(true);
        fieldComponent.repaint();
        label.setEnabled(true);
        addItemButton.setEnabled(true);
        terminalOptionalLabel.setText("X");
        if(!nodeArgument(0).collection())
        {
            Handler.updateInput(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), nodeArgument(0), getUserInput());
        }
        repaint();
    }

    public void activateHybrid(boolean activate)
    {
        if(!isHybrid()) return;
        fieldComponent.setEnabled(activate);
        fieldComponent.setVisible(activate);
        if(activate)
        {
            LudemeNode node = inputArea().LNC().node();
            if(node.providedInputsMap().get(nodeArgument(0)) == null)
                Handler.updateInput(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), nodeArgument(0), getUserInput());
            else if(node.providedInputsMap().get(nodeArgument(0)) instanceof Object[])
                Handler.updateCollectionInput(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), nodeArgument(0), getUserInput(), elementIndex());
        }
        repaint();
    }

    public void deactivate()
    {
        if(!isTerminal()) return;
        Handler.activateOptionalTerminalField(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), nodeArgument(0), false);
    }

    public void notifyDeactivated()
    {
        active = false;
        inputArea().LNC().graphPanel().setBusy(true);

        fieldComponent.setEnabled(false);
        addItemButton.setEnabled(false);
        label.setEnabled(false);
        terminalOptionalLabel.setText("+");

        inputArea().removedConnection(LInputField.this);

        boolean hasChildren = children.size() > 0;
        for(LInputField child : new ArrayList<>(children))
            inputArea().removeInputField(child);

        // notify handler
        Handler.updateInput(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), nodeArgument(0), null);
        inputArea().LNC().graphPanel().setBusy(false);
        children.clear();
        if(hasChildren)
            inputArea().drawInputFields();
        repaint();
    }

    public int elementIndex()
    {
        if(!nodeArgument(0).collection())
            return -1;
        if(nodeArgument(0).collection() && parent() == null)
            return 0;
        return parent().children.indexOf(this)+1;
    }

    /**
     * Listens for changes to a terminal component and updates the model accordingly
     */
    final PropertyChangeListener userInputListener_propertyChange = evt -> updateUserInputs();

    final ChangeListener userInputListener_change = evt ->
    {
        ((JFormattedTextField) ((JSpinner) fieldComponent).getEditor().getComponent(0)).setValue(((JSpinner) fieldComponent).getValue());
        updateUserInputs();
    };

    final ActionListener userInputListener_dropdown = evt -> updateUserInputs();

    /**
     * Listens for changes via keys to a terminal component and updates the model accordingly
     */
    final KeyListener userInputListener_keyListener = new KeyListener()
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
            updateUserInputs();
        }
    };

    final MouseAdapter terminalOptionalLabelListener = new MouseAdapter()
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            super.mouseClicked(e);
            if(isActive())
                deactivate();
            else
                activate();
        }
    };

    /**
     * Sets the input field to the given value
     * @param input input to set the input field to
     */
    public void setUserInput(Object input)
    {
        if(input == null)
            return;

        if(nodeArgument(0).collection() && input instanceof Object[])
        {
            IGraphPanel graphPanel = inputArea().LNC().graphPanel();
            Object[] inputs = (Object[]) input;
            for(int i = 1; i < inputs.length; i++)
                addCollectionItem();

            for(int i = 0; i < inputs.length; i++)
            {
                Object input_i = inputs[i];
                if(input_i == null)
                {
                    continue;
                }
                if(input_i instanceof LudemeNode)
                {
                    // get correct collection component
                    LConnectionComponent connectionComponentChild;
                    if (i == 0)
                    {
                        connectionComponentChild = connectionComponent;
                    }
                    else
                    {
                        connectionComponentChild = children.get(i-1).connectionComponent();
                    }
                    Handler.addEdge(graphPanel.graph(), connectionComponentChild.inputField().inputArea().LNC().node(), (LudemeNode) input_i, connectionComponentChild.inputField().nodeArgument(0));
                }
                else
                {
                    if (i == 0)
                    {
                        setUserInput(input_i);
                    }
                    else
                    {
                        children().get(i-1).setUserInput(input_i);
                    }
                }
            }
        }
        else if(fieldComponent == connectionComponent)
        {
            // then its ludeme input
            Handler.addEdge(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), (LudemeNode) input, nodeArgument(0));
            if(((LudemeNode) input).collapsed())
            {
                notifyCollapsed();
            }
        }
        else
        {
            if (fieldComponent instanceof JTextField)
            {
                if (input instanceof String)
                {
                    ((JTextField) fieldComponent).setText((String) input);
                }
                else
                {
                    ((JTextField) fieldComponent).setText("");
                }
            }
            if (fieldComponent instanceof JSpinner)
            {
                if(input instanceof Integer)
                {
                    ((JSpinner) fieldComponent).setValue(input);
                }
            }
            if (fieldComponent instanceof JComboBox && input instanceof Symbol)
            {
                ((JComboBox<?>) fieldComponent).setSelectedItem(input);
            }
        }
    }

    /**
     * Updates the model with the current user input
     * Only works for single input fields
     */
    public void updateUserInputs()
    {
        if(inputArea().LNC().graphPanel().isBusy())
            return;
        if(nodeArguments.get(0).collection() && parent() == null && !isActive())
            return;
        if(nodeArguments.get(0).collection() && parent() != null && !parent().isActive())
            return;
        if(nodeArguments.get(0).collection())
        {
            if (inputArea().LNC().node().providedInputsMap().get(nodeArgument(0)) == null)
            {
                Object[] in = new Object[1];
                in[0] = getUserInput();
                Handler.updateInput(LIA.LNC().graphPanel().graph(), LIA.LNC().node(), nodeArgument(0), in); // TODO: Verify this works
            }
            else
            {
                int index = 0;
                if(parent != null) index = parent.children.indexOf(this)+1;
                Handler.updateCollectionInput(inputArea().LNC().graphPanel().graph(), inputArea().LNC().node(), nodeArgument(0), getUserInput(), index);
            }
        }
        else
            Handler.updateInput(LIA.LNC().graphPanel().graph(), LIA.LNC().node(), nodeArgument(0), getUserInput());
    }

    /**
     *
     * @return the user supplied input for an input field
     */
    public Object getUserInput()
    {
        if(isMerged())
            return null;
        if(parent() == null && optional() && !isActive())
            return null;
        if(fieldComponent == connectionComponent) // Ludeme Input
        {
            if(connectionComponent.connectedTo() == null)
                return null;
            return connectionComponent.connectedTo().node();
        }

        // Terminal Inputs
        if(fieldComponent instanceof JTextField)
            return ((JTextField)fieldComponent).getText();
        if(fieldComponent instanceof JSpinner)
            return ((JSpinner)fieldComponent).getValue();
        if(fieldComponent instanceof JComboBox)
            return ((JComboBox<?>)fieldComponent).getSelectedItem();

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

    private Dimension buttonSize()
    {
        if(label != null && !label.getText().equals(("")))
            return new Dimension((int) (label().getPreferredSize().height*buttonWidthPercentage), (int) (label().getPreferredSize().height*buttonWidthPercentage));
        if(fieldComponent != null)
            return new Dimension((int) (fieldComponent.getPreferredSize().height*buttonWidthPercentage), (int) (fieldComponent.getPreferredSize().height*buttonWidthPercentage));
        else if(connectionComponent != null)
            return new Dimension((int) (connectionComponent.getSize().width*buttonWidthPercentage), (int) (connectionComponent.getSize().height*buttonWidthPercentage));
        else return null;
    }

    /**
     *
     * @return Whether this input field is a terminal input field
     */
    public boolean isTerminal()
    {
        if(isMerged())
            return false; // merged input fields cannot be terminals
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

    public boolean isHybrid()
    {
        return nodeArguments.get(0).canBePredefined();
    }

    /**
     *
     * @return whether this input field is optional
     */
    public boolean optional()
    {
        for(NodeArgument nodeArgument : nodeArguments)
            if(!nodeArgument.optional())
                return false;
        return true;
    }

    /**
     *
     * @return whether this input field is a choice
     */
    public boolean choice()
    {
        return nodeArgument(0).choice();
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
     * @return list of all node argument indices
     */
    public List<Integer> inputIndices() {
        List<Integer> indices = new ArrayList<>();
        for (NodeArgument nodeArgument : nodeArguments)
            indices.add(nodeArgument.index());
        return indices;
    }

    /**
     *
     * @return a list of Symbols that can be provided as input to this LInputField
     */
    public List<Symbol> possibleSymbolInputs()
    {
        if(isTerminal())
            return null;
        if(!isMerged())
            return nodeArgument(0).possibleSymbolInputsExpanded();
        List<Symbol> possibleSymbolInputs = new ArrayList<>();
        for(NodeArgument nodeArgument : nodeArguments)
            possibleSymbolInputs.addAll(nodeArgument.possibleSymbolInputsExpanded());
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
        if(connectionComponent().connectedTo() == null)
            return false;
        return
                connectionComponent().connectedTo().node().collapsed();
    }

    /**
     *
     * @return the label of this LInputField
     */
    public JLabel label()
    {
        return label;
    }

    public void setLabelText(String text)
    {
        label.setText(text);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        label.setFont(DesignPalette.LUDEME_INPUT_FONT);
        if(fieldComponent != null && fieldComponent.getBackground() != Handler.currentPalette().INPUT_FIELD_BACKGROUND() && !(fieldComponent instanceof JComboBox)) // JComboBox background does not work
            loadFieldComponentColours();

        if(addItemButton.ACTIVE_COLOR != Handler.currentPalette().FONT_LUDEME_INPUTS_COLOR())
        {
            addItemButton.ACTIVE_COLOR = Handler.currentPalette().FONT_LUDEME_INPUTS_COLOR();
            addItemButton.ACTIVE_ICON = Handler.currentPalette().COLLECTION_ICON_ACTIVE();
            addItemButton.updateDP();

            removeItemButton.ACTIVE_COLOR = Handler.currentPalette().FONT_LUDEME_INPUTS_COLOR();
            removeItemButton.ACTIVE_ICON = Handler.currentPalette().COLLECTION_REMOVE_ICON_ACTIVE();
            removeItemButton.updateDP();

            choiceButton.ACTIVE_COLOR = Handler.currentPalette().FONT_LUDEME_INPUTS_COLOR();
            choiceButton.ACTIVE_ICON = Handler.currentPalette().CHOICE_ICON_ACTIVE();
            choiceButton.updateDP();

            expandButton.ACTIVE_COLOR = Handler.currentPalette().FONT_LUDEME_INPUTS_COLOR();
            expandButton.ACTIVE_ICON = Handler.currentPalette().UNCOLLAPSE_ICON();
            expandButton.updateDP();
        }

    }

    @Override
    public String toString()
    {
        return "LIF: " + label.getText() + ", " + nodeArguments;
    }
}
