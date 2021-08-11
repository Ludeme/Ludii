package metadata.ai.heuristics.terms;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.is.line.IsLine;
import game.functions.ints.IntFunction;
import game.rules.phase.Phase;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.GraphElement;
import game.util.graph.Radial;
import gnu.trove.list.array.TFloatArrayList;
import main.Constants;
import main.ReflectionUtils;
import main.collections.FVector;
import main.collections.ListUtils;
import metadata.ai.heuristics.transformations.HeuristicTransformation;
import other.Ludeme;
import other.context.Context;
import other.location.Location;
import other.state.container.ContainerState;
import other.state.owned.Owned;
import other.topology.TopologyElement;
import other.trial.Trial;

/**
 * Defines a heuristic state value based on a player's potential to
 * complete lines up to a given target length.
 * This mostly follows the description of the N-in-a-Row advisor as
 * described on pages 82-84 of:
 * 	``Browne, C.B. (2009) Automatic generation and evaluation of recombination games. 
 * 	PhD thesis, Queensland University of Technology''.
 *
 * @author Dennis Soemers
 */
public class LineCompletionHeuristic extends HeuristicTerm
{
	
	//-------------------------------------------------------------------------
	
	/** If true, we want to automatically determine a default target length per game board */
	private final boolean autoComputeTargetLength;
	
	/** Our target length */
	private int targetLength;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param transformation An optional transformation to be applied to any 
	 * raw heuristic score outputs.
	 * @param weight The weight for this term in a linear combination of multiple terms.
	 * If not specified, a default weight of $1.0$ is used.
	 * @param targetLength The target length for line completions. If not specified,
	 * we automatically determine a target length based on properties of the game 
	 * rules or board.
	 * 
	 * @example (lineCompletionHeuristic targetLength:3)
	 */
	public LineCompletionHeuristic
	(
		@Name @Opt final HeuristicTransformation transformation,
		@Name @Opt final Float weight,
		@Name @Opt final Integer targetLength
	)
	{
		super(transformation, weight);
		
		if (targetLength == null)
		{
			autoComputeTargetLength = true;
		}
		else
		{
			autoComputeTargetLength = false;
			this.targetLength = targetLength.intValue();
		}
	}
	
	@Override
	public HeuristicTerm copy()
	{
		return new LineCompletionHeuristic(this);
	}
	
	/**
	 * Copy constructor (private, so not visible to grammar)
	 * @param other
	 */
	private LineCompletionHeuristic(final LineCompletionHeuristic other)
	{
		super(other.transformation, Float.valueOf(other.weight));
		this.autoComputeTargetLength = other.autoComputeTargetLength;
		this.targetLength = other.targetLength;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public float computeValue(final Context context, final int player, final float absWeightThreshold)
	{
		final Game game = context.game();
		final Owned owned = context.state().owned();
		final List<? extends Location>[] pieces = owned.positions(player);
		final List<? extends TopologyElement> sites = game.graphPlayElements();
		final boolean[] ignore = new boolean[sites.size()];
		final SiteType siteType = game.board().defaultSite();
				
		final TFloatArrayList lineValues = new TFloatArrayList();
		
		for (final List<? extends Location> piecesList : pieces)
		{
			for (final Location piecePos : piecesList)
			{
				final int pieceSite = piecePos.site();

				if (context.containerId()[pieceSite] > 0)
					continue;	// we only support the main board

				final ContainerState state = context.state().containerStates()[0];
				
				final List<Radial> radials = game.board().graph().trajectories().radials(piecePos.siteType(), pieceSite).distinctInDirection(AbsoluteDirection.Adjacent);
				for (final Radial radial : radials)
				{
					final GraphElement[] path = radial.steps();
					final List<Radial> opposites = radial.opposites();
					
					final List<GraphElement[]> oppositePaths = new ArrayList<GraphElement[]>();
					if (opposites != null)
					{
						for (final Radial opposite : opposites)
						{
							oppositePaths.add(opposite.steps());
						}
					}
					else
					{
						oppositePaths.add(new GraphElement[0]);
					}
					
					// Index 0 is the "current" location; we already know
					// we have a piece there, so can skip checking that
					// and directly add it to our counts
					final int indexBound = Math.min(path.length, targetLength + 1);
					
					final boolean[] endPathsBlocked = new boolean[targetLength];
					final int[] potentialLineLengths = new int[targetLength];
					final int[] realPieces = new int[targetLength];
					
					// Fill all the counts up starting with 1, since we know
					// there's at least 1 piece (the one we're starting from)
					Arrays.fill(potentialLineLengths, 1);
					Arrays.fill(realPieces, 1);

					for (int indexPath = 1; indexPath < indexBound; ++indexPath)
					{
						final int site = path[indexPath].id();
						final int who = state.who(site, siteType);

						if (ignore[site])
						{
							// We've already been here, skip this
							break;
						}
						else if (who != Constants.NOBODY && who != player)
						{
							// An enemy piece
							assert (endPathsBlocked[targetLength - indexPath] == false);
							endPathsBlocked[targetLength - indexPath] = true;
							break;
						}
						else
						{
							for (int j = 0; j < targetLength - indexPath; ++j)
							{
								potentialLineLengths[j] += 1;

								if (who == player)
									realPieces[j] += 1;
							}
						}
					}
					
					for (final GraphElement[] oppositePath : oppositePaths)
					{
						// At best there can be targetLength lines for this radial + opposite combo;
						// There's:
						//	- one line starting in piece pos and following direction
						//	- one line with one piece in opposite direction, and rest in direction
						//	- one line with two pieces in opposite direction, and rest in direction
						// 	- etc.
						
						final boolean[] endOppositePathsBlocked = new boolean[targetLength];
						final boolean[] endPathsBlockedInner = Arrays.copyOf(endPathsBlocked, targetLength);
						final int[] potentialLineLengthsInner = Arrays.copyOf(potentialLineLengths, targetLength);
						final int[] realPiecesInner = Arrays.copyOf(realPieces, targetLength);
						
						// Now the same thing, but in opposite radial
						final int oppositeIndexBound = Math.min(oppositePath.length, targetLength + 1);

						for (int indexPath = 1; indexPath < oppositeIndexBound; ++indexPath)
						{
							final int site = oppositePath[indexPath].id();
							final int who = state.who(site, siteType);

							if (ignore[site])
							{
								// We've already been here, skip this
								break;
							}
							else if (who != Constants.NOBODY && who != player)
							{
								// An enemy piece
								assert (endOppositePathsBlocked[indexPath - 1] == false);
								endOppositePathsBlocked[indexPath - 1] = true;
								break;
							}
							else
							{
								for (int j = indexPath; j < targetLength; ++j)
								{
									potentialLineLengthsInner[j] += 1;

									if (who == player)
										realPiecesInner[j] += 1;
								}
							}
						}
						
						// Compute values for all potential lines along this radial
						for (int j = 0; j < potentialLineLengthsInner.length; ++j)
						{
							if (potentialLineLengthsInner[j] == targetLength)
							{
								// This is a potential line
								float value = (float) realPiecesInner[j] / (float) potentialLineLengthsInner[j];
	
								if (endPathsBlockedInner[j])
									value *= 0.5f;
								if (endOppositePathsBlocked[j])
									value *= 0.5f;
	
								lineValues.add(value);
							}
						}
					}
				}

				// From now on we should ignore any lines including this piece;
				// we've already counted all of them!
				ignore[pieceSite] = true;
			}
		}
		
		// Union of probabilities takes way too long to compute, so we just
		// take average of the top 2 line values
		final int argMax = ListUtils.argMax(lineValues);
		final float maxVal = lineValues.getQuick(argMax);
		lineValues.setQuick(argMax, -1.f);
		final int secondArgMax = ListUtils.argMax(lineValues);
		final float secondMaxVal = lineValues.getQuick(secondArgMax);
		
		return maxVal + secondMaxVal / 2.f;
		
		// compute "union of probabilities" from all line values
		//return MathRoutines.unionOfProbabilities(lineValues);
	}
	
	@Override
	public FVector computeStateFeatureVector(final Context context, final int player)
	{
		final FVector featureVector = new FVector(1);
		featureVector.set(0, computeValue(context, player, -1.f));
		return featureVector;
	}
	
	@Override
	public FVector paramsVector()
	{
		return null;
	}
	
	@Override
	public void init(final Game game)
	{
		if (autoComputeTargetLength)
		{
			// We need to compute target length for this game automatically
			final List<IsLine> lineLudemes = new ArrayList<IsLine>();
			
			if (game.rules().end() != null)
				collectLineLudemes(lineLudemes, game.rules().end(), new HashMap<Object, Set<String>>());
			
			for (final Phase phase : game.rules().phases())
			{
				if (phase != null && phase.end() != null)
					collectLineLudemes(lineLudemes, phase.end(), new HashMap<Object, Set<String>>());
			}
			
			int maxTargetLength = 2;		// anything less than 2 makes no sense
			
			if (lineLudemes.isEmpty())
			{
//				// we'll take the longest distance of any single site to the
//				// centre region (or 2 if that's bigger)
//				final Topology graph = game.board().topology();
//				final SiteType siteType = game.board().defaultSite();
//				final int[] distancesToCentre = graph.distancesToCentre(siteType);
//				
//				if (distancesToCentre != null)
//				{
//					for (final int dist : distancesToCentre)
//					{
//						maxTargetLength = Math.max(maxTargetLength, dist);
//					}
//				}
//				else
//				{
//					// Centres don't exist, instead we'll just pick a large number
//					maxTargetLength = 15;
//				}
				
				// We'll just pick 3 (nice number, but also low, so relatively cheap)
				maxTargetLength = 3;
			}
			else
			{
				final Context dummyContext = new Context(game, new Trial(game));
				
				for (final IsLine line : lineLudemes)
				{
					maxTargetLength = Math.max(maxTargetLength, line.length().eval(dummyContext));
				}
			}
			
			targetLength = maxTargetLength;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return True if heuristic of this type could be applicable to given game
	 */
	public static boolean isApplicableToGame(final Game game)
	{
		if (game.isEdgeGame())
			return false;
		
		final Component[] components = game.equipment().components();
		
		if (components.length <= 1)
			return false;
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Helper method to recursively collect all (line x) ludemes in the subtree
	 * rooted in a given ludeme.
	 * 
	 * @param outList
	 * @param ludeme
	 * @param visited
	 */
	private static void collectLineLudemes
	(
		final List<IsLine> outList, 
		final Ludeme ludeme, 
		final Map<Object, Set<String>> visited
	)
	{
		final Class<? extends Ludeme> clazz = ludeme.getClass();
		final List<Field> fields = ReflectionUtils.getAllFields(clazz);
		
		try
		{
			for (final Field field : fields)
			{
				if (field.getName().contains("$"))
					continue;
				
				field.setAccessible(true);
				
				if ((field.getModifiers() & Modifier.STATIC) != 0)
					continue;
				
				if (visited.containsKey(ludeme) && visited.get(ludeme).contains(field.getName()))
					continue;		// avoid stack overflow
								
				final Object value = field.get(ludeme);
				
				if (!visited.containsKey(ludeme))
					visited.put(ludeme, new HashSet<String>());
				
				visited.get(ludeme).add(field.getName());
				
				if (value != null)
				{
					final Class<?> valueClass = value.getClass();
					
					if (Enum.class.isAssignableFrom(valueClass))
						continue;
					
					if (Ludeme.class.isAssignableFrom(valueClass))
					{
						if (IsLine.class.isAssignableFrom(valueClass))
						{
							final IsLine line = (IsLine) value;
							final IntFunction length = line.length();
							
							if (length.isStatic())
								outList.add(line);
						}
							
						collectLineLudemes(outList, (Ludeme) value, visited);
					}
					else if (valueClass.isArray())
					{
						final Object[] array = ReflectionUtils.castArray(value);
						
						for (final Object element : array)
						{
							if (element != null)
							{
								final Class<?> elementClass = element.getClass();
								
								if (Ludeme.class.isAssignableFrom(elementClass))
								{
									if (IsLine.class.isAssignableFrom(elementClass))
									{
										final IsLine line = (IsLine) element;
										final IntFunction length = line.length();
										
										if (length.isStatic())
											outList.add(line);
									}
									
									collectLineLudemes(outList, (Ludeme) element, visited);
								}
							}
						}
					}
					else if (Iterable.class.isAssignableFrom(valueClass))
					{
						final Iterable<?> iterable = (Iterable<?>) value;
						
						for (final Object element : iterable)
						{
							if (element != null)
							{
								final Class<?> elementClass = element.getClass();
								
								if (Ludeme.class.isAssignableFrom(elementClass))
								{
									if (IsLine.class.isAssignableFrom(elementClass))
									{
										final IsLine line = (IsLine) element;
										final IntFunction length = line.length();
										
										if (length.isStatic())
											outList.add(line);
									}
									
									collectLineLudemes(outList, (Ludeme) element, visited);
								}
							}
						}
					}
				}
			}
		}
		catch (final IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(lineCompletionHeuristic");
		if (transformation != null)
			sb.append(" transformation:" + transformation.toString());
		if (weight != 1.f)
			sb.append(" weight:" + weight);
		if (!autoComputeTargetLength)
			sb.append(" targetLength:" + targetLength);
		sb.append(")");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toStringThresholded(final float threshold)
	{
		boolean shouldPrint = false;
		
		if (Math.abs(weight) >= threshold)
		{
			// No manually specified weights, so they will all default to 1.0,
			// and we have a large enough term-wide weight
			shouldPrint = true;
		}
		
		if (shouldPrint)
		{
			final StringBuilder sb = new StringBuilder();
		
			sb.append("(lineCompletionHeuristic");
			if (transformation != null)
				sb.append(" transformation:" + transformation.toString());
			if (weight != 1.f)
				sb.append(" weight:" + weight);
			if (!autoComputeTargetLength)
				sb.append(" targetLength:" + targetLength);
			sb.append(")");
			
			return sb.toString();
		}
		else
		{
			return null;
		}
	}
	
	//-------------------------------------------------------------------------

}
