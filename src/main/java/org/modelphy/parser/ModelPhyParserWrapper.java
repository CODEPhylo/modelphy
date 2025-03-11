package org.modelphy.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.modelphy.antlr.*;
import org.modelphy.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parser wrapper for ModelPhy files. Implements a simple API for parsing ModelPhy
 * without exposing the ANTLR implementation details.
 */
public class ModelPhyParserWrapper {
    
    private ModelPhyModel model;
    private Map<String, Variable> variables;
    private Map<String, Constraint> constraints;
    private boolean debug = false;
    
    /**
     * Create a new ModelPhy parser.
     */
    public ModelPhyParserWrapper() {
        this(false);
    }
    
    /**
     * Create a new ModelPhy parser with debug option.
     * 
     * @param debug Whether to print debug information
     */
    public ModelPhyParserWrapper(boolean debug) {
        reset();
        this.debug = debug;
    }
    
    /**
     * Reset internal state for reuse.
     */
    private void reset() {
        this.variables = new HashMap<>();
        this.constraints = new HashMap<>();
        this.model = new ModelPhyModel();
    }
    
    /**
     * Parse a ModelPhy file and build the internal model representation.
     * 
     * @param filePath Path to the .mphy file
     * @return The parsed model
     * @throws IOException If file cannot be read
     */
    public ModelPhyModel parse(Path filePath) throws IOException {
        String input = Files.readString(filePath);
        return parse(input);
    }
    
    /**
     * Parse ModelPhy content from a string.
     * 
     * @param input The ModelPhy content as a string
     * @return The parsed model
     */
    public ModelPhyModel parse(String input) {
        try {
            reset();
            
            // Set up the ANTLR lexer and parser
            CharStream charStream = CharStreams.fromString(input);
            ModelPhyLexer lexer = new ModelPhyLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            ModelPhyParser parser = new ModelPhyParser(tokens);
            
            // Add error listener for better error reporting
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, 
                                        int line, int charPositionInLine, String msg, RecognitionException e) {
                    System.err.println("line " + line + ":" + charPositionInLine + " " + msg);
                }
            });
            
            // Get the parse tree
            ModelPhyParser.ProgramContext tree = parser.program();
            
            // Create a visitor to build our model
            ModelBuilder visitor = new ModelBuilder();
            visitor.visit(tree);
            
            return model;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing ModelPhy code: " + e.getMessage(), e);
        }
    }
    
    /**
     * Custom visitor that builds a model from the parse tree.
     */
    private class ModelBuilder extends ModelPhyBaseVisitor<Object> {
        
        @Override
        public Object visitProgram(ModelPhyParser.ProgramContext ctx) {
            if (debug) System.out.println("Visiting program");
            
            // Visit all statements in the program
            for (ModelPhyParser.StatementContext statement : ctx.statement()) {
                visit(statement);
            }
            return null;
        }
        
        @Override
        public Object visitDeclaration(ModelPhyParser.DeclarationContext ctx) {
            if (debug) System.out.println("Visiting declaration: " + ctx.getText());
            
            String type = ctx.type().getText();
            String id = ctx.identifier().getText();
            
            Variable var = new Variable(id, type);
            
            // If there's an initialization expression
            if (ctx.expression() != null) {
                Object value = visit(ctx.expression());
                var.setValue(value);
            }
            
            variables.put(id, var);
            model.addVariable(var);
            
            return null;
        }
        
        @Override
        public Object visitStochasticAssignment(ModelPhyParser.StochasticAssignmentContext ctx) {
            if (debug) System.out.println("Visiting stochastic assignment: " + ctx.getText());
            
            // Handle both variable ~ distribution and functionCall ~ distribution
            if (ctx.identifier() != null) {
                String type = ctx.type().getText();
                String id = ctx.identifier().getText();
                Distribution dist = (Distribution) visit(ctx.distribution());
                
                StochasticVariable var = new StochasticVariable(id, type, dist);
                variables.put(id, var);
                model.addStochasticVariable(var);
            } else {
                // Handle constraint ~ distribution
                FunctionCall func = (FunctionCall) visit(ctx.functionCall());
                Distribution dist = (Distribution) visit(ctx.distribution());
                
                Constraint constraint = new Constraint(func, dist);
                constraints.put(func.getName(), constraint);
                model.addConstraint(constraint);
            }
            
            return null;
        }
        
        @Override
        public Object visitDeterministicAssignment(ModelPhyParser.DeterministicAssignmentContext ctx) {
            if (debug) System.out.println("Visiting deterministic assignment: " + ctx.getText());
            
            String type = ctx.type().getText();
            String id = ctx.identifier().getText();
            Object expr = visit(ctx.expression());
            
            DeterministicVariable var;
            if (expr instanceof FunctionCall) {
                var = new DeterministicVariable(id, type, (FunctionCall) expr);
            } else {
                var = new DeterministicVariable(id, type, expr);
            }
            
            variables.put(id, var);
            model.addDeterministicVariable(var);
            
            return null;
        }
        
        @Override
        public Object visitObservationStatement(ModelPhyParser.ObservationStatementContext ctx) {
            if (debug) System.out.println("Visiting observation statement: " + ctx.getText());
            
            String id = ctx.identifier().getText();
            Variable var = variables.get(id);
            
            if (var == null) {
                // Handle error - variable not found
                throw new RuntimeException("Variable not found: " + id);
            }
            
            if (ctx.STRING_LITERAL() != null) {
                // Observation from file
                String filename = ctx.STRING_LITERAL().getText();
                // Remove quotes
                filename = filename.substring(1, filename.length() - 1);
                
                Observation obs = new Observation(id, ObservationType.FILE, filename);
                var.setObservation(obs);
            } else {
                // Observation with key-value list
                Observation obs = new Observation(id, ObservationType.INLINE);
                
                // Process key-value pairs
                for (ModelPhyParser.KeyValueContext keyValue : ctx.keyValueList().keyValue()) {
                    try {
                        String key = keyValue.identifier().getText();
                        if (debug) System.out.println("Processing key-value: " + key + " with expression: " + 
                                                     keyValue.value.getText());
                        
                        Object value = visit(keyValue.value);
                        if (debug) System.out.println("  Value type: " + 
                                                     (value != null ? value.getClass().getName() : "null"));
                        
                        // Handle string values directly
                        if (value instanceof String) {
                            obs.addKeyValue(key, value);
                        } 
                        // Handle function calls (like sequence)
                        else if (value instanceof FunctionCall) {
                            FunctionCall func = (FunctionCall) value;
                            if (func.getName().equals("sequence")) {
                                // Extract the string value from the sequence function
                                for (Argument arg : func.getArguments()) {
                                    if ("str".equals(arg.getName()) && arg.getValue() instanceof String) {
                                        obs.addKeyValue(key, arg.getValue());
                                        break;
                                    }
                                }
                            } else {
                                obs.addKeyValue(key, value);
                            }
                        }
                        // Handle other types
                        else {
                            obs.addKeyValue(key, value);
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing key-value: " + keyValue.getText());
                        e.printStackTrace();
                        // Continue processing other key-values
                    }
                }
                
                var.setObservation(obs);
            }
            
            return null;
        }
        
        @Override
        public Object visitConstraintStatement(ModelPhyParser.ConstraintStatementContext ctx) {
            if (debug) System.out.println("Visiting constraint statement: " + ctx.getText());
            
            String id = ctx.identifier().getText();
            FunctionCall func = (FunctionCall) visit(ctx.functionCall());
            
            Constraint constraint = new Constraint(id, func);
            constraints.put(id, constraint);
            model.addConstraint(constraint);
            
            return null;
        }
        
        @Override
        public Object visitDistribution(ModelPhyParser.DistributionContext ctx) {
            if (debug) System.out.println("Visiting distribution: " + ctx.getText());
            
            String name = ctx.identifier().getText();
            List<Argument> args = new ArrayList<>();
            
            if (ctx.namedArgumentList() != null) {
                for (ModelPhyParser.NamedArgumentContext arg : ctx.namedArgumentList().namedArgument()) {
                    String paramName = arg.name.getText();
                    Object value = visit(arg.value);
                    args.add(new Argument(paramName, value));
                }
            }
            
            return new Distribution(name, args);
        }
        
        @Override
        public Object visitFunctionCall(ModelPhyParser.FunctionCallContext ctx) {
            if (debug) System.out.println("Visiting function call: " + ctx.getText());
            
            String name = ctx.identifier().getText();
            List<Argument> args = new ArrayList<>();
            
            if (ctx.namedArgumentList() != null) {
                for (ModelPhyParser.NamedArgumentContext arg : ctx.namedArgumentList().namedArgument()) {
                    String paramName = arg.name.getText();
                    Object value = visit(arg.value);
                    args.add(new Argument(paramName, value));
                }
            }
            
            return new FunctionCall(name, args);
        }
        
        @Override
        public Object visitLiteralExpr(ModelPhyParser.LiteralExprContext ctx) {
            if (debug) System.out.println("Visiting literal expression: " + ctx.getText());
            return visit(ctx.literal());
        }
        
        @Override
        public Object visitIdentifierExpr(ModelPhyParser.IdentifierExprContext ctx) {
            if (debug) System.out.println("Visiting identifier expression: " + ctx.getText());
            return new VariableReference(ctx.identifier().getText());
        }
        
        @Override
        public Object visitFunctionCallExpr(ModelPhyParser.FunctionCallExprContext ctx) {
            if (debug) System.out.println("Visiting function call expression: " + ctx.getText());
            return visit(ctx.functionCall());
        }
        
        @Override
        public Object visitArrayExpr(ModelPhyParser.ArrayExprContext ctx) {
            if (debug) System.out.println("Visiting array expression: " + ctx.getText());
            return visit(ctx.arrayLiteral());
        }
        
        @Override
        public Object visitParenExpr(ModelPhyParser.ParenExprContext ctx) {
            if (debug) System.out.println("Visiting parenthesized expression: " + ctx.getText());
            return visit(ctx.expression());
        }
        
        @Override
        public Object visitArrayLiteral(ModelPhyParser.ArrayLiteralContext ctx) {
            if (debug) System.out.println("Visiting array literal: " + ctx.getText());
            
            List<Object> elements = new ArrayList<>();
            
            if (ctx.expression() != null) {
                for (ModelPhyParser.ExpressionContext expr : ctx.expression()) {
                    elements.add(visit(expr));
                }
            }
            
            return new ArrayValue(elements);
        }
        
        @Override
        public Object visitLiteral(ModelPhyParser.LiteralContext ctx) {
            if (debug) System.out.println("Visiting literal: " + ctx.getText());
            
            if (ctx.INTEGER_LITERAL() != null) {
                return Integer.parseInt(ctx.INTEGER_LITERAL().getText());
            } else if (ctx.FLOAT_LITERAL() != null) {
                return Double.parseDouble(ctx.FLOAT_LITERAL().getText());
            } else if (ctx.STRING_LITERAL() != null) {
                String text = ctx.STRING_LITERAL().getText();
                // Remove quotes
                return text.substring(1, text.length() - 1);
            } else if (ctx.BOOLEAN_LITERAL() != null) {
                return Boolean.parseBoolean(ctx.BOOLEAN_LITERAL().getText());
            }
            
            return null;
        }
    }
}