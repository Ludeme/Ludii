package app.display.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import app.DesktopApp;
import app.display.dialogs.util.DialogUtil;

public class ReconstructionDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	final JTextField txtcommonresoutput;
	JTextField textFieldMaxRecons;
	JTextField textFieldCSNScore;
	JTextField textFieldConceptScore;
	JTextField textFieldPlayability;

	/**
	 * Launch the application.
	 */
	public static void createAndShowGUI()
	{
		try
		{
			final ReconstructionDialog dialog = new ReconstructionDialog();
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
	public ReconstructionDialog()
	{
		setTitle("Reconstruction");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		final JLabel lblOutputPath = new JLabel("Output Path");
		lblOutputPath.setBounds(12, 18, 149, 38);
		contentPanel.add(lblOutputPath);
		
		txtcommonresoutput = new JTextField();
		txtcommonresoutput.setText("/common/res/output/");
		txtcommonresoutput.setBounds(167, 30, 220, 19);
		contentPanel.add(txtcommonresoutput);
		txtcommonresoutput.setColumns(10);
		{
			final JLabel lblMaximumNumber = new JLabel("Maximum Number");
			lblMaximumNumber.setBounds(12, 61, 149, 38);
			contentPanel.add(lblMaximumNumber);
		}
		{
			textFieldMaxRecons = new JTextField();
			textFieldMaxRecons.setText("100");
			textFieldMaxRecons.setColumns(10);
			textFieldMaxRecons.setBounds(280, 71, 130, 19);
			contentPanel.add(textFieldMaxRecons);
		}
		{
			final JLabel lblCsnScore = new JLabel("CSN Score");
			lblCsnScore.setBounds(12, 132, 149, 38);
			contentPanel.add(lblCsnScore);
		}
		{
			final JLabel lblConceptScore = new JLabel("Concept Score");
			lblConceptScore.setBounds(12, 161, 149, 38);
			contentPanel.add(lblConceptScore);
		}
		{
			final JLabel lblPlayability = new JLabel("Playability");
			lblPlayability.setBounds(12, 190, 149, 38);
			contentPanel.add(lblPlayability);
		}
		{
			textFieldCSNScore = new JTextField();
			textFieldCSNScore.setText("1.0");
			textFieldCSNScore.setColumns(10);
			textFieldCSNScore.setBounds(280, 142, 130, 19);
			contentPanel.add(textFieldCSNScore);
		}
		{
			textFieldConceptScore = new JTextField();
			textFieldConceptScore.setText("1.0");
			textFieldConceptScore.setColumns(10);
			textFieldConceptScore.setBounds(280, 171, 130, 19);
			contentPanel.add(textFieldConceptScore);
		}
		{
			textFieldPlayability = new JTextField();
			textFieldPlayability.setText("1.0");
			textFieldPlayability.setColumns(10);
			textFieldPlayability.setBounds(280, 200, 130, 19);
			contentPanel.add(textFieldPlayability);
		}
		
		final JCheckBox chckbxRankByContext = new JCheckBox("Rank by context");
		chckbxRankByContext.setSelected(true);
		chckbxRankByContext.setBounds(10, 110, 168, 23);
		final ActionListener chckbxRankByContextListener = new ActionListener()
		{
			@Override
			public void actionPerformed(final ActionEvent arg0)
			{
				if (chckbxRankByContext.isSelected())
				{
					textFieldCSNScore.setEnabled(true);
					textFieldConceptScore.setEnabled(true);
					textFieldPlayability.setEnabled(true);
				}
				else
				{
					textFieldCSNScore.setEnabled(false);
					textFieldConceptScore.setEnabled(false);
					textFieldPlayability.setEnabled(false);
				}
			}
		};
		chckbxRankByContext.addActionListener(chckbxRankByContextListener);
		contentPanel.add(chckbxRankByContext);
		
		final JButton buttonSelectDir = new JButton("");
		buttonSelectDir.setBounds(388, 30, 20, 18);
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
						final Integer maxRecons = Integer.valueOf(textFieldMaxRecons.getText());
						final Boolean rankByContext = chckbxRankByContext.isSelected();
						final Double csnScore = Double.valueOf(textFieldCSNScore.getText());
						final Double conceptScore = Double.valueOf(textFieldConceptScore.getText());
						final Double playabilityScore = Double.valueOf(textFieldPlayability.getText());
						
						System.out.println("output path = " + outputPath);
						System.out.println("max recons = " + maxRecons);
						System.out.println("rank by context = " + rankByContext);
						System.out.println("csn score = " + csnScore);
						System.out.println("concept score = " + conceptScore);
						System.out.println("playability = " + playabilityScore);
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
		{
			final JButton cancelButton = new JButton("Cancel");
			cancelButton.setActionCommand("Cancel");
			buttonPane.add(cancelButton);
		}
		
		
		
	}
}
