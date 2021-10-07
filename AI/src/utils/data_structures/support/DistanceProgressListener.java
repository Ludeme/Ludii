package utils.data_structures.support;

/**
 * while a distance matrix get filled, update is 
 * @author Markus
 *
 */
public interface DistanceProgressListener
{
	
	void update(boolean assumeFirstLineIsSlower, int lengthFirstRow, double percentage, int completedComparisions, int totalComparisions);

	void update(int completedComparisions, int totalComparisions);


}
