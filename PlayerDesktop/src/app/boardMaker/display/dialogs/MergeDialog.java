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
import app.utils.SVGUtil;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Dialog used to merge/union/intersect boards
 */
public class MergeDialog extends JDialog {
    private Maker maker;
    // The transformation to use (merge, union or intersect)
    private Transformations transformation;
    // Left panel
    private BoardPlacementPanel left;
    // Right panel
    private BoardPlacementPanel right;
    // Center panel
    private ResultPanel center;
    // Result of transformation
    private BoardInfo resultInfo;

    public MergeDialog(Maker maker, Transformations type) {
        this.maker = maker;
        this.transformation = type;
        this.resultInfo = new BoardInfo(maker);

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
                maker.getItemList().addEmptyBoard();
                maker.setBoard(resultInfo);
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
        GraphInfo graph;
        switch (transformation) {
            case Transformations.Merge :
                graph = new MergeInfo(left.graphInfo,right.graphInfo);
                resultInfo.setGraphInfo(graph);
                break;

            case Transformations.Union :
                graph = new UnionInfo(left.graphInfo,right.graphInfo);
                resultInfo.setGraphInfo(graph);
                break;

            case Transformations.Intersect :
                graph = new IntersectInfo(left.graphInfo,right.graphInfo);
                resultInfo.setGraphInfo(graph);
                break;

            default :
                break;
        }
    }

    private class BoardPlacementPanel extends JPanel implements ActionListener, ChangeListener {
        GraphInfo baseInfo;
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
            baseInfo = ((BoardInfo)(maker.getItemList().get((String) boardCB.getSelectedItem()))).getGraphInfo();
            graphInfo = apply_placement(baseInfo);
            computeBoard();
            center.repaint();
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            graphInfo = apply_placement(baseInfo);
            computeBoard();
            center.repaint();
        }

        private GraphInfo apply_placement(GraphInfo info) {
            GraphInfo function = info;
            if ((Double) shiftX.getValue() != 0.0 || (Double) shiftY.getValue() != 0.0) {
                double sx = (Double) shiftX.getValue(), sy = (Double) shiftY.getValue();
                function = new ShiftInfo((float) sx, (float) sy,info);
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

            if (resultInfo.getBoard() != null) {
                BufferedImage image = SVGUtil.createSVGImage(resultInfo.getSVG(maker.getSiteType()), this.getWidth(),this.getHeight());
                g2d.drawImage(image,0
                        ,0,null);
            }
        }
    }
}
