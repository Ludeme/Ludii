package app.display.dialogs.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Document that can contain a maximum number of characters.
 * @author Matthew.Stephenson
 */
public class MaxLengthTextDocument extends PlainDocument 
{
	private static final long serialVersionUID = 1L;
	
	//Store maximum characters permitted
    private int maxChars;

    @Override
    public void insertString(final int offs, final String str, final AttributeSet a)
    throws BadLocationException 
    {
        // the length of string that will be created is getLength() + str.length()
        if(str != null && (getLength() + str.length() < maxChars))
        {
            super.insertString(offs, str, a);
        }
    }
    
    public void setMaxChars(final int i)
    {
    	maxChars = i;
    }
    
    public int getMaxChars()
    {
    	return maxChars;
    }

}
