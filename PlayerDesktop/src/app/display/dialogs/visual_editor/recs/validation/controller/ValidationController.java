package validation.controller;

import codecompletion.Ludeme;
import codecompletion.controller.Controller;
import codecompletion.domain.filehandling.LudiiGameDatabase;
import codecompletion.domain.model.ModelCreator;
import codecompletion.domain.model.NGram;
import codecompletion.domain.model.Preprocessing;
import utils.StringUtils;
import java.util.Arrays;
import java.util.List;

public class ValidationController {

    private static final double TEST_PROPORTION = 0.66;
    private static final int REPLICATIONS = 31;
    private static final int ITERATIONS = 1000;

    public void validate(int minN, int maxN) {
        for(int N = minN; N <= maxN; N++) {
            validate(N);
        }
    }

    public void validate(int N) {
        String location = "res/out/"+System.currentTimeMillis()+"_"+N+".csv";
        Report report = new Report(N,location);

        // game database
        LudiiGameDatabase db = LudiiGameDatabase.getInstance();
        int amtGames = db.getAmountGames();

        // replicate this experiment
        for(int r = 0; r < REPLICATIONS; r++) {
            // select games for the split
            CrossValidation crossValidation = new CrossValidation(amtGames, TEST_PROPORTION);
            List<Integer> testIDs = crossValidation.getTestIDs();
            List<Integer> validationIDs = crossValidation.getValidationIDs();

            //model creation with the testIDs
            NGram model = ModelCreator.createModel(N, testIDs, true);
            Controller controller = new Controller(model);

            //times
            long currentTime;

            long[] delays = new long[ITERATIONS];

            //precision
            byte[] top1Precision = new byte[ITERATIONS];
            byte[] top3Precision = new byte[ITERATIONS];
            byte[] top5Precision = new byte[ITERATIONS];
            byte[] top7Precision = new byte[ITERATIONS];

            // TEST GAMES HERE
            for(int i = 0; i < ITERATIONS; i++) {
                //0. take one of the games, remove one word and store it
                int id = (int) (Math.random() * (validationIDs.size()-1)); // select one random id
                String gameDescription = db.getDescription(id);

                gameDescription = Preprocessing.preprocess(gameDescription); // preprocess

                String[] split = gameDescription.split(" ");

                String cutOut = "";
                int j = -1;
                while(cutOut.length() < 3) {
                    // select one that is a word --> at least 3 chars long
                    double u = Math.random(); // at random
                    j = (int) (u * (split.length - 1));
                    cutOut = split[j];
                }

                String context = split[0]; //given to code completion

                for(int k = 1; k < j && k < split.length; k++) {
                    context += " " + split[k];
                }



                //1. take the start time in nano seconds
                long startTime = System.nanoTime();
                //2. get the picklist, length 7 suffices
                List<Ludeme> picklist = controller.getPicklist(context,7);
                //3. take the stop time in nano seconds
                long endTime = System.nanoTime();
                long duration = endTime - startTime;
                //4. Calculate top 1, top 3, top 5 and top 7 precision


                boolean top1 = false;
                boolean top3 = false;
                boolean top5 = false;
                boolean top7 = false;

                for(int k = 0; k < 7 && k < picklist.size(); k++) {
                    Ludeme ludeme = picklist.get(k);
                    if (StringUtils.equals(ludeme.getKeyword(), cutOut)) {
                        // set the variables
                        if (k <= 6) {
                            top7 = true;
                        }
                        if (k <= 4) {
                            top5 = true;
                        }
                        if (k <= 2) {
                            top3 = true;
                        }
                        if (k == 0) {
                            top1 = true;
                        }
                        // exit out of the loop
                        break;
                    }
                }
                //5. add everything to the arrays
                delays[i] = duration;
                top1Precision[i] = (byte) (top1 ? 1 : 0);
                top3Precision[i] = (byte) (top3 ? 1 : 0);
                top5Precision[i] = (byte) (top5 ? 1 : 0);
                top7Precision[i] = (byte) (top7 ? 1 : 0);
            }

            //6. calculate statitics
            double nanosSum = 0;
            int top1Sum = 0;
            int top3Sum = 0;
            int top5Sum = 0;
            int top7Sum = 0;
            for(int i = 0; i < ITERATIONS; i++) {
                nanosSum += (delays[i] / (double) ITERATIONS);
                top1Sum += top1Precision[i];
                top3Sum += top3Precision[i];
                top5Sum += top5Precision[i];
                top7Sum += top7Precision[i];
            }
            //times
            double nanosAverage = nanosSum;
            double milisAverage = nanosAverage / 1000000.0;
            double sAverage = milisAverage / 1000.0;
            double minAverage = sAverage / 60.0;
            double hAverage = minAverage / 60.0;
            double top1Average = top1Sum / (double) ITERATIONS;
            double top3Average = top3Sum / (double) ITERATIONS;
            double top5Average = top5Sum / (double) ITERATIONS;
            double top7Average = top7Sum / (double) ITERATIONS;

            report.addRecord(Arrays.asList((double)r,nanosAverage,milisAverage,sAverage,minAverage,hAverage,
                    top1Average,top3Average,top5Average,top7Average));
        }

        report.writeToCSV();
    }
}
