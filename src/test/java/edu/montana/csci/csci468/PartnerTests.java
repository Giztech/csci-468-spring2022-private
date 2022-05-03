package edu.montana.csci.csci468;
import edu.montana.csci.csci468.parser.ParseErrorException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PartnerTests extends CatscriptTestBase {



    @Test
    void scopingWorksProperly() {
        assertThrows(ParseErrorException.class, () -> {
            executeProgram("var x = false function func() {var x : int = 1}");
        });
    }

    @Test
    void variableAssignment() {
        String  testString = "var x = 0\n" +
                "print(x)\n" +
                "x = 1\n" +
                "print(x)";

        assertEquals("0\n1\n", executeProgram(testString));
    }

    @Test
    void IfFunctionWorksProperly() {
        assertEquals("false\n", executeProgram("function foo(x : int) : bool { if (x>0) {return true}" +
                "else {return false}} print(foo(0))"));
    }

}
