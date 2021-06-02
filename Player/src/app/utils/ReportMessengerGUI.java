package app.utils;

import java.awt.EventQueue;

import app.PlayerApp;
import main.grammar.Report.ReportMessenger;

/**
 * Report Messenger implementation for the GUI.
 * 
 * @author Matthew.Stephenson
 */
public class ReportMessengerGUI implements ReportMessenger
{
	
	private final PlayerApp app;
	
	//-------------------------------------------------------------------------
	
	public ReportMessengerGUI(final PlayerApp app)
	{
		super();
		this.app = app;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void printMessageInStatusPanel(final String s)
	{
		try
		{
			EventQueue.invokeLater(() -> 
			{
				app.addTextToStatusPanel(s);
			});
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void printMessageInAnalysisPanel(final String s)
	{
		try
		{
			EventQueue.invokeLater(() -> 
			{
				app.addTextToAnalysisPanel(s);
			});
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

}
