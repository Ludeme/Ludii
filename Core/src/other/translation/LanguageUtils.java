package other.translation;

import java.util.ArrayList;
import java.util.List;

import game.types.board.SiteType;
import game.types.play.RoleType;
import main.Constants;

/**
 * @author Matthew.Stephenson
 *
 */
public class LanguageUtils 
{

	//-------------------------------------------------------------------------
	
	/** 
	 * Splits an item string into its piece name and owner.
	 * @param itemName 
	 * @return "Pawn3" -> ["Pawn","3"]
	 */
	public static String[] SplitPieceName(final String itemName) 
	{
		int index = itemName.length() - 1;
		while (index >= 0)
		{
			if (itemName.charAt(index) < '0' || itemName.charAt(index) > '9')
			{
				index++;
				break;
			}
			index--;
		}
		
		final String pieceName = itemName.substring(0,index);
		String pieceOwner = String.valueOf(Constants.UNDEFINED);
		if (index < itemName.length())
			pieceOwner = itemName.substring(index);
		
		return new String[] {pieceName, pieceOwner};
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param siteText 
	 * @param type 
	 * @return A location in text form.
	 */
	public static String getLocationName(final String siteText, final SiteType type)
	{
		return type.name() + " " + siteText;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param role
	 * @param isNewSentence
	 * @return RoleType in text form.
	 */
	public static String RoleTypeAsText(final RoleType role, final boolean isNewSentence) 
	{
		switch(role) {
		case P1:
		case P2:
		case P3:
		case P4:
		case P5:
		case P6:
		case P7:
		case P8:
		case P9:
		case P10:
		case P11:
		case P12:
		case P13:
		case P14:
		case P15:
		case P16:
			return (isNewSentence ? "Player" : "player") + " " + NumberAsText(role.owner());
		case Mover:
			return (isNewSentence ? "The" : "the") + " moving player";
		case Next:
			return (isNewSentence ? "The" : "the") + " next player";
		case Neutral:
			return (isNewSentence ? "No" : "no") + " player";
		default:
			return role.name();
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param number
	 * @return Integer in text form.
	 */
	public static String NumberAsText(final int number) 
	{
		return NumberAsText(number, null, null);
	}

	/**
	 * @param originalNumber
	 * @param suffixSingular
	 * @param suffixPlural
	 * @return Integer in text form.
	 */
	public static String NumberAsText(final int originalNumber, final String suffixSingular, final String suffixPlural) 
	{
		int number = originalNumber;
		
		if(number < -999 || number > 999)
			throw new IllegalArgumentException("This function is not implemented for numbers at this range yet! [" + number + "]");

		String text = "";
		if(number < 0) {
			text += "minus ";
			number *= -1;
		}

		if(number == 0) {
			text += "zero";
		} else if(number == 1) {
			text += "one";
		} else if(number == 2) {
			text += "two";
		} else if(number == 3) {
			text += "three";
		} else if(number == 4) {
			text += "four";
		} else if(number == 5) {
			text += "five";
		} else if(number == 6) {
			text += "six";
		} else if(number == 7) {
			text += "seven";
		} else if(number == 8) {
			text += "eight";
		} else if(number == 9) {
			text += "nine";
		} else if(number == 10) {
			text += "ten";
		} else if(number == 11) {
			text += "eleven";
		} else if(number == 12) {
			text += "twelve";
		} else if(number == 13) {
			text += "thirteen";
		} else if(number == 15) {
			text += "fifteen";
		} else if(number == 18) {
			text += "eighteen";
		} else if(number > 10 && number < 20) {
			text += NumberAsText(number % 10) + "teen";
		} else if(number == 20) {
			text += "twenty";
		} else if(number == 30) {
			text += "thirty";
		} else if(number == 40) {
			text += "forty";
		} else if(number == 50) {
			text += "fifty";
		} else if(number == 60) {
			text += "sixty";
		} else if(number == 70) {
			text += "seventy";
		} else if(number == 80) {
			text += "eighty";
		} else if(number == 90) {
			text += "ninety";
		} else if(number >= 100) {
			text += NumberAsText(number / 100) + " hundred";
			if(number % 100 > 0)
				text += " and " + NumberAsText(number % 100);
		} else if(number > 20 && number < 100) {
			text += NumberAsText(number / 10 * 10) + "-" + NumberAsText(number % 10);
		} else {
			throw new IllegalArgumentException("Unknown number recognized! [" + number + "]");
		}

		if(number == 1) {
			if(suffixSingular != null)
				text += " " + suffixSingular;
		} else {
			if(suffixPlural != null)
				text += " " + suffixPlural;
		}

		return text;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param index
	 * @return Index in text form.
	 */
	public static String IndexAsText(final int index) 
	{
		if(index < 0 || index > 999)
			throw new IllegalArgumentException("This function is not implemented for indices at this range yet! [" + index + "]");

		String text = "";

		if(index == 0) {
			text += "zeroth";
		} if(index == 1) {
			text += "first";
		} else if(index == 2) {
			text += "second";
		} else if(index == 3) {
			text += "third";
		} else if(index == 4) {
			text += "fourth";
		} else if(index == 5) {
			text += "fifth";
		} else if(index == 6) {
			text += "sixth";
		} else if(index == 7) {
			text += "seventh";
		} else if(index == 8) {
			text += "eighth";
		} else if(index == 9) {
			text += "ninth";
		} else if(index == 10) {
			text += "tenth";
		} else if(index == 11) {
			text += "eleventh";
		} else if(index == 12) {
			text += "twelfth";
		} else if(index >= 13 && index <= 19) {
			text += NumberAsText(index) + "th";
		} else if(index == 20) {
			text += "twentieth";
		} else if(index == 30) {
			text += "thirtieth";
		} else if(index == 40) {
			text += "fortieth";
		} else if(index == 50) {
			text += "fiftieth";
		} else if(index == 60) {
			text += "sixtieth";
		} else if(index == 70) {
			text += "seventieth";
		} else if(index == 80) {
			text += "eightieth";
		} else if(index == 90) {
			text += "ninetieth";
		} else if(index >= 100) {
			final int h = index / 100;
			final int d = index - h * 100;
			if(h > 1)
				text += NumberAsText(h);
			text += " hundred" + (d == 0 ? "th" : "");
			if(d > 0)
				text += " and " + IndexAsText(d);
		} else if(index > 20 && index < 100) {
			text += NumberAsText(index / 10 * 10) + "-" + IndexAsText(index % 10);
		} else {
			throw new IllegalArgumentException("Unknown index recognized! [" + index + "]");
		}

		return text;
	}
	
	//-------------------------------------------------------------------------

//	/**
//	 * @param basisType
//	 * @return Board's BasisType in text form.
//	 */
//	public static String ConvertBoardNameToText(final BasisType basisType) 
//	{
//		if (basisType == null)
//			return "";
//		
//		switch (basisType) {
//		case NoBasis:
//			return "";
//		case Square:
//			return "square";
//		case T33336:
//			return "semi-regular tiling made up of hexagons surrounded by triangles";
//		case T33344:
//			return "semi-regular tiling made up of alternating rows of squares and triangles";
//		case T33434:
//			return "semi-regular tiling made up of squares and pairs of triangles";
//		case T3464:
//			return "rhombitrihexahedral";
//		case T3636:
//			return "semi-regular tiling 3.6.3.6 made up of hexagons with interstitial triangles";
//		case T4612:
//			return "semi-regular tiling made up of squares, hexagons and dodecagons";
//		case T488:
//			return "semi-regular tiling 4.8.8. made up of octagons with interstitial squares";
//		case T31212:
//			return "semi-regular tiling made up of triangles and dodecagons";
//		case T333333_33434:
//			return "tiling 3.3.3.3.3.3,3.3.4.3.4";
//		default:
//			return basisType.name().toLowerCase();
//		}
//	}

	//-------------------------------------------------------------------------

	/**
	 * @param direction
	 * @return Direction in text form.
	 */
	public static String GetDirection(final String direction) 
	{
		switch (direction) {
		case "N":
			return "north";
		case "S":
			return "south";
		case "E":
			return "east";
		case "W":
			return "west";
		case "FL":
			return "forward-left";
		case "FLL":
			return "forward-left-left";
		case "FLLL":
			return "forward-left-left-left";
		case "BL":
			return "backward-left";
		case "BLL":
			return "backward-left-left";
		case "BLLL":
			return "backward-left-left-left";
		case "FR":
			return "forward-right";
		case "FRR":
			return "forward-right-right";
		case "FRRR":
			return "forward-right-right-right";
		case "BR":
			return "backward-right";
		case "BRR":
			return "backward-right-right";
		case "BRRR":
			return "backward-right-right-right";
		default:
			return LanguageUtils.splitCamelCase(direction);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param string
	 * @return the camel case string split into separate words
	 */
	public final static String splitCamelCase(final String string)
	{
		final List<String> splitClassName = new ArrayList<String>();
	    for (final String w : string.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"))
	    	splitClassName.add(w);
	    return String.join(" ", splitClassName).toLowerCase();
	}
	
	//-------------------------------------------------------------------------
	
}