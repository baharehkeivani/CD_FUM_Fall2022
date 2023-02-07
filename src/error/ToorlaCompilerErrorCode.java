package error;

public enum ToorlaCompilerErrorCode {
    CLASS_ALREADY_DEFINED(101, "class %s has been defined already"),
    METHOD_ALREADY_DEFINED(102, "method %s has been defined already"),
    FIELD_ALREADY_DEFINED(103, "field %s has been defined already"),
    VAR_ALREADY_DEFINED(104, "var %s has been defined already"),
    ARGUMENT_MISMATCH(210, "Return type of this method must be %s"),
    METHOD_NOT_ACCESSIBLE(310, "private methods are not accessible outside of class");
    final int code;
    final String message;
    ToorlaCompilerErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
