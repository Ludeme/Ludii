package other.topology;

import game.equipment.container.board.Board;
import game.types.board.SiteType;

/**
 * Find a cell with a specified coordinate label.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class SiteFinder
{
	/**
	 * @param board The board.
	 * @param coord The coordinate
	 * @param type  The graph element type.
	 * @return Cell with specified coordinate label, else null if not found.
	 */
	public final static TopologyElement find(final Board board, final String coord, final SiteType type)
	{
		if ((type == null && board.defaultSite() == SiteType.Cell)
				|| (type != null && type.equals(SiteType.Cell)))
		{
			for (final Cell cell : board.topology().cells())
				if (cell.label().equals(coord))
					return cell;
		}
		else if ((type == null && board.defaultSite() == SiteType.Vertex)
				|| (type != null && type.equals(SiteType.Vertex)))
		{
			for (final Vertex vertex : board.topology().vertices())
				if (vertex.label().equals(coord))
					return vertex;
		}

		return null;
	}
}
