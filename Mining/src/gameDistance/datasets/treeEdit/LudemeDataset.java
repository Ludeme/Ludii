package gameDistance.datasets.treeEdit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import game.Game;
import gameDistance.datasets.Dataset;
import ludemeplexDetection.GetLudemeInfo;
import main.grammar.Call;
import main.grammar.LudemeInfo;
import utils.data_structures.support.zhang_shasha.Tree;

/**
 * Dataset containing ludemes used for each game's description.
 * - BagOfWords
 * - Sequence
 * - Tree
 * 
 * @author matthew.stephenson
 */
public class LudemeDataset implements Dataset
{

	//-------------------------------------------------------------------------
	
	@Override
	public Map<String, Double> getBagOfWords(final Game game) 
	{
		final List<LudemeInfo> allLudemes = GetLudemeInfo.getLudemeInfo();
		
		// Initialise all values to zero
		final Map<String, Double> featureMap = new HashMap<>();
		for (final LudemeInfo ludeme :allLudemes)
			featureMap.put(ludeme.symbol().name(), Double.valueOf(0.0));
		
		final Call callTree = game.description().callTree();
		final Set<LudemeInfo> gameLudemes = callTree.analysisFormat(0, allLudemes).keySet();

		for (final LudemeInfo ludeme : gameLudemes)
			featureMap.put(ludeme.symbol().name(), Double.valueOf(featureMap.get(ludeme.symbol().name()).doubleValue()+1.0));
			
		return featureMap;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public List<String> getSequence(final Game game) 
	{
		final List<LudemeInfo> allLudemes = GetLudemeInfo.getLudemeInfo();
		final Call callTree = game.description().callTree();
		final Set<LudemeInfo> gameLudemes = callTree.analysisFormat(0, allLudemes).keySet();
		
		final List<String> ludemeSequence = new ArrayList<>();
		for (final LudemeInfo ludeme : gameLudemes)
			ludemeSequence.add(ludeme.symbol().name());
		
		return ludemeSequence;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public Tree getTree(final Game game) 
	{
		final List<LudemeInfo> allLudemes = GetLudemeInfo.getLudemeInfo();
		final Call callTree = game.description().callTree();
		final String gameLudemes = callTree.preorderFormat(0, allLudemes);
		final Tree ludemeTree = new Tree(gameLudemes);
		return ludemeTree;
	}
	
	//-------------------------------------------------------------------------
	
}
