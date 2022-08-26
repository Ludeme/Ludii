package utils.experiments;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDateTime;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * A wrapper around a (long) interruptible experiment. This class
 * provides a small, simple frame with a button that can be used to set a boolean
 * "interrupted" flag to true. An abstract method can be overridden by subclasses
 * to run an experiment. Inside that method, subclasses can periodically check the
 * flag, and cleanly interrupt the experiment (after performing any required
 * file saving etc.) if the button has been pressed.
 *
 * The complete GUI functionality can also be disabled (which allows the same experiment
 * code to run in headless mode on cluster for example).
 *
 * @author Dennis Soemers
 */
public abstract class InterruptableExperiment
{

	//-------------------------------------------------------------------------

	/** Flag which will be set to true if the interrupt button is pressed */
	protected boolean interrupted = false;

	/** Start time of experiment (in milliseconds) */
	protected final long experimentStartTime;

	/** Maximum wall time we're allowed to run for (in milliseconds) */
	protected final long maxWallTimeMs;

	//-------------------------------------------------------------------------

	/**
	 * Creates an "interruptible" experiment (with button to interrupt
	 * experiment if useGUI = True), which will then also immediately
	 * start running.
	 *
	 * @param useGUI
	 */
	public InterruptableExperiment(final boolean useGUI)
	{
		this(useGUI, -1);
	}

	/**
	 * Creates an "interruptible" experiment (with button to interrupt
	 * experiment if useGUI = True), which will then also immediately
	 * start running.
	 *
	 * Using the checkWallTime() method, the experiment can also automatically
	 * interrupt itself and exit cleanly before getting interrupted in a
	 * not-clean manner due to exceeding wall time (e.g. on cluster)
	 *
	 * @param useGUI
	 * @param maxWallTime Maximum wall time (in minutes)
	 */
	public InterruptableExperiment(final boolean useGUI, final int maxWallTime)
	{
		JFrame frame = null;

		experimentStartTime = System.currentTimeMillis();
		maxWallTimeMs = 60L * 1000L * maxWallTime;

		if (useGUI)
		{
			frame = new JFrame("Ludii Interruptible Experiment");
			frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

			frame.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(final WindowEvent e)
				{
					interrupted = true;
				}
			});

			// Logo
			try
			{
				final URL resource = this.getClass().getResource("/ludii-logo-100x100.png");
				final BufferedImage image = ImageIO.read(resource);
				frame.setIconImage(image);
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}

			final JPanel panel = new JPanel(new GridLayout());

			final JButton interruptButton = new JButton("Interrupt Experiment");
			interruptButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e) {
					interrupted = true;
				}
			});
			panel.add(interruptButton);

			frame.setContentPane(panel);

			frame.setSize(600, 250);
			frame.setLocationRelativeTo(null);

			frame.setVisible(true);
		}

		try
		{
			runExperiment();
		}
		finally
		{
			if (frame != null)
			{
				frame.dispose();
			}
		}
	}

	//-------------------------------------------------------------------------

	/** Implement experiment code here */
	public abstract void runExperiment();

	//-------------------------------------------------------------------------

	/**
	 * Checks if we're about to exceed maximum wall time, and automatically
	 * sets the interrupted flag if we are.
	 *
	 * @param safetyBuffer Ratio of maximum wall time that we're willing to
	 * throw away just to be safe (0.01 or 0.05 should be good values)
	 */
	public void checkWallTime(final double safetyBuffer)
	{
		if (maxWallTimeMs > 0)
		{
			final long terminateAt =
					(long) (experimentStartTime +
							(1.0 - safetyBuffer) * maxWallTimeMs);

			if (System.currentTimeMillis() >= terminateAt)
			{
				interrupted = true;
			}
		}
	}

	/**
	 * Utility method that uses a given PrintWriter to print a single given
	 * line to a log, but with the current time prepended.
	 * @param logWriter
	 * @param line
	 */
	@SuppressWarnings("static-method")
	public void logLine(final PrintWriter logWriter, final String line)
	{
		if (logWriter != null)
		{
			logWriter.println
			(
				String.format
				(
					"[%s]: %s",
					LocalDateTime.now(),
					line
				)
			);
		}
	}
	
	/**
	 * @return Do we want to be interrupted?
	 */
	public boolean wantsInterrupt()
	{
		return interrupted;
	}

	//-------------------------------------------------------------------------

}
