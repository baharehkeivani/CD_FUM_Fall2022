package symbol_table.symbol;

public class ParamFieldSymbol extends FieldSymbol{

    public ParamFieldSymbol(String name, int line, int col, String type, boolean primitive, boolean defined) {
        super(name, line, col, type, primitive, defined);
    }

    @Override
    public String toString() {
        return "ParamField" + " (name:" + getName() + ") " +
                "(type: [" + (isPrimitive() ? "" : " classTyped= " ) + getType()  + "] " + ", isDefined: " + isDefined() + " )";
    }
}
