package features.generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import features.aspatial.AspatialFeature;
import features.aspatial.InterceptFeature;
import features.aspatial.PassMoveFeature;
import features.aspatial.SwapMoveFeature;
import features.spatial.AbsoluteFeature;
import features.spatial.Pattern;
import features.spatial.RelativeFeature;
import features.spatial.SpatialFeature;
import features.spatial.Walk;
import features.spatial.elements.FeatureElement;
import features.spatial.elements.FeatureElement.ElementType;
import features.spatial.elements.RelativeFeatureElement;
import game.Game;
import game.equipment.component.Component;
import game.equipment.other.Regions;
import game.types.state.GameType;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * Generates "atomic" features for a game. We say that a spatial feature is atomic if 
 * and only if it consists of exactly 0 or 1 restrictions (Walk + element type)
 * 
 * The maximum size any Walk is allowed to have can be specified on 
 * instantiation of the generator
 * 
 * Any relevant aspatial features are also included.
 * 
 * @author Dennis Soemers
 */
public class AtomicFeatureGenerator 
{
	
	//-------------------------------------------------------------------------
	
	/** Reference to our game */
	protected final Game game;

	/** Generated aspatial features */
	protected final List<AspatialFeature> aspatialFeatures;
	
	/** Generated spatial features */
	protected final List<SpatialFeature> spatialFeatures;

	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param game
	 * @param maxWalkSize Maximum size of walks generated for atomic features
	 * @param maxStraightWalkSize Maximum size of straight-line walks for 
	 * atomic features. These straight-line walks may be longer than 
	 * non-straight-line walks, which are subject to the standard maxWalkSize 
	 * restriction.
	 */
	public AtomicFeatureGenerator
	(
		final Game game, 
		final int maxWalkSize, 
		final int maxStraightWalkSize
	)
	{
		this.game = game;
		
		// First generate spatial features
		spatialFeatures = simplifyFeatureSet(generateFeatures(maxWalkSize, maxStraightWalkSize));
		spatialFeatures.sort(new Comparator<SpatialFeature>() 
		{

			@Override
			public int compare(final SpatialFeature o1, final SpatialFeature o2) 
			{
				final FeatureElement[] els1 = o1.pattern().featureElements();
				final FeatureElement[] els2 = o2.pattern().featureElements();
				
				if (els1.length < els2.length)
				{
					return -1;
				}
				else if (els1.length > els2.length)
				{
					return 1;
				}
				else
				{
					int sumWalkLengths1 = 0;
					int sumWalkLengths2 = 0;
					
					for (final FeatureElement el : els1)
					{
						if (el instanceof RelativeFeatureElement)
						{
							sumWalkLengths1 += ((RelativeFeatureElement) el).walk().steps().size();
						}
					}
					
					for (final FeatureElement el : els2)
					{
						if (el instanceof RelativeFeatureElement)
						{
							sumWalkLengths2 += ((RelativeFeatureElement) el).walk().steps().size();
						}
					}
					
					return sumWalkLengths1 - sumWalkLengths2;
				}
			}
			
		});
		
		aspatialFeatures = new ArrayList<AspatialFeature>();
		
		// Intercept feature always considered relevant
		aspatialFeatures.add(InterceptFeature.instance());
		
		// Pass feature always considered relevant
		aspatialFeatures.add(PassMoveFeature.instance());
		
		// Swap feature only relevant if game uses swap rule
		if ((game.gameFlags() & GameType.UsesSwapRule) != 0L)
			aspatialFeatures.add(SwapMoveFeature.instance());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Generated aspatial features
	 */
	public List<AspatialFeature> getAspatialFeatures()
	{
		return aspatialFeatures;
	}
	
	/**
	 * @return Generated spatial features
	 */
	public List<SpatialFeature> getSpatialFeatures()
	{
		return spatialFeatures;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Generates new features with additional walks up to the given max size.
	 * 
	 * @param maxSize
	 * @param maxStraightWalkSize
	 * @return
	 */
	private List<SpatialFeature> generateFeatures(final int maxSize, final int maxStraightWalkSize)
	{
		//final MoveFeatures moveFeaturesGenerator = new MoveFeatures(game);
		//final List<Feature> moveFeatures = moveFeaturesGenerator.features();
		//final Set<Feature> generatedFeatures = new HashSet<Feature>(16384);
		//generatedFeatures.addAll(moveFeatures);
		
		final List<SpatialFeature> emptyFeatures = new ArrayList<SpatialFeature>();
		emptyFeatures.add(new RelativeFeature(new Pattern(), new Walk(), null));
		
		if ((game.gameFlags() & GameType.UsesFromPositions) != 0L)
			emptyFeatures.add(new RelativeFeature(new Pattern(), null, new Walk()));
		
		final Set<SpatialFeature> generatedFeatures = new HashSet<SpatialFeature>(16384);
		generatedFeatures.addAll(emptyFeatures);
		
		final TIntArrayList connectivities = game.board().topology().trueOrthoConnectivities(game);
		final TFloatArrayList allGameRotations = new TFloatArrayList(Walk.allGameRotations(game));
		final EnumSet<ElementType> elementTypes = FeatureGenerationUtils.usefulElementTypes(game);
		
		elementTypes.add(ElementType.LastFrom);
		elementTypes.add(ElementType.LastTo);
		
		for (int walkSize = 0; walkSize <= maxStraightWalkSize; ++walkSize)
		{
			final List<Walk> allWalks = generateAllWalks(walkSize, maxSize, allGameRotations);

			// for every base feature, create new versions with all possible 
			// additions of a Walk of length walkSize
			for (final SpatialFeature baseFeature : emptyFeatures)
			{
				final Pattern basePattern = baseFeature.pattern();

				// we'll always add exactly one Walk
				for (final Walk walk : allWalks)
				{					
					// for every element type...
					for (final ElementType elementType : elementTypes)
					{
						final TIntArrayList itemIndices = new TIntArrayList();

						if (elementType == ElementType.Item)
						{
							final Component[] components = game.equipment().components();
							for (int i = 1; i < components.length; ++i)
							{
								if (components[i] != null)
								{
									itemIndices.add(i);
								}
							}
						}
						else if (elementType == ElementType.IsPos)
						{
							System.err.println("WARNING: not yet including position indices in AtomicFeatureGenerator.generateFeatures()");
						}
						else if (elementType == ElementType.Connectivity)
						{
							itemIndices.addAll(connectivities);
						}
						else if (elementType == ElementType.RegionProximity)
						{
							if (walkSize > 0)		// RegionProximity test on anchor is useless
							{
								// Only include regions for which we actually have distance tables
								final Regions[] regions = game.equipment().regions();
								
								for (int i = 0; i < regions.length; ++i)
								{
									if (game.distancesToRegions()[i] != null)
										itemIndices.add(i);
								}
							}
						}
						else if (elementType == ElementType.LineOfSightOrth || elementType == ElementType.LineOfSightDiag)
						{
							final Component[] components = game.equipment().components();
							for (int i = 1; i < components.length; ++i)
							{
								if (components[i] != null)
								{
									itemIndices.add(i);
								}
							}
						}
						else
						{
							itemIndices.add(-1);
						}

						// normal tests and NOT-tests
						for (final boolean not : new boolean[] {false, true})
						{
							// for every item / position index...
							for (int idx = 0; idx < itemIndices.size(); ++idx)
							{
								// create a new version of the pattern where we add the 
								// current walk + the current element type
								final Pattern newPattern = new Pattern(baseFeature.pattern());

								if (elementType != ElementType.LastFrom && elementType != ElementType.LastTo)
								{
									newPattern.addElement
									(
										new RelativeFeatureElement(elementType, not, new Walk(walk), itemIndices.getQuick(idx))
									);

								}
									
								// make sure it's actually a consistent pattern
								if (newPattern.isConsistent())
								{
									// remove elements that appear with same walk multiple times
									newPattern.removeRedundancies();

									// make sure that we're still meaningfully 
									// different from base pattern after removing 
									// redundancies
									if 
									(
										!newPattern.equals(basePattern) || 
										elementType != ElementType.LastFrom || 
										elementType != ElementType.LastTo
									)
									{
										final SpatialFeature newFeature;

										if (baseFeature instanceof AbsoluteFeature)
										{
											final AbsoluteFeature absBase = (AbsoluteFeature) baseFeature;
											newFeature = 
													new AbsoluteFeature
													(
														newPattern, 
														absBase.toPosition(), 
														absBase.fromPosition()
													);
										}
										else
										{
											final Walk lastTo = (elementType == ElementType.LastTo) ? new Walk(walk) : null;
											final Walk lastFrom = (elementType == ElementType.LastFrom) ? new Walk(walk) : null;
											
											final RelativeFeature relBase = (RelativeFeature) baseFeature;
											newFeature = new RelativeFeature
													(
														newPattern,
														relBase.toPosition() != null ? 
																new Walk(relBase.toPosition()) : null,
														relBase.fromPosition() != null ? 
																new Walk(relBase.fromPosition()) : null,
														lastTo,
														lastFrom
													);
										}

										// try to eliminate duplicates under rotation 
										// and/or reflection
										newFeature.normalise(game);	
										// remove elements that appear with 
										// same walk multiple times
										newFeature.pattern().removeRedundancies();

										generatedFeatures.add(newFeature);
									}
								}
							}
						}
					}
				}
			}
		}

		return new ArrayList<SpatialFeature>(generatedFeatures);
	}
	
	/**
	 * @param walkSize
	 * @param maxWalkSize
	 * @param allGameRotations
	 * @return A list of all possible walks of the given size.
	 * Returns a list containing just null for walkSize < 0
	 */
	private static List<Walk> generateAllWalks
	(
		final int walkSize, 
		final int maxWalkSize,
		final TFloatArrayList allGameRotations
	)
	{
		if (walkSize < 0)
		{
			final List<Walk> walks = new ArrayList<Walk>(1);
			walks.add(null);
			return walks;
		}
		
		List<Walk> allWalks = Arrays.asList(new Walk());

		int currWalkLengths = 0;
		while (currWalkLengths < walkSize)
		{
			final List<Walk> allWalksReplacement = new ArrayList<Walk>(allWalks.size() * 4);

			for (final Walk walk : allWalks)
			{
				for (int i = 0; i < allGameRotations.size(); ++i)
				{
					final float rot = allGameRotations.getQuick(i);
					
					if (rot == 0.f || currWalkLengths == 0 || walkSize <= maxWalkSize)
					{
						// only straight-line walks are allowed to exceed maxWalkSize
						if (rot != 0.5f || currWalkLengths == 0)
						{
							// rotating by 0.5 is never useful 
							// (except as very first step)
							final Walk newWalk = new Walk(walk);
							newWalk.steps().add(rot);
							allWalksReplacement.add(newWalk);
						}
					}
				}
			}

			++currWalkLengths;
			allWalks = allWalksReplacement;
		}
		
		return allWalks;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Simplifies the feature set given by the list of features by:
	 * 
	 * 	1) If there are two different features (which may not yet allow
	 * 	reflection), such that they would become equal if one of them were
	 * 	reflected; keeps only one of them (with reflection allowed).
	 * 
	 * 	2) If there are two different features (which may not yet allow all
	 * 	rotations), such that they would become equal if one of them were
	 * 	rotated by a certain amount; keeps only one of them (with the required
	 * 	rotation allowed)
	 * 
	 * @param featuresIn
	 * @return
	 */
	private List<SpatialFeature> simplifyFeatureSet(final List<SpatialFeature> featuresIn)
	{
		final List<SpatialFeature> simplified = new ArrayList<SpatialFeature>(featuresIn.size());
		
		final Map<Object, RotRefInvariantFeature> featuresToKeep = 
				new HashMap<Object, RotRefInvariantFeature>();
		
		final TFloatArrayList rotations = new TFloatArrayList(Walk.allGameRotations(game));
		final boolean[] reflections = {true, false};
		
		for (final SpatialFeature feature : featuresIn)
		{
			boolean shouldAddFeature = true;
			
			for (int i = 0; i < rotations.size(); ++i)
			{
				final float rotation = rotations.get(i);
				
				for (int j = 0; j < reflections.length; ++j)
				{
					final boolean reflect = reflections[j];
					
					SpatialFeature rotatedFeature = feature.rotatedCopy(rotation);
					
					if (reflect)
					{
						rotatedFeature = rotatedFeature.reflectedCopy();
					}
					
					rotatedFeature.normalise(game);
					final RotRefInvariantFeature wrapped = new RotRefInvariantFeature(rotatedFeature);
					
					if (featuresToKeep.containsKey(wrapped))
					{
						shouldAddFeature = false; 
						
						final SpatialFeature keepFeature = featuresToKeep.remove(wrapped).feature;
						
						// make sure the feature that we decide to keep also
						// allows for the necessary rotation
						final float requiredRot = rotation == 0.f ? 0.f : 1.f - rotation;
						
						if (keepFeature.pattern().allowedRotations() != null)
						{
							if (!keepFeature.pattern().allowedRotations().contains(requiredRot))
							{
								final TFloatArrayList allowedRotations = new TFloatArrayList();
								allowedRotations.addAll
								(
									keepFeature.pattern().allowedRotations()
								);
								allowedRotations.add(requiredRot);
								keepFeature.pattern().setAllowedRotations(allowedRotations);
								keepFeature.pattern().allowedRotations().sort();
								keepFeature.normalise(game);
							}
						}
						
						final RotRefInvariantFeature wrappedKeep = new RotRefInvariantFeature(keepFeature);
						featuresToKeep.put(wrappedKeep, wrappedKeep);
						/*System.out.println("using " + keepFeature + 
								" instead of " + feature);*/
						
						break;
					}
				}
				
				if (!shouldAddFeature)
				{
					break;
				}
			}
			
			if (shouldAddFeature)
			{
				final RotRefInvariantFeature wrapped = new RotRefInvariantFeature(feature);
				featuresToKeep.put(wrapped, wrapped);
			}
		}
		
		for (final RotRefInvariantFeature feature : featuresToKeep.values())
		{
			simplified.add(feature.feature);
		}
		
		return simplified;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Wrapper around a feature, with equals() and hashCode() functions that
	 * ignore rotation / reflection permissions in feature/pattern.
	 * 
	 * @author Dennis Soemers
	 */
	private class RotRefInvariantFeature
	{
		/** Wrapped Feature */
		protected SpatialFeature feature;
		
		/**
		 * Constructor
		 * @param feature
		 */
		public RotRefInvariantFeature(final SpatialFeature feature)
		{
			this.feature = feature;
		}
		
		@Override
		public boolean equals(final Object other)
		{
			if (!(other instanceof RotRefInvariantFeature))
			{
				return false;
			}
			
			return feature.equalsIgnoreRotRef(((RotRefInvariantFeature) other).feature);
		}
		
		@Override
		public int hashCode()
		{
			return feature.hashCodeIgnoreRotRef();
		}
	}
	
	//-------------------------------------------------------------------------

}
