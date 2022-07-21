package app.display.dialogs.visual_editor.recs.codecompletion.domain.model;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.LudiiGameDatabase;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.ModelFilehandler;
import app.display.dialogs.visual_editor.recs.display.ProgressBar;
import app.display.dialogs.visual_editor.recs.utils.NGramUtils;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author filreh
 */
public class ModelCreator {
    /**
     * This method should only be called as part of the validation process or by the ModelLibrary
     * This method creates a new model and writes a model to a .gz file.
     * The location is according to the internal storing mechanism that is based on the N parameter.
     * This method only adds the game descriptions with the specified ids to the model
     *
     * By listing only a limited number of games, this method can construct a model for validation
     * @param N
     * @param gameIDs List of game descriptions to be included in the model
     * @param validation If true, the model will not be written to a file
     * @return
     */
    public static NGram createModel(int N, List<Integer> gameIDs, boolean validation) {

        NGram model = new NGram(N);

        LudiiGameDatabase db = LudiiGameDatabase.getInstance();

        // this method only adds the game descriptions with the specified ids to the model
        // not all games! therefore not db.getAmountGames()
        int amountGames = gameIDs.size();

        //create progressbar
        ProgressBar pb = new ProgressBar("Creating model","Creating the model from the game description database.",amountGames);
        //multithreading start

        //    T - the result type returned by this SwingWorker's doInBackground and  get methods
        //    V - the type used for carrying out intermediate results by this SwingWorker's
        //        publish and process methods
        SwingWorker<NGram,Integer> modelCreatorTask = new SwingWorker<NGram, Integer>() {

            @Override
            protected NGram doInBackground() throws Exception {
                NGram ngramModel = new NGram(N);
                for(int i = 0; i < amountGames; i++) {
                    int gameID = gameIDs.get(i);
                    String curGameDescription = db.getDescription(gameID);

                    //apply preprocessing
                    String cleanGameDescription = Preprocessing.preprocess(curGameDescription);

                    //add all instances of length in {2,...,N}
                    for(int j = 2; j <= N; j++) {
                        List<List<String>> substrings = NGramUtils.allSubstrings(cleanGameDescription, j);
                        for(List<String> substring : substrings) {
                            Instance curInstance = NGramUtils.createInstance(substring);
                            if(curInstance != null) {
                                ngramModel.addInstanceToModel(curInstance);
                            }
                        }
                    }
                    //update progressbar
                    double percent = (((double) i) / ((double) amountGames));
                    int progress = (int) (percent*100.0);
                    setProgress(progress);
                    Thread.sleep(125);
                }
                return ngramModel;
            }

            @Override
            public void process(List<Integer> chunks) {
                for(int chunk : chunks) {
                    pb.updateProgress(chunk);
                }
            }
        };

        modelCreatorTask.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double percent = (modelCreatorTask.getProgress() / 100.0);
                pb.updateProgress(percent);
            }
        });
        modelCreatorTask.execute();
        try {
            model = modelCreatorTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        //discard of the progressbar
        pb.close();
        //if the model is only created for validation purposes, it is not written to a file
        System.out.println("VALIDATION:"+validation);
        if(!validation) {
            ModelFilehandler.writeModel(model);
        }
        return model;
    }

    /**
     * This method should only be called as part of the validation process or by the ModelLibrary
     * This method creates a new model and writes a model to a .gz file.
     * The location is according to the internal storing mechanism that is based on the N parameter.
     * This method adds all game descriptions to the model and is therefore not for validation purposes.
     * @param N
     * @return
     */
    public static NGram createModel(int N) {
        List<Integer> gameIDs = new ArrayList<>();
        LudiiGameDatabase db = LudiiGameDatabase.getInstance();

        int amountGames = db.getAmountGames();

        for (int i = 0; i < amountGames; i++) {
            gameIDs.add(i);
        }

        //return createModel(N, gameIDs, false);
        return createModel(N, gameIDs, false);
    }
}
