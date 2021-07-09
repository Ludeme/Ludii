package game.functions.ints.trackSite.first;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.container.board.Track;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import main.Constants;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the first site of a track.
 * 
 * @author Eric.Piette
 */
@Hide
public final class TrackSiteFirstTrack extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The name of the track. */
	private final String name;
	
	/** Player Id function. */
	private final IntFunction pidFn;

	/** The site from which to look. */
	private final IntFunction fromFn;
	
	/** The condition to verify. */
	private final BooleanFunction condFn;

	//-------------------------------------------------------------------------

	/** The pre-computed value. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * @param player The index of the player. 
	 * @param role   The role of the player.
	 * @param name   The name of the track.
	 * @param from   The site from where to look [First site of the track].
	 * @param If     The condition to verify for that site [True].
	 */
	public TrackSiteFirstTrack
	(
			  @Or @Opt final game.util.moves.Player player,
			  @Or @Opt final RoleType	            role,
			      @Opt final String                 name,
		@Name	  @Opt final IntFunction            from,
		@Name	  @Opt final BooleanFunction        If
	)
	{
		this.name = name;
		this.pidFn = (player != null) ? player.index() : (role != null) ? RoleType.toIntFunction(role) : null;
		this.fromFn = (from == null) ? null : from;
		this.condFn = (If == null) ? new BooleanConstant(true) : If;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		final int playerId = (pidFn != null) ? pidFn.eval(context) : 0;
		Track track = null;

		for (final Track t : context.tracks())
		{
			if (name != null && playerId == 0)
			{
				if (t.name().contains(name))
				{
					track = t;
					break;
				}
			}
			else if (name != null)
			{
				if (name != null)
				{
					if (t.name().contains(name) && t.owner() == playerId)
					{
						track = t;
						break;
					}
				}
			}
			else if (t.owner() == playerId || t.owner() == 0)
			{
				track = t;
				break;
			}
		}

		if (track == null)
		{
			if (context.game().board().tracks().size() == 0)
				return Constants.UNDEFINED; // no track at all.
			else
				track = context.game().board().tracks().get(0);
		}

		// Get first site.
		final int from = (fromFn == null) ? Constants.UNDEFINED : fromFn.eval(context);
		boolean found = false;
		int i = 0;
		for(; i < track.elems().length; i++)
		{
			final int site = track.elems()[i].site;
			if(from == Constants.UNDEFINED || site == from)
			{
				found = true;
				break;
			}
		}
		
		if(!found)
			return Constants.UNDEFINED;

		found = false;
		final int origTo = context.to();
		
		// Check the condition.
		for(int j = i; j < track.elems().length + i; j++)
		{
			final int index = j % track.elems().length;
			final int site = track.elems()[index].site;
			context.setTo(site);
			if(condFn.eval(context))
			{
				found = true;
				i = index;
				break;
			}
		}

		context.setTo(origTo);	
		
		if(!found)
			return Constants.UNDEFINED;
		
		return track.elems()[i].site;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if(pidFn != null && !pidFn.isStatic())
			return false;
		
		if(condFn != null && !condFn.isStatic())
			return false;
		
		return pidFn == null || pidFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0L;

		if (pidFn != null)
			flags |= pidFn.gameFlags(game);

		if (fromFn != null)
			flags |= fromFn.gameFlags(game);

		if (condFn != null)
			flags |= condFn.gameFlags(game);
		
		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		if (pidFn != null)
			concepts.or(pidFn.concepts(game));

		if (fromFn != null)
			concepts.or(fromFn.concepts(game));
		
		if (condFn != null)
			concepts.or(condFn.concepts(game));
		
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();

		if (pidFn != null)
			writeEvalContext.or(pidFn.writesEvalContextRecursive());

		if (fromFn != null)
			writeEvalContext.or(fromFn.writesEvalContextRecursive());

		if (condFn != null)
			writeEvalContext.or(condFn.writesEvalContextRecursive());
		writeEvalContext.set(EvalContextData.To.id(), true);
		
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();

		if (pidFn != null)
			readEvalContext.or(pidFn.readsEvalContextRecursive());
		
		if (fromFn != null)
			readEvalContext.or(fromFn.readsEvalContextRecursive());

		if (condFn != null)
			readEvalContext.or(condFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (pidFn != null)
			pidFn.preprocess(game);
		
		if (fromFn != null)
			fromFn.preprocess(game);

		if (condFn != null)
			condFn.preprocess(game);

		if (isStatic())
			precomputedValue = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasTrack())
		{
			game.addRequirementToReport("The ludeme (trackSite EndTrack ...) is used but the board has no tracks.");
			missingRequirement = true;
		}

		if (pidFn != null)
			missingRequirement |= pidFn.missingRequirement(game);
		if (fromFn != null)
			missingRequirement |= fromFn.missingRequirement(game);
		if (condFn != null)
			missingRequirement |= condFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (pidFn != null)
			willCrash |= pidFn.willCrash(game);
		if (fromFn != null)
			willCrash |= fromFn.willCrash(game);
		if (condFn != null)
			willCrash |= condFn.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "";
	}
}