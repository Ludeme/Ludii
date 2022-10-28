package features;

import static org.junit.Assert.assertEquals;

import java.util.BitSet;
import java.util.List;

import org.junit.Test;

import features.feature_sets.network.JITSPatterNetFeatureSet;
import features.feature_sets.network.SPatterNetFeatureSet;
import features.generation.AtomicFeatureGenerator;
import features.spatial.FeatureUtils;
import features.spatial.instances.FeatureInstance;
import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FastArrayList;
import other.GameLoader;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Tests the SPatterNetFeatureSet with atomic features by running random trials and ensuring
 * that active feature instances and active features always match.
 *
 * @author Dennis Soemers
 */
public class TestSPatterNetFeatureSet
{
	
	private static final String[] GAMES = 
		{
			"Chess.lud",				// Many piece types
			"Amazons.lud",				// Neutral piece
			"Feed the Ducks.lud",		// Hex cells, also a neutral piece (I think?)
			"Kensington.lud",			// Weird board
			"Xiangqi.lud",				// Like chess, but on vertices
			"Hex.lud"					// Always a nice game
		};
	
	private static final int MAX_MOVES_PER_TRIAL = 200;
	
	@Test
	@SuppressWarnings("static-method")
	public void test()
	{
		for (final String gameName : GAMES)
		{
			System.out.println("Testing game: " + gameName + "...");
			final Game game = GameLoader.loadGameFromName(gameName);
			final AtomicFeatureGenerator featureGenerator = new AtomicFeatureGenerator(game, 2, 4);
			final SPatterNetFeatureSet featureSet = 
					new SPatterNetFeatureSet(featureGenerator.getAspatialFeatures(), featureGenerator.getSpatialFeatures());
			final JITSPatterNetFeatureSet jitFeatureSet = 
					JITSPatterNetFeatureSet.construct(featureGenerator.getAspatialFeatures(), featureGenerator.getSpatialFeatures());
			
			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);
			game.start(context);
			
			final int[] playersArray = new int[game.players().count()];
			for (int i = 0; i < playersArray.length; ++i)
			{
				playersArray[i] = i + 1;
			}
			featureSet.init(game, playersArray, null);
			jitFeatureSet.init(game, playersArray, null);
			
			int numMovesPlayed = 0;
			while (!trial.over() && numMovesPlayed < MAX_MOVES_PER_TRIAL)
			{
				++numMovesPlayed;
				final FastArrayList<Move> legalMoves = game.moves(context).moves();
				
				final int lastFrom = FeatureUtils.fromPos(trial.lastMove());
				final int lastTo = FeatureUtils.toPos(trial.lastMove());
				
				for (final Move move : legalMoves)
				{
					final int from = FeatureUtils.fromPos(move);
					final int to = FeatureUtils.toPos(move);
					
					final List<FeatureInstance> activeInstances =
							featureSet.getActiveSpatialFeatureInstances(context.state(), lastFrom, lastTo, from, to, move.mover());
					final TIntArrayList activeFeatureIndices = 
							featureSet.getActiveSpatialFeatureIndices
							(
								context.state(), lastFrom, lastTo, from, to, move.mover(), 
								Math.random() < 0.5 ? true : false
							);
					
					final List<FeatureInstance> jitActiveInstances =
							jitFeatureSet.getActiveSpatialFeatureInstances(context.state(), lastFrom, lastTo, from, to, move.mover());
					final TIntArrayList jitActiveFeatureIndices = 
							jitFeatureSet.getActiveSpatialFeatureIndices
							(
								context.state(), lastFrom, lastTo, from, to, move.mover(), 
								Math.random() < 0.5 ? true : false
							);
					
					// Convert instances to bitset representation, retaining only features (not caring about instances anymore)
					final BitSet instancesBitSet = new BitSet();
					for (final FeatureInstance instance : activeInstances)
					{
						instancesBitSet.set(instance.feature().spatialFeatureSetIndex());
					}
					final BitSet jitInstancesBitSet = new BitSet();
					for (final FeatureInstance instance : jitActiveInstances)
					{
						jitInstancesBitSet.set(instance.feature().spatialFeatureSetIndex());
					}
					
					// Same with the feature indices list
					final BitSet featuresBitSet = new BitSet();
					for (int i = 0; i < activeFeatureIndices.size(); ++i)
					{
						featuresBitSet.set(activeFeatureIndices.getQuick(i));
					}
					final BitSet jitFeaturesBitSet = new BitSet();
					for (int i = 0; i < jitActiveFeatureIndices.size(); ++i)
					{
						jitFeaturesBitSet.set(jitActiveFeatureIndices.getQuick(i));
					}
					
					// The two bitsets must be equal
					assertEquals(instancesBitSet, featuresBitSet);
					assertEquals(jitInstancesBitSet, jitFeaturesBitSet);
					
					// And also equal between JIT and non-JIT
					assertEquals(instancesBitSet, jitInstancesBitSet);
				}
					
				// Randomly move on to next game state
				context.model().startNewStep(context, null, 0.0);
			}
		}
	}

}
