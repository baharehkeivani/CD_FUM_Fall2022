package error;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ToorlaCompilerError {

    private int line;
    private int column;
    private int code;
    private String message;

    @Override
    public String toString() {
        return "Error" + code + ": in line [" + line + ":" + column + "], " + message;
    }

}
