package app.utils;

public enum EnglishSwedishTranslations 
{
	// Menu Text
	MYOGTITLE("Make Your Own Game", "Gör Ditt Eget Spel"),
	CHOOSEBOARD("Choose a board", "Välj en bräda"),
	DRAGPIECES("Drag pieces onto board", "Dra bitar till brädet"),
	MOVEMENT("Movement", "Rörelse"),
	CAPTURE("Capture", "Fånga"),
	GOAL("Goal", "Mål"),
	
	// Button Text
	RESET("Reset", "Återställa"),
	PLAY("Play", "Spela"),
	PLAYAGAIN("Play Again", "Spela igen"),
	EDIT("Edit", "Redigera"),
	HUMANVSHUMAN("vs. Human", "vs. Person"),
	HUMANVSAI("vs. AI", "vs. AI"),
	
	// Goal Options
	LINE3("Line 3", "Rad 3"),
	LINE4("Line 4", "Rad 4"),
	ELIMINATE("Eliminate", "Eliminera"),
	BLOCK("Block", "Blockera"),
	SURROUND("Surround", "Omge"),
	
	// Move Options
	STEP("Step", "Steg"),
	SLIDE("Slide", "Glida"),
	KNIGHT("Knight", "Riddare"),
	ADD("Add", "tillsätta"),
	ANY("Any", "Några"),
	
	// Capture Options
	REPLACE("Replace", "Ersätta"),
	HOP("Hop", "Hopp"),
	FLANK("Flank", "Flank"),
	NEIGHBOR("Neighbor", "Granne"),
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
