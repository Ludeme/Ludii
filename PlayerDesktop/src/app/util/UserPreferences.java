package app.util;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import app.DesktopApp;
import app.PlayerApp;
import app.move.MoveFormat;
import app.utils.AnimationVisualsType;
import app.utils.PuzzleSelectionType;
import main.Constants;
import manager.ai.AIDetails;
import utils.AIRegistry;

/**
 * The values of various options and settings that are to be retained throughout multiple loadings of the application.
 * 
 * @author Matthew.Stephenson
 */
public class UserPreferences
{
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Save all relevant variables in an external preferences file. 
	 * Only called when the app is closed.
	 */
	public static void savePreferences(final PlayerApp app)
	{
		BufferedWriter bw = null;
		try
		{
			app.manager().settingsNetwork().restoreAiPlayers(app.manager());
			final File file = new File("." + File.separator + "ludii_preferences.json");
			if (!file.exists())
				file.createNewFile();

			final JSONObject json = new JSONObject();
			
			// Ludii version number
			json.put("VersionNumber", Constants.LUDEME_VERSION);
			
			// Game Options
			final List<String> gameOptionStrings = 
					app.manager().settingsManager().userSelections().selectedOptionStrings();
			final JSONArray jsonArray = new JSONArray(gameOptionStrings);
			json.put("OptionStrings", jsonArray);
			json.put("SelectedRuleset", app.manager().settingsManager().userSelections().ruleset());
			
			// If loaded from memory
			json.put("LoadedFromMemory", app.settingsPlayer().loadedFromMemory());
			
			// lud filename for the last loaded game (used for checking last modified time on external lud files).
			json.put("savedLudName", app.manager().savedLudName());

			// Store the last time the external lud file was modified, if not loaded from memory.
			if (!app.settingsPlayer().loadedFromMemory())
			{
				final Path path = Paths.get(app.manager().savedLudName());
				FileTime fileTime = null;
				try
				{
					fileTime = Files.getLastModifiedTime(path);
					json.put("SavedLudLastModifiedTime", fileTime.toString());
				}
				catch (final IOException e)
				{
					System.err.println("Cannot get the last modified time - " + e);
				}
			}

			// Settings Desktop
			json.put("FrameMaximizedBoth", DesktopApp.frame().getExtendedState() == Frame.MAXIMIZED_BOTH);
			
			// Settings Manager/Network
			json.put("networkPolling", app.manager().settingsNetwork().longerNetworkPolling());
			json.put("networkRefresh", app.manager().settingsNetwork().noNetworkRefresh());
			json.put("TickLength", app.manager().settingsManager().tickLength());
			json.put("alwaysAutoPass", app.manager().settingsManager().alwaysAutoPass());
			
			// Settings Player
			json.put("moveFormat", app.settingsPlayer().moveFormat().name());
			json.put("CursorTooltip", app.settingsPlayer().cursorTooltipDev());
			json.put("tabFontSize", app.settingsPlayer().tabFontSize());
			json.put("editorFontSize", app.settingsPlayer().editorFontSize());
			json.put("editorParseText", app.settingsPlayer().isEditorParseText());
			json.put("testLudeme1", app.settingsPlayer().testLudeme1());
			json.put("testLudeme2", app.settingsPlayer().testLudeme2());
			json.put("testLudeme3", app.settingsPlayer().testLudeme3());
			json.put("testLudeme4", app.settingsPlayer().testLudeme4());
			json.put("showZoomBox", app.settingsPlayer().showZoomBox());
			json.put("AnimationVisualsType", app.settingsPlayer().animationType().name());
			json.put("SwapRule", app.settingsPlayer().swapRule());
			json.put("NoRepetition", app.settingsPlayer().noRepetition());
			json.put("NoRepetitionWithinTurn", app.settingsPlayer().noRepetitionWithinTurn());
			json.put("SaveHeuristics", app.settingsPlayer().saveHeuristics());
			json.put("ShowLastMove", app.settingsPlayer().showLastMove());
			json.put("ShowRepetitions", app.manager().settingsManager().showRepetitions());
			json.put("ShowEndingMove", app.settingsPlayer().showEndingMove());
			json.put("ShowAIDistribution", app.settingsPlayer().showAIDistribution());
			json.put("PuzzleValueSelection", app.settingsPlayer().puzzleDialogOption().name());
			json.put("IllegalMoves", app.settingsPlayer().illegalMovesValid());
			json.put("moveSoundEffect", app.settingsPlayer().isMoveSoundEffect());
			json.put("saveTrialAfterMove", app.settingsPlayer().saveTrialAfterMove());
			json.put("ShowBoard", app.settingsPlayer().showBoard());
			json.put("ShowPieces", app.settingsPlayer().showPieces());
			json.put("ShowGraph", app.settingsPlayer().showGraph());
			json.put("ShowConnections", app.settingsPlayer().showConnections());
			json.put("ShowAxes", app.settingsPlayer().showAxes());
			json.put("HideAiMoves", app.settingsPlayer().hideAiMoves());
			json.put("devMode",app.settingsPlayer().devMode());
			json.put("editorAutocomplete", app.settingsPlayer().editorAutocomplete());
			json.put("MoveCoord", app.settingsPlayer().isMoveCoord());
			json.put("PhaseTitle", app.settingsPlayer().showPhaseInTitle());
			
			// Settings VC
			json.put("FlatBoard", app.bridge().settingsVC().flatBoard());
			json.put("ShowCellIndices", app.bridge().settingsVC().showCellIndices());
			json.put("ShowEdgeIndices", app.bridge().settingsVC().showEdgeIndices());
			json.put("ShowFaceIndices", app.bridge().settingsVC().showVertexIndices());
			json.put("ShowContainerIndices", app.bridge().settingsVC().showContainerIndices());
			json.put("ShowVertexCoordinates", app.bridge().settingsVC().showCellCoordinates());
			json.put("ShowEdgeCoordinates", app.bridge().settingsVC().showEdgeCoordinates());
			json.put("ShowFaceCoordinates", app.bridge().settingsVC().showVertexCoordinates());
			json.put("ShowIndices", app.bridge().settingsVC().showIndices());
			json.put("ShowCoordinates", app.bridge().settingsVC().showCoordinates());
			json.put("drawBottomCells", app.bridge().settingsVC().drawBottomCells());
			json.put("drawCornersCells", app.bridge().settingsVC().drawCornerCells());
			json.put("drawCornerConcaveCells", app.bridge().settingsVC().drawCornerConcaveCells());
			json.put("drawCornerConvexCells", app.bridge().settingsVC().drawCornerConvexCells());
			json.put("drawMajorCells", app.bridge().settingsVC().drawMajorCells());
			json.put("drawMinorCells", app.bridge().settingsVC().drawMinorCells());
			json.put("drawInnerCells", app.bridge().settingsVC().drawInnerCells());
			json.put("drawLeftCells", app.bridge().settingsVC().drawLeftCells());
			json.put("drawOuterCells", app.bridge().settingsVC().drawOuterCells());
			json.put("drawPerimeterCells", app.bridge().settingsVC().drawPerimeterCells());
			json.put("drawRightCells", app.bridge().settingsVC().drawRightCells());
			json.put("drawCenterCells", app.bridge().settingsVC().drawCenterCells());
			json.put("drawTopCells", app.bridge().settingsVC().drawTopCells());
			json.put("drawPhasesCells", app.bridge().settingsVC().drawPhasesCells());
			json.put("drawNeighboursCells", app.bridge().settingsVC().drawNeighboursCells());
			json.put("drawRadialsCells", app.bridge().settingsVC().drawRadialsCells());
			json.put("drawDistanceCells", app.bridge().settingsVC().drawDistanceCells());
			json.put("drawBottomVertices", app.bridge().settingsVC().drawBottomVertices());
			json.put("drawCornersVertices", app.bridge().settingsVC().drawCornerVertices());
			json.put("drawCornerConcaveVertices", app.bridge().settingsVC().drawCornerConcaveVertices());
			json.put("drawCornerConvexVertices", app.bridge().settingsVC().drawCornerConvexVertices());
			json.put("drawMajorVertices", app.bridge().settingsVC().drawMajorVertices());
			json.put("drawMinorVertices", app.bridge().settingsVC().drawMinorVertices());
			json.put("drawInnerVertices", app.bridge().settingsVC().drawInnerVertices());
			json.put("drawLeftVertices", app.bridge().settingsVC().drawLeftVertices());
			json.put("drawOuterVertices", app.bridge().settingsVC().drawOuterVertices());
			json.put("drawPerimeterVertices", app.bridge().settingsVC().drawPerimeterVertices());
			json.put("drawRightVertices", app.bridge().settingsVC().drawRightVertices());
			json.put("drawCenterVertices", app.bridge().settingsVC().drawCenterVertices());
			json.put("drawTopVertices", app.bridge().settingsVC().drawTopVertices());
			json.put("drawPhasesVertices", app.bridge().settingsVC().drawPhasesVertices());
			json.put("drawSideNumberVertices", app.bridge().settingsVC().drawSideVertices().size());
			json.put("drawNeighboursVertices", app.bridge().settingsVC().drawNeighboursVertices());
			json.put("drawRadialsVertices", app.bridge().settingsVC().drawRadialsVertices());
			json.put("drawDistanceVertices", app.bridge().settingsVC().drawDistanceVertices());
			json.put("drawCornerEdges", app.bridge().settingsVC().drawCornerEdges());
			json.put("drawCornerConcaveEdges", app.bridge().settingsVC().drawCornerConcaveEdges());
			json.put("drawCornerConvexEdges", app.bridge().settingsVC().drawCornerConvexEdges());
			json.put("drawMajorEdges", app.bridge().settingsVC().drawMajorEdges());
			json.put("drawMinorEdges", app.bridge().settingsVC().drawMinorEdges());
			json.put("drawBottomEdges", app.bridge().settingsVC().drawBottomEdges());
			json.put("drawInnerEdges", app.bridge().settingsVC().drawInnerEdges());
			json.put("drawLeftEdges", app.bridge().settingsVC().drawLeftEdges());
			json.put("drawOuterEdges", app.bridge().settingsVC().drawOuterEdges());
			json.put("drawPerimeterEdges", app.bridge().settingsVC().drawPerimeterEdges());
			json.put("drawRightEdges", app.bridge().settingsVC().drawRightEdges());
			json.put("drawTopEdges", app.bridge().settingsVC().drawTopEdges());
			json.put("drawCentreEdges", app.bridge().settingsVC().drawCentreEdges());
			json.put("drawPhasesEdges", app.bridge().settingsVC().drawPhasesEdges());
			json.put("drawDistanceEdges", app.bridge().settingsVC().drawDistanceEdges());
			json.put("drawAxialEdges", app.bridge().settingsVC().drawAxialEdges());
			json.put("drawHorizontalEdges", app.bridge().settingsVC().drawHorizontalEdges());
			json.put("drawVerticalEdges", app.bridge().settingsVC().drawVerticalEdges());
			json.put("drawAngledEdges", app.bridge().settingsVC().drawAngledEdges());
			json.put("drawSlashEdges", app.bridge().settingsVC().drawSlashEdges());
			json.put("drawSloshEdges", app.bridge().settingsVC().drawSloshEdges());
			json.put("ShowPossibleMoves", app.bridge().settingsVC().showPossibleMoves());
			json.put("CandidateMoves", app.bridge().settingsVC().showCandidateValues());
			//json.put("abstractPriority", app.bridge().settingsVC().abstractPriority());
			json.put("coordWithOutline", app.bridge().settingsVC().coordWithOutline());
			for (int i = 0; i < app.bridge().settingsVC().trackNames().size(); i++)
				json.put(app.bridge().settingsVC().trackNames().get(i), app.bridge().settingsVC().trackShown().get(i));
			for (int p = 0; p < app.settingsPlayer().recentGames().length; p++)
				json.put("RecentGames_" + p, app.settingsPlayer().recentGames()[p]);
			
			// Settings network
			if (app.manager().settingsNetwork().rememberDetails())
			{
				json.put("LoginUsername", app.manager().settingsNetwork().loginUsername());
				json.put("RememberDetails", app.manager().settingsNetwork().rememberDetails());
			}
			
			// Save frame parameters
			json.put("FrameWidth", DesktopApp.frame().getWidth());
			json.put("FrameHeight", DesktopApp.frame().getHeight());
			json.put("FrameLocX", DesktopApp.frame().getLocation().x);
			json.put("FrameLocY", DesktopApp.frame().getLocation().y);

			// Save the Name and AI preferences
			for (int p = 0; p < app.manager().aiSelected().length; ++p)
			{
				json.put("Names_" + p, app.manager().aiSelected()[p].name());
				json.put("MenuNames_" + p, app.manager().aiSelected()[p].menuItemName());
				if (app.manager().aiSelected()[p].ai() != null)
				{
					json.put("AI_" + p, app.manager().aiSelected()[p].object());
					json.put("SearchTime_" + p, app.manager().aiSelected()[p].thinkTime());
				}
			}
			
			// Save filepaths of filechoosers
			final File selectedJsonFile = DesktopApp.jsonFileChooser().getSelectedFile();
			if (selectedJsonFile != null && selectedJsonFile.exists())
				json.put("LastSelectedJsonFile", selectedJsonFile.getCanonicalPath());
			
			final File selectedJarFile = DesktopApp.jarFileChooser().getSelectedFile();
			if (selectedJarFile != null && selectedJarFile.exists())
				json.put("LastSelectedJarFile", selectedJarFile.getCanonicalPath());
			
			final File selectedAiDefFile = DesktopApp.aiDefFileChooser().getSelectedFile();
			if (selectedAiDefFile != null && selectedAiDefFile.exists())
				json.put("LastSelectedAiDefFile", selectedAiDefFile.getCanonicalPath());
			
			final File selectedGameFile = DesktopApp.gameFileChooser().getSelectedFile();
			if (selectedGameFile != null && selectedGameFile.exists())
				json.put("LastSelectedGameFile", selectedGameFile.getCanonicalPath());
			
			final File selectedSaveGameFile = DesktopApp.saveGameFileChooser().getSelectedFile();
			if (selectedSaveGameFile != null && selectedSaveGameFile.exists())
				json.put("LastSelectedSaveGameFile", selectedSaveGameFile.getCanonicalPath());
			
			final File selectedLoadTrialFile = DesktopApp.loadTrialFileChooser().getSelectedFile();
			if (selectedLoadTrialFile != null && selectedLoadTrialFile.exists())
				json.put("LastSelectedLoadTrialFile", selectedLoadTrialFile.getCanonicalPath());
			
			final File selectedLoadTournamentFile = DesktopApp.loadTournamentFileChooser().getSelectedFile();
			if (selectedLoadTournamentFile != null && selectedLoadTournamentFile.exists())
				json.put("LastSelectedLoadTournamentFile", selectedLoadTournamentFile.getCanonicalPath());

			// Game turn limits
			for (final String gameName : app.manager().settingsManager().turnLimits().keySet())
			{
				json.put("MAXTURN" + gameName, app.manager().settingsManager().turnLimits().get(gameName));
			}
			
			// Game piece families
			for (final String gameName : app.bridge().settingsVC().pieceFamilies().keySet())
			{
				json.put("PIECEFAMILY" + gameName, app.bridge().settingsVC().pieceFamilies().get(gameName));
			}

			final FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			bw.write(json.toString(4));
			
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			System.out.println("Problem while saving preferences.");
		}
		finally
		{
			app.manager().databaseFunctionsPublic().logout(app.manager());
			
			try
			{
				if (bw != null)
					bw.close();
			}
			catch (final Exception ex)
			{
				System.out.println("Error in closing the BufferedWriter" + ex);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Load all relevant variables from an external preferences file (if exists).
	 * Only called when the app is opened. 
	 */
	public static void loadPreferences(final PlayerApp app)
	{
		app.settingsPlayer().setPreferencesLoaded(false);
		try (final InputStream inputStream = new FileInputStream(new File("." + File.separator + "ludii_preferences.json")))
		{
			final JSONObject json = new JSONObject(new JSONTokener(inputStream));
			
//			if (!(json.optString("VersionNumber").equals(Constants.LUDEME_VERSION)))
//				throw new Exception("Incorrect version number");
		
			// Game Options
			final List<String> listdata = new ArrayList<String>();
			final JSONArray jArray = json.optJSONArray("OptionStrings");
			if (jArray != null) 
			{
				for (int i = 0; i < jArray.length(); i++)
				   listdata.add(jArray.getString(i));
			   
				// ** FIXME: Not thread-safe.
				app.manager().settingsManager().userSelections().setSelectOptionStrings(listdata);
			}
			
			// Game Ruleset
			app.manager().settingsManager().userSelections().setRuleset(json.optInt("SelectedRuleset", app.manager().settingsManager().userSelections().ruleset()));
			
			// If loaded from memory
			app.settingsPlayer().setLoadedFromMemory(json.optBoolean("LoadedFromMemory", app.settingsPlayer().loadedFromMemory()));

			// If not loading from memory, check that the file hasn't been modified since the last time it was loaded.
			// If it has, don't try to load the trial.
			if (!app.settingsPlayer().loadedFromMemory())
			{
				try
				{
					final String fileModifiedTime = json.optString("SavedLudLastModifiedTime");
					final Path path = Paths.get(json.optString("savedLudName", app.manager().savedLudName()));
					FileTime fileTime = null;
					fileTime = Files.getLastModifiedTime(path);
					if (fileModifiedTime.matches(fileTime.toString()))
						DesktopApp.setLoadTrial(true);
					else
						System.out.println("External .lud has been modified since last load.");
				}
				catch (final Exception E)
				{
					System.out.println("Failed to load external .lud from preferences");
				}
			}
			else
			{
				DesktopApp.setLoadTrial(true);
			}
			
			// Settings General
			app.settingsPlayer().setMoveCoord(json.optBoolean("MoveCoord", app.settingsPlayer().isMoveCoord()));
			
			// Settings Manager
			app.settingsPlayer().setShowLastMove
			(
				json.optBoolean("ShowLastMove", app.settingsPlayer().showLastMove())
			);
			app.manager().settingsManager().setShowRepetitions
			(
				json.optBoolean("ShowRepetitions", app.manager().settingsManager().showRepetitions())
			);
			app.settingsPlayer().setShowEndingMove
			(
				json.optBoolean("ShowEndingMove", app.settingsPlayer().showEndingMove())
			);
			app.settingsPlayer().setShowAIDistribution
			(
				json.optBoolean("ShowAIDistribution", app.settingsPlayer().showAIDistribution())
			);
			app.settingsPlayer().setShowBoard(json.optBoolean("ShowBoard", app.settingsPlayer().showBoard()));
			app.settingsPlayer().setShowPieces(json.optBoolean("ShowPieces", app.settingsPlayer().showPieces()));
			app.settingsPlayer().setShowGraph(json.optBoolean("ShowGraph", app.settingsPlayer().showGraph()));
			app.settingsPlayer().setShowConnections(json.optBoolean("ShowConnections", app.settingsPlayer().showConnections()));
			app.settingsPlayer().setShowAxes(json.optBoolean("ShowAxes", app.settingsPlayer().showAxes()));
			app.settingsPlayer().setHideAiMoves
			(
				json.optBoolean("HideAiMoves", app.settingsPlayer().hideAiMoves())
			);
			app.settingsPlayer().setDevMode
			(
				json.optBoolean("devMode", app.settingsPlayer().devMode())
			);
			app.manager().settingsNetwork().setLongerNetworkPolling(json.optBoolean("networkPolling", app.manager().settingsNetwork().longerNetworkPolling()));
			app.manager().settingsNetwork().setNoNetworkRefresh(json.optBoolean("networkRefresh", app.manager().settingsNetwork().noNetworkRefresh()));
			app.settingsPlayer().setEditorAutocomplete(json.optBoolean("editorAutocomplete", app.settingsPlayer().editorAutocomplete()));
			app.manager().settingsManager().setTickLength
			(
				json.optDouble("TickLength", app.manager().settingsManager().tickLength())
			);
			app.manager().settingsManager().setAlwaysAutoPass
			(
				json.optBoolean("alwaysAutoPass", app.manager().settingsManager().alwaysAutoPass())
			);
			app.settingsPlayer().setSwapRule
			(
				json.optBoolean("SwapRule", app.settingsPlayer().swapRule())
			);
			app.settingsPlayer().setNoRepetition
			(
				json.optBoolean("NoRepetition", app.settingsPlayer().noRepetition())
			);
			app.settingsPlayer().setNoRepetitionWithinTurn
			(
				json.optBoolean("NoRepetitionWithinTurn", app.settingsPlayer().noRepetitionWithinTurn())
			);
			app.settingsPlayer().setSaveHeuristics
			(
				json.optBoolean("SaveHeuristics", app.settingsPlayer().saveHeuristics())
			);
			app.settingsPlayer().setPuzzleDialogOption
			(
				PuzzleSelectionType.getPuzzleSelectionType
				(
					json.optString("PuzzleValueSelection", 
					app.settingsPlayer().puzzleDialogOption().name())
				)
			);
			app.settingsPlayer().setIllegalMovesValid(json.optBoolean("IllegalMoves", app.settingsPlayer().illegalMovesValid()));
			app.settingsPlayer().setMoveSoundEffect
			(
				json.optBoolean("moveSoundEffect", app.settingsPlayer().isMoveSoundEffect())
			);
			app.settingsPlayer().setSaveTrialAfterMove
			(
				json.optBoolean("saveTrialAfterMove", app.settingsPlayer().saveTrialAfterMove())
			);
			
			// Settings Desktop
			app.settingsPlayer().setCursorTooltipDev(json.optBoolean("CursorTooltip", app.settingsPlayer().cursorTooltipDev()));			
			app.settingsPlayer().setTabFontSize(json.optInt("tabFontSize", app.settingsPlayer().tabFontSize()));
			app.settingsPlayer().setEditorFontSize(json.optInt("editorFontSize", app.settingsPlayer().editorFontSize()));
			app.settingsPlayer().setEditorParseText(json.optBoolean("editorParseText", app.settingsPlayer().isEditorParseText()));
			app.settingsPlayer().setMoveFormat(MoveFormat.valueOf(json.optString("moveFormat", app.settingsPlayer().moveFormat().name())));
			app.settingsPlayer().setFrameMaximised(json.optBoolean("FrameMaximizedBoth", app.settingsPlayer().frameMaximised()));
			app.settingsPlayer().setTestLudeme1(json.optString("testLudeme1", app.settingsPlayer().testLudeme1()));
			app.settingsPlayer().setTestLudeme2(json.optString("testLudeme2", app.settingsPlayer().testLudeme2()));
			app.settingsPlayer().setTestLudeme3(json.optString("testLudeme3", app.settingsPlayer().testLudeme3()));
			app.settingsPlayer().setTestLudeme4(json.optString("testLudeme4", app.settingsPlayer().testLudeme4()));
			app.settingsPlayer().setShowZoomBox(json.optBoolean("showZoomBox", app.settingsPlayer().showZoomBox()));
			app.settingsPlayer().setAnimationType(AnimationVisualsType.getAnimationVisualsType(json.optString("AnimationVisualsType", app.settingsPlayer().animationType().name())));
			app.settingsPlayer().setShowPhaseInTitle(json.optBoolean("PhaseTitle", app.settingsPlayer().showPhaseInTitle()));
			
			// Settings VC
			app.bridge().settingsVC().setFlatBoard(json.optBoolean("FlatBoard", app.bridge().settingsVC().flatBoard()));
			app.bridge().settingsVC().setShowPossibleMoves(json.optBoolean("ShowPossibleMoves", app.bridge().settingsVC().showPossibleMoves()));
			app.bridge().settingsVC().setShowCellIndices(json.optBoolean("ShowCellIndices", app.bridge().settingsVC().showCellIndices()));
			app.bridge().settingsVC().setShowEdgeIndices(json.optBoolean("ShowEdgeIndices", app.bridge().settingsVC().showEdgeIndices()));
			app.bridge().settingsVC().setShowVertexIndices(json.optBoolean("ShowFaceIndices", app.bridge().settingsVC().showVertexIndices()));
			app.bridge().settingsVC().setShowContainerIndices(json.optBoolean("ShowContainerIndices", app.bridge().settingsVC().showContainerIndices()));
			app.bridge().settingsVC().setShowCellCoordinates(json.optBoolean("ShowVertexCoordinates", app.bridge().settingsVC().showCellCoordinates()));
			app.bridge().settingsVC().setShowEdgeCoordinates(json.optBoolean("ShowEdgeCoordinates", app.bridge().settingsVC().showEdgeCoordinates()));
			app.bridge().settingsVC().setShowVertexCoordinates(json.optBoolean("ShowFaceCoordinates", app.bridge().settingsVC().showVertexCoordinates()));
			app.bridge().settingsVC().setShowIndices(json.optBoolean("ShowIndices", app.bridge().settingsVC().showIndices()));
			app.bridge().settingsVC().setShowCoordinates(json.optBoolean("ShowCoordinates", app.bridge().settingsVC().showCoordinates()));
			app.bridge().settingsVC().setDrawBottomCells(json.optBoolean("drawBottomCells", app.bridge().settingsVC().drawBottomCells()));
			app.bridge().settingsVC().setDrawCornerCells(json.optBoolean("drawCornerCells", app.bridge().settingsVC().drawCornerCells()));
			app.bridge().settingsVC().setDrawCornerConcaveCells(json.optBoolean("drawCornerConcaveCells", app.bridge().settingsVC().drawCornerConcaveCells()));
			app.bridge().settingsVC().setDrawCornerConvexCells(json.optBoolean("drawCornerConvexCells", app.bridge().settingsVC().drawCornerConvexCells()));
			app.bridge().settingsVC().setDrawMajorCells(json.optBoolean("drawMajorCells", app.bridge().settingsVC().drawMajorCells()));
			app.bridge().settingsVC().setDrawMinorCells(json.optBoolean("drawMinorCells", app.bridge().settingsVC().drawMinorCells()));
			app.bridge().settingsVC().setDrawInnerCells(json.optBoolean("drawInnerCells", app.bridge().settingsVC().drawInnerCells()));
			app.bridge().settingsVC().setDrawLeftCells(json.optBoolean("drawLeftCells", app.bridge().settingsVC().drawLeftCells()));
			app.bridge().settingsVC().setDrawOuterCells(json.optBoolean("drawOuterCells", app.bridge().settingsVC().drawOuterCells()));
			app.bridge().settingsVC().setDrawPerimeterCells(json.optBoolean("drawPerimeterCells", app.bridge().settingsVC().drawPerimeterCells()));
			app.bridge().settingsVC().setDrawRightCells(json.optBoolean("drawRightCells", app.bridge().settingsVC().drawRightCells()));
			app.bridge().settingsVC().setDrawTopCells(json.optBoolean("drawTopCells", app.bridge().settingsVC().drawTopCells()));
			app.bridge().settingsVC().setDrawCenterCells(json.optBoolean("drawCenterCells", app.bridge().settingsVC().drawCenterCells()));
			app.bridge().settingsVC().setDrawPhasesCells(json.optBoolean("drawPhasesCells", app.bridge().settingsVC().drawPhasesCells()));
			app.bridge().settingsVC().setDrawNeighboursCells(json.optBoolean("drawNeighboursCells", app.bridge().settingsVC().drawNeighboursCells()));
			app.bridge().settingsVC().setDrawRadialsCells(json.optBoolean("drawRadialsCells", app.bridge().settingsVC().drawRadialsCells()));
			app.bridge().settingsVC().setDrawDistanceCells(json.optBoolean("drawDistanceCells", app.bridge().settingsVC().drawDistanceCells()));
			app.bridge().settingsVC().setDrawBottomVertices(json.optBoolean("drawBottomVertices", app.bridge().settingsVC().drawBottomVertices()));
			app.bridge().settingsVC().setDrawCornerVertices(json.optBoolean("drawCornerVertices", app.bridge().settingsVC().drawCornerVertices()));
			app.bridge().settingsVC().setDrawCornerConcaveVertices(json.optBoolean("drawCornerConcaveVertices", app.bridge().settingsVC().drawCornerConcaveVertices()));
			app.bridge().settingsVC().setDrawCornerConvexVertices(json.optBoolean("drawCornerConvexVertices", app.bridge().settingsVC().drawCornerConvexVertices()));
			app.bridge().settingsVC().setDrawMajorVertices(json.optBoolean("drawMajorVertices", app.bridge().settingsVC().drawMajorVertices()));
			app.bridge().settingsVC().setDrawMinorVertices(json.optBoolean("drawMinorVertices", app.bridge().settingsVC().drawMinorVertices()));
			app.bridge().settingsVC().setDrawInnerVertices(json.optBoolean("drawInnerVertices", app.bridge().settingsVC().drawInnerVertices()));
			app.bridge().settingsVC().setDrawLeftVertices(json.optBoolean("drawLeftVertices", app.bridge().settingsVC().drawLeftVertices()));
			app.bridge().settingsVC().setDrawOuterVertices(json.optBoolean("drawOuterVertices", app.bridge().settingsVC().drawOuterVertices()));
			app.bridge().settingsVC().setDrawPerimeterVertices(json.optBoolean("drawPerimeterVertices", app.bridge().settingsVC().drawPerimeterVertices()));
			app.bridge().settingsVC().setDrawRightVertices(json.optBoolean("drawRightVertices", app.bridge().settingsVC().drawRightVertices()));
			app.bridge().settingsVC().setDrawTopVertices(json.optBoolean("drawTopVertices", app.bridge().settingsVC().drawTopVertices()));
			app.bridge().settingsVC().setDrawCenterVertices(json.optBoolean("drawCenterVertices", app.bridge().settingsVC().drawCenterVertices()));
			app.bridge().settingsVC().setDrawPhasesVertices(json.optBoolean("drawPhasesVertices", app.bridge().settingsVC().drawPhasesVertices()));
			app.bridge().settingsVC().setDrawCornerEdges(json.optBoolean("drawCornerEdges", app.bridge().settingsVC().drawCornerEdges()));
			app.bridge().settingsVC().setDrawCornerConcaveEdges(json.optBoolean("drawCornerConcaveEdges", app.bridge().settingsVC().drawCornerConcaveEdges()));
			app.bridge().settingsVC().setDrawCornerConvexEdges(json.optBoolean("drawCornerConvexEdges", app.bridge().settingsVC().drawCornerConvexEdges()));
			app.bridge().settingsVC().setDrawMajorEdges(json.optBoolean("drawMajorEdges", app.bridge().settingsVC().drawMajorEdges()));
			app.bridge().settingsVC().setDrawMinorEdges(json.optBoolean("drawMinorEdges", app.bridge().settingsVC().drawMinorEdges()));
			app.bridge().settingsVC().setDrawBottomEdges(json.optBoolean("drawBottomEdges", app.bridge().settingsVC().drawBottomEdges()));
			app.bridge().settingsVC().setDrawInnerEdges(json.optBoolean("drawInnerEdges", app.bridge().settingsVC().drawInnerEdges()));
			app.bridge().settingsVC().setDrawLeftEdges(json.optBoolean("drawLeftEdges", app.bridge().settingsVC().drawLeftEdges()));
			app.bridge().settingsVC().setDrawOuterEdges(json.optBoolean("drawOuterEdges", app.bridge().settingsVC().drawOuterEdges()));
			app.bridge().settingsVC().setDrawPerimeterEdges(json.optBoolean("drawPerimeterEdges", app.bridge().settingsVC().drawPerimeterEdges()));
			app.bridge().settingsVC().setDrawRightEdges(json.optBoolean("drawRightEdges", app.bridge().settingsVC().drawRightEdges()));
			app.bridge().settingsVC().setDrawTopEdges(json.optBoolean("drawTopEdges", app.bridge().settingsVC().drawTopEdges()));
			app.bridge().settingsVC().setDrawCentreEdges(json.optBoolean("drawCentreEdges", app.bridge().settingsVC().drawCentreEdges()));
			app.bridge().settingsVC().setDrawPhasesEdges(json.optBoolean("drawPhasesEdges", app.bridge().settingsVC().drawPhasesEdges()));
			app.bridge().settingsVC().setDrawDistanceEdges(json.optBoolean("drawDistanceEdges", app.bridge().settingsVC().drawDistanceEdges()));
			app.bridge().settingsVC().setDrawAxialEdges(json.optBoolean("drawAxialEdges", app.bridge().settingsVC().drawAxialEdges()));
			app.bridge().settingsVC().setDrawHorizontalEdges(json.optBoolean("drawHorizontalEdges", app.bridge().settingsVC().drawHorizontalEdges()));
			app.bridge().settingsVC().setDrawVerticalEdges(json.optBoolean("drawVerticalEdges", app.bridge().settingsVC().drawVerticalEdges()));
			app.bridge().settingsVC().setDrawAngledEdges(json.optBoolean("drawAngledEdges", app.bridge().settingsVC().drawAngledEdges()));
			app.bridge().settingsVC().setDrawSlashEdges(json.optBoolean("drawSlashEdges", app.bridge().settingsVC().drawSlashEdges()));
			app.bridge().settingsVC().setDrawSloshEdges(json.optBoolean("drawSloshEdges", app.bridge().settingsVC().drawSloshEdges()));
			app.bridge().settingsVC().setDrawNeighboursVertices(json.optBoolean("drawNeighboursVertices", app.bridge().settingsVC().drawNeighboursVertices()));
			app.bridge().settingsVC().setDrawRadialsVertices(json.optBoolean("drawRadialsVertices", app.bridge().settingsVC().drawRadialsVertices()));
			app.bridge().settingsVC().setDrawDistanceVertices(json.optBoolean("drawDistanceVertices", app.bridge().settingsVC().drawDistanceVertices()));
			app.bridge().settingsVC().setShowCandidateValues(json.optBoolean("CandidateMoves", app.bridge().settingsVC().showCandidateValues()));
			//app.bridge().settingsVC().setAbstractPriority(json.optBoolean("abstractPriority", app.bridge().settingsVC().abstractPriority()));		
			app.bridge().settingsVC().setCoordWithOutline(json.optBoolean("coordWithOutline", app.bridge().settingsVC().coordWithOutline()));
			
			// Settings Network
			app.manager().settingsNetwork().setLoginUsername(json.optString("LoginUsername", app.manager().settingsNetwork().loginUsername()));
			app.manager().settingsNetwork().setRememberDetails(json.optBoolean("RememberDetails", app.manager().settingsNetwork().rememberDetails()));
			
			// Load last-selected filepaths in filechoosers
			DesktopApp.setLastSelectedJsonPath(json.optString("LastSelectedJsonFile", DesktopApp.lastSelectedJsonPath()));
			DesktopApp.setLastSelectedJarPath(json.optString("LastSelectedJarFile", DesktopApp.lastSelectedJarPath()));
			DesktopApp.setLastSelectedAiDefPath(json.optString("LastSelectedAiDefFile", DesktopApp.lastSelectedAiDefPath()));
			DesktopApp.setLastSelectedGamePath(json.optString("LastSelectedGameFile", DesktopApp.lastSelectedGamePath()));
			DesktopApp.setLastSelectedSaveGamePath(json.optString("LastSelectedSaveGameFile", DesktopApp.lastSelectedSaveGamePath()));
			DesktopApp.setLastSelectedLoadTrialPath(json.optString("LastSelectedLoadTrialFile", DesktopApp.lastSelectedLoadTrialPath()));
			DesktopApp.setLastSelectedLoadTournamentPath(json.optString("LastSelectedLoadTournamentFile", DesktopApp.lastSelectedLoadTournamentPath()));
			
			// Recent games
			for (int p = 0; p < app.settingsPlayer().recentGames().length; p++)
				if (json.has("RecentGames_" + p))
					app.settingsPlayer().recentGames()[p] = json.optString("RecentGames_" + p, app.settingsPlayer().recentGames()[p]);

			// load the frame preferences
			final int frameWidth = json.optInt("FrameWidth", SettingsDesktop.defaultWidth);
			final int frameHeight = json.optInt("FrameHeight", SettingsDesktop.defaultHeight);
			final int frameX = json.optInt("FrameLocX", app.settingsPlayer().defaultX());
			final int frameY = json.optInt("FrameLocY", app.settingsPlayer().defaultY());
			final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			if ((frameX + frameWidth <= screenSize.getWidth()) && (frameY + frameHeight <= screenSize.getHeight()))
			{
				SettingsDesktop.defaultWidth = frameWidth;
				SettingsDesktop.defaultHeight = frameHeight;
				app.settingsPlayer().setDefaultX(frameX);
				app.settingsPlayer().setDefaultY(frameY);
			}
			
			// Load the AI preferences
			for (int p = 0; p < app.manager().aiSelected().length; ++p)
			{
				app.manager().aiSelected()[p].setName(json.optString("Names_" + p, app.manager().aiSelected()[p].name()));
				try
				{
					final JSONObject jsonAI = json.optJSONObject("AI_" + p);
					AIRegistry.processJson(jsonAI);
					if (jsonAI != null)
					{
						app.manager().aiSelected()[p] = new AIDetails(app.manager(), jsonAI, p, json.optString("MenuNames_" + p, app.manager().aiSelected()[p].menuItemName()));
						app.manager().aiSelected()[p].setThinkTime(json.optDouble("SearchTime_" + p, app.manager().aiSelected()[p].thinkTime()));
					}
				}
				catch (final Exception error)
				{
					// invalid AI
					error.printStackTrace();
				}
			}

			// Game specific preferences
			final Iterator<?> keysToCopyIterator = json.keys();
			final List<String> keysList = new ArrayList<>();
			while (keysToCopyIterator.hasNext())
			{
				final String key = (String) keysToCopyIterator.next();
				keysList.add(key);
			}
			final String[] keysArray = keysList.toArray(new String[keysList.size()]);

			for (int i = 0; i < keysArray.length; i++)
			{
				
				// game turn limits
				if (keysArray[i].length() > 7 && keysArray[i].substring(0, 7).contentEquals("MAXTURN"))
				{
					app.manager().settingsManager().setTurnLimit(keysArray[i].substring(7), json.optInt(keysArray[i], app.manager().settingsManager().turnLimit(keysArray[i].substring(7))));
				}
				
				// game piece style.
				if (keysArray[i].length() > 11 && keysArray[i].substring(0, 11).contentEquals("PIECEFAMILY"))
				{
					app.bridge().settingsVC().setPieceFamily
					(
						keysArray[i].substring(11), 
						json.optString(keysArray[i], 
						app.bridge().settingsVC().pieceFamily(keysArray[i].substring(11)))
					);
				}
			}
			app.manager().settingsNetwork().backupAiPlayers(app.manager());

			app.settingsPlayer().setPreferencesLoaded(true);
		}
		catch (final Exception e)
		{
			//e.printStackTrace();
			
			// Problem loading preferences
			System.out.println("Loading default preferences.");

			// Try to delete preferences
			final File brokenPreferences = new File("." + File.separator + "ludii_preferences.json");
			brokenPreferences.delete();
		}
	}
	
	//-------------------------------------------------------------------------


}
