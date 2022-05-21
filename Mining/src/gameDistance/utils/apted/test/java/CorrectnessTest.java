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

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import gameDistance.utils.apted.costmodel.StringUnitCostModel;
import gameDistance.utils.apted.distance.APTED;
import gameDistance.utils.apted.node.Node;
import gameDistance.utils.apted.node.StringNodeData;
import gameDistance.utils.apted.parser.BracketStringInputParser;

/**
 * Correctness unit tests of distance and mapping computation.
 *
 * <p>In case of mapping, only mapping cost is verified against the correct
 * distance.
 *
 * <p>Currently tests only for unit-cost model and single string-value labels.
 *
 * @see StringNodeData
 * @see StringUnitCostModel
 */
@SuppressWarnings("all")
@RunWith(Parameterized.class)
public class CorrectnessTest {

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
  public CorrectnessTest(final TestCase testCase) {
    this.testCase = testCase;
  }

  /**
   * Parse trees from bracket notation to {node.StringNodeData}, convert back
   * to strings and verify equality with the input.
   */
  @Test
  public void parsingBracketNotationToStringNodeData() {
    // Parse the input.
    final BracketStringInputParser parser = new BracketStringInputParser();
    final Node<StringNodeData> t1 = parser.fromString(testCase.getT1());
    final Node<StringNodeData> t2 = parser.fromString(testCase.getT2());
    assertEquals(testCase.getT1(), t1.toString());
    assertEquals(testCase.getT2(), t2.toString());
  }

  /**
   * Compute TED for a single test case and compare to the correct value. Uses
   * node labels with a single string value and unit cost model.
   *
   * @see node.StringNodeData
   * @see costmodel.StringUnitCostModel
   */
  @Test
  public void distanceUnitCostStringNodeDataCostModel() {
    // Parse the input.
    final BracketStringInputParser parser = new BracketStringInputParser();
    final Node<StringNodeData> t1 = parser.fromString(testCase.getT1());
    final Node<StringNodeData> t2 = parser.fromString(testCase.getT2());
    // Initialise APTED.
    final APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
    // This cast is safe due to unit cost.
    int result = (int)apted.computeEditDistance(t1, t2);
    assertEquals(testCase.getD(), result);
    // Verify the symmetric case.
    result = (int)apted.computeEditDistance(t2, t1);
    assertEquals(testCase.getD(), result);
  }

  /**
   * Compute TED for a single test case and compare to the correct value. Uses
   * node labels with a single string value and unit cost model.
   *
   * <p>Triggers spf_L to execute. The strategy is fixed to left paths in the
   * left-hand tree.
   *
   * @see node.StringNodeData
   * @see costmodel.StringUnitCostModel
   */
  @Test
  public void distanceUnitCostStringNodeDataCostModelSpfL() {
    // Parse the input.
    final BracketStringInputParser parser = new BracketStringInputParser();
    final Node<StringNodeData> t1 = parser.fromString(testCase.getT1());
    final Node<StringNodeData> t2 = parser.fromString(testCase.getT2());
    // Initialise APTED.
    final APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
    // This cast is safe due to unit cost.
    final int result = (int)apted.computeEditDistance_spfTest(t1, t2, 0);
    assertEquals(testCase.getD(), result);
  }

  /**
   * Compute TED for a single test case and compare to the correct value. Uses
   * node labels with a single string value and unit cost model.
   *
   *<p>Triggers spf_R to execute. The strategy is fixed to right paths in the
   * left-hand tree.
   *
   * @see node.StringNodeData
   * @see costmodel.StringUnitCostModel
   */
  @Test
  public void distanceUnitCostStringNodeDataCostModelSpfR() {
    // Parse the input.
    final BracketStringInputParser parser = new BracketStringInputParser();
    final Node<StringNodeData> t1 = parser.fromString(testCase.getT1());
    final Node<StringNodeData> t2 = parser.fromString(testCase.getT2());
    // Initialise APTED.
    final APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
    // This cast is safe due to unit cost.
    final int result = (int)apted.computeEditDistance_spfTest(t1, t2, 1);
    assertEquals(testCase.getD(), result);
  }

  // IDEA: Write test that triggers spf_A for each subtree pair - disallow
  //       using spf_L and spf_R.

  /**
   * Compute minimum-cost edit mapping for a single test case and compare its
   * cost to the correct TED value. Uses node labels with a single string value
   * and unit cost model.
   *
   * @see node.StringNodeData
   * @see costmodel.StringUnitCostModel
   */
  @Test
  public void mappingCostUnitCostStringNodeDataCostModel() {
    // Parse the input.
    final BracketStringInputParser parser = new BracketStringInputParser();
    final Node<StringNodeData> t1 = parser.fromString(testCase.getT1());
    final Node<StringNodeData> t2 = parser.fromString(testCase.getT2());
    // Initialise APTED.
    final APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
    // Although we don't need TED value yet, TED must be computed before the
    // mapping. This cast is safe due to unit cost.
    apted.computeEditDistance(t1, t2);
    // Get TED value corresponding to the computed mapping.
    final List<int[]> mapping = apted.computeEditMapping();
    // This cast is safe due to unit cost.
    final int result = (int)apted.mappingCost(mapping);
    assertEquals(testCase.getD(), result);
  }

    @Parameters
    public static Iterable<TestCase> testCase() {
        return TestCase.loadFromFile("/correctness_test_cases.json");
    }
}
