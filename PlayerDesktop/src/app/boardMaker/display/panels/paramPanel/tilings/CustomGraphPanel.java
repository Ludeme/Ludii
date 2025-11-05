package app.boardMaker.display.panels.paramPanel.tilings;

import app.boardMaker.dataStruct.board.BoardInfo;
import app.boardMaker.dataStruct.board.ContainerInfo;
import app.boardMaker.dataStruct.board.graphFunction.generator.CustomInfo;
import app.boardMaker.display.components.buttons.CancelButton;
import app.boardMaker.display.components.buttons.CreateButton;
import app.boardMaker.display.panels.graphView.GraphBuildingModes;
import app.boardMaker.display.panels.graphView.GraphListener;
import app.boardMaker.display.panels.graphView.GraphView;
import app.boardMaker.display.panels.previewPanel.PreviewListener;
import app.boardMaker.handlers.Displayer;
import app.boardMaker.handlers.Maker;
import app.boardMaker.utils.Edge;
import app.boardMaker.utils.Vertex;
import game.equipment.container.board.Board;
import game.functions.graph.GraphFunction;
import game.util.graph.Graph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class CustomGraphPanel extends OptionPanel {
    private Maker maker;
    private Displayer displayer;

    private PreviewListener pl;
    private GraphView gv;
    private GraphListener gl;

    private Board board;

    public CustomGraphPanel(Maker maker) {
        this.maker = maker;
        this.displayer = maker.getDisplayer();

        gv = new GraphView(maker,this);
        gl = new GraphListener(gv);
        gv.setListener(gl);
        JTabbedPane gvContainer = new JTabbedPane();
        gvContainer.addTab("Custom Graph",gv);
        displayer.getBoardMakerPane().add(gvContainer,BorderLayout.CENTER);


        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(displayer.getParamPanel().getWidth(), displayer.getParamPanel().getHeight()));

        pl = new PreviewListener(this, displayer.getPreviewPanel());

        add(Box.createVerticalStrut(5));

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup group = new ButtonGroup();

        JRadioButton button = new JRadioButton("Add vertex");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gl.setBuildMode(GraphBuildingModes.ADDVERTEX);
            }
        });
        button.setSelected(true);
        group.add(button);
        panel.add(button);

        button = new JRadioButton("Add edge");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gl.setBuildMode(GraphBuildingModes.ADDEDGE);
            }
        });
        group.add(button);
        panel.add(button);

        button = new JRadioButton("Remove vertex");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gl.setBuildMode(GraphBuildingModes.REMOVEVERTEX);
            }
        });
        group.add(button);
        panel.add(button);

        button = new JRadioButton("Remove edge");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gl.setBuildMode(GraphBuildingModes.REMOVEEDGE);
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
                createBoard();
                maker.addBoard(displayer.getPreviewPanel().getBoardData());
                if (displayer.getPreviewPanel().getTabCount() > 1) {
                    displayer.getPreviewPanel().removeTabAt(1);
                }
                maker.getCurrentBoard().setContainerInfo(createInfo());
                displayer.mainView();
            }
        }));
        buttonPanel.add(new CancelButton(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (displayer.getPreviewPanel().getTabCount() > 1) {
                    displayer.getPreviewPanel().removeTabAt(1);
                }
                displayer.mainView();
            }
        }));

        add(buttonPanel);

        add(Box.createVerticalStrut(5));

        createBoard();
        displayer.getPreviewPanel().setBoard(board);
    }

    @Override
    public void createBoard() {
        List<Vertex> vertex = gv.getVertexes();
        List<Edge> edge = gv.getEdges();

        Float[][] vertices = new Float[vertex.size()][2];
        Integer[][] edges = new Integer[edge.size()][2];

        for (int i = 0; i < vertex.size(); i++) {
            vertices[i] = vertex.get(i).convert();
        }

        for (int i = 0; i < edge.size(); i++) {
            edges[i] = new Integer[]{vertex.indexOf(edge.get(i).start()), vertex.indexOf(edge.get(i).end())};
        }

        GraphFunction graph = new Graph(vertices,edges);
        board = new Board(graph,null,null,null,null,maker.getSiteType(),maker.largeStack());
    }

    @Override
    public Board board() {
        return board;
    }

    @Override
    public ContainerInfo createInfo() {
        CustomInfo info = new CustomInfo(gv.getVertexes(),gv.getEdges());
        return new BoardInfo(maker,info);
    }
}
