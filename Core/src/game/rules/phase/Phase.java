package game.rules.phase;

import java.io.Serializable;
import java.util.BitSet;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.mode.Mode;
import game.rules.end.End;
import game.rules.play.Play;
import game.types.play.RoleType;
import other.BaseLudeme;
import other.playout.Playout;

/**
 * Defines the phase of a game.
 * 
 * @author Eric.Piette and cambolbro
 * @remarks A phase can be defined for only one player.
 */
public class Phase extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Move logic. */
	private Play play;

	/** End logic. */
	private End end;
	
	/** Owner of the phase. */
	private final RoleType role;
	
	/** Mode for this phase */
	private final Mode mode;

	/** Name of the phase. */
	private final String name;
	
	/** Conditions to reach another phase. */
	private final NextPhase[] nextPhase;
	
	/** Playout implementation to use inside this phase */
	private Playout playout;

	//-------------------------------------------------------------------------
	
	/**
	 * @param name       The name of the phase.
	 * @param role       The roleType of the owner of the phase [Shared].
	 * @param mode       The mode of this phase within the game [mode defined for
	 *                   whole game)].
	 * @param play       The playing rules of this phase.
	 * @param end        The ending rules of this phase.
	 * @param nextPhase  The next phase of this phase.
	 * @param nextPhases The next phases of this phase.
	 * 
	 * @example (phase "Movement" (play (forEach Piece)))
	 */
	public Phase
	(
			 	 final String      name,
		@Opt 	 final RoleType    role,
		@Opt	 final Mode        mode,
		 	     final Play        play,
		@Opt 	 final End         end,
		@Opt @Or final NextPhase   nextPhase,
		@Opt @Or final NextPhase[] nextPhases
	) 
	{
		this.name = name;
		this.role = (role == null) ? RoleType.Shared : role;
		this.mode = mode;
		this.play = play;
		this.end = end;
		
		int numNonNull = 0;
		if (nextPhase != null)
			numNonNull++;
		if (nextPhases != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Zero or one Or parameter must be non-null.");

		if (nextPhase != null)
		{
			this.nextPhase = new NextPhase[1];
			this.nextPhase[0] = nextPhase;
		}
		else if (nextPhases != null)
		{
			this.nextPhase = nextPhases;
		}
		else
		{
			this.nextPhase = new NextPhase[0];
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Mode for this phase
	 */
	public Mode mode()
	{
		return mode;
	}

	/**
	 * @return Play logic.
	 */
	public Play play()
	{
		return play;
	}

	/**
	 * @return End logic.
	 */
	public End end()
	{
		return end;
	}
	
	/**
	 * @return Name of the phase.
	 */
	public String name()
	{
		return name;
	}

	/**
	 * @return Owner of the phase
	 */
	public RoleType owner()
	{
		return role;
	}

	/**
	 * @return Conditions to reach another phase.
	 */
	public NextPhase[] nextPhase()
	{
		return nextPhase;
	}
	
	/**
	 * Set the Play object
	 * @param play
	 */
	public void setPlay(final Play play)
	{
		this.play = play;
	}
	
	/**
	 * Set the End object
	 * @param end
	 */
	public void setEnd(final End end)
	{
		this.end = end;
	}
	
	/**
	 * @return Playout implementation for this phase
	 */
	public Playout playout()
	{
		return playout;
	}
	
	/**
	 * Sets the playout implementation for this phase
	 * @param playout
	 */
	public void setPlayout(final Playout playout)
	{
		this.playout = playout;
	}

	//---------------------------------------------------

	/**
	 * To preprocess the phase.
	 * 
	 * @param game The game.
	 */
	public void preprocess(final Game game)
	{
		play.moves().preprocess(game);
		for (final NextPhase next : nextPhase())
			next.preprocess(game);
		if (end() != null)
			end().preprocess(game);
	}

	/**
	 * @param game The game.
	 * @return The gameFlags of the phase.
	 */
	public long gameFlags(final Game game)
	{
		long gameFlags = play.moves().gameFlags(game);
		for (final NextPhase next : nextPhase())
			gameFlags |= next.gameFlags(game);
		if (end() != null)
			gameFlags |= end().gameFlags(game);
		return gameFlags;
	}

	/**
	 * @param game The game.
	 * @return The gameFlags of the phase.
	 */
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(play.moves().concepts(game));

		for (final NextPhase next : nextPhase())
			concepts.or(next.concepts(game));
		if (end() != null)
			concepts.or(end().concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(play.moves().writesEvalContextRecursive());

		for (final NextPhase next : nextPhase())
			writeEvalContext.or(next.writesEvalContextRecursive());
		if (end() != null)
			writeEvalContext.or(end().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(play.moves().readsEvalContextRecursive());

		for (final NextPhase next : nextPhase())
			readEvalContext.or(next.readsEvalContextRecursive());
		if (end() != null)
			readEvalContext.or(end().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (game.rules().phases().length > 1)
		{
			// We check if the owner is correct.
			final int indexOwnerPhase = role.owner();
			if ((indexOwnerPhase < 1 && !role.equals(RoleType.Shared)) || indexOwnerPhase > game.players().size())
			{
				game.addRequirementToReport(
						"The phase \"" + name + "\" has an incorrect owner which is " + owner() + ".");
				missingRequirement = true;
			}
			else
			{
				final boolean[] playersInitPhase = new boolean[game.players().size()];
				boolean phaseUsed = false;
				boolean nextPhaseIsReached = false;
				// We check if the phase can be reached.
				for (int i = 0; i < game.rules().phases().length; i++)
				{
					final Phase phase = game.rules().phases()[i];
					final String phaseName = phase.name();

					if (nextPhaseIsReached)
					{
						if (phaseName.equals(name))
						{
							phaseUsed = true;
							break;
						}
						nextPhaseIsReached = false;
					}

					final RoleType rolePhase = phase.owner();

					// The phase belongs to all the players.
					if (rolePhase.equals(RoleType.Shared))
					{
						for (int pid = 1; pid <= game.players().count(); pid++)
						{
							if (!playersInitPhase[pid])
							{
								if (phaseName.equals(name))
									phaseUsed = true;
								break;
							}
						}
						for (int pid = 1; pid < game.players().count(); pid++)
							playersInitPhase[pid] = true;
					}
					else // The phase is for a particular player.
					{
						if (rolePhase.owner() < 1 || rolePhase.owner() > game.players().size())
							continue;

						if (playersInitPhase[rolePhase.owner()] == false)
						{
							if (phaseName.equals(name))
							{
								phaseUsed = true;
								break;
							}
							playersInitPhase[rolePhase.owner()] = true;
						}
					}

					// Check next phases.
					for (int j = 0; j < phase.nextPhase.length; j++)
					{
						final String nameNextPhase = phase.nextPhase[j].phaseName();
						if (nameNextPhase == null)
						{
							nextPhaseIsReached = true;
						}
						else if (nameNextPhase.equals(name))
						{
							phaseUsed = true;
							break;
						}
					}
					if (phaseUsed)
						break;
				}

				if (!phaseUsed)
				{
					game.addRequirementToReport(
							"The phase \"" + name + "\" is described but the phase is never used in the game.");
					missingRequirement = true;
				}
			}
		}

		missingRequirement |= play.moves().missingRequirement(game);
		for (final NextPhase next : nextPhase())
			missingRequirement |= next.missingRequirement(game);
		if (end() != null)
			missingRequirement |= end().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= play.moves().willCrash(game);
		for (final NextPhase next : nextPhase())
			willCrash |= next.willCrash(game);
		if (end() != null)
			willCrash |= end().willCrash(game);
		return willCrash;
	}
}
