package instructionGeneration;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import manager.Referee;
import other.action.Action;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import other.state.State;
import other.state.container.ContainerState;
import util.ContainerUtil;

public class ValueUtils
{

	//-------------------------------------------------------------------------
	
	/**
	 * Returns the what value of a given move at the current point in the context.
	 */
	public final static int getWhatOfMove(final Context context, final Move move)
	{
		final Location moveFrom = move.getFromLocation();
		final int containerIdFrom = ContainerUtil.getContainerId(context, moveFrom.site(), moveFrom.siteType());
		
		int what = -1;
		
		if (containerIdFrom != -1)
		{
			final State state = context.state();
			final ContainerState cs = state.containerStates()[containerIdFrom];
			
			// Get the what of the component at the move's from location
			what = cs.what(moveFrom.site(), moveFrom.level(), moveFrom.siteType());
			
			// If adding a piece at the site, get the what of the first action that matches the move's from location instead.
			if (what == 0)
			{
				for (final Action a : move.actions())
				{
					final Location actionLocationA = new FullLocation(a.from(), a.levelFrom(), a.fromType());
					final Location actionLocationB = new FullLocation(a.to(), a.levelTo(), a.toType());
					final Location testingLocation = new FullLocation(moveFrom.site(), moveFrom.level(), moveFrom.siteType());
					
					if (actionLocationA.equals(testingLocation) && actionLocationB.equals(testingLocation))
					{
						what = a.what();
						break;
					}
				}
			}
		}
		
		return what;
	}
	
	//-------------------------------------------------------------------------
	
	public final static String getComponentNameFromIndex(final Context context, final int componentIndex)
	{
		String moveComponentName = "No Component";
		
		if (context.game().isDeductionPuzzle())
			moveComponentName = "Puzzle Value " + String.valueOf(componentIndex);
		
		else if (!context.game().isDeductionPuzzle() && componentIndex > 0)
			moveComponentName = context.equipment().components()[componentIndex].getNameWithoutNumber();
		
		return moveComponentName;
	}
	
	//-------------------------------------------------------------------------
	
	public final static boolean checkGameValid(Game game)
	{
		if (game.isEdgeGame())
			return false;
		
		if (game.isSimultaneousMoveGame())
			return false;
		
		if (game.hasSubgames())
			return false;
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
}
