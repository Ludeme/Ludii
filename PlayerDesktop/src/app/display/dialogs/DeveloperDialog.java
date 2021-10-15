package app.display.dialogs;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import app.PlayerApp;
import app.display.dialogs.util.DialogUtil;
import app.display.dialogs.util.JComboCheckBox;
import game.types.board.SiteType;
import game.util.directions.DirectionFacing;
import gnu.trove.iterator.TIntIterator;
import main.Constants;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Dialog that is used to display various Developer options.
 * 
 * @author Matthew.Stephenson and Eric.Piette
 */
public class DeveloperDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** X coordinate of the cell column. */
	private final int CELL_X = 27;

	/** X coordinate of the vertex column. */
	private final int VERTEX_X = 228;

	/** X coordinate of the edge column. */
	private final int EDGE_X = 440;

	/** Gap between two check boxes. */
	private final int GAP = 26;

	/** The init of Y coordinate for the check boxes. */
	private final int INIT_Y = 60;

	/** The size of the combo boxes. */
	private final int SIZE_COMBO_BOXES = 100;

	/** The indices of the pregeneration boxes according to its name. */
	private final Map<String, Integer> indexPregen = new HashMap<String, Integer>();

	//-------------------------------------------------------------------------

	/**
	 * Show the Dialog.
	 */
	public static void showDialog(final PlayerApp app)
	{
		try
		{
			final DeveloperDialog dialog = new DeveloperDialog(app);
			DialogUtil.initialiseSingletonDialog(dialog, "Developer Settings", null);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Init the map of the indices of the pregen.
	 */
	public void initMapIndexPregen()
	{
		indexPregen.put("Inner", Integer.valueOf(0));
		indexPregen.put("Outer", Integer.valueOf(1));
		indexPregen.put("Perimeter", Integer.valueOf(2));
		indexPregen.put("Center", Integer.valueOf(3));
		indexPregen.put("Major", Integer.valueOf(4));
		indexPregen.put("Minor", Integer.valueOf(5));

		indexPregen.put("Corners", Integer.valueOf(7));
		indexPregen.put("Corners Concave", Integer.valueOf(8));
		indexPregen.put("Corners Convex", Integer.valueOf(9));

		indexPregen.put("Top", Integer.valueOf(11));
		indexPregen.put("Bottom", Integer.valueOf(12));
		indexPregen.put("Left", Integer.valueOf(13));
		indexPregen.put("Right", Integer.valueOf(14));

		indexPregen.put("Phases", Integer.valueOf(16));

		indexPregen.put("Side", Integer.valueOf(18));

		indexPregen.put("Col", Integer.valueOf(20));
		indexPregen.put("Row", Integer.valueOf(21));

		indexPregen.put("Neighbours", Integer.valueOf(23));
		indexPregen.put("Radials", Integer.valueOf(24));
		indexPregen.put("Distance", Integer.valueOf(25));

		indexPregen.put("Axial", Integer.valueOf(20));
		indexPregen.put("Horizontal", Integer.valueOf(21));
		indexPregen.put("Vertical", Integer.valueOf(22));
		indexPregen.put("Angled", Integer.valueOf(23));
		indexPregen.put("Slash", Integer.valueOf(24));
		indexPregen.put("Slosh", Integer.valueOf(25));
	}

	//------------------------------------------------------------------------------------------------------

	/**
	 * Create the dialog.
	 */
	public DeveloperDialog(final PlayerApp app)
	{
		initMapIndexPregen();
		setBounds(100, 100, 1200, indexPregen.values().size() * (int) (GAP * 1.2));
		getContentPane().setLayout(new BorderLayout());
		final JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		final JLabel lblNewLabel = new JLabel("Pregeneration visuals");
		lblNewLabel.setBounds(27, 11, 331, 15);
		contentPanel.add(lblNewLabel);

		final Topology topology = app.contextSnapshot().getContext(app).board().topology();

		makeColumnCell(app, contentPanel, topology);
		makeColumnVertex(app, contentPanel, topology);
		makeColumnEdge(app, contentPanel, topology);
		makeColumnOther(app, contentPanel, topology);
	}

	//-----------------------------------------------------------------------------------------
	
	/**
	 * Make the column of the cells.
	 */
	public void makeColumnCell(final PlayerApp app, final JPanel contentPanel, final Topology topology)
	{
		final JLabel label = new JLabel("Cells");
		label.setBounds(37, 37, 104, 15);
		contentPanel.add(label);
		
		final JCheckBox checkBox_Corners = checkBox(contentPanel, CELL_X, INIT_Y, "Corners",
				app.bridge().settingsVC().drawCornerCells());
		checkBox_Corners.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCornerCells(checkBox_Corners.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Corners_Concave = checkBox(contentPanel, CELL_X, INIT_Y, "Corners Concave",
				app.bridge().settingsVC().drawCornerConcaveCells());
		checkBox_Corners_Concave.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCornerConcaveCells(checkBox_Corners_Concave.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Corners_Convex = checkBox(contentPanel, CELL_X, INIT_Y, "Corners Convex",
				app.bridge().settingsVC().drawCornerConvexCells());
		checkBox_Corners_Convex.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCornerConvexCells(checkBox_Corners_Convex.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Major = checkBox(contentPanel, CELL_X, INIT_Y, "Major", app.bridge().settingsVC().drawMajorCells());
		checkBox_Major.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawMajorCells(checkBox_Major.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Minor = checkBox(contentPanel, CELL_X, INIT_Y, "Minor", app.bridge().settingsVC().drawMinorCells());
		checkBox_Minor.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawMinorCells(checkBox_Minor.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Perimeter = checkBox(contentPanel, CELL_X, INIT_Y, "Perimeter",
				app.bridge().settingsVC().drawPerimeterCells());
		checkBox_Perimeter.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawPerimeterCells(checkBox_Perimeter.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Outer = checkBox(contentPanel, CELL_X, INIT_Y, "Outer",
				app.bridge().settingsVC().drawOuterCells());
		checkBox_Outer.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawOuterCells(checkBox_Outer.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Inner = checkBox(contentPanel, CELL_X, INIT_Y, "Inner",
				app.bridge().settingsVC().drawInnerCells());
		checkBox_Inner.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawInnerCells(checkBox_Inner.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Top = checkBox(contentPanel, CELL_X, INIT_Y, "Top", app.bridge().settingsVC().drawTopCells());
		checkBox_Top.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawTopCells(checkBox_Top.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Bottom = checkBox(contentPanel, CELL_X, INIT_Y, "Bottom",
				app.bridge().settingsVC().drawBottomCells());
		checkBox_Bottom.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawBottomCells(checkBox_Bottom.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Left = checkBox(contentPanel, CELL_X, INIT_Y, "Left", app.bridge().settingsVC().drawLeftCells());
		checkBox_Left.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawLeftCells(checkBox_Left.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Right = checkBox(contentPanel, CELL_X, INIT_Y, "Right",
				app.bridge().settingsVC().drawRightCells());
		checkBox_Right.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawRightCells(checkBox_Right.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Center = checkBox(contentPanel, CELL_X, INIT_Y, "Center",
				app.bridge().settingsVC().drawCenterCells());
		checkBox_Center.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCenterCells(checkBox_Center.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Phases = checkBox(contentPanel, CELL_X, INIT_Y, "Phases",
				app.bridge().settingsVC().drawPhasesCells());
		checkBox_Phases.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawPhasesCells(checkBox_Phases.isSelected());
				app.repaint();
			}
		});

		final JComboCheckBox comboCheckBox_Column = comboCheckBox(contentPanel, CELL_X, INIT_Y, "Col",
				app.bridge().settingsVC().drawColumnsCells(), topology.columns(SiteType.Cell));
		if (comboCheckBox_Column != null)
		{
			comboCheckBox_Column.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							for (int i = 0; i < comboCheckBox_Column.getItemCount(); i++)
							{
								app.bridge().settingsVC().drawColumnsCells().set(i,
										Boolean.valueOf(((JCheckBox) comboCheckBox_Column.getItemAt(i)).isSelected()));
							}
							app.repaint();
						}
					});

				}
			});
		}

		final JComboCheckBox comboCheckBox_Row = comboCheckBox(contentPanel, CELL_X, INIT_Y, "Row",
				app.bridge().settingsVC().drawRowsCells(), topology.rows(SiteType.Cell));
		if (comboCheckBox_Row != null)
		{
			comboCheckBox_Row.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							for (int i = 0; i < comboCheckBox_Row.getItemCount(); i++)
							{
								app.bridge().settingsVC().drawRowsCells().set(i,
										Boolean.valueOf(((JCheckBox) comboCheckBox_Row.getItemAt(i)).isSelected()));
							}
							app.repaint();
						}
					});

				}
			});
		}

		if (topology.sides(SiteType.Cell).size() > 0)
		{
			final Vector<JCheckBox> v = new Vector<JCheckBox>();
			for (final DirectionFacing d : topology.sides(SiteType.Cell).keySet())
			{
				app.bridge().settingsVC().drawSideCells().put(d.uniqueName().toString(), Boolean.valueOf(false));
				final JCheckBox tempCheckBox = new JCheckBox(d.uniqueName().toString(), false);
				tempCheckBox.setSelected(app.bridge().settingsVC().drawSideCells().get(d.uniqueName().toString()).booleanValue());
				v.add(tempCheckBox);
			}
		    final JComboCheckBox directionLimitOptions = new JComboCheckBox(v);
		    directionLimitOptions.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							for (int i = 0; i < directionLimitOptions.getItemCount(); i++)
							{
								app.bridge().settingsVC().drawSideCells().put(((JCheckBox) directionLimitOptions.getItemAt(i)).getText(), Boolean.valueOf(((JCheckBox) directionLimitOptions.getItemAt(i)).isSelected()));
							}
							app.repaint();
						}
					});
					
				}
			});
			directionLimitOptions.setBounds(CELL_X, INIT_Y + GAP * indexPregen.get("Side").intValue(),
					SIZE_COMBO_BOXES, 23);
			contentPanel.add(directionLimitOptions);
		}
		
		final JCheckBox checkBox_Neighbours = checkBox(contentPanel, CELL_X, INIT_Y, "Neighbours", app.bridge().settingsVC().drawNeighboursCells());
		checkBox_Neighbours.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawNeighboursCells(checkBox_Neighbours.isSelected());
			}
		});

		
		final JCheckBox checkBox_Radials = checkBox(contentPanel, CELL_X, INIT_Y, "Radials",
				app.bridge().settingsVC().drawRadialsCells());
		checkBox_Radials.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawRadialsCells(checkBox_Radials.isSelected());
			}
		});

		final JCheckBox checkBox_Distance = checkBox(contentPanel, CELL_X, INIT_Y, "Distance",
				app.bridge().settingsVC().drawDistanceCells());
		checkBox_Distance.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawDistanceCells(checkBox_Distance.isSelected());
			}
		});
	}

	//-----------------------------------------------------------------------------------------
	
	/**
	 * Make the column of the vertices.
	 */
	public void makeColumnVertex(final PlayerApp app, final JPanel contentPanel, final Topology topology)
	{
		final JLabel label = new JLabel("Vertices");
		label.setBounds(249, 37, 104, 15);
		contentPanel.add(label);
		
		final JCheckBox checkBox_Corners = checkBox(contentPanel, VERTEX_X, INIT_Y, "Corners",
				app.bridge().settingsVC().drawCornerVertices());
		checkBox_Corners.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCornerVertices(checkBox_Corners.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Corners_Concave = checkBox(contentPanel, VERTEX_X, INIT_Y, "Corners Concave",
				app.bridge().settingsVC().drawCornerConcaveVertices());
		checkBox_Corners_Concave.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCornerConcaveVertices(checkBox_Corners_Concave.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Corners_Convex = checkBox(contentPanel, VERTEX_X, INIT_Y, "Corners Convex",
				app.bridge().settingsVC().drawCornerConvexVertices());
		checkBox_Corners_Convex.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCornerConvexVertices(checkBox_Corners_Convex.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Major = checkBox(contentPanel, VERTEX_X, INIT_Y, "Major",
				app.bridge().settingsVC().drawMajorVertices());
		checkBox_Major.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawMajorVertices(checkBox_Major.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Minor = checkBox(contentPanel, VERTEX_X, INIT_Y, "Minor",
				app.bridge().settingsVC().drawMinorVertices());
		checkBox_Minor.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawMinorVertices(checkBox_Minor.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Perimeter = checkBox(contentPanel, VERTEX_X, INIT_Y, "Perimeter",
				app.bridge().settingsVC().drawPerimeterVertices());
		checkBox_Perimeter.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawPerimeterVertices(checkBox_Perimeter.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Outer = checkBox(contentPanel, VERTEX_X, INIT_Y, "Outer",
				app.bridge().settingsVC().drawOuterVertices());
		checkBox_Outer.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawOuterVertices(checkBox_Outer.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Inner = checkBox(contentPanel, VERTEX_X, INIT_Y, "Inner",
				app.bridge().settingsVC().drawInnerVertices());
		checkBox_Inner.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawInnerVertices(checkBox_Inner.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Top = checkBox(contentPanel, VERTEX_X, INIT_Y, "Top", app.bridge().settingsVC().drawTopVertices());
		checkBox_Top.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawTopVertices(checkBox_Top.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Bottom = checkBox(contentPanel, VERTEX_X, INIT_Y, "Bottom",
				app.bridge().settingsVC().drawBottomVertices());
		checkBox_Bottom.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawBottomVertices(checkBox_Bottom.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Left = checkBox(contentPanel, VERTEX_X, INIT_Y, "Left", app.bridge().settingsVC().drawLeftVertices());
		checkBox_Left.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawLeftVertices(checkBox_Left.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Right = checkBox(contentPanel, VERTEX_X, INIT_Y, "Right",
				app.bridge().settingsVC().drawRightVertices());
		checkBox_Right.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawRightVertices(checkBox_Right.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Center = checkBox(contentPanel, VERTEX_X, INIT_Y, "Center",
				app.bridge().settingsVC().drawCenterVertices());
		checkBox_Center.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCenterVertices( checkBox_Center.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Phases = checkBox(contentPanel, VERTEX_X, INIT_Y, "Phases",
				app.bridge().settingsVC().drawPhasesVertices());
		checkBox_Phases.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawPhasesVertices(checkBox_Phases.isSelected());
				app.repaint();
			}
		});

		final JComboCheckBox comboCheckBox_Column = comboCheckBox(contentPanel, VERTEX_X, INIT_Y, "Col",
				app.bridge().settingsVC().drawColumnsVertices(), topology.columns(SiteType.Vertex));
		if (comboCheckBox_Column != null)
		{
			comboCheckBox_Column.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							for (int i = 0; i < comboCheckBox_Column.getItemCount(); i++)
							{
								app.bridge().settingsVC().drawColumnsVertices().set(i,
										Boolean.valueOf(((JCheckBox) comboCheckBox_Column.getItemAt(i)).isSelected()));
							}
							app.repaint();
						}
					});

				}
			});
		}

		final JComboCheckBox comboCheckBox_Row = comboCheckBox(contentPanel, VERTEX_X, INIT_Y, "Row",
				app.bridge().settingsVC().drawRowsVertices(), topology.rows(SiteType.Vertex));
		if (comboCheckBox_Row != null)
		{
			comboCheckBox_Row.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							for (int i = 0; i < comboCheckBox_Row.getItemCount(); i++)
							{
								app.bridge().settingsVC().drawRowsVertices().set(i,
										Boolean.valueOf(((JCheckBox) comboCheckBox_Row.getItemAt(i)).isSelected()));
							}
							app.repaint();
						}
					});

				}
			});
		}

		if (topology.sides(SiteType.Vertex).size() > 0)
		{
			final Vector<JCheckBox> v = new Vector<JCheckBox>();
			for (final DirectionFacing d : topology.sides(SiteType.Vertex).keySet())
			{
				app.bridge().settingsVC().drawSideVertices().put(d.uniqueName().toString(), Boolean.valueOf(false));
				final JCheckBox tempCheckBox = new JCheckBox(d.uniqueName().toString(), false);
				tempCheckBox.setSelected(
						app.bridge().settingsVC().drawSideVertices().get(d.uniqueName().toString()).booleanValue());
				v.add(tempCheckBox);
			}
			final JComboCheckBox directionLimitOptions = new JComboCheckBox(v);
			directionLimitOptions.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							for (int i = 0; i < directionLimitOptions.getItemCount(); i++)
							{
								app.bridge().settingsVC().drawSideVertices().put(
										((JCheckBox) directionLimitOptions.getItemAt(i)).getText(),
										Boolean.valueOf(((JCheckBox) directionLimitOptions.getItemAt(i)).isSelected()));
							}
							app.repaint();
						}
					});

				}
			});
			directionLimitOptions.setBounds(VERTEX_X, INIT_Y + GAP * indexPregen.get("Side").intValue(),
					SIZE_COMBO_BOXES, 23);
			contentPanel.add(directionLimitOptions);
		}
		
		final JCheckBox checkBox_Neighbours = checkBox(contentPanel, VERTEX_X, INIT_Y, "Neighbours", app.bridge().settingsVC().drawNeighboursVertices());
		checkBox_Neighbours.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawNeighboursVertices(checkBox_Neighbours.isSelected());
			}
		});

		final JCheckBox checkBox_Radials = checkBox(contentPanel, VERTEX_X, INIT_Y, "Radials",
				app.bridge().settingsVC().drawRadialsVertices());
		checkBox_Radials.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawRadialsVertices(checkBox_Radials.isSelected());
			}
		});

		final JCheckBox checkBox_Distance = checkBox(contentPanel, VERTEX_X, INIT_Y, "Distance",
				app.bridge().settingsVC().drawDistanceVertices());
		checkBox_Distance.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawDistanceVertices(checkBox_Distance.isSelected());
			}
		});
	}
	
	//-----------------------------------------------------------------------------------------

	/**
	 * Make the column of the edges.
	 */
	public void makeColumnEdge(final PlayerApp app, final JPanel contentPanel, final Topology topology)
	{
		final JLabel label = new JLabel("Edges");
		label.setBounds(461, 37, 104, 15);
		contentPanel.add(label);

		final JCheckBox checkBox_Corners = checkBox(contentPanel, EDGE_X, INIT_Y, "Corners",
				app.bridge().settingsVC().drawCornerEdges());
		checkBox_Corners.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCornerEdges(checkBox_Corners.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Corners_Concave = checkBox(contentPanel, EDGE_X, INIT_Y, "Corners Concave",
				app.bridge().settingsVC().drawCornerConcaveEdges());
		checkBox_Corners_Concave.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCornerConcaveEdges(checkBox_Corners_Concave.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Corners_Convex = checkBox(contentPanel, EDGE_X, INIT_Y, "Corners Convex",
				app.bridge().settingsVC().drawCornerConvexEdges());
		checkBox_Corners_Convex.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCornerConvexEdges(checkBox_Corners_Convex.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Major = checkBox(contentPanel, EDGE_X, INIT_Y, "Major",
				app.bridge().settingsVC().drawMajorEdges());
		checkBox_Major.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawMajorEdges(checkBox_Major.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Minor = checkBox(contentPanel, EDGE_X, INIT_Y, "Minor",
				app.bridge().settingsVC().drawMinorEdges());
		checkBox_Minor.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawMinorEdges(checkBox_Minor.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Perimeter = checkBox(contentPanel, EDGE_X, INIT_Y, "Perimeter",
				app.bridge().settingsVC().drawPerimeterEdges());
		checkBox_Perimeter.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawPerimeterEdges(checkBox_Perimeter.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Outer = checkBox(contentPanel, EDGE_X, INIT_Y, "Outer",
				app.bridge().settingsVC().drawOuterEdges());
		checkBox_Outer.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawOuterEdges(checkBox_Outer.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Inner = checkBox(contentPanel, EDGE_X, INIT_Y, "Inner",
				app.bridge().settingsVC().drawInnerEdges());
		checkBox_Inner.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawInnerEdges(checkBox_Inner.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Top = checkBox(contentPanel, EDGE_X, INIT_Y, "Top", app.bridge().settingsVC().drawTopEdges());
		checkBox_Top.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawTopEdges(checkBox_Top.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Bottom = checkBox(contentPanel, EDGE_X, INIT_Y, "Bottom",
				app.bridge().settingsVC().drawBottomEdges());
		checkBox_Bottom.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawBottomEdges(checkBox_Bottom.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Left = checkBox(contentPanel, EDGE_X, INIT_Y, "Left", app.bridge().settingsVC().drawLeftEdges());
		checkBox_Left.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawLeftEdges(checkBox_Left.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Right = checkBox(contentPanel, EDGE_X, INIT_Y, "Right",
				app.bridge().settingsVC().drawRightEdges());
		checkBox_Right.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawRightEdges(checkBox_Right.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Center = checkBox(contentPanel, EDGE_X, INIT_Y, "Center",
				app.bridge().settingsVC().drawCentreEdges());
		checkBox_Center.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawCentreEdges(checkBox_Center.isSelected());
				app.repaint();
			}
		});

		final JCheckBox checkBox_Phases = checkBox(contentPanel, EDGE_X, INIT_Y, "Phases",
				app.bridge().settingsVC().drawPhasesEdges());
		checkBox_Phases.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawPhasesEdges(checkBox_Phases.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Axial = checkBox(contentPanel, EDGE_X, INIT_Y, "Axial",
				app.bridge().settingsVC().drawAxialEdges());
		checkBox_Axial.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawAxialEdges(checkBox_Axial.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Horizontal = checkBox(contentPanel, EDGE_X, INIT_Y, "Horizontal",
				app.bridge().settingsVC().drawHorizontalEdges());
		checkBox_Horizontal.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawHorizontalEdges(checkBox_Horizontal.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Vertical = checkBox(contentPanel, EDGE_X, INIT_Y, "Vertical",
				app.bridge().settingsVC().drawVerticalEdges());
		checkBox_Vertical.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawVerticalEdges(checkBox_Vertical.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Angled = checkBox(contentPanel, EDGE_X, INIT_Y, "Angled",
				app.bridge().settingsVC().drawAngledEdges());
		checkBox_Angled.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawAngledEdges(checkBox_Angled.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Slash = checkBox(contentPanel, EDGE_X, INIT_Y, "Slash",
				app.bridge().settingsVC().drawSlashEdges());
		checkBox_Slash.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawSlashEdges(checkBox_Slash.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox checkBox_Slosh = checkBox(contentPanel, EDGE_X, INIT_Y, "Slosh",
				app.bridge().settingsVC().drawSloshEdges());
		checkBox_Slosh.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawSloshEdges(checkBox_Slosh.isSelected());
				app.repaint();
			}
		});

		if (topology.sides(SiteType.Edge).size() > 0)
		{
			final Vector<JCheckBox> v = new Vector<JCheckBox>();
			for (final DirectionFacing d : topology.sides(SiteType.Edge).keySet())
			{
				app.bridge().settingsVC().drawSideEdges().put(d.uniqueName().toString(), Boolean.valueOf(false));
				final JCheckBox tempCheckBox = new JCheckBox(d.uniqueName().toString(), false);
				tempCheckBox
						.setSelected(app.bridge().settingsVC().drawSideEdges().get(d.uniqueName().toString()).booleanValue());
				v.add(tempCheckBox);
			}
			final JComboCheckBox directionLimitOptions = new JComboCheckBox(v);
			directionLimitOptions.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent e)
				{
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							for (int i = 0; i < directionLimitOptions.getItemCount(); i++)
							{
								app.bridge().settingsVC().drawSideEdges().put(
										((JCheckBox) directionLimitOptions.getItemAt(i)).getText(),
										Boolean.valueOf(((JCheckBox) directionLimitOptions.getItemAt(i)).isSelected()));
							}
							app.repaint();
						}
					});

				}
			});
			directionLimitOptions.setBounds(EDGE_X, INIT_Y + GAP * indexPregen.get("Side").intValue(), SIZE_COMBO_BOXES,
					23);
			contentPanel.add(directionLimitOptions);
		}
	}

	//-------------------------------------------------------------------------------------------------------

	/**
	 * Make the column to the right in the panel. TO REWRITE, NOT DONE FOR NOW.
	 * 
	 * @param contentPanel
	 * @param topology
	 */
	private static void makeColumnOther(final PlayerApp app, final JPanel contentPanel, final Topology topology)
	{
		final JTextField textFieldMaximumNumberOfTurns = new JTextField();
		textFieldMaximumNumberOfTurns.setColumns(10);

		textFieldMaximumNumberOfTurns.setBounds(970, 495, 86, 20);
		contentPanel.add(textFieldMaximumNumberOfTurns);
		textFieldMaximumNumberOfTurns.setText("" + app.contextSnapshot().getContext(app).game().getMaxMoveLimit());

		final JLabel lblNewLabel_1 = new JLabel("Maximum number of moves");
		lblNewLabel_1.setBounds(719, 498, 241, 14);
		contentPanel.add(lblNewLabel_1);

		final DocumentListener documentListenerMaxTurns = new DocumentListener()
		{
			@Override
			public void changedUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			@Override
			public void insertUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			@Override
			public void removeUpdate(final DocumentEvent documentEvent)
			{
				update(documentEvent);
			}

			private void update(final DocumentEvent documentEvent)
			{
				try
				{
					app.contextSnapshot().getContext(app).game()
							.setMaxMoveLimit(Integer.parseInt(textFieldMaximumNumberOfTurns.getText()));
				}
				catch (final Exception e)
				{
					// not an integer;
					app.contextSnapshot().getContext(app).game().setMaxMoveLimit(Constants.DEFAULT_MOVES_LIMIT);
				}

				app.repaint();
			}
		};

		textFieldMaximumNumberOfTurns.getDocument().addDocumentListener(documentListenerMaxTurns);

		final JCheckBox chckbxFacesofvertex = new JCheckBox("Faces of Vertices");
		chckbxFacesofvertex.setSelected(app.bridge().settingsVC().drawFacesOfVertices());
		chckbxFacesofvertex.setBounds(707, 60, 199, 23);
		contentPanel.add(chckbxFacesofvertex);
		chckbxFacesofvertex.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawFacesOfVertices(chckbxFacesofvertex.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox chckbxEdgesOfVertices = new JCheckBox("Edges of Vertices");
		chckbxEdgesOfVertices.setSelected(app.bridge().settingsVC().drawEdgesOfVertices());
		chckbxEdgesOfVertices.setBounds(707, 86, 199, 23);
		contentPanel.add(chckbxEdgesOfVertices);
		chckbxEdgesOfVertices.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawEdgesOfVertices(chckbxEdgesOfVertices.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox chckbxVerticesOfFaces = new JCheckBox("Vertices of Faces");
		chckbxVerticesOfFaces.setSelected(app.bridge().settingsVC().drawVerticesOfFaces());
		chckbxVerticesOfFaces.setBounds(707, 112, 199, 23);
		contentPanel.add(chckbxVerticesOfFaces);
		chckbxVerticesOfFaces.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawVerticesOfFaces(chckbxVerticesOfFaces.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox chckbxEdgesOfFaces = new JCheckBox("Edges of Faces");
		chckbxEdgesOfFaces.setSelected(app.bridge().settingsVC().drawEdgesOfFaces());
		chckbxEdgesOfFaces.setBounds(707, 138, 199, 23);
		contentPanel.add(chckbxEdgesOfFaces);
		chckbxEdgesOfFaces.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawEdgesOfFaces(chckbxEdgesOfFaces.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox chckbxVerticesOfEdges = new JCheckBox("Vertices of Edges");
		chckbxVerticesOfEdges.setSelected(app.bridge().settingsVC().drawVerticesOfEdges());
		chckbxVerticesOfEdges.setBounds(707, 165, 199, 23);
		contentPanel.add(chckbxVerticesOfEdges);
		chckbxVerticesOfEdges.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawVerticesOfEdges(chckbxVerticesOfEdges.isSelected());
				app.repaint();
			}
		});
		
		final JCheckBox chckbxFacesOfEdges = new JCheckBox("Faces of Edges");
		chckbxFacesOfEdges.setSelected(app.bridge().settingsVC().drawFacesOfEdges());
		chckbxFacesOfEdges.setBounds(707, 191, 199, 23);
		contentPanel.add(chckbxFacesOfEdges);
		chckbxFacesOfEdges.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.bridge().settingsVC().setDrawFacesOfEdges(chckbxFacesOfEdges.isSelected());
				app.repaint();
			}
		});
		
		
		final JLabel lblPendingValue = new JLabel();
		lblPendingValue.setBounds(719, 546, 241, 15);
		contentPanel.add(lblPendingValue);
		String allPendingValues = "";
		if (app.manager().ref().context().state().pendingValues() != null)
		{
			final TIntIterator it = app.manager().ref().context().state().pendingValues().iterator();
			while (it.hasNext())
			{
				allPendingValues += Integer.valueOf(it.next()) + ", ";
			}
		}
		lblPendingValue.setText("Pending Values: " + allPendingValues);
		
		final JLabel lblCounterValue = new JLabel();
		lblCounterValue.setBounds(719, 584, 241, 15);
		contentPanel.add(lblCounterValue);
		lblCounterValue.setText("Counter Value: " + app.manager().ref().context().state().counter());
		
		final JLabel lblRememberValue = new JLabel();
		lblRememberValue.setBounds(719, 624, 241, 15);
		contentPanel.add(lblRememberValue);
		lblRememberValue.setText("Remember Values: " + app.manager().ref().context().state().rememberingValues());

		final JLabel lblTempValue = new JLabel();
		lblTempValue.setBounds(719, 664, 241, 15);
		contentPanel.add(lblTempValue);
		lblTempValue.setText("Temp Value: " + app.manager().ref().context().state().temp());

	}

	//------------------------------------------------------------------------------------------------------

	/**
	 * @return A check box on the correct position.
	 */
	public JCheckBox checkBox
	(
			final JPanel contentPanel,
			final int x, 
			final int init_y,
			final String namePregen, 
			final boolean settingSelected
	)
	{
		final JCheckBox checkBox = new JCheckBox(namePregen);
		checkBox.setBounds(x, init_y + indexPregen.get(namePregen).intValue() * GAP, 199, 23);
		contentPanel.add(checkBox);
		checkBox.setSelected(settingSelected);
		return checkBox;
	}

	/**
	 * @return A combo check box on the correct position.
	 */
	public JComboCheckBox comboCheckBox
	(
			final JPanel contentPanel,
			final int x, 
			final int init_y,
			final String namePregen, 
			final ArrayList<Boolean> settingSelected,
			final List<List<TopologyElement>> graphElements
	)
	{
		JComboCheckBox comboCheckBox = null;
		
		if (graphElements.size() > 0)
		{
			final Vector<JCheckBox> v = new Vector<JCheckBox>();
			for (int i = 0; i < graphElements.size(); i++)
			{
				if (settingSelected.size() <= i)
				{
					settingSelected.add(Boolean.valueOf(false));
				}
				final JCheckBox tempCheckBox = new JCheckBox(namePregen + " " + i, false);
				tempCheckBox.setSelected(settingSelected.get(i).booleanValue());
				v.add(tempCheckBox);
			}
			comboCheckBox = new JComboCheckBox(v);
			comboCheckBox.setBounds(x, init_y + GAP * indexPregen.get(namePregen).intValue(), SIZE_COMBO_BOXES, 23);
			contentPanel.add(comboCheckBox);
		}
		
		return comboCheckBox;
	}
}
