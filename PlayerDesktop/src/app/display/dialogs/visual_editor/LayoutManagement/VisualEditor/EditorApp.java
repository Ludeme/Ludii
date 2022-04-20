package app.display.dialogs.visual_editor.LayoutManagement.VisualEditor;


import app.display.dialogs.visual_editor.LayoutManagement.VisualEditor.GrammarModel.SyntaxBase;

public class EditorApp
{
    public static SyntaxBase base = SyntaxBase.getInstance();

    public static void main(String[] args)
    {

        base.generateSyntaxBase();
        System.out.println(base.toString());

    }

}
