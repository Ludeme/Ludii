package app.boardMaker.display.tabbedBar;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.*;

import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import app.utils.SVGUtil;
import game.types.board.SiteType;
import game.types.play.ModeType;
import game.types.play.RoleType;
import graphics.ImageUtil;
import graphics.svg.SVGtoImage;
import org.jfree.graphics2d.svg.SVGGraphics2D;

/**
 * Class representing the main tabbed bar of the board maker
 */

public class BoardMakerTabbedBar extends JTabbedPane
{
	private Displayer displayer;
	private Maker maker;
	
	private JToolBar toolbar;

	private GameTabListener gtl;
	
	private boolean visible;
	
	public BoardMakerTabbedBar(Maker maker) {
		this.displayer = maker.getDisplayer();
		this.maker = maker;
		
		displayer.setTabbedBar(this);

		gtl = new GameTabListener(maker);
		
		initToolbar();
		makeGameToolbar();
		addTab("Game", toolbar);
	}
	
	private void initToolbar() {
		toolbar = new JToolBar();
		toolbar.setRollover(true);
		toolbar.setFloatable(false);
	}
	
	/**
	 * Creates the game tab toolbar
	 */
	private void makeGameToolbar() {
		JLabel label = new JLabel("Name: " + maker.getName());
		toolbar.add(label);
		
		toolbar.addSeparator();
		
		label = new JLabel("Players: " + maker.getPlayers());
		toolbar.add(label);

		toolbar.addSeparator();
		
		label = new JLabel("Mode: " + maker.getMode());
		toolbar.add(label);
		
		toolbar.addSeparator();

		label = new JLabel("Site: " + maker.getSiteType());
		toolbar.add(label);

		toolbar.addSeparator();
		
		toolbar.add(new JSeparator(SwingConstants.VERTICAL));
		
		toolbar.addSeparator();

		label = new JLabel("Show: ");
		toolbar.add(label);
		
		JRadioButton button;
		ButtonGroup group = new ButtonGroup();
		button = new JRadioButton("Cells");
		button.setActionCommand("Cells");
		button.setSelected(maker.getSiteType() == SiteType.Cell);
		button.addActionListener(gtl);
		group.add(button);
		toolbar.add(button);
		button = new JRadioButton("Graph");
		button.setActionCommand("Graph");
		button.setSelected(maker.getSiteType() != SiteType.Cell);
		button.addActionListener(gtl);
		group.add(button);
		toolbar.add(button);
		
		toolbar.addSeparator();
		
		toolbar.add(new JSeparator(SwingConstants.VERTICAL));
		
		toolbar.addSeparator();
		
		button = new JRadioButton("Large stacks");
		button.setActionCommand("Stack");
		button.setToolTipText("Allows the game to involve stacks higher than 32.");
		button.setSelected(false);
		button.addActionListener(gtl);
		toolbar.add(button);

		toolbar.addSeparator();

		toolbar.add(new JSeparator(SwingConstants.VERTICAL));

		toolbar.addSeparator();

		label = new JLabel("Tool: ");
		toolbar.add(label);

		group = new ButtonGroup();
		button = new JRadioButton("Board");
		button.setActionCommand("Board");
		button.setSelected(true);
		button.addActionListener(gtl);
		group.add(button);
		toolbar.add(button);
		button = new JRadioButton("Setup");
		button.setActionCommand("Setup");
		button.addActionListener(gtl);
		group.add(button);
		toolbar.add(button);

		toolbar.addSeparator();

		toolbar.add(new JSeparator(SwingConstants.VERTICAL));

		toolbar.addSeparator();

		JButton playButton = new JButton("Load in Ludii");
		int size = 20;
		SVGGraphics2D g2d = new SVGGraphics2D(size, size);
		String filename = ImageUtil.getImageFullPath("button-play");
		SVGtoImage.loadFromFilePath(g2d,filename,new Rectangle(0,0,size,size),Color.black,Color.black,0);
		BufferedImage image = SVGUtil.createSVGImage(g2d.getSVGElement(),size,size);
		if (image != null) {
			ImageIcon icon = new ImageIcon(image);
			playButton.setIcon(icon);
		}
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				maker.writeAndPlay();
			}
		});
		toolbar.add(playButton);
	}
	
	public void visibility(boolean b) {
		visible = b;
	}
	
	public boolean visible() {
		return visible;
	}
}
