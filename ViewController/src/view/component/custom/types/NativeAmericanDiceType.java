package view.component.custom.types;

/**
 * Native American Dice types.
 *
 * @author Matthew.Stephenson
 */
public enum NativeAmericanDiceType
{
	// Patol
	Patol1("PatolDice", "blank on one side, two lines on other"),
	Patol2("Dice2", "blank on one side, three lines on other"),
	
	// Notched
	Notched("NotchedDice", "blank on one side, dots on the other"),
	
	// Set Dilth
	SetDilth("SetDilthDice", "blank on one side, two lines on other near middle"),
	
	// Nebakuthana
	Nebakuthana1("NebakuthanaDice1", "blank on one side, many lines on other"),
	Nebakuthana2("NebakuthanaDice2", "blank on one side, cross on other"),
	Nebakuthana3("NebakuthanaDice3", "blank on one side, diamond on other"),
	Nebakuthana4("NebakuthanaDice4", "line and dots on one side, star pattern of lines on other"),
	
	// Kints
	Kints1("KintsDice1", "blank on one side, zigzag on other"),
	Kints2("KintsDice2", "blank on one side, four lines on other"),
	Kints3("KintsDice3", "blank on one side, two triangles on other"),
	Kints4("KintsDice4", "blank on one side, cross on other"),
	
	// Kolica
	Kolica1("Kolica1", "blank on one side, many lines on other"),
	Kolica2("Kolica2", "blank on one side, cross on other"),
	Kolica3("Kolica3", "blank on one side, two lines on other near middle"),
	Kolica4("Kolica4", "blank on one side, two lines on other near middle");

	//-------------------------------------------------------------------------

	private String englishName;
	private String description;

	//--------------------------------------------------------------------------

	NativeAmericanDiceType(final String englishName, final String description)
	{
		this.englishName = englishName;
		this.description = description;
	}

	//-------------------------------------------------------------------------
	
	public String englishName() 
	{
		return englishName;
	}

	public String description() 
	{
		return description;
	}
	
	//-------------------------------------------------------------------------

}