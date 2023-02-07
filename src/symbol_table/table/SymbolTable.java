package symbol_table.table;

import symbol_table.symbol.BaseSymbol;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SymbolTable {
    private String scope;
    private int line;
    private int parentTableLine;
    private Map<String, BaseSymbol> table = new LinkedHashMap<>();

    public SymbolTable(String scope, int line) {
        this.scope = scope;
        this.line = line;
    }

    public void insert(String key, BaseSymbol baseSymbol) {
        table.put(key, baseSymbol);
    }

    public BaseSymbol lookup(String key) {
        return table.getOrDefault(key, null);
    }

    public boolean containsKey(String key) {
        return lookup(key) != null;
    }

    public String toString() {
        return "\n\n------------- " + scope + " : " + line + " -------------\n" +
                printItems() +
                "\n=====================================================================================================================================================";
    }

    public String printItems() {
        List<String> items = new ArrayList<>();
        table.forEach((key, symbol) -> items.add("Key:" + (key.startsWith("LocalVar" )? key.replace("LocalVar","Field") : key) + " | Value: " + symbol.toString()));
        return String.join("\n", items);
    }

}
