package app.boardMaker.display.panels.paramPanel.tilings;

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.board.graphFunction.generator.RegularInfo;
import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.shape.Regular;
import game.functions.graph.generators.shape.ShapeStarType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegularPanel extends OptionPanel{
    private Maker maker;
    private Displayer displayer;
    private PreviewListener pl;

    private JSpinner nsides;
    private boolean star = false;

    private Board board;

    public RegularPanel(Maker maker) {
        super();
        this.maker = maker;
        this.displayer = maker.getDisplayer();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        pl = new PreviewListener(this, displayer.getPreviewPanel());

        add(Box.createVerticalStrut(5));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Sides: ");
        panel.add(label);
        nsides = new JSpinner(new SpinnerNumberModel(3,3,50,1));
        nsides.addChangeListener(pl);
        panel.add(nsides);
        add(panel);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel("Shape: ");
        panel.add(label);
        ButtonGroup group = new ButtonGroup();
        JRadioButton button = new JRadioButton("Star");
        button.addActionListener(pl);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                star = true;
            }
        });
        group.add(button);
        panel.add(button);
        button = new JRadioButton("Polygon");
        button.setSelected(true);
        button.addActionListener(pl);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                star = false;
            }
        });
        group.add(button);
        panel.add(button);
        add(panel);

        add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new CreateButton(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                maker.setBoard(createInfo());
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

    @Override
    public void createBoard() {
        GraphFunction graph = new Regular(star ? ShapeStarType.Star : null,new DimConstant((Integer)(nsides.getValue())));
        board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
    }

    @Override
    public Board board() {
        return board;
    }

    @Override
    public ContainerInfo createInfo() {
        RegularInfo info = new RegularInfo(star,(Integer)(nsides.getValue()));
        System.out.println("Createinfo" + star);
        return new BoardInfo(maker,info);
    }
}
