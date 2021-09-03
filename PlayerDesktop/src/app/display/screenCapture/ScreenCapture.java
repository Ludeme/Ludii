package app.display.screenCapture;

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

public class ScreenCapture
{

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
				final File outputFile = new File(savedName + ".png");
				outputFile.getParentFile().mkdirs();
				ImageIO.write(snapShot, "png", outputFile);
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		});
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Save a gif animation of the current game board state.
	 */
	public static void gameGif(final String savedName)
	{				
		EventQueue.invokeLater(() ->
		{
			final int numberPictures = 10;
			final int delay = 100;
			
			// First, set up our robot to take several pictures, based on the above parameters.
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
			    	if (index >= numberPictures)
			    	{
			    		System.out.println("Gif images taken.");
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
			}, 0, delay);
			
			// Second, save these pictures as jpeg files.
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
				    			final String imageName = savedName + i + ".jpeg";
				    			final File outputFile = new File(imageName);
								outputFile.getParentFile().mkdirs();
								ImageIO.write(snapshot, "jpeg", new File(imageName));
								imgLst.add(imageName);
							}
							catch (final IOException e)
							{
								e.printStackTrace();
							}
			            }
						System.out.println("Gif images saved.");
		            }
		        }, 
		        numberPictures*delay + 1000 
			);
			
			// Third, Combine these jpeg files into a single .gif animation 
			new java.util.Timer().schedule
			( 
		        new java.util.TimerTask() 
		        {
		            @Override
		            public void run() 
		            {
		            	final String videoLocation = savedName + ".gif";

						try
						{
							// grab the output image type from the first image in the sequence
							final BufferedImage firstImage = ImageIO.read(new File(imgLst.get(0)));

							// create a new BufferedOutputStream with the last argument
							try(final ImageOutputStream output = new FileImageOutputStream(new File(videoLocation)))
							{
								// create a gif sequence with the type of the first image, 10 miliseconds between frames, which loops continuously.
								final GifSequenceWriter writer = new GifSequenceWriter(output, firstImage.getType(), 1, true);
								
								// write out all images in our sequence.
								for(int i=0; i<imgLst.size(); i++) 
								{
									final File imageFile = new File(imgLst.get(i));
									final BufferedImage nextImage = ImageIO.read(imageFile);
									writer.writeToSequence(nextImage);
									imageFile.delete();
								}
			            	  
								writer.close();
							}

							System.out.println("Gif animation completed.");
						}
						catch (final IOException e)
						{
							e.printStackTrace();
						}
		            }
		        }, 
		        numberPictures*delay + 1500  
			);
		});
	}
	
	//-------------------------------------------------------------------------
	
//		/**
//		 * Save a video animation of the current game board state.
//		 */
//		public static void gameVideo(final String savedName)
//		{				
//			EventQueue.invokeLater(() ->
//			{
//				Robot robotTemp = null;
//				try
//				{
//					robotTemp = new Robot();
//				}
//				catch (final AWTException e)
//				{
//					e.printStackTrace();
//				}
//				final Robot robot = robotTemp;
//				
//				final java.awt.Container panel = DesktopApp.frame().getContentPane();
//				final Point pos = panel.getLocationOnScreen();
//				final Rectangle bounds = panel.getBounds();
//				bounds.x = pos.x;
//				bounds.y = pos.y;
//				bounds.x -= 1;
//				bounds.y -= 1;
//				bounds.width += 2;
//				bounds.height += 2;
//				
//				//final List<BufferedImage> snapShots = new ArrayList<>();
//				final Vector<String> imgLst = new Vector<>();
//				
//				final Timer screenshotTimer = new Timer();
//				screenshotTimer.scheduleAtFixedRate(new TimerTask()
//				{
//					int index = 0;
//					
//				    @Override
//				    public void run()
//				    {
//				    	if (index >= 50)
//				    	{
//				    		System.out.println("Screenshots complete.");
//				    		screenshotTimer.cancel();
//				    		screenshotTimer.purge();
//				    	}
//				    	else
//				    	{
//				    		final BufferedImage snapShot = robot.createScreenCapture(bounds);
//				    		
//				    		try
//							{
//				    			final String imageName = "tutorialVisualisation/video/" + index + ".jpeg";
//								ImageIO.write(snapShot, "jpeg", new File(imageName));
//								imgLst.add(imageName);
//							}
//							catch (final IOException e)
//							{
//								e.printStackTrace();
//							}
//				    		
//				    		index++;
//				    	}
//				    }
//				}, 0, 1);
//				
//				new java.util.Timer().schedule
//				( 
//			        new java.util.TimerTask() 
//			        {
//			            @Override
//			            public void run() 
//			            {
//		            	    final JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
//		            	    MediaLocator oml;
//		            	    final String videoLocation = "tutorialVisualisation/video/" + savedName;
//		            	    
//		            	    if ((oml = JpegImagesToMovie.createMediaLocator(videoLocation)) == null) 
//		            	    {
//		            	        System.err.println("Cannot build media locator from: " + videoLocation);
//		            	        System.exit(0);
//		            	    }
//		            	    
//		            	    final int interval = 50;
//		            	    try
//							{
//								imageToMovie.doIt(bounds.width, bounds.height, (1000 / interval), imgLst, oml);
//							}
//							catch (final MalformedURLException e)
//							{
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//			            }
//			        }, 
//			        10000 
//				);
//			});
//		}
	
}
