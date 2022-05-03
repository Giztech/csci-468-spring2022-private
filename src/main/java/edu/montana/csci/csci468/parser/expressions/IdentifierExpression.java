package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import org.objectweb.asm.Opcodes;

import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

public class IdentifierExpression extends Expression {
    private final String name;
    private CatscriptType type;

    public IdentifierExpression(String value) {
        this.name = value;
    }

    public String getName() {
        return name;
    }

    @Override
    public CatscriptType getType() {
        return type;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        CatscriptType type = symbolTable.getSymbolType(getName());
        if (type == null) {
            addError(ErrorType.UNKNOWN_NAME);
        } else {
            this.type = type;
        }
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        return runtime.getValue(name);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        Integer integer = code.resolveLocalStorageSlotFor(getName());
        if (integer != null) {
            if (type.equals(CatscriptType.INT) || type.equals(CatscriptType.BOOLEAN)) {
                code.addVarInstruction(Opcodes.ILOAD, integer);
            } else {
                code.addVarInstruction(Opcodes.ALOAD, integer);
            }
        } else {
            code.addVarInstruction(Opcodes.ALOAD, 0);
            if (type.equals(CatscriptType.INT) || type.equals(CatscriptType.BOOLEAN)) {
                code.addFieldInstruction(Opcodes.GETFIELD, name, "I", code.getProgramInternalName());
            } else {
                code.addFieldInstruction(Opcodes.GETFIELD, name, "L"+internalNameFor(type.getJavaType())+";", code.getProgramInternalName());
            }
        }

    }


}
