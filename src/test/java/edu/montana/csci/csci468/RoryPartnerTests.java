package edu.montana.csci.csci468;
import edu.montana.csci.csci468.CatscriptTestBase;
import edu.montana.csci.csci468.parser.expressions.*;
import org.junit.jupiter.api.Test;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;
import static org.junit.jupiter.api.Assertions.*;

public class RoryPartnerTests extends CatscriptTestBase{
    @Test
    public void symbolsAndCommentsTokenizeTogether(){
        //This test verifies that symbols and comments are tokenized all in the same set of code
        assertTokensAre("9 + 10 = 21\n" +
                        " // no its not",
                INTEGER, PLUS, INTEGER, EQUAL, INTEGER,SLASH, SLASH, EOF);
    }

    @Test
    public void largeExpressionIsHandledCorrectly(){
        //This test verifies that a large expression is recognized and evaluated correctly
        String strExpr = "(36 * (81 / 9)) + ((52 / 13) - (39 * 69))";
        AdditiveExpression expr = parseExpression(strExpr);
        assertTrue(expr.isAdd());
        assertTrue(((ParenthesizedExpression)expr.getLeftHandSide()).getExpression() instanceof FactorExpression);
        assertTrue(((ParenthesizedExpression)expr.getRightHandSide()).getExpression() instanceof AdditiveExpression);

        //This set of code verifies the left side of the larger expressions
        FactorExpression exprLeft =
                (FactorExpression) ((ParenthesizedExpression)expr.getLeftHandSide()).getExpression();
        assertTrue(exprLeft.getLeftHandSide() instanceof IntegerLiteralExpression);
        assertTrue(((ParenthesizedExpression) exprLeft.getRightHandSide()).getExpression() instanceof FactorExpression);
        assertEquals(((IntegerLiteralExpression) exprLeft.getLeftHandSide()).getValue(), 36);

        //This set of code verifies the right side of the larger expressions
        AdditiveExpression exprRight =
                (AdditiveExpression) ((ParenthesizedExpression)expr.getRightHandSide()).getExpression();
        assertTrue(((ParenthesizedExpression) exprRight.getLeftHandSide()).getExpression()
                instanceof FactorExpression);
        assertTrue(((ParenthesizedExpression) exprRight.getRightHandSide()).getExpression()
                instanceof  FactorExpression);
        assertEquals(((IntegerLiteralExpression) ((FactorExpression) ((ParenthesizedExpression)
                exprRight.getLeftHandSide()).getExpression()).getRightHandSide()).getValue(), 13);

        //This line of code tests if the expression is evaluated properly
        assertEquals(evaluateExpression(strExpr), -2363);
    }

    @Test
    public void forStatementsIterateThroughDefinedLists(){
        /* This test verifies that a segment of code is able to properly execute, specifically testing
         * list variables and for loops.
         * */
        String forStmt = "var newList : list<int> = [1, 2, 3, 4, 5]\n" +
                "for(x in newList) { print(x * x) }";
        assertEquals("1\n4\n9\n16\n25\n", executeProgram(forStmt));
    }
}