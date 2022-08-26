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
import main.collections.FastArrayList;
import other.GameLoader;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Unit test for features in Kharbaga
 *
 * @author Dennis Soemers
 */
public class TestKharbagaFeatures
{
	
	@Test
	@SuppressWarnings("static-method")
	public void test()
	{
		final Game game = GameLoader.loadGameFromName("Kharbaga.lud");
		
		// generate handcrafted feature set
		final List<SpatialFeature> features = new ArrayList<SpatialFeature>();
		features.add((SpatialFeature)Feature.fromString(
				"rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, N8{}]>:comment=\"move to empty N8 position\""));							// 0
		features.add((SpatialFeature)Feature.fromString(
				"rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, N8{}, #{0.0}]>:comment=\"move to N8 pos at to edge of board\""));		// 1
		features.add((SpatialFeature)Feature.fromString(
				"rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, N8{}, f{0.0}]>:comment=\"move to N8 pos next to friendly piece\""));		// 2
		features.add((SpatialFeature)Feature.fromString(
				"rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, N8{}, e{0.0}]>:comment=\"move to N8 pos next to enemy piece\""));		// 3
		features.add((SpatialFeature)Feature.fromString(
				"rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, #{0.0}, #{0.25}]>:comment=\"move to corner of board\""));				// 4
		
		features.add
		(
			(SpatialFeature)Feature.fromString																											// 5
			(
				"rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, N8{}, f{0.0}, f{1/8}, e{2/8}, e{3/8}, e{4/8}, e{5/8}, f{6/8}, f{7/8}]>"
				+ ":comment=\"Move to 8-connected vertex with 4 adjacent friends and 4 adjacent enemies (e.g. opening move)\""
			)
		);
		
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
		// - Feature 0, 2, 3, and 5 should all be active for ALL moves (= 3)
		// - Features 1 and 4 should not be active for ANY move
		final int[] initialStateNumActives = new int[featureSet.spatialFeatures().length];
		final FastArrayList<Move> legalMoves = game.moves(context).moves();
		final TIntArrayList[] initialStateActiveFeatures = featureSet.computeSparseSpatialFeatureVectors(context, legalMoves, false);
		
		//System.out.println("legal moves = " + legalMoves);
		
		for (final TIntArrayList sparse : initialStateActiveFeatures)
		{
			//System.out.println("sparse active features = " + sparse);
			for (int i = 0; i < sparse.size(); ++i)
			{
				initialStateNumActives[sparse.getQuick(i)] += 1;
			}
		}
		
		assert initialStateNumActives[0] == 3;
		assert initialStateNumActives[1] == 0;
		assert initialStateNumActives[2] == 3;
		assert initialStateNumActives[3] == 3;
		assert initialStateNumActives[4] == 0;
		assert initialStateNumActives[5] == 3;
	}

}
