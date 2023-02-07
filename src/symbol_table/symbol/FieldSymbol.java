package symbol_table.symbol;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldSymbol extends BaseSymbol {
    private String type;
    private final boolean primitive;
    private final boolean defined;

    public FieldSymbol(String name, int line, int col, String type, boolean primitive, boolean defined) {
        super(name, line, col);
        this.type = type;
        this.primitive = primitive;
        this.defined = defined;
    }

    public String getFieldType() {
        return type;
    }

    @Override
    public String toString() {
        return  "ClassField"  + " (name:" + getName() + ") " +
                (isPrimitive() ? "(type: " : "(classType: " ) + getType() + ", isDefined: " + isDefined() + " )";
    }
}
