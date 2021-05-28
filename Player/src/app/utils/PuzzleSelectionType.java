package app.utils;

/**
 * Different ways that values can be selected for deduction puzzles.
 * 
 * @author Matthew.Stephenson
 */
public enum PuzzleSelectionType 
{
	
	Automatic, 	// Pick from the others based on the # possible values.
	Dialog,		// Show a dialog with all possible values.
	Cycle;		// Cycle through to the next possible value.
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the PuzzleSelectionType who's value matches the provided name.
	 */
	public static PuzzleSelectionType getPuzzleSelectionType(final String name)
	{
        for (final PuzzleSelectionType puzzleSelectionType : PuzzleSelectionType.values())
            if (puzzleSelectionType.name().equals(name)) 
            	return puzzleSelectionType;
        
        return Automatic;
    }

	//-------------------------------------------------------------------------
	
}
