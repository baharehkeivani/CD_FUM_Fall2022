package symbol_table.table;

public class KeyGenerator {
    public static String generate(SymbolType type, String name) {
        return type + "_" + name;
    }

    public static String generateDuplicated(String oldKey, int line, int col) {
        return oldKey + "_" + line + "_" + col;
    }

}
