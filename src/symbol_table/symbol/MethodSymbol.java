package symbol_table.symbol;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MethodSymbol extends ConstructorSymbol {
    public MethodSymbol(String name, int line, int col,
                        List<MethodParameter> parameters, String returnType, boolean returnPrimitive, String accessModifier) {
        super(name, line, col, parameters, returnType, returnPrimitive, accessModifier);
    }

    @Override
    public String toString() {
        boolean hasParam = !getParameterList().isEmpty();
        return "Method (name:" + getName() + ") " +
                "(return type: " + (isReturnPrimitive() ? String.format("[%s]", returnType)
                : String.format("[class Type: %s]", returnType)) + ") " +
                "(parameter list: " + (hasParam ? getParameterList() : "[]") + ")";
    }
}
