package app.display.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import app.DesktopApp;
import app.PlayerApp;
import app.utils.BufferedImageUtil;
import app.utils.GameUtil;
import app.utils.SVGUtil;
import game.equipment.container.board.Board;
import main.DatabaseInformation;
import other.context.Context;
import other.trial.Trial;
import util.PlaneType;
import view.container.ContainerStyle;
import view.container.styles.board.BoardlessStyle;


/**
 * Functions for generating thumbnail images.
 * 
 * @author Matthew Stephenson
 */
public class Thumbnails
{

	//-------------------------------------------------------------------------
	
	/**
	 * Take snapshots of the current game's starting position and end position.
	 */
	public static void generateThumbnails(final PlayerApp app, final boolean includeRulesetName)
	{
		final int imageSize = DesktopApp.view().getBoardPanel().boardSize();
		final Board board = app.manager().ref().context().board();
		final Context context = app.manager().ref().context();

		boolean boardEmptyAtStart = true;
		for (int i = 0; i < app.manager().ref().context().board().topology().cells().size(); i++)
			if (context.containerState(0).whatCell(i) != 0)
				boardEmptyAtStart = false;

		for (int i = 0; i < app.manager().ref().context().board().topology().vertices().size(); i++)
			if (context.containerState(0).whatVertex(i) != 0)
				boardEmptyAtStart = false;

		for (int i = 0; i < app.manager().ref().context().board().topology().edges().size(); i++)
			if (context.containerState(0).whatEdge(i) != 0)
				boardEmptyAtStart = false;
		
		final ContainerStyle boardStyle = app.bridge().getContainerStyle(board.index());
		
		final BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		if (!app.manager().ref().context().game().metadata().graphics().boardHidden())
		{
			boardStyle.render(PlaneType.BOARD, context);
			final String svg = boardStyle.containerSVGImage();
			final BufferedImage img = SVGUtil.createSVGImage(svg, imageSize, imageSize);
			if (!(boardStyle instanceof BoardlessStyle))
				g2d.drawImage(img, 0, 0, imageSize, imageSize, 0, 0, img.getWidth(), img.getHeight(), null);
		}

		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.COMPONENTS, context);
		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.HINTS, context);
		app.bridge().getContainerStyle(context.board().index()).draw(g2d, PlaneType.COSTS, context);
	
		String outputName = app.manager().ref().context().game().name();
		if (includeRulesetName && app.manager().settingsManager().userSelections().ruleset() != -1)
		{
			final String rulesetNameString = DatabaseInformation.getRulesetDBName(app.manager().ref().context().game().description().rulesets().get(app.manager().settingsManager().userSelections().ruleset()).heading());
			outputName += "-" + rulesetNameString;
		}
		outputName = outputName.trim();
		
		try
		{
			final File outputfile = new File("./thumb-" + outputName + "-a.png");
			ImageIO.write(image, "png", outputfile);
			final File outputfileSmallEmpty = new File("./thumb-" + outputName + "-f.png");
			ImageIO.write(BufferedImageUtil.resize(image, 100, 100), "png", outputfileSmallEmpty);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}

		// 2. Save end position
		final BufferedImage image2;
		if (boardEmptyAtStart)
			image2 = generateEndPosition(app, imageSize, true, outputName);
		else
			image2 = generateEndPosition(app, imageSize, false, outputName);

		// If the initial screenshot of the game is empty, then use the end state for thumbnail.
		if (boardEmptyAtStart)
		{
			try
			{
				final File outputfileBig = new File("./thumb-" + outputName + "-d.png");
				ImageIO.write(image2, "png", outputfileBig);
				final File outputfile = new File("./thumb-" + outputName + "-c.png");
				ImageIO.write(BufferedImageUtil.resize(image2, 100, 100), "png", outputfile);
				final File outputfileSmall = new File("./thumb-" + outputName + "-e.png");
				ImageIO.write(BufferedImageUtil.resize(image2, 30, 30), "png", outputfileSmall);
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			
			try
			{
				final File outputfileBig = new File("./thumb-" + outputName + "-d.png");
				ImageIO.write(image, "png", outputfileBig);
				final File outputfile = new File("./thumb-" + outputName + "-c.png");
				ImageIO.write(BufferedImageUtil.resize(image, 100, 100), "png", outputfile);
				final File outputfileSmall = new File("./thumb-" + outputName + "-e.png");
				ImageIO.write(BufferedImageUtil.resize(image, 30, 30), "png", outputfileSmall);
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}

		app.graphicsCache().clearAllCachedImages();
		app.repaint();
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Generate the ending position of the game using a random playout.
	 */
	private static BufferedImage generateEndPosition(final PlayerApp app, final int imageSize, final boolean notEmpty, final String outputName)
	{
		final Board board2 = app.manager().ref().context().board();
		
		final int moveLimit = 300;
		
		boolean boardEmptyAtEnd = true;
		// Create a context for drawing in the background
		Trial trial2 = new Trial(app.manager().ref().context().game());
		Context context2 = new Context(app.manager().ref().context().game(), trial2);
		app.manager().ref().context().game().start(context2);

		app.manager().ref().context().game().playout(context2, null, 1.0, null, 0, moveLimit, ThreadLocalRandom.current());
		
		for (int i = 0; i < app.manager().ref().context().board().topology().cells().size(); i++)
		{
			if (context2.containerState(0).whatCell(i) != 0)
			{
				boardEmptyAtEnd = false;
			}
		}
		for (int i = 0; i < app.manager().ref().context().board().topology().vertices().size(); i++)
		{
			if (context2.containerState(0).whatVertex(i) != 0)
			{
				boardEmptyAtEnd = false;
			}
		}
		for (int i = 0; i < app.manager().ref().context().board().topology().edges().size(); i++)
		{
			if (context2.containerState(0).whatEdge(i) != 0)
			{
				boardEmptyAtEnd = false;
			}
		}
		
		if (notEmpty)
		{
			int counter = 0;
			while (boardEmptyAtEnd && counter < 50)
			{
				// Create a context for drawing in the background
				trial2 = new Trial(app.manager().ref().context().game());
				context2 = new Context(app.manager().ref().context().game(), trial2);
				app.manager().ref().context().game().start(context2);

				app.manager().ref().context().game().playout(context2, null, 1.0, null, 0, moveLimit, ThreadLocalRandom.current());
				
				for (int i = 0; i < app.manager().ref().context().board().topology().cells().size(); i++)
				{
					if (context2.containerState(0).whatCell(i) != 0)
					{
						boardEmptyAtEnd = false;
					}
				}
				for (int i = 0; i < app.manager().ref().context().board().topology().vertices().size(); i++)
				{
					if (context2.containerState(0).whatVertex(i) != 0)
					{
						boardEmptyAtEnd = false;
					}
				}
				for (int i = 0; i < app.manager().ref().context().board().topology().edges().size(); i++)
				{
					if (context2.containerState(0).whatEdge(i) != 0)
					{
						boardEmptyAtEnd = false;
					}
				}
				
				counter++;
			}
		}
		
		final ContainerStyle boardStyle = app.bridge().getContainerStyle(board2.index());
		
		final BufferedImage image2 = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d2 = image2.createGraphics();
		g2d2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		if (!app.manager().ref().context().game().metadata().graphics().boardHidden())
		{
			boardStyle.render(PlaneType.BOARD, context2);
			final String svg = boardStyle.containerSVGImage();
			final BufferedImage img = SVGUtil.createSVGImage(svg, imageSize, imageSize);
			if (!(boardStyle instanceof BoardlessStyle))
				g2d2.drawImage(img, 0, 0, imageSize, imageSize, 0, 0, img.getWidth(), img.getHeight(), null);
		}

		app.bridge().getContainerStyle(context2.board().index()).draw(g2d2, PlaneType.COMPONENTS, context2);
		app.bridge().getContainerStyle(context2.board().index()).draw(g2d2, PlaneType.HINTS, context2);
		app.bridge().getContainerStyle(context2.board().index()).draw(g2d2, PlaneType.COSTS, context2);
		
		try
		{
			final File outputfile = new File("./thumb-" + outputName + "-b.png");
			ImageIO.write(image2, "png", outputfile);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		return image2;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Take snapshot of the current game's board.
	 */
	public static void generateBoardThumbnail(final PlayerApp app)
	{
		final int imageSize = DesktopApp.view().getBoardPanel().boardSize();

		final Board board = app.manager().ref().context().board();

		// Create a context for drawing in the background
		final Trial trial = new Trial(app.manager().ref().context().game());
		final Context context = new Context(app.manager().ref().context().game(), trial);
		GameUtil.startGame(app);
		
		final ContainerStyle boardStyle = app.bridge().getContainerStyle(board.index());

		//BoardView.drawBoardState(g2d, context);	
		final BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		if (!app.manager().ref().context().game().metadata().graphics().boardHidden())
		{
			boardStyle.render(PlaneType.BOARD, context);
			final String svg = boardStyle.containerSVGImage();
			final BufferedImage img = SVGUtil.createSVGImage(svg, imageSize, imageSize);
			if (!(boardStyle instanceof BoardlessStyle))
				g2d.drawImage(img, 0, 0, imageSize, imageSize, 0, 0, img.getWidth(), img.getHeight(), null);
		}

		try
		{
			final File outputfile = new File("./thumb-Board_" + app.manager().ref().context().game().name() + ".png");
			ImageIO.write(image, "png", outputfile);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		app.graphicsCache().clearAllCachedImages();
		app.repaint();
	}
	
	//-------------------------------------------------------------------------
	
}
