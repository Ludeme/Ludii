package app.utils;

public enum EnglishSwedishTranslations 
{
	// Menu Text
	MYOGTITLE("Make Your Own Game", "TODO"),
	CHOOSEBOARD("Choose a board", "TODO"),
	DRAGPIECES("Drag pieces onto board", "TODO"),
	MOVEMENT("Movement", "TODO"),
	CAPTURE("Capture", "TODO"),
	CHOOSEGOALS("Choose goal(s)", "TODO"),
	
	// Button Text
	RESET("Reset", "TODO"),
	PLAY("Play", "TODO"),
	PLAYAGAIN("Player Again", "TODO"),
	EDIT("Edit", "TODO"),
	HUMANVSHUMAN("Human vs Human", "TODO"),
	HUMANVSAI("Huma vs AI", "TODO"),
	
	// Goal Options
	LINE3("Line 3", "TODO"),
	LINE4("Line 4", "TODO"),
	ELIMINATE("Eliminate", "TODO"),
	BLOCK("Block", "TODO"),
	SURROUND("Surround", "TODO"),
	
	// Move Options
	STEP("Step", "TODO"),
	SLIDE("Slide", "TODO"),
	KNIGHT("Knight", "TODO"),
	ADD("Add", "TODO"),
	ANY("Any", "TODO"),
	
	// Capture Options
	REPLACE("Replace", "TODO"),
	HOP("Hop", "TODO"),
	FLANK("Flank", "TODO"),
	NEIGHBOR("Neighbor", "TODO"),
	;

	//-------------------------------------------------------------------------

	private String english;
	private String swedish;

	// Change this to use english or swedish words.
	private static boolean inEnglish = true;

	//--------------------------------------------------------------------------

	EnglishSwedishTranslations(final String english, final String swedish)
	{
		this.english = english;
		this.swedish = swedish;
	}
	
	//-------------------------------------------------------------------------

	public static void setInEnglish(final boolean inEnglish)
	{
		EnglishSwedishTranslations.inEnglish = inEnglish;
	}
	
	@Override
	public String toString() 
	{
		if (inEnglish)
			return english;
		else
			return swedish;
	}
	
}
