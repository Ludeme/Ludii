package app.display.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import app.PlayerApp;
import app.display.dialogs.util.DialogUtil;
import compiler.Compiler;
import game.equipment.component.Component;
import game.types.board.SiteType;
import game.util.directions.DirectionFacing;
import gnu.trove.list.array.TIntArrayList;
import grammar.Grammar;
import main.StringRoutines;
import main.grammar.Report;
import main.grammar.Symbol;
import other.concept.Concept;
import other.concept.ConceptType;
import other.context.Context;
import other.topology.TopologyElement;

/**
 * Dialog that can be used to compile a given string, using the current game context.
 * 
 * @author Matthew.Stephenson and cambolbro and Eric.Piette
 */
public class TestLudemeDialog extends JDialog
{
	
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	private final JPanel contentPanel = new JPanel();

	/**
	 * Launch the dialog.
	 */
	public static void showDialog(final PlayerApp app, final Context context)
	{
		try
		{
			final TestLudemeDialog dialog = new TestLudemeDialog(app, context);
			DialogUtil.initialiseDialog(dialog, "Test Ludeme Dialog", null);
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
	public TestLudemeDialog(final PlayerApp app, final Context context)
	{
		super(null, java.awt.Dialog.ModalityType.DOCUMENT_MODAL);
		setBounds(100, 100, 650, 509);
		getContentPane().setLayout(new BorderLayout());
		{
			{
				contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
				getContentPane().add(contentPanel, BorderLayout.CENTER);
				contentPanel.setLayout(new GridLayout(2,2));
				
				// Margin size around each panel.
				final int marginSize = 10;
				
				
				// Panel 1
				final JPanel panel_1 = new JPanel();
				panel_1.setBorder(BorderFactory.createEmptyBorder(marginSize, marginSize, marginSize, marginSize));
				contentPanel.add(panel_1);
				panel_1.setLayout(new BorderLayout(0, 0));
				
				final JTextPane textPane_1 = new JTextPane();
				final JScrollPane scrollpane_1 = new JScrollPane(textPane_1);
				panel_1.add(scrollpane_1, BorderLayout.CENTER);		
				
				textPane_1.setPreferredSize(new Dimension(5000, panel_1.getHeight()-20));
				
				final JPanel panel_11 = new JPanel();
				panel_1.add(panel_11, BorderLayout.NORTH);
				panel_11.setLayout(new BorderLayout(0, 0));
				
				final JLabel label_1 = new JLabel("Enter a ludeme:");
				panel_11.add(label_1, BorderLayout.WEST);
				
				final JPanel panel = new JPanel();
				panel_11.add(panel, BorderLayout.EAST);
				
				final JButton button_1 = new JButton("Test");
				panel.add(button_1);
				button_1.setEnabled(false);
				
				final JButton button_5 = new JButton("Concepts");
				panel.add(button_5);
				button_5.setEnabled(false);
				
				

				
				// Panel 2
				final JPanel panel_2 = new JPanel();
				panel_2.setBorder(BorderFactory.createEmptyBorder(marginSize, marginSize, marginSize, marginSize));
				contentPanel.add(panel_2);
				panel_2.setLayout(new BorderLayout(0, 0));
				
				final JTextPane textPane_2 = new JTextPane();
				final JScrollPane scrollpane_2 = new JScrollPane(textPane_2);
				panel_2.add(scrollpane_2, BorderLayout.CENTER);		
				
				textPane_2.setPreferredSize(new Dimension(5000, panel_2.getHeight()-20));
				
				final JPanel panel_22 = new JPanel();
				panel_2.add(panel_22, BorderLayout.NORTH);
				panel_22.setLayout(new BorderLayout(0, 0));
				
				final JLabel label_2 = new JLabel("Enter a ludeme:");
				panel_22.add(label_2, BorderLayout.WEST);
				
				final JPanel panel_5 = new JPanel();
				panel_22.add(panel_5, BorderLayout.EAST);
				
				final JButton button_2 = new JButton("Test");
				panel_5.add(button_2);
				button_2.setEnabled(false);
				
				final JButton button_6 = new JButton("Concepts");
				button_6.setEnabled(false);
				panel_5.add(button_6);
				
				
				
				// Panel 3
				final JPanel panel_3 = new JPanel();
				panel_3.setBorder(BorderFactory.createEmptyBorder(marginSize, marginSize, marginSize, marginSize));
				contentPanel.add(panel_3);
				panel_3.setLayout(new BorderLayout(0, 0));
				
				final JTextPane textPane_3 = new JTextPane();
				final JScrollPane scrollpane_3 = new JScrollPane(textPane_3);
				panel_3.add(scrollpane_3, BorderLayout.CENTER);		
				
				textPane_3.setPreferredSize(new Dimension(5000, panel_3.getHeight()-20));
				
				final JPanel panel_33 = new JPanel();
				panel_3.add(panel_33, BorderLayout.NORTH);
				panel_33.setLayout(new BorderLayout(0, 0));
				
				final JLabel label_3 = new JLabel("Enter a ludeme:");
				panel_33.add(label_3, BorderLayout.WEST);
				
				final JPanel panel_6 = new JPanel();
				panel_33.add(panel_6, BorderLayout.EAST);
				
				final JButton button_3 = new JButton("Test");
				panel_6.add(button_3);
				button_3.setEnabled(false);
				
				final JButton button_7 = new JButton("Concepts");
				button_7.setEnabled(false);
				panel_6.add(button_7);
				
				
				
				// Panel 4
				final JPanel panel_4 = new JPanel();
				panel_4.setBorder(BorderFactory.createEmptyBorder(marginSize, marginSize, marginSize, marginSize));
				contentPanel.add(panel_4);
				panel_4.setLayout(new BorderLayout(0, 0));
				
				final JTextPane textPane_4 = new JTextPane();
				final JScrollPane scrollpane_4 = new JScrollPane(textPane_4);
				panel_4.add(scrollpane_4, BorderLayout.CENTER);		
				
				textPane_4.setPreferredSize(new Dimension(5000, panel_4.getHeight()-20));
				
				final JPanel panel_44 = new JPanel();
				panel_4.add(panel_44, BorderLayout.NORTH);
				panel_44.setLayout(new BorderLayout(0, 0));
				
				final JLabel label_4 = new JLabel("Enter a ludeme:");
				panel_44.add(label_4, BorderLayout.WEST);
				
				final JPanel panel_7 = new JPanel();
				panel_44.add(panel_7, BorderLayout.EAST);
				
				final JButton button_4 = new JButton("Test");
				panel_7.add(button_4);
				button_4.setEnabled(false);
				
				final JButton button_8 = new JButton("Concepts");
				button_8.setEnabled(false);
				panel_7.add(button_8);
				
				
				
				
				// Action listeners for each button
				final ActionListener listener_1 = new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e)
					{
						testLudemeString(app, textPane_1.getText(), context);
					}
				};
				
				final ActionListener listener_2 = new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e)
					{
						testLudemeString(app, textPane_2.getText(), context);
					}
				};
				
				final ActionListener listener_3 = new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e)
					{
						testLudemeString(app, textPane_3.getText(), context);
					}
				};
				
				final ActionListener listener_4 = new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e)
					{
						testLudemeString(app, textPane_4.getText(), context);
					}
				};
				
				button_1.addActionListener(listener_1);
				button_2.addActionListener(listener_2);
				button_3.addActionListener(listener_3);
				button_4.addActionListener(listener_4);
				
				
				// Action listeners for each button
				final ActionListener listener_5 = new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e)
					{
						testLudemeStringConcepts(app, textPane_1.getText(), context);
					}
				};
				
				final ActionListener listener_6 = new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e)
					{
						testLudemeStringConcepts(app, textPane_2.getText(), context);
					}
				};
				
				final ActionListener listener_7 = new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e)
					{
						testLudemeStringConcepts(app, textPane_3.getText(), context);
					}
				};
				
				final ActionListener listener_8 = new ActionListener()
				{
					@Override
					public void actionPerformed(final ActionEvent e)
					{
						testLudemeStringConcepts(app, textPane_4.getText(), context);
					}
				};
				
				button_5.addActionListener(listener_5);
				button_6.addActionListener(listener_6);
				button_7.addActionListener(listener_7);
				button_8.addActionListener(listener_8);
				
				
				// Action listeners for each textpane
				final DocumentListener listenerText_1 = new DocumentListener()
				{
					@Override
					public void changedUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_1, button_1);
						checkButtonEnabled(textPane_1, button_5);
						app.settingsPlayer().setTestLudeme1(textPane_1.getText());
					}

					@Override
					public void insertUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_1, button_1);
						checkButtonEnabled(textPane_1, button_5);
						app.settingsPlayer().setTestLudeme1(textPane_1.getText());
					}

					@Override
					public void removeUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_1, button_1);
						checkButtonEnabled(textPane_1, button_5);
						app.settingsPlayer().setTestLudeme1(textPane_1.getText());
					}
				};
			
				final DocumentListener listenerText_2 = new DocumentListener()
				{
					@Override
					public void changedUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_2, button_2);
						checkButtonEnabled(textPane_2, button_6);
						app.settingsPlayer().setTestLudeme2(textPane_2.getText());
					}

					@Override
					public void insertUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_2, button_2);
						checkButtonEnabled(textPane_2, button_6);
						app.settingsPlayer().setTestLudeme2(textPane_2.getText());
					}

					@Override
					public void removeUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_2, button_2);
						checkButtonEnabled(textPane_2, button_6);
						app.settingsPlayer().setTestLudeme2(textPane_2.getText());
					}
				};

				final DocumentListener listenerText_3 = new DocumentListener()
				{
					@Override
					public void changedUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_3, button_3);
						checkButtonEnabled(textPane_3, button_7);
						app.settingsPlayer().setTestLudeme3(textPane_3.getText());
					}

					@Override
					public void insertUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_3, button_3);
						checkButtonEnabled(textPane_3, button_7);
						app.settingsPlayer().setTestLudeme3(textPane_3.getText());
					}

					@Override
					public void removeUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_3, button_3);
						checkButtonEnabled(textPane_3, button_7);
						app.settingsPlayer().setTestLudeme3(textPane_3.getText());
					}
				};
				
				final DocumentListener listenerText_4 = new DocumentListener()
				{
					@Override
					public void changedUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_4, button_4);
						checkButtonEnabled(textPane_4, button_8);
						app.settingsPlayer().setTestLudeme4(textPane_4.getText());
					}

					@Override
					public void insertUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_4, button_4);
						checkButtonEnabled(textPane_4, button_8);
						app.settingsPlayer().setTestLudeme4(textPane_4.getText());
					}

					@Override
					public void removeUpdate(final DocumentEvent e)
					{
						checkButtonEnabled(textPane_4, button_4);
						checkButtonEnabled(textPane_4, button_8);
						app.settingsPlayer().setTestLudeme4(textPane_4.getText());
					}
				};
				
				textPane_1.getDocument().addDocumentListener(listenerText_1);
				textPane_2.getDocument().addDocumentListener(listenerText_2);
				textPane_3.getDocument().addDocumentListener(listenerText_3);
				textPane_4.getDocument().addDocumentListener(listenerText_4);
				
				textPane_1.setText(app.settingsPlayer().testLudeme1());
				textPane_2.setText(app.settingsPlayer().testLudeme2());
				textPane_3.setText(app.settingsPlayer().testLudeme3());
				textPane_4.setText(app.settingsPlayer().testLudeme4());
			}
		}
	}
	
	static void checkButtonEnabled(final JTextPane textPane_1, final JButton button_1)
	{
		if (textPane_1.getText().length() > 0)
			button_1.setEnabled(true);
		else
			button_1.setEnabled(false);
	}
	
	//-------------------------------------------------------------------------
	
	// Tests a specified ludeme string when a test button is pressed in the dialog
	static void testLudemeString(final PlayerApp app, final String str, final Context context)
	{
		if (str == null || str.equals(""))
			return;

		try
		{
			final Object compiledObject = compileString(str);
			if (compiledObject != null)
			{
				final String error = evalCompiledObject(app, compiledObject, context);
				if (error != null)
					app.addTextToStatusPanel(error + "\n");
			}
			else
			{
				app.addTextToStatusPanel("Couldn't compile ludeme \"" + str + "\".\n");
			}
		}
		catch (final Exception ex)
		{
			app.addTextToStatusPanel("Couldn't evaluate ludeme.\n");
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Tests a specified ludeme string when a test button is pressed in the
	 * dialog
	 * 
	 * @param app The app.
	 * @param str The string to check.
	 * @param context The context.
	 */
	static void testLudemeStringConcepts
	(
		final PlayerApp app, 
		final String str, 
		final Context context
	)
	{
		if (str == null || str.equals(""))
			return;

		try
		{
			final Object compiledObject = compileString(str);
			if (compiledObject != null)
			{
				final String error = evalConceptCompiledObject(app, compiledObject, context);
				if (error != null)
					app.addTextToStatusPanel(error + "\n");
			}
			else
			{
				app.addTextToStatusPanel("Couldn't compile ludeme \"" + str + "\".\n");
			}
		}
		catch (final Exception ex)
		{
			app.addTextToStatusPanel("Couldn't evaluate ludeme.\n");
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Attempts to compile a given string for every possible symbol class.
	 * @return Compiled object if possible, else null.
	 */
	static Object compileString(final String str)
	{
		Object obj = null;
		
		final String token = StringRoutines.getFirstToken(str);
		final List<Symbol> symbols = Grammar.grammar().symbolsWithPartialKeyword(token);

		// Try each possible symbol for this token
		for (final Symbol symbol : symbols)
		{
			final String className = symbol.cls().getName();
			final Report report = new Report();
			
			try
			{
				obj = Compiler.compileObject(str, className, report);
			}
			catch (final Exception ex)
			{
				//System.out.println("Couldn't compile.");
			}
				
			if (obj == null)
				continue;
		}
		
		return obj;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Attempts to evaluate a compiled object.
	 * 
	 * @author Eric.Piette
	 */
	static String evalCompiledObject(final PlayerApp app, final Object obj, final Context context)
	{
		String error = null;
		boolean foundEval = false;
		boolean success = false;
		
		// Need to preprocess the ludemes before to call the eval method.
		Method preprocess = null;
		try
		{
			preprocess = obj.getClass().getDeclaredMethod("preprocess", context.game().getClass());
			if (preprocess != null)
				preprocess.invoke(obj, context.game());
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

		// Get the right eval method according to the ludeme, in general
		// eval(context) but not always.
		Method eval = null;
		String className = "";
		try
		{
			className = obj.getClass().toString();

			if (className.contains("game.functions.graph"))
				eval = obj.getClass().getDeclaredMethod("eval", Context.class,
						SiteType.class);
			else if (className.contains("game.functions.directions"))
				eval = obj.getClass().getDeclaredMethod("convertToAbsolute", SiteType.class, TopologyElement.class,
						Component.class, DirectionFacing.class, Integer.class, Context.class);
			else
				eval = obj.getClass().getDeclaredMethod("eval", context.getClass());
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}


		// Call the right eval method.
		if (eval != null)
		{
			// Found eval method, try calling it
			foundEval = true;
			try
			{
				String result;

				if (className.contains("game.functions.graph"))
					result = eval.invoke(obj, context, context.board().defaultSite()).toString();
				else if (className.contains("game.functions.directions"))
					result = eval.invoke(obj, context.board().defaultSite(),
							context.topology().centre(context.board().defaultSite()).get(0), null, null, null, context)
							.toString();
				else if (className.contains("game.functions.intArray"))
				{
					final int[] sites = ((int[]) eval.invoke(obj, context));
					result = new TIntArrayList(sites).toString();
				}
//				else if (className.contains("game.rules.play.moves"))
//				{
//					final int[] sites = ((M) eval.invoke(obj, context));
//					result = new TIntArrayList(sites).toString();
//				}
				else
					result = eval.invoke(obj, context).toString();

				success = true;
				if (result.trim().length() > 0)
					app.addTextToStatusPanel(result + "\n");
				else
					app.addTextToStatusPanel("Ludeme compiles, but no result produced.\n");
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
		
		else if (!foundEval)
			error = "Couldn't evaluate ludeme \"" + obj.getClass() + "\".";
		else if (!success)
			error = "Couldn't invoke ludeme \"" + obj.getClass() + "\".";
		
		return error;
	}

	/**
	 * Attempts to evaluate a compiled object.
	 * 
	 * @author Eric.Piette
	 */
	static String evalConceptCompiledObject(final PlayerApp app, final Object obj, final Context context)
	{
		// Need to preprocess the ludemes before to call the eval method.
		Method preprocess = null;
		try
		{
			preprocess = obj.getClass().getDeclaredMethod("preprocess", context.game().getClass());
			if (preprocess != null)
				preprocess.invoke(obj, context.game());
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		Method conceptMethod = null;
		BitSet concepts = new BitSet();
		try
		{
			conceptMethod = obj.getClass().getDeclaredMethod("concepts", context.game().getClass());
			if (preprocess != null)
				concepts = ((BitSet) conceptMethod.invoke(obj, context.game()));
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		final List<List<String>> conceptsPerCategories = new ArrayList<List<String>>();
		for (int i = 0; i < ConceptType.values().length; i++)
			conceptsPerCategories.add(new ArrayList<String>());

		for (int i = 0; i < Concept.values().length; i++)
		{
			final Concept concept = Concept.values()[i];
			final ConceptType type = concept.type();
			if (concepts.get(concept.id()))
				conceptsPerCategories.get(type.ordinal()).add(concept.name());
		}

		final StringBuffer properties = new StringBuffer("The boolean concepts of this ludeme are: \n\n");

		for (int i = 0; i < conceptsPerCategories.size(); i++)
		{
			if(!conceptsPerCategories.get(i).isEmpty())
			{
				final ConceptType type = ConceptType.values()[i];
				properties.append("******* " + type.name() + " concepts *******\n");
				for(int j = 0; j < conceptsPerCategories.get(i).size();j++)
				{
					final String concept = conceptsPerCategories.get(i).get(j);
					properties.append(concept + "\n");
				}
				properties.append("\n");
			}
		}
		
		return properties.toString();
	}
}
