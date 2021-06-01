package app.display.dialogs.MoveDialog;

import java.awt.EventQueue;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import app.DesktopApp;
import app.PlayerApp;
import app.display.dialogs.util.DialogUtil;
import app.utils.sandbox.SandboxUtil;
import app.utils.sandbox.SandboxValueType;
import game.equipment.component.Component;
import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.move.ActionAdd;
import other.action.move.ActionInsert;
import other.action.state.ActionSetCount;
import other.action.state.ActionSetState;
import other.action.state.ActionSetValue;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import other.state.container.ContainerState;
import util.ContainerUtil;

/**
 * Dialog for showing sandbox options.
 * 
 * @author Matthew.Stephenson
 */
public class SandboxDialog extends MoveDialog
{
	private static final long serialVersionUID = 1L;
	List<JButton> buttonList = new ArrayList<>();

	//-------------------------------------------------------------------------
	
	/**
	 * Show the Dialog.
	 */
	public static void createAndShowGUI(final PlayerApp app, final Location location, final SandboxValueType sandboxValueType)
	{
		try
		{
			final Context context = app.manager().ref().context();
			if (context.components().length == 1)
			{
				DesktopApp.view().setTemporaryMessage("No valid components.");
				return;
			}
			
			final SandboxDialog dialog = new SandboxDialog(app, context, location, sandboxValueType);
			final Point drawPosn = new Point(MouseInfo.getPointerInfo().getLocation().x - dialog.getWidth() / 2, MouseInfo.getPointerInfo().getLocation().y - dialog.getHeight() / 2);
			DialogUtil.initialiseForcedDialog(dialog, "Sandbox (" + sandboxValueType.name() + ")", new Rectangle(drawPosn));
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Create the dialog.
	 */
	public SandboxDialog(final PlayerApp app, final Context context, final Location location, final SandboxValueType sandboxValueType)
	{
		final int locnUpSite = location.site();
		final SiteType locnType = location.siteType();
		final int containerId = ContainerUtil.getContainerId(context, locnUpSite, locnType);
		
		if 
		(
			(containerId == -1) 
			|| 
			!SandboxUtil.isSandboxAllowed(app, location)
			||
			app.manager().settingsNetwork().getActiveGameId() > 0		// Shouldn't have been able to get here if playing online game, but better safe than sorry!
		) 
		{
			EventQueue.invokeLater(() -> 
			{
				dispose();
				return;
			});
		}
		
		final ContainerState cs = context.state().containerStates()[containerId];
		
		final int numButtonsNeeded = SandboxUtil.numSandboxButtonsNeeded(app, sandboxValueType);
		setDialogLayout(app, context, numButtonsNeeded);

		// Setting some property of a component.
		if (sandboxValueType != SandboxValueType.Component)
		{
			for (int i = 0; i < numButtonsNeeded; i++)
			{
				final String buttonText = Integer.toString(i);
				final Move move = SandboxUtil.getSandboxVariableMove(app, location, sandboxValueType, i);
				final JButton button = AddButton(app, move, null, buttonText);
				setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
			}
		}
		// Adding/Removing a component.
		else
		{
			// Add in button to remove existing component
			final Move move = SandboxUtil.getSandboxRemoveMove(app, location);
			final JButton button = AddButton(app, move, null, "");
			setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);

			// Add in button for each possible component.
			for (int componentIndex = 1; componentIndex < context.components().length; componentIndex++)
			{
				final Component c = context.components()[componentIndex];
				final BufferedImage im = app.graphicsCache().getComponentImage(app.bridge(), containerId, c, c.owner(), 0, 0, 0, 0, locnType,imageSize, app.contextSnapshot().getContext(app), 0, 0, true);
				
				// If not a stacking game, need to remove piece first
				if (!context.game().isStacking() || cs.sizeStack(locnUpSite, locnType) == 0)
				{
					final Move removeMove = SandboxUtil.getSandboxRemoveMove(app, location);
					final Move addMove = SandboxUtil.getSandboxAddMove(app, location, componentIndex);
					removeMove.actions().addAll(addMove.actions());
					final JButton buttonAdd = AddButton(app, removeMove, im, "");
					setDialogSize(buttonAdd, columnNumber, rowNumber, buttonBorderSize);
				}
				else
				{
					final Move insertMove = SandboxUtil.getSandboxInsertMove(app, location, componentIndex);
					final JButton buttonAdd = AddButton(app, insertMove, im, "");
					setDialogSize(buttonAdd, columnNumber, rowNumber, buttonBorderSize);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected void buttonMove(final PlayerApp app, final Move move)
	{
		final Context context = app.manager().ref().context();
		
		final int currentMover = context.state().mover();
		final int nextMover = context.state().next();
		final int previousMover = context.state().prev();
		
		move.apply(context, true);
		
		context.state().setMover(currentMover);
		context.state().setNext(nextMover);
		context.state().setPrev(previousMover);

		EventQueue.invokeLater(() -> 
		{
			app.contextSnapshot().setContext(app);
			app.updateTabs(context);
			app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
			app.repaint();
		});
		
		dispose();
		
		// Determine if there are any follow up values to set.
		if (context.game().requiresLocalState() && context.game().maximalLocalStates() > 1)
		{
			for (final Action action : move.actions())
			{
				if (action instanceof ActionInsert || action instanceof ActionAdd)
				{
					createAndShowGUI(app, move.getFromLocation(), SandboxValueType.LocalState);
					return;
				}
			}
		}
		if (context.game().requiresCount() && context.game().maxCount() > 1)
		{
			for (final Action action : move.actions())
			{
				if (action instanceof ActionInsert || action instanceof ActionAdd || action instanceof ActionSetState)
				{
					createAndShowGUI(app, move.getFromLocation(), SandboxValueType.Count);
					return;
				}
			}
		}
		if (context.game().requiresPieceValue() && context.game().maximalValue() > 1)
		{
			for (final Action action : move.actions())
			{
				if (action instanceof ActionInsert || action instanceof ActionAdd || action instanceof ActionSetState || action instanceof ActionSetCount)
				{
					createAndShowGUI(app, move.getFromLocation(), SandboxValueType.Value);
					return;
				}
			}
		}
		if (context.game().requiresRotation() && context.game().maximalRotationStates() > 1)
		{
			for (final Action action : move.actions())
			{
				if (action instanceof ActionInsert || action instanceof ActionAdd || action instanceof ActionSetState || action instanceof ActionSetCount || action instanceof ActionSetValue)
				{
					createAndShowGUI(app, move.getFromLocation(), SandboxValueType.Rotation);
					return;
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
}
