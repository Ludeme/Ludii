package app.utils;

public enum EnglishSwedishTranslations 
{
	// Menu Text
	MYOGTITLE("Make Your Own Game", "Gor Ditt Eget Spel"),  //"Gör Ditt Eget Spel"),
	
	INTRO_A("Ludemes are the elements that make up games.", "Ludemes är beståndsdelar som utgör spel."),
	INTRO_B("Here you can mix and match ludemes to make your own game!", "Här kan du mixa och matcha ludemes och göra ditt eget spel!"),
			
	MAKE_YOUR_GAME("Make Your Game", "Gora Ditt Spel"),
	PLAY_YOUR_GAME("Play Your Game", "Spela Ditt Spel"),

	HOME("Home", "Home"),
	
	ENGLISH("EN", "EN"),	
	SWEDISH("SV", "SV"),
	
	CHOOSEBOARD("Choose a board", "Välj ett spelbräde"),
	DRAGPIECES("Drag pieces onto the board", "Dra pjäser till brädet"),
	MOVEMENT("Piece moves", "Stycke rörelse"),
	CAPTURE("How to Capture", "Hur man fångar"),
	GOAL("How to win", "Hur man vinner"),
	
	// Button Text
	START("Start", "Start"),
	RESET("Reset", "Återställ"),
	PLAY("Play", "Spela"),
	PLAYAGAIN("Play Again", "Spela igen"),
	EDIT("Edit Rules", "Redigera Regler"),
	HUMANVSHUMAN("vs Human", "vs Person"),
	HUMANVSAI("vs AI", "vs AI"),
	PRINT("Print", "Skriva ut"),
	
	// Goal Options
	LINE3("Line of 3", "Rad om 3"),
	LINE4("Line of 4", "Rad om 4"),
	ELIMINATE("Eliminate", "Eliminera"),
	BLOCK("Block", "Blockera"),
	SURROUND("Surround", "Omringa"),
	
	// Move Options
	STEP("Step", "Steg"),
	SLIDE("Slide", "Glida"),
	KNIGHT("Knight", "Riddare"),
	ADD("Add", "Tillsätta"),
	ANY("Anywhere", "Överallt"),
	
	// Capture Options
	REPLACE("Replace", "Ersätta"),
	HOP("Hop", "Hopp"),
	FLANK("Flank", "Flank"),
	NEIGHBOR("Neighbor", "Angränsa"),
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
