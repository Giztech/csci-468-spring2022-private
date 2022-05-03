package edu.montana.csci.csci468.parser.statements;


import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.BooleanLiteralExpression;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class IfStatement extends Statement {
    private Expression expression;
    private List<Statement> trueStatements = Collections.emptyList();
    private List<Statement> elseStatements = Collections.emptyList();

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = addChild(expression);
    }

    public List<Statement> getTrueStatements() {
        return trueStatements;
    }

    public void setTrueStatements(List<Statement> statements) {
        this.trueStatements = new LinkedList<>();
        for (Statement statement : statements) {
            this.trueStatements.add(addChild(statement));
        }
    }

    public List<Statement> getElseStatements() {
        return elseStatements;
    }

    public void setElseStatements(List<Statement> statements) {
        this.elseStatements = new LinkedList<>();
        for (Statement statement : statements) {
            this.elseStatements.add(addChild(statement));
        }
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
        if (!expression.getType().equals(CatscriptType.BOOLEAN)) {
            expression.addError(ErrorType.INCOMPATIBLE_TYPES);
        }
        symbolTable.pushScope();
        for (Statement trueStatement : trueStatements) {
            trueStatement.validate(symbolTable);
        }
        symbolTable.popScope();
        symbolTable.pushScope();
        for (Statement elseStatement : elseStatements) {
            elseStatement.validate(symbolTable);
        }
        symbolTable.popScope();
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        Object condition = expression.evaluate(runtime);

        if(condition.equals(true)){
            for (Statement statement : trueStatements) {
                statement.execute(runtime);
            }
        }
        else if(condition.equals(false)) {
            for (Statement statement : elseStatements) {
                statement.execute(runtime);
            }
        }
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        expression.compile(code);
        Label elseLabel = new Label();
        Label endLabel = new Label();

        if (elseStatements.size() > 0) {
            code.addJumpInstruction(Opcodes.IFEQ, elseLabel);
        }else {
            code.addJumpInstruction(Opcodes.IFEQ, endLabel);
        }
        for (Statement trueStatement : trueStatements) {
            trueStatement.compile(code);
        }

        code.addJumpInstruction(Opcodes.GOTO, endLabel);
        if (elseStatements.size() > 0){
            code.addLabel(elseLabel);
            for(Statement elseStatement: elseStatements){
                elseStatement.compile(code);
            }
        }


        code.addLabel(endLabel);
    }
}
