package gameDistance.metrics.treeEdit;

import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import gameDistance.metrics.DistanceMetric;
import gameDistance.utils.apted.costmodel.StringUnitCostModel;
import gameDistance.utils.apted.distance.APTED;
import gameDistance.utils.apted.node.Node;
import gameDistance.utils.apted.node.StringNodeData;
import gameDistance.utils.apted.parser.BracketStringInputParser;
import utils.data_structures.support.zhang_shasha.Tree;

/**
 * Returns Apted tree edit distance.
 * https://github.com/DatabaseGroup/apted
 * http://tree-edit-distance.dbresearch.uni-salzburg.at
 * 
 * M. Pawlik and N. Augsten. Tree edit distance: Robust and memory- efficient. Information Systems 56. 2016.
 * M. Pawlik and N. Augsten. Efficient Computation of the Tree Edit Distance. ACM Transactions on Database Systems (TODS) 40(1). 2015.
 * 
 * @author matthew.stephenson
 */
public class Apted implements DistanceMetric
{
	
	//---------------------------------------------------------------------
	
	@Override
	public double distance(final Dataset dataset, final Map<String, Double> vocabulary, final Game gameA, final Game gameB)
	{
		final Tree treeA = dataset.getTree(gameA);
		final Tree treeB = dataset.getTree(gameB);
		
		final String treeABracketNotation = treeA.bracketNotation();
		final String treeBBracketNotation = treeB.bracketNotation();

		final BracketStringInputParser parser = new BracketStringInputParser();
	    final Node<StringNodeData> t1 = parser.fromString(treeABracketNotation);
	    final Node<StringNodeData> t2 = parser.fromString(treeBBracketNotation);
	    
	    final APTED<StringUnitCostModel, StringNodeData> apted = new APTED<>(new StringUnitCostModel());
	    apted.computeEditDistance(t1, t2);
	    final List<int[]> mapping = apted.computeEditMapping();

		final int maxTreeSize = Math.max(treeA.size(), treeB.size()); 
		
		return (double) apted.mappingCost(mapping) / maxTreeSize;
	}
	
	//---------------------------------------------------------------------
	
}
