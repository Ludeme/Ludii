package app.display.dialogs.visual_editor.LayoutManagement.GraphFactory;

import app.display.dialogs.visual_editor.model.MetaGraph.ExpGraph;
import app.display.dialogs.visual_editor.model.interfaces.iGraph;

import java.io.File;

public abstract class GraphCreator {

    protected File file;
    protected ExpGraph graph;

    public GraphCreator() {this.graph = new ExpGraph();}

    public GraphCreator(File file) {this.file = file;}

    public abstract iGraph createGraph();

}
