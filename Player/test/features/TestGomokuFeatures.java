package features;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import features.aspatial.AspatialFeature;
import features.feature_sets.BaseFeatureSet;
import features.feature_sets.LegacyFeatureSet;
import features.feature_sets.NaiveFeatureSet;
import features.feature_sets.network.JITSPatterNetFeatureSet;
import features.feature_sets.network.SPatterNetFeatureSet;
import features.spatial.SpatialFeature;
import game.Game;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.FastArrayList;
import other.GameLoader;
import other.action.move.ActionAdd;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Unit test for features in Gomoku
 *
 * @author Dennis Soemers
 */
public class TestGomokuFeatures
{
	
	@Test
	@SuppressWarnings("static-method")
	public void test()
	{
		// Default board size = 15x15 vertices
		final Game game = GameLoader.loadGameFromName("Gomoku.lud");
		
		// generate handcrafted feature set
		final List<SpatialFeature> features = new ArrayList<SpatialFeature>();
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}]>:comment=\"play in empty position\""));								// 0
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, #{0.0}]>:comment=\"play next to edge of board\""));					// 1
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, f{0.0}]>:comment=\"play next to friendly piece\""));					// 2
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, e{0.0}]>:comment=\"play next to enemy piece\""));					// 3
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, f{0.0, 0.25}]>:comment=\"play diagonally adjacent to friend\""));	// 4
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, #{0.0}, #{0.25}]>:comment=\"play in corner of board\""));			// 5
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[N4{}]>:comment=\"Every to-position has connectivity 4.\""));				// 6
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[N8{}]>:comment=\"No to-position has connectivity 8.\""));					// 7
		
		// Randomly pick one of the feature set implementations to test
		final BaseFeatureSet featureSet;
		final double rand = ThreadLocalRandom.current().nextDouble();
		if (rand < 0.25)
			featureSet = new SPatterNetFeatureSet(new ArrayList<AspatialFeature>(), features);
		else if (rand < 0.5)
			featureSet = JITSPatterNetFeatureSet.construct(new ArrayList<AspatialFeature>(), features);
		else if (rand < 0.75)
			featureSet = new LegacyFeatureSet(new ArrayList<AspatialFeature>(), features);
		else
			featureSet = new NaiveFeatureSet(new ArrayList<AspatialFeature>(), features);
		
		featureSet.init(game, new int[] {1, 2}, null);
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		
		game.start(context);
		
		// In the initial game state:
		// - Feature 0 is active for ALL moves (15 * 15 = 225)
		// - Feature 1 is active for 15 + 15 + 13 + 13 = 56 moves (along edges)
		// - Feature 5 is active for 4 moves (in corners)
		// - Feature 6 is active for ALL moves (15 * 15 = 225)
		final int[] initialStateNumActives = new int[featureSet.spatialFeatures().length];
		final FastArrayList<Move> legalMoves = game.moves(context).moves();
		final TIntArrayList[] initialStateActiveFeatures = featureSet.computeSparseSpatialFeatureVectors(context, legalMoves, false);
		
		for (final TIntArrayList sparse : initialStateActiveFeatures)
		{
			for (int i = 0; i < sparse.size(); ++i)
			{
				initialStateNumActives[sparse.getQuick(i)] += 1;
			}
		}
		
		assertEquals(initialStateNumActives[0], 225);
		assertEquals(initialStateNumActives[1], 56);
		assertEquals(initialStateNumActives[2], 0);
		assertEquals(initialStateNumActives[3], 0);
		assertEquals(initialStateNumActives[4], 0);
		assertEquals(initialStateNumActives[5], 4);
		assertEquals(initialStateNumActives[6], 225);
		assertEquals(initialStateNumActives[7], 0);
		
		// P1 places a piece at vertex 16
		game.apply(context,
				new Move(new ActionAdd(SiteType.Vertex, 16, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED,
						null)
						.withDecision(true)).withFrom(16).withTo(16).withMover(1));
		
		// Suppose that P2 tried placing at vertex 15 next...
		TIntArrayList activeFeatures = 
				featureSet.computeSparseSpatialFeatureVector
				(
					context, 
					new Move
					(
						new ActionAdd
						(
							SiteType.Vertex, 15, 1, 1, Constants.UNDEFINED,
							Constants.UNDEFINED, Constants.UNDEFINED, null
						).withDecision(true)
					).withFrom(15).withTo(15).withMover(2),
					false
				);
		
		// then we should have 4 active features:
		// 0: empty position
		// 1: next to edge
		// 3: next to enemy
		// 6: connectivity = 4
		assert activeFeatures.size() == 4;
		assert activeFeatures.contains(0);
		assert activeFeatures.contains(1);
		assert activeFeatures.contains(3);
		assert activeFeatures.contains(6);
		
		// Suppose that P2 tried placing at vertex 0 next...
		activeFeatures = 
				featureSet.computeSparseSpatialFeatureVector
				(
					context, 
					new Move
					(
						new ActionAdd
						(
							SiteType.Vertex, 0, 2, 1, Constants.UNDEFINED,
							Constants.UNDEFINED, Constants.UNDEFINED, null
						).withDecision(true)
					).withFrom(0).withTo(0).withMover(2),
					false
				);

		// then we should have 4 active features:
		// 0: empty position
		// 1: next to edge
		// 5: in corner
		// 6: connectivity = 4
		assert activeFeatures.size() == 4;
		assert activeFeatures.contains(0);
		assert activeFeatures.contains(1);
		assert activeFeatures.contains(5);
		assert activeFeatures.contains(6);
		
		// Suppose that P2 tried placing at vertex 32 next...
		activeFeatures = 
				featureSet.computeSparseSpatialFeatureVector
				(
					context, 
					new Move
					(
						new ActionAdd
						(
							SiteType.Vertex, 32, 2, 1, Constants.UNDEFINED,
							Constants.UNDEFINED, Constants.UNDEFINED, null
						).withDecision(true)
					).withFrom(32).withTo(32).withMover(2),
					false
				);

		// then we should have 2 active features:
		// 0: empty position
		// 6: connectivity = 4
		assert activeFeatures.size() == 2;
		assert activeFeatures.contains(0);
		assert activeFeatures.contains(6);
		
		// P2 places a piece at vertex 0
		game.apply(context,
				new Move(new ActionAdd(SiteType.Vertex, 0, 2, 1, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED,
						null)
				.withDecision(true)).withFrom(0).withTo(0).withMover(2));
		
		// Suppose that P1 tried placing at vertex 32 next...
		activeFeatures = 
				featureSet.computeSparseSpatialFeatureVector
				(
					context, 
					new Move
					(
						new ActionAdd
						(
							SiteType.Vertex, 32, 2, 1, Constants.UNDEFINED,
							Constants.UNDEFINED, Constants.UNDEFINED, null
						).withDecision(true)
					).withFrom(32).withTo(32).withMover(1),
					false
				);

		// then we should have 3 active features:
		// 0: empty position
		// 4: diagonally adjacent to friend
		// 6: connectivity = 4
		assert activeFeatures.size() == 3;
		assert activeFeatures.contains(0);
		assert activeFeatures.contains(4);
		assert activeFeatures.contains(6);
	}

}
