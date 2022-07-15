package app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.filehandling;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.NGram;

import java.util.List;

/**
 * @author filreh
 */
public interface iModelLibrary {
    /**
     * This method returns a model with the specified N.
     * If it didn't exist before it is created.
     * Adds it to the model locations. Also in the documents.txt
     * @param N
     * @return Model of size N.
     */
    NGram getModel(int N);

    /**
     * Returns all model locations.
     * @return Model locations.
     */
    List<String> allModelLocations();

    /**
     * Returns the amount of models stored currently.
     * @return Amount of models stored.
     */
    int getAmountModels();
}
