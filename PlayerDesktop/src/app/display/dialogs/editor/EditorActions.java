package app.display.dialogs.editor;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public enum EditorActions
{
	UNDO,
	REDO,
	DELETE_LINE,
	AUTOSUGGEST,
	COPY_SELECTION,
	REMOVE_SELECTION,
	PASTE_BUFFER,
	NO_ACTION,
	TAB;
	
	/**
	 * @param e
	 * @return action requested by the user
	 */
	@SuppressWarnings("deprecation")
	public static EditorActions fromKeyEvent (KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_TAB) return TAB;
		
		if ((e.getModifiers() & InputEvent.CTRL_MASK) == 0) return NO_ACTION;
		
		switch (e.getKeyCode()) {
		case KeyEvent.VK_D: return DELETE_LINE;
		case KeyEvent.VK_Y: return REDO;
		case KeyEvent.VK_Z: return UNDO;
		case KeyEvent.VK_PERIOD: return AUTOSUGGEST;
		case KeyEvent.VK_C: return COPY_SELECTION;
		case KeyEvent.VK_X: return REMOVE_SELECTION;
		case KeyEvent.VK_V: return PASTE_BUFFER;
		}
		
		return NO_ACTION;
	}
}
