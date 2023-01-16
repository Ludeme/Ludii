package app.utils;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 * Functions relating to the playing of sounds/music.
 * 
 * @author Matthew.Stephenson
 */
public class Sound
{
	
	//-------------------------------------------------------------------------

//	/**
//	 * Play a specific sound once.
//	 */
//	public static synchronized void playSound(final String soundName) 
//	{
//		final String soundPath = "../Common/res/audio/" + soundName + ".wav";
//		
//		new Thread(new Runnable() 
//		{
//			// Audio playing doesn't work with standard try-with-resource approach.
//			// https://stackoverflow.com/questions/25564980/java-use-a-clip-and-a-try-with-resources-block-which-results-with-no-sound
//			@SuppressWarnings("resource")
//			@Override
//			public void run() 
//		    {
//				try 
//				{
//					final AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(soundPath));
//			        final DataLine.Info info = new DataLine.Info(Clip.class, inputStream.getFormat());
//			        final Clip clip = (Clip)AudioSystem.getLine(info);
//			        clip.open(inputStream);
//					clip.start(); 
//				} 
//				catch (final Exception e) 
//				{
//					e.printStackTrace();
//				}
//		    }
//		}).start();
//	}
	
	/**
	 * Play a specific sound once.
	 */
	public static synchronized void playSound(final String soundName) 
	{
		final String soundPath = "/" + soundName + ".wav";
		new Thread(new Runnable() 
		{
			// Audio playing doesn't work with standard try-with-resource approach.
			// https://stackoverflow.com/questions/25564980/java-use-a-clip-and-a-try-with-resources-block-which-results-with-no-sound
			@SuppressWarnings("resource")
			@Override
			public void run() 
		    {
				try 
				{
					final Clip clip = AudioSystem.getClip();
					final InputStream is = Sound.class.getResourceAsStream(soundPath);
					final BufferedInputStream bufferedIS = new BufferedInputStream(is);
					final AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedIS);	
					clip.open(audioInputStream);
					
					clip.start(); 
					bufferedIS.close();
					audioInputStream.close();
					
					// Close the clip only once it has finished playing.
					clip.addLineListener(new LineListener() 
					{
					    @Override
					    public void update(final LineEvent event) 
					    {
					        if (event.getType() == LineEvent.Type.STOP)
					            clip.close();
					    }
					});
				} 
				catch (final Exception e) 
				{
					e.printStackTrace();
				}
		    }
		}).start();
	}
	
}
