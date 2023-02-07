package symbol_table.symbol;

public class ClassSymbol extends BaseSymbol{

    private String parent ;
    private Boolean isEntry;

    public ClassSymbol(String name, int line, int col, String parent , Boolean isEntry, boolean inheritanceLoop) {
        super(name, line, col, inheritanceLoop);
        this.parent = parent;
        this.isEntry = isEntry;
    }

    @Override
    public String toString() {
        return "Class (name: " + getName() + ") (parent: " +  parent + ")  (isEntry: " + isEntry.toString() +")";
    }
}
