package app.boardMaker.display.dialogs;

import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.components.sliders.RotationSlider;
import app.boardMaker.handlers.Maker;
import app.boardMaker.res.Transformations;
import app.boardMaker.utils.BoardData;
import app.utils.SVGUtil;
import game.equipment.container.board.Board;
import game.functions.floats.FloatConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.*;
import game.rules.play.moves.nonDecision.effect.Select;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

public class MergeDialog extends JDialog {
    private Maker maker;

    private Transformations transformation;

    private BoardPlacementPanel left;
    private BoardPlacementPanel right;
    private ResultPanel center;

    private BoardData result;

    public MergeDialog(Maker maker, Transformations type) {
        this.maker = maker;
        this.transformation = type;
        this.result = new BoardData(maker);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModalityType(ModalityType.APPLICATION_MODAL);

        JPanel mainPanel = createMainPanel();

        setContentPane(mainPanel);
        requestFocus();

        pack();
        setVisible(true);
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        center = new ResultPanel();
        left = new BoardPlacementPanel();
        right = new BoardPlacementPanel();
        JPanel bottom = createButtonPanel();

        mainPanel.add(center,BorderLayout.CENTER);
        mainPanel.add(left, BorderLayout.WEST);
        mainPanel.add(right, BorderLayout.EAST);
        mainPanel.add(bottom,BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();

        panel.add(new CreateButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maker.addBoard(result);
                maker.getDisplayer().getCurrentDisplay().repaint();
                dispose();
            }
        }));
        panel.add(new CancelButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }));

        return panel;
    }

    private void computeBoard() {
        ArrayList<GraphFunction> boards = new ArrayList<>();
        if (left.board != null) {
            boards.add(left.board.getBoard().graphFunction());
        }
        if (right.board != null) {
            boards.add(right.board.getBoard().graphFunction());
        }
        GraphFunction graph;
        Board board;
        switch (transformation) {
            case Transformations.Merge :
                graph = new Merge(boards.toArray(new GraphFunction[0]),false);
                board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
                result.setBoard(board);
                break;

            case Transformations.Union :
                graph = new Union(boards.toArray(new GraphFunction[0]),false);
                board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
                result.setBoard(board);
                break;

            case Transformations.Intersect :
                graph = new Intersect(boards.toArray(new GraphFunction[0]));
                board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
                result.setBoard(board);
                break;

            default :
                break;
        }
        maker.drawBoard(result,center,null);
    }

    private class BoardPlacementPanel extends JPanel implements ActionListener, ChangeListener {
        BoardData board = null;
        GraphFunction baseFunction;

        JComboBox<String> boardCB;
        JSpinner shiftX;
        JSpinner shiftY;
        RotationSlider rotation;
        JSpinner scaleX;
        JSpinner scaleY;
        JSpinner skewVal;

        BoardPlacementPanel() {
            super(new BorderLayout());

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

            p.add(Box.createVerticalStrut(5));

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel label = new JLabel("Board: ");
            panel.add(label);
            boardCB = new JComboBox<>(maker.getBoardList().boards().keySet().toArray(new String[0]));
            boardCB.setSelectedIndex(-1);
            boardCB.addActionListener(this);
            panel.add(boardCB);
            p.add(panel);

            p.add(Box.createVerticalStrut(5));
            p.add(new JSeparator(SwingConstants.HORIZONTAL));
            p.add(Box.createVerticalStrut(5));

            panel = new JPanel();
            label = new JLabel("X: ");
            panel.add(label);
            shiftX = new JSpinner(new SpinnerNumberModel(0.0,-100.0,100.0,0.1));
            shiftX.addChangeListener(this);
            panel.add(shiftX);

            label = new JLabel("Y: ");
            panel.add(label);
            shiftY = new JSpinner(new SpinnerNumberModel(0.0,-100.0,100.0,0.1));
            shiftY.addChangeListener(this);
            panel.add(shiftY);
            panel.setBorder(BorderFactory.createTitledBorder("Translation"));
            p.add(panel);

            p.add(Box.createVerticalStrut(5));
            p.add(new JSeparator(SwingConstants.HORIZONTAL));
            p.add(Box.createVerticalStrut(5));

            panel = new JPanel();
            label = new JLabel("Angle: ");
            panel.add(label);
            rotation = new RotationSlider();
            rotation.addChangeListener(this);
            panel.add(rotation);
            panel.setBorder(BorderFactory.createTitledBorder("Rotation"));
            p.add(panel);

            p.add(Box.createVerticalStrut(5));
            p.add(new JSeparator(SwingConstants.HORIZONTAL));
            p.add(Box.createVerticalStrut(5));

            panel = new JPanel();
            label = new JLabel("X: ");
            panel.add(label);
            scaleX = new JSpinner(new SpinnerNumberModel(1.0,0.0,100.0,0.1));
            scaleX.addChangeListener(this);
            panel.add(scaleX);

            label = new JLabel("Y: ");
            panel.add(label);
            scaleY = new JSpinner(new SpinnerNumberModel(1.0,0.0,100.0,0.1));
            scaleY.addChangeListener(this);
            panel.add(scaleY);
            panel.setBorder(BorderFactory.createTitledBorder("Scale"));
            p.add(panel);

            p.add(Box.createVerticalStrut(5));
            p.add(new JSeparator(SwingConstants.HORIZONTAL));
            p.add(Box.createVerticalStrut(5));

            panel = new JPanel();
            label = new JLabel("Value: ");
            panel.add(label);
            skewVal = new JSpinner(new SpinnerNumberModel(0.0,-100.0,100.0,0.1));
            skewVal.addChangeListener(this);
            panel.add(skewVal);
            panel.setBorder(BorderFactory.createTitledBorder("Skew"));
            p.add(panel);

            add(p, BorderLayout.PAGE_START);
            setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (board == null) {
                board = new BoardData(maker);
            }
            baseFunction = maker.getBoardList().get((String) boardCB.getSelectedItem()).getBoard().graphFunction();
            board.setBoard(new Board(apply_placement(),null,null,null,null,
                    maker.getSiteType(),maker.largeStack()));
            computeBoard();
            center.repaint();
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            if (board == null) {
                return;
            }
            board.setBoard(new Board(apply_placement(),null,null,null,null,
                    maker.getSiteType(),maker.largeStack()));
            computeBoard();
            center.repaint();
        }

        private GraphFunction apply_placement() {
            GraphFunction function = baseFunction;
            if ((Double) shiftX.getValue() != 0.0 || (Double) shiftY.getValue() != 0.0) {
                function = new Shift(new FloatConstant(((Double) shiftX.getValue()).floatValue()),
                        new FloatConstant(((Double) shiftY.getValue()).floatValue()),null,function);
            }
            if (rotation.getValue() != 0) {
                function = new Rotate(new FloatConstant((float) rotation.getValue()), function);
            }
            if ((Double) scaleX.getValue() != 0.0 || (Double) scaleY.getValue() != 0.0) {
                function = new Scale(new FloatConstant(((Double) scaleX.getValue()).floatValue()),
                        new FloatConstant(((Double) scaleY.getValue()).floatValue()),new FloatConstant(1.0F),function);
            }
            if ((Double) skewVal.getValue() != 0.0) {
                function = new Skew(((Double) skewVal.getValue()).floatValue(),function);
            }
            return function;
        }
    }

    private class ResultPanel extends JPanel {
        ResultPanel() {
            setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            setPreferredSize(new Dimension(400,0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            g2d.setBackground(Color.white);
            g2d.clearRect(0,0,getWidth(),getHeight());

            if (result.getBoard() != null) {
                BufferedImage image = SVGUtil.createSVGImage(result.getSVG(), this.getWidth(),this.getHeight());
                g2d.drawImage(image,0
                        ,0,null);
            }
        }
    }
}
