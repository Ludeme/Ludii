package app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing;

import app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.View.ExpEdgeComponent;
import app.display.dialogs.visual_editor.LayoutManagement.GraphDrawing.View.ExpNodeComponent;
import app.display.dialogs.visual_editor.LayoutManagement.GraphFactory.GraphCreator;
import app.display.dialogs.visual_editor.LayoutManagement.GraphFactory.GraphGeometry;
import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.model.MetaGraph.ExpGraph;
import app.display.dialogs.visual_editor.model.MetaGraph.ExpNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel where graph to be drawn
 * @author nic0gin
 */

public class GraphPanel extends JPanel
{

    private static LayoutHandler lm;
    private ExpGraph expGraph;

    private List<ExpEdgeComponent> edgeComponentList;
    private List<ExpNodeComponent> nodeComponentList;

    public GraphPanel(Timer timer)
    {

        // initialise graph for the panel
        GraphCreator gc = new GraphGeometry();
        expGraph = (ExpGraph) gc.createGraph();

        // initialise layout manager
        lm = new LayoutHandler(expGraph, 1);

        add(getMenuBar(timer));

        // Set up edge components
        // Set up node components
        constructVComps();
    }

    public void constructVComps()
    {
        // TODO
        edgeComponentList = new ArrayList<ExpEdgeComponent>();
        expGraph.getEdgeList().forEach((e) -> edgeComponentList.add(new ExpEdgeComponent(expGraph, e)));

        // TODO
        nodeComponentList = new ArrayList<ExpNodeComponent>();
        expGraph.getNodeList().forEach((id,n) -> nodeComponentList.add(new ExpNodeComponent((ExpNode) n)));
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        // draw edges
        edgeComponentList.forEach((e)->e.drawEdge((Graphics2D) g));
        // draw nodes
        nodeComponentList.forEach((n)-> n.drawNode(g));

    }

    private JMenuBar getMenuBar(Timer timer) {
        JMenuBar jMenuBar = new JMenuBar();
        JMenu jMenuFD = new JMenu("Force-Directed Layout");
        JMenuItem fdLStart = new JMenuItem("Start");
        JMenuItem fdLSettings = new JMenuItem("Settings");
        JMenuItem fdLStop = new JMenuItem("Stop");

        fdLStart.addActionListener(new StartListener(timer));
        fdLSettings.addActionListener(new OpenSettings(timer));
        fdLStop.addActionListener(new StopListener(timer));

        jMenuFD.add(fdLStart);
        jMenuFD.add(fdLSettings);
        jMenuFD.add(fdLStop);
        jMenuBar.add(jMenuFD);

        return jMenuBar;
    }

    public static class StartListener implements ActionListener
    {

        private final Timer timer;

        public StartListener(Timer timer)
        {
            this.timer = timer;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            timer.start();
        }
    }

    public static class StopListener implements ActionListener {

        private final Timer timer;

        public StopListener(Timer timer) {

            this.timer = timer;

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            timer.stop();
        }
    }

    private class OpenSettings implements ActionListener {

        private final Timer timer;

        private OpenSettings(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new SettingsFrame(timer);
        }
    }

    public LayoutHandler getLayoutManager() {
        return lm;
    }

    public ExpGraph getExpGraph() {
        return expGraph;
    }
}

