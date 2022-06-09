package validation.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * This class creates a list of the games in the test and validation sets
 */
public class CrossValidation {
    private final int amtGames;
    private final double testProbability;
    private final double validationProbability;

    private List<Integer> testIDs, validationIDs;

    public CrossValidation(int amtGames, double testProbability) {
        this.amtGames = amtGames;
        this.testProbability = testProbability;
        this.validationProbability = 1.0 - testProbability;

        testIDs = new ArrayList<>();
        validationIDs = new ArrayList<>();

        selectIDs();
    }

    /**
     * every time a random number (0,1) is created, if it is smaller than testProbability,
     * it is added to the testIDs, else to the validationIDs
     */
    private void selectIDs() {
        for(int id = 0; id < amtGames; id++) {
            double u = Math.random();

            if(u < testProbability) {
                testIDs.add(id);
            } else {
                validationIDs.add(id);
            }
        }
    }

    public List<Integer> getTestIDs() {
        return testIDs;
    }

    public List<Integer> getValidationIDs() {
        return validationIDs;
    }


}
