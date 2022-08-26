package features.spatial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import features.Feature;
import features.spatial.elements.AbsoluteFeatureElement;
import features.spatial.elements.FeatureElement;
import features.spatial.elements.FeatureElement.ElementType;
import features.spatial.elements.RelativeFeatureElement;
import features.spatial.graph_search.GraphSearch;
import features.spatial.graph_search.Path;
import features.spatial.instances.FeatureInstance;
import game.Game;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.GraphElement;
import game.util.graph.Radial;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TFloatIntMap;
import gnu.trove.map.hash.TFloatIntHashMap;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Geometric features contain a Pattern that can be matched to parts of the board, and
 * some description of an action to play.
 * 
 * @author Dennis Soemers
 */
public abstract class SpatialFeature extends Feature
{

	//-------------------------------------------------------------------------

	/** Different types of BitSets we may want to compare against. */
	public static enum BitSetTypes
	{
		/** Empty ChunkSet */
		Empty, 
		/** Who ChunkSet */
		Who, 
		/** What ChunkSet */
		What,

		/** If we don't want to do anything */
		None;
	}

	//-------------------------------------------------------------------------

	/** The feature's pattern */
	protected Pattern pattern;
	
	/** The graph element type this feature applies to. Tries to auto-detect for game if null */
	protected SiteType graphElementType = null;

	/**
	 * Feature Sets will set this index to be the index that the feature has inside
	 * its Feature Set This member is not really a "part" of the feature, and is
	 * therefore not used in methods such as equals() and hashCode().
	 */
	protected int spatialFeatureSetIndex = -1;

//	/** Optional comment */
//	protected String comment = "";

	//-------------------------------------------------------------------------

	/**
	 * @return The feature's pattern
	 */
	public Pattern pattern()
	{
		return pattern;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Index in our feature set
	 */
	public int spatialFeatureSetIndex()
	{
		return spatialFeatureSetIndex;
	}

	/**
	 * Sets index in our feature set
	 * @param newIdx
	 */
	public void setSpatialFeatureSetIndex(final int newIdx)
	{
		spatialFeatureSetIndex = newIdx;
	}
	
	/**
	 * @return Is this a reactive feature (with specifiers for last-from 
	 * 	and/or last-to position)?
	 */
	@SuppressWarnings("static-method")
	public boolean isReactive()
	{
		return false;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param rotation
	 * @return Copy of this feature with the given rotation applied
	 */
	public abstract SpatialFeature rotatedCopy(final float rotation);

	/**
	 * @return Copy of this feature, with reflection applied
	 */
	public abstract SpatialFeature reflectedCopy();

	/**
	 * @param other
	 * @return True if we generalise the given other feature
	 */
	public abstract boolean generalises(final SpatialFeature other);

	//-------------------------------------------------------------------------

	/**
	 * Creates all possible instances for this feature
	 * 
	 * @param game
	 * @param container
	 * @param player           		Player for whom to create the feature instance
	 * @param anchorConstraint		Constraint on anchor position (-1 if no constraint)
	 * @param fromPosConstraint 	Constraint on absolute from-position (-1 if no constraint)
	 * @param toPosConstraint   	Constraint on absolute to-position (-1 if no constraint)
	 * @param lastFromConstraint	Constraint on absolute last-from-position (-1 if no constraint)
	 * @param lastToConstraint   	Constraint on absolute last-to-position (-1 if no constraint)
	 * @return List of possible feature instances
	 */
	public final List<FeatureInstance> instantiateFeature
	(
		final Game game, 
		final ContainerState container,
		final int player, 
		final int anchorConstraint,
		final int fromPosConstraint, 
		final int toPosConstraint,
		final int lastFromConstraint,
		final int lastToConstraint
	)
	{
		final Topology topology = game.board().topology();
		final SiteType instanceType;
		
		if (graphElementType != null)
			instanceType = graphElementType;
		else if (game.board().defaultSite() == SiteType.Vertex)
			instanceType = SiteType.Vertex;
		else
			instanceType = SiteType.Cell;
		
		final List<FeatureInstance> instances = new ArrayList<FeatureInstance>();
		final int[] reflections = pattern.allowsReflection() ? new int[]{ 1, -1 } : new int[]{ 1 };

		// for every site, we want to try creating feature instances with that site as
		// anchor position
		final List<? extends TopologyElement> sites = game.graphPlayElements();
		boolean moreSitesRelevant = true;

		int siteIdx = (anchorConstraint >= 0) ? anchorConstraint : 0;
		for (/** */; siteIdx < sites.size(); ++siteIdx)
		{
			if (anchorConstraint >= 0 && anchorConstraint != siteIdx)
				break;
			
			final TopologyElement anchorSite = sites.get(siteIdx);
			
			if (anchorSite.sortedOrthos().length == 0)
				continue;	// No point in using this as an anchor
			
			TFloatArrayList rots = pattern.allowedRotations();
			if (rots == null)
				rots = Walk.rotationsForNumOrthos(anchorSite.sortedOrthos().length);
	
			if (rots.size() == 0)
				System.err.println("Warning: rots.size() == 0 in Feature.instantiateFeature()");

			// test all allowed multipliers due to reflection
			for (final int reflectionMult : reflections)
			{
				boolean moreReflectionsRelevant = false;

				// test every rotation
				for (int rotIdx = 0; rotIdx < rots.size(); ++rotIdx)
				{
					boolean moreRotationsRelevant = false;
					final float rot = rots.get(rotIdx);

					boolean allElementsAbsolute = true;

					// first compute instances with resolved action positions
					final List<FeatureInstance> instancesWithActions = new ArrayList<FeatureInstance>(1);
					final FeatureInstance baseInstance = new FeatureInstance(this, siteIdx, reflectionMult, rot, instanceType);

					if (this instanceof AbsoluteFeature)
					{
						// this case is easy, actions specified in terms of absolute positions
						final AbsoluteFeature absThis = (AbsoluteFeature) this;

						if (toPosConstraint < 0 || toPosConstraint == absThis.toPosition)
						{
							if (fromPosConstraint < 0 || fromPosConstraint == absThis.fromPosition)
							{
								baseInstance.setAction(absThis.toPosition, absThis.fromPosition);
								baseInstance.setLastAction(absThis.lastToPosition, absThis.lastFromPosition);
								instancesWithActions.add(baseInstance);
							}
						}
					}
					else
					{
						// this is the hard case, we'll have to follow along any non-null Walks
						// like the Walks above, these can split up again in cells with odd edge counts
						final RelativeFeature relThis = (RelativeFeature) this;
						final Walk toWalk = relThis.toPosition;
						final Walk fromWalk = relThis.fromPosition;
						final Walk lastToWalk = relThis.lastToPosition;
						final Walk lastFromWalk = relThis.lastFromPosition;

						// need to walk the last-to-Walk
						TIntArrayList possibleLastToPositions;
						if (lastToWalk == null)
						{
							possibleLastToPositions = TIntArrayList.wrap(-1);
						}
						else
						{
							possibleLastToPositions = lastToWalk.resolveWalk(game, anchorSite, rot, reflectionMult);

							final TFloatArrayList steps = lastToWalk.steps();

							if (steps.size() > 0)
							{
								// we actually do have a meaningful Walk,
								// so rotations will change things
								moreRotationsRelevant = true;

								// reflections become relevant if we
								// have non-0, non-0.5 turns
								if (!moreReflectionsRelevant)
								{
									for (int step = 0; step < steps.size(); ++step)
									{
										final float turn = steps.getQuick(step);

										if (turn != 0.f && turn != 0.5f && turn != -0.5f)
										{
											moreReflectionsRelevant = true;
										}
									}
								}
							}
						}

						for (int lastToPosIdx = 0; lastToPosIdx < possibleLastToPositions.size(); ++lastToPosIdx)
						{
							final int lastToPos = possibleLastToPositions.getQuick(lastToPosIdx);

							if 
							(
								(lastToWalk == null || lastToPos >= 0) 
								&&
								(lastToConstraint < 0 || lastToPos < 0 || lastToPos == lastToConstraint)
							)
							{
								// need to walk the last-from-Walk
								TIntArrayList possibleLastFromPositions;
								if (lastFromWalk == null)
								{
									possibleLastFromPositions = TIntArrayList.wrap(-1);
								}
								else
								{
									possibleLastFromPositions = lastFromWalk.resolveWalk(game, anchorSite, rot, reflectionMult);

									final TFloatArrayList steps = lastFromWalk.steps();

									if (steps.size() > 0)
									{
										// we actually do have a meaningful
										// Walk, so rotations will change things
										moreRotationsRelevant = true;

										// reflections become relevant if we
										// have non-0, non-0.5 turns
										if (!moreReflectionsRelevant)
										{
											for (int step = 0; step < steps.size(); ++step)
											{
												final float turn = steps.getQuick(step);

												if (turn != 0.f && turn != 0.5f && turn != -0.5f)
												{
													moreReflectionsRelevant = true;
												}
											}
										}
									}
								}

								for (int lastFromPosIdx = 0; lastFromPosIdx < possibleLastFromPositions.size(); ++lastFromPosIdx)
								{
									final int lastFromPos = possibleLastFromPositions.getQuick(lastFromPosIdx);

									if 
									(
										(lastFromWalk == null || lastFromPos >= 0) 
										&&
										(lastFromConstraint < 0 || lastFromPos < 0 || lastFromPos == lastFromConstraint)
									)
									{
										// need to walk the to-Walk
										TIntArrayList possibleToPositions;
										if (toWalk == null)
										{
											possibleToPositions = TIntArrayList.wrap(-1);
										}
										else
										{
											possibleToPositions = toWalk.resolveWalk(game, anchorSite, rot, reflectionMult);

											final TFloatArrayList steps = toWalk.steps();

											if (steps.size() > 0)
											{
												// we actually do have a
												// meaningful Walk, so rotations
												// will change things
												moreRotationsRelevant = true;

												// reflections become relevant
												// if we have non-0,
												// non-0.5 turns
												if (!moreReflectionsRelevant)
												{
													for (int step = 0; step < steps.size(); ++step)
													{
														final float turn = steps.getQuick(step);

														if (turn != 0.f && turn != 0.5f && turn != -0.5f)
														{
															moreReflectionsRelevant = true;
														}
													}
												}
											}
										}

										for (int toPosIdx = 0; toPosIdx < possibleToPositions.size(); ++toPosIdx)
										{
											final int toPos = possibleToPositions.getQuick(toPosIdx);

											if (toPos == -1 && toWalk != null)
												continue;
											
											if (toPosConstraint >= 0 && toPos >= 0 && toPosConstraint != toPos)
												continue;

											// need to walk the from-Walk
											TIntArrayList possibleFromPositions;
											if (fromWalk == null)
											{
												possibleFromPositions = TIntArrayList.wrap(-1);
											}
											else
											{
												possibleFromPositions = fromWalk.resolveWalk(
														game, anchorSite, rot, reflectionMult);

												final TFloatArrayList steps = fromWalk.steps();

												if (steps.size() > 0)
												{
													// we actually do have a meaningful Walk,
													// so rotations will change things
													moreRotationsRelevant = true;

													// reflections become relevant if we
													// have non-0, non-0.5 turns
													if (!moreReflectionsRelevant)
													{
														for (int step = 0; step < steps.size(); ++step)
														{
															final float turn = steps.getQuick(step);

															if (turn != 0.f && turn != 0.5f && turn != -0.5f)
															{
																moreReflectionsRelevant = true;
															}
														}
													}
												}
											}

											for (int fromPosIdx = 0; fromPosIdx < possibleFromPositions.size(); ++fromPosIdx)
											{
												final int fromPos = possibleFromPositions.getQuick(fromPosIdx);
												
												if (fromPos == -1 && fromWalk != null)
													continue;
												
												if (fromPosConstraint >= 0 && fromPos >= 0 && fromPosConstraint != fromPos)
													continue;

												final FeatureInstance newInstance = new FeatureInstance(baseInstance);
												newInstance.setAction(toPos, fromPos);
												newInstance.setLastAction(lastToPos, lastFromPos);
												instancesWithActions.add(newInstance);
											}
										}
									}
								}
							}
						}
					}

					// try to make the pattern fit
					//
					// we'll usually just be creating a single new instance here, but on boards
					// with cells with odd numbers of edges (e.g. triangular cells), every step of
					// a Walk (except for the first step) can get split up into two different
					// positions.
					List<FeatureInstance> instancesWithElements = new ArrayList<FeatureInstance>(instancesWithActions);

					for (final FeatureElement element : pattern.featureElements())
					{
						final List<FeatureInstance> replaceNewInstances = new ArrayList<FeatureInstance>(instancesWithElements.size());
						
						if (element instanceof RelativeFeatureElement)
							allElementsAbsolute = false;

						for (final FeatureInstance instance : instancesWithElements) // usually just size 1
						{
							// first determine where we're testing for something
							TIntArrayList testSites = new TIntArrayList(1); // usually just a single site

							if (element instanceof AbsoluteFeatureElement)
							{
								final AbsoluteFeatureElement absElement = (AbsoluteFeatureElement) element;
								testSites.add(absElement.position());
							}
							else
							{
								final RelativeFeatureElement relElement = (RelativeFeatureElement) element;

								// need to walk the Walk
								testSites = relElement.walk().resolveWalk(game, anchorSite, rot, reflectionMult);

								final TFloatArrayList steps = relElement.walk().steps();

								if (steps.size() > 0)
								{
									// we actually do have a meaningful Walk,
									// so rotations will change things
									moreRotationsRelevant = true;

									// reflections become relevant if we
									// have non-0, non-0.5 turns
									if (!moreReflectionsRelevant)
									{
										for (int step = 0; step < steps.size(); ++step)
										{
											final float turn = steps.getQuick(step);

											if (turn != 0.f && turn != 0.5f && turn != -0.5f)
											{
												moreReflectionsRelevant = true;
											}
										}
									}
								}
							}

							// for every possible site that our Walk might be specifying, check the type
							for (int testSiteIdx = 0; testSiteIdx < testSites.size(); ++testSiteIdx)
							{
								final int testSite = testSites.getQuick(testSiteIdx);
								final ElementType type = element.type();

								if (type == ElementType.Empty) // entry in "empty" BitSet must (not) be on
								{
									if (testSite >= 0)
									{
										final FeatureInstance newInstance = new FeatureInstance(instance);

										if (newInstance.addTest(container, BitSetTypes.Empty, testSite, !element.not()))
										{
											replaceNewInstances.add(newInstance);
										}
									}
									else if (element.not()) // off board is also not empty
									{
										final FeatureInstance newInstance = new FeatureInstance(instance);
										newInstance.addInitTimeElement(element);
										replaceNewInstances.add(newInstance);	
									}
								}
								else if (type == ElementType.Friend) // entry in "who" BitSetS must (not) equal the player
								{
									if (testSite >= 0)
									{
										final FeatureInstance newInstance = new FeatureInstance(instance);

										if (newInstance.addTest(container, BitSetTypes.Who, testSite, !element.not(), player))
										{
											replaceNewInstances.add(newInstance);
										}
									}
									else if (element.not()) // off board is also not a friend
									{
										final FeatureInstance newInstance = new FeatureInstance(instance);
										newInstance.addInitTimeElement(element);
										replaceNewInstances.add(newInstance);	
									}
								}
								else if (type == ElementType.Enemy)
								{
									if (element.not())
									{
										// we'll have to split up into an
										// off-board detector,
										// an empty detector,
										// and a friend detector
										if (testSite < 0)
										{
											// off-board already detected,
											// no more test needed
											final FeatureInstance newInstance = new FeatureInstance(instance);
											newInstance.addInitTimeElement(element);
											replaceNewInstances.add(newInstance);	
										}
										else
										{
											if (game.players().count() == 2)
											{
												// SPECIAL CASE: in 2-player
												// games, we can just directly
												// test for not-enemy
												final FeatureInstance newInstance = new FeatureInstance(instance);
												if (newInstance.addTest(container, BitSetTypes.Who, testSite, false, player == 1 ? 2 : 1))
												{
													replaceNewInstances.add(newInstance);
												}
											}
											else
											{
												// more than two players, so
												// need separate checks for
												// empty and friend
												FeatureInstance newInstance = new FeatureInstance(instance);
												if (newInstance.addTest(container, BitSetTypes.Empty, testSite, true))
												{
													replaceNewInstances.add(newInstance);
												}

												newInstance = new FeatureInstance(instance);
												if (newInstance.addTest(container, BitSetTypes.Who, testSite, true, player))
												{
													replaceNewInstances.add(newInstance);
												}
											}
										}
									}
									else if (testSite >= 0)
									{
										if (game.players().count() == 2)
										{
											// SPECIAL case: in 2-player
											// games, we can just test for
											// enemy directly
											final FeatureInstance newInstance = new FeatureInstance(instance);
											if (newInstance.addTest(container, BitSetTypes.Who, testSite, true, player == 1 ? 2 : 1))
											{
												replaceNewInstances.add(newInstance);
											}
										}
										else
										{
											// game with more than 2 players;
											// need an instance that tests for
											// not-empty as well as not-player
											final FeatureInstance newInstance = new FeatureInstance(instance);
											if (newInstance.addTest(container, BitSetTypes.Empty, testSite, false))
											{
												if (newInstance.addTest(container, BitSetTypes.Who, testSite, false, player))
												{
													replaceNewInstances.add(newInstance);
												}
											}
										}
									}
								}
								else if (type == ElementType.Off)
								{
									if (testSite < 0 != element.not())
									{
										// already performed our off-board check
										final FeatureInstance newInstance = new FeatureInstance(instance);
										newInstance.addInitTimeElement(element);
										replaceNewInstances.add(newInstance);	
									}
								}
								else if (type == ElementType.Any)
								{
									// anything is fine
									final FeatureInstance newInstance = new FeatureInstance(instance);
									newInstance.addInitTimeElement(element);
									replaceNewInstances.add(newInstance);	
								}
								else if (type == ElementType.P1)
								{
									if (testSite >= 0)
									{
										final FeatureInstance newInstance = new FeatureInstance(instance);
										if (newInstance.addTest(container, BitSetTypes.Who, testSite, !element.not(), 1))
										{
											replaceNewInstances.add(newInstance);
										}
									}
									else if (element.not()) // off board is also not player 1
									{
										final FeatureInstance newInstance = new FeatureInstance(instance);
										newInstance.addInitTimeElement(element);
										replaceNewInstances.add(newInstance);	
									}
								}
								else if (type == ElementType.P2)
								{
									if (testSite >= 0)
									{
										final FeatureInstance newInstance = new FeatureInstance(instance);
										if (newInstance.addTest(container, BitSetTypes.Who, testSite, !element.not(), 2))
										{
											replaceNewInstances.add(newInstance);
										}
									}
									else if (element.not()) // off board is also not player 2
									{
										final FeatureInstance newInstance = new FeatureInstance(instance);
										newInstance.addInitTimeElement(element);
										replaceNewInstances.add(newInstance);	
									}
								}
								else if (type == ElementType.Item)
								{
									if (testSite >= 0)
									{
										final FeatureInstance newInstance = new FeatureInstance(instance);
										if (newInstance.addTest(container, BitSetTypes.What, testSite, !element.not(), element.itemIndex()))
										{
											replaceNewInstances.add(newInstance);
										}
									}
									else if (element.not()) // off board is also not an item
									{
										final FeatureInstance newInstance = new FeatureInstance(instance);
										newInstance.addInitTimeElement(element);
										replaceNewInstances.add(newInstance);	
									}
								}
								else if (type == ElementType.IsPos)
								{
									if ((testSite == element.itemIndex()) != element.not()) // test has already succeeded!
									{
										final FeatureInstance newInstance = new FeatureInstance(instance);
										newInstance.addInitTimeElement(element);
										replaceNewInstances.add(newInstance);	
									} // else the test has already failed!
								}
								else if (type == ElementType.Connectivity)
								{
									if (testSite >= 0 && (sites.get(testSite).sortedOrthos().length == element.itemIndex()))
									{
										// Test succeeds if we either have or do not have the correct connectivity
										// (depending on element's not-flag)
										if ((sites.get(testSite).sortedOrthos().length == element.itemIndex()) != element.not())
										{
											final FeatureInstance newInstance = new FeatureInstance(instance);
											newInstance.addInitTimeElement(element);
											replaceNewInstances.add(newInstance);	
										}
									}
								}
								else if (type == ElementType.RegionProximity)
								{
									// Test succeeds if we're (not) closer to specific region than the anchor position
									// (which is at index siteIdx)
									// Either test will always fail for off-board positions
									if (testSite >= 0)
									{
										final int[] distances = game.distancesToRegions()[element.itemIndex()];
										final int anchorDist = distances[siteIdx];
										final int testSiteDist = distances[testSite];
										
										if ((anchorDist > testSiteDist) != element.not())
										{
											// Test passed
											final FeatureInstance newInstance = new FeatureInstance(instance);
											newInstance.addInitTimeElement(element);
											replaceNewInstances.add(newInstance);	
										}
									}
								}
								else if (type == ElementType.LineOfSightOrth)
								{
									if (!element.not())
									{
										// We want a specific piece in orthogonal LOS
										if (testSite >= 0)
										{
											// There's lots of new instances we can create here, for every orth. radial:
											// - An instance that just directly wants a piece of given type next to us
											// - An instance that first wants one empty site, and then a piece
											// - An instance that first wants two empty sites, and then a piece
											// - etc...
											final TIntArrayList runningMustEmptiesList = new TIntArrayList();
											for (final Radial radial : topology.trajectories().radials(instanceType, testSite, AbsoluteDirection.Orthogonal))
											{
												final GraphElement[] steps = radial.steps();
												for (int stepIdx = 1; stepIdx < steps.length; ++stepIdx)
												{
													// Create a feature instance with piece at stepIdx, and everything else leading up to it empty
													final FeatureInstance newInstance = new FeatureInstance(instance);
													boolean failure = 
															(!newInstance.addTest(container, BitSetTypes.What, steps[stepIdx].id(), true, element.itemIndex()));
													
													for (int emptyStepIdx = 0; emptyStepIdx < runningMustEmptiesList.size(); ++emptyStepIdx)
													{
														// See if we can successfully add next must-empty requirement
														failure = 
																(
																	failure 
																	|| 
																	(
																		!newInstance.addTest
																		(
																			container, BitSetTypes.Empty, 
																			runningMustEmptiesList.getQuick(emptyStepIdx), 
																			true
																		)
																	)
																);
													}
													
													if (!failure)
														replaceNewInstances.add(newInstance);
													
													// All other instances we add will have this step as must-empty
													runningMustEmptiesList.add(steps[stepIdx].id());
												}
											}
										}
									}
									else
									{
										// We do NOT want a specific piece type in orthogonal LOS
										if (testSite >= 0)
										{
											// There's lots of new instances we can create here, for every orth. radial:
											// - An instance that just directly wants not empty and not given piece next to us
											// - An instance that first wants one empty site, and then not empty and not given piece
											// - An instance that first wants two empty sites, and then not empty and not given piece
											// - etc...
											final TIntArrayList runningMustEmptiesList = new TIntArrayList();
											for (final Radial radial : topology.trajectories().radials(instanceType, testSite, AbsoluteDirection.Orthogonal))
											{
												final GraphElement[] steps = radial.steps();
												for (int stepIdx = 1; stepIdx < steps.length; ++stepIdx)
												{
													// Create a feature instance with not empty and not given piece at stepIdx, 
													// and everything else leading up to it empty
													final FeatureInstance newInstance = new FeatureInstance(instance);
													boolean failure = 
															(!newInstance.addTest(container, BitSetTypes.What, steps[stepIdx].id(), false, element.itemIndex()));
													failure = failure ||
															(!newInstance.addTest(container, BitSetTypes.Empty, steps[stepIdx].id(), false));
													
													for (int emptyStepIdx = 0; emptyStepIdx < runningMustEmptiesList.size(); ++emptyStepIdx)
													{
														// See if we can successfully add next must-empty requirement
														failure = 
																(
																	failure 
																	|| 
																	(
																		!newInstance.addTest
																		(
																			container, BitSetTypes.Empty, 
																			runningMustEmptiesList.getQuick(emptyStepIdx), 
																			true
																		)
																	)
																);
													}
													
													if (!failure)
														replaceNewInstances.add(newInstance);
													
													// All other instances we add will have this step as must-empty
													runningMustEmptiesList.add(steps[stepIdx].id());
												}
											}
										}
									}
								}
								else if (type == ElementType.LineOfSightDiag)
								{
									if (!element.not())
									{
										// We want a specific piece in diagonal LOS
										if (testSite >= 0)
										{
											// There's lots of new instances we can create here, for every orth. radial:
											// - An instance that just directly wants a piece of given type next to us
											// - An instance that first wants one empty site, and then a piece
											// - An instance that first wants two empty sites, and then a piece
											// - etc...
											final TIntArrayList runningMustEmptiesList = new TIntArrayList();
											for (final Radial radial : topology.trajectories().radials(instanceType, testSite, AbsoluteDirection.Diagonal))
											{
												final GraphElement[] steps = radial.steps();
												for (int stepIdx = 1; stepIdx < steps.length; ++stepIdx)
												{
													// Create a feature instance with piece at stepIdx, and everything else leading up to it empty
													final FeatureInstance newInstance = new FeatureInstance(instance);
													boolean failure = 
															(!newInstance.addTest(container, BitSetTypes.What, steps[stepIdx].id(), true, element.itemIndex()));
													
													for (int emptyStepIdx = 0; emptyStepIdx < runningMustEmptiesList.size(); ++emptyStepIdx)
													{
														// See if we can successfully add next must-empty requirement
														failure = 
																(
																	failure 
																	|| 
																	(
																		!newInstance.addTest
																		(
																			container, BitSetTypes.Empty, 
																			runningMustEmptiesList.getQuick(emptyStepIdx), 
																			true
																		)
																	)
																);
													}
													
													if (!failure)
														replaceNewInstances.add(newInstance);
													
													// All other instances we add will have this step as must-empty
													runningMustEmptiesList.add(steps[stepIdx].id());
												}
											}
										}
									}
									else
									{
										// We do NOT want a specific piece type in diagonal LOS
										if (testSite >= 0)
										{
											// There's lots of new instances we can create here, for every orth. radial:
											// - An instance that just directly wants not empty and not given piece next to us
											// - An instance that first wants one empty site, and then not empty and not given piece
											// - An instance that first wants two empty sites, and then not empty and not given piece
											// - etc...
											final TIntArrayList runningMustEmptiesList = new TIntArrayList();
											for (final Radial radial : topology.trajectories().radials(instanceType, testSite, AbsoluteDirection.Diagonal))
											{
												final GraphElement[] steps = radial.steps();
												for (int stepIdx = 1; stepIdx < steps.length; ++stepIdx)
												{
													// Create a feature instance with not empty and not given piece at stepIdx, 
													// and everything else leading up to it empty
													final FeatureInstance newInstance = new FeatureInstance(instance);
													boolean failure = 
															(!newInstance.addTest(container, BitSetTypes.What, steps[stepIdx].id(), false, element.itemIndex()));
													failure = failure ||
															(!newInstance.addTest(container, BitSetTypes.Empty, steps[stepIdx].id(), false));
													
													for (int emptyStepIdx = 0; emptyStepIdx < runningMustEmptiesList.size(); ++emptyStepIdx)
													{
														// See if we can successfully add next must-empty requirement
														failure = 
																(
																	failure 
																	|| 
																	(
																		!newInstance.addTest
																		(
																			container, BitSetTypes.Empty, 
																			runningMustEmptiesList.getQuick(emptyStepIdx), 
																			true
																		)
																	)
																);
													}
													
													if (!failure)
														replaceNewInstances.add(newInstance);
													
													// All other instances we add will have this step as must-empty
													runningMustEmptiesList.add(steps[stepIdx].id());
												}
											}
										}
									}
								}
								else
								{
									System.err.println("Warning: Element Type " + type + " not supported by Feature.instantiateFeature()");
								}
							}
						}

						instancesWithElements = replaceNewInstances;
					}

					if (allElementsAbsolute)
					{
						// only need to consider 1 possible rotation if all element positions are
						// absolute
						moreRotationsRelevant = false;

						if (this instanceof AbsoluteFeature)
						{
							// if actions are ALSO specified absolutely, we also don't need to consider more
							// sites
							moreSitesRelevant = false;
						}
					}

					instances.addAll(instancesWithElements);

					if (!moreRotationsRelevant)
					{
						break;
					}
				}

				if (!moreReflectionsRelevant)
				{
					break;
				}
			}

			if (!moreSitesRelevant)
			{
				break;
			}
		}

		return FeatureInstance.deduplicate(instances);
	}

	//-------------------------------------------------------------------------

	/**
	 * This method expects to only be called with two features that are already
	 * known to be compatible (e.g. because they have already successfully fired
	 * together for a single state+action pair)
	 * 
	 * @param game
	 * @param a
	 * @param b
	 * @return A new feature constructed by combining the features of instances a
	 *         and b
	 */
	public static SpatialFeature combineFeatures(final Game game, final FeatureInstance a, final FeatureInstance b)
	{
		final SpatialFeature featureA = a.feature();
		final SpatialFeature featureB = b.feature();
		final Pattern patternA = featureA.pattern();
		final Pattern patternB = featureB.pattern();
		
		// If a and b have different anchors, we can only preserve region-proximity requirements 
		// from one of the two instances, because they are relative to the anchor.
		boolean bHasRegionProxim = false;
		
		if (a.anchorSite() != b.anchorSite())
		{
			for (final FeatureElement elemB : patternB.featureElements())
			{
				if (elemB.type() == ElementType.RegionProximity)
				{
					bHasRegionProxim = true;
					
					boolean aHasRegionProxim = false;
					for (final FeatureElement elemA : patternA.featureElements())
					{
						if (elemA.type() == ElementType.RegionProximity)
						{
							aHasRegionProxim = true;
							break;
						}
					}
					
					if (!aHasRegionProxim)
					{
						// Instead of proceeding (and discarding region proxim from B), we'll just flip A and B
						return combineFeatures(game, b, a);
					}
					
					break;
				}
			}
		}

		// Compute extra rotation we should apply to pattern B
		final float requiredBRotation = b.reflection() * b.rotation() - a.reflection() * a.rotation();
		//System.out.println("requiredBRotation = " + requiredBRotation);

		// Create modified copies of Patterns and last-from / last-to walks
		final Pattern modifiedPatternA = new Pattern(patternA);
		modifiedPatternA.applyReflection(a.reflection());

		final Pattern modifiedPatternB = new Pattern(patternB);
		
		if (bHasRegionProxim)
		{
			// B has Region Proximity tests, and A must also have some (because otherwise we
			// would have flipped A and B around), and anchors are different (otherwise we wouldn't
			// have checked whether or not B has Region Proximity tests).
			//
			// We can only preserve those from A, so we should discard any in pattern B
			final List<FeatureElement> newElementsList = new ArrayList<FeatureElement>(modifiedPatternB.featureElements().length);
			for (final FeatureElement el : modifiedPatternB.featureElements())
				newElementsList.add(el);
			
			for (int i = newElementsList.size() - 1; i >= 0; --i)
			{
				if (newElementsList.get(i).type() == ElementType.RegionProximity)
					newElementsList.remove(i);
			}
			
			modifiedPatternB.setFeatureElements(newElementsList.toArray(new FeatureElement[newElementsList.size()]));
		}
		
		modifiedPatternB.applyReflection(b.reflection());
		modifiedPatternB.applyRotation(requiredBRotation);
		
		final List<? extends TopologyElement> sites = game.graphPlayElements();
		
		final Path anchorsPath;
		final Walk anchorsWalk;
		if (a.anchorSite() != b.anchorSite())
		{
			anchorsPath = 
					GraphSearch.shortestPathTo
					(
						game, 
						sites.get(a.anchorSite()), 
						sites.get(b.anchorSite())
					);
			
			// A temporary solution to avoid issues in graphs with multiple disconnected
			// subgraphs. TODO find a better permanent fix
			if (anchorsPath == null)
			{
				return a.feature().rotatedCopy(0.f);
			}
			
			anchorsWalk = anchorsPath.walk();
			//System.out.println("anchors walk = " + anchorsWalk);
			
			//anchorsWalk.applyReflection(b.reflection());
			anchorsWalk.applyRotation(-a.rotation() * a.reflection());
			modifiedPatternB.prependWalkWithCorrection(anchorsWalk, anchorsPath, a.rotation(), a.reflection());
			
			//System.out.println("corrected anchors walk = " + anchorsWalk);
		}
		else
		{
			anchorsPath = null;
			anchorsWalk = null;
		}
				
		final Pattern newPattern = Pattern.merge(modifiedPatternA, modifiedPatternB);

		if (featureA instanceof AbsoluteFeature && featureB instanceof AbsoluteFeature)
		{
			final AbsoluteFeature absA = (AbsoluteFeature) featureA;
			final AbsoluteFeature absB = (AbsoluteFeature) featureB;
			final AbsoluteFeature newFeature = new AbsoluteFeature(newPattern,
					Math.max(absA.toPosition, absB.toPosition), Math.max(absA.fromPosition, absB.fromPosition));
			newFeature.normalise(game);
			newFeature.pattern().removeRedundancies();

			if (!newFeature.pattern().isConsistent())
			{
				System.err.println("Generated inconsistent pattern: " + newPattern);
				System.err.println("active feature A = " + featureA);
				System.err.println("rot A = " + a.rotation());
				System.err.println("ref A = " + a.reflection());
				System.err.println("anchor A = " + a.anchorSite());
				System.err.println("active feature B = " + featureB);
				System.err.println("rot B = " + b.rotation());
				System.err.println("ref B = " + b.reflection());
				System.err.println("anchor B = " + b.anchorSite());
			}

			return newFeature;
		}
		else if (featureA instanceof RelativeFeature && featureB instanceof RelativeFeature)
		{
			final RelativeFeature relA = (RelativeFeature) featureA;
			final RelativeFeature relB = (RelativeFeature) featureB;

			Walk newToPosition = null;
			if (relA.toPosition() != null)
			{
				newToPosition = new Walk(relA.toPosition());
				newToPosition.applyReflection(a.reflection());
			}
			else if (relB.toPosition() != null)
			{
				newToPosition = new Walk(relB.toPosition());
				
				newToPosition.applyReflection(b.reflection());
				newToPosition.applyRotation(requiredBRotation);
				
				if (anchorsWalk != null)
					newToPosition.prependWalkWithCorrection(anchorsWalk, anchorsPath, a.rotation(), a.reflection());
			}

			Walk newFromPosition = null;
			if (relA.fromPosition() != null)
			{
				newFromPosition = new Walk(relA.fromPosition());
				newFromPosition.applyReflection(a.reflection());
			}
			else if (relB.fromPosition() != null)
			{
				newFromPosition = new Walk(relB.fromPosition());
				
				newFromPosition.applyReflection(b.reflection());
				newFromPosition.applyRotation(requiredBRotation);
				
				if (anchorsWalk != null)
					newFromPosition.prependWalkWithCorrection(anchorsWalk, anchorsPath, a.rotation(), a.reflection());
			}
			
			Walk newLastFromPosition = null;
			if (relA.lastFromPosition() != null)
			{
				newLastFromPosition = new Walk(relA.lastFromPosition());
				newLastFromPosition.applyReflection(a.reflection());
			}
			else if (relB.lastFromPosition() != null)
			{
				newLastFromPosition = new Walk(relB.lastFromPosition());
				
				newLastFromPosition.applyReflection(b.reflection());
				newLastFromPosition.applyRotation(requiredBRotation);
				
				if (anchorsWalk != null)
					newLastFromPosition.prependWalkWithCorrection(anchorsWalk, anchorsPath, a.rotation(), a.reflection());
			}
			
			Walk newLastToPosition = null;
			if (relA.lastToPosition() != null)
			{
				newLastToPosition = new Walk(relA.lastToPosition());
				newLastToPosition.applyReflection(a.reflection());
			}
			else if (relB.lastToPosition() != null)
			{
				newLastToPosition = new Walk(relB.lastToPosition());
				
				newLastToPosition.applyReflection(b.reflection());
				newLastToPosition.applyRotation(requiredBRotation);
				
				if (anchorsWalk != null)
					newLastToPosition.prependWalkWithCorrection(anchorsWalk, anchorsPath, a.rotation(), a.reflection());
			}
			
			if (featureA.graphElementType != featureB.graphElementType)
			{
				System.err.println("WARNING: combining two features for different graph element types!");
			}

			final RelativeFeature newFeature = 
					new RelativeFeature
					(
						newPattern, newToPosition, newFromPosition, newLastToPosition, newLastFromPosition
					);
			newFeature.graphElementType = featureA.graphElementType;

			newFeature.pattern().removeRedundancies();
			//System.out.println("pre-normalise = " + newFeature);
			newFeature.normalise(game);
			//System.out.println("post-normalise = " + newFeature);
			newFeature.pattern().removeRedundancies();

			if (!newFeature.pattern().isConsistent())
			{
				System.err.println("Generated inconsistent pattern: " + newPattern);
				System.err.println("active feature A = " + featureA);
				System.err.println("rot A = " + a.rotation());
				System.err.println("ref A = " + a.reflection());
				System.err.println("anchor A = " + a.anchorSite());
				System.err.println("active feature B = " + featureB);
				System.err.println("rot B = " + b.rotation());
				System.err.println("ref B = " + b.reflection());
				System.err.println("anchor B = " + b.anchorSite());
			}
			
			//System.out.println("constructed " + newFeature + " from " + a + " and " + b);
			
			return newFeature;
		}
		else
		{
			// TODO need to think of how we want to combine absolute
			// with relative features
		}

		// this should never happen
		System.err.println("WARNING: Feature.combineFeatures() returning null!");
		return null;
	}

	//-------------------------------------------------------------------------

	/**
	 * Simplifies the Feature, by: 
	 * 1) Getting the first turn of as many Walks as
	 * possible down to 0.0 
	 * 2) Making all turns positive if they were originally all
	 * negative.
	 * 3) Making sure any turns that are very close to one of the game's
	 * rotations are set to precisely that rotation (for example, some of the
	 * modifications described above can result in turns of 0.49999997 due to
	 * floating point inaccuracies, which should instead be set to 0.5).
	 * 4) Preferring small turns in opposite direction over large turns.
	 * 5) Ensuring all turns are in [-1.0, 1.0].
	 * 6) Setting any turns of -0.5 to +0.5, and turns of -0.0 to +0.0.
	 * 
	 * The first modification will only be done for features that allow all
	 * rotations, and the second will only be done for patterns that allow
	 * reflection.
	 * 
	 * After these modifications, the originals can still be re-obtained through
	 * rotation and/or reflection.
	 * 
	 * @param game
	 */
	public void normalise(final Game game)
	{
		final float[] allGameRotations = Walk.allGameRotations(game);

		// if the absolute difference between a turn in a Walk and one of the
		// game's legal rotations is less than this tolerance level, we treat
		// them as equal
		//
		// for example, on a hexagonal grid the tolerance level will be about
		// (1/6) / 100 = 0.16666667 / 100 ~= 0.0016666667
		final float turnEqualTolerance = (allGameRotations[1] - allGameRotations[0]) / 100.f;

		// let's first make sure our allowedRotations array has clean floating
		// point numbers
		final TFloatArrayList allowedRotations = pattern.allowedRotations();
		if (allowedRotations != null)
		{
			for (int i = 0; i < allowedRotations.size(); ++i)
			{
				final float allowedRot = allowedRotations.getQuick(i);

				for (int j = 0; j < allGameRotations.length; ++j)
				{
					if (Math.abs(allowedRot - allGameRotations[j]) < turnEqualTolerance)
					{
						// this can only be close to 0.f if allowedRot is
						// positive, or if both are already approx. equal to 0.f
						allowedRotations.setQuick(i, allGameRotations[j]);
						break;
					}
					else if (Math.abs(allGameRotations[j] + allowedRot) < turnEqualTolerance)
					{
						// this can only be close to 0.f if allowedRot is
						// negative, or if both are already approx. equal to 0.f
						allowedRotations.setQuick(i, -allGameRotations[j]);
						break;
					}
				}
			}
		}
		
		// Collect all the lists of steps we want to look at / modify (can handle them all as a single big batch)
		final List<TFloatArrayList> stepsLists = new ArrayList<TFloatArrayList>(pattern.featureElements().length + 4);
		
		if (this instanceof RelativeFeature)
		{
			for (final FeatureElement featureElement : pattern.featureElements())
			{
				stepsLists.add(((RelativeFeatureElement) featureElement).walk().steps);
			}
			
			final RelativeFeature relFeature = (RelativeFeature) this;
			for 
			(
				final Walk walk : new Walk[]
				{ 
					relFeature.fromPosition, relFeature.toPosition, 
					relFeature.lastFromPosition, relFeature.lastToPosition 
				}
			)
			{
				if (walk != null)
					stepsLists.add(walk.steps);
			}
		}
		
		// Make sure we don't have any steps outside of [-1.0, 1.0]
		for (final TFloatArrayList steps : stepsLists)
		{
			for (int i = 0; i < steps.size(); ++i)
			{
				float turn = steps.getQuick(i);

				while (turn < -1.f)
					turn += 1.f;

				while (turn > 1.f)
					turn -= 1.f;

				steps.setQuick(i, turn);
			}
		}

		if (allowedRotations == null || Arrays.equals(allowedRotations.toArray(), allGameRotations))
		{
			// All rotations are allowed

			// Find the most common turn among the first steps of all Walks
			// with length > 0
			float mostCommonTurn = Float.MAX_VALUE;
			int numOccurrences = 0;
			final TFloatIntMap occurrencesMap = new TFloatIntHashMap();
			
			for (final TFloatArrayList steps : stepsLists)
			{
				if (steps.size() > 0)
				{
					final float turn = steps.getQuick(0);
					final int newOccurrences = occurrencesMap.adjustOrPutValue(turn, 1, 1);

					if (newOccurrences > numOccurrences)
					{
						numOccurrences = newOccurrences;
						mostCommonTurn = turn;
					}
					else if (newOccurrences == numOccurrences)
					{
						// Prioritise small turns in case of tie
						mostCommonTurn = Math.min(mostCommonTurn, turn);
					}
				}
			}
			
			if (mostCommonTurn != 0.f)
			{
				// Now subtract that most common turn from the first step of
				// every walk
				for (final TFloatArrayList steps : stepsLists)
				{
					if (steps.size() > 0)
					{
						steps.setQuick(0, steps.getQuick(0) - mostCommonTurn);
					}
				}
			}
		}

		// Prefer small turns in opposite direction over large turns
		for (final TFloatArrayList steps : stepsLists)
		{
			for (int i = 0; i < steps.size(); ++i)
			{
				final float step = steps.getQuick(i);
				if (step > 0.5f)
				{
					steps.setQuick(i, step - 1.f);
				}
				else if (step < -0.5f)
				{
					steps.setQuick(i, step + 1.f);
				}
			}
		}

		if (pattern.allowsReflection())
		{
			// Reflection is allowed

			// first figure out if we have any positive turns
			boolean havePositiveTurns = false;
			
			for (final TFloatArrayList steps : stepsLists)
			{
				for (int i = 0; i < steps.size(); ++i)
				{
					if (steps.getQuick(i) > 0.f)
					{
						havePositiveTurns = true;
						break;
					}
				}
				
				if (havePositiveTurns)
				{
					break;
				}
			}

			// if we didn't find any, we can multiply all the turns
			// by -1 (and reflection can later turn this back for us)
			if (!havePositiveTurns)
			{
				for (final TFloatArrayList steps : stepsLists)
				{
					for (int i = 0; i < steps.size(); ++i)
					{
						steps.setQuick(i, steps.getQuick(i) * -1.f);
					}
				}
			}
		}

		// Make sure floating point math didn't mess anything up with the turn
		// values if possible, we prefer them to PRECISELY match the perfect
		// fractions given by the game's possible rotations, because the
		// floating point numbers are used in hashCode() and equals() methods
		//
		// here, we'll also set any turns of -0.f to 0.f (don't like negative 0)
		for (final TFloatArrayList steps : stepsLists)
		{
			for (int i = 0; i < steps.size(); ++i)
			{
				final float turn = steps.getQuick(i);

				if (turn == -0.f)
				{
					steps.setQuick(i, 0.f);
				}
				else
				{
					for (int j = 0; j < allGameRotations.length; ++j)
					{
						if (Math.abs(turn - allGameRotations[j]) < turnEqualTolerance)
						{
							// this can only be close to 0.f if turn is positive,
							// or if both are already approx. equal to 0.f
							steps.setQuick(i, allGameRotations[j]);
							break;
						}
						else if (Math.abs(allGameRotations[j] + turn) < turnEqualTolerance)
						{
							// this can only be close to 0.f if turn is negative,
							// or if both are already approx. equal to 0.f
							steps.setQuick(i, -allGameRotations[j]);
							break;
						}
					}
				}
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @param features
	 * @return Copy of given list of features, with duplicates removed
	 */
	public static List<SpatialFeature> deduplicate(final List<SpatialFeature> features)
	{
		// TODO doing this with a set is almost always going to be faster
		final List<SpatialFeature> deduplicated = new ArrayList<SpatialFeature>(features.size());

		for (final SpatialFeature feature : features)
		{
			boolean foundDuplicate = false;

			for (final SpatialFeature alreadyAdded : deduplicated)
			{
				if (alreadyAdded.equals(feature))
				{
					// System.out.println("removing " + feature + " (equal to " + alreadyAdded +
					// ")");
					foundDuplicate = true;
					break;
				}
			}

			if (!foundDuplicate)
			{
				deduplicated.add(feature);
			}
		}

		return deduplicated;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param game The game for which we're generating features (passed such that
	 * 	we can normalise)
	 * @return List of new spatial features that generalise (in a game-independent
	 * manner, i.e. having strictly fewer elements in patterns or action-specifiers)
	 * this feature.
	 */
	public abstract List<SpatialFeature> generateGeneralisers(final Game game);
	
	//-------------------------------------------------------------------------

//	/**
//	 * @return The feature's comment
//	 */
//	public String comment()
//	{
//		return comment;
//	}
//
//	/**
//	 * @param newComment New comment for the feature
//	 * @return this Feature object (after modification)
//	 */
//	public SpatialFeature setComment(final String newComment)
//	{
//		comment = newComment;
//		return this;
//	}
	
	/**
	 * @return This feature's graph element type
	 */
	public SiteType graphElementType()
	{
		return graphElementType;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean equals(final Object other)
	{
		if (!(other instanceof SpatialFeature))
			return false;

		final SpatialFeature otherFeature = (SpatialFeature) other;
		return pattern.equals(otherFeature.pattern);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		return result;
	}

	//-------------------------------------------------------------------------

	/**
	 * equals() function that ignores restrictions on rotation / reflection in
	 * pattern.
	 * 
	 * @param other
	 * @return Result of test.
	 */
	public boolean equalsIgnoreRotRef(final SpatialFeature other)
	{
		return pattern.equalsIgnoreRotRef(other.pattern);
	}

	/**
	 * hashCode() function that ignores restrictions on rotation / reflection in
	 * pattern.
	 * 
	 * @return Hash code.
	 */
	public int hashCodeIgnoreRotRef()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCodeIgnoreRotRef());
		return result;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Simplifies the given list of spatial features by:
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
	 */
	public static List<SpatialFeature> simplifySpatialFeaturesList(final Game game, final List<SpatialFeature> featuresIn)
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
						
						final SpatialFeature keepFeature = featuresToKeep.remove(wrapped).feature();
						
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
			simplified.add(feature.feature());
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
	public static class RotRefInvariantFeature
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
		
		/**
		 * @return The wrapped feature
		 */
		public SpatialFeature feature()
		{
			return feature;
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