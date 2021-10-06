package processing.visualisation_3d_scatter_plot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import common.DistanceMatrix;

import common.LudRul;
import processing.kmedoid.Clustering;


public interface ColorAssignment
{

	public Color getColor(LudRul game);

	public default float[] getHsb(LudRul game)
	{
		float[] hsb = new float[3];
		Color c = getColor(game);
		return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);

	}

	public HashMap<String, Color> getLegend();

	public String getClass(LudRul r);

	public HashMap<String, HashSet<LudRul>> getClassifiedAs();

	@Override
	public String toString();

	public static ColorAssignment getAssignmentByFolder(List<LudRul> candidates)
	{
		ArrayList<String> folders = new ArrayList<>();
		HashMap<String, Color> folderToColor = new HashMap<>();

		HashMap<String, HashSet<LudRul>> classifiedAs = new HashMap<>();
		for (LudRul ludRul : candidates)
		{
			if (!folders.contains(ludRul.getCurrentClassName()))
			{
				folders.add(ludRul.getCurrentClassName());
			}
			HashSet<LudRul> set = classifiedAs
					.get(ludRul.getCurrentClassName());
			if (set == null)
			{
				set = new HashSet<LudRul>();
				classifiedAs.put(ludRul.getCurrentClassName(), set);
			}
			set.add(ludRul);

		}

		for (int i = 0; i < folders.size(); i++)
		{
			final float angle = (3f / 4f
					+ ((float) i + 1) / (folders.size() + 1)) % 1f;

			final Color cc = Color.getHSBColor(angle, 1.f, 1.0f);
			folderToColor.put(folders.get(i), cc);
		}

		return new ColorAssignment()
		{

			@Override
			public Color getColor(LudRul game)
			{

				return folderToColor.get(game.getCurrentClassName());
			}

			@Override
			public String toString()
			{

				return "Folder";
			}

			@Override
			public HashMap<String, Color> getLegend()
			{
				return folderToColor;
			}

			@Override
			public HashMap<String, HashSet<LudRul>> getClassifiedAs()
			{

				return classifiedAs;
			}

			@Override
			public String getClass(LudRul r)
			{
				return r.getCurrentClassName();
			}

			@Override
			public void overwrite(String key, Color newColor)
			{
				folderToColor.put(key, newColor);

			}

		};
	}
	public static ColorAssignment getAssignmentByMathewFolder(List<LudRul> candidates)
	{
		ArrayList<String> folders = new ArrayList<>();
		HashMap<String, Color> folderToColor = new HashMap<>();

		HashMap<String, HashSet<LudRul>> classifiedAs = new HashMap<>();
		for (LudRul ludRul : candidates)
		{
			if (!folders.contains(ludRul.getMatthewClassName()))
			{
				folders.add(ludRul.getMatthewClassName());
			}
			HashSet<LudRul> set = classifiedAs
					.get(ludRul.getMatthewClassName());
			if (set == null)
			{
				set = new HashSet<LudRul>();
				classifiedAs.put(ludRul.getMatthewClassName(), set);
			}
			set.add(ludRul);

		}

		for (int i = 0; i < folders.size(); i++)
		{
			final float angle = (3f / 4f
					+ ((float) i + 1) / (folders.size() + 1)) % 1f;

			final Color cc = Color.getHSBColor(angle, 1.f, 1.0f);
			folderToColor.put(folders.get(i), cc);
		}

		return new ColorAssignment()
		{

			@Override
			public Color getColor(LudRul game)
			{

				return folderToColor.get(game.getMatthewClassName());
			}

			@Override
			public String toString()
			{

				return "Matthew";
			}

			@Override
			public HashMap<String, Color> getLegend()
			{
				return folderToColor;
			}

			@Override
			public HashMap<String, HashSet<LudRul>> getClassifiedAs()
			{

				return classifiedAs;
			}

			@Override
			public String getClass(LudRul r)
			{
				return r.getMatthewClassName();
			}

			@Override
			public void overwrite(String key, Color newColor)
			{
				folderToColor.put(key, newColor);

			}

		};
	}


	public static ColorAssignment getAssignmentByClustering(
			ArrayList<LudRul> candidates, Clustering clustering,
			DistanceMatrix<LudRul, LudRul> distanceMatrix
	)
	{
		HashMap<LudRul, String> cma = clustering.getMedoidAssignment(candidates,
				distanceMatrix);
		LudRul[] med = clustering.getMedoid();
		HashSet<LudRul> medoids = new HashSet<>();
		for (LudRul ludRul : med)
		{
			medoids.add(ludRul);
		}
		HashMap<String, HashSet<LudRul>> classifiedAs = new HashMap<>();
		for (Entry<LudRul, String> entry : cma.entrySet())
		{
			HashSet<LudRul> list = classifiedAs.get(entry.getValue());
			if (list == null)
			{
				list = new HashSet<LudRul>();
				classifiedAs.put(entry.getValue(), list);
			}
			list.add(entry.getKey());
		}
		// int n = clustering.getMedoid().length;
		ArrayList<String> folders = new ArrayList<>();
		HashMap<String, Color> folderToColor = new HashMap<>();

		for (LudRul ludRul : candidates)
		{
			if (!folders.contains(cma.get(ludRul)))
			{
				folders.add(cma.get(ludRul));
			}
		}

		for (int i = 0; i < folders.size(); i++)
		{
			final float angle = (3f / 4f
					+ ((float) i + 1) / (folders.size() + 1)) % 1f;

			final Color cc = Color.getHSBColor(angle, 1.f, 1.0f);
			folderToColor.put(folders.get(i), cc);

		}

		return new ColorAssignment()
		{

			@Override
			public float[] getHsb(LudRul game)
			{
				float[] hsb = new float[3];
				Color c = getColor(game);
				Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
				if (medoids.contains(game))hsb[2]=hsb[2]*0.5f;
				return hsb;

			}
			
			@Override
			public Color getColor(LudRul game)
			{

				return folderToColor.get(cma.get(game));
			}

			@Override
			public String toString()
			{

				return "kMedoid " + clustering.getK();
			}

			@Override
			public HashMap<String, Color> getLegend()
			{
				return folderToColor;
			}

			@Override
			public HashMap<String, HashSet<LudRul>> getClassifiedAs()
			{
				return classifiedAs;
			}

			@Override
			public String getClass(LudRul r)
			{

				return cma.get(r);
			}

			@Override
			public void overwrite(String key, Color newColor)
			{
				folderToColor.put(key, newColor);

			}
		};

	}

	public static ColorAssignment getAssignmentByCorrectness(
			ArrayList<LudRul> candidates,
			HashMap<LudRul, String> classAssignment
	)
	{

		return new ColorAssignment()
		{
			Color cCor = new Color(0, 100, 0);
			Color cFalse = Color.RED;
			HashMap<String, Color> colorMap = new HashMap<>();
			{
				float[] hsbvalsCor = new float[3];
				float[] hsbvalsFal = new float[3];
				Color.RGBtoHSB(cCor.getRed(), cCor.getGreen(), cCor.getBlue(),
						hsbvalsCor);
				Color.RGBtoHSB(cFalse.getRed(), cFalse.getGreen(),
						cFalse.getBlue(), hsbvalsFal);

				colorMap.put("correct", cCor);
				colorMap.put("false", cFalse);
			}

			@Override
			public HashMap<String, Color> getLegend()
			{

				return colorMap;
			}

			@Override
			public Color getColor(LudRul game)
			{
				if (game.getCurrentClassName()
						.equals(classAssignment.get(game)))
				{
					return cCor;
				}
				return cFalse;
			}

			@Override
			public HashMap<String, HashSet<LudRul>> getClassifiedAs()
			{
				HashMap<String, HashSet<LudRul>> classified = new HashMap<>();
				HashSet<LudRul> hsCor = new HashSet<>();
				HashSet<LudRul> hsFal = new HashSet<>();
				classified.put("false", hsFal);
				classified.put("correct", hsCor);

				for (LudRul game : candidates)
				{
					if (game.getCurrentClassName()
							.equals(classAssignment.get(game)))
						hsCor.add(game);
					else
						hsFal.add(game);
				}
				return classified;
			}

			@Override
			public String getClass(LudRul game)
			{
				if (game.getCurrentClassName()
						.equals(classAssignment.get(game)))
					return "correct";

				return "false";
			}

			@Override
			public String toString()
			{
				return "Correctly Assigned";
			}

			@Override
			public void overwrite(String key, Color newColor)
			{

				colorMap.put(key, newColor);
				cCor = colorMap.get("correct");
				cFalse = colorMap.get("false");

			}
		};
	}

	public void overwrite(String key, Color newColor);

	public default Color getColor(LudRul ns, float brightness)
	{
		float[] hsb = getHsb(ns);
		Color c = Color.getHSBColor(hsb[0], hsb[1], hsb[2] * brightness);
		return c;
	}

	public static ColorAssignment getAssignmentByClassifier(
			ArrayList<LudRul> candidates,
			HashMap<LudRul, String> classAssignment,
			ColorAssignment assignmentByFolder
	)
	{
		return new ColorAssignment()
		{
			private HashMap<String, Color> folderToColor;
			private HashMap<LudRul, String> classification = classAssignment;
			private HashMap<String, HashSet<LudRul>> classifiedAs = new HashMap<>();

			{
				folderToColor = assignmentByFolder.getLegend();
				for (LudRul ludRul : candidates)
				{
					
					HashSet<LudRul> set = classifiedAs
							.get(classAssignment.get(ludRul));
					if (set == null)
					{
						set = new HashSet<LudRul>();
						classifiedAs.put(classAssignment.get(ludRul), set);
					}
					set.add(ludRul);
				}
			}
			@Override
			public Color getColor(LudRul game)
			{

				return folderToColor.get(classAssignment.get(game));
			}

			@Override
			public String toString()
			{

				return "Classified";
			}

			@Override
			public HashMap<String, Color> getLegend()
			{
				return folderToColor;
			}

			@Override
			public HashMap<String, HashSet<LudRul>> getClassifiedAs()
			{

				return classifiedAs;
			}

			@Override
			public String getClass(LudRul r)
			{
				
				return classification.get(r);
			}

			@Override
			public void overwrite(String key, Color newColor)
			{
				folderToColor.put(key, newColor);

			}
		};
	}
}
