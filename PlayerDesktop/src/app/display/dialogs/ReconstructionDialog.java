package app.display.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import app.DesktopApp;
import app.PlayerApp;
import app.display.dialogs.util.DialogUtil;
import main.FileHandling;

public class ReconstructionDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	final JTextField txtcommonresoutput;
	JTextField textFieldMaxRecons;
	JTextField textFieldCSNScore;
	JTextField textFieldConceptScore;
	JTextField textFieldPlayability;
	private final JTextField textField;
	static String selectedLudPath = "";

	/**
	 * Launch the application.
	 */
	public static void createAndShowGUI(final PlayerApp app)
	{
		try
		{
			final ReconstructionDialog dialog = new ReconstructionDialog(app);
			DialogUtil.initialiseSingletonDialog(dialog, "Reconstruction", null);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ReconstructionDialog(final PlayerApp app)
	{
		setTitle("Reconstruction");
		setBounds(100, 100, 450, 350);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		final JLabel lblOutputPath = new JLabel("Output Path");
		lblOutputPath.setBounds(12, 56, 149, 38);
		contentPanel.add(lblOutputPath);
		
		txtcommonresoutput = new JTextField();
		txtcommonresoutput.setText("/common/res/output/");
		txtcommonresoutput.setBounds(167, 68, 220, 19);
		contentPanel.add(txtcommonresoutput);
		txtcommonresoutput.setColumns(10);
		{
			final JLabel lblMaximumNumber = new JLabel("Playable Recons");
			lblMaximumNumber.setBounds(12, 99, 149, 38);
			contentPanel.add(lblMaximumNumber);
		}
		{
			textFieldMaxRecons = new JTextField();
			textFieldMaxRecons.setText("100");
			textFieldMaxRecons.setColumns(10);
			textFieldMaxRecons.setBounds(280, 109, 130, 19);
			contentPanel.add(textFieldMaxRecons);
		}
		{
			final JLabel lblCsnScore = new JLabel("Historical");
			lblCsnScore.setBounds(12, 170, 149, 38);
			contentPanel.add(lblCsnScore);
		}
		{
			final JLabel lblConceptScore = new JLabel("Concept");
			lblConceptScore.setBounds(12, 199, 149, 38);
			contentPanel.add(lblConceptScore);
		}
		{
			final JLabel lblPlayability = new JLabel("Quality");
			lblPlayability.setBounds(12, 228, 149, 38);
			contentPanel.add(lblPlayability);
		}
		{
			textFieldCSNScore = new JTextField();
			textFieldCSNScore.setText("1.0");
			textFieldCSNScore.setColumns(10);
			textFieldCSNScore.setBounds(280, 180, 130, 19);
			contentPanel.add(textFieldCSNScore);
		}
		{
			textFieldConceptScore = new JTextField();
			textFieldConceptScore.setText("1.0");
			textFieldConceptScore.setColumns(10);
			textFieldConceptScore.setBounds(280, 209, 130, 19);
			contentPanel.add(textFieldConceptScore);
		}
		{
			textFieldPlayability = new JTextField();
			textFieldPlayability.setText("1.0");
			textFieldPlayability.setColumns(10);
			textFieldPlayability.setBounds(280, 238, 130, 19);
			contentPanel.add(textFieldPlayability);
		}
		
		final JButton btnSelectGame = new JButton("Select Game");
		btnSelectGame.setBounds(12, 28, 130, 25);
		contentPanel.add(btnSelectGame);
		
		final JLabel selectedGameText = new JLabel("");
		selectedGameText.setBounds(177, 21, 233, 38);
		contentPanel.add(selectedGameText);
		
		btnSelectGame.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent e)
			{
				try
				{
		        	final String[] choices = FileHandling.listGames();
		        	
	        		String initialChoice = choices[0];
	        		for (final String choice : choices)
	        		{
	        			if (app.manager().savedLudName() != null && app.manager().savedLudName().endsWith(choice.replaceAll(Pattern.quote("\\"), "/")))
	        			{
	        				initialChoice = choice;
	        				break;
	        			}
	        		}
		        	
	        		selectedLudPath = GameLoaderDialog.showDialog(DesktopApp.frame(), choices, initialChoice);
	        		selectedGameText.setText(selectedLudPath.split("/")[selectedLudPath.split("/").length-1]);
				}
				catch (final Exception e1)
				{
					// Probably just cancelled the game loader.
				}
			}	
		});
		
		final JButton buttonSelectDir = new JButton("");
		buttonSelectDir.setBounds(388, 68, 20, 18);
		final ActionListener buttonListener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				final JFileChooser fileChooser = DesktopApp.jsonFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogTitle("Select output directory.");
				final int jsonReturnVal = fileChooser.showOpenDialog(DesktopApp.frame());
				final File directory;

				if (jsonReturnVal == JFileChooser.APPROVE_OPTION)
					directory = fileChooser.getSelectedFile();
				else
					directory = null;

				if (directory != null && directory.exists())
				{
					txtcommonresoutput.setText(directory.getPath());
				}
				else
				{
					System.err.println("Could not find output directory.");
				}
			}
		};
		buttonSelectDir.addActionListener(buttonListener);
		contentPanel.add(buttonSelectDir);
		
		final JLabel lblMaximumTries = new JLabel("Maximum Tries");
		lblMaximumTries.setBounds(12, 128, 149, 38);
		contentPanel.add(lblMaximumTries);
		
		textField = new JTextField();
		textField.setText("10000");
		textField.setColumns(10);
		textField.setBounds(280, 138, 130, 19);
		contentPanel.add(textField);
		
		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		{
			final JButton okButton = new JButton("OK");
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);
			
			final ActionListener okButtonListener = new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent arg0)
				{
					// TODO Cameron you can call your function with the parameters stored here
					try
					{
						final String outputPath = txtcommonresoutput.getText();
						final Integer playableRecons = Integer.valueOf(textFieldMaxRecons.getText());
						final Integer maxTries = Integer.valueOf(lblMaximumTries.getText());
						final Double csnScore = Double.valueOf(textFieldCSNScore.getText());
						final Double conceptScore = Double.valueOf(textFieldConceptScore.getText());
						final Double qualityScore = Double.valueOf(textFieldPlayability.getText());
						
						System.out.println(selectedLudPath);
					}
					catch (final Exception e)
					{
						// You probably entered a string instead of a number.
						e.printStackTrace();
					}
				}
			};
			okButton.addActionListener(okButtonListener);
		}
	}
}
