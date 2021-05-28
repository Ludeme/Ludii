package app.utils;

import app.PlayerApp;
import bridge.ViewControllerFactory;
import game.equipment.component.Component;
import game.equipment.container.Container;
import metadata.Metadata;
import other.context.Context;

/**
 * Functions for setting up the Model-View-Controller within the Bridge.
 * 
 * @author Matthew.Stephenson and cambolbro
 */
public class MVCSetup 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Set the Bridge MVC objects to the correct Styles for each container and component.
	 */
	public static void setMVC(final PlayerApp app)
	{
		final Context context = app.manager().ref().context();
		final Metadata metadata = context.metadata();
		
		// Game metadata can be used to disable animation.
		app.bridge().settingsVC().setNoAnimation(context.game().metadata().graphics().noAnimation());
		
		// ContainerStyle
		if (metadata != null && metadata.graphics().boardStyle() != null)
			context.board().setStyle(metadata.graphics().boardStyle());
		
		for (final Container c : context.equipment().containers())
		{
			app.bridge().addContainerStyle(ViewControllerFactory.createStyle(app.bridge(), c, c.style(), context), c.index());
			app.bridge().addContainerController(ViewControllerFactory.createController(app.bridge(), c, c.controller()), c.index());
		}
		
		// Component style
		for (final Component c : context.equipment().components())
		{
			if (c != null)
			{				
				if (metadata != null && metadata.graphics().componentStyle(c.owner(), c.name(), context) != null)
				{
					// Override the component's default style with that specified in metadata
					c.setStyle( metadata.graphics().componentStyle(c.owner(), c.name(), context));
				}
				app.bridge().addComponentStyle(ViewControllerFactory.createStyle(app.bridge(), c, c.style()), c.index());
			}
		}
		
		app.bridge().setGraphicsRenderer(app);
	}
	
	//-------------------------------------------------------------------------
	
}
