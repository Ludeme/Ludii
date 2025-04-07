package app.boardMaker.display.panels.welcomePanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.types.play.ModeType;

public class WelcomePanel extends JPanel
{
	private Displayer displayer;
	private Maker maker;
	
	private boolean visible;
	
	public WelcomePanel(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		panel.add(Box.createVerticalGlue());
		
		JPanel p = new JPanel();
		JLabel label = new JLabel("Game name: ");
		p.add(label);
		JTextField textfield = new JTextField(10);
		p.add(textfield);
		panel.add(p);
		
		panel.add(Box.createVerticalStrut(5));
		
		p = new JPanel();
		label = new JLabel("Players: ");
		p.add(label);
		JSpinner players = new JSpinner(new SpinnerNumberModel(2, 1, Integer.MAX_VALUE, 1));
		p.add(players);
		panel.add(p);
		
		panel.add(Box.createVerticalStrut(5));
		
		p = new JPanel();
		label = new JLabel("Game mode: ");
		p.add(label);
		JComboBox<ModeType> mode = new JComboBox<ModeType>(ModeType.values());
		p.add(mode);
		panel.add(p);
		
		panel.add(Box.createVerticalStrut(5));
		
		JButton button = new JButton("New Game");
		button.setAlignmentX(CENTER_ALIGNMENT);
		button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				maker.setGameInfo(textfield.getText(), (Integer)players.getValue(), (ModeType)mode.getSelectedItem());
				displayer.getBoardMakerPane().createPanels();
				displayer.mainView();
				displayer.setSizes();
			}
		});
		panel.add(button);
		
		panel.add(Box.createVerticalGlue());
		
		add(panel);
		
		setOpaque(true);
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	
	public boolean visible() {
		return visible;
	}
}
