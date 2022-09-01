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
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.GameLoader;
import other.action.move.ActionAdd;
import other.action.move.move.ActionMove;
import other.action.state.ActionSetNextPlayer;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Unit tests for features in Amazons
 *
 * @author Dennis Soemers
 */
@SuppressWarnings("static-method")
public class TestAmazonsFeatures
{
	
	@Test
	public void test()
	{
		final Game game = GameLoader.loadGameFromName("Amazons.lud");
		
		// generate handcrafted feature set
		final List<SpatialFeature> features = new ArrayList<SpatialFeature>();
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}]>:comment=\"move/shoot to empty position\""));						// 0
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, f{0.0}]>:comment=\"move/shoot next to friend\""));					// 1
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, e{0.0}]>:comment=\"move/shoot next to enemy\""));					// 2
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, I3{0.0}]>:comment=\"move/shoot next to arrow\""));					// 3
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, #{0.0}]>:comment=\"move/shoot next to edge\""));						// 4
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, #{0.0}, #{0.25}]>:comment=\"move/shoot into corner\""));				// 5
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[-{}, !-{0.0}]>:comment=\"move/shoot next to a non-empty position\""));	// 6
		
		features.add((SpatialFeature)Feature.fromString("rel:from=<{}>:pat=<refl=true,rots=all,els=[-{}]>:comment=\"move/shoot from empty position (impossible!)\""));		// 7
		features.add((SpatialFeature)Feature.fromString("rel:from=<{}>:pat=<refl=true,rots=all,els=[f{0.0}]>:comment=\"move from position next to friend\""));				// 8
		features.add((SpatialFeature)Feature.fromString("rel:from=<{}>:pat=<refl=true,rots=all,els=[e{0.0}]>:comment=\"move from position next to enemy\""));				// 9
		features.add((SpatialFeature)Feature.fromString("rel:from=<{}>:pat=<refl=true,rots=all,els=[I3{0.0}]>:comment=\"move from position next to arrow\""));				// 10
		features.add((SpatialFeature)Feature.fromString("rel:from=<{}>:pat=<refl=true,rots=all,els=[#{0.0}]>:comment=\"move from position next to edge\""));				// 11
		features.add((SpatialFeature)Feature.fromString("rel:from=<{}>:pat=<refl=true,rots=all,els=[#{0.0}, #{0.25}]>:comment=\"move/shoot from corner\""));				// 12
		
		features.add																																						// 13
		(
			(SpatialFeature)Feature.fromString
			(
				"rel:last_to=<{0.0}>:from=<{}>:pat=<refl=true,rots=all,els=[f{}, I3{0.0}]>:"
				+ "comment=\"move from position next to where opponent shot in last move\""
			)
		);
		
		features.add																																						// 14
		(
			(SpatialFeature)Feature.fromString
			(
				"rel:last_to=<{0.0}>:to=<{}>:pat=<refl=true,rots=all,els=[-{}, I3{0.0}]>:"
				+ "comment=\"move/shoot to position next to where opponent shot in last move\""
			)
		);
		
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[N4{}]>:comment=\"Every to-position has connectivity 4.\""));				// 15
		features.add((SpatialFeature)Feature.fromString("rel:to=<{}>:pat=<refl=true,rots=all,els=[N8{}]>:comment=\"No to-position has connectivity 8.\""));					// 16
		
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
		
		// P1 moves from 39 to 9
		game.apply(context,
				new Move(
						ActionMove.construct(SiteType.Cell, 39, Constants.UNDEFINED, SiteType.Cell, 9, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF, false)
						.withDecision(true),
						new ActionSetNextPlayer(1)).withFrom(39).withTo(9));
		// P1 shoots at 59
		game.apply(context, new Move(
				new ActionAdd(null, 59, 3, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withFrom(59).withTo(59));
		
		// P2 moves from 69 to 36
		game.apply(context,
				new Move(
						ActionMove.construct(SiteType.Cell, 69, Constants.UNDEFINED, SiteType.Cell, 36, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF, false)
						.withDecision(true),
						new ActionSetNextPlayer(2)).withFrom(69).withTo(36));
		// P2 shoots at 16
		game.apply(context, new Move(
				new ActionAdd(null, 16, 3, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withFrom(16).withTo(16));
		// P1 moves from 30 to 35
		game.apply(context,
				new Move(
						ActionMove.construct(SiteType.Cell, 30, Constants.UNDEFINED, SiteType.Cell, 35, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF, false)
						.withDecision(true),
						new ActionSetNextPlayer(1)).withFrom(30).withTo(35));
		// P1 shoots at 95
		game.apply(context, new Move(
				new ActionAdd(null, 95, 3, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withFrom(95).withTo(95));
		// P2 moves from 93 to 90
		game.apply(context,
				new Move(
						ActionMove.construct(SiteType.Cell, 93, Constants.UNDEFINED, SiteType.Cell, 90, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF, false)
						.withDecision(true),
						new ActionSetNextPlayer(2)).withFrom(93).withTo(90));
		// P2 shoots at 45
		game.apply(context, new Move(
				new ActionAdd(null, 45, 3, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, 
						null)
						.withDecision(true))
						.withFrom(45).withTo(45));
		
		// suppose that we tried moving from 3 to 0...
		TIntArrayList activeFeatures = 
				featureSet.computeSparseSpatialFeatureVector
				(
					context, 
					new Move
					(
						ActionMove.construct
						(
							SiteType.Cell, 3, Constants.UNDEFINED, SiteType.Cell, 0,
							Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF,
							false
						).withDecision(true),
						new ActionSetNextPlayer(1)
					).withFrom(3).withTo(0).withMover(1),
					false
				);
		
		// then we should have 6 active features:
		// 0: move to empty position
		// 4: move next to edge
		// 5: move into corner
		// 6: move next to a non-empty position
		// 11: move from position next to edge
		// 15: connectivity 4
		//System.out.println(activeFeatures);
		assertEquals(activeFeatures.size(), 6);
		assert activeFeatures.contains(0);
		assert activeFeatures.contains(4);
		assert activeFeatures.contains(5);
		assert activeFeatures.contains(6);
		assert activeFeatures.contains(11);
		assert activeFeatures.contains(15);
		
		// suppose that we tried moving from 3 to 30...
		activeFeatures = 
				featureSet.computeSparseSpatialFeatureVector
				(
					context, 
					new Move
					(
							ActionMove.construct
						(
							SiteType.Cell, 3, Constants.UNDEFINED, SiteType.Cell, 30,
							Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF,
							false
						).withDecision(true),
						new ActionSetNextPlayer(1)).withFrom(3).withTo(30).withMover(1),
					false
				);
		
		// then we should have 5 active features:
		// 0: move to empty position
		// 4: move next to edge
		// 6: move next to a non-empty position
		// 11: move from position next to edge
		// 15: connectivity 4
		assert activeFeatures.size() == 5;
		assert activeFeatures.contains(0);
		assert activeFeatures.contains(4);
		assert activeFeatures.contains(6);
		assert activeFeatures.contains(11);
		assert activeFeatures.contains(15);
		
		// suppose that we tried moving from 35 to 32...
		activeFeatures = 
				featureSet.computeSparseSpatialFeatureVector
				(
					context, 
					new Move
					(
							ActionMove.construct
						(
							SiteType.Cell, 35, Constants.UNDEFINED, SiteType.Cell, 32,
							Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF,
							false
						).withDecision(true),
						new ActionSetNextPlayer(1)
					).withFrom(35).withTo(32).withMover(1),
					false
				);
		
		// then we should have 5 active features:
		// 0: move to empty position
		// 9: move from position next to enemy
		// 10: move from position next to arrow
		// 13: move from position next to where opponent just shot
		// 15: connectivity 4
		assertEquals(activeFeatures.size(), 5);
		assert activeFeatures.contains(0);
		assert activeFeatures.contains(9);
		assert activeFeatures.contains(10);
		assert activeFeatures.contains(13);
		assert activeFeatures.contains(15);
		
		// suppose that we tried moving from 35 to 44...
		activeFeatures = 
				featureSet.computeSparseSpatialFeatureVector
				(
					context, 
					new Move
					(
							ActionMove.construct
						(
							SiteType.Cell, 35, Constants.UNDEFINED, SiteType.Cell, 44,
							Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF,
							false
						).withDecision(true),
						new ActionSetNextPlayer(1)
					).withFrom(35).withTo(44).withMover(1),
					false
				);
		
		// then we should have 8 active features:
		// 0: move to empty position
		// 3: move next to arrow
		// 6: move next to a non-empty position
		// 9: move from position next to enemy
		// 10: move from position next to arrow
		// 13: move from position next to where opponent just shot
		// 14: move to position next to where opponent just shot
		// 15: connectivity 4
		assertEquals(activeFeatures.size(), 8);
		assert activeFeatures.contains(0);
		assert activeFeatures.contains(3);
		assert activeFeatures.contains(6);
		assert activeFeatures.contains(9);
		assert activeFeatures.contains(10);
		assert activeFeatures.contains(13);
		assert activeFeatures.contains(14);
		assert activeFeatures.contains(15);
		
		// suppose that we tried moving from 35 to 46...
		activeFeatures = 
				featureSet.computeSparseSpatialFeatureVector
				(
					context, 
					new Move
					(
							ActionMove.construct
						(
							SiteType.Cell, 35, Constants.UNDEFINED, SiteType.Cell, 46,
							Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF,
							false
						).withDecision(true),
						new ActionSetNextPlayer(1)).withFrom(35).withTo(46).withMover(1),
					false
				);
		
		// then we should have 9 active features:
		// 0: move to empty position
		// 2: move next to enemy
		// 3: move next to arrow
		// 6: move next to a non-empty position
		// 9: move from position next to enemy
		// 10: move from position next to arrow
		// 13: move from position next to where opponent just shot
		// 14: move to position next to where opponent just shot
		// 15: connectivity 4
		assertEquals(activeFeatures.size(), 9);
		assert activeFeatures.contains(0);
		assert activeFeatures.contains(2);
		assert activeFeatures.contains(3);
		assert activeFeatures.contains(6);
		assert activeFeatures.contains(9);
		assert activeFeatures.contains(10);
		assert activeFeatures.contains(13);
		assert activeFeatures.contains(14);
		assert activeFeatures.contains(15);
		
		// suppose that we tried moving from 9 to 27...
		activeFeatures = 
				featureSet.computeSparseSpatialFeatureVector
				(
					context, 
					new Move
					(
							ActionMove.construct
						(
							SiteType.Cell, 9, Constants.UNDEFINED, SiteType.Cell, 27,
							Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF,
							false
						).withDecision(true),
						new ActionSetNextPlayer(1)).withFrom(9).withTo(27).withMover(1),
					false
				);
		
		// then we should have 4 active features:
		// 0: move to empty position
		// 11: move from position next to edge
		// 12: move from corner
		// 15: connectivity 4
		assertEquals(activeFeatures.size(), 4);
		assert activeFeatures.contains(0);
		assert activeFeatures.contains(11);
		assert activeFeatures.contains(12);
		assert activeFeatures.contains(15);
	}
	
	@Test
	public void testB()
	{
		final Game game = GameLoader.loadGameFromName("Amazons.lud");

		// generate handcrafted feature set (just a single feature)
		final List<SpatialFeature> features = new ArrayList<SpatialFeature>();
		features.add((SpatialFeature)Feature.fromString("rel:from=<{0.0}>:to=<{}>:pat=<refl=true,rots=all,els=[!-{0.25,0.0,0.0,0.0}, #{0.0,0.5,0.0,0.0,0.0}]>"));
		
		final BaseFeatureSet featureSet = new SPatterNetFeatureSet(new ArrayList<AspatialFeature>(), features);
		featureSet.init(game, new int[] {1, 2}, null);
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		
		game.start(context);
		
		// P1 moves from 39 to 37
		game.apply(context,
				new Move(
						ActionMove.construct(SiteType.Cell, 39, Constants.UNDEFINED, SiteType.Cell, 37, Constants.UNDEFINED,
								Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF, false)
						.withDecision(true),
						new ActionSetNextPlayer(1)).withFrom(39).withTo(37).withMover(1));
		// P1 shoots at 97
		game.apply(context, new Move(
				new ActionAdd(null, 97, 3, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withFrom(97).withTo(97).withMover(1));
		
		// P2 moves from 96 to 36
		game.apply(context,
				new Move(
						ActionMove.construct(SiteType.Cell, 96, Constants.UNDEFINED, SiteType.Cell, 36, Constants.UNDEFINED,
								Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF, false)
						.withDecision(true),
						new ActionSetNextPlayer(2)).withFrom(96).withTo(36).withMover(2));
		// P2 shoots at 35
		game.apply(context, new Move(
				new ActionAdd(null, 35, 3, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withFrom(35).withTo(35).withMover(2));
		
		// P1 moves from 30 to 32
		game.apply(context,
				new Move(
						ActionMove.construct(SiteType.Cell, 30, Constants.UNDEFINED, SiteType.Cell, 32, Constants.UNDEFINED,
								Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF, false)
						.withDecision(true),
						new ActionSetNextPlayer(1)).withFrom(30).withTo(32).withMover(1));
		// P1 shoots at 34
		game.apply(context, new Move(
				new ActionAdd(null, 34, 3, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						null)
						.withDecision(true))
						.withFrom(34).withTo(34).withMover(1));
		
		// P2 moves from 93 to 83
		game.apply(context,
				new Move(
						ActionMove.construct(SiteType.Cell, 93, Constants.UNDEFINED, SiteType.Cell, 83, Constants.UNDEFINED,
								Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF,
						false)
						.withDecision(true),
						new ActionSetNextPlayer(2)).withFrom(93).withTo(83).withMover(2));
		// P2 shoots at 89
		game.apply(context, new Move(
				new ActionAdd(null, 89, 3, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, 
						null)
						.withDecision(true))
						.withFrom(89).withTo(89).withMover(2));
		
		// go through all legal moves
		for (final Move move : game.moves(context).moves())
		{
			final int lastFrom = FeatureUtils.fromPos(trial.lastMove());
			final int lastTo = FeatureUtils.toPos(trial.lastMove());
			final int from = FeatureUtils.fromPos(move);
			final int to = FeatureUtils.toPos(move);
			final List<FeatureInstance> activeInstances = 
					featureSet.getActiveSpatialFeatureInstances(context.state(), lastFrom, lastTo, from, to, 1);
			
			if (from == 3 && to == 2)
			{
				assert (activeInstances.size() == 1);
			}
			else if (from == 6 && to == 7)
			{
				assert (activeInstances.size() == 1);
			}
			else if (from == 37 && to == 38)
			{
				assert (activeInstances.size() == 1);
			}
			else if (from == 37 && to == 27)
			{
				assert (activeInstances.size() == 1);
			}
			else if (from == 32 && to == 22)
			{
				assert (activeInstances.size() == 1);
			}
			else if (from == 32 && to == 31)
			{
				assert (activeInstances.size() == 1);
			}
			else
			{
				if (!activeInstances.isEmpty())
				{
					System.out.println("Active instances for " + move + " = " + activeInstances);
					
					for (final FeatureInstance instance : activeInstances)
					{
						System.out.println("instance = " + instance);
					}
				}
				
				assert (activeInstances.isEmpty());
			}
		}
	}

}
