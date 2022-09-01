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
import features.spatial.FeatureUtils;
import features.spatial.SpatialFeature;
import features.spatial.instances.FeatureInstance;
import game.Game;
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
public class TestYavalathFeatures
{
	
	@SuppressWarnings("static-method")
	@Test
	public void test()
	{
		final Game game = GameLoader.loadGameFromName("/Yavalath.lud");
		
		// generate handcrafted feature set
		final List<SpatialFeature> features = new ArrayList<SpatialFeature>();
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<els=[f{0,1/6}]>"));
		
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
		
		// P1 plays in 60 (irrelevant)
		game.apply(context, new Move(
				new ActionAdd(null, 60, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withTo(60).withFrom(60).withMover(1));
		// P2 plays in 11
		game.apply(context, new Move(
				new ActionAdd(null, 11, 2, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withTo(11).withFrom(11).withMover(2));
		// P1 plays in 56 (irrelevant)
		game.apply(context, new Move(
				new ActionAdd(null, 56, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withTo(56).withFrom(56).withMover(1));
		// P2 plays in 4
		game.apply(context, new Move(
				new ActionAdd(null, 4, 2, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withTo(4).withFrom(4).withMover(2));
		// P1 plays in 58 (irrelevant)
		game.apply(context, new Move(
				new ActionAdd(null, 58, 1, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withTo(58).withFrom(58).withMover(1));
		
		// Compute feature instances for P2 moving to 27
		final Move move27 = 
					new Move
					(
						new ActionAdd
						(
							null, 27, 2, 1, Constants.UNDEFINED, Constants.UNDEFINED,
							Constants.UNDEFINED, null
						).withDecision(true)
					).withTo(27).withFrom(27).withMover(2);
		
		final List<FeatureInstance> instances27 = featureSet.getActiveSpatialFeatureInstances
		(
			context.state(), 
			FeatureUtils.fromPos(context.trial().lastMove()), 
			FeatureUtils.toPos(context.trial().lastMove()), 
			FeatureUtils.fromPos(move27), 
			FeatureUtils.toPos(move27),
			2
		);
		
		assertEquals(2, instances27.size());
		
		// Compute feature instances for P2 moving to 16
		final Move move16 = 
				new Move
				(
					new ActionAdd
					(
						null, 16, 2, 1, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED, null
					).withDecision(true)
				).withTo(16).withFrom(16).withMover(2);

		final List<FeatureInstance> instances16 = featureSet.getActiveSpatialFeatureInstances
				(
					context.state(), 
					FeatureUtils.fromPos(context.trial().lastMove()), 
					FeatureUtils.toPos(context.trial().lastMove()), 
					FeatureUtils.fromPos(move16), 
					FeatureUtils.toPos(move16),
					2
				);

		assertEquals(2, instances16.size());
	}

}
