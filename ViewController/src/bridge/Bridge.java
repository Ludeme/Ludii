package bridge;

import java.util.ArrayList;

import controllers.Controller;
import util.SettingsColour;
import util.SettingsVC;
import view.component.ComponentStyle;
import view.container.ContainerStyle;

/**
 * Bridge Object, for linking the ViewController entities (component/container styles/controllers) with the PlayerDesktop.
 * 
 * @author Matthew.Stephenson
 */
public class Bridge 
{
	private PlatformGraphics graphicsRenderer;
	private final ArrayList<ContainerStyle> containerStyles = new ArrayList<>();
	private final ArrayList<ComponentStyle> componentStyles = new ArrayList<>();
	private final ArrayList<Controller> containerControllers = new ArrayList<>();
	
	private final SettingsVC settingsVC = new SettingsVC();
	private final SettingsColour settingsColour = new SettingsColour();

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public Bridge()
	{
	}
	
	//-------------------------------------------------------------------------
	
	public void setGraphicsRenderer(final PlatformGraphics g)
	{
		graphicsRenderer = g;
	}
	
	public PlatformGraphics graphicsRenderer()
	{
		return graphicsRenderer;
	}
	
	//-------------------------------------------------------------------------
	
	public void addContainerStyle(final ContainerStyle containerStyle, final int index)
	{
		for (int i = containerStyles.size(); i <= index; i++)
		{
			containerStyles.add(null);
		}
		containerStyles.set(index, containerStyle);
	}
	
	public void clearContainerStyles()
	{
		containerStyles.clear();
	}

	public ContainerStyle getContainerStyle(final int index)
	{
		return containerStyles.get(index);
	}
	
	public ArrayList<ContainerStyle> getContainerStyles()
	{
		return containerStyles;
	}
	
	//-------------------------------------------------------------------------
	
	public void addComponentStyle(final ComponentStyle componentStyle, final int index)
	{
		for (int i = componentStyles.size(); i <= index; i++)
		{
			componentStyles.add(null);
		}
		componentStyles.set(index, componentStyle);
	}
	
	public void clearComponentStyles()
	{
		componentStyles.clear();
	}

	public ComponentStyle getComponentStyle(final int index)
	{
		return componentStyles.get(index);
	}
	
	public ArrayList<ComponentStyle> getComponentStyles()
	{
		return componentStyles;
	}
	
	//-------------------------------------------------------------------------
	
	public void addContainerController(final Controller containerController, final int index)
	{
		for (int i = containerControllers.size(); i <= index; i++)
		{
			containerControllers.add(null);
		}
		containerControllers.set(index, containerController);
	}
	
	public void clearContainerControllers()
	{
		containerControllers.clear();
	}

	public Controller getContainerController(final int index)
	{
		return containerControllers.get(index);
	}

	public SettingsVC settingsVC() 
	{
		return settingsVC;
	}
	
	public SettingsColour settingsColour() 
	{
		return settingsColour;
	}
	
	//-------------------------------------------------------------------------
	
}
