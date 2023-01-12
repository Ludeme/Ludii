package game.rules.end;

import java.util.BitSet;

import game.Game;
import game.functions.ints.board.Id;
import game.types.state.GameType;
import game.util.end.Payoff;
import main.Status;
import other.context.Context;
import other.trial.Trial;

/**
 * Is used to end a game based on the payoff of each player.
 * 
 * @author Eric.Piette
 */
public class Payoffs extends Result
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** To compute the scores. */
	final private Payoff[] finalPayoff;

	//-------------------------------------------------------------------------

	/**
	 * @param finalPayoffs The final score of each player.
	 * @example (payoffs {(payoff P1 5.5) (payoff P2 1.2)})
	 */
	public Payoffs
	(
		final Payoff[] finalPayoffs
	)
	{
		super(null, null);
		this.finalPayoff = finalPayoffs;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		final Trial trial = context.trial();
		
		if (finalPayoff != null)
		{
			for (int i = 0; i < finalPayoff.length; i++)
			{
				final Payoff payoff = finalPayoff[i];
				final int pid = new Id(null, payoff.role()).eval(context);
				final double payoffToSet = payoff.payoff().eval(context);
				context.setPayoff(pid, payoffToSet);
			}
		}
			 
		final int numPlayers = context.game().players().count();

		context.setAllInactive();

		final double[] allPayoffs = new double[numPlayers + 1];

		for (int pid = 1; pid < allPayoffs.length; pid++)
		{
//			System.out.println("Player " + pid + " has score " + context.score(pid) + ".");
			allPayoffs[pid] = context.payoff(pid);
		}
		
		if (numPlayers == 1)
		{
			// Special case: "lose" if payoff <= 0, "win" otherwise
			context.trial().ranking()[1] = (allPayoffs[1] <= 0.0) ? 0.0 : 1.0;
		}
		else
		{
			// Keep assigning ranks until everyone got a rank
			int numAssignedRanks = 0;
			
			while (true)
			{
				double maxPayoff = Integer.MIN_VALUE;
				int numMax = 0;
	
				// Detection of the max score
				for (int p = 1; p < allPayoffs.length; p++)
				{
					final double payoff = allPayoffs[p];
					if (payoff > maxPayoff)
					{
						maxPayoff = payoff;
						numMax = 1;
					}
					else if (payoff == maxPayoff)
					{
						++numMax;
					}
				}
					
				if (maxPayoff == Integer.MIN_VALUE) // We've assigned players to every rank
					break;
	
				final double nextWinRank = ((numAssignedRanks + 1.0) * 2.0 + numMax - 1.0) / 2.0;
				assert(nextWinRank >= 1.0 && nextWinRank <= context.trial().ranking().length);
	
				for (int p = 1; p < allPayoffs.length; p++)
				{
					if (maxPayoff == allPayoffs[p])
					{
						context.trial().ranking()[p] = nextWinRank;
						allPayoffs[p] = Integer.MIN_VALUE;
					}
				}
				
				numAssignedRanks += numMax;
			}
		}
			
		// Set status (with winner if someone has full rank 1.0)
		int winner = 0;
		int loser = 0;
		for (int p = 1; p < context.trial().ranking().length; ++p)
		{
			if (context.trial().ranking()[p] == 1.0)
				winner = p;
			else if (context.trial().ranking()[p] == context.trial().ranking().length)
				loser = p;
		}

		if (winner > 0)
			context.addWinner(winner);
		if (loser > 0)
			context.addLoser(loser);
		trial.setStatus(new Status(winner));
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0L;
		
		gameFlags |= GameType.Payoff;
		
		if (finalPayoff != null)
			for (final Payoff fPayoff : finalPayoff)
				gameFlags |= fPayoff.gameFlags(game);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		if (finalPayoff != null)
			for (final Payoff fPayoff : finalPayoff)
				concepts.or(fPayoff.concepts(game));

		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (finalPayoff != null)
			for (final Payoff fPayoff : finalPayoff)
				writeEvalContext.or(fPayoff.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (finalPayoff != null)
			for (final Payoff fPayoff : finalPayoff)
				readEvalContext.or(fPayoff.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (finalPayoff != null)
			for (final Payoff fPayoff : finalPayoff)
				missingRequirement |= fPayoff.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (finalPayoff != null)
			for (final Payoff fPayoff : finalPayoff)
				willCrash |= fPayoff.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (finalPayoff != null)
			for (final Payoff fPayoff : finalPayoff)
				fPayoff.preprocess(game);
	}
}
