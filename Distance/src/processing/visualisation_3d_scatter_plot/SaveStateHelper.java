package processing.visualisation_3d_scatter_plot;

import java.io.File;
import java.util.HashMap;

import javax.swing.JFileChooser;

import common.DistanceUtils;
import common.FolderLocations;
import common.LudRul;
import processing.visualisation_3d_scatter_plot.WrapperClass.Pos;

public class SaveStateHelper
{

	public static void saveDialoge(WrapperClass wc)
	{
		final JFileChooser f = new JFileChooser(FolderLocations.resourceAnnealingPosition);
		f.setDialogTitle("Choose SaveLocation");
		f.setMultiSelectionEnabled(false);
		f.setSelectedFile(new File(FolderLocations.resourceAnnealingPosition+File.separator+"jada.ser"));
		f.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final int userSelection = f.showDialog(null, "select");

		if (userSelection == JFileChooser.APPROVE_OPTION)
		{
			final File file = f.getSelectedFile();
			DistanceUtils.serialise(wc.getPosAssignment(), file.getAbsolutePath(), false);
		}
		
		
		
	}

	public static void loadDialoge(WrapperClass wc)
	{
		final JFileChooser f = new JFileChooser(FolderLocations.resourceAnnealingPosition);
		f.setDialogTitle("Choose ");
		f.setMultiSelectionEnabled(false);
		f.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final int userSelection = f.showDialog(null, "select");

		if (userSelection == JFileChooser.APPROVE_OPTION)
		{
			final File file = f.getSelectedFile();
			try
			{
				Object object = DistanceUtils.deserialise(file.getAbsolutePath());
			
				@SuppressWarnings("unchecked")
				HashMap<LudRul, Pos> posAssignment = (HashMap<LudRul, Pos>) object;
				wc.overridePosAssignment(posAssignment);
			} catch (Exception e)
			{
				// TODO: handle exception
			}
			
		}
		
		
	}

}
