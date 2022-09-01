package app.display.dialogs.visual_editor.recs.codecompletion.controller;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.DocHandler;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.ModelLibrary;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Context;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Instance;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.NGram;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Preprocessing;
import app.display.dialogs.visual_editor.recs.interfaces.codecompletion.controller.iController;
import app.display.dialogs.visual_editor.recs.utils.*;

import java.io.File;
import java.util.List;

import static app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Preprocessing.COMPLETION_WILDCARD;

/**
 * @author filreh
 */
public class NGramController implements iController {

    private static final int MAX_PICKLIST_LENGTH = 50;

    private int N;
    private ModelLibrary lib;
    private NGram model;
    private DocHandler docHandler;

    /**
     * Standard constructor
     * @param N
     */
    public NGramController(int N) {
        this.N = N;
        initModel();
    }

    /**
     * This constructor is only for validation
     * @param model
     */
    public NGramController(NGram model) {
        this.N = model.getN();
        docHandler = DocHandler.getInstance();
        this.model = model;
    }

    /**
     * This method should
     * - request a model with the specified N from the ModelLibrary
     * - load in the grammar
     */
    private void initModel() {
        docHandler = DocHandler.getInstance();
        lib = ModelLibrary.getInstance();
        model = lib.getModel(N);
    }

    /**
     * Code Completion method for Visual Editor
     *
     * 1. Convert the context string into a Context object
     * 2. Get the matching Instance objects from the model
     * 3. Filter out the invalid Instances using the Grammar
     * 4. Use the BucketSort for the correct ordering
     *
     * @param contextString
     * @return list of candidate predictions sort after matching words with context, multiplicity
     */
    @SuppressWarnings("all")
    @Override
    public List<Instance> getPicklist(String contextString) {
        // 0. if there is the wildcard COMPLETION_WILDCARD in there and cut it and everything after it off
        if(contextString.contains(COMPLETION_WILDCARD)) {
            int pos = contextString.lastIndexOf(COMPLETION_WILDCARD);
            contextString = contextString.substring(0,pos);
        }
        // 1. acquire context and preprocess it
        String cleanContextString = Preprocessing.preprocess(contextString);
        Context context = NGramUtils.createContext(cleanContextString);
        // 2. context sensitivity
        List<Instance> match = model.getMatch(context.getKey());
        // 4. Calculate Number of Matching words & Remove duplicate predictions
        List<Pair<Instance, Integer>> uniquePredictions = NGramUtils.uniteDuplicatePredictions(match, context);
        // 5. Sorting after matching words and multiplicity
        List<Instance> picklist = BucketSort.sort(uniquePredictions, MAX_PICKLIST_LENGTH);
        return picklist;
    }

    /**
     * Code Completion method for Visual Editor
     *
     * 1. Convert the context string into a Context object
     * 2. Get the matching Instance objects from the model
     * 3. Filter out the invalid Instances using the Grammar
     * 4. Use the BucketSort for the correct ordering
     * 5. Optional: Shorten list to maxLength
     *
     * @param context
     * @param maxLength
     * @return list of candidate predictions sort after matching words with context, multiplicity
     */
    @Override
    public List<Instance> getPicklist(String context, int maxLength) {
        List<Instance> picklist = getPicklist(context);
        if(picklist.size() >= maxLength) {
            picklist = picklist.subList(0,maxLength);
        }
        return picklist;
    }

    /**
     * Code Completion method for Text Editor
     *
     * 1. Convert the context string into a Context object
     * 2. Get the matching Instance objects from the model
     * 3. Filter out the invalid Instances using the Grammar
     * 4. Use the BucketSort for the correct ordering
     * 5. a. Preprocess begunWord
     *    b. Filter out choices based on begunWord
     *
     * @param context
     * @param begunWord
     */
    @Override
    public List<Instance> getPicklist(String context, String begunWord) {
        //String cleanBegunWord = Preprocessing.preprocessBegunWord(begunWord);
        System.out.println("CONTROLLER: context -> "+context);
        //List<Instance> preliminaryPicklist = getPicklist(context);
        //List<Symbol> picklist = NGramUtils.filterByBegunWord(cleanBegunWord,preliminaryPicklist);//TODO
        return null;
    }

    /**
     * Code Completion method for Text Editor
     *
     * 1. Convert the context string into a Context object
     * 2. Get the matching Instance objects from the model
     * 3. Filter out the invalid Instances using the Grammar
     * 4. Use the BucketSort for the correct ordering
     * 5. Filter out choices based on begunWord
     * 6. Optional: Shorten list to maxLength
     *
     * @param context
     * @param begunWord
     * @param maxLength
     */
    @Override
    public List<Instance> getPicklist(String context, String begunWord, int maxLength) {
        List<Instance> picklist;
        if(StringUtils.equals(begunWord,"")) {
            picklist = getPicklist(context,maxLength);
        } else {
            picklist = getPicklist(context, begunWord);
        }
        if(picklist.size() >= maxLength) {
            picklist = picklist.subList(0,maxLength);
        }
        return picklist;
    }

    /**
     * This method switches out the current model, remember to update the N parameter
     *
     * @param ngramModel
     */
    @Override
    public void changeModel(NGram ngramModel) {
        this.model = ngramModel;
        this.N = ngramModel.getN();
    }

    /**
     * End all necessary connections and open links to storage. Discard the model
     */
    @Override
    public void close() {
        DocHandler docHandlerInstance = DocHandler.getInstance();
        //find all files in res/models that end in .csv and delete them
        //because models are stored compressed as .gz
        String modelsLocation = docHandlerInstance.getModelsLocation();
        List<File> allFilesModels = FileUtils.listFilesForFolder(modelsLocation);
        for(File f : allFilesModels) {
            String fPath = f.getPath();
            if(FileUtils.isFileCSV(fPath)) {
                FileUtils.deleteFile(fPath);
            }
        }

        docHandlerInstance.close();
    }

    /**
     * Get the value of N for the current model
     */
    @Override
    public int getN() {
        return N;
    }
    
    /**
     * @return a docHandler.
     */
    public DocHandler getDocHandler()
    {
    	return docHandler;
    }
}
