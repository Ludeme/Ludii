package app.boardMaker.display.dialogs;

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.dataStruct.board.graphFunction.operators.IntersectInfo;
import app.boardMaker.dataStruct.board.graphFunction.operators.MergeInfo;
import app.boardMaker.dataStruct.board.graphFunction.operators.ShiftInfo;
import app.boardMaker.dataStruct.board.graphFunction.operators.UnionInfo;
import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.handlers.Maker;
import app.boardMaker.res.Transformations;
import app.boardMaker.dataStruct.board.BoardData;
import app.utils.SVGUtil;
import game.equipment.container.board.Board;
import game.functions.floats.FloatConstant;
import game.functions.graph.GraphFunction;
import game.functions.graph.operators.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

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
                GraphInfo info;
                double lx = (Double) left.shiftX.getValue(), ly = (Double) left.shiftY.getValue();
                double rx = (Double) right.shiftX.getValue(), ry = (Double) right.shiftY.getValue();
                GraphInfo leftInfo;
                if (lx == 0 && ly == 0) {
                    leftInfo = left.graphInfo;
                } else {
                    leftInfo = new ShiftInfo((float) lx, (float) ly,left.graphInfo);
                }
                GraphInfo rightInfo;
                if (rx == 0 && ry == 0) {
                    rightInfo = right.graphInfo;
                } else {
                    rightInfo = new ShiftInfo((float) rx, (float) ry,right.graphInfo);
                }
                switch (transformation) {
                    case Merge :
                        info = new MergeInfo(leftInfo,rightInfo);
                        break;

                    case Union:
                        info = new UnionInfo(leftInfo,rightInfo);
                        break;

                    case Intersect :
                        info = new IntersectInfo(leftInfo,rightInfo);
                        break;

                    default :
                        info = null;
                        break;
                }
                result.setContainerInfo(new BoardInfo(maker,info));
                maker.setBoard(result);
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
        GraphInfo graphInfo;

        JComboBox<String> boardCB;
        JSpinner shiftX;
        JSpinner shiftY;

        BoardPlacementPanel() {
            super(new BorderLayout());

            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));

            p.add(Box.createVerticalStrut(5));

            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel label = new JLabel("Board: ");
            panel.add(label);
            boardCB = new JComboBox<>(maker.getItemList().boards().keySet().toArray(new String[0]));
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
            shiftX = new JSpinner(new SpinnerNumberModel(0.0,-50.0,50.0,0.1));
            shiftX.addChangeListener(this);
            panel.add(shiftX);

            label = new JLabel("Y: ");
            panel.add(label);
            shiftY = new JSpinner(new SpinnerNumberModel(0.0,-100.0,100.0,0.1));
            shiftY.addChangeListener(this);
            panel.add(shiftY);
            panel.setBorder(BorderFactory.createTitledBorder("Translation"));
            p.add(panel);

            add(p, BorderLayout.PAGE_START);
            setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (board == null) {
                board = new BoardData(maker);
            }
            baseFunction = maker.getItemList().get((String) boardCB.getSelectedItem()).getBoard().graphFunction();
            graphInfo = ((BoardInfo)maker.getItemList().get((String) boardCB.getSelectedItem()).getContainerInfo()).getGraphInfo();
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

            return function;
        }
    }

    private class ResultPanel extends JPanel {
        ResultPanel() {
            setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            setPreferredSize(new Dimension(500,500));
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
