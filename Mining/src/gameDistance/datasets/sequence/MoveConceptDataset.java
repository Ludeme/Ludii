package gameDistance.datasets.sequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.Game;
import gameDistance.datasets.Dataset;
import gameDistance.datasets.DatasetUtils;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;
import utils.data_structures.support.zhang_shasha.Tree;

/**
 * Dataset containing move concepts from trials.
 * - BagOfWords
 * - Sequence
 * 
 * @author matthew.stephenson
 */
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
			
			final TIntArrayList gameMoveConceptSet = new TIntArrayList();
			
			for(int j = 0; j < Concept.values().length; j++)
				gameMoveConceptSet.add(0);
			
			for(int o = newTrial.numInitialPlacementMoves(); o < trial.numMoves(); o++) 
			{
				final TIntArrayList moveConceptSet = trial.getMove(o).moveConceptsValue(context);
				for(int j = 0; j < Concept.values().length; j++)
					gameMoveConceptSet.set(j, gameMoveConceptSet.get(j) + moveConceptSet.get(j));
				
				game.apply(context, trial.getMove(o));
			}
			
			for(int j = 0; j < gameMoveConceptSet.size(); j++)
				featureMap.put(Concept.values()[j].name(), Double.valueOf(gameMoveConceptSet.get(j)));
		}
		
		return featureMap;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public List<String> getSequence(final Game game) 
	{
		final List<String> moveConceptSequence = new ArrayList<>();
		final List<Trial> gameTrials = DatasetUtils.getSavedTrials(game);
		
		// For now, just take the first trial. gameTrials.size()
		for (int i = 0; i < 1; ++i)
		{
			final Trial trial = gameTrials.get(i);
			final Trial newTrial = new Trial(game);
			final Context context = new Context(game, newTrial);
			game.start(context);
			
			final ArrayList<TIntArrayList> moveConceptSet = new ArrayList<>();
			for(int o = newTrial.numInitialPlacementMoves(); o < trial.numMoves(); o++) 
			{
				moveConceptSet.add(trial.getMove(o).moveConceptsValue(context));
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
