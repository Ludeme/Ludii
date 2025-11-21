package app.boardMaker.display.panels.westPanel;

import app.boardMaker.display.panels.westPanel.itemList.ItemList;
import app.boardMaker.display.panels.westPanel.library.Library;
import app.boardMaker.handlers.Maker;

import javax.swing.*;

public class WestPanel extends JTabbedPane {
    private Maker maker;
    private boolean visible;

    public WestPanel(Maker maker) {
        this.maker = maker;

        maker.getDisplayer().setWestPanel(this);

        Library library = new Library(maker);
        ItemList itemList = new ItemList(maker);

        addTab("Library", library);
        addTab("Boards", itemList);
    }

    public void visibility(boolean b) {
        visible = b;
    }

    public boolean visible() {
        return visible;
    }
}
