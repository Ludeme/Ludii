package app.display.dialogs.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import app.DesktopApp;
import app.PlayerApp;
import app.display.dialogs.util.DialogUtil;
import app.utils.GameSetup;
import graphics.ImageProcessing;
import main.Constants;
import main.EditorHelpData;
import main.grammar.Description;
import main.grammar.Report;
import manager.Manager;
import other.location.FullLocation;
import parser.Parser;
import parser.SelectionType;
import parser.TokenRange;

/**
 * Editor dialog.
 * @author cambolbro & matthew.stephenson & mrraow
 */
public class EditorDialog extends JDialog
{
	private static final long serialVersionUID = -3636781014267129575L;

	private static final String TAB_STRING = "\t";
	private static final String TAB_REPLACEMENT = "    ";
	/** If the length of time between key presses is less than this, change is not stored in undoDescriptions */
	public final static int TIMERLENGTH = 500;
	private final JPanel contentPanel = new JPanel();
	private final List<UndoRecord> undoRecords = new ArrayList<>();
	
	private int undoDescriptionsMarker = 0;
	
	static EditorDialog dialog;
	final EditorHelpData editorHelpData = EditorHelpData.get();  // Pre-initialise the EditorhelpData
	final boolean useColouredText;
	final JTextPane textArea;
	final JLabel verifiedByParser;
	
	String pasteBuffer = "";
	SuggestionDialog suggestion = null; 
	static boolean trace = false;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param longDescription
	 * @param textColoured
	 * @param shortcutsActive
	 */
	public static void createAndShowGUI(final PlayerApp app, final boolean longDescription, final boolean textColoured, final boolean shortcutsActive)
	{
		try
		{			
			dialog = new EditorDialog(app, longDescription, textColoured, shortcutsActive);
			DialogUtil.initialiseSingletonDialog(dialog, "Editor", null);

			dialog.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosed(final WindowEvent e)
				{
					DesktopApp.frame().setContentPane(DesktopApp.view());
					DesktopApp.view().invalidate();								// WAS: setSize(DesktopApp.view().getSize()); let's see if anybody screams!
					app.repaint();
					app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
				}
			});
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Create the dialog.
	 */
	private EditorDialog (final PlayerApp app, final boolean longDescription, final boolean useColouredText, final boolean shortcutsActive)
	{
		super(null, java.awt.Dialog.ModalityType.DOCUMENT_MODAL);
		
		this.useColouredText = useColouredText;
		
		setBounds(100, 100, 759, 885);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		textArea = createTextPane();
		
		final JPanel noWrapPanel = new JPanel( new BorderLayout() );
		noWrapPanel.add (textArea);
		final JScrollPane scrollPane = new JScrollPane( noWrapPanel );
		contentPanel.add(scrollPane);
		
		final JPanel topPane = new JPanel();
		topPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(topPane, BorderLayout.NORTH);

		JCheckBox verifiedByParserCheckbox = new JCheckBox();
		verifiedByParserCheckbox.setHorizontalAlignment(SwingConstants.RIGHT);
		verifiedByParserCheckbox.setText("Parse Text");
		verifiedByParserCheckbox.setSelected(app.settingsPlayer().isEditorParseText());
		topPane.add(verifiedByParserCheckbox, BorderLayout.NORTH);
		
		final ActionListener parserListener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				app.settingsPlayer().setEditorParseText(verifiedByParserCheckbox.isSelected());
				verifiedByParserCheckbox.setSelected(app.settingsPlayer().isEditorParseText());
				app.addTextToStatusPanel("Please close and repoen the editor for this change to apply.\n");
			}
		};
		
		verifiedByParserCheckbox.addActionListener(parserListener);
		
		verifiedByParser = new JLabel();
		verifiedByParser.setHorizontalAlignment(SwingConstants.RIGHT);
		topPane.add(verifiedByParser, BorderLayout.NORTH);
		
		final JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.LINE_AXIS));
		getContentPane().add(bottomPane, BorderLayout.SOUTH);
		
		final JPanel buttonPaneLeft = new JPanel();
		buttonPaneLeft.setLayout(new FlowLayout(FlowLayout.LEFT));
		bottomPane.add(buttonPaneLeft);
		
		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bottomPane.add(buttonPane);
		
		addNewButton(app, buttonPaneLeft);
		addCompileButton(app, buttonPane);
		addLoadButton(app, buttonPane);
		addDebugButton(app, buttonPane);
		addSaveButton(app, buttonPane);
		addNewFileButton(app, buttonPane);
		addCancelButton(buttonPane);
		
		final String gameDescription = getGameDescription(app, longDescription);
		setText(app, gameDescription.replace("\r", "")); // This character can cause discrepancies between internal model and document model for textArea 

		if (useColouredText) {
			setTextUpdateMonitor(app);
		}
				
		if (shortcutsActive){
			addUndoHandler(app); 
		}
		
		addMouseListener(app);
//		addMouseMotionListener(textArea);
	}
	
	//-------------------------------------------------------------------------

	private static JTextPane createTextPane()
	{
		final JTextPane jTextPane = new JTextPane();
		
		// This prevents switching between getText(0 and getDocument().getText(...) causing weird discrepancies in the character positions
		jTextPane.getDocument().putProperty(DefaultEditorKit.EndOfLineStringProperty, "\n");
		jTextPane.setFocusTraversalKeysEnabled(false);
		
		jTextPane.addFocusListener(new FocusListener() {
			@Override public void focusGained(final FocusEvent e) { jTextPane.getCaret().setVisible(true); }
			@Override public void focusLost(final FocusEvent e) { jTextPane.getCaret().setVisible(true); }
		});
		
		final StyledDocument styledDoc = jTextPane.getStyledDocument();
		if (styledDoc instanceof AbstractDocument) 
		{
			final AbstractDocument doc = (AbstractDocument)styledDoc;
		    doc.setDocumentFilter(new DocumentFilter() 
		    {
				@Override public void insertString(final FilterBypass fb, final int offset, final String text, final AttributeSet attrs) throws BadLocationException 
				{
			       super.insertString(fb, offset, text.replace(TAB_STRING, TAB_REPLACEMENT), attrs);
				}

				@Override public void replace(final FilterBypass fb, final int offset, final int length, final String text, final AttributeSet attrs) throws BadLocationException
				{
					if (text.equals(TAB_STRING)) {
						indentRange(jTextPane);
						return; // Dealt with elsewhere!
					}
					
					super.replace(fb, offset, length, text.replace(TAB_STRING, TAB_REPLACEMENT), attrs);
				}
				
				
		    });
		} 
		return jTextPane;
	}
    
	//-------------------------------------------------------------------------
	
	private void addUndoHandler(final PlayerApp app)
	{
		undoRecords.add(new UndoRecord(textArea));

		final Timer undoRecordTimer = new Timer(TIMERLENGTH, new ActionListener() 
		{
			@Override
			public void actionPerformed(final ActionEvent arg0) 
			{
				storeUndoText();
			}
		});
		undoRecordTimer.setRepeats(false);

		// undo, redo and delete line
		textArea.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(final KeyEvent e)
			{
				EventQueue.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						highlightMatchingBracket();
						
						if (trace) System.out.println(">>EVENT: textArea/keypressed");
						undoRecordTimer.stop();
						storeUndoText();
						
						switch (EditorActions.fromKeyEvent(e))
						{
						case DELETE_LINE:
							if (trace) System.out.println(">>EVENT: textArea/keypressed delete line");
							storeUndoText();
							deleteLine(app);
							break;
							
						case REDO:
							if (trace) System.out.println(">>EVENT: textArea/keypressed redo");
							storeUndoText();
							redo();
							break;
							
						case UNDO:
							if (trace) System.out.println(">>EVENT: textArea/keypressed undo");
							storeUndoText();
							undo();
							break;

						case NO_ACTION:
							if (trace) System.out.println(">>EVENT: textArea/keypressed ignored "+KeyEvent.getKeyText(e.getKeyChar()));							
							break;
						
						case COPY_SELECTION:
							copySelection();
							if (trace) System.out.println(">>EVENT: textArea/keypressed copy selection "+pasteBuffer);							
							break;
						
						case REMOVE_SELECTION:
							storeUndoText();
							removeSelection(app);
							if (trace) System.out.println(">>EVENT: textArea/keypressed remove selection "+pasteBuffer);							
							break;

						case PASTE_BUFFER:
							storeUndoText();
							pasteBuffer(app);
							if (trace) System.out.println(">>EVENT: textArea/keypressed paste "+pasteBuffer);							
							break;
							
						case AUTOSUGGEST:
							// Get current location
							if (trace) System.out.println(">>EVENT: textArea/keypressed autosuggest");
							storeUndoText();
							showAutosuggest(app, TextPaneUtils.cursorCoords(textArea), true);
							break;		
							
						case TAB:
							// Handled in the text filter!
							//if (trace) System.out.println(">>EVENT: textArea/keypressed autosuggest");
							//storeUndoText();
							//indentRange();
							break;
						}
						
						undoRecordTimer.start();
					}
				});
			}
			
			@SuppressWarnings("deprecation")
			@Override
			public void keyTyped(final KeyEvent e)
			{
				if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0) return; // Ignore control keys - they are dealt with elsewhere
				if (e.getKeyChar()==KeyEvent.CHAR_UNDEFINED) return; // Not a display character
				if (Character.isWhitespace(e.getKeyChar())) return; // Context menu not appropriate after a space?

				if (trace) System.out.println(">>EVENT: textArea/keytyped - maybe showing autosuggest");

				storeUndoText();

				// let typing finish first, then show the suggestions dialogue
		    	EventQueue.invokeLater(new Runnable() {
					@Override public void run() { showAutosuggest(app, TextPaneUtils.cursorCoords(textArea), true); }
				});
			}
		});
	}
	
	//-------------------------------------------------------------------------

	void deleteLine(final PlayerApp app)
	{
		final int newCaretPosition = TextPaneUtils.startOfCaretCurrentRow(textArea);
		final int caretRowNumber = TextPaneUtils.getCaretRowNumber(textArea);

		// remove the current caret line
		final String[] lines =  textAreaFullDocument().split("\n");
		final StringBuilder newDesc = new StringBuilder();
		for (int s = 0; s < lines.length; s++)
			if (s != caretRowNumber-1)
				newDesc.append(lines[s]).append("\n");

		setText(app, newDesc.toString());
		textArea.setCaretPosition(newCaretPosition);
	}

	void storeUndoText ()
	{
		if (undoRecords.get(undoDescriptionsMarker).ignoreChanges(textArea))
			return;

		// Roll back to current cursor
		for (int i = undoRecords.size()-1; i > undoDescriptionsMarker ; i--)
			undoRecords.remove(i);

		// Add new text
    	undoDescriptionsMarker++;
    	undoRecords.add(new UndoRecord(textArea));
	}
	
	//-------------------------------------------------------------------------
		
	void redo()
	{
		if (undoDescriptionsMarker >= undoRecords.size()-1)
			return;			

		undoDescriptionsMarker++;
		undoRecords.get(undoDescriptionsMarker).apply(textArea);
	}
	
	//-------------------------------------------------------------------------

	void undo()
	{
		if (undoDescriptionsMarker <= 0)
			return;
		
		undoDescriptionsMarker--;
		undoRecords.get(undoDescriptionsMarker).apply(textArea);
	}
	
	//-------------------------------------------------------------------------

	private void addMouseListener(final PlayerApp app)
	{
		textArea.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(final MouseEvent e)
			{
				if (trace) System.out.println(">>EVENT: textArea/mouseClicked");
				if (SwingUtilities.isRightMouseButton(e))
				{
					if (trace) System.out.println(">>EVENT: textArea/mouseClicked/right click");
					showAutosuggest(app, e.getPoint(), false);
				}
				else if (e.getClickCount()==1)
				{
					if (trace) System.out.println(">>EVENT: textArea/mouseClicked/single click");
					if (suggestion != null) suggestion.setVisible(false);
				}
				else if (e.getClickCount()==2)
				{
					if (trace) System.out.println(">>EVENT: textArea/mouseClicked/double click");
					final int caretPos = textArea.getCaretPosition();
					final TokenRange range = Parser.tokenScope(textArea.getText(), caretPos, true, SelectionType.SELECTION);
					if (range != null) 
					{
						textArea.setSelectionStart(range.from());
						textArea.setSelectionEnd(range.to());
					}
				}
				
				highlightMatchingBracket();
			}
//			@Override public void mousePressed(MouseEvent e)
//			@Override public void mouseReleased(MouseEvent e)
//			@Override public void mouseEntered(MouseEvent e) 
//			@Override public void mouseExited(MouseEvent e)

			
		});
	}
	
	//-------------------------------------------------------------------------
	
	void highlightMatchingBracket()
	{
		textArea.getHighlighter().removeAllHighlights();
		final int caretPos = textArea.getCaretPosition();
		String prevChar;
		try
		{
			prevChar = textArea.getText(caretPos-1, 1);
			int matchingCharLocation = -1;
			
			if (prevChar.equals("("))
				matchingCharLocation = findMatching("(", ")", caretPos, true);
			else if (prevChar.equals(")"))
				matchingCharLocation = findMatching(")", "(", caretPos, false);
			else if (prevChar.equals("{"))
				matchingCharLocation = findMatching("{", "}", caretPos, true);
			else if (prevChar.equals("}"))
				matchingCharLocation = findMatching("}", "{", caretPos, false);
			else if (prevChar.equals("["))
				matchingCharLocation = findMatching("[", "]", caretPos, true);
			else if (prevChar.equals("]"))
				matchingCharLocation = findMatching("]", "[", caretPos, false);
			
			if (matchingCharLocation != -1)
			{
				Highlighter highlighter = textArea.getHighlighter();
				HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
				highlighter.addHighlight(matchingCharLocation, matchingCharLocation+1, painter);
			}
		}
		catch (BadLocationException e)
		{
			// carry on
		}
	}
	
	int findMatching(final String selectedString, final String matchingString, final int initialPosition, final boolean checkForward)
	{
		int caretPos = initialPosition;
		int numSelectedString = 1;
		String textToCheck;
		try
		{
			if (checkForward)
			{
				textToCheck = textArea.getText(caretPos, textArea.getText().length()-caretPos);
				while (textToCheck.length() > 0)
				{
					if (String.valueOf(textToCheck.charAt(0)).equals(matchingString))
						numSelectedString--;
					else if (String.valueOf(textToCheck.charAt(0)).equals(selectedString))
						numSelectedString++;
					
					if (numSelectedString == 0)
						return caretPos;
					
					caretPos++;
					textToCheck = textToCheck.substring(1);
				}
			}
			else
			{
				textToCheck = textArea.getText(0, caretPos-1);
				while (textToCheck.length() > 0)
				{
					if (String.valueOf(textToCheck.charAt(textToCheck.length()-1)).equals(matchingString))
						numSelectedString--;
					else if (String.valueOf(textToCheck.charAt(textToCheck.length()-1)).equals(selectedString))
						numSelectedString++;
					
					if (numSelectedString == 0)
						return caretPos-2;
					
					caretPos--;
					textToCheck = textToCheck.substring(0,textToCheck.length()-1);
				}
			}
		}
		catch (BadLocationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return -1;
	}
	
	//-------------------------------------------------------------------------

	@SuppressWarnings("deprecation")
	void showAutosuggest(final PlayerApp app, final Point point, final boolean usePartial)
	{
		if (suggestion != null) 
			suggestion.setVisible(false);
		
		if (!app.settingsPlayer().editorAutocomplete()) 
			return;
		
		if (trace) System.out.println("### Showing Autosuggest");
		
		if (!usePartial) 
		{
			final int posn = textArea.viewToModel(point);
			textArea.setCaretPosition(posn);
		}
		
		try {
			final Rectangle rect = textArea.modelToView(textArea.getCaretPosition());
			final Point screenPos = new Point(rect.x, rect.y);
			
			SwingUtilities.convertPointToScreen(screenPos, textArea);
			screenPos.y += rect.height;
	
			suggestion = new SuggestionDialog(app, EditorDialog.this, screenPos, usePartial);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	private static String getGameDescription (final PlayerApp app, final boolean longDescription)
	{
		String gameDescription = "";	
		if (longDescription)
		{
			gameDescription = app.manager().ref().context().game().description().expanded();
		}
		else
		{
			gameDescription = app.manager().ref().context().game().description().raw();
		}
		return gameDescription;
	}
	
	//-------------------------------------------------------------------------

	private void setTextUpdateMonitor(final PlayerApp app)
	{
		final Timer colourRecordTimer = new Timer(TIMERLENGTH, new ActionListener() 
		{
			@Override
			public void actionPerformed(final ActionEvent arg0) 
			{
				setText(app, textAreaFullDocument());
			}
		});
		colourRecordTimer.setRepeats(false);
		
		textArea.addKeyListener(new KeyAdapter()
		{
			@Override
		    public void keyPressed(final KeyEvent e)
		    {
		    	EventQueue.invokeLater(new Runnable()
				{

					@Override
					public void run()
					{
						colourRecordTimer.stop();
						colourRecordTimer.start();
					}
				
				});
		    }
		});
	}
	
	//-------------------------------------------------------------------------

	final String textAreaFullDocument()
	{
		return textArea.getText();
	}
	
	//-------------------------------------------------------------------------

	final String documentForSave()
	{
		final String full = textAreaFullDocument();
		return full.replace("\n", System.lineSeparator());
	}
	
	//-------------------------------------------------------------------------
	
	private static JButton addButton(final JPanel buttonPane, final String label, final ActionListener listener) 
	{
		final JButton button = new JButton(label);
		button.setActionCommand(label);
		button.addActionListener(listener);	
		buttonPane.add(button);
		
		return button;
	}
	
	//-------------------------------------------------------------------------
	
	private static JButton addCancelButton(final JPanel buttonPane)
	{
		final ActionListener listener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final Component component = (Component) e.getSource();
				final JDialog dialog1 = (JDialog) SwingUtilities.getRoot(component);
				dialog1.dispose();
			}
		};

		return addButton (buttonPane, "Cancel", listener);
	}
	
	//-------------------------------------------------------------------------

	private JButton addNewFileButton(final PlayerApp app, final JPanel buttonPane)
	{		
		final ActionListener listener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				saveGameDescription(app, documentForSave());
			}
		};
		
		return addButton (buttonPane, "Save new file", listener);
	}
	
	//-------------------------------------------------------------------------

	private JButton addSaveButton(final PlayerApp app, final JPanel buttonPane)
	{
		final ActionListener listener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				// TDO: Consider an "are you sure?" option.
				try (PrintWriter out = new PrintWriter(app.manager().savedLudName())) {
					out.println(documentForSave());
					System.out.println(app.manager().savedLudName() + " overridden");
				}
				catch (final FileNotFoundException e2)
				{
					// Fixme - report to user
					System.out.println("You cannot override a game description loaded from memory. Use the 'Save new file' option");
				}
			}
		};

		return addButton (buttonPane, "Override existing file", listener);
	}
	
	//-------------------------------------------------------------------------

	private JButton addNewButton(final PlayerApp app, final JPanel buttonPane)
	{
		final ActionListener listener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				setText(app, "(game <string> [<players>] [<mode>] [<equipment>] [<rules>])");
			}
		};

		final JButton newButton = addButton (buttonPane, "New", listener);		
		return newButton;
	}
	
	//-------------------------------------------------------------------------

	private JButton addCompileButton(final PlayerApp app, final JPanel buttonPane)
	{
		final ActionListener listener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				GameSetup.compileAndShowGame(app, textAreaFullDocument(), false);
			}
		};

		final JButton okButton = addButton (buttonPane, "Compile", listener);
		getRootPane().setDefaultButton(okButton);

		return okButton;
	}
	
	//-------------------------------------------------------------------------

	private JButton addLoadButton(final PlayerApp app, final JPanel buttonPane)
	{
		final ActionListener listener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				final int fcReturnVal = DesktopApp.gameFileChooser().showOpenDialog(DesktopApp.frame());
				if (fcReturnVal == JFileChooser.APPROVE_OPTION)
				{
					app.manager().ref().interruptAI(app.manager());
					final File file = DesktopApp.gameFileChooser().getSelectedFile();
					try
					{
						setText(app, String.join("\n", Files.readAllLines(file.toPath())));
						dialog.toFront();
					}
					catch (final IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		};

		final JButton okButton = addButton (buttonPane, "Load", listener);

		return okButton;
	}
	
	//-------------------------------------------------------------------------

	private JButton addDebugButton(final PlayerApp app, final JPanel buttonPane)
	{
		final ActionListener listener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				GameSetup.compileAndShowGame(app, textAreaFullDocument(), true);
			}
		};

		final JButton okButton = addButton (buttonPane, "Debug", listener);

		return okButton;
	}
	
	//-------------------------------------------------------------------------

	void setText(final PlayerApp app, final String gameDescription)
	{
		final int pos = textArea.getCaretPosition();
		final int selStart = textArea.getSelectionStart();
		final int selEnd = textArea.getSelectionEnd();

		if (useColouredText)
		{
			textArea.setText("");
			final String[] tokens = new LudiiTokeniser(gameDescription).getTokens();

			int bracketCount = 0;
			int curlyCount = 0;
			boolean inAngle = false;
			
			EditorTokenType lastTokenType = null;
			
			for (final String token : tokens) 
			{
				final EditorTokenType ttype = LudiiTokeniser.typeForToken(token, inAngle, lastTokenType);

				switch (ttype)
				{
				case OPEN_CURLY:
					appendToPane(app, textArea, token, EditorLookAndFeel.bracketColourByDepthAndType(ttype, curlyCount), ttype.isBold());
					curlyCount++;
					break;

				case OPEN_ROUND:
				case OPEN_SQUARE:
					appendToPane(app, textArea, token, EditorLookAndFeel.bracketColourByDepthAndType(ttype, bracketCount), ttype.isBold());
					bracketCount++;
					break;
					
				case CLOSE_CURLY:
					curlyCount--;
					appendToPane(app, textArea, token, EditorLookAndFeel.bracketColourByDepthAndType(ttype, curlyCount), ttype.isBold());
					break;
				
				case CLOSE_ROUND:
				case CLOSE_SQUARE:
					bracketCount--;
					appendToPane(app, textArea, token, EditorLookAndFeel.bracketColourByDepthAndType(ttype, bracketCount), ttype.isBold());
					break;
				
				case OPEN_ANGLE:
					inAngle = true;
					appendToPane(app, textArea, token, ttype.fgColour(), ttype.isBold());
					break;

				case CLOSE_ANGLE:
					appendToPane(app, textArea, token, ttype.fgColour(), ttype.isBold());
					inAngle = false;
					break;

				case WHITESPACE:
				case INT:
				case FLOAT:
				case OTHER:
				case STRING:
				case LABEL:
				case CLASS:
				case ENUM:
				case RULE:
					appendToPane(app, textArea, token, ttype.fgColour(), ttype.isBold());
					break;
				}
				
				lastTokenType = ttype;
			}			
		}
		else
		{
			textArea.setText(gameDescription);
		}
		
		textArea.setCaretPosition(pos);
		if (selStart != selEnd) 
		{
			textArea.setSelectionStart(selStart);
			textArea.setSelectionEnd(selEnd);
		}

		if (app.settingsPlayer().isEditorParseText())
			checkParseState(app.manager(), gameDescription);
		
		highlightMatchingBracket();
	}
	
	//-------------------------------------------------------------------------

	private final void checkParseState(final Manager manager, final String gameStr)
	{
		try 
		{
			final Description gameDescription = new Description(gameStr);
			final boolean success = Parser.parseTest
									(
										gameDescription, 
										manager.settingsManager().userSelections(), 
										new Report(),
										false
									);
			wasVerifiedByParser(success ? true : false);
		} 
		catch (final Exception e) 
		{
			wasVerifiedByParser(false);			
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Called with the result of the parser when trying to verify the game description.
	 */
	private void wasVerifiedByParser(final boolean b)
	{
		final int r = 7;
		final Color markerColour = b ? Color.GREEN : Color.RED;
		final BufferedImage image = new BufferedImage(r*4, r*3, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		ImageProcessing.ballImage(g2d, r, r, r, markerColour);	
		final ImageIcon icon = new ImageIcon(image); 
		verifiedByParser.setIcon(icon);
	}
	
	//-------------------------------------------------------------------------

	public static String saveGameDescription (final PlayerApp app, final String desc)
	{
		final int fcReturnVal = DesktopApp.saveGameFileChooser().showSaveDialog(DesktopApp.frame());

		if (fcReturnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = DesktopApp.saveGameFileChooser().getSelectedFile();
			final String filePath = file.getAbsolutePath();
			if (!filePath.endsWith(".lud"))
			{
				file = new File(filePath + ".lud");
			}

			try (PrintWriter out = new PrintWriter(file.getAbsolutePath())) {
			    out.println(desc);
			}
			catch (final FileNotFoundException e)
			{
				e.printStackTrace();
			}
			return filePath;
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	private static void appendToPane
	(
		final PlayerApp app, final JTextPane tp, final String msg, final Color c, final boolean isBold
	)
    {
        final StyleContext sc = StyleContext.getDefaultStyleContext();
        
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        aset = sc.addAttribute(aset, StyleConstants.Size, Integer.valueOf(app.settingsPlayer().editorFontSize()));
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Arial");
        aset = sc.addAttribute(aset, StyleConstants.Bold, Boolean.valueOf(isBold));
        aset = sc.addAttribute(aset, StyleConstants.Alignment, Integer.valueOf(StyleConstants.ALIGN_JUSTIFIED));

        final int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }

	//-------------------------------------------------------------------------

	final String getText() { return textArea.getText(); }
	final int getCaretPosition() { return textArea.getCaretPosition(); }

	final void applyBackspace(final PlayerApp app)
	{
		final int pos = textArea.getCaretPosition();
		textArea.select(pos-1, pos);
		textArea.replaceSelection("");
		setText(app, textArea.getText());
	}

	final void applyDelete(final PlayerApp app)
	{
		final int pos = textArea.getCaretPosition();
		textArea.select(pos, pos+1);
		textArea.replaceSelection("");
		setText(app, textArea.getText());
	}

	final void insertCharacter(final PlayerApp app, final char keyChar)
	{
		final String keyVal = Character.toString(keyChar);
		TextPaneUtils.insertAtCaret(textArea, keyVal);
		setText(app, textArea.getText());
	}

	final void cursorLeft()
	{
		final int pos = textArea.getCaretPosition();
		if (pos > 0) textArea.setCaretPosition(pos-1);
		if (trace) System.out.println("LEFT");
	}

	final void cursorRight()
	{
		final int pos = textArea.getCaretPosition();
		if (pos < textArea.getText().length()) textArea.setCaretPosition(pos+1);
		if (trace) System.out.println("RIGHT");
	}

	final void replaceTokenScopeWith(final PlayerApp app, final String substitution, final boolean isPartial)
	{
		try {
			final int caretPos = textArea.getCaretPosition();

			final TokenRange range = Parser.tokenScope(textArea.getText(), caretPos, isPartial, isPartial ? SelectionType.TYPING : SelectionType.CONTEXT);
			if (range == null) 
			{
				if (trace) System.out.println("No range available");
				return;
			}
			
			final String pre = textArea.getText(0, range.from());
			final String post = textArea.getText(range.to(), textArea.getText().length() - range.to());

			final String after = pre + substitution + post;
			setText(app, after);
			textArea.setCaretPosition(pre.length()+substitution.length());
		} catch (final BadLocationException e) {
			e.printStackTrace();
		}
	}

	final void pasteBuffer(final PlayerApp app)
	{
		if (!pasteBuffer.isEmpty()) 
		{
			textArea.replaceSelection(pasteBuffer);
			setText(app, textArea.getText());			
		}
	}

	final void removeSelection(final PlayerApp app)
	{
		final int start = textArea.getSelectionStart();
		final int end = textArea.getSelectionEnd();
		if (start > end) 
		{
			pasteBuffer = textArea.getSelectedText();
			textArea.replaceSelection("");
			setText(app, textArea.getText());			
		}
	}

	final void copySelection()
	{
		final int start = textArea.getSelectionStart();
		final int end = textArea.getSelectionEnd();
		if (start > end) {
			pasteBuffer = textArea.getSelectedText();
		}
	}

	static final void indentRange(final JTextPane textArea)
	{
		if (trace) System.out.println("### INDENT ###");
		final int start = textArea.getSelectionStart();
		final int end = textArea.getSelectionEnd();
		
		if (trace)  System.out.println("start: "+start+", end: "+end);

		if (start < end) {
			final String test = textArea.getSelectedText();
			final String[] lines = test.split("\\R");
			final String fixed = TAB_REPLACEMENT + String.join("\n"+TAB_REPLACEMENT, lines);
			textArea.replaceSelection(fixed);
			textArea.setSelectionStart(start);
			textArea.setSelectionEnd(start+fixed.length());		
		}
		else
		{
			textArea.replaceSelection(TAB_REPLACEMENT);
		}
	}
		
	void returnFocus()
	{
		textArea.requestFocus();
	}

	public String charsBeforeCursor()
	{
		if (textArea.getText().isEmpty()) return "";
		
		final int pos = textArea.getCaretPosition();
		int start = pos-1;
		try {
			while (start > 0 && Character.isLetterOrDigit(textArea.getText(start-1,1).charAt(0)))
				start--;
			
			final String result = textArea.getText(Math.max(0,start),pos-start);
			if (trace) System.out.println("charsBeforeCursor returning "+start+":"+pos+":"+result);
			return result;
		} 
		catch (final Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}

	//-------------------------------------------------------------------------

	
	
}