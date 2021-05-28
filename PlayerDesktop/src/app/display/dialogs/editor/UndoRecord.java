package app.display.dialogs.editor;

import javax.swing.JTextPane;

public class UndoRecord
{
	public final String text;
	public final int caretPos;
	public final int selectionStart;
	public final int selectionEnd;
	
	public UndoRecord (final JTextPane textArea)
	{
		text = textArea.getText();
		caretPos = textArea.getCaret().getDot();
		selectionStart = textArea.getSelectionStart();
		selectionEnd = textArea.getSelectionEnd();
	}
	
	public void apply (final JTextPane textArea)
	{
		textArea.setText(text);
		textArea.setCaretPosition(caretPos);
		textArea.setSelectionStart(selectionStart);
		textArea.setSelectionEnd(selectionEnd);
	}
	
	public boolean ignoreChanges (final JTextPane textArea)
	{
		return text.equals(textArea.getText());
	}
}
