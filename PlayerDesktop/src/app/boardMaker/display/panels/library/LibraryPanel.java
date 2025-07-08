package app.boardMaker.display.panels.library;

import app.boardMaker.handlers.Maker;

import javax.swing.*;

public class LibraryPanel extends JTabbedPane {
    private Maker maker;
    private boolean visible;

    public LibraryPanel(Maker maker) {
        this.maker = maker;

        Library library = new Library(maker);
        maker.getDisplayer().setBoardList(this);

        addTab("Library", library);
    }

    public void visibility(boolean b) {
        visible = b;
    }

    public boolean visible() {
        return visible;
    }
}
