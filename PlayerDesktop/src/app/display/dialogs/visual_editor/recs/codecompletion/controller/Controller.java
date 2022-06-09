package codecompletion.controller;

import codecompletion.Ludeme;
import codecompletion.domain.filehandling.DocHandler;
import codecompletion.domain.filehandling.ModelLibrary;
import codecompletion.domain.model.*;
import interfaces.codecompletion.controller.iController;
import utils.*;

import java.io.File;
import java.util.List;

/**
 * @author filreh
 */
public class Controller implements iController {

    private static final int MAX_PICKLIST_LENGTH = 50;

    private int N;
    private ModelLibrary lib;
    private Grammar grammar;
    private NGram model;
    private DocHandler docHandler;

    /**
     * Standard constructor
     * @param N
     */
    public Controller(int N) {
        this.N = N;
        initModel();
    }

    /**
     * This constructor is only for validation
     * @param model
     */
    public Controller(NGram model) {
        this.N = model.getN();
        initModel();
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
        grammar = Grammar.getInstance();
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
    @Override
    public List<Ludeme> getPicklist(String contextString) {
        // 1. acquire context and preprocess it
        String cleanContextString = Preprocessing.preprocess(contextString);
        Context context = NGramUtils.createContext(cleanContextString);
        // 2. context sensitivity
        List<Instance> match = model.getMatch(context.getKey());
        // 3. type-meatching
        List<Instance> unorderedPicklist = grammar.filterOutInvalid(context,match);
        // 4. Calculate Number of Matching words & Remove duplicate predictions
        List<Pair<Instance, Integer>> uniquePredictions = NGramUtils.uniteDuplicatePredictions(unorderedPicklist, context);
        // 5. Sorting after matching words and multiplicity
        List<Instance> picklist = BucketSort.sort(uniquePredictions, MAX_PICKLIST_LENGTH);
        // convert to ludemes
        List<Ludeme> picklistLudemes = Instance2Ludeme.foreachInstance2ludeme(picklist);
        return picklistLudemes;
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
    public List<Ludeme> getPicklist(String context, int maxLength) {
        List<Ludeme> picklist = getPicklist(context);
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
     * @return
     */
    @Override
    public List<Ludeme> getPicklist(String context, String begunWord) {
        String cleanBegunWord = Preprocessing.preprocessBegunWord(begunWord);
        System.out.println("CONTROLLER: context -> "+context);
        List<Ludeme> preliminaryPicklist = getPicklist(context);
        List<Ludeme> picklist = NGramUtils.filterByBegunWord(cleanBegunWord,preliminaryPicklist);
        return picklist;
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
     * @return
     */
    @Override
    public List<Ludeme> getPicklist(String context, String begunWord, int maxLength) {
        List<Ludeme> picklist;
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
     * @param model
     */
    @Override
    public void changeModel(NGram model) {
        this.model = model;
        this.N = model.getN();
    }

    /**
     * End all necessary connections and open links to storage. Discard the model
     */
    @Override
    public void close() {
        DocHandler docHandler = DocHandler.getInstance();
        //find all files in res/models that end in .csv and delete them
        //because models are stored compressed as .gz
        String modelsLocation = docHandler.getModelsLocation();
        List<File> allFilesModels = FileUtils.listFilesForFolder(modelsLocation);
        for(File f : allFilesModels) {
            String fPath = f.getPath();
            if(FileUtils.isFileCSV(fPath)) {
                FileUtils.deleteFile(fPath);
            }
        }

        docHandler.close();
    }

    /**
     * Get the value of N for the current model
     *
     * @return
     */
    @Override
    public int getN() {
        return N;
    }
}
