package game.rules.end;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.board.Id;
import game.types.state.GameType;
import game.util.end.Score;
import main.Status;
import other.concept.Concept;
import other.context.Context;
import other.trial.Trial;

/**
 * Is used to end a game based on the score of each player.
 * 
 * @author Eric.Piette and cambolbro
 */
public class ByScore extends Result
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** To compute the scores. */
	final private Score[] finalScore;

	/** Misere version. */
	private final BooleanFunction misereFn;

	//-------------------------------------------------------------------------

	/**
	 * @param finalScore The final score of each player.
	 * @param misere     Misere version of the ludeme [False].
	 * @example (byScore)
	 */
	public ByScore
	(
		@Opt           final Score[] finalScore,
		@Opt     @Name final BooleanFunction    misere
	)
	{
		super(null, null);
		this.finalScore = finalScore;
		this.misereFn   = (misere == null) ? new BooleanConstant(false) : misere;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		final Trial trial = context.trial();
		final boolean misere = misereFn.eval(context);
		
		if (finalScore != null)
		{
			for (int i = 0; i < finalScore.length; i++)
			{
				final Score score = finalScore[i];
				final int pid = new Id(null, score.role()).eval(context);
				final int scoreToSet = score.score().eval(context);
				context.setScore(pid, scoreToSet);
			}
		}
			 
		final int numPlayers = context.game().players().count();

		context.setAllInactive();

		final int[] allScores = new int[numPlayers + 1];

		for (int pid = 1; pid < allScores.length; pid++)
		{
//			System.out.println("Player " + pid + " has score " + context.score(pid) + ".");
			allScores[pid] = context.score(pid);
		}
			
		// Keep assigning ranks until everyone got a rank
		int numAssignedRanks = 0;
		
		if(!misere)
		{
			while (true)
			{
				int maxScore = Integer.MIN_VALUE;
				int numMax = 0;
	
				// Detection of the max score
				for (int p = 1; p < allScores.length; p++)
				{
					final int score = allScores[p];
					if (score > maxScore)
					{
						maxScore = score;
						numMax = 1;
					}
					else if (score == maxScore)
					{
						++numMax;
					}
				}
					
				if (maxScore == Integer.MIN_VALUE) // We've assigned players to every rank
					break;
	
				final double nextWinRank = ((numAssignedRanks + 1.0) * 2.0 + numMax - 1.0) / 2.0;
				assert(nextWinRank >= 1.0 && nextWinRank <= context.trial().ranking().length);
	
				for (int p = 1; p < allScores.length; p++)
				{
					if (maxScore == allScores[p])
					{
						context.trial().ranking()[p] = nextWinRank;
						allScores[p] = Integer.MIN_VALUE;
					}
				}
				
				numAssignedRanks += numMax;
			}
		}
		else
		{
			while (true)
			{
				int minScore = Integer.MAX_VALUE;
				int numMin = 0;
	
				// Detection of the min score
				for (int p = 1; p < allScores.length; p++)
				{
					final int score = allScores[p];
					if (score < minScore)
					{
						minScore = score;
						numMin = 1;
					}
					else if (score == minScore)
					{
						++numMin;
					}
				}
					
				if (minScore == Integer.MAX_VALUE) // We've assigned players to every rank
					break;
	
				final double nextWinRank = ((numAssignedRanks + 1.0) * 2.0 + numMin - 1.0) / 2.0;
				assert(nextWinRank >= 1.0 && nextWinRank <= context.trial().ranking().length);
	
				for (int p = 1; p < allScores.length; p++)
				{
					if (minScore == allScores[p])
					{
						context.trial().ranking()[p] = nextWinRank;
						allScores[p] = Integer.MAX_VALUE;
					}
				}
				
				numAssignedRanks += numMin;
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
		
		gameFlags |= GameType.Score;
		
		if (finalScore != null)
			for (final Score fScore : finalScore)
				gameFlags |= fScore.gameFlags(game);
		
		gameFlags |= misereFn.gameFlags(game);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Scoring.id(), true);
		concepts.set(Concept.ScoringEnd.id(), true);
		concepts.set(Concept.ScoringWin.id(), true);

		if (finalScore != null)
			for (final Score fScore : finalScore)
				concepts.or(fScore.concepts(game));

		if (concepts.get(Concept.Territory.id()))
		{
			concepts.set(Concept.TerritoryEnd.id(), true);
			concepts.set(Concept.TerritoryWin.id(), true);
		}
		
		concepts.or(misereFn.concepts(game));

		return concepts;
	}
	
	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (finalScore != null)
			for (final Score fScore : finalScore)
				writeEvalContext.or(fScore.writesEvalContextRecursive());
		
		writeEvalContext.or(misereFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (finalScore != null)
			for (final Score fScore : finalScore)
				readEvalContext.or(fScore.readsEvalContextRecursive());
		
		readEvalContext.or(misereFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (finalScore != null)
			for (final Score fScore : finalScore)
				missingRequirement |= fScore.missingRequirement(game);
		
		missingRequirement |= misereFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (finalScore != null)
			for (final Score fScore : finalScore)
				willCrash |= fScore.willCrash(game);

		willCrash |= misereFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (finalScore != null)
			for (final Score fScore : finalScore)
				fScore.preprocess(game);

		misereFn.preprocess(game);
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		final boolean misere = misereFn.eval(new Context(game, new Trial(game)));
		
		if(misere)
			return "the game ends and the player with the lowest score wins";
		else
			return "the game ends and the player with the highest score wins";
	}
}
