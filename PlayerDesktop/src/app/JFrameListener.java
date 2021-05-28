package app;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import app.move.MoveHandler;
import app.views.tools.ToolView;
import game.util.directions.AbsoluteDirection;

/**
 * JFrame for listening to keyboard button presses.
 * @author Matthew.Stephenson
 *
 */
public class JFrameListener extends JFrame implements KeyListener
{
	private static final long serialVersionUID = 1L;

	public PlayerApp app;

	JFrameListener(final String appName, final PlayerApp app)
	{
		super(appName);
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		this.app = app;
	}

	@Override
	public void keyPressed(final KeyEvent e)
	{
		// Rotate large pieces.
		if (e.getKeyCode() == KeyEvent.VK_R)
		{
			app.settingsPlayer().setCurrentWalkExtra(app.settingsPlayer().currentWalkExtra()+1);
			app.repaint();
		}
		else if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			DesktopApp.view().toolPanel().buttons.get(ToolView.PLAY_BUTTON_INDEX).press();
		}
		else if (e.getKeyCode() == KeyEvent.VK_LEFT)
		{
			DesktopApp.view().toolPanel().buttons.get(ToolView.BACK_BUTTON_INDEX).press();
		}
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
		{
			DesktopApp.view().toolPanel().buttons.get(ToolView.FORWARD_BUTTON_INDEX).press();
		}
		else if (e.getKeyCode() == KeyEvent.VK_DOWN)
		{
			DesktopApp.view().toolPanel().buttons.get(ToolView.START_BUTTON_INDEX).press();
		}
		else if (e.getKeyCode() == KeyEvent.VK_UP)
		{
			DesktopApp.view().toolPanel().buttons.get(ToolView.END_BUTTON_INDEX).press();
		}
		else if (e.getKeyCode() == KeyEvent.VK_TAB)
		{
			int nextTabIndex = app.settingsPlayer().tabSelected() + 1;
			if (nextTabIndex >= DesktopApp.view().tabPanel().pages().size())
				nextTabIndex = 0;
			DesktopApp.view().tabPanel().select(nextTabIndex);
		}
		else if (e.getKeyCode() == KeyEvent.VK_NUMPAD8)
		{
			MoveHandler.applyDirectionMove(app, AbsoluteDirection.N);
		}
		else if (e.getKeyCode() == KeyEvent.VK_NUMPAD4)
		{
			MoveHandler.applyDirectionMove(app, AbsoluteDirection.W);
		}
		else if (e.getKeyCode() == KeyEvent.VK_NUMPAD2)
		{
			MoveHandler.applyDirectionMove(app, AbsoluteDirection.S);
		}
		else if (e.getKeyCode() == KeyEvent.VK_NUMPAD6)
		{
			MoveHandler.applyDirectionMove(app, AbsoluteDirection.E);
		}
		else if (e.getKeyCode() == KeyEvent.VK_NUMPAD1)
		{
			MoveHandler.applyDirectionMove(app, AbsoluteDirection.SW);
		}
		else if (e.getKeyCode() == KeyEvent.VK_NUMPAD3)
		{
			MoveHandler.applyDirectionMove(app, AbsoluteDirection.SE);
		}
		else if (e.getKeyCode() == KeyEvent.VK_NUMPAD7)
		{
			MoveHandler.applyDirectionMove(app, AbsoluteDirection.NW);
		}
		else if (e.getKeyCode() == KeyEvent.VK_NUMPAD9)
		{
			MoveHandler.applyDirectionMove(app, AbsoluteDirection.NE);
		}
	}

	@Override
	public void keyReleased(final KeyEvent arg0)
	{
		// do nothing
	}

	@Override
	public void keyTyped(final KeyEvent arg0)
	{
		// do nothing
	}
}
