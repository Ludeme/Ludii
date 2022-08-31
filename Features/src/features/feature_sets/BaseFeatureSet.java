package features.feature_sets;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import features.Feature;
import features.FeatureVector;
import features.WeightVector;
import features.aspatial.AspatialFeature;
import features.spatial.FeatureUtils;
import features.spatial.SpatialFeature;
import features.spatial.cache.footprints.BaseFootprint;
import features.spatial.instances.FeatureInstance;
import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FVector;
import main.collections.FastArrayList;
import other.context.Context;
import other.move.Move;
import other.state.State;

/**
 * Abstract class for Feature Sets (basically; things that can compute feature
 * vectors for game states + actions).
 * 
 * @author Dennis Soemers
 */
public abstract class BaseFeatureSet
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Different implementations we have for evaluating feature sets.
	 * 
	 * @author Dennis Soemers
	 */
	public static enum FeatureSetImplementations
	{
		NAIVE,
		TREE,
		SPATTERNET,
		JITSPATTERNET
	}
	
	//-------------------------------------------------------------------------
	
	/** Only spatial features with an absolute value greater than this are considered relevant for AI */
	public static final float SPATIAL_FEATURE_WEIGHT_THRESHOLD = 0.001f;
	
	/** Reference to game for which we currently have instantiated features */
	protected WeakReference<Game> game = new WeakReference<>(null);
	
	/** Vector of feature weights for which we have last instantiated features */
	protected FVector spatialFeatureInitWeights = null;
	
	//-------------------------------------------------------------------------
	
	/** Array of aspatial features */
	protected AspatialFeature[] aspatialFeatures;
	
	/** Array of features */
	protected SpatialFeature[] spatialFeatures;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The array of aspatial features contained in this feature set
	 */
	public final AspatialFeature[] aspatialFeatures()
	{
		return aspatialFeatures;
	}
	
	/**
	 * @return The array of spatial features contained in this feature set
	 */
	public final SpatialFeature[] spatialFeatures()
	{
		return spatialFeatures;
	}
	
	/**
	 * @return The number of aspatial features in this feature set
	 */
	public final int getNumAspatialFeatures()
	{
		return aspatialFeatures.length;
	}
	
	/**
	 * @return The number of spatial features in this feature set
	 */
	public final int getNumSpatialFeatures()
	{
		return spatialFeatures.length;
	}
	
	/**
	 * @return Number of features in this feature set (spatial + aspatial features)
	 */
	public final int getNumFeatures()
	{
		return spatialFeatures.length + aspatialFeatures.length;
	}
	
	/**
	 * @return Weak reference to game for which we last initialised
	 */
	public WeakReference<Game> gameRef()
	{
		return game;
	}
	
	/**
	 * Lets the feature set initialise itself for a given game, array of supported players, and vector of weights
	 * (for example, can instantiate features here).
	 * @param newGame
	 * @param supportedPlayers
	 * @param weights
	 */
	public void init(final Game newGame, final int[] supportedPlayers, final WeightVector weights)
	{
		final FVector spatialOnlyWeights;
		
		if (weights == null)
			spatialOnlyWeights = null;
		else
			spatialOnlyWeights = weights.allWeights().range(aspatialFeatures.length, weights.allWeights().dim());
		
		if (this.game.get() == newGame)
		{
			if (this.spatialFeatureInitWeights == null && spatialOnlyWeights == null)
				return;		// Nothing to do, already instantiated
			else if (this.spatialFeatureInitWeights != null && this.spatialFeatureInitWeights.equals(spatialOnlyWeights))
				return;		// Also nothing to do here
		}
		
		this.game = new WeakReference<>(newGame);
		
		if (spatialOnlyWeights == null)
			spatialFeatureInitWeights = null;
		else
			spatialFeatureInitWeights = spatialOnlyWeights;
		
		// Need to instantiate
		instantiateFeatures(supportedPlayers);
	}
	
	/**
	 * Lets the feature set instantiate its features
	 * @param supportedPlayers
	 */
	protected abstract void instantiateFeatures(final int[] supportedPlayers);
	
	/**
	 * Closes / cleans up cache of active features
	 */
	public abstract void closeCache();
	
	/**
	 * @param state
	 * @param from
	 * @param to
	 * @param player
	 * @return The complete footprint of all tests that may possibly be run
	 * for computing proactive features of actions with the given from- and to- 
	 * positions.
	 */
	public abstract BaseFootprint generateFootprint
	(
		final State state, 
		final int from, 
		final int to,
		final int player
	);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context
	 * @param action
	 * @param thresholded
	 * 
	 * @return A sparse feature vector for a single action
	 */
	public TIntArrayList computeSparseSpatialFeatureVector
	(
		final Context context,
		final Move action,
		final boolean thresholded
	)
	{
		return computeSparseSpatialFeatureVector(context.state(), context.trial().lastMove(), action, thresholded);
	}
	
	/**
	 * @param context
	 * @param actions
	 * @param thresholded
	 * 
	 * @return A list of sparse feature vectors (one for every given action in the given trial)
	 */
	public TIntArrayList[] computeSparseSpatialFeatureVectors
	(
		final Context context,
		final FastArrayList<Move> actions,
		final boolean thresholded
	)
	{
		return computeSparseSpatialFeatureVectors(context.state(), context.trial().lastMove(), actions, thresholded);
	}
	
	/**
	 * @param state
	 * @param lastDecisionMove
	 * @param action
	 * @param thresholded
	 * 
	 * @return A spare feature vector for a single action
	 */
	public TIntArrayList computeSparseSpatialFeatureVector
	(
		final State state, 
		final Move lastDecisionMove,
		final Move action,
		final boolean thresholded
	)
	{
		final int lastFrom = FeatureUtils.fromPos(lastDecisionMove);
		final int lastTo = FeatureUtils.toPos(lastDecisionMove);
		final int from = FeatureUtils.fromPos(action);
		final int to = FeatureUtils.toPos(action);
			
//		System.out.println("last decision move = " + trial.lastDecisionMove());
//		System.out.println("lastFrom = " + lastFrom);
//		System.out.println("lastTo = " + lastTo);
//		System.out.println("from = " + from);
//		System.out.println("to = " + to);
			
		final TIntArrayList sparseFeatureVector = 
				getActiveSpatialFeatureIndices(state, lastFrom, lastTo, from, to, action.mover(), thresholded);
		
		return sparseFeatureVector;
	}
	
	/**
	 * @param state
	 * @param lastDecisionMove
	 * @param actions
	 * @param thresholded
	 * 
	 * @return A list of sparse feature vectors (one for every given action in the given trial)
	 */
	public TIntArrayList[] computeSparseSpatialFeatureVectors
	(
		final State state, 
		final Move lastDecisionMove,
		final FastArrayList<Move> actions,
		final boolean thresholded
	)
	{
		final TIntArrayList[] sparseFeatureVectors = new TIntArrayList[actions.size()];
		
		for (int i = 0; i < actions.size(); ++i)
		{
			final Move action = actions.get(i);
			sparseFeatureVectors[i] = computeSparseSpatialFeatureVector(state, lastDecisionMove, action, thresholded);
		}
		
		return sparseFeatureVectors;
	}
	
	/**
	 * @param context
	 * @param move
	 * @return List of all active features for given move in given context (spatial and aspatial ones).
	 * 	Non-binary features are considered to be active if their value is not equal to 0
	 */
	public List<Feature> computeActiveFeatures(final Context context, final Move move)
	{
		final List<Feature> activeFeatures = new ArrayList<Feature>();
		
		// Compute and add spatial features
		final Move lastDecisionMove = context.trial().lastMove();
		final int lastFrom = FeatureUtils.fromPos(lastDecisionMove);
		final int lastTo = FeatureUtils.toPos(lastDecisionMove);
		final int from = FeatureUtils.fromPos(move);
		final int to = FeatureUtils.toPos(move);
		final TIntArrayList activeSpatialFeatureIndices = 
				getActiveSpatialFeatureIndices
				(
					context.state(),
					lastFrom, lastTo,
					from, to,
					move.mover(),
					false
				);
		
		for (int i = 0; i < activeSpatialFeatureIndices.size(); ++i)
		{
			activeFeatures.add(spatialFeatures[activeSpatialFeatureIndices.getQuick(i)]);
		}
		
		// Compute and add active aspatial features
		for (final AspatialFeature feature : aspatialFeatures)
		{
			if (feature.featureVal(context.state(), move) != 0.f)
				activeFeatures.add(feature);
		}
		
		return activeFeatures;
	}
	
	/**
	 * @param context
	 * @param moves
	 * @param thresholded
	 * @return Features vectors for all the given moves in given context
	 */
	public FeatureVector[] computeFeatureVectors
	(
		final Context context, final FastArrayList<Move> moves, final boolean thresholded
	)
	{
		final FeatureVector[] featureVectors = new FeatureVector[moves.size()];
		for (int i = 0; i < moves.size(); ++i)
		{
			featureVectors[i] = computeFeatureVector(context, moves.get(i), thresholded);
		}
		return featureVectors;
	}
	
	/**
	 * @param context
	 * @param move
	 * @param thresholded Whether to use thresholding for spatial features
	 * @return Feature vector for given move in given context
	 */
	public FeatureVector computeFeatureVector(final Context context, final Move move, final boolean thresholded)
	{
		// Compute active spatial feature indices
		final Move lastDecisionMove = context.trial().lastMove();
		final int lastFrom = FeatureUtils.fromPos(lastDecisionMove);
		final int lastTo = FeatureUtils.toPos(lastDecisionMove);
		final int from = FeatureUtils.fromPos(move);
		final int to = FeatureUtils.toPos(move);
		final TIntArrayList activeSpatialFeatureIndices = 
				getActiveSpatialFeatureIndices
				(
					context.state(),
					lastFrom, lastTo,
					from, to,
					move.mover(),
					thresholded
				);
		
		// Compute aspatial feature values
		final float[] aspatialFeatureValues = new float[aspatialFeatures.length];
		for (int i = 0; i < aspatialFeatures.length; ++i)
		{
			aspatialFeatureValues[i] = aspatialFeatures[i].featureVal(context.state(), move);
		}
		
		return new FeatureVector(activeSpatialFeatureIndices, new FVector(aspatialFeatureValues));
	}
	
	/**
	 * @param state
	 * @param lastMove
	 * @param moves
	 * @param thresholded
	 * @return Features vectors for all the given moves in given context
	 */
	public FeatureVector[] computeFeatureVectors
	(
		final State state, final Move lastMove, final FastArrayList<Move> moves, final boolean thresholded
	)
	{
		final int lastFrom = FeatureUtils.fromPos(lastMove);
		final int lastTo = FeatureUtils.toPos(lastMove);
		
		final FeatureVector[] featureVectors = new FeatureVector[moves.size()];
		for (int i = 0; i < moves.size(); ++i)
		{
			final Move move = moves.get(i);
			final int from = FeatureUtils.fromPos(move);
			final int to = FeatureUtils.toPos(move);
			
			final TIntArrayList activeSpatialFeatureIndices = 
				getActiveSpatialFeatureIndices
				(
					state,
					lastFrom, lastTo,
					from, to,
					move.mover(),
					thresholded
				);
			
			final float[] aspatialFeatureValues = new float[aspatialFeatures.length];
			for (int j = 0; j < aspatialFeatures.length; ++j)
			{
				aspatialFeatureValues[j] = aspatialFeatures[j].featureVal(state, move);
			}
			
			featureVectors[i] = new FeatureVector(activeSpatialFeatureIndices, new FVector(aspatialFeatureValues));
		}
		return featureVectors;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param state 
	 * @param lastFrom
	 * @param lastTo
	 * @param from
	 * @param to
	 * @param player
	 * @param thresholded
	 * 
	 * @return A list of indices of all the features that are active for a 
	 * given state+action pair (where action is defined by from and 
	 * to positions)
	 */
	public abstract TIntArrayList getActiveSpatialFeatureIndices
	(
		final State state, 
		final int lastFrom, 
		final int lastTo, 
		final int from, 
		final int to,
		final int player,
		final boolean thresholded
	);
	
	/**
	 * @param state
	 * @param lastFrom
	 * @param lastTo
	 * @param from
	 * @param to
	 * @param player
	 * @return A list of all spatial feature instances that are active for a given 
	 * state+action pair (where action is defined by from and to positions)
	 */
	public abstract List<FeatureInstance> getActiveSpatialFeatureInstances
	(
		final State state, 
		final int lastFrom, 
		final int lastTo, 
		final int from, 
		final int to,
		final int player
	);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param targetGame
	 * @param newFeature
	 * @return Expanded feature set with the new feature added, or null in the case of failure.
	 */
	public abstract BaseFeatureSet createExpandedFeatureSet
	(
		final Game targetGame,
		final SpatialFeature newFeature
	);
	
	/**
	 * @param targetGame
	 * @param newFeatures
	 * @return Expanded feature set with multiple new features added, or original object
	 * 	if none of the new features were successfully added.
	 */
	public BaseFeatureSet createExpandedFeatureSet(final Game targetGame, final List<SpatialFeature> newFeatures)
	{
		BaseFeatureSet featureSet = this;
		
		for (final SpatialFeature feature : newFeatures)
		{
			final BaseFeatureSet expanded = featureSet.createExpandedFeatureSet(targetGame, feature);
			if (expanded != null)
			{
				featureSet = expanded;
			}
		}
		
		return featureSet;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * NOTE: implementation not very fast.
	 * 
	 * @param s
	 * @return Index of feature in this feature set that matches given string, or -1 if none found.
	 */
	public int findFeatureIndexForString(final String s)
	{
		for (int i = 0; i < aspatialFeatures.length; ++i)
		{
			if (aspatialFeatures[i].toString().equals(s))
				return i;
		}
		
		for (int i = 0; i < spatialFeatures.length; ++i)
		{
			if (spatialFeatures[i].toString().equals(s))
				return i;
		}
		
		return -1;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Writes the feature set to a file
	 * @param filepath Filepath to write to
	 */
	public void toFile(final String filepath)
	{
		try (final PrintWriter writer = new PrintWriter(filepath, "UTF-8"))
		{
			writer.print(toString());
		} 
		catch (final FileNotFoundException | UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		for (final AspatialFeature feature : aspatialFeatures)
		{
			sb.append(feature + System.lineSeparator());
		}

		for (final SpatialFeature feature : spatialFeatures)
		{
			sb.append(feature + System.lineSeparator());
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Interface for move features keys (either proactive or reactive)
	 *
	 * @author Dennis Soemers
	 */
	public interface MoveFeaturesKey
	{
		/** @return Player index for the key */
		public int playerIdx();
		
		/** @return From position for the key */
		public int from();
		
		/** @return To position for the key */
		public int to();
		
		/** @return Last from position for the key */
		public int lastFrom();
		
		/** @return Last to position for the key */
		public int lastTo();
	}
	
	/**
	 * Small class for objects used as keys in HashMaps related to proactive 
	 * features.
	 * 
	 * @author Dennis Soemers
	 */
	public static class ProactiveFeaturesKey implements MoveFeaturesKey
	{
		//--------------------------------------------------------------------
		
		/** Player index */
		private int playerIdx = -1;
		
		/** from-position */
		private int from = -1;
		
		/** to-position */
		private int to = -1;
		
		/** Cached hash code */
		private transient int cachedHashCode = -1;
		
		//--------------------------------------------------------------------
		
		/**
		 * Default constructor
		 */
		public ProactiveFeaturesKey()
		{
			// Do nothing
		}
		
		/**
		 * Copy constructor
		 * @param other
		 */
		public ProactiveFeaturesKey(final ProactiveFeaturesKey other)
		{
			resetData(other.playerIdx, other.from, other.to);
		}
		
		//--------------------------------------------------------------------
		
		/**
		 * Resets the data in this object and recomputes cached hash code
		 * @param p Player Index
		 * @param f From
		 * @param t To
		 */
		public void resetData(final int p, final int f, final int t)
		{
			this.playerIdx = p;
			this.from = f;
			this.to = t;
			
			// Create and cache hash code
			final int prime = 31;
			int result = 17;
			result = prime * result + from;
			result = prime * result + playerIdx;
			result = prime * result + to;
			cachedHashCode = result;
		}
		
		//--------------------------------------------------------------------
		
		@Override
		public int playerIdx()
		{
			return playerIdx;
		}
		
		@Override
		public int from()
		{
			return from;
		}
		
		@Override
		public int to()
		{
			return to;
		}
		
		@Override
		public int lastFrom()
		{
			return -1;
		}
		
		@Override
		public int lastTo()
		{
			return -1;
		}
		
		//--------------------------------------------------------------------
		
		@Override
		public int hashCode()
		{
			return cachedHashCode;
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
				return true;

			if (!(obj instanceof ProactiveFeaturesKey))
				return false;
			
			final ProactiveFeaturesKey other = (ProactiveFeaturesKey) obj;
			
			return (playerIdx == other.playerIdx &&
					from == other.from &&
					to == other.to);
		}
		
		@Override
		public String toString()
		{
			return "[ProactiveFeaturesKey: " + playerIdx + ", " + from + ", " + to + "]";
		}
		
		//--------------------------------------------------------------------
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Small class for objects used as keys in HashMaps related to reactive 
	 * features.
	 * 
	 * @author Dennis Soemers
	 */
	public static class ReactiveFeaturesKey implements MoveFeaturesKey
	{
		//--------------------------------------------------------------------
		
		/** Player index */
		private int playerIdx = -1;
		
		/** Last from-position */
		private int lastFrom = -1;
		
		/** Last to-position */
		private int lastTo = -1;
		
		/** from-position */
		private int from = -1;
		
		/** to-position */
		private int to = -1;
		
		/** Cached hash code */
		private transient int cachedHashCode = -1;
		
		//--------------------------------------------------------------------
		
		/**
		 * Default constructor
		 */
		public ReactiveFeaturesKey()
		{
			// Do nothing
		}
		
		/**
		 * Copy constructor
		 * @param other
		 */
		public ReactiveFeaturesKey(final ReactiveFeaturesKey other)
		{
			resetData(other.playerIdx, other.lastFrom, other.lastTo, other.from, other.to);
		}
		
		//--------------------------------------------------------------------
		
		/**
		 * Resets the data in this object and recomputes cached hash code
		 * @param p Player Index
		 * @param lastF Last from
		 * @param lastT last To
		 * @param f From
		 * @param t To
		 */
		public void resetData(final int p, final int lastF, final int lastT, final int f, final int t)
		{
			this.playerIdx = p;
			this.lastFrom = lastF;
			this.lastTo = lastT;
			this.from = f;
			this.to = t;
			
			// Create and cache hash code
			final int prime = 31;
			int result = 17;
			result = prime * result + from;
			result = prime * result + lastFrom;
			result = prime * result + lastTo;
			result = prime * result + playerIdx;
			result = prime * result + to;
			cachedHashCode = result;
		}
		
		//--------------------------------------------------------------------
		
		@Override
		public int playerIdx()
		{
			return playerIdx;
		}
		
		@Override
		public int from()
		{
			return from;
		}
		
		@Override
		public int to()
		{
			return to;
		}
		
		@Override
		public int lastFrom()
		{
			return lastFrom;
		}
		
		@Override
		public int lastTo()
		{
			return lastTo;
		}
		
		//--------------------------------------------------------------------
		
		@Override
		public int hashCode()
		{
			return cachedHashCode;
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
				return true;

			if (!(obj instanceof ReactiveFeaturesKey))
				return false;
			
			final ReactiveFeaturesKey other = (ReactiveFeaturesKey) obj;
			
			return (playerIdx == other.playerIdx &&
					lastFrom == other.lastFrom &&
					lastTo == other.lastTo &&
					from == other.from &&
					to == other.to);
		}
		
		@Override
		public String toString()
		{
			return "[ReactiveFeaturesKey: " + playerIdx + ", " + from + ", " + to + ", " + lastFrom + ", " + lastTo + "]";
		}
		
		//--------------------------------------------------------------------
	}
	
	//-------------------------------------------------------------------------

}
