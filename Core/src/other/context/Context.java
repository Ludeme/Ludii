package other.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.rng.core.source64.SplitMix64;

import game.Game;
import game.equipment.Equipment;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.equipment.container.board.Board;
import game.equipment.container.board.Track;
import game.equipment.container.other.Dice;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import game.functions.region.RegionFunction;
import game.match.Subgame;
import game.players.Player;
import game.rules.Rules;
import game.rules.end.End;
import game.rules.play.moves.Moves;
import game.types.play.RoleType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import main.Constants;
import main.math.BitTwiddling;
import metadata.Metadata;
import other.GameLoader;
import other.UndoData;
import other.model.MatchModel;
import other.model.Model;
import other.move.Move;
import other.state.State;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.trial.Trial; 

/**
 * Context for generating moves during playouts.
 *
 * @author cambolbro and Eric Piette
 */
public class Context
{
	//-------------------------------------------------------------------------

	/**
	 * A single RNG which is shared by all Context objects created through
	 * copy-constructor.
	 *
	 * Such Context objects should only be created by search algorithms such as
	 * MCTS, and for those we do not need reproducibility.
	 */
	private static SplitMix64 sharedRNG = new SplitMix64();

	//-------------------------------------------------------------------------

	/** Reference to controlling game object. */
	private final Game game;
	
	/** Reference to "parent" Context of match we're in. Will be null if this is the top-level Context. */
	private Context parentContext;
	
	/** Reference to active subcontext. Will be null if this is not a context for a Match */
	private Context subcontext;
	
	/** Current game state. */
	protected transient State state;
	
	/** Index for our current subgame (will always be 0 for non-Matches) */
	private int currentSubgameIdx = 0;

	/** Our control flow models (one per phase) */
	private Model[] models;

	/** Our Trial. */
	private Trial trial;
	
	/** 
	 * List of trials that have already been completed.
	 * 
	 * Will only be non-empty for Matches.		TODO should probably just have it be null for instances then
	 */
	private List<Trial> completedTrials;

	//-------------------------------------------------------------------------

	/** RNG object used for any rules / actions / etc. in this context */
	private SplitMix64 rng;

	//-------------------------------------------------------------------------

	/** Data used to evaluate ludemes */
	private EvalContext evalContext = new EvalContext();

	//-------------------------------------------------------------------------

	/** Require for loop */
	//private boolean ringFlagCalled = false;

	/** Used in case of recursive called for some ludeme. */
	private boolean recursiveCalled = false;

	/**
	 * Data used during the computation of the ranking in case of multi results in
	 * the same turn. (we need a better name for that variable but I am too tired to
	 * find one ^^)			TODO officially I guess this should actually be in EvalContext?
	 */
	private int numLossesDecided = 0;
	
	/** Same as above, but for wins */		// TODO officially I guess this should actually be in EvalContext?
	private int numWinsDecided = 0;
	
	// WARNING: if we have a State object, that object should perform modifications of the below fields for us!
	// this allows it to also update Zobrist hash!
	
	/** Scores per player. Game scores if this is a trial for just a game, match scores if it's a trial for a Match */
	private int[] scores;
	
	/**
	 * Payoffs per player. Game payoff if this is a trial for just a game, match
	 * scores if it's a trial for a Match
	 */
	private double[] payoffs;

	/** For every player, a bit indicating whether they are active */
	private int active = 0;
	
	// WARNING: if we have a State object, that object should perform modifications of the above fields for us!
	// this allows it to also update Zobrist hash!
	
	/** List of players who've already won */
	private TIntArrayList winners;
	
	/** List of players who've already lost */
	private TIntArrayList losers;
	
	/** Tells us whether we've ever called game.start() with this context */
	private boolean haveStarted = false;
	
	/** The states of each site where is a die */
	final TIntIntHashMap diceSiteStates;

	//-------------------------------------------------------------------------
	
	/** Lock for Game methods that should not be executed in parallel on the same Context object. */
	private transient ReentrantLock lock = new ReentrantLock();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructs a new Context for given game and trial
	 *
	 * @param game
	 * @param trial
	 */
	public Context(final Game game, final Trial trial)
	{
		this(game, trial, new SplitMix64(), null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param game
	 * @param trial
	 * @param rng
	 * @param parentContext
	 */
	private Context(final Game game, final Trial trial, final SplitMix64 rng, final Context parentContext)
	{
		this.game = game;
		this.parentContext = parentContext;
		diceSiteStates = new TIntIntHashMap();
		this.trial = trial;
		completedTrials = new ArrayList<Trial>(1);
		this.rng = rng;
		
		if (game.hasSubgames())
		{
			// This is a Context for a Match
			state = null;
			final Game subgame = game.instances()[0].getGame();
			subcontext = new Context(subgame, new Trial(subgame), rng, this);	// plug in the same RNG for complete Match
			
			models = new Model[1];	// Assuming no phases in matches
			models[0] = new MatchModel();
		}
		else
		{
			// This is a Context for just a single Game
			state = game.stateReference() != null ? new State(game.stateReference()) : null;
			subcontext = null;
			
			models = new Model[game.rules().phases().length];
			
			for (int i = 0; i < game.rules().phases().length; ++i)
			{
				if (game.rules().phases()[i].mode() != null)
					models[i] = game.rules().phases()[i].mode().createModel();
				else
					models[i] = game.mode().createModel();
			}
		}
		
		if (game.requiresScore())
			scores = new int[game.players().count() + 1];
		else
			scores = null;
		
		if (game.requiresPayoff())
			payoffs = new double[game.players().count() + 1];
		else
			payoffs = null;

		for (int p = 1; p <= game.players().count(); ++p)
			setActive(p, true);
		
		winners = new TIntArrayList(game.players().count());
		losers = new TIntArrayList(game.players().count());
	}

	/**
	 * Copy constructor, which creates a deep copy of the Trial (except for ignoring
	 * its history), but only copies the reference to the game. Intended for search
	 * algorithms, which can safely apply moves on these copies without modifying
	 * the original context.
	 *
	 * @param other
	 */
	public Context(final Context other)
	{
		this(other, null);
	}
	
	/**
	 * @param other
	 * @return A copy of the given other context, with a new RNG that has
	 * 	a copied interal state (i.e., seed etc.)
	 */
	public static Context copyWithSeed(final Context other)
	{
		final Context copy = new Context(other, null, new SplitMix64());
		copy.rng.restoreState(other.rng.saveState());
		return copy;
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param other
	 * @param otherParentCopy Copy of the parent context of the given other
	 */
	private Context(final Context other, final Context otherParentCopy)
	{
		// Pass shared RNG, don't expect to need reproducibility
		this(other, otherParentCopy, sharedRNG);
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param other
	 * @param otherParentCopy Copy of the parent context of the given other
	 * @param rng The RNG to use for the copy
	 */
	private Context(final Context other, final Context otherParentCopy, final SplitMix64 rng)
	{
		other.getLock().lock();
		
		try
		{
			game = other.game;
			parentContext = otherParentCopy;
			diceSiteStates = new TIntIntHashMap();
			state = copyState(other.state);
			trial = copyTrial(other.trial);
			
			// WARNING: Currently just copying the completed trials by reference here
			// TODO:    Would actually want these trials to become immutable somehow...
			//		    Would add a level of safety but is not critical (to do when time permits).
			completedTrials = new ArrayList<Trial>(other.completedTrials);
			
			this.rng = rng;
			
			subcontext = other.subcontext == null ? null : new Context(other.subcontext, this);
			currentSubgameIdx = other.currentSubgameIdx;
			
			models = new Model[other.models.length];
			for (int i = 0; i < models.length; ++i)
			{
				models[i] = other.models[i].copy();
			}
			
			evalContext = new EvalContext(other.evalContext());
			
			numLossesDecided = other.numLossesDecided;
			numWinsDecided = other.numWinsDecided;
	
			//ringFlagCalled = other.ringFlagCalled;
			recursiveCalled = other.recursiveCalled;
		
			if (other.scores != null)
				scores = Arrays.copyOf(other.scores, other.scores.length);
			else
				scores = null;
			
			if (other.payoffs != null)
				payoffs = Arrays.copyOf(other.payoffs, other.payoffs.length);
			else
				payoffs = null;

			active = other.active;
			
			winners = new TIntArrayList(other.winners);
			losers = new TIntArrayList(other.losers);
		}
		finally
		{
			other.getLock().unlock();
		}
	}
	
	/**
	 * @return The object used to evalutate the ludemes.
	 */
	public EvalContext evalContext()
	{
		return evalContext;
	}

	/**
	 * Method for copying game states. NOTE: we override this in TempContext for
	 * copy-on-write states.
	 * 
	 * @param otherState
	 * @return Copy of given game state.
	 */
	@SuppressWarnings("static-method")
	protected State copyState(final State otherState)
	{
		return otherState == null ? null : new State(otherState);
	}
	
	/**
	 * Method for copying Trials. NOTE: we override this in TempContext
	 * for Trial copies with MoveSequences that are allowed to be
	 * invalidated.
	 * 
	 * @param otherTrial
	 * @return Copy of given Trial
	 */
	@SuppressWarnings("static-method")
	protected Trial copyTrial(final Trial otherTrial)
	{
		return new Trial(otherTrial);
	}
	
	/**
	 * NOTE: we don't really need this method to exist in Java, but this is
	 * much more convenient to call from Python than directly calling the
	 * copy constructor.
	 * 
	 * @return Deep copy of this context.
	 */
	public Context deepCopy()
	{
		return new Context(this, null);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Reset this Context to play again from the start.
	 */
	public void reset()
	{
		// **
		// ** Don't clear state here. Calling function should copy
		// ** the reference state stored in the Game object.
		// **
		
		if (state != null)
			state.resetStateTo(game.stateReference(), game);
		
		trial.reset(game);
		
		if (scores != null)
			Arrays.fill(scores, 0);
		
		if (payoffs != null)
			Arrays.fill(payoffs, 0);

		active = 0;
		for (int p = 1; p <= game.players().count(); ++p)
			setActive(p, true);
		
		winners.reset();
		losers.reset();
		haveStarted = true;
		
		if (subcontext != null)
		{
			final Game subgame = game.instances()[0].getGame();
			subcontext = new Context(subgame, new Trial(subgame), rng, this);	// plug in the same RNG for complete Match
			completedTrials.clear();
		}
		
		currentSubgameIdx = 0;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * To set the active bits.
	 * @param active For each player a bit to indicate if a player is active.
	 */
	public void setActive(final int active)
	{
		this.active = active;
	}
	
	/**
	 * @param who
	 * @return Whether player is active.
	 */
	public boolean active(final int who)
	{
		return (active & (1 << (who - 1))) != 0;
	}
	
	/**
	 * @return If only one player is active, we return the id of that player. Otherwise 0
	 */
	public int onlyOneActive()
	{
		if (BitTwiddling.exactlyOneBitSet(active))
			return BitTwiddling.lowBitPos(active) + 1;
		
		return 0;
	}
	
	/**
	 * @return if only one team is active, we return the id of that team. Otherwise 0
	 */
	public int onlyOneTeamActive()
	{
		final TIntArrayList activePlayers = new TIntArrayList();
		for (int i = 1; i <= game.players().count(); i++)
			if (active(i))
				activePlayers.add(i);

		final TIntArrayList activeTeam = new TIntArrayList();
		for (int i = 0; i < activePlayers.size(); i++)
		{
			final int pid = activePlayers.getQuick(i);
			final int tid = state.getTeam(pid);
			if (!activeTeam.contains(tid))
				activeTeam.add(tid);
		}

		if (activeTeam.size() != 1)
			return 0;
		return activeTeam.getQuick(0);
	}
	
	/**
	 * To add a winner in the list of winners.
	 * NOTE: important to call this AFTER calling computeNextWinRank(), if
	 * the intention is to also call that to compute the rank for this player
	 * 
	 * @param idPlayer
	 */
	public void addWinner(final int idPlayer)
	{
		winners.add(idPlayer);
	}
	
	/**
	 * To add a loser in the list of losers.
	 * @param idPlayer
	 */
	public void addLoser(final int idPlayer)
	{
		losers.add(idPlayer);
	}

	/**
	 * @return The number of winners.
	 */
	public int numWinners()
	{
		return winners.size();
	}
	
	/**
	 * @return The number of losers.
	 */
	public int numLosers()
	{
		return losers.size();
	}

	/**
	 * @return The winners.
	 */
	public TIntArrayList winners()
	{
		return winners;
	}
	
	/**
	 * @return The losers.
	 */
	public TIntArrayList losers()
	{
		return losers;
	}
	
	/**
	 * @return The array of scores.
	 */
	public int[] scores()
	{
		return this.scores;
	}
	
	/**
	 * @param pid
	 * @return Current score for player with given Player ID
	 */
	public int score(final int pid)
	{
		return scores[pid];
	}
	
	/**
	 * @return The array of payoffs.
	 */
	public double[] payoffs()
	{
		return this.payoffs;
	}
	
	/**
	 * @param pid
	 * @return Current payoff for player with given Player ID
	 */
	public double payoff(final int pid)
	{
		return payoffs[pid];
	}

	/**
	 * Sets the payoff for the given player
	 * 
	 * @param pid         Player ID
	 * @param payoffToSet New payoff
	 */
	public void setPayoff(final int pid, final double payoffToSet)
	{
		if (state != null)		// Let State do it so it can also update Zobrist hash!
			state.setPayoff(pid, payoffToSet, payoffs);
		else
			payoffs[pid] = payoffToSet;
	}
	
	/**
	 * Sets the score for the given player
	 * @param pid Player ID
	 * @param scoreToSet New score
	 */
	public void setScore(final int pid, final int scoreToSet)
	{
		if (state != null)		// Let State do it so it can also update Zobrist hash!
			state.setScore(pid, scoreToSet, scores);
		else
			scores[pid] = scoreToSet;
	}
	
	/**
	 * Sets a player to be active or inactive.
	 *
	 * @param who
	 * @param newActive
	 */
	public void setActive(final int who, final boolean newActive)
	{
		if (state != null)		// Let State do it so it can also update Zobrist hash!
		{
			active = state.setActive(who, newActive, active);
		}
		else
		{
			final int whoBit = (1 << (who - 1));
			final boolean wasActive = (active & whoBit) != 0;
			
			if (wasActive && !newActive)
				active &= ~whoBit;
			else if (!wasActive && newActive)
				active |= whoBit;
		}
	}
	
	/**
	 * @return Whether any player is active
	 */
	public boolean active()
	{
		return active != 0;
	}
	
	/**
	 * Set all players to "inactive".
	 */
	public void setAllInactive()
	{
		active = 0;
		
		if (state != null)
			state.updateHashAllPlayersInactive();
	}
	
	/**
	 * @return The number of active players.
	 */
	public int numActive()
	{
		return Integer.bitCount(active);
	}
	
	/**
	 * @return Next rank to assign to players who obtain a win now.
	 */
	public double computeNextWinRank()
	{
		final int numWinRanksTaken = numWinners();
		return numWinRanksTaken + 1;
	}
	
	/**
	 * @return Next rank to assign to players who obtain a loss now.
	 */
	public double computeNextLossRank()
	{
		final int numRanks = trial.ranking().length - 1;
		final int numLossRanksTaken = numLosers();
		return numRanks - numLossRanksTaken;
	}
	
	/**
	 * @return Rank to assign to remaining players if we were to obtain a draw now.
	 */
	public double computeNextDrawRank()
	{
		return (numActive() + 1) / 2.0 + (numWinners());
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Game.
	 */
	public Game game()
	{
		return game;
	}
	
	/**
	 * @return True if game being played is a Match. 
	 */
	public boolean isAMatch()
	{
		return game.hasSubgames();
	}

	/**
	 * @return Our control flow model.
	 */
	public Model model()
	{
		if (models.length == 1)
			return models[0];
		else
			return models[state.currentPhase(state.mover())];
	}

	/**
	 * @return Shortcut reference to list of players.
	 */
	public List<Player> players()
	{
		return game.players().players();
	}

	/**
	 * @return Trial.
	 */
	public Trial trial()
	{
		return trial;
	}
	
	/**
	 * @return Current subcontext in the case of Matches. Will be null if this is
	 * 	already a context for just an instance.
	 */
	public Context subcontext()
	{
		return subcontext;
	}
	
	/**
	 * @return The context for the current instance. May just be this context if it's
	 * 	already not a context for a Match.
	 */
	public Context currentInstanceContext()
	{
		Context context = this;
		
		while (context.isAMatch())
		{
			context = context.subcontext();
		}
		
		return context;
	}

	/**
	 * @return Random Number Generator for this context.
	 */
	public SplitMix64 rng()
	{
		return rng;
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @return Team array.
	 */
	public int[] team()
	{
		return evalContext.team();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @param team The team..
	 */
	public void setTeam(int[] team)
	{
		evalContext.setTeam(team);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @return From index.
	 */
	public int from()
	{
		return evalContext.from();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @param val The value.
	 */
	public void setTrack(final int val)
	{
		evalContext.setTrack(val);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @return track index.
	 */
	public int track()
	{
		return evalContext.track();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @param val
	 */
	public void setFrom(final int val)
	{
		evalContext.setFrom(val);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls 
	 * @return To index.
	 */
	public int to()
	{
		return evalContext.to();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @param val
	 */
	public void setTo(final int val)
	{
		evalContext.setTo(val);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls 
	 * @return between index.
	 */
	public int between()
	{
		return evalContext.between();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls To set the
	 * between value.
	 * 
	 * @param val
	 */
	public void setBetween(final int val)
	{
		evalContext.setBetween(val);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @return player index.
	 */
	public int player()
	{
		return evalContext.player();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls To set the
	 * player value.
	 * 
	 * @param val
	 */
	public void setPlayer(final int val)
	{
		evalContext.setPlayer(val);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls 
	 * @return dieValue.
	 */
	public int pipCount()
	{
		return evalContext.pipCount();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @param val
	 */
	public void setPipCount(final int val)
	{
		evalContext.setPipCount(val);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls 
	 * @return Level index.
	 */
	public int level()
	{
		return evalContext.level();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @param val
	 */
	public void setLevel(final int val)
	{
		evalContext.setLevel(val);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls 
	 * @return Hint index.
	 */
	public int hint()
	{
		return evalContext.hint();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @param val
	 */
	public void setHint(final int val)
	{
		evalContext.setHint(val);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls 
	 * @return Edge index.
	 */
	public int edge()
	{
		return evalContext.edge();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @param val
	 */
	public void setEdge(final int val)
	{
		evalContext.setEdge(val);
	}
	
	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @return site index.
	 */
	public int site()
	{
		return evalContext.site();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls To set the site
	 * value.
	 * 
	 * @param val
	 */
	public void setSite(final int val)
	{
		evalContext.setSite(val);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @return value.
	 */
	public int value()
	{
		return evalContext.value();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls To set the
	 * value.
	 * 
	 * @param val
	 */
	public void setValue(final int val)
	{
		evalContext.setValue(val);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @return A region iterated.
	 */
	public Region region()
	{
		return evalContext.region();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * To set the region iterated.
	 * 
	 * @param region The region.
	 */
	public void setRegion(final Region region)
	{
		evalContext.setRegion(region);
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * @return The hint region iterated in the
	 *         game.functions.booleans.deductionPuzzle.ForEach or called by (sites
	 *         Hint).
	 */
	public RegionFunction hintRegion()
	{
		return evalContext.hintRegion();
	}

	/**
	 * WARNING: Should NOT be used outside of Ludemes' eval() calls
	 * 
	 * To set the hint region when we iterate them in
	 * game.functions.booleans.deductionPuzzle.ForEach.
	 * 
	 * @param region The region.
	 */
	public void setHintRegion(final RegionFunction region)
	{
		evalContext.setHintRegion(region);
	}

	/**
	 * @return Number of times we applied loss End result in a single apply()
	 */
	public int numLossesDecided()
	{
		return numLossesDecided;
	}
	
	/**
	 * @return Number of times we applied win End result in a single apply()
	 */
	public int numWinsDecided()
	{
		return numWinsDecided;
	}
	
	/**
	 * Set the number of times we applied loss End result in a single ForEach apply()
	 * 
	 * @param numLossesDecided
	 */
	public void setNumLossesDecided(final int numLossesDecided)
	{
		this.numLossesDecided = numLossesDecided;
	}
	
	/**
	 * Set the number of times we applied win End result in a single ForEach apply()
	 * 
	 * @param numWinsDecided
	 */
	public void setNumWinsDecided(final int numWinsDecided)
	{
		this.numWinsDecided = numWinsDecided;
	}
	
	/**
	 * @return Tells us whether we've ever called game.start() with this context
	 */
	public boolean haveStarted()
	{
		return haveStarted;
	}

	//-------------------------------------------------------------------------
	
//	/**
//	 * @return The ring flag.
//	 */
//	public boolean ringFlagCalled()
//	{
//		return ringFlagCalled;
//	}
//	
//	/**
//	 * Set the ring flag.
//	 * 
//	 * @param called
//	 */
//	public void setRingFlagCalled(final boolean called)
//	{
//		ringFlagCalled = called;
//	}

	
	/**
	 * @return Reference to containers list.
	 */
	public Container[] containers()
	{
		if (subcontext != null)
			return subcontext.containers();

		return game.equipment().containers();
	}

	/**
	 * @return Reference to components list.
	 */
	public Component[] components()
	{
		if (subcontext != null)
			return subcontext.components();

		return game.equipment().components();
	}

	/**
	 * @return Reference to track list.
	 */
	public List<Track> tracks() 
	{
		if (subcontext != null)
			return subcontext.tracks();

		return game.board().tracks();
	}

	/**
	 * @return Reference to region list.
	 */
	public game.equipment.other.Regions[] regions()
	{
		if (subcontext != null)
			return subcontext.regions();

		return game.equipment().regions();
	}

	/**
	 * @return Reference to the containerId.
	 */
	public int[] containerId()
	{
		if (subcontext != null)
			return subcontext.containerId();

		return game.equipment().containerId();
	}

	/**
	 * @return Reference to the sitesFrom.
	 */
	public int[] sitesFrom()
	{
		if (subcontext != null)
			return subcontext.sitesFrom();

		return game.equipment().sitesFrom();
	}

	/**
	 * @return Reference to main board.
	 */
	public Board board()
	{
		if (subcontext != null)
			return subcontext.board();

		return game.board();
	}
	
	/**
	 * @return Reference to main board.
	 */
	public Rules rules()
	{
		if (subcontext != null)
			return subcontext.rules();

		return game.rules();
	}
	
	/**
	 * @return Metadata of game we're currently playing in this context.
	 */
	public Metadata metadata()
	{
		if (subcontext != null)
			return subcontext.metadata();
		
		return game.metadata();
	}
	
	/**
	 * @return Equipment of the game we're currently playing in this context.
	 */
	public Equipment equipment()
	{
		if (subcontext != null)
			return subcontext.equipment();
		
		return game.equipment();
	}
	
	/**
	 * @return The dice hands of the game we're currently playing in this context
	 */
	public List<Dice> handDice()
	{
		if (subcontext != null)
			return subcontext.handDice();
		
		return game.handDice();
	}
	
	/**
	 * @return Reference to main board graph.
	 */
	public Topology topology()
	{
		if (subcontext != null)
			return subcontext.topology();

		return game.board().topology();
	}
	
	/**
	 * @return True if the game we're currently player has any containers owned by the Shared player
	 */
	public boolean hasSharedPlayer()
	{
		if (subcontext != null)
			return subcontext.hasSharedPlayer();
		
		return game.hasSharedPlayer();
	}
	
	/**
	 * @return Number of distinct containers in the game we're currently playing.
	 */
	public int numContainers()
	{
		if (subcontext != null)
			return subcontext.numContainers();
		
		return game.numContainers();
	}

	/**
	 * @return Number of distinct components in the game we're currently playing.
	 */
	public int numComponents()
	{
		if (subcontext != null)
			return subcontext.numComponents();
		
		return game.numComponents();
	}

	/**
	 * @return Reference to current state.
	 */
	public State state()
	{
		if (subcontext != null)
			return subcontext.state();

		return state;
	}

	/**
	 * Helper method to return player name for given player index. Takes into
	 * account whether or not player roles have been swapped in the current state.
	 *
	 * @param p
	 * @return Player name
	 */
	public String getPlayerName(final int p)
	{
		if (subcontext != null)
			return subcontext.getPlayerName(p);

		return game.players().players().get(state().playerToAgent(p)).name();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Whether all players passed in the last round.
	 */
	public boolean allPass()
	{
		if (subcontext != null)
			return subcontext.allPass();
		
		final int numPlayers = game.players().count();
		final Iterator<Move> reverseMoves = trial.reverseMoveIterator();
		
		int lastMover = state().mover();

		if (numPlayers == 1)
			return trial.lastMove().isPass();

		boolean passMove = false;
		int countMovesTurn = 0;
		
		for (int i = 1; i <= numPlayers; i++) // we look the previous turn of each player.
		{
			while (true) // We need to check each previous turn of each player.
			{
				if (!reverseMoves.hasNext()) // Not enough moves to check if all players passed.
				{
					if (!passMove) // First move of the next turn was not a pass.
						return false;
					return true;
				}

				if (countMovesTurn > 1) // we look only for full round with only a pass move.
					return false;

				final Move move = reverseMoves.next();

				if (lastMover != move.mover()) // That's a new turn.
				{
					if (!passMove) // First move of the next turn was not a pass.
						return false;

					lastMover = move.mover(); // get the mover of this new turn.
					countMovesTurn = 0; // we init the counter.
					passMove = move.isPass(); // we init the test on passMove
					break;
				}

				countMovesTurn++;
				passMove = move.isPass();
			}
		}

		return true;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Our direct parent context (null if we're already the top-level context)
	 */
	public Context parentContext()
	{
		return parentContext;
	}
	
	/**
	 * @return List of completed trials within this context.
	 */
	public List<Trial> completedTrials()
	{
		return completedTrials;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param role
	 * @return A list of the index of the player in function of the role type.
	 */

	public TIntArrayList convertRole(final RoleType role)
	{
		TIntArrayList indexPlayer = new TIntArrayList();
		final int moverId = state.mover();

		if (role == RoleType.Enemy)
		{
			indexPlayer = game.players().players().get(moverId).enemies();
		}
		else if (role == RoleType.Shared)
		{
			for (int i = 1; i <= game.players().count(); i++)
				indexPlayer.add(i);
		}
		else
		{
			indexPlayer.add(new Id(null, role).eval(this));
		}

		return indexPlayer;
	}

	/**
	 * @param cid
	 * @return The ItemStateContainer of the container cid.
	 */
	public ContainerState containerState(final int cid)
	{
		if (subcontext != null)
			return subcontext.containerState(cid);

		return state().containerStates()[cid];
	}

	/**
	 * @return the recursiveCalled value
	 */
	public boolean recursiveCalled()
	{
		return recursiveCalled;
	}

	/**
	 * To set the value of the recursiveCalled
	 *
	 * @param value
	 */
	public void setRecursiveCalled(final boolean value)
	{
		recursiveCalled = value;
	}
	
	/**
	 * @return The from location of the first move of the current turn.
	 */
	public int fromStartOfTurn()
	{
		if (trial.numMoves() == 0)
			return Constants.UNDEFINED;
		
		final Iterator<Move> reverseMoves = trial.reverseMoveIterator();

		final int mover = state.mover();
		
		Move currMove = reverseMoves.next();

		// If the current state corresponds to a new turn the from is not defined.
		if (mover != currMove.mover())
			return Constants.UNDEFINED;

		int fromStartOfTurn = currMove.fromNonDecision();
		while (reverseMoves.hasNext())
		{
			currMove = reverseMoves.next();
			
			if (currMove.mover() != mover)
				break;
			
			fromStartOfTurn = currMove.fromNonDecision();
		}
		
		return fromStartOfTurn;
	}
	
	/**
	 * @return Index of our current Subgame (always 0 for non-Match games).
	 */
	public int currentSubgameIdx()
	{
		return currentSubgameIdx;
	}
	
	/**
	 * Advance this context to the next instance in a multi-game match.
	 */
	public void advanceInstance()
	{
		final int numPlayers = trial.ranking().length - 1;
		final Subgame currentInstance = game.instances()[currentSubgameIdx];
		
		for (int p = 1; p <= numPlayers; p++)
		{
			final int currentMatchScore = score(p);
			final int scoreToAdd;

			if (currentInstance.result() != null && subcontext.winners.contains(p))
			{
				scoreToAdd = currentInstance.result().eval(subcontext);
			}
			else
			{
				if(numPlayers > 2)
					scoreToAdd = subcontext.winners().contains(p) ? 1 : 0;
				else if (numPlayers == 2)
					scoreToAdd = numPlayers - (int) subcontext.trial().ranking()[p];
				else
					scoreToAdd = (subcontext.trial().ranking()[p] == 1.0) ? 1 : 0;
			}

			setScore(p, currentMatchScore + scoreToAdd);
		}
		
		completedTrials.add(subcontext.trial());

		final End end = game.endRules();
		end.eval(this);
		
		if (!trial.over())
		{
			final IntFunction nextFunc = currentInstance.next();

			if (nextFunc == null)
			{
				if (currentSubgameIdx + 1 >= game.instances().length)
					currentSubgameIdx = 0;
				else
					currentSubgameIdx += 1;
			}
			else
				currentSubgameIdx = nextFunc.eval(this);


			final Subgame nextInstance = game.instances()[currentSubgameIdx];
			//System.out.println("advancing to next instance: " + nextInstance);

			// If next game not compiled we compile it.
			if (nextInstance.getGame() == null)
				GameLoader.compileInstance(nextInstance);

			final Game nextGame = nextInstance.getGame();
			final Trial nextTrial = new Trial(nextGame);
			subcontext = new Context(nextGame, nextTrial, rng, this);
			((MatchModel) model()).resetCurrentInstanceModel();
			
			// TODO set players to be inactive in the trial if they should be inactive from the start
			
			// May have to tell subtrial to store auxiliary data
			if (trial().auxilTrialData() != null)
			{
				if (trial().auxilTrialData().legalMovesHistory() != null)
					nextTrial.storeLegalMovesHistory();
				
				if (trial().auxilTrialData().legalMovesHistorySizes() != null)
					nextTrial.storeLegalMovesHistorySizes();
			}
			
			nextGame.start(subcontext);
		}
		
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Lock for Game methods that should not be executed in parallel 
	 *         on the same Context object.
	 *         Do not call this method "lock()" otherwise we get context.lock().lock();
	 */
	public ReentrantLock getLock()
	{
		return lock;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Set the mover to the index in entry of the method and set the correct
	 * previous and next player according to that index.
	 * 
	 * @param newMover The new mover.
	 */
	public void setMoverAndImpliedPrevAndNext(final int newMover)
	{
		state.setMover(newMover);

		int next = (newMover) % game().players().count() + 1;
		while (!active(next))
		{
			next++;
			if (next > game().players().count())
				next = 1;
		}
		state.setNext(next);

		int prev = (newMover - 1);
		if (prev < 1)
			prev = game().players().count();
		while (!active(prev))
		{
			prev--;
			if (prev < 1)
				prev = game().players().count();
		}
		state.setPrev(prev);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if the game we're current playing is a graph game.
	 */
	public boolean isGraphGame()
	{
		if (subcontext != null)
			return subcontext.isGraphGame();
		
		return game.isGraphGame();
	}

	/**
	 * @return True if the game we're current playing is a vertex game.
	 */
	public boolean isVertexGame()
	{
		if (subcontext != null)
			return subcontext.isVertexGame();
		
		return game.isVertexGame();
	}

	/**
	 * @return True if the game we're current playing is an edge game.
	 */
	public boolean isEdgeGame()
	{
		if (subcontext != null)
			return subcontext.isEdgeGame();
		
		return game.isEdgeGame();
	}

	/**
	 * @return True if the game we're current playing is a cell game.
	 */
	public boolean isCellGame()
	{
		if (subcontext != null)
			return subcontext.isCellGame();
		
		return game.isCellGame();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param context The context.
	 * @return The list of legal moves for that state.
	 */
	@SuppressWarnings("static-method")
	public Moves moves(final Context context)
	{
		return context.game().moves(context);
	}

	/**
	 * @return The id of the player point of view used.
	 */
	public int pointofView()
	{
		return state().mover(); // For the normal context that's always the mover.
	}
	
	/**
	 * NOTE: The RNG seed is NOT reset to the one of the startContext here!
	 * Method used to set the context to another context.
	 * @param context The context to reset to.
	 */
	public void resetToContext(final Context context)
	{
		parentContext = context.parentContext();
		state.resetStateTo(context.state(),game);
		trial.resetToTrial(context.trial());
		
		// WARNING: Currently just copying the completed trials by reference here
		// TODO:    Would actually want these trials to become immutable somehow...
		//		    Would add a level of safety but is not critical (to do when time permits).
		completedTrials = new ArrayList<Trial>(context.completedTrials());
		
		subcontext = context.subcontext() == null ? null : new Context(context.subcontext(), this);
		currentSubgameIdx = context.currentSubgameIdx;
		
		models = new Model[context.models.length];
		for (int i = 0; i < models.length; ++i)
			models[i] = context.models[i].copy();
		
		evalContext = new EvalContext(context.evalContext());
		
		numLossesDecided = context.numLossesDecided;
		numWinsDecided = context.numWinsDecided;

		//ringFlagCalled = other.ringFlagCalled;
		recursiveCalled = context.recursiveCalled();
	
		if (context.scores != null)
			scores = Arrays.copyOf(context.scores, context.scores.length);
		else
			scores = null;
		
		if (context.payoffs != null)
			payoffs = Arrays.copyOf(context.payoffs, context.payoffs.length);
		else
			payoffs = null;

		active = context.active;
		
		winners = new TIntArrayList(context.winners());
		losers = new TIntArrayList(context.losers());
	}
	
	/**
	 * @return A map with key = site of a die, value = state of the die (for GUI only).
	 */
	public TIntIntHashMap diceSiteState()
	{
		return diceSiteStates;
	}
	
	/**
	 * Store the current end data into the trial.
	 */
	public void storeCurrentData()
	{
		// Store the phase of each player.
		final int[] phases = new int[players().size()];
		for(int pid = 1; pid < players().size(); pid++)
			phases[pid] = state().currentPhase(pid);
		
		final UndoData endData = new UndoData(
				trial.ranking(),
				trial.status(), 
				winners, 
				losers, 
				active, 
				scores, 
				payoffs, 
				numLossesDecided, 
				numWinsDecided, 
				phases, 
				state.pendingValues(), 
				state.counter(), 
				trial.previousStateWithinATurn(), 
				trial.previousState(),
				state.prev(),
				state.mover(),
				state.next(),
				state.numTurn(),
				state.numTurnSamePlayer(),
				state.numConsecutivesPasses(),
				state.remainingDominoes(),
				state.visited(),
				state.sitesToRemove(),
				state.onTrackIndices(),
				state.owned(),
				state.isDecided()
		);
		
		trial.addUndoData(endData);
	}
}
