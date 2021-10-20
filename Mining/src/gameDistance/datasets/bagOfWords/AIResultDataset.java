package gameDistance.datasets.bagOfWords;

import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import utils.data_structures.support.zhang_shasha.Tree;

/**
 * Dataset containing performance results of different AI.
 * - BagOfWords
 * 
 * @author matthew.stephenson
 */
public class AIResultDataset implements Dataset
{

	@Override
	public Map<String, Double> getBagOfWords(final Game game) 
	{
		return null;
	}

	/**
	 * Not Supported
	 */
	@Override
	public List<String> getSequence(final Game game)
	{
		return null;
	}

	/**
	 * Not Supported
	 */
	@Override
	public Tree getTree(final Game game) 
	{
		return null;
	}

}
