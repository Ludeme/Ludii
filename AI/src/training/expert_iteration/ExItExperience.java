package training.expert_iteration;

import java.io.Serializable;

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
	
	/** Outcomes at the end of the game in which this experience occurred (one per player) */
	protected double[] playerOutcomes = null;
	
	/** Importance sampling weight assigned to this sample by Prioritized Experience Replay */
	protected float weightPER = -1.f;
	
	/** Importance sampling weight for CE Explore */
	protected float weightCEExplore = -1.f;
	
	/** The index in replay buffer from which we sampled this if using PER */
	protected int bufferIdx = -1;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param state
	 * @param moves 
	 * @param expertDistribution
	 * @param expertValueEstimates 
	 */
	public ExItExperience
	(
		final ExItExperienceState state,
		final FastArrayList<Move> moves, 
		final FVector expertDistribution,
		final FVector expertValueEstimates
	)
	{
		this.state = state;
		this.moves = moves;
		this.expertDistribution = expertDistribution;
		this.expertValueEstimates = expertValueEstimates;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return state
	 */
	public ExItExperienceState state()
	{
		return state;
	}
	
	/**
	 * @return List of legal actions
	 */
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
		return expertDistribution;
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
