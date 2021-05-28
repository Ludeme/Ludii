package game.equipment.other;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.equipment.Item;
import game.equipment.component.Component;
import game.equipment.container.board.Board;
import game.functions.ints.IntFunction;
import game.types.board.LandmarkType;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.math.Pair;
import gnu.trove.map.hash.TIntIntHashMap;
import main.Constants;
import main.StringRoutines;
import other.ItemType;
import other.context.Context;
import other.topology.SiteFinder;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Defines a map between two locations or integers.
 * 
 * @author Eric.Piette
 * @remarks Used to map a site to another or to map an integer to another.
 */
public class Map extends Item
{
	/** The map. */
	private final TIntIntHashMap map = new TIntIntHashMap();

	/** The pairs of the map. */
	private final Pair[] mapPairs;

	//------------------------------------------------------------------------------

	/**
	 * For map of pairs.
	 * 
	 * @param name  The name of the map ["Map"].
	 * @param pairs The pairs of each map.
	 * 
	 * @example (map "Entry" { (pair P1 "D1") (pair P2 "E8") (pair P3 "H4") (pair P4
	 *          "A5")})
	 * 
	 * @example (map { (pair 5 19) (pair 7 9) (pair 34 48) (pair 36 38) } )
	 * 
	 * @example (map {(pair P1 P4) (pair P2 P5) (pair P3 P6) (pair P4 P1) (pair P5
	 *          P2) (pair P6 P3)})
	 */
	public Map
	(
		@Opt final String name,
			 final Pair[] pairs
	)
	{
		super(((name == null) ? "Map" : name), Constants.UNDEFINED, RoleType.Neutral);
		this.mapPairs = pairs;
		setType(ItemType.Map);
	}
	
	/**
	 * For map between integers.
	 * 
	 * @param name   The name of the map ["Map"].
	 * @param keys   The keys of the map.
	 * @param values The values of the map.
	 * 
	 * @example (map {1..9} {1 2 4 8 16 32 64 128 256})
	 */
	public Map
	(
		@Opt final String        name, 
		     final IntFunction[] keys, 
		     final IntFunction[] values
    )
	{
		super(((name == null) ? "Map" : name), Constants.UNDEFINED, RoleType.Neutral);

		if (keys.length != values.length)
			throw new IllegalArgumentException(
					"A map has to be defined with exactly the same number of keys than values.");

		final int minLength = Math.min(keys.length, values.length);

		mapPairs = new Pair[minLength];
		for (int i = 0; i < minLength; i++)
			mapPairs[i] = new Pair(keys[i], values[i]);

		setType(ItemType.Map);
	}
	
	//------------------------------------------------------------------------------
	
	/**
	 * @return the map of Integer.
	 */
	public TIntIntHashMap map()
	{
		return this.map;
	}

	/**
	 * To get the value of the key in the map.
	 * 
	 * @param key
	 * @return value corresponding to the key.
	 */
	public int to(final int key)
	{
		return map.get(key);
	}
	
	/**
	 * @return The value returned by map when values don't exist for any given key.
	 */
	public int noEntryValue()
	{
		return map.getNoEntryValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * We compute the maps.
	 * 
	 * @param game The game.
	 */
	public void computeMap(final Game game)
	{
		for (final Pair pair : mapPairs)
		{
			int intKey = pair.intKey().eval(new Context(game, new Trial(game)));
			if (intKey == Constants.OFF)
			{
				final TopologyElement element = SiteFinder.find(game.board(), pair.stringKey(),
						game.board().defaultSite());
				if (element != null)
					intKey = element.index();
			}

			int intValue = pair.intValue().eval(new Context(game, new Trial(game)));
			if (intValue == Constants.OFF)
			{
				if (pair.stringValue() != null)
				{
					if (StringRoutines.isCoordinate(pair.stringValue()))
					{
						final TopologyElement element = SiteFinder.find(game.board(), pair.stringValue(),
								game.board().defaultSite());
						if (element != null)
							intValue = element.index();
					}
					else
					{
						for (int i = 1; i < game.equipment().components().length; i++)
						{
							final Component component = game.equipment().components()[i];
							if (component.name().equals(pair.stringValue()))
							{
								intValue = i;
								break;
							}
						}
					}
				}
				else
				{
					final LandmarkType landmarkType = pair.landmarkType();
					intValue = getSite(game.board(), landmarkType);
				}

			}

			if (intValue != Constants.OFF && intKey != Constants.OFF)
				map.put(intKey, intValue);

		}
	}

	/**
	 * @param board        The graph of the board.
	 * @param landmarkType The landmark site to get.
	 * @return The site corresponding to the landmark.
	 */
	private static int getSite(final Board board, final LandmarkType landmarkType)
	{
		switch (landmarkType)
		{
		case BottomSite:
			return (board.defaultSite() == SiteType.Vertex 
					? board.topology().bottom(SiteType.Vertex)
					: board.topology().bottom(SiteType.Cell)).get(0).index();
		case CentreSite:
			return (board.defaultSite() == SiteType.Vertex 
					? board.topology().centre(SiteType.Vertex)
					: board.topology().centre(SiteType.Cell)).get(0).index();
		case LeftSite:
			return (board.defaultSite() == SiteType.Vertex 
					? board.topology().left(SiteType.Vertex)
					: board.topology().left(SiteType.Cell))
					.get(0).index();
		case RightSite:
			return (board.defaultSite() == SiteType.Vertex 
					? board.topology().right(SiteType.Vertex)
					: board.topology().right(SiteType.Cell)).get(0).index();
		case Topsite:
			return (board.defaultSite() == SiteType.Vertex 
					? board.topology().top(SiteType.Vertex)
					: board.topology().top(SiteType.Cell)).get(0).index();
		case FirstSite:
			return 0;
		case LastSite:
			return (board.defaultSite() == SiteType.Vertex
					? board.topology().vertices().get(board.topology().vertices().size() - 1).index()
					: board.topology().cells().get(board.topology().cells().size() - 1).index());
		default:
			return Constants.UNDEFINED;
		}
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		for (final Pair pair : mapPairs)
			gameFlags |= pair.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		for (final Pair pair : mapPairs)
			concepts.or(pair.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		for (final Pair pair : mapPairs)
			writeEvalContext.or(pair.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		for (final Pair pair : mapPairs)
			readEvalContext.or(pair.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (role() != null)
		{
			final int indexOwnerPhase = role().owner();
			if (
					(
						indexOwnerPhase < 1 
						&& 
						!role().equals(RoleType.Shared)
						&& 
						!role().equals(RoleType.Neutral)
						&& 
						!role().equals(RoleType.All)
					) 
					|| 
					indexOwnerPhase > game.players().count()
				)
			{
				game.addRequirementToReport(
						"A map is defined in the equipment with an incorrect owner: " + role() + ".");
				missingRequirement = true;
			}
		}

		for (final Pair pair : mapPairs)
			missingRequirement |= pair.missingRequirement(game);

		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;

		for (final Pair pair : mapPairs)
			willCrash |= pair.willCrash(game);

		return willCrash;
	}
}
