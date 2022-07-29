package app.display.dialogs.visual_editor.recs.validation.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * This class creates a list of the games in the test and validation sets
 */
public class CrossValidation {
    private final int amtGames;
    private final double trainingProbability;
    private final double testProbability;

    private List<Integer> trainingIDs, testIDs;

    public CrossValidation(int amtGames, double trainingProbability) {
        this.amtGames = amtGames;
        this.trainingProbability = trainingProbability;
        this.testProbability = 1.0 - trainingProbability;

        trainingIDs = new ArrayList<>();
        testIDs = new ArrayList<>();

        selectIDs();
    }

    /**
     * every time a random number (0,1) is created, if it is smaller than testProbability,
     * it is added to the testIDs, else to the validationIDs
     */
    private void selectIDs() {
        for(int id = 0; id < amtGames; id++) {
            double u = Math.random();

            if(u < trainingProbability) {
                trainingIDs.add(id);
            } else {
                testIDs.add(id);
            }
        }
    }

    public List<Integer> getTrainingIDs() {
        return trainingIDs;
    }

    public List<Integer> getTestIDs() {
        return testIDs;
    }

	public double getTestProbability()
	{
		return testProbability;
	}

}
