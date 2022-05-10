package app.display.dialogs.visual_editor.recs.validation;


import app.display.dialogs.visual_editor.recs.model.Ludii.NGramModelLudii;
import app.display.dialogs.visual_editor.recs.split.LudiiFileCleanup;
import app.display.dialogs.visual_editor.recs.split.SentenceSplit;
import app.display.dialogs.visual_editor.recs.utils.FileUtils;
import app.display.dialogs.visual_editor.recs.utils.ReadAllGameFiles;
import app.display.dialogs.visual_editor.recs.utils.Triple;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NGramValidation {
    public static final int K = 10;
    public static final int M = 5;
    public static final String WILDCARD_REC = "--REC--";
    private static final boolean DEBUG = false;

    public static void handleValidations(int maxN) throws IOException {
        NGramModelLudii[] models = new NGramModelLudii[maxN - 1];
        List<ArrayList<Double>> readingModelsTimeComplexity = new ArrayList<>();
        //prepare data
        List<String> locations = ReadAllGameFiles.findAllGames("res/Ludii/lud");
        List<String> gameDescriptions = new ArrayList<>();
        for(String location : locations) {//load & clean up
            String gameDescription = LudiiFileCleanup.allLinesOneString(location);
            gameDescriptions.add(gameDescription);
        }
        for(int N = 6; N <= maxN; N++) {
            System.out.println("Validating "+N+"-Gram model");
            kFoldCrossValidation(N, K, gameDescriptions);
            System.out.println("Finished validating "+N+"-Gram model");
        }

    }

    public static void kFoldCrossValidation(int N, int k, List<String> gameDescriptions) throws IOException {
        //          id,time_ms,top_5_hit
        List<Triple<Integer,Long,Boolean>> performanceData = new ArrayList<>();

        //divide the data into k folds
        List<List<String>> folds = new ArrayList<>(k);
        int foldSize = gameDescriptions.size()/k;
        System.out.println("Dividing into folds...");
        for(int foldIt = 0; foldIt < k - 1; foldIt++) {
            List<String> fold = gameDescriptions.subList(foldIt*foldSize,(foldIt+1)*foldSize);
            folds.add(fold);
        }
        //add the rest to the last fold
        //k-1 because arraylist starts at 0 --> until (k-1) are k folds!!
        List<String> foldTmp = gameDescriptions.subList((k-1)*foldSize,(k-1)*(foldSize+1));
        folds.add(foldTmp);

        //for each fold perform validation
        int validationId = 0;//increased when added to list
        for(int foldIt = 0; foldIt < k; foldIt++) {
            System.out.println("Performing validation on fold "+foldIt+"...");
            List<String> fold = folds.get(foldIt);
            //each fold contains game descriptions
            //1. train model with all folds but the current one
            List<String> curGameDescriptions = new ArrayList<>();
            for(int foldJt = 0; foldJt < k; foldJt++) {
                //leave out the current fold
                if(foldJt != foldIt) {
                    curGameDescriptions.addAll(folds.get(foldJt));
                }
            }
            String input = curGameDescriptions.get(0);
            NGramModelLudii m = new NGramModelLudii(input,N);
            List<String> curGDFromIndex1 = curGameDescriptions.subList(1, curGameDescriptions.size());
            for(String gd : curGDFromIndex1) {
                m.addToModel(gd);
            }
            //2. on each game description of the current fold:
            //found on: https://stackoverflow.com/questions/2444019/how-do-i-generate-a-random-integer-between-min-and-max-in-java?rq=1
            Random random = new Random();
            for(String gameDescription : fold) {
                //perform it more times
                int amt = 20;
                for(int i = 0; i < amt; i++) {
                    //2.0. split into list of words
                    List<String> words = SentenceSplit.splitText(gameDescription);
                    words = LudiiFileCleanup.cleanup(words);
                    //2.a. replace one word with --REC-- to get rec there
                    int max = words.size() - 1;
                    int min = 0;
                    int randomIndex = random.nextInt(max + 1 - min) + min;
                    String correct = words.get(randomIndex);
                    words.set(randomIndex, WILDCARD_REC);
                    words = words.subList(0, randomIndex);
                    //2.b. pass to match in prediction & record time
                    long start = System.currentTimeMillis();
                    //check prediction top 5
                    boolean top5Hit = topMHitInPrediction(M, m, words, correct);
                    long finish = System.currentTimeMillis();
                    long timeTakenMs = finish - start;
                    //2.c. add to list
                    //id, time, top5hit
                    performanceData.add(new Triple<>(validationId++, timeTakenMs, top5Hit));
                }
            }
        }


        //write performance data to file
        FileWriter fw = FileUtils.writeFile("res/validation/LudiiModel"+N+"Performance.csv");
        fw.write("validation_id,time_ms,top_5_hit,time_avg,time_min,time_max,top_5_precision_avg\n");
        System.out.println("Writing results...");
        double timeSum = 0;
        double hitSum = 0;
        double timeMin = 1000000000;
        double timeMax = -1;
        for(Triple<Integer,Long,Boolean> t : performanceData) {
            double timeMs = t.getS();
            if(timeMs < timeMin) {
                timeMin = timeMs;
            }
            if(timeMs > timeMax) {
                timeMax = timeMs;
            }

            timeSum += timeMs;
            int hit = t.getT() ? 1 : 0;
            hitSum += hit;
        }
        double hitAvg = hitSum/ (double)performanceData.size();
        double timeAvg = timeSum/ (double)performanceData.size();
        fw.write(",,,"+timeAvg+","+timeMin+","+timeMax+","+hitAvg+"\n");
        performanceData.forEach(t-> {
            try {
                //             N,time_ms,top_5_hit
                int hit = t.getT() ? 1 : 0;
                fw.write(t.getR()+","+t.getS()+","+hit+"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        fw.close();
    }


    /**
     * This method is a subroutine of the k fold cross validation
     * @param m
     * @param context
     * @param correct
     * @return whether or not there was a hit in the top 5 of the predictions
     */
    private static boolean topMHitInPrediction(int topM, NGramModelLudii m, List<String> context, String correct) {
        if(DEBUG)System.out.println(context);
        //1. get picklist
        List<String> picklist = m.getPicklist(context,topM);
        if(DEBUG)System.out.println("PICKLIST"+picklist);
        for(String prediction : picklist) {
            if(prediction.equals(correct)) {
                return true;
            }
        }
        return false;
    }
}
