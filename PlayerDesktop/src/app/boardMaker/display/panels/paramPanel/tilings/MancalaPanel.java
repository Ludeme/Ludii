package app.boardMaker.display.panels.paramPanel.tilings;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.board.MancalaInfo;
import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.equipment.container.board.custom.MancalaBoard;
import game.types.board.StoreType;

public class MancalaPanel extends JPanel
{
	private Maker maker;
	private Displayer displayer;
	private PreviewListener pl;
	
	private JSpinner rowSpinner;
	private JSpinner colSpinner;
	private JSpinner storeSpinner;
	private JComboBox<StoreType> sBox;
	
	private Board board;
	
	public MancalaPanel(Maker maker) {
		super();
		
		this.maker = maker;
		this.displayer = maker.getDisplayer();

		pl = new PreviewListener(this, displayer.getPreviewPanel());
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));
		
		add(Box.createVerticalStrut(5));
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel("Rows: ");
		label.setToolTipText("Sets the number of rows on the board.");
		panel.add(label);
		rowSpinner = new JSpinner(new SpinnerNumberModel(2, 2, Integer.MAX_VALUE, 1));
		rowSpinner.addChangeListener(pl);
		panel.add(rowSpinner);
		add(panel);
		
		add(Box.createVerticalStrut(5));
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Columns: ");
		label.setToolTipText("Sets the number of columns on the board. If 0, as many columns as rows.");
		panel.add(label);
		colSpinner = new JSpinner(new SpinnerNumberModel(6,1,Integer.MAX_VALUE,1));
		colSpinner.addChangeListener(pl);
		panel.add(colSpinner);
		add(panel);
		
		add(Box.createVerticalStrut(5));
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Store type: ");
		label.setToolTipText("The type of the stores.");
		panel.add(label);
		sBox = new JComboBox<StoreType>(StoreType.values());
		sBox.setSelectedItem(StoreType.Outer);
		sBox.addActionListener(pl);
		panel.add(sBox);
		add(panel);
		
		add(Box.createVerticalStrut(5));
		
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		label = new JLabel("Stores: ");
		label.setToolTipText("The number of stores.");
		panel.add(label);
		storeSpinner = new JSpinner(new SpinnerNumberModel(2,1,Integer.MAX_VALUE,1));
		storeSpinner.addChangeListener(pl);
		panel.add(storeSpinner);
		add(panel);
		
		add(Box.createVerticalGlue());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new CreateButton(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				maker.setBoard(createInfo());
				//maker.getCurrentBoard().setContainerInfo(createInfo());
				displayer.mainView();	
			}
		}));
		buttonPanel.add(new CancelButton(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				displayer.mainView();
			}
		}));
		
		add(buttonPanel);
		
		add(Box.createVerticalStrut(5));

		displayer.getPreviewPanel().setBoard(createInfo());
	}

	public ContainerInfo createInfo() {
		return new MancalaInfo(maker,(int)rowSpinner.getValue(),(int)colSpinner.getValue(),
				(StoreType)sBox.getSelectedItem(),(int)storeSpinner.getValue());
	}

	public Board board() {
		return board;
	}
	
	public void createBoard() {
		board = new MancalaBoard((Integer)rowSpinner.getValue(), (Integer)colSpinner.getValue(), (StoreType)sBox.getSelectedItem(), (Integer)storeSpinner.getValue(), maker.largeStack(), null, null);
	}
}
