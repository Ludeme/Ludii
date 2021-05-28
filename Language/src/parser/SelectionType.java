package parser; 

/**
 * Types of ways in which editor text can be replaced.
 * 
 * @author cambolbro
 */
public enum SelectionType
{
	/** Replacement through right-click context selection. */
	CONTEXT,

	/** Replacement of selected text. */
	SELECTION,
	
	/** Replacement through autosuggest in response to typing. */
	TYPING
}
