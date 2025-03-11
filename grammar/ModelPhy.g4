/**
 * ModelPhy Language Grammar
 * ANTLR4 Grammar for parsing ModelPhy files
 */

grammar ModelPhy;

@header {
package org.modelphy.antlr;
}

// Parser Rules

program
    : statement+
    ;

statement
    : declaration
    | stochasticAssignment
    | deterministicAssignment
    | observationStatement
    | constraintStatement
    ;

constraintStatement
    : 'constraint' identifier '=' functionCall ';'
    ;

declaration
    : type identifier ('=' expression)? ';'
    ;

stochasticAssignment
    : type identifier '~' distribution ';'
    | functionCall '~' distribution ';'  // For MRCA constraints
    ;

deterministicAssignment
    : type identifier '=' expression ';'
    ;

observationStatement
    : identifier 'observe' '[' keyValueList ']' ';'
    | identifier 'observe' 'from' STRING_LITERAL ';'
    ;

keyValueList
    : keyValue (',' keyValue)*
    ;

keyValue
    : identifier '=' value=expression
    ;

expression
    : literal                                      # LiteralExpr
    | identifier                                   # IdentifierExpr
    | functionCall                                 # FunctionCallExpr
    | arrayLiteral                                 # ArrayExpr
    | '(' expression ')'                           # ParenExpr
    ;

functionCall
    : identifier '(' namedArgumentList? ')'
    ;

namedArgumentList
    : namedArgument (',' namedArgument)*
    ;

namedArgument
    : name=anyIdentifier '=' value=expression
    ;

distribution
    : identifier '(' namedArgumentList? ')'
    ;

// Allow any identifier (including type names) when used as parameter names
anyIdentifier
    : IDENTIFIER
    | 'real'
    | 'integer'
    | 'boolean'
    | 'string'
    | 'simplex'
    | 'vector'
    | 'matrix'
    | 'timetree'
    | 'tree'
    | 'alignment'
    | 'sequence'
    | 'substmodel'
    ;

type
    : 'real'
    | 'integer'
    | 'boolean'
    | 'string'
    | 'simplex'
    | 'vector'
    | 'matrix'
    | 'timetree'
    | 'tree'
    | 'alignment'
    | 'sequence'
    | 'substmodel'
    | arrayType
    ;

arrayType
    : baseType '[' ']'
    ;

baseType
    : 'real'
    | 'integer'
    | 'boolean'
    | 'string'
    | identifier  // For user-defined types
    ;

literal
    : INTEGER_LITERAL
    | FLOAT_LITERAL
    | STRING_LITERAL
    | BOOLEAN_LITERAL
    ;

arrayLiteral
    : '[' (expression (',' expression)*)? ']'
    ;

identifier
    : IDENTIFIER
    ;

// Lexer Rules

BOOLEAN_LITERAL
    : 'true'
    | 'false'
    ;

IDENTIFIER
    : [a-zA-Z][a-zA-Z0-9_]*
    ;

INTEGER_LITERAL
    : [0-9]+
    ;

FLOAT_LITERAL
    : [0-9]+ '.' [0-9]*
    | '.' [0-9]+
    ;

STRING_LITERAL
    : '"' ('\\"' | ~["])* '"'
    ;

COMMENT
    : '//' ~[\r\n]* -> skip
    ;

WS
    : [ \t\r\n]+ -> skip
    ;