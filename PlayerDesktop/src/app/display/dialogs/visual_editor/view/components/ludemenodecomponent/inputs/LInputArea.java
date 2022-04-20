package app.display.dialogs.visual_editor.view.components.ludemenodecomponent.inputs;

import app.display.dialogs.visual_editor.model.grammar.input.Input;
import app.display.dialogs.visual_editor.view.components.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LInputArea extends JPanel {

    private List<LInputField> inputFields = new ArrayList<>();;
    private LudemeNodeComponent LNC;


    public LInputArea(LudemeNodeComponent ludemeNodeComponent) {
        this.LNC = ludemeNodeComponent;

        inputFields = getInputFields(LNC);
        drawInputFields();

    }

    public void drawInputFields(){
        removeAll();
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setAlignmentX(LEFT_ALIGNMENT);
        setBackground(DesignPalette.BACKGROUND_LUDEME_BODY);

       /* if(inputFields.size() == 1 && inputFields.get(0).getRequiredLudemes().size() == 1){
            LudemeNode ln = new LudemeNode(inputFields.get(0).getRequiredLudemes().get(0), 0, 0);
            this.inputFields = getInputFields(new LudemeNodeComponent(ln, LNC.getWidth(), LNC.getGraphPanel()));
            drawInputFields();
            return;
        } */

        for(LInputField inputField : inputFields){
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

    public void updateComponent(){
        mergeOptionalArgumentsInOne();
        drawInputFields();
    }

    private List<LInputField> getInputFields(LudemeNodeComponent ludemeNodeComponent){
        List<LInputField> fields = new ArrayList<>();
        List<Input> inputs = ludemeNodeComponent.getLudemeNode().getCurrentConstructor().getInputs();

        List<InputInformation> consequentOptionalInputs = new ArrayList<>();

        for(int i = 0; i < inputs.size(); i++){
            Input in = inputs.get(i);
            InputInformation ii = new InputInformation(ludemeNodeComponent.getLudemeNode().getCurrentConstructor(), in);

            if(ii.isOptional() && LNC.getLudemeNode().getProvidedInputs()[ii.getIndex()] == null){
                consequentOptionalInputs.add(ii);
                continue;
            }
            else if(consequentOptionalInputs.size() == 1){
                LInputField inputFieldPrevious = new LInputField(ludemeNodeComponent, consequentOptionalInputs.get(0));
                fields.add(inputFieldPrevious);
                consequentOptionalInputs = new ArrayList<>();
            }
            else if(consequentOptionalInputs.size() > 1){
                LInputField inputFieldPrevious = new LInputField(ludemeNodeComponent, consequentOptionalInputs);
                fields.add(inputFieldPrevious);
                consequentOptionalInputs = new ArrayList<>();
            }

            LInputField inputField = new LInputField(ludemeNodeComponent, ii);
            fields.add(inputField);
        }
        if(consequentOptionalInputs.size() == 1){
            LInputField inputFieldPrevious = new LInputField(ludemeNodeComponent, consequentOptionalInputs.get(0));
            fields.add(inputFieldPrevious);
            consequentOptionalInputs = new ArrayList<>();
        }
        if(consequentOptionalInputs.size() > 1){
            LInputField inputFieldPrevious = new LInputField(ludemeNodeComponent, consequentOptionalInputs);
            fields.add(inputFieldPrevious);
            consequentOptionalInputs = new ArrayList<>();
        }
        return fields;
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

    public void addInputFieldAbove(LInputField newInputField, LInputField inputFieldAbove){
        inputFields.add(inputFields.indexOf(inputFieldAbove), newInputField);
        if(inputFieldAbove.getInputInformations().size() == 0) inputFields.remove(inputFieldAbove);
        drawInputFields();
        System.out.println("Adding " + newInputField + " above " + inputFieldAbove);
    }

    public void addInputFieldBelow(LInputField newInputField, LInputField inputFieldBelow){
        inputFields.add(inputFields.indexOf(inputFieldBelow) + 1, newInputField);
        if(inputFieldBelow.getInputInformations().size() == 0) inputFields.remove(inputFieldBelow);
        drawInputFields();
        System.out.println("Adding " + newInputField + " below " + inputFieldBelow);
    }

    public void updateConstructor(){
        inputFields = getInputFields(LNC);
        drawInputFields();
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
