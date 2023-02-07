package compiler;

import gen.ToorlaListener;
import gen.ToorlaParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import symbol_table.symbol.*;
import symbol_table.table.KeyGenerator;
import symbol_table.table.SymbolTable;
import symbol_table.table.SymbolType;
import error.ErrorHandler;

import java.util.*;


//Custom Listener With Symbol table
public class CustomListenerWST implements ToorlaListener {

    private boolean isEntry = false;
    private String current_class_name = "";
    private int nested = 0;
    final List<String> primitiveTypes = Arrays.asList("int", "bool", "string");
    private Stack<SymbolTable> tables = new Stack<>();
    private PriorityQueue<SymbolTable> orderedTables = new PriorityQueue<>(Comparator.comparing(SymbolTable::getLine));
    private Set<String> classes = new HashSet<>();
    private List<SymbolTable> allTables = new ArrayList<>();
    private ArrayList<String> inheritedClasses = new ArrayList<>();
    public ArrayList<String> inheritanceChain = new ArrayList<>();
    private Stack<String> methodsReturnType = new Stack<>();
    public ArrayList<String> returnTypeErrors = new ArrayList<>();
    private ArrayList<String> privateMethods = new ArrayList<>();
    private ArrayList<String> publicMethods = new ArrayList<>();
    public ArrayList<String> privateMethodsError = new ArrayList<>();

    private String lastClassTypeName = "";//for non-primitive assignments or var defines

    private void newScope(String name, int line) {
        tables.push(new SymbolTable(name, line));
    }

    private void endScope() {
        orderedTables.add(tables.pop());
    }

    private void printTables() {
        while (!orderedTables.isEmpty()) {
            SymbolTable table = orderedTables.poll();
            System.out.println(table.toString());
            allTables.add(table);
        }
    }

    private void addToTable(String key, BaseSymbol symbol) {
        SymbolType symbolType = SymbolType.valueOf(key.split("_")[0]);
        if (tables.peek().containsKey(key)) {
            key = KeyGenerator.generateDuplicated(key, symbol.getLine(), symbol.getCol());
            symbol.setDuplicated(true);
        }
        symbol.setSymbolType(symbolType);
        tables.peek().insert(key, symbol);
    }

    private List<MethodParameter> extractMethodParameter(ToorlaParser.MethodDeclarationContext ctx) {
        List<MethodParameter> parameters = new ArrayList<>();
        int param_length = ctx.toorlaType().toArray().length; //n
        List<ToorlaParser.ToorlaTypeContext> types = ctx.toorlaType(); // [n] -> 1 ta n-1 param types
        List<TerminalNode> ids = ctx.ID(); // [n] -> 2 ta n param names
        for (int i = 1; i < param_length; i++) {
            String type = types.get(i - 1).getText();
            String name = ids.get(i).getText();
            parameters.add(new MethodParameter(name, type, i, isPrimitive(type)));
        }
        return parameters;
    }

    private boolean isPrimitive(String type) {
        if (primitiveTypes.contains(type)) return true;
        return false;
    }

    private boolean isDefined(String type) {
        boolean isArray = type.contains("[");
        if (classes.contains(type) || isPrimitive(type) || isArray || type == "var") return true;
        return false;
    }

    public void enterProgram(ToorlaParser.ProgramContext ctx) {
        newScope("program", 0);
    }

    public void exitProgram(ToorlaParser.ProgramContext ctx) {
        endScope();
        printTables();
        ErrorHandler.handle(allTables, inheritanceChain, returnTypeErrors, privateMethodsError);
    }

    public void inheritanceStringCreator(String name, String parent, int index) {
        for (int i = 1; i < inheritedClasses.size(); i += 2) {
            if (inheritedClasses.get(i).equals(name)) {
                inheritanceStringCreator(inheritedClasses.get(i - 1), inheritedClasses.get(i), index);
            }
        }
        inheritanceChain.set(index, inheritanceChain.get(index) + name + "," + parent + ",");
    }

    public void enterClassDeclaration(ToorlaParser.ClassDeclarationContext ctx) {

        privateMethods.clear();

        current_class_name = ctx.className.getText();
        String name = ctx.className.getText();
        String key = KeyGenerator.generate(SymbolType.Class, name);
        String parent = "[]";
        boolean inheritanceLoop = false;
        if (ctx.classParent != null) {
            parent = ctx.classParent.getText();
            // checking for inheritance loops
            int index = inheritanceChain.size();
            inheritanceChain.add("");

            inheritanceStringCreator(name, parent, index);

            String[] temp = inheritanceChain.get(index).split(",");
            int counter = 0;
            for (String s : temp)
                if (s.equals(parent)) counter++;
            if (counter > 1) inheritanceLoop = true;
            else inheritanceChain.remove(index);

            inheritedClasses.add(name);
            inheritedClasses.add(parent);
        }

        ClassSymbol classSymbol = new ClassSymbol(name, ctx.start.getLine(), ctx.stop.getCharPositionInLine(), parent, isEntry, inheritanceLoop);
        addToTable(key, classSymbol);
        classes.add(name);
        newScope(name, classSymbol.getLine());
    }

    public void exitClassDeclaration(ToorlaParser.ClassDeclarationContext ctx) {
        ArrayList<String> tempErrors = new ArrayList<>();
        // fix the return types error when looked up in class scope
        for (String returnTypeError : returnTypeErrors) {
            String[] temp = returnTypeError.split(",");
            if(temp.length == 4) {
                String tempKey = temp[3].replace("self.", "").trim();
                tempKey = "Field_" + tempKey;
                if (tables.peek().lookup(tempKey) != null) {
                    if(tables.peek().lookup(tempKey).getFieldType().equals(temp[0].trim()))
                        tempErrors.add(returnTypeError);
                }
            }
        }
        for (String tempError : tempErrors) {
            returnTypeErrors.remove(tempError);
        }

        endScope();
    }

    public void enterEntryClassDeclaration(ToorlaParser.EntryClassDeclarationContext ctx) {
        isEntry = true;
    }

    public void exitEntryClassDeclaration(ToorlaParser.EntryClassDeclarationContext ctx) {
        isEntry = false;
    }

    public void enterFieldDeclaration(ToorlaParser.FieldDeclarationContext ctx) {
        String name = ctx.fieldName.getText();
        String key = KeyGenerator.generate(SymbolType.Field, name);
        String type = ctx.fieldType.getText();
        FieldSymbol fieldSymbol = new FieldSymbol(name, ctx.start.getLine(), ctx.stop.getCharPositionInLine(), type, isPrimitive(type), isDefined(type));
        addToTable(key, fieldSymbol);
    }

    public void exitFieldDeclaration(ToorlaParser.FieldDeclarationContext ctx) {
    }

    public void enterAccess_modifier(ToorlaParser.Access_modifierContext ctx) {
    }

    public void exitAccess_modifier(ToorlaParser.Access_modifierContext ctx) {
    }

    public void enterMethodDeclaration(ToorlaParser.MethodDeclarationContext ctx) {
        String name = ctx.methodName.getText();
        List<MethodParameter> parameters = extractMethodParameter(ctx);
        boolean returnTypeIsPrimitive = false;
        if (ctx.t != null) {
            returnTypeIsPrimitive = isPrimitive(ctx.t.getText());
        }
        boolean isVoid = returnTypeIsPrimitive && ctx.t == null;
        String returnType = isVoid ? "void" : ctx.t.getText();
        methodsReturnType.push(returnType);
        String accessModifier = "public";
        if (ctx.methodAccessModifier != null) {
            accessModifier = ctx.methodAccessModifier.getText();
        }
        if(accessModifier.equals("private")) privateMethods.add(name);
        else publicMethods.add(name);

        if (name.equals(current_class_name)) {
            //constructor
            String key = KeyGenerator.generate(SymbolType.Constructor, name);
            ConstructorSymbol constructorSymbol = new ConstructorSymbol(name, ctx.start.getLine(), ctx.stop.getCharPositionInLine(), parameters, returnType, returnTypeIsPrimitive, accessModifier);
            addToTable(key, constructorSymbol);
            newScope(name, constructorSymbol.getLine());
        } else {
            //method
            String key = KeyGenerator.generate(SymbolType.Method, name);
            MethodSymbol methodSymbol = new MethodSymbol(name, ctx.start.getLine(), ctx.stop.getCharPositionInLine(), parameters, returnType, returnTypeIsPrimitive, accessModifier);
            addToTable(key, methodSymbol);
            newScope(name, methodSymbol.getLine());
        }
        //ParameterFields
        int param_length = ctx.toorlaType().toArray().length; //n
        List<ToorlaParser.ToorlaTypeContext> types = ctx.toorlaType(); // [n] -> 1 ta n-1 param types
        List<TerminalNode> ids = ctx.ID(); // [n] -> 2 ta n param names
        for (int i = 1; i < param_length; i++) {
            String type = types.get(i - 1).getText();
            String field_name = ids.get(i).getText();
            String key = KeyGenerator.generate(SymbolType.Field, field_name);
            ParamFieldSymbol fieldSymbol = new ParamFieldSymbol(field_name, ctx.start.getLine(), ctx.stop.getCharPositionInLine(), type, isPrimitive(type), isDefined(type));
            addToTable(key, fieldSymbol);
        }
    }

    public void exitMethodDeclaration(ToorlaParser.MethodDeclarationContext ctx) {
        methodsReturnType.pop();
        endScope();
    }

    public void enterClosedStatement(ToorlaParser.ClosedStatementContext ctx) {

    }

    public void exitClosedStatement(ToorlaParser.ClosedStatementContext ctx) {

    }

    public void enterClosedConditional(ToorlaParser.ClosedConditionalContext ctx) {
        newScope("if", ctx.start.getLine());
        nested++;
        if (nested > 1) {
            newScope("nested", ctx.start.getLine());
        }
    }

    public void exitClosedConditional(ToorlaParser.ClosedConditionalContext ctx) {
        endScope();
        if (nested > 1) {
            endScope();
        }
        nested--;
    }

    public void enterOpenConditional(ToorlaParser.OpenConditionalContext ctx) {
        nested++;
        if (nested > 1) {
            newScope("nested", ctx.start.getLine());
        }
    }

    public void exitOpenConditional(ToorlaParser.OpenConditionalContext ctx) {
        if (nested > 1) {
            endScope();
        }
        nested--;
    }

    public void enterOpenStatement(ToorlaParser.OpenStatementContext ctx) {
    }

    public void exitOpenStatement(ToorlaParser.OpenStatementContext ctx) {
    }

    public void enterStatement(ToorlaParser.StatementContext ctx) {
    }

    public void exitStatement(ToorlaParser.StatementContext ctx) {
    }

    public void enterStatementVarDef(ToorlaParser.StatementVarDefContext ctx) {
        String name = ctx.myVar.getText();
        String key = KeyGenerator.generate(SymbolType.LocalVar, name);
        String type = "var";
        String expression = ctx.assign.getText();
        if (expression.matches("[0-9]+")) {
            type = "int";
        } else if (expression.startsWith("\"") && expression.endsWith("\"")) {
            type = "string";
        } else if (expression.equalsIgnoreCase("true") || expression.equalsIgnoreCase("false")) {
            type = "bool";
        }
        MethodVarSymbol methodVarSymbol = new MethodVarSymbol(name, ctx.start.getLine(), ctx.stop.getCharPositionInLine(), type, isPrimitive(type), isDefined(type));
        addToTable(key, methodVarSymbol);
    }

    public void exitStatementVarDef(ToorlaParser.StatementVarDefContext ctx) {
    }

    public void enterStatementBlock(ToorlaParser.StatementBlockContext ctx) {
    }

    public void exitStatementBlock(ToorlaParser.StatementBlockContext ctx) {
    }

    public void enterStatementContinue(ToorlaParser.StatementContinueContext ctx) {
    }

    public void exitStatementContinue(ToorlaParser.StatementContinueContext ctx) {
    }

    public void enterStatementBreak(ToorlaParser.StatementBreakContext ctx) {
    }

    public void exitStatementBreak(ToorlaParser.StatementBreakContext ctx) {
    }

    public void enterStatementReturn(ToorlaParser.StatementReturnContext ctx) {
        String type = "var";
        String expression = ctx.e.getText();
        if (expression.matches("[0-9]+")) {
            type = "int";
        } else if (expression.startsWith("\"") && expression.endsWith("\"")) {
            type = "string";
        } else if (expression.equalsIgnoreCase("true") || expression.equalsIgnoreCase("false")) {
            type = "bool";
        } else {
            // like self.result
            if (expression.contains("self.")) {
                String temp = expression.replace("self.", "").trim();
                temp = "Field_" + temp;
                // lookup element to find its type but most likely scope isnt on class and needs further checking
                type = "self";
                if (tables.peek().lookup(temp.trim()) != null) {
                    type = tables.peek().lookup(temp.trim()).getFieldType();
                }
            }
            // local var
            else {
                String temp = "LocalVar_" + expression;
                if (tables.peek().lookup(temp) != null) {
                    type = tables.peek().lookup(temp).getFieldType();
                }
            }

        }
        // self
        if(type.equals("self")) {
            returnTypeErrors.add(methodsReturnType.peek() + "," + ctx.start.getLine() + "," + ctx.stop.getCharPositionInLine()+","+expression);
        } else {
            if (!type.equals(methodsReturnType.peek())) {
                returnTypeErrors.add(methodsReturnType.peek() + "," + ctx.start.getLine() + "," + ctx.stop.getCharPositionInLine());
            }
        }

    }

    public void exitStatementReturn(ToorlaParser.StatementReturnContext ctx) {
    }

    public void enterStatementClosedLoop(ToorlaParser.StatementClosedLoopContext ctx) {
        newScope("while", ctx.start.getLine());
        nested++;
        if (nested > 1) {
            newScope("nested", ctx.start.getLine());
        }
    }

    public void exitStatementClosedLoop(ToorlaParser.StatementClosedLoopContext ctx) {
        endScope();
        if (nested > 1) {
            endScope();
        }
        nested--;
    }

    public void enterStatementOpenLoop(ToorlaParser.StatementOpenLoopContext ctx) {
        nested++;
        if (nested > 1) {
            newScope("nested", ctx.start.getLine());
        }
    }

    public void exitStatementOpenLoop(ToorlaParser.StatementOpenLoopContext ctx) {
        if (nested > 1) {
            endScope();
        }
        nested--;
    }

    public void enterStatementWrite(ToorlaParser.StatementWriteContext ctx) {
    }

    public void exitStatementWrite(ToorlaParser.StatementWriteContext ctx) {
    }

    public void enterStatementAssignment(ToorlaParser.StatementAssignmentContext ctx) {
        lastClassTypeName = ctx.left.getText();
    }

    public void exitStatementAssignment(ToorlaParser.StatementAssignmentContext ctx) {
    }

    public void enterStatementInc(ToorlaParser.StatementIncContext ctx) {
    }

    public void exitStatementInc(ToorlaParser.StatementIncContext ctx) {
    }

    public void enterStatementDec(ToorlaParser.StatementDecContext ctx) {
    }

    public void exitStatementDec(ToorlaParser.StatementDecContext ctx) {
    }

    public void enterExpression(ToorlaParser.ExpressionContext ctx) {
    }

    public void exitExpression(ToorlaParser.ExpressionContext ctx) {
    }

    public void enterExpressionOr(ToorlaParser.ExpressionOrContext ctx) {
    }

    public void exitExpressionOr(ToorlaParser.ExpressionOrContext ctx) {
    }

    public void enterExpressionOrTemp(ToorlaParser.ExpressionOrTempContext ctx) {
    }

    public void exitExpressionOrTemp(ToorlaParser.ExpressionOrTempContext ctx) {
    }

    public void enterExpressionAnd(ToorlaParser.ExpressionAndContext ctx) {
    }

    public void exitExpressionAnd(ToorlaParser.ExpressionAndContext ctx) {
    }

    public void enterExpressionAndTemp(ToorlaParser.ExpressionAndTempContext ctx) {
    }

    public void exitExpressionAndTemp(ToorlaParser.ExpressionAndTempContext ctx) {
    }

    public void enterExpressionEq(ToorlaParser.ExpressionEqContext ctx) {
    }

    public void exitExpressionEq(ToorlaParser.ExpressionEqContext ctx) {
    }

    public void enterExpressionEqTemp(ToorlaParser.ExpressionEqTempContext ctx) {
    }

    public void exitExpressionEqTemp(ToorlaParser.ExpressionEqTempContext ctx) {
    }

    public void enterExpressionCmp(ToorlaParser.ExpressionCmpContext ctx) {
    }

    public void exitExpressionCmp(ToorlaParser.ExpressionCmpContext ctx) {
    }

    public void enterExpressionCmpTemp(ToorlaParser.ExpressionCmpTempContext ctx) {
    }

    public void exitExpressionCmpTemp(ToorlaParser.ExpressionCmpTempContext ctx) {
    }

    public void enterExpressionAdd(ToorlaParser.ExpressionAddContext ctx) {
    }

    public void exitExpressionAdd(ToorlaParser.ExpressionAddContext ctx) {
    }

    public void enterExpressionAddTemp(ToorlaParser.ExpressionAddTempContext ctx) {
    }

    public void exitExpressionAddTemp(ToorlaParser.ExpressionAddTempContext ctx) {
    }

    public void enterExpressionMultMod(ToorlaParser.ExpressionMultModContext ctx) {
    }

    public void exitExpressionMultMod(ToorlaParser.ExpressionMultModContext ctx) {
    }

    public void enterExpressionMultModTemp(ToorlaParser.ExpressionMultModTempContext ctx) {
    }

    public void exitExpressionMultModTemp(ToorlaParser.ExpressionMultModTempContext ctx) {
    }

    public void enterExpressionUnary(ToorlaParser.ExpressionUnaryContext ctx) {
    }

    public void exitExpressionUnary(ToorlaParser.ExpressionUnaryContext ctx) {
    }

    public void enterExpressionMethods(ToorlaParser.ExpressionMethodsContext ctx) {

    }

    public void exitExpressionMethods(ToorlaParser.ExpressionMethodsContext ctx) {
    }

    public void enterExpressionMethodsTemp(ToorlaParser.ExpressionMethodsTempContext ctx) {
        if (ctx.i != null) {
            if(!publicMethods.contains(ctx.i.getText()) && !privateMethods.contains(ctx.i.getText())) {
                privateMethodsError.add(ctx.start.getLine() + "," + ctx.stop.getCharPositionInLine());
            }
        }
    }

    public void exitExpressionMethodsTemp(ToorlaParser.ExpressionMethodsTempContext ctx) {
    }

    public void enterExpressionOther(ToorlaParser.ExpressionOtherContext ctx) {
        if (ctx.newModifier != null) {
            String name = lastClassTypeName;
            String key = KeyGenerator.generate(SymbolType.LocalVar, name);
            String type = "";
            if (ctx.st != null) {
                type = ctx.st.getText() + "[]";
            } else if (ctx.i != null) {
                type = ctx.i.getText();
            }
            MethodVarSymbol methodVarSymbol = new MethodVarSymbol(name, ctx.start.getLine(), ctx.stop.getCharPositionInLine(), type, isPrimitive(type), isDefined(type));
            addToTable(key, methodVarSymbol);
        }
    }

    public void exitExpressionOther(ToorlaParser.ExpressionOtherContext ctx) {

    }

    public void enterToorlaType(ToorlaParser.ToorlaTypeContext ctx) {
    }

    public void exitToorlaType(ToorlaParser.ToorlaTypeContext ctx) {
    }

    public void enterSingleType(ToorlaParser.SingleTypeContext ctx) {
    }

    public void exitSingleType(ToorlaParser.SingleTypeContext ctx) {
    }

    public void enterEveryRule(ParserRuleContext ctx) {
    }

    public void exitEveryRule(ParserRuleContext ctx) {
    }

    public void visitTerminal(TerminalNode node) {
    }

    public void visitErrorNode(ErrorNode node) {
    }
}