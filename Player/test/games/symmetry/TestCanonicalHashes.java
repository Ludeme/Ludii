package games.symmetry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import game.Game;
import main.Constants;
import other.GameLoader;
import other.action.move.ActionAdd;
import other.context.Context;
import other.move.Move;
import other.state.symmetry.AcceptNone;
import other.state.symmetry.ReflectionsOnly;
import other.state.symmetry.RotationsOnly;
import other.state.symmetry.SubstitutionsOnly;
import other.topology.Cell;
import other.trial.Trial;

public class TestCanonicalHashes
{
	/**
	 * Plays different 2 hex moves, verifies canonical hash is the same
	 */
	@Test
	public static void testPlayerEquivalence()
	{
		final Game game1 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial1 = new Trial(game1);
		final Context context1 = new Context(game1, trial1);
		game1.start(context1);

		final Game game2 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial2 = new Trial(game2);
		final Context context2 = new Context(game2, trial2);
		game2.start(context2);

		// Game 1 has a white piece at cell #0, game 2 has a black piece
		add(game1, context1, 0, 1);	
		add(game2, context2, 0, 2);
		
		Assert.assertEquals
		(
			context1.state().canonicalHash(new SubstitutionsOnly(), false), 
			context2.state().canonicalHash(new SubstitutionsOnly(), false)
		);
	}

	/**
	 * Plays different 2 hex moves, verifies canonical hash is the same
	 */
	@Test
	public static void testPlayerNonEquivalence()
	{
		final Game game1 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial1 = new Trial(game1);
		final Context context1 = new Context(game1, trial1);
		game1.start(context1);

		final Game game2 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial2 = new Trial(game2);
		final Context context2 = new Context(game2, trial2);
		game2.start(context2);

		// Game 1 has a white piece at cell #0, game 2 has a black piece
		add(game1, context1, 0, 1);	
		add(game2, context2, 0, 2);
		
		Assert.assertNotEquals
		(
			context1.state().canonicalHash(new AcceptNone(), false), 
			context2.state().canonicalHash(new AcceptNone(), false)
		);
	}
	
	/**
	 * Plays different 2 hex moves, verifies canonical hash is the same
	 */
	@Test
	public static void testRotationEquivalence()
	{
		final Game game1 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial1 = new Trial(game1);
		final Context context1 = new Context(game1, trial1);
		game1.start(context1);

		final Game game2 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial2 = new Trial(game2);
		final Context context2 = new Context(game2, trial2);
		game2.start(context2);

		final List<Cell> cells = context1.containers()[0].topology().cells();
		final Map<String, Integer> nameToCell = new HashMap<>();
		for (int c = 0; c < cells.size(); c++) {
			nameToCell.put(cells.get(c).label(), Integer.valueOf(c));
		}

		final boolean whoOnly = false;
		
		// Two cells chosen to have rotational symmetry only...
		add(game1, context1, nameToCell.get("B12").intValue(), 1);	
		add(game2, context2, nameToCell.get("T10").intValue(), 1);
		Assert.assertEquals
		(
			context1.state().canonicalHash(new RotationsOnly(), whoOnly), 
			context2.state().canonicalHash(new RotationsOnly(), whoOnly)
		);

		// Centre...
		add(game1, context1, nameToCell.get("K11").intValue(), 1);	
		add(game2, context2, nameToCell.get("K11").intValue(), 1);
		Assert.assertEquals
		(
			context1.state().canonicalHash(new RotationsOnly(), whoOnly), 
			context2.state().canonicalHash(new RotationsOnly(), whoOnly)
		);

		// Break the symmetry...
		add(game1, context1, nameToCell.get("A11").intValue(), 1);	
		add(game2, context2, nameToCell.get("F8").intValue(), 1);
		Assert.assertNotEquals
		(
			context1.state().canonicalHash(new RotationsOnly(), whoOnly), 
			context2.state().canonicalHash(new RotationsOnly(), whoOnly)
		);
	}
	
	/**
	 * Plays different 2 hex moves, verifies canonical hash is the same
	 */
	@Test
	public static void testRotationNonEquivalence()
	{
		final Game game1 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial1 = new Trial(game1);
		final Context context1 = new Context(game1, trial1);
		game1.start(context1);

		final Game game2 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial2 = new Trial(game2);
		final Context context2 = new Context(game2, trial2);
		game2.start(context2);

		final List<Cell> cells = context1.containers()[0].topology().cells();
		final Map<String,Integer> nameToCell = new HashMap<>();
		for (int c = 0; c < cells.size(); c++) {
			System.out.println(cells.get(c).label());
			nameToCell.put(cells.get(c).label(), Integer.valueOf(c));
		}

		// Two cells chosen to have rotational symmetry only...
		add(game1, context1, nameToCell.get("B12").intValue(), 1);	
		add(game2, context2, nameToCell.get("T10").intValue(), 1);
		
		// Different with no symmetry
		Assert.assertNotEquals
		(
			context1.state().canonicalHash(new AcceptNone(), false), 
			context2.state().canonicalHash(new AcceptNone(), false)
		);
	}

	/**
	 * Plays different 2 hex moves, verifies canonical hash is the same
	 */
	@Test
	public static void testReflectionEquivalence()
	{
		final Game game1 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial1 = new Trial(game1);
		final Context context1 = new Context(game1, trial1);
		game1.start(context1);

		final Game game2 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial2 = new Trial(game2);
		final Context context2 = new Context(game2, trial2);
		game2.start(context2);

		final List<Cell> cells = context1.containers()[0].topology().cells();
		final Map<String, Integer> nameToCell = new HashMap<>();
		for (int c = 0; c < cells.size(); c++) {
			nameToCell.put(cells.get(c).label(), Integer.valueOf(c));
		}

		// Two cells chosen to have reflection...
		final boolean whoOnly = false;
		
		// Note that reflections alone don't give the full set of hashes, so reflection canonical hashes are flawed unless there is half-turn symmetry
		add(game1, context1, nameToCell.get("B10").intValue(), 1);	
		add(game2, context2, nameToCell.get("B12").intValue(), 1);
		add(game1, context1, nameToCell.get("T10").intValue(), 1);	
		add(game2, context2, nameToCell.get("T12").intValue(), 1);
		Assert.assertEquals
		(
			context1.state().canonicalHash(new ReflectionsOnly(), whoOnly), 
			context2.state().canonicalHash(new ReflectionsOnly(), whoOnly)
		);

		// Centre...
		add(game1, context1, nameToCell.get("K11").intValue(), 1);	
		add(game2, context2, nameToCell.get("K11").intValue(), 1);
		Assert.assertEquals
		(
			context1.state().canonicalHash(new ReflectionsOnly(), whoOnly), 
			context2.state().canonicalHash(new ReflectionsOnly(), whoOnly)
		);

		// Break the symmetry...
		add(game1, context1, nameToCell.get("G13").intValue(), 1);	
		add(game2, context2, nameToCell.get("G15").intValue(), 1);
		Assert.assertNotEquals
		(
			context1.state().canonicalHash(new ReflectionsOnly(), whoOnly), 
			context2.state().canonicalHash(new ReflectionsOnly(), whoOnly)
		);
		
		final Game game3 = GameLoader.loadGameFromName("board/space/blocking/Mu Torere.lud");
		final Trial trial3 = new Trial(game3);
		final Context context3 = new Context(game3, trial3);
		game3.start(context3);

		final Game game4 = GameLoader.loadGameFromName("board/space/blocking/Mu Torere.lud");
		final Trial trial4 = new Trial(game4);
		final Context context4 = new Context(game4, trial4);
		game4.start(context4);
		Assert.assertEquals
		(
			context3.state().canonicalHash(new ReflectionsOnly(), whoOnly), 
			context4.state().canonicalHash(new ReflectionsOnly(), whoOnly)
		);
	}
	
	/**
	 * Plays different 2 hex moves, verifies canonical hash is the same
	 */
	@Test
	public static void testReflectionNonEquivalence()
	{
		final Game game1 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial1 = new Trial(game1);
		final Context context1 = new Context(game1, trial1);
		game1.start(context1);

		final Game game2 = GameLoader.loadGameFromName("board/space/connection/Hex.lud");
		final Trial trial2 = new Trial(game2);
		final Context context2 = new Context(game2, trial2);
		game2.start(context2);

		final List<Cell> cells = context1.containers()[0].topology().cells();
		final Map<String, Integer> nameToCell = new HashMap<>();
		for (int c = 0; c < cells.size(); c++) {
			nameToCell.put(cells.get(c).label(), Integer.valueOf(c));
		}

		// Note that reflections alone don't give the full set of hashes, so reflection canonical hashes are flawed unless there is half-turn symmetry
		add(game1, context1, nameToCell.get("B10").intValue(), 1);	
		add(game2, context2, nameToCell.get("S13").intValue(), 1);
		add(game1, context1, nameToCell.get("T10").intValue(), 1);	
		add(game2, context2, nameToCell.get("T12").intValue(), 1);

		// Different with no symmetry
		Assert.assertNotEquals
		(
			context1.state().canonicalHash(new AcceptNone(), false), 
			context2.state().canonicalHash(new AcceptNone(), false)
		);
	}
		

	private static void add(final Game game, final Context context, final int cell, final int who)
	{
		game.apply(context,
				new Move(new ActionAdd(null, cell, who, 1, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED, null)
						.withDecision(true)).withFrom(cell).withTo(cell));
	}

}
