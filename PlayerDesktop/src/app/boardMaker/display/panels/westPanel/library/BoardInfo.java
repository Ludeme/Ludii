package app.boardMaker.display.panels.westPanel.library;

public class BoardInfo {
    private String name;
    private String classname;

    public BoardInfo(String name, String classname) {
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
