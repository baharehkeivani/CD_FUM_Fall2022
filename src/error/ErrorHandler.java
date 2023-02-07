package error;

import symbol_table.table.SymbolTable;
import symbol_table.table.SymbolType;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {

    public static void handle(List<SymbolTable> tables, ArrayList<String> inheritanceChain, ArrayList<String> returnTypeErrors, ArrayList<String> privateMethodsError) {
        List<ToorlaCompilerError> errors = new ArrayList<>();

        for (SymbolTable symbolTable : tables) {
            symbolTable.getTable().forEach((key, symbol) -> {
                if (symbol.getSymbolType() == SymbolType.Class) {
                    if (symbol.isDuplicated()) {
                        errors.add(new ToorlaCompilerError(
                                symbol.getLine(),
                                symbol.getCol(),
                                ToorlaCompilerErrorCode.CLASS_ALREADY_DEFINED.code,
                                String.format(ToorlaCompilerErrorCode.CLASS_ALREADY_DEFINED.message, symbol.getName())
                        ));
                    }

                } else if (symbol.getSymbolType() == SymbolType.Method) {
                    if (symbol.isDuplicated()) {
                        errors.add(new ToorlaCompilerError(
                                symbol.getLine(),
                                symbol.getCol(),
                                ToorlaCompilerErrorCode.METHOD_ALREADY_DEFINED.code,
                                String.format(ToorlaCompilerErrorCode.METHOD_ALREADY_DEFINED.message, symbol.getName())
                        ));
                    }
                } else if (symbol.getSymbolType() == SymbolType.Field) {
                    if (symbol.isDuplicated()) {
                        errors.add(new ToorlaCompilerError(
                                symbol.getLine(),
                                symbol.getCol(),
                                ToorlaCompilerErrorCode.FIELD_ALREADY_DEFINED.code,
                                String.format(ToorlaCompilerErrorCode.FIELD_ALREADY_DEFINED.message, symbol.getName())
                        ));
                    }
                } else if (symbol.getSymbolType() == SymbolType.LocalVar) {
                    if (symbol.isDuplicated()) {
                        errors.add(new ToorlaCompilerError(
                                symbol.getLine(),
                                symbol.getCol(),
                                ToorlaCompilerErrorCode.VAR_ALREADY_DEFINED.code,
                                String.format(ToorlaCompilerErrorCode.VAR_ALREADY_DEFINED.message, symbol.getName())
                        ));
                    }
                }

            });
        }

        if (returnTypeErrors.size() > 0) {
            for (String returnTypeError : returnTypeErrors) {
                String[] temp = returnTypeError.split(",");
                errors.add(new ToorlaCompilerError(
                        Integer.parseInt(temp[1]),
                        Integer.parseInt(temp[2]),
                        ToorlaCompilerErrorCode.ARGUMENT_MISMATCH.code,
                        String.format(ToorlaCompilerErrorCode.ARGUMENT_MISMATCH.message, temp[0])
                ));
            }
        }

        if (privateMethodsError.size() > 0) {
            for (String privateMethodsErr : privateMethodsError) {
                String[] temp = privateMethodsErr.split(",");
                errors.add(new ToorlaCompilerError(
                        Integer.parseInt(temp[0]),
                        Integer.parseInt(temp[1]),
                        ToorlaCompilerErrorCode.METHOD_NOT_ACCESSIBLE.code,
                        String.format(ToorlaCompilerErrorCode.METHOD_NOT_ACCESSIBLE.message)
                ));
            }
        }

        for (ToorlaCompilerError error : errors) {
            System.err.println(error.toString());
        }

        if (inheritanceChain.size() > 0) {
            for (int i = 0; i < inheritanceChain.size(); i++) {
                System.err.print("Error410: Invalid inheritance ");
                String[] temp = inheritanceChain.get(i).split(",");
                for (int j = 0; j < temp.length; j += 2) {
                    if (j > 0 && temp[j].equals(temp[j - 1])) System.err.print(" -> " + temp[j + 1]);
                    else System.err.print(temp[j] + " -> " + temp[j + 1]);
                }
                System.err.print("\n");
            }


        }

        if (returnTypeErrors.size() > 0) {
            for (String returnTypeError : returnTypeErrors) {
                String[] temp = returnTypeError.split(",");
                errors.add(new ToorlaCompilerError(
                        Integer.parseInt(temp[1]),
                        Integer.parseInt(temp[2]),
                        ToorlaCompilerErrorCode.ARGUMENT_MISMATCH.code,
                        String.format(ToorlaCompilerErrorCode.ARGUMENT_MISMATCH.message, temp[0])
                ));
            }
        }


    }

}
