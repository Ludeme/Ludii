package utils.bgg;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

public class Recommender
{
	private final static DecimalFormat df3 = new DecimalFormat("#.###");
	
	//-------------------------------------------------------------------------

	/**
	 * Find game by name, using date if necessary to disambiguate.
	 */
	public static BggGame findGame(final BggData data, final String gameName, final String date, final boolean pickMostRated, final boolean skipNullGames)
	{
		BggGame game = null;
		
		List<BggGame> candidates = data.gamesByName().get(gameName);
		
		// If no name found, try searching for Id instead.
		if (candidates == null)
		{
			try
			{
				final BggGame gameById = data.gamesByBggId().get(Integer.valueOf(Integer.parseInt(gameName)));
				if(gameById != null)
				{
					candidates = new ArrayList<BggGame>();
					candidates.add(gameById);
				}
			}
			catch (final NumberFormatException E)
			{
				// name was not a number....
			}
		}

		if (candidates == null)
		{
			if(!skipNullGames)
			{
				JOptionPane.showMessageDialog
				(
					null,
					"Couldn't find game with name '" + gameName + "'.",
					"Failed to Find Game",
					JOptionPane.PLAIN_MESSAGE, 
					null
				);
			}
			return null;
		}
		
		if (candidates.size() == 1)
		{
			// Immediate match on name
			game = candidates.get(0);
		}
		else
		{
			// Match game by date
			for (final BggGame gm : candidates)
				if (gm.date().equalsIgnoreCase(date))
				{
					game = gm;
					break;
				}
		}
		
		if (game == null)
		{
			if (pickMostRated)
			{
				BggGame mostRatedCandidate = null;
				int mostRatings = -1;
				for (final BggGame gm : candidates)
				{
					if (gm.ratings().size() > mostRatings)
					{
						mostRatings = gm.ratings().size();
						mostRatedCandidate = gm;
					}
				}
				game = mostRatedCandidate;
			}
			else
			{	
				// Failed to find game
				final StringBuilder sb = new StringBuilder();
				sb.append("Couldn't choose game among candidates:\n");
				for (final BggGame gm : candidates)
					sb.append(gm.name() + " (" + gm.date() + ")\n");
				
				System.out.println(sb.toString());	
				JOptionPane.showMessageDialog
				(
					null,
					sb.toString(),
					"Failed to Find Game",
					JOptionPane.PLAIN_MESSAGE, 
					null
				);
				return null;
			}
		}
	
		return game;
	}

	//-------------------------------------------------------------------------

	/**
	 * List top 50 recommendations based on game and year.
	 */
	public static String recommendCBB(final BggData data, final String gameName, final String date)
	{
		final StringBuilder sb = new StringBuilder();
		
		final BggGame game = findGame(data, gameName, date, false, false);
		if (game == null)
			return "";
		
		sb.append("\n" + game.name() + " (" + date + ") has " + game.ratings().size() + " ratings.\n");

		final int ratingsThreshold = 30;
		final int matchesThreshold =  5;
				
		final Map<Integer, Matches> ratingMap = new HashMap<Integer, Matches>();

		for (final Rating gameRating : game.ratings())
		{
			// Check the user who made each rating
			final User user = gameRating.user();
			final double baseScore = gameRating.score() / 10.0;
			
			// Determine a penalty based on number of ratings: the fewer ratings the better
			final double userPenalty = 1; 
			//final double userPenalty = 1 / (double)user.ratings().size();
			//final double userPenalty = 1 / Math.sqrt(user.ratings().size() ); 
			//final double userPenalty = 1 / Math.log10(user.ratings().size() + 1); 
			
			for (final Rating userRating : user.ratings())
			{
				final BggGame otherGame = userRating.game();
				final double otherScore = userRating.score() / 10.0;
			
				Matches matches = ratingMap.get(Integer.valueOf(otherGame.index()));
				if (matches == null)
				{
					matches = new Matches(otherGame);
					ratingMap.put(Integer.valueOf(otherGame.index()), matches);
				}
				matches.add(baseScore * otherScore * userPenalty);
			}
		}
		
		// Divide each matches tally by the total number of ratings for that game
		final List<Matches> result = new ArrayList<Matches>();
		for (final Matches matches : ratingMap.values())
		{
			//matches.normalise();
			if 
			(
				matches.game().ratings().size() < ratingsThreshold
				||
				matches.scores().size() < matchesThreshold
			)
			{
				// Eliminate this game from the possible winners
				matches.setScore(0);
			}
			else
			{
				// Normalise to account for number of ratings
				matches.setScore(matches.score() / Math.sqrt(matches.game().ratings().size()));
				//matches.setScore(matches.score() / (double)matches.game().ratings().size());
			}
			
			if (Database.validGameIds().size() == 0 || Database.validGameIds().contains(Integer.valueOf(matches.game().bggId())))
				result.add(matches);
		}
	
		Collections.sort(result, new Comparator<Matches>() 
		{
			@Override
            public int compare(final Matches a, final Matches b) 
            {
				if (a.score() == b.score())
					return 0;
				
				return (a.score() > b.score()) ? -1 : 1;
            }
        });
		
		for (int n = 0; n < Math.min(50, result.size()); n++)
		{
			final Matches matches = result.get(n);
//			message += String.format("%2d. %s (%s) %.3f / %d\n", 
//								Integer.valueOf(n + 1), 
//								matches.game().name(), matches.game().date(),
//								Double.valueOf(matches.score()), 
//								Integer.valueOf(matches.scores().size()));	
			sb.append
			(
				"" + (n + 1) + ". " +  
				matches.game().name() + " (" + 
				matches.game().date() + ") " +
				df3.format(matches.score()) + " / " + 
				matches.scores().size() + ".\n"
			);	
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * List top 50 recommendations based on game and year.
	 */
	public static String recommendGameByUser(final BggData data, final String gameName, final String date)
	{
//		String message = "";
//		
//		final BggGame game = findGame(data, gameName, date, false, false);
//		if (game == null)
//			return "";
//		
//		message = ("\n" + game.name() + " (" + date + ") has " + game.ratings().size() + " ratings.\n");
//
//		final int ratingsThreshold = 30;
//		final int matchesThreshold =  5;
//				
//		final Map<Integer, Matches> ratingMap = new HashMap<Integer, Matches>();
//
//		for (final Rating gameRating : game.ratings())
//		{
//			// Check the user who made each rating
//			final User user = gameRating.user();
//			final double baseScore = gameRating.score() / 10.0;
//			
//			// Determine a penalty based on number of ratings: the fewer ratings the better
//			final double userPenalty = 1; 
//			//final double userPenalty = 1 / (double)user.ratings().size();
//			//final double userPenalty = 1 / Math.sqrt(user.ratings().size() ); 
//			//final double userPenalty = 1 / Math.log10(user.ratings().size() + 1); 
//			
//			for (final Rating userRating : user.ratings())
//			{
//				final BggGame otherGame = userRating.game();
//				final double otherScore = userRating.score() / 10.0;
//			
//				Matches matches = ratingMap.get(Integer.valueOf(otherGame.index()));
//				if (matches == null)
//				{
//					matches = new Matches(otherGame);
//					ratingMap.put(Integer.valueOf(otherGame.index()), matches);
//				}
//				matches.add(baseScore * otherScore * userPenalty);
//			}
//		}
//		
//		// Divide each matches tally by the total number of ratings for that game
//		final List<Matches> result = new ArrayList<Matches>();
//		for (final Matches matches : ratingMap.values())
//		{
//			//matches.normalise();
//			if 
//			(
//				matches.game().ratings().size() < ratingsThreshold
//				||
//				matches.scores().size() < matchesThreshold
//			)
//			{
//				// Eliminate this game from the possible winners
//				matches.setScore(0);
//			}
//			else
//			{
//				// Normalise to account for number of ratings
//				matches.setScore(matches.score() / Math.sqrt(matches.game().ratings().size()));
//				//matches.setScore(matches.score() / (double)matches.game().ratings().size());
//			}
//			
//			if (Database.validGameIds().size() == 0 || Database.validGameIds().contains(Integer.valueOf(matches.game().bggId())))
//				result.add(matches);
//		}
//	
//		Collections.sort(result, new Comparator<Matches>() 
//		{
//			@Override
//            public int compare(final Matches a, final Matches b) 
//            {
//				if (a.score() == b.score())
//					return 0;
//				
//				return (a.score() > b.score()) ? -1 : 1;
//            }
//        });
//		
//		for (int n = 0; n < Math.min(50, result.size()); n++)
//		{
//			final Matches matches = result.get(n);
//			message += String.format("%2d. %s (%s) %.3f / %d\n", 
//								(n+1), matches.game().name(), matches.game().date(),
//								matches.score(), matches.scores().size());	
//		}
//		
//		return message;
		return "Not implement yet.";
	}
	
	//-------------------------------------------------------------------------

	/**
	 * List top 50 recommendations for a given user.
	 * @param includeOwn Whether to include games rated by the user.
	 */
	public static String recommendFor(final BggData data, final String userName, final boolean includeOwn)
	{
		String messageString = "";
		
		final User userA = data.usersByName().get(userName);		
		if (userA == null)
		{
			return "Couldn't find user '" + userName + "'.";
		}
		messageString += userA.name() + " has " + userA.ratings().size() + " ratings.\n";

		final Map<Integer, Matches> ratingMap = new HashMap<Integer, Matches>();

		for (final Rating ratingA : userA.ratings())
		{
			final BggGame gameA = ratingA.game();
		
			for (final Rating ratingB : gameA.ratings())
			{
				// Check other games rated by user
				final User   userB  = ratingB.user();
				final double scoreB = ratingB.score() / 10.0;
				
				for (final Rating ratingC : userB.ratings())
				{
					final BggGame   gameC  = ratingC.game();
					final double scoreC = ratingC.score() / 10.0;
				
					//if (!includeOwn)
					//	 (user rates this game)
					//		continue;

					Matches matches = ratingMap.get(Integer.valueOf(gameC.index()));
					if (matches == null)
					{
						matches = new Matches(gameC);
						ratingMap.put(Integer.valueOf(gameC.index()), matches);
					}
					matches.add(scoreB * scoreC);
				}
			}
		}
		
		final List<Matches> result = new ArrayList<Matches>();
		for (final Matches matches : ratingMap.values())
			if (Database.validGameIds().size() == 0 || Database.validGameIds().contains(Integer.valueOf(matches.game().bggId())))
				result.add(matches);
	
		Collections.sort(result, new Comparator<Matches>() 
		{
			@Override
            public int compare(final Matches a, final Matches b) 
            {
				if (a.score() == b.score())
					return 0;
				
				return (a.score() > b.score()) ? -1 : 1;
            }
        });
		
		for (int n = 0; n < Math.min(20, result.size()); n++)
		{
			final Matches matches = result.get(n);
			messageString += ("Match: " + matches.score() + " (" + matches.scores().size() + ") " + matches.game().name() + "\n");
		}
		
		return messageString;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Estimated match between the two users.
	 */
	public static double userMatch(final BggData data, final User userA, final User userB)
	{
		// Accumulate the number of games rated by both players and the difference in ratings		
		double tally = 0;
		int count = 0;

		final User minUser = (userA.ratings().size() < userB.ratings().size()) ? userA : userB;
		final User maxUser = (userA.ratings().size() < userB.ratings().size()) ? userB : userA;
		
		for (final Rating ratingMin : minUser.ratings())
		{
			final int gameIndexMin = ratingMin.game().index();
			double score = 0;  // unless proven otherwise
				
			for (final Rating ratingMax : maxUser.ratings())
				if (ratingMax.game().index() == gameIndexMin)
				{
					// Update the game match with the actual value
					score = 1 - Math.abs(ratingMin.score() - ratingMax.score()) / 10.0;
					count++;
					break;
				}
			tally += score;
		}
		
		if (count == 0)
		{
			System.out.println("** No shared rating between users.");
			return 0;
		}
		
		return tally / minUser.ratings().size();
	}

	//-------------------------------------------------------------------------

	/**
	 * List top 100 users who gave similar scores to similar games.
	 */
	public static String findMatchingUsers(final BggData data, final String userName)
	{
		String messageString = "";
		
		final User user = data.usersByName().get(userName);		
		if (user == null)
		{
			return "Couldn't find user '" + userName + "'.";
		}
		messageString += (user.name() + " has " + user.ratings().size() + " ratings.\n");

		// Find other users who've scored at least one game this user has scored
		final List<User> others = new ArrayList<User>();
		final Map<String, User> othersMap = new HashMap<String, User>();
		
		for (final Rating rating : user.ratings())
		{
			final BggGame game = rating.game();
			for (final Rating otherRating : game.ratings())
				othersMap.put(otherRating.user().name(), otherRating.user());
		}
		
		for (final User other : othersMap.values())
			others.add(other);
		
		messageString += (others.size() + " users have scored at least one game that " + userName + " has scored.\n");
		
		// Determine scores for overlapping users
		for (final User other : others)
		{
			// Find match for each game scored by user
			double tally = 0;
			for (final Rating userRating : user.ratings())
			{
				double score = 0;
				for (final Rating otherRating : other.ratings())
				{
					if (userRating.game().index() == otherRating.game().index())
					{
						// Match!
						score = 1 - Math.abs(userRating.score() - otherRating.score()) / 10.0;
						break;
					}
				}
				tally += score;
			}
			tally /= user.ratings().size();
			other.setMatch(tally);
//			other.setMatch(userMatch(data, user, other));
		}
		
		Collections.sort(others, new Comparator<User>() 
		{
			@Override
            public int compare(final User a, final User b) 
            {
				if (a.match() == b.match())
					return 0;
				
				return (a.match() > b.match()) ? -1 : 1;
            }
        });
		
		for (int n = 0; n < Math.min(100, others.size()); n++)
		{
			final User other = others.get(n);
			messageString += ((n + 1) + ". " + other.name() + ", " + other.ratings().size() + " ratings, match=" + other.match() + ".\n");
		}
		
		return messageString;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * List top 50 recommendations based on game and year (binary version).
	 */
	public static String binaryRecommendFor(final BggData data, final String gameName, final String date)
	{
		String messageString = "";
		
		final BggGame game = findGame(data, gameName, date, false, false);
		if (game == null)
			return "";
			
		messageString = ("\n" + game.name() + " (" + date + ") has " + game.ratings().size() + " ratings.\n");

		int threshold = 10;
		if (game.ratings().size() > 100)
			threshold = 20;
		if (game.ratings().size() > 1000)
			threshold = 30;
				
		final Map<Integer, Integer> numberOfRecommendsMap = new HashMap<Integer, Integer>();
		final Map<Integer, Integer> numberOfMatchesMap = new HashMap<Integer, Integer>();

		for (final Rating gameRating : game.ratings())
		{
			// Check other games rated by this user
			final User   user  = gameRating.user();
			final boolean wouldrecommend = gameRating.score() >= 7.0;
			
			// only look at the rating of users who would recommend this game.
			if (wouldrecommend)
			{
				for (final Rating userRating : user.ratings())
				{
					final BggGame   otherGame  = userRating.game();
					final boolean wouldrecommendOther = userRating.score() >= 7.0;
					
					final int gameIndex = otherGame.index();
					
					int newScore = 1;
					if (numberOfMatchesMap.containsKey(Integer.valueOf(gameIndex)))
						newScore = numberOfMatchesMap.get(Integer.valueOf(gameIndex)).intValue()+1;
					numberOfMatchesMap.put(Integer.valueOf(gameIndex), Integer.valueOf(newScore));
					
					if (wouldrecommendOther)
					{
						newScore = 1;
						if (numberOfRecommendsMap.containsKey(Integer.valueOf(gameIndex)))
							newScore = numberOfRecommendsMap.get(Integer.valueOf(gameIndex)).intValue()+1;
						numberOfRecommendsMap.put(Integer.valueOf(gameIndex), Integer.valueOf(newScore));
					}
				}
			}
		}
		
		// Divide each matches tally by the total number of ratings for that game
		final List<Matches> result = new ArrayList<Matches>();
		for (final Integer gameId : numberOfRecommendsMap.keySet())
		{
			if (numberOfRecommendsMap.get(gameId).intValue() > threshold)
			{
				final Matches match = new Matches(data.games().get(gameId.intValue()));
				match.setNumberMatches(numberOfRecommendsMap.get(gameId).intValue());
				match.setScore(Double.valueOf(numberOfRecommendsMap.get(gameId).intValue()).doubleValue() / Double.valueOf(numberOfMatchesMap.get(gameId).intValue()).doubleValue());
				if (Database.validGameIds().size() == 0 || Database.validGameIds().contains(Integer.valueOf(match.game().bggId())))
					result.add(match);
			}
		}

		Collections.sort(result, new Comparator<Matches>() 
		{
			@Override
            public int compare(final Matches a, final Matches b) 
            {
				if (a.score() == b.score())
					return 0;
				
				return (a.score() > b.score()) ? -1 : 1;
            }
        });
		
		for (int n = 0; n < Math.min(50, result.size()); n++)
		{
			final Matches matches = result.get(n);
			messageString += ((n + 1) + ". Match: " + matches.score() + " (" + matches.getNumberMatches() + ") " + matches.game().name() + "\n");
		}
		
		return messageString;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * List top 50 recommendations based on game and year (rating similarity version).
	 */
	public static String ratingSimilarityRecommendFor(final BggData data, final String gameName, final String date)
	{
		String messageString = "";
		
		final BggGame game = findGame(data, gameName, date, false, false);
		if (game == null)
			return "";

		messageString = ("\n" + game.name() + " (" + date + ") has " + game.ratings().size() + " ratings.\n");

		int threshold = 0;
		if (game.ratings().size() > 50)
			threshold = 10;
		if (game.ratings().size() > 100)
			threshold = 20;
		if (game.ratings().size() > 1000)
			threshold = 30;
				
		final Map<Integer, Integer> scoreSimilarityMap = new HashMap<Integer, Integer>();
		final Map<Integer, Integer> numberOfMatchesMap = new HashMap<Integer, Integer>();

		for (final Rating gameRating : game.ratings())
		{
			// Check other games rated by this user
			final User   user  = gameRating.user();
			final int gameScore = gameRating.score();
			
			for (final Rating userRating : user.ratings())
			{
				final BggGame   otherGame  = userRating.game();
				final int otherGameScore = userRating.score();
				final int gameIndex = otherGame.index();
				
				final int scoreSimilarity = 10 - Math.abs(gameScore - otherGameScore);
				
				int newTotal = 1;
				if (numberOfMatchesMap.containsKey(Integer.valueOf(gameIndex)))
					newTotal = numberOfMatchesMap.get(Integer.valueOf(gameIndex)).intValue()+1;
				numberOfMatchesMap.put(Integer.valueOf(gameIndex), Integer.valueOf(newTotal));
				
				int newScore = scoreSimilarity;
				if (scoreSimilarityMap.containsKey(Integer.valueOf(gameIndex)))
					newScore = scoreSimilarityMap.get(Integer.valueOf(gameIndex)).intValue()+newScore;
				scoreSimilarityMap.put(Integer.valueOf(gameIndex), Integer.valueOf(newScore));
			}
		}
		
		// Divide each matches tally by the total number of ratings for that game
		final List<Matches> result = new ArrayList<Matches>();
		for (final Integer gameId : numberOfMatchesMap.keySet())
		{
			if (numberOfMatchesMap.get(gameId).intValue() > threshold)
			{
				final Matches match = new Matches(data.games().get(gameId.intValue()));
				match.setNumberMatches(numberOfMatchesMap.get(gameId).intValue());
				match.setScore(Double.valueOf(scoreSimilarityMap.get(gameId).intValue()).doubleValue() / Double.valueOf(numberOfMatchesMap.get(gameId).intValue()).doubleValue());
				if (Database.validGameIds().size() == 0 || Database.validGameIds().contains(Integer.valueOf(match.game().bggId())))
					result.add(match);
			}
		}

		Collections.sort(result, new Comparator<Matches>() 
		{
			@Override
            public int compare(final Matches a, final Matches b) 
            {
				if (a.score() == b.score())
					return 0;
				
				return (a.score() > b.score()) ? -1 : 1;
            }
        });
		
		for (int n = 0; n < Math.min(50, result.size()); n++)
		{
			final Matches matches = result.get(n);
			messageString += ((n + 1) + ". Match: " + matches.score() + " (" + matches.getNumberMatches() + ") " + matches.game().name() + "\n");
			System.out.print(matches.game().bggId() + ", ");
		}
		System.out.println("");
		
		return messageString;
	}
	
}
