package training.expert_iteration;

import java.io.Serializable;
import java.util.BitSet;

import features.FeatureVector;
import features.feature_sets.BaseFeatureSet;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.state.State;
import training.ExperienceSample;

/**
 * A single sample of experience for Expert Iteration.
 * Contains a trial, a list of actions, a distribution
 * over those actions resulting from an MCTS search process,
 * and value estimates per action as computed by MCTS.
 * 
 * @author Dennis Soemers
 */
public class ExItExperience extends ExperienceSample implements Serializable
{
	
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;
	
	/** Context for which experience was generated (transient, not serialised) */
	protected transient final Context context;
	
	/** Game state (+ last decision move, wrapped in wrapper class) */
	protected final ExItExperienceState state;
	
	/** Legal actions in the game state */
	protected final FastArrayList<Move> moves;
	
	/** Distribution over actions computed by Expert (e.g. MCTS) */
	protected final FVector expertDistribution;
	
	/** Value estimates computed by Expert (e.g. MCTS) */
	protected final FVector expertValueEstimates;
	
	/** Feature vector for state (heuristic terms) */
	protected FVector stateFeatureVector;
	
	/** Duration of full episode in which this experience was generated */
	protected int episodeDuration = -1;
	
	/** Outcomes at the end of the game in which this experience occurred (one per agent) */
	protected double[] playerOutcomes = null;
	
	/** Which legal moves are winning moves? */
	protected final BitSet winningMoves = new BitSet();
	
	/** Which legal moves are losing moves? */
	protected final BitSet losingMoves = new BitSet();
	
	/** Which legal moves are anti-defeating moves? */
	protected final BitSet antiDefeatingMoves = new BitSet();
	
	/** Importance sampling weight assigned to this sample by Prioritized Experience Replay */
	protected float weightPER = -1.f;
	
	/** Importance sampling weight for CE Explore */
	protected float weightCEExplore = -1.f;
	
	/** Importance sampling weight assigned to this sample based on tree search visit count */
	protected final float weightVisitCount;
	
	/** The index in replay buffer from which we sampled this if using PER */
	protected int bufferIdx = -1;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param context
	 * @param state
	 * @param moves 
	 * @param expertDistribution
	 * @param expertValueEstimates 
	 * @param weightVisitCount
	 */
	public ExItExperience
	(
		final Context context,
		final ExItExperienceState state,
		final FastArrayList<Move> moves, 
		final FVector expertDistribution,
		final FVector expertValueEstimates,
		final float weightVisitCount
	)
	{
		this.context = context;
		this.state = state;
		this.moves = moves;
		this.expertDistribution = expertDistribution;
		this.expertValueEstimates = expertValueEstimates;
		this.weightVisitCount = weightVisitCount;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Context
	 */
	public Context context()
	{
		return context;
	}
	
	/**
	 * @return state
	 */
	public ExItExperienceState state()
	{
		return state;
	}
	
	@Override
	public FastArrayList<Move> moves()
	{
		return moves;
	}
	
	/**
	 * @return The index in replay buffer from which we sampled this if using PER
	 */
	public int bufferIdx()
	{
		return bufferIdx;
	}
	
	@Override
	public FVector expertDistribution()
	{
		final FVector adjustedExpertDistribution = expertDistribution.copy();
		
		if (!winningMoves.isEmpty() || !losingMoves.isEmpty() || !antiDefeatingMoves.isEmpty())
		{
			final float maxVal = adjustedExpertDistribution.max();
			final float minVal = adjustedExpertDistribution.min();
			
			// Put high (but less than winning) values on anti-defeating moves
			for (int i = antiDefeatingMoves.nextSetBit(0); i >= 0; i = antiDefeatingMoves.nextSetBit(i + 1))
			{
				adjustedExpertDistribution.set(i, maxVal);
			}
			
			// Put large values on winning moves
			for (int i = winningMoves.nextSetBit(0); i >= 0; i = winningMoves.nextSetBit(i + 1))
			{
				adjustedExpertDistribution.set(i, maxVal * 2.f);
			}
			
			// Put low values on losing moves
			for (int i = losingMoves.nextSetBit(0); i >= 0; i = losingMoves.nextSetBit(i + 1))
			{
				adjustedExpertDistribution.set(i, minVal / 2.f);
			}
			
			// Re-normalise to probability distribution
			adjustedExpertDistribution.normalise();
		}
		
		return adjustedExpertDistribution;
	}
	
	/**
	 * @return Value estimates computed by expert (MCTS)
	 */
	public FVector expertValueEstimates()
	{
		return expertValueEstimates;
	}
	
	/**
	 * @return Duration of full episode in which this experience was generated
	 */
	public int episodeDuration()
	{
		return episodeDuration;
	}
	
	/**
	 * @return Array of outcomes (one per player) of episode in which this experience was generated
	 */
	public double[] playerOutcomes()
	{
		return playerOutcomes;
	}
	
	/**
	 * Sets the index in replay buffer from which we sampled this if using PER
	 * @param bufferIdx
	 */
	public void setBufferIdx(final int bufferIdx)
	{
		this.bufferIdx = bufferIdx;
	}
	
	/**
	 * Sets the episode duration
	 * @param episodeDuration
	 */
	public void setEpisodeDuration(final int episodeDuration)
	{
		this.episodeDuration = episodeDuration;
	}
	
	/**
	 * Sets the per-player outcomes for the episode in which this experience was generated
	 * @param playerOutcomes
	 */
	public void setPlayerOutcomes(final double[] playerOutcomes)
	{
		this.playerOutcomes = playerOutcomes;
	}
	
	/**
	 * Sets our state-feature-vector (for state value functions)
	 * @param vector
	 */
	public void setStateFeatureVector(final FVector vector)
	{
		this.stateFeatureVector = vector;
	}
	
	/**
	 * Set which moves are winning moves
	 * @param winningMoves
	 */
	public void setWinningMoves(final BitSet winningMoves)
	{
		this.winningMoves.clear();
		this.winningMoves.or(winningMoves);
	}
	
	/**
	 * Set which moves are losing moves
	 * @param losingMoves
	 */
	public void setLosingMoves(final BitSet losingMoves)
	{
		this.losingMoves.clear();
		this.losingMoves.or(losingMoves);
	}
	
	/**
	 * Set which moves are anti-defeating moves
	 * @param antiDefeatingMoves
	 */
	public void setAntiDefeatingMoves(final BitSet antiDefeatingMoves)
	{
		this.antiDefeatingMoves.clear();
		this.antiDefeatingMoves.or(antiDefeatingMoves);
	}
	
	/**
	 * Sets the importance sampling weight assigned to this sample by CE Explore
	 * @param weightCEExplore
	 */
	public void setWeightCEExplore(final float weightCEExplore)
	{
		this.weightCEExplore = weightCEExplore;
	}
	
	/**
	 * Sets the importance sampling weight assigned to this sample by Prioritized Experience Replay
	 * @param weightPER
	 */
	public void setWeightPER(final float weightPER)
	{
		this.weightPER = weightPER;
	}
	
	/**
	 * @return State feature vector
	 */
	public FVector stateFeatureVector()
	{
		return stateFeatureVector;
	}
	
	/**
	 * @return Importance sampling weight for CE exploration
	 */
	public float weightCEExplore()
	{
		return weightCEExplore;
	}
	
	/**
	 * @return Importance sampling weight assigned to this sample by Prioritized Experience Replay
	 */
	public float weightPER()
	{
		return weightPER;
	}
	
	/**
	 * @return Importance sampling weight assigned to this sampled based on tree search visit count
	 */
	public float weightVisitCount()
	{
		return weightVisitCount;
	}
	
	@Override
	public State gameState()
	{
		return state.state();
	}
	
	@Override
	public Move lastDecisionMove()
	{
		return state.lastDecisionMove();
	}
	
	@Override
	public BitSet winningMoves()
	{
		return winningMoves;
	}
	
	@Override
	public BitSet losingMoves()
	{
		return losingMoves;
	}
	
	@Override
	public BitSet antiDefeatingMoves()
	{
		return antiDefeatingMoves;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public FeatureVector[] generateFeatureVectors(final BaseFeatureSet featureSet)
	{
		return featureSet.computeFeatureVectors
				(
					state().state(), 
					state().lastDecisionMove(), 
					moves(), 
					false
				);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Wrapper class for game states in an ExIt experience buffer.
	 * Contains game state + last decision move (which we need access
	 * to for reactive features).
	 *
	 * @author Dennis Soemers
	 */
	public static final class ExItExperienceState implements Serializable
	{

		/** */
		private static final long serialVersionUID = 1L;
		
		/** Game state */
		private final State state;
		
		/** Last decision move */
		private final Move lastDecisionMove;
		
		/**
		 * Constructor
		 * @param context
		 */
		public ExItExperienceState(final Context context)
		{
			state = context.state();
			lastDecisionMove = context.trial().lastMove();
		}
		
		/**
		 * @return Game state
		 */
		public State state()
		{
			return state;
		}
		
		/**
		 * @return Last decision move
		 */
		public Move lastDecisionMove()
		{
			return lastDecisionMove;
		}
		
	}
	
	//-------------------------------------------------------------------------

}
