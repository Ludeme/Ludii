package gameDistance.metrics.treeEdit;

import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import gameDistance.metrics.DistanceMetric;
import utils.data_structures.support.zhang_shasha.Tree;

/**
 * Returns Zhang-Shasha tree edit distance.
 * 
 * @author matthew.stephenson
 */
public class ZhangShasha implements DistanceMetric
{
	
	//---------------------------------------------------------------------
	
	@Override
	public double distance(Dataset dataset, Map<String, Double> vocabulary, Game gameA, Game gameB)
	{
		final Tree treeA = dataset.getTree(gameA);
		final Tree treeB = dataset.getTree(gameB);
		
		final int edits = Tree.ZhangShasha(treeA, treeB);
		final int maxTreeSize = Math.max(treeA.size(), treeB.size()); 
		return (double) edits / maxTreeSize;
	}
	
	//---------------------------------------------------------------------
	
}
