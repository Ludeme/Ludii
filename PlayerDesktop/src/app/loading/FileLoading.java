package app.loading;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import app.DesktopApp;
import app.display.MainWindowDesktop;
import app.util.SettingsDesktop;

/**
 * Functions for loading external resources.
 * 
 * @author Matthew Stephenson
 */
public class FileLoading
{

	//-------------------------------------------------------------------------

	/**
	 * Select a file to load (used when loading .lud and .svg)
	 * @return Filepath of selected file.
	 */
	public static final String selectFile(final JFrame parent, final boolean isOpen, final String relativePath,
			final String description, final MainWindowDesktop view, final String... extensions)
	{
		final String baseFolder = System.getProperty("user.dir");

		String folder = baseFolder + relativePath;
		final File testFile = new File(folder);
		if (!testFile.exists())
			folder = baseFolder; // no suitable subfolder - let user find them

		final JFileChooser dlg = new JFileChooser(folder);
		dlg.setPreferredSize(new Dimension(500, 500));

		// Set the file filter to show mgl files only
		final FileFilter filter = new FileNameExtensionFilter(description, extensions);
		dlg.setFileFilter(filter);

		int response;
		if (isOpen)
			response = dlg.showOpenDialog(parent);
		else
			response = dlg.showSaveDialog(parent);

		if (response == JFileChooser.APPROVE_OPTION)
			return dlg.getSelectedFile().getAbsolutePath();

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Instantiates a few File Chooser objects
	 */
	public static void createFileChoosers()
	{
		DesktopApp.setJsonFileChooser(createFileChooser(DesktopApp.lastSelectedJsonPath(), ".json", "JSON files (.json)"));
		DesktopApp.setJarFileChooser(createFileChooser(DesktopApp.lastSelectedJarPath(), ".jar", "JAR files (.jar)"));
		DesktopApp.setGameFileChooser(createFileChooser(DesktopApp.lastSelectedGamePath(), ".lud", "LUD files (.lud)"));
		DesktopApp.setAiDefFileChooser(createFileChooser(DesktopApp.lastSelectedAiDefPath(), "ai.def", "AI.DEF files (ai.def)"));

		// Also create file chooser for saving played games
		DesktopApp.setSaveGameFileChooser(new JFileChooser(DesktopApp.lastSelectedSaveGamePath()));
		DesktopApp.saveGameFileChooser().setPreferredSize(new Dimension(SettingsDesktop.defaultWidth, SettingsDesktop.defaultHeight));

		DesktopApp.setLoadTrialFileChooser(new JFileChooser(DesktopApp.lastSelectedLoadTrialPath()));
		DesktopApp.loadTrialFileChooser().setPreferredSize(new Dimension(SettingsDesktop.defaultWidth, SettingsDesktop.defaultHeight));

		DesktopApp.setLoadTournamentFileChooser(new JFileChooser(DesktopApp.lastSelectedLoadTournamentPath()));
		DesktopApp.loadTournamentFileChooser().setPreferredSize(new Dimension(SettingsDesktop.defaultWidth, SettingsDesktop.defaultHeight));
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Creates a File Chooser at a specified directory looking for a specific file extension.
	 */
	private static JFileChooser createFileChooser(final String defaultDir, final String extension, final String description)
	{
		final JFileChooser fileChooser;

		// Try to set a useful default directory
		if (defaultDir != null && defaultDir.length() > 0 && new File(defaultDir).exists())
		{
			fileChooser = new JFileChooser(defaultDir);
		}
		else
		{
			fileChooser = new JFileChooser("");
		}
			

		final FileFilter filter = new FileFilter()
		{
			@Override
			public boolean accept(final File f)
			{
				return f.isDirectory() || f.getName().endsWith(extension);
			}

			@Override
			public String getDescription()
			{
				return description;
			}

		};
		fileChooser.setFileFilter(filter);
		fileChooser.setPreferredSize(new Dimension(SettingsDesktop.defaultWidth, SettingsDesktop.defaultHeight));

		// Automatically try to switch to details view in file chooser
		final Action details = fileChooser.getActionMap().get("viewTypeDetails");
		if (details != null)
			details.actionPerformed(null);

		return fileChooser;
	}

	//-------------------------------------------------------------------------
	
	/** 
	 * Writes specified text to a specified file, at the root directory. 
	 */
	public static void writeTextToFile(final String fileName, final String text)
	{
		final File file = new File("." + File.separator + fileName);
		if (!file.exists())
		{
			try
			{
				file.createNewFile();
			}
			catch (final IOException e2)
			{
				e2.printStackTrace();
			}
		}
		
		try (FileWriter writer = new FileWriter(file);)
		{
			writer.write(text + "\n");
			writer.close();
			DesktopApp.view().setTemporaryMessage("Log file created.");
		}
		catch (final Exception e1)
		{
			e1.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Writes specified exception message to a specified file, at the root directory. 
	 */
	public static void writeErrorFile(final String fileName, final Exception e)
	{
		final File file = new File("." + File.separator + fileName);
		if (!file.exists())
		{
			try
			{
				file.createNewFile();
			}
			catch (final IOException e2)
			{
				e2.printStackTrace();
			}
		}
		
		try (final PrintWriter writer = new PrintWriter(file))
		{
			final StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			writer.println(errors.toString() + "\n");
			writer.close();
			DesktopApp.view().setTemporaryMessage("Error report file created.");
		}
		catch (final Exception e1)
		{
			e1.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
}
