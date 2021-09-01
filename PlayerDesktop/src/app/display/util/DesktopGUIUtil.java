package app.display.util;

import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import app.DesktopApp;
import app.PlayerApp;
import app.tutorialVisualisation.GifSequenceWriter;
import app.views.players.PlayerViewUser;
import main.Constants;
import manager.ai.AIUtil;
import metadata.graphics.util.PieceStackType;
import metadata.graphics.util.StackPropertyType;
import other.context.Context;
import other.location.Location;
import other.state.container.ContainerState;
import util.ContainerUtil;


/**
 * Utility functions for the GUI.
 * 
 * @author Matthew Stephenson
 */
public class DesktopGUIUtil
{

	//-------------------------------------------------------------------------

	/**
	 * If the current system is a Mac computer.
	 */
	public static boolean isMac()
	{
		final String osName = System.getProperty("os.name");  
		final boolean isMac = osName.toLowerCase().startsWith("mac os x");	
		return isMac;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Save a screenshot of the current game board state.
	 */
	public static void gameScreenshot(final String savedName)
	{				
		EventQueue.invokeLater(() ->
		{
			Robot robot = null;
			try
			{
				robot = new Robot();
			}
			catch (final AWTException e)
			{
				e.printStackTrace();
			}
			final java.awt.Container panel = DesktopApp.frame().getContentPane();
			final Point pos = panel.getLocationOnScreen();
			final Rectangle bounds = panel.getBounds();
			bounds.x = pos.x;
			bounds.y = pos.y;
			bounds.x -= 1;
			bounds.y -= 1;
			bounds.width += 2;
			bounds.height += 2;
			final BufferedImage snapShot = robot.createScreenCapture(bounds);
			try
			{
				ImageIO.write(snapShot, "png", new File(savedName + ".png"));
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		});
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Save a gif of the current game board state.
	 */
	public static void gameGif(final String savedName)
	{				
		EventQueue.invokeLater(() ->
		{
			Robot robotTemp = null;
			try
			{
				robotTemp = new Robot();
			}
			catch (final AWTException e)
			{
				e.printStackTrace();
			}
			final Robot robot = robotTemp;
			
			final java.awt.Container panel = DesktopApp.frame().getContentPane();
			final Point pos = panel.getLocationOnScreen();
			final Rectangle bounds = panel.getBounds();
			bounds.x = pos.x;
			bounds.y = pos.y;
			bounds.x -= 1;
			bounds.y -= 1;
			bounds.width += 2;
			bounds.height += 2;
			
			final List<BufferedImage> snapshots = new ArrayList<>();
			final List<String> imgLst = new ArrayList<>();
			
			final Timer screenshotTimer = new Timer();
			screenshotTimer.scheduleAtFixedRate(new TimerTask()
			{
				int index = 0;
				
			    @Override
			    public void run()
			    {
			    	if (index >= 10)
			    	{
			    		System.out.println("Screenshots complete.");
			    		screenshotTimer.cancel();
			    		screenshotTimer.purge();
			    	}
			    	else
			    	{
			    		final BufferedImage snapshot = robot.createScreenCapture(bounds);
			    		snapshots.add(snapshot);
			    		index++;
			    	}
			    }
			}, 0, 100);
			
			new java.util.Timer().schedule
			( 
		        new java.util.TimerTask() 
		        {
		            @Override
		            public void run() 
		            {
						for (int i = 0; i < snapshots.size(); i++)
						{
							final BufferedImage snapshot = snapshots.get(i);
							try
							{
				    			final String imageName = "tutorialVisualisation/gif/" + savedName + i + ".jpeg";
								ImageIO.write(snapshot, "jpeg", new File(imageName));
								imgLst.add(imageName);
							}
							catch (final IOException e)
							{
								e.printStackTrace();
							}
			            }
						System.out.println("Screenshots saved.");
		            }
		        }, 
		        10000 
			);
			
			new java.util.Timer().schedule
			( 
		        new java.util.TimerTask() 
		        {
		            @Override
		            public void run() 
		            {
		            	final String videoLocation = "tutorialVisualisation/gif/gifs/" + savedName;
		            	
		            	// grab the output image type from the first image in the sequence
						try
						{
							final BufferedImage firstImage = ImageIO.read(new File(imgLst.get(0)));

		            	  // create a new BufferedOutputStream with the last argument
		            	  final ImageOutputStream output = new FileImageOutputStream(new File(videoLocation));
		            	  
		            	  // create a gif sequence with the type of the first image, 1 second between frames, which loops continuously
		            	  final GifSequenceWriter writer = new GifSequenceWriter(output, firstImage.getType(), 1, false);
		            	  
		            	  // write out the first image to our sequence...
		            	  writer.writeToSequence(firstImage);
		            	  for(int i=1; i<imgLst.size()-1; i++) {
		            	    final BufferedImage nextImage = ImageIO.read(new File(imgLst.get(i)));
		            	    writer.writeToSequence(nextImage);
		            	  }
		            	  
		            	  writer.close();
		            	  output.close();
		            	  
		            	  System.out.println("Gif completed.");
		            	  
						}
						catch (final IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            }
		        }, 
		        15000 
			);
		});
	}
	
	//-------------------------------------------------------------------------
	
//	/**
//	 * Save a video animation of the current game board state.
//	 */
//	public static void gameVideo(final String savedName)
//	{				
//		EventQueue.invokeLater(() ->
//		{
//			Robot robotTemp = null;
//			try
//			{
//				robotTemp = new Robot();
//			}
//			catch (final AWTException e)
//			{
//				e.printStackTrace();
//			}
//			final Robot robot = robotTemp;
//			
//			final java.awt.Container panel = DesktopApp.frame().getContentPane();
//			final Point pos = panel.getLocationOnScreen();
//			final Rectangle bounds = panel.getBounds();
//			bounds.x = pos.x;
//			bounds.y = pos.y;
//			bounds.x -= 1;
//			bounds.y -= 1;
//			bounds.width += 2;
//			bounds.height += 2;
//			
//			//final List<BufferedImage> snapShots = new ArrayList<>();
//			final Vector<String> imgLst = new Vector<>();
//			
//			final Timer screenshotTimer = new Timer();
//			screenshotTimer.scheduleAtFixedRate(new TimerTask()
//			{
//				int index = 0;
//				
//			    @Override
//			    public void run()
//			    {
//			    	if (index >= 50)
//			    	{
//			    		System.out.println("Screenshots complete.");
//			    		screenshotTimer.cancel();
//			    		screenshotTimer.purge();
//			    	}
//			    	else
//			    	{
//			    		final BufferedImage snapShot = robot.createScreenCapture(bounds);
//			    		
//			    		try
//						{
//			    			final String imageName = "tutorialVisualisation/video/" + index + ".jpeg";
//							ImageIO.write(snapShot, "jpeg", new File(imageName));
//							imgLst.add(imageName);
//						}
//						catch (final IOException e)
//						{
//							e.printStackTrace();
//						}
//			    		
//			    		index++;
//			    	}
//			    }
//			}, 0, 1);
//			
//			new java.util.Timer().schedule
//			( 
//		        new java.util.TimerTask() 
//		        {
//		            @Override
//		            public void run() 
//		            {
//	            	    final JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
//	            	    MediaLocator oml;
//	            	    final String videoLocation = "tutorialVisualisation/video/" + savedName;
//	            	    
//	            	    if ((oml = JpegImagesToMovie.createMediaLocator(videoLocation)) == null) 
//	            	    {
//	            	        System.err.println("Cannot build media locator from: " + videoLocation);
//	            	        System.exit(0);
//	            	    }
//	            	    
//	            	    final int interval = 50;
//	            	    try
//						{
//							imageToMovie.doIt(bounds.width, bounds.height, (1000 / interval), imgLst, oml);
//						}
//						catch (final MalformedURLException e)
//						{
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//		            }
//		        }, 
//		        10000 
//			);
//		});
//	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get a list of all AI display names.
	 */
	public static ArrayList<String> getAIDropdownStrings(final PlayerApp app, final boolean includeHuman)
	{
		final ArrayList<String> allStrings = new ArrayList<>();
		
		if (includeHuman)
			allStrings.add("Human");
		
		allStrings.addAll(AIUtil.allValidAgentNames(app.contextSnapshot().getContext(app).game()));

		allStrings.add("From JAR");
		
		return allStrings;
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Repaints the necessary area for a component moving between two points.
	 */
	public static void repaintComponentBetweenPoints(final PlayerApp app, final Context context, final Location componentLocation, final Point oldPoint, final Point newPoint)
	{
		try
		{
			if (app.contextSnapshot().getContext(app).game().hasLargePiece())
			{
				DesktopApp.view().repaint();
				return;
			}
			
			// If any of the player panels have been moved due to metadata, repaint the whole board.
			for (final PlayerViewUser panel : DesktopApp.view().getPlayerPanel().playerSections)
			{
				if (context.game().metadata().graphics().handPlacement(context, panel.playerId()) != null)
				{
					DesktopApp.view().repaint();
					return;
				}
			}
			
			// Determine the size of the component image being dragged.
			final int cellSize = app.bridge().getContainerStyle(context.board().index()).cellRadiusPixels() * 2;
			final int containerId = ContainerUtil.getContainerId(context, componentLocation.site(), componentLocation.siteType());
			final ContainerState cs = context.state().containerStates()[containerId];
			final int localState = cs.state(componentLocation.site(), componentLocation.level(), componentLocation.siteType());
			final int who = cs.who(componentLocation.site(), componentLocation.level(), componentLocation.siteType());
			final int value = cs.value(componentLocation.site(), componentLocation.level(), componentLocation.siteType());
			final int rotation = cs.rotation(componentLocation.site(), componentLocation.level(), componentLocation.siteType());
			final PieceStackType componentStackType = PieceStackType.getTypeFromValue((int) context.metadata().graphics().stackMetadata(context, context.equipment().containers()[containerId], componentLocation.site(), componentLocation.siteType(), localState, value, StackPropertyType.Type));
			
			// Find the largest component image in the stack.
			int maxComponentSize = cellSize;
			for (int level = componentLocation.level(); level < Constants.MAX_STACK_HEIGHT; level++)
			{
				final int what = cs.what(componentLocation.site(), componentLocation.level(), componentLocation.siteType());
				if (what == 0)
					break;
				
				final int componentSize = app.graphicsCache().getComponentImageSize(containerId, what, who, localState, value, 0, rotation);

				if (componentSize > maxComponentSize)
					maxComponentSize = componentSize;
			}
			
			int midX = (newPoint.x + oldPoint.x) / 2;
			int midY = (newPoint.y + oldPoint.y) / 2;
			int width = ((Math.abs(newPoint.x - oldPoint.x) + maxComponentSize + cellSize));
			int height = ((Math.abs(newPoint.y - oldPoint.y) + maxComponentSize + cellSize));
			
			// If the component is stacked in a vertical manner, need to repaint the whole column.
			if (componentStackType.verticalStack())
			{
				height = DesktopApp.frame().getHeight();
				midY = height/2;
			}
			
			// If the component is stacked in a horizontal manner, need to repaint the whole row.
			if (componentStackType.horizontalStack())
			{
				width = DesktopApp.frame().getWidth();
				midX = width/2;
			}

			final Rectangle repaintArea = new Rectangle(midX - width/2, midY - height/2, width, height);
			DesktopApp.view().repaint(repaintArea);
		}
		catch (final Exception e)
		{
			// mouse off screen
		}
	}
	
	//-----------------------------------------------------------------------------

}
