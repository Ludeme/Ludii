package app.display.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import app.DesktopApp;
import main.AliasesData;
import main.FileHandling;

/**
 * Class for showing a dialog to choose a game to load.
 * 
 * @author Dennis Soemers, Matthew Stephenson
 */
public class GameLoaderDialog
{
	static String lastKeyPressed = "";
	static String oldSearchString = "";
	
	//-------------------------------------------------------------------------
	
	/**
	 * Shows a Game Loader dialog.
	 * 
	 * @param frame
	 * @param choices
	 * @param initialChoice
	 * @return Chosen game, or null if no choice made
	 */
	public static String showDialog
	(
		final JFrame frame, 
		final String[] choices, 
		final String initialChoice
	)
	{
		final JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		
		lastKeyPressed = "";	// reset this since it's static, shared between dialogs
		
		// convert our list of games into a tree
		final List<GameLoaderNode> leafNodes = new ArrayList<GameLoaderNode>();
		final Map<String, GameLoaderNode> nodesMap = new HashMap<String, GameLoaderNode>();
		final GameLoaderNode root = new GameLoaderNode("Games", "/lud/");
		
		for (final String choice : choices)
		{
			if (!DesktopApp.devJar && FileHandling.shouldIgnoreLudRelease(choice))
				continue;
			
			String str = choice.replaceAll(Pattern.quote("\\"), "/");
			if (str.startsWith("/"))
				str = str.substring(1);
			final String[] parts = str.split("/");
			
			if (!parts[0].equals("lud"))
				System.err.println("top level is not lud: " + parts[0]);
			
			String runningFullName = "/lud/";
			GameLoaderNode internalNode = root;
			
			for (int i = 1; i < parts.length - 1; ++i)
			{
				final GameLoaderNode nextInternal;
				runningFullName += parts[i] + "/";
				
				if (!nodesMap.containsKey(runningFullName))
				{
					nextInternal = new GameLoaderNode(parts[i], runningFullName);
					nodesMap.put(runningFullName, nextInternal);
					
					int childIdx = 0;
					while (childIdx < internalNode.getChildCount())
					{
						final GameLoaderNode existingChild = 
								(GameLoaderNode) internalNode.getChildAt(childIdx);
						final String name = (String) existingChild.getUserObject();
						
						if (existingChild.fullName.endsWith(".lud"))
						{
							// Our internal node should always go before .lud files
							break;
						}
						else if (parts[i].compareToIgnoreCase(name) < 0)
						{
							// Alphabetical ordering means we should go before this node
							break;
						}
						
						++childIdx;
					}
					internalNode.insert(nextInternal, childIdx);
				}
				else
				{
					nextInternal = nodesMap.get(runningFullName);
				}

				internalNode = nextInternal;
			}
			
			final GameLoaderNode leafNode = new GameLoaderNode(parts[parts.length - 1].substring(0, parts[parts.length - 1].length()-4), choice);
			nodesMap.put(choice, leafNode);
			leafNodes.add(leafNode);
			internalNode.add(leafNode);
		}
		
		final GameLoaderTree tree = new GameLoaderTree(root);
		
		// only allow selecting a single leaf, not multiple leaves
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		// select initial choice
		try
		{
			tree.setSelectionPath(new TreePath(nodesMap.get(initialChoice).getPath()));
		}
		catch (final Exception e)
		{
			// user probably has a puzzle or other non-online game loaded
		}
		
		// create our text field for filtering
		final JTextField filterField = new JTextField();
		filterField.setFont(new Font("Arial", Font.PLAIN, 20));

		final Font gainFont = new Font("Arial", Font.PLAIN, 20);  
		final Font lostFont = new Font("Arial", Font.PLAIN, 20);  
		final String hint = "Search Game";
		
		filterField.setText(hint);  
		filterField.setFont(lostFont);  
		filterField.setForeground(Color.GRAY);  
		
		tree.addFocusListener(new FocusAdapter() 
		{  
			@Override  
			public void focusGained(final FocusEvent e) 
			{  
				if (lastKeyPressed == "UP")
				{
					tree.setSelectionRow(tree.getLeadSelectionRow()-1);
					lastKeyPressed = "";
				}
				if (lastKeyPressed == "DOWN")
				{
					tree.setSelectionRow(tree.getLeadSelectionRow()+1);
					lastKeyPressed = "";
				}
			}  
		});  

		filterField.addFocusListener(new FocusAdapter() 
		{  

			@Override  
			public void focusGained(final FocusEvent e) 
			{  
				if (filterField.getText().equals(hint)) 
				{  
					filterField.setText("");  
					filterField.setFont(gainFont);  
				} 
				else 
				{  
					filterField.setText(filterField.getText());  
					filterField.setFont(gainFont);  
				}  
				
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						final String currentSearchString = filterField.getText();						
						if (oldSearchString.equals(currentSearchString) || (oldSearchString.equals("Search Game") && currentSearchString.equals("")))
						{
							if (!lastKeyPressed.equals(""))
							{
								filterField.setText(filterField.getText() + lastKeyPressed);
								lastKeyPressed = "";
							}
						}
					}
				});
				
				setTextColour(); 
			}  

			@Override  
			public void focusLost(final FocusEvent e) 
			{  
				if (filterField.getText().equals(hint) || filterField.getText().length() == 0) 
				{
					filterField.setText(hint); 
				}
				setTextColour(); 
			}  
			
			public void setTextColour()
			{
				if (filterField.getText().equals(hint)) 
				{  
					filterField.setFont(lostFont);  
					filterField.setForeground(Color.GRAY);  
				} 
				else 
				{  
					filterField.setFont(gainFont);  
					filterField.setForeground(Color.BLACK);  
				} 
			}
		});  
		
		filterField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(final DocumentEvent e)
			{
				handleEvent(e);
			}

			@Override
			public void removeUpdate(final DocumentEvent e)
			{
				handleEvent(e);
			}

			@Override
			public void changedUpdate(final DocumentEvent e)
			{
				handleEvent(e);
			}
			
			/**
			 * Handle any kind of change in text
			 * @param e
			 */
			public void handleEvent(final DocumentEvent e)
			{
				tree.updateTreeFilter(filterField);
				tree.revalidate();
			}
	
		});
		
		final JScrollPane treeView = new JScrollPane(tree);
		contentPane.add(treeView, BorderLayout.CENTER);
		contentPane.add(filterField, BorderLayout.SOUTH);
		contentPane.setPreferredSize(new Dimension(650, 700));
		
		// try to make our dialog resizable
		contentPane.addHierarchyListener(new HierarchyListener() 
		{
			@Override
			public void hierarchyChanged(final HierarchyEvent e) 
			{
				final Window window = SwingUtilities.getWindowAncestor(contentPane);
				if (window instanceof Dialog) 
				{
					final Dialog dialog = (Dialog) window;
					if (!dialog.isResizable()) 
						dialog.setResizable(true);
					
					dialog.setLocationRelativeTo(DesktopApp.frame());
					
					tree.requestFocus();
				}
			}
		});
		
		// give focus to our tree after the "Ok" button steals it
		tree.addFocusListener
		(
			new FocusListener()
			{
				/** Should only steal back once */
				private boolean isFirstTime = true;

				@Override
				public void focusGained(final FocusEvent e)
				{
					// Do nothing
				}

				@Override
				public void focusLost(final FocusEvent e)
				{
					if (isFirstTime)
					{
						tree.requestFocus();
						isFirstTime = false;
					}
				}
						
			}
		);

		// finally we can create our dialog
		final URL iconURL = DesktopApp.class.getResource("/ludii-logo-100x100.png");
		BufferedImage image = null;
		try
		{
			image = ImageIO.read(iconURL);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		final JOptionPane pane = 
				new JOptionPane
				(
					contentPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, 
					null, null, null
				);
		final JDialog dialog = pane.createDialog("Choose a Game to Load");
		dialog.setIconImage(image);
		
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setModal(true);
		
		final KeyEventDispatcher keyDispatcher = new KeyEventDispatcher() 
		{
			@Override
			public boolean dispatchKeyEvent(final KeyEvent e) 
			{
				if (e.getID() == KeyEvent.KEY_PRESSED) 
				{
					if (e.getKeyCode() == KeyEvent.VK_ENTER) 
					{
						pane.setValue(Integer.valueOf(JOptionPane.OK_OPTION));
	            		dialog.dispose();
					} 
					else if (e.getKeyCode() == KeyEvent.VK_UP) 
					{
						if (!tree.hasFocus())
						{
							lastKeyPressed = "UP";
							tree.requestFocus();
						}
					} 
					else if (e.getKeyCode() == KeyEvent.VK_DOWN) 
					{
						if (!tree.hasFocus())
						{
							lastKeyPressed = "DOWN";
							tree.requestFocus();
						}
					}
					else if 
					(
						e.getKeyCode() != KeyEvent.VK_LEFT && 
						e.getKeyCode() != KeyEvent.VK_RIGHT && 
						KeyEvent.getKeyText(e.getKeyCode()).length() == 1
					) 
					{
						if (!filterField.hasFocus())
						{
							oldSearchString = filterField.getText();
							lastKeyPressed = Character.toString(e.getKeyChar());
							filterField.requestFocus();	
							return true;
						}
					}
				}
				return false;
			}
		};

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);
		
		// Create mouse listener to allow double-click selection of games
		tree.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(final MouseEvent e) 
			{
		        if (e.getClickCount() == 2) 
		        {
		            final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
		            if (path != null) 
		            {
		            	final GameLoaderNode node = (GameLoaderNode) path.getLastPathComponent();
		            	if (node.fullName.endsWith(".lud"))
		            	{
		            		pane.setValue(Integer.valueOf(JOptionPane.OK_OPTION));
		            		dialog.dispose();
		            	}
		            }
		        }
		    }
		});
		
		// expand all nodes that have at least one non-leaf child
		final Enumeration<?> bfsEnumeration = root.breadthFirstEnumeration();
	    
		while (bfsEnumeration.hasMoreElements())
		{
			final GameLoaderNode node = (GameLoaderNode) bfsEnumeration.nextElement();
			
			final Enumeration<?> children = node.children();
			while (children.hasMoreElements())
			{
				final GameLoaderNode child = (GameLoaderNode) children.nextElement();
				if (!child.isLeaf())
				{
					tree.expandPath(new TreePath(node.getPath()));
					break;
				}
			}
		}

		// show the dialog
		dialog.setVisible(true);
		final Object selectedValue = pane.getValue();
		
		// get rid of our key event dispatcher again
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyDispatcher);
		
		final int result;
		
		if (selectedValue == null)
		{
            result = JOptionPane.CLOSED_OPTION;
		}
		else
		{
			if (selectedValue instanceof Integer)
				result = ((Integer)selectedValue).intValue();
			else
				result = JOptionPane.CLOSED_OPTION;
		}
		
		if (result == JOptionPane.OK_OPTION)
		{
			final TreePath treePath = tree.getSelectionPath();
			
			if (treePath != null)
			{
				final GameLoaderNode selectedLeaf = (GameLoaderNode) treePath.getLastPathComponent();
				
				if (!selectedLeaf.isLeaf())
					return null;
				
				return selectedLeaf.fullName;
			}
		}
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Custom class for the nodes in our tree
	 * 
	 * Filtering code based on: http://www.java2s.com/Code/Java/Swing-Components/InvisibleNodeTreeExample.htm
	 * 
	 * @author Dennis Soemers
	 */
	private static class GameLoaderNode extends DefaultMutableTreeNode
	{
		/** */
		private static final long serialVersionUID = 1L;
		
		/** Full name */
		public final String fullName;
		
		/** Aliases that we want to be searchable */
		protected final List<String> aliases;
		
		/** Whether or not it's visible (given current filter) */
		protected boolean isVisible = true;
		
		/**
		 * Constructor
		 * @param shortName
		 * @param fullName
		 */
		public GameLoaderNode(final String shortName, final String fullName)
		{
			super(shortName);
			this.fullName = fullName;
			
			final AliasesData aliasesData = AliasesData.loadData();
			final List<String> loadedAliases = aliasesData.aliasesForGame(this.fullName.replaceAll(Pattern.quote("\\"), "/"));
			
			if (loadedAliases != null)
			{
				aliases = new ArrayList<String>(loadedAliases.size());
				
				for (final String alias : loadedAliases)
				{
					aliases.add(
							alias
							.toLowerCase()
							.replaceAll(Pattern.quote("-"), "")
							.replaceAll(Pattern.quote(" "), "")
							.replaceAll(Pattern.quote("'"), "")
					);
				}
			}
			else
			{
				aliases = new ArrayList<String>();
			}
		}
		
		/**
		 * @return Child at given index (after filtering if filter == true).
		 */
		public TreeNode getChildAt(final int index, final boolean filter)
		{
			if (!filter)
				return super.getChildAt(index);
			
			int visibleIdx = -1;
			final Enumeration<?> e = children.elements();
			
			while (e.hasMoreElements())
			{
				final GameLoaderNode node = (GameLoaderNode) e.nextElement();
				
				if (node.isVisible)
					++visibleIdx;
				
				if (visibleIdx == index)
					return node;
			}
			
			throw new ArrayIndexOutOfBoundsException("index unmatched after filtering");
		}
		
		/**
		 * @return Number of child nodes (after filtering if filter == true)
		 */
		public int getChildCount(final boolean filter)
		{
			if (!filter)
				return super.getChildCount();
			
			int count = 0;
			
			try
			{
				final Enumeration<?> e = children.elements();
				while (e.hasMoreElements())
				{
					final GameLoaderNode node = (GameLoaderNode) e.nextElement();
					if (node.isVisible)
						++count;
				}
			}
			catch (final Exception e)
			{
				// carry on
			}
			
			return count;
		}
		
		/**
		 * Recursively updates visibility based on user's filter text
		 */
		public void updateVisibility(final String filterText)
		{
			if (isLeaf())
			{
				final String[] fullNameSplit = fullName.split(Pattern.quote("/"));
				final String gameName = fullNameSplit[fullNameSplit.length-1];
				isVisible = 
						(
							gameName
							.toLowerCase()
							.replaceAll(Pattern.quote("-"), "")
							.replaceAll(Pattern.quote(" "), "")
							.replaceAll(Pattern.quote("'"), "")
							.contains(filterText)
						);
				
				if (!isVisible)
				{
					for (final String alias : aliases)
					{
						if (alias.contains(filterText))
						{
							isVisible = true;
							break;
						}
					}
				}
			}
			else
			{
				isVisible = false;
				final Enumeration<?> e = children.elements();
			
				while (e.hasMoreElements())
				{
					final GameLoaderNode child = (GameLoaderNode) e.nextElement();
					child.updateVisibility(filterText);
					
					if (child.isVisible)
						isVisible = true;
				}
			}
		}
		
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Custom class for our tree model. Provides functionality for the filtering
	 * field.
	 * 
	 * Filtering code based on: http://www.java2s.com/Code/Java/Swing-Components/InvisibleNodeTreeExample.htm
	 * 
	 * @author Dennis Soemers
	 */
	private static class GameLoaderTreeModel extends DefaultTreeModel
	{
		
		/** */
		private static final long serialVersionUID = 1L;
		
		/** Whether or not we should filter */
		protected boolean filterActive = false;
		
		/**
		 * Constructor
		 * @param root
		 */
		public GameLoaderTreeModel(final GameLoaderNode root)
		{
			super(root);
		}
		
		/**
		 * Set whether filter should be active
		 * @param active
		 */
		public void setFilterActive(final boolean active)
		{
			filterActive = active;
		}
		
		@Override
		public Object getChild(final Object parent, final int index)
		{
			return ((GameLoaderNode) parent).getChildAt(index, filterActive);
		}
		
		@Override
		public int getChildCount(final Object parent)
		{
			return ((GameLoaderNode) parent).getChildCount(filterActive);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Custom class for our tree. Overrides getNextMatch() to allow keyboard-based
	 * searching of games inside collapsed categories/folders.
	 * 
	 * @author Dennis Soemers
	 */
	private static class GameLoaderTree extends JTree
	{
	
		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * @param root
		 */
		public GameLoaderTree(final GameLoaderNode root)
		{
			super(new GameLoaderTreeModel(root));
		}
		
		@Override
		public TreePath getNextMatch
		(
			final String prefix, 
			final int startingRow,
            final Position.Bias bias
		) 
		{
	        final int max = getRowCount();
	        
	        if (prefix == null) 
	            throw new IllegalArgumentException();
	        if (startingRow < 0 || startingRow >= max) 
	            throw new IllegalArgumentException();
	        
	        final String str = prefix.toUpperCase();
	        final int increment = (bias == Position.Bias.Forward) ? 1 : -1;
	        
	        int row = startingRow;
	        do 
	        {
	            final TreePath path = getPathForRow(row);
	            final GameLoaderNode rowNode = (GameLoaderNode) path.getLastPathComponent();
				final Enumeration<?> bfsEnumeration = rowNode.breadthFirstEnumeration();
	            
	            while (bfsEnumeration.hasMoreElements())
	            {
	            	final GameLoaderNode node = (GameLoaderNode) bfsEnumeration.nextElement();
	            	final String nodeName = ((String) node.getUserObject()).toUpperCase();
	            	if (nodeName.startsWith(str) && node.fullName.endsWith(".LUD"))
	            		return new TreePath(node.getPath());
	            }

	            row = (row + increment + max) % max;
	        } while (row != startingRow);
	        
	        return null;
	    }
		
		/**
		 * Updates visible nodes in tree based on text entered in filter field
		 * @param filterField
		 */
		public void updateTreeFilter(final JTextField filterField)
		{
			final GameLoaderTreeModel model = (GameLoaderTreeModel) getModel();
			model.setFilterActive(false);
			
			String filterText = 
					filterField.getText()
					.toLowerCase()
					.replaceAll(Pattern.quote("-"), "")
					.replaceAll(Pattern.quote(" "), "")
					.replaceAll(Pattern.quote("'"), "");
			final GameLoaderNode root = ((GameLoaderNode) model.getRoot());
			
			if (filterText.equals("searchgame"))
			{
				filterText = "";
			}
			
			if (filterText.length() > 0)
			{
				root.updateVisibility(filterText);
				model.setFilterActive(true);
			}
			
			model.reload();
			
			if (filterText.length() == 0)
			{
				// expand all nodes that have at least one non-leaf child
				final Enumeration<?> bfsEnumeration = root.breadthFirstEnumeration();
			            
				while (bfsEnumeration.hasMoreElements())
				{
					final GameLoaderNode node = (GameLoaderNode) bfsEnumeration.nextElement();
					
					final Enumeration<?> children = node.children();
					while (children.hasMoreElements())
					{
						final GameLoaderNode child = (GameLoaderNode) children.nextElement();
						if (!child.isLeaf())
						{
							expandPath(new TreePath(node.getPath()));
							break;
						}
					}
				}
			}
			else
			{
				// expand all nodes that have at least one child
				final Enumeration<?> bfsEnumeration = root.breadthFirstEnumeration();
			            
				while (bfsEnumeration.hasMoreElements())
				{
					final GameLoaderNode node = (GameLoaderNode) bfsEnumeration.nextElement();
					if (!node.isLeaf())
						expandPath(new TreePath(node.getPath()));
				}
				
				// set the first game, found in a depth-first enumeration, 
				// that's a complete match from the start of game's filename,
				// as the current selection
				final Enumeration<?> dfsEnumeration = root.depthFirstEnumeration();
				
				while (dfsEnumeration.hasMoreElements())
				{
					final GameLoaderNode node = (GameLoaderNode) dfsEnumeration.nextElement();
					if (node.isLeaf())
					{
						final String gameFilename = 
								((String) node.getUserObject())
								.toLowerCase()
								.replaceAll(Pattern.quote("-"), "")
								.replaceAll(Pattern.quote(" "), "")
								.replaceAll(Pattern.quote("'"), "");
						
						if (gameFilename.startsWith(filterText))
						{
							// found a complete match at start of filename
							setSelectionPath(new TreePath(node.getPath()));
							break;
						}
					}
				}
			}
			
			scrollRowToVisible(getLeadSelectionRow());
		}
		
	}

	//-------------------------------------------------------------------------

}