package app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.ModelCreator;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.NGram;
import app.display.dialogs.visual_editor.recs.display.ProgressBar;
import app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.filehandling.iModelLibrary;
import app.display.dialogs.visual_editor.recs.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author filreh
 */
public class ModelLibrary implements iModelLibrary 
{
    private final DocHandler docHandler;

    private List<String> modelLocations;
    private Map<Integer, NGram> allModels;

    //Singleton
    private static ModelLibrary lib;

    public static ModelLibrary getInstance() 
    {
        // create object if it's not already created
        if(lib == null) {
            lib = new ModelLibrary();
        }

        // returns the singleton object
        return lib;
    }

    private ModelLibrary() 
    {
        this.docHandler = DocHandler.getInstance();
        allModels = new HashMap<>();
        this.modelLocations = allModelLocations();
    }

    /**
     * This method returns a model with the specified N.
     * If it didn't exist before it is created. ANd written to a file.
     * Adds it to the model locations. Also in the documents.txt
     *
     * @param N
     */
    @Override
    public NGram getModel(int N) 
    {
        // progress bar
        ProgressBar pb = new ProgressBar("Fetching Data","",100);

        //1. check if it is in the already loaded in models
        NGram model = allModels.getOrDefault(Integer.valueOf(N), null);
        if(model == null) {
            model = addModel(N, pb);
        }
        pb.updateProgress(100);
        pb.close();
        return model;
    }

    /**
     * Returns all model locations, is updated everytime it is called
     */
    @Override
    public List<String> allModelLocations() 
    {
        modelLocations = new ArrayList<>();
        for(int N = 2; N <= 20; N++) {
            String location = docHandler.getModelLocation(N);
            //if the model exists
            if(!StringUtils.equals(location,DocHandler.MODEL_DOES_NOT_EXIST)) {
                modelLocations.add(location);
            }
        }
        return modelLocations;
    }

    /**
     * Returns the amount of models stored currently
     */
    @Override
    public int getAmountModels() 
    {
        //update the list before returning
        allModelLocations();
        return modelLocations.size();
    }

    /**
     * If the model is not already included in the list of models, then it is created and added to the list.
     * If this method is called, then the model is not in allModels
     * @param N
     * @return
     */
    private NGram addModel(int N, ProgressBar pb) 
    {
        NGram model = null;
        pb.updateProgress(33);
        //1. check if it exists
        if(docHandler.getModelLocation(N).equals(DocHandler.MODEL_DOES_NOT_EXIST)) 
        {
            //1.a does not exist: create a new one
            model = ModelCreator.createModel(N);

            //multithreading stop
            pb.updateProgress(90);

        } 
        else 
        {
            //1.b model does exist: read it in from file
            model = ModelFilehandler.readModel(N);
            pb.updateProgress(66);
        }
        //either way add it to the loaded in models
        allModels.put(Integer.valueOf(N), model);
        return model;
    }
}
