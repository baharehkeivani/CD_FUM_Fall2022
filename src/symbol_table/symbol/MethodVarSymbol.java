package symbol_table.symbol;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MethodVarSymbol extends FieldSymbol{

    public MethodVarSymbol(String name, int line, int col, String type, boolean primitive, boolean defined) {
        super(name, line, col, type, primitive, defined);
    }
//    public String toString() {
//        return "MethodVar" + " (name:" + getName() + ") " +
//                "(type: [" + " localVar= " + (isPrimitive() ? String.format("%s", getType())
//                : String.format("[class Type: %s]", getType())) + "] " + ", isDefined: " + isDefined() + " )";
//    }
    @Override
    public String toString() {
        return "MethodVar" + " (name:" + getName() + ") " +
                "(type: [" + " localVar= " + getType() + "] " + ", isDefined: " + isDefined() + " )";
    }
}
