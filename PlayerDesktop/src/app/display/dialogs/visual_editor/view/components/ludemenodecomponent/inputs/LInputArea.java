package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.NodeArgument;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Generates and stores LInputFields for the current Clause of a LudemeNodeComponent
 * Procedure (re-executed when the selected Clause is changed)
 *  1. Generate a list of NodeArguments for the current Clause
 *  2. Generate a list of lists of NodeArgument
 *          - Each list of NodeArgument is for one LInputField
 *          - Consequent optional NodeArguments are grouped together in one List
 *  3. Construct a list of LInputFields
 *  4. Add and display the LInputFields
 * @author Filipp Dokienko
 */

public class LInputArea extends JPanel
{
    /** LudemeNodeComponent that this LInputAreaNew is associated with */
    private final LudemeNodeComponent LNC;
    /** HashMap of NodeArguments keyed by the clause they correspond to */
    private final HashMap<Clause, List<NodeArgument>> nodeArguments;
    /** List of NodeArguments for the current Clause of the associated LudemeNodeComponent */
    private List<NodeArgument> currentNodeArguments;
    /** List of lists of NodeArguments for the current Clause of the associated LudemeNodeComponent */
    private List<List<NodeArgument>> currentNodeArgumentsLists;
    /** List of LInputFields for the current Clause of the associated LudemeNodeComponent */
    public List<LInputField> currentInputFields;

    //public LinkedHashMap<List<NodeArgument>, LInputField> currentInputFields;


    /** Variables for a dynamic node
     *  How it works: Initially the user can provide node arguments / inputs for any clause the node has.
     *                Whenever an argument is provided, clauses that do not include that argument are removed from the list of active clauses.
     *                The list of possible arguments is updated whenever an argument is provided.
     */
    /** Clauses that satisfy currently provided inputs */
    private List<Clause> activeClauses;
    /** Clauses that do not satisfy currently provided inputs */
    private List<Clause> inactiveClauses;
    /** NodeArguments that are currently provided */
    private List<NodeArgument> providedNodeArguments;
    /** NodeArguments that can be provided to satisfy active clauses */
    private List<NodeArgument> activeNodeArguments;
    /** NodeArguments that cannot be provided to satisfy active clauses */
    private List<NodeArgument> inactiveNodeArguments;
    /** Whether there is an active clause (only one active clause left) */
    private boolean activeClause = false;

    private final boolean DEBUG = true;


    /**
     * Constructor
     * @param LNC LudemeNodeComponent that this LInputAreaNew is associated with
     */
    public LInputArea(LudemeNodeComponent LNC)
    {
        this.LNC = LNC;
        nodeArguments = nodeArguments();
        if(dynamic())
        {
            activeClauses = LNC.node().activeClauses();
            if(activeClauses.size() == 1) activeClause = true;
            inactiveClauses = LNC.node().inactiveClauses();
            providedNodeArguments = LNC.node().providedNodeArguments();
            activeNodeArguments = LNC.node().activeNodeArguments();
            //for(List<NodeArgument> nas: nodeArguments.values()) activeNodeArguments.addAll(nas);
            inactiveNodeArguments = LNC.node().inactiveNodeArguments();
            currentNodeArguments = activeNodeArguments;
        }
        else
        {
            currentNodeArguments = LNC.node().currentNodeArguments();
        }
        currentNodeArgumentsLists = generateNodeArgumentsLists(currentNodeArguments);
        currentInputFields = generateInputFields(currentNodeArgumentsLists);
        drawInputFields();
        setOpaque(false);
        setVisible(true);
    }

    /**
     * Groups consequent optional NodeArguments together in one list
     * Only if the NodeArgument is not provided with input by the user
     * @param nodeArguments List of NodeArguments to group into lists
     * @return List of lists of NodeArguments where each list corresponds to a LInputField
     */
    private List<List<NodeArgument>> generateNodeArgumentsLists(List<NodeArgument> nodeArguments)
    {
        if(dynamic()) return generateDynamicNodeArgumentsLists(nodeArguments);
        List<List<NodeArgument>> nodeArgumentsLists = new ArrayList<>();
        List<NodeArgument> currentNodeArgumentsList = new ArrayList<>(); // List of NodeArguments currently being added to the current list
        for (NodeArgument nodeArgument : nodeArguments) {

            // If optional and not filled, add it to the current list
            if(nodeArgument.optional() && !isArgumentProvided(nodeArgument))
            {
                currentNodeArgumentsList.add(nodeArgument);
            }
            else // If not optional, add it to a new empty list and add it to the list of lists
            {
                // if the current list is not empty, add it to the list of lists and clear it (happens when previous nodeArguments were optional)
                if (!currentNodeArgumentsList.isEmpty()) {
                    nodeArgumentsLists.add(currentNodeArgumentsList);
                    currentNodeArgumentsList = new ArrayList<>();
                }
                List<NodeArgument> list = new ArrayList<>();
                list.add(nodeArgument);
                nodeArgumentsLists.add(list);
            }
        }
        // if the current list is not empty, add it to the list of lists and clear it (happens when previous nodeArguments were optional)
        if(!currentNodeArgumentsList.isEmpty())
        {
            nodeArgumentsLists.add(currentNodeArgumentsList);
        }
        return nodeArgumentsLists;
    }

    /**
     * Groups consequent NodeArguments together in one list
     * Only if the NodeArgument is not provided with input by the user
     * @param nodeArguments List of NodeArguments to group into lists
     * @return List of lists of NodeArguments where each list corresponds to a LInputField
     */
    private List<List<NodeArgument>> generateDynamicNodeArgumentsLists(List<NodeArgument> nodeArguments)
    {
        List<List<NodeArgument>> nodeArgumentsLists = new ArrayList<>();
        List<NodeArgument> currentNodeArgumentsList = new ArrayList<>(); // List of NodeArguments currently being added to the current list
        for (NodeArgument nodeArgument : nodeArguments) {

            // Only if not provided with input by the user
            if(!providedNodeArguments.contains(nodeArgument))
            {
                currentNodeArgumentsList.add(nodeArgument);
            }
            else // If not optional, add it to a new empty list and add it to the list of lists
            {
                // if the current list is not empty, add it to the list of lists and clear it (happens when previous nodeArguments were optional)
                if (!currentNodeArgumentsList.isEmpty()) {
                    nodeArgumentsLists.add(currentNodeArgumentsList);
                    currentNodeArgumentsList = new ArrayList<>();
                }
                List<NodeArgument> list = new ArrayList<>();
                list.add(nodeArgument);
                nodeArgumentsLists.add(list);
            }
        }
        // if the current list is not empty, add it to the list of lists and clear it (happens when previous nodeArguments were optional)
        if(!currentNodeArgumentsList.isEmpty())
        {
            nodeArgumentsLists.add(currentNodeArgumentsList);
        }
        return nodeArgumentsLists;
    }

    /**
     *
     * @param nodeArgument
     * @return Whether this NodeArgument was provided with input by the user
     */
    private boolean isArgumentProvided(NodeArgument nodeArgument)
    {
        return LNC.node().providedInputsMap().get(nodeArgument) != null;
        //return LNC.node().providedInputs()[nodeArgument.index()] != null;
    }

    /**
     * Generates a list of LInputFields for every list of NodeArguments in the current list of lists of NodeArguments
     * @return List of LInputFields for the current list of lists of NodeArguments
     */
    private List<LInputField> generateInputFields(List<List<NodeArgument>> nodeArgumentsLists)
    {
        List<LInputField> inputFields = new ArrayList<>();
        for(List<NodeArgument> nodeArgumentsList : nodeArgumentsLists)
        {
            LInputField inputField = new LInputField(this, nodeArgumentsList);
            inputFields.add(inputField);
            //inputFields.put(nodeArgumentsList, inputField);
        }
        return inputFields;
    }

    /**
     * Draws the HashMap of LInputFields
     * Handles the merging/singling out of LInputFields of the dynamic constructor
     */
    public void drawInputFields()
    {
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);

        if(dynamic() && activeClauses.size() == 1 && !activeClause)
        {
            addRemainingInputFields();
        }
        else if(dynamic() && activeClauses.size() > 1 && activeClause)
        {
            removeUnprovidedInputFields();
            mergeUnprovidedMergedInputFields();
        }

        removeEmptyFields();

        //if(activeClauses.size() > 1) unmergeSameSymbolFields();

        for (LInputField inputField : currentInputFields)
        {
            inputField.setAlignmentX(LEFT_ALIGNMENT);
            add(inputField);
        }

        int preferredHeight = getPreferredSize().height;
        setSize(new Dimension(LNC.width(), preferredHeight));

        LNC.updateComponentDimension();
        LNC.updatePositions();
        repaint();
    }

    private void removeEmptyFields()
    {
        for(LInputField lif : new ArrayList<>(currentInputFields))
        {
            if(lif.nodeArguments().size() == 0) removeInputField(lif);
        }
    }

    /**
     * If the selected clause is changed, this method is called to update the current list of NodeArguments and list of lists of NodeArguments and redraw the InputArea
     */
    public void changedSelectedClause()
    {
        // TODO: Remove all edges of this ludeme node AND MODEL

        LNC.graphPanel().connectionHandler().cancelNewConnection();
        LNC.graphPanel().connectionHandler().removeAllConnections(LNC.node());

        LNC.graphPanel().setBusy(true);
        removeAll();
        LNC.graphPanel().setBusy(false);
        currentNodeArguments = currentNodeArguments();
        currentNodeArgumentsLists = generateNodeArgumentsLists(currentNodeArguments);
        currentInputFields = generateInputFields(currentNodeArgumentsLists);
        drawInputFields();
        setOpaque(false);
        setVisible(true);
    }

    private void unmergeSameSymbolFields()
    {
        for(LInputField inputField : currentInputFields)
        {
            // attempt to unmerge the same symbol fields
            if(inputField.isMerged())
            {
                unmergeSameSymbolField(inputField);
            }
        }
    }


    private void unmergeSameSymbolField(LInputField inputField)
    {
        // if every node argument symbol is the same, unmerge them
       for(NodeArgument nodeArgument : inputField.nodeArguments())
       {
           if(nodeArgument.arg().symbol() != inputField.nodeArguments().get(0).arg().symbol())
           {
               return;
           }
       }
       // then it is the same symbol, unmerge them
        // notify currentNodeArgumentsLists and currentInputFields of the change
        int index = inputFieldIndex(inputField);
        for(int i = 1 ; i < inputField.nodeArguments().size() ; i++)
        {
            inputField.removeNodeArgument(inputField.nodeArguments().get(0));
        }
        currentNodeArgumentsLists.set(index, inputField.nodeArguments());
    }




    /**
     * Method which syncs the Ludeme Node Component with provided inputs (stored in the Ludeme Node).
     * Called when drawing a graph.
     */
    public void updateProvidedInputs(){
        // Fill existing inputs
        Object[] providedInputs = LNC.node().providedInputsMap().values().toArray(new Object[0]);
        for(int input_index = 0; input_index < providedInputs.length; input_index++){
            Object providedInput = providedInputs[input_index];
            if(providedInput != null){
                // find the inputfield with same index
                LInputField inputField = null;
                for(LInputField lInputField : currentInputFields){
                    if(lInputField.inputIndices().contains(input_index)){
                        inputField = lInputField;
                        break;
                    }
                }
                assert inputField != null;
                inputField.setUserInput(providedInput);
                if(providedInput instanceof LudemeNode)
                {
                    if(((LudemeNode) providedInput).collapsed()) inputField.notifyCollapsed();
                }
                else if(providedInput instanceof Object[])
                {
                    for(int i = 0; i < ((Object[]) providedInput).length; i++)
                    {
                        Object o = ((Object[])providedInput)[i];
                        if(!(o instanceof LudemeNode)) continue;
                        LudemeNode ln = (LudemeNode) o;
                        if(!ln.collapsed()) continue;
                        // find according input field to notify it about the collapse
                        if(i == 0) inputField.notifyCollapsed();
                        else inputField.children().get(i-1).notifyCollapsed();
                    }
                }
            }
        }
        repaint();
        revalidate();
        setVisible(true);
    }

    /**
     * Notifies the Input Area that the user has provided non-terminal input for a NodeArgument
     * @param lnc The LudemeNodeComponent that the user connected to
     * @param inputField LInputField that the user has provided input for
     * @return The LInputField associated with the NodeArgument that the user provided input for
     */
    public LInputField addedConnection(LudemeNodeComponent lnc, LInputField inputField)
    {
        System.out.println("Adding connection " + lnc.node().title() + " -> " + inputField);
        // Find the NodeArgument that the user provided input for
        NodeArgument providedNodeArgument = null;
        for(NodeArgument nodeArgument : inputField.nodeArguments())
        {
            for(Symbol s : nodeArgument.possibleSymbolInputsExpanded())
            {
                if(s.equals(lnc.node().symbol()) || s.returnType().equals(lnc.node().symbol()))
                {
                    providedNodeArgument = nodeArgument;
                    break;
                }
            }
            if(providedNodeArgument != null) break;
        }
        // Update active and inactive variables for dynamic nodes
        if(dynamic()) providedNodeArgument(providedNodeArgument);
        // If the input field only contains one NodeArgument, it is the one that the user provided input for
        if(!inputField.isMerged())
        {
            // if the field is a choice, update its label
            if(inputField.choice())
            {
                inputField.setLabelText(providedNodeArgument.arg().symbol().name());
            }
            return inputField;
        }
        // Otherwise it is a merged one.
        // Therefore, the NodeArgument which corresponds to the NodeArgument that the user provided input for is removed from the merged InputField
        // Single out the NodeArgument that the user provided input for and return the new InputField
        return singleOutInputField(providedNodeArgument, inputField);
    }

    /**
     * Notifies the Input Area that the user has provided terminal input for a NodeArgument
     * @param nodeArgument The NodeArgument that the user provided input for
     * @param inputField LInputField that the user has provided input for
     * @return The LInputField associated with the NodeArgument that the user provided input for
     */
    public LInputField addedConnection(NodeArgument nodeArgument, LInputField inputField)
    {
        // Update active and inactive variables for dynamic nodes
        if(dynamic()) providedNodeArgument(nodeArgument);
        // Single out the NodeArgument that the user provided input for and return the new InputField
        return singleOutInputField(nodeArgument, inputField);
    }

    /**
     * Notifies the Input Area that the user has provided terminal input for a NodeArgument
     * @param symbol The symbol of the NodeArgument that the user provided input for
     * @param inputField LInputField that the user has provided input for
     * @return The LInputField associated with the NodeArgument that the user provided input for
     */
    public LInputField addedConnection(Symbol symbol, LInputField inputField)
    {
        System.out.println("Adding connection " + symbol + " -> " + inputField);
        // Find the NodeArgument that the user provided input for
        NodeArgument providedNodeArgument = null;
        for(NodeArgument nodeArgument : inputField.nodeArguments())
        {
            for(ClauseArg arg : nodeArgument.args())
            {
                if(arg.symbol().equals(symbol))
                {
                    providedNodeArgument = nodeArgument;
                    break;
                }
            }
            if(providedNodeArgument != null) break;
        }
        return addedConnection(providedNodeArgument, inputField);
    }

    /**
     * Removes the NodeArgument from a merged InputField, and creates a new InputField for the remaining NodeArguments and for the NodeArgument that was removed
     * 3 Cases:
     *      1. The removed NodeArgument has the highest index in the merged InputField
     *              -> The new InputField is above the merged InputField
     *      2. The removed NodeArgument has the lowest index in the merged InputField
     *              -> The new InputField is below the merged InputField
     *      3. The removed NodeArgument is in the middle of the merged InputField
     *              -> The merged InputField is split into two InputFields
     *              -> The new InputField is centered between the two InputFields
     * @param nodeArgument The NodeArgument to remove from the merged InputField
     * @param inputField The merged InputField to remove the NodeArgument from
     * @return The new InputField for the removed NodeArgument
     */
    private LInputField singleOutInputField(NodeArgument nodeArgument, LInputField inputField)
    {
        // Create the new InputField for the removed NodeArgument
        List<NodeArgument> nodeArguments = new ArrayList<>();
        nodeArguments.add(nodeArgument);
        LInputField newInputField = new LInputField(this, nodeArguments);

        // Dynamic nodes have a special case, only case 3
        if(dynamic()) {
            splitAndAddBetween(newInputField, inputField);
            // Remove the NodeArgument from the merged InputField
            inputField.removeNodeArgument(nodeArgument);
            // If the merged InputField now only contains one NodeArgument, notify it to update it accordingly
            if(inputField.nodeArguments().size() == 1)
            {
                inputField.reconstruct();
            }
            if(inputField.nodeArguments().size() == 0)
            {
                removeInputField(inputField);
            }
            // Redraw
            drawInputFields();
            return newInputField;
        }

        // Case 1
        if(nodeArgument.index() == inputField.nodeArguments().get(0).index())
        {
            addInputFieldAbove(newInputField, inputField);
        }
        // Case 2
        else if(nodeArgument.index() == inputField.nodeArguments().get(inputField.nodeArguments().size() - 1).index())
        {
            addInputFieldBelow(newInputField, inputField);
        }
        // Case 3
        else {
            splitAndAddBetween(newInputField, inputField);
        }
        // Remove the NodeArgument from the merged InputField
        inputField.removeNodeArgument(nodeArgument);
        // If the merged InputField now only contains one NodeArgument, notify it to update it accordingly
        if(inputField.nodeArguments().size() == 1)
        {
            inputField.reconstruct();
        }
        newInputField.activate();
        // Redraw
        drawInputFields();
        return newInputField;
    }

    public void addCollectionItem(LInputField inputField)
    {
        inputField.addCollectionItem();
    }

    /**
     * Adds an InputField to the current input fields
     * @param inputField InputField to add
     * @param index Index to add the InputField to
     */
    private void addInputField(LInputField inputField, int index)
    {
        currentNodeArgumentsLists.add(index, inputField.nodeArguments());
        currentInputFields.add(index, inputField);
    }

    /**
     * Adds an InputField to the current input fields (last position)
     * @param inputField InputField to add
     */
    private void addInputField(LInputField inputField)
    {
        currentNodeArgumentsLists.add(inputField.nodeArguments());
        currentInputFields.add(inputField);
    }

    /**
     * Removes an InputField from the current input fields
     * @param inputField InputField to remove
     */
    protected void removeInputField(LInputField inputField)
    {
        int index = inputFieldIndex(inputField);
        currentNodeArgumentsLists.remove(index);
        currentInputFields.remove(index);
    }


    /**
     * Adds a new InputField above another InputField
     * @param inputFieldNew The InputField to add above the other InputField
     * @param inputField The InputField to add the new InputField above
     */
    public void addInputFieldAbove(LInputField inputFieldNew, LInputField inputField)
    {
        if(DEBUG) System.out.println("Adding InputField " + inputFieldNew + " above " + inputField);
        int index = inputFieldIndex(inputField);
        addInputField(inputFieldNew, index);
    }

    /**
     * Adds a new InputField below another InputField
     * @param inputFieldNew The InputField to add below the other InputField
     * @param inputField The InputField to add the new InputField below
     */
    public void addInputFieldBelow(LInputField inputFieldNew, LInputField inputField)
    {
        if(DEBUG) System.out.println("Adding InputField " + inputFieldNew + " below " + inputField);
        int index = inputFieldIndex(inputField) + 1;
        addInputField(inputFieldNew, index);
    }

    /**
     * Splits a merged InputField into two InputFields and singles out the NodeArgument that the user provided input for
     * @param inputFieldNew The new single InputField
     * @param inputField The merged InputField to split
     */
    private void splitAndAddBetween(LInputField inputFieldNew, LInputField inputField)
    {
        // different for dynamic nodes
        if(dynamic()) {
            splitAndAddBetweenDynamic(inputFieldNew, inputField);
            return;
        }

        // Find the NodeArgument that the user provided input for
        // Split the merged InputField into two InputFields
        List<NodeArgument> nodeArguments1 = new ArrayList<>();
        List<NodeArgument> nodeArguments2 = new ArrayList<>();
        for(NodeArgument nodeArgument : inputField.nodeArguments())
        {
            if(nodeArgument.index() < inputFieldNew.nodeArguments().get(0).index())
            {
                nodeArguments1.add(nodeArgument);
            }
            else if(nodeArgument.index() > inputFieldNew.nodeArguments().get(0).index())
            {
                nodeArguments2.add(nodeArgument);
            }
            else if(nodeArgument != inputFieldNew.nodeArguments().get(0))
            {
                System.err.println("A NodeArgument disappeared from the merged InputField");
            }
        }
        // Create the new InputFields
        LInputField inputField1 = new LInputField(this, nodeArguments1);
        LInputField inputField2 = new LInputField(this, nodeArguments2);
        // Add inputField1 above the old merged InputField
        if(!nodeArguments1.isEmpty()) addInputFieldAbove(inputField1, inputField);
        // Add the new single InputField between the two InputFields
        addInputFieldAbove(inputFieldNew, inputField);
        // Add inputField2 below the new single InputField
        if(!nodeArguments2.isEmpty()) addInputFieldAbove(inputField2, inputField);
        // Remove the old merged InputField
        if(DEBUG) System.out.println("Removing old InputField " + inputField);
        removeInputField(inputField);
    }

    /**
     * Splits a merged InputField into two InputFields and singles out the NodeArgument that the user provided input for
     * @param inputFieldNew The new single InputField
     * @param inputField The merged InputField to split
     */
    private void splitAndAddBetweenDynamic(LInputField inputFieldNew, LInputField inputField)
    {
        // Find the NodeArgument that the user provided input for
        // Split the merged InputField into two InputFields
        List<NodeArgument> nodeArguments1 = new ArrayList<>(); // nodeArguments1 contains active node arguments which index is smaller than the max_index of provided node arguments excluding provided arguments
        List<NodeArgument> nodeArguments2 = new ArrayList<>(); // nodeArguments2 contains active node arguments which index is greater than the max_index of provided node arguments excluding provided arguments

        int min_index = 100000;
        int max_index = 0;
        for(NodeArgument providedNA : providedNodeArguments) {
            if(!inputField.nodeArguments().contains(providedNA)) continue;
            min_index = Math.min(min_index, providedNA.index());
            max_index = Math.max(max_index, providedNA.index());
        }

        for(NodeArgument nodeArgument : inputField.nodeArguments())
        {
            if(nodeArgument.index() <= min_index && !providedNodeArguments.contains(nodeArgument))
            {
                nodeArguments1.add(nodeArgument);
            }
            else if(nodeArgument.index() >= max_index && !providedNodeArguments.contains(nodeArgument))
            {
                nodeArguments2.add(nodeArgument);
            }
        }

        if(DEBUG) {
            System.out.println("---------");
            System.out.println("ABOVE: " + nodeArguments1);
            System.out.println("BETWEEN: " + inputFieldNew.nodeArguments());
            System.out.println("BELOW: " + nodeArguments2);
            System.out.println("---------");
        }

        // Create the new InputFields
        LInputField inputField1 = new LInputField(this, nodeArguments1);
        LInputField inputField2 = new LInputField(this, nodeArguments2);
        // Add inputField1 above the old merged InputField
        if(!nodeArguments1.isEmpty()) addInputFieldAbove(inputField1, inputField);
        // Add the new single InputField between the two InputFields
        addInputFieldAbove(inputFieldNew, inputField);
        // Add inputField2 below the new single InputField
        if(!nodeArguments2.isEmpty()) addInputFieldAbove(inputField2, inputField);
        // Remove the old merged InputField
        if(DEBUG) System.out.println("Removing old InputField " + inputField);
        removeInputField(inputField);
    }

    /**
     * For Dynamic Nodes.
     * Called when the connection of a input field is removed
     * Attempts to merge the input field into the input field above or below (or both)
     * @param inputField the input field which connection is removed
     */
    private void removedConnectionDynamic(LInputField inputField)
    {
        // get input fields above and below (null if there is no input field above or below)
        LInputField inputFieldAbove = inputFieldAbove(inputField);
        LInputField inputFieldBelow = inputFieldBelow(inputField);

        // check where the input field can be merged into
        // for dynamic: field above/below must be unfilled
        boolean canBeMergedIntoAbove = inputFieldAbove != null && !providedNodeArguments.contains(inputFieldAbove.nodeArgument(0));
        boolean canBeMergedIntoBelow = inputFieldBelow != null && !providedNodeArguments.contains(inputFieldBelow.nodeArgument(0));
        // update active node arguments etc.
        // get freed up node arguments (now active, before inactive)
        List<List<NodeArgument>> freedUpArguments = removedProvidedNodeArgument(inputField.nodeArgument(0));
        List<NodeArgument> freedUpAbove = freedUpArguments.get(0);
        List<NodeArgument> freedUpBelow = freedUpArguments.get(1);

        if(activeClauses.size() == 1) return;

        if(!canBeMergedIntoBelow && !canBeMergedIntoAbove)
        {
            System.err.println("Cannot remove connection, because there is no place to merge the input field! [dynamic]");
            return;
        }
        // if can be merged into both, combine the three inputfields into one
        if(canBeMergedIntoAbove && canBeMergedIntoBelow)
        {
            LInputField inputFieldNew = mergeInputFields(new LInputField[]{inputFieldAbove, inputField, inputFieldBelow});
            for(NodeArgument na : freedUpAbove) if(!providedNodeArguments.contains(na)) inputFieldNew.addNodeArgument(na);
            for(NodeArgument na : freedUpBelow) if(!providedNodeArguments.contains(na)) inputFieldNew.addNodeArgument(na);
        }
        // if can be merged into above, merge into above
        else if(canBeMergedIntoAbove)
        {
            LInputField inputFieldNew = mergeInputFields(new LInputField[]{inputFieldAbove, inputField});
            // add freed up node arguments to input field
            for(NodeArgument na : freedUpAbove) if(!providedNodeArguments.contains(na)) inputFieldNew.addNodeArgument(na);
            // get input field below
            LInputField inputFieldBelowMerged = inputFieldBelow(inputFieldNew);
            if(inputFieldBelowMerged != null)
            {
                // add freed up node arguments to input field
                for(NodeArgument na : freedUpBelow) if(!providedNodeArguments.contains(na)) inputFieldBelowMerged.addNodeArgument(na);
            }
            else
            {
                // add freed up node arguments to input field
                for(NodeArgument na : freedUpBelow) if(!providedNodeArguments.contains(na)) inputFieldNew.addNodeArgument(na);
            }
        }
        // if can be merged into below, merge into below
        else if(canBeMergedIntoBelow)
        {
            LInputField inputFieldNew = mergeInputFields(new LInputField[]{inputField, inputFieldBelow});
            // add freed up node arguments to input field
            for(NodeArgument na : freedUpBelow) if(!providedNodeArguments.contains(na)) inputFieldNew.addNodeArgument(na);
            // get input field below
            LInputField inputFieldAboveMerged = inputFieldAbove(inputFieldNew);
            if(inputFieldAboveMerged != null)
            {
                // add freed up node arguments to input field
                for(NodeArgument na : freedUpAbove) if(!providedNodeArguments.contains(na)) inputFieldAboveMerged.addNodeArgument(na);
            }
            else
            {
                // add freed up node arguments to input field
                for(NodeArgument na : freedUpAbove) if(!providedNodeArguments.contains(na)) inputFieldNew.addNodeArgument(na);
            }
        }
    }

    /**
     * Called when the connection of a input field is removed
     * Attempts to merge the input field into the input field above or below (or both)
     * @param inputField the input field which connection is removed
     * @return whether the input field was merged
     */
    public boolean removedConnection(LInputField inputField)
    {
        if(DEBUG) System.out.println("removedConnection: " + inputField);
        if(!inputField.isMerged() && dynamic()) {
            removedConnectionDynamic(inputField);
            drawInputFields();
            setOpaque(false);
            setVisible(true);
            return true;
        }

        // if the inputfield is single and optional, check whether it can be merged into another inputfield
        if(!inputField.isMerged() && inputField.optional())
        {
            // get input fields above and below (null if there is no input field above or below)
            LInputField inputFieldAbove = inputFieldAbove(inputField);
            //boolean canBeMergedIntoAbove = inputFieldAbove != null && !isArgumentProvided(inputFieldAbove.nodeArgument(0)) && (inputFieldAbove.optional() || dynamic());
            LInputField inputFieldBelow = inputFieldBelow(inputField);
            //boolean canBeMergedIntoBelow = inputFieldBelow != null && !isArgumentProvided(inputFieldBelow.nodeArgument(0)) && (inputFieldBelow.optional() || dynamic());


            // check where the input field can be merged into
            // for optional: field above/below must be unfilled & optional
            boolean canBeMergedIntoAbove = inputFieldAbove != null && !isArgumentProvided(inputFieldAbove.nodeArgument(0)) && inputFieldAbove.optional();
            boolean canBeMergedIntoBelow = inputFieldBelow != null && !isArgumentProvided(inputFieldBelow.nodeArgument(0)) && inputFieldBelow.optional();


            // if cannot be merged into above or below field, then find suitable LInputField to merge into
            if(!canBeMergedIntoBelow && !canBeMergedIntoAbove)
            {
                // check whether this is a block of subsequent symbols (e.g. int)
                if((inputFieldAbove != null && inputFieldAbove.nodeArgument(0).arg().symbol().equals(inputField.nodeArgument(0).arg().symbol()) && inputFieldAbove.nodeArgument(0).collection() == inputField.nodeArgument(0).collection()) || (inputFieldBelow != null && inputFieldBelow.nodeArgument(0).arg().symbol().equals(inputField.nodeArgument(0).arg().symbol()) && inputFieldBelow.nodeArgument(0).collection() == inputField.nodeArgument(0).collection()))
                {
                    // find this block
                    List<LInputField> block = new ArrayList<>();
                    // find first element
                    LInputField e = inputFieldAbove;
                    if(e == null) e = inputField;
                    while(inputFieldAbove!=null && !inputFieldAbove.isMerged() && inputFieldAbove.nodeArgument(0).arg().symbol().equals(inputField.nodeArgument(0).arg().symbol()) && inputFieldAbove.nodeArgument(0).collection() == inputField.nodeArgument(0).collection())
                    {
                        e = inputFieldAbove;
                        inputFieldAbove = inputFieldAbove(inputFieldAbove);
                    }
                    // add remaining elements to list
                    block.add(e);
                    LInputField current = inputFieldBelow(e);
                    while(current != null && !current.isMerged() && current.nodeArgument(0).arg().symbol().equals(e.nodeArgument(0).arg().symbol()) && current.nodeArgument(0).collection() == inputField.nodeArgument(0).collection())
                    {
                        block.add(current);
                        current = inputFieldBelow(current);
                    }
                    // find whether there is another unprovided input field in the block. if yes, then merge them together
                    LInputField newMerged = inputField;
                    for(LInputField lif : block)
                    {
                        if(lif == inputField) continue;
                        if(!isArgumentProvided(lif.nodeArgument(0)))
                        {
                            newMerged = mergeInputFields(new LInputField[]{lif, inputField});
                            break;
                        }
                    }
                    // check whether the (maybe merged) input field can be merged into a field above/below the block
                    LInputField lif_b_block = inputFieldBelow(block.get(block.size()-1));
                    LInputField lif_a_block = inputFieldAbove(block.get(0));
                    canBeMergedIntoAbove = lif_a_block != null && !isArgumentProvided(lif_a_block.nodeArgument(0)) && lif_a_block.optional() && lif_a_block.nodeArgument(0).collection() == inputField.nodeArgument(0).collection();
                    canBeMergedIntoBelow = lif_b_block != null && !isArgumentProvided(lif_b_block.nodeArgument(0)) && lif_b_block.optional() && lif_b_block.nodeArgument(0).collection() == inputField.nodeArgument(0).collection();
                    if(canBeMergedIntoAbove && canBeMergedIntoBelow)
                    {
                        // check whether one of those contains the symbol already
                        for(NodeArgument na : lif_a_block.nodeArguments())
                            if(na.arg().symbol().equals(inputField.nodeArgument(0).arg().symbol()))
                            {
                                canBeMergedIntoBelow = false;
                                break;
                            }
                        if(canBeMergedIntoBelow) {
                            for (NodeArgument na : lif_b_block.nodeArguments())
                                if (na.arg().symbol().equals(inputField.nodeArgument(0).arg().symbol())) {
                                    canBeMergedIntoAbove = false;
                                    break;
                                }
                        }
                    }
                    if(canBeMergedIntoBelow)
                    {
                        mergeInputFields(new LInputField[]{newMerged, lif_b_block});
                    }
                    else if(canBeMergedIntoAbove)
                    {
                        mergeInputFields(new LInputField[]{lif_a_block, newMerged});
                    }
                    else
                    {
                        if(!newMerged.isMerged() && newMerged.choice())
                        {
                            // if the field is a choice, update its label
                            //newMerged.setLabelText("Choice");
                        }
                        // otherwise move it to the top of the block
                    }
                    drawInputFields();
                    setOpaque(false);
                    setVisible(true);
                    return true;
                }
                else
                {
                    return false;
                }

            }

            // if can be merged into both, combine the three inputfields into one
            if(canBeMergedIntoAbove && canBeMergedIntoBelow) {
                inputField = mergeInputFields(new LInputField[]{inputFieldAbove, inputField, inputFieldBelow});
            }
            // if can be merged into above, merge into above
            else if(canBeMergedIntoAbove) {
                inputField = mergeInputFields(new LInputField[]{inputFieldAbove, inputField});
            }
            // if can be merged into below, merge into below
            else if(canBeMergedIntoBelow) {
                inputField = mergeInputFields(new LInputField[]{inputField, inputFieldBelow});
            }
        }

        // if the field is a choice, update its label
        /*if(inputField.choice())
        {
            inputField.setLabelText("Choice");
        }*/

        drawInputFields();
        setOpaque(false);
        setVisible(true);
        return true;
    }

    /**
     * Merges multiple InputFields into one InputField and updates the currentInputFields map
     * @param inputFields The InputFields to merge
     * @return The merged InputField
     */
    private LInputField mergeInputFields(LInputField[] inputFields)
    {
        List<NodeArgument> nodeArguments = new ArrayList<>();
        for(LInputField inputField : inputFields)
        {
            nodeArguments.addAll(inputField.nodeArguments());
        }
        LInputField mergedInputField = new LInputField(this, nodeArguments);
        // Update the currentNodeArgumentsLists list and the currentInputFields map
        // add the new nodeArguments to the currentNodeArgumentsLists list
        int index = inputFieldIndex(inputFields[0]);
        addInputField(mergedInputField, index);
        // remove the old nodeArguments from the currentNodeArgumentsLists list
        for(LInputField inputField : inputFields)
        {
            if(DEBUG) System.out.println("Removing old InputField " + inputField);
            removeInputField(inputField);
        }
        return mergedInputField;
    }

    /**
     *
     * @param inputField
     * @return The index of an inputfield in the currentInputFields list
     */
    public int inputFieldIndex(LInputField inputField)
    {
        return currentInputFields.indexOf(inputField);
    }

    /**
     * Returns the inputfield above the given inputfield
     * @param inputField The inputfield to get the above inputfield of
     * @return The inputfield above the given inputfield
     */
    private LInputField inputFieldAbove(LInputField inputField)
    {
        int index = inputFieldIndex(inputField)-1;
        if(index < 0) return null;
        return currentInputFields.get(index);
    }

    /**
     * Returns the inputfield below the given inputfield
     * @param inputField The inputfield to get the below inputfield of
     * @return The inputfield below the given inputfield
     */
    private LInputField inputFieldBelow(LInputField inputField)
    {
        // this is inefficient, but the one below doesnt work. Maybe because the ArrayList is altered in the process before (but id stays the same!)
        int index = inputFieldIndex(inputField) + 1;
        if(index >= currentInputFields.size()) return null;
        return currentInputFields.get(index);
    }

    /**
     * For Dynamic Nodes.
     * When a new NodeArgument is provided by the user, update the list of available node arguments to input
     * @param nodeArgument NodeArgument that was provided by the user
     */
    private void providedNodeArgument(NodeArgument nodeArgument)
    {
        // add all node arguments with the same symbol to the providedNodeArguments list
        for(NodeArgument activeNA : new ArrayList<>(activeNodeArguments))
        {
            if(activeNA.arg().symbol().equals(nodeArgument.arg().symbol()))
            {
                providedNodeArguments.add(activeNA);
            }
        }
        // check whether all active clauses satisfy the provided node argument
        List<Clause> notSatisfiedClauses = new ArrayList<>();
        for(Clause activeClause : activeClauses)
        {
            if(!clauseSatisfiesArgument(activeClause, nodeArgument))
            {
                notSatisfiedClauses.add(activeClause);
            }
        }
        // remove the not satisfied clauses from the active clauses and add them to the inactive clauses
        activeClauses.removeAll(notSatisfiedClauses);
        inactiveClauses.addAll(notSatisfiedClauses);
        // update the list of active and inactive node arguments
        for(Clause notSatisfiedClause : notSatisfiedClauses) {
            // Get NodeArguments from notSatisfiedClause
            List<NodeArgument> notSatisfiedClauseArguments = nodeArguments.get(notSatisfiedClause);
            // Add all previously activeNodeArguments that are in the list of notSatisfiedClauseArguments to the list of inactiveNodeArguments
            for (NodeArgument notSatisfiedClauseArgument : notSatisfiedClauseArguments) {
                if (activeNodeArguments.contains(notSatisfiedClauseArgument))
                    removeActiveNodeArgument(notSatisfiedClauseArgument); // remove the argument from the active list and updates input fields accordingly
            }
        }
    }
    /**
     * For Dynamic Nodes.
     * When the remaining NodeArguments to be provided by the user are known, add a inputfield for each
     */
    private void addRemainingInputFields()
    {
        activeClause = true;
        // expand all input fields that are not yet expanded
        List<NodeArgument> addedNodeArguments = new ArrayList<>(); // NodeArguments for which a LInputFieldNew was added
        for(LInputField inputField : new ArrayList<>(currentInputFields))
        {
            if(!inputField.isMerged()) continue;
            List<NodeArgument> lif_arguments = inputField.nodeArguments();
            lif_arguments.sort(Comparator.comparingInt(NodeArgument::index)); // sort ascending by index

            for(NodeArgument na: lif_arguments)
            {
                LInputField lif = new LInputField(this, na);
                addedNodeArguments.add(na);
                // add this field above the current input field
                addInputFieldAbove(lif, inputField);
            }

            // remove the current input field
            if(DEBUG) System.out.println("Removing old InputField " + inputField);
            removeInputField(inputField);
        }

        LNC().node().setSelectedClause(activeClauses.get(0));
    }

    /**
     * For Dynamic Nodes.
     * Empty single InputFields should be merged together
     * Called when there is no more single active clause anymore.
     * Every automatically singled-out inputfields that are not provided with input should be merged together
     */
    private void removeUnprovidedInputFields()
    {
        activeClause = false;
        // try to merge empty input fields
        for(LInputField inputField : new ArrayList<>(currentInputFields))
        {
            if(!currentInputFields.contains(inputField)) continue;
            if(inputField.isMerged()) continue;

            // check whether can be merged
            LInputField inputFieldAbove = inputFieldAbove(inputField);
            LInputField inputFieldBelow = inputFieldBelow(inputField);
            if((inputFieldAbove == null || providedNodeArguments.contains(inputFieldAbove.nodeArgument(0))) && (inputFieldBelow == null || providedNodeArguments.contains(inputFieldBelow.nodeArgument(0)))) continue;

            if(!providedNodeArguments.contains(inputField.nodeArgument(0)))
            {
                removedConnectionDynamic(inputField);
                removeUnprovidedInputFields();
                break;
            }
        }
    }

    /**
     * If merged inputfields are both unprovided for, they can be merged together
     */
    private void mergeUnprovidedMergedInputFields()
    {
        List<LInputField> consequentInputFields = new ArrayList<>();
        for(LInputField inputField : new ArrayList<>(currentInputFields))
        {
            if (!inputField.isMerged()) {
                if(consequentInputFields.size() > 1)
                {
                    // convert consequentInputFields to an array
                    LInputField[] consequentInputFieldsArray = new LInputField[consequentInputFields.size()];
                    for(int i = 0; i < consequentInputFields.size(); i++) consequentInputFieldsArray[i] = consequentInputFields.get(i);
                    mergeInputFields(consequentInputFieldsArray);
                    mergeUnprovidedMergedInputFields();
                    consequentInputFields.clear();
                    break;
                }
            }
            else
            {
                consequentInputFields.add(inputField);
            }
        }
    }
    /**
     * For Dynamic Nodes.
     * A NodeArgument was removed from the active node arguments list.
     * @param nodeArgument NodeArgument that was removed from the active node arguments list
     */
    private void removeActiveNodeArgument(NodeArgument nodeArgument)
    {
        // find inputfield for argument
        LInputField inputField = null;
        for(LInputField lif : currentInputFields)
        {
            if(lif.isMerged() && lif.nodeArguments().contains(nodeArgument))
            {
                inputField = lif;
                break;
            }
        }

        // Remove the NodeArgument from the merged InputField
        if(inputField != null) {
            inputField.removeNodeArgument(nodeArgument);
            // If the merged InputField now only contains one NodeArgument, notify it to update it accordingly (shouldnt happen)
            if (inputField.nodeArguments().size() == 0) {
                for(LInputField lif : currentInputFields)
                {
                    if(lif == inputField)
                    {
                        if(DEBUG) System.out.println("Removing old InputField " + inputField);
                        removeInputField(inputField);
                    }
                }
            }
        }

        activeNodeArguments.remove(nodeArgument);
        inactiveNodeArguments.add(nodeArgument);
    }

    /**
     *
     * @param clause Clause to check
     * @param nodeArgument NodeArgument that was added
     * @return whether the provided node argument satisfies a clause, i.e. whether this clause satisfies the newly added node argument
     */
    private boolean clauseSatisfiesArgument(Clause clause, NodeArgument nodeArgument)
    {
        // check whether the clause contains the node argument's symbol
        for (ClauseArg clauseArg : clause.args())
        {
            Symbol argSymbol = clauseArg.symbol();
            if (argSymbol.equals(nodeArgument.arg().symbol()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param clause
     * @param nodeArguments
     * @return Whether a clause satisfies a list of node arguments
     */
    private boolean clauseSatisfiesArguments(Clause clause, List<NodeArgument> nodeArguments)
    {
        for(NodeArgument na : nodeArguments)
        {
            if(!clauseSatisfiesArgument(clause, na))
            {
                return false;
            }
        }
        return true;
    }


    /**
     * For Dynamic Nodes.
     * Called when a provided node argument was deleted (connection was removed)
     * @param nodeArgument NodeArgument that was deleted
     * @return A list of two lists of NodeArguments. They contain node arguments that are now active but previously were inactive. The first list contains node arguments with an index
     *         lower than the index of the deleted node argument. The second list contains node arguments with an index higher than the index of the deleted node argument.
     */
    private List<List<NodeArgument>> removedProvidedNodeArgument(NodeArgument nodeArgument)
    {
        // find which node arguments to remove from the provided node arguments list
        List<NodeArgument> nodeArgumentsToRemove = new ArrayList<>();
        List<NodeArgument> freedNodeArgumentsBelow = new ArrayList<>();
        List<NodeArgument> freedNodeArgumentsAbove = new ArrayList<>();
        // get max index from inputfield above, and min index from inputfield below
        // all node arguments with the same symbol as the removed node argument and between the min and max index are removed from the provided node arguments list
        for(NodeArgument providedNA : providedNodeArguments)
        {
            if(providedNA.arg().symbol().equals(nodeArgument.arg().symbol()))
            {
                if(providedNA.index() >= nodeArgument.index())
                {
                    nodeArgumentsToRemove.add(providedNA);
                    freedNodeArgumentsAbove.add(providedNA);
                }
                else if(providedNA.index() <= nodeArgument.index())
                {
                    nodeArgumentsToRemove.add(providedNA);
                    freedNodeArgumentsBelow.add(providedNA);
                }
            }
        }
        // update the list of provided node arguments
        providedNodeArguments.removeAll(nodeArgumentsToRemove);

        // check which clauses are now satisfied by the provided arguments
        List<Clause> satisfiedClauses = new ArrayList<>();
        for(Clause inactiveClause : inactiveClauses)
        {
            if(clauseSatisfiesArguments(inactiveClause, providedNodeArguments))
            {
                satisfiedClauses.add(inactiveClause);
            }
        }

        // add their nodearguments to the active node arguments list
        for(Clause satisfiedClause : satisfiedClauses) {
            for(NodeArgument na: nodeArguments.get(satisfiedClause)) {
                activeNodeArguments.add(na);
                inactiveNodeArguments.remove(na);

                if(na == nodeArgument) continue;

                if(na.index() <= nodeArgument.index())
                {
                    freedNodeArgumentsBelow.add(na);
                }
                else
                {
                    freedNodeArgumentsAbove.add(na);
                }

            }
        }

        // add the satisfied clauses to the active clauses and remove them from the inactive clauses
        activeClauses.addAll(satisfiedClauses);
        inactiveClauses.removeAll(satisfiedClauses);

        List<List<NodeArgument>> freedNodeArguments = new ArrayList<>();
        freedNodeArguments.add(freedNodeArgumentsBelow);
        freedNodeArguments.add(freedNodeArgumentsAbove);

        return freedNodeArguments;
    }


    /**
     * Updates the positions of all LInputFields' connection components
     */
    public void updateConnectionPointPositions()
    {
        for(LInputField inputField : currentInputFields)
        {
            if(inputField.connectionComponent() != null)
            {
                inputField.connectionComponent().updatePosition();
            }
        }
    }

    /**
     *
     * @return List of lists of NodeArguments where each list corresponds to a LInputField
     */
    public List<List<NodeArgument>> nodeArgumentsLists()
    {
        return currentNodeArgumentsLists;
    }

    /**
     *
     * @return the List of NodeArguments for the current Clause of the associated LudemeNodeComponent
     */
    public List<NodeArgument> currentNodeArguments()
    {
        return LNC().node().currentNodeArguments();
    }

    /**
     *
     * @return a HashMap of NodeArguments keyed by the clause they correspond to
     */
    public HashMap<Clause, List<NodeArgument>> nodeArguments()
    {
        return LNC().node().nodeArguments();
    }

    /**
     *
     * @return The currently selected Clause of the associated LudemeNodeComponent
     */
    private Clause selectedClause()
    {
        return LNC.node().selectedClause();
    }

    /**
     *
     * @return the LudemeNodeComponent that this LInputAreaNew is associated with
     */
    public LudemeNodeComponent LNC()
    {
        return LNC;
    }

    /**
     *
     * @return Whether the node is dynamic or not
     */
    private boolean dynamic()
    {
        return LNC.node().dynamic();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBorder(DesignPalette.INPUT_AREA_PADDING_BORDER); // just space between this and bottom of LNC
    }

}
