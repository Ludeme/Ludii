package app.boardMaker.handlers;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.westPanel.WestPanel;

import app.boardMaker.display.panels.boardPanel.BoardPanel;
import app.boardMaker.display.panels.paramPanel.ParamPanel;
import app.boardMaker.display.panels.pawnPanel.PawnPanel;
import app.boardMaker.display.panels.previewPanel.PreviewPanel;
import app.boardMaker.display.tabbedBar.BoardMakerTabbedBar;
import app.boardMaker.display.window.BoardMakerFrame;
import app.boardMaker.display.window.BoardMakerPane;
import game.types.board.SiteType;
import game.types.play.ModeType;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

/**
 * Graphics handler of the desktop app
 */

public class Displayer
{
	private Maker maker;
	
	/** Main frame */
	private BoardMakerFrame frame;
	/** Parent pane of the board maker */
	private BoardMakerPane boardMakerPane;
	/** Main tabbed bar */
	private BoardMakerTabbedBar tabbedBar;
	/** Board display panel */
	private BoardPanel boardPanel;
	/** Board parameter panel */
	private ParamPanel paramPanel;
	/** Panel containing list of pawns */
	private PawnPanel pawnPanel;
	/** Panel containing the board preview */
	private PreviewPanel previewPanel;

	private WestPanel westPanel;
	
	public Displayer(Maker maker) {
		this.maker = maker;
	}
	
	public void createWindow() {
		frame = new BoardMakerFrame(maker);
		frame.requestFocus();
		//setSizes();
	}
	
	/**
	 * Switches to the main view
	 */
	public void mainView() {
		boardMakerPane.invalidate();
		boardMakerPane.removeAll();
		boardMakerPane.setLayout(new BorderLayout());

		previewPanel.visibility(false);
		paramPanel.visibility(false);

		boardMakerPane.add(tabbedBar,BorderLayout.NORTH);

		boardPanel.visibility(true);
		boardMakerPane.add(boardPanel,BorderLayout.CENTER);

		westPanel.visibility(true);
		boardMakerPane.add(westPanel,BorderLayout.WEST);

		boardMakerPane.revalidate();
		boardMakerPane.repaint();
	}
	
	/**
	 * Switches to the creation view
	 */
	public void creationView() {
		boardMakerPane.invalidate();
		boardMakerPane.removeAll();
		boardMakerPane.setLayout(new BorderLayout());

		boardMakerPane.add(tabbedBar,BorderLayout.NORTH);

		boardPanel.visibility(false);
		westPanel.visibility(false);

		paramPanel.visibility(true);
		boardMakerPane.add(paramPanel,BorderLayout.WEST);

		previewPanel.visibility(true);
		boardMakerPane.add(previewPanel,BorderLayout.CENTER);

		boardMakerPane.revalidate();
		boardMakerPane.repaint();
	}

	/**
	 * Switches to the creation view for polygonal board shape
	 */
	public void creationViewPoly() {
		boardMakerPane.invalidate();
		boardMakerPane.removeAll();
		boardMakerPane.setLayout(new BorderLayout());

		boardMakerPane.add(tabbedBar,BorderLayout.NORTH);

		boardPanel.visibility(false);
		westPanel.visibility(false);

		paramPanel.visibility(true);
		boardMakerPane.add(paramPanel,BorderLayout.WEST);

		previewPanel.visibility(true);
		boardMakerPane.add(previewPanel,BorderLayout.EAST);

		boardMakerPane.revalidate();
		boardMakerPane.repaint();
	}

	public void welcomeView() {
		boardMakerPane.invalidate();
		boardMakerPane.removeAll();
		boardMakerPane.setLayout(new BoxLayout(boardMakerPane,BoxLayout.Y_AXIS));

		boardMakerPane.add(Box.createVerticalGlue());
		boardMakerPane.add(new WelcomePanel(),BorderLayout.CENTER);
		boardMakerPane.add(Box.createVerticalGlue());

		boardMakerPane.revalidate();
		boardMakerPane.repaint();
	}
	
	/**
	 * Sets the different sizes of the panels
	 */
	public void setSizes() {
		Dimension size = new Dimension(boardMakerPane.getWidth() / 2, boardMakerPane.getHeight());

		boardPanel.setPreferredSize(size);
		boardPanel.setMinimumSize(size);
		
		previewPanel.setPreferredSize(size);
		previewPanel.setMinimumSize(size);
		
		size = new Dimension(boardMakerPane.getWidth() / 4, boardMakerPane.getHeight());

		//paramPanel.setPreferredSize(size);
		//paramPanel.setMaximumSize(size);

		westPanel.setPreferredSize(size);
		westPanel.setMaximumSize(size);
	}
	
	//----------------------------------------------------------
	
	public BoardMakerFrame getFrame() {
		return frame;
	}
	
	public BoardMakerPane getBoardMakerPane() {
		return boardMakerPane;
	}
	
	public BoardMakerTabbedBar getTabbedBar() {
		return tabbedBar;
	}
	
	public BoardPanel getBoardPanel() {
		return boardPanel;
	}
	
	public ParamPanel getParamPanel() {
		return paramPanel;
	}
	
	public PreviewPanel getPreviewPanel() {
		return previewPanel;
	}

	public JTabbedPane getCurrentDisplay() {
		if (previewPanel.visible()) {
			return previewPanel;
		} else {
			return boardPanel;
		}
	}
	
	//----------------------------------------------------------
	public void setBoardMakerPane(BoardMakerPane bmp) {
		boardMakerPane = bmp;
	}
	
	public void setTabbedBar(BoardMakerTabbedBar tb) {
		tabbedBar = tb;
	}
	
	public void setBoardPanel(BoardPanel bp) {
		boardPanel = bp;
	}
	
	public void setParamPanel(ParamPanel pp) {
		paramPanel = pp;
	}
	
	public void setPawnPanel(PawnPanel pp) {
		pawnPanel = pp;
	}
	
	public void setPreviewPanel(PreviewPanel pp) {
		previewPanel = pp;
	}

	public void setWestPanel(WestPanel bl) {
		westPanel = bl;
	}

	private class WelcomePanel extends JPanel {
		public WelcomePanel() {

			JPanel main = new JPanel(new BorderLayout());
			JPanel left = new JPanel();
			left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
			JPanel right = new JPanel();
			right.setLayout(new BoxLayout(right,BoxLayout.Y_AXIS));

			left.add(Box.createVerticalGlue());
			right.add(Box.createVerticalGlue());

			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JLabel label = new JLabel("Game name: ");
			p.add(label);
			left.add(p);
			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JTextField textfield = new JTextField("New Game",10);
			p.add(textfield);
			right.add(p);

			left.add(Box.createVerticalStrut(5));
			right.add(Box.createVerticalStrut(5));

			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			label = new JLabel("Players: ");
			p.add(label);
			left.add(p);
			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JSpinner players = new JSpinner(new SpinnerNumberModel(2, 0, 16, 1));
			p.add(players);
			right.add(p);

			left.add(Box.createVerticalStrut(5));
			right.add(Box.createVerticalStrut(5));

			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			label = new JLabel("Game mode: ");
			p.add(label);
			left.add(p);
			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JComboBox<ModeType> mode = new JComboBox<>(ModeType.values());
			mode.removeItem(ModeType.Simulation);
			p.add(mode);
			right.add(p);

			left.add(Box.createVerticalStrut(5));
			right.add(Box.createVerticalStrut(5));

			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			label = new JLabel("Site: ");
			p.add(label);
			left.add(p);
			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JComboBox<SiteType> site = new JComboBox<>(SiteType.values());
			site.setSelectedItem(SiteType.Cell);
			p.add(site);
			right.add(p);

			JPanel buttonPanel = new JPanel();
			JButton button = new CreateButton(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					maker.setGameInfo(textfield.getText(),(Integer) players.getValue(),
							(ModeType) mode.getSelectedItem(), (SiteType) site.getSelectedItem());
					boardMakerPane.createPanels();
					setSizes();
					mainView();
				}
			});
			buttonPanel.add(button);
			button = new JButton("Load");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//TODO load save file
					System.out.println("load a save file");
					maker.setGameInfo(textfield.getText(),(Integer) players.getValue(),
							(ModeType) mode.getSelectedItem(), (SiteType) site.getSelectedItem());
					boardMakerPane.createPanels();
					setSizes();
					mainView();
				}
			});
			//buttonPanel.add(button);
			main.add(left,BorderLayout.WEST);
			main.add(right,BorderLayout.EAST);
			main.add(buttonPanel,BorderLayout.SOUTH);
			main.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

			add(main);
		}
	}
}
