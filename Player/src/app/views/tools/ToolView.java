package app.views.tools;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import app.PlayerApp;
import app.utils.GameUtil;
import app.views.View;
import app.views.tools.buttons.ButtonBack;
import app.views.tools.buttons.ButtonEnd;
import app.views.tools.buttons.ButtonForward;
import app.views.tools.buttons.ButtonInfo;
import app.views.tools.buttons.ButtonOther;
import app.views.tools.buttons.ButtonPass;
import app.views.tools.buttons.ButtonPlayPause;
import app.views.tools.buttons.ButtonSettings;
import app.views.tools.buttons.ButtonShow;
import app.views.tools.buttons.ButtonStart;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.location.FullLocation;
import other.move.Move;
import other.trial.Trial;

//-----------------------------------------------------------------------------

/**
 * View showing the tool buttons.
 * @author Matthew.Stephenson and cambolbro
 */
public class ToolView extends View
{
	/** List of buttons. */
	public List<ToolButton> buttons = new ArrayList<>();

	/** Index values for each of the tool buttons, determines the order drawn from left to right. */
	
	public static final int START_BUTTON_INDEX    		= 0;
	public static final int BACK_BUTTON_INDEX     		= 1;
	public static final int PLAY_BUTTON_INDEX     		= 2;
	public static final int FORWARD_BUTTON_INDEX  		= 3;
	public static final int END_BUTTON_INDEX      		= 4;
	
	public static final int PASS_BUTTON_INDEX     		= 5;
	public static final int OTHER_BUTTON_INDEX    		= 6;
	public static final int SHOW_BUTTON_INDEX    		= 7;
	
	public static final int SETTINGS_BUTTON_INDEX 		= 8;
	public static final int INFO_BUTTON_INDEX     		= 9;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public ToolView(final PlayerApp app)
	{
		super(app);

		final int toolHeight = 40;
		final int boardSize = app.height();
		
		final int startX = boardSize;
		final int startY = app.height() - toolHeight;
		final int width = app.width() - boardSize;
		
		placement.setBounds(startX, startY, width, toolHeight);
		drawButtons();
	}
	
	//-------------------------------------------------------------------------
	
	public void drawButtons()
	{
		int cx = placement.x;
		final int cy = placement.y;

		final int sx = placement.height - 8;
		final int sy = placement.height - 8;

		buttons.add(new ButtonStart(app, cx, cy, sx, sy, START_BUTTON_INDEX));
		buttons.add(new ButtonBack(app, cx, cy, sx, sy, BACK_BUTTON_INDEX));
		buttons.add(new ButtonPlayPause(app, cx, cy, sx, sy, PLAY_BUTTON_INDEX));
		buttons.add(new ButtonForward(app, cx, cy, sx, sy, FORWARD_BUTTON_INDEX));
		buttons.add(new ButtonEnd(app, cx, cy, sx, sy, END_BUTTON_INDEX));
		buttons.add(new ButtonPass(app, cx, cy, sx, sy, PASS_BUTTON_INDEX));
		if (otherButtonShown(app.manager().ref().context()))
			buttons.add(new ButtonOther(app, cx, cy, sx, sy, OTHER_BUTTON_INDEX));
		else
			buttons.add(null);  
		buttons.add(new ButtonShow(app, cx, cy, sx, sy, SHOW_BUTTON_INDEX));
		buttons.add(new ButtonSettings(app, cx, cy, sx, sy, SETTINGS_BUTTON_INDEX));
		buttons.add(new ButtonInfo(app, cx, cy, sx, sy, INFO_BUTTON_INDEX));
	
		final double spacing = placement.width / (double) buttons.size();

		for (int b = 0; b < buttons.size(); b++)
		{
			if (buttons.get(b) == null)
				continue;  // is spacer
			
			cx = placement.x + (int) ((b + 0.25) * spacing) + 10;
			buttons.get(b).setPosition(cx, cy);
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public void paint(final Graphics2D g2d)
	{
		for (final ToolButton button : buttons)
			if (button != null)
				button.draw(g2d);
		
		paintDebug(g2d, Color.BLUE);
	}

	//-------------------------------------------------------------------------

	/**
	 * Handle click on tool panel.
	 * @param pixel
	 */
	public void clickAt(final Point pixel)
	{
		for (final ToolButton button : buttons)
			if (button != null && button.hit(pixel.x, pixel.y))
				button.press();
	}

	//-------------------------------------------------------------------------

	@Override
	public void mouseOverAt(final Point pixel)
	{
		// See if mouse is over any of the tool buttons
		for (final ToolButton button : buttons)
		{
			if (button == null)
				continue;
			
			if (button.hit(pixel.x, pixel.y))
			{
				if (button.mouseOver() != true)
				{
					button.setMouseOver(true);
					app.repaint(button.rect());
				}
			}
			else
			{
				if (button.mouseOver() != false)
				{
					button.setMouseOver(false);
					app.repaint(button.rect());
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	public static void jumpToMove(final PlayerApp app, final int moveToJumpTo)
	{
		app.manager().settingsManager().setAgentsPaused(app.manager(), true);
		app.settingsPlayer().setJumpingMoves(true);
		
		final Context context = app.manager().ref().context();

		if (app.manager().savedTrial() == null)
		{
			final Trial savedTrial = new Trial(context.trial());
			app.manager().setSavedTrial(savedTrial);
		}
		
		GameUtil.resetContext(app);
		
		final int moveToJumpToWithSetup;
		if (moveToJumpTo == 0)
			moveToJumpToWithSetup = context.currentInstanceContext().trial().numInitialPlacementMoves();
		else
			moveToJumpToWithSetup = moveToJumpTo;

		final List<Move> savedTrialMoves = app.manager().savedTrial().generateCompleteMovesList();
		for (int i = context.trial().numMoves(); i < moveToJumpToWithSetup; i++)
		{
			app.manager().ref().makeSavedMove(app.manager(), savedTrialMoves.get(i));
			
			final int moveNumber = context.currentInstanceContext().trial().numMoves() - 1;
			
			// Current game (or previous instance in match) is over
			if (context.trial().over() || (context.isAMatch() && moveNumber < context.currentInstanceContext().trial().numInitialPlacementMoves()))
				GameUtil.gameOverTasks(app);
		}
		
		// this is just a tiny bit hacky, but makes sure MCTS won't reuse incorrect tree after going back in Trial
		context.game().incrementGameStartCount();

		EventQueue.invokeLater(() ->
		{
			app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
			app.settingsPlayer().setJumpingMoves(false);
			app.updateTabs(context);
		});
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return If the Other button should be shown for this game.
	 */
	private static boolean otherButtonShown(final Context context)
	{
		if (context.game().booleanConcepts().get(Concept.BetDecision.id()))
			return true;
		if (context.game().booleanConcepts().get(Concept.VoteDecision.id()))
			return true;
		if (context.game().booleanConcepts().get(Concept.SetNextPlayer.id()))
			return true;
		if (context.game().booleanConcepts().get(Concept.ChooseTrumpSuit.id()))
			return true;
		if (context.game().booleanConcepts().get(Concept.SwapOption.id()))
			return true;
		if (context.game().booleanConcepts().get(Concept.ProposeDecision.id()))
			return true;
		return false;
	}
	
	//-------------------------------------------------------------------------
	
}
