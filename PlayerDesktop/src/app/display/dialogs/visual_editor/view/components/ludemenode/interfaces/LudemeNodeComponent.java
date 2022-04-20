package app.display.dialogs.visual_editor.view.components.ludemenode.interfaces;

import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.view.panels.editor.EditorPanel;

import javax.swing.*;

public abstract class LudemeNodeComponent extends JComponent implements ILudemeNodeComponent {

    protected int x, y;
    protected final Ludeme LUDEME;
    public final EditorPanel EDITOR_PANEL;

    private final LudemeNode LUDEME_NODE;


    public LudemeNodeComponent(LudemeNode ludemeNode, EditorPanel editorPanel){
        this.LUDEME_NODE = ludemeNode;
        this.LUDEME = ludemeNode.getLudeme();
        this.EDITOR_PANEL = editorPanel;
        this.x = (int) ludemeNode.getPos().getX();
        this.y = (int) ludemeNode.getPos().getY();
    }
}
