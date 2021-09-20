package manualGeneration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import game.Game;
import manager.Referee;
import metadata.ai.heuristics.HeuristicUtil;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.HeuristicTerm;
import other.context.Context;
import other.translation.LanguageUtils;

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
			"  <link rel=\"stylesheet\" href=\"../css/style.css\">\n" + 
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
		outputString += "<p><pre>" + formatString(game.toEnglish(game)) + "\n</pre></p>";
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
			// Record the heuristic strings that are applicable to each player. Index 0 applies to all players.
			final List<Map<String, Set<String>>> allHeuristicStringsPerPlayer = new ArrayList<>();

			final Heuristics heuristicValueFunction = HeuristicUtil.normaliseHeuristic(Heuristics.copy(aiMetadata.heuristics()));
			outputString += "<h1>Game Heuristics:</h1>";
			
			// Record heuristic strings/values for each player.
			for (int i = 0; i <= game.players().count(); i++)
    		{
				allHeuristicStringsPerPlayer.add(new HashMap<>());
				for (final HeuristicTerm heuristic : heuristicValueFunction.heuristicTerms())
				{
					heuristic.init(game);
					heuristic.simplify();
					
					String heuristicTitle = "<b>" + LanguageUtils.splitCamelCase(heuristic.getClass().getSimpleName()) + "</b>\n" + "<i>" + heuristic.description() + "</i>";
					final String[] heuristicValuesStrings = heuristic.toEnglishString(context, i).split("\n");
					
					// Get existing list of values for this heuristic.
					Set<String> existingValues = allHeuristicStringsPerPlayer.get(i).get(heuristicTitle);
					if (existingValues == null)
						existingValues = new HashSet<>();
					
					for (String heuristicValueString : heuristicValuesStrings)
						if (heuristicValueString.length() > 0)
							existingValues.add(heuristicValueString);
					
					if (existingValues.size() > 0)
						allHeuristicStringsPerPlayer.get(i).put(heuristicTitle, existingValues);
				}
    		}
			
			// Merge heuristic strings that apply to all players. Go through all the heuristics for player 1 and see if all other players have them.
			final Map<String, Set<String>> player1Heuristics = allHeuristicStringsPerPlayer.get(1);
			for (final Map.Entry<String, Set<String>> entry : player1Heuristics.entrySet())
			{
				String heuristicTitle = entry.getKey();
				
				Set<String> existingValues = allHeuristicStringsPerPlayer.get(0).get(heuristicTitle);
				if (existingValues == null)
					existingValues = new HashSet<>();
				
				for (String heurisitcValue : entry.getValue())
				{
					// Check if this heuristic already exists for all other players heuristics.
					boolean heuristicAcrossAllPlayers = true;
					for (int i = 2; i <= game.players().count(); i++)
					{
						if (!allHeuristicStringsPerPlayer.get(i).containsKey(heuristicTitle))
						{
							heuristicAcrossAllPlayers = false;
							break;
						}
						
						if (!allHeuristicStringsPerPlayer.get(i).get(heuristicTitle).contains(heurisitcValue))
						{
							heuristicAcrossAllPlayers = false;
							break;
						}
					}
					
					// Add this heuristic value to the merged heuristics.
					if (heuristicAcrossAllPlayers)
					{
						existingValues.add(heurisitcValue);
						allHeuristicStringsPerPlayer.get(0).put(heuristicTitle, existingValues);
					}
				}
			}
			
			// Remove any heuristic values that apply to all players from the individual player heuristics
			final Map<String, Set<String>> player0Heuristics = allHeuristicStringsPerPlayer.get(0);
			for (final Map.Entry<String, Set<String>> entry : player0Heuristics.entrySet())
			{
				String heuristicTitle = entry.getKey();
				for (String heuristicValue : entry.getValue())
				{	
					for (int i = 1; i <= game.players().count(); i++)
            		{
						if (allHeuristicStringsPerPlayer.get(i).containsKey(heuristicTitle))
						{
							allHeuristicStringsPerPlayer.get(i).get(heuristicTitle).remove(heuristicValue);
							
							if (allHeuristicStringsPerPlayer.get(i).get(heuristicTitle).size() == 0)
								allHeuristicStringsPerPlayer.get(i).remove(heuristicTitle);
						}
            		}	
				}
			}
			
			// Write the merged heuristic strings
			for (int i = 0; i < allHeuristicStringsPerPlayer.size(); i++)
    		{
				if (allHeuristicStringsPerPlayer.get(i).size() > 0)
				{
					if (i == 0)
						outputString += "<h2>All Players:</h2>";
					else
						outputString += "<h2>Player: " + i + "</h2>";
					
					outputString += "<p><pre>";
    				
    				for (final Map.Entry<String, Set<String>> entry : allHeuristicStringsPerPlayer.get(i).entrySet())
    				{
    					String heuristicStringCombined = entry.getKey() + "\n";
    					for (String heuristicValue : entry.getValue())
    						heuristicStringCombined += heuristicValue + "\n";
    					outputString += heuristicStringCombined + "\n";
    				}

    				outputString += "</pre></p>";
				}
    		}
		}
		
		return outputString + "<br>";
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
			outputString += formatString(moveInformation.endingDescription()) + "\n<br>";
			outputString += "<img src=\"" + moveInformation.screenshotA() + "\" />\n";
			outputString += "<img src=\"" + moveInformation.gifLocation() + "\" />\n";
			outputString += "<img src=\"" + moveInformation.screenshotB() + "\" />\n";
			outputString += "<br><br>\n";
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
		final Set<String> allMoveEnglishDescriptions = new TreeSet<>();
		final Set<String> allMoveActionDescriptions = new TreeSet<>();
		
		for (final MoveCompleteInformation moveInformation : condensedMoveList)
		{
			allMovers.add(String.valueOf(moveInformation.move().mover()));
			allComponents.add(moveInformation.pieceName());
			allMoveEnglishDescriptions.add(moveInformation.englishDescription());
			allMoveActionDescriptions.add(moveInformation.move().actionDescriptionStringShort());
		}
		
		final boolean splitMovers = MoveComparison.isCompareMover();
		final boolean splitPieces = MoveComparison.isComparePieceName();
		final boolean splitEnglishDescription = MoveComparison.isCompareEnglishDescription();
		final boolean splitActionDescriptions = MoveComparison.isCompareActions();
		
		if (!splitMovers)
		{
			allMovers.clear();
			allMovers.add("");
		}
		if (!splitPieces)
		{
			allComponents.clear();
			allComponents.add("");
		}
		if (!splitEnglishDescription)
		{
			allMoveEnglishDescriptions.clear();
			allMoveEnglishDescriptions.add("");
		}
		if (!splitActionDescriptions)
		{
			allMoveActionDescriptions.clear();
			allMoveActionDescriptions.add("");
		}
		
		final String[] storedTitles = {"", "", "", ""};
		for (final String moverString : allMovers)
		{
			storedTitles[0] = splitMovers ? "<h2>Player: " + moverString + "</h2>\n" : "";
			for (final String componentString : allComponents)
    		{
				storedTitles[1] = splitPieces ? "<h3>Piece: " + componentString + "</h3>\n" : "";
				for (final String moveEnglishString : allMoveEnglishDescriptions)
        		{
					storedTitles[2] = splitEnglishDescription ? "<h4>Move: " + formatString(moveEnglishString) + "</h4>\n" : "";
					for (final String actionDescriptionString : allMoveActionDescriptions)
            		{
						//storedTitles[3] = splitActionDescriptions ? "<h5>Actions: " + actionDescriptionString + "</h5>\n" : "";
						for (final MoveCompleteInformation moveInformation : condensedMoveList)
	            		{
							if 
							(
								(String.valueOf(moveInformation.move().mover()).equals(moverString) || !splitMovers)
								&&
								(moveInformation.pieceName().equals(componentString) || !splitPieces)
								&&
								(moveInformation.englishDescription().equals(moveEnglishString) || !splitEnglishDescription)
								&&
								(moveInformation.move().actionDescriptionStringShort().equals(actionDescriptionString) || !splitActionDescriptions)
							)
							{
								outputString += String.join("", storedTitles);
								Arrays.fill(storedTitles, "");
								//outputString += moveInformation.move().actionDescriptionStringLong(ref.context(), true) + "\n<br>";
								//outputString += moveInformation.move().actions().toString() + "\n<br>";
								//outputString += moveInformation.move().actionDescriptionStringShort() + "\n<br>";
								outputString += "<img src=\"" + moveInformation.screenshotA() + "\" />\n";
								outputString += "<img src=\"" + moveInformation.gifLocation() + "\" />\n";
								outputString += "<img src=\"" + moveInformation.screenshotB() + "\" />\n";
								outputString += "<br><br>\n";
							}
	            		}
            		}
        		}
    		}
		}
		return outputString;
	}
	
	//-------------------------------------------------------------------------
	
	private static String formatString(String s)
	{
		String newString = s;
		
		if (s.length() <= 1)
			return s.toUpperCase();
		
		// Make sure string starts with Capital
		newString = newString.substring(0, 1).toUpperCase() + newString.substring(1);
		
		// Remove any double spaces
		// newString = s.trim().replaceAll(" +", " ");
		
		// Remove any problematic html Characters
		newString = newString.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		
		return newString;
	}
	
}
