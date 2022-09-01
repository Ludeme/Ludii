package utils.bgg;

/**
 * Rating of a game by a BGG user.
 * @author cambolbro
 */
public class Rating
{
	private final BggGame game;
	private final User    user;
	private final byte    score;
	//private final String stat;
	//private final String time;
	
	public Rating(final BggGame game, final User user, final String details)
	{
		this.game  = game;
		this.user  = user;
		this.score = extractScore(details);
		//this.stat  = extractStat(details);
		//this.time  = extractTime(details);
	}
	
	//-------------------------------------------------------------------------
	
	public BggGame game()
	{
		return game;
	}
	
	public User user()
	{
		return user;
	}
	
	public int score()
	{
		return score;
	}
	
//	public String stat()
//	{
//		return stat;
//	}
//	
//	public String time()
//	{
//		return time;
//	}

	//-------------------------------------------------------------------------

	@SuppressWarnings("static-method")
	byte extractScore(final String details)
	{
		final int c = details.indexOf("'score':");
		if (c < 0)
		{
			System.out.println("** Failed to find score in: " + details);
			return -1;  //"";
		}
		
		int cc = c + 1;
		while (cc < details.length() && details.charAt(cc) != ',')
			cc++;
		
		if (cc >= details.length())
		{
			System.out.println("** Failed to find closing ',' for score in: " + details);
			return -1;  //"";
		}
		
		final String str = details.substring(c + 9, cc).trim();
		
		double value = -1;
		try { value = Double.parseDouble(str); } 
		catch (NumberFormatException e) 
		{ 
     		try { value = Integer.parseInt(str); } 
     		catch (NumberFormatException f) { /** */ }
        }
		
		//System.out.println(details);
		//System.out.println(value);
		
		return (byte)(value + 0.5);  //str;
	}
	
//	String extractStat(final String details)
//	{
//		// ...
//		return details;  //null;
//	}
//	
//
//	String extractTime(final String details)
//	{
//		// ...
//		return details;  //null;
//	}
	
	//-------------------------------------------------------------------------

}
