package game.rules.end;

import java.util.Arrays;
import java.util.BitSet;

import annotations.Or;
import game.Game;
import game.functions.ints.board.Id;
import game.rules.Rule;
import game.types.play.ResultType;
import game.types.play.RoleType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.Status;
import other.BaseLudeme;
import other.context.Context;
import other.state.State;
import other.trial.Trial;

/**
 * Defines the rules for ending a game.
 *
 * @author Eric.Piette and cambolbro
 */
public class End extends BaseLudeme implements Rule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Condition that triggers end of game. */
	private final EndRule[] endRules;

	/** True if this ludeme is used for a match. */
	private boolean match = false;

	//-------------------------------------------------------------------------
	
	/**
	 * @param endRule  The ending rule.
	 * @param endRules The ending rules.
	 * 
	 * @example (end (if (no Moves Next) (result Mover Win) ) )
	 */
	public End
	(
		@Or final EndRule   endRule,
		@Or final EndRule[] endRules
	)
	{
		int numNonNull = 0;
		if (endRule != null)
			numNonNull++;
		if (endRules != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");

		if (endRule != null)
			this.endRules = new EndRule[] { endRule };
		else
		{
			this.endRules = endRules;
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Array of end rules.
	 */
	public EndRule[] endRules()
	{
		return endRules;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{		
		if (match)
		{
			evalMatch(context);
		}
		else
		{
			Result applyResult;

			for (final EndRule endingRule : endRules)
			{
				final EndRule endRuleResult = endingRule.eval(context);
				if (endRuleResult == null)
					continue;

				applyResult = endRuleResult.result();
				if (applyResult == null)
					continue;

				final int whoResult = new Id(null, applyResult.who()).eval(context);

				if (!context.active(whoResult) && whoResult != context.game().players().size())
					continue;

				applyResult(applyResult, context);

				// Do not break here. Sometimes we need to check all
				// end conditions, not just the first one that's true.
				// break;
			}

			if (!context.trial().over() && context.game().requiresAllPass() && context.allPass())
			{
				// Nobody wins
				applyResult = new Result(RoleType.All, ResultType.Draw);
				applyResult(applyResult, context);
			}
		}

		// Reinit these numbers
		context.setNumLossesDecided(0);
		context.setNumWinsDecided(0);

		// To print the rank at the end
//		for (int id = 0; id < context.state().ranking().length; id++)
//			System.out.println("id = 0, rank is " + context.state().ranking()[id]);
	}

	/**
	 * Apply the result of the game.
	 * 
	 * @param applyResult The result to apply.
	 * @param context     The context.
	 */
	public static void applyResult(final Result applyResult, final Context context)
	{
		final RoleType whoRole = applyResult.who();
		if (whoRole.toString().contains("Team"))
		{
			applyTeamResult(applyResult, context);
		}
		else
		{
			final Trial trial = context.trial();
			final State state = context.state();
			
			// Apply result
			final int who = new Id(null, whoRole).eval(context);

			// System.out.println("WHO: " + who);

			double rank = 1.0;
			int onlyOneActive = Constants.UNDEFINED;
			switch (applyResult.result())
			{
			case Win: // "who" wins
				if (whoRole.equals(RoleType.All))
				{
					if (context.game().players().count() > 1)
					{
						final double score = 1.0;
						for (int player = 1; player < context.trial().ranking().length; player++)
						{
							if (context.trial().ranking()[player] == 0.0)
							{
								context.addWinner(player);
								context.trial().ranking()[player] = score;
							}
						}
					}
					else
					{
						context.trial().ranking()[1] = 0.0;
					}

					context.setAllInactive();

					trial.setStatus(new Status(0));
				}
				else
				{
					context.setActive(who, false);
					rank = context.computeNextWinRank();
					context.addWinner(who);
					assert(rank >= 1.0 && rank <= context.trial().ranking().length);
					context.trial().ranking()[who] = rank;

					onlyOneActive = context.onlyOneActive();
					if (onlyOneActive != 0)
					{
						context.trial().ranking()[onlyOneActive] = rank + 1.0;
						for (int player = 1; player < context.trial().ranking().length; player++)
						{
							if (context.trial().ranking()[player] == 1)
							{
								trial.setStatus(new Status(player));
								break;
							}
						}
					}
					else if (!context.active())
					{
						context.setAllInactive();
						for (int player = 1; player < context.trial().ranking().length; player++)
						{
							if (context.trial().ranking()[player] == 1)
							{
								trial.setStatus(new Status(player));
								break;
							}
						}
					}
				}
				
				// We increment that data in case of many results in the same turn.
				context.setNumWinsDecided(context.numWinsDecided() + 1);
				
				break;
			case Loss: // "opp" loses
				if (whoRole.equals(RoleType.All))
				{
					if (context.game().players().count() > 1)
					{
						final double score = context.game().players().count();
						for (int player = 1; player < context.trial().ranking().length; player++)
						{
							if (context.trial().ranking()[player] == 0.0)
							{
								context.trial().ranking()[player] = score;
							}
						}
					}
					else
					{
						context.trial().ranking()[1] = 0.0;
					}

					context.setAllInactive();
					trial.setStatus(new Status(0));
					break;
				}
				else
				{
					if (context.active(who)) // We apply the result only if that player was active before to lose.
					{
						context.setActive(who, false);
						if (state.next() == who && context.game().players().count() != 1)
						{
							int next = who;
							while (!context.active(next) && context.active())
							{
								next++;
								if (next > context.game().players().count())
									next = 1;
							}
							state.setNext(next);
						}
						// Don't do anything for one player game.
						if (context.trial().ranking().length > 2)
						{
							// Rank we would assign for a brand new singular loss
							rank = context.computeNextLossRank();		
							// Num losses already assigned in same eval() call
							final int numSimulLosses = context.numLossesDecided();
							// Compute rank we may have temporarily assigned to any previous losses
							final double prevLossRank = rank + 1.0 + 0.5 * (numSimulLosses - 1.0);
							// Compute the correct rank that we want for this player and any other simultaneous losers
							rank = prevLossRank - 0.5;
							
							context.trial().ranking()[who] = rank;
							context.addLoser(who);
							assert(rank >= 1.0 && context.trial().ranking()[who] <= rank);
							
							for (int id = 0; id < context.trial().ranking().length; id++)
							{
								if (context.trial().ranking()[id] == prevLossRank)
									context.trial().ranking()[id] = rank;
							}

							if (!context.active())
							{
								context.setAllInactive();
								for (int player = 1; player < context.trial().ranking().length; player++)
								{
									if (context.trial().ranking()[player] == 1.0)
									{
										trial.setStatus(new Status(player));
										break;
									}
								}
							}
							else
							{
								onlyOneActive = context.onlyOneActive();
								if (onlyOneActive != 0)
								{
									rank = context.computeNextWinRank();
									assert(rank >= 1.0 && rank <= context.trial().ranking().length);
									context.trial().ranking()[onlyOneActive] = rank;
									double minRank = Constants.DEFAULT_MOVES_LIMIT; // A high limit to be sure that will
																					// be
																					// never reach with 16 players.
									for (int player = 1; player < context.trial().ranking().length; player++)
										if (context.trial().ranking()[player] < minRank)
											minRank = context.trial().ranking()[player];

									for (int player = 1; player < context.trial().ranking().length; player++)
									{
										if (context.trial().ranking()[player] == minRank)
										{
											trial.setStatus(new Status(player));
											context.addWinner(player);
											break;
										}
									}
									context.setActive(onlyOneActive, false);
								}
							}
						}
						else
						{
							context.setAllInactive();
							trial.setStatus(new Status(0));
						}
					}
					
					// We increment that data in case of many results in the same turn.
					context.setNumLossesDecided(context.numLossesDecided() + 1);
					
					break;
				}
			case Draw: // nobody wins
				if (context.game().players().count() > 1)
				{
					final double score = context.computeNextDrawRank();
					assert(score >= 1.0 && score <= context.trial().ranking().length);
					for (int player = 1; player < context.trial().ranking().length; player++)
					{
						if (context.trial().ranking()[player] == 0.0)
						{
							context.trial().ranking()[player] = score;
						}
					}
				}
				else if (context.game().players().count() == 1)
				{
					context.trial().ranking()[1] = 0.0;
				}

				context.setAllInactive();

				trial.setStatus(new Status(0));
				for (int i = 0; i < context.trial().ranking().length; i++)
				{
					if (context.trial().ranking()[i] == 1.0)
						trial.setStatus(new Status(i));
				}

				break;
			case Tie: // everybody wins
				context.setAllInactive();
				trial.setStatus(new Status(state.numPlayers() + 1)); // 0));
				break;
			case Abandon: 
			case Crash:	  // nobody wins
				context.setAllInactive();
				trial.setStatus(new Status(-1));
				break;
			default:
				System.out.println("** End.apply(): Result type " + applyResult.result() + " not recognised.");
			}
		}
	}

	/**
	 * Eval the end of a match
	 *
	 * @param context
	 */
	private void evalMatch(final Context context)
	{
		for (final EndRule endingRule : endRules)
		{
			final EndRule endRule = endingRule.eval(context);

			if (endRule == null)
				continue;

			final Result applyResult = endRule.result();
			if (applyResult != null)
			{
				applyResultMatch(applyResult, context);
				break;
			}
		}
	}

	/**
	 * Apply the result of the match.
	 * 
	 * @param applyResult The result to apply.
	 * @param context     The context.
	 */
	public static void applyResultMatch(final Result applyResult, final Context context)
	{
		final RoleType whoRole = applyResult.who();
		if (whoRole.toString().contains("Team"))
		{
			applyTeamResult(applyResult, context);
		}
		else
		{
			final Trial trial = context.trial();
			
			// Apply result
			final int who = new Id(null, whoRole).eval(context);

			// System.out.println("WHO: " + who);

			switch (applyResult.result())
			{
			case Win: // "who" wins
				context.setActive(who, false);
				context.addWinner(who);
				// To get the rank of the player.
				double rank = 1.0;
				for (/**/; rank < context.trial().ranking().length; rank++)
				{
					boolean yourRank = true;
					for (int player = 1; player < context.trial().ranking().length; player++)
					{
						if (context.trial().ranking()[player] == rank)
						{
							yourRank = false;
							break;
						}
					}
					if (yourRank)
					{
						context.trial().ranking()[who] = rank;
						assert(rank >= 1.0 && rank <= context.trial().ranking().length);
						break;
					}
				}

				final int onlyOneActive = context.onlyOneActive();
				if (onlyOneActive != 0)
				{
					context.trial().ranking()[onlyOneActive] = rank + 1;
					assert(context.trial().ranking()[onlyOneActive] >= 1.0 && context.trial().ranking()[onlyOneActive] <= context.trial().ranking().length);
					for (int player = 1; player < context.trial().ranking().length; player++)
					{
						if (context.trial().ranking()[player] == 1)
						{
							trial.setStatus(new Status(player));
							break;
						}
					}
				}
				else if (!context.active())
				{
					context.setAllInactive();
					for (int player = 1; player < context.trial().ranking().length; player++)
					{
						if (context.trial().ranking()[player] == 1)
						{
							trial.setStatus(new Status(player));
							break;
						}
					}
				}
				break;
			case Loss: // "opp" loses
//					state.setActive(who, false);
//					if (state.next() == who && context.activeGame().players().count() != 1)
//					{
//						int next = who;
//						while (!state.active(next))
//						{
//							next++;
//							if (next > context.activeGame().players().count())
//								next = 1;
//						}
//						state.setNext(next);
//					}
//					// Don't do anything for one player game.
//					if (state.ranking().length > 2)
//					{
//						rank = state.ranking().length - 1;
//						for (; rank > 0; rank--)
//						{
//							boolean yourRank = true;
//							for (int player = 1; player < state.ranking().length; player++)
//							{
//								if (state.ranking()[player] == rank)
//								{
//									yourRank = false;
//									break;
//								}
//							}
//							if (yourRank)
//							{
//								state.ranking()[who] = rank;
//								break;
//							}
//						}
//
//						if (!state.active())
//						{
//							state.setAllInactive();
//							for (int player = 1; player < state.ranking().length; player++)
//							{
//								if (state.ranking()[player] == 1)
//								{
//									trial.setStatus(new Status(player));
//									break;
//								}
//							}
//						}
//						else
//						{
//							onlyOneActive = state.onlyOneActive(state.numPlayers());
//							if (onlyOneActive != 0)
//							{
//								state.ranking()[onlyOneActive] = rank - 1;
//								for (int player = 1; player < state.ranking().length; player++)
//								{
//									if (state.ranking()[player] == 1)
//									{
//										trial.setStatus(new Status(player));
//										state.setActive(onlyOneActive, false);
//										state.addWinner(player);
//										break;
//									}
//								}
//							}
//						}
//					}
//					else
//					{
//						state.setAllInactive();
//						trial.setStatus(new Status(0));
//					}
				break;
			case Draw: // nobody wins
				if (context.game().players().count() > 1)
				{
					final double score = (context.numActive() + 1) / 2.0 + (context.numWinners());
					assert(score >= 1.0 && score <= context.trial().ranking().length);
					for (int player = 1; player < context.trial().ranking().length; player++)
					{
						if (context.trial().ranking()[player] == 0.0)
						{
							context.trial().ranking()[player] = score;
						}
					}
				}
				else
				{
					context.trial().ranking()[1] = 0.0;
				}

				trial.setStatus(new Status(0));
				for (int i = 0; i < context.trial().ranking().length; i++)
				{
					if (context.trial().ranking()[i] == 1.0)
						trial.setStatus(new Status(i));
				}

				break;
			case Tie: // everybody wins
				trial.setStatus(new Status(context.game().players().count() + 1)); // 0));
				break;
			case Abandon: 
			case Crash:	  // nobody wins
				trial.setStatus(new Status(-1));
				break;
			default:
				System.out.println("** End.apply(): Result type " + applyResult.result() + " not recognised.");
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Apply the result of the game.
	 *
	 * @param applyResult
	 * @param context
	 */
	public static void applyTeamResult(final Result applyResult, final Context context)
	{
		final Trial trial      = context.trial();
		final State state      = context.state();
		final RoleType whoRole = applyResult.who();
		final int team         = new Id(null, whoRole).eval(context);

		switch (applyResult.result())
		{
		case Win: // "who" wins
			final TIntArrayList teamMembers = new TIntArrayList();
			for (int pid = 1; pid <= context.game().players().count(); pid++)
				if (context.state().playerInTeam(pid, team))
					teamMembers.add(pid);

			for (int i = 0; i < teamMembers.size(); i++)
			{
				final int pid = teamMembers.getQuick(i);
				context.setActive(pid, false);
				context.addWinner(pid);
			}

			// To get the rank of the player.
			int rank = 1;
			for (/**/; rank < context.trial().ranking().length; rank++)
			{
				boolean yourRank = true;
				for (int player = 1; player < context.trial().ranking().length; player++)
				{
					if (context.trial().ranking()[player] == rank)
					{
						yourRank = false;
						break;
					}
				}
				if (yourRank)
				{
					for (int i = 0; i < teamMembers.size(); i++)
					{
						final int pid = teamMembers.getQuick(i);
						context.trial().ranking()[pid] = rank;
					}
					break;
				}
			}

			final int onlyOneActive = context.onlyOneTeamActive();
			if (onlyOneActive != 0)
			{
				final TIntArrayList teamLossMembers = new TIntArrayList();
				for (int pid = 1; pid <= context.game().players().count(); pid++)
					if (context.state().playerInTeam(pid, onlyOneActive))
						teamLossMembers.add(pid);

				for (int i = 0; i < teamLossMembers.size(); i++)
				{
					final int pid = teamLossMembers.getQuick(i);
					context.trial().ranking()[pid] = rank + teamMembers.size();
					for (int player = 1; player < context.trial().ranking().length; player++)
					{
						if (context.trial().ranking()[player] == 1)
						{
							trial.setStatus(new Status(player));
							break;
						}
					}
				}
			}
			else if (!context.active())
			{
				context.setAllInactive();
				for (int player = 1; player < context.trial().ranking().length; player++)
				{
					if (context.trial().ranking()[player] == 1)
					{
						trial.setStatus(new Status(player));
						break;
					}
				}
			}
			break;
		case Loss: // "opp" loses
			// TO DO ERIC
//			state.setActive(team, false);
//			if (state.next() == team && context.activeGame().players().count() != 1)
//			{
//				int next = team;
//				while (!state.active(next))
//				{
//					next++;
//					if (next > context.activeGame().players().count())
//						next = 1;
//				}
//				state.setNext(next);
//			}
//			state.setNumActive(state.numActive() - 1);
//			// Don't do anything for one player game.
//			if (state.ranking().length > 2)
//			{
//				rank = state.ranking().length - 1;
//				for (; rank > 0; rank--)
//				{
//					boolean yourRank = true;
//					for (int player = 1; player < state.ranking().length; player++)
//					{
//						if (state.ranking()[player] == rank)
//						{
//							yourRank = false;
//							break;
//						}
//					}
//					if (yourRank)
//					{
//						state.ranking()[team] = rank;
//						break;
//					}
//				}
//
//				if (!state.active())
//				{
//					state.setAllInactive();
//					for (int player = 1; player < state.ranking().length; player++)
//					{
//						if (state.ranking()[player] == 1)
//						{
//							trial.setStatus(new Status(player));
//							break;
//						}
//					}
//				}
//				else
//				{
//					onlyOneActive = state.onlyOneActive(state.numPlayers());
//					if (onlyOneActive != 0)
//					{
//						state.setNumActive(state.numActive() - 1);
//						state.ranking()[onlyOneActive] = rank - 1;
//						for (int player = 1; player < state.ranking().length; player++)
//						{
//							if (state.ranking()[player] == 1)
//							{
//								trial.setStatus(new Status(player));
//								state.setActive(onlyOneActive, false);
//								state.addWinner(player);
//								break;
//							}
//						}
//					}
//				}
//			}
//			else
//			{
//				state.setAllInactive();
//				trial.setStatus(new Status(0));
//			}
			break;
		case Draw: // nobody wins
			// TO DO ERIC
//			if (context.activeGame().players().count() > 1)
//			{
//				final double score = (state.numActive() + 1) / 2.0 + (state.numWinner());
//				for (int player = 1; player < state.ranking().length; player++)
//				{
//					if (state.ranking()[player] == 0.0)
//					{
//						state.ranking()[player] = score;
//					}
//				}
//			}
//			else
//			{
//				state.ranking()[1] = 0.0;
//			}
//
//			state.setAllInactive();
//
//			trial.setStatus(new Status(0));
//			for (int i = 0; i < state.ranking().length; i++)
//			{
//				if (state.ranking()[i] == 1.0)
//					trial.setStatus(new Status(i));
//			}

			break;
		case Tie: // everybody wins
			context.setAllInactive();
			trial.setStatus(new Status(state.numPlayers() + 1)); // 0));
			break;
		case Abandon: 
		case Crash:   // nobody wins
			context.setAllInactive();
			trial.setStatus(new Status(-1));
			break;
		default:
			System.out.println("** End.apply(): Result type " + applyResult.result() + " not recognised.");

		}
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
		long gameFlags = 0;
				
		for (final EndRule endRule : endRules)
			gameFlags |= endRule.gameFlags(game);
		
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		for (final EndRule endRule : endRules)
			concepts.or(endRule.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		for (final EndRule endRule : endRules)
			writeEvalContext.or(endRule.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		for (final EndRule endRule : endRules)
			readEvalContext.or(endRule.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		for (final EndRule endRule : endRules)
			endRule.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		for (final EndRule endRule : endRules)
			missingRequirement |= endRule.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		for (final EndRule endRule : endRules)
			willCrash |= endRule.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	/**
	 * set this ending rules to a match.
	 *
	 * @param value
	 */
	public void setMatch(final boolean value)
	{
		match = value;
	}

	//-------------------------------------------------------------------------
	// Ludeme overrides
	
	@Override
	public String toEnglish(final Game game)
	{
		return "<End>";
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "[End: " + Arrays.toString(endRules) + "]";
	}

	//-------------------------------------------------------------------------

}
