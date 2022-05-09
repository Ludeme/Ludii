package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.grammar.Constructor;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.input.Input;
import app.display.dialogs.visual_editor.model.grammar.input.LudemeInput;
import app.display.dialogs.visual_editor.view.components.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LInputArea extends JPanel {

    private List<LInputField> inputFields = new ArrayList<>();
    ;
    private LudemeNodeComponent LNC;

    private List<Constructor> activeConstructors = new ArrayList<>();
    private List<Constructor> inactiveConstructors = new ArrayList<>();

    private List<InputInformation> allInputInformations = new ArrayList<>();
    private List<LInputField> providedInputFields = new ArrayList<>();
    private List<LudemeNodeComponent> providedInputFieldsConnections = new ArrayList<>();


    private boolean dynamicConstructorActive = false;

    private static final boolean DEBUG = true;


    public LInputArea(LudemeNodeComponent ludemeNodeComponent) {
        this.LNC = ludemeNodeComponent;
        this.dynamicConstructorActive = LNC.dynamic;


        activeConstructors = new ArrayList<>(LNC.getLudemeNode().getLudeme().getConstructors());

        inputFields = getInputFields(LNC);
        drawInputFields();

    }

    private List<LInputField> getInputFields(LudemeNodeComponent ludemeNodeComponent) {

        if(DEBUG) System.out.println("[LIA]: Getting input fields");

        List<LInputField> fields = new ArrayList<>();

        if (LNC.dynamic && dynamicConstructorActive) {
            List<InputInformation> inputInformationList = new ArrayList<>();
            for (Constructor c : ludemeNodeComponent.getLudemeNode().getLudeme().getConstructors()) {
                for (Input input : c.getInputs()) {
                    InputInformation ii = new InputInformation(c, input);
                    inputInformationList.add(ii);
                    allInputInformations.add(ii);
                }
            }
            fields.add(new LInputField(ludemeNodeComponent, inputInformationList));
            return fields;
        }

        List<Input> inputs = ludemeNodeComponent.getLudemeNode().getCurrentConstructor().getInputs();

        List<InputInformation> consequentOptionalInputs = new ArrayList<>();

        for (int i = 0; i < inputs.size(); i++) {
            Input in = inputs.get(i);
            InputInformation ii = new InputInformation(ludemeNodeComponent.getLudemeNode().getCurrentConstructor(), in);

            if (ii.isOptional() && LNC.getLudemeNode().getProvidedInputs()[ii.getIndex()] == null) {
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
        if(DEBUG) System.out.println("[LIA]: Drawing input fields");
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);
        setBackground(DesignPalette.BACKGROUND_LUDEME_BODY);
        setBackground(DesignPalette.BACKGROUND_LUDEME_BODY);

        for (LInputField inputField : inputFields) {
            inputField.setAlignmentX(LEFT_ALIGNMENT);
            add(inputField);
        }

        int preferredHeight = getPreferredSize().height;
        setSize(new Dimension(LNC.getWidth(), preferredHeight));

        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        LNC.updateComponent();
        LNC.updatePositions();


        revalidate();
        repaint();
        setVisible(true);
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
            c_inputField.setToSingle(node.getLudeme());
        }
        drawInputFields();
    }

    public LInputField addedConnection(LudemeNodeComponent nodeComponent, LInputField c_inputField){
        LudemeNode node = nodeComponent.getLudemeNode();

        if(LNC.dynamic && dynamicConstructorActive){
            LInputField addedInputField = addedConnectionDynamic(node, c_inputField);
            providedInputFields.add(addedInputField);
            providedInputFieldsConnections.add(nodeComponent);
            return addedInputField;
        }

        // else if not dynamic:
        LInputField addedInputField = null;
        if(c_inputField != null && !c_inputField.isSingle()){
            addedInputField = c_inputField.setToSingle(node.getLudeme());
        }
        providedInputFields.add(addedInputField);
        providedInputFieldsConnections.add(nodeComponent);
        return addedInputField;
    }

    private LInputField addedConnectionDynamic(LudemeNode node, LInputField c_inputField) {

        Ludeme ludeme = node.getLudeme();
        // find LInputField containg the node's ludeme
        LInputField lif0 = c_inputField;
        /*for (LInputField inputField : inputFields) {
            for (InputInformation ii : inputField.getInputInformations()) {
                if (ii.getPossibleLudemeInputs().contains(ludeme)) {
                    lif0 = inputField;
                    break;
                }
                if (lif0 != null) break;
            }
        }*/
        if (DEBUG) System.out.println("[DYNAMIC LIA]: lif0 = " + lif0);

        // find all InputInformation containing the node's ludeme
        List<InputInformation> IC = new ArrayList<>();
        for (InputInformation ii : lif0.getInputInformations()) {
            if (ii.getPossibleLudemeInputs().contains(ludeme)) {
                IC.add(ii);
            }
        }
        if (DEBUG) System.out.println("[DYNAMIC LIA]: IC = " + IC);

        // active constructors are all ii's constructors in IC
        List<Constructor> newActiveC = new ArrayList<>();
        List<Constructor> newInactiveC = new ArrayList<>();
        for (InputInformation ii : IC) {
            if (!newActiveC.contains(ii.getConstructor())) newActiveC.add(ii.getConstructor());
        }
        // every constructor in activeConsturctors but not in newActiveC is inactive
        for (Constructor c : activeConstructors) {
            if (!newActiveC.contains(c)) newInactiveC.add(c);
        }

        // remove

        activeConstructors = newActiveC;
        inactiveConstructors = newInactiveC;

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
                    if (j.getConstructor() == ii.getConstructor() && j.getIndex() < ii.getIndex()) {
                        lif01_iis.add(j);
                    }
                    else if (j.getConstructor() == ii.getConstructor() && j.getIndex() > ii.getIndex()) {
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
                    Ludeme l = ((LudemeInput) (ii.getInput())).getRequiredLudeme();
                    if(l.equals(node.getLudeme())){
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
            Handler.updateCurrentConstructor(LNC.getGraphPanel().getGraph(), LNC.getLudemeNode(), activeConstructors.get(0));
            dynamicConstructorActive = false;
            if(DEBUG) System.out.println("[DYNAMIC LIA]: Setting dynamicConstructorActive to " + dynamicConstructorActive + "(299)");
            updateConstructor();
            // provided inputs should still be connected
            transferInputs();
            // return inputfield corresponding to new connection
            Ludeme providedLudeme = node.getLudeme();
            System.out.println("Looking for " + providedLudeme + " in " + inputFields);
            for(LInputField lif : inputFields){
                for(InputInformation ii : lif.getInputInformations()){
                    System.out.println("ii: " + ii.getPossibleLudemeInputs());
                    if(ii.getPossibleLudemeInputs().contains(providedLudeme) && LNC.getLudemeNode().getProvidedInputs()[ii.getIndex()] == null){
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
            System.out.println("[DYNAMIC LIA]: Active Constructors: " + activeConstructors + "(" + activeConstructors.size() + ")");
            System.out.println("[DYNAMIC LIA]: Inactive Constructors: " + inactiveConstructors + "(" + inactiveConstructors.size() + ")");
        }

        int indexOfCInputField = inputFields.indexOf(c_inputField);
        providedInputFieldsConnections.remove(providedInputFields.indexOf(c_inputField));
        providedInputFields.remove(c_inputField);

        List<InputInformation> providedII = new ArrayList<>();
        for(LInputField lif : providedInputFields){
            providedII.addAll(lif.getInputInformations());
        }
        providedII.removeAll(c_inputField.getInputInformations());

        if(DEBUG) System.out.println("[DYNAMIC LIA]: Provided II: " + providedII);

        List<Constructor> addActiveConstructors = new ArrayList<>();

        for(Constructor c : inactiveConstructors){
            boolean flag = true;
            for(InputInformation ii : providedII){
                if(!c.getInputs().contains(ii.getInput())){
                    flag = false;
                    break;
                }
            }
            if(!flag) continue;
            addActiveConstructors.add(c);
        }

        activeConstructors.addAll(addActiveConstructors);
        inactiveConstructors.removeAll(addActiveConstructors);
        if(DEBUG) System.out.println("[DYNAMIC LIA]: Add Active Constructors: " + addActiveConstructors);


        if(!dynamicConstructorActive && activeConstructors.size() > 1) {
            dynamicConstructorActive = true;
            if(DEBUG) System.out.println("[DYNAMIC LIA]: Setting dynamicConstructorActive to " + dynamicConstructorActive + "(362)");
            inputFields = getInputFields(LNC);
            updateConstructor();
            return;
        }

        if(dynamicConstructorActive){

            // Create list of InputInformations to be added to lifs
            List<InputInformation> newActiveII = new ArrayList<>();

            for(Constructor c : addActiveConstructors){
                for(InputInformation ii : allInputInformations){
                    if(c.getInputs().contains(ii.getInput())){
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
                System.out.println("- " + lif0_iis);
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
                    Ludeme connectedTo = providedInputFieldsConnections.get(i).getLudemeNode().getLudeme();
                    // find corresponding InputInformations
                    List<InputInformation> correspondingII = new ArrayList<>();
                    for(InputInformation ii : allInputInformations){
                        if(ii.getPossibleLudemeInputs().contains(connectedTo)) correspondingII.add(ii);
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
        if(DEBUG) System.out.println("[DYNAMIC LIA]: Transfering inputs....");
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

            System.out.println("[LIA] adding connection!");
            LNC.getGraphPanel().addConnection(newInputField.getConnectionComponent(), providedInputFieldsConnections.get(i).getIngoingConnectionComponent());

        }
    }

    public void updateConstructor(){
        if(DEBUG) System.out.println("[LIA] updateConstructor()");
        // TODO: Remove all edges of this ludeme node AND MODEL
        LNC.getGraphPanel().cancelNewConnection();
        LNC.getGraphPanel().removeAllConnections(LNC.getLudemeNode());
        //providedInputFields.clear();
        //providedInputFieldsConnections.clear();
        inputFields = getInputFields(LNC);
        drawInputFields();
    }

    public void removeAllConnections(){

    }

    private void mergeOptionalArgumentsInOne(){
        List<LInputField> consequentOptionalInputs = new ArrayList<>();
        List<LInputField> newFields = new ArrayList<>();
        for(int i = 0; i < inputFields.size(); i++){
            LInputField inputField = inputFields.get(i);
            if((inputField.isSingle || LNC.getLudemeNode().getProvidedInputs()[inputField.getInputInformation().getIndex()] == null) && inputField.getInputInformation().isOptional()){
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

    public void updateProvidedInputs(){

        // Fill existing inputs
        Object[] providedInputs = LNC.getLudemeNode().getProvidedInputs();
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

        drawInputFields();

        repaint();
        revalidate();
        setVisible(true);

    }

    public void addInputFieldAbove(LInputField newInputField, LInputField inputFieldAbove){
        inputFields.add(inputFields.indexOf(inputFieldAbove), newInputField);
        if(inputFieldAbove.getInputInformations().size() == 0) inputFields.remove(inputFieldAbove);
        drawInputFields();
        System.out.println("[LInputArea] Adding " + newInputField + " above " + inputFieldAbove);
    }

    public void addInputFieldBelow(LInputField newInputField, LInputField inputFieldBelow){
        inputFields.add(inputFields.indexOf(inputFieldBelow) + 1, newInputField);
        if(inputFieldBelow.getInputInformations().size() == 0) inputFields.remove(inputFieldBelow);
        drawInputFields();
        System.out.println("[LInputArea] Adding " + newInputField + " below " + inputFieldBelow);
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

}
