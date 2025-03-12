/**
 * ModelPhy Language Grammar (PhyloSpec-aligned)
 * ANTLR4 Grammar for parsing ModelPhy files conforming to PhyloSpec
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
    : CONSTRAINT identifier EQUALS functionCall SEMICOLON
    ;

declaration
    : type identifier (EQUALS expression)? SEMICOLON
    ;

stochasticAssignment
    : type identifier TILDE distribution SEMICOLON
    | functionCall TILDE distribution SEMICOLON  // For MRCA constraints
    ;

deterministicAssignment
    : type identifier EQUALS expression SEMICOLON
    ;

observationStatement
    : identifier OBSERVE LBRACKET keyValueList RBRACKET SEMICOLON
    | identifier OBSERVE FROM STRING_LITERAL SEMICOLON
    ;

keyValueList
    : keyValue (COMMA keyValue)*
    ;

keyValue
    : identifier EQUALS value=expression
    ;

expression
    : literal                                      # LiteralExpr
    | identifier                                   # IdentifierExpr
    | functionCall                                 # FunctionCallExpr
    | arrayLiteral                                 # ArrayExpr
    | LPAREN expression RPAREN                     # ParenExpr
    ;

functionCall
    : identifier LPAREN namedArgumentList? RPAREN
    ;

namedArgumentList
    : namedArgument (COMMA namedArgument)*
    ;

namedArgument
    : name=anyIdentifier EQUALS value=expression
    ;

distribution
    : identifier (LPAREN namedArgumentList? RPAREN)?
    ;

// Allow any identifier (including type names) when used as parameter names
anyIdentifier
    : IDENTIFIER
    | basicType
    ;

type
    : basicType
    | parameterizedType
    | arrayType
    ;

basicType
    : REAL
    | INTEGER
    | BOOLEAN
    | STRING
    | SIMPLEX
    | VECTOR
    | MATRIX
    | TIME_TREE
    | TREE
    | ALIGNMENT
    | SEQUENCE
    | Q_MATRIX
    | POSITIVE_REAL
    | PROBABILITY
    | TAXON
    | TAXON_SET
    | TREE_NODE
    ;

parameterizedType
    : simpleType LT type (COMMA type)* GT
    ;

simpleType
    : VECTOR
    | MATRIX
    | MAP
    | SEQUENCE
    | ALIGNMENT
    | IDENTIFIER  // For user-defined parameterized types
    ;

arrayType
    : basicType ARRAY_SUFFIX
    | parameterizedType ARRAY_SUFFIX
    ;

literal
    : INTEGER_LITERAL
    | FLOAT_LITERAL
    | STRING_LITERAL
    | BOOLEAN_LITERAL
    ;

arrayLiteral
    : LBRACKET (expression (COMMA expression)*)? RBRACKET
    ;

identifier
    : IDENTIFIER
    ;

// Lexer Rules

// Keywords
CONSTRAINT  : 'constraint';
OBSERVE     : 'observe';
FROM        : 'from';

// Type Keywords
REAL        : 'Real';
INTEGER     : 'Integer';
BOOLEAN     : 'Boolean';
STRING      : 'String';
SIMPLEX     : 'Simplex';
VECTOR      : 'Vector';
MATRIX      : 'Matrix';
TIME_TREE   : 'TimeTree';
TREE        : 'Tree';
ALIGNMENT   : 'Alignment';
SEQUENCE    : 'Sequence';
Q_MATRIX    : 'QMatrix';
POSITIVE_REAL : 'PositiveReal';
PROBABILITY : 'Probability';
TAXON       : 'Taxon';
TAXON_SET   : 'TaxonSet';
TREE_NODE   : 'TreeNode';
MAP         : 'Map';

// Operators and punctuation
TILDE       : '~';
EQUALS      : '=';
SEMICOLON   : ';';
COMMA       : ',';
LPAREN      : '(';
RPAREN      : ')';
LBRACKET    : '[';
RBRACKET    : ']';
LT          : '<';
GT          : '>';
ARRAY_SUFFIX : '[]';

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

// Fixed comment rule with skip at the very end of each alternative
LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;

BLOCK_COMMENT
    : '/*' .*? '*/' -> skip
    ;

WS
    : [ \t\r\n]+ -> skip
    ;