package app.display.dialogs.visual_editor.model;

import app.display.dialogs.visual_editor.model.grammar.input.Input;

/**
 * User-provided input for an Input(field)
 * @author Filipp Dokienko
 */

public class ProvidedInput {
    private Input inputType;
    private Object providedInput;

    private boolean isBoolean;

    public ProvidedInput(boolean bool){

    }

    public ProvidedInput(int integer){

    }
}
