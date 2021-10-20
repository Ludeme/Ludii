package gameDistance.datasets.sequence;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import gameDistance.datasets.DatasetUtils;
import other.context.Context;
import other.trial.Trial;
import utils.data_structures.support.zhang_shasha.Tree;

public class MoveConceptDataset implements Dataset
{

	//-------------------------------------------------------------------------
	
	@Override
	public Map<String, Double> getBagOfWords(final Game game) 
	{
		final Map<String, Double> featureMap = new HashMap<>();
		final List<Trial> gameTrials = DatasetUtils.getSavedTrials(game);
		
		for (int i = 0; i < gameTrials.size(); ++i)
		{
			final Trial trial = gameTrials.get(i);
			final Trial newTrial = new Trial(game);
			final Context context = new Context(game, newTrial);
			game.start(context);
			
			final ArrayList<BitSet> moveConceptSet = new ArrayList<>();
			for(int o = newTrial.numInitialPlacementMoves(); o < trial.numMoves(); o++) 
			{
				moveConceptSet.add(trial.getMove(o).moveConcepts(context));
				game.apply(context, trial.getMove(o));
			}
					
			for (int j = 0; j < moveConceptSet.size(); j++)
			{
				if (featureMap.containsKey(moveConceptSet.get(j).toString()))
					featureMap.put(moveConceptSet.get(j).toString(), Double.valueOf(featureMap.get(moveConceptSet.get(j).toString()).doubleValue()+1.0));
				else
					featureMap.put(moveConceptSet.get(j).toString(), Double.valueOf(1.0));
			}
		}
		
		return featureMap;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public List<String> getSequence(final Game game) 
	{
		final List<String> moveConceptSequence = new ArrayList<>();
		final List<Trial> gameTrials = DatasetUtils.getSavedTrials(game);
		
		// For now, just take the first trial.
		for (int i = 0; i < 1; ++i)
		{
			final Trial trial = gameTrials.get(i);
			final Trial newTrial = new Trial(game);
			final Context context = new Context(game, newTrial);
			game.start(context);
			
			final ArrayList<BitSet> moveConceptSet = new ArrayList<>();
			for(int o = newTrial.numInitialPlacementMoves(); o < trial.numMoves(); o++) 
			{
				moveConceptSet.add(trial.getMove(o).moveConcepts(context));
				game.apply(context, trial.getMove(o));
			}
					
			for (int j = 0; j < moveConceptSet.size(); j++)
				moveConceptSequence.add(moveConceptSet.get(j).toString());
		}
		
		return moveConceptSequence;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Not Supported
	 */
	@Override
	public Tree getTree(final Game game) 
	{
		return null;
	}
	
	//-------------------------------------------------------------------------

}
