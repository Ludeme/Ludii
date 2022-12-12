package app.views.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

import app.PlayerApp;
import app.utils.SettingsExhibition;
import game.rules.play.moves.Moves;
import other.action.Action;
import other.context.Context;
import other.move.Move;

//-----------------------------------------------------------------------------

/**
 * Tool panel button.
 * 
 * @author Matthew.Stephenson and cambolbro
 */
public abstract class ToolButton
{
	protected final PlayerApp app;
	
	/** Button name. */
	protected String name = "?";

	/** Default Button colour. */
	protected static Color buttonColour = new Color(50, 50, 50);

	/** Rollover button colour */
	protected static Color rolloverButtonColour = new Color(127, 127, 127);

	/** Default grayed out / invalid Button colour. */
	protected static Color invalidButtonColour = new Color(220,220,220);

	/** Rectangle bounding box for button */
	protected Rectangle rect = new Rectangle();

	/** Whether or not a mouse is over the button */
	protected boolean mouseOver = false;
	
	/** Tooltip message for when cursor is over this button. */
	protected String tooltipMessage = "Default Message";
	
	protected int buttonIndex = -1;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param name
	 * @param cx
	 * @param cy
	 */
	public ToolButton(final PlayerApp app, final String name, final int cx, final int cy, final int sx, final int sy, final int buttonIndex)
	{
		this.app = app;
		this.name = name;
		this.buttonIndex = buttonIndex;

		rect.x = cx;
		rect.y = cy;
		rect.width = sx;
		rect.height = sy;
		
		if (SettingsExhibition.exhibitionVersion)
		{
			buttonColour = new Color(220,220,220);
			invalidButtonColour = new Color(100,100,100);
			rect.width *= 2;
			rect.height *= 2;
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Button name.
	 */
	public String name()
	{
		return name;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param x
	 * @param y
	 */
	public void setPosition(final int x, final int y)
	{
		rect.x = x;
		rect.y = y;
	}

	/**
	 * Scale toolbar buttons depending on type of device.
	 * @return 2 for mobile device, 1 for everything else.
	 */
	public double scaleForDevice()
	{
		// Based on default toolbar height for desktop player of 32 pixels.
		return rect.getHeight() / 32.0;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param g2d
	 */
	public abstract void draw(final Graphics2D g2d);

	//-------------------------------------------------------------------------

	/**
	 * Execute the button press.
	 */
	public abstract void press();

	//-------------------------------------------------------------------------

	/**
	 * @param x X screen pixel.
	 * @param y Y screen pixel.
	 * @return Whether the specified point hits this button.
	 */
	public boolean hit(final int x, final int y)
	{
		return (x >= rect.x) && (x <= rect.x + rect.width) && (y >= rect.y) && y <= (rect.y + rect.height);
	}

	//-------------------------------------------------------------------------

	/**
	 * Set if the mouse if over the button.
	 */
	public void setMouseOver(final boolean b)
	{
		mouseOver = b;
	}

	/**
	 * If the mouse cursor is over the button.
	 */
	public boolean mouseOver()
	{
		return mouseOver;
	}

	/**
	 * The bounding box of the button.
	 */
	public Rectangle rect()
	{
		return rect;
	}
	
	/**
	 * The tooltip message when the cursor hovers over the button
	 */
	public String tooltipMessage()
	{
		return tooltipMessage;
	}
	
	/** 
	 * If the button is enabled and can be pressed. True by default. 
	 */
	@SuppressWarnings("static-method")
	protected boolean isEnabled()
	{
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Set temporary message to all legal actions.
	 */
	protected void showPossibleMovesTemporaryMessage()
	{
		final Context context = app.contextSnapshot().getContext(app);
		final Moves legal = context.moves(context);
		final ArrayList<String> allOtherMoveDescriptions = new ArrayList<String>();
		for (final Move move : legal.moves())
		{
			for (int i = 0; i < move.actions().size(); i++)
			{
				if (move.actions().get(i).isDecision())
				{
					final Action decisionAction = move.actions().get(i);
					final String desc = decisionAction.getDescription();
					
					if (!allOtherMoveDescriptions.contains(desc))
						allOtherMoveDescriptions.add(desc);
					break;
				}
			}
		}
		if (allOtherMoveDescriptions.size() > 0)
		{
			String tempMessageString = "You may ";
			if (legal.moves().size() == 1)
				tempMessageString = "You must ";
				
			for (final String s : allOtherMoveDescriptions)
				tempMessageString += s + " or ";

			tempMessageString = tempMessageString.substring(0, tempMessageString.length()-4);
			tempMessageString += ".";
			app.setTemporaryMessage(tempMessageString);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * The colour of the button. 
	 */
	protected Color getButtonColour()
	{
		if (isEnabled())
		{
			if (mouseOver)
				return rolloverButtonColour;
			else
				return buttonColour;
		}
		else
			return invalidButtonColour;
	}

}
