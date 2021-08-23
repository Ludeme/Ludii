package game.functions.booleans.is.component;

import java.util.BitSet;
import java.util.function.Supplier;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.board.where.WhereSite;
import game.functions.region.RegionFunction;
import game.functions.region.sites.occupied.SitesOccupied;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.play.RoleType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.IntArrayFromRegion;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.state.container.ContainerState;

/**
 * Returns true if a location is under threat for one specific player.
 * 
 * @author Eric.Piette
 * @remarks Used to avoid being under threat, for example to know if the king is
 *          check.
 */
@Hide
public final class IsThreatened extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** What is under threat. */
	private final IntFunction what;
	
	/** Which sites. */
	protected final IntArrayFromRegion region;

	/** Cell/Edge/Vertex. */
	private SiteType type;
	
	/** The specific moves used to threat. */
	private final Moves specificMoves;

	/** For every thread, store if we'll auto-fail for nested calls in the same thread */
	private static ThreadLocal<Boolean> autoFail = 
			ThreadLocal.withInitial(new Supplier<Boolean>()
			{
				@Override
				public Boolean get()
				{
					return Boolean.FALSE;
				}
			});
	
	//-------------------------------------------------------------------------

	/**
	 * @param what          The component.
	 * @param type          The graph element type [default SiteType of the board].
	 * @param site          The location to check.
	 * @param sites         The locations to check.
	 * @param specificMoves The specific moves used to threat.
	 */
	public IsThreatened 
	(
		@Opt	 final IntFunction    what,
		@Opt     final SiteType       type,
		@Opt @Or final IntFunction    site,
		@Opt @Or final RegionFunction sites,
		@Opt     final Moves          specificMoves
	)
	{
		RegionFunction regionFn = null;
		if (site == null && sites == null && what == null)
			regionFn = new SitesOccupied(null, RoleType.All, null, null, null, null, null, null, null);
		else if (sites != null)
			regionFn = sites;

		IntFunction intFn = null;
		if (site != null)
			intFn = site;
		else if (sites == null && what != null)
			intFn = new WhereSite(what, null);

		region = new IntArrayFromRegion(intFn, regionFn);

		this.what = what;
		this.type = type;
		this.specificMoves = specificMoves;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (autoFails())
			return false;
		
		if (what != null)
		{
			if (what.eval(context) < 1)
				return false;
			
			// To avoid the infinite call of this ludeme.
			if (context.recursiveCalled())
				return false;
		
			final int ownerWhat = context.components()[what.eval(context)].owner();			
			final int[] sites = region.eval(context);
			for (final int site : sites)
			{
				if (site == Constants.OFF)
					continue;

				final Context newContext = new TempContext(context);
				newContext.state().setPrev(ownerWhat);
				newContext.containerState(newContext.containerId()[site]).setSite
					(
						newContext.state(), 
						site, 
						ownerWhat,
						what.eval(newContext), 
						Constants.UNDEFINED, 
						Constants.UNDEFINED,
						Constants.UNDEFINED,
						Constants.UNDEFINED,
						(
							(
								(
									type != null 
									&& 
									type.equals(SiteType.Cell)
								)
								|| 
								(
									type == null 
									&& 
									context.game().board().defaultSite() != SiteType.Vertex
								)
							)
							? SiteType.Vertex
							: SiteType.Cell)
					);

				autoFail.set(Boolean.TRUE);

				final TIntArrayList enemies = context.game().players().players().get(ownerWhat).enemies();
				for (int i = 0; i < enemies.size(); i++)
				{
					final int enemyId = enemies.getQuick(i);
					newContext.state().setMover(enemyId);
					newContext.setRecursiveCalled(true);
					final int enemyPhase = newContext.state().currentPhase(enemyId);
					final Moves moves = (specificMoves == null)
							? newContext.game().rules().phases()[enemyPhase].play().moves()
							: specificMoves;

					if (moves.canMoveTo(newContext, site))
					{
						autoFail.set(Boolean.FALSE);
						return true;
					}
				}

				autoFail.set(Boolean.FALSE);
			}
			return false;
		}
		else // We check if any piece is under threat.
		{
			final int[] sites = region.eval(context);

			if (sites.length == 0)
				return false;
			final ContainerState cs = context.containerState(context.containerId()[sites[0]]);
			for (final int site : sites)
			{
				final int idPiece = cs.what(site, type);

				if (idPiece <= 0 || site == Constants.OFF)
					continue;

				final int ownerWhat = context.components()[idPiece].owner();

				// To avoid the infinite call of this ludeme.
				final int nextPlayer = context.state().next();
				if (0 == nextPlayer || nextPlayer > context.game().players().count())
					return false;

				final Context newContext = new TempContext(context);
				newContext.state().setPrev(ownerWhat);
				newContext.containerState(newContext.containerId()[site]).setSite
					(
						newContext.state(), 
						site,
						ownerWhat,
						idPiece,
						Constants.UNDEFINED, 
						Constants.UNDEFINED, 
						Constants.UNDEFINED,
						Constants.UNDEFINED,
						(
							(
								(
									type != null
									&& 
									type.equals(SiteType.Cell)
								)
								|| 
								(
									type == null 
									&& 
									context.game().board().defaultSite() != SiteType.Vertex
								)
							)
							? SiteType.Vertex
							: SiteType.Cell
						)
					);
				
				autoFail.set(Boolean.TRUE);

				final TIntArrayList enemies = newContext.game().players().players().get(ownerWhat).enemies();

				for (int i = 0; i < enemies.size(); i++)
				{
					newContext.state().setMover(enemies.getQuick(i));
					newContext.state().setNext(ownerWhat);
					final int enemyPhase = newContext.state().currentPhase(enemies.getQuick(i));
					final Moves moves = (specificMoves == null)
							? newContext.game().rules().phases()[enemyPhase].play().moves()
							: specificMoves;

					if (moves.canMoveTo(newContext, site))
					{
						autoFail.set(Boolean.FALSE);
						return true;
					}
				}
				
				autoFail.set(Boolean.FALSE);
			}
			return false;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Threatened(" + what + "," + region + ")";
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		gameFlags |= SiteType.gameFlags(type);

		if (what != null)
			gameFlags |= what.gameFlags(game);

		if (specificMoves != null)
			gameFlags |= specificMoves.gameFlags(game);

		gameFlags |= region.gameFlags(game);
		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Threat.id(), true);
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.CopyContext.id(), true);

		if (what != null)
			concepts.or(what.concepts(game));

		if (specificMoves != null)
			concepts.or(specificMoves.concepts(game));

		concepts.or(region.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (what != null)
			writeEvalContext.or(what.writesEvalContextRecursive());

		if (specificMoves != null)
			writeEvalContext.or(specificMoves.writesEvalContextRecursive());

		writeEvalContext.or(region.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (what != null)
			readEvalContext.or(what.readsEvalContextRecursive());

		if (specificMoves != null)
			readEvalContext.or(specificMoves.readsEvalContextRecursive());

		readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		if (what != null)
			what.preprocess(game);
		region.preprocess(game);

		if (specificMoves != null)
			specificMoves.preprocess(game);
	}
	
	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		if (what != null)
			missingRequirement |= what.missingRequirement(game);

		if (specificMoves != null)
			missingRequirement |= specificMoves.missingRequirement(game);

		missingRequirement |= region.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		if (what != null)
			willCrash |= what.willCrash(game);

		if (specificMoves != null)
			willCrash |= specificMoves.willCrash(game);

		willCrash |= region.willCrash(game);
		return willCrash;
	}

	@Override
	public boolean autoFails()
	{
		return autoFail.get().booleanValue();
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		String whatEnglish = "a piece";
		if (what != null)
			whatEnglish = what.toEnglish(game);

		return whatEnglish + " is threatened";
	}
}
