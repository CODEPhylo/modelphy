# ModelPhy Language Specification

## Overview

ModelPhy is a domain-specific language (DSL) for specifying phylogenetic models in a platform-independent way. It provides a concise syntax for defining probabilistic models of sequence evolution, tree priors, and observed data. The language is designed to be both human-readable and machine-parsable, serving as an interchange format between different phylogenetic inference software packages.

## Version

This specification describes ModelPhy version 1.0.

## Lexical Structure

### Whitespace and Comments

Whitespace (spaces, tabs, newlines) is ignored except as a separator between tokens. Comments begin with `//` and continue to the end of the line.

### Identifiers

Identifiers are used to name variables in the model. They must begin with a letter and can contain letters, numbers, and underscores.

```
identifier ::= [a-zA-Z][a-zA-Z0-9_]*
```

### Literals

#### Numeric Literals

Numeric literals can be integers or floating-point numbers.

```
integer_literal ::= [0-9]+
float_literal   ::= [0-9]+ '.' [0-9]* | '.' [0-9]+
numeric_literal ::= integer_literal | float_literal
```

#### String Literals

String literals are enclosed in double quotes and may contain any character except unescaped double quotes.

```
string_literal ::= '"' (any_char_except_double_quote | '\"')* '"'
```

#### Array Literals

Array literals are enclosed in square brackets with comma-separated values.

```
array_literal ::= '[' value (',' value)* ']'
value         ::= numeric_literal | string_literal | array_literal | identifier
```

## Types

ModelPhy defines the following basic types:

- `real`: A continuous real number
- `integer`: A discrete integer
- `boolean`: A truth value (true or false)
- `string`: A sequence of characters
- `simplex`: A vector of non-negative real numbers that sum to 1
- `vector`: An ordered collection of numeric values
- `matrix`: A two-dimensional collection of numeric values
- `timetree`: A phylogenetic tree with branch lengths representing time
- `tree`: A general phylogenetic tree structure
- `alignment`: A collection of aligned sequences
- `sequence`: A single biological sequence
- `substmodel`: A substitution model defining transition probabilities between states

### Array Types

Arrays of any base type can be specified using square brackets:
- `real[]`: An array of real numbers
- `integer[]`: An array of integers
- `string[]`: An array of strings
- `<type>[]`: An array of any defined type

## Statements

### Variable Declaration and Assignment

Variables can be declared with a type and an identifier, and optionally assigned a value.

```
declaration ::= type identifier ('=' expression)? ';'
```

### Stochastic Assignment

Variables can be assigned values drawn from probability distributions using the tilde (~) operator.

```
stochastic_assignment ::= type identifier '~' distribution ';'
```

### Deterministic Assignment

Variables can be assigned values as the result of a deterministic function.

```
deterministic_assignment ::= type identifier '=' function_call ';'
```

### Observation Statement

Observed data can be attached to a stochastic variable using the observe keyword.

```
observation ::= identifier 'observe' '[' key_value_list ']' ';'
key_value_list ::= key_value (',' key_value)*
key_value ::= identifier '=' expression
```

### Constraint Statement

Constraints define relationships or restrictions in the model, particularly useful for tree calibrations.

```
constraint_statement ::= 'constraint' identifier '=' function_call ';'
```

Constraints can be assigned probability distributions:

```
function_call '~' distribution ';'
```

## Expressions

### Function Calls

Function calls consist of a function name followed by parenthesized, comma-separated arguments, which may be named.

```
function_call ::= identifier '(' (argument (',' argument)*)? ')'
argument ::= (identifier '=')? expression
```

### Distributions

Distributions are a special case of function calls that define probability distributions.

```
distribution ::= identifier '(' (argument (',' argument)*)? ')'
```

Common distributions include:
- `normal(mean, sd)`
- `lognormal(mean, sigma)`
- `exponential(mean)`
- `dirichlet(alpha)`
- `uniform(min, max)`

## Examples

### Basic HKY Model

```
// Define transition transversion ratio prior
real kappa ~ lognormal(mean=1.0, sigma=0.5);

// Define nucleotide frequency prior
simplex pi ~ dirichlet(alpha=[1.0, 1.0, 1.0, 1.0]);

// Create HKY substitution model
substmodel subst_model = hky(kappa=kappa, freqs=pi);

// Define birth rate and create Yule tree prior
real birth_rate ~ exponential(mean=0.1);
timetree phylogeny ~ yule(birthrate=birth_rate, n=3);

// Create phylogenetic CTMC model
alignment seq ~ phyloCTMC(tree=phylogeny, substmodel=subst_model);

// Attach observed sequence data
seq observe [ 
  human = sequence(str="ACGTACGTACGTACGTACGTACGT"),
  chimp = sequence(str="ACGTACGTACGTACGTATGTACGT"),
  gorilla = sequence(str="ACGTACGTACGCACGTACGTACGT")
];
```

### GTR+Gamma Model

```
// Define nucleotide frequency prior
simplex pi ~ dirichlet(alpha=[1.0, 1.0, 1.0, 1.0]);

// Define GTR rate matrix parameters
real a ~ exponential(mean=0.1);
real b ~ exponential(mean=0.1);
real c ~ exponential(mean=0.1);
real d ~ exponential(mean=0.1);
real e ~ exponential(mean=0.1);
real f ~ exponential(mean=0.1);

// Define gamma shape parameter
real alpha ~ exponential(mean=0.5);

// Create GTR+Gamma substitution model
substmodel subst_model = gtr(rates=[a,b,c,d,e,f], freqs=pi, gamma=alpha, categories=4);

// Define birth-death process parameters
real birth_rate ~ exponential(mean=0.1);
real death_rate ~ exponential(mean=0.05);

// Create tree from birth-death process
timetree phylogeny ~ birthdeath(birthrate=birth_rate, deathrate=death_rate, n=10);

// Create phylogenetic CTMC model
alignment seq ~ phyloCTMC(tree=phylogeny, substmodel=subst_model);

// Attach observed sequence data from file
seq observe from "sequences.fasta";
```

## Implementation Guidelines

### Parsing

The language is designed to be parsed using standard parser generators like ANTLR. The grammar is context-free and can be processed top-down.

### Type Checking

Implementations should perform type checking to ensure that variables are used consistently and that function arguments have the correct types.

### Execution

ModelPhy is primarily a specification language, and implementations should translate it into their native representation of phylogenetic models. The language itself does not specify how inference should be performed.

## File Format

ModelPhy files should use the extension `.mph`. They should be encoded in UTF-8.

## Future Extensions

Future versions of the language may include:
- Module system for reusing model components
- Additional phylogenetic models and priors
- Structured constraints and conditioning
- Parallel tempering and other MCMC strategies
- Explicit support for partitioned models

## ANTLR Grammar

Below is a simplified ANTLR4 grammar for ModelPhy:

```antlr
grammar ModelPhy;

program
    : statement+
    ;

statement
    : declaration
    | stochasticAssignment
    | deterministicAssignment
    | observationStatement
    ;

declaration
    : type IDENTIFIER '=' expression ';'
    ;

stochasticAssignment
    : type IDENTIFIER '~' distribution ';'
    ;

deterministicAssignment
    : type IDENTIFIER '=' functionCall ';'
    ;

observationStatement
    : IDENTIFIER 'observe' '[' keyValueList ']' ';'
    | IDENTIFIER 'observe' 'from' STRING_LITERAL ';'
    ;

keyValueList
    : keyValue (',' keyValue)*
    ;

keyValue
    : IDENTIFIER '=' expression
    ;

expression
    : literal
    | IDENTIFIER
    | functionCall
    | arrayLiteral
    ;

functionCall
    : IDENTIFIER '(' argumentList? ')'
    ;

distribution
    : IDENTIFIER '(' argumentList? ')'
    ;

argumentList
    : argument (',' argument)*
    ;

argument
    : (IDENTIFIER '=')? expression
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
    ;

literal
    : INTEGER_LITERAL
    | FLOAT_LITERAL
    | STRING_LITERAL
    | 'true'
    | 'false'
    ;

arrayLiteral
    : '[' (expression (',' expression)*)? ']'
    ;

// Lexer rules
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
```