package reconstruction.preprocessing;

/**
 * To run the preprocessing steps of the recons process (not the CSN generation).
 * 
 * @author Eric.Piette
 */
public final class PreProcessReconstruction
{
	public static void main(final String[] args)
	{
		System.out.println("********** Generate all the complete ruleset description on a single line **********");
		FormatRulesetAndIdOnOneLine.generateCSV();
		
		System.out.println("********** Generate avg true concepts between recons and complete rulesets **********");
		ComputeCommonExpectedConcepts.generateCSVs();
	}
}