package app.display.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
import reconstruction.ReconstructionGenerator;

/**
 * @author Matthew.Stephenson and Eric.Piette
 */
public class ReconstructionDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	final JTextField txtOutput;
	final JTextField txtInput;
	final JTextField txtData;
	JTextField textFieldMaxRecons;
	JTextField textFieldCSNScore;
	JTextField textFieldConceptScore;
	final JTextField textFieldMaxTries;
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
		setResizable(false);
		setBounds(100, 100, 450, 350);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		// Input Path
		final JLabel lblInputPath = new JLabel("Input Path");
		lblInputPath.setBounds(12, 34, 130, 25);
		contentPanel.add(lblInputPath);
		
		txtInput = new JTextField();
		txtInput.setText(".");
		txtInput.setBounds(167, 38, 220, 19);
		contentPanel.add(txtInput);
		txtInput.setColumns(10);
		
		final JButton buttonSelectReconstruction = new JButton("");
		buttonSelectReconstruction.setBounds(388, 38, 20, 18);
		final ActionListener buttonListenerReconstruction = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				final JFileChooser fileChooser = DesktopApp.gameFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setDialogTitle("Select input reconstruction.");
				final int gameReturnVal = fileChooser.showOpenDialog(DesktopApp.frame());
				final File fileReconstruction;

				if (gameReturnVal == JFileChooser.APPROVE_OPTION)
					fileReconstruction = fileChooser.getSelectedFile();
				else
					fileReconstruction = null;

				if (fileReconstruction != null && fileReconstruction.exists())
				{
					txtInput.setText(fileReconstruction.getPath());
				}
				else
				{
					System.err.println("Could not find input file.");
				}
			}
		};
		buttonSelectReconstruction.addActionListener(buttonListenerReconstruction);
		contentPanel.add(buttonSelectReconstruction);
		
		// Output Path
		final JLabel lblOutputPath = new JLabel("Output Path");
		lblOutputPath.setBounds(12, 58, 149, 38);
		contentPanel.add(lblOutputPath);
		
		txtOutput = new JTextField();
		txtOutput.setText(".");
		txtOutput.setBounds(167, 68, 220, 19);
		contentPanel.add(txtOutput);
		txtOutput.setColumns(10);
		
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
					txtOutput.setText(directory.getPath());
				}
				else
				{
					System.err.println("Could not find output directory.");
				}
			}
		};
		buttonSelectDir.addActionListener(buttonListener);
		contentPanel.add(buttonSelectDir);
		
		// Data Path
		final JLabel lblDataPath = new JLabel("Data Path");
		lblDataPath.setBounds(12, 90, 149, 38);
		contentPanel.add(lblDataPath);
		
		txtData = new JTextField();
		txtData.setText(".");
		txtData.setBounds(167, 99, 220, 19);
		contentPanel.add(txtData);
		txtData.setColumns(10);
		
		final JButton buttonSelectDataDir = new JButton("");
		buttonSelectDataDir.setBounds(388, 98, 20, 18);
		final ActionListener buttonDataListener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				final JFileChooser fileChooser = DesktopApp.jsonFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogTitle("Select data directory.");
				final int jsonReturnVal = fileChooser.showOpenDialog(DesktopApp.frame());
				final File directory;

				if (jsonReturnVal == JFileChooser.APPROVE_OPTION)
					directory = fileChooser.getSelectedFile();
				else
					directory = null;

				if (directory != null && directory.exists())
				{
					txtData.setText(directory.getPath());
				}
				else
				{
					System.err.println("Could not find output directory.");
				}
			}
		};
		buttonSelectDataDir.addActionListener(buttonDataListener);
		contentPanel.add(buttonSelectDataDir);
		
		// Maximum Attempts
		final JLabel lblMaximumTries = new JLabel("Maximum Attempts");
		lblMaximumTries.setBounds(12, 118, 149, 38);
		contentPanel.add(lblMaximumTries);
		
		textFieldMaxTries = new JTextField();
		textFieldMaxTries.setText("10000");
		textFieldMaxTries.setColumns(10);
		textFieldMaxTries.setBounds(280, 128, 130, 19);
		contentPanel.add(textFieldMaxTries);
		
		// Maximum Recons
		final JLabel lblMaximumNumber = new JLabel("Number Results");
		lblMaximumNumber.setBounds(12, 150, 149, 38);
		contentPanel.add(lblMaximumNumber);
		textFieldMaxRecons = new JTextField();
		textFieldMaxRecons.setText("10");
		textFieldMaxRecons.setColumns(10);
		textFieldMaxRecons.setBounds(280, 160, 130, 19);
		contentPanel.add(textFieldMaxRecons);
		
		// Historical Weight
		final JLabel lblCsnScore = new JLabel("Historical Weight");
		lblCsnScore.setBounds(12, 182, 149, 38);
		contentPanel.add(lblCsnScore);
		textFieldCSNScore = new JTextField();
		textFieldCSNScore.setText("0.5");
		textFieldCSNScore.setColumns(10);
		textFieldCSNScore.setBounds(280, 192, 130, 19);
		contentPanel.add(textFieldCSNScore);
		
		// Conceptual Weight
		final JLabel lblConceptScore = new JLabel("Conceptual Weight");
		lblConceptScore.setBounds(12, 214, 149, 38);
		contentPanel.add(lblConceptScore);
		textFieldConceptScore = new JTextField();
		textFieldConceptScore.setText("0.5");
		textFieldConceptScore.setColumns(10);
		textFieldConceptScore.setBounds(280, 224, 130, 19);
		contentPanel.add(textFieldConceptScore);
		
		
		// Ok Button
		final JButton okButton = new JButton("OK");
		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		{
			
			okButton.setActionCommand("OK");
			buttonPane.add(okButton);
			getRootPane().setDefaultButton(okButton);
			okButton.setEnabled(true);
			
			final ActionListener okButtonListener = new ActionListener()
			{
				@Override
				public void actionPerformed(final ActionEvent arg0)
				{
					try
					{
						final String outputPath = txtOutput.getText();
						final String selectedLudPath = txtInput.getText();
						final Integer numRecons = Integer.valueOf(textFieldMaxRecons.getText());
						final Integer maxTries = Integer.valueOf(textFieldMaxTries.getText());
						Double csnScore = Double.valueOf(textFieldCSNScore.getText());
						Double conceptScore = Double.valueOf(textFieldConceptScore.getText());
						//Double geoScore = Double.valueOf(textFieldGeographical.getText());
						double totalWeight = csnScore.doubleValue() + conceptScore.doubleValue();
						double csnWeight = csnScore.doubleValue() / totalWeight;
						double conceptWeight = conceptScore.doubleValue() / totalWeight;
						//double geoWeight = geoScore.doubleValue() / totalWeight;
						
						System.out.println("the selected lud path is " + selectedLudPath);
						
						ReconstructionGenerator.reconstruction(outputPath + File.separatorChar, numRecons.intValue(), maxTries.intValue(), conceptWeight, csnWeight, 0, selectedLudPath, "");
					}
					catch (final Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			okButton.addActionListener(okButtonListener);
		}
	}
}
