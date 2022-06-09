package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.Symbol;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LInputArea extends JPanel {

    private List<LInputField> inputFields = new ArrayList<>();
    ;
    private LudemeNodeComponent LNC;

    //public List<Constructor> activeClauses = new ArrayList<>();
    //public List<Constructor> inactiveClauses = new ArrayList<>();

    public List<Clause> activeClauses = new ArrayList<>();
    public List<Clause> inactiveClauses = new ArrayList<>();

    private List<InputInformation> allInputInformations = new ArrayList<>();
    public List<LInputField> providedInputFields = new ArrayList<>();
    private List<LudemeNodeComponent> providedInputFieldsConnections = new ArrayList<>();


    private boolean dynamicConstructorActive = false;

    private static final boolean DEBUG = true;


    public LInputArea(LudemeNodeComponent ludemeNodeComponent) {
        this.LNC = ludemeNodeComponent;
        this.dynamicConstructorActive = LNC.dynamic;

        activeClauses = new ArrayList<>(LNC.node().clauses());

        inputFields = getInputFields(LNC);
        drawInputFields();
        setOpaque(false);
        setVisible(true);
    }

    private List<LInputField> getInputFields(LudemeNodeComponent ludemeNodeComponent) {

        if(DEBUG) System.out.println("[LIA]: Getting input fields");

        List<LInputField> fields = new ArrayList<>();

        if (LNC.dynamic && dynamicConstructorActive) {
            // Convert every clause's clause arguments to InputInformation
            List<InputInformation> inputInformationList = new ArrayList<>();
            for(Clause clause : LNC.node().clauses()) {
                if (clause.args() == null) {
                    for (Clause c : clause.symbol().rule().rhs()) {
                        for (int i = 0; i < c.args().size(); i++) {
                            ClauseArg arg = c.args().get(i);
                            NodeInput nodeInput = new NodeInput(c, arg);
                            InputInformation ii = new InputInformation(c, nodeInput);
                            // if argument was part of or group, skip the rest of the group
                            i = i + nodeInput.size() - 1;
                        }
                    }
                } else {
                    for (int i = 0; i < clause.args().size(); i++) {
                        ClauseArg arg = clause.args().get(i);
                        NodeInput nodeInput = new NodeInput(clause, arg);
                        InputInformation ii = new InputInformation(clause, nodeInput);
                        // if argument was part of or group, skip the rest of the group
                        i = i + nodeInput.size() - 1;
                    }
                }
            }
            fields.add(new LInputField(ludemeNodeComponent, inputInformationList));
            return fields;
        }

        // List of clause arguments of currently selected clause
        Clause selectedClause = LNC.node().selectedClause();
        List<NodeInput> inputs = new ArrayList<>();
        if(selectedClause.args() == null) {
            for(Clause c : selectedClause.symbol().rule().rhs()){
                for(int i = 0; i < c.args().size(); i++){
                    ClauseArg arg = c.args().get(i);
                    NodeInput nodeInput = new NodeInput(c, arg);
                    inputs.add(nodeInput);
                    // if argument was part of or group, skip the rest of the group
                    i = i + nodeInput.size() - 1;
                }
            }
        } else {
            for (int i = 0; i < selectedClause.args().size(); i++) {
                ClauseArg arg = selectedClause.args().get(i);
                NodeInput nodeInput = new NodeInput(selectedClause, arg);
                inputs.add(nodeInput);
                InputInformation ii = new InputInformation(selectedClause, nodeInput);
                // if argument was part of or group, skip the rest of the group
                i = i + nodeInput.size() - 1;
            }
        }

        System.out.println("These are the inputs: ");
        System.out.println(inputs);
        System.out.println(LNC.node().selectedClause().args());

        // Store consequent optional arguments to group them
        List<InputInformation> consequentOptionalInputs = new ArrayList<>();

        // Create InputInformation for each clause argument
        for(int i = 0; i < inputs.size(); i++){
            NodeInput nodeInput = inputs.get(i);
            InputInformation ii = new InputInformation(selectedClause, nodeInput);
            if(ii.optional() && LNC.node().providedInputs()[ii.getIndex()] == null){
                consequentOptionalInputs.add(ii);
                continue;
            } else if (consequentOptionalInputs.size() == 1) {
                LInputField inputFieldPrevious = new LInputField(ludemeNodeComponent, consequentOptionalInputs.get(0));
                fields.add(inputFieldPrevious);
                consequentOptionalInputs = new ArrayList<>();
            } else if (consequentOptionalInputs.size() > 1) {
                LInputField inputFieldPrevious = new LInputField(ludemeNodeComponent, consequentOptionalInputs);
                fields.add(inputFieldPrevious);
                consequentOptionalInputs = new ArrayList<>();
            }
            LInputField inputField = new LInputField(ludemeNodeComponent, ii);
            fields.add(inputField);
        }

        if (consequentOptionalInputs.size() == 1) {
            LInputField inputFieldPrevious = new LInputField(ludemeNodeComponent, consequentOptionalInputs.get(0));
            fields.add(inputFieldPrevious);
            consequentOptionalInputs = new ArrayList<>();
        }
        if (consequentOptionalInputs.size() > 1) {
            LInputField inputFieldPrevious = new LInputField(ludemeNodeComponent, consequentOptionalInputs);
            fields.add(inputFieldPrevious);
            consequentOptionalInputs = new ArrayList<>();
        }
        return fields;
    }

    // called when: (a) change constructor (b) remove edge
    public void drawInputFields() {
        //if(DEBUG) System.out.println("[LIA]: Drawing input fields");

        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);
        //setBackground(DesignPalette.BACKGROUND_LUDEME_BODY);
        //setBackground(DesignPalette.BACKGROUND_LUDEME_BODY);

        for (LInputField inputField : inputFields) {
            inputField.setAlignmentX(LEFT_ALIGNMENT);
            add(inputField);
        }

        int preferredHeight = getPreferredSize().height;
        setSize(new Dimension(LNC.getWidth(), preferredHeight));

        LNC.updateComponent();
        LNC.updatePositions();
    }

    public void setDynamic(boolean dynamic){
        if (DEBUG) System.out.println("[LIA]: Setting dynamic to " + dynamic);
        this.dynamicConstructorActive = dynamic;
        updateConstructor();
    }

    public void updateComponent(LudemeNode node, LInputField c_inputField, boolean removed) {
        if(DEBUG) System.out.println("[LIA]: Updating component");
        if(!dynamicConstructorActive) mergeOptionalArgumentsInOne();
        if (!removed && c_inputField != null && !c_inputField.isSingle()) {
            c_inputField.setToSingle(node.symbol());
        }
        drawInputFields();
    }

    public LInputField addedConnection(LudemeNodeComponent nodeComponent, LInputField c_inputField){
        LudemeNode node = nodeComponent.node();

        if(LNC.dynamic && dynamicConstructorActive){
            LInputField addedInputField = addedConnectionDynamic(node, c_inputField);
            providedInputFields.add(addedInputField);
            providedInputFieldsConnections.add(nodeComponent);
            return addedInputField;
        }

        // else if not dynamic:
        LInputField addedInputField = null;
        if(c_inputField != null && !c_inputField.isSingle()){
            addedInputField = c_inputField.setToSingle(node.symbol());
        }
        providedInputFields.add(addedInputField);
        providedInputFieldsConnections.add(nodeComponent);
        return addedInputField;
    }

    private LInputField addedConnectionDynamic(LudemeNode node, LInputField c_inputField) {
        if(DEBUG) System.out.println("[LIA]: Adding connection to dynamic constructor of " + LNC.node().symbol().name() + " to " + node.symbol().name());

        Symbol symbol = node.symbol();

        // find LInputField containg the node's ludeme
        LInputField lif0 = c_inputField;

        if (DEBUG) System.out.println("[DYNAMIC LIA]: lif0 = " + lif0);

        // find all InputInformation containing the node's ludeme
        List<InputInformation> IC = new ArrayList<>();
        for (InputInformation ii : lif0.getInputInformations()) {
            if (ii.getPossibleSymbolInputs().contains(symbol)) {
                IC.add(ii);
            }
        }
        if (DEBUG) System.out.println("[DYNAMIC LIA]: IC = " + IC);

        // active constructors are all ii's constructors in IC
        List<Clause> newActiveC = new ArrayList<>();
        List<Clause> newInactiveC = new ArrayList<>();

        for (InputInformation ii : IC) {
            if (!newActiveC.contains(ii.clause())) newActiveC.add(ii.clause());
        }
        // every constructor in activeConsturctors but not in newActiveC is inactive
        for (Clause c : activeClauses) {
            if (!newActiveC.contains(c)) newInactiveC.add(c);
        }

        // remove

        activeClauses = newActiveC;
        for(Clause c : newInactiveC){
            if(!inactiveClauses.contains(c)) inactiveClauses.add(c);
        }
        //inactiveClauses = newInactiveC;

        if (DEBUG) System.out.println("[DYNAMIC LIA]: newActiveC = " + newActiveC.size() + ", " + newActiveC);
        if (DEBUG) System.out.println("[DYNAMIC LIA]: newInactiveC = " + newInactiveC.size() + ", " + newInactiveC);

        // get all InputIndices of every ii in IC
        List<Integer> IX = new ArrayList<>();
        for (InputInformation ii : IC) {
            IX.add(ii.getIndex());
        }
        if (DEBUG) System.out.println("[DYNAMIC LIA]: IX = " + IX);

        // remove IC from lif0_ii
        List<InputInformation> lif0_ii = new ArrayList<>(lif0.getInputInformations());
        lif0_ii.removeAll(IC);


        // if |IX| > 1, then split up lif0 in lif0,1 and lif0,2
        if (IX.size() > 1) {

            List<InputInformation> lif01_iis = new ArrayList<>();
            List<InputInformation> lif02_iis = new ArrayList<>();



            // for every InputInformation ii in IC:
            //          for every Inputinformation j in lif0_ii:
            //                  if constructor(j) == constructor(i) && index(j) < index(ii) -> lif01_iis.add(j)
            //                  if constructor(j) == constructor(i) && index(j) > index(ii) -> lif02_iis.add(j)
            for (InputInformation ii : IC) {
                for (InputInformation j : lif0_ii) {
                    if (j.clause() == ii.clause() && j.getIndex() < ii.getIndex()) {
                        lif01_iis.add(j);
                    }
                    else if (j.clause() == ii.clause() && j.getIndex() > ii.getIndex()) {
                        lif02_iis.add(j);
                    }
                }
            }
            if (DEBUG) System.out.println("[DYNAMIC LIA]: lif01_iis = " + lif01_iis);
            if (DEBUG) System.out.println("[DYNAMIC LIA]: lif02_iis = " + lif02_iis);

            if(lif01_iis.size() == 0 && lif02_iis.size() == 0) {
                System.err.println("[DYNAMIC LIA] lif0_1_iis and lif0_2_iis are empty!");
                InputInformation single_ii = IC.get(0);
                for(InputInformation ii : IC) {
                    Symbol s = ii.nodeInput().arg().symbol();
                    if(s.equals(node.symbol())){
                        single_ii = ii;
                    }
                }
                LInputField single = new LInputField(LNC, single_ii);
                addInputFieldAbove(single, c_inputField);
                removeField(c_inputField);
                return single;
            }

            // if |lif0,1| == 0 ->
            //      replace lif0 with lif0,2
            //      add setToSingle(ludeme) ABOVE lif0

            if(lif01_iis.size() == 0){
                // replace lif0 with lif0,2
                LInputField lif02 = new LInputField(LNC, lif02_iis);
                addInputFieldAbove(lif02, c_inputField);
                removeField(c_inputField);
                // add single inputfield
                LInputField single = new LInputField(LNC, IC.get(0));
                addInputFieldAbove(single, lif02);
                return single;
            }

            // if |lif0,2| == 0 ->
            //      replace lif0 with lif0,1
            //      add setToSingle(ludeme) BELOW lif0

            else if(lif02_iis.size() == 0){
                // replace lif0 with lif0,1
                LInputField lif01 = new LInputField(LNC, lif01_iis);
                addInputFieldAbove(lif01, c_inputField);
                removeField(c_inputField);
                // add single inputfield
                LInputField single = new LInputField(LNC, IC.get(0));
                addInputFieldBelow(single, lif01);
                return single;
            }

            // else ->
            //      replace lif0 with lif0,1
            //      add setToSingle(ludeme) BELOW lif0
            //      add lif0,2 BELOW setToSingle(ludeme)

            else {
                // replace lif0 with lif0,1
                LInputField lif01 = new LInputField(LNC, lif01_iis);
                addInputFieldAbove(lif01, c_inputField);
                removeField(c_inputField);
                // add single inputfield
                LInputField single = new LInputField(LNC, IC.get(0));
                addInputFieldBelow(single, lif01);
                // add lif0,2 BELOW setToSingle(ludeme)
                LInputField lif02 = new LInputField(LNC, lif02_iis);
                addInputFieldBelow(lif02, single);
                return single;
            }
        }
        // if |IX| == 1 ->
        else {
            Object[] providedInputs = node.providedInputs();
            System.out.println("PROVIDED INPUTS RESETED of " + LNC.node().symbol().name()); // TODO: REMOVE THIS DEBUG LINES
            System.out.println("  ->  before: " + Arrays.toString(providedInputs));
            Handler.updateCurrentClause(LNC.getGraphPanel().getGraph(), LNC.node(), activeClauses.get(0)); // TODO: WARNING: DELETES PROVIDED INPUTS
            dynamicConstructorActive = false;
            if(DEBUG) System.out.println("[DYNAMIC LIA]: Setting dynamicConstructorActive of " + LNC.node().symbol().name() + " to " + dynamicConstructorActive + "(317)");
            updateConstructor();
            // provided inputs should still be connected
            transferInputs();
            // return inputfield corresponding to new connection
            Symbol providedSymbol = node.symbol();
            if(DEBUG) System.out.println("  --> Looking for " + providedSymbol + " in " + inputFields);
            for(LInputField lif : inputFields){
                for(InputInformation ii : lif.getInputInformations()){
                    if(ii.getPossibleSymbolInputs().contains(providedSymbol) && LNC.node().providedInputs()[ii.getIndex()] == null){
                        System.out.println("  ->  after: " + Arrays.toString(providedInputs)); // TODO: REMOVE
                        return lif;
                    }
                }
            }

        }
        if(DEBUG) {
            System.err.println("[LIA] Returning null as single inputfield");
            System.out.println("c_inputfield: " + c_inputField);
            System.out.println("inputFields: " + inputFields);
            System.out.println("dynamicConstructorActive: " + dynamicConstructorActive);
        }
        return null;
    }

    public void removedConnectionDynamic(LudemeNode node, LInputField c_inputField){

        if(DEBUG){
            System.out.println("[DYNAMIC LIA]: Removing Connection of " + LNC.node().symbol().name() + " from " + c_inputField.getInputInformations());
            System.out.println("[DYNAMIC LIA]: Active Constructors: " + activeClauses + "(" + activeClauses.size() + ")");
            System.out.println("[DYNAMIC LIA]: Inactive Constructors: " + inactiveClauses + "(" + inactiveClauses.size() + ")");
        }

        int indexOfCInputField = inputFields.indexOf(c_inputField);
        providedInputFieldsConnections.remove(providedInputFields.indexOf(c_inputField));
        providedInputFields.remove(c_inputField);

        List<InputInformation> providedII = new ArrayList<>();
        for(LInputField lif : providedInputFields){
            providedII.addAll(lif.getInputInformations());
        }
        // add ii with equivalent node to providedII
        for(InputInformation ii : new ArrayList<>(providedII)){
            for(InputInformation ii2 : allInputInformations){
                if(ii2.nodeInput().arg().symbol().name().equals(ii.nodeInput().arg().symbol().name()) && ii2.getIndex() == ii.getIndex()){
                    if(!providedII.contains(ii2)) providedII.add(ii2);
                }
            }
        }
        providedII.removeAll(c_inputField.getInputInformations());

        if(DEBUG) System.out.println("[DYNAMIC LIA]: Provided II: " + providedII);

        /*List<Constructor> addactiveClauses = new ArrayList<>();

        for(Constructor c : inactiveClauses){
            boolean flag = true;
            for(InputInformation ii : providedII){
                if(!c.getInputs().contains(ii.getInput())){
                    flag = false;
                    break;
                }
            }
            if(!flag) continue;
            addactiveClauses.add(c);
        }

        activeClauses.addAll(addactiveClauses);
        inactiveClauses.removeAll(addactiveClauses);*/

        List<Clause> newActiveC = new ArrayList<>();
        List<Clause> newInactiveC = new ArrayList<>();
        newInactiveC.addAll(activeClauses);
        newInactiveC.addAll(inactiveClauses);

        List<Clause> addactiveClauses = new ArrayList<>();


        for(InputInformation ii : providedII){
            for(Clause c : new ArrayList<>(newInactiveC)){
                boolean isActive = false;
                if(c.args().contains(ii.nodeInput().arg())){
                    // then is active
                    if(!newActiveC.contains(c)) newActiveC.add(c);
                    newInactiveC.remove(c);
                    if(!addactiveClauses.contains(c) && !activeClauses.contains(c)) addactiveClauses.add(c);
                    isActive = true;
                    break;
                }
            }
        }

        /*for(Constructor c : new ArrayList<>(newActiveC)){
            boolean isInactive = false;
            for(InputInformation ii : providedII){
                if(!c.getInputs().contains(ii.getInput())){
                    isInactive = true;
                    break;
                }

            }
            if(isInactive) {
                System.out.println("[LIA DYNAMIC] " + c.getInputs() + " does not contain any II of " + providedII);
                newInactiveC.add(c);
                newActiveC.remove(c);
            }
            if(!isInactive){
                if(!activeClauses.contains(c)){
                    addactiveClauses.add(c);
                }
            }
        }*/

        activeClauses = newActiveC;
        inactiveClauses = newInactiveC;
        if(DEBUG) System.out.println("[DYNAMIC LIA]: Active Constructors: " + activeClauses + "(" + activeClauses.size() + ")");
        if(DEBUG) System.out.println("[DYNAMIC LIA]: Inactive Constructors: " + inactiveClauses + "(" + inactiveClauses.size() + ")");

        //if(DEBUG) System.out.println("[DYNAMIC LIA]: Add Active Constructors: " + addactiveClauses);


        if(!dynamicConstructorActive && activeClauses.size() > 1) {
            dynamicConstructorActive = true;
            if(DEBUG) System.out.println("[DYNAMIC LIA]: Setting dynamicConstructorActive to " + dynamicConstructorActive + "(387)");
            inputFields = getInputFields(LNC);
            updateConstructor();
            return;
        }

        if(dynamicConstructorActive){

            // Create list of InputInformations to be added to lifs
            List<InputInformation> newActiveII = new ArrayList<>();

            for(Clause c : addactiveClauses){
                for(InputInformation ii : allInputInformations){
                    if(c.args().contains(ii.nodeInput().arg())){
                        newActiveII.add(ii);
                    }
                }
            }
            if(DEBUG) System.out.println("[DYNAMIC LIA]: New Active II: " + newActiveII);

            /*
        Cases:
            1. Field above c_inputField is not single
            2. Field below c_inputField is not single
            3. Field above AND below is not single
            4. There was only one field
         */
            LInputField lifAbove = null;
            LInputField lifBelow = null;
            if(indexOfCInputField - 1 >= 0) {
                lifAbove = inputFields.get(indexOfCInputField - 1);
            }
            if(indexOfCInputField + 1 <= inputFields.size() - 1) {
                lifBelow = inputFields.get(indexOfCInputField + 1);
            }

            // case 4
            if(lifAbove == null && lifBelow == null){
                List<InputInformation> lif0_iis = new ArrayList<InputInformation>(c_inputField.getInputInformations());
                LInputField lif0 = new LInputField(LNC, lif0_iis);
                addInputFieldAbove(lif0, c_inputField);
                removeField(c_inputField);
            }
            // case 3
            else if(lifAbove != null && lifBelow != null && !lifAbove.isSingle() && !lifBelow.isSingle()){
                /*
                           |           lifAbove  |
                  node =   |      c_inputField   |
                           |           lifBelow  |

                 a. lif0 = UNION(lifAbove, c_inputField, lifBelow)
                 b. add lif0 above lifAbove
                 c. remove lifAbove, c_inputField, lifBelow from inputFields

                 */

                List<InputInformation> lif0_iis = new ArrayList<InputInformation>();
                lif0_iis.addAll(lifAbove.getInputInformations());
                lif0_iis.addAll(c_inputField.getInputInformations());
                lif0_iis.addAll(lifBelow.getInputInformations());
                LInputField lif0 = new LInputField(LNC, lif0_iis);
                addInputFieldAbove(lif0, lifAbove);
                removeField(lifAbove);
                removeField(c_inputField);
                removeField(lifBelow);
            }
            // case 2
            else if(lifBelow != null && !lifBelow.isSingle()){
                /*
                  node =   |      c_inputField   |
                           |           lifBelow  |

                 a. lif0 = UNION(lifBelow, c_inputField)
                 b. add lif0 above lifBelow
                 c. remove c_inputField, lifBelow from inputFields

                 */
                List<InputInformation> lif0_iis = new ArrayList<InputInformation>();
                lif0_iis.addAll(lifBelow.getInputInformations());
                lif0_iis.addAll(c_inputField.getInputInformations());
                LInputField lif0 = new LInputField(LNC, lif0_iis);
                addInputFieldAbove(lif0, lifBelow);
                removeField(c_inputField);
                removeField(lifBelow);
            }
            // case 1
            else if(lifAbove != null && !lifAbove.isSingle()){
                /*
                  node =   |           lifAbove  |
                           |      c_inputField   |

                a. lif0 = UNION(lifAbove, c_inputField)
                b. add lif0 above lifAbove
                c. remove lifAbove, c_inputField from inputFields

                 */

                List<InputInformation> lif0_iis = new ArrayList<InputInformation>();
                lif0_iis.addAll(lifAbove.getInputInformations());
                lif0_iis.addAll(c_inputField.getInputInformations());
                LInputField lif0 = new LInputField(LNC, lif0_iis);
                addInputFieldAbove(lif0, lifAbove);
                removeField(lifAbove);
            }
            else {
                System.err.println("[LIA] lifAbove = lifBelow = null");
            }

            // if there is only one input field it contains all new active II
            if(inputFields.size() == 1){
                for(InputInformation ii : newActiveII) if(!inputFields.get(0).getInputInformations().contains(ii)) inputFields.get(0).addInputInformation(ii);
            }
            else {
                // TODO: CHECK WHETHER THIS IN THE NAME OF MIGHTY GOD WORKS
                // if there are more than one input field, we need to check where to add each II in newActiveII
                int minIndex = 0;
                List<InputInformation> addedInputInformations = new ArrayList<InputInformation>();
                for(int i = 0; i < providedInputFields.size(); i++){
                    LInputField providedInputField = providedInputFields.get(i);
                    Symbol connectedTo = providedInputFieldsConnections.get(i).node().symbol();
                    // find corresponding InputInformations
                    List<InputInformation> correspondingII = new ArrayList<>();
                    for(InputInformation ii : allInputInformations){
                        if(ii.getPossibleSymbolInputs().contains(connectedTo)) correspondingII.add(ii);
                    }

                    // check whether providedInputField has a non-single input field above or below
                    int indexOfProvidedInputField = inputFields.indexOf(providedInputField);
                    lifAbove = null;
                    lifBelow = null;
                    if(indexOfProvidedInputField > 0) lifAbove = inputFields.get(indexOfProvidedInputField - 1);
                    if(indexOfProvidedInputField < inputFields.size() - 1) lifBelow = inputFields.get(indexOfProvidedInputField + 1);
                    if(lifAbove!= null && !lifAbove.isSingle()) lifAbove = null;
                    if(lifBelow != null && !lifBelow.isSingle()) lifBelow = null;

                    // add any ii in newActiveII with smaller indices than ii in correspondingII, but bigger than previous corresponding ii's min index to lifAbove
                    int maxIndexInCorrespondingII = correspondingII.get(0).getIndex();
                    for(InputInformation ii : correspondingII){
                        maxIndexInCorrespondingII = Math.max(maxIndexInCorrespondingII, ii.getIndex());
                    }
                    for(InputInformation ii : newActiveII){
                        if(ii.getIndex() > minIndex && ii.getIndex() <= maxIndexInCorrespondingII){
                            lifAbove.addInputInformation(ii);
                            addedInputInformations.add(ii);
                        }
                    }

                    // update minIndex
                    minIndex = correspondingII.get(0).getIndex();
                    for(InputInformation correspondingII_ii : correspondingII){
                        minIndex = Math.min(minIndex, correspondingII_ii.getIndex());
                    }
                }
                // add any ii in newActiveII not added to the last non-single lif
                newActiveII.removeAll(addedInputInformations);
                // find last non-single lif
                int lastNonSingleLifIndex = 0;
                for(int i = 0; i < inputFields.size(); i++){
                    if(!inputFields.get(i).isSingle()) lastNonSingleLifIndex = i;
                }
                LInputField lastNonSingleLif = inputFields.get(lastNonSingleLifIndex);
                for(InputInformation ii : newActiveII){
                    lastNonSingleLif.addInputInformation(ii);
                }

            }

        }
        drawInputFields();

    }

    // when dynamic: if only one constructor active, transfer provided inputs
    private void transferInputs(){
        if(DEBUG) System.out.println("[DYNAMIC LIA]: Transfering inputs of " +  LNC.node().symbol().name() + "....");
        System.out.println("Provided inputs model: " + Arrays.toString(LNC.node().providedInputs()));
        System.out.println("Provided input fields: " + providedInputFields);
        for(int i = 0; i < providedInputFields.size(); i++){
            LInputField providedInputField = providedInputFields.get(i); // input fields of "dynamic" node
            InputInformation ii = providedInputField.getInputInformation();
            LInputField newInputField = null;

            for(LInputField lif : inputFields){
                for(InputInformation inputInformation : lif.getInputInformations()){
                    if(inputInformation.getIndex() == ii.getIndex()){
                        newInputField = lif;
                        break;
                    }
                }
                if(newInputField != null) break;
            }

            if(DEBUG) System.out.println("[LIA] adding connection!");
            LNC.getGraphPanel().getCh().addConnection(newInputField.getConnectionComponent(), providedInputFieldsConnections.get(i).getIngoingConnectionComponent());

        }
    }

    public void updateConstructor(){
        if(DEBUG) System.out.println("[LIA] updateConstructor()");
        System.out.println("Provided input fields 586: " + providedInputFields);
        // TODO: Remove all edges of this ludeme node AND MODEL
        LNC.getGraphPanel().getCh().cancelNewConnection();
        LNC.getGraphPanel().getCh().removeAllConnections(LNC.node());

        inputFields = getInputFields(LNC);
        drawInputFields();
    }


    private void mergeOptionalArgumentsInOne(){
        List<LInputField> consequentOptionalInputs = new ArrayList<>();
        List<LInputField> newFields = new ArrayList<>();
        for(int i = 0; i < inputFields.size(); i++){
            LInputField inputField = inputFields.get(i);
            if((inputField.isSingle || LNC.node().providedInputs()[inputField.getInputInformation().getIndex()] == null) && inputField.getInputInformation().optional()){
                consequentOptionalInputs.add(inputField);
            } else if(consequentOptionalInputs.size() == 1){
                newFields.add(consequentOptionalInputs.get(0));
                newFields.add(inputField);
                consequentOptionalInputs.clear();
            } else if(consequentOptionalInputs.size() > 1){
                // add merged input field
                List<InputInformation> additionalArguments = new ArrayList<>();
                for(LInputField inputFieldOptional : consequentOptionalInputs) {
                    additionalArguments.addAll(inputFieldOptional.getInputInformations());
                }
                newFields.add(new LInputField(LNC, additionalArguments));
                newFields.add(inputField);
                consequentOptionalInputs = new ArrayList<>();
            } else {
                newFields.add(inputField);
            }
        }
        if(consequentOptionalInputs.size() == 1) {
            newFields.add(consequentOptionalInputs.get(0));
            consequentOptionalInputs.clear();
        }
        if(consequentOptionalInputs.size() > 1){
            List<InputInformation> additionalArguments = new ArrayList<>();
            for(LInputField inputFieldOptional : consequentOptionalInputs){
                additionalArguments.add(inputFieldOptional.getInputInformation());
            }
            newFields.add(new LInputField(LNC, additionalArguments));
            consequentOptionalInputs = new ArrayList<>();
        }
        this.inputFields = newFields;
    }


    /**
     * Method which syncs the Ludeme Node Component with provided inputs (stored in the Ludeme Node).
     * Called when drawing a graph.
     */
    public void updateProvidedInputs(){
        if(DEBUG) System.out.println("[LIA] Syncing " + LNC.node().symbol().name() + " with provided inputs...");
        if(DEBUG) System.out.println("   -> " + Arrays.toString(LNC.node().providedInputs()));
        // Fill existing inputs
        Object[] providedInputs = LNC.node().providedInputs();
        for(int input_index = 0; input_index < providedInputs.length; input_index++){
            Object providedInput = providedInputs[input_index];
            if(providedInput != null){
                // find the inputfield with same index
                LInputField inputField = null;
                for(LInputField lInputField : inputFields){
                    if(lInputField.getInputIndices().contains(input_index)){
                        inputField = lInputField;
                        break;
                    }
                }
                inputField.setUserInput(providedInput, input_index);
            }
        }
        if(DEBUG) System.out.println("   -> " + Arrays.toString(LNC.node().providedInputs()));

        drawInputFields();

        repaint();
        revalidate();
        setVisible(true);

    }

    public void addInputFieldAbove(LInputField newInputField, LInputField inputFieldAbove){
        inputFields.add(inputFields.indexOf(inputFieldAbove), newInputField);
        if(inputFieldAbove.getInputInformations().size() == 0) inputFields.remove(inputFieldAbove);
        drawInputFields();
        if(DEBUG) System.out.println("[LIA] Adding " + newInputField + " above " + inputFieldAbove);
    }

    public void addInputFieldBelow(LInputField newInputField, LInputField inputFieldBelow){
        inputFields.add(inputFields.indexOf(inputFieldBelow) + 1, newInputField);
        if(inputFieldBelow.getInputInformations().size() == 0) inputFields.remove(inputFieldBelow);
        drawInputFields();
        if(DEBUG) System.out.println("[LIA] Adding " + newInputField + " below " + inputFieldBelow);
    }

    public void removeField(LInputField inputField){
        inputFields.remove(inputField);
        drawInputFields();
    }

    public void updatePosition(){
        for(LInputField inputField : inputFields){
            if(inputField.getConnectionComponent() != null){
                inputField.getConnectionComponent().updatePosition();
            }
        }
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBorder(DesignPalette.INPUT_AREA_PADDING_BORDER); // just space between this and bottom of LNC
    }

}
