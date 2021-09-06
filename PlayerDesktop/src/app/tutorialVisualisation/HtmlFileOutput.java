package app.tutorialVisualisation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import game.Game;
import manager.Referee;
import metadata.ai.heuristics.HeuristicUtil;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import other.context.Context;

public class HtmlFileOutput
{

	//-------------------------------------------------------------------------
	
	final public static String htmlHeader = "<!DOCTYPE html>\n" + 
			"<html lang=\"en\">\n" + 
			"\n" + 
			"<head>\n" + 
			"  <meta name=\"description\" content=\"Ludii Auto-Generated Instructions\" />\n" + 
			"  <meta charset=\"utf-8\">\n" + 
			"  <title>Ludii Auto-Generated Instructions</title>\n" + 
			"  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" + 
			"  <meta name=\"author\" content=\"\">\n" + 
			"  <link rel=\"stylesheet\" href=\"css/style.css\">\n" + 
			"</head>\n" + 
			"\n" + 
			"<body>";
	
	final public static String htmlFooter = "</body>\n" + 
			"</html>";
	
	//-------------------------------------------------------------------------
	
	/**
	 * Output toEnglish of the game description.
	 */
	public static String htmlEnglishRules(final Game game)
	{
		String outputString = "<h1>Game Rules:</h1>";
		outputString += "<p><pre>" + game.toEnglish(game).replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "\n</pre></p>";
		return outputString;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Output board setup.
	 */
	public static String htmlBoardSetup()
	{
		String outputString = "<h1>Board Setup:</h1>";
		outputString += "<img src=\"screenshot/Game_Setup.png\" />\n<br><br>";
		return outputString;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Output strategy/heuristics for this game based on metadata (if present).
	 */
	public static String htmlEnglishHeuristics(final Context context)
	{
		String outputString = "";
		final Game game = context.game();
		
		final metadata.ai.Ai aiMetadata = game.metadata().ai();
		if (aiMetadata != null && aiMetadata.heuristics() != null)
		{
			// Record the heuristic strings that are applicable to each player.
			final List<List<String>> allHeuristicStringsPerPlayer = new ArrayList<>();
			final Set<String> allHeuristicStrings = new HashSet<>();
			allHeuristicStringsPerPlayer.add(new ArrayList<>());
			final Heuristics heuristicValueFunction = HeuristicUtil.normaliseHeuristic(Heuristics.copy(aiMetadata.heuristics()));
			outputString += "<h1>Game Heuristics:</h1>";
			for (int i = 1; i <= game.players().count(); i++)
    		{
				allHeuristicStringsPerPlayer.add(new ArrayList<>());
				for (final HeuristicTerm heuristic : heuristicValueFunction.heuristicTerms())
				{
					heuristic.init(game);
					heuristic.simplify();
					final String heuristicEnglishString = heuristic.toEnglishString(context, i);
					if (heuristicEnglishString.length() > 0)
					{
    					String finalHeuristicString = "<b>" + ValueUtils.splitCamelCase(heuristic.getClass().getSimpleName()) + "</b>\n";
    					finalHeuristicString += "<i>" + heuristic.description() + "</i>\n";	
    					finalHeuristicString += heuristicEnglishString + "\n\n";
    					allHeuristicStringsPerPlayer.get(i).add(finalHeuristicString);
    					allHeuristicStrings.add(finalHeuristicString);
					}
				}
    		}
			
			// Merge heuristic strings that apply to all players
			for (final String heuristicString : allHeuristicStrings)
			{
				boolean validForAllPlayers = true;
				for (int i = 1; i <= game.players().count(); i++)
        		{
					final List<String> playerValidHeuristics = allHeuristicStringsPerPlayer.get(i);
					if (!playerValidHeuristics.contains(heuristicString))
					{
						validForAllPlayers = false;
						break;
					}
				}
				
				if (validForAllPlayers)
				{
					allHeuristicStringsPerPlayer.get(0).add(heuristicString);
					for (int i = 1; i <= game.players().count(); i++)
            		{
						final List<String> playerValidHeuristics = allHeuristicStringsPerPlayer.get(i);
						playerValidHeuristics.remove(playerValidHeuristics.indexOf(heuristicString));
            		}
				}
			}
			
			// Write the merged heuristic strings
			for (int i = 0; i < allHeuristicStringsPerPlayer.size(); i++)
    		{
				if (allHeuristicStringsPerPlayer.get(i).size() > 0)
				{
					if (i == 0)
						outputString += "<h2>All Players:</h2>\n";
					else
						outputString += "<h2>Player: " + i + "</h2>\n";
					
					outputString += "<p><pre>";
    				
    				for (final String heuristicString : allHeuristicStringsPerPlayer.get(i))
    					outputString += heuristicString;
    				
    				outputString += "</pre></p>";
				}
    		}
		}
		
		return outputString;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Output endings.
	 */
	public static String htmlEndings(final List<String> rankingStrings, final List<MoveCompleteInformation> endingMoveList)
	{
		String outputString = "<br><h1>Game Endings:</h1>";
		for (int i = 0; i < rankingStrings.size(); i++)
		{
			final MoveCompleteInformation moveInformation = endingMoveList.get(i);
			outputString += "<p><pre>" + rankingStrings.get(i) + "</pre></p>";
			outputString += moveInformation.endingDescription() + "\n<br>";
			outputString += "<img src=\"" + moveInformation.screenshotA() + "\" />\n";
			outputString += "<img src=\"" + moveInformation.screenshotB() + "\" />\n";
			outputString += "<img src=\"" + moveInformation.gifLocation() + "\" />\n<br><br>\n";
		}
		return outputString;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Output all Move images/animations.
	 */
	public static String htmlMoves(final Referee ref, final List<MoveCompleteInformation> condensedMoveList)
	{
		String outputString = "<br><h1>Moves:</h1>";
		
		final Set<String> allMovers = new TreeSet<>();
		final Set<String> allComponents = new TreeSet<>();
		final Set<String> allMoveActionDescriptions = new TreeSet<>();
		
		for (final MoveCompleteInformation moveInformation : condensedMoveList)
		{
			allMovers.add(String.valueOf(moveInformation.move().mover()));
			final String moveComponentName = ValueUtils.getComponentNameFromIndex(ref, moveInformation.what());
			allComponents.add(moveComponentName);
			allMoveActionDescriptions.add(moveInformation.move().actionDescriptionStringShort());
		}
		
		final String[] storedTitles = {"", "", ""};
		for (final String moverString : allMovers)
		{
			storedTitles[0] = "<h2>Player: " + moverString + "</h2>\n";
			for (final String componentString : allComponents)
    		{
				storedTitles[1] = "<h3>Piece: " + componentString + "</h3>\n";
				for (final String actionDescriptionString : allMoveActionDescriptions)
        		{
					storedTitles[2] = "<h4>Actions: " + actionDescriptionString + "</h4>\n";
					for (final MoveCompleteInformation moveInformation : condensedMoveList)
            		{
						if 
						(
							String.valueOf(moveInformation.move().mover()).equals(moverString)
							&&
							ValueUtils.getComponentNameFromIndex(ref, moveInformation.what()).equals(componentString)
							&&
							moveInformation.move().actionDescriptionStringShort().equals(actionDescriptionString)
						)
						{
							outputString += String.join("", storedTitles);
							Arrays.fill(storedTitles, "");
							outputString += moveInformation.move().actionDescriptionStringLong(ref.context(), true) + "\n<br>";
							outputString += moveInformation.move().actions().toString() + "\n<br>";
							outputString += moveInformation.englishDescription() + "\n<br>";
							outputString += "<img src=\"" + moveInformation.screenshotA() + "\" />\n";
							outputString += "<img src=\"" + moveInformation.screenshotB() + "\" />\n";
							outputString += "<img src=\"" + moveInformation.gifLocation() + "\" />\n<br><br>\n";
						}
            		}
        		}
    		}
		}
		return outputString;
	}
	
	//-------------------------------------------------------------------------
	
}
