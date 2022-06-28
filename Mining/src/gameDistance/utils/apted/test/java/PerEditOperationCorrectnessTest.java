package gameDistance.utils.apted.test.java;
/* MIT License
 *
 * Copyright (c) 2017 Mateusz Pawlik
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import gameDistance.utils.apted.costmodel.PerEditOperationStringNodeDataCostModel;
import gameDistance.utils.apted.distance.APTED;
import gameDistance.utils.apted.distance.AllPossibleMappingsTED;
import gameDistance.utils.apted.node.Node;
import gameDistance.utils.apted.node.StringNodeData;
import gameDistance.utils.apted.parser.BracketStringInputParser;

/**
 * Correctness unit tests of distance computation for node labels with a single
 * string value and per-edit-operation cost model.
 *
 * @see node.StringNodeData
 * @see costmodel.PerEditOperationStringNodeDataCostModel
 */
@SuppressWarnings("all")
@RunWith(Parameterized.class)
public class PerEditOperationCorrectnessTest {

  /**
   * Test case object holding parameters of a single test case.
   *
   * <p>Could be also deserialized here but without much benefit.
   */
  private final TestCase testCase;


  /**
   * Constructs a single test for a single test case. Used for parameterised
   * tests.
   *
   * @param testCase single test case.
   */
  public PerEditOperationCorrectnessTest(final TestCase testCase) {
    this.testCase = testCase;
  }

  /**
   * Compute TED for a single test case and compare to the correct value. Uses
   * node labels with a single string value and per-edit-operation cost model.
   *
   * <p>The correct value is calculated using AllPossibleMappingsTED algorithm.
   * <p>The costs of edit operations are set to some example values different
   * than in the unit cost model.
   *
   * @see node.StringNodeData
   * @see costmodel.PerEditOperationStringNodeDataCostModel
   * @see distance.AllPossibleMappingsTED
   */
  @Test
  public void distancePerEditOperationStringNodeDataCostModel() {
    // Parse the input.
    final BracketStringInputParser parser = new BracketStringInputParser();
    final Node<StringNodeData> t1 = parser.fromString(testCase.getT1());
    final Node<StringNodeData> t2 = parser.fromString(testCase.getT2());
    // Initialise algorithms.
    final APTED<PerEditOperationStringNodeDataCostModel, StringNodeData> apted = new APTED<>(new PerEditOperationStringNodeDataCostModel(0.4f, 0.4f, 0.6f));
    final AllPossibleMappingsTED<PerEditOperationStringNodeDataCostModel, StringNodeData> apmted = new AllPossibleMappingsTED<>(new PerEditOperationStringNodeDataCostModel(0.4f, 0.4f, 0.6f));
    // Calculate distances using both algorithms.
    final float result = apted.computeEditDistance(t1, t2);
    final float correctResult = apmted.computeEditDistance(t1, t2);
    assertEquals(correctResult, result, 0.0001);
  }

    @Parameters
    public static Iterable<TestCase> testCase() {
        return TestCase.loadFromFile("/mini.json");
    }

}
