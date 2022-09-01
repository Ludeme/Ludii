package features;

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
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.GameLoader;
import other.action.move.ActionAdd;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Unit test for testing some handcrafted active features in Hex
 * 
 * @author Dennis Soemers
 */
public class TestHexFeatures
{
	
	@Test
	@SuppressWarnings("static-method")
	public void test()
	{
		final Game game = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		
		// generate handcrafted feature set
		final List<SpatialFeature> features = new ArrayList<SpatialFeature>();
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}]>:comment=\"play in empty position\""));
		features.add((SpatialFeature)Feature.fromString("rel:last_to=<{0.16666667}>:to=<{}>:pat=<refl=true,rots=all,els=[-{}, f{0.0}, f{0.33333334}]>:comment=\"reactive bridge completion\""));
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, f{0.0}, f{0.33333334}]>:comment=\"proactive bridge completion\""));
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, f{0.0}]>:comment=\"play next to friend\""));
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, e{0.0}]>:comment=\"play next to enemy\""));
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, #{0.0}]>:comment=\"play next to off-board\""));
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[N6{}]>:comment=\"To-positions with connectivity 6.\""));
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[N12{}]>:comment=\"No to-position has connectivity 12.\""));
		
		// Randomly pick one of the feature set impelmentations to test
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
		
		// P1 plays above 60
		game.apply(context, new Move(
				new ActionAdd(null, 80, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withTo(80).withFrom(80).withMover(1));
		// P2 plays all the way in west corner (unimportant move)
		game.apply(context, new Move(
				new ActionAdd(null, 55, 2, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withTo(55).withFrom(55).withMover(2));
		// P1 plays lower right of 60
		game.apply(context, new Move(
				new ActionAdd(null, 50, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withTo(50).withFrom(50).withMover(1));
		// P2 plays top right of 60, threatening bridge between 80 and 50
		game.apply(context, new Move(
				new ActionAdd(null, 71, 2, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withTo(71).withFrom(71).withMover(2));
		
		// compute active features for P1 moving to 60 (completing the threatened bridge)
		final TIntArrayList featuresMoveTo60 = 
				featureSet.computeSparseSpatialFeatureVector
				(
					context, 
					new Move
					(
						new ActionAdd
						(
							null, 60, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED,
							Constants.UNDEFINED, null
						).withDecision(true)
					).withTo(60).withFrom(60).withMover(1),
					false
				);
		
		// should have 6 active features:
		// 0: play in empty position
		// 1: reactive bridge completion
		// 2: proactive bridge completion
		// 3: play next to friend
		// 4: play next to enemy
		// 6: connectivity = 6
		assert featuresMoveTo60.size() == 6;
		assert featuresMoveTo60.contains(0);
		assert featuresMoveTo60.contains(1);
		assert featuresMoveTo60.contains(2);
		assert featuresMoveTo60.contains(3);
		assert featuresMoveTo60.contains(4);
		assert featuresMoveTo60.contains(6);
		
		// playing in the following positions should have only 3 active features:
		// 0: play in empty position
		// 3: play next to friend
		// 6: connectivity = 6
		for (final int pos : new int[]{70, 88, 96, 41, 32, 40})
		{
			final TIntArrayList featuresMoveToPos = 
					featureSet.computeSparseSpatialFeatureVector
					(
						context, 
						new Move
						(
							new ActionAdd
							(
								null, pos, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED,
								Constants.UNDEFINED, null
							).withDecision(true)
						).withTo(pos).withFrom(pos).withMover(1),
						false
					);
			
			assert featuresMoveToPos.size() == 3;
			assert featuresMoveToPos.contains(0);
			assert featuresMoveToPos.contains(3);
			assert featuresMoveToPos.contains(6);
		}
		
		// playing in the following positions should have only 4 active features:
		// 0: play in empty position
		// 3: play next to friend
		// 4: play next to enemy
		// 6: connectivity = 6
		for (final int pos : new int[]{89, 61})
		{
			final TIntArrayList featuresMoveToPos = 
					featureSet.computeSparseSpatialFeatureVector
					(
						context, 
						new Move
						(
							new ActionAdd
							(
								null, pos, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED,
								Constants.UNDEFINED, null
							).withDecision(true)
						).withTo(pos).withFrom(pos).withMover(1),
						false
					);

			assert featuresMoveToPos.size() == 4;
			assert featuresMoveToPos.contains(0);
			assert featuresMoveToPos.contains(3);
			assert featuresMoveToPos.contains(4);
			assert featuresMoveToPos.contains(6);
		}
		
		// playing in the following positions should have only 3 active features:
		// 0: play in empty position
		// 4: play next to enemy
		// 6: connectivity = 6
		for (final int pos : new int[]{81})
		{
			final TIntArrayList featuresMoveToPos = 
					featureSet.computeSparseSpatialFeatureVector
					(
						context, 
						new Move
						(
							new ActionAdd
							(
								null, pos, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED,
								Constants.UNDEFINED, null
							).withDecision(true)
						).withTo(pos).withFrom(pos).withMover(1),
						false
					);

			assert featuresMoveToPos.size() == 3;
			assert featuresMoveToPos.contains(0);
			assert featuresMoveToPos.contains(4);
			assert featuresMoveToPos.contains(6);
		}
		
		// playing in the following positions should have 3 active features:
		// 0: play in empty position
		// 5: play in off-board position
		// 6: connectivity = 6
		for 
		(
			final int pos : new int[]
			{
				0, 2, 5, 9, 14, 20, 27, 35, 44, 54,
				1, 3, 6, 10, 15, 21, 28, 36, 
				76, 85, 93, 100, 106, 111, 115, 118, 120,
				75, 84, 92, 99, 105, 110, 114, 117, 119
			}
		)
		{
			final TIntArrayList featuresMoveToPos = 
					featureSet.computeSparseSpatialFeatureVector
					(
						context, 
						new Move
						(
							new ActionAdd
							(
								null, pos, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED,
								Constants.UNDEFINED, null
							).withDecision(true)
						).withTo(pos).withFrom(pos).withMover(1),
						false
					);

			assert featuresMoveToPos.size() == 3;
			assert featuresMoveToPos.contains(0);
			assert featuresMoveToPos.contains(5);
			assert featuresMoveToPos.contains(6);
		}
		
		// playing in the following position should have 3 active features:
		// 0: play in empty position
		// 5: play in off-board position
		// 6: connectivity = 6
		for (final int pos : new int[]{ 65 })
		{
			final TIntArrayList featuresMoveToPos = 
					featureSet.computeSparseSpatialFeatureVector
					(
						context, 
						new Move
						(
							new ActionAdd
							(
								null, pos, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED,
								Constants.UNDEFINED, null
							).withDecision(true)
						).withTo(pos).withFrom(pos).withMover(1),
						false
					);

			assert featuresMoveToPos.size() == 3;
			assert featuresMoveToPos.contains(0);
			assert featuresMoveToPos.contains(5);
			assert featuresMoveToPos.contains(6);
		}
		
		// playing in the following positions should have 4 active features:
		// 0: play in empty position
		// 4: play next to enemy
		// 5: play in off-board position
		// 6: connectivity = 6
		for (final int pos : new int[]{45, 66})
		{
			final TIntArrayList featuresMoveToPos = 
					featureSet.computeSparseSpatialFeatureVector
					(
						context, 
						new Move
						(
							new ActionAdd
							(
								null, pos, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED,
								Constants.UNDEFINED, null
							).withDecision(true)
						).withTo(pos).withFrom(pos).withMover(1),
						false
					);

			assert featuresMoveToPos.size() == 4;
			assert featuresMoveToPos.contains(0);
			assert featuresMoveToPos.contains(4);
			assert featuresMoveToPos.contains(5);
			assert featuresMoveToPos.contains(6);
		}
		
		// the following positions should be occupied and not have any active features (except for connectivity 6)
		for (final int pos : new int[]{50, 71, 80})
		{
			final TIntArrayList featuresMoveToPos = 
					featureSet.computeSparseSpatialFeatureVector
					(
						context, 
						new Move
						(
							new ActionAdd
							(
								null, pos, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED,
								Constants.UNDEFINED, null
							).withDecision(true)
						).withTo(pos).withFrom(pos).withMover(1),
						false
					);

			assert featuresMoveToPos.size() == 1;
			assert featuresMoveToPos.contains(6);
		}
		
		// the following position should be occupied and not have any active features (except for connectivity 6)
		for (final int pos : new int[]{55})
		{
			final TIntArrayList featuresMoveToPos = 
					featureSet.computeSparseSpatialFeatureVector
					(
						context, 
						new Move
						(
							new ActionAdd
							(
								null, pos, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED,
								Constants.UNDEFINED, null
							).withDecision(true)
						).withTo(pos).withFrom(pos).withMover(1),
						false
					);

			assert featuresMoveToPos.size() == 1;
			assert featuresMoveToPos.contains(6);
		}
	}

}
