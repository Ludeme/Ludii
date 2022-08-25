package features.spatial.instances;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import features.spatial.SpatialFeature;
import features.spatial.SpatialFeature.BitSetTypes;
import features.spatial.elements.FeatureElement;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.ChunkSet;
import main.math.BitTwiddling;
import other.state.State;
import other.state.container.ContainerState;

//-----------------------------------------------------------------------------

/**
 * A concrete instance of a feature (always in an absolute positions, and in
 * BitSet-representation for efficient detection).
 * 
 * @author Dennis Soemers
 */
public final class FeatureInstance implements BitwiseTest 
{
	//-------------------------------------------------------------------------
	
	/** Reference to the feature of which this is an instance */
	protected final SpatialFeature parentFeature;
	
	/** Index of the vertex used as anchor for this instance */
	protected final int anchorSite;
	
	/** Reflection multiplier applied to parent feature for this instance */
	protected final int reflection;
	
	/** Additional rotation applied to parent feature for this instance */
	protected final float rotation;
	
	/** The graph element type this instance tests on */
	protected final SiteType graphElementType;
	
	/** Elements that have already passed testing at init-time */
	protected final List<FeatureElement> initTimeElements = new ArrayList<FeatureElement>();
	
	//-------------------------------------------------------------------------
	
	/** Set bits must be empty in the game state */
	protected ChunkSet mustEmpty;
	
	/** Set bits must be NOT empty in the game state */
	protected ChunkSet mustNotEmpty;
	
	/** After masking the game state's "who" bits, it must equal these bits */
	protected ChunkSet mustWho;
	/** Mask to apply to game state's "who" bits before testing */
	protected ChunkSet mustWhoMask;
	
	/** 
	 * After masking the game state's "who" bits, it must NOT equal these bits 
	 */
	protected ChunkSet mustNotWho;
	/** Mask to apply to game state's "who" bits before testing */
	protected ChunkSet mustNotWhoMask;
	
	/** After masking the game state's "what" bits, it must equal these bits */
	protected ChunkSet mustWhat;
	/** Mask to apply to game state's "what" bits before testing */
	protected ChunkSet mustWhatMask;
	
	/** 
	 * After masking the game state's "what" bits, it must NOT equal these bits 
	 */
	protected ChunkSet mustNotWhat;
	/** Mask to apply to game state's "what" bits before testing */
	protected ChunkSet mustNotWhatMask;
	
	/** This will be True if all of the above ChunkSets are null */
	protected transient boolean allRestrictionsNull;
	
	//-------------------------------------------------------------------------
	
	/** "to" position of action recommended by this feature instance */
	protected int toPosition;
	
	/** "from" position of action recommended by this feature instance */
	protected int fromPosition;
	
	/** "to" position of last action, which this feature instance reacts to */
	protected int lastToPosition;
	
	/** "from" position of last action, which this feature instance reacts to */
	protected int lastFromPosition;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructs new Feature Instance
	 * @param parentFeature
	 * @param anchorSite
	 * @param reflection
	 * @param rotation
	 * @param graphElementType
	 */
	public FeatureInstance
	(
		final SpatialFeature parentFeature, 
		final int anchorSite, 
		final int reflection, 
		final float rotation,
		final SiteType graphElementType
	)
	{
		this.parentFeature = parentFeature;
		this.anchorSite = anchorSite;
		this.reflection = reflection;
		this.rotation = rotation;
		this.graphElementType = graphElementType;
		
		mustEmpty = null;
		
		mustNotEmpty = null;
		
		mustWho = null;
		mustWhoMask = null;
		
		mustNotWho = null;
		mustNotWhoMask = null;
		
		mustWhat = null;
		mustWhatMask = null;
		
		mustNotWhat = null;
		mustNotWhatMask = null;
		
		allRestrictionsNull = true;
		
		toPosition = -1;
		fromPosition = -1;
		
		lastToPosition = -1;
		lastFromPosition = -1;
	}
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public FeatureInstance(final FeatureInstance other)
	{
		this.parentFeature = other.parentFeature;
		this.anchorSite = other.anchorSite;
		this.reflection = other.reflection;
		this.rotation = other.rotation;
		this.graphElementType = other.graphElementType;
		
		mustEmpty = other.mustEmpty == null ? null : (ChunkSet) other.mustEmpty.clone();
		
		mustNotEmpty = other.mustNotEmpty == null ? null : (ChunkSet) other.mustNotEmpty.clone();
		
		mustWho = other.mustWho == null ? null : (ChunkSet) other.mustWho.clone();
		mustWhoMask = other.mustWhoMask == null ? null : (ChunkSet) other.mustWhoMask.clone();
		
		mustNotWho = other.mustNotWho == null ? null : (ChunkSet) other.mustNotWho.clone();
		mustNotWhoMask = other.mustNotWhoMask == null ? null : (ChunkSet) other.mustNotWhoMask.clone();
		
		mustWhat = other.mustWhat == null ? null : (ChunkSet) other.mustWhat.clone();
		mustWhatMask = other.mustWhatMask == null ? null : (ChunkSet) other.mustWhatMask.clone();
		
		mustNotWhat = other.mustNotWhat == null ? null : (ChunkSet) other.mustNotWhat.clone();
		mustNotWhatMask = other.mustNotWhatMask == null ? null : (ChunkSet) other.mustNotWhatMask.clone();
		
		allRestrictionsNull = other.allRestrictionsNull;
		
		toPosition = other.toPosition;
		fromPosition = other.fromPosition;
		
		lastToPosition = other.lastToPosition;
		lastFromPosition = other.lastFromPosition;
		
		initTimeElements.addAll(other.initTimeElements);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Adds an element that has already been satisfied at init-time
	 * @param element
	 */
	public void addInitTimeElement(final FeatureElement element)
	{
		initTimeElements.add(element);
	}
	
	/**
	 * Adds a test without any particular value (testing for something like Empty, rather than something like
	 * a specific Player or Item).
	 * @param container
	 * @param bitSetType
	 * @param testSite
	 * @param active
	 * @return True if the test was successfully added, false if it would lead to inconsistencies
	 */
	public boolean addTest
	(
		final ContainerState container, 
		final BitSetTypes bitSetType, 
		final int testSite, 
		final boolean active
	)
	{
		return addTest(container, bitSetType, testSite, active, -1);
	}
	
	/**
	 * Adds a test to the feature instance
	 * @param container
	 * @param bitSetType
	 * @param testSite
	 * @param active
	 * @param value
	 * @return True if the test was successfully added, false if it would lead to inconsistencies
	 */
	public boolean addTest
	(
		final ContainerState container,
		final BitSetTypes bitSetType, 
		final int testSite, 
		final boolean active, 
		final int value
	)
	{
		switch (bitSetType)
		{
		case Empty:
			if (active)
			{
				if (mustNotEmpty != null && mustNotEmpty.get(testSite))
					return false;	// inconsistency: we already require this site to be non-empty
				
				if (mustEmpty == null)
				{
					switch (graphElementType)
					{
					case Cell:
						mustEmpty = new ChunkSet(1, 1);
						break;
					case Vertex:
						mustEmpty = new ChunkSet(1, 1);
						break;
					case Edge:
						mustEmpty = new ChunkSet(1, 1);
						break;
					//$CASES-OMITTED$ Hint
					default:
						break;
					}
				}
				
				mustEmpty.set(testSite);
				allRestrictionsNull = false;
			}
			else
			{
				if (mustEmpty != null && mustEmpty.get(testSite))
					return false;	// inconsistency: we already require this site to be empty
				
				if (mustNotEmpty == null)
				{
					switch (graphElementType)
					{
					case Cell:
						mustNotEmpty = new ChunkSet(1, 1);
						break;
					case Vertex:
						mustNotEmpty = new ChunkSet(1, 1);
						break;
					case Edge:
						mustNotEmpty = new ChunkSet(1, 1);
						break;
					//$CASES-OMITTED$ Hint
					default:
						break;
					}
				}
				
				mustNotEmpty.set(testSite);
				allRestrictionsNull = false;
			}
			break;
		case Who:
			if (active)
			{
				if (mustWhoMask != null && mustWhoMask.getChunk(testSite) != 0)
				{
					// we already have who-requirements for this chunk
					// will only be fine if exactly the same value is already 
					// required (redundant), otherwise it will be an 
					// inconsistency which is not fine
					return (mustWho.getChunk(testSite) == value);
				}
				else if (mustNotWhoMask != null && mustNotWho.getChunk(testSite) == value)
				{
					// inconsistency: we already have a requirement that who 
					// should specifically NOT be this value
					return false;
				}
				
				if (mustWho == null)
				{
					final int chunkSize;
					
					switch (graphElementType)
					{
					case Cell:
						chunkSize = container.chunkSizeWhoCell();
						break;
					case Edge:
						chunkSize = container.chunkSizeWhoEdge();
						break;
					case Vertex:
						chunkSize = container.chunkSizeWhoVertex();
						break;
						//$CASES-OMITTED$		Hint
					default:
						chunkSize = Constants.UNDEFINED;
						break;
					}
						
					mustWho = new ChunkSet(chunkSize, 1);
					mustWhoMask = new ChunkSet(chunkSize, 1);
				}
				
				mustWho.setChunk(testSite, value);
				mustWhoMask.setChunk(testSite, BitTwiddling.maskI(mustWhoMask.chunkSize()));
				allRestrictionsNull = false;
			}
			else
			{
				if (mustNotWhoMask != null && mustNotWhoMask.getChunk(testSite) != 0)
				{
					// we already have not-who-requirements for this chunk
					// will only be fine if exactly the same value is already 
					// not allowed (redundant), otherwise it will be an 
					// inconsistency which is not fine
					return (mustNotWho.getChunk(testSite) == value);
				}
				else if (mustWhoMask != null && mustWho.getChunk(testSite) == value)
				{
					// inconsistency: we already have a requirement that who 
					// should specifically be this value
					return false;
				}
				
				if (mustNotWho == null)
				{
					final int chunkSize;
					
					switch (graphElementType)
					{
					case Cell:
						chunkSize = container.chunkSizeWhoCell();
						break;
					case Edge:
						chunkSize = container.chunkSizeWhoEdge();
						break;
					case Vertex:
						chunkSize = container.chunkSizeWhoVertex();
						break;
						//$CASES-OMITTED$		Hint
					default:
						chunkSize = Constants.UNDEFINED;
						break;
					}
					
					mustNotWho = new ChunkSet(chunkSize, 1);
					mustNotWhoMask = new ChunkSet(chunkSize, 1);
				}
				
				mustNotWho.setChunk(testSite, value);
				mustNotWhoMask.setChunk(testSite, BitTwiddling.maskI(mustNotWhoMask.chunkSize()));
				allRestrictionsNull = false;
			}
			break;
		case What:
			if (active)
			{
				if (mustWhatMask != null && mustWhatMask.getChunk(testSite) != 0)
				{
					// we already have what-requirements for this chunk
					// will only be fine if exactly the same value is already 
					// required (redundant), otherwise it will be an 
					// inconsistency which is not fine
					return (mustWhat.getChunk(testSite) == value);
				}
				else if 
				(
					mustNotWhatMask != null 
					&& 
					mustNotWhat.getChunk(testSite) == value
				)
				{
					// inconsistency: we already have a requirement that what 
					// should specifically NOT be this value
					return false;
				}
				
				if (mustWhat == null)
				{
					final int chunkSize;
					
					switch (graphElementType)
					{
					case Cell:
						chunkSize = container.chunkSizeWhatCell();
						break;
					case Edge:
						chunkSize = container.chunkSizeWhatEdge();
						break;
					case Vertex:
						chunkSize = container.chunkSizeWhatVertex();
						break;
						//$CASES-OMITTED$		Hint
					default:
						chunkSize = Constants.UNDEFINED;
						break;
					}
					
					mustWhat = new ChunkSet(chunkSize, 1);
					mustWhatMask = new ChunkSet(chunkSize, 1);
				}
				
				mustWhat.setChunk(testSite, value);
				mustWhatMask.setChunk(testSite, BitTwiddling.maskI(mustWhatMask.chunkSize()));
				allRestrictionsNull = false;
			}
			else
			{
				if (mustNotWhatMask != null && mustNotWhatMask.getChunk(testSite) != 0)
				{
					// we already have not-what-requirements for this chunk
					// will only be fine if exactly the same value is already 
					// not allowed (redundant), otherwise it will be an 
					// inconsistency which is not fine
					return (mustNotWhat.getChunk(testSite) == value);
				}
				else if (mustWhatMask != null && mustWhat.getChunk(testSite) == value)
				{
					// inconsistency: we already have a requirement that what 
					// should specifically be this value
					return false;
				}
				
				if (mustNotWhat == null)
				{
					final int chunkSize;
					
					switch (graphElementType)
					{
					case Cell:
						chunkSize = container.chunkSizeWhatCell();
						break;
					case Edge:
						chunkSize = container.chunkSizeWhatEdge();
						break;
					case Vertex:
						chunkSize = container.chunkSizeWhatVertex();
						break;
						//$CASES-OMITTED$		Hint
					default:
						chunkSize = Constants.UNDEFINED;
						break;
					}
					
					mustNotWhat = new ChunkSet(chunkSize, 1);
					mustNotWhatMask = new ChunkSet(chunkSize, 1);
				}
				
				mustNotWhat.setChunk(testSite, value);
				mustNotWhatMask.setChunk(testSite, BitTwiddling.maskI(mustNotWhatMask.chunkSize()));
				allRestrictionsNull = false;
			}
			break;
			//$CASES-OMITTED$
		default:
			System.err.println("Warning: bitSetType " + bitSetType + " not supported by FeatureInstance.addTest()!");
			return false;
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param state
	 * @return True if this feature instance is active in the given game state
	 */
	@Override
	public final boolean matches(final State state)
	{
		if (allRestrictionsNull)
			return true;
		
		final ContainerState container = state.containerStates()[0];
		
		switch (graphElementType)
		{
		case Cell:
			if (mustEmpty != null)
			{
				if (!container.emptyChunkSetCell().matches(mustEmpty, mustEmpty))
					return false;
			}
			
			if (mustNotEmpty != null)
			{
				if (container.emptyChunkSetCell().violatesNot(mustNotEmpty, mustNotEmpty))
					return false;
			}
			
			if (mustWho != null)
			{
				if (!container.matchesWhoCell(mustWhoMask, mustWho))
					return false;
			}
			
			if (mustNotWho != null)
			{
				if (container.violatesNotWhoCell(mustNotWhoMask, mustNotWho))
					return false;
			}
			
			if (mustWhat != null)
			{
				if (!container.matchesWhatCell(mustWhatMask, mustWhat))
					return false;
			}
			
			if (mustNotWhat != null)
			{
				if (container.violatesNotWhatCell(mustNotWhatMask, mustNotWhat))
					return false;
			}
			break;
		case Vertex:
			if (mustEmpty != null)
			{
				if (!container.emptyChunkSetVertex().matches(mustEmpty, mustEmpty))
					return false;
			}
			
			if (mustNotEmpty != null)
			{
				if (container.emptyChunkSetVertex().violatesNot(mustNotEmpty, mustNotEmpty))
					return false;
			}
			
			if (mustWho != null)
			{
				if (!container.matchesWhoVertex(mustWhoMask, mustWho))
					return false;
			}
			
			if (mustNotWho != null)
			{
				if (container.violatesNotWhoVertex(mustNotWhoMask, mustNotWho))
					return false;
			}
			
			if (mustWhat != null)
			{
				if (!container.matchesWhatVertex(mustWhatMask, mustWhat))
					return false;
			}
			
			if (mustNotWhat != null)
			{
				if (container.violatesNotWhatVertex(mustNotWhatMask, mustNotWhat))
					return false;
			}
			break;
		case Edge:
			if (mustEmpty != null)
			{
				if (!container.emptyChunkSetEdge().matches(mustEmpty, mustEmpty))
					return false;
			}
			
			if (mustNotEmpty != null)
			{
				if (container.emptyChunkSetEdge().violatesNot(mustNotEmpty, mustNotEmpty))
					return false;
			}
			
			if (mustWho != null)
			{
				if (!container.matchesWhoEdge(mustWhoMask, mustWho))
					return false;
			}
			
			if (mustNotWho != null)
			{
				if (container.violatesNotWhoEdge(mustNotWhoMask, mustNotWho))
					return false;
			}
			
			if (mustWhat != null)
			{
				if (!container.matchesWhatEdge(mustWhatMask, mustWhat))
					return false;
			}
			
			if (mustNotWhat != null)
			{
				if (container.violatesNotWhatEdge(mustNotWhatMask, mustNotWhat))
					return false;
			}
			break;
		default:
			break;
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * We say that a Feature Instance A generalises another Feature Instance B
	 * if and only if any restrictions encoded in the various ChunkSets of
	 * A are also contained in B. Note that A and B are also considered to
	 * be generalising each other if they happen to have exactly the same
	 * ChunkSets.
	 * 
	 * Other properties (rotation, reflection, anchor, parent feature, etc.)
	 * are not included in the generalisation test.
	 * 
	 * Note that this definition of generalisation is a bit different from
	 * the generalisation tests of full Feature objects.
	 * 
	 * @param other
	 * @return True if and only if this Feature Instance generalises the other
	 */
	public boolean generalises(final FeatureInstance other)
	{
		if (other.mustEmpty == null)
		{
			if (mustEmpty != null)
				return false;
		}
		else
		{
			if (mustEmpty != null && !other.mustEmpty.matches(mustEmpty, mustEmpty))
				return false;
		}
		
		if (other.mustNotEmpty == null)
		{
			if (mustNotEmpty != null)
				return false;
		}
		else
		{
			if (mustNotEmpty != null && !other.mustNotEmpty.matches(mustNotEmpty, mustNotEmpty))
				return false;
		}
		
		if (other.mustWho == null)
		{
			if (mustWho != null)
				return false;
		}
		else
		{
			if (mustWho != null && !other.mustWho.matches(mustWhoMask, mustWho))
				return false;
		}
		
		if (other.mustNotWho == null)
		{
			if (mustNotWho != null)
				return false;
		}
		else
		{
			if (mustNotWho != null && !other.mustNotWho.matches(mustNotWhoMask, mustNotWho))
				return false;
		}
		
		if (other.mustWhat == null)
		{
			if (mustWhat != null)
				return false;
		}
		else
		{
			if (mustWhat != null && !other.mustWhat.matches(mustWhatMask, mustWhat))
				return false;
		}
		
		if (other.mustNotWhat == null)
		{
			if (mustNotWhat != null)
				return false;
		}
		else
		{
			if (mustNotWhat != null && !other.mustNotWhat.matches(mustNotWhatMask, mustNotWhat))
				return false;
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Removes any tests from this instance that are also already contained
	 * in the other given Feature Instance. This implies that we'll make sure
	 * to only attempt matching this Feature Instance to game states in which
	 * we have already previously verified that the other Feature Instance
	 * matches.
	 * 
	 * @param other
	 */
	public void removeTests(final FeatureInstance other)
	{
		if (other.mustEmpty != null)
		{
			mustEmpty.andNot(other.mustEmpty);
			
			if (mustEmpty.cardinality() == 0)
			{
				// no longer need this
				mustEmpty = null;
			}
		}
		
		if (other.mustNotEmpty != null)
		{
			mustNotEmpty.andNot(other.mustNotEmpty);
			
			if (mustNotEmpty.cardinality() == 0)
			{
				// no longer need this
				mustNotEmpty = null;
			}
		}
		
		if (other.mustWho != null)
		{
			mustWho.andNot(other.mustWho);
			mustWhoMask.andNot(other.mustWhoMask);
			
			if (mustWho.cardinality() == 0)
			{
				// no longer need this
				mustWho = null;
				mustWhoMask = null;
			}
		}
		
		if (other.mustNotWho != null)
		{
			mustNotWho.andNot(other.mustNotWho);
			mustNotWhoMask.andNot(other.mustNotWhoMask);
			
			if (mustNotWho.cardinality() == 0)
			{
				// no longer need this
				mustNotWho = null;
				mustNotWhoMask = null;
			}
		}
		
		if (other.mustWhat != null)
		{
			mustWhat.andNot(other.mustWhat);
			mustWhatMask.andNot(other.mustWhatMask);
			
			if (mustWhat.cardinality() == 0)
			{
				// no longer need this
				mustWhat = null;
				mustWhatMask = null;
			}
		}
		
		if (other.mustNotWhat != null)
		{
			mustNotWhat.andNot(other.mustNotWhat);
			mustNotWhatMask.andNot(other.mustNotWhatMask);
			
			if (mustNotWhat.cardinality() == 0)
			{
				// no longer need this
				mustNotWhat = null;
				mustNotWhatMask = null;
			}
		}
		
		allRestrictionsNull = hasNoTests();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if and only if this Feature Instance has no meaningful
	 * tests (i.e. all ChunkSets are null)
	 */
	@Override
	public final boolean hasNoTests()
	{
		return (mustEmpty == null &&
				
				mustNotEmpty == null &&

				mustWho == null &&
				mustWhoMask == null &&

				mustNotWho == null &&
				mustNotWhoMask == null &&

				mustWhat == null &&
				mustWhatMask == null &&

				mustNotWhat == null &&
				mustNotWhatMask == null);
	}
	
	@Override
	public final boolean onlyRequiresSingleMustEmpty()
	{
		if 
		(
			mustEmpty != null &&
			mustNotEmpty == null &&

			mustWho == null &&
			mustWhoMask == null &&

			mustNotWho == null &&
			mustNotWhoMask == null &&

			mustWhat == null &&
			mustWhatMask == null &&

			mustNotWhat == null &&
			mustNotWhatMask == null
		)
		{
			return mustEmpty.numNonZeroChunks() == 1;
		}
		
		return false;
	}
	
	@Override
	public final boolean onlyRequiresSingleMustWho()
	{
		if 
		(
			mustEmpty == null &&
			mustNotEmpty == null &&

			mustWho != null &&
			mustWhoMask != null &&

			mustNotWho == null &&
			mustNotWhoMask == null &&

			mustWhat == null &&
			mustWhatMask == null &&

			mustNotWhat == null &&
			mustNotWhatMask == null
		)
		{
			return mustWhoMask.numNonZeroChunks() == 1;
		}
		
		return false;
	}
	
	@Override
	public final boolean onlyRequiresSingleMustWhat()
	{
		if 
		(
			mustEmpty == null &&
			mustNotEmpty == null &&

			mustWho == null &&
			mustWhoMask == null &&

			mustNotWho == null &&
			mustNotWhoMask == null &&

			mustWhat != null &&
			mustWhatMask != null &&

			mustNotWhat == null &&
			mustNotWhatMask == null
		)
		{
			return mustWhatMask.numNonZeroChunks() == 1;
		}
		
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Feature of which this is an instance
	 */
	public final SpatialFeature feature()
	{
		return parentFeature;
	}
	
	/**
	 * @return Anchor site for this instance
	 */
	public final int anchorSite()
	{
		return anchorSite;
	}
	
	/**
	 * @return Reflection applied to parent feature to obtain this instance
	 */
	public final int reflection()
	{
		return reflection;
	}
	
	/**
	 * @return Rotation applied to parent feature to obtain this instance
	 */
	public final float rotation()
	{
		return rotation;
	}
	
	@Override
	public final SiteType graphElementType()
	{
		return graphElementType;
	}
	
	/**
	 * @return From-position (-1 if not restricted)
	 */
	public final int from()
	{
		return fromPosition;
	}
	
	/**
	 * @return To-position (-1 if not restricted)
	 */
	public final int to()
	{
		return toPosition;
	}
	
	/**
	 * @return Last-from-position (-1 if not restricted)
	 */
	public final int lastFrom()
	{
		return lastFromPosition;
	}
	
	/**
	 * @return Last-to-position (-1 if not restricted)
	 */
	public final int lastTo()
	{
		return lastToPosition;
	}
	
	/**
	 * Set action corresponding to this feature (instance)
	 * @param toPos
	 * @param fromPos
	 */
	public void setAction(final int toPos, final int fromPos)
	{
		this.toPosition = toPos;
		this.fromPosition = fromPos;
	}
	
	/**
	 * Set last action (which we're reacting to) corresponding to this 
	 * feature (instance)
	 * @param lastToPos
	 * @param lastFromPos
	 */
	public void setLastAction(final int lastToPos, final int lastFromPos)
	{
		this.lastToPosition = lastToPos;
		this.lastFromPosition = lastFromPos;
	}
	
	/**
	 * @return ChunkSet of sites that must be empty
	 */
	public final ChunkSet mustEmpty()
	{
		return mustEmpty;
	}
	
	/**
	 * @return ChunkSet of sites that must NOT be empty
	 */
	public final ChunkSet mustNotEmpty()
	{
		return mustNotEmpty;
	}
	
	/**
	 * @return mustWho ChunkSet
	 */
	public final ChunkSet mustWho()
	{
		return mustWho;
	}
	
	/**
	 * @return mustNotWho ChunkSet
	 */
	public final ChunkSet mustNotWho()
	{
		return mustNotWho;
	}
	
	/**
	 * @return mustWhoMask ChunkSet
	 */
	public final ChunkSet mustWhoMask()
	{
		return mustWhoMask;
	}
	
	/**
	 * @return mustNotWhoMask ChunkSet
	 */
	public final ChunkSet mustNotWhoMask()
	{
		return mustNotWhoMask;
	}
	
	/**
	 * @return mustWhat ChunkSet
	 */
	public final ChunkSet mustWhat()
	{
		return mustWhat;
	}
	
	/**
	 * @return mustNotWhat ChunkSet
	 */
	public final ChunkSet mustNotWhat()
	{
		return mustNotWhat;
	}
	
	/**
	 * @return mustWhatMask ChunkSet
	 */
	public final ChunkSet mustWhatMask()
	{
		return mustWhatMask;
	}
	
	/**
	 * @return mustNotWhatMask ChunkSet
	 */
	public final ChunkSet mustNotWhatMask()
	{
		return mustNotWhatMask;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return List of all the atomic propositions this feature instance requires.
	 */
	public List<AtomicProposition> generateAtomicPropositions()
	{
		final List<AtomicProposition> propositions = new ArrayList<AtomicProposition>();
		
		switch (graphElementType)
		{
		case Cell:
			if (mustEmpty != null)
			{
				final TIntArrayList nonzeroChunks = mustEmpty.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					propositions.add(new SingleMustEmptyCell(nonzeroChunks.getQuick(i)));
				}
			}
			
			if (mustNotEmpty != null)
			{
				final TIntArrayList nonzeroChunks = mustNotEmpty.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					propositions.add(new SingleMustNotEmptyCell(nonzeroChunks.getQuick(i)));
				}
			}
			
			if (mustWho != null)
			{
				final TIntArrayList nonzeroChunks = mustWho.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustWhoCell(chunk, mustWho.getChunk(chunk), mustWho.chunkSize()));
				}
			}
			
			if (mustNotWho != null)
			{
				final TIntArrayList nonzeroChunks = mustNotWho.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustNotWhoCell(chunk, mustNotWho.getChunk(chunk), mustNotWho.chunkSize()));
				}
			}
			
			if (mustWhat != null)
			{
				final TIntArrayList nonzeroChunks = mustWhat.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustWhatCell(chunk, mustWhat.getChunk(chunk), mustWhat.chunkSize()));
				}
			}
			
			if (mustNotWhat != null)
			{
				final TIntArrayList nonzeroChunks = mustNotWhat.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustNotWhatCell(chunk, mustNotWhat.getChunk(chunk), mustNotWhat.chunkSize()));
				}
			}
			break;
		case Vertex:
			if (mustEmpty != null)
			{
				final TIntArrayList nonzeroChunks = mustEmpty.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					propositions.add(new SingleMustEmptyVertex(nonzeroChunks.getQuick(i)));
				}
			}
			
			if (mustNotEmpty != null)
			{
				final TIntArrayList nonzeroChunks = mustNotEmpty.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					propositions.add(new SingleMustNotEmptyVertex(nonzeroChunks.getQuick(i)));
				}
			}
			
			if (mustWho != null)
			{
				final TIntArrayList nonzeroChunks = mustWho.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustWhoVertex(chunk, mustWho.getChunk(chunk), mustWho.chunkSize()));
				}
			}
			
			if (mustNotWho != null)
			{
				final TIntArrayList nonzeroChunks = mustNotWho.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustNotWhoVertex(chunk, mustNotWho.getChunk(chunk), mustNotWho.chunkSize()));
				}
			}
			
			if (mustWhat != null)
			{
				final TIntArrayList nonzeroChunks = mustWhat.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustWhatVertex(chunk, mustWhat.getChunk(chunk), mustWhat.chunkSize()));
				}
			}
			
			if (mustNotWhat != null)
			{
				final TIntArrayList nonzeroChunks = mustNotWhat.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustNotWhatVertex(chunk, mustNotWhat.getChunk(chunk), mustNotWhat.chunkSize()));
				}
			}
			break;
		case Edge:
			if (mustEmpty != null)
			{
				final TIntArrayList nonzeroChunks = mustEmpty.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					propositions.add(new SingleMustEmptyEdge(nonzeroChunks.getQuick(i)));
				}
			}
			
			if (mustNotEmpty != null)
			{
				final TIntArrayList nonzeroChunks = mustNotEmpty.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					propositions.add(new SingleMustNotEmptyEdge(nonzeroChunks.getQuick(i)));
				}
			}
			
			if (mustWho != null)
			{
				final TIntArrayList nonzeroChunks = mustWho.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustWhoEdge(chunk, mustWho.getChunk(chunk), mustWho.chunkSize()));
				}
			}
			
			if (mustNotWho != null)
			{
				final TIntArrayList nonzeroChunks = mustNotWho.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustNotWhoEdge(chunk, mustNotWho.getChunk(chunk), mustNotWho.chunkSize()));
				}
			}
			
			if (mustWhat != null)
			{
				final TIntArrayList nonzeroChunks = mustWhat.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustWhatEdge(chunk, mustWhat.getChunk(chunk), mustWhat.chunkSize()));
				}
			}
			
			if (mustNotWhat != null)
			{
				final TIntArrayList nonzeroChunks = mustNotWhat.getNonzeroChunks();
				for (int i = 0; i < nonzeroChunks.size(); ++i)
				{
					final int chunk = nonzeroChunks.getQuick(i);
					propositions.add(new SingleMustNotWhatEdge(chunk, mustNotWhat.getChunk(chunk), mustNotWhat.chunkSize()));
				}
			}
			break;
		default:
			break;
		}
		
		return propositions;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param instances
	 * @return New list of feature instances where duplicates in given list
	 * have been removed
	 */
	public static List<FeatureInstance> deduplicate(final List<FeatureInstance> instances)
	{
		final Set<FeatureInstance> deduplicated = new HashSet<FeatureInstance>();
		deduplicated.addAll(instances);
		return new ArrayList<FeatureInstance>(deduplicated);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + anchorSite;
		result = prime * result + fromPosition;
		result = prime * result + lastFromPosition;
		result = prime * result + lastToPosition;
		result = prime * result + ((mustEmpty == null) ? 0 : mustEmpty.hashCode());
		result = prime * result + ((mustNotEmpty == null) ? 0 : mustNotEmpty.hashCode());
		result = prime * result + ((mustNotWhat == null) ? 0 : mustNotWhat.hashCode());
		result = prime * result + ((mustNotWhatMask == null) ? 0 : mustNotWhatMask.hashCode());
		result = prime * result + ((mustNotWho == null) ? 0 : mustNotWho.hashCode());
		result = prime * result + ((mustNotWhoMask == null) ? 0 : mustNotWhoMask.hashCode());
		result = prime * result + ((mustWhat == null) ? 0 : mustWhat.hashCode());
		result = prime * result + ((mustWhatMask == null) ? 0 : mustWhatMask.hashCode());
		result = prime * result + ((mustWho == null) ? 0 : mustWho.hashCode());
		result = prime * result + ((mustWhoMask == null) ? 0 : mustWhoMask.hashCode());
		result = prime * result + reflection;
		result = prime * result + Float.floatToIntBits(rotation);
		result = prime * result + toPosition;
		
		// Order of elements in initTimeElements should not matter
		int initTimeElementsHash = 0;
		for (final FeatureElement element : initTimeElements)
		{
			// XORing them all means order does not matter
			initTimeElementsHash ^= element.hashCode();
		}
		
		result = prime * result + (prime + initTimeElementsHash);
		
		return result;
	}

	@Override
	public boolean equals(final Object other)
	{
		if (!(other instanceof FeatureInstance))
			return false;
		
		final FeatureInstance otherInstance = (FeatureInstance) other;
		
		// Order of elements in initTimeElements should not matter
		if (initTimeElements.size() != otherInstance.initTimeElements.size())
			return false;
		
		for (final FeatureElement element : initTimeElements)
		{
			if (!otherInstance.initTimeElements.contains(element))
				return false;
		}
		
		return (
				toPosition == otherInstance.toPosition &&
				fromPosition == otherInstance.fromPosition &&
				
				lastToPosition == otherInstance.lastToPosition &&
				lastFromPosition == otherInstance.lastFromPosition &&
				
				anchorSite == otherInstance.anchorSite &&
				rotation == otherInstance.rotation &&
				reflection == otherInstance.reflection &&
				
				Objects.equals(mustEmpty, otherInstance.mustEmpty) &&
				
				Objects.equals(mustNotEmpty, otherInstance.mustNotEmpty) &&
				
				Objects.equals(mustWho, otherInstance.mustWho) &&
				Objects.equals(mustWhoMask, otherInstance.mustWhoMask) &&
				
				Objects.equals(mustNotWho, otherInstance.mustNotWho) &&
				Objects.equals(mustNotWhoMask, otherInstance.mustNotWhoMask) &&
				
				Objects.equals(mustWhat, otherInstance.mustWhat) &&
				Objects.equals(mustWhatMask, otherInstance.mustWhatMask) &&
				
				Objects.equals(mustNotWhat, otherInstance.mustNotWhat) &&
				Objects.equals(mustNotWhatMask, otherInstance.mustNotWhatMask)
				);
	}
	
	/**
	 * @param other
	 * @return True if and only if the given other feature instance is functionally equal
	 * (has the same tests).
	 */
	public boolean functionallyEquals(final FeatureInstance other)
	{
		return (
				toPosition == other.toPosition &&
				fromPosition == other.fromPosition &&
				
				lastToPosition == other.lastToPosition &&
				lastFromPosition == other.lastFromPosition &&
				
				Objects.equals(mustEmpty, other.mustEmpty) &&
				
				Objects.equals(mustNotEmpty, other.mustNotEmpty) &&
				
				Objects.equals(mustWho, other.mustWho) &&
				Objects.equals(mustWhoMask, other.mustWhoMask) &&
				
				Objects.equals(mustNotWho, other.mustNotWho) &&
				Objects.equals(mustNotWhoMask, other.mustNotWhoMask) &&
				
				Objects.equals(mustWhat, other.mustWhat) &&
				Objects.equals(mustWhatMask, other.mustWhatMask) &&
				
				Objects.equals(mustNotWhat, other.mustNotWhat) &&
				Objects.equals(mustNotWhatMask, other.mustNotWhatMask)
				);
	}
	
	/**
	 * @param other
	 * @return True if and only if we would have been equal to the given other
	 * 	instance if our anchors were the same.
	 */
	public boolean equalsIgnoreAnchor(final FeatureInstance other)
	{
		return 
				(
					rotation == other.rotation &&
					reflection == other.reflection &&
					feature().equals(other.feature())
				);
	}
	
	/**
	 * @return Hash code that takes into account rotation and reflection and
	 * 	feature, but not anchor.
	 */
	public int hashCodeIgnoreAnchor()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + feature().hashCode();
		result = prime * result + reflection;
		result = prime * result + Float.floatToIntBits(rotation);
		
		return result;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String requirementsStr = "";
		
		if (fromPosition >= 0)
		{
			requirementsStr += 
					String.format(
							"Move from %s to %s: ", 
							Integer.valueOf(fromPosition), 
							Integer.valueOf(toPosition));
		}
		else
		{
			requirementsStr += String.format("Move to %s: ", Integer.valueOf(toPosition));
		}
		
		if (mustEmpty != null)
		{
			for (int i = mustEmpty.nextSetBit(0); 
					i >= 0; i = mustEmpty.nextSetBit(i + 1)) 
			{
				requirementsStr += i + " must be empty, ";
			}
		}
		
		if (mustNotEmpty != null)
		{
			for (int i = mustNotEmpty.nextSetBit(0); 
					i >= 0; i = mustNotEmpty.nextSetBit(i + 1)) 
			{
				requirementsStr += i + " must NOT be empty, ";
			}
		}
		
		if (mustWho != null)
		{
			for (int i = 0; i < mustWho.numChunks(); ++i)
			{
				if (mustWhoMask.getChunk(i) != 0)
				{
					requirementsStr += 
							i + " must belong to " + mustWho.getChunk(i) + ", ";
				}
			}
		}
		
		if (mustNotWho != null)
		{
			for (int i = 0; i < mustNotWho.numChunks(); ++i)
			{
				if (mustNotWhoMask.getChunk(i) != 0)
				{
					requirementsStr += 
							i + " must NOT belong to "
									+ mustNotWho.getChunk(i) + ", ";
				}
			}
		}
		
		if (mustWhat != null)
		{
			for (int i = 0; i < mustWhat.numChunks(); ++i)
			{
				if (mustWhatMask.getChunk(i) != 0)
				{
					requirementsStr += 
							i + " must contain " + mustWhat.getChunk(i) + ", ";
				}
			}
		}
		
		if (mustNotWhat != null)
		{
			for (int i = 0; i < mustNotWhat.numChunks(); ++i)
			{
				if (mustNotWhatMask.getChunk(i) != 0)
				{
					requirementsStr += 
							i + " must NOT contain "
									+ mustNotWhat.getChunk(i) + ", ";
				}
			}
		}
		
		if (lastToPosition >= 0)
		{
			if (lastFromPosition >= 0)
			{
				requirementsStr += 
						" (response to last move from " + lastFromPosition + 
						" to " + lastToPosition + ")";
			}
			else
			{
				requirementsStr += 
						" (response to last move to " + lastToPosition + ")";
			}
		}
		
		String metaStr = String.format(
				"anchor=%d, ref=%d, rot=%.2f", 
				Integer.valueOf(anchorSite),
				Integer.valueOf(reflection),
				Float.valueOf(rotation));
		
		return String.format(
				"Feature Instance [%s] [%s] [%s]", 
				requirementsStr,
				metaStr,
				parentFeature);
	}
	
	//-------------------------------------------------------------------------

}
