package game.types.board;

import java.util.BitSet;

import game.Game;
import game.types.state.GameType;
import other.concept.Concept;

/**
 * Defines the element types that make up each graph.
 * 
 * @author Eric.Piette and Matthew Stephenson and cambolbro
 */
public enum SiteType
{
	/** Graph vertex. */
	Vertex,
	
	/** Graph edge. */
	Edge, 
	
	/** Graph cell/face. */
	Cell; 

	//-------------------------------------------------------------------------
	
	/**
	 * @param preferred The graph element type.
	 * @param game      The game.
	 * @return Graph element type to use, based on a preferred type (using game
	 *         settings if null).
	 */
	public static SiteType use
	(
		final SiteType preferred, final Game game
	)
	{
		if (preferred != null)
			return preferred;
		
		return game.board().defaultSite();
	}
	
	/**
	 * @param type The graph element type.
	 * @return The corresponding flags to return for the siteType.
	 */
	public static long gameFlags
	(
		final SiteType type
	)
	{
		long gameFlags = 0l;
		
		if (type != null)
		{	
			switch (type)
			{
			case Vertex: gameFlags |= (GameType.Vertex | GameType.Graph); break;
			case Edge:	 gameFlags |= (GameType.Edge   | GameType.Graph); break;
			case Cell:   gameFlags |=  GameType.Cell; 					  break;
			}
		}
		return gameFlags;
	}
	
	
	/**
	 * @param type The graph element type.
	 * @return The corresponding flags to return for the siteType.
	 */
	public static BitSet concepts
	(
		final SiteType type
	)
	{
		final BitSet concepts = new BitSet();
		
		if (type != null)
		{	
			switch (type)
			{
			case Vertex: concepts.set(Concept.Vertex.id(), true); break;
			case Edge:	 concepts.set(Concept.Edge.id(), true); break;
			case Cell:   concepts.set(Concept.Cell.id(), true); break;
			}
		}
		return concepts;
	}
}
