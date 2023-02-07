package symbol_table.symbol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MethodParameter {
    private String name;
    private String type;
    private int index;
    private boolean primitive;
}
