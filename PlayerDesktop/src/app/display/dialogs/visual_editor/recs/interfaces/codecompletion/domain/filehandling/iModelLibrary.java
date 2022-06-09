package interfaces.codecompletion.domain.filehandling;

import codecompletion.domain.model.NGram;
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
     * @return
     */
    NGram getModel(int N);

    /**
     * Returns all model locations
     * @return
     */
    List<String> allModelLocations();

    /**
     * Returns the amount of models stored currently
     * @return
     */
    int getAmountModels();
}
