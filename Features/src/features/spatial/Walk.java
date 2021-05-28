package features.spatial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import features.spatial.graph_search.Path;
import game.Game;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.StringRoutines;
import other.topology.TopologyElement;

/**
 * A relative position specified by a sequence of turns to take whilst
 * walking<br>
 * <br>
 * In regular mode, every number indicates the fraction of possible clockwise
 * turns to make <br>
 * relative to the previous direction before taking another step. For example:
 * [0, 0.25, 0, -0.5] means: <br>
 * - 0 turns, walk once (straight ahead) <br>
 * - 1/4th of the possible turns clockwise (e.g. one turn on a square cell),
 * walk once, <br>
 * - 0 additional turns, so continue in the direction given by our previous
 * turns <br>
 * - half of the possible turns counter-clockwise (e.g. turning backwards), walk
 * once <br>
 * 
 * @author Dennis Soemers and cambolbro
 */
public class Walk
{
	
	//-------------------------------------------------------------------------
	
	/** Relative turns to make for every step */
	protected TFloatArrayList steps;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public Walk()
	{
		this.steps = new TFloatArrayList(1);
	}
	
	/**
	 * Constructor
	 * @param steps
	 */
	public Walk(final float... steps)
	{
		this.steps = TFloatArrayList.wrap(steps);
	}
	
	/**
	 * Constructor
	 * @param steps
	 */
	public Walk(final TFloatArrayList steps)
	{
		this.steps = new TFloatArrayList(steps);
	}
	
	/**
	 * Constructor
	 * @param other
	 */
	public Walk(final Walk other)
	{
		this.steps = new TFloatArrayList(other.steps());
	}
	
	/**
	 * Constructor
	 * @param string
	 */
	public Walk(final String string)
	{
		final String walkString = string.substring("{".length(), string.length() - "}".length());
		
		if (walkString.length() > 0)
		{
			final String[] stepStrings = walkString.split(",");
			steps = new TFloatArrayList(stepStrings.length);
			
			for (final String stepString : stepStrings)
			{
				final String s = stepString.trim();
				
				if (s.contains("/"))
				{
					final String[] parts = s.split(Pattern.quote("/"));
					steps.add((float) Integer.parseInt(parts[0]) / (float) Integer.parseInt(parts[1]));
				}
				else
				{
					steps.add(Float.parseFloat(stepString.trim()));
				}
			}
		}
		else
		{
			steps = new TFloatArrayList(0);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Applies given reflection multiplier to this Walk
	 * @param reflection
	 */
	public void applyReflection(final int reflection)
	{
		if (reflection == 1)
		{
			// guess we're doing nothing at all
			return;
		}
		
		for (int i = 0; i < steps.size(); ++i)
		{
			steps.setQuick(i, steps.getQuick(i) * reflection);
		}
	}
	
	/**
	 * Applies given rotation to this walk
	 * @param rotation
	 */
	public void applyRotation(final float rotation)
	{
		if (steps.size() > 0)
		{
			steps.setQuick(0, steps.getQuick(0) + rotation);
		}
	}
	
	/**
	 * Adds all steps from the given walk to the end of this walk
	 * @param walk
	 */
	public void appendWalk(final Walk walk)
	{
		steps.add(walk.steps().toArray());
	}
	
	/**
	 * Adds a step corresponding to the given direction to the beginning of the Walk
	 * 
	 * @param step
	 */
	public void prependStep(final float step)
	{
		steps.insert(0, step);
	}
	
	/**
	 * Adds all steps from the given walk to the beginning of this Walk
	 * @param walk
	 */
	public void prependWalk(final Walk walk)
	{
		steps.insert(0, walk.steps().toArray());
	}
	
	/**
	 * Prepends all steps of the given walk to this walk.
	 * Additionally applies a correction to the first step (if it exists) of this walk,
	 * to make sure that it still continues moving in the same direction that it
	 * would have without prepending the new walk.
	 * 
	 * @param walk
	 * @param path The path we followed when walking the prepended walk
	 * @param rotToRevert Already-applied rotation which should be reverted
	 * @param refToRevert Already-applied reflection which should be reverted
	 */
	public void prependWalkWithCorrection
	(
		final Walk walk, 
		final Path path, 
		final float rotToRevert, 
		final int refToRevert
	)
	{
		if (walk.steps.size() == 0)
			return;
		
		if (steps.size() > 0)
		{
			final TopologyElement endSite = path.destination();
			final TopologyElement penultimateSite = path.sites().get(path.sites().size() - 2);
			
			// TODO code duplication with resolveWalk()
			// compute the directions that count as "continuing straight ahead"
			// (will be only one in the case of cells with even number of edges, but two otherwise)
			final TIntArrayList contDirs = new TIntArrayList(2);
	
			final TopologyElement[] sortedOrthos = endSite.sortedOrthos();	
			int fromDir = -1;
	
			for (int orthIdx = 0; orthIdx < sortedOrthos.length; ++orthIdx)
			{
				if 
				(
					sortedOrthos[orthIdx] != null && 
					sortedOrthos[orthIdx].index() == penultimateSite.index()
				)
				{
					fromDir = orthIdx;
					break;
				}
			}
	
			if (fromDir == -1)
			{
				System.err.println("Warning! Walk.prependWalkWithCorrection() could not find fromDir!");
			}
	
			if (sortedOrthos.length % 2 == 0)
			{
				contDirs.add(fromDir + sortedOrthos.length / 2);
			}
			else
			{
				contDirs.add(fromDir + sortedOrthos.length / 2);
				//contDirs.add(fromDir + 1 + sortedOrthos.length / 2);
			}
			
			final float toSubtract = (contDirs.getQuick(0) / (float) sortedOrthos.length) - rotToRevert * refToRevert;
			
			// TODO now just assuming we get a single contDir
//			System.out.println();
//			System.out.println("prepended walk = " + walk);
//			System.out.println("old code would have subtracted: " + walk.steps().sum());
//			System.out.println("new code subtracts: " + toSubtract);
//			System.out.println("steps.getQuick(0) = " + steps.getQuick(0));
//			System.out.println("num orthos = " + sortedOrthos.length);
//			System.out.println("contDir = " + contDirs.getQuick(0));
//			System.out.println("fromDir = " + fromDir);
//			System.out.println("start = " + path.start());
//			System.out.println("end = " + path.destination());
			steps.setQuick(0, steps.getQuick(0) - toSubtract);
		}
			
		steps.insert(0, walk.steps().toArray());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Reference to this Walk's list of steps
	 */
	public TFloatArrayList steps()
	{
		return steps;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Resolves this Walk
	 * @param game Game in which we're resolving a Walk
	 * @param startSite Vertex to start walking from
	 * @param rotModifier Additional rotation to apply to the complete Walk
	 * @param reflectionMult Reflection multiplier
	 * @return Indices of all possible vertices this Walk can end up in 
	 * 	(may be many if we're walking through cells with odd numbers of edges where turns can be ambiguous).
	 */
	public TIntArrayList resolveWalk
	(
		final Game game,
		final TopologyElement startSite, 
		final float rotModifier, 
		final int reflectionMult
	)
	{
//		System.out.println();
//		System.out.println("resolving walk: " + this);
//		System.out.println("starting at vertex: " + startVertex);
//		System.out.println("rotModifier = " + rotModifier);
//		System.out.println("reflectionMult = " + reflectionMult);
		final TIntArrayList results = new TIntArrayList(1);
		
		if (steps.size() > 0)
		{
			final TopologyElement[] sortedOrthos = startSite.sortedOrthos();			
			final TIntArrayList connectionIndices = new TIntArrayList(2);
			
			// apply rotation to first step, 
			// rest of the Walk will then auto-rotate
			float connectionIdxFloat = ((steps.get(0) + rotModifier) * reflectionMult) * sortedOrthos.length;	
			float connectionIdxFractionalPart = connectionIdxFloat - (int) connectionIdxFloat;
			
			if 
			(
				Math.abs(0.5f - connectionIdxFractionalPart) < 0.02f ||
				Math.abs(0.5f + connectionIdxFractionalPart) < 0.02f
			)
			{
				// we're (almost) exactly halfway between two integer indices, so we'll use both
				connectionIndices.add((int) Math.floor(connectionIdxFloat));
				//connectionIndices.add((int) Math.ceil(connectionIdxFloat));
			}
			else	// not almost exactly halfway, so just round and use a single integer index
			{
				connectionIndices.add(Math.round(connectionIdxFloat));
			}
			
			boolean wentOffBoard = false;
			
			for (int c = 0; c < connectionIndices.size(); ++c)
			{
				TopologyElement prevSite = startSite;
				int connectionIdx = connectionIndices.getQuick(c);
				//System.out.println("connectionIdx = " + connectionIdx);
				
				// wrap around... (thanks https://stackoverflow.com/a/4412200/6735980)
				connectionIdx = (connectionIdx % sortedOrthos.length + sortedOrthos.length) % sortedOrthos.length;
				TopologyElement nextSite = sortedOrthos[connectionIdx];
				//System.out.println("nextSite = " + nextSite);
				
				List<TopologyElement> nextSites = Arrays.asList(nextSite);
				List<TopologyElement> prevSites = Arrays.asList(prevSite);
				
				for (int step = 1; step < steps.size(); ++step)
				{
					// apply step to all possible "next vertices"
					//System.out.println("step = " + step);
					
					final List<TopologyElement> newNextSites = new ArrayList<TopologyElement>(nextSites.size());
					final List<TopologyElement> newPrevSites = new ArrayList<TopologyElement>(nextSites.size());
					
					for (int i = 0; i < nextSites.size(); ++i)
					{
						prevSite = prevSites.get(i);
						nextSite = nextSites.get(i);
						//System.out.println("prevVertex = " + prevVertex);
						//System.out.println("nextVertex = " + nextVertex);
						
						if (nextSite == null)
						{
							//System.out.println("went off board");
							wentOffBoard = true;
						}
						else
						{
							// compute the directions that count as "continuing straight ahead"
							// (will be only one in the case of cells with even number of edges, but two otherwise)
							final TIntArrayList contDirs = new TIntArrayList(2);
							
							final TopologyElement[] nextSortedOrthos = nextSite.sortedOrthos();	
							//System.out.println("sorted orthos of " + nextSite + " = " + Arrays.toString(nextSortedOrthos));
							int fromDir = -1;
							
							for (int nextOrthIdx = 0; nextOrthIdx < nextSortedOrthos.length; ++nextOrthIdx)
							{
								if 
								(
									nextSortedOrthos[nextOrthIdx] != null && 
									nextSortedOrthos[nextOrthIdx].index() == prevSite.index()
								)
								{
									fromDir = nextOrthIdx;
									break;
								}
							}
							
							if (fromDir == -1)
							{
								System.err.println("Warning! Walk.resolveWalk() could not find fromDir!");
							}
							
							if (nextSortedOrthos.length % 2 == 0)
							{
								contDirs.add(fromDir + nextSortedOrthos.length / 2);
							}
							else
							{
								contDirs.add(fromDir + nextSortedOrthos.length / 2);
								//contDirs.add(fromDir + 1 + nextSortedOrthos.length / 2);
							}
							
							// for each of these "continue directions", we apply the next rotation 
							// specified in the Walk and move on
							for (int contDirIdx = 0; contDirIdx < contDirs.size(); ++contDirIdx)
							{
								final int contDir = contDirs.getQuick(contDirIdx);
								
								final TIntArrayList nextConnectionIndices = new TIntArrayList(2);
								connectionIdxFloat = 
										contDir + (steps.get(step) * reflectionMult) * nextSortedOrthos.length;
								connectionIdxFractionalPart = connectionIdxFloat - (int) connectionIdxFloat;
								
								if 
								(
									Math.abs(0.5f - connectionIdxFractionalPart) < 0.02f ||
									Math.abs(0.5f + connectionIdxFractionalPart) < 0.02f
								)
								{
									// we're (almost) exactly halfway between two integer indices, so we'll use both
									nextConnectionIndices.add((int) Math.floor(connectionIdxFloat));
									//nextConnectionIndices.add((int) Math.ceil(connectionIdxFloat));
								}
								else	// not almost exactly halfway, so just round and use a single integer index
								{
									nextConnectionIndices.add(Math.round(connectionIdxFloat));
								}
								
								for (int n = 0; n < nextConnectionIndices.size(); ++n)
								{
									// wrap around...
									connectionIdx = 
											(nextConnectionIndices.getQuick(n) % nextSortedOrthos.length + 
													nextSortedOrthos.length) % nextSortedOrthos.length;
									
									final TopologyElement newNextSite = nextSortedOrthos[connectionIdx];
									//System.out.println("newNextSite = " + newNextSite);
									
									newPrevSites.add(nextSite);
									newNextSites.add(newNextSite);
								}
							}
						}
					}
					
					nextSites = newNextSites;
					prevSites = newPrevSites;
				}
				
				// add all destinations reached by the Walk(s)
				for (final TopologyElement destination : nextSites)
				{
					if (destination == null)
					{
						wentOffBoard = true;
					}
					else
					{
						results.add(destination.index());
					}
				}
			}
			
			if (wentOffBoard)
			{
				results.add(-1);
			}
		}
		else	// no real Walk, so just testing on the site's pos
		{
			results.add(startSite.index());
		}
		
		return results;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		
		// We very often have values of 0.f in steps, and otherwise also very often
		// permutations of the same values.
		// The standard hashCode() implementation of TFloatArrayList produces lots
		// of collisions for these types of lists, so we roll our own implementation
		for (int i = 0; i < steps.size(); ++i)
		{
			result = prime * result + Float.floatToIntBits((steps.getQuick(i) + 1.f) * 663608941.737f);
		}
		
		result = prime * result + ((steps == null) ? 0 : steps.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object other)
	{
		if (!(other instanceof Walk))
			return false;
		
		return steps.equals(((Walk) other).steps());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return List of all possible sensible rotations for the given game's
	 * board
	 */
	public static TFloatArrayList allGameRotations(final Game game)
	{
		final TIntArrayList connectivities = game.board().topology().trueOrthoConnectivities(game);
		final TFloatArrayList rotations = new TFloatArrayList();
		
		for (int i = connectivities.size() - 1; i >= 0; --i)
		{
			final int connectivity = connectivities.getQuick(i);
			
			if (connectivity == 0)
				continue;
			
			boolean alreadyHandled = false;
			
			for (int j = i + 1; j < connectivities.size(); ++j)
			{
				if (connectivities.getQuick(j) % connectivity == 0)
				{
					// All rotations that this connectivity would generate have
					// already been generated by another (larger) connectivity number
					alreadyHandled = true;
					break;
				}
			}
			
			if (!alreadyHandled)
			{
				final TFloatArrayList newRots = rotationsForNumOrthos(connectivity);
				
				// Just double-checking that we don't add any duplicates
				for (int j = 0; j < newRots.size(); ++j)
				{
					if (!rotations.contains(newRots.getQuick(j)))
						rotations.add(newRots.getQuick(j));
				}
			}
		}
		
		return rotations;
	}
	
	/**
	 * @param numOrthos
	 * @return List of all possible rotations for cell with given number of
	 * 	orthogonals.
	 */
	public static TFloatArrayList rotationsForNumOrthos(final int numOrthos)
	{
		final TFloatArrayList allowedRotations = new TFloatArrayList();
		
		for (int i = 0; i < numOrthos; ++i)
		{
			allowedRotations.add((float) i / (float) numOrthos);
		}
		
		return allowedRotations;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String str = "";
		
		for (int i = 0; i < steps.size(); ++i)
		{
			str += StringRoutines.floatToFraction(steps.get(i), 10);
			
			if (i < steps.size() - 1)
			{
				str += ",";
			}
		}
		
		return String.format("{%s}", str);
	}
	
	//-------------------------------------------------------------------------

}
