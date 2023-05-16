package app.utils.sandbox;

import java.awt.EventQueue;

import app.PlayerApp;
import game.equipment.component.Component;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.move.ActionAdd;
import other.action.move.ActionInsert;
import other.action.move.move.ActionMove;
import other.action.move.remove.ActionRemove;
import other.action.state.ActionSetCount;
import other.action.state.ActionSetNextPlayer;
import other.action.state.ActionSetRotation;
import other.action.state.ActionSetState;
import other.action.state.ActionSetValue;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import other.state.container.ContainerState;
import util.ContainerUtil;

/**
 * Various util functions to do with the sandbox.
 * 
 * @author Matthew.Stephenson
 */
public class SandboxUtil
{
	
	//-------------------------------------------------------------------------

	/**
	 * Returns an error message if sandbox is not allowed for the specified component.
	 */
	public static boolean isSandboxAllowed(final PlayerApp app, final Location selectedLocation)
	{
		final Context context = app.manager().ref().context();	
		
		final int locnUpSite = selectedLocation.site();
		final SiteType locnType = selectedLocation.siteType();
		final int containerId = ContainerUtil.getContainerId(context, locnUpSite, locnType);
		final Component componentAtSite = context.components()[(context.containerState(containerId)).what(locnUpSite,locnType)];

		if (componentAtSite != null)
		{
			if (componentAtSite.isDie())
			{
				app.setVolatileMessage("Setting dice not supported yet.");
				return false;
			}
			if (componentAtSite.isLargePiece())
			{
				app.setVolatileMessage("Setting large pieces is not supported yet.");
				return false;
			}
		}

		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return the number of buttons needed for the sandbox dialog.
	 */
	public static int numSandboxButtonsNeeded(final PlayerApp app, final SandboxValueType sandboxValueType)
	{
		final Context context = app.manager().ref().context();	
		
		int numButtonsNeeded = 0;
		if (sandboxValueType == SandboxValueType.Component)
		{
			numButtonsNeeded = context.components().length;
		}
		else if (sandboxValueType == SandboxValueType.LocalState)
		{
			numButtonsNeeded = context.game().maximalLocalStates();
		}
		else if (sandboxValueType == SandboxValueType.Count)
		{
			numButtonsNeeded = context.game().maxCount();
		}
		else if (sandboxValueType == SandboxValueType.Rotation)
		{
			numButtonsNeeded = context.game().maximalRotationStates();
		}
		else if (sandboxValueType == SandboxValueType.Value)
		{
			numButtonsNeeded = context.game().maximalValue();
		}
		
		return numButtonsNeeded;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Move a piece from one location to another.
	 */
	public static void makeSandboxDragMove(final PlayerApp app, final Location selectedFromLocation, final Location selectedToLocation)
	{
		final Context context = app.manager().ref().context();	
		
		try
		{
			final int currentMover = context.state().mover();
			final int nextMover = context.state().next();
			final int previousMover = context.state().prev();
			
			final Action actionRemove = ActionMove.construct(selectedFromLocation.siteType(), selectedFromLocation.site(), selectedFromLocation.level(), selectedToLocation.siteType(), selectedToLocation.site(), selectedToLocation.level(), Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, false);
			actionRemove.setDecision(true);
			final Move moveToApply = new Move(actionRemove);
			final Moves csq = new BaseMoves(null);
			final Move nextMove = new Move(new ActionSetNextPlayer(context.state().mover()));
			csq.moves().add(nextMove);
			moveToApply.then().add(csq);
			moveToApply.apply(context, true);
			
			context.state().setMover(currentMover);
			context.state().setNext(nextMover);
			context.state().setPrev(previousMover);
		}
		catch (final Exception e)
		{
			// An invalid drag location.
		}

		EventQueue.invokeLater(() -> 
		{
			app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
			app.repaint();
		});
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Move object corresponding to removing a piece at a specified location.
	 */
	public static Move getSandboxRemoveMove(final PlayerApp app, final Location selectedLocation)
	{
		final Context context = app.manager().ref().context();	
		
		final Action actionRemove = ActionRemove.construct(selectedLocation.siteType(), selectedLocation.site(), selectedLocation.level(), true);	
		actionRemove.setDecision(true);
		final Move moveToApply = new Move(actionRemove);
		final Moves csq = new BaseMoves(null);
		final Move nextMove = new Move(new ActionSetNextPlayer(context.state().mover()));
		csq.moves().add(nextMove);
		moveToApply.then().add(csq);
		
		return moveToApply;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Move object corresponding to adding a piece at a specified location.
	 */
	public static Move getSandboxAddMove(final PlayerApp app, final Location selectedLocation, final int componentIndex)
	{
		final Context context = app.manager().ref().context();	
		
		final Action actionAdd = new ActionAdd(selectedLocation.siteType(), selectedLocation.site(), componentIndex, 1, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, null);
		actionAdd.setDecision(true);
		final Move moveToApply = new Move(actionAdd);
		final Moves csq = new BaseMoves(null);
		final Move nextMove = new Move(new ActionSetNextPlayer(context.state().mover()));
		csq.moves().add(nextMove);
		moveToApply.then().add(csq);

		return moveToApply;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Move object corresponding to inserting a piece at a specified location.
	 */
	public static Move getSandboxInsertMove(final PlayerApp app, final Location selectedLocation, final int componentIndex)
	{
		final Context context = app.manager().ref().context();	
		
		final Action actionInsert = new ActionInsert(selectedLocation.siteType(), selectedLocation.site(), selectedLocation.level()+1, componentIndex, 1);
		actionInsert.setDecision(true);
		final Move moveToApply = new Move(actionInsert);
		final Moves csq = new BaseMoves(null);
		final Move nextMove = new Move(new ActionSetNextPlayer(context.state().mover()));
		csq.moves().add(nextMove);
		moveToApply.then().add(csq);
		
		return moveToApply;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Move object corresponding to setting a piece's variable at a specified location.
	 */
	public static Move getSandboxVariableMove(final PlayerApp app, final Location selectedLocation, final SandboxValueType sandboxValueType, final int value)
	{
		final Context context = app.manager().ref().context();	
		
		final int locnUpSite = selectedLocation.site();
		final int locnLevel = selectedLocation.level();
		final SiteType locnType = selectedLocation.siteType();
		
		final int containerId = ContainerUtil.getContainerId(context, locnUpSite, locnType);
		final ContainerState cs = context.state().containerStates()[containerId];
		
		// Determine the action based on the type.
		Action action = null;
		if (sandboxValueType == SandboxValueType.LocalState)
		{
			action = new ActionSetState(locnType, locnUpSite, locnLevel, value);
		}
		else if (sandboxValueType == SandboxValueType.Count)
		{
			action = new ActionSetCount(locnType, locnUpSite, cs.what(locnUpSite, locnLevel, locnType), value);
		}
		else if (sandboxValueType == SandboxValueType.Value)
		{
			action = new ActionSetValue(locnType, locnUpSite, locnLevel, value);
		}
		else if (sandboxValueType == SandboxValueType.Rotation)
		{
			action = new ActionSetRotation(locnType, locnUpSite, value);
		}
		action.setDecision(true);
		
		final Move moveToApply = new Move(action);
		final Moves csq = new BaseMoves(null);
		final Move nextMove = new Move(new ActionSetNextPlayer(context.state().mover()));
		csq.moves().add(nextMove);
		moveToApply.then().add(csq);
		
		return moveToApply;
	}
	
	//-------------------------------------------------------------------------
	
}
