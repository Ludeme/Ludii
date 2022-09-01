package app.display.dialogs.visual_editor.recs.validation.controller;

import app.display.dialogs.visual_editor.recs.codecompletion.controller.NGramController;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling.LudiiGameDatabase;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Instance;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.ModelCreator;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.NGram;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Preprocessing;
import app.display.dialogs.visual_editor.recs.utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ValidationController {
    private static final boolean DEBUG = false;

    private static final double TRAINING_PROPORTION = 0.66;
    private static final int REPLICATIONS = 31;
    private static final int ITERATIONS = 1000;

    public static void validate(int minN, int maxN) {
        for(int N = minN; N <= maxN; N++) {
            validate(TRAINING_PROPORTION,REPLICATIONS,ITERATIONS,N);
        }
    }

    public static void validate() {
        for(int N = 2; N <= 15; N++) {
            switch (N){
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    validate(TRAINING_PROPORTION,REPLICATIONS,ITERATIONS,N);
                    break;
                case 9:
                case 10:
                    validate(TRAINING_PROPORTION,15,2*ITERATIONS,N);
                    break;
                case 11:
                case 12:
                    validate(TRAINING_PROPORTION,5,6*ITERATIONS,N);
                    break;
                case 13:
                case 14:
                case 15:
                    validate(TRAINING_PROPORTION,2,10*ITERATIONS,N);
                    break;
            }
        }
    }

    public static void validate(double trainingProportion, int replications, int iterations, int N) {
        String location = "src/app/display/dialogs/visual_editor/resources/recs/validation/precision_and_time/"+System.currentTimeMillis()+"_"+N+".csv";
        Report report = new Report(N,location);

        // game database
        LudiiGameDatabase db = LudiiGameDatabase.getInstance();
        int amtGames = db.getAmountGames();

        // replicate this experiment
        for(int r = 0; r < replications; r++) {
            // select games for the split
            CrossValidation crossValidation = new CrossValidation(amtGames, trainingProportion);
            List<Integer> trainingsIDs = crossValidation.getTrainingIDs();
            List<Integer> testIDs = crossValidation.getTestIDs();

            //model creation with the testIDs
            if(DEBUG)System.out.println("CREATING MODEL " + r);
            NGram model = ModelCreator.createModel(N, trainingsIDs, true);
            NGramController NGramController = new NGramController(model);

            long[] delays = new long[iterations];

            //precision
            byte[] top1PrecisionTest = new byte[iterations];
            byte[] top3PrecisionTest = new byte[iterations];
            byte[] top5PrecisionTest = new byte[iterations];
            byte[] top7PrecisionTest = new byte[iterations];

            //precision
            byte[] top1PrecisionTraining = new byte[iterations];
            byte[] top3PrecisionTraining = new byte[iterations];
            byte[] top5PrecisionTraining = new byte[iterations];
            byte[] top7PrecisionTraining = new byte[iterations];

            // TEST GAMES HERE
            gamestest:for(int i = 0; i < iterations; i++) {
                if(DEBUG)System.out.println("R: "+r+" i: "+i);
                //0. take one of the games, remove one word and store it
                //TRAINING
                if(DEBUG)System.out.println("Selecting Training word to cut out");
                int idTraining = (int) (Math.random() * (trainingsIDs.size()-1)); // select one random id
                idTraining = trainingsIDs.get(idTraining).intValue();
                String gameDescriptionTraining = db.getDescription(idTraining);

                gameDescriptionTraining = Preprocessing.preprocess(gameDescriptionTraining); // preprocess
                if(DEBUG)System.out.println("GAME DESC: " + gameDescriptionTraining + "ID: " + idTraining);
                if(gameDescriptionTraining.length() < 10) {
                    continue gamestest;
                }

                String[] splitTraining = gameDescriptionTraining.split(" ");

                String cutOutTraining = "";
                int j = -1;
                //find all words that are at least 3 symbols long
                List<Integer> atLeastLength3Training = new ArrayList<>();
                for(int f = 0; f < splitTraining.length; f++) {
                    if(splitTraining[f].length() > 2) {
                        atLeastLength3Training.add(Integer.valueOf(f));
                    }
                }
                double u = Math.random(); // at random
                j = (int) (u * (atLeastLength3Training.size() - 1));
                j = atLeastLength3Training.get(j).intValue();
                cutOutTraining = splitTraining[j];

                String contextTraining = splitTraining[0]; //given to code completion
                //get all words before the cut out word as context
                for(int k = 1; k < j && k < splitTraining.length; k++) {
                    contextTraining += " " + splitTraining[k];
                }
                //TEST
                if(DEBUG)System.out.println("Selecting Test word to cut out");
                int idTest = (int) (Math.random() * (testIDs.size()-1)); // select one random id
                idTest = testIDs.get(idTest).intValue();
                String gameDescriptionTest = db.getDescription(idTest);

                gameDescriptionTest = Preprocessing.preprocess(gameDescriptionTest); // preprocess
                if(DEBUG)System.out.println("GAME DESC:"+gameDescriptionTest + " ID: " + idTest);
                if(gameDescriptionTest.length() < 10) {
                    continue gamestest;
                }

                String[] splitTest = gameDescriptionTest.split(" ");

                String cutOutTest = "";
                j = -1;
                //find all words that are at least 3 symbols long
                List<Integer> atLeastLength3Test = new ArrayList<>();
                for(int f = 0; f < splitTest.length; f++) {
                    if(splitTest[f].length() > 2) {
                        atLeastLength3Test.add(Integer.valueOf(f));
                    }
                }
                u = Math.random(); // at random
                j = (int) (u * (atLeastLength3Test.size() - 1));
                j = atLeastLength3Test.get(j).intValue();
                cutOutTest = splitTest[j];

                String contextTest = splitTest[0]; //given to code completion

                for(int k = 1; k < j && k < splitTest.length; k++) {
                    contextTest += " " + splitTest[k];
                }



                //1. take the start time in nano seconds
                if(DEBUG)System.out.println("Generating predictions");
                long startTime = System.nanoTime();
                //2. get the picklistTest, length 7 suffices
                List<Instance> picklistTest = NGramController.getPicklist(contextTest,7);
                //3. take the stop time in nano seconds
                long endTime = System.nanoTime();
                long duration = endTime - startTime;

                List<Instance> picklistTraining = NGramController.getPicklist(contextTraining,7);

                //4. Calculate top 1, top 3, top 5 and top 7 precision
                if(DEBUG)System.out.println("Calculate Training precision");
                boolean top1Training = false;
                boolean top3Training = false;
                boolean top5Training = false;
                boolean top7Training = false;

                for(int k = 0; k < 7 && k < picklistTraining.size(); k++) {
                    Instance instance = picklistTraining.get(k);
                    if (StringUtils.equals(instance.getPrediction(), cutOutTraining)) {
                        // set the variables
                        if (k <= 6) {
                            top7Training = true;
                        }
                        if (k <= 4) {
                            top5Training = true;
                        }
                        if (k <= 2) {
                            top3Training = true;
                        }
                        if (k == 0) {
                            top1Training = true;
                        }
                        // exit out of the loop
                        break;
                    }
                }
                if(DEBUG)System.out.println("Calculate Test precision");
                boolean top1Test = false;
                boolean top3Test = false;
                boolean top5Test = false;
                boolean top7Test = false;

                for(int k = 0; k < 7 && k < picklistTest.size(); k++) {
                    Instance instance = picklistTest.get(k);
                    if (StringUtils.equals(instance.getPrediction(), cutOutTest)) {
                        // set the variables
                        if (k <= 6) {
                            top7Test = true;
                        }
                        if (k <= 4) {
                            top5Test = true;
                        }
                        if (k <= 2) {
                            top3Test = true;
                        }
                        if (k == 0) {
                            top1Test = true;
                        }
                        // exit out of the loop
                        break;
                    }
                }
                //5. add everything to the arrays
                if(DEBUG)System.out.println("Statistics step");
                delays[i] = duration;
                top1PrecisionTraining[i] = (byte) (top1Training ? 1 : 0);
                top3PrecisionTraining[i] = (byte) (top3Training ? 1 : 0);
                top5PrecisionTraining[i] = (byte) (top5Training ? 1 : 0);
                top7PrecisionTraining[i] = (byte) (top7Training ? 1 : 0);

                top1PrecisionTest[i] = (byte) (top1Test ? 1 : 0);
                top3PrecisionTest[i] = (byte) (top3Test ? 1 : 0);
                top5PrecisionTest[i] = (byte) (top5Test ? 1 : 0);
                top7PrecisionTest[i] = (byte) (top7Test ? 1 : 0);
            }

            //6. calculate statitics
            if(DEBUG)System.out.println("Calc Stats");
            double nanosSum = 0;

            int top1SumTraining = 0;
            int top3SumTraining = 0;
            int top5SumTraining = 0;
            int top7SumTraining = 0;

            int top1SumTest = 0;
            int top3SumTest = 0;
            int top5SumTest = 0;
            int top7SumTest = 0;
            for(int i = 0; i < iterations; i++) {
                nanosSum += (delays[i] / (double) iterations);
                top1SumTraining += top1PrecisionTraining[i];
                top3SumTraining += top3PrecisionTraining[i];
                top5SumTraining += top5PrecisionTraining[i];
                top7SumTraining += top7PrecisionTraining[i];

                top1SumTest += top1PrecisionTest[i];
                top3SumTest += top3PrecisionTest[i];
                top5SumTest += top5PrecisionTest[i];
                top7SumTest += top7PrecisionTest[i];
            }
            //times
            double nanosAverage = nanosSum;

            double top1AverageTraining = top1SumTraining / (double) iterations;
            double top3AverageTraining = top3SumTraining / (double) iterations;
            double top5AverageTraining = top5SumTraining / (double) iterations;
            double top7AverageTraining = top7SumTraining / (double) iterations;

            double top1AverageTest = top1SumTest / (double) iterations;
            double top3AverageTest = top3SumTest / (double) iterations;
            double top5AverageTest = top5SumTest / (double) iterations;
            double top7AverageTest = top7SumTest / (double) iterations;

            report.addRecord(Arrays.asList(Double.valueOf(r),
            		Double.valueOf(nanosAverage),
            		Double.valueOf(top1AverageTraining),Double.valueOf(top3AverageTraining),Double.valueOf(top5AverageTraining),Double.valueOf(top7AverageTraining),
            		Double.valueOf(top1AverageTest),Double.valueOf(top3AverageTest),Double.valueOf(top5AverageTest),Double.valueOf(top7AverageTest)));
        }

        report.writeToCSV();
    }
}
