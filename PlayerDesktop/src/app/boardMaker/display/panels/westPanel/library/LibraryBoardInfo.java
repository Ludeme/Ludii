package app.boardMaker.display.panels.westPanel.library;

public class LibraryBoardInfo {
    private String name;
    private String classname;

    public LibraryBoardInfo(String name, String classname) {
        this.name = name;
        this.classname = classname;
    }

    public String getClassname() {
        return classname;
    }

    @Override
    public String toString() {
        return name;
    }
}
