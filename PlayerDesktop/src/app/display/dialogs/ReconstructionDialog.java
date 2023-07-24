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
	final JTextField txtcommonresoutput;
	final JTextField txtcommonresinput;
	JTextField textFieldMaxRecons;
	JTextField textFieldCSNScore;
	JTextField textFieldConceptScore;
	//JTextField textFieldGeographical;
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
		setBounds(100, 100, 450, 350);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		final JLabel lblOutputPath = new JLabel("Output Path");
		lblOutputPath.setBounds(12, 56, 149, 38);
		contentPanel.add(lblOutputPath);
		
		final JLabel lblInputPath = new JLabel("Input Reconstruction");
		lblInputPath.setBounds(12, 28, 130, 25);
		contentPanel.add(lblInputPath);
		
		final JButton okButton = new JButton("OK");
		
		txtcommonresinput = new JTextField();
		txtcommonresinput.setText(".");
		txtcommonresinput.setBounds(167, 38, 220, 19);
		contentPanel.add(txtcommonresinput);
		txtcommonresinput.setColumns(10);
		
		txtcommonresoutput = new JTextField();
		txtcommonresoutput.setText(".");
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
			textFieldMaxRecons.setText("10");
			textFieldMaxRecons.setColumns(10);
			textFieldMaxRecons.setBounds(280, 109, 130, 19);
			contentPanel.add(textFieldMaxRecons);
		}
		{
			final JLabel lblCsnScore = new JLabel("Historical Weight");
			lblCsnScore.setBounds(12, 170, 149, 38);
			contentPanel.add(lblCsnScore);
		}
		{
			final JLabel lblConceptScore = new JLabel("Conceptual Weight");
			lblConceptScore.setBounds(12, 199, 149, 38);
			contentPanel.add(lblConceptScore);
		}
//		{
//			final JLabel lblGeographical = new JLabel("Geographical Weight");
//			lblGeographical.setBounds(12, 228, 149, 38);
//			contentPanel.add(lblGeographical);
//		}
		{
			textFieldCSNScore = new JTextField();
			textFieldCSNScore.setText("0.5");
			textFieldCSNScore.setColumns(10);
			textFieldCSNScore.setBounds(280, 180, 130, 19);
			contentPanel.add(textFieldCSNScore);
		}
		{
			textFieldConceptScore = new JTextField();
			textFieldConceptScore.setText("0.5");
			textFieldConceptScore.setColumns(10);
			textFieldConceptScore.setBounds(280, 209, 130, 19);
			contentPanel.add(textFieldConceptScore);
		}
//		{
//			textFieldGeographical = new JTextField();
//			textFieldGeographical.setText("0.0");
//			textFieldGeographical.setColumns(10);
//			textFieldGeographical.setBounds(280, 238, 130, 19);
//			contentPanel.add(textFieldGeographical);
//		}
		
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
					txtcommonresinput.setText(fileReconstruction.getPath());
				}
				else
				{
					System.err.println("Could not find input file.");
				}
			}
		};
		buttonSelectReconstruction.addActionListener(buttonListenerReconstruction);
		contentPanel.add(buttonSelectReconstruction);
		
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
		
		final JLabel lblMaximumTries = new JLabel("Maximum Attempts");
		lblMaximumTries.setBounds(12, 128, 149, 38);
		contentPanel.add(lblMaximumTries);
		
		textFieldMaxTries = new JTextField();
		textFieldMaxTries.setText("10000");
		textFieldMaxTries.setColumns(10);
		textFieldMaxTries.setBounds(280, 138, 130, 19);
		contentPanel.add(textFieldMaxTries);
		
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
						final String outputPath = txtcommonresoutput.getText();
						final String selectedLudPath = txtcommonresinput.getText();
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
