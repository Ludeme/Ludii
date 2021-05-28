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
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
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

import org.jfree.graphics2d.svg.SVGGraphics2D;

import app.DesktopApp;
import app.PlayerApp;
import app.display.SVGWindow;
import app.loading.MiscLoading;
import app.utils.SVGUtil;
import other.context.Context;

/**
 * Class for showing a dialog to choose a svg to load.
 *
 * @author Matthew Stephenson
 */
public class SVGViewerDialog
{
	static String lastKeyPressed = "";

	//-------------------------------------------------------------------------

	/**
	 * Shows a svg Loader dialog.
	 *
	 * @param frame
	 * @param choices
	 * @return Chosen svg, or null if no choice made
	 */
	public static String showDialog(final PlayerApp app, final JFrame frame, final String[] choices)
	{
		final JPanel contentPane = new JPanel();
		final SVGWindow svgView = new SVGWindow();
		contentPane.setLayout(new BorderLayout());

		lastKeyPressed = "";	// reset this since it's static, shared between dialogs

		// convert our list of svgs into a tree
		final List<svgLoaderNode> leafNodes = new ArrayList<>();
		final Map<String, svgLoaderNode> nodesMap = new HashMap<>();
		final svgLoaderNode root = new svgLoaderNode("svgs", File.separator + "svg" + File.separator);

		for (final String choice : choices)
		{
			String str = choice.replaceAll(Pattern.quote("\\"), "/");
			if (str.startsWith("/"))
				str = str.substring(1);
			final String[] parts = str.split("/");

			if (!parts[0].equals("svg"))
				System.err.println("top level is not svg: " + parts[0]);

			String runningFullName = File.separator + "svg" + File.separator;
			svgLoaderNode internalNode = root;

			for (int i = 1; i < parts.length - 1; ++i)
			{
				final svgLoaderNode nextInternal;
				runningFullName += parts[i] + File.separator;

				if (!nodesMap.containsKey(runningFullName))
				{
					nextInternal = new svgLoaderNode(parts[i], runningFullName);
					nodesMap.put(runningFullName, nextInternal);

					int childIdx = 0;
					while (childIdx < internalNode.getChildCount())
					{
						final svgLoaderNode existingChild =
								(svgLoaderNode) internalNode.getChildAt(childIdx);
						final String name = (String) existingChild.getUserObject();

						if (name.endsWith(".svg"))
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

			final svgLoaderNode leafNode = new svgLoaderNode(parts[parts.length - 1], choice);
			nodesMap.put(choice, leafNode);
			leafNodes.add(leafNode);
			internalNode.add(leafNode);
		}

		final svgLoaderTree tree = new svgLoaderTree(root);
		expandAllNodes(tree, 0, tree.getRowCount());

		// only allow selecting a single leaf, not multiple leaves
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		// create our text field for filtering
		final JTextField filterField = new JTextField();
		filterField.setFont(new Font("Arial", Font.PLAIN, 20));

		final Font gainFont = new Font("Arial", Font.PLAIN, 20);
		final Font lostFont = new Font("Arial", Font.PLAIN, 20);
		final String hint = "Search SVG";

		filterField.setText(hint);
		filterField.setFont(lostFont);
		filterField.setForeground(Color.GRAY);

		final KeyEventDispatcher keyDispatcher = new KeyEventDispatcher()
		{
			@Override
			public boolean dispatchKeyEvent(final KeyEvent e)
			{
				
				boolean focusRequested = false;
				
				if (e.getKeyCode() == KeyEvent.VK_UP)
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
						lastKeyPressed = Character.toString(e.getKeyChar());
						filterField.requestFocus();
						focusRequested = true;
					}
				}
				
				String fileName = null;
				final TreePath treePath = tree.getSelectionPath();
				if (treePath != null)
				{
					final svgLoaderNode selectedLeaf = (svgLoaderNode) treePath.getLastPathComponent();
					if (selectedLeaf.isLeaf())
						fileName = selectedLeaf.fullName;
				}
				if (fileName != null)
				{
					displayImage(app, fileName, contentPane, svgView);	
				}
				
				return focusRequested;
			}
		};

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);

		
		tree.addKeyListener(new KeyListener()
		{

			@Override
			public void keyTyped(final KeyEvent e)
			{
				// nothing
			}

			@Override
			public void keyPressed(final KeyEvent e)
			{
				final int keyCode = e.getKeyCode();
				
				if (keyCode == KeyEvent.VK_UP)
				{
					updateSVGView();	
				}
				if (keyCode == KeyEvent.VK_DOWN)
				{
					updateSVGView();
				}
			}

			private void updateSVGView()
			{
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						String fileName = null;
						final TreePath treePath = tree.getSelectionPath();

						if (treePath != null)
						{
							final svgLoaderNode selectedLeaf = (svgLoaderNode) treePath.getLastPathComponent();

							if (selectedLeaf.isLeaf())
								fileName = selectedLeaf.fullName;
						}

						if (fileName != null)
						{
							displayImage(app, fileName, contentPane, svgView);
						}
					}
				});
			}

			@Override
			public void keyReleased(final KeyEvent e)
			{
				// nothing
			}
		});
		
		
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

				if (!lastKeyPressed.equals(""))
				{
					filterField.setText(filterField.getText() + lastKeyPressed);
					lastKeyPressed = "";
				}
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
		treeView.setPreferredSize(new Dimension(300, 400));
		contentPane.add(treeView, BorderLayout.WEST);
		contentPane.add(filterField, BorderLayout.SOUTH);
		contentPane.add(svgView, BorderLayout.CENTER);
		contentPane.setPreferredSize(new Dimension(1000, 400));

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
					// do nothing
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
					contentPane, JOptionPane.PLAIN_MESSAGE, JOptionPane.CLOSED_OPTION,
					null, null, null
				);
		final JDialog dialog = pane.createDialog("Choose an SVG to View");
		dialog.setIconImage(image);

		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setModal(true);

		tree.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(final MouseEvent e)
			{
		        String fileName = null;
				final TreePath treePath = tree.getSelectionPath();

				if (treePath != null)
				{
					final svgLoaderNode selectedLeaf = (svgLoaderNode) treePath.getLastPathComponent();

					if (selectedLeaf.isLeaf())
						fileName = selectedLeaf.fullName;
				}

				if (fileName != null)
				{
					displayImage(app, fileName, contentPane, svgView);
				}
		    }
		});

		// expand all nodes that have at least one non-leaf child
		final Enumeration<?> bfsEnumeration = root.breadthFirstEnumeration();

		while (bfsEnumeration.hasMoreElements())
		{
			final svgLoaderNode node = (svgLoaderNode) bfsEnumeration.nextElement();

			final Enumeration<?> children = node.children();
			while (children.hasMoreElements())
			{
				final svgLoaderNode child = (svgLoaderNode) children.nextElement();
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
				final svgLoaderNode selectedLeaf = (svgLoaderNode) treePath.getLastPathComponent();

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
	private static class svgLoaderNode extends DefaultMutableTreeNode
	{
		/** */
		private static final long serialVersionUID = 1L;

		/** Full name */
		public final String fullName;

		/** Whether or not it's visible (given current filter) */
		protected boolean isVisible = true;

		/**
		 * Constructor
		 * @param shortName
		 * @param fullName
		 */
		public svgLoaderNode(final String shortName, final String fullName)
		{
			super(shortName);
			this.fullName = fullName;
		}

		/**
		 * @param index
		 * @param filter
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
				final svgLoaderNode node = (svgLoaderNode) e.nextElement();

				if (node.isVisible)
					++visibleIdx;

				if (visibleIdx == index)
					return node;
			}

			throw new ArrayIndexOutOfBoundsException("index unmatched after filtering");
		}

		/**
		 * @param filter
		 * @return Number of child nodes (after filtering if filter == true)
		 */
		public int getChildCount(final boolean filter)
		{
			if (!filter)
				return super.getChildCount();

			int count = 0;
			final Enumeration<?> e = children.elements();
			while (e.hasMoreElements())
			{
				final svgLoaderNode node = (svgLoaderNode) e.nextElement();
				if (node.isVisible)
					++count;
			}
			return count;
		}

		/**
		 * Recursively updates visibility based on user's filter text
		 * @param filterText
		 */
		public void updateVisibility(final String filterText)
		{
			if (isLeaf())
			{
				isVisible =
						(
							fullName
							.toLowerCase()
							.replaceAll(Pattern.quote("-"), "")
							.replaceAll(Pattern.quote(" "), "")
							.contains(filterText)
						);
			}
			else
			{
				isVisible = false;
				final Enumeration<?> e = children.elements();

				while (e.hasMoreElements())
				{
					final svgLoaderNode child = (svgLoaderNode) e.nextElement();
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
	private static class svgLoaderTreeModel extends DefaultTreeModel
	{
		private static final long serialVersionUID = 1L;

		/** Whether or not we should filter */
		protected boolean filterActive = false;

		/**
		 * Constructor
		 * @param root
		 */
		public svgLoaderTreeModel(final svgLoaderNode root)
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
			return ((svgLoaderNode) parent).getChildAt(index, filterActive);
		}

		@Override
		public int getChildCount(final Object parent)
		{
			return ((svgLoaderNode) parent).getChildCount(filterActive);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Custom class for our tree. Overrides getNextMatch() to allow keyboard-based
	 * searching of svgs inside collapsed categories/folders.
	 *
	 * @author Dennis Soemers
	 */
	private static class svgLoaderTree extends JTree
	{

		/** */
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * @param root
		 */
		public svgLoaderTree(final svgLoaderNode root)
		{
			super(new svgLoaderTreeModel(root));
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
	            final svgLoaderNode rowNode = (svgLoaderNode) path.getLastPathComponent();
				final Enumeration<?> bfsEnumeration = rowNode.breadthFirstEnumeration();

	            while (bfsEnumeration.hasMoreElements())
	            {
	            	final svgLoaderNode node = (svgLoaderNode) bfsEnumeration.nextElement();
	            	final String nodeName = ((String) node.getUserObject()).toUpperCase();
	            	if (nodeName.startsWith(str) && nodeName.endsWith(".SVG"))
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
			final svgLoaderTreeModel model = (svgLoaderTreeModel) getModel();
			model.setFilterActive(false);

			String filterText =
					filterField.getText()
					.toLowerCase()
					.replaceAll(Pattern.quote("-"), "")
					.replaceAll(Pattern.quote(" "), "");
			final svgLoaderNode root = ((svgLoaderNode) model.getRoot());

			if (filterText.equals("searchsvg"))
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
					final svgLoaderNode node = (svgLoaderNode) bfsEnumeration.nextElement();

					final Enumeration<?> children = node.children();
					while (children.hasMoreElements())
					{
						final svgLoaderNode child = (svgLoaderNode) children.nextElement();
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
					final svgLoaderNode node = (svgLoaderNode) bfsEnumeration.nextElement();
					if (!node.isLeaf())
						expandPath(new TreePath(node.getPath()));
				}

				// set the first svg, found in a depth-first enumeration,
				// that's a complete match from the start of svg's filename,
				// as the current selection
				final Enumeration<?> dfsEnumeration = root.depthFirstEnumeration();

				while (dfsEnumeration.hasMoreElements())
				{
					final svgLoaderNode node = (svgLoaderNode) dfsEnumeration.nextElement();
					if (node.isLeaf())
					{
						final String svgFilename =
								((String) node.getUserObject())
								.toLowerCase()
								.replaceAll(Pattern.quote("-"), "")
								.replaceAll(Pattern.quote(" "), "");

						if (svgFilename.startsWith(filterText))
						{
							// found a complete match at start of filename
							setSelectionPath(new TreePath(node.getPath()));
							break;
						}
					}
				}
			}
		}
	}

	//-------------------------------------------------------------------------

	private static void expandAllNodes(final JTree tree, final int startingIndex, final int rowCount)
	{
	    for(int i=startingIndex;i<rowCount;++i)
	    {
	        tree.expandRow(i);
	    }

	    if(tree.getRowCount()!=rowCount)
	    {
	        expandAllNodes(tree, rowCount, tree.getRowCount());
	    }
	}
	
	//-------------------------------------------------------------------------
	
	static void displayImage(final PlayerApp app, final String filePath, final JPanel contentPane, final SVGWindow svgView)
	{
		final int sz = contentPane.getWidth()/3;
		final String fileName = filePath.replaceAll(Pattern.quote("\\"), "/");

		final Context context = app.contextSnapshot().getContext(app);
		final SVGGraphics2D svg = MiscLoading.renderImageSVG(sz, fileName, app.bridge().settingsColour().playerColour(context, 1));
		final SVGGraphics2D svg2 = MiscLoading.renderImageSVG(sz, fileName, app.bridge().settingsColour().playerColour(context, 2));

		final BufferedImage componentImageDot1 = SVGUtil.createSVGImage(svg.getSVGDocument(), sz, sz);
		final BufferedImage componentImageDot2 = SVGUtil.createSVGImage(svg2.getSVGDocument(), sz, sz);
						
		svgView.setImages(componentImageDot1, componentImageDot2);
		svgView.repaint();
	}

}