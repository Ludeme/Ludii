package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

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

public class LInputAreaNew extends JPanel
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
    private LinkedHashMap<List<NodeArgument>, LInputFieldNew> currentInputFields;


    /** Variables for a dynamic node
     *  How it works: Initially the user can provide node arguments / inputs for any clause the node has.
     *                Whenever an argument is provided, clauses that do not include that argument are removed from the list of active clauses.
     *                The list of possible arguments is updated whenever an argument is provided.
     */
    /** Clauses that satisfy currently provided inputs */
    private List<Clause> activeClauses = new ArrayList<>();
    /** Clauses that do not satisfy currently provided inputs */
    private List<Clause> inactiveClauses = new ArrayList<>();
    /** NodeArguments that are currently provided */
    private List<NodeArgument> providedNodeArguments = new ArrayList<>();
    /** NodeArguments that can be provided to satisfy active clauses */
    private List<NodeArgument> activeNodeArguments = new ArrayList<>();
    /** NodeArguments that cannot be provided to satisfy active clauses */
    private List<NodeArgument> inactiveNodeArguments = new ArrayList<>();


    private final boolean DEBUG = true;




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
                if (activeNodeArguments.contains(notSatisfiedClauseArgument)) {
                    removeActiveNodeArgument(notSatisfiedClauseArgument); // remove the argument from the active list and updates input fields accordingly
                }
            }
        }

        // update the inputfield for the provided node argument is done in addedConnection() TOOD: is this true?

        // print out variables
        System.out.println("ActiveClauses: " + activeClauses);
        System.out.println("InactiveClauses: " + inactiveClauses);
        System.out.println("ProvidedNodeArguments: " + providedNodeArguments);
        System.out.println("ActiveNodeArguments: " + activeNodeArguments);
        System.out.println("InactiveNodeArguments: " + inactiveNodeArguments);

    }

    private void removeActiveNodeArgument(NodeArgument nodeArgument)
    {
        // find inputfield for argument
        LInputFieldNew inputField = null;
        for(LInputFieldNew lif : currentInputFields.values())
        {
            if(lif.nodeArguments().contains(nodeArgument))
            {
                inputField = lif;
                break;
            }
        }
        /*for(List<NodeArgument> nodeArgumentList : currentNodeArgumentsLists) {
            if (nodeArgumentList.contains(nodeArgument)) {
                inputField = currentInputFields.get(nodeArgumentList);
                break;
            }
        }*/

        // remove inputfield from currentInputFields
        //currentNodeArgumentsLists.get(currentNodeArgumentsLists.indexOf(inputField.nodeArguments())).remove(nodeArgument);

        // Remove the NodeArgument from the merged InputField
        inputField.removeNodeArgument(nodeArgument);
        // If the merged InputField now only contains one NodeArgument, notify it to update it accordingly
        if(inputField.nodeArguments().size() == 1)
        {
            inputField.reconstruct();
        }

        if(inputField.nodeArguments().size() == 0)
        {
            currentNodeArgumentsLists.remove(inputField.nodeArguments());
            currentInputFields.remove(inputField.nodeArguments());
        }

        activeNodeArguments.remove(nodeArgument);
        inactiveNodeArguments.add(nodeArgument);

        // TODO: is map updated automatically?
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
     * Constructor
     * @param LNC LudemeNodeComponent that this LInputAreaNew is associated with
     */
    public LInputAreaNew(LudemeNodeComponent LNC)
    {
        this.LNC = LNC;
        nodeArguments = generateNodeArguments();
        if(dynamic())
        {
            activeClauses = new ArrayList<>(LNC.node().clauses());
            inactiveClauses = new ArrayList<>();
            providedNodeArguments = new ArrayList<>();
            activeNodeArguments = new ArrayList<>();
            for(List<NodeArgument> nas: nodeArguments.values()) activeNodeArguments.addAll(nas);
            inactiveNodeArguments = new ArrayList<>();
            currentNodeArguments = activeNodeArguments;
        }
        else
        {
            currentNodeArguments = currentNodeArguments();
        }
        currentNodeArgumentsLists = generateNodeArgumentsLists(currentNodeArguments);
        currentInputFields = generateInputFields(currentNodeArgumentsLists);
        drawInputFields(currentInputFields);
        setOpaque(false);
        setVisible(true);
    }


    /**
     *
     * @return a HashMap of NodeArguments keyed by the clause they correspond to
     */
    private HashMap<Clause, List<NodeArgument>> generateNodeArguments()
    {
        HashMap<Clause, List<NodeArgument>> nodeArguments = new HashMap<>();
        for (Clause clause : LNC.node().clauses())
        {
            nodeArguments.put(clause, generateNodeArguments(clause));
        }
        return nodeArguments;
    }

    /**
     * Generates a list of lists of NodeArguments for a given Clause
     * @param clause Clause to generate the list of lists of NodeArguments for
     * @return List of lists of NodeArguments for the given Clause
     */
    private List<NodeArgument> generateNodeArguments(Clause clause)
    {
        List<NodeArgument> nodeArguments = new ArrayList<>();
        if(clause.symbol().ludemeType().equals(Symbol.LudemeType.Predefined))
        {
            System.out.println("yeeep");
            NodeArgument nodeArgument = new NodeArgument(clause);
            nodeArguments.add(nodeArgument);
            return nodeArguments;
        }
        List<ClauseArg> clauseArgs = clause.args();
        for(int i = 0; i < clauseArgs.size(); i++)
        {
            ClauseArg clauseArg = clauseArgs.get(i);
            // Some clauses have Constant clauseArgs followed by the constructor keyword. They should not be included in the InputArea
            if(nodeArguments.isEmpty() && clauseArg.symbol().ludemeType().equals(Symbol.LudemeType.Constant))
                continue;
            NodeArgument nodeArgument = new NodeArgument(clause, clauseArg);
            nodeArguments.add(nodeArgument);
            // if the clauseArg is part of a OR-Group, they all are added to the NodeArgument automatically, and hence can be skipped in the next iteration
            i = i + nodeArgument.size() - 1;
        }
        return nodeArguments;
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
        return LNC.node().providedInputs()[nodeArgument.indexFirst()] != null;
    }

    /**
     * Generates a list of LInputFields for every list of NodeArguments in the current list of lists of NodeArguments
     * @return List of LInputFields for the current list of lists of NodeArguments
     */
    private LinkedHashMap<List<NodeArgument>, LInputFieldNew> generateInputFields(List<List<NodeArgument>> nodeArgumentsLists)
    {
        LinkedHashMap<List<NodeArgument>, LInputFieldNew> inputFields = new LinkedHashMap<>();
        for(List<NodeArgument> nodeArgumentsList : nodeArgumentsLists)
        {
            LInputFieldNew inputField = new LInputFieldNew(this, nodeArgumentsList);
            inputFields.put(nodeArgumentsList, inputField);
        }
        return inputFields;
    }

    private void drawInputFields(HashMap<List<NodeArgument>, LInputFieldNew> inputFields)
    {
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);

        for (LInputFieldNew inputField : inputFields.values()) {
            inputField.setAlignmentX(LEFT_ALIGNMENT);
            add(inputField);
        }

        int preferredHeight = getPreferredSize().height;
        setSize(new Dimension(LNC.width(), preferredHeight));

        LNC.updateComponentDimension();
        LNC.updatePositions();
        repaint();
    }

    /**
     * If the selected clause is changed, this method is called to update the current list of NodeArguments and list of lists of NodeArguments and redraw the InputArea
     */
    public void changedSelectedClause(){
        // TODO: Remove all edges of this ludeme node AND MODEL
        LNC.graphPanel().connectionHandler().cancelNewConnection();
        LNC.graphPanel().connectionHandler().removeAllConnections(LNC.node());

        removeAll();
        currentNodeArguments = currentNodeArguments();
        currentNodeArgumentsLists = generateNodeArgumentsLists(currentNodeArguments);
        currentInputFields = generateInputFields(currentNodeArgumentsLists);
        drawInputFields(currentInputFields);
        setOpaque(false);
        setVisible(true);
    }

    /**
     * Method which syncs the Ludeme Node Component with provided inputs (stored in the Ludeme Node).
     * Called when drawing a graph.
     */
    public void updateProvidedInputs(){
        // Fill existing inputs
        Object[] providedInputs = LNC.node().providedInputs();
        for(int input_index = 0; input_index < providedInputs.length; input_index++){
            Object providedInput = providedInputs[input_index];
            if(providedInput != null){
                // find the inputfield with same index
                LInputFieldNew inputField = null;
                for(LInputFieldNew lInputField : currentInputFields.values()){
                    if(lInputField.inputIndices().contains(input_index)){
                        inputField = lInputField;
                        break;
                    }
                }
                assert inputField != null;
                inputField.setUserInput(providedInput);
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
    public LInputFieldNew addedConnection(LudemeNodeComponent lnc, LInputFieldNew inputField)
    {
        // Find the NodeArgument that the user provided input for
        NodeArgument providedNodeArgument = null;
        for(NodeArgument nodeArgument : inputField.nodeArguments())
        {
            for(ClauseArg arg : nodeArgument.args())
            {
                if(arg.symbol().equals(lnc.node().symbol()))
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
        if(!inputField.isMerged()) return inputField;
        // Otherwise it is a merged one.
        // Therefore, the NodeArgument which corresponds to the NodeArgument that the user provided input for is removed from the merged InputField

        // Single out the NodeArgument that the user provided input for and return the new InputField
        return singleOutInputField(providedNodeArgument, inputField);
    }


    /**
     * Notifies the Input Area that the user has provided terminal input for a NodeArgument
     * @param symbol The symbol of the NodeArgument that the user provided input for
     * @param inputField LInputField that the user has provided input for
     * @return The LInputField associated with the NodeArgument that the user provided input for
     */
    public LInputFieldNew addedConnection(Symbol symbol, LInputFieldNew inputField)
    {
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
        providedNodeArgument.setSeparateNode(true);
        System.out.println("providedNodeArgument: " + providedNodeArgument + ", " + providedNodeArgument.separateNode());
        // Update active and inactive variables for dynamic nodes
        if(dynamic()) providedNodeArgument(providedNodeArgument);
        // Single out the NodeArgument that the user provided input for and return the new InputField
        return singleOutInputField(providedNodeArgument, inputField);
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
    private LInputFieldNew singleOutInputField(NodeArgument nodeArgument, LInputFieldNew inputField)
    {
        // Create the new InputField for the removed NodeArgument
        List<NodeArgument> nodeArguments = new ArrayList<>();
        nodeArguments.add(nodeArgument);
        LInputFieldNew newInputField = new LInputFieldNew(this, nodeArguments);

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
            // Redraw
            drawInputFields(currentInputFields);
            return newInputField;
        }

        // Case 1
        if(nodeArgument.indexFirst() == inputField.nodeArguments().get(0).indexFirst())
        {
            addInputFieldAbove(newInputField, inputField);
        }
        // Case 2
        else if(nodeArgument.indexFirst() == inputField.nodeArguments().get(inputField.nodeArguments().size() - 1).indexFirst())
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
        // Redraw
        drawInputFields(currentInputFields);
        return newInputField;
    }

    /**
     * Adds a new InputField above another InputField
     * @param inputFieldNew The InputField to add above the other InputField
     * @param inputField The InputField to add the new InputField above
     */
    private void addInputFieldAbove(LInputFieldNew inputFieldNew, LInputFieldNew inputField)
    {
        // Add the new InputField before the other InputField in the currentNodeArgumentsLists list
        int index = currentNodeArgumentsLists.indexOf(inputField.nodeArguments());
        currentNodeArgumentsLists.add(index, inputFieldNew.nodeArguments());
        // update the currentInputFields map
        LinkedHashMap<List<NodeArgument>, LInputFieldNew> newInputFields = new LinkedHashMap<>();
        for(List<NodeArgument> nodeArgumentsList : currentNodeArgumentsLists)
        {
            newInputFields.put(nodeArgumentsList, currentInputFields.getOrDefault(nodeArgumentsList, inputFieldNew));
        }
        currentInputFields = newInputFields;
    }

    /**
     * Adds a new InputField below another InputField
     * @param inputFieldNew The InputField to add below the other InputField
     * @param inputField The InputField to add the new InputField below
     */
    private void addInputFieldBelow(LInputFieldNew inputFieldNew, LInputFieldNew inputField)
    {
        // Add the new InputField after the other InputField in the currentNodeArgumentsLists list
        int index = currentNodeArgumentsLists.indexOf(inputField.nodeArguments()) + 1;
        currentNodeArgumentsLists.add(index, inputFieldNew.nodeArguments());
        // update the currentInputFields map
        LinkedHashMap<List<NodeArgument>, LInputFieldNew> newInputFields = new LinkedHashMap<>();
        for(List<NodeArgument> nodeArgumentsList : currentNodeArgumentsLists)
        {
            newInputFields.put(nodeArgumentsList, currentInputFields.getOrDefault(nodeArgumentsList, inputFieldNew));
        }
        currentInputFields = newInputFields;
    }


    /**
     * Splits a merged InputField into two InputFields and singles out the NodeArgument that the user provided input for
     * @param inputFieldNew The new single InputField
     * @param inputField The merged InputField to split
     */
    private void splitAndAddBetween(LInputFieldNew inputFieldNew, LInputFieldNew inputField)
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
            if(nodeArgument.indexFirst() < inputFieldNew.nodeArguments().get(0).indexFirst())
            {
                nodeArguments1.add(nodeArgument);
            }
            else if(nodeArgument.indexFirst() > inputFieldNew.nodeArguments().get(0).indexFirst())
            {
                nodeArguments2.add(nodeArgument);
            }
            else if(nodeArgument != inputFieldNew.nodeArguments().get(0))
            {
                System.err.println("A NodeArgument disappeared from the merged InputField");
            }
        }
        // Create the new InputFields
        LInputFieldNew inputField1 = new LInputFieldNew(this, nodeArguments1);
        LInputFieldNew inputField2 = new LInputFieldNew(this, nodeArguments2);
        // Add inputField1 above the old merged InputField
        if(!nodeArguments1.isEmpty()) addInputFieldAbove(inputField1, inputField);
        // Add the new single InputField between the two InputFields
        addInputFieldAbove(inputFieldNew, inputField);
        // Add inputField2 below the new single InputField
        if(!nodeArguments2.isEmpty()) addInputFieldAbove(inputField2, inputField);
        // Remove the old merged InputField
        currentNodeArgumentsLists.remove(inputField.nodeArguments());
        currentInputFields.remove(inputField.nodeArguments());
    }

    /**
     * Splits a merged InputField into two InputFields and singles out the NodeArgument that the user provided input for
     * @param inputFieldNew The new single InputField
     * @param inputField The merged InputField to split
     */
    private void splitAndAddBetweenDynamic(LInputFieldNew inputFieldNew, LInputFieldNew inputField)
    {
        // Find the NodeArgument that the user provided input for
        // Split the merged InputField into two InputFields
        List<NodeArgument> nodeArguments1 = new ArrayList<>(); // nodeArguments1 contains active node arguments which index is smaller than the max_index of provided node arguments excluding provided arguments
        List<NodeArgument> nodeArguments2 = new ArrayList<>(); // nodeArguments2 contains active node arguments which index is greater than the max_index of provided node arguments excluding provided arguments

        int min_index = 100000;
        int max_index = 0;
        for(NodeArgument providedNA : providedNodeArguments) {
            if(!inputField.nodeArguments().contains(providedNA)) continue;
            min_index = Math.min(min_index, providedNA.indexFirst());
            max_index = Math.max(max_index, providedNA.indexFirst());
        }

        System.out.println("Reading node arguments: " + min_index + " " + max_index + ": " + inputField.nodeArguments() );

        for(NodeArgument nodeArgument : inputField.nodeArguments())
        {
            if(nodeArgument.indexFirst() <= min_index && !providedNodeArguments.contains(nodeArgument))
            {
                nodeArguments1.add(nodeArgument);
            }
            else if(nodeArgument.indexFirst() >= max_index && !providedNodeArguments.contains(nodeArgument))
            {
                nodeArguments2.add(nodeArgument);
            }
            else if(nodeArgument != inputFieldNew.nodeArguments().get(0))
            {
                System.err.println("A NodeArgument disappeared from the merged InputField: " + nodeArgument);
            }
        }

        System.out.println("---------");
        System.out.println("ABOVE: " + nodeArguments1);
        System.out.println("BETWEEN: " + inputFieldNew.nodeArguments());
        System.out.println("BELOW: " + nodeArguments2);
        System.out.println("---------");

        // Create the new InputFields
        LInputFieldNew inputField1 = new LInputFieldNew(this, nodeArguments1);
        LInputFieldNew inputField2 = new LInputFieldNew(this, nodeArguments2);
        // Add inputField1 above the old merged InputField
        if(!nodeArguments1.isEmpty()) addInputFieldAbove(inputField1, inputField);
        // Add the new single InputField between the two InputFields
        addInputFieldAbove(inputFieldNew, inputField);
        // Add inputField2 below the new single InputField
        if(!nodeArguments2.isEmpty()) addInputFieldAbove(inputField2, inputField);
        // Remove the old merged InputField
        currentNodeArgumentsLists.remove(inputField.nodeArguments());
        currentInputFields.remove(inputField.nodeArguments());
    }


    public void removedConnection(LInputFieldNew inputField)
    {

        // if the inputfield is single and optional, check whether it can be merged into another inputfield
        if(!inputField.isMerged() && inputField.optional())
        {
            // check whether there is a unfilled optional inputfield above AND below this one
            LInputFieldNew inputFieldAbove = inputFieldAbove(inputField);
            boolean canBeMergedIntoAbove = inputFieldAbove != null && !isArgumentProvided(inputFieldAbove.nodeArgument(0)) && inputFieldAbove.optional();
            LInputFieldNew inputFieldBelow = inputFieldBelow(inputField);
            boolean canBeMergedIntoBelow = inputFieldBelow != null && !isArgumentProvided(inputFieldBelow.nodeArgument(0)) && inputFieldBelow.optional();
            if(!canBeMergedIntoBelow && !canBeMergedIntoAbove) return;
            // if can be merged into both, combine the three inputfields into one
            if(canBeMergedIntoAbove && canBeMergedIntoBelow) {
                System.out.println("Merging between");
                LInputFieldNew inputFieldNew = mergeInputFields(new LInputFieldNew[]{inputFieldAbove, inputField, inputFieldBelow});
            }
            // if can be merged into above, merge into above
            else if(canBeMergedIntoAbove) {
                System.out.println("Merging above");
                LInputFieldNew inputFieldNew = mergeInputFields(new LInputFieldNew[]{inputFieldAbove, inputField});
            }
            // if can be merged into below, merge into below
            else if(canBeMergedIntoBelow) {
                System.out.println("Merging below");
                LInputFieldNew inputFieldNew = mergeInputFields(new LInputFieldNew[]{inputField, inputFieldBelow});
            }
        }
        drawInputFields(currentInputFields);
        setOpaque(false);
        setVisible(true);
    }

    /**
     * Merges multiple InputFields into one InputField and updates the currentInputFields map
     * @param inputFields The InputFields to merge
     * @return The merged InputField
     */
    private LInputFieldNew mergeInputFields(LInputFieldNew[] inputFields) {
        List<NodeArgument> nodeArguments = new ArrayList<>();
        for(LInputFieldNew inputField : inputFields)
        {
            nodeArguments.addAll(inputField.nodeArguments());
        }
        LInputFieldNew mergedInputField = new LInputFieldNew(this, nodeArguments);
        // Update the currentNodeArgumentsLists list and the currentInputFields map
        // add the new nodeArguments to the currentNodeArgumentsLists list
        int index = currentNodeArgumentsLists.indexOf(inputFields[0].nodeArguments());
        currentNodeArgumentsLists.add(index, nodeArguments);
        // remove the old nodeArguments from the currentNodeArgumentsLists list
        for(LInputFieldNew inputField : inputFields)
        {
            currentNodeArgumentsLists.remove(inputField.nodeArguments());
        }
        // update the currentInputFields map
        LinkedHashMap<List<NodeArgument>, LInputFieldNew> newInputFields = new LinkedHashMap<>();
        for(List<NodeArgument> nodeArgumentsList : currentNodeArgumentsLists)
        {
            newInputFields.put(nodeArgumentsList, currentInputFields.getOrDefault(nodeArgumentsList, mergedInputField));
        }
        currentInputFields = newInputFields;
        return mergedInputField;
    }

    /**
     * Returns the inputfield above the given inputfield
     * @param inputField The inputfield to get the above inputfield of
     * @return The inputfield above the given inputfield
     */
    private LInputFieldNew inputFieldAbove(LInputFieldNew inputField)
    {
        // TODO: this is inefficient, but the one below doesnt work. Maybe because the ArrayList is altered in the process before (but id stays the same!)
        int index = currentNodeArgumentsLists.indexOf(inputField.nodeArguments());
        if(index <= 0) return null;
        List<NodeArgument> nodeArguments = currentNodeArgumentsLists.get(index - 1);
        for(LInputFieldNew lif : currentInputFields.values())
        {
            if(lif.nodeArguments().equals(nodeArguments))
            {
                return lif;
            }
        }

        if(index > 0)
        {
            return currentInputFields.get(currentNodeArgumentsLists.get(index - 1));
        }
        return null;
    }

    /**
     * Returns the inputfield below the given inputfield
     * @param inputField The inputfield to get the below inputfield of
     * @return The inputfield below the given inputfield
     */
    private LInputFieldNew inputFieldBelow(LInputFieldNew inputField)
    {
        // TODO: this is inefficient, but the one below doesnt work. Maybe because the ArrayList is altered in the process before (but id stays the same!)
        int index = currentNodeArgumentsLists.indexOf(inputField.nodeArguments());
        if(index >= currentNodeArgumentsLists.size() - 1) return null;
        List<NodeArgument> nodeArguments = currentNodeArgumentsLists.get(index + 1);
        for(LInputFieldNew lif : currentInputFields.values())
        {
            if(lif.nodeArguments().equals(nodeArguments))
            {
                return lif;
            }
        }


        if(index < currentNodeArgumentsLists.size() - 1)
        {
            return currentInputFields.get(currentNodeArgumentsLists.get(index + 1));
        }
        return null;
    }



    /**
     * Updates the positions of all LInputFields' connection components
     */
    public void updateConnectionPointPositions()
    {
        for(LInputFieldNew inputField : currentInputFields.values())
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
        return nodeArguments().get(selectedClause());
    }

    /**
     *
     * @return a HashMap of NodeArguments keyed by the clause they correspond to
     */
    public HashMap<Clause, List<NodeArgument>> nodeArguments()
    {
        return nodeArguments;
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
