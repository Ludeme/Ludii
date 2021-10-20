package gameDistance.datasets;

import java.util.List;
import java.util.Map;

import game.Game;
import utils.data_structures.support.zhang_shasha.Tree;

public interface Dataset 
{
	/**
	 * @return dataset in a bag of words format <FeatureName, FeatureValue>
	 */
	Map<String, Double> getBagOfWords(Game game);
	
	/**
	 * @return dataset in a sequence format.
	 */
	List<String> getSequence(Game game);
	
	/**
	 * @return dataset in a tree format.
	 */
	Tree getTree(Game game);
}
