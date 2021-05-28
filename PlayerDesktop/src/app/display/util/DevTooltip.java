package app.display.util;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ToolTipManager;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import app.DesktopApp;
import app.PlayerApp;
import app.utils.SVGUtil;
import app.views.tools.ToolButton;
import game.Game;
import game.equipment.component.Card;
import game.equipment.component.Component;
import game.types.board.SiteType;
import game.types.component.CardType;
import main.Constants;
import other.context.Context;
import other.location.Location;
import other.state.container.ContainerState;
import util.ContainerUtil;
import util.LocationUtil;
import view.component.ComponentStyle;

/**
 * Tooltip used for displaying developer information.
 * 
 * @author Matthew.Stephenson
 */
public class DevTooltip
{

	//-------------------------------------------------------------------------
	
	/**
	 * Display a tool tip message for the current point
	 * Shows the following values:
	 * - cellIndex
	 * - componentName
	 * - componentIndex
	 * - cellOwner
	 * - localState
	 * - rotationState
	 * - countState
	 * - hidden value for each player
	 * - component image
	 */
	public static void displayToolTipMessage(final PlayerApp app, final Point pt)
	{
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    	ToolTipManager.sharedInstance().setReshowDelay(500);
    	boolean toolTipShown = false;
    	
    	// If cursor is over any tool buttons then print message.
    	for (final ToolButton toolButton : DesktopApp.view().toolPanel().buttons)
    	{
    		if (toolButton != null && toolButton.mouseOver())
    		{
    			String toolTipMessage = "<html>";
    			toolTipMessage += toolButton.tooltipMessage();
    			toolTipMessage += "</html>";
    			DesktopApp.view().setToolTipText(toolTipMessage);
    			toolTipShown = true;
    			break;
    		}
    	}
    	
    	// If dev tooltips are on, show full information.
		if 
		(
			!toolTipShown 
			&&
			app.settingsPlayer().cursorTooltipDev() 
			&& 
			app.manager().settingsNetwork().getActiveGameId() == 0
		)
		{
			try
			{
				final Context context = app.manager().ref().context();
				final Game game = context.game();
				
				ToolTipManager.sharedInstance().setEnabled(true);
				
				final Point ptOff = new Point(pt.x, pt.y);
	
				final Location location = LocationUtil.calculateNearestLocation(context, app.bridge(), ptOff, LocationUtil.getAllLocations(context, app.bridge()));
				final int index = location.site();
				final int level = location.level();
				final SiteType type = location.siteType();
				final int containerId = ContainerUtil.getContainerId(context, index, type);
				final ContainerState cs = context.state().containerStates()[containerId];
	
				final int componentIndex = cs.what(index, level, type);
				String componentName = "";
				Component component = null;
				if (componentIndex != 0)
				{
					component = context.equipment().components()[componentIndex];
					componentName = component.name();
				}
				final int owner = cs.who(index, level, type);
				final int localState = cs.state(index, level, type);
				final int rotationState = cs.rotation(index, level, type);
				final int countState = cs.count(index, type);
				
				final int cardSuit = component.isCard() ? ((Card)component).suit() : Constants.UNDEFINED;
				final int cardRank = component.rank();
				final int trumpRank = component.trumpRank();
				final int trumpValue = component.trumpValue();
				final CardType cardType = component.cardType();
				
				final int value1 = cs.value(index, level, type);
				final int value2 = component.getValue2();
				
				final int stackSize = cs.sizeStack(index, type);
				
				final String[] hiddenArray = new String[game.players().count()+1];
				if (game.hiddenInformation() || game.hasCard())
				{
					
					for (int i = 1; i <= game.players().count(); i++)
					{
						final boolean hidden = cs.isHidden(i, index, level, type);
						final boolean hiddenWhat = cs.isHiddenWhat(i, index, level, type);
						final boolean hiddenWho =  cs.isHiddenWho(i, index, level, type);
						final boolean hiddenCount = cs.isHiddenState(i, index, level, type);
						final boolean hiddenValue = cs.isHiddenValue(i, index, level, type);
						final boolean hiddenState = cs.isHiddenCount(i, index, level, type);
						final boolean hiddenRotation = cs.isHiddenRotation(i, index, level, type);
						
						hiddenArray[i] = "hidden: " + hidden + ", hiddenWhat: " + hiddenWhat + ", hiddenWho: " + hiddenWho + ", hiddenCount: " + hiddenCount + ", hiddenValue: " + hiddenValue + ", hiddenState: " + hiddenState + ", hiddenRotation: " + hiddenRotation;
					}
				}
	
				String toolTipMessage = "<html>";
				
					try
					{
						final int imageSize = 100;		// this size of the image when displayed on the tooltip
						final File outputfile = File.createTempFile("tooltipImage", ".png");
						outputfile.deleteOnExit();
						String fullPath = outputfile.getAbsolutePath();
						fullPath = "file:" + fullPath.replaceAll(Pattern.quote("\\"), "/");
						final ComponentStyle componentStyle = app.bridge().getComponentStyle(component.index());
						componentStyle.renderImageSVG(context, imageSize, localState, value1, true, 0, rotationState);
						final SVGGraphics2D svg = app.bridge().getComponentStyle(component.index()).getImageSVG(localState);
						final BufferedImage toolTipImage = SVGUtil.createSVGImage(svg.getSVGDocument(), imageSize, imageSize);
						ImageIO.write(toolTipImage, "png", outputfile);
						toolTipMessage += "<img src=\"" + fullPath + "\">" +"<br>";
					}
					catch (final Exception e)
					{
						// something went wrong when displaying the image, carry on.
					}
	
				toolTipMessage += "Index: " + index +"<br>";
				if (type != null)
					toolTipMessage += "Type: " + type +"<br>";
				if (componentIndex != 0)
					toolTipMessage += "componentName: " + componentName +"<br>";
				if (componentIndex != 0)
					toolTipMessage += "componentIndex: " + componentIndex +"<br>";
				if (owner != 0)
					toolTipMessage += "Owner: " + owner +"<br>";
				if (localState != 0)
					toolTipMessage += "localState: " + localState +"<br>";
				//if (rotationState != 0)
					toolTipMessage += "rotationState: " + rotationState +"<br>";
				if (countState != 0)
					toolTipMessage += "countState: " + countState +"<br>";
				if (value1 != -1)
					toolTipMessage += "value1: " + value1 +"<br>";
				if (value2 != -1)
					toolTipMessage += "value2: " + value2 +"<br>";
				if (game.isStacking())
				{
					toolTipMessage += "stackSize: " + stackSize +"<br>";
					toolTipMessage += "level: " + level +"<br>";
				}
				
				if (game.hasCard())
				{
					toolTipMessage += "cardSuit: " + cardSuit +"<br>";
					toolTipMessage += "cardRank: " + cardRank +"<br>";
					toolTipMessage += "trumpRank: " + trumpRank +"<br>";
					toolTipMessage += "trumpValue: " + trumpValue +"<br>";
					toolTipMessage += "cardType: " + cardType +"<br>";
				}

				for (int i = 1; i < hiddenArray.length; i++)
				{
					if (hiddenArray[i] != null)
					{
						toolTipMessage += "Player " + (i) +  ": " + hiddenArray[i] +"<br>";
					}
				}
	
				toolTipMessage += "</html>";
		    	DesktopApp.view().setToolTipText(toolTipMessage);
		    	toolTipShown = true;
		    	
			}
			catch (final Exception e)
			{
				//e.printStackTrace();
				DesktopApp.view().setToolTipText(null);
				return;
			}
		}
		
		if (!toolTipShown)
			DesktopApp.view().setToolTipText(null);
	}
	
	//-------------------------------------------------------------------------
	
}
