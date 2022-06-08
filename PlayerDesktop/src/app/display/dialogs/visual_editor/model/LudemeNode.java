package app.display.dialogs.visual_editor.model;


import app.display.dialogs.visual_editor.LayoutManagement.Math.Vector2D;
import app.display.dialogs.visual_editor.model.grammar.Constructor;
import app.display.dialogs.visual_editor.model.grammar.Ludeme;
import app.display.dialogs.visual_editor.model.grammar.input.Input;
import app.display.dialogs.visual_editor.model.grammar.input.LudemeInput;
import app.display.dialogs.visual_editor.model.interfaces.iGNode;
import app.display.dialogs.visual_editor.model.interfaces.iLudemeNode;
import main.grammar.Clause;
import main.grammar.Symbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static app.display.dialogs.visual_editor.LayoutManagement.GraphRoutines.repeatString;

/**
 * Node representation of a ludeme in the current description
 * @author Filipp Dokienko
 */

public class LudemeNode implements iLudemeNode, iGNode {

    private static int LAST_ID = 0;
    private final int ID;

    //private final Ludeme LUDEME;
    //private Constructor currentConstructor;

    private final Symbol SYMBOL;
    private final List<Clause> CLAUSES;
    private Clause selectedClause;

    private Object[] providedInputs;

    private HashMap<LudemeNode, Integer> childrenOrder = new HashMap<>();

    private int depth = 0;
    private int width,height;

    private LudemeNode parent;
    private List<LudemeNode> children = new ArrayList<>();

    private int x,y;

    // For dynamic constructor
    private boolean dynamic = false; // TODO: Not hard-coded


    public LudemeNode(Symbol symbol, int x, int y) {
        this.ID = LAST_ID++;
        this.SYMBOL = symbol;
        this.CLAUSES = new ArrayList<>(symbol.rule().rhs());
        this.x = x;
        this.y = y;
        this.width = 100;
        this.height = 100;
        this.selectedClause = symbol.rule().rhs().get(0);
        this.providedInputs = new Object[selectedClause.args().size()];
    }

    /*
    public LudemeNode(Ludeme ludeme, int x, int y){
        LAST_ID++;
        this.ID = LAST_ID;

        this.LUDEME = ludeme;
        this.currentConstructor = ludeme.getConstructors().get(0); // automatically first one
        this.providedInputs = new Object[currentConstructor.getInputs().size()];
        this.x = x;
        this.y = y;
    }
*/
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

    public int getDepthManual(){
        int depth = 0;
        LudemeNode current = this;
        while(current.getParentNode() != null){
            depth++;
            current = current.getParentNode();
        }
        return depth;
    }

    // TODO: Should be probably in iGNode ?
    public void setPos(int x, int y){
        this.x = x;
        this.y = y;
    }


    @Override
    public Symbol symbol() {
        return SYMBOL;
    }

    public List<Clause> clauses(){
        return CLAUSES;
    }

    @Override
    public Clause selectedClause() {
        return selectedClause;
    }

    @Override
    public Object[] providedInputs() {
        return providedInputs;
    }

    @Override
    public void setProvidedInput(int index, Object input) {
        providedInputs[index] = input;
    }

    @Override
    public void setParent(iLudemeNode ludemeNode) {
        this.parent = (LudemeNode) ludemeNode; // TODO: should it be casted?
    }

    @Override
    public String getStringRepresentation() {
        return ""; // TODO
    }

    public void addChildren(LudemeNode children){
        // Checks if child nodes was already added
        /*
        if (!this.children.contains(children))
        {
            this.children.add(children);
            // get order of new child in current constructor
            // TODO: something goes wrong for [optional] inputs
            int order = -1;
            for (Input in: currentConstructor.getInputs())
            {
                if (in.getName().equals(children.getLudeme().getName()))
                {
                    order = currentConstructor.getInputs().indexOf(in);
                    break;
                }
            }
            childrenOrder.put(children, order);
            // placing child in correct order
            for (int i = this.children.size()-1; i > 0; i--) {
                if (childrenOrder.get(this.children.get(i-1)) > childrenOrder.get(this.children.get(i)))
                {
                    // swap i-1 and i
                    Collections.swap(this.children, i-1, i);
                }
            }
        }*/
        // TODO: Implement
    }

    public void removeChildren(LudemeNode children){
        this.children.remove(children);
    }


    @Override
    public String toString(){
        return getStringRepresentation();
    }


    public boolean canBeDynamic(){
        if(CLAUSES.size() == 1) return false;
        for(Clause clause : CLAUSES){
            if(clause.args().size() > 1) return true;
        }
        return false;
    }

    public boolean isDynamic(){
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public void setSelectedClause(Clause selectedClause) {
        this.selectedClause = selectedClause;
        this.providedInputs = new Object[selectedClause.args().size()];
    }
}
