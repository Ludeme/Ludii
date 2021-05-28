package controllers.container;

import bridge.Bridge;
import controllers.BaseController;
import game.equipment.container.Container;

/**
 * Basic controller for moving pieces. Used in most games.
 * 
 * @author Matthew.Stephenson
 */
public class BasicController extends BaseController
{
	public BasicController(final Bridge bridge, final Container container) 
	{
		super(bridge, container);
	}
}
