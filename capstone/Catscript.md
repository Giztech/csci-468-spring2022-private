# Catscript Guide

## Introduction
Catscript is a simple scripting language that is comparable to languages like Java and Python.
The Catscript Compiler program is a program that takes in, processes, and executes code formatted in the Catscript language.
The execution of that code is based on its Java interpretation and the compiler translates between them.
An example of the Catsript language can be seen here with more examples shown in the [features](#features) section:

```
var x = "Hello World"
print(x)
```

## Features

### Statements

#### For loops

For loops in Catscript are able to iterate over defined lists and perform operations per iteration. Operations are able to contain more types of statements such as [print](#print-statements) and [if](#if-statements) statements with no issue.
Example:
``` 
for(x in [1, 2, 3]) {
    print(x)
}
```
Output:
``` 
1
2
3
```
#### If Statements
If statements in Catscript are able to have conditional statements that restrict access to a portion of code unless the conditional statement is met.  
Example with conditional met:

``` 
if(1 < 2) {
    print("One is less than two")
}
```
Example with conditional not met:

``` 
if(1 > 2) {
    print("One is greater than two")
}
```

#### Print Statements
Print statements in Catscript are able to take and print its contents such as String or Integer Literals.  
Example:
``` 
print("Hello World")
```
Output:
``` 
Hello World
```

#### Variable Statements
Variable Statements in Catscript are able to assign values to variable names. Any of the [primary expressions](#primary-expressions) are assignable to variable names. Types can be explicitly or implicitly defined when creating the variables.  
Examples:
``` 
Var x = 1
```
Or:
``` 
Var x : int = 1
```

#### Assignment statements
Assignment statements in Catscript are used when a variable is being assigned to a different value. Variables can only be re-assigned to the same type that they were implicitly or explicitly defined as, an Integer variable cannot be assigned to a String value.  
Example:
``` 
Var x = 1

x = 3
```

#### Function Definition Statements
Function Definition Statements in Catscript are statements that define a function that is callable elsewhere in the program. The Function can be defined with parameters and must be defined with a body. The parameters can be defined explicitly or implicitly just like [variable statements](#variable-statements). The body can contain any number and types of statements. Functions are also able to contain [return statements](#return-statements) which should end the function. Functions with return statements should have a return type defined after the name and parameters of the function.  
Examples:
``` 
foo(a, b, c) {
    print(a)
    print(b)
    print(c)
}
```
Or:
``` 
foo(a: string, b: string, c: string) {
    print(a + b + c)
}
```
Or:
``` 
foo(a: int, b: int, c: int) : int {
    return a * b * c
}
```

#### Return Statements
Return Statements in Catscript are statements that pass values from functions to where they were called in the greater scope. Return statements will end the function they are contained within. Return statements can have expressions contained within them.  
Examples:
``` 
return x
```
Or:
```
return 1 + 1
```

### Expressions

#### Primary Expressions
There are eight types of expressions defined under primary expressions: Identifier, Integer Literal, String Literal, Boolean Literal, List Literal, Null Literal, Function Call, and Parenthesized Expressions.
Below, you will find short descriptions and examples of each. 

Identifier Expressions are expressions that represent a keyword defined by the user:
``` 
x
y
z
```
Integer Literal Expressions are expressions that represent integer numbers:
``` 
42
144
```
String Literal Expressions are expressions that represent strings of characters:
``` 
"Hello World"
"I am alive"
```
Boolean Literal Expressions are expressions that represent the True and False symbols:
``` 
True
False
```
List Literal Expressions are expressions that represent a set of Integer, String, Boolean, and List Literal Expressions:
``` 
[1, 2, 3]
["Hello", "World"]
```
Null Literal Expressions are expressions that represent the null symbol. Null Literal Expressions are used when there is no value represented for a variable:
``` 
null
```
Function Call Expressions are expressions that contain information about what information to send to an existing functions parameters. Function Call Expressions are used to signal the execution of a function during runtime:
``` 
foo(1, 2, 3)
```
Parenthesized Expressions are expressions that contain any type of expression inside two parentheses. The parentheses do not affect the contained expressions in any way: 
``` 
("Hello" + "World")
(12 < 24)
```

#### Unary Expressions
Unary Expressions are expressions that are applied to only one expression. The two symbols that a unary expression can have, are the negative symbol and not symbol which can only be applied to Integer Literals and Boolean Literals respectively.  
Examples:
``` 
-1
not True
```

#### Equality Expressions
Equality Expressions are expressions that have a double equal or bang equal symbol separating two expressions, with the double equal symbol asserting both sides are the same and the bang equal symbol asserting both sides are different. The separated expressions may be any type of expression.  
Examples:
``` 
True == True
True != False
```

#### Comparison Expressions
Comparison Expressions are expressions that have a less than, greater than, less than or equal to, or greater than or equal to symbol separating two expressions. The separated expressions may only be Integer Literals.  
Examples:
``` 
1 < 2
2 > 1
x <= y
y >= x
```

#### Additive Expressions
Additive Expressions are expressions that have an addition or subtraction symbol, a plus or a minus respectively, separating two expressions. The separated expressions may be Integer Literals, String Literals, or Parenthesized expressions containing either Integer or String Literals. String Literals can only be added together and not subtracted.  
Examples:
``` 
"a" + "b"
2 - 1
```

#### Factor Expressions
Factor Expressions are expressions that have a multiplication or division symbol, an asterisk or a slash respectively, separating two expressions. The separated expressions may only be Integer Literals or Parenthesized Expressions containing Integer Literals.  
Examples:
``` 
5 * 6
30 / 5
```
