package controllers.container;

import java.awt.Point;
import java.util.ArrayList;

import bridge.Bridge;
import controllers.BaseController;
import game.equipment.container.Container;
import game.rules.play.moves.Moves;
import gnu.trove.list.array.TIntArrayList;
import other.action.Action;
import other.action.ActionType;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import other.topology.Cell;
import util.WorldLocation;
import view.container.ContainerStyle;

/**
 * Controller for pyramidal boards/games (e.g. Shibumi)
 * 
 * @author Matthew.Stephenson
 */
public class PyramidalController extends BaseController
{
	
	//-------------------------------------------------------------------------
	
	public PyramidalController(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
	}

	//-------------------------------------------------------------------------
	
	@Override
	protected Location translateClicktoSite(final Point pt, final Context context, final ArrayList<WorldLocation> allLocations) 
	{
		Location location = super.translateClicktoSite(pt, context, allLocations);
		
		final Moves legal = context.moves(context);
				
		if (location.site() != -1)
		{
			final ContainerStyle containerStyle = bridge.getContainerStyle(container.index());
			
			// If using a Shibumi board and have selected a piece, check that there are no add moves on the sites above it.
			int newCid = -1;
			final Cell selectedVertex = containerStyle.drawnCells().get(location.site());
			final TIntArrayList possibleCid = new TIntArrayList();
			for (final Cell vertex : containerStyle.drawnCells())
				if (Math.abs(vertex.centroid().getX()-selectedVertex.centroid().getX()) < 0.001 && Math.abs(vertex.centroid().getY()-selectedVertex.centroid().getY()) < 0.001)
					possibleCid.add(vertex.index());

			for (int m = 0; m < legal.moves().size(); m++)
			{
				Action decisionAction = null;
				final Move move = legal.moves().get(m);
				for (int a = 0; a < move.actions().size(); a++)
				{
					if (move.actions().get(a).isDecision())
					{
						decisionAction = move.actions().get(a);
						break;
					}
				}
				if (decisionAction.actionType() == ActionType.Add)
				{
					for (int i = 0; i < possibleCid.size(); i++)
					{
						final int moveIndex = possibleCid.getQuick(i);
						
						if (decisionAction.from() == moveIndex && decisionAction.to() == moveIndex)
						{
							newCid = moveIndex;
							break;
						}
					}
					if (newCid != -1)
					{
						location = new FullLocation(newCid, location.level(), location.siteType());
						break;
					}
				}
			}
		}
		
		return location;
	}
	
	//-------------------------------------------------------------------------
	
}
