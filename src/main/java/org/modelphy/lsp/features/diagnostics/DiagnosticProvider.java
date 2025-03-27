package org.modelphy.lsp.features.diagnostics;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.modelphy.antlr.ModelPhyLexer;
import org.modelphy.antlr.ModelPhyParser;
import org.modelphy.model.ModelPhyModel;
import org.modelphy.parser.ModelPhyParserWrapper;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticProvider {
    public List<Diagnostic> provideDiagnostics(String content) {
        List<Diagnostic> diagnostics = new ArrayList<>();
        
        // First: collect syntax errors
        collectSyntaxErrors(content, diagnostics);
        
        // Second: collect semantic errors
        if (diagnostics.isEmpty()) {
            collectSemanticErrors(content, diagnostics);
        }
        
        return diagnostics;
    }
    
    private void collectSyntaxErrors(String content, List<Diagnostic> diagnostics) {
        CharStream input = CharStreams.fromString(content);
        ModelPhyLexer lexer = new ModelPhyLexer(input);
        
        // Collect lexer errors
        BaseErrorListener lexerErrorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, 
                                   int line, int charPositionInLine, String msg, RecognitionException e) {
                Diagnostic diagnostic = createDiagnostic(
                    line - 1, charPositionInLine, line - 1, charPositionInLine + 1,
                    "Lexer error: " + msg, DiagnosticSeverity.Error
                );
                diagnostics.add(diagnostic);
            }
        };
        lexer.removeErrorListeners();
        lexer.addErrorListener(lexerErrorListener);
        
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ModelPhyParser parser = new ModelPhyParser(tokens);
        
        // Collect parser errors
        BaseErrorListener parserErrorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, 
                                   int line, int charPositionInLine, String msg, RecognitionException e) {
                Diagnostic diagnostic = createDiagnostic(
                    line - 1, charPositionInLine, line - 1, charPositionInLine + 1,
                    "Syntax error: " + msg, DiagnosticSeverity.Error
                );
                diagnostics.add(diagnostic);
            }
        };
        parser.removeErrorListeners();
        parser.addErrorListener(parserErrorListener);
        
        // Parse the content to trigger any syntax errors
        parser.program();
    }
    
    private void collectSemanticErrors(String content, List<Diagnostic> diagnostics) {
        // This is a placeholder for future semantic validation
        // For now, we'll do minimal validation to get things working
        try {
            // Basic model parsing - just to check if it's valid
            ModelPhyModel model = ModelPhyParserWrapper.parseModel(content);
            
            // In the future, we'll add proper semantic validation here
            // For example, checking for undefined variables, type mismatches, etc.
            
        } catch (Exception e) {
            // If parsing fails, report it as a diagnostic
            Diagnostic diagnostic = createDiagnostic(
                0, 0, 0, 1,
                "Error validating model: " + e.getMessage(), 
                DiagnosticSeverity.Error
            );
            diagnostics.add(diagnostic);
        }
    }    
    
    private Diagnostic createDiagnostic(int startLine, int startChar, int endLine, int endChar, 
                                       String message, DiagnosticSeverity severity) {
        Range range = new Range(
            new Position(startLine, startChar),
            new Position(endLine, endChar)
        );
        Diagnostic diagnostic = new Diagnostic(range, message);
        diagnostic.setSeverity(severity);
        return diagnostic;
    }
}