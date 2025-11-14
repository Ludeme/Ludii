package app.boardMaker.display.panels.paramPanel.tilings;

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.board.graphFunction.GraphInfo;
import app.boardMaker.dataStruct.board.graphFunction.generator.*;
import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.polygonView.PolygonView;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import game.equipment.container.board.Board;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.celtic.Celtic;
import game.functions.graph.generators.basis.hex.Hex;
import game.functions.graph.generators.basis.square.DiagonalsType;
import game.functions.graph.generators.basis.square.Square;
import game.functions.graph.generators.basis.tiling.Tiling;
import game.functions.graph.generators.basis.tiling.TilingType;
import game.functions.graph.generators.basis.tri.Tri;
import game.util.graph.Poly;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class CustomShapePanel extends OptionPanel implements ItemListener {
    private Maker maker;
    private Displayer displayer;
    private PolygonView pv;
    private PreviewListener pl;

    private JPanel cards;
    private JTabbedPane pvContainer;

    private final String[] tilings = {"Celtic", "Hexagon", "Semi-regular" ,"Square", "Triangle"};
    private JComboBox<String> tilingCBox;
    private JComboBox<DiagonalsType> diagCBox;
    private JComboBox<TilingType> tileTypeCBox;

    private Board board;

    public CustomShapePanel(Maker maker) {
        super();
        this.maker = maker;
        this.displayer = maker.getDisplayer();

        pl = new PreviewListener(this,displayer.getPreviewPanel());
        pv = new PolygonView(displayer,this);
        pvContainer = new JTabbedPane();
        pvContainer.addTab("Polygon",pv);
        displayer.getBoardMakerPane().add(pvContainer,BorderLayout.CENTER);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));

        add(Box.createVerticalStrut(5));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Tiling: ");
        p.add(label);

        tilingCBox = new JComboBox<>(tilings);
        tilingCBox.setSelectedItem("Square");
        tilingCBox.addActionListener(pl);
        tilingCBox.addItemListener(this);
        p.add(tilingCBox);
        add(p);

        add(Box.createVerticalStrut(5));

        cards = new JPanel(new CardLayout());
        cards.add(new JPanel(),"Empty");
        makeSquareCard();
        makeTilingCard();
        add(cards);
        ((CardLayout)cards.getLayout()).show(cards,"Square");

        add(Box.createVerticalStrut(5));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new CreateButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maker.setBoard(createInfo());
                //maker.getCurrentBoard().setContainerInfo(createInfo());
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

    private void makeSquareCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Diagonal type: ");
        label.setToolTipText("How to handle diagonals between opposite corners.");
        p.add(label);
        diagCBox = new JComboBox<>(DiagonalsType.values());
        diagCBox.setSelectedItem(DiagonalsType.Implied);
        diagCBox.addActionListener(pl);
        p.add(diagCBox);
        card.add(p);

        cards.add(card,"Square");
    }

    private void makeTilingCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel label = new JLabel("Tiling: ");
        label.setToolTipText("Sets the type of tiling.");
        p.add(label);
        tileTypeCBox = new JComboBox<>(TilingType.values());
        tileTypeCBox.addActionListener(pl);
        p.add(tileTypeCBox);
        card.add(p);

        cards.add(card,"Semi-regular");
    }

    @Override
    public void createBoard() {
        if (pv.getPoly().size() < 2) {
            board = null;
            return;
        }
        Poly poly = pv.makePoly();
        GraphFunction graph;
        String tiling = (String) tilingCBox.getSelectedItem();
        switch (tiling) {
            case "Celtic" :
                graph = new Celtic(poly,null);
                break;
            case "Square" :
                graph = Square.construct(poly,null,(DiagonalsType) diagCBox.getSelectedItem());
                break;
            case "Triangle" :
                graph = Tri.construct(poly,null);
                break;
            case "Hexagon" :
                graph = Hex.construct(poly,null);
                break;
            case "Semi-regular" :
                graph = Tiling.construct((TilingType) tileTypeCBox.getSelectedItem(),poly,null);
                break;
            default :
                graph = null;
                break;
        }
        board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
    }

    @Override
    public Board board() {
        return board;
    }

    @Override
    public ContainerInfo createInfo() {
        String tiling = (String) tilingCBox.getSelectedItem();
        GraphInfo info;
        if (pv.getPoly().size() <= 2) {
            return null;
        }
        switch (tiling) {
            case "Celtic" :
                System.out.println("celt");
                info = new CelticInfo(pv.getPoly());
                break;

            case "Hexagon" :
                info = new HexInfo(pv.getPoly());
                break;

            case "Square" :
                info = new SquareInfo(pv.getPoly(), (DiagonalsType) diagCBox.getSelectedItem());
                break;

            case "Semi-regular" :
                info = new SemiregularInfo(pv.getPoly(), (TilingType) tileTypeCBox.getSelectedItem());
                break;

            case "Triangle" :
                info = new TriangleInfo(pv.getPoly());
                break;

            default :
                info = null;
                break;
        }
        return new BoardInfo(maker,info);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        CardLayout cl = (CardLayout) cards.getLayout();
        String tiling = (String) tilingCBox.getSelectedItem();
        switch (tiling) {
            case "Celtic" :
                cl.show(cards,"Empty");
                break;

            case "Hexagon" :
                cl.show(cards,"Empty");
                break;

            case "Square" :
                cl.show(cards,tiling);
                break;

            case "Semi-regular" :
                cl.show(cards,tiling);
                break;

            case "Triangle" :
                cl.show(cards,"Empty");
                break;
                
            default : break;
        }
        displayer.getPreviewPanel().setBoard(createInfo());
    }
}
