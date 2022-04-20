package app.display.dialogs.visual_editor.LayoutManagement.interfaces;

/**
 * @author frehburg
 */
public enum NodeType {
    ERR(-1),
    ROOT(0),
    INNER(1),
    LEAF(2),
    SUPER(Integer.MAX_VALUE);

    private int id;

    NodeType(int id){
        this.id = id;
    }

    public boolean equals(NodeType nodeType) {
        if(this.id == nodeType.id)
            return true;
        return false;
    }
}
