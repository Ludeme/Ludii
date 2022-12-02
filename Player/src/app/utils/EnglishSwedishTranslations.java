package app.utils;

public enum EnglishSwedishTranslations 
{
	// Menu Text
	MYOGTITLE("Make Your Own Game", "TODO SWEDISH"),
	CHOOSEBOARD("Choose a board", "TODO SWEDISH"),
	DRAGPIECES("Drag pieces onto board", "TODO SWEDISH"),
	MOVEMENT("Movement", "TODO SWEDISH"),
	CAPTURE("Capture", "TODO SWEDISH"),
	GOAL("Goal", "TODO SWEDISH"),
	
	// Button Text
	RESET("Reset", "TODO SWEDISH"),
	PLAY("Play", "TODO SWEDISH"),
	PLAYAGAIN("Play Again", "TODO SWEDISH"),
	EDIT("Edit", "TODO SWEDISH"),
	HUMANVSHUMAN("vs. Human", "TODO SWEDISH"),
	HUMANVSAI("vs. AI", "TODO SWEDISH"),
	
	// Goal Options
	LINE3("Line 3", "TODO SWEDISH"),
	LINE4("Line 4", "TODO SWEDISH"),
	ELIMINATE("Eliminate", "TODO SWEDISH"),
	BLOCK("Block", "TODO SWEDISH"),
	SURROUND("Surround", "TODO SWEDISH"),
	
	// Move Options
	STEP("Step", "TODO SWEDISH"),
	SLIDE("Slide", "TODO SWEDISH"),
	KNIGHT("Knight", "TODO SWEDISH"),
	ADD("Add", "TODO SWEDISH"),
	ANY("Any", "TODO SWEDISH"),
	
	// Capture Options
	REPLACE("Replace", "TODO SWEDISH"),
	HOP("Hop", "TODO SWEDISH"),
	FLANK("Flank", "TODO SWEDISH"),
	NEIGHBOR("Neighbor", "TODO SWEDISH"),
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

	public static boolean inEnglish()
	{
		return inEnglish;
	}
	
	public static void setInEnglish(final boolean inEnglish)
	{
		EnglishSwedishTranslations.inEnglish = inEnglish;
	}
	
	@Override
	public String toString() 
	{
		return inEnglish ? english : swedish;
	}
	
}
