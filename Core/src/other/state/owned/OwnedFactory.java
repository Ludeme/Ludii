package other.state.owned;

import game.Game;
import game.equipment.component.Component;
import game.types.state.GameType;

/**
 * Factory to instantiate appropriate "Owned" containers for a given
 * game's states.
 *
 * @author Dennis Soemers
 */
public final class OwnedFactory
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private OwnedFactory()
	{
		// Do not instantiate
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return Owned object created for given game
	 */
	public static Owned createOwned(final Game game)
	{
		final long gameFlags = game.gameFlags();
		
		if 
		(
			(gameFlags & GameType.Cell) != 0L &&
			(gameFlags & GameType.Edge) == 0L &&
			(gameFlags & GameType.Vertex) == 0L
		)
		{
			// We only use Cells, no other site types
			if (game.isStacking())
				return new CellOnlyOwned(game);
			else
				return new FlatCellOnlyOwned(game);
		}
		else if
		(
			(gameFlags & GameType.Cell) == 0L &&
			(gameFlags & GameType.Edge) == 0L &&
			(gameFlags & GameType.Vertex) != 0L
		)
		{
			// We only use Vertices, no other site types
			if (!game.isStacking())
			{
				boolean maxOneCompPerPlayer = true;
				
				final Component[] components = game.equipment().components();
				
				for (int p = 0; p < game.players().count() + 2; ++p)
				{
					int numComps = 0;
					
					for (int e = 0; e < components.length; ++e)
					{
						final Component comp = components[e];
						if (comp != null && comp.owner() == p)
							numComps++;
					}
					
					if (numComps > 1)
					{
						maxOneCompPerPlayer = false;
						break;
					}
				}
				
				if (maxOneCompPerPlayer)
					return new FlatVertexOnlyOwnedSingleComp(game);
				else
					return new FlatVertexOnlyOwned(game);
			}
		}
		
		// Default to all the data we might ever need
		return new FullOwned(game);
	}
	
	//-------------------------------------------------------------------------

}
