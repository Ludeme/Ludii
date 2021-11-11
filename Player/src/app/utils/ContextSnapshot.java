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
	
	private static int getInformationContextPlayerNumber(final PlayerApp app)
	{
		final Context context = app.manager().ref().context();
		int mover = context.state().mover();
		
		if (context.game().isDeductionPuzzle())
			return mover;
		
		if (app.manager().settingsNetwork().getNetworkPlayerNumber() > 0)
		{
			mover = app.manager().settingsNetwork().getNetworkPlayerNumber();
		}
		else if (app.settingsPlayer().hideAiMoves())
		{
			int humansFound = 0;
			int humanIndex = 0;
			for (int i = 1; i <= context.game().players().count(); i++)
			{
				if (app.manager().aiSelected()[i].ai() == null)
				{
					humansFound++;
					humanIndex = app.manager().playerToAgent(i);
				}
			}
			
			if (humansFound == 1)
				mover = humanIndex;
		}
		
		return mover;
	}
	
	//-------------------------------------------------------------------------
	
	public void setContext(final Context context)
	{
		copyOfCurrentContext = context;
	}
	
	public void setContext(final PlayerApp app)
	{
		copyOfCurrentContext = new InformationContext(app.manager().ref().context(), getInformationContextPlayerNumber(app));
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
