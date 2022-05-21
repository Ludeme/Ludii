package gameDistance.utils.apted.test.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class represents a single test case from the JSON file. JSON keys
 * are mapped to fiels of this class.
 */
// [TODO] Verify if this is the best placement for this class.
public class TestCase {

    /**
     * Test identifier to quickly find failed test case in JSON file.
     */
    private final int testID;
    /**
     * Source tree as string.
     */
    private final String t1;
    /**
     * Destination tree as string.
     */
    private final String t2;
    /**
     * Correct distance value between source and destination trees.
     */
    private final int d;

    public TestCase(int testID, String t1, String t2, int d) {
        this.testID = testID;
        this.t1 = t1;
        this.t2 = t2;
        this.d = d;
    }

    /**
     * Used in printing the test case details on failure with '(name = "{0}")'.
     *
     * @return test case details.
     * @see CorrectnessTest#data()
     */
    @Override
    public String toString() {
        return "testID:" + testID + ",t1:" + t1 + ",t2:" + t2 + ",d:" + d;
    }

    /**
     * Returns identifier of this test case.
     *
     * @return test case identifier.
     */
    public int getTestID() {
        return testID;
    }

    /**
     * Returns source tree of this test case.
     *
     * @return source tree.
     */
    public String getT1() {
        return t1;
    }

    /**
     * Returns destination tree of this test case.
     *
     * @return destination tree.
     */
    public String getT2() {
        return t2;
    }

    /**
     * Returns correct distance value between source and destination trees
     * of this test case.
     *
     * @return correct distance.
     */
    public int getD() {
        return d;
    }

    public static Iterable<TestCase> loadFromFile(String fileName) {
        final String testCases = new BufferedReader(new InputStreamReader(TestCase.class.getResourceAsStream(fileName)))
            .lines()
            .map(String::trim)
            .collect(Collectors.joining("\n"));
        return loadFromJson(testCases)
            .collect(Collectors.toList());
    }

    private static Stream<TestCase> loadFromJson(String jsonText) {
        return StreamSupport.stream(new JSONArray(jsonText).spliterator(), false)
            .map(JSONObject.class::cast)
            .map(TestCase::fromJsonObject);
    }

    private static TestCase fromJsonObject(JSONObject jSONObject) throws JSONException {
        return new TestCase(
            jSONObject.getInt("testID"), jSONObject.getString("t1"), jSONObject.getString("t2"), jSONObject.getInt("d"));
    }

}
