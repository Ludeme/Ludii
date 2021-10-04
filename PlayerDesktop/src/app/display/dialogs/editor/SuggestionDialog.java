package app.display.dialogs.editor;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;

import app.PlayerApp;
import grammar.Grammar;

/**
 * Create a suggestion list of alternative ludemes.
 * @author mrraow
 */
public class SuggestionDialog extends JDialog implements KeyListener, ListSelectionListener, MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = 4115195324471730562L;
	
	private static final Font FONT = UIManager.getFont("Label.font");
	private static final int VIEW_WIDTH = 600;
	private static final int VIEW_HEIGHT = 400;
	
	final EditorDialog parent;
	
	private final boolean isPartial;
	private final JList<String> list;
	private final JEditorPane docs;
	
	private final List<SuggestionInstance> suggestionInstances = new ArrayList<>();
	
	private PlayerApp app = null;
	
	//-------------------------------------------------------------------------
	
    /**
	 * @param parent
	 * @param point
	 * @param isPartial
	 */
	public SuggestionDialog(final PlayerApp app, final EditorDialog parent, final Point point, final boolean isPartial)
	{
		super(parent);
		setUndecorated(true);
		
		this.parent = parent;
		this.isPartial = isPartial;
		this.app = app;
			    
		final JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.PAGE_AXIS));
		getContentPane().add(top);

		final JPanel fpanel = new JPanel();
		fpanel.setLayout(new BoxLayout(fpanel, BoxLayout.LINE_AXIS));
		top.add(fpanel);
		
		final DefaultListModel<String> listModel = new DefaultListModel<String>();
		list = new JList<>();
		list.setModel(listModel);
		list.getSelectionModel().addListSelectionListener(this);
		list.addMouseListener(this);
		list.addMouseMotionListener(this);
		list.setFont(FONT);
	    list.addKeyListener(this);
		
		final JScrollPane scroll1 = new JScrollPane(list);
		scroll1.setPreferredSize(new Dimension(VIEW_WIDTH,VIEW_HEIGHT));
		scroll1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		fpanel.add(scroll1);

		docs = new JEditorPane("text/html", "");
		docs.setEditable(false);
		docs.addKeyListener(this);

		final StyleSheet styleSheet = ((HTMLDocument)(docs.getDocument())).getStyleSheet();
		styleSheet.addRule("body { font-family: " + FONT.getFamily() + "; " + "font-size: " + FONT.getSize() + "pt; }");
		styleSheet.addRule("p { font-family: " + FONT.getFamily() + "; " + "font-size: " + FONT.getSize() + "pt; }");
		styleSheet.addRule("* { font-family: " + FONT.getFamily() + "; " + "font-size: " + FONT.getSize() + "pt; }");

		final JScrollPane scroll2 = new JScrollPane(docs);
		scroll2.setPreferredSize(new Dimension(VIEW_WIDTH,VIEW_HEIGHT));
		scroll2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		fpanel.add(scroll2);

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModalityType(ModalityType.MODELESS);
		
	    addKeyListener(this);

	    filterAndAdd(point);
	    pack();
	}
	
	private void filterAndAdd(final Point screenPos)
	{
		setVisible(false);
		filter();
	    
		if (isEmpty()) 
		{
			parent.returnFocus();
			return;
		} 
		else 
		{
			setLocation(screenPos.x, screenPos.y);
			setVisible(true);
		}
	}

	void filter()
	{
		final DefaultListModel<String> listModel = (DefaultListModel<String>)list.getModel();
		listModel.clear();
		list.removeAll();
		suggestionInstances.clear();
		
		final List<String> allCandidates = Grammar.grammar().classPaths(parent.getText(), parent.getCaretPosition(), isPartial);
		//System.out.println ("Returned classpaths: " + allCandidates);
		
		final List<SuggestionInstance> suggestionsFromClasspaths = EditorHelpDataHelper.suggestionsForClasspaths(parent.editorHelpData, allCandidates, isPartial);

		final String charsBefore = parent.charsBeforeCursor();
		//System.out.println("### charsBefore:" + charsBefore);
		
		for (final SuggestionInstance si: suggestionsFromClasspaths)
		{
			if (!isPartial || matches(charsBefore, si.substitution))
				suggestionInstances.add(si);
		}
		
		//suggestionInstances.addAll(suggestionsFromClasspaths);
		if (suggestionInstances.isEmpty()) 
		{
			setVisible(false);
			parent.returnFocus();
			return;
		}

		//System.out.println(suggestionInstances.size()+" suggestions found");
		suggestionInstances.sort((a,b)->a.label.compareTo(b.label));
		for (final SuggestionInstance si: suggestionInstances)
				listModel.addElement(EditorHelpDataHelper.formatLabel(si.substitution));
		
		list.setSelectedIndex(0);
		list.invalidate();
	}

	private static boolean matches(final String charsBefore, final String substitution)
	{
		final boolean result = substitution.startsWith(charsBefore) || substitution.startsWith("("+charsBefore);
		//System.out.println("testing: "+charsBefore+" vs "+substitution);
		return result;
	}

	public boolean isEmpty() 
	{ 
    	return suggestionInstances.isEmpty(); 
    }

	@Override
	public void keyTyped(final KeyEvent e)
	{
		if (e.isActionKey()) return;
		System.out.println("Key typed: "+e.toString());
		
		final char keyChar = e.getKeyChar();
		if (keyChar == KeyEvent.CHAR_UNDEFINED) return;
		
		switch (keyChar) 
		{
		case KeyEvent.VK_TAB:
		case KeyEvent.VK_CANCEL:
		case KeyEvent.VK_CLEAR:
		case KeyEvent.VK_SHIFT:
		case KeyEvent.VK_CONTROL:
		case KeyEvent.VK_ALT:
		case KeyEvent.VK_PAUSE:
		case KeyEvent.VK_CAPS_LOCK:
		case KeyEvent.VK_PAGE_UP:
		case KeyEvent.VK_PAGE_DOWN:
		case KeyEvent.VK_END:
		case KeyEvent.VK_HOME:
			break;
			
		case KeyEvent.VK_ENTER:
			{
				final int pos = list.getSelectedIndex();
				insertListEntryAndClose(pos);
			}
	    	break;

		case KeyEvent.VK_ESCAPE:
			setVisible(false);
			break;
			
		case KeyEvent.VK_BACK_SPACE:
			if (isPartial)
			{
				parent.applyBackspace(app);
				updateList();
			}
	    	break;
		
		case KeyEvent.VK_DELETE:
			if (isPartial)
			{
				parent.applyDelete(app);
				updateList();
			}
	    	break;
		
	    default:
	    	if (isPartial)
	    	{
	    		parent.insertCharacter(app, e.getKeyChar());
				updateList();
	    	}
		}
	}

	private void updateList()
	{
		SwingUtilities.invokeLater(
				new Runnable() 
				{ 
					@Override 
					public void run() 
					{ 
						filter(); 
					} 
				}
		);
	}

	@Override public void keyPressed(final KeyEvent e)  
	{ 
	    switch (e.getKeyCode()) 
	    { 
	        case KeyEvent.VK_LEFT:
			{
				parent.cursorLeft();
				updateList();
		    	break;
			}
	        case KeyEvent.VK_RIGHT:
			{
				parent.cursorRight();
				updateList();
		    	break;
			}
	     }
	}
	
	@Override public void keyReleased(final KeyEvent e) { /* Don't care! Required for KeyListener interface */ }

	@Override
	public void valueChanged(final ListSelectionEvent e)
	{
        if (e.getValueIsAdjusting()) return;

        final int pos = list.getSelectedIndex();
        if (pos >= 0 && pos < suggestionInstances.size()) 
        	docs.setText("<html>"+suggestionInstances.get(pos).javadoc+"</html>");
	}

	@Override
	public void mouseClicked(final MouseEvent evt)
	{
		final int pos = list.locationToIndex(evt.getPoint());
		insertListEntryAndClose(pos);
	}

	private void insertListEntryAndClose(final int listSelection)
	{
		if (listSelection >= 0) 
		{
			parent.replaceTokenScopeWith(app, suggestionInstances.get(listSelection).substitution, isPartial);
			setVisible(false);
		}
	}

	@Override public void mousePressed(final MouseEvent e) { /* Don't care! Required for MouseListener interface */ }
	@Override public void mouseReleased(final MouseEvent e) { /* Don't care! Required for MouseListener interface */ }
	@Override public void mouseEntered(final MouseEvent e) { /* Don't care! Required for MouseListener interface */ } 
	@Override public void mouseExited(final MouseEvent e) { /* Don't care! Required for MouseListener interface */ }

	@Override public void mouseDragged(final MouseEvent e) { /* Don't care! Required for MouseMotionListener interface */ }

	@Override
	public void mouseMoved(final MouseEvent me)
	{
		final Point p = new Point(me.getX(),me.getY());
		final int index = list.locationToIndex(p);
		if (index >= 0) 
        	list.setSelectedIndex(index);
	}
}
