package app.utils;

import app.PlayerApp;
import other.context.Context;
import other.context.InformationContext;

/**
 * A snapshot of the last recorded context.
 * Updated and frozen whenever painting to avoid threading issues.
 * 
 * @author Matthew.Stephenson
 */
public class ContextSnapshot
{
	
	private Context copyOfCurrentContext = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Don't instantiate me.
	 */
	public ContextSnapshot()
	{
		// Do Nothing
	}
	
	//-------------------------------------------------------------------------
	
	public void setContext(final PlayerApp app)
	{
		copyOfCurrentContext = new InformationContext(app.manager().ref().context(), app.settingsPlayer().getIntermediateContextPlayerNumber(app));
	}
	
	//-------------------------------------------------------------------------
	
	public Context getContext(final PlayerApp app)
	{
		if (copyOfCurrentContext == null)
			setContext(app);
		
		return copyOfCurrentContext;
	}
	
	//-------------------------------------------------------------------------
	
}
