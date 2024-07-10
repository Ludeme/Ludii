package app.display.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import app.DesktopApp;
import app.PlayerApp;
import app.views.players.PlayerViewUser;
import main.Constants;
import metadata.graphics.util.PieceStackType;
import metadata.graphics.util.StackPropertyType;
import other.context.Context;
import other.location.Location;
import other.state.container.ContainerState;
import util.ContainerUtil;
import utils.AIRegistry;


/**
 * Utility functions for the GUI.
 * 
 * @author Matthew Stephenson
 */
public class DesktopGUIUtil
{

	//-------------------------------------------------------------------------

	/**
	 * If the current system is a Mac computer.
	 */
	public static boolean isMac()
	{
		final String osName = System.getProperty("os.name");  
		final boolean isMac = osName.toLowerCase().startsWith("mac os x");	
		return isMac;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get a list of all AI display names.
	 */
	public static ArrayList<String> getAIDropdownStrings(final PlayerApp app, final boolean includeHuman)
	{
		final ArrayList<String> allStrings = new ArrayList<>();
		
		if (includeHuman)
			allStrings.add("Human");
		
		allStrings.addAll(AIRegistry.generateValidAgentNames(app.contextSnapshot().getContext(app).game()));

		allStrings.add("From JAR");
		allStrings.add("From JSON");
		allStrings.add("From AI.DEF");
		
		return allStrings;
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Repaints the necessary area for a component moving between two points.
	 */
	public static void repaintComponentBetweenPoints(final PlayerApp app, final Context context, final Location componentLocation, final Point oldPoint, final Point newPoint)
	{
		try
		{
			if (app.contextSnapshot().getContext(app).game().hasLargePiece())
			{
				DesktopApp.view().repaint();
				return;
			}
			
			// If any of the player panels have been moved due to metadata, repaint the whole board.
			for (final PlayerViewUser panel : DesktopApp.view().getPlayerPanel().playerSections)
			{
				if (context.game().metadata().graphics().handPlacement(context, panel.playerId()) != null)
				{
					DesktopApp.view().repaint();
					return;
				}
			}
			
			// Determine the size of the component image being dragged.
			final int cellSize = app.bridge().getContainerStyle(context.board().index()).cellRadiusPixels() * 2;
			final int containerId = ContainerUtil.getContainerId(context, componentLocation.site(), componentLocation.siteType());
			final ContainerState cs = context.state().containerStates()[containerId];
			final int localState = cs.state(componentLocation.site(), componentLocation.level(), componentLocation.siteType());
			final int who = cs.who(componentLocation.site(), componentLocation.level(), componentLocation.siteType());
			final int value = cs.value(componentLocation.site(), componentLocation.level(), componentLocation.siteType());
			final int rotation = cs.rotation(componentLocation.site(), componentLocation.level(), componentLocation.siteType());
			final PieceStackType componentStackType = PieceStackType.getTypeFromValue((int) context.metadata().graphics().stackMetadata(context, context.equipment().containers()[containerId], componentLocation.site(), componentLocation.siteType(), localState, value, StackPropertyType.Type));
			
			// Find the largest component image in the stack.
			int maxComponentSize = cellSize;
			for (int level = componentLocation.level(); level < Constants.MAX_STACK_HEIGHT; level++)
			{
				final int what = cs.what(componentLocation.site(), componentLocation.level(), componentLocation.siteType());
				if (what == 0)
					break;
				
				final int componentSize = app.graphicsCache().getComponentImageSize(containerId, what, who, localState, value, 0, rotation);

				if (componentSize > maxComponentSize)
					maxComponentSize = componentSize;
			}
			
			int midX = (newPoint.x + oldPoint.x) / 2;
			int midY = (newPoint.y + oldPoint.y) / 2;
			int width = ((Math.abs(newPoint.x - oldPoint.x) + maxComponentSize + cellSize));
			int height = ((Math.abs(newPoint.y - oldPoint.y) + maxComponentSize + cellSize));
			
			// If the component is stacked in a vertical manner, need to repaint the whole column.
			if (componentStackType.verticalStack())
			{
				height = DesktopApp.frame().getHeight();
				midY = height/2;
			}
			
			// If the component is stacked in a horizontal manner, need to repaint the whole row.
			if (componentStackType.horizontalStack())
			{
				width = DesktopApp.frame().getWidth();
				midX = width/2;
			}

			final Rectangle repaintArea = new Rectangle(midX - width/2, midY - height/2, width, height);
			DesktopApp.view().repaint(repaintArea);
		}
		catch (final Exception e)
		{
			// mouse off screen
		}
	}
	
	//-----------------------------------------------------------------------------

}
