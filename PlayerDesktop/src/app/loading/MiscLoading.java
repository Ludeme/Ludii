package app.loading;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import app.DesktopApp;
import app.PlayerApp;
import app.display.MainWindowDesktop;
import app.display.SVGWindow;
import app.menu.MainMenu;
import app.utils.GameUtil;
import app.utils.SVGUtil;
import game.Game;
import graphics.svg.SVGtoImage;
import manager.Referee;
import manager.ai.AIMenuName;
import manager.ai.AIUtil;
import manager.utils.game_logs.MatchRecord;
import other.context.Context;
import tournament.Tournament;

public class MiscLoading
{

	//-------------------------------------------------------------------------

	/**
	 * Load and view an external .svg file.
	 */
	public static void loadSVG(final PlayerApp app, final MainWindowDesktop view)
	{
		JFrame svgFrame = null;
		SVGWindow svgView = null;
		
		final String fileName = FileLoading.selectFile(DesktopApp.frame(), true, "/../Common/img/svg/", "SVG files (*.svg)", view, "svg");
		if (fileName == null)
			return;

		svgView = new SVGWindow();
		svgFrame = new JFrame("SVG Viewer");
		svgFrame.add(svgView);
		
		final int sz = (Math.min(DesktopApp.frame().getWidth()/2, DesktopApp.frame().getHeight()-40)) - 20;
		svgFrame.setSize((sz + 20) * 2, sz + 60);
		svgFrame.setLocationRelativeTo(DesktopApp.frame());

		final Context context = app.contextSnapshot().getContext(app);
		final SVGGraphics2D image1 = renderImageSVG(sz, fileName, app.bridge().settingsColour().playerColour(context, 1));
		final SVGGraphics2D image2 = renderImageSVG(sz, fileName, app.bridge().settingsColour().playerColour(context, 2));

		final BufferedImage img1 = SVGUtil.createSVGImage(image1.getSVGDocument(), sz, sz);
		final BufferedImage img2 = SVGUtil.createSVGImage(image2.getSVGDocument(), sz, sz);
		svgView.setImages(img1, img2);

		svgFrame.setVisible(true);

		svgView.repaint();
	}	
	
	//----------------------------------------------------------------------------
	
	/**
	 * Create SVG piece image of internal file, only used for the SVG viewer.
	 */
	public static SVGGraphics2D renderImageSVG(final int pixels, final String filePath1, final Color fillColour)
	{
		final SVGGraphics2D g2d = new SVGGraphics2D(pixels, pixels);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		SVGtoImage.loadFromFilePath
		(
			g2d, filePath1, new Rectangle(0,0,pixels,pixels), Color.BLACK, fillColour, 0
		);
		return g2d;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Loads a demo as described by a JSON object.
	 */
	public static void loadDemo(final PlayerApp app, final JSONObject jsonDemo)
	{
		app.manager().settingsManager().setAgentsPaused(app.manager(), true);
		final Referee ref = app.manager().ref();
		final Context context = ref.context();
		final Game game = context.game();

		final String gameName = jsonDemo.getString("Game");
		final List<String> gameOptions = new ArrayList<>();

		final JSONArray optionsArray = jsonDemo.optJSONArray("Options");
		if (optionsArray != null)
			for (final Object object : optionsArray)
				gameOptions.add((String) object);
	
		GameLoading.loadGameFromName(app, gameName, gameOptions, false);

		for (int p = 1; p <= game.players().count(); ++p)
		{
			final JSONObject jsonPlayer = jsonDemo.optJSONObject("Player " + p);
			if (jsonPlayer != null)
			{
				if (jsonPlayer.has("AI"))
					AIUtil.updateSelectedAI(app.manager(), jsonPlayer, p, AIMenuName.getAIMenuName(jsonPlayer.getJSONObject("AI").getString("algorithm")));
				
				if (jsonPlayer.has("Time Limit"))
					app.manager().aiSelected()[p].setThinkTime(jsonPlayer.getDouble("Time Limit"));
			}
		}

		final JSONObject jsonSettings = jsonDemo.optJSONObject("Settings");
		if (jsonSettings != null)
		{
			if (jsonSettings.has("Show AI Distribution"))
				app.settingsPlayer().setShowAIDistribution(jsonSettings.getBoolean("Show AI Distribution"));
		}

		app.resetMenuGUI();

		if (jsonDemo.has("Trial"))
		{
			final String trialFile = jsonDemo.getString("Trial").replaceAll(Pattern.quote("\\"), "/");

			try 
			(
				final InputStreamReader reader = 
					new InputStreamReader(MainMenu.class.getResourceAsStream(trialFile), "UTF-8");		
			)
			{
				final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromInputStream(reader, game);
				app.manager().setCurrGameStartRngState(loadedRecord.rngState());

				app.manager().ref().makeSavedMoves(app.manager(), loadedRecord.trial().generateCompleteMovesList());
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	

	//-------------------------------------------------------------------------

	/**
	 * Select and load an external tournament file.
	 * NOTE. Tournament should only include 2 player games.
	 */
	public static void loadTournamentFile(final PlayerApp app)
	{
		GameUtil.resetGame(app, false);
		final int fcReturnVal = DesktopApp.loadTournamentFileChooser().showOpenDialog(DesktopApp.frame());
		if (fcReturnVal == JFileChooser.APPROVE_OPTION)
		{
			final File file = DesktopApp.loadTournamentFileChooser().getSelectedFile();

			try (final InputStream inputStream = new FileInputStream(file))
			{
				final JSONObject json = new JSONObject(new JSONTokener(inputStream));
				app.setTournament(new Tournament(json));
				app.tournament().setupTournament();
				app.tournament().startNextTournamentGame(app.manager());
			}
			catch (final Exception e1)
			{
				System.out.println("Tournament file is not formatted correctly");
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
}
