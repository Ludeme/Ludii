package app.display.dialogs.visual_editor.recs.utils;

import main.grammar.Symbol;

public class ReadableSymbol {

    private final Symbol symbol;

    public ReadableSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol.name();
    }
}
