package app.display.dialogs.visual_editor.recs.codecompletion;

public class Ludeme {
    private final String keyword;

    public Ludeme(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    @Override
	public String toString() {
        return keyword;
    }
}
