package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class CatScriptParser {

    private TokenList tokens;
    private FunctionDefinitionStatement currentFunctionDefinition;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = null;
        try {
            expression = parseExpression();
        } catch(RuntimeException re) {
            // ignore :)
        }
        if (expression == null || tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================

    private Statement parseProgramStatement() {
        Statement stmt = parseStatement();
        if (stmt != null) {
            return stmt;
        }
        return new SyntaxErrorStatement(tokens.consumeToken());


    }

    private Statement parseStatement() {
        try {

            Statement stmt = parsePrintStatement();
            if(stmt != null)
            {
                return stmt;
            }
            stmt = parseForStatement();
            if(stmt != null)
            {
                return stmt;
            }
            stmt = parseIfStatement();
            if(stmt != null)
            {
                return stmt;
            }
            stmt = parseVarStatement();
            if(stmt != null)
            {
                return stmt;
            }
            stmt = parseAssignmentOrFunctionCallStatement();
            if(stmt != null)
            {
                return stmt;
            }
            stmt = parseFunctionDefinition();
            if(stmt != null)
            {
                return stmt;
            }
            stmt = parseFunctionDeclarationStatement();
            if(stmt != null){
                return stmt;
            }
            if(currentFunctionDefinition != null){
                stmt = parseReturnStatement();
                if(stmt != null)
                {
                    return stmt;
                }
            }
            return new SyntaxErrorStatement(tokens.consumeToken());
        } catch (UnknownExpressionParseException e) {
            SyntaxErrorStatement syntaxErrorStatement = new SyntaxErrorStatement(tokens.consumeToken());
            while(tokens.hasMoreTokens()) {
                if(tokens.match(VAR, FOR, IF, ELSE, PRINT )) {
                    break;
                }
                else {
                    tokens.consumeToken();
                }
            }
            return syntaxErrorStatement;
        }
    }

    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        }
            return null;
    }

    private Statement parseForStatement() {
        if(tokens.match(FOR)){
            ForStatement forStatement = new ForStatement();
            forStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, forStatement);
            forStatement.setVariableName(tokens.consumeToken().getStringValue());
            require(IN, forStatement);
            forStatement.setExpression(parseExpression());
            require(RIGHT_PAREN, forStatement);
            require(LEFT_BRACE, forStatement);
            List<Statement> statements = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE)){
                statements.add(parseStatement());
                if(tokens.match(EOF) || tokens.match(RIGHT_BRACE)){
                    break;
                }
            }
            forStatement.setBody(statements);
            forStatement.setEnd(require(RIGHT_BRACE, forStatement));
            return forStatement;
        }
        return null;
    }

    private Statement parseIfStatement() {
        if(tokens.match(IF)){
            IfStatement ifStatement = new IfStatement();
            ifStatement.setStart(tokens.consumeToken());
            require(LEFT_PAREN, ifStatement);
            Expression parsedBool = parseExpression();
            ifStatement.setExpression(parsedBool);
            require(RIGHT_PAREN, ifStatement);
            require(LEFT_BRACE, ifStatement);
            List<Statement> statements = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE)){
                statements.add(parseStatement());
                if(tokens.match(EOF)){
                    break;
                }
            }
            ifStatement.setTrueStatements(statements);
            require(RIGHT_BRACE, ifStatement);
            if(tokens.match(ELSE)){
                tokens.consumeToken();
                require(LEFT_BRACE, ifStatement);
                if(tokens.match(EOF)){
                    ifStatement.addError(ErrorType.UNTERMINATED_ARG_LIST);
                    return ifStatement;
                }
                if(tokens.match(IF)){
                    parseIfStatement();
                }
                else {
                    if(tokens.match(EOF)){
                        ifStatement.addError(ErrorType.UNTERMINATED_ARG_LIST);
                    }
                    List<Statement> elseStatements = new LinkedList<>();
                    while(!tokens.match(RIGHT_BRACE)){
                        elseStatements.add(parseStatement());
                        if(tokens.match(EOF)){
                            break;
                        }
                    }
                    ifStatement.setElseStatements(elseStatements);
                    require(RIGHT_BRACE, ifStatement);

                }
            }
            return ifStatement;
        }
        return null;
    }

    private Statement parseVarStatement() {
        if(tokens.match(VAR)){
            VariableStatement variableStatement = new VariableStatement();
            variableStatement.setStart(tokens.consumeToken());
            String identifierString = tokens.consumeToken().getStringValue();

            if(tokens.match(COLON)){
                tokens.consumeToken();
                String explicitIdentifierString = tokens.consumeToken().getStringValue();
                CatscriptType variableType = parseTypeExpression(explicitIdentifierString);
                variableStatement.setExplicitType(variableType);

            }

            require(EQUAL,variableStatement);
            variableStatement.setVariableName(identifierString);
            Expression endExp = parseExpression();
            variableStatement.setExpression(endExp);
            variableStatement.setEnd(endExp.getEnd());

            return variableStatement;
        }
        return null;
    }

    private CatscriptType parseTypeExpression(String explicitIdentifierString){
        Token currentToken = tokens.getCurrentToken();
        if(explicitIdentifierString.equals("int")) {
            return  CatscriptType.INT;
        }
        else if(explicitIdentifierString.equals("bool")) {
            return CatscriptType.BOOLEAN;
        }
        else if(explicitIdentifierString.equals("string")) {
            return CatscriptType.STRING;
        }
        else if(explicitIdentifierString.equals("object")) {
            return CatscriptType.OBJECT;
        }
        else if(explicitIdentifierString.equals("list")) {
            tokens.consumeToken();
            String listType = tokens.consumeToken().getStringValue();
            if(listType.equals("int")) {
                tokens.consumeToken();
                return CatscriptType.getListType(CatscriptType.INT);
            }
        }
        return null;
    }

    private Statement parseAssignmentOrFunctionCallStatement() {
        if(tokens.match(IDENTIFIER)){
            Token identifier = tokens.getCurrentToken();
            String identifierString = tokens.consumeToken().getStringValue();

            if(tokens.match(LEFT_PAREN)){

                List<Expression> funcArgs = new ArrayList<>();
                Token firstParen = tokens.consumeToken();
                if (tokens.match(RIGHT_PAREN)) {
                    FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierString, funcArgs);
                    FunctionCallStatement functionCallStatement = new FunctionCallStatement(functionCallExpression);
                    tokens.consumeToken();
                    return functionCallStatement;

                }

                else {
                    Expression firstArg = parseExpression();
                    funcArgs.add(firstArg);
                    while (tokens.match(COMMA)) {
                        tokens.consumeToken();
                        Expression argument = parseExpression();
                        funcArgs.add(argument);
                    }
                }
                boolean unterminated = false;
                if (!tokens.match(RIGHT_PAREN)) {
                    unterminated = true;

                }
                FunctionCallExpression functionCallExpression = new FunctionCallExpression(identifierString,funcArgs);
                FunctionCallStatement functionCallStatement = new FunctionCallStatement(functionCallExpression);
                if(unterminated){
                    functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
                }
                tokens.consumeToken();
                return functionCallStatement;


            }
            else if(tokens.match(EQUAL)) {

                AssignmentStatement assignmentStatement = new AssignmentStatement();
                assignmentStatement.setStart(tokens.getCurrentToken());
                assignmentStatement.setVariableName(identifierString);
                require(EQUAL, assignmentStatement);
                Expression endExp = parseExpression();
                assignmentStatement.setExpression(endExp);
                assignmentStatement.setEnd(endExp.getEnd());
                return assignmentStatement;
            }
        }
        return null;
    }

    private FunctionDefinitionStatement parseFunctionDefinition() {
        if (tokens.match(FUNCTION)) {
            FunctionDefinitionStatement func = new FunctionDefinitionStatement();
            func.setStart(tokens.consumeToken());
            Token functionName = require(IDENTIFIER, func);
            func.setName(functionName.getStringValue());
            require(LEFT_PAREN, func);
            if(!tokens.match(RIGHT_PAREN)) {
                do{
                    Token paramName = require(IDENTIFIER, func);
                    TypeLiteral typeLiteral = null;
                    if(tokens.matchAndConsume(COLON)){
                        typeLiteral = parseTypeLiteral();
                    }
                    func.addParameter(paramName.getStringValue(), typeLiteral);
                } while (tokens.matchAndConsume(COMMA));

            }
            require(RIGHT_PAREN,func);
            TypeLiteral typeLiteral = null;
            if(tokens.matchAndConsume(COLON)) {
                typeLiteral = parseTypeLiteral();
            }
            func.setType(typeLiteral);

            currentFunctionDefinition = func;

            require(LEFT_BRACE, func);
            LinkedList<Statement> statements = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE) && tokens.hasMoreTokens()){
                statements.add(parseStatement());
            }
            require(RIGHT_BRACE, func);
            func.setBody(statements);

            return func;
        }
        else {
            return null;
        }
    }

    private TypeLiteral parseTypeLiteral() {
        if(tokens.match("int")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.INT);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        if(tokens.match("string")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.STRING);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        if(tokens.match("bool")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.BOOLEAN);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        if(tokens.match("object")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.OBJECT);
            typeLiteral.setToken(tokens.consumeToken());
            return typeLiteral;
        }
        if(tokens.match("list")) {
            TypeLiteral typeLiteral = new TypeLiteral();
            typeLiteral.setType(CatscriptType.getListType(CatscriptType.OBJECT));
            typeLiteral.setToken(tokens.consumeToken());
            if(tokens.matchAndConsume(LESS)) {
                TypeLiteral componentType = parseTypeLiteral();
                typeLiteral.setType(CatscriptType.getListType(componentType.getType()));
                require(GREATER, typeLiteral);
            }
            return typeLiteral;
        }
        TypeLiteral typeLiteral = new TypeLiteral();
        typeLiteral.setType(CatscriptType.OBJECT);
        typeLiteral.setToken(tokens.consumeToken());
        typeLiteral.addError(ErrorType.BAD_TYPE_NAME);
        return typeLiteral;
    }

    private Statement parseFunctionDeclarationStatement(){
        if(tokens.match(FUNCTION)){
            try {
                currentFunctionDefinition = new FunctionDefinitionStatement();
                Token start = tokens.consumeToken();
                currentFunctionDefinition.setStart(start);
                Token functionName = require(IDENTIFIER, currentFunctionDefinition);
                currentFunctionDefinition.setName(functionName.getStringValue());
                require(LEFT_PAREN, currentFunctionDefinition);

                while (!tokens.match(RIGHT_PAREN)) {
                    if (tokens.match(EOF)) {
                        break;
                    } else {
                        Token paramToken = tokens.consumeToken();
                        if (tokens.match(COLON)) {
                            tokens.consumeToken();
                            TypeLiteral paramType = parseTypeLiteral();
                            currentFunctionDefinition.addParameter(paramToken.getStringValue(), paramType);
                            if(tokens.match(COMMA)) {
                                tokens.consumeToken();
                            }
                        } else {
                            TypeLiteral objectType = new TypeLiteral();
                            objectType.setType(CatscriptType.OBJECT);
                            currentFunctionDefinition.addParameter(paramToken.getStringValue(), objectType);
                            if (tokens.match(COMMA)) {
                                tokens.consumeToken();
                            }
                        }

                    }
                }
                require(RIGHT_PAREN, currentFunctionDefinition);
                if (tokens.match(COLON)) {
                    tokens.consumeToken();
                    TypeLiteral functionType = parseTypeLiteral();
                    currentFunctionDefinition.setType(functionType);

                } else {
                    TypeLiteral voidType = new TypeLiteral();
                    voidType.setType(CatscriptType.VOID);
                    currentFunctionDefinition.setType(voidType);
                }
                require(LEFT_BRACE, currentFunctionDefinition);
                List<Statement> functionBodyStatements = new LinkedList<>();
                while (!tokens.match(RIGHT_BRACE)) {
                    if (tokens.match(EOF)) {
                        break;
                    } else {
                        functionBodyStatements.add(parseFunctionBodyStatement());
                    }
                }
                currentFunctionDefinition.setBody(functionBodyStatements);

                if (tokens.match(EOF)) {
                    currentFunctionDefinition.addError(ErrorType.UNTERMINATED_ARG_LIST);
                }
                currentFunctionDefinition.setEnd(require(RIGHT_BRACE, currentFunctionDefinition));
                return currentFunctionDefinition;
            } finally {
                currentFunctionDefinition = null;
            }
        }
        return null;
    }

    private Statement parseFunctionBodyStatement(){
        return parseStatement();
    }


    private Statement parseReturnStatement() {
        if (tokens.match(RETURN)){
            ReturnStatement returnStatement = new ReturnStatement();
            returnStatement.setStart(tokens.consumeToken());
            returnStatement.setFunctionDefinition(currentFunctionDefinition);
            if(tokens.match(RIGHT_BRACE)){
                returnStatement.setEnd(tokens.getCurrentToken());
                return returnStatement;
            }
            else {
                returnStatement.setExpression(parseExpression());
                returnStatement.setEnd(tokens.getCurrentToken());
                return returnStatement;
            }
        }
        return null;
    }


    //============================================================
    //  Expressions
    //============================================================

    private Expression parseExpression() {
        return parseEqualityExpression();
    }

    private Expression parseAdditiveExpression() {
        Expression expression = parseFactorExpression();
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseFactorExpression();
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, expression, rightHandSide);
            additiveExpression.setStart(expression.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            expression = additiveExpression;
        }
        return expression;
    }

    private Expression parseFactorExpression() {
        Expression expression = parseUnaryExpression();
        while (tokens.match(SLASH, STAR)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseUnaryExpression();
            FactorExpression factorExpression = new FactorExpression(operator, expression, rightHandSide);
            factorExpression.setStart(expression.getStart());
            factorExpression.setEnd(rightHandSide.getEnd());
            expression = factorExpression;
        }
        return expression;
    }

    private Expression parseEqualityExpression() {
        Expression expression = parseComparisonExpression();
        while (tokens.match(EQUAL_EQUAL,BANG_EQUAL)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseComparisonExpression();
            EqualityExpression equalityExpression = new EqualityExpression(operator, expression, rightHandSide);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(rightHandSide.getEnd());
            expression = equalityExpression;
        }
        return expression;
    }

    private Expression parseComparisonExpression() {
        Expression expression = parseAdditiveExpression();
        while (tokens.match(GREATER, GREATER_EQUAL, LESS,LESS_EQUAL)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseAdditiveExpression();
            ComparisonExpression comparisonExpression = new ComparisonExpression(operator, expression, rightHandSide);
            comparisonExpression.setStart(expression.getStart());
            comparisonExpression.setEnd(rightHandSide.getEnd());
            expression = comparisonExpression;
        }
        return expression;
    }

    private Expression parseUnaryExpression() {
        if (tokens.match(MINUS, NOT)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseUnaryExpression();
            UnaryExpression unaryExpression = new UnaryExpression(token, rhs);
            unaryExpression.setStart(token);
            unaryExpression.setEnd(rhs.getEnd());
            return unaryExpression;
        } else {
            return parsePrimaryExpression();
        }
    }

    private Expression parsePrimaryExpression() {
        if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerExpression.setToken(integerToken);
            return integerExpression;
        }
        else if(tokens.match(STRING)){
            Token strToken = tokens.consumeToken();
            StringLiteralExpression strExpression = new StringLiteralExpression(strToken.getStringValue());
            strExpression.setToken(strToken);
            return  strExpression;
        }
        else if(tokens.match(TRUE, FALSE)) {
            Token boolToken = tokens.consumeToken();
            BooleanLiteralExpression boolExpression = new BooleanLiteralExpression(boolToken.getType() == TRUE);
            boolExpression.setToken(boolToken);
            return  boolExpression;
        }
        else if (tokens.match(NULL)){
            Token nullToken = tokens.consumeToken();
            NullLiteralExpression nullLiteralExpression = new NullLiteralExpression();
            nullLiteralExpression.setToken(nullToken);
            return nullLiteralExpression;
        }
        else if (tokens.match(IDENTIFIER)) {
            Token start = tokens.consumeToken();
            if(tokens.matchAndConsume(LEFT_PAREN)){
                return parseFunctionCall(start);
            }
            else{
                IdentifierExpression expr = new IdentifierExpression(start.getStringValue());
                expr.setToken(start);
                return expr;
            }
        }
        else if (tokens.match(LEFT_BRACKET)){
            Token start = tokens.consumeToken();
            List<Expression> values = new LinkedList<>();
            if (!tokens.match(RIGHT_BRACKET)) {
                do {
                    values.add(parseExpression());
                }
                while (tokens.matchAndConsume(COMMA) && tokens.hasMoreTokens());
            }
            ListLiteralExpression expr = new ListLiteralExpression(values);
            expr.setStart(start);
            expr.setEnd(require(RIGHT_BRACKET, expr, ErrorType.UNTERMINATED_LIST));
            return expr;
        }
        else if (tokens.match(LEFT_PAREN)){
            Token start = tokens.consumeToken();
            ParenthesizedExpression expr = new ParenthesizedExpression(parseExpression());
            expr.setStart(start);
            if(tokens.match((RIGHT_PAREN))) {
                expr.setEnd(tokens.consumeToken());

            }
            else{
                expr.setEnd(tokens.getCurrentToken());
                expr.addError(ErrorType.UNTERMINATED_LIST);
            }
            return expr;
        }
        else {

            throw new UnknownExpressionParseException();

//            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
//            syntaxErrorExpression.setToken(tokens.consumeToken());
//            return syntaxErrorExpression;
        }
    }

    class UnknownExpressionParseException extends RuntimeException{

    }

    private Expression parseFunctionCall(Token token) {
        List<Expression> expr = new LinkedList<>();
        while (!tokens.match(RIGHT_PAREN)) {
            if (!tokens.hasMoreTokens()) {
                FunctionCallExpression errList = new FunctionCallExpression(token.getStringValue(), expr);
                errList.addError(ErrorType.UNTERMINATED_ARG_LIST);
                return errList;
            } else if (tokens.match(COMMA)) {
                tokens.consumeToken();
            } else {
                Expression exp = parseExpression();
                expr.add(exp);
            }

        }
        tokens.consumeToken();
        return new FunctionCallExpression(token.getStringValue(), expr);
    }


    private Expression parseListLiteral() {
        if(tokens.match(LEFT_BRACKET)) {
            Token listStart = tokens.consumeToken();
            List<Expression> expr = new ArrayList<>();
            do {
                Expression expression = parseExpression();
                expr.add(expression);
            } while (tokens.matchAndConsume(COMMA));
            ListLiteralExpression listLiteralExpression = new ListLiteralExpression(expr);
            listLiteralExpression.setStart(listStart);
            boolean foundBracket = tokens.match(RIGHT_BRACKET);
            if(foundBracket) {
                Token token = tokens.consumeToken();
                listLiteralExpression.setEnd(token);
            } else {
                listLiteralExpression.addError(ErrorType.UNTERMINATED_LIST);
            }
            return listLiteralExpression;
        }
        return null;
    }

    private Expression parseParenthesizedExpression() {
        if(tokens.match(LEFT_PAREN)) {
            Token parenStart = tokens.consumeToken();
            Expression innerExpression = parseExpression();
            ParenthesizedExpression parenExpr = new ParenthesizedExpression(innerExpression);
            parenExpr.setStart(parenStart);
            boolean foundRightParen = tokens.match(RIGHT_PAREN);
            if(foundRightParen) {
                Token token = tokens.consumeToken();
                parenExpr.setEnd(token);
            } else {
              parenExpr.addError(ErrorType.UNEXPECTED_TOKEN);
            }
            return parenExpr;
        }
        return null;
    }



    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if(tokens.match(type)){
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}
