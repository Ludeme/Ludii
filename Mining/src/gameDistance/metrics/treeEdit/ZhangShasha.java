package gameDistance.metrics.treeEdit;

import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import gameDistance.metrics.DistanceMetric;
import utils.data_structures.support.zhang_shasha.Tree;

/**
 * Returns Zhang-Shasha tree edit distance.
 * https://www.researchgate.net/publication/220618233_Simple_Fast_Algorithms_for_the_Editing_Distance_Between_Trees_and_Related_Problems
 * 
 * @author matthew.stephenson
 */
public class ZhangShasha implements DistanceMetric
{
	
	//---------------------------------------------------------------------
	
	@Override
	public double distance(final Dataset dataset, final Map<String, Double> vocabulary, final Game gameA, final Game gameB)
	{
		final Tree treeA = dataset.getTree(gameA);
		final Tree treeB = dataset.getTree(gameB);
		
		final int edits = Tree.ZhangShasha(treeA, treeB);
		
		final int maxTreeSize = Math.max(treeA.size(), treeB.size()); 
		
		return (double) edits / maxTreeSize;
	}
	
	//---------------------------------------------------------------------
	
}
