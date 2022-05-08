package app.display.dialogs.visual_editor.model;


import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.grammar.Constructor;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.input.Input;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iLudemeNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Node representation of a ludeme in the current description
 * @author Filipp Dokienko
 */

public class LudemeNode implements iLudemeNode, iGNode {

    private static int LAST_ID = 0;
    private final int ID;

    private final Ludeme LUDEME;
    private Constructor currentConstructor;
    private Object[] providedInputs;

    private int depth = 0;
    private int width,height;

    private LudemeNode parent;
    private List<LudemeNode> children = new ArrayList<>();

    private int x,y;

    public LudemeNode(Ludeme ludeme, int x, int y){
        LAST_ID++;
        this.ID = LAST_ID;

        this.LUDEME = ludeme;
        this.currentConstructor = ludeme.getConstructors().get(0); // automatically first one
        this.providedInputs = new Object[currentConstructor.getInputs().size()];
        this.x = x;
        this.y = y;
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public int getParent() {
        return parent.getId();
    }

    public LudemeNode getParentNode(){
        return parent;
    }

    @Override
    public List<Integer> getChildren() {
        List<Integer> children_ids = new ArrayList<>();
        for(LudemeNode c : children) children_ids.add(c.getId());
        return children_ids;
    }

    @Override
    public List<Integer> getSiblings() {
        // TODO implement
        return null;
    }

    @Override
    public Vector2D getPos() {
        return new Vector2D(x, y);
    }

    @Override
    public void setPos(Vector2D pos) {
        x = (int) pos.getX();
        y = (int) pos.getY();
    }

    public void setWidth(int width){
        this.width = width;
    }

    @Override
    public int getWidth() {
        return width;
    }

   public void setHeight(int height){
        this.height = height;
   }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setDepth(int depth)
    {
        this.depth = depth;
    }

    @Override
    public int getDepth() {
        return depth;
    }


    // TODO: Should be probably in iGNode ?
    public void setPos(int x, int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public Ludeme getLudeme() {
        return this.LUDEME;
    }

    @Override
    public Constructor getCurrentConstructor() {
        return currentConstructor;
    }

    @Override
    public void setCurrentConstructor(Constructor selectedConstructor) {
        this.currentConstructor = selectedConstructor;
        // update providedInputs size
        this.providedInputs = new Object[currentConstructor.getInputs().size()];
    }

    @Override
    public Object[] getProvidedInputs() {
        return providedInputs;
    }

    @Override
    public void setProvidedInput(int index, Object providedInput) {
        providedInputs[index] = providedInput;
    }

    @Override
    public void setProvidedInput(Input input, Object providedInput) {
        // TODO
    }

    @Override
    public void setParent(iLudemeNode ludemeNode) {
        this.parent = (LudemeNode) ludemeNode; // TODO: should it be casted?
    }

    public void addChildren(LudemeNode children){
        // Checks if child nodes was already added
        if (!this.children.contains(children)) this.children.add(children);
    }

    public void removeChildren(LudemeNode children){
        this.children.remove(children);
    }


    @Override
    public String getStringRepresentation() {

        char c = '"';

        if(currentConstructor.getInputs().size() == 1 && currentConstructor.getInputs().get(0).isTerminal()){
            if(providedInputs[0] instanceof String) return c+providedInputs[0].toString()+c+" ";
            else return providedInputs[0].toString();
        }

        StringBuilder s = new StringBuilder("");
        String[] ludemeNameSplit = getLudeme().getName().split("\\.");
        if(getLudeme().isHidden()) s.append("");
        else if(ludemeNameSplit.length >= 1){
            s.append("(");
            s.append(ludemeNameSplit[ludemeNameSplit.length-1]);
        }
        else {
            s.append("(");
            s.append(getLudeme().getName());
        }
        s.append(" ");
        s.append(getCurrentConstructor().getName());
        s.append(" ");
        for(Object o : getProvidedInputs()){
            if(o == null); // TODO: What to do when input is empty?
            else if(o instanceof LudemeNode[]) {
                s.append("{");
                for(LudemeNode ln : (LudemeNode[]) o){
                    if(ln == null) continue;
                    s.append(ln.getStringRepresentation());
                    s.append("\n");
                }
                s.append("}");
            }
            else if(o instanceof String) s.append("\"").append(o.toString()).append("\"");
            else s.append(o.toString());
        }
        if(s.toString().startsWith("(")) s.append(")");
        return s.toString().trim().replaceAll(" +", " ");
    }

    @Override
    public String toString(){
        return getStringRepresentation();
    }
}
