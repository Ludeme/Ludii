package app.boardMaker.display.panels.paramPanel.tilings;

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.board.graphFunction.generator.RepeatInfo;
import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.polygonView.PolygonView;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RepeatPanel extends OptionPanel{
    private Maker maker;
    private Displayer displayer;
    private PreviewListener pl;
    private PolygonView pv;

    private JSpinner nrows;
    private JSpinner ncol;
    private JSpinner itemStepx;
    private JSpinner itemStepy;
    private JSpinner repStepx;
    private JSpinner repStepy;

    private JTabbedPane pvContainer;

    public RepeatPanel(Maker maker) {
        super();
        this.maker = maker;
        this.displayer = maker.getDisplayer();

        pl = new PreviewListener(this,displayer.getPreviewPanel());
        pv = new PolygonView(displayer,this);
        pvContainer = new JTabbedPane();
        pvContainer.addTab("Polygon",pv);
        displayer.getBoardMakerPane().add(pvContainer, BorderLayout.CENTER);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Box.createVerticalStrut(5));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("N° of repeat: ");
        panel.add(label);
        nrows = new JSpinner(new SpinnerNumberModel(1,1,50,1));
        nrows.addChangeListener(pl);
        panel.add(nrows);
        add(panel);

        panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel("N° of items to repeat: ");
        panel.add(label);
        ncol = new JSpinner(new SpinnerNumberModel(1,1,50,1));
        ncol.addChangeListener(pl);
        panel.add(ncol);
        add(panel);

        panel = new JPanel(new FlowLayout( FlowLayout.LEFT));
        label = new JLabel("Step to next item: ");
        panel.add(label);
        label = new JLabel("X: ");
        panel.add(label);
        itemStepx = new JSpinner(new SpinnerNumberModel(1,0,50,0.01));
        itemStepx.addChangeListener(pl);
        panel.add(itemStepx);
        label = new JLabel("Y: ");
        panel.add(label);
        itemStepy = new JSpinner(new SpinnerNumberModel(1,0,50,0.01));
        itemStepy.addChangeListener(pl);
        panel.add(itemStepy);
        add(panel);

        panel = new JPanel(new FlowLayout( FlowLayout.LEFT));
        label = new JLabel("Step to next repetition: ");
        panel.add(label);
        label = new JLabel("X: ");
        panel.add(label);
        repStepx = new JSpinner(new SpinnerNumberModel(1,0,50,0.01));
        repStepx.addChangeListener(pl);
        panel.add(repStepx);
        label = new JLabel("Y: ");
        panel.add(label);
        repStepy = new JSpinner(new SpinnerNumberModel(1,0,50,0.01));
        repStepy.addChangeListener(pl);
        panel.add(repStepy);
        add(panel);

        add(Box.createVerticalStrut(5));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new CreateButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maker.setBoard(createInfo());
                displayer.mainView();
            }
        }));
        buttonPanel.add(new CancelButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayer.mainView();
            }
        }));
        add(buttonPanel);

        add(Box.createVerticalGlue());

        displayer.getPreviewPanel().setBoard(createInfo());
    }

    @Override
    public void createBoard() {

    }

    @Override
    public Board board() {
        return null;
    }

    @Override
    public ContainerInfo createInfo() {
        int rep = (int)nrows.getValue();
        int items = (int)ncol.getValue();
        float[] itemStep = new float[]{(float) ((double)itemStepx.getValue()),(float)((double)itemStepy.getValue())};
        float[] repStep = new float[]{(float)((double)repStepx.getValue()),(float)((double)repStepy.getValue())};
        RepeatInfo info = new RepeatInfo(pv.getPoly(),rep,items,itemStep,repStep);
        return new BoardInfo(maker,info);
    }
}
