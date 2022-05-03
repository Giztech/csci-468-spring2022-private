package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenType;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class EqualityExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public EqualityExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
        this.leftHandSide = addChild(leftHandSide);
        this.rightHandSide = addChild(rightHandSide);
        this.operator = operator;
    }

    public Expression getLeftHandSide() {
        return leftHandSide;
    }

    public Expression getRightHandSide() {
        return rightHandSide;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    public boolean isEqual() {
        return operator.getType().equals(TokenType.EQUAL_EQUAL);
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        leftHandSide.validate(symbolTable);
        rightHandSide.validate(symbolTable);
    }

    @Override
    public CatscriptType getType() {
        return CatscriptType.BOOLEAN;
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        if(operator.equals("==")){
            return getLeftHandSide() == getRightHandSide();
        }
        else{
            return getLeftHandSide() != getRightHandSide();
        }
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        leftHandSide.compile(code);
        box(code, leftHandSide.getType());
        rightHandSide.compile(code);
        box(code, rightHandSide.getType());
        if (isEqual()) {
            Label endLabel = new Label();
            Label falseLabel = new Label();
            code.addJumpInstruction(Opcodes.IF_ACMPNE, falseLabel);
            code.addInstruction(Opcodes.ICONST_1);
            code.addJumpInstruction(Opcodes.GOTO, endLabel);
            code.addLabel(falseLabel);
            code.addInstruction(Opcodes.ICONST_0);
            code.addLabel(endLabel);
        } else {
            Label endLabel = new Label();
            Label trueLabel = new Label();
            code.addJumpInstruction(Opcodes.IF_ACMPNE, trueLabel);
            code.addInstruction(Opcodes.ICONST_0);
            code.addJumpInstruction(Opcodes.GOTO, endLabel);
            code.addLabel(trueLabel);
            code.addInstruction(Opcodes.ICONST_1);
            code.addLabel(endLabel);
        }
    }


}
