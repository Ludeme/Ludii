package app.display.dialogs.visual_editor.view.panels.editor.gameEditor;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.panels.editor.GraphPanel;
import grammar.Grammar;
import main.grammar.Clause;

import javax.swing.*;
public class GameGraphPanel extends GraphPanel
{

    public GameGraphPanel(int width, int height)
    {
        super(width, height);
        Handler.gameDescriptionGraph = graph();
        Handler.gameGraphPanel = this;
        Handler.currentGraphPanel = this;
        Handler.addGraphPanel(graph(), this);
    }

    public void initialize(JScrollPane scrollPane)
    {
        super.initialize(scrollPane);

        Handler.recordUserActions = false;
        LudemeNode gameLudemeNode = Handler.addNode(graph(), Grammar.grammar().symbolsByName("Game").get(0), null,
                scrollPane.getViewport().getViewRect().x + (int)(scrollPane.getViewport().getViewRect().getWidth()/2),
                scrollPane.getViewport().getViewRect().y + (int)(scrollPane.getViewport().getViewRect().getHeight()/2),
                false);

        // find default game clause
        Clause defaultGameClause;
        if(gameLudemeNode.symbolClauseMap().get(gameLudemeNode.symbol()).get(0).args().size() > 2)
            defaultGameClause = gameLudemeNode.symbolClauseMap().get(gameLudemeNode.symbol()).get(0);
        else
            defaultGameClause = gameLudemeNode.symbolClauseMap().get(gameLudemeNode.symbol()).get(1);

        Handler.updateCurrentClause(graph(), gameLudemeNode, defaultGameClause);
        Handler.recordUserActions = true;
    }
}
