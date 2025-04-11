package app.boardMaker.display.panels.paramPanel.tilings;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import app.boardMaker.display.buttons.CancelButton;
import app.boardMaker.display.buttons.CreateButton;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import game.equipment.container.board.Board;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.dim.DimConstant;
import game.functions.dim.DimFunction;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.shape.concentric.Concentric;
import game.functions.graph.generators.shape.concentric.ConcentricShapeType;

public class ConcentricPanel extends OptionPanel implements  ItemListener
{
	private Displayer displayer;
	
	private JPanel cards;
	
	private JSpinner ringSpinner;
	private JSpinner stepSpinner;
	private JSpinner sideSpinner;
	private JComboBox<ConcentricShapeType> shapes;
	private JComboBox<String> cb;
	private JTextField dimensions;
	private int selectedShapeParameter;
	/* Midpoints -> 0 / JoinMidpoints -> 1 / JoinCorners -> 2 / Stagger -> 3*/
	private boolean[] parameters = new boolean[] {true,true,false,false};
	
	private PreviewListener pl;
	
	private Board board;

	public ConcentricPanel(Displayer displayer) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));	
		setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));
		
		this.displayer = displayer;
		pl = new PreviewListener(this,displayer.getPreviewPanel());
		
		add(Box.createVerticalStrut(5));
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		String[] comboBoxItems = new String[] {"Board ring shape: ", "Number of sides: ", "Number of cells per ring: "};
		cb = new JComboBox<String>(comboBoxItems);
		cb.setEditable(false);
		cb.addActionListener(pl);
		cb.addItemListener(this);
		
		JPanel card1 = new JPanel();
		shapes = new JComboBox<ConcentricShapeType>(ConcentricShapeType.values());
		shapes.setSelectedItem(ConcentricShapeType.Triangle);
		shapes.setToolTipText("Shape of board rings.");
		shapes.addActionListener(pl);
		card1.add(shapes);
		
		JPanel card2 = new JPanel();
		sideSpinner = new JSpinner(new SpinnerNumberModel(3,3,Integer.MAX_VALUE,1));
		sideSpinner.setToolTipText("Number of sides for a polygonal shape.");
		sideSpinner.addChangeListener(pl);
		card2.add(sideSpinner);
		
		JPanel card3 = new JPanel();
		dimensions = new JTextField();
		dimensions.setToolTipText("Number of cells per circular ring. Each number represent the number of cells on the corresponding ring, rings are separated by spaces.");
		dimensions.setText("5 5");
		dimensions.setColumns(10);
		dimensions.addActionListener(pl);
		card3.add(dimensions);
		
		cards = new JPanel(new CardLayout());
		cards.add(card1,comboBoxItems[0]);
		cards.add(card2,comboBoxItems[1]);
		cards.add(card3,comboBoxItems[2]);
		
		panel.add(cb);
		panel.add(cards);
		add(panel);
		
		add(Box.createVerticalStrut(5));
		
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel l = new JLabel("Number of rings: ");
		p.add(l);
		ringSpinner = new JSpinner(new SpinnerNumberModel(3, 1, Integer.MAX_VALUE, 1));
		ringSpinner.setToolTipText("Number of rings.");
		ringSpinner.addChangeListener(pl);
		p.add(ringSpinner);
		add(p);
		
		add(Box.createVerticalStrut(5));
		
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		l = new JLabel("Number of steps: ");
		p.add(l);
		stepSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		stepSpinner.setToolTipText("Number of steps for target boards with multiple sites per ring.");
		stepSpinner.addChangeListener(pl);
		p.add(stepSpinner);
		add(p);
		
		add(Box.createVerticalStrut(5));
		
		createGroupButton("mid", 0);
		
		add(Box.createVerticalStrut(5));
		
		createGroupButton("midj", 1);
		
		add(Box.createVerticalStrut(5));
		
		createGroupButton("cornerj", 2);
		
		add(Box.createVerticalStrut(5));
		
		createGroupButton("stagger", 3);
		
		add(Box.createVerticalStrut(5));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new CreateButton(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				createBoard();
				displayer.getBoardPanel().setBoard(board);
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
		
		add(Box.createVerticalGlue());
		
		createBoard();
		displayer.getPreviewPanel().setBoard(board);
	}
	
	private void createGroupButton(String label, int paramIdx) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel l = new JLabel((String) displayer.getStrings().get(label));
		l.setToolTipText((String) displayer.getStrings().get(label+"TT"));
		p.add(l);
		ButtonGroup group = new ButtonGroup();
		JRadioButton button = new JRadioButton((String) displayer.getStrings().get("on"));
		button.setSelected(parameters[paramIdx]);
		button.addActionListener(pl);
		button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// TODO Auto-generated method stub
				parameters[paramIdx] = true;
			}
		});
		group.add(button);
		p.add(button);
		button = new JRadioButton((String) displayer.getStrings().get("off"));
		button.setSelected(!parameters[paramIdx]);
		button.addActionListener(pl);
		button.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// TODO Auto-generated method stub
				parameters[paramIdx] = false;
			}
		});
		group.add(button);
		p.add(button);
		add(p);
	}
	
	@Override
	public void createBoard()
	{
		// TODO Auto-generated method stub
				BooleanFunction bf0 = (parameters[0]) ? new BooleanConstant(true) : new BooleanConstant(false);
				BooleanFunction bf1 = (parameters[1]) ? new BooleanConstant(true) : new BooleanConstant(false);
				BooleanFunction bf2 = (parameters[2]) ? new BooleanConstant(true) : new BooleanConstant(false);
				BooleanFunction bf3 = (parameters[3]) ? new BooleanConstant(true) : new BooleanConstant(false);
				
				switch (selectedShapeParameter)
				{
				case 0: {
					GraphFunction graph = Concentric.construct((ConcentricShapeType)shapes.getSelectedItem(), null, null, new DimConstant((int)ringSpinner.getValue()), 
							((int)stepSpinner.getValue() == 0) ? null : new DimConstant((int) stepSpinner.getValue()), bf0, bf1, bf2, bf3);
					board = new Board(graph, null, null, null, null, null, null);
					break;
				}
				case 1: {
					GraphFunction graph = Concentric.construct(null, new DimConstant((int)sideSpinner.getValue()), null, new DimConstant((int)ringSpinner.getValue()), 
							((int)stepSpinner.getValue() == 0) ? null : new DimConstant((int) stepSpinner.getValue()), bf0, bf1, bf2, bf3);
					board = new Board(graph, null, null, null, null, null, null);
					break;
				}
				case 2: {
					if (isCorrectFormat(dimensions.getText())) {
						GraphFunction graph = Concentric.construct(null, null, parseDimensions(), new DimConstant((int)ringSpinner.getValue()), 
								((int)stepSpinner.getValue() == 0) ? null : new DimConstant((int) stepSpinner.getValue()), bf0, bf1, bf2, bf3);
						board = new Board(graph, null, null, null, null, null, null);
					} else {
						// dialog to reformat
						JOptionPane.showMessageDialog(this, "Dimensions does not respect the right format.\n"
								+ "Input should be composed of integers separated by a blank space where each number represents "
								+ "the number of cells in the corresponding ring.\n"
								+ "Example of valid input: 3 4 5 ...");
					}
					break;
				}
				default:
					break;
				}
	}

	private DimFunction[] parseDimensions()
	{
		ArrayList<DimConstant> d = new ArrayList<DimConstant>();
		for (char n : dimensions.getText().toCharArray()) {
			if (Character.isDigit(n)) {
				d.add(new DimConstant(Character.getNumericValue(n)));
			}
		}
		DimConstant[] array = new DimConstant[d.size()];
		return d.toArray(array);
	}

	private boolean isCorrectFormat(String text)
	{
		for (char n : dimensions.getText().toCharArray()) {
			if (!Character.isDigit(n) && !Character.isWhitespace(n)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Board board()
	{
		return board;
	}

	@Override
	public void itemStateChanged(ItemEvent e)
	{
		CardLayout cl = (CardLayout) cards.getLayout();
		cl.show(cards, (String) e.getItem());
		selectedShapeParameter = cb.getSelectedIndex();
	}

}
