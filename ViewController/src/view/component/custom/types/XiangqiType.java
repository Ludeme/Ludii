package view.component.custom.types;

/**
 * Xiangqi types.
 *
 * @author Matthew.Stephenson
 */
public enum XiangqiType
{
	KING("周", "Zhou", "King"),
	WHITEGENERAL("秦", "Qin Jiang", "General White"),
	REDGENERAL("楚", "Chu Jiang", "General Red"),
	ORANGEGENERAL("韓", "Han Jiang", "General Orange"),
	BLUEGENERAL("齊", "Qi Jiang", "General Blue"),
	GREENGENERAL("魏", "Wei Jiang", "General Green"),
	BLACKGENERAL("趙", "Yan Jiang", "General Grey"),
	PURPLEGENERAL("燕", "Zhao Jiang", "General Magenta"),
	DEPUTYGENERAL("偏", "Pian", "Deputy General"),
	OFFICER("裨", "Bai", "Officer"),
	DIPLOMAT("行人", "Xing ren", "Diplomat"),
	CATAPULT("砲", "Pao", "Catapult"),
	ARCHER("弓", "Gong", "Archer"),
	CROSSBOW("弩", "Nu", "Crossbow"),
	KNIFE("刀", "Dao", "Knife"),
	BROADSWORD("劍", "Jian", "Broadsword"),
	KNIGHT("騎", "Qi", "Knight"),
	FIRE("火", "Huo", "Fire"),
	FLAG("旗", "Qi", "Flag"),
	OCEAN("海", "Hai", "Ocean"),
	MOUNTAIN("山", "Shan", "Mountain"),
	CITY("城", "Cheng", "City");

	//-------------------------------------------------------------------------

	private String kanji;
	private String romaji;
	private String englishName;

	//--------------------------------------------------------------------------

	XiangqiType(final String kanji, final String romaji, final String englishName)
	{
		this.kanji = kanji;
		this.romaji = romaji;
		this.englishName = englishName;
	}
	
	//-------------------------------------------------------------------------

    public String kanji()
    {
        return kanji;
    }

    public String romaji()
    {
        return romaji;
    }

    public String englishName()
    {
    	return englishName;
    }
}