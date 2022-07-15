package app.display.dialogs.visual_editor.recs.interfaces.codecompletion.controller;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Instance;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.NGram;

import java.util.List;

/**
 * @author filreh
 */
public interface iController {

    /**
     * Code Completion method for Visual Editor
     *
     * 1. Convert the context string into a Context object
     * 2. Get the matching Instance objects from the model
     * 3. Filter out the invalid Instances using the Grammar
     * 4. Use the BucketSort for the correct ordering
     * @param context
     * @return list of candidate predictions sort after matching words with context, multiplicity
     */
    List<Instance> getPicklist(String context);
    /**
     * Code Completion method for Visual Editor
     *
     * 1. Convert the context string into a Context object
     * 2. Get the matching Instance objects from the model
     * 3. Filter out the invalid Instances using the Grammar
     * 4. Use the BucketSort for the correct ordering
     * 5. Optional: Shorten list to maxLength
     * @param context
     * @param maxLength
     * @return list of candidate predictions sort after matching words with context, multiplicity
     */
    List<Instance> getPicklist(String context, int maxLength);

    /**
     * Code Completion method for Text Editor
     *
     * 1. Convert the context string into a Context object
     * 2. Get the matching Instance objects from the model
     * 3. Filter out the invalid Instances using the Grammar
     * 4. Use the BucketSort for the correct ordering
     * 5. Filter out choices based on begunWord
     * @param context
     * @param begunWord
     * @return
     */
    List<Instance> getPicklist(String context, String begunWord);

    /**
     * Code Completion method for Text Editor
     *
     * 1. Convert the context string into a Context object
     * 2. Get the matching Instance objects from the model
     * 3. Filter out the invalid Instances using the Grammar
     * 4. Use the BucketSort for the correct ordering
     * 5. Filter out choices based on begunWord
     * 6. Optional: Shorten list to maxLength
     * @param context
     * @param begunWord
     * @param maxLength
     * @return
     */
    List<Instance> getPicklist(String context, String begunWord, int maxLength);

    /**
     * This method switches out the current model, remember to update the N parameter
     * @param model
     */
    void changeModel(NGram model);

    /**
     * End all necessary connections and open links to storage. Discard the model
     */
    void close();

    /**
     * Get the value of N for the current model
     * @return
     */
    int getN();


}
