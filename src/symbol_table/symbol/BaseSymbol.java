package symbol_table.symbol;

import symbol_table.table.SymbolType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseSymbol {
    private String name;
    private int line;
    private int col;
    private boolean duplicated = false;
    private boolean inheritanceLoop;
    private String returnType;
    private SymbolType symbolType;

    public BaseSymbol(String name, int line, int col) {
        this.name = name;
        this.line = line;
        this.col = col;
    }

    public BaseSymbol(String name, int line, int col, boolean inheritanceLoop) {
        this.name = name;
        this.line = line;
        this.col = col;
        this.inheritanceLoop = inheritanceLoop;
    }

    public String getFieldType() {
        return null;
    }
}
