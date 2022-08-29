package app.display.dialogs.editor;

import java.awt.Point;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

/**
 * Utilities for handling text panes
 * @author mrraow
 */
public class TextPaneUtils
{
	private static final String HTML_BREAK = "<br/>";
	private static final String HTML_END = "</html>";
	private static final String HTML_START = "<html>";

	/**
	 * @param textArea
	 * @return row number for the current caret position
	 */
	public static int getCaretRowNumber (final JTextPane textArea)
	{
		final int originalCarotPosition = textArea.getCaret().getDot();
    	int rn = (originalCarotPosition==0) ? 1 : 0;
        try 
        {
            int offs=originalCarotPosition;
            while(offs>0) 
            {
                offs=Utilities.getRowStart(textArea, offs)-1;
                rn++;
            }
        } catch (final BadLocationException e) 
        {
            e.printStackTrace();
        }
        return rn;
	}


	/**
	 * @param textArea
	 * @return the start of the current row as a caret position
	 */
	public static Point cursorCoords (final JTextPane textArea)
	{
		final Caret caret = textArea.getCaret();
		final Point point = caret.getMagicCaretPosition();
		return point;
	}
	
	/**
	 * @param textArea
	 * @return the start of the current row as a caret position
	 */
	public static int startOfCaretCurrentRow (final JTextPane textArea)
	{
		try
		{
			final int originalCarotPosition = textArea.getCaret().getDot();
			return Utilities.getRowStart(textArea, originalCarotPosition);
		}
		catch (final BadLocationException e2)
		{
			e2.printStackTrace();
			return 0;
		}
	}

	/**
	 * @param tc
	 * @param pt
	 * @return partial word before specified point in text area
	 */
	public static String getLettersBeforePoint (final JTextComponent tc, final Point pt) 
	{
		try {
			final int pos = tc.viewToModel(pt);
			final int start = Utilities.getWordStart(tc, pos);
			return tc.getText(start, pos - start);
		} catch (final BadLocationException e) {
			System.err.println(e);
		}

		return null;
	}

	/**
	 * @param tc
	 * @param pt
	 * @return Word at specified point in text area
	 */
	public static String getWordAtPoint (final JTextComponent tc, final Point pt) 
	{
		try {
			final int pos = tc.viewToModel(pt);
			final int start = Utilities.getWordStart(tc, pos);
			final int end = Utilities.getWordEnd(tc, pos);
			return tc.getText(start, end - start);
		} catch (final BadLocationException e) {
			System.err.println(e);
		}

		return null;
	}

	public static String replaceWordAtPoint (final JTextComponent tc, final Point pt, final String newWord) 
	{
		try {
			final int pos = tc.viewToModel(pt);
			final int start = Utilities.getWordStart(tc, pos);
			final int end = Utilities.getWordEnd(tc, pos);
			return (tc.getText(0, start) + newWord + tc.getText(end, tc.getText().length() - end));
		} catch (final BadLocationException e) {
			System.err.println(e);
		}

		return null;
	}

	/**
	 * @param tc
	 * @param newWord
	 * @return new text after replacement
	 */
	public static String replaceWordAtCaret (final JTextComponent tc, final String newWord) 
	{
		try {
			final int pos = tc.getCaretPosition();
			final int start = Utilities.getWordStart(tc, pos);
			final int end = Utilities.getWordEnd(tc, pos);
			return (tc.getText(0, start) + newWord + tc.getText(end, tc.getText().length() - end));
		} catch (final BadLocationException e) {
			System.err.println(e);
		}

		return null;
	}
	
	/**
	 * Inserts the text at the caret
	 * @param tc
	 * @param text
	 */
	public static void insertAtCaret (final JTextComponent tc, final String text) 
	{
		tc.replaceSelection(text);
	}

	/**
	 * @param s1
	 * @param s2
	 * @return first difference between two strings with a little context, for debugging undo/redo
	 */
	public static String firstDiff (final String s1, final String s2)
	{
		for (int i = 0; i < s1.length() && i < s2.length(); i++) {
			if (s1.charAt(i) != s2.charAt(i)) {
				final String sub = i+20 < s1.length() ? s1.substring(i, i+20) : s1.substring(i);
				return sub.replace("\r", "\\r").replace("\n", "\\n");
			}
		}
		return "";
	}

	/**
	 * Converts a string with newlines, etc. into something with html markup.
	 * @return HTML version of input string.
	 */
	public static String convertToHTML (final String source)
	{
		if (source.toLowerCase().startsWith(HTML_START)) return source;
		
		return HTML_START+
				source.replace("\r\n",  HTML_BREAK)
				.replace("\r", HTML_BREAK)
				.replace("\n", HTML_BREAK)+
				HTML_END;
	}
}
