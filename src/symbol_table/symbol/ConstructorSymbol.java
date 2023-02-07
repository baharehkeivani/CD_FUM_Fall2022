package symbol_table.symbol;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
public class ConstructorSymbol extends BaseSymbol{

    protected final String returnType;
    protected final boolean returnPrimitive;
    protected List<MethodParameter> parameters;
    private final String accessModifier;

    public ConstructorSymbol(String name, int line, int col, List<MethodParameter> parameters, String returnType, boolean returnPrimitive, String accessModifier) {
        super(name, line, col);
        this.returnType = returnType;
        this.parameters = parameters;
        this.returnPrimitive = returnPrimitive;
        this.accessModifier = accessModifier;
    }

    protected String getParameterList() {
        if (this.parameters == null) {
            this.parameters = Collections.emptyList();
        }
        return this.parameters
                .stream()
                .map(p -> ("[name: " + p.getName() + ", type: "  + p.getType() + ", index:" + p.getIndex() + "]"))
                .collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        boolean hasParam = !getParameterList().isEmpty();
        return "Constructor (name:" + getName() + ") " + "(return type: " + (isReturnPrimitive() ? String.format("[%s]", returnType)
                : String.format("[class Type: %s]", returnType)) + ") "  + "(parameter list: " + (hasParam ? getParameterList() : "[]") + " )";
    }
}
