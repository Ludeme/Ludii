package game.functions.booleans.no;

import java.util.BitSet;
import java.util.function.Supplier;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.board.Id;
import game.types.play.RoleType;
import other.concept.Concept;
import other.context.Context;
import other.state.State;

/**
 * To check if one specific or all players can just pass.
 * 
 * @author Eric.Piette
 * @remarks Checks if a player is stalemated in the ending conditions.
 */
@Hide
public final class NoMoves extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Role of the player under investigation. */
	private final RoleType role;
	
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
	 * @param playerFn The roleType to check.
	 */
	public NoMoves(final RoleType playerFn)
	{
		this.role = playerFn;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (role == RoleType.Next)
		{
			if (autoFail.get().booleanValue())
				return false;
			
			// (stalemated Next) is a common special case which we expect
			// to return true if the next player will be stalemated in the
			// turn that directly follows the current turn.
			//
			// The normal Ludii code only sets the stalemated flag correctly
			// AFTER we switch over to the next player and compute their legal
			// moves. So, to get the expected behaviour in this special case,
			// we have to temporarily switch over to the next player and compute
			// their legal moves

			final State state = context.state();
			final int currentPrevious = state.prev();
			final int currentMover = state.mover();
			final int nextMover = state.next();
			
			if (!context.trial().over() && context.active())
			{
				context.setMoverAndImpliedPrevAndNext(state.next());
				state.setPrev(currentMover);
			}
			
			autoFail.set(Boolean.TRUE);		// Avoid recursive calls
			context.game().computeStalemated(context);
			autoFail.set(Boolean.FALSE);
			
			// and switch mover back
			state.setPrev(currentPrevious);
			state.setMover(currentMover);
			state.setNext(nextMover);
		}
		
		return context.state().isStalemated(new Id(null, role).eval(context));
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Stalemated(" + role + ")";
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0;
	}
	
	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		// We check if the role is correct.
		final int indexOwnerPhase = role.owner();
		if ((indexOwnerPhase < 1 && !role.equals(RoleType.Mover) && !role.equals(RoleType.Prev)
				&& !role.equals(RoleType.Next)) || indexOwnerPhase > game.players().count())
		{
			game.addRequirementToReport(
					"In the ludeme (no Moves ...) a wrong RoleType is used: " + role + ".");
			missingRequirement = true;
		}

		return missingRequirement;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Stalemate.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// do nothing
	}
	
	@Override
	public boolean autoFails()
	{
		return autoFail.get().booleanValue();
	}
}
