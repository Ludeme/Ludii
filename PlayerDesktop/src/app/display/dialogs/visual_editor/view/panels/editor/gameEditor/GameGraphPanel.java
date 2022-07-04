package app.display.dialogs.visual_editor.view.panels.editor.gameEditor;


import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.components.AddArgumentPanel;
import app.display.dialogs.visual_editor.view.panels.editor.GraphPanel;
import grammar.Grammar;
import main.grammar.Clause;
import main.grammar.Symbol;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GameGraphPanel extends GraphPanel
{

    private AddArgumentPanel addDefinePanel = new AddArgumentPanel(new ArrayList<>(), this, false, true);
    public GameGraphPanel(int width, int height)
    {
        super(width, height);
        Handler.gameDescriptionGraph = graph();
        Handler.gameGraphPanel = this;
        Handler.addGraphPanel(graph(), this);
        Handler.updateCurrentGraphPanel(this);
        add(addDefinePanel);
    }

    public void initialize(JScrollPane scrollPane)
    {
        super.initialize(scrollPane);

        // Create a "game" root node

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

    public void showAddDefinePanel()
    {
        // Define Nodes
        List<LudemeNode> defines = Handler.defineNodes();
        // Their Symbols
        List<Symbol> defineSymbols = new ArrayList<>();
        for(LudemeNode n : defines)
            if(n!=null)
                defineSymbols.add(n.symbol());
        // Update list of defines
        addDefinePanel.updateList(defineSymbols, defines);
        addDefinePanel.setVisible(true);
        addDefinePanel.setLocation(mousePosition());
        addDefinePanel.searchField.requestFocus();
        revalidate();
        repaint();
    }

    @Override
    public void hideAllAddArgumentPanels()
    {
        super.hideAllAddArgumentPanels();
        addDefinePanel.setVisible(false);
    }

}
