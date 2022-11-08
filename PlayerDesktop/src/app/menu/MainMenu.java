package app.menu;

import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
//import javax.swing.ButtonGroup;
//import javax.swing.JCheckBoxMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import app.DesktopApp;
import app.PlayerApp;
import app.loading.MiscLoading;
import app.utils.PuzzleSelectionType;
import game.equipment.container.board.Track;
import game.types.play.RepetitionType;
import main.Constants;
import main.FileHandling;
import main.StringRoutines;
import main.options.GameOptions;
import main.options.Option;
import main.options.Ruleset;
import other.context.Context;

//--------------------------------------------------------

/**
 * The app's main menu.
 *
 * @author cambolbro and Eric.Piette
 */
public class MainMenu extends JMenuBar
{
	private static final long serialVersionUID = 1L;

	public static JMenu mainOptionsMenu;
	protected JMenu submenu;

	public JMenuItem showIndexOption;
	public JMenuItem showCoordinateOption;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public MainMenu(final PlayerApp app)
	{
		// No menu for exhibition app.
		if (app.settingsPlayer().usingExhibitionApp())
			return;
		
		final ActionListener al = app;
		final ItemListener il = app;
		
		JMenuItem menuItem;
		JCheckBoxMenuItem cbMenuItem;

		UIManager.put("Menu.font", new Font("Arial", Font.PLAIN, 16));
		UIManager.put("MenuItem.font", new Font("Arial", Font.PLAIN, 16));
		UIManager.put("CheckBoxMenuItem.font", new Font("Arial", Font.PLAIN, 16));
		UIManager.put("RadioButtonMenuItem.font", new Font("Arial", Font.PLAIN, 16));

		//---------------------------------------------------------------------
		// Ludii Menu
		
		JMenu menu = new JMenu("Ludii");
		this.add(menu);
		
		menuItem = new JMenuItem("Preferences");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.SHIFT_DOWN_MASK));
		menuItem.addActionListener(al);
		menu.add(menuItem);

		menuItem = new JMenuItem("Quit");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('Q', CTRL_DOWN_MASK));
		menuItem.addActionListener(al);
		menu.add(menuItem);

		//---------------------------------------------------------------------
		// File Menu

		if (app.manager().settingsNetwork().getActiveGameId() == 0)
		{
			menu = new JMenu("File");
			this.add(menu);
		
			menuItem = new JMenuItem("Load Game");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('L', CTRL_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);
		
			submenu = new JMenu("Load Recent");
			for (int i = 0; i < app.settingsPlayer().recentGames().length; i++)
			{
				if (app.settingsPlayer().recentGames()[i] == null)
				{
					break;
				}
				menuItem = new JMenuItem(app.settingsPlayer().recentGames()[i]);
				switch(i)
				{
				case 0: menuItem.setAccelerator(KeyStroke.getKeyStroke('1', ALT_DOWN_MASK)); break;
				case 1: menuItem.setAccelerator(KeyStroke.getKeyStroke('2', ALT_DOWN_MASK)); break;
				case 2: menuItem.setAccelerator(KeyStroke.getKeyStroke('3', ALT_DOWN_MASK)); break;
				case 3: menuItem.setAccelerator(KeyStroke.getKeyStroke('4', ALT_DOWN_MASK)); break;
				case 4: menuItem.setAccelerator(KeyStroke.getKeyStroke('5', ALT_DOWN_MASK)); break;
				case 5: menuItem.setAccelerator(KeyStroke.getKeyStroke('6', ALT_DOWN_MASK)); break;
				case 6: menuItem.setAccelerator(KeyStroke.getKeyStroke('7', ALT_DOWN_MASK)); break;
				case 7: menuItem.setAccelerator(KeyStroke.getKeyStroke('8', ALT_DOWN_MASK)); break;
				case 8: menuItem.setAccelerator(KeyStroke.getKeyStroke('9', ALT_DOWN_MASK)); break;
				case 9: menuItem.setAccelerator(KeyStroke.getKeyStroke('0', ALT_DOWN_MASK)); break;
				}
				menuItem.addActionListener(al);
				submenu.add(menuItem);
			}
			menu.add(submenu);
		
			menuItem = new JMenuItem("Load Game from File");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('F', CTRL_DOWN_MASK));
			menuItem.addActionListener(al);
			menuItem.setToolTipText("Load a game description from and external .lud file");
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Load Random Game");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menu.addSeparator();
	
			menuItem = new JMenuItem("Load Trial");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			menuItem.setAccelerator(KeyStroke.getKeyStroke('T', CTRL_DOWN_MASK));
	
			menuItem = new JMenuItem("Save Trial");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			menuItem.setAccelerator(KeyStroke.getKeyStroke('S', CTRL_DOWN_MASK));
			
			menu.addSeparator();
			
			menuItem = new JMenuItem("Create Game");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Editor (Packed)");
			menuItem.addActionListener(al);
			menu.add(menuItem);

			menuItem = new JMenuItem("Editor (Expanded)");
			menuItem.addActionListener(al);
			menu.add(menuItem);

			menuItem = new JMenuItem("Visual Editor (Beta)");
			menuItem.addActionListener(al);
			menu.add(menuItem);
		}

		//---------------------------------------------------------------------
		// Game Menu
		
		menu = new JMenu("Game");
		this.add(menu);
		
		if (app.manager().settingsNetwork().getActiveGameId() == 0)
		{
			menuItem = new JMenuItem("Restart");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('R', CTRL_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);

			menuItem = new JMenuItem("Random Move");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('M', CTRL_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);

			menuItem = new JMenuItem("Random Playout");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('P', CTRL_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);
		}
		else if (app.manager().settingsNetwork().getActiveGameId() != 0)
		{
			menuItem = new JMenuItem("Propose/Accept a Draw");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Resign Game");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Leave Game");
			menuItem.addActionListener(al);
			menu.add(menuItem);
		}

		menu.addSeparator();

		// Don't allow legal moves to be listed if playing an online game with hidden information.
		if (app.manager().settingsNetwork().getActiveGameId() == 0 || !app.contextSnapshot().getContext(app).game().hiddenInformation())
		{
			menuItem = new JMenuItem("List Legal Moves");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('L', InputEvent.SHIFT_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);
		}

		menu.addSeparator();
		
		menuItem = new JMenuItem("Game Screenshot");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.SHIFT_DOWN_MASK));
		menuItem.addActionListener(al);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Game Gif");
		menuItem.addActionListener(al);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Make QR Code");
		menuItem.addActionListener(al);
		menu.add(menuItem);

		if (app.manager().settingsNetwork().getActiveGameId() == 0)
		{
			menu.addSeparator();

			menuItem = new JMenuItem("Cycle Players");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('R', ALT_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);
	
			menu.addSeparator();
	
			menuItem = new JMenuItem("Test Ludeme");
			menuItem.addActionListener(al);
			menu.add(menuItem);
		
			menu.addSeparator();
				
			menuItem = new JMenuItem("Generate Grammar");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('G', CTRL_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);
	
			menuItem = new JMenuItem("Count Ludemes");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Game Description Length");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Game Description Length (All Games)");
			menuItem.addActionListener(al);
			menu.add(menuItem);
		}

		//---------------------------------------------------------------------
		// Navigation Menu

		if (app.manager().settingsNetwork().getActiveGameId() == 0)
		{
			menu = new JMenu("Navigation");
			this.add(menu);
	
			menuItem = new JMenuItem("Play/Pause");
			menuItem.setAccelerator(KeyStroke.getKeyStroke("SPACE"));
			menuItem.addActionListener(al);
			menu.add(menuItem);
	
			menu.addSeparator();
	
			menuItem = new JMenuItem("Previous Move");
			menuItem.setAccelerator(KeyStroke.getKeyStroke("LEFT"));
			menuItem.addActionListener(al);
			menu.add(menuItem);
	
			menuItem = new JMenuItem("Next Move");
			menuItem.setAccelerator(KeyStroke.getKeyStroke("RIGHT"));
			menuItem.addActionListener(al);
			menu.add(menuItem);
	
			menu.addSeparator();
	
			menuItem = new JMenuItem("Go To Start");
			menuItem.setAccelerator(KeyStroke.getKeyStroke("DOWN"));
			menuItem.addActionListener(al);
			menu.add(menuItem);
	
			menuItem = new JMenuItem("Go To End");
			menuItem.setAccelerator(KeyStroke.getKeyStroke("UP"));
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			if (app.contextSnapshot().getContext(app).game().hasSubgames())
			{
				menu.addSeparator();
				
				menuItem = new JMenuItem("Random Playout Instance");
				menuItem.addActionListener(al);
				menu.add(menuItem);
			}
			
			menu.addSeparator();
			
			menuItem = new JMenuItem("Pass");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('P', ALT_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);
		}

		//---------------------------------------------------------------------
		// Puzzle Menu

		if (app.contextSnapshot().getContext(app).game().isDeductionPuzzle())
		{
			menu = new JMenu("Puzzle");
			this.add(menu);

			submenu = new JMenu("Value Selection");

			JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("Automatic");
			rbMenuItem.setSelected(app.settingsPlayer().puzzleDialogOption() == PuzzleSelectionType.Automatic);
			rbMenuItem.addItemListener(il);
			submenu.add(rbMenuItem);

			rbMenuItem = new JRadioButtonMenuItem("Dialog");
			rbMenuItem.setSelected(app.settingsPlayer().puzzleDialogOption() == PuzzleSelectionType.Dialog);
			rbMenuItem.addItemListener(il);
			submenu.add(rbMenuItem);

			rbMenuItem = new JRadioButtonMenuItem("Cycle");
			rbMenuItem.setSelected(app.settingsPlayer().puzzleDialogOption() == PuzzleSelectionType.Cycle);
			rbMenuItem.addItemListener(il);
			submenu.add(rbMenuItem);

			menu.add(submenu);

			menu.addSeparator();

			cbMenuItem = new JCheckBoxMenuItem("Illegal Moves Allowed");
			cbMenuItem.setSelected(app.settingsPlayer().illegalMovesValid());
			cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('I', InputEvent.SHIFT_DOWN_MASK));
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);

			menu.addSeparator();

			cbMenuItem = new JCheckBoxMenuItem("Show Possible Values");
			cbMenuItem.setSelected(app.bridge().settingsVC().showCandidateValues());
			cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.SHIFT_DOWN_MASK));
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);
		}

		//---------------------------------------------------------------------
		// Analysis Menu

		if (app.manager().settingsNetwork().getActiveGameId() == 0)
		{
			menu = new JMenu("Analysis");
			this.add(menu);

			menuItem = new JMenuItem("Estimate Branching Factor");
			menuItem.addActionListener(al);
			menu.add(menuItem);

			menuItem = new JMenuItem("Estimate Game Length");
			menuItem.addActionListener(al);
			menu.add(menuItem);

			menuItem = new JMenuItem("Estimate Game Tree Complexity");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Estimate Game Tree Complexity (No State Repetition)");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menu.addSeparator();
			
			menuItem = new JMenuItem("Compare Agents");
			menuItem.addActionListener(al);
			menu.add(menuItem);

//			menu.addSeparator();
//			
//			menuItem = new JMenuItem("Prove Win");
//			menuItem.addActionListener(al);
//			menu.add(menuItem);
//			
//			menuItem = new JMenuItem("Prove Loss");
//			menuItem.addActionListener(al);
//			menu.add(menuItem);

			menu.addSeparator();

			menuItem = new JMenuItem("Evaluation Dialog");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menu.addSeparator();
			
			menuItem = new JMenuItem("Show Compilation Concepts");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menu.addSeparator();

			if (app.manager().settingsNetwork().getActiveGameId() == 0)
			{	
				menuItem = new JMenuItem("Time Random Playouts");
				menuItem.setAccelerator(KeyStroke.getKeyStroke('O', CTRL_DOWN_MASK));
				menuItem.addActionListener(al);
				menu.add(menuItem);
	
				menuItem = new JMenuItem("Time Random Playouts in Background");
				menuItem.setAccelerator(KeyStroke.getKeyStroke('R', InputEvent.SHIFT_DOWN_MASK));
				menuItem.addActionListener(al);
				menu.add(menuItem);
			}
			
			menu.addSeparator();
			
			menuItem = new JMenuItem("Duplicates Moves Test");
			menuItem.addActionListener(al);
			menu.add(menuItem);

			menu.addSeparator();
			
			submenu = new JMenu("Predict Best Agent (internal)");

			menuItem = new JMenuItem("Linear Regression (internal)");
			menuItem.addActionListener(al);
			submenu.add(menuItem);
			
			menu.add(submenu);
			
			if (DesktopApp.devJar)
			{
				final File file = new File("../../LudiiPrivate/DataMiningScripts/Sklearn/res/trainedModels");
				final String[] directories = file.list(new FilenameFilter() {
					  @Override
					  public boolean accept(final File current, final String name) {
					    return new File(current, name).isDirectory();
					  }
					});
				
				menu.addSeparator();
				
				//---------------------------------------------------------------------
				// Agent prediction
				
				submenu = new JMenu("Predict Best Agent (external)");
				
				final JMenu submenuAgentReg = new JMenu("Regression");
				final JMenu submenuAgentCla = new JMenu("Classification");
				
				final JMenu submenuAgentRegComp = new JMenu("Compilation");
				final JMenu submenuAgentClaComp = new JMenu("Compilation");
				final JMenu submenuAgentRegAll = new JMenu("All");
				final JMenu submenuAgentClaAll = new JMenu("All");
				
				submenuAgentReg.add(submenuAgentRegComp);
				submenuAgentReg.add(submenuAgentRegAll);
				submenuAgentCla.add(submenuAgentClaComp);
				submenuAgentCla.add(submenuAgentClaAll);
				
				submenu.add(submenuAgentReg);
				submenu.add(submenuAgentCla);

				if (directories != null)
				{
					for (final String s : directories)
					{
						if (s.contains("Agents"))
						{
							if (s.contains("Classification"))
							{
								if (s.contains("True"))
								{
									menuItem = new JMenuItem(s.split("-")[0]);
									menuItem.addActionListener(al);
									submenuAgentClaComp.add(menuItem);
								}
								else
								{
									menuItem = new JMenuItem(s.split("-")[0]);
									menuItem.addActionListener(al);
									submenuAgentClaAll.add(menuItem);
								}
							}
							else
							{
								if (s.contains("True"))
								{
									menuItem = new JMenuItem(s.split("-")[0]);
									menuItem.addActionListener(al);
									submenuAgentRegComp.add(menuItem);
								}
								else
								{
									menuItem = new JMenuItem(s.split("-")[0]);
									menuItem.addActionListener(al);
									submenuAgentRegAll.add(menuItem);
								}
							}
						}
					}
				}
				
				menu.add(submenu);
				
				//---------------------------------------------------------------------
				// Heuristic prediction
				
				submenu = new JMenu("Predict Best Heuristic (external)");

				final JMenu submenuHeuristicReg = new JMenu("Regression");
				final JMenu submenuHeuristicCla = new JMenu("Classification");
				
				final JMenu submenuHeuristicRegComp = new JMenu("Compilation");
				final JMenu submenuHeuristicClaComp = new JMenu("Compilation");
				final JMenu submenuHeuristicRegAll = new JMenu("All");
				final JMenu submenuHeuristicClaAll = new JMenu("All");
				
				submenuHeuristicReg.add(submenuHeuristicRegComp);
				submenuHeuristicReg.add(submenuHeuristicRegAll);
				submenuHeuristicCla.add(submenuHeuristicClaComp);
				submenuHeuristicCla.add(submenuHeuristicClaAll);
				
				submenu.add(submenuHeuristicReg);
				submenu.add(submenuHeuristicCla);
				
				if (directories != null)
				{
					for (final String s : directories)
					{
						if (s.contains("Heuristics"))
						{
							if (s.contains("Classification"))
							{
								if (s.contains("True"))
								{
									menuItem = new JMenuItem(s.split("-")[0]);
									menuItem.addActionListener(al);
									submenuHeuristicClaComp.add(menuItem);
								}
								else
								{
									menuItem = new JMenuItem(s.split("-")[0]);
									menuItem.addActionListener(al);
									submenuHeuristicClaAll.add(menuItem);
								}
							}
							else
							{
								if (s.contains("True"))
								{
									menuItem = new JMenuItem(s.split("-")[0]);
									menuItem.addActionListener(al);
									submenuHeuristicRegComp.add(menuItem);
								}
								else
								{
									menuItem = new JMenuItem(s.split("-")[0]);
									menuItem.addActionListener(al);
									submenuHeuristicRegAll.add(menuItem);
								}
							}
						}
					}
				}
				
				menu.add(submenu);
				
				//---------------------------------------------------------------------
				// Metric prediction
				
				submenu = new JMenu("Predict Metrics (external)");
				
				final JMenu submenuComp = new JMenu("Compilation");
				final JMenu submenuAll = new JMenu("All");
				
				if (directories != null)
				{
					for (final String s : directories)
					{
						if (s.contains("Metrics"))
						{
							if (s.contains("True"))
							{
								menuItem = new JMenuItem(s.split("-")[0]);
								menuItem.addActionListener(al);
								submenuComp.add(menuItem);
							}
							else
							{
								menuItem = new JMenuItem(s.split("-")[0]);
								menuItem.addActionListener(al);
								submenuAll.add(menuItem);
							}
						}
					}
				}
				
				submenu.add(submenuComp);
				submenu.add(submenuAll);
				
				menu.add(submenu);
			}
		}
		
//		//---------------------------------------------------------------------
//		// Generation menu
//		
//		if (app.manager().settingsNetwork().getActiveGameId() == 0)
//		{
//			menu = new JMenu("Generation");
//			this.add(menu);
//			
//			menuItem = new JMenuItem("Generate Random Game");
//			menuItem.addActionListener(al);
//			menu.add(menuItem);
//									
//			menuItem = new JMenuItem("Generate 1000 Random Games");
//			menuItem.addActionListener(al);
//			menu.add(menuItem);
//
//			if (DesktopApp.devJar)
//			{
//				menuItem = new JMenuItem("Generate 1 Game with Restrictions (dev)");
//				menuItem.addActionListener(al);
//				menu.add(menuItem);
//			}
//		}

		//---------------------------------------------------------------------
		// Options Menu

		boolean optionsFound = false;
		for (int o = 0; o < app.contextSnapshot().getContext(app).game().description().gameOptions().numCategories(); o++)
		{
			final List<Option> options = app.contextSnapshot().getContext(app).game().description().gameOptions().categories().get(o).options();
			if (!options.isEmpty())
				optionsFound = true;
		}

		if (optionsFound && app.manager().settingsNetwork().getActiveGameId()==0)
		{
			mainOptionsMenu = new JMenu("Options");
			this.add(mainOptionsMenu);
			updateOptionsMenu(app, app.contextSnapshot().getContext(app), mainOptionsMenu);
		}

		//---------------------------------------------------------------------
		// Network Menu

		menu = new JMenu("Remote");
		this.add(menu);

		menuItem = new JMenuItem("Remote Play");
		menuItem.addActionListener(al);
		menu.add(menuItem);
		
		menu.addSeparator();
		
		menuItem = new JMenuItem("Initialise Server Socket");
		menuItem.addActionListener(al);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Test Message Socket");
		menuItem.addActionListener(al);
		menu.add(menuItem);
		
		menu.addSeparator();
		
		menuItem = new JMenuItem("Select Move from String");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('S', ALT_DOWN_MASK));
		menuItem.addActionListener(al);
		menu.add(menuItem);

		//---------------------------------------------------------------------
		// Demos Menu
		
		if (app.manager().settingsNetwork().getActiveGameId() == 0)
		{
			menu = new JMenu("Demos");
			final String[] demos = findDemos();
			
			if (demos.length > 0)
			{
				this.add(menu);
				
				for (String demo : demos)
				{
					if (!demo.endsWith(".json"))
						continue;
					
					demo = demo.replaceAll(Pattern.quote("\\"), "/");
					
					if (demo.contains("/demos/"))
						demo = demo.substring(demo.indexOf("/demos/"));
					
					if (!demo.startsWith("/"))
						demo = "/" + demo;
					
					if (!demo.startsWith("/demos"))
						demo = "/demos" + demo;
		
					try (final InputStream inputStream = MainMenu.class.getResourceAsStream(demo))
					{
						final JSONObject json = new JSONObject(new JSONTokener(inputStream));
						final JSONObject jsonDemo = json.getJSONObject("Demo");
						final String demoName = jsonDemo.getString("Name");
		
						menuItem = new JMenuItem(demoName);
						menuItem.addActionListener(new ActionListener()
								{
									@Override
									public void actionPerformed(final ActionEvent e)
									{
										MiscLoading.loadDemo(app, jsonDemo);
									}
								});
						menu.add(menuItem);
					}
					catch (final JSONException e)
					{
						System.err.println("Warning: JSON parsing error for demo file: " + demo);
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}

		//---------------------------------------------------------------------
		// Developer Menu
		
		if 
		(
			app.settingsPlayer().devMode()
			&& 
			app.manager().settingsNetwork().getActiveGameId() == 0
		)
		{
		
			menu = new JMenu("Developer");
			this.add(menu);
	
			menuItem = new JMenuItem("Compile Game (Debug)");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Recompile Current Game");
			menuItem.addActionListener(al);
			menu.add(menuItem);

			menuItem = new JMenuItem("Expanded Description");
			menuItem.addActionListener(al);
			menu.add(menuItem);

			menuItem = new JMenuItem("Metadata Description");
			menuItem.addActionListener(al);
			menu.add(menuItem);
				
			menuItem = new JMenuItem("Generate Symbols");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('G', ALT_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);

			menuItem = new JMenuItem("Show Call Tree");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menu.addSeparator();
			
			menuItem = new JMenuItem("Rules in English");
			menuItem.addActionListener(al);
			menu.add(menuItem);
	
			menuItem = new JMenuItem("Game Manual Generation (Beta)");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
//			menu.addSeparator();
//			
//			menuItem = new JMenuItem("Advanced Distance Dialog");
//			menuItem.addActionListener(al);
//			menu.add(menuItem);
			
			menu.addSeparator();
	
			menuItem = new JMenuItem("Print Board Graph");
			menuItem.addActionListener(al);
			menu.add(menuItem);
	
			menuItem = new JMenuItem("Print Trajectories");
			menuItem.addActionListener(al);
			menu.add(menuItem);
	
			menu.addSeparator();
	
			menuItem = new JMenuItem("Jump to Move");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menu.addSeparator();
			
			cbMenuItem = new JCheckBoxMenuItem("Show dev tooltip");
			cbMenuItem.setSelected(app.settingsPlayer().cursorTooltipDev());
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);
			
			menu.addSeparator();
			
			cbMenuItem = new JCheckBoxMenuItem("Sandbox");
			cbMenuItem.setSelected(app.settingsPlayer().sandboxMode());
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);
			
			menuItem = new JMenuItem("Clear Board");
			menuItem.setEnabled(app.settingsPlayer().sandboxMode());
			menuItem.addActionListener(al);
			menu.add(menuItem);
	
			menu.addSeparator();
			
			if (DesktopApp.devJar)
			{
				menuItem = new JMenuItem("Export Thumbnails");
				menuItem.addActionListener(al);
				menu.add(menuItem);
		
				menuItem = new JMenuItem("Export All Thumbnails");
				menuItem.addActionListener(al);
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Export Thumbnails (ruleset)");
				menuItem.addActionListener(al);
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Export All Thumbnails (rulesets)");
				menuItem.addActionListener(al);
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Export Board Thumbnail");
				menuItem.addActionListener(al);
				menu.add(menuItem);
		
				menuItem = new JMenuItem("Export All Board Thumbnails");
				menuItem.addActionListener(al);
				menu.add(menuItem);
				
				menu.addSeparator();
			}
			
			app.settingsPlayer().setSwapRule(false);
			if (app.contextSnapshot().getContext(app).game().metaRules().usesSwapRule())
				app.settingsPlayer().setSwapRule(true);
	
			app.settingsPlayer().setNoRepetition(false);
			if (app.contextSnapshot().getContext(app).game().metaRules().repetitionType() == RepetitionType.Positional)
				app.settingsPlayer().setNoRepetition(true);
			
			app.settingsPlayer().setNoRepetitionWithinTurn(false);
			if (app.contextSnapshot().getContext(app).game().metaRules().repetitionType() == RepetitionType.PositionalInTurn)
				app.settingsPlayer().setNoRepetitionWithinTurn(true);
	
			if (app.manager().settingsNetwork().getActiveGameId() == 0)
			{
				cbMenuItem = new JCheckBoxMenuItem("Swap Rule");
				cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('J', InputEvent.SHIFT_DOWN_MASK));
				cbMenuItem.setSelected(app.settingsPlayer().swapRule());
				cbMenuItem.addItemListener(il);
				menu.add(cbMenuItem);
			}
	
			if (app.manager().settingsNetwork().getActiveGameId() == 0)
			{
				cbMenuItem = new JCheckBoxMenuItem("No Repetition Of Game State");
				cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('N', InputEvent.SHIFT_DOWN_MASK));
				cbMenuItem.setSelected(app.settingsPlayer().noRepetition());
				cbMenuItem.addItemListener(il);
				menu.add(cbMenuItem);
			}
			
			if (app.manager().settingsNetwork().getActiveGameId() == 0)
			{
				cbMenuItem = new JCheckBoxMenuItem("No Repetition Within A Turn");
				cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('W', InputEvent.SHIFT_DOWN_MASK));
				cbMenuItem.setSelected(app.settingsPlayer().noRepetitionWithinTurn());
				cbMenuItem.addItemListener(il);
				menu.add(cbMenuItem);
			}
			
			menu.addSeparator();
			
			cbMenuItem = new JCheckBoxMenuItem("Show Cell Indices");
			cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('I', ALT_DOWN_MASK));
			cbMenuItem.setSelected(app.bridge().settingsVC().showCellIndices());
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);

			cbMenuItem = new JCheckBoxMenuItem("Show Edge Indices");
			cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('E', ALT_DOWN_MASK));
			cbMenuItem.setSelected(app.bridge().settingsVC().showEdgeIndices());
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);

			cbMenuItem = new JCheckBoxMenuItem("Show Vertex Indices");
			cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('F', ALT_DOWN_MASK));
			cbMenuItem.setSelected(app.bridge().settingsVC().showVertexIndices());
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);
			
			cbMenuItem = new JCheckBoxMenuItem("Show Container Indices");
			cbMenuItem.setSelected(app.bridge().settingsVC().showContainerIndices());
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);

			menu.addSeparator();

			cbMenuItem = new JCheckBoxMenuItem("Show Cell Coordinates");
			cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('C', ALT_DOWN_MASK));
			cbMenuItem.setSelected(app.bridge().settingsVC().showCellCoordinates());
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);
			
			cbMenuItem = new JCheckBoxMenuItem("Show Edge Coordinates");
			cbMenuItem.setSelected(app.bridge().settingsVC().showEdgeCoordinates());
			cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('B', ALT_DOWN_MASK));
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);
			
			cbMenuItem = new JCheckBoxMenuItem("Show Vertex Coordinates");
			cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('W', ALT_DOWN_MASK));
			cbMenuItem.setSelected(app.bridge().settingsVC().showVertexCoordinates());
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);
			
			menu.addSeparator();
			
			menuItem = new JMenuItem("Print Working Directory");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menu.addSeparator();
			
			menuItem = new JMenuItem("Evaluate Heuristic");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menuItem = new JMenuItem("Evaluate Features");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			cbMenuItem = new JCheckBoxMenuItem("Print Move Features");
			cbMenuItem.setSelected(app.settingsPlayer().printMoveFeatures());
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);
			
			cbMenuItem = new JCheckBoxMenuItem("Print Move Feature Instances");
			cbMenuItem.setSelected(app.settingsPlayer().printMoveFeatureInstances());
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);
			
			menu.addSeparator();
			
			menuItem = new JMenuItem("Generate Random Game");
			menuItem.addActionListener(al);
			menu.add(menuItem);
									
			menuItem = new JMenuItem("Generate 1000 Random Games");
			menuItem.addActionListener(al);
			menu.add(menuItem);

			if (DesktopApp.devJar)
			{
				menuItem = new JMenuItem("Generate 1 Game with Restrictions (dev)");
				menuItem.addActionListener(al);
				menu.add(menuItem);
				
				menuItem = new JMenuItem("Contextual Distance");
				menuItem.addActionListener(al);
				menu.add(menuItem);
			}

			menu.addSeparator();
			
			menuItem = new JMenuItem("Reconstruction Dialog");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			menu.addSeparator();
			
			menuItem = new JMenuItem("More Developer Options");
			menuItem.addActionListener(al);
			menu.add(menuItem);
			
			MenuScroller.setScrollerFor(menu, 30, 50, 0, 0);
		}
		
		//---------------------------------------------------------------------
		// View Menu

		menu = new JMenu("View");
		this.add(menu);
		
		menuItem = new JMenuItem("Clear Status Panel");
		menuItem.addActionListener(al);
		menu.add(menuItem);
		
		menu.addSeparator();

		cbMenuItem = new JCheckBoxMenuItem("Show Board");
		cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('B', InputEvent.SHIFT_DOWN_MASK));
		cbMenuItem.setSelected(app.settingsPlayer().showBoard());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Show Pieces");
		cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('P', InputEvent.SHIFT_DOWN_MASK));
		cbMenuItem.setSelected(app.settingsPlayer().showPieces());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Show Graph");
		cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('G', InputEvent.SHIFT_DOWN_MASK));
		cbMenuItem.setSelected(app.settingsPlayer().showGraph());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);
		
		cbMenuItem = new JCheckBoxMenuItem("Show Cell Connections");
		cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('D', InputEvent.SHIFT_DOWN_MASK));
		cbMenuItem.setSelected(app.settingsPlayer().showConnections());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Show Axes");
		cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('A', InputEvent.SHIFT_DOWN_MASK));
		cbMenuItem.setSelected(app.settingsPlayer().showAxes());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);

		menu.addSeparator();

		cbMenuItem = new JCheckBoxMenuItem("Show Legal Moves");
		cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('M', ALT_DOWN_MASK));
		cbMenuItem.setSelected(app.bridge().settingsVC().showPossibleMoves());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Show Last Move");
		cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('L', ALT_DOWN_MASK));
		cbMenuItem.setSelected(app.settingsPlayer().showLastMove());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);
		
		cbMenuItem = new JCheckBoxMenuItem("Show Ending Moves");
		cbMenuItem.setSelected(app.settingsPlayer().showEndingMove());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);
		
		cbMenuItem = new JCheckBoxMenuItem("Show Repetitions");
		cbMenuItem.setSelected(app.manager().settingsManager().showRepetitions());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);

		menu.addSeparator();

		cbMenuItem = new JCheckBoxMenuItem("Show Indices");
		cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('I', CTRL_DOWN_MASK));
		cbMenuItem.setSelected(app.bridge().settingsVC().showIndices());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);
		
		cbMenuItem = new JCheckBoxMenuItem("Show Coordinates");
		cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('C', CTRL_DOWN_MASK));
		cbMenuItem.setSelected(app.bridge().settingsVC().showCoordinates());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);
		
		menu.addSeparator();

		cbMenuItem = new JCheckBoxMenuItem("Show Magnifying Glass");
		cbMenuItem.setSelected(app.settingsPlayer().showZoomBox());
		cbMenuItem.addItemListener(il);
		menu.add(cbMenuItem);

		if (app.manager().settingsNetwork().getActiveGameId() == 0)
		{
			menu.addSeparator();
			cbMenuItem = new JCheckBoxMenuItem("Show AI Distribution");
			cbMenuItem.setAccelerator(KeyStroke.getKeyStroke('A', ALT_DOWN_MASK));
			cbMenuItem.setSelected(app.settingsPlayer().showAIDistribution());
			cbMenuItem.addItemListener(il);
			menu.add(cbMenuItem);
		}

		submenu = new JMenu("Pick Tracks to show");

		if (app.contextSnapshot().getContext(app).board().tracks().size() > 0)
		{
			menu.addSeparator();
			submenu = new JMenu("Show Tracks");

			for (int trackNumber = 0; trackNumber < app.contextSnapshot().getContext(app).board().tracks().size(); trackNumber++)
			{
				final Track track = app.contextSnapshot().getContext(app).board().tracks().get(trackNumber);
				
				cbMenuItem = new JCheckBoxMenuItem("Show Track " + track.name());

				boolean trackFound = false;
				for (int i = 0; i < app.bridge().settingsVC().trackNames().size(); i++)
				{
					if (cbMenuItem.getText().equals(app.bridge().settingsVC().trackNames().get(i)))
					{
						cbMenuItem.setSelected(app.bridge().settingsVC().trackShown().get(i).booleanValue());
						trackFound = true;
						break;
					}
				}
				if (!trackFound)
				{
					app.bridge().settingsVC().trackNames().add(cbMenuItem.getText());
					app.bridge().settingsVC().trackShown().add(Boolean.valueOf(false));
				}
				
				// If our track number exceeds 9, we'd get weird keyboard shortcuts and maybe even duplicate ones
				if (trackNumber < 10)
					cbMenuItem.setAccelerator(KeyStroke.getKeyStroke((char)(trackNumber+'0'), InputEvent.SHIFT_DOWN_MASK));
				
				cbMenuItem.addItemListener(il);
				submenu.add(cbMenuItem);
			}
			menu.add(submenu);
		}

		if (app.manager().settingsNetwork().getActiveGameId() == 0)
		{
			menu.addSeparator();
	
			menuItem = new JMenuItem("View SVG");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('V', ALT_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);
	
			menuItem = new JMenuItem("Load SVG");
			menuItem.setAccelerator(KeyStroke.getKeyStroke('U', ALT_DOWN_MASK));
			menuItem.addActionListener(al);
			menu.add(menuItem);
		}
		
		menu.addSeparator();
		
		menuItem = new JMenuItem("Game Hashcode");
		menuItem.addActionListener(al);
		menu.add(menuItem);
		
		//---------------------------------------------------------------------
		// Help menu
		
		menu = new JMenu("Help");
		this.add(menu);
		
		menuItem = new JMenuItem("About");
		menuItem.setAccelerator(KeyStroke.getKeyStroke('H', CTRL_DOWN_MASK));
		menuItem.addActionListener(al);
		menu.add(menuItem);
	}

	//-------------------------------------------------------------------------

	/**
	 * Update the options menu listing with any options found for the current game.
	 * @param context
	 */
	public static void updateOptionsMenu(final PlayerApp app, final Context context, final JMenu optionsMenu)
	{
		if (optionsMenu != null)
		{
			optionsMenu.removeAll();
			
			final GameOptions gameOptions = context.game().description().gameOptions();
			final List<String> currentOptions = 
					gameOptions.allOptionStrings
					(
						app.manager().settingsManager().userSelections().selectedOptionStrings()
					);

			// List possible options
			for (int cat = 0; cat < gameOptions.numCategories(); cat++)
			{
				// Create a submenu for this option group
				final List<Option> options = gameOptions.categories().get(cat).options();
				if (options.isEmpty())
					continue;  // no options for this group

				final List<String> headings = options.get(0).menuHeadings();
				if (headings.size() < 2)
				{
					System.out.println("** Not enough headings for menu option group: " + headings);
					return;
				}

				final JMenu submenu = new JMenu(headings.get(0));
				optionsMenu.add(submenu);

				// Group the choices for this option group together
				final ButtonGroup group = new ButtonGroup();
				for (int i = 0; i < options.size(); i++)
				{
					final Option option = options.get(i);

					if (option.menuHeadings().size() < 2)
					{
						System.out.println("** Not enough headings for menu option: " + option.menuHeadings());
						return;
					}

					final JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(option.menuHeadings().get(1));
					rbMenuItem.setSelected(currentOptions.contains(StringRoutines.join("/", option.menuHeadings())));
					rbMenuItem.addItemListener(app);
					group.add(rbMenuItem);
					submenu.add(rbMenuItem);
				}
				
				MenuScroller.setScrollerFor(submenu, 20, 50, 0, 0);
			}
			
			// Auto-select ruleset if necessary
			if (app.manager().settingsManager().userSelections().ruleset() == Constants.UNDEFINED)
				app.manager().settingsManager().userSelections().setRuleset(context.game().description().autoSelectRuleset(currentOptions));
			
			// List predefined rulesets
			final List<Ruleset> rulesets = context.game().description().rulesets();
			if (rulesets != null && !rulesets.isEmpty())
			{
				optionsMenu.addSeparator();
			
				final ButtonGroup rulesetGroup = new ButtonGroup();
				
				for (int rs = 0; rs < rulesets.size(); rs++)
				{
					final Ruleset ruleset = rulesets.get(rs);
								
					if (!ruleset.optionSettings().isEmpty() && !ruleset.heading().contains("Incomplete"))	// Eric wants to hide unimplemented and incomplete rulesets
					{
						if (ruleset.variations().isEmpty())
						{
							final JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(ruleset.heading());
							rbMenuItem.setSelected(app.manager().settingsManager().userSelections().ruleset() == rs);
							rbMenuItem.addItemListener(app);
							rulesetGroup.add(rbMenuItem);
							optionsMenu.add(rbMenuItem);
						}
						else
						{
							final JMenu submenuRuleset = new JMenu(ruleset.heading());
							optionsMenu.add(submenuRuleset);

							final JRadioButtonMenuItem rbMenuDefaultItem = new JRadioButtonMenuItem("Default");
							rbMenuDefaultItem.setSelected(app.manager().settingsManager().userSelections().ruleset() == rs);
							rbMenuDefaultItem.addItemListener(app);
							rulesetGroup.add(rbMenuDefaultItem);
							submenuRuleset.add(rbMenuDefaultItem);
							
							// Group the choices for this option group together
							for (final String rulesetOptionName : ruleset.variations().keySet())
							{
								final JMenu rbMenuItem = new JMenu(rulesetOptionName);
								submenuRuleset.add(rbMenuItem);
								
								// Group the choices for this option group together
								final ButtonGroup rulesetOptionsGroup = new ButtonGroup();
								for (int i = 0; i < ruleset.variations().get(rulesetOptionName).size(); i++)
								{
									final String rulesetOption = ruleset.variations().get(rulesetOptionName).get(i);

									final JRadioButtonMenuItem rulesetOptionMenuItem = new JRadioButtonMenuItem(rulesetOption);
									rulesetOptionMenuItem.setSelected(currentOptions.contains(rulesetOptionName + "/" + rulesetOption));
									rulesetOptionMenuItem.addItemListener(app);
									rulesetOptionsGroup.add(rulesetOptionMenuItem);
									rbMenuItem.add(rulesetOptionMenuItem);
								}
							}
							
							MenuScroller.setScrollerFor(submenuRuleset, 20, 50, 0, 0);
						}
					}
				}
			}
			
			MenuScroller.setScrollerFor(optionsMenu, 20, 50, 0, 0);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Finds all demos in the Player module.
	 * @return List of our demos
	 */
	private static String[] findDemos()
	{
		// Try loading from JAR file
        String[] choices = FileHandling.getResourceListing(MainMenu.class, "demos/", ".json");
        if (choices == null)
        {
        	try
        	{
        		// Try loading from memory in IDE
        		// Start with known .json file
				final URL url = MainMenu.class.getResource("/demos/Hnefatafl - Common.json");
        		String path = new File(url.toURI()).getPath();
				path = path.substring(0, path.length() - "Hnefatafl - Common.json".length());

        		// Get the list of .json files in this directory and subdirectories
        		final List<String> names = new ArrayList<>();
        		visitFindDemos(path, names);

        		Collections.sort(names);
        		choices = names.toArray(new String[names.size()]);
        	}
        	catch (final URISyntaxException exception)
        	{
        		exception.printStackTrace();
        	}
        }
        return choices;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Recursive helper method for finding all demos
	 * @param path
	 * @param names
	 */
	private static void visitFindDemos(final String path, final List<String> names)
	{
        final File root = new File( path );
        final File[] list = root.listFiles();

        if (list == null)
        	return;

        for (final File file : list)
        {
            if (file.isDirectory())
            {
            	visitFindDemos(path + file.getName() + File.separator, names);
            }
            else
            {
				if (file.getName().contains(".json"))
				{
					// Add this demo name to the list of choices
					final String name = new String(file.getName());
					names.add(path.substring(path.indexOf(File.separator + "demos" + File.separator)) + name);
				}
            }
        }
    }
	
	//-------------------------------------------------------------------------

}
